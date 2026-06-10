# OfficeSync

OfficeSync is a Java Swing inventory system for managing office supplies and supply requests. It uses MySQL for storage and Maven for the MySQL Connector/J dependency.

## Main Features

- Login with role-based access.
- Dashboard summary for supplies, low-stock items, pending requests, and role scope.
- Supplies management for inventory records.
- Request submission, viewing, approval, rejection, and deletion.
- Reports for low-stock and pending-request summaries.

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
- `OfficeSyncDatabase.findAllSupplies(...)`, `addSupply(...)`, `updateSupply(...)`, and `deleteSupply(...)` manage inventory records.
- `OfficeSyncDatabase.findVisibleRequests(...)`, `submitRequest(...)`, `updateRequestStatus(...)`, and `deleteRequest(...)` manage requests.

The database helper returns model objects such as `User`, `Supply`, and `SupplyRequest`. This keeps database code separate from the Swing UI while keeping the number of Java files smaller.

## Database Setup

1. Start MySQL.
2. Run `database/officesync.sql` in MySQL Workbench, phpMyAdmin, or the MySQL command line.
3. Check the credentials at the top of `src/main/java/Database/OfficeSyncDatabase.java`.
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
| Department Head | `head@officesync.local` |
| Employee | `employee@officesync.local` |
| Employee | `maria@officesync.local` |

For a deeper database explanation, see `docs/DATABASE.md`.

## Role Rules

- Admin can see all requests and manage supplies.
- Department Head can see requests from users in the same department.
- Employee can see only their own requests.

These rules are applied in `OfficeSyncDatabase.findVisibleRequests(...)` and `OfficeSyncDatabase.countPendingRequestsFor(...)`.
