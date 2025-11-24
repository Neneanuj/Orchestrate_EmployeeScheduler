# Employee Shift Scheduling System

A desktop application for managing employee schedules in intramural sports programs. Built with JavaFX and SQL Server.

## What This Does

This is a scheduling app I built to help manage staff for sports programs. It handles:

- Creating and managing shift schedules
- Tracking employee availability 
- Preventing scheduling conflicts
- Managing time-off requests
- Tracking hours worked per week
- Analytics and reporting

The main goal was to automate the tedious parts of scheduling while catching conflicts before they become problems.

## Tech Stack

- **Java 21** - Main language
- **JavaFX 21** - For the UI
- **SQL Server** - Database
- **JDBC** - Database connection
- **BCrypt** - Password security
- **Maven** - Build tool (optional)

## System Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   Presentation Layer                     │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────┐ │
│  │  Login   │  │Dashboard │  │Schedule  │  │Employee │ │
│  │  View    │  │  View    │  │ Builder  │  │  Mgmt   │ │
│  └──────────┘  └──────────┘  └──────────┘  └─────────┘ │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│                   Business Logic Layer                   │
│  ┌────────────┐  ┌────────────┐  ┌──────────────────┐  │
│  │ Scheduling │  │ Conflict   │  │  Notification    │  │
│  │  Engine    │  │  Checker   │  │    Service       │  │
│  └────────────┘  └────────────┘  └──────────────────┘  │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│                    Data Access Layer                     │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────┐ │
│  │ User DAO │  │Employee  │  │ Shift    │  │Schedule │ │
│  │          │  │   DAO    │  │  DAO     │  │  DAO    │ │
│  └──────────┘  └──────────┘  └──────────┘  └─────────┘ │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│                    SQL Server Database                   │
│    Users | Employees | Shifts | Schedules | Analytics   │
└─────────────────────────────────────────────────────────┘
```

## Project Structure

```
📦 Orchestrate_EmployeeScheduler
├── 📂 src/com/intramural/scheduling/
│   ├── 📂 model/                    # Data classes
│   │   ├── 📄 User.java
│   │   ├── 📄 Employee.java
│   │   ├── 📄 SeasonalAvailability.java
│   │   ├── 📄 TimeOffRequest.java
│   │   ├── 📄 Shift.java
│   │   ├── 📄 ShiftAssignment.java
│   │   ├── 📄 GameSchedule.java
│   │   ├── 📄 Sport.java
│   │   └── 📄 SchedulingRecommendation.java
│   ├── 📂 dao/                      # Database access layer
│   │   ├── 📄 DatabaseConnection.java
│   │   ├── 📄 UserDAO.java
│   │   ├── 📄 EmployeeDAO.java
│   │   ├── 📄 AvailabilityDAO.java
│   │   ├── 📄 TimeOffDAO.java
│   │   ├── 📄 ShiftDAO.java
│   │   └── 📄 GameScheduleDAO.java
│   ├── 📂 service/                  # Business logic
│   │   ├── 📄 AuthenticationService.java
│   │   ├── 📄 AvailabilityService.java
│   │   ├── 📄 SchedulingEngine.java
│   │   ├── 📄 ConflictChecker.java
│   │   ├── 📄 HoursTracker.java
│   │   └── 📄 NotificationService.java
│   ├── 📂 controller/               # UI controllers
│   │   ├── 📄 LoginController.java
│   │   ├── 📄 AdminDashboardController.java
│   │   ├── 📄 StaffDashboardController.java
│   │   ├── 📄 AvailabilityController.java
│   │   ├── 📄 TimeOffController.java
│   │   └── 📄 SchedulingController.java
│   ├── 📂 view/                     # JavaFX screens
│   │   ├── 📄 LoginView.java
│   │   ├── 📄 AdminDashboard.java
│   │   ├── 📄 StaffDashboard.java
│   │   ├── 📄 AvailabilityEditor.java
│   │   ├── 📄 ScheduleBuilder.java
│   │   ├── 📄 EmployeesPage.java
│   │   ├── 📄 AnalyticsPage.java
│   │   └── 📄 TimeOffManager.java
│   ├── 📂 util/                     # Helper classes
│   │   ├── 📄 TimeSlot.java
│   │   ├── 📄 DateTimeUtil.java
│   │   └── 📄 ValidationUtil.java
│   ├── 📄 Main.java                 # Application entry point
│   ├── 📄 TestDashboard.java        # Dashboard test
│   ├── 📄 TestSchedule.java         # Schedule test
│   ├── 📄 TestEmployees.java        # Employees test
│   └── 📄 TestAnalytics.java        # Analytics test
├── 📂 resources/
│   ├── 📂 config/
│   │   └── 📄 application.properties
│   ├── 📂 database/
│   │   └── 📄 schema.sql
│   └── 🎨 styles.css
├── 📂 lib/
│   └── 📦 mssql-jdbc.jar
├── 📂 test/
│   ├── 📄 SchedulingEngineTest.java
│   └── 📄 ConflictCheckerTest.java
├── 📄 .gitignore
├── 📄 pom.xml
└── 📄 README.md
```

## Getting Started

### What You'll Need

- Java 17 or higher
- SQL Server (I'm using 2019)
- JavaFX SDK 21
- An IDE (I use VS Code but IntelliJ works too)

### Setting Up the Database

1. Open SQL Server Management Studio and create the database:

```sql
CREATE DATABASE [Emploment shift scheduling];
```

2. Run the schema.sql file from the resources/database folder to create all the tables.

3. Create a user for the app:

```sql
CREATE LOGIN scheduling_app WITH PASSWORD = 'YourPassword123!';
USE [Emploment shift scheduling];
CREATE USER scheduling_app FOR LOGIN scheduling_app;
ALTER ROLE db_owner ADD MEMBER scheduling_app;
```

### Configuring the App

Edit `resources/config/application.properties` with your database info:

```properties
db.url=jdbc:sqlserver://localhost:1433;databaseName=Emploment shift scheduling;encrypt=false;trustServerCertificate=true
db.username=scheduling_app
db.password=YourPassword123!
db.driver=com.microsoft.sqlserver.jdbc.SQLServerDriver
```

### Running It

If you're compiling manually:

```bash
# Compile
javac -cp ".;lib/*" --module-path "C:\javafx-sdk-21\lib" --add-modules javafx.controls,javafx.fxml -d out src\com\intramural\scheduling\*.java src\com\intramural\scheduling\dao\*.java src\com\intramural\scheduling\view\*.java

# Run
java -cp "out;lib/*" --module-path "C:\javafx-sdk-21\lib" --add-modules javafx.controls,javafx.fxml com.intramural.scheduling.Main
```

Or if you set up Maven:

```bash
mvn clean install
mvn javafx:run
```

Default login is `admin` / `admin` for testing.

## Features

### Dashboard
Shows an overview of everything - upcoming shifts, team stats, who's working, etc. Pretty standard admin dashboard stuff.

### Schedule Management
This is the main part. You can:
- View shifts in a calendar layout
- Create new shifts
- See which ones need more staff
- Search and filter by sport, date, location
- Track shift status (fully staffed vs needs people)

### Employee Management
Keep track of your team:
- Contact info
- Which sports they can work
- Active/inactive status
- Certifications and expertise levels

### Analytics
Some basic charts and metrics:
- Shifts over time
- Hours worked trends
- Sport distribution
- Fill rate percentages

I used Canvas to draw the charts since I didn't want to add another library dependency.

## Database Schema

The main tables are:

- **users** - Login credentials and roles
- **employees** - Employee details and contact info
- **sports** - Different sports with requirements
- **shifts** - Individual shift slots to fill
- **game_schedules** - Scheduled games
- **seasonal_availability** - When people can work
- **time_off_requests** - PTO management
- **weekly_hours** - Hours tracking

There are a few more for things like notifications and assignment history. Check the schema.sql file for details.

## How Scheduling Works

The scheduling engine tries to match employees to shifts based on:

**Hard rules (must follow):**
- Employee is available at that time
- No double-booking
- Under their max weekly hours
- Have the right sport expertise
- Time-off is respected

**Soft preferences (nice to have):**
- Higher expertise level is better
- Spread work evenly
- Match preferred times
- Balance workload

It suggests two options for each shift and you can pick one or assign manually.

## What's Not Done Yet

Still working on:
- [ ] The availability editor UI
- [ ] Time-off request forms
- [ ] Email notifications
- [ ] Calendar export/import
- [ ] Mobile version
- [ ] Shift swap functionality
- [ ] More advanced reporting

This was a semester project so some features are stubbed out but the core scheduling logic is there.

## Testing

There are test files for each main screen:

```bash
# Test individual pages
java -cp "out;lib/*" --module-path "C:\javafx-sdk-21\lib" --add-modules javafx.controls,javafx.fxml com.intramural.scheduling.TestDashboard
java -cp "out;lib/*" --module-path "C:\javafx-sdk-21\lib" --add-modules javafx.controls,javafx.fxml com.intramural.scheduling.TestSchedule
```

Unit tests are in the test/ folder but honestly I need to write more of those.

## Known Issues

- The charts in Analytics are basic - might add a proper charting library later
- Some error messages could be more helpful
- Need better input validation in a few places
- The UI isn't super responsive on small screens

## Contributing

If you want to contribute, just fork it and send a PR. No formal process or anything.

## License

MIT License - do whatever you want with it.

## Contact

If you have questions or find bugs, open an issue on GitHub or email me at misha@example.com

## Acknowledgments

Thanks to:
- The JavaFX community for good docs
- Stack Overflow for helping me figure out SQL Server connection issues
- My professor for the project idea

Built this over a few weeks as a learning project. Hope it's useful!