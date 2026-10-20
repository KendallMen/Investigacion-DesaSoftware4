PERFIL="reservas-lab"
K8S="v1.34.1"
MEMORIA="6500mb"
CPUS=4

if ! docker info > /dev/null 2>&1; then
  echo "Docker no está corriendo. Inícialo y vuelve a intentar."
  exit 1
fi

minikube start --profile="$PERFIL" --driver=docker --container-runtime=docker \
  --kubernetes-version="$K8S" --memory="$MEMORIA" --cpus="$CPUS" || {
    echo "Minikube no pudo iniciar. Revisa docs/instalacion.md."
    exit 1
  }

minikube addons enable ingress -p "$PERFIL"
minikube addons enable metrics-server -p "$PERFIL"
kubectl config use-context "$PERFIL"
echo "Listo. Verifica con: kubectl get nodes"