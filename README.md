<p align="center">
  <img src="https://img.shields.io/badge/Java-Swing-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/MySQL-8.0+-4479A1?style=for-the-badge&logo=mysql&logoColor=white"/>
  <img src="https://img.shields.io/badge/Leaflet.js-GIS_Maps-199900?style=for-the-badge&logo=leaflet&logoColor=white"/>
  <img src="https://img.shields.io/badge/Architecture-Thin_Client-blueviolet?style=for-the-badge"/>
</p>

<h1 align="center">🚨 Relief-OP</h1>
<h3 align="center">Disaster Relief Operations & Decision System</h3>

<p align="center">
  A role-based disaster management system built with <b>Java Swing</b> and a <b>SQL Decision Engine</b>.<br/>
  Designed for real-time coordination of victims, shelters, resources, and field personnel during crisis scenarios.
</p>

---

## 🎯 Overview

**Relief-OP** follows a **Thin-Client Architecture** — the Java application acts purely as a UI layer, while **100% of business logic** (request processing, stock management, safety constraints, audit logging) is enforced at the **MySQL database level** through stored procedures, triggers, and views.

This ensures:
- **Concurrency safety** via transactional locks (`FOR UPDATE`)
- **Data integrity** via `CHECK` constraints and `BEFORE` triggers
- **Full auditability** via forensic logging with session-based user tracking

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│                   Java Swing UI                      │
│  LoginFrame → MainFrame → Role-Based Panel Router    │
│                                                      │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌─────────┐ │
│  │ Victims  │ │ Requests │ │ Shelters │ │ Map/GIS │ │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬────┘ │
│       │             │            │             │      │
└───────┼─────────────┼────────────┼─────────────┼──────┘
        │             │            │             │
        ▼             ▼            ▼             ▼
┌─────────────────────────────────────────────────────┐
│              MySQL Decision Engine                   │
│                                                      │
│  Stored Procedures:  process_request()               │
│                      process_all_high_priority()     │
│  Triggers:           capacity validation             │
│                      stock safety nets               │
│                      forensic audit logging          │
│  Views:              priority queue, demand trends,  │
│                      stock pressure, heatmap data    │
└─────────────────────────────────────────────────────┘
```

---

## 👤 Role-Based Access

| Role | Modules | Mission |
|------|---------|---------|
| **Operator** | Victims, Requests, Map Dashboard | Field Triage & Emergency Response |
| **Coordinator** | Shelters, Resources, Volunteers, Helpers, Map Dashboard | Regional Logistics & Shelter Health |
| **Admin** | System-Wide Reports, Map Dashboard, Suppliers/Donors | Strategic Oversight & System Forensics |

---

## ⚙️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Frontend** | Java Swing (Dark Mode UI with custom `Theme.java`) |
| **Database** | MySQL 8.0+ with Stored Procedures, Triggers, Views |
| **GIS/Mapping** | Leaflet.js (launched via `MapController.java`) |
| **JDBC Driver** | MySQL Connector/J 9.6.0 |

---

## 🚀 Getting Started

### Prerequisites

- **Java JDK** 8 or higher
- **MySQL** 8.0+
- Git (optional)

### 1. Set Up the Database

```sql
-- Run the schema file in MySQL to create the database, tables, and seed data:
source sql/schema.sql;

-- (Optional) Inject geographic cluster data:
source sql/inject_clusters.sql;
```

### 2. Configure Database Connection

Edit the credentials in [`DBConnection.java`](java/src/panels/DBConnection.java):

```java
private static final String URL  = "jdbc:mysql://localhost:3306/reliefops";
private static final String USER = "root";
private static final String PASS = "your_password";
```

### 3. Compile & Run

```batch
cd java
run.bat
```

Or manually:

```bash
cd java/src
javac -cp ".;../lib/mysql-connector-j-9.6.0.jar" *.java panels/*.java
java -cp ".;../lib/mysql-connector-j-9.6.0.jar" Main
```

---

## 📁 Project Structure

```
Relief-OP/
├── java/
│   ├── lib/
│   │   └── mysql-connector-j-9.6.0.jar    # JDBC Driver
│   ├── src/
│   │   ├── Main.java                       # Bootstrap
│   │   ├── LoginFrame.java                 # Authentication & Session Init
│   │   ├── MainFrame.java                  # Role-based Tab Router
│   │   └── panels/
│   │       ├── Theme.java                  # Dark Mode Design System
│   │       ├── DBConnection.java           # Database Connector
│   │       ├── VictimPanel.java            # Victim Registration & Triage
│   │       ├── RequestPanel.java           # Request Workflow (→ SQL Procedures)
│   │       ├── ShelterPanel.java           # Shelter Capacity Management
│   │       ├── ResourcePanel.java          # Inventory Tracking
│   │       ├── VolunteerPanel.java         # Volunteer Coordination
│   │       ├── HelperPanel.java            # Helper Management
│   │       ├── SupplierPanel.java          # Donor / Supplier Intake
│   │       ├── InsightsPanel.java          # Strategic Reports & Batch Ops
│   │       ├── MapsPanel.java              # GIS Map Container
│   │       ├── MapController.java          # Leaflet.js Bridge & Heatmap
│   │       ├── OperatorDashboard.java      # Operator Home
│   │       ├── CoordinatorDashboard.java   # Coordinator Home
│   │       └── AdminDashboard.java         # Admin Home
│   └── run.bat                             # One-click compile & run
├── sql/
│   ├── schema.sql                          # Full DB schema + procedures + seed data
│   ├── inject_clusters.sql                 # Geographic cluster data
│   └── sql.MD                              # SQL architecture documentation
└── README.md
```

---

## 🗄️ SQL Decision Engine

### Stored Procedures

- **`process_request(req_id)`** — Transactional request fulfillment with `FOR UPDATE` row locking. Handles full fulfillment, partial fulfillment (with backorder tracking), and rejection.
- **`process_all_high_priority()`** — Batch processor for severity ≥ 4 requests with fault-tolerant `CONTINUE HANDLER`.

### Safety Triggers

- `trig_validate_shelter_capacity` — Prevents occupancy from exceeding capacity
- `trig_prevent_negative_stock` — Guards against negative resource quantities
- `trig_sync_shelter_stock_on_supply` — Auto-distributes supplier deliveries to regional and global pools
- `trig_audit_*` — Forensic logging for all entity changes

### Analytical Views

| View | Purpose |
|------|---------|
| `view_priority_requests` | Live queue of urgent needs sorted by severity |
| `view_low_stock_alerts` | Proactive monitoring against configurable thresholds |
| `view_demand_trends` | Top 3 most demanded resources |
| `view_current_stock_pressure` | Demand-to-supply pressure indicator |
| `view_shelter_utilization` | Occupancy rate analytics |
| `view_map_master_heat` | Geospatial heatmap data for the GIS layer |

---

## 🗺️ GIS Map Dashboard

The Map Dashboard uses **Leaflet.js** to provide a geospatial command view:

- **Shelter markers** with capacity/occupancy data
- **Victim markers** with severity-coded coloring
- **Heatmap overlays** for disaster intensity visualization
- **Pulsing hazard animations** for isolated/critical zones

---

## 📄 License

This project was built as a semester project for academic purposes.

---

<p align="center">
  Built with ☕ Java + 🐬 MySQL
</p>
