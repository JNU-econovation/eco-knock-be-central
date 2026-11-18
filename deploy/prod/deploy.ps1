param(
    [Parameter(Position = 0)]
    [string] $Command = "up",

    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]] $Rest
)

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepoRoot = Resolve-Path (Join-Path $ScriptDir "..\..")

Push-Location $RepoRoot
try {
    git pull origin main
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
} finally {
    Pop-Location
}

# CI가 GHCR에 올린 이미지를 APP_IMAGE로 넘기면 서버에서 빌드하지 않고 그 이미지를 받아 실행한다.
if ($Command -eq "up" -and $env:APP_IMAGE) {
    & (Join-Path $ScriptDir "prod.ps1") pull app
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
    & (Join-Path $ScriptDir "prod.ps1") up --no-build @Rest
    exit $LASTEXITCODE
}

& (Join-Path $ScriptDir "prod.ps1") $Command @Rest
exit $LASTEXITCODE
