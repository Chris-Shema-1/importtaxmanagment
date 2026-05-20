# Software Requirements Specification
## Import Tax Management System

**Student:** Shema Christian
**Student ID:** 26991
**Course:** Java Programming

---

## Table of Contents

1. [Abstract](#1-abstract)
2. [Scope](#2-scope)
3. [System Architecture](#3-system-architecture)
4. [Functional Requirements](#4-functional-requirements)
5. [Non-Functional Requirements](#5-non-functional-requirements)
6. [Technologies Used](#6-technologies-used)
7. [Database Design](#7-database-design)
8. [DAO Layer](#8-dao-layer)
9. [RMI Service Layer](#9-rmi-service-layer)
10. [Client Application](#10-client-application)
11. [Validation Rules](#11-validation-rules)
12. [Reporting and Export](#12-reporting-and-export)
13. [Notification System](#13-notification-system)
14. [Conclusion](#14-conclusion)

---

## 1. Abstract

The **Import Tax Management System** is a distributed Java-based application designed to automate customs and tax operations. It handles:

- Recording of imported goods
- Tax calculation
- Invoice generation
- Payment management

The system uses **Java RMI** for client-server communication, **Java Swing** for the user interface, and **Hibernate ORM** for database persistence. It improves efficiency, accuracy, and transparency in import tax management by centralizing all operations securely.

---

## 2. Scope

### In Scope

The system covers the following operations within a customs or financial environment:

- Registering imported goods
- Calculating applicable taxes
- Generating and managing invoices
- Processing and tracking payments
- Monitoring goods clearance status
- User authentication with OTP simulation
- Reporting and data export

### Out of Scope

The following are explicitly excluded from this system:

- Integration with real banking systems
- Real SMS or email services (simulation only)

---

## 3. System Architecture

The system follows a **three-tier distributed architecture**:

| Tier | Component | Responsibility |
|------|-----------|----------------|
| Presentation | Client App (Swing GUI) | User interaction |
| Business Logic | RMI Server | Processing and rules |
| Data | MySQL / PostgreSQL | Persistent storage |

### Design Patterns

- **MVC** — Model-View-Controller for UI separation
- **DAO** — Data Access Object for database abstraction

---

## 4. Functional Requirements

### 4.1 Core Features

- The system shall allow users to register imported goods.
- The system shall calculate tax based on item value and applicable tax rate.
- The system shall generate invoices for registered imported goods.
- The system shall allow payment recording and confirmation.
- The system shall update goods status: **Pending → Paid → Cleared**.

### 4.2 User Management

- The system shall support user login with credentials.
- The system shall verify login using OTP simulation.

### 4.3 Data Management

- The system shall support full **CRUD** operations (Create, Read, Update, Delete).
- The system shall allow searching and filtering of records.

### 4.4 Reporting

- The system shall generate reports covering imports and payments.
- The system shall export reports to **CSV** and **PDF** formats.

---

## 5. Non-Functional Requirements

| Category | Requirement |
|----------|-------------|
| Security | Authentication required for all operations |
| Performance | System must be responsive and efficient under load |
| Concurrency | Must support multiple simultaneous users via RMI |
| Consistency | Data integrity enforced through Hibernate transactions |
| Usability | Clear, intuitive Swing-based GUI |

---

## 6. Technologies Used

| Technology | Purpose |
|------------|---------|
| Java (Core) | Primary programming language |
| Java RMI | Client-server remote communication |
| Java Swing | Desktop graphical user interface |
| Hibernate ORM | Database persistence and mapping |
| MySQL / PostgreSQL | Relational database storage |
| ActiveMQ / RabbitMQ | Message broker for notification simulation |

---

## 7. Database Design

**Database Name:** `import_tax_management_system_db`

### 7.1 Entities

#### User
| Field | Description |
|-------|-------------|
| `userId` | Primary key |
| `username` | Login name |
| `password` | Hashed password |
| `role` | User role (admin, officer, etc.) |

#### ImportItem
| Field | Description |
|-------|-------------|
| `itemId` | Primary key |
| `name` | Item name |
| `quantity` | Number of units |
| `value` | Declared monetary value |

#### Invoice
| Field | Description |
|-------|-------------|
| `invoiceId` | Primary key |
| `totalAmount` | Total amount due |
| `status` | Invoice status |

#### Payment
| Field | Description |
|-------|-------------|
| `paymentId` | Primary key |
| `amount` | Amount paid |
| `date` | Payment date |
| `status` | Payment status |

#### Tax
| Field | Description |
|-------|-------------|
| `taxId` | Primary key |
| `taxRate` | Applicable tax rate (%) |
| `calculatedAmount` | Computed tax value |

### 7.2 Entity Relationships

- **One-to-One:** `Invoice` ↔ `Payment`
- **One-to-Many:** `User` → `ImportItems`
- **Many-to-Many:** `ImportItem` ↔ `Tax`

---

## 8. DAO Layer

Each entity has a corresponding:
- **DAO Interface** — defines standard CRUD method signatures
- **DAO Implementation** — implemented using Hibernate

### DAO Classes

| DAO Class | Entity |
|-----------|--------|
| `UserDAO` | User |
| `ImportItemDAO` | ImportItem |
| `InvoiceDAO` | Invoice |
| `PaymentDAO` | Payment |

---

## 9. RMI Service Layer

The server exposes a remote interface with the following methods:

| Method | Description |
|--------|-------------|
| `addImportItem()` | Registers a new imported item |
| `calculateTax()` | Computes tax for an item |
| `generateInvoice()` | Creates an invoice |
| `recordPayment()` | Records a payment transaction |
| `getAllItems()` | Retrieves all import records |

**Server Port:** `5000`

---

## 10. Client Application

The Swing-based client includes the following screens:

| Screen | Description |
|--------|-------------|
| Login Form | User authentication with OTP verification |
| Dashboard | Overview and navigation hub |
| Import Item Form | Register new goods |
| Invoice Management | View and manage invoices |
| Payment Form | Record payments |
| Reports Screen | View data in JTable, export results |

### Key UI Components

- `JTable` — for displaying tabular records
- `JOptionPane` — for alerts and confirmation messages
- RMI stub — for communicating with the remote server

---

## 11. Validation Rules

### 11.1 Business Rules

| Rule | Description |
|------|-------------|
| Tax rate | Must be greater than 0 |
| Item value | Must be a positive number |
| Payment amount | Must match the invoice total |
| Clearance | Goods cannot be cleared before full payment |
| Invoices | Duplicate invoices are not permitted |

### 11.2 Technical Rules

| Rule | Description |
|------|-------------|
| Required fields | All mandatory fields must be completed |
| Numeric fields | Must contain valid numeric values |
| Database | Connection must be active before operations |
| Authentication | User must be authenticated before access |
| RMI server | Must be reachable for any client operation |

---

## 12. Reporting and Export

### Supported Export Formats

- **CSV** — comma-separated values for spreadsheet use
- **PDF** — formatted document for printing or archiving

### Report Types

- List of all imported goods
- Payment history and status
- Tax calculation summaries

---

## 13. Notification System

The system simulates notifications using a message broker (ActiveMQ or RabbitMQ):

| Event | Notification |
|-------|-------------|
| User login | OTP sent to user (simulated) |
| Payment confirmed | "Payment successful" notification |

> **Note:** All notifications are simulated. No real SMS or email services are used.

---

## 14. Conclusion

The **Import Tax Management System** delivers a scalable and efficient solution for managing import tax processes using modern Java technologies. By integrating:

- **Java RMI** for distributed communication
- **Hibernate ORM** for reliable data persistence
- **Java Swing** for an accessible GUI
- **MVC and DAO** design patterns for clean architecture

...the system achieves maintainability, performance, and usability suitable for a customs or financial management environment.
