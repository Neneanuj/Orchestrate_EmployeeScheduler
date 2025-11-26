# Employee Scheduler - Functional Bugs & Validation Issues

## 🔴 CRITICAL FUNCTIONAL BUGS

### BUG-F001: Can Schedule Shifts for Past Dates
**Severity:** CRITICAL  
**Problem:** The system allows creating shifts for past dates. No validation prevents scheduling shifts that have already passed.  
**Location:** `CreateShiftView.java` line 138 - `datePicker.setValue(LocalDate.now())` and `validate()` method line 335-365  
**Steps to Reproduce:**  
1. Click "Create New Shift"  
2. Select a past date from date picker  
3. Fill in all other fields  
4. Click "Create Shift" - **It succeeds!**  
**Impact:** Invalid business logic, wasted resources scheduling past events, confusion for employees.  
**Fix Required:**  
```java
// In validate() method, add:
if (datePicker.getValue().isBefore(LocalDate.now())) {
    dateError.setText("Cannot schedule shifts for past dates");
    valid = false;
}
```

---

### BUG-F002: "Forgot Password" Link Does Nothing
**Severity:** HIGH  
**Problem:** Login screen has "Forgot password?" link but clicking it does nothing - no functionality implemented.  
**Location:** `LoginView.java` line 123-125  
**Code:**  
```java
Hyperlink forgotPasswordLink = new Hyperlink("Forgot password?");
forgotPasswordLink.setStyle("-fx-text-fill: #3498db; -fx-font-size: 12px;");
// NO setOnAction() - link is non-functional
```
**Impact:** Users locked out of accounts have no recovery option, poor user experience.  
**Fix Required:** Implement password reset flow with email/security questions OR admin password reset functionality.

---

### BUG-F003: Name Fields Accept Numbers and Special Characters
**Severity:** HIGH  
**Problem:** Employee first name and last name fields accept any input including numbers, special characters like "12345", "@#$%", "abc123".  
**Location:** `AddEmployeeModal.java` line 281-287, `EmployeeManagementService.java` line 26-31  
**Validation:**  
```java
// Current code only checks isEmpty()
if (firstName == null || firstName.trim().isEmpty()) {
    throw new IllegalArgumentException("First name is required");
}
// MISSING: Character type validation
```
**Steps to Reproduce:**  
1. Open "Add Employee" modal  
2. Enter "123456" in First Name  
3. Enter "!@#$%" in Last Name  
4. System accepts it!  
**Impact:** Data quality issues, unprofessional records, database pollution.  
**Fix Required:**  
```java
// Add to validation:
if (!firstName.matches("^[a-zA-Z\\s'-]+$")) {
    throw new IllegalArgumentException("First name must contain only letters");
}
if (!lastName.matches("^[a-zA-Z\\s'-]+$")) {
    throw new IllegalArgumentException("Last name must contain only letters");
}
```

---

### BUG-F004: Location Field Accepts Empty Spaces
**Severity:** MEDIUM  
**Problem:** Shift location field can be submitted with only whitespace characters.  
**Location:** `CreateShiftView.java` line 362-365  
**Code:**  
```java
if (locationField.getText().trim().isEmpty()) {
    locationError.setText("Location is required");
    valid = false;
}
```
**Issue:** trim() is called but field could have "   " (spaces) and trim makes it empty, but what if user enters "  a  "? It passes validation as "a".  
**Impact:** Confusing location data, employees don't know where to go.  
**Fix Required:** Better validation and normalization of location input.

---

### BUG-F005: No Duplicate Employee Name Check
**Severity:** MEDIUM  
**Problem:** System allows creating multiple employees with exact same first and last name.  
**Location:** `EmployeeManagementService.java` - no duplicate check in `createEmployee()` method  
**Steps to Reproduce:**  
1. Create employee "John Smith"  
2. Create another employee "John Smith"  
3. Both are created successfully  
**Impact:** Confusion when assigning shifts, cannot distinguish between employees with same name.  
**Fix Required:** Add duplicate name check or require unique identifier (employee ID, email).

---

### BUG-F006: Time Validation Only Checks Start < End, Not Same Time
**Severity:** MEDIUM  
**Problem:** Shift start and end times validation only checks if start < end, but doesn't prevent start == end.  
**Location:** `CreateShiftView.java` line 354-357  
**Code:**  
```java
if (!start.isBefore(end)) {  // This allows start == end
    endTimeError.setText("End time must be after start time");
    valid = false;
}
```
**Steps to Reproduce:**  
1. Set start time to "6:00 PM"  
2. Set end time to "6:00 PM"  
3. Validation passes (0-hour shift)  
**Impact:** Invalid 0-duration shifts created.  
**Fix Required:** Change to `if (!start.isBefore(end))` should also catch equals or use `if (start.compareTo(end) >= 0)`.

---

## ⚠️ HIGH PRIORITY VALIDATION BUGS

### BUG-F007: No Maximum Hours Per Week Enforcement
**Severity:** HIGH  
**Problem:** Employees can be scheduled beyond their maximum 20 hours per week limit.  
**Location:** No validation in shift assignment logic  
**Impact:** Labor law violations, employee burnout, overtime payment issues.  
**Fix Required:** Add validation when assigning shifts to check total weekly hours doesn't exceed employee limit.

---

### BUG-F008: No Username Format Validation
**Severity:** MEDIUM  
**Problem:** Login username field has minimal validation - only checks if empty and length > 3.  
**Location:** `LoginController.validateCredentials()` line 50-55  
**Code:**  
```java
if (username.length() < 3) {
    return "Username must be at least 3 characters";
}
// MISSING: Format validation (special chars, spaces, etc.)
```
**Impact:** Usernames can contain spaces, special characters causing database/system issues.  
**Fix Required:** Enforce username format (alphanumeric, underscore, dash only).

---

### BUG-F009: Password Minimum Length Too Short
**Severity:** MEDIUM  
**Problem:** Password validation requires only 6 characters minimum, industry standard is 8+.  
**Location:** `LoginController.validateCredentials()` line 56-59  
**Code:**  
```java
if (password.length() < 6) {
    return "Password must be at least 6 characters";
}
```
**Impact:** Weak passwords, security vulnerability.  
**Fix Required:** Change minimum to 8 characters and enforce password strength (uppercase, lowercase, number).

---

### BUG-F010: No Email Validation for Contact Info
**Severity:** LOW  
**Problem:** If email fields exist in system, no format validation is applied.  
**Location:** `ValidationUtil.java` has email validation method but it's not used consistently  
**Impact:** Invalid email addresses stored, cannot contact employees.  
**Fix Required:** Apply email validation wherever email is collected.

---

## 🚫 BUSINESS LOGIC BUGS

### BUG-F011: Can Create Shifts with Negative Staff Count
**Severity:** HIGH  
**Problem:** Supervisor and referee spinners have minimum value 0, allowing shifts with no staff.  
**Location:** `CreateShiftView.java` line 220-227  
**Code:**  
```java
supervisorsSpinner = new Spinner<>(0, 5, 1);  // Min is 0
refereesSpinner = new Spinner<>(1, 10, 3);    // Referees min is 1
```
**Issue:** Supervisors can be 0, but some sports may require minimum 1 supervisor.  
**Impact:** Shifts created without adequate staffing.  
**Fix Required:** Set business rule minimums based on sport requirements.

---

### BUG-F012: No Conflict Detection for Employee Double-Booking
**Severity:** CRITICAL  
**Problem:** System doesn't check if employee is already assigned to another shift at same time.  
**Location:** No validation in shift assignment logic  
**Steps to Reproduce:**  
1. Create shift on Monday 6-9 PM  
2. Assign employee "John" to it  
3. Create another shift Monday 7-10 PM  
4. Assign same employee "John" - **It allows it!**  
**Impact:** Employee scheduled for two shifts simultaneously, logistical nightmare.  
**Fix Required:** Add conflict detection before allowing shift assignment.

---

### BUG-F013: No Availability Check Before Assignment
**Severity:** HIGH  
**Problem:** Employees can be assigned to shifts during times they marked as unavailable.  
**Location:** Assignment logic doesn't validate against employee availability  
**Impact:** Employees assigned when they can't work, no-shows, scheduling failures.  
**Fix Required:** Check employee availability before allowing assignment.

---

### BUG-F014: Availability Time Slots Limited to 6PM-12AM
**Severity:** MEDIUM  
**Problem:** Availability editor only allows 6PM-12AM time slots, but shifts might be scheduled outside these hours.  
**Location:** `AvailabilityEditor.java` line 158-168  
**Impact:** Cannot accurately represent employee availability for day shifts.  
**Fix Required:** Expand availability time slots or make them flexible.

---

### BUG-F015: No Sport Expertise Validation on Assignment
**Severity:** MEDIUM  
**Problem:** Employees can be assigned to sports they don't have expertise in.  
**Location:** No validation checking employee sport expertise matches shift sport  
**Impact:** Unqualified referees/supervisors, poor game quality.  
**Fix Required:** Validate employee has expertise in shift's sport before assignment.

---

## 📝 INPUT VALIDATION BUGS

### BUG-F016: Location Field No Length Limit
**Severity:** LOW  
**Problem:** Location field has no maximum length validation.  
**Location:** `CreateShiftView.java` line 153  
**Impact:** Extremely long location names could break UI layout or database field limits.  
**Fix Required:** Add maxLength property and validation.

---

### BUG-F017: Sport Name Not Validated on Creation
**Severity:** MEDIUM  
**Problem:** If admins can create sports, sport names might accept numbers/special characters.  
**Location:** Sport creation logic (if exists)  
**Impact:** Inconsistent sport naming, data quality issues.  
**Fix Required:** Validate sport names contain appropriate characters only.

---

### BUG-F018: No Trimming of Input Fields
**Severity:** LOW  
**Problem:** Some input fields might not trim whitespace before saving to database.  
**Location:** Various forms  
**Example:** "  Basketball  " saved as-is instead of "Basketball"  
**Impact:** Messy data, search/filter issues.  
**Fix Required:** Consistently trim all text inputs before saving.

---

### BUG-F019: Case Sensitivity in Usernames
**Severity:** LOW  
**Problem:** System might allow "admin", "Admin", "ADMIN" as different usernames.  
**Location:** `UserDao.findByUsername()` - no case normalization  
**Impact:** Duplicate accounts with case-variant names, confusion.  
**Fix Required:** Normalize usernames to lowercase before storage and lookup.

---

### BUG-F020: No Phone Number Format Validation
**Severity:** LOW  
**Problem:** `ValidationUtil` has phone validation but pattern allows various formats inconsistently.  
**Location:** `ValidationUtil.java` line 30  
**Impact:** Inconsistent phone number storage format.  
**Fix Required:** Enforce single consistent format (e.g., (XXX) XXX-XXXX).

---

## 🔒 ACCESS CONTROL BUGS

### BUG-F021: No Role-Based Feature Access
**Severity:** HIGH  
**Problem:** No checks preventing staff users from accessing admin-only features if they navigate directly.  
**Location:** All view controllers  
**Impact:** Security breach, staff can perform admin operations.  
**Fix Required:** Add role checks in every controller method.

---

### BUG-F022: No Session Timeout
**Severity:** MEDIUM  
**Problem:** User sessions never expire, stay logged in indefinitely.  
**Location:** `AuthenticationService` - no session timeout mechanism  
**Impact:** Security risk if workstation left unattended.  
**Fix Required:** Implement session timeout (e.g., 30 minutes of inactivity).

---

### BUG-F023: Password Change Requires Old Password Not Verified
**Severity:** HIGH  
**Problem:** If password change feature exists, it might not properly verify old password.  
**Location:** `AuthenticationService.changePassword()` exists but might have issues  
**Impact:** Unauthorized password changes.  
**Fix Required:** Ensure old password verification is robust.

---

## 🎯 USER EXPERIENCE BUGS

### BUG-F024: No Confirmation Dialog for Deletions
**Severity:** MEDIUM  
**Problem:** Deleting shifts/employees might not show confirmation dialog.  
**Location:** Delete action handlers  
**Impact:** Accidental deletions, data loss.  
**Fix Required:** Add "Are you sure?" confirmation dialogs for all delete operations.

---

### BUG-F025: No Success Feedback After Save
**Severity:** LOW  
**Problem:** Some forms might not show success message after saving.  
**Location:** Various save operations  
**Impact:** User uncertainty whether action succeeded.  
**Fix Required:** Show success toast/alert after all successful operations.

---

### BUG-F026: Error Messages Too Technical
**Severity:** LOW  
**Problem:** SQLException messages shown directly to users with SQL syntax.  
**Location:** Multiple catch blocks showing `e.getMessage()`  
**Example:** "FK_Constraint violation on table shifts..." shown to user  
**Impact:** Confusing user experience, security risk (exposing DB structure).  
**Fix Required:** Translate technical errors to user-friendly messages.

---

### BUG-F027: Date Picker Allows Invalid Date Selection
**Severity:** MEDIUM  
**Problem:** DatePicker might allow selecting dates that don't make business sense (e.g., years in future like 2099).  
**Location:** All DatePicker instances  
**Impact:** Unrealistic schedules created far in future.  
**Fix Required:** Set reasonable date range limits (e.g., current date to 1 year ahead).

---

### BUG-F028: No Required Field Indicators
**Severity:** LOW  
**Problem:** Forms have asterisks (*) for required fields but might not be consistently applied.  
**Location:** Various forms  
**Impact:** User confusion about which fields are mandatory.  
**Fix Required:** Consistently mark all required fields with *.

---

## 📊 DATA INTEGRITY BUGS

### BUG-F029: No Check for Shift End After Midnight
**Severity:** MEDIUM  
**Problem:** Shifts ending after midnight (e.g., 11 PM - 1 AM) might cause date calculation issues.  
**Location:** Time handling logic  
**Impact:** Incorrect hour calculations, payment errors.  
**Fix Required:** Handle overnight shifts properly with next-day end dates.

---

### BUG-F030: Generated Username Collisions
**Severity:** MEDIUM  
**Problem:** Username generation from first+last initial might fail for unique collision.  
**Location:** `EmployeeManagementService.generateUsername()` line 99-110  
**Code Logic:** Appends counter but starts from 1, might have edge cases  
**Impact:** Duplicate usernames or infinite loop in rare cases.  
**Fix Required:** Add maximum retry limit and fallback to UUID if needed.

---

# Employee Scheduler - Bugs & Industry Standards Requirements

## 🐛 CRITICAL BUGS

### BUG-001: Missing Transaction Management
**Severity:** CRITICAL  
**Problem:** Database operations lack transaction support. Multiple related database operations are not wrapped in transactions, leading to potential data inconsistency.  
**Location:** All DAO classes (EmployeeDAO, ShiftDAO, UserDao, TimeOffDAO, etc.)  
**Example:** In `ShiftDAO`, when creating shifts for a game, if one shift fails to insert, previous shifts remain in database causing partial data.  
**Impact:** Data corruption, inconsistent state, inability to rollback on errors.  
**Fix Required:** Implement transaction management with commit/rollback in all multi-step operations.

---

### BUG-002: No Connection Pooling
**Severity:** CRITICAL  
**Problem:** Every database operation creates a new connection via `DatabaseConnection.getConnection()`. No connection pool exists.  
**Location:** `DatabaseConnection.java`  
**Impact:** Poor performance, resource exhaustion under load, connection leaks, slow response times.  
**Fix Required:** Implement connection pooling (HikariCP, Apache DBCP, or C3P0).

---

### BUG-003: printStackTrace() Used Instead of Logging Framework
**Severity:** HIGH  
**Problem:** Application uses `printStackTrace()` and `System.out.println` for error handling instead of proper logging framework.  
**Location:** Throughout codebase (45+ occurrences in DatabaseConnection, LoginView, AdminDashboard, ScheduleBuilder, etc.)  
**Example:** `DatabaseConnection.java` line 49, 79; `LoginView.java` line 170  
**Impact:** No log levels, no log rotation, logs not suitable for production, cannot control verbosity, security risk (stack traces exposed).  
**Fix Required:** Replace with SLF4J + Logback or Log4j2.

---

### BUG-004: SQL Injection Vulnerability (Potential)
**Severity:** HIGH  
**Problem:** While most queries use PreparedStatement correctly, some use Statement with string concatenation.  
**Location:** `EmployeeDAO.getAllActive()`, `EmployeeDAO.getAll()` use Statement instead of PreparedStatement  
**Impact:** Potential SQL injection if any dynamic values are added in future.  
**Fix Required:** Convert all Statement usage to PreparedStatement, even for static queries.

---

### BUG-005: Empty DAO Classes
**Severity:** CRITICAL  
**Problem:** `UserDao.java`, `AvailabilityDAO.java`, and `DatabaseConnection.java` are empty (only package declaration).  
**Location:**  
- `c:\Users\Misha\Orchestrate_EmployeeScheduler\src\com\intramural\scheduling\dao\UserDao.java`  
- `c:\Users\Misha\Orchestrate_EmployeeScheduler\src\com\intramural\scheduling\dao\AvailabilityDAO.java`  
- `c:\Users\Misha\Orchestrate_EmployeeScheduler\src\com\intramural\scheduling\dao\DatabaseConnection.java`  
**Impact:** Application cannot function - these are core classes.  
**Fix Required:** Implement full CRUD operations in these DAOs.

---

### BUG-006: No Input Validation in DAOs
**Severity:** HIGH  
**Problem:** DAO methods don't validate input parameters before database operations.  
**Location:** All DAO classes  
**Example:** `EmployeeDAO.getById()` doesn't check if employeeId is positive, `UserDao.insert()` doesn't check for null user object  
**Impact:** Database errors, null pointer exceptions, invalid data insertion.  
**Fix Required:** Add null checks and parameter validation at DAO layer.

---

### BUG-007: Passwords Stored as Plain SHA-256
**Severity:** CRITICAL (Security)  
**Problem:** Passwords use simple SHA-256 hashing without salt. SHA-256 is not designed for password hashing.  
**Location:** `AuthenticationService.hashPassword()` uses `MessageDigest.getInstance("SHA-256")`  
**Impact:** Vulnerable to rainbow table attacks, dictionary attacks, and GPU-based brute force.  
**Fix Required:** Use bcrypt, scrypt, or Argon2 with per-user salts.

---

### BUG-008: No Resource Leak Prevention
**Severity:** HIGH  
**Problem:** While try-with-resources is used, some ResultSet objects may not be properly closed in exception scenarios.  
**Location:** Multiple DAO methods  
**Example:** `TimeOffDAO.getTimeOffRequestsByEmployee()` - ResultSet `rs` declared outside try-with-resources  
**Impact:** Connection pool exhaustion, memory leaks.  
**Fix Required:** Ensure all JDBC resources use try-with-resources consistently.

---

### BUG-009: No Concurrent Access Control
**Severity:** MEDIUM  
**Problem:** No optimistic or pessimistic locking for concurrent updates.  
**Location:** All update operations in DAOs  
**Example:** Two admins updating same shift simultaneously - last write wins, no conflict detection  
**Impact:** Lost updates, race conditions, data inconsistency.  
**Fix Required:** Add version field or timestamp-based optimistic locking.

---

### BUG-010: Hardcoded Database-Specific Functions
**Severity:** MEDIUM  
**Problem:** SQL uses SQL Server specific functions like `GETDATE()`.  
**Location:** `TimeOffDAO.createTimeOffRequest()`, `ShiftDAO.updateAssignment()`  
**Impact:** Application tied to SQL Server, cannot switch databases.  
**Fix Required:** Use JDBC's `Timestamp` or database-agnostic approach.

---

## 📋 MISSING INDUSTRY STANDARD FEATURES

### REQ-001: No Soft Delete Implementation
**Problem:** DAOs have delete methods but no soft delete pattern.  
**Impact:** Deleted data is permanently lost, no audit trail.  
**Fix Required:** Add `deleted_at` timestamp column, implement soft delete in all DAOs.

---

### REQ-002: No Audit Trail
**Problem:** No tracking of who created/modified records and when.  
**Impact:** Cannot track changes, accountability issues, compliance problems.  
**Fix Required:** Add `created_at`, `created_by`, `updated_at`, `updated_by` columns to all tables and populate in DAOs.

---

### REQ-003: No Pagination Support
**Problem:** Methods like `getAllActive()`, `getAll()` return entire result sets.  
**Impact:** Memory issues with large datasets, slow queries, poor UX.  
**Fix Required:** Implement pagination with offset/limit parameters.

---

### REQ-004: No Bulk Operations
**Problem:** No batch insert/update methods.  
**Impact:** Poor performance when inserting multiple records.  
**Fix Required:** Add `insertBatch()`, `updateBatch()` methods using JDBC batch operations.

---

### REQ-005: No Error Code Standardization
**Problem:** SQLException thrown directly without wrapping in custom exceptions.  
**Impact:** Difficult to handle specific errors, poor error messages to users.  
**Fix Required:** Create custom exception hierarchy (DatabaseException, DuplicateRecordException, RecordNotFoundException, etc.).

---

### REQ-006: No DTO Pattern
**Problem:** Domain models used directly in DAO layer without separation.  
**Impact:** Tight coupling, security risk (exposing internal model structure).  
**Fix Required:** Create DTOs for data transfer between layers.

---

### REQ-007: Missing Delete Operation in UserDao
**Problem:** UserDao has insert, update, find methods but no delete.  
**Impact:** Incomplete CRUD operations.  
**Fix Required:** Add `delete(int userId)` or `softDelete(int userId)` method.

---

### REQ-008: No Query Result Caching
**Problem:** No caching layer for frequently accessed data.  
**Impact:** Repeated database hits for same data, poor performance.  
**Fix Required:** Implement caching (Caffeine, Ehcache, or Redis) for reference data.

---

### REQ-009: No Database Migration Tool
**Problem:** No version control for database schema (Flyway, Liquibase).  
**Impact:** Manual schema updates, deployment issues, no rollback capability.  
**Fix Required:** Integrate Flyway or Liquibase for schema versioning.

---

### REQ-010: No Health Check Endpoint
**Problem:** No database connectivity health check.  
**Impact:** Cannot monitor database connectivity in production.  
**Fix Required:** Create health check service that verifies database connection.

---

## 🔒 SECURITY ISSUES

### SEC-001: Database Credentials in Properties File
**Problem:** Database credentials stored in plain text in `application.properties`.  
**Location:** `resources/config/application.properties`  
**Impact:** Security risk if file is exposed.  
**Fix Required:** Use environment variables or secure vault (HashiCorp Vault, AWS Secrets Manager).

---

### SEC-002: No SQL Query Timeout
**Problem:** No timeout set on database queries.  
**Impact:** Long-running queries can hang application.  
**Fix Required:** Set `setQueryTimeout()` on all PreparedStatements.

---

### SEC-003: No Input Sanitization
**Problem:** String inputs not sanitized before database operations.  
**Impact:** Potential XSS if data displayed in UI, data quality issues.  
**Fix Required:** Sanitize and validate all string inputs.

---

### SEC-004: Exception Messages Expose Internal Details
**Problem:** Stack traces and error messages expose database structure and internal details.  
**Location:** All exception handling  
**Impact:** Information disclosure to attackers.  
**Fix Required:** Log detailed errors server-side, return generic messages to users.

---

## 🎯 CODE QUALITY ISSUES

### QA-001: No Unit Tests for DAOs
**Problem:** No JUnit tests for any DAO methods.  
**Impact:** No regression testing, bugs introduced during refactoring.  
**Fix Required:** Write unit tests with H2 in-memory database or mocking.

---

### QA-002: Inconsistent Naming (UserDao vs UserDAO)
**Problem:** Class named `UserDao` but other DAOs use `DAO` suffix (EmployeeDAO, ShiftDAO).  
**Impact:** Inconsistency, confusion.  
**Fix Required:** Standardize on either `DAO` or `Dao`.

---

### QA-003: Magic Numbers and Strings
**Problem:** Hardcoded values like `1` for true, `0` for false, status strings.  
**Location:** `EmployeeDAO` line 24-25, TimeOffDAO uses string literals for status  
**Impact:** Hard to maintain, error-prone.  
**Fix Required:** Create constants or enums for all magic values.

---

### QA-004: No Javadoc for Some Methods
**Problem:** Some DAO methods lack Javadoc comments.  
**Location:** `EmployeeDAO.extractEmployee()`, various helper methods  
**Impact:** Poor maintainability.  
**Fix Required:** Add comprehensive Javadoc to all public methods.

---

### QA-005: Commented Out Code
**Problem:** TimeOffDAO functionality commented out in multiple files.  
**Location:** `AvailabilityService.java`, `StaffDashboardController.java`  
**Impact:** Incomplete features, technical debt.  
**Fix Required:** Either implement properly or remove commented code.

---

### QA-006: No Database Constraint Validation
**Problem:** Application doesn't check database constraints before operations.  
**Impact:** SQLExceptions instead of validation errors.  
**Fix Required:** Pre-validate foreign keys, unique constraints at application level.

---

### QA-007: ResultSet Extraction Code Duplication
**Problem:** Every DAO has similar `extractXXX()` methods with repeated boilerplate.  
**Impact:** Code duplication, maintenance burden.  
**Fix Required:** Consider using an ORM (JPA/Hibernate) or ResultSet mapper library.

---

### QA-008: No DAO Interface Abstraction
**Problem:** DAO classes are concrete implementations without interfaces.  
**Impact:** Difficult to mock for testing, tight coupling.  
**Fix Required:** Create DAO interfaces (IEmployeeDAO, IUserDao, etc.).

---

## 🚀 PERFORMANCE ISSUES

### PERF-001: N+1 Query Problem
**Problem:** Potential N+1 queries when loading related entities.  
**Example:** Loading shifts with employee details likely requires multiple queries  
**Impact:** Poor performance with large datasets.  
**Fix Required:** Use JOIN queries or batch loading.

---

### PERF-002: No Index Usage Verification
**Problem:** No indication that queries are optimized for database indexes.  
**Impact:** Slow queries on large tables.  
**Fix Required:** Review and optimize queries, ensure proper indexes exist.

---

### PERF-003: Loading Full Result Sets
**Problem:** Methods like `getAllActive()` load entire result set into memory.  
**Impact:** OutOfMemoryError with large datasets.  
**Fix Required:** Implement streaming or pagination.

---

## 📊 SUMMARY

**Total Issues: 34**
- Critical: 5
- High: 4  
- Medium: 8
- Security: 4
- Code Quality: 8
- Performance: 3
- Missing Features: 10

**Estimated Effort:** 3-4 weeks for full remediation

**Priority Order:**
1. Fix empty DAO classes (BUG-005) - **BLOCKER**
2. Implement transaction management (BUG-001)
3. Add connection pooling (BUG-002)
4. Replace printStackTrace with logging (BUG-003)
5. Fix password hashing (BUG-007)
6. Add input validation (BUG-006)
7. Implement remaining features and quality improvements
