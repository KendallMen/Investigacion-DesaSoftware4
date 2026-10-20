PERFIL="reservas-lab"
VERSION="${1:-v1}"
shift || true
COMPONENTES=("$@")
[ ${#COMPONENTES[@]} -eq 0 ] && COMPONENTES=(ms-usuarios ms-laboratorios ms-reservas frontend)
RAIZ="$(cd "$(dirname "$0")" && pwd)"

if ! minikube status -p "$PERFIL" > /dev/null 2>&1; then
  echo "El clúster '$PERFIL' no está corriendo. Ejecutá primero: bash docs/start-minikube.sh"
  exit 1
fi

eval "$(minikube -p "$PERFIL" docker-env)"

FALLOS=()
for c in "${COMPONENTES[@]}"; do
  if [ ! -f "$RAIZ/$c/Dockerfile" ]; then
    echo "Omitido $c (no existe $c/Dockerfile)"
    continue
  fi
  echo
  echo "=== Construyendo reservas-lab/$c:$VERSION ==="
  docker build -t "reservas-lab/$c:$VERSION" "$RAIZ/$c" || FALLOS+=("$c")
done

echo
docker images --filter "reference=reservas-lab/*"
[ ${#FALLOS[@]} -gt 0 ] && { echo "Fallaron: ${FALLOS[*]}"; exit 1; }
echo "Listo."