# Import Tax Management System

**Student ID:** 26991  
**Project:** `ImportTaxSystemServer26991` + `ImportTaxSystemClient26991`  
**Stack:** Java 21 · RMI · Swing · Hibernate 6 · PostgreSQL · ActiveMQ 5.18

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Architecture](#2-architecture)
3. [Technology Stack](#3-technology-stack)
4. [Entity Model & Relationships](#4-entity-model--relationships)
5. [Prerequisites](#5-prerequisites)
6. [Database Setup](#6-database-setup)
7. [Configuration](#7-configuration)
8. [Building the Projects](#8-building-the-projects)
9. [Running the Application](#9-running-the-application)
10. [OTP / Email Verification — ⚠️ Demo Mode](#10-otp--email-verification--️-demo-mode)
11. [Features & Pages](#11-features--pages)
12. [Validation Rules](#12-validation-rules)
13. [Reports & Export](#13-reports--export)
14. [Project Structure](#14-project-structure)
15. [Known Limitations & TODO](#15-known-limitations--todo)

---

## 1. Project Overview

The **Import Tax Management System** is a distributed Java desktop application that manages the full lifecycle of import tax operations — from registering import items and calculating taxes, to generating invoices, recording payments, and producing exportable reports.

The system is split into two Maven modules:

| Module | Purpose |
|--------|---------|
| `ImportTaxSystemServer26991` | RMI server, Hibernate persistence, ActiveMQ broker, business logic |
| `ImportTaxSystemClient26991` | Java Swing GUI, connects to server via RMI only — no direct DB access |

---

## 2. Architecture

```
┌─────────────────────────────────────┐        RMI (port 5000)
│        Swing Client (GUI)           │ ◄──────────────────────►
│  AppShell → CardLayout pages        │
│  LoginPanel / RegisterPanel         │        ┌──────────────────────────────┐
│  OtpVerificationDialog              │        │        RMI Server            │
│  ImportItemPage / TaxPage           │        │  ServerLauncher              │
│  InvoicePage / PaymentPage          │        │  UserServiceImpl             │
│  ReportsPage / NotificationsPage    │        │  ImportItemServiceImpl       │
│  UsersPage / SettingsPage           │        │  TaxServiceImpl              │
└─────────────────────────────────────┘        │  InvoiceServiceImpl          │
                                               │  PaymentServiceImpl          │
                                               │  NotificationServiceImpl     │
                                               │  OtpServiceImpl              │
                                               │         │                    │
                                               │  ActiveMQ Broker             │
                                               │  (OTP.NOTIFICATIONS queue)   │
                                               │         │                    │
                                               │  Hibernate 6 (DAO layer)     │
                                               │         │                    │
                                               │  PostgreSQL Database         │
                                               └──────────────────────────────┘
```

**Design Patterns used:**
- **MVC** — Swing pages are Views, RMI service impls are Controllers, Hibernate entities are the Model
- **DAO** — `GenericDao` interface + `AbstractHibernateDao` + entity-specific DAO impls
- **Service Layer** — `RemoteCrudService<T>` generic RMI interface, extended per entity

---

## 3. Technology Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| GUI | Java Swing + FlatLaf 3.4.1 + MigLayout 11.3 |
| Charts | JFreeChart 1.5.4 |
| PDF Export | iText 5.5.13.3 |
| RMI | Java RMI on port 5000 |
| Message Broker | Apache ActiveMQ 5.18.4 (embedded) |
| ORM | Hibernate 6.6.3 + Jakarta Persistence 3.1 |
| Database | PostgreSQL 42.7.4 driver |
| Logging | SLF4J 2.0 + Logback (client) / SLF4J Simple (server) |
| Build | Apache Maven 3.x |

---

## 4. Entity Model & Relationships

```
User ──────────────────────────── ImportItem
(One-to-Many)                     │
                                  │ (Many-to-Many)
                                  Tax
                                  [join table: import_item_taxes]

Invoice ──────────────────────── Payment
(One-to-One, bidirectional)

Notification  (standalone)
```

| Relationship | Entities | Type |
|---|---|---|
| User → ImportItem | A user owns many import items | One-to-Many |
| Invoice ↔ Payment | Each invoice has at most one payment | One-to-One |
| ImportItem ↔ Tax | An item can have multiple applicable taxes; a tax applies to many items | Many-to-Many |

Hibernate `hbm2ddl.auto=update` creates/updates all tables including the `import_item_taxes` join table automatically on server start.

---

## 5. Prerequisites

- Java 21 JDK
- Apache Maven 3.8+
- PostgreSQL 14+ running on `localhost:5432`
- No ActiveMQ installation needed — broker is embedded in the server jar

---

## 6. Database Setup

Create the database before first run:

```sql
CREATE DATABASE import_tax_management_system_db;
```

Hibernate will create all tables automatically on first server start (`hbm2ddl.auto=update`).

A seed script is also available:

```
ImportTaxSystemServer26991/seed_data.sql
ImportTaxSystemServer26991/sql schema.sql
```

---

## 7. Configuration

### Database credentials
**File:** `ImportTaxSystemServer26991/src/main/resources/hibernate.cfg.xml`

```xml
<property name="hibernate.connection.url">
    jdbc:postgresql://localhost:5432/import_tax_management_system_db
</property>
<property name="hibernate.connection.username">postgres</property>
<property name="hibernate.connection.password">YOUR_PASSWORD_HERE</property>
```

> Change `YOUR_PASSWORD_HERE` to your PostgreSQL password.

### RMI host
**File:** `ImportTaxSystemClient26991/src/main/java/com/importtax/client/rmi/RmiConnection.java`

```java
private static final String RMI_HOST = "localhost";  // change if server is remote
private static final int    RMI_PORT = 5000;
```

---

## 8. Building the Projects

### Build the server fat jar

```bash
cd ImportTaxSystemServer26991
mvn package -DskipTests
```

Output: `target/ImportTaxSystemServer26991-1.0-SNAPSHOT.jar`

### Build the client

```bash
cd ImportTaxSystemClient26991
mvn compile
```

---

## 9. Running the Application

> Always start the **server first**, wait for it to print `RMI server started successfully`, then start the client.

### Terminal 1 — Server

```bash
cd ImportTaxSystemServer26991
java -Djava.rmi.server.hostname=127.0.0.1 -jar target\ImportTaxSystemServer26991-1.0-SNAPSHOT.jar
```

Expected output:
```
ActiveMQ embedded broker started — queue: OTP.NOTIFICATIONS
RMI registry started on port 5000
Bound service: rmi://localhost:5000/userService
Bound service: rmi://localhost:5000/importItemService
Bound service: rmi://localhost:5000/taxService
Bound service: rmi://localhost:5000/invoiceService
Bound service: rmi://localhost:5000/paymentService
Bound service: rmi://localhost:5000/notificationService
Bound service: rmi://localhost:5000/otpService
RMI server started successfully on port 5000
```

### Terminal 2 — Client

```bash
cd ImportTaxSystemClient26991
mvn exec:java
```

---

## 10. OTP / Email Verification — ⚠️ Demo Mode

### Current behaviour

OTP verification is required as the **last step of user registration**. The flow is:

1. User fills in the registration form
2. Client validates all fields locally
3. `OtpVerificationDialog` opens — server generates a 6-digit OTP
4. Server publishes the OTP to the ActiveMQ `OTP.NOTIFICATIONS` queue
5. The ActiveMQ consumer receives the message and **simulates** email delivery by printing to the server log
6. **The OTP is also displayed directly in the dialog status bar** (demo convenience)
7. User enters the OTP → if valid and not expired (5-minute TTL), account is created

### ⚠️ No real email is sent

There is **no SMTP configuration** in this project. Email delivery is fully simulated. The OTP appears in:
- The server console log (look for `[EMAIL SIMULATION]` block)
- The `OtpVerificationDialog` status label on the client (prefixed `OTP sent (demo):`)

### 🔧 To wire up real email delivery (TODO — fix tomorrow)

Two files need to be changed:

---

#### Fix 1 — Add JavaMail / SMTP dependency to server `pom.xml`

**File:** [`ImportTaxSystemServer26991/pom.xml`](ImportTaxSystemServer26991/pom.xml)

Add inside `<dependencies>`:

```xml
<!-- TODO: uncomment to enable real email delivery -->
<!--
<dependency>
    <groupId>com.sun.mail</groupId>
    <artifactId>jakarta.mail</artifactId>
    <version>2.0.1</version>
</dependency>
-->
```

---

#### Fix 2 — Replace the simulated consumer with a real SMTP sender

**File:** [`ImportTaxSystemServer26991/src/main/java/com/importtax/server/broker/NotificationBroker.java`](ImportTaxSystemServer26991/src/main/java/com/importtax/server/broker/NotificationBroker.java)

Inside `startOtpConsumer()`, replace the `onMessage` body:

```java
// TODO: replace this entire block with real SMTP sending
// ── CURRENT (simulation) ──────────────────────────────────────────────
LOGGER.info("║  [EMAIL SIMULATION]  To: {}  OTP: {}", recipient, otp);

// ── REPLACE WITH (real email) ─────────────────────────────────────────
// Properties props = new Properties();
// props.put("mail.smtp.host",       "<YOUR_SMTP_HOST>");       // e.g. smtp.gmail.com
// props.put("mail.smtp.port",       "<YOUR_SMTP_PORT>");       // e.g. 587
// props.put("mail.smtp.auth",       "true");
// props.put("mail.smtp.starttls.enable", "true");
// jakarta.mail.Session mailSession = jakarta.mail.Session.getInstance(props,
//     new jakarta.mail.Authenticator() {
//         protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
//             return new jakarta.mail.PasswordAuthentication(
//                 "<YOUR_SMTP_USERNAME>",    // e.g. your@gmail.com
//                 "<YOUR_SMTP_PASSWORD>"     // e.g. app password
//             );
//         }
//     });
// jakarta.mail.Message email = new jakarta.mail.internet.MimeMessage(mailSession);
// email.setFrom(new jakarta.mail.internet.InternetAddress("<YOUR_FROM_ADDRESS>"));
// email.setRecipients(jakarta.mail.Message.RecipientType.TO,
//     jakarta.mail.internet.InternetAddress.parse(recipient));
// email.setSubject("Your Import Tax OTP Code");
// email.setText("Your OTP is: " + otp + "\nValid for 5 minutes.");
// jakarta.mail.Transport.send(email);
// LOGGER.info("OTP email sent to {}", recipient);
```

---

#### Fix 3 — Remove the OTP from the dialog status label

**File:** [`ImportTaxSystemClient26991/src/main/java/com/importtax/client/ui/OtpVerificationDialog.java`](ImportTaxSystemClient26991/src/main/java/com/importtax/client/ui/OtpVerificationDialog.java)

In the `generateOtp()` method, `SwingWorker.done()` block:

```java
// TODO: remove the OTP value from the status message before going to production
// CURRENT (demo — shows OTP in UI):
setStatus("OTP sent (demo): " + otp, UIConstants.SUCCESS_COLOR);

// REPLACE WITH (production — hides OTP):
// setStatus("OTP sent to your registered email address.", UIConstants.SUCCESS_COLOR);
```

---

#### Fix 4 — Use the user's real email address as the recipient

**File:** [`ImportTaxSystemServer26991/src/main/java/com/importtax/server/rmi/impl/OtpServiceImpl.java`](ImportTaxSystemServer26991/src/main/java/com/importtax/server/rmi/impl/OtpServiceImpl.java)

Currently `generateOtp(username)` uses the username as the recipient. When real email is wired:

```java
// TODO: look up the user's email from the database and pass it as recipient
// CURRENT:
NotificationBroker.publishOtp(username, otp);  // username used as recipient placeholder

// REPLACE WITH:
// User user = userDao.findByUsername(username).orElseThrow(...);
// NotificationBroker.publishOtp(user.getEmail(), otp);
```

> `OtpServiceImpl` will need a `UserDao` injected — add it to the constructor similar to how `ImportItemServiceImpl` injects `UserDao`.

---

## 11. Features & Pages

| Page | Description |
|------|-------------|
| Login | Username + password authentication |
| Register | Full registration form with OTP verification as final step |
| Dashboard | Summary stat cards, quick-action buttons, activity feed |
| Import Items | Full CRUD — add, edit, delete, search import items with auto tax calculation |
| Tax Rates | Full CRUD — define tax rate configurations |
| Invoices | Full CRUD — manage tax invoices |
| Payments | Full CRUD — record payments linked to invoices |
| Notifications | Create, view, delete system notifications |
| Users | View all users, edit roles, delete accounts |
| Reports | Live stats with bar/pie charts, export to CSV and PDF |
| Settings | Read-only profile card for the logged-in user |

---

## 12. Validation Rules

### Business Validation Rules (server-side)

| # | Rule |
|---|------|
| 1 | Import item status must be `PENDING`, `CLEARED`, or `HOLD` |
| 2 | `CLEARED` import items cannot be deleted |
| 3 | Username must be unique across all users |
| 4 | Email must be unique across all users |
| 5 | Tax rate must be between 0 and 100 |

### Technical Validation Rules (client + server)

| # | Rule |
|---|------|
| 1 | All required fields must be non-empty before submission |
| 2 | Email must match the pattern `^[A-Za-z0-9+_.-]+@(.+)$` |
| 3 | Username must be at least 4 characters |
| 4 | Password must be at least 6 characters |
| 5 | Confirm password must match password |

---

## 13. Reports & Export

The **Reports** page provides:

- Live stat cards for Import Items, Invoices, and Payments
- Bar chart — Import Items by status (Pending / Cleared / Other)
- Pie chart — Invoice overview
- Bar chart — Payments by status

### Export formats

| Format | Button | Output |
|--------|--------|--------|
| CSV | Export CSV | `import_tax_report.csv` — three sections: Import Items, Invoices, Payments |
| PDF | Export PDF | `import_tax_report.pdf` — formatted tables with blue headers, generated via iText 5 |

---

## 14. Project Structure

```
importProject/
├── ImportTaxSystemServer26991/
│   ├── src/main/java/com/importtax/server/
│   │   ├── broker/          NotificationBroker.java        ← ActiveMQ embedded broker
│   │   ├── config/          ServerConfig.java
│   │   ├── dao/             GenericDao + entity DAOs
│   │   │   └── impl/        AbstractHibernateDao + impls
│   │   ├── model/           User, ImportItem, Tax, Invoice, Payment, Notification
│   │   ├── rmi/             Remote service interfaces (+ OtpService)
│   │   │   └── impl/        Service implementations (+ OtpServiceImpl)
│   │   ├── server/          ServerLauncher.java
│   │   └── util/            HibernateUtil, DatabaseSeeder
│   └── src/main/resources/
│       └── hibernate.cfg.xml                               ← DB credentials here
│
└── ImportTaxSystemClient26991/
    └── src/main/java/com/importtax/
        ├── client/
        │   ├── rmi/         RmiConnection.java
        │   ├── ui/          All Swing pages + OtpVerificationDialog.java
        │   └── util/        UIConstants, CurrentSession, validators
        └── server/          ← Shared model + RMI interface copies (no JPA annotations)
            ├── model/       User, ImportItem, Tax, Invoice, Payment, Notification
            └── rmi/         Remote service interfaces (client-side copies)
```

---

## 15. Known Limitations & TODO

| # | Item | File to fix |
|---|------|-------------|
| 1 | **No real email** — OTP is shown in the UI and server log only | See [Section 10](#10-otp--email-verification--️-demo-mode) for all 4 fix locations |
| 2 | OTP recipient is the username string, not the user's actual email address | [`OtpServiceImpl.java`](ImportTaxSystemServer26991/src/main/java/com/importtax/server/rmi/impl/OtpServiceImpl.java) — inject `UserDao`, look up email |
| 3 | Passwords stored in plain text — should be hashed with BCrypt | [`UserServiceImpl.java`](ImportTaxSystemServer26991/src/main/java/com/importtax/server/rmi/impl/UserServiceImpl.java) — `registerUser()` and `login()` |
| 4 | No pagination on large JTable datasets | All CRUD pages — add server-side paging to DAO queries |
| 5 | `import_item_taxes` Many-to-Many not yet exposed in the UI | [`ImportItemPage.java`](ImportTaxSystemClient26991/src/main/java/com/importtax/client/ui/ImportItemPage.java) — add a tax picker when creating/editing items |
