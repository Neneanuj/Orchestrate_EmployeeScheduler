# Forgot Password Feature - Quick Setup Script
# Run this script to set up and test the forgot password functionality

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   Forgot Password Setup Script" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Step 1: Create database table
Write-Host "[Step 1/4] Creating PasswordResetTokens table..." -ForegroundColor Yellow
$sqlScript = "c:\Users\Misha\Orchestrate_EmployeeScheduler\resources\database\password_reset_tokens.sql"

try {
    sqlcmd -S DESKTOP-UDDCF59 -E -i $sqlScript
    Write-Host "✓ Database table created successfully!" -ForegroundColor Green
} catch {
    Write-Host "✗ Database creation failed. You may need to run the SQL script manually." -ForegroundColor Red
    Write-Host "  Script location: $sqlScript" -ForegroundColor Red
}

# Step 2: Set JavaFX Path
Write-Host "`n[Step 2/4] Setting JavaFX path..." -ForegroundColor Yellow
$env:JAVAFX_PATH = "C:\javafx-sdk-21.0.1\lib"
Write-Host "✓ JAVAFX_PATH set to: $env:JAVAFX_PATH" -ForegroundColor Green

# Step 3: Compile the application
Write-Host "`n[Step 3/4] Compiling application..." -ForegroundColor Yellow
Set-Location "c:\Users\Misha\Orchestrate_EmployeeScheduler"

try {
    $javaFiles = Get-ChildItem -Recurse -Filter *.java | Select-Object -ExpandProperty FullName
    javac -d out --module-path $env:JAVAFX_PATH --add-modules javafx.controls -cp "lib\*" $javaFiles
    Write-Host "✓ Compilation successful!" -ForegroundColor Green
} catch {
    Write-Host "✗ Compilation failed. Check for errors above." -ForegroundColor Red
    exit
}

# Step 4: Run the application
Write-Host "`n[Step 4/4] Starting application..." -ForegroundColor Yellow
Write-Host "`nTo test the forgot password feature:" -ForegroundColor Cyan
Write-Host "  1. Click 'Forgot password?' on the login screen" -ForegroundColor White
Write-Host "  2. Enter email: misha@example.com" -ForegroundColor White
Write-Host "  3. Click 'Send Reset Token'" -ForegroundColor White
Write-Host "  4. Copy the token displayed" -ForegroundColor White
Write-Host "  5. Click 'I have a token - Reset Password'" -ForegroundColor White
Write-Host "  6. Enter the token and new password" -ForegroundColor White
Write-Host "  7. Click 'Reset Password'" -ForegroundColor White
Write-Host "`nStarting application now...`n" -ForegroundColor Cyan

java -cp ".\out;.\lib\*" --module-path $env:JAVAFX_PATH --add-modules javafx.controls com.intramural.scheduling.Main
