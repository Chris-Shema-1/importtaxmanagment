$ErrorActionPreference = "Stop"

$javaExe = "C:\Program Files\Java\jdk-26\bin\java.exe"
$serverDir = Join-Path $PSScriptRoot "ImportTaxSystemServer26991"
$serverJar = Join-Path $serverDir "target\ImportTaxSystemServer26991-1.0-SNAPSHOT.jar"

if (-not (Test-Path $javaExe)) {
    throw "Java 26 was not found at $javaExe. Install JDK 21+ or update this script."
}

if (-not (Test-Path $serverJar)) {
    throw "Server jar was not found at $serverJar. Build the server first with Maven."
}

# Only prompt for MySQL password if not already set and user explicitly provides one
# Default XAMPP MySQL has NO password, so leave it empty
if (-not $env:IMPORT_TAX_DB_PASSWORD) {
    $passwordInput = Read-Host "MySQL password for user root (XAMPP default: just press Enter)"
    if ($passwordInput -and $passwordInput.Length -gt 0) {
        $env:IMPORT_TAX_DB_PASSWORD = $passwordInput
    }
}

Push-Location $serverDir
try {
    & $javaExe "-Djava.rmi.server.hostname=127.0.0.1" -jar $serverJar
} finally {
    Pop-Location
}
