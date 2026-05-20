$ErrorActionPreference = "Stop"

# MySQL connection details (XAMPP default: root with NO password)
$mysqlExe = "C:\xampp\mysql\bin\mysql.exe"
$databaseName = "import_tax_management_system_db"
$mysqlUser = if ($env:IMPORT_TAX_DB_USER) { $env:IMPORT_TAX_DB_USER } else { "root" }
$mysqlHost = "localhost"
$mysqlPort = 3306

if (-not (Test-Path $mysqlExe)) {
    throw "mysql.exe was not found at $mysqlExe. Ensure XAMPP is installed with MySQL or update this script path."
}

# Get password - XAMPP default is NO password (just press Enter)
$mysqlPassword = if ($env:IMPORT_TAX_DB_PASSWORD) { 
    $env:IMPORT_TAX_DB_PASSWORD 
} else { 
    Read-Host "MySQL password for user $mysqlUser (XAMPP default: just press Enter)"
}

# Build password flag
$passwordFlag = if ($mysqlPassword -and $mysqlPassword.Length -gt 0) { 
    "-p$mysqlPassword" 
} else { 
    "" 
}

# Check if database exists
Write-Host "Checking if database exists..." -ForegroundColor Cyan
$checkQuery = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '$databaseName';"

if ($passwordFlag) {
    $checkOutput = & $mysqlExe -h $mysqlHost -P $mysqlPort -u $mysqlUser $passwordFlag -e $checkQuery 2>&1
} else {
    $checkOutput = & $mysqlExe -h $mysqlHost -P $mysqlPort -u $mysqlUser -e $checkQuery 2>&1
}

$checkOutputStr = $checkOutput | Out-String
if ($checkOutputStr -match "ERROR") {
    Write-Host "ERROR connecting to MySQL:" -ForegroundColor Red
    Write-Host $checkOutputStr -ForegroundColor Red
    exit 1
}

if ($checkOutputStr -match $databaseName) {
    Write-Host "Database '$databaseName' already exists." -ForegroundColor Green
    exit 0
}

# Create database
Write-Host "Creating database..." -ForegroundColor Cyan
$createQuery = "CREATE DATABASE IF NOT EXISTS $databaseName CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

if ($passwordFlag) {
    $createOutput = & $mysqlExe -h $mysqlHost -P $mysqlPort -u $mysqlUser $passwordFlag -e $createQuery 2>&1
} else {
    $createOutput = & $mysqlExe -h $mysqlHost -P $mysqlPort -u $mysqlUser -e $createQuery 2>&1
}

$createOutputStr = $createOutput | Out-String
if ($createOutputStr -match "ERROR") {
    Write-Host "ERROR creating database:" -ForegroundColor Red
    Write-Host $createOutputStr -ForegroundColor Red
    exit 1
}

Write-Host "Database '$databaseName' created successfully." -ForegroundColor Green
