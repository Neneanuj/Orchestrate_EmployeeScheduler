-- Database changes for Forgot Password Functionality
-- Run this script in SQL Server Management Studio or via sqlcmd

USE SchedulingSystem;
GO

-- Step 1: Add email column to Users table
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'email')
BEGIN
    ALTER TABLE users ADD email VARCHAR(100) NULL;
    PRINT '✓ Email column added to users table';
END
ELSE
BEGIN
    PRINT '✓ Email column already exists';
END
GO

-- Step 2: Create PasswordResetTokens table
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES 
               WHERE TABLE_NAME = 'PasswordResetTokens')
BEGIN
    CREATE TABLE PasswordResetTokens (
        token_id INT IDENTITY(1,1) PRIMARY KEY,
        user_id INT NOT NULL,
        reset_token VARCHAR(255) NOT NULL UNIQUE,
        expiry_datetime DATETIME NOT NULL,
        is_used BIT NOT NULL DEFAULT 0,
        created_at DATETIME NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_PasswordResetTokens_Users 
            FOREIGN KEY (user_id) REFERENCES users(user_id)
            ON DELETE CASCADE
    );
    
    CREATE INDEX IX_PasswordResetTokens_Token ON PasswordResetTokens(reset_token);
    CREATE INDEX IX_PasswordResetTokens_Expiry ON PasswordResetTokens(expiry_datetime);
    
    PRINT '✓ PasswordResetTokens table created successfully';
END
ELSE
BEGIN
    PRINT '✓ PasswordResetTokens table already exists';
END
GO

-- Step 3: Update existing user with email
UPDATE users 
SET email = 'misha@example.com' 
WHERE username = 'Misha' AND (email IS NULL OR email = '');
PRINT '✓ Updated Misha user with email';
GO

-- Step 4: Verify changes
PRINT '';
PRINT '========================================';
PRINT 'Verification:';
PRINT '========================================';

SELECT 'Users table structure:' AS Info;
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'users' 
ORDER BY ORDINAL_POSITION;

SELECT 'PasswordResetTokens table structure:' AS Info;
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'PasswordResetTokens' 
ORDER BY ORDINAL_POSITION;

SELECT 'User data:' AS Info;
SELECT user_id, username, role, email 
FROM users;

PRINT '';
PRINT '✓ Database changes completed successfully!';
PRINT '✓ Forgot password functionality is ready to use';
GO
