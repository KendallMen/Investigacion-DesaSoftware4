# Inicia Minikube con la configuración común del equipo
$ErrorActionPreference = "Stop"

docker info *> $null
if ($LASTEXITCODE -ne 0) { Write-Error "Docker Desktop no está corriendo."; exit 1 }

minikube start `
  --profile=reservas-lab `
  --driver=docker `
  --container-runtime=docker `
  --kubernetes-version=v1.34.1 `
  --memory=6500mb `
  --cpus=4

minikube addons enable ingress -p reservas-lab
minikube addons enable metrics-server -p reservas-lab
kubectl config use-context reservas-lab