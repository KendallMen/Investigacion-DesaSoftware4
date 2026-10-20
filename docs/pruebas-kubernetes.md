# Pruebas del despliegue en Kubernetes

Comandos en PowerShell, desde la raíz del repo, con el clúster `reservas-lab` corriendo.
Cada prueba indica qué captura guardar para el informe.

## 0. Preparación

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\docs\start-minikube.ps1
.\build-all.ps1
kubectl apply -f k8s/
kubectl get pods -n reservas-lab -w
```

Esperar a que todo esté en `1/1 Running` (`crear-bases` queda en `Completed`) y salir con Ctrl+C.
Los manifiestos de `k8s/pendientes/` no se aplican: cuando C o D tengan su imagen, mover el archivo a `k8s/` y reaplicar.

En **otra terminal**, que tiene que quedar abierta (puede pedir permisos de administrador):

```powershell
minikube tunnel -p reservas-lab
```

Comprobar:

```powershell
Invoke-RestMethod http://127.0.0.1/api/reservas/info
Invoke-RestMethod http://127.0.0.1/api/reservas
```

Las rutas de servicios todavía no desplegados responden 503; es normal.

## 1. Despliegue

```powershell
kubectl get pods -n reservas-lab -o wide
kubectl get deploy,svc,ingress,pvc -n reservas-lab
```

**Captura:** `kubectl get pods` con todo en Running.

## 2. Balanceo de carga

```powershell
1..10 | ForEach-Object { (Invoke-RestMethod http://127.0.0.1/api/reservas/info).pod }
```

Se alternan los dos Pods `ms-reservas-...`. Con `kubectl port-forward` no se ve, porque se conecta a un solo Pod.

**Captura:** la salida con los dos nombres.

## 3. Autorrecuperación

```powershell
kubectl get pods -n reservas-lab -l app=ms-reservas
kubectl delete pod <nombre-de-un-pod-ms-reservas> -n reservas-lab
kubectl get pods -n reservas-lab -l app=ms-reservas -w
```

Kubernetes crea un Pod nuevo para volver a 2 réplicas; el otro sigue atendiendo.

**Captura:** el Pod en Terminating y el nuevo pasando a Running.

## 4. Resiliencia (Circuit Breaker)

Necesita `ms-laboratorios` desplegado.

```powershell
kubectl scale deployment ms-laboratorios -n reservas-lab --replicas=0

$body = @{ usuarioId = 1; laboratorioId = 1; fecha = "2026-11-10"; horaInicio = "08:00:00"; horaFin = "09:00:00" } | ConvertTo-Json
try { Invoke-RestMethod -Method Post -Uri http://127.0.0.1/api/reservas -ContentType "application/json" -Body $body }
catch { $_.ErrorDetails.Message }

kubectl scale deployment ms-laboratorios -n reservas-lab --replicas=2
```

Esperado: 503 con `Servicio de laboratorios no disponible, intente más tarde`.

**Captura:** el mensaje del fallback.

## 5. Configuración en vivo (ConfigMap)

```powershell
kubectl create configmap ms-reservas -n reservas-lab --from-literal=reservas.max-horas=1 --dry-run=client -o yaml | kubectl apply -f -
```

Esperar unos 10 segundos y crear una reserva de 2 horas: debe rechazarla con `La reserva no puede superar las 1 horas`, sin que cambie el contador RESTARTS de los Pods.

Volver al valor original:

```powershell
kubectl apply -f k8s/05msreservasconfig.yaml
```

Si no se aplica, buscar errores de permisos:

```powershell
kubectl logs -n reservas-lab deploy/ms-reservas | Select-String -Pattern "forbidden|reload|ConfigMap"
```

**Captura:** el error con el nuevo límite y `kubectl get pods` sin reinicios.

## 6. Persistencia

```powershell
kubectl exec -n reservas-lab deploy/sqlserver -- /opt/mssql-tools18/bin/sqlcmd -C -S localhost -U sa -P "Reservas#2026Lab" -d reservas_db -Q "SELECT COUNT(*) AS reservas FROM reservas"
kubectl delete pod -n reservas-lab -l app=sqlserver
kubectl get pods -n reservas-lab -l app=sqlserver -w
```

Cuando el Pod nuevo esté en `1/1 Running`, repetir la consulta: el número tiene que ser el mismo.
Los comandos `sqlcmd` van en una sola línea. Mientras SQL Server vuelve, `ms-reservas` puede responder con error; es esperable.

**Captura:** la consulta antes y después.

## 7. Rolling Update

```powershell
.\build-all.ps1 -Version v2 -Solo ms-reservas
```

En **otra terminal**, un bucle que llama a la API todo el tiempo:

```powershell
while ($true) {
  try { "{0}  {1}" -f (Get-Date -Format HH:mm:ss), (Invoke-RestMethod http://127.0.0.1/api/reservas/info).pod }
  catch { "{0}  ERROR" -f (Get-Date -Format HH:mm:ss) }
  Start-Sleep -Milliseconds 500
}
```

Aplicar la actualización:

```powershell
kubectl set image deployment/ms-reservas ms-reservas=reservas-lab/ms-reservas:v2 -n reservas-lab
kubectl rollout status deployment/ms-reservas -n reservas-lab
```

En el bucle, los nombres de Pod cambian sin líneas de ERROR.
Para volver a v1: `kubectl rollout undo deployment/ms-reservas -n reservas-lab`.
`kubectl set image` no cambia el YAML: un `kubectl apply -f k8s/` posterior vuelve a v1.

**Captura:** el bucle sin errores y el rollout terminado.

## 8. Consumo de recursos

```powershell
kubectl top nodes
kubectl top pods -n reservas-lab
```

Con 6500 MB todo el sistema queda justo. Si hay Pods en Pending u OOMKilled, bajar réplicas de lo que no se esté probando:
`kubectl scale deployment ms-usuarios -n reservas-lab --replicas=1`.

**Captura:** `kubectl top pods`.

## Diagnóstico rápido

| Estado                                   | Qué revisar                                                                           |
| ---------------------------------------- | ------------------------------------------------------------------------------------- |
| `Pending`                                | `kubectl describe pod ...`, sección Events: PVC inexistente o memoria insuficiente.   |
| `CreateContainerConfigError`             | Falta el Secret o una clave (revisar que el YAML no pese 0 bytes).                    |
| `ErrImagePull` / `ImagePullBackOff`      | Falta la imagen en Minikube: `.\build-all.ps1`.                                       |
| `CrashLoopBackOff` o `0/1` con reinicios | `kubectl logs -n reservas-lab deploy/<nombre> --previous`.                            |
| 503 por el Ingress                       | El Service no existe o no tiene Pods listos: `kubectl get endpoints -n reservas-lab`. |
| 404 por el Ingress                       | Falta aplicar el Ingress o falta `minikube tunnel`.                                   |
