# Implementation Plan — Secure Role-Based Access Control

This plan details how we will implement role-based access control (RBAC) and UI visibility modifications across the distributed Java Swing + Java RMI system, addressing the server-side compilation blocker and building out client-side page modifications.

---

## Technical Approach & Safety Adjustments

To ensure absolute system stability and prevent regressions:
1. **Server (RMI Service)**: Fix the compilation error in `ImportItemServiceImpl.java` by performing the database deletion operation directly inside the `execute` wrapper callback, rather than calling the RMI-exposed `deleteItem(itemId)` method which throws a checked `RemoteException`.
2. **Client (Swing UI)**:
   - Filter Dashboard quick actions based on permissions, using a `FlowLayout` with buttons set to a uniform width of `180` to prevent alignment issues.
   - Restrict action buttons and table double-click triggers in `ImportItemPage.java`, `TaxPage.java`, and `InvoicePage.java` based on role permissions.
   - Overload `ImportItemDialog` to support a dedicated view-only mode for `FINANCE_OFFICER`.
   - Prevent `CUSTOMS_OFFICER` from selecting `CLEARED` status for unpaid items.
   - Use secure wrapper methods for payments and users management.

---

## Proposed Changes

### Server Project

#### [MODIFY] [ImportItemServiceImpl.java](file:///c:/Users/USER/Documents/NetBeansProjects/importProject/ImportTaxSystemServer26991/src/main/java/com/importtax/server/rmi/impl/ImportItemServiceImpl.java)
- Update `deleteItemSecure` to delete via database layer directly inside the `execute` wrapper instead of calling `deleteItem(itemId)`.

---

### Client Project

#### [MODIFY] [DashboardPage.java](file:///c:/Users/USER/Documents/NetBeansProjects/importProject/ImportTaxSystemClient26991/src/main/java/com/importtax/client/ui/DashboardPage.java)
- Replace `GridLayout(1, 4)` in quick actions with `FlowLayout(FlowLayout.LEFT, 12, 0)`.
- Set action button preferred width to `180` (instead of `0`) so they render correctly in `FlowLayout`.
- Dynamically add buttons based on `CurrentSession` roles.

#### [MODIFY] [ImportItemPage.java](file:///c:/Users/USER/Documents/NetBeansProjects/importProject/ImportTaxSystemClient26991/src/main/java/com/importtax/client/ui/ImportItemPage.java)
- In constructor/initialization, hide `addButton`, `editButton`, `deleteButton` based on permissions.
- In row double-click listener, open `ImportItemDialog` in read-only mode if the caller is a `FINANCE_OFFICER`.

#### [MODIFY] [ImportItemDialog.java](file:///c:/Users/USER/Documents/NetBeansProjects/importProject/ImportTaxSystemClient26991/src/main/java/com/importtax/client/ui/ImportItemDialog.java)
- Add a constructor overload `ImportItemDialog(JFrame owner, ImportItem item, boolean isReadOnly)`.
- If `isReadOnly`, set input fields to non-editable, disable status dropdown, hide Save/Reset buttons, and rename Cancel to "Close".
- For `CUSTOMS_OFFICER` when status is not `PAID`, exclude `CLEARED` from the status combo box.

#### [MODIFY] [PaymentPage.java](file:///c:/Users/USER/Documents/NetBeansProjects/importProject/ImportTaxSystemClient26991/src/main/java/com/importtax/client/ui/PaymentPage.java)
- Modify save/update/delete calls to use `savePaymentSecure`, `updatePaymentSecure`, and `deletePaymentSecure`.

#### [MODIFY] [UsersPage.java](file:///c:/Users/USER/Documents/NetBeansProjects/importProject/ImportTaxSystemClient26991/src/main/java/com/importtax/client/ui/UsersPage.java)
- Update `ROLES` array to: `{"ADMIN", "CUSTOMS_OFFICER", "FINANCE_OFFICER"}`.
- Adapt color-switch styling for the new roles list.
- Call `updateUserSecure` and `deleteUserSecure` on edit and delete operations.

#### [MODIFY] [RegisterPanel.java](file:///c:/Users/USER/Documents/NetBeansProjects/importProject/ImportTaxSystemClient26991/src/main/java/com/importtax/client/ui/RegisterPanel.java)
- Filter `roleComboBox` to exclude `ADMIN` from the registration roles.

#### [MODIFY] [TaxPage.java](file:///c:/Users/USER/Documents/NetBeansProjects/importProject/ImportTaxSystemClient26991/src/main/java/com/importtax/client/ui/TaxPage.java)
- Hide/remove `addBtn`, `editBtn`, `deleteBtn` for non-admins.
- Disable table double-click edit listener for non-admins.

#### [MODIFY] [InvoicePage.java](file:///c:/Users/USER/Documents/NetBeansProjects/importProject/ImportTaxSystemClient26991/src/main/java/com/importtax/client/ui/InvoicePage.java)
- Hide/remove `addBtn`, `editBtn`, `deleteBtn` for non-admins.
- Disable table double-click edit listener for non-admins.

---

## Verification Plan

### Automated Build Verification
- Compile both projects using Maven commands:
  ```powershell
  mvn -f ImportTaxSystemServer26991/pom.xml clean compile
  mvn -f ImportTaxSystemClient26991/pom.xml clean compile
  ```
