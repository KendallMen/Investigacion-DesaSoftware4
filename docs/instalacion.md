# Guía de instalación del entorno (Minikube + kubectl)

Esta guía deja a todo el equipo con el mismo entorno para correr el sistema de reservas de laboratorios en Kubernetes.

## 1. Requisitos

| Requisito         | Detalle                                                                            |
| ----------------- | ---------------------------------------------------------------------------------- |
| Computadora       | 16 GB de RAM recomendados                                                          |
| Procesador        | Intel/AMD (la imagen de SQL Server **no funciona** en Mac con chip M1 o posterior) |
| Sistema operativo | Windows 10/11 (con WSL2), Linux o macOS Intel                                      |
| Docker            | Docker Desktop (Windows/macOS) o Docker Engine (Linux)                             |

## 2. Versiones acordadas

| Herramienta             | Versión                |
| ----------------------- | ---------------------- |
| Minikube                | v1.39.0                |
| kubectl                 | v1.34.1                |
| Kubernetes (clúster)    | v1.34.1                |
| Runtime de contenedores | `docker` (obligatorio) |
| Perfil de Minikube      | `reservas-lab`         |

> Importante: todos deben usar `--container-runtime=docker`. Con `containerd` el comando `docker-env` no funciona en Windows y no se pueden construir las imágenes dentro de Minikube.

## 3. Instalación

### 3.1 Docker

- **Windows / macOS:** instalar [Docker Desktop](https://www.docker.com/products/docker-desktop/). En Windows se necesita WSL2 (el instalador lo ofrece activar).
- **Linux:** instalar Docker Engine y agregar el usuario al grupo `docker`:
  ```bash
  sudo usermod -aG docker $USER
  ```
  Cerrar sesión y volver a entrar.

Verificar:

```bash
docker --version
docker info
```

La sección **Server** de `docker info` debe mostrar datos. Si muestra un error de conexión, Docker Desktop no está corriendo.

### 3.2 Minikube y kubectl

**Windows (PowerShell):**

```powershell
winget install Kubernetes.kubectl
winget install Kubernetes.minikube --version 1.39.0
```

Cerrar y volver a abrir la terminal.

**macOS (Homebrew):**

```bash
brew install kubectl minikube
```

**Linux:**

```bash
curl -LO "https://dl.k8s.io/release/v1.34.1/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

curl -LO https://github.com/kubernetes/minikube/releases/download/v1.39.0/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube
```

Verificar:

```bash
minikube version
kubectl version --client
```

## 4. Memoria para Docker en Windows

Docker Desktop con WSL2 puede tener un límite de memoria menor al que pide Minikube. Si al iniciar aparece `Docker Desktop has only XXXX MB memory`, crear o editar `C:\Users\<usuario>\.wslconfig`:

```
[wsl2]
memory=10GB
processors=4
```

Luego:

```powershell
wsl --shutdown
```

y abrir Docker Desktop de nuevo, esperando a que diga **Engine running**.

SQL Server necesita al menos 2 GB de RAM para sí solo.

## 5. Iniciar el clúster

Usar el script del repo, para que todos queden con la misma configuración:

- Windows: `.\docs\start-minikube.ps1`
- Linux / macOS: `bash docs/start-minikube.sh`

El script inicia Minikube con el perfil `reservas-lab`, runtime Docker, Kubernetes v1.34.1, 4 CPUs y memoria acordada, y activa los addons `ingress` y `metrics-server`.

Verificar:

```bash
minikube status -p reservas-lab
kubectl get nodes
kubectl get pods -n ingress-nginx
kubectl top nodes
```

El nodo debe estar en `Ready` y el pod `ingress-nginx-controller` en `Running`. `kubectl top nodes` puede tardar uno o dos minutos en responder después de activar metrics-server.

## 6. Construir imágenes dentro de Minikube

Las imágenes se construyen directamente en el Docker interno de Minikube, así los pods las encuentran sin subirlas a un registro.

**Windows (PowerShell):**

```powershell
& minikube -p reservas-lab docker-env --shell powershell | Invoke-Expression
```

**Linux / macOS:**

```bash
eval $(minikube -p reservas-lab docker-env)
```

Comprobar con `docker ps`: deben verse contenedores `k8s_...`.

Notas:

- El comando aplica solo a **esa ventana de terminal**. Hay que repetirlo en cada ventana nueva.
- Nombre de las imágenes: `reservas-lab/<componente>:v1`.
- En todos los Deployments usar `imagePullPolicy: IfNotPresent`; si no, Kubernetes intenta descargar la imagen de internet y falla.
- Para volver al Docker normal en Windows:
  ```powershell
  & minikube docker-env --shell powershell --unset | Invoke-Expression
  ```

## 7. Acceder a la aplicación por el Ingress

Con el driver Docker en Windows y macOS, el Ingress no queda accesible directamente. Dejar corriendo en **otra terminal**:

```bash
minikube tunnel -p reservas-lab
```

Mientras esté abierto, la aplicación responde en `http://localhost`.

## 8. Comandos útiles

```bash
minikube stop -p reservas-lab      # detener el clúster
minikube start -p reservas-lab     # volver a iniciarlo
minikube delete -p reservas-lab    # borrarlo por completo
kubectl get pods -n reservas-lab   # ver los pods de la aplicación
```

Si hay que cambiar la memoria o el runtime de un clúster existente, Minikube no lo permite: hay que borrarlo con `minikube delete -p reservas-lab` y recrearlo con el script.

## 9. Problemas comunes

| Síntoma                                                              | Causa y solución                                                                                                                     |
| -------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------ |
| `failed to connect to the docker API`                                | Docker Desktop no está corriendo. Abrirlo y esperar a **Engine running**. Si no arranca: `wsl --update`, `wsl --shutdown` y reabrir. |
| `Docker Desktop has only XXXX MB memory`                             | Subir el límite en `.wslconfig` (sección 4) o bajar `--memory` en el script.                                                         |
| `You cannot change the memory size for an existing minikube cluster` | El clúster ya existe. Borrarlo con `minikube delete -p reservas-lab` y recrearlo.                                                    |
| `docker-env` falla con `SSH_AGENT_START`                             | El clúster usa `containerd`. Borrarlo y recrearlo con `--container-runtime=docker` (el script ya lo hace).                           |
| `minikube` o `kubectl` no se reconoce                                | Cerrar y volver a abrir la terminal; si persiste, revisar que esté en el PATH.                                                       |
| `Metrics API not available`                                          | metrics-server tarda uno o dos minutos en arrancar. Esperar y repetir `kubectl top nodes`.                                           |
| Pods en `Pending` o reinicios por falta de memoria                   | Aumentar la memoria de Docker/WSL y recrear el clúster.                                                                              |
