# Forgot Password Feature - Implementation Guide

## Overview
This feature allows users to reset their password using a secure token-based system.

## Components Created

### 1. PasswordResetService.java
**Location:** `src/com/intramural/scheduling/service/PasswordResetService.java`

**Methods:**
- `generateResetToken(String email)` - Generates a unique UUID token for password reset
- `validateToken(String token)` - Validates if a token is valid, not used, and not expired
- `resetPassword(String token, String newPassword)` - Resets the password using a valid token

**Features:**
- Tokens expire after 1 hour
- Tokens are marked as used after password reset
- Transaction support for data integrity
- Email validation

### 2. ForgotPasswordView.java
**Location:** `src/com/intramural/scheduling/view/ForgotPasswordView.java`

**UI Components:**
- Email input field with validation
- Token generation button
- Token display (for demo - in production, tokens would be emailed)
- Password reset section (expandable)
- New password and confirm password fields
- Back to login link

**Features:**
- Email format validation
- Password strength validation (minimum 8 characters)
- Password confirmation matching
- Success/error message display
- Auto-redirect to login after successful reset

### 3. Database Table: PasswordResetTokens
**Location:** `resources/database/password_reset_tokens.sql`

**Schema:**
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
        ON DELETE CASCADE
);
```

**Indexes:**
- `IX_PasswordResetTokens_Token` on reset_token (for fast lookups)
- `IX_PasswordResetTokens_Expiry` on expiry_datetime (for cleanup queries)

**Stored Procedure:**
- `sp_CleanupExpiredTokens` - Removes expired and used tokens

### 4. Updated LoginView.java
**Changes:**
- Added "Forgot password?" hyperlink below login button
- Hyperlink navigates to ForgotPasswordView

## Setup Instructions

### Step 1: Run Database Script
Open SQL Server Management Studio (or use sqlcmd) and run:

```powershell
# Using sqlcmd
sqlcmd -S DESKTOP-UDDCF59 -E -i "c:\Users\Misha\Orchestrate_EmployeeScheduler\resources\database\password_reset_tokens.sql"
```

Or manually execute the SQL script in SSMS.

### Step 2: Compile the Application
```powershell
$env:JAVAFX_PATH = "C:\javafx-sdk-21.0.1\lib"
javac -d out --module-path $env:JAVAFX_PATH --add-modules javafx.controls -cp "lib\*" (Get-ChildItem -Recurse -Filter *.java | Select-Object -ExpandProperty FullName)
```

### Step 3: Run the Application
```powershell
java -cp ".\out;.\lib\*" --module-path $env:JAVAFX_PATH --add-modules javafx.controls com.intramural.scheduling.Main
```

## Usage Flow

### User Perspective:

1. **Navigate to Forgot Password**
   - Click "Forgot password?" link on login screen
   - Or directly open ForgotPasswordView

2. **Request Reset Token**
   - Enter email address (e.g., misha@example.com)
   - Click "Send Reset Token"
   - Token appears on screen (in production, would be emailed)

3. **Reset Password**
   - Click "I have a token - Reset Password"
   - Enter the token shown
   - Enter new password (minimum 8 characters)
   - Confirm new password
   - Click "Reset Password"

4. **Success**
   - See success message
   - Automatically redirected to login after 2 seconds
   - Login with new password

## Security Features

✅ **Token Expiration:** Tokens expire after 1 hour
✅ **One-time Use:** Tokens are marked as used after successful reset
✅ **Email Validation:** Email format is validated before token generation
✅ **Password Strength:** Minimum 8 characters required
✅ **Transaction Safety:** Password reset uses database transactions
✅ **User Verification:** Only generates tokens for existing email addresses

## Testing

### Test Scenario 1: Successful Password Reset
1. Navigate to forgot password page
2. Enter: `misha@example.com`
3. Click "Send Reset Token"
4. Copy the token displayed
5. Click "I have a token - Reset Password"
6. Paste token, enter new password (e.g., "NewPassword123")
7. Confirm password
8. Click "Reset Password"
9. Should redirect to login
10. Login with username: `Misha` and password: `NewPassword123`

### Test Scenario 2: Invalid Email
1. Enter: `nonexistent@example.com`
2. Should show: "No account found with this email address."

### Test Scenario 3: Expired Token
1. Generate a token
2. Wait 1 hour (or manually change expiry in database)
3. Try to use the token
4. Should show: "Invalid or expired token. Please request a new one."

### Test Scenario 4: Password Mismatch
1. Generate token
2. Enter token
3. Enter password: "Password123"
4. Enter confirm: "Password456"
5. Should show: "Passwords do not match."

## Future Enhancements

### Email Integration (Optional)
To send actual emails instead of displaying tokens:

1. **Add JavaMail Dependency**
   Download `javax.mail.jar` and add to `lib/` folder

2. **Update PasswordResetService**
```java
import javax.mail.*;
import javax.mail.internet.*;

public void sendResetEmail(String email, String token) {
    Properties props = new Properties();
    props.put("mail.smtp.host", "smtp.gmail.com");
    props.put("mail.smtp.port", "587");
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    
    Session session = Session.getInstance(props, new Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication("your-email@gmail.com", "your-app-password");
        }
    });
    
    try {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("noreply@scheduling.com"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
        message.setSubject("Password Reset Request");
        message.setText("Your password reset token is: " + token + "\n\n" +
                       "This token will expire in 1 hour.\n\n" +
                       "If you didn't request this, please ignore this email.");
        
        Transport.send(message);
    } catch (MessagingException e) {
        e.printStackTrace();
    }
}
```

3. **Update ForgotPasswordView**
   - Hide token display
   - Show "Check your email" message instead

### Password Hashing
Currently using plain text (for demo). For production:

1. **Add BCrypt Dependency**
   Download `bcrypt.jar` or use Spring Security

2. **Update PasswordResetService.hashPassword()**
```java
import org.mindrot.jbcrypt.BCrypt;

private String hashPassword(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt());
}
```

3. **Update AuthenticationService** to use BCrypt for verification

## Troubleshooting

### Issue: Database Connection Error
**Solution:** Ensure SQL Server is running and Windows Authentication is enabled
```powershell
# Check SQL Server service
Get-Service -Name MSSQLSERVER
```

### Issue: Token Not Generated
**Solution:** Check if email exists in Users table
```sql
SELECT * FROM Users WHERE email = 'misha@example.com';
```

### Issue: Compilation Error
**Solution:** Ensure all imports are correct and JavaFX is in classpath
```powershell
# Verify JAVAFX_PATH
echo $env:JAVAFX_PATH
```

## Database Maintenance

### Clean Up Expired Tokens
Run periodically to remove old tokens:
```sql
EXEC sp_CleanupExpiredTokens;
```

Or schedule as SQL Server Agent Job for automatic cleanup.

### Check Active Tokens
```sql
SELECT u.username, prt.reset_token, prt.expiry_datetime, prt.is_used
FROM PasswordResetTokens prt
JOIN Users u ON prt.user_id = u.user_id
WHERE prt.expiry_datetime > GETDATE() AND prt.is_used = 0;
```

## File Structure
```
Orchestrate_EmployeeScheduler/
├── src/
│   └── com/intramural/scheduling/
│       ├── service/
│       │   └── PasswordResetService.java       [NEW]
│       └── view/
│           ├── ForgotPasswordView.java         [NEW]
│           └── LoginView.java                  [UPDATED]
└── resources/
    └── database/
        └── password_reset_tokens.sql           [NEW]
```

## Summary
✅ Secure token-based password reset
✅ User-friendly JavaFX interface
✅ Database integration with SQL Server
✅ Email validation and password strength checking
✅ Transaction-safe password updates
✅ Automatic token expiration and cleanup
✅ Ready for email integration enhancement
