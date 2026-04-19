# Relief-OP: SQL Decision Engine (Transactional Design)

> **All business rules are enforced at the database level to ensure consistency, concurrency safety, and auditability.**

This document outlines the database-level intelligence and architecture of the **Relief-OP** Disaster Management System.

---

## 🏗️ 1. Core Schema (3NF Normalized)
The system uses a strictly normalized schema to ensure data integrity.

| Table      | Primary Key | Foreign Key Behavior | Purpose |
|------------|-------------|-----------------------|---------|
| `Users`      | `user_id`     | -                     | Role-based authentication (Admin, Operator, Coordinator) |
| `Victims`    | `victim_id`   | -                     | Disaster profiling with severity levels (1-5) |
| `Shelters`   | `shelter_id`  | -                     | Occupancy and capacity tracking |
| `Resources`  | `resource_id` | -                     | Inventory management with low-stock thresholds |
| `Requests`   | `request_id`  | `ON DELETE CASCADE` (Victims) | Atomic request workflow with `remaining_quantity` |
| `Logs`       | `log_id`      | `ON DELETE SET NULL` (Users) | Forensic audit trail with before/after snapshots |
| `Personnel`  | `person_id`   | `ON DELETE SET NULL` (Shelters) | Human resource coordination |
| `Settings`   | `setting_key` | -                     | Global system configurations (e.g. `low_stock_threshold`) |

---

## 🧠 2. The Decision Engine (Stored Procedures)

### `process_request(req_id)`
- **Transaction Control**: Explicitly uses `START TRANSACTION` and `COMMIT/ROLLBACK` for strict concurrency control.
- **Idempotency**: Uses `FOR UPDATE` to lock rows and prevent race conditions.
- **Decision Path**:
    1. If `stock >= requested`: Full fulfillment.
    2. If `0 < stock < requested`: **Partial Fulfillment**. Stock is depleted, and the `remaining_quantity` is stored as an informational backorder.
    3. If `0 stock`: Rejection.
- **Guards**: Standardized `ERR:` signaling for Java-side parsing.

### `process_all_high_priority()`
- **Batch Logic**: Controlled batch execution with fault tolerance (non-atomic).
- **Failure Resilience**: Implements `DECLARE CONTINUE HANDLER` to ensure the batch continues even if individual requests fail (e.g. stock running out mid-batch).

---

## 🛡️ 3. Safety Layer (Triggers & Constraints)

- **`trig_audit_requests`**: Forensic logging that captures `old_status`, `new_status`, `qty_before`, `qty_after`, and `action_type`.
- **`trig_validate_shelter_capacity`**: Enforces `occupancy <= capacity` with custom SQL signals.
- **Data Integrity**: Enforces `severity_level [1-5]` and `quantity >= 0` at the database level.

---

## 📈 4. Intelligent Views (Insights Layer)

1.  **`view_priority_requests`**: Live queue of urgent needs sorted by severity.
2.  **`view_resource_pressure`**: Predictive indicator showing `Demand / (Supply + 1) %`. (Safeguarded against division by zero using `NULLIF`).
3.  **`view_demand_trends`**: Aggregates `SUM(quantity_requested)` by `resource_id` to provide proactive stock insights.

---

## 🔑 5. Role-Based Access (RBAC)
- **Strategy**: Role enforcement is primarily handled at the application layer (Java), with database procedures assuming authenticated access and session metadata.
- **Audit**: Every action is stamped with the `user_id` via the `@current_user_id` session variable.
