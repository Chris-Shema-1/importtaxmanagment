$ErrorActionPreference = "Stop"

$javaExe = "C:\Program Files\Java\jdk-26\bin\java.exe"
$clientDir = Join-Path $PSScriptRoot "ImportTaxSystemClient26991"
$clientJar = Join-Path $clientDir "target\ImportTaxClient-jar-with-dependencies.jar"

if (-not (Test-Path $javaExe)) {
    throw "Java 26 was not found at $javaExe. Install JDK 21+ or update this script."
}

if (-not (Test-Path $clientJar)) {
    throw "Client jar was not found at $clientJar. Build the client first with Maven."
}

if (-not $env:IMPORT_TAX_RMI_HOST) {
    $env:IMPORT_TAX_RMI_HOST = "localhost"
}

Push-Location $clientDir
try {
    & $javaExe -jar $clientJar
} finally {
    Pop-Location
}
