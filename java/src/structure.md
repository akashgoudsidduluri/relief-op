# Relief-OP: Java Project Structure & UI Architecture

> **All business rules are enforced at the database level to ensure consistency, concurrency safety, and auditability.**

This document outlines the **thin-client** architecture of the **Relief-OP** Java Swing application.

---

## 🏛️ 1. Main Entry Points
- **`Main.java`**: Bootstrap class that launches the initial `LoginFrame`.
- **`LoginFrame.java`**: 
    - **Authentication**: Validates users against the `Users` table.
    - **Session Variable Duty**: Executes `SET @current_user_id = ?` to established a secure database session. **This is executed per-connection/session to ensure accurate logging and auditability.**
    - **Role Handover**: Passes the `role` (Admin, Operator, Coordinator) to the `MainFrame`.

---

## 🏗️ 2. Core UI Layout (`MainFrame.java`)
- **Role Enforcement Strategy**: Role enforcement is primarily handled at the application layer through dynamic tab accessibility.
    - **Operator**: `Victims`, `Requests`, `Insights`, `Map Dashboard`.
    - **Coordinator**: `Shelters`, `Resources`, `Insights`, `Map Dashboard`.
    - **Admin**: All modules including `Personnel` and `Suppliers`.
- **Global Theme (`Theme.java`)**: Centralized styling for dark-mode premium aesthetics.

---

## 🧠 3. Logic: Java vs SQL
Java acts as a **Thin Interface** only. No complex business rules reside in the Java code.

| Feature               | Java Role                             | SQL Role                                     |
|-----------------------|---------------------------------------|----------------------------------------------|
| **Request Processing**    | Trigger `CALL process_request`        | Transactional logic, stock deduction, state machine |
| **Batch Processing**      | Trigger `CALL process_all_high_priority` | Controlled batch execution (non-atomic), fault tolerance |
| **Resource Alerts**       | Display `view_low_stock_alerts`       | Threshold evaluation based on settings      |
| **Planning Insights**     | Display `view_demand_trends`          | Predictive aggregation (Top 3 Demanded)     |
| **Safety Constraints**    | UI Parsing of `ERR:` error messages   | `BEFORE` triggers and `CHECK` constraints   |

---

## 📦 4. Panels Package (`src/panels/`)
- **`InsightsPanel.java`**: Strategic dashboard. Features "Best-Effort" batch processing for high-priority needs.
- **`RequestPanel.java`**: Workflow module. Logic delegates to SQL stored procedures.
- **`MapsPanel.java` & `MapController.java`**: GIS bridge to Leaflet.js for spatial coordination.

---

## 🛡️ 5. Database Consistency
All panels use **`SwingWorker`** for non-blocking interaction. The system follows a strict **Refresh-All** rule: immediately after any successful stored procedure execution, all active data listeners are updated to ensure UI-DB consistency.