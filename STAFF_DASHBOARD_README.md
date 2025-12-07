# Staff Dashboard - Implementation Guide

## ✅ What's Been Created

### 1. **StaffDashboard.java**
**Location:** `src/com/intramural/scheduling/view/StaffDashboard.java`

Complete staff-facing dashboard with:
- **Weekly Summary Cards**: Shows shifts, hours, days working this week
- **This Week's Schedule**: Day-by-day breakdown of current week
- **Upcoming Shifts**: Next 30 days of assigned shifts with countdown
- **Real-time Data**: Loads directly from database

### 2. **ShiftDAO Enhancement**
**Added Method:** `getShiftsByEmployee(int employeeId)`
- Retrieves all shifts assigned to a specific employee
- Joins with game_schedules and sports tables
- Returns complete shift information with dates, times, locations

### 3. **LoginView Update**
**Role-Based Routing:**
- STAFF users → StaffDashboard
- ADMIN/SUPERVISOR users → AdminDashboard

## 📊 Staff Dashboard Features

### **Welcome Section**
- Personalized greeting with username
- Beautiful gradient header

### **Weekly Summary (4 Cards)**
1. **Shifts This Week** - Count of shifts in current week
2. **Total Hours** - Sum of working hours this week
3. **Days Working** - Number of unique days with shifts
4. **Upcoming Shifts** - Total upcoming shifts

### **This Week's Schedule**
- Shows Monday through Sunday
- Each day displays:
  - ✅ Date (e.g., "Dec 09, 2025")
  - ✅ Time (e.g., "6:00 PM - 10:00 PM")
  - ✅ Location (e.g., "Main Gym")
  - ✅ Duration (e.g., "4.0 hrs")
- **Today's shifts** highlighted in green
- Days with no shifts clearly marked

### **Upcoming Shifts (Next 30 Days)**
- Card-based layout for each shift
- Shows:
  - 📅 Date with month/day/day-of-week
  - 🕐 Time range
  - 📍 Location
  - ⏰ Hours
  - "In X days" countdown
- Sorted chronologically

## 🚀 Setup & Testing

### **Step 1: Compile**
```powershell
cd c:\Users\Misha\Orchestrate_EmployeeScheduler
$env:JAVAFX_PATH = "C:\javafx-sdk-21.0.1\lib"
javac -d out --module-path $env:JAVAFX_PATH --add-modules javafx.controls -cp "lib\*" (Get-ChildItem -Recurse -Filter *.java | Select-Object -ExpandProperty FullName)
```

### **Step 2: Run**
```powershell
java -cp ".\out;.\lib\*" --module-path $env:JAVAFX_PATH --add-modules javafx.controls com.intramural.scheduling.Main
```

### **Step 3: Test Staff Dashboard**

#### Create Test Data (SQL)
First, create a staff user and assign some shifts:

```sql
USE EmployeeScheduling;

-- Create a staff user
INSERT INTO Users (username, password_hash, role, email)
VALUES ('staff1', 'staff123', 'STAFF', 'staff1@example.com');

-- Get the user_id (let's say it's 2)
DECLARE @staffUserId INT = 2;

-- Create a sport
INSERT INTO Sports (sport_name, description)
VALUES ('Basketball', 'Indoor basketball games');

DECLARE @sportId INT = SCOPE_IDENTITY();

-- Create game schedules for this week
DECLARE @today DATE = GETDATE();
DECLARE @monday DATE = DATEADD(DAY, 1 - DATEPART(WEEKDAY, @today), @today);

-- Monday shift
INSERT INTO game_schedules (sport_id, game_date, start_time, end_time, location, 
    required_supervisors, required_referees, schedule_cycle_start, schedule_cycle_end, created_by)
VALUES (@sportId, @monday, '18:00:00', '22:00:00', 'Main Gym', 1, 2, @monday, 
    DATEADD(DAY, 6, @monday), 1);

DECLARE @gameId1 INT = SCOPE_IDENTITY();

-- Create shift and assign to staff
INSERT INTO shifts (game_schedule_id, position_type, position_number, assigned_employee_id, assignment_status)
VALUES (@gameId1, 'SUPERVISOR', 1, @staffUserId, 'ASSIGNED');

-- Wednesday shift
INSERT INTO game_schedules (sport_id, game_date, start_time, end_time, location, 
    required_supervisors, required_referees, schedule_cycle_start, schedule_cycle_end, created_by)
VALUES (@sportId, DATEADD(DAY, 2, @monday), '19:00:00', '21:00:00', 'Court A', 1, 2, @monday, 
    DATEADD(DAY, 6, @monday), 1);

DECLARE @gameId2 INT = SCOPE_IDENTITY();

INSERT INTO shifts (game_schedule_id, position_type, position_number, assigned_employee_id, assignment_status)
VALUES (@gameId2, 'REFEREE', 1, @staffUserId, 'ASSIGNED');

-- Friday shift
INSERT INTO game_schedules (sport_id, game_date, start_time, end_time, location, 
    required_supervisors, required_referees, schedule_cycle_start, schedule_cycle_end, created_by)
VALUES (@sportId, DATEADD(DAY, 4, @monday), '18:30:00', '23:00:00', 'Field B', 1, 2, @monday, 
    DATEADD(DAY, 6, @monday), 1);

DECLARE @gameId3 INT = SCOPE_IDENTITY();

INSERT INTO shifts (game_schedule_id, position_type, position_number, assigned_employee_id, assignment_status)
VALUES (@gameId3, 'SUPERVISOR', 1, @staffUserId, 'ASSIGNED');

-- Next week shift
INSERT INTO game_schedules (sport_id, game_date, start_time, end_time, location, 
    required_supervisors, required_referees, schedule_cycle_start, schedule_cycle_end, created_by)
VALUES (@sportId, DATEADD(DAY, 10, @monday), '20:00:00', '22:00:00', 'Gym 2nd Floor', 1, 2, 
    DATEADD(DAY, 7, @monday), DATEADD(DAY, 13, @monday), 1);

DECLARE @gameId4 INT = SCOPE_IDENTITY();

INSERT INTO shifts (game_schedule_id, position_type, position_number, assigned_employee_id, assignment_status)
VALUES (@gameId4, 'REFEREE', 1, @staffUserId, 'ASSIGNED');
```

#### Login as Staff
1. Run the application
2. Login with:
   - **Username:** `staff1`
   - **Password:** `staff123`
3. You'll be automatically routed to the Staff Dashboard

## 📱 Staff Dashboard UI

### Visual Design
- **Modern card-based layout**
- **Color-coded sections:**
  - Header: Dark navy (#2c3e50)
  - Welcome: Purple gradient
  - Summary cards: Blue, green, orange, purple
  - Today's shifts: Green highlight
  - Upcoming shifts: Clean white cards

### Navigation
- **Refresh Button** (🔄): Reload all shift data
- **Logout Button**: Return to login screen

## 🎯 What Staff Can See

### ✅ This Week's Schedule
```
MONDAY - Dec 09, 2025
🕐 6:00 PM - 10:00 PM   📍 Main Gym   4.0 hrs

WEDNESDAY - Dec 11, 2025
🕐 7:00 PM - 9:00 PM   📍 Court A   2.0 hrs

FRIDAY - Dec 13, 2025
🕐 6:30 PM - 11:00 PM   📍 Field B   4.5 hrs
```

### ✅ Upcoming Shifts
```
┌─────────────────────────────────────────────┐
│  DEC │  🕐 8:00 PM - 10:00 PM              │
│  19  │  📍 Gym 2nd Floor                   │
│  THU │  In 12 days                 2.0 hrs │
└─────────────────────────────────────────────┘
```

### ✅ Weekly Summary
```
📅 Shifts This Week: 3
⏰ Total Hours: 10.5 hrs
📆 Days Working: 3
🔜 Upcoming Shifts: 1
```

## 🔄 Data Flow

1. **User logs in** → AuthenticationService validates
2. **LoginView checks role** → Routes to StaffDashboard if STAFF
3. **StaffDashboard loads** → Calls `shiftDAO.getShiftsByEmployee(employeeId)`
4. **Query executes:**
   ```sql
   SELECT gs.*, s.*, sp.sport_name 
   FROM game_schedules gs 
   JOIN shifts s ON gs.schedule_id = s.game_schedule_id 
   JOIN sports sp ON gs.sport_id = sp.sport_id 
   WHERE s.assigned_employee_id = ?
   ORDER BY gs.game_date, gs.start_time
   ```
5. **Dashboard displays** → Processes and organizes shifts by week/day
6. **User clicks Refresh** → Reloads data from database

## 🆚 Admin vs Staff Dashboard

| Feature | Admin Dashboard | Staff Dashboard |
|---------|----------------|-----------------|
| Create Employees | ✅ | ❌ |
| Create Shifts | ✅ | ❌ |
| Assign Shifts | ✅ | ❌ |
| View All Shifts | ✅ | ❌ |
| View Own Shifts | N/A | ✅ |
| Weekly Summary | ❌ | ✅ |
| Day-by-Day Schedule | ❌ | ✅ |
| Upcoming Shifts | ❌ | ✅ |

## 🔐 Security

- **Role-based access**: Staff can only see their own shifts
- **Database query filtering**: `WHERE assigned_employee_id = ?`
- **No admin functions**: Staff cannot create or modify shifts
- **Auto-routing**: Correct dashboard based on user role

## 🎨 Customization Options

### Change Colors
Edit `StaffDashboard.java`:
- Header: Line 70 `#2c3e50`
- Welcome gradient: Line 115 `#667eea to #764ba2`
- Summary cards: Lines 175-198 (individual colors)

### Adjust Date Range
- This week: Automatic (Monday-Sunday)
- Upcoming: Line 421 - Change `plusDays(30)` to desired range

### Add More Stats
Add new summary cards in `createWeeklySummary()` method

## 📝 Testing Checklist

- [ ] Staff user can login
- [ ] Redirected to Staff Dashboard (not Admin)
- [ ] Weekly summary shows correct counts
- [ ] This week's schedule displays all shifts
- [ ] Today's date is highlighted in green
- [ ] Upcoming shifts show with countdown
- [ ] Time format is 12-hour (PM/AM)
- [ ] Locations display correctly
- [ ] Hours calculated correctly
- [ ] Refresh button reloads data
- [ ] Logout returns to login screen

## 🚨 Troubleshooting

### Issue: No shifts showing
**Solution:** Run the SQL test data script above

### Issue: "Table not found" error
**Solution:** Ensure tables exist:
```sql
SELECT * FROM game_schedules;
SELECT * FROM shifts;
SELECT * FROM sports;
```

### Issue: Wrong dashboard after login
**Solution:** Check user role in database:
```sql
SELECT username, role FROM Users WHERE username = 'staff1';
```
Should be `STAFF` (not `ADMIN` or `SUPERVISOR`)

### Issue: Compilation error
**Solution:** Ensure all imports are available and JavaFX is configured

## ✨ Summary

**Files Created:**
1. ✅ `StaffDashboard.java` - Complete staff interface (500+ lines)
2. ✅ `ShiftDAO.java` - Added `getShiftsByEmployee()` method

**Files Updated:**
3. ✅ `LoginView.java` - Role-based dashboard routing

**Features Implemented:**
- ✅ Weekly schedule view
- ✅ Day-by-day breakdown
- ✅ Upcoming shifts with countdown
- ✅ Working hours calculation
- ✅ Location and timing display
- ✅ Real-time database integration
- ✅ Refresh functionality
- ✅ Professional UI design

The staff panel is now complete and ready to use!
