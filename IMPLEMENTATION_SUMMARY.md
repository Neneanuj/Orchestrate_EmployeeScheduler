# Forgot Password Feature - Implementation Summary

## ✅ Files Created

### 1. Service Layer
- **PasswordResetService.java**
  - Location: `src/com/intramural/scheduling/service/PasswordResetService.java`
  - Functions: Generate tokens, validate tokens, reset passwords
  - Features: 1-hour expiration, one-time use, transaction safety

### 2. View Layer
- **ForgotPasswordView.java**
  - Location: `src/com/intramural/scheduling/view/ForgotPasswordView.java`
  - UI: Email input, token display, password reset form
  - Validation: Email format, password strength, password matching

### 3. Database
- **password_reset_tokens.sql**
  - Location: `resources/database/password_reset_tokens.sql`
  - Table: PasswordResetTokens with indexes and constraints
  - Stored Procedure: sp_CleanupExpiredTokens

### 4. Documentation
- **FORGOT_PASSWORD_README.md** - Complete implementation guide
- **setup_forgot_password.ps1** - Automated setup script

## ✅ Files Updated

### LoginView.java
- Added action to "Forgot password?" hyperlink
- Navigates to ForgotPasswordView when clicked

## 🚀 Quick Start

### Option 1: Automated Setup (Recommended)
```powershell
cd c:\Users\Misha\Orchestrate_EmployeeScheduler
.\setup_forgot_password.ps1
```

### Option 2: Manual Setup

**Step 1: Create Database Table**
```powershell
sqlcmd -S DESKTOP-UDDCF59 -E -i "resources\database\password_reset_tokens.sql"
```

**Step 2: Compile & Run**
```powershell
$env:JAVAFX_PATH = "C:\javafx-sdk-21.0.1\lib"
javac -d out --module-path $env:JAVAFX_PATH --add-modules javafx.controls -cp "lib\*" (Get-ChildItem -Recurse -Filter *.java | Select-Object -ExpandProperty FullName)
java -cp ".\out;.\lib\*" --module-path $env:JAVAFX_PATH --add-modules javafx.controls com.intramural.scheduling.Main
```

## 🧪 Testing Steps

1. **Start the application**
2. **On Login Screen:** Click "Forgot password?" link
3. **Enter Email:** misha@example.com
4. **Click:** "Send Reset Token"
5. **View Token:** Token appears on screen
6. **Click:** "I have a token - Reset Password"
7. **Enter Token:** Paste the token shown
8. **Enter New Password:** (minimum 8 characters)
9. **Confirm Password:** Re-enter the same password
10. **Click:** "Reset Password"
11. **Success:** Redirected to login after 2 seconds
12. **Login:** Use username "Misha" with your new password

## 📋 Database Schema

```sql
CREATE TABLE PasswordResetTokens (
    token_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    reset_token VARCHAR(255) NOT NULL UNIQUE,
    expiry_datetime DATETIME NOT NULL,
    is_used BIT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_PasswordResetTokens_Users 
        FOREIGN KEY (user_id) REFERENCES Users(user_id)
);
```

## 🔒 Security Features

- ✅ Tokens expire after 1 hour
- ✅ One-time use (marked as used after reset)
- ✅ Email validation before token generation
- ✅ Password strength validation (8+ characters)
- ✅ Database transactions for atomic updates
- ✅ Only generates tokens for existing emails

## 📝 Key Features

### PasswordResetService
```java
String generateResetToken(String email)  // Returns UUID token or null
boolean validateToken(String token)      // Checks validity and expiration
boolean resetPassword(String token, String newPassword)  // Resets with transaction
```

### ForgotPasswordView
- Modern JavaFX UI matching login screen
- Email format validation
- Token display (for demo purposes)
- Expandable password reset section
- Success/error message feedback
- Auto-redirect after successful reset

## 🎯 User Flow

```
Login Screen
    ↓ (Click "Forgot password?")
Forgot Password Screen
    ↓ (Enter email)
Token Generated & Displayed
    ↓ (Expand reset section)
Enter Token & New Password
    ↓ (Submit)
Password Reset Success
    ↓ (Auto-redirect after 2s)
Login Screen (with new password)
```

## 💡 Future Enhancements

### Email Integration
Add JavaMail API to send tokens via email instead of displaying them:
- Download `javax.mail.jar`
- Implement `sendResetEmail()` method
- Configure SMTP settings
- Hide token display in UI

### Password Hashing
Upgrade from plain text to BCrypt:
- Add BCrypt library
- Update `hashPassword()` method
- Update `AuthenticationService` verification

### Token Cleanup
Schedule automatic cleanup of expired tokens:
- Create SQL Server Agent Job
- Run `sp_CleanupExpiredTokens` daily
- Monitor token table size

## 📂 Project Structure

```
Orchestrate_EmployeeScheduler/
├── src/com/intramural/scheduling/
│   ├── service/
│   │   └── PasswordResetService.java       ✨ NEW
│   └── view/
│       ├── ForgotPasswordView.java         ✨ NEW
│       └── LoginView.java                  🔄 UPDATED
├── resources/database/
│   └── password_reset_tokens.sql           ✨ NEW
├── FORGOT_PASSWORD_README.md               ✨ NEW
└── setup_forgot_password.ps1               ✨ NEW
```

## ✅ Implementation Complete

All components are ready to use. Run the setup script or follow manual steps to activate the forgot password functionality.

For detailed information, see **FORGOT_PASSWORD_README.md**.
