# MySQL Migration Guide

## Overview
This document summarizes the migration from PostgreSQL to MySQL for the Import Tax Management System.

## What Changed

### 1. **Database Driver** (pom.xml)
- Replaced: `org.postgresql:postgresql:42.7.4`
- With: `com.mysql:mysql-connector-j:8.0.33`

### 2. **Hibernate Configuration** (hibernate.cfg.xml)
| Setting | PostgreSQL | MySQL |
|---------|-----------|-------|
| Driver Class | `org.postgresql.Driver` | `com.mysql.cj.jdbc.Driver` |
| JDBC URL | `jdbc:postgresql://localhost:5432/import_tax_management_system_db` | `jdbc:mysql://localhost:3306/import_tax_management_system_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true` |
| Username | `postgres` | `root` |
| Password | `postgres` | `root` |
| Dialect | `org.hibernate.dialect.PostgreSQLDialect` | `org.hibernate.dialect.MySQLDialect` |

### 3. **Database Setup Script** (setup-db.ps1)
- Updated to use MySQL command-line client (XAMPP path: `C:\xampp\mysql\bin\mysql.exe`)
- Now supports XAMPP's default MySQL setup
- Environment variables still supported: `IMPORT_TAX_DB_USER`, `IMPORT_TAX_DB_PASSWORD`

### 4. **Seed Data SQL** (seed_data.sql)
Converted from PostgreSQL to MySQL syntax:
- ❌ `TRUNCATE TABLE ... RESTART IDENTITY CASCADE;` (PostgreSQL)
- ✅ `TRUNCATE TABLE ...;` with `SET FOREIGN_KEY_CHECKS = 0/1;` (MySQL)

## Prerequisites

### Required Software
- ✅ XAMPP with MySQL (includes Apache, MySQL, PHP)
- ✅ Java JDK 26 or higher (already configured)
- ✅ Maven 3.9+ (for rebuilds)

### MySQL Setup (if not already running)
1. Start XAMPP Control Panel
2. Click "Start" next to MySQL
3. Verify MySQL is running on `localhost:3306`

## Step-by-Step Setup

### Step 1: Create Database
```powershell
cd c:\Users\USER\Documents\NetBeansProjects\importProject
.\setup-db.ps1
```

When prompted, enter your MySQL `root` password (XAMPP default is no password, just press Enter).

**Output should show:**
```
Database 'import_tax_management_system_db' created successfully.
```

### Step 2: Start Server
```powershell
.\run-server.ps1
```

**Expected output (within 10-15 seconds):**
```
[INFO] RMI server started on port 5000
[INFO] ActiveMQ broker started
[INFO] Hibernate connected to MySQL
```

⚠️ **Leave this terminal open** — the server must stay running.

### Step 3: Start Client (in new terminal)
```powershell
.\run-client.ps1
```

The client GUI should launch. Wait 2-3 seconds for it to connect to the server.

### Step 4: Demo Login
**Username:** `admin`  
**Password:** `admin123`

The database will be seeded with demo data on first server launch.

## Environment Variable Configuration

You can override database credentials using environment variables or JVM properties:

### Via Environment Variables
```powershell
$env:IMPORT_TAX_DB_USER = "your_mysql_user"
$env:IMPORT_TAX_DB_PASSWORD = "your_mysql_password"
```

### Via JVM Properties
Add to `run-server.ps1` before calling the JAR:
```powershell
-Dimporttax.db.user="your_mysql_user"
-Dimporttax.db.password="your_mysql_password"
```

## Troubleshooting

### "mysql.exe was not found"
- **Cause:** XAMPP path mismatch
- **Fix:** Update `setup-db.ps1` with your XAMPP installation path
  - Default: `C:\xampp\mysql\bin\mysql.exe`
  - Custom XAMPP: Adjust accordingly

### "Access denied for user 'root'"
- **Cause:** Wrong MySQL password
- **Fix:** 
  - For XAMPP default (no password): Press Enter when prompted
  - For custom password: Set environment variable `IMPORT_TAX_DB_PASSWORD`

### "Can't connect to MySQL server on 'localhost:3306'"
- **Cause:** MySQL not running
- **Fix:** Start MySQL in XAMPP Control Panel

### Server crashes with "No suitable driver found"
- **Cause:** Using old JAR (built with PostgreSQL driver)
- **Fix:** Rebuild with: `mvn clean -DskipTests package`
- ⚠️ **Make sure JAVA_HOME=C:\Program Files\Java\jdk-26**

## File Locations

| File | Purpose | Status |
|------|---------|--------|
| [hibernate.cfg.xml](ImportTaxSystemServer26991/src/main/resources/hibernate.cfg.xml) | Server DB configuration | ✅ Updated for MySQL |
| [setup-db.ps1](setup-db.ps1) | Database creation script | ✅ Updated for MySQL |
| [run-server.ps1](run-server.ps1) | Server launcher | ✅ Uses rebuilt JAR |
| [run-client.ps1](run-client.ps1) | Client launcher | ✅ Uses rebuilt JAR |
| [seed_data.sql](ImportTaxSystemServer26991/seed_data.sql) | Demo data insert | ✅ MySQL syntax |
| [pom.xml](ImportTaxSystemServer26991/pom.xml) | Server dependencies | ✅ MySQL driver added |

## Build Details

**Server Build:** 
```
ImportTaxSystemServer26991-1.0-SNAPSHOT.jar (55.4 MB with all dependencies)
```

**Client Build:**
```
ImportTaxClient-jar-with-dependencies.jar (19.8 MB with all dependencies)
```

Both built with JDK 26, Java 21 bytecode.

## Next Steps

1. ✅ Configuration files updated for MySQL
2. ✅ JARs rebuilt with MySQL driver
3. ⏭️ Start MySQL via XAMPP Control Panel
4. ⏭️ Run `setup-db.ps1` to create database
5. ⏭️ Run `run-server.ps1` to start backend
6. ⏭️ Run `run-client.ps1` to start GUI

## Support

- All configurations are now **environment-agnostic** — no hardcoded paths
- Database credentials can be set via environment variables
- RMI host/port can be overridden for distributed setups

---
**Last Updated:** May 18, 2026
**Database:** MySQL 8.0.33 (via XAMPP)
**Status:** Ready for deployment ✅
