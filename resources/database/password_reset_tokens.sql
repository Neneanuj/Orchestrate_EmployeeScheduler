-- Create PasswordResetTokens table for forgot password functionality
-- This table stores password reset tokens with expiry times

USE SchedulingSystem;
GO

-- Drop table if exists (for re-running the script)
IF OBJECT_ID('PasswordResetTokens', 'U') IS NOT NULL
    DROP TABLE PasswordResetTokens;
GO

-- Create the table
CREATE TABLE PasswordResetTokens (
    token_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    reset_token VARCHAR(255) NOT NULL UNIQUE,
    expiry_datetime DATETIME NOT NULL,
    is_used BIT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    
    -- Foreign key constraint to Users table
    CONSTRAINT FK_PasswordResetTokens_Users 
        FOREIGN KEY (user_id) REFERENCES Users(user_id)
        ON DELETE CASCADE
);
GO

-- Create index on reset_token for faster lookups
CREATE INDEX IX_PasswordResetTokens_Token 
    ON PasswordResetTokens(reset_token);
GO

-- Create index on expiry_datetime for cleanup queries
CREATE INDEX IX_PasswordResetTokens_Expiry 
    ON PasswordResetTokens(expiry_datetime);
GO

-- Optional: Create a stored procedure to clean up expired tokens
CREATE PROCEDURE sp_CleanupExpiredTokens
AS
BEGIN
    DELETE FROM PasswordResetTokens
    WHERE expiry_datetime < GETDATE() OR is_used = 1;
END;
GO

PRINT 'PasswordResetTokens table created successfully!';
PRINT 'You can run: EXEC sp_CleanupExpiredTokens to remove expired tokens periodically.';
