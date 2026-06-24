$ErrorActionPreference = "Stop"

$projectRoot = "C:\Users\Usuario\AndroidStudioProjects\WifiNetworkScanner"
$packageRoot = Join-Path $projectRoot "app\src\main\java\com\example\wifinetworkscanner"

$directories = @(
    "data",
    "data\local",
    "data\repository",
    "di",
    "domain",
    "domain\model",
    "domain\repository",
    "domain\usecase",
    "domain\validation",
    "ui",
    "ui\components",
    "ui\navigation",
    "ui\screens",
    "ui\screens\home",
    "ui\theme",
    "utils",
    "utils\logger",
    "utils\network"
)

Write-Host "Verificando projeto em: $projectRoot"

if (-not (Test-Path $projectRoot)) {
    throw "Projeto não encontrado em: $projectRoot"
}

Write-Host "Criando pacote base em: $packageRoot"

if (-not (Test-Path $packageRoot)) {
    New-Item -ItemType Directory -Path $packageRoot -Force | Out-Null
    Write-Host "Pacote base criado."
} else {
    Write-Host "Pacote base já existe."
}

foreach ($directory in $directories) {
    $fullPath = Join-Path $packageRoot $directory

    if (-not (Test-Path $fullPath)) {
        New-Item -ItemType Directory -Path $fullPath -Force | Out-Null
        Write-Host "Criado: $fullPath"
    } else {
        Write-Host "Já existe: $fullPath"
    }

    $gitKeepPath = Join-Path $fullPath ".gitkeep"

    if (-not (Test-Path $gitKeepPath)) {
        New-Item -ItemType File -Path $gitKeepPath -Force | Out-Null
    }
}

Write-Host ""
Write-Host "Estrutura criada com sucesso."
Write-Host ""
Write-Host "Base final:"
Write-Host $packageRoot