# OfficeSync

OfficeSync is a Java Swing inventory system for managing office supplies and supply requests. It uses MySQL for storage and Maven for the MySQL Connector/J dependency.

## Main Features

- Login with role-based access.
- Dashboard summary for supplies, low-stock items, pending requests, and role scope.
- Supplies management for inventory records, including inactive status when stock is 0.
- Request submission, viewing, approval/rejection by Admin, and deletion.
- Notifications for low-stock items, out-of-stock items, and pending request details.

## Project Structure

```text
OfficeSync/
  database/
    officesync.sql              Database schema and sample data
  src/main/java/
    com/mycompany/officesync/   Application entry point
    controls/                   JFrame windows
    panels/                     Swing screens inside the windows
    Database/                   Database access classes
    models/                     Plain Java data objects
    constants/                  Shared UI, validation, password, and DB config helpers
    dialogs/                    Shared dialog helper
```

## How The App Is Organized

The app starts from `OfficeSync.java`, opens the login page, then shows the dashboard after a successful login.

The UI panels do not open MySQL connections directly. They call one compact database helper:

- `OfficeSyncDatabase.authenticate(...)` checks login credentials.
- `OfficeSyncDatabase.findAllSupplies(...)`, `addSupply(...)`, `updateSupply(...)`, and `deleteSupply(...)` manage inventory records and keep `is_available` in sync with stock.
- `OfficeSyncDatabase.findVisibleRequests(...)`, `submitRequest(...)`, `updateRequestStatus(...)`, and `deleteRequest(...)` manage requests.

The database helper returns model objects such as `User`, `Supply`, and `SupplyRequest`. This keeps database code separate from the Swing UI while keeping the number of Java files smaller.

## Database Setup

1. Start MySQL.
2. Run `database/officesync.sql` in MySQL Workbench, phpMyAdmin, or the MySQL command line.
3. Check the credentials at the top of `src/main/java/Database/OfficeSyncDatabase.java`.

If you already created the database before the `is_available` field was added, run `database/add_is_available_to_supplies.sql` once instead of rebuilding the whole database.
If your database still has `Department Head` users, run `database/combine_department_head_with_employee.sql` once to convert them to `Employee`.
4. Build/run the project with Maven or NetBeans.

Default local database settings:

```java
URL: jdbc:mysql://localhost:3306/officesync?useSSL=false&serverTimezone=Asia/Manila
USER: root
PASSWORD: empty string
```

Default login accounts all use password `1234`:

| Role | Email |
| --- | --- |
| Admin | `admin@officesync.local` |
| Employee | `head@officesync.local` |
| Employee | `employee@officesync.local` |
| Employee | `maria@officesync.local` |

For a deeper database explanation, see `docs/DATABASE.md`.

## Role Rules

- Admin can see all requests and manage supplies.
- Admin can approve and reject requests.
- Employee can see only their own requests and cannot approve or reject.

These rules are applied in `OfficeSyncDatabase.findVisibleRequests(...)` and `OfficeSyncDatabase.countPendingRequestsFor(...)`.
