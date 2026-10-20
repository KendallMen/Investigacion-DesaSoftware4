param(
    [string]$Version = "v1",
    [string[]]$Solo = @()
)

$PERFIL = "reservas-lab"
$COMPONENTES = @("ms-usuarios", "ms-laboratorios", "ms-reservas", "frontend")
if ($Solo.Count -gt 0) { $COMPONENTES = $Solo }

minikube status -p $PERFIL *> $null
if ($LASTEXITCODE -ne 0) {
    Write-Host "El clúster '$PERFIL' no está corriendo. Ejecutá primero .\docs\start-minikube.ps1" -ForegroundColor Red
    exit 1
}

& minikube -p $PERFIL docker-env --shell powershell | Invoke-Expression

$fallos = @()
foreach ($c in $COMPONENTES) {
    $dir = Join-Path $PSScriptRoot $c
    if (-not (Test-Path (Join-Path $dir "Dockerfile"))) {
        Write-Host "Omitido $c (no existe $c\Dockerfile)" -ForegroundColor Yellow
        continue
    }
    Write-Host "`n=== Construyendo reservas-lab/${c}:$Version ===" -ForegroundColor Cyan
    docker build -t "reservas-lab/${c}:$Version" $dir
    if ($LASTEXITCODE -ne 0) { $fallos += $c }
}

Write-Host ""
docker images --filter "reference=reservas-lab/*"

if ($fallos.Count -gt 0) {
    Write-Host "`nFallaron: $($fallos -join ', ')" -ForegroundColor Red
    exit 1
}
Write-Host "`nListo. Si la etiqueta ya estaba desplegada, reiniciá los pods con:" -ForegroundColor Green
Write-Host "  kubectl rollout restart deployment <nombre> -n reservas-lab"