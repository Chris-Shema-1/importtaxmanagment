# Product Requirements Document (PRD)
## Import Tax Management System (ITMS)

**Student:** Shema Christian
**Student ID:** 26991
**Course:** Java Programming
**Version:** 1.0

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Problem Statement](#2-problem-statement)
3. [Project Objectives](#3-project-objectives)
4. [Scope](#4-scope)
5. [System Users](#5-system-users)
6. [Functional Requirements](#6-functional-requirements)
7. [Non-Functional Requirements](#7-non-functional-requirements)
8. [Technologies Used](#8-technologies-used)
9. [System Architecture](#9-system-architecture)
10. [Design Patterns](#10-design-patterns)
11. [Database Design](#11-database-design)
12. [Entity Relationships](#12-entity-relationships)
13. [Validation Rules](#13-validation-rules)
14. [Reporting Features](#14-reporting-features)
15. [Notification System](#15-notification-system)
16. [User Interface Requirements](#16-user-interface-requirements)
17. [Security Requirements](#17-security-requirements)
18. [Expected Outputs](#18-expected-outputs)
19. [Testing Strategy](#19-testing-strategy)
20. [Future Improvements](#20-future-improvements)
21. [Conclusion](#21-conclusion)

---

## 1. Introduction

### 1.1 Project Overview

The **Import Tax Management System (ITMS)** is a distributed Java desktop application designed to automate and centralize import taxation processes. It enables authorized users to:

- Record and track imported goods
- Calculate applicable taxes automatically
- Generate and manage invoices
- Process and confirm payments
- Monitor import clearance status
- Produce structured reports

The system is built on a **client-server architecture** using Java RMI for distributed communication, Java Swing for the graphical interface, and Hibernate ORM for database persistence.

### 1.2 Background & Motivation

Manual import tax management introduces significant operational risk. Common issues include delayed tax processing, handwritten paperwork prone to error, inconsistent record-keeping, and poor audit trails. ITMS directly addresses these problems by providing a centralized, validated, and automated digital solution.

---

## 2. Problem Statement

Many customs agencies and import tax offices still rely on manual workflows or isolated software tools. This leads to:

| Problem | Impact |
|---------|--------|
| Human errors in tax calculations | Incorrect tax collection |
| No centralized records | Data duplication and inconsistency |
| Slow payment tracking | Delayed goods clearance |
| Poor reporting tools | Difficult auditing and oversight |
| Weak access controls | Security and compliance risk |
| Limited transparency | Lack of accountability |

ITMS proposes a distributed, Java-based solution to digitize and centralize all import tax operations, eliminating these gaps.

---

## 3. Project Objectives

### 3.1 Main Objective

To develop a distributed Import Tax Management System that automates the recording, taxation, payment processing, and reporting of imported goods.

### 3.2 Specific Objectives

| # | Objective |
|---|-----------|
| 1 | Implement a client-server architecture using Java RMI |
| 2 | Build a user-friendly Java Swing desktop interface |
| 3 | Implement secure user authentication with OTP verification |
| 4 | Automate tax calculation using a defined formula |
| 5 | Manage import item records with full CRUD support |
| 6 | Generate invoices and track payment lifecycle |
| 7 | Export reports in PDF and CSV formats |
| 8 | Simulate notification delivery via a message broker |
| 9 | Apply MVC and DAO design patterns throughout the codebase |
| 10 | Persist all data reliably using Hibernate ORM |

---

## 4. Scope

### 4.1 Included Features

#### User Management
- User registration and login
- OTP-based login verification
- Role-based access control

#### Import Item Management
- Register, edit, and delete import records
- Search by item name, category, status, or importer
- View full import history

#### Tax Management
- Automatic tax calculation using the formula:
  > **Total Tax = Quantity × Unit Price × Tax Rate**
- Tax rate configuration
- Finance officer tax approval workflow

#### Invoice Management
- Automatic invoice generation upon tax approval
- Invoice tracking and viewing

#### Payment Management
- Record and confirm tax payments
- Track and update payment status
- Prevent clearance before full payment

#### Notifications
- OTP verification messages (simulated)
- Payment confirmation alerts
- Import status update notifications

#### Reporting
- Generate import, payment, and tax summary reports
- Export to PDF and CSV
- Print report support

---

### 4.2 Excluded Features

The following are outside the scope of this version:

| Excluded Feature | Reason |
|-----------------|--------|
| Online payment gateways | Requires third-party banking integration |
| Mobile application | Separate development scope |
| Multi-language support | Not required for current deployment |
| Biometric authentication | Hardware dependency |
| Cloud deployment | Infrastructure out of scope |
| Real SMS/email services | Simulation used instead |

---

## 5. System Users

| User Role | Permissions & Responsibilities |
|-----------|-------------------------------|
| **Admin** | Manages user accounts, assigns roles, monitors all system activity |
| **Customs Officer** | Registers imported goods, updates import records, initiates tax workflow |
| **Finance Officer** | Reviews and approves tax calculations, confirms invoice payments |
| **Importer** | Views their own import records and payment status (read-only) |

---

## 6. Functional Requirements

### 6.1 Authentication Module

| ID | Requirement | Description |
|----|-------------|-------------|
| FR-01 | User Registration | The system shall allow new users to create an account with a unique username and email. |
| FR-02 | User Login | The system shall authenticate users via username and password. |
| FR-03 | OTP Verification | The system shall generate and verify a one-time password during login (simulated delivery). |
| FR-04 | Logout | The system shall allow users to securely end their session. |

---

### 6.2 Import Item Management Module

| ID | Requirement | Description |
|----|-------------|-------------|
| FR-05 | Add Import Item | Authorized users shall register new imported goods with name, quantity, unit price, and category. |
| FR-06 | Update Import Item | Users shall be able to modify existing import records before tax approval. |
| FR-07 | Delete Import Item | Users shall be able to remove import records that have not yet been invoiced. |
| FR-08 | Search Import Items | Users shall search records by item name, category, status, or importer name. |
| FR-09 | View Import Items | The system shall display all registered items in a searchable, sortable table. |

---

### 6.3 Tax Management Module

| ID | Requirement | Description |
|----|-------------|-------------|
| FR-10 | Calculate Tax | The system shall automatically compute total tax using: **Total Tax = Quantity × Unit Price × Tax Rate**. |
| FR-11 | Approve Tax | Finance officers shall review and approve tax calculations before invoice generation. |

---

### 6.4 Invoice Management Module

| ID | Requirement | Description |
|----|-------------|-------------|
| FR-12 | Generate Invoice | The system shall automatically generate an invoice once tax is approved. |
| FR-13 | View Invoice | All authorized users shall be able to view invoice details and status. |

---

### 6.5 Payment Module

| ID | Requirement | Description |
|----|-------------|-------------|
| FR-14 | Record Payment | Finance officers shall record payments against outstanding invoices. |
| FR-15 | Update Payment Status | The system shall update the payment status and trigger goods clearance upon full payment. |

---

### 6.6 Reporting Module

| ID | Requirement | Description |
|----|-------------|-------------|
| FR-16 | Generate Reports | The system shall compile import, payment, and tax summary reports on demand. |
| FR-17 | Export Reports | Reports shall be exportable to PDF and CSV formats. |

---

## 7. Non-Functional Requirements

### 7.1 Performance

- System response time shall not exceed **3 seconds** for standard operations under normal load.
- Database queries shall be optimized via Hibernate caching.

### 7.2 Security

- All passwords shall be stored using encryption (e.g., BCrypt or SHA-256).
- Role-based access control shall restrict functionality by user type.
- OTP verification shall add a second factor to the login process.
- All input shall be validated server-side to prevent injection attacks.

### 7.3 Reliability

- The system shall maintain data consistency through Hibernate transactions.
- Failed operations shall roll back to prevent partial data writes.

### 7.4 Availability

- The RMI server shall remain active throughout working hours.
- The system shall display a clear error message if the server becomes unreachable.

### 7.5 Maintainability

- Code shall strictly follow **MVC** and **DAO** design patterns.
- Each module shall be independently testable.

### 7.6 Usability

- The Swing GUI shall be intuitive enough for non-technical users.
- All error messages shall be descriptive and actionable.

### 7.7 Scalability

- The architecture shall support adding new modules (e.g., mobile client) without redesigning the core system.

---

## 8. Technologies Used

| Technology | Version / Type | Purpose |
|------------|---------------|---------|
| Java SE | JDK 11+ | Core application language |
| Java Swing | Built-in | Desktop graphical interface |
| Java RMI | Built-in | Client-server remote communication |
| Hibernate ORM | 5.x / 6.x | Database persistence and mapping |
| MySQL | 8.x | Relational database engine |
| Maven | 3.x | Dependency and build management |
| FlatLaf | Latest | Modern look-and-feel UI theme |
| JOptionPane | Built-in | In-app alerts and dialogs |
| ActiveMQ / RabbitMQ | Latest | Message broker for notification simulation |

---

## 9. System Architecture

The system is structured as a **three-tier distributed application**:

```
┌───────────────────────┐
│   CLIENT (Swing GUI)  │  ← User interacts here
│  Forms, Tables, Menus │
└──────────┬────────────┘
           │ Java RMI
┌──────────▼────────────┐
│   SERVER (RMI Server) │  ← Business logic lives here
│  Services, Validation │
└──────────┬────────────┘
           │ Hibernate ORM
┌──────────▼────────────┐
│   DATABASE (MySQL)    │  ← Data persisted here
│  Tables, Constraints  │
└───────────────────────┘
```

### 9.1 Client Application
- Renders Java Swing forms and dialogs
- Captures and displays user input
- Communicates with the server exclusively via RMI stubs

### 9.2 Server Application
- Hosts all RMI remote service implementations
- Enforces business rules and validation logic
- Delegates data access to the DAO layer
- Runs on **port 5000**

### 9.3 Database Layer
- Stores all persistent system data
- Enforces referential integrity via foreign key constraints
- Managed through Hibernate entity mappings

---

## 10. Design Patterns

### 10.1 MVC (Model-View-Controller)

| Layer | Role | Implementation |
|-------|------|----------------|
| **Model** | Represents data and business entities | Java POJOs mapped with Hibernate annotations |
| **View** | User interface and display logic | Java Swing forms, JTable, JOptionPane |
| **Controller** | Handles user actions and coordinates model/view | Action listeners and RMI service calls |

### 10.2 DAO (Data Access Object)

Each entity has:
- A **DAO Interface** defining CRUD method contracts
- A **DAO Implementation** using Hibernate Session for execution

| DAO Class | Entity Managed |
|-----------|---------------|
| `UserDAO` | Users |
| `ImportItemDAO` | Imported goods |
| `TaxDAO` | Tax records |
| `InvoiceDAO` | Invoices |
| `PaymentDAO` | Payments |
| `NotificationDAO` | Notifications |

---

## 11. Database Design

**Database Name:** `import_tax_management_system_db`

### Table: `users`

| Column | Type | Constraints |
|--------|------|-------------|
| `user_id` | INT | PRIMARY KEY, AUTO_INCREMENT |
| `username` | VARCHAR(100) | NOT NULL, UNIQUE |
| `password` | VARCHAR(255) | NOT NULL (encrypted) |
| `email` | VARCHAR(150) | NOT NULL, UNIQUE |
| `role` | ENUM | NOT NULL (ADMIN, OFFICER, FINANCE, IMPORTER) |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |

### Table: `import_items`

| Column | Type | Constraints |
|--------|------|-------------|
| `item_id` | INT | PRIMARY KEY, AUTO_INCREMENT |
| `name` | VARCHAR(150) | NOT NULL |
| `category` | VARCHAR(100) | NOT NULL |
| `quantity` | INT | NOT NULL, > 0 |
| `unit_price` | DECIMAL(15,2) | NOT NULL, > 0 |
| `import_date` | DATE | NOT NULL |
| `status` | ENUM | DEFAULT 'PENDING' (PENDING, INVOICED, PAID, CLEARED) |
| `user_id` | INT | FOREIGN KEY → users(user_id) |

### Table: `taxes`

| Column | Type | Constraints |
|--------|------|-------------|
| `tax_id` | INT | PRIMARY KEY, AUTO_INCREMENT |
| `tax_rate` | DECIMAL(5,2) | NOT NULL, ≥ 0 |
| `calculated_amount` | DECIMAL(15,2) | NOT NULL |
| `approved` | BOOLEAN | DEFAULT FALSE |
| `item_id` | INT | FOREIGN KEY → import_items(item_id) |

### Table: `invoices`

| Column | Type | Constraints |
|--------|------|-------------|
| `invoice_id` | INT | PRIMARY KEY, AUTO_INCREMENT |
| `total_amount` | DECIMAL(15,2) | NOT NULL |
| `status` | ENUM | DEFAULT 'UNPAID' (UNPAID, PAID) |
| `generated_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |
| `tax_id` | INT | FOREIGN KEY → taxes(tax_id) |

### Table: `payments`

| Column | Type | Constraints |
|--------|------|-------------|
| `payment_id` | INT | PRIMARY KEY, AUTO_INCREMENT |
| `amount` | DECIMAL(15,2) | NOT NULL |
| `payment_date` | DATE | NOT NULL |
| `status` | ENUM | DEFAULT 'PENDING' (PENDING, CONFIRMED) |
| `invoice_id` | INT | FOREIGN KEY → invoices(invoice_id) |

### Table: `notifications`

| Column | Type | Constraints |
|--------|------|-------------|
| `notification_id` | INT | PRIMARY KEY, AUTO_INCREMENT |
| `message` | TEXT | NOT NULL |
| `type` | VARCHAR(50) | (OTP, PAYMENT, STATUS) |
| `sent_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |
| `user_id` | INT | FOREIGN KEY → users(user_id) |

---

## 12. Entity Relationships

```
users ──────────────────────< import_items
  │                                │
  │                                │ (One-to-One)
  │                                ▼
  │                             taxes
  │                                │ (One-to-One)
  │                                ▼
  │                            invoices
  │                                │ (One-to-One)
  │                                ▼
  │                            payments
  │
  └────────────────────────< notifications
```

| Relationship | Entities |
|-------------|----------|
| One-to-Many | `users` → `import_items` |
| One-to-Many | `users` → `notifications` |
| One-to-One | `import_items` ↔ `taxes` |
| One-to-One | `taxes` ↔ `invoices` |
| One-to-One | `invoices` ↔ `payments` |
| Many-to-Many | `users` ↔ `roles` *(planned for future version)* |

---

## 13. Validation Rules

### 13.1 Business Validation Rules

| # | Rule | Enforcement |
|---|------|-------------|
| 1 | Quantity must be greater than zero | Server-side + UI |
| 2 | Unit price must be greater than zero | Server-side + UI |
| 3 | Tax rate cannot be negative | Server-side |
| 4 | Username must be unique | Database constraint |
| 5 | Email address must be unique | Database constraint |
| 6 | Import date cannot be a future date | Server-side |
| 7 | Payment amount cannot exceed invoice amount | Server-side |
| 8 | Goods cannot be cleared before full payment | Server-side |
| 9 | Duplicate invoices for one item are not allowed | Server-side |

### 13.2 Technical Validation Rules

| # | Rule | Enforcement |
|---|------|-------------|
| 1 | All required fields must be completed | UI + Server |
| 2 | Email must match a valid format pattern | UI + Server |
| 3 | Numeric fields must reject non-numeric input | UI |
| 4 | Passwords must meet a minimum length requirement | UI + Server |
| 5 | Database connection must be active before operations | Server startup check |
| 6 | RMI server must be reachable before client actions | Client connection check |
| 7 | Duplicate record submission must be prevented | Database + Server |

---

## 14. Reporting Features

### Supported Report Types

| Report | Contents |
|--------|----------|
| Import Items Report | All registered goods with status, quantity, and value |
| Payment Report | All payment transactions with dates and statuses |
| Tax Summary Report | Tax rates applied, amounts calculated, and approval status |
| User Activity Report | Log of user actions within the system |

### Export Formats

| Format | Use Case |
|--------|----------|
| **PDF** | Formal documentation, printing, filing |
| **CSV** | Data analysis, spreadsheet import |

---

## 15. Notification System

The notification system simulates real-world communication using a message broker.

| Event | Message | Delivery Method |
|-------|---------|-----------------|
| User login | OTP code sent for verification | Simulated (console / dialog) |
| Payment confirmed | "Your payment has been received" | Simulated via broker |
| Import status change | "Your item status has been updated" | Simulated via broker |

**Message Broker:** ActiveMQ or RabbitMQ
> All notifications are simulated. No real SMS or email services are used in this version.

---

## 16. User Interface Requirements

### General UI Standards

- The interface shall use the **FlatLaf** theme for a modern appearance.
- All screens shall support scrollable content for large datasets.
- Modal dialogs shall be used for confirmations and form entries.
- Error and success messages shall use `JOptionPane` alerts.

### Required Screens

| Screen | Key Components |
|--------|---------------|
| Login Form | Username field, password field, OTP dialog |
| Dashboard | Navigation menu, summary stats |
| Import Item Form | Input fields for item details, submit button |
| Tax Management View | Tax rate input, calculated amount display, approve button |
| Invoice View | Invoice table, status indicator, print/export button |
| Payment Form | Amount input, invoice reference, confirm button |
| Reports Screen | JTable with filters, export to PDF/CSV buttons |
| Admin Panel | User management table, role assignment |

### UI Component Standards

| Component | Usage |
|-----------|-------|
| `JTable` | Display all record lists with sorting |
| `JOptionPane` | Alerts, confirmations, OTP prompts |
| `JTextField` | Standard text input |
| `JComboBox` | Role, status, and category dropdowns |
| `JScrollPane` | Wrap all tables for scroll support |

---

## 17. Security Requirements

| Requirement | Implementation |
|-------------|---------------|
| Password encryption | BCrypt or SHA-256 hashing before storage |
| Role-based access | UI elements and RMI methods gated by user role |
| Session validation | Sessions invalidated on logout or timeout |
| OTP verification | One-time code required at login (simulated) |
| Input validation | All inputs validated on both client and server |
| SQL injection prevention | Hibernate parameterized queries used throughout |

---

## 18. Expected Outputs

| Output | Format | Triggered By |
|--------|--------|-------------|
| Tax Invoice | PDF / On-screen | Tax approval by Finance Officer |
| Payment Receipt | PDF / On-screen | Payment confirmation |
| Import Report | PDF / CSV | User request from Reports screen |
| Payment History Report | PDF / CSV | User request from Reports screen |
| OTP Message | Dialog (simulated) | User login attempt |

---

## 19. Testing Strategy

### 19.1 Unit Testing

- Test all individual DAO methods (create, read, update, delete).
- Test tax calculation logic with boundary values (zero quantity, maximum rate).

### 19.2 Integration Testing

- Verify RMI client-server communication for each remote method.
- Test Hibernate session management and transaction rollback on failure.

### 19.3 UI Testing

- Validate that all Swing forms submit correct data.
- Verify that error dialogs appear for invalid input.
- Confirm that role-based UI restrictions are enforced.

### 19.4 Validation Testing

- Test all business rules with valid and invalid inputs.
- Confirm duplicate prevention at database and application levels.
- Test edge cases: zero payment, future import date, missing fields.

---

## 20. Future Improvements

| Feature | Description |
|---------|-------------|
| Mobile Application | Android/iOS client for importers |
| Online Payment Integration | Connect to real payment gateways |
| Real Notifications | Actual SMS and email delivery |
| Dashboard Analytics | Charts and KPIs for admins |
| Barcode / QR Support | Scan items for faster registration |
| Improved Concurrency | Thread-safe multi-user RMI handling |
| Cloud Deployment | Host server on AWS or GCP |
| Multi-language Support | Localization for multiple regions |

---

## 21. Conclusion

The **Import Tax Management System** delivers a complete, distributed solution for automating import taxation workflows. By combining:

- **Java RMI** for reliable client-server communication
- **Java Swing + FlatLaf** for an accessible and modern desktop interface
- **Hibernate ORM** for consistent and transactional data persistence
- **MVC and DAO patterns** for a clean, maintainable architecture
- **Role-based access and OTP verification** for security

...the system resolves the core inefficiencies of manual import tax management and provides a scalable foundation for future enhancements.
