<#
run_main_only.ps1
Compile and run ONLY the Main and LoginView (minimal dependencies)
#>

param(
    [string]$JavaFXLib = 'C:\Users\Misha\Downloads\openjfx-21.0.9_windows-x64_bin-sdk\javafx-sdk-21.0.9\lib'
)

Write-Host "Compiling Main and LoginView only..." -ForegroundColor Cyan

# Clean
Remove-Item -Recurse -Force out -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path out -Force | Out-Null

# Compile minimal set
$files = @(
    "src\com\intramural\scheduling\Main.java",
    "src\com\intramural\scheduling\view\LoginView.java"
)

javac --module-path "$JavaFXLib" --add-modules javafx.controls -d out $files

if ($LASTEXITCODE -ne 0) {
    Write-Error "Compilation failed"
    exit 1
}

Write-Host "Running Main..." -ForegroundColor Green

java --module-path "$JavaFXLib" --add-modules javafx.controls -cp "out;lib\*" com.intramural.scheduling.Main
