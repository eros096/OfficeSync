# OfficeSync Database Guide

This guide explains the MySQL database used by OfficeSync and how the Java code uses it.

## Database Name

```sql
officesync
```

The schema and sample data are in:

```text
database/officesync.sql
```

Existing databases can be updated without deleting data by running:

```text
database/add_is_available_to_supplies.sql
database/combine_department_head_with_employee.sql
```

## Tables

### departments

Stores the company departments used to group users.

| Column | Purpose |
| --- | --- |
| `department_id` | Primary key. |
| `department_name` | Unique department name. |

### users

Stores login accounts and role information.

| Column | Purpose |
| --- | --- |
| `user_id` | Primary key. |
| `full_name` | User display name. |
| `email` | Login email, unique. |
| `password_hash` | SHA-256 password hash. |
| `role` | `Admin` or `Employee`. |
| `department_id` | Foreign key to `departments`. |

The Java `User.Role` enum uses `Admin` and `Employee`. Existing `Department Head` rows are treated as `Employee` for compatibility.

### supplies

Stores inventory items.

| Column | Purpose |
| --- | --- |
| `supply_id` | Primary key. |
| `supply_name` | Supply/item name. |
| `category` | Group such as Paper, Writing, Filing. |
| `quantity_in_stock` | Current stock count. |
| `reorder_level` | Minimum desired stock level. |
| `is_available` | `TRUE` when stock is greater than 0, otherwise `FALSE`. |

The app treats a supply as `Not Active` when:

```sql
quantity_in_stock = 0
```

The app treats a supply as `Low Stock` when:

```sql
is_available = TRUE AND quantity_in_stock <= reorder_level
```

### requests

Stores the request header: who requested, when, and current status.

| Column | Purpose |
| --- | --- |
| `request_id` | Primary key. |
| `user_id` | Foreign key to the requesting user. |
| `request_date` | Date the request was submitted. |
| `status` | `Pending`, `Approved`, or `Rejected`. |

### request_details

Stores the requested item and quantity for a request.

| Column | Purpose |
| --- | --- |
| `request_detail_id` | Primary key. |
| `request_id` | Foreign key to `requests`. |
| `supply_id` | Foreign key to `supplies`. |
| `quantity_requested` | Quantity requested by the user. |

The current app creates one detail row per request. The table design still allows future support for multiple supplies in one request.

## Relationships

```mermaid
erDiagram
    departments ||--o{ users : has
    users ||--o{ requests : submits
    requests ||--o{ request_details : contains
    supplies ||--o{ request_details : requested_as

    departments {
        int department_id PK
        varchar department_name
    }
    users {
        int user_id PK
        varchar full_name
        varchar email
        varchar password_hash
        varchar role
        int department_id FK
    }
    supplies {
        int supply_id PK
        varchar supply_name
        varchar category
        int quantity_in_stock
        int reorder_level
        boolean is_available
    }
    requests {
        int request_id PK
        int user_id FK
        date request_date
        varchar status
    }
    request_details {
        int request_detail_id PK
        int request_id FK
        int supply_id FK
        int quantity_requested
    }
```

## How Java Uses The Database

OfficeSync uses one compact database class:

```text
src/main/java/Database/OfficeSyncDatabase.java
```

This file contains the connection settings, connection helper, password hashing, and the login, supply, and request database methods. It keeps the project closer to the compact style of the Hospital Management System project while still keeping SQL separate from the Swing UI.

### Login Methods

`OfficeSyncDatabase.authenticate(email, password)` hashes the typed password with SHA-256 and compares it to `users.password_hash`. It joins `users` with `departments` so the logged-in `User` object includes the department name.

### Supply Methods

`OfficeSyncDatabase` handles inventory records with these methods:

- `findAllSupplies()` lists supplies.
- `findLowStockSupplies()` lists active supplies where stock is less than or equal to reorder level.
- `addSupply(...)` inserts a new supply and stores `is_available` based on stock.
- `updateSupply(...)` edits a supply and updates `is_available` based on stock.
- `deleteSupply(...)` deletes a supply.
- `countSupplies()` and `countLowStockSupplies()` feed dashboard/report cards.

### Request Methods

`OfficeSyncDatabase` handles supply requests with these methods:

- `findVisibleRequests(user)` lists requests based on the logged-in user's role.
- `submitRequest(...)` inserts into both `requests` and `request_details` inside one transaction.
- `updateRequestStatus(...)` changes request status to approved/rejected for Admin users.
- `deleteRequest(...)` deletes detail rows first, then the request row, inside one transaction.
- `countPendingRequestsFor(user)` counts pending requests using the same role visibility rules.

## Role-Based Request Visibility

| Role | What They Can See |
| --- | --- |
| Admin | All requests. |
| Employee | Only their own requests. |

This logic is in `OfficeSyncDatabase.findVisibleRequests(...)`.

## Important Notes

- The SQL script drops and recreates tables, so running it resets the database.
- `request_details` depends on `requests` and `supplies`.
- `requests` depends on `users`.
- `users` depends on `departments`.
- Because of these foreign keys, delete child records before parent records.
