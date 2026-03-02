# 🏢 Enterprise Procurement Management System (EPMS)
### *A production-grade backend simulating SAP MM module — built with Java 21, Spring Boot 3.5 & OData V4*

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3.5.10-brightgreen?logo=springboot&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Spring_Security-6.x-brightgreen?logo=springsecurity&logoColor=white" alt="Spring Security"/>
  <img src="https://img.shields.io/badge/OData_V4-Apache_Olingo-blue" alt="OData V4"/>
  <img src="https://img.shields.io/badge/MySQL-8.x-blue?logo=mysql&logoColor=white" alt="MySQL"/>
  <img src="https://img.shields.io/badge/Docker-Deployed-2496ED?logo=docker&logoColor=white" alt="Docker"/>
  <img src="https://img.shields.io/badge/deploy-Render-46E3B7?logo=render&logoColor=white" alt="Render"/>
</p>

<p align="center">
  <a href="https://procurement-system-z7vj.onrender.com/procurement/swagger-ui/index.html" target="_blank">
    <img src="https://img.shields.io/badge/📖_Live_API_Docs-Swagger_UI-85EA2D?style=for-the-badge&logo=swagger&logoColor=black" alt="Swagger UI"/>
  </a>
  &nbsp;&nbsp;
  <a href="https://epms-portal.vercel.app" target="_blank">
    <img src="https://img.shields.io/badge/🌐_Live_Demo-Vercel-000000?style=for-the-badge&logo=vercel&logoColor=white" alt="Live Demo"/>
  </a>
</p>

---

## 📌 What is this project?

EPMS is a **full-stack enterprise web application** that simulates the **SAP MM (Materials Management)** module used in real-world ERP systems. It was built to demonstrate enterprise-grade backend development skills including multi-level approval workflows, OData V4 integration, and strict role-based access control — all of which are core requirements for software development roles at companies like **Bosch, SAP, and Siemens**.

> ⚠️ **Note on Free Hosting:** The backend runs on Render's free tier and may take **~50 seconds to wake up** after inactivity. Please be patient on the first request.

---

## ✨ Key Features

| Feature | Description |
|---------|-------------|
| 🔐 **JWT Authentication** | Stateless auth via JJWT 0.12.6 with custom entry point handlers |
| 👥 **RBAC (3 roles)** | ADMIN, MANAGER, EMPLOYEE — each with distinct data scopes and permissions |
| 📄 **5-step PO Workflow** | `CREATED → PENDING → APPROVED → RECEIVED / REJECTED / CANCELLED` |
| 📦 **Goods Receipt (GR)** | Stock auto-updated on receive; low-stock warnings on materisl threshold breach |
| 🌐 **OData V4 API** | Full `$filter`, `$orderby`, `$top/$skip`, `$count` support via Apache Olingo |
| 📸 **Snapshot Pattern** | Material details frozen at PO creation time for immutable audit trail |
| 📊 **Analytics API** | Dashboard, PO status summary, top vendors, monthly trend |
| 🔢 **Auto PO Numbering** | Format: `PO-{YYYY}-{XXXXXX}`, auto-resets each year |
| 🧹 **Soft Delete** | Vendors & Materials deactivated instead of hard-deleted |
| 📝 **Swagger UI** | Complete interactive API documentation, live on production |

---

## 🏗️ System Architecture

```
                         ┌───────────────────────┐
  Browser / API Client   │    React SPA (Vercel)  │
                         └──────────┬────────────┘
                                    │ HTTPS (REST + OData)
                         ┌──────────▼────────────┐
                         │  Spring Boot Backend   │  ← Java 21 / Docker / Render
                         │  (procurement-system)  │
                         └──────────┬────────────┘
                                    │ JDBC
                         ┌──────────▼────────────┐
                         │   MySQL 8 (Railway)    │  ← Cloud-hosted database
                         └───────────────────────┘
```

### Layered Architecture (MVC / Clean)

```
Controller Layer   ← REST + OData endpoints, input validation
        ↓
Service Layer      ← Business logic, workflow state machine
        ↓
Repository Layer   ← Spring Data JPA, custom JPQL queries
        ↓
Entity Layer       ← JPA Entities with JPA Auditing (BaseEntity)
```

---

## 🔐 Security & RBAC

### Authentication Flow (Stateless JWT)

```
POST /procurement/auth/login  →  { username, password }
                              ←  { token: "eyJ..." }

Subsequent requests:
Header: Authorization: Bearer eyJ...
        ↓
JwtAuthenticationFilter → Validates token → Sets SecurityContext
```

### Role-Based Permissions

| Action | ADMIN | EMPLOYEE | MANAGER |
|--------|-------|----------|---------|
| Vendor/Material CRUD | ✅ Full | 👁️ Read | 👁️ Read |
| Create / Edit PO | ❌ | ✅ Own only | ❌ |
| Submit / Delete PO | ❌ | ✅ Own + CREATED | ❌ |
| Approve / Reject PO | ❌ | ❌ | ✅ PENDING only |
| Analytics Dashboard | ✅ | ❌ | ✅ |

> Ownership is enforced at the service layer: Employees can only mutate **their own** purchase orders.

---

## 🌐 OData V4 — SAP Integration Ready

Implemented using **Apache Olingo 4.10.0**, the official SAP community library.

**Base URL:** `https://procurement-system-z7vj.onrender.com/procurement/odata/`

| Entity Set | Supported Options |
|------------|------------------|
| `Vendors` | `$filter`, `$orderby`, `$top`, `$skip`, `$count` |
| `Materials` | `$filter`, `$orderby`, `$top`, `$skip`, `$count` |
| `PurchaseOrders` | `$filter`, `$orderby`, `$top`, `$skip`, `$count` |

```bash
# Example OData queries
GET /odata/Vendors?$filter=vendorCategory eq 'DOMESTIC'&$orderby=rating desc&$top=5
GET /odata/PurchaseOrders?$filter=status eq 'PENDING'&$count=true
GET /odata/$metadata   # SAP Fiori uses this to auto-generate UI
```

---

## 📦 Purchase Order Workflow

```
                   ┌─────────┐
       [submit()]  │ CREATED │ ←── Employee creates PO
       ──────────► │  (Draft) │
                   └────┬────┘
                        │
                        ▼
                   ┌─────────┐
      [approve()]  │ PENDING │ ──[reject()]──► REJECTED (terminal)
       ──────────► │         │
                   └────┬────┘
                        │
                        ▼
                   ┌──────────┐
      [receive()]  │ APPROVED │
       ──────────► │          │
                   └────┬─────┘
                        │
                        ▼
                   ┌──────────┐
                   │ RECEIVED │  ← Stock auto-updated (Goods Receipt)
                   └──────────┘
```

### Item-Level Tax Calculation

```
netAmount  = quantity × unitPrice
taxAmount  = netAmount × taxRate  (configurable per line item, default 10%)
lineTotal  = netAmount + taxAmount

grandTotal = SUM(all lineTotal)
```

---

## 🛠️ Tech Stack

| Category | Technology | Version |
|----------|-----------|---------|
| Language | Java | 21 (LTS) |
| Framework | Spring Boot | 3.5.10 |
| Security | Spring Security | 6.x |
| JWT | JJWT | 0.12.6 |
| ORM | Spring Data JPA / Hibernate | — |
| OData | Apache Olingo | 4.10.0 |
| Database | MySQL | 8.x |
| DTO Mapping | MapStruct | 1.6.3 |
| Code Gen | Lombok | — |
| API Docs | SpringDoc OpenAPI (Swagger UI) | 2.8.5 |
| Build | Maven | — |
| Container | Docker (multi-stage build) | — |
| Hosting — DB | Railway.app | Cloud MySQL |
| Hosting — BE | Render.com | Docker deploy |
| Hosting — FE | Vercel | SPA deploy |

---

## 🧩 Design Patterns Applied

| Pattern | Where Used | Purpose |
|---------|------------|---------|
| **DTO Pattern** | Request / Response objects | Decouple API contract from DB schema |
| **Mapper Pattern** | MapStruct | Auto-convert Entity ↔ DTO |
| **Snapshot Pattern** | `PurchaseOrderItem` | Freeze material data for audit integrity |
| **Soft Delete Pattern** | Vendor, Material | Preserve historical data |
| **Repository Pattern** | Spring Data JPA | Abstract data access layer |
| **Builder Pattern** | Lombok `@Builder` | Safe, readable object construction |
| **Strategy Pattern** | Security Filter Chain | Flexible, pluggable security config |

---

## 📊 API Overview (~40 endpoints)

| Module | Endpoints |
|--------|-----------|
| Authentication | 1 |
| User Management & Profile | 10 |
| Vendor Management | 5 |
| Material Management | 6 |
| Purchase Order Management | 8 |
| Analytics & Reporting | 5 |
| OData V4 (Vendors, Materials, POs) | 5 |
| **Total** | **~40 endpoints** |

👉 **Full interactive docs:** [Swagger UI](https://procurement-system-z7vj.onrender.com/procurement/swagger-ui/index.html)

---

## 🚀 Getting Started (Local)

### Prerequisites

- Java 21+
- Maven 3.9+
- MySQL 8.x (or use the cloud Railway DB)

### 1. Clone the repository

```bash
git clone https://github.com/TuanAnhVu81/procurement-system.git
cd procurement-system
```

### 2. Configure environment

Create a `.env` file in the project root:

```env
DB_URL=jdbc:mysql://localhost:3306/procurement_system
DB_USERNAME=root
DB_PASSWORD=your_password

JWT_SIGNER_KEY=your_256bit_base64_secret
JWT_EXPIRATION=86400000

ADMIN_USERNAME=admin
ADMIN_PASSWORD=123456a
ADMIN_EMAIL=admin@epms.com

MANAGER_USERNAME=manager
MANAGER_PASSWORD=123456m
MANAGER_EMAIL=manager@epms.com
```

### 3. Run the application

```bash
./mvnw spring-boot:run
```

Application starts at: `http://localhost:8080/procurement`

### 4. Access Swagger UI

```
http://localhost:8080/procurement/swagger-ui/index.html
```

---

## 🧪 Testing

Unit tests implemented using **JUnit 5** and **Mockito** covering service layer business logic and security rules.

```bash
./mvnw test
```

---

## 🌍 Deployment Architecture

```
┌──────────────────────────────────────────────────────┐
│                  Free Cloud Stack                    │
│                                                      │
│  ┌─────────────┐   REST / OData   ┌───────────────┐ │
│  │    Vercel   │◄────────────────►│    Render     │ │
│  │  (FE / SPA) │                  │ (BE / Docker) │ │
│  └─────────────┘                  └──────┬────────┘ │
│     React 18                             │ JDBC       │
│     Vite                          ┌──────▼────────┐ │
│                                   │   Railway.app  │ │
│                                   │  (MySQL 8.x)  │ │
│                                   └───────────────┘ │
│                                   Total cost: $0/mo  │
└──────────────────────────────────────────────────────┘
```

---

## 📁 Project Structure

```
procurement-system/
├── src/
│   ├── main/java/com/anhvt/epms/procurement/
│   │   ├── configuration/    # Security, CORS, JPA Auditing, CORS
│   │   ├── controller/       # REST & OData controllers
│   │   │   └── odata/        # Apache Olingo OData V4 handlers
│   │   ├── dto/              # Request / Response DTOs
│   │   ├── entity/           # JPA Entities (BaseEntity + domain)
│   │   ├── enums/            # POStatus, VendorCategory, MaterialType ...
│   │   ├── mapper/           # MapStruct mappers
│   │   ├── repository/       # Spring Data JPA repositories
│   │   ├── security/         # JWT filter, entry point, access handler
│   │   └── service/          # Business logic (interface + impl)
│   └── resources/
│       └── application.yaml  # App configuration (env-driven)
├── Dockerfile                # Multi-stage Docker build
├── pom.xml
└── README.md
```

---

## 🔗 Related Repositories

| Repo | Description |
|------|-------------|
| **This repo** — Backend | Java 21 / Spring Boot / OData V4 API |
| [epms-frontend](https://github.com/TuanAnhVu81/epms-frontend) | React 18 / Vite / Ant Design SPA |

---

## 👤 Author

**Tuan Anh Vu**
- GitHub: [@TuanAnhVu81](https://github.com/TuanAnhVu81)
- Project in active development — Jan 2026 → Present

---

<p align="center">
  <i>Built with ❤️ to simulate real-world SAP MM procurement workflows.<br/>Designed to showcase enterprise Java development for SAP/ERP-oriented internship applications.</i>
</p>
