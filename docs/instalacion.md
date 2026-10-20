# Guía de instalación del entorno (Windows) — Minikube, kubectl y Docker

Esta guía deja a todo el equipo con el mismo entorno para correr el sistema de reservas de laboratorios en Kubernetes. Está escrita para **Windows 10/11 con PowerShell**; al final hay notas para Linux y macOS. Incluye los errores reales que aparecieron al montar el entorno y cómo resolverlos.

Las pruebas del despliegue y el guion de la demostración están en [`pruebas-kubernetes.md`](pruebas-kubernetes.md).

### Estructura del repositorio

```
<raíz del repo>/
├── ms-usuarios/        # Spring Boot (C)
├── ms-laboratorios/    # Spring Boot (C)
├── ms-reservas/        # Spring Boot (A)
├── frontend/           # Angular (D)
├── k8s/                # manifiestos (B); k8s/pendientes/ = componentes sin imagen todavía
├── docs/               # guía, pruebas y scripts de arranque
└──build-all.ps1       # construye las imágenes (Windows)
```

## 1. Requisitos

| Requisito         | Detalle                                                                            |
| ----------------- | ---------------------------------------------------------------------------------- |
| Computadora       | 16 GB de RAM recomendados                                                          |
| Procesador        | Intel/AMD (la imagen de SQL Server **no funciona** en Mac con chip M1 o posterior) |
| Sistema operativo | Windows 10/11 con WSL2 y virtualización habilitada                                 |
| Docker            | Docker Desktop (motor WSL2)                                                        |
| Git               | Cuenta de GitHub con acceso al repo del equipo (ver sección 11)                    |

Para comprobar la virtualización: Administrador de tareas, pestaña **Rendimiento**, **CPU**, debe decir **Virtualización: Habilitada**. Si está deshabilitada, se activa en la BIOS (SVM en AMD, VT-x en Intel).

## 2. Versiones acordadas

| Herramienta             | Versión                                                       |
| ----------------------- | ------------------------------------------------------------- |
| Minikube                | v1.39.0                                                       |
| kubectl                 | v1.34.1                                                       |
| Kubernetes (clúster)    | v1.34.1                                                       |
| Runtime de contenedores | `docker` (obligatorio)                                        |
| Perfil de Minikube      | `reservas-lab`                                                |
| Java                    | A definir por el equipo (el plan dice 17 o 21; ver sección 9) |

> **Importante:** todos deben crear el clúster con `--container-runtime=docker`. Con `containerd` el comando `docker-env` no funciona en Windows y no se pueden construir las imágenes dentro de Minikube. El script `start-minikube.ps1` ya lo hace.

## 3. Instalación en Windows

### 3.1 Docker Desktop

1. Instalar [Docker Desktop](https://www.docker.com/products/docker-desktop/) y aceptar la activación de WSL2 si la ofrece.
2. Abrirlo y esperar a que abajo a la izquierda diga **Engine running**.
3. Verificar en PowerShell:
   ```powershell
   docker --version
   docker info
   ```
   La sección **Server** de `docker info` debe mostrar datos. Si muestra `failed to connect to the docker API at npipe:////./pipe/dockerDesktopLinuxEngine`, el motor **no está corriendo**: abrir Docker Desktop y esperar. Si no arranca:
   ```powershell
   wsl --status
   wsl --update
   wsl --shutdown
   ```
   y abrir Docker Desktop de nuevo. Si sigue sin arrancar, reiniciar la computadora.

### 3.2 Minikube y kubectl

```powershell
winget install Kubernetes.kubectl
winget install Kubernetes.minikube --version 1.39.0
```

**Cerrar todas las ventanas de PowerShell (y la terminal de VS Code) y abrir una nueva**, porque si no, la terminal no reconoce los comandos. Si `winget` responde que ya hay un paquete instalado y no hay actualizaciones, es normal: ya está instalado.

Verificar:

```powershell
minikube version
kubectl version --client
```

Cuidado con escribir mal el nombre del comando (por ejemplo, `minikuber`): PowerShell dirá que no lo reconoce.

## 4. Memoria para Docker (WSL2)

Minikube no puede pedir más memoria de la que tiene Docker Desktop. Si aparece:

```
Docker Desktop has only 7577MB memory but you specified 8192MB
```

hay dos soluciones:

- **Rápida:** pedir menos memoria (el script usa `6500mb`, que alcanza para SQL Server y deja margen para otros contenedores).
- **Recomendada:** subir el límite de WSL. Crear o editar `C:\Users\<usuario>\.wslconfig`:
  ```
  [wsl2]
  memory=10GB
  processors=4
  ```
  Luego:
  ```powershell
  wsl --shutdown
  ```
  Abrir Docker Desktop y esperar a **Engine running**. Con eso se puede usar `--memory=8g` en el script.

Los contenedores que ya tengas corriendo en Docker (`docker ps`) también consumen esa memoria. Detener los que no se necesiten con `docker stop <nombre>`. SQL Server necesita al menos 2 GB de RAM para sí solo.

## 5. Permitir la ejecución de scripts de PowerShell

Por defecto Windows bloquea los `.ps1` y aparece:

```
la ejecución de scripts está deshabilitada en este sistema
```

Habilitarlos **solo para la ventana actual** (no cambia nada permanente):

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
```

Hay que repetirlo en cada ventana nueva. Alternativa en una sola línea:

```powershell
powershell -ExecutionPolicy Bypass -File .\docs\start-minikube.ps1
```

## 6. Iniciar el clúster

Ejecutar el script **desde la raíz del repo** (donde están `docs/`, `src/` o las carpetas de los microservicios):

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\docs\start-minikube.ps1
```

Si la terminal ya está dentro de `docs/`, la ruta correcta es `.\start-minikube.ps1` (con `.\docs\...` falla con "no se reconoce como nombre de un cmdlet"). Y `cd` sirve solo para carpetas, no para archivos.

El script crea el perfil `reservas-lab` con runtime Docker, Kubernetes v1.34.1 y 4 CPUs, y activa los addons `ingress` y `metrics-server`. La primera vez tarda varios minutos porque descarga imágenes.

Verificar:

```powershell
minikube status -p reservas-lab
kubectl get nodes
kubectl get pods -n ingress-nginx
kubectl top nodes
```

Resultado esperado:

- El nodo `reservas-lab` en `Ready`.
- `ingress-nginx-controller` en `1/1 Running`. Los pods `ingress-nginx-admission-create` y `-patch` en `Completed` son normales: son tareas que corren una vez y terminan.
- `kubectl top nodes` puede decir `error: Metrics API not available` durante uno o dos minutos después de activar metrics-server. Esperar y repetir. No bloquea nada del plan.

### Cambiar memoria o runtime de un clúster existente

Minikube no lo permite (`You cannot change the memory size for an existing minikube cluster`). Hay que borrarlo y recrearlo:

```powershell
minikube delete -p reservas-lab
.\docs\start-minikube.ps1
```

Si alguien creó antes un clúster con el perfil por defecto (`minikube`) y quedó con `containerd`, puede borrarlo con `minikube delete`.

## 7. Dos kubectl en el mismo equipo

Docker Desktop trae su propio `kubectl` (por ejemplo v1.36.1) y puede tapar al v1.34.1 instalado con winget. kubectl solo garantiza compatibilidad con una versión menor de diferencia respecto al clúster, y Minikube avisa:

```
kubectl.exe is version 1.36.1, which may have incompatibilities with Kubernetes 1.34.1
```

Ver cuál se está usando:

```powershell
Get-Command kubectl -All
```

Soluciones:

- **Fácil:** usar `minikube kubectl --` en lugar de `kubectl` (por ejemplo, `minikube kubectl -- get pods -A`).
- **Definitiva:** en las variables de entorno del sistema, poner la carpeta del kubectl de winget **por encima** de la de Docker en el `Path`.

## 8. Construir imágenes dentro de Minikube

Las imágenes se construyen directamente en el Docker interno de Minikube, así los pods las encuentran sin subirlas a ningún registro.

```powershell
& minikube -p reservas-lab docker-env --shell powershell | Invoke-Expression
docker ps
```

Si `docker ps` muestra contenedores `k8s_...`, esa terminal está apuntando al Docker de Minikube.

Notas:

- Vale **solo para esa ventana**: hay que repetirlo en cada ventana nueva.
- Para volver al Docker normal:
  ```powershell
  & minikube docker-env --shell powershell --unset | Invoke-Expression
  ```
  Los errores `Remove-Item : No se encuentra la ruta de acceso 'Env:\SSH_AUTH_SOCK'` (y `SSH_AGENT_PID`) son inofensivos: esas variables no existían.
- Si tras un `docker-env` fallido aparece `false : El término 'false' no se reconoce`, es solo ruido de PowerShell. El error real está en la línea anterior (el perfil no existe o el clúster usa containerd).
- Si el log de `minikube start` muestra `MINIKUBE_ACTIVE_DOCKERD=minikube`, es una variable que quedó de un clúster anterior; correr el `--unset` de arriba.
- Error `starting an SSH agent on Windows is not yet supported`: el clúster usa `containerd`. Recrearlo con `--container-runtime=docker`.
- Nombre de las imágenes: `reservas-lab/<componente>:v1`.
- En todos los Deployments usar `imagePullPolicy: IfNotPresent`; si no, Kubernetes intenta descargar la imagen de internet y falla.

## 9. Dockerfile de los microservicios

Va en la carpeta que contiene el `pom.xml` del microservicio. Si se corre `docker build` en otra carpeta falla con:

```
failed to read dockerfile: open Dockerfile: no such file or directory
```

Primero revisar la versión de Java del proyecto:

```powershell
findstr "java.version" pom.xml
```

`Dockerfile` (ejemplo con Java 25; cambiar el número por el de cada `pom.xml`):

```dockerfile
# Etapa 1: compilar con Maven
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package -DskipTests

# Etapa 2: ejecutar solo con JRE
FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd -r -u 1001 appuser
COPY --from=build /app/target/*.jar app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

`.dockerignore` (misma carpeta):

```
target
.git
.idea
.vscode
*.iml
```

Construir (con el `docker-env` de la sección 8 activo en esa ventana):

```powershell
docker build -t reservas-lab/ms-reservas:v1 .
docker images | findstr reservas-lab
```

La primera vez tarda unos dos minutos porque descarga Maven y las dependencias.

Observaciones:

- **Versión de Java:** el plan dice 17 o 21, pero un proyecto puede estar en una versión mayor (por ejemplo 25). La imagen lleva su propio JRE, así que funciona igual, pero el equipo debe acordar **una sola versión** y que coincida con el `<java.version>` del `pom.xml` y con las dos etapas del Dockerfile. Spring Boot necesita una versión reciente para soportar Java 25; si el `pom.xml` de los demás tiene un Boot más viejo, no podrán usarla.
- **Puerto:** todos los microservicios escuchan en el 8080 dentro del contenedor.
- **No correr la imagen suelta con `docker run`:** sin SQL Server el servicio se cae al arrancar. La prueba real es desplegarla en el clúster.
- `-DskipTests` evita que el build intente conectarse a la base de datos durante las pruebas.

### Construir todas las imágenes

`build-all.ps1` en la raíz del repo (se salta las carpetas sin Dockerfile):

```powershell
$PERFIL = "reservas-lab"
& minikube -p $PERFIL docker-env --shell powershell | Invoke-Expression

foreach ($c in "ms-usuarios", "ms-laboratorios", "ms-reservas", "frontend") {
    if (Test-Path "$c\Dockerfile") {
        Write-Host "Construyendo $c ..." -ForegroundColor Cyan
        docker build -t "reservas-lab/${c}:v1" ".\$c"
        if ($LASTEXITCODE -ne 0) { Write-Host "Falló $c" -ForegroundColor Red; exit 1 }
    } else {
        Write-Host "Omitido $c (sin Dockerfile)" -ForegroundColor Yellow
    }
}
docker images | findstr reservas-lab
```

Los nombres de carpeta deben coincidir con los del repo, porque el script los usa como nombre de imagen.

## 9.1 Desplegar y acceder por el Ingress

```powershell
kubectl apply -f k8s/
kubectl get pods -n reservas-lab -w
```

`kubectl apply -f k8s/` aplica en orden alfabético y no entra a subcarpetas. Los manifiestos de `k8s/pendientes/` se mueven a `k8s/` cuando su imagen exista.

| Ruta                | Service                |
| ------------------- | ---------------------- |
| `/api/usuarios`     | `ms-usuarios:8080`     |
| `/api/laboratorios` | `ms-laboratorios:8080` |
| `/api/reservas`     | `ms-reservas:8080`     |
| `/`                 | `frontend:80`          |

En otra terminal, que debe quedar abierta (puede pedir permisos de administrador):

```powershell
minikube tunnel -p reservas-lab
```

La aplicación responde en `http://127.0.0.1` (por ejemplo, `Invoke-RestMethod http://127.0.0.1/api/reservas/info`). Las rutas de componentes no desplegados responden 503.

## 10. Comandos útiles

```powershell
minikube stop -p reservas-lab        # detener el clúster
minikube start -p reservas-lab       # volver a iniciarlo
minikube delete -p reservas-lab      # borrarlo por completo
kubectl get pods -n reservas-lab     # ver los pods de la aplicación
```

## 11. Resumen de problemas comunes

| Síntoma                                                              | Causa y solución                                                                                   |
| -------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------- |
| `minikube` / `kubectl` no se reconoce                                | Cerrar y abrir la terminal; revisar que el nombre esté bien escrito; revisar el `Path`.            |
| `failed to connect to the docker API`                                | Docker Desktop no está corriendo. Abrirlo y esperar a **Engine running** (sección 3.1).            |
| `Docker Desktop has only XXXX MB memory`                             | Subir el límite en `.wslconfig` o bajar `--memory` (sección 4).                                    |
| `You cannot change the memory size for an existing minikube cluster` | El clúster ya existe: `minikube delete -p reservas-lab` y recrearlo.                               |
| La ejecución de scripts está deshabilitada                           | `Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass` (sección 5).                          |
| `.\docs\start-minikube.ps1` no se reconoce                           | La terminal ya está dentro de `docs/`; usar `.\start-minikube.ps1` o volver a la raíz con `cd ..`. |
| `docker-env` falla con `SSH_AGENT_START`                             | El clúster usa `containerd`. Recrearlo con `--container-runtime=docker`.                           |
| `false : El término 'false' no se reconoce`                          | Ruido de PowerShell tras un `docker-env` fallido; revisar el error anterior.                       |
| `Remove-Item ... Env:\SSH_AUTH_SOCK` al usar `--unset`               | Inofensivo: la variable no existía.                                                                |
| `kubectl is version 1.36.1 ... incompatibilities`                    | Hay dos kubectl; usar `minikube kubectl --` o corregir el `Path` (sección 7).                      |
| `Metrics API not available`                                          | metrics-server tarda uno o dos minutos; esperar y repetir `kubectl top nodes`.                     |
| `open Dockerfile: no such file or directory`                         | `docker build` se corrió fuera de la carpeta del Dockerfile y el `pom.xml`.                        |
| Pods en `Pending` o reinicios por falta de memoria                   | Aumentar la memoria de Docker/WSL y recrear el clúster.                                            |
| `403` al hacer `git push`                                            | Sin permisos de escritura; ver sección 11.                                                         |
