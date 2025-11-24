<#
run_all_tests.ps1
Compile and run all test pages (Dashboard, Schedule, Employees, Analytics)
#>

param(
    [string]$JavaFXLib = 'C:\Users\Misha\Downloads\openjfx-21.0.9_windows-x64_bin-sdk\javafx-sdk-21.0.9\lib',
    [string]$Test = 'all'  # Options: all, dashboard, schedule, employees, analytics
)

Write-Host "=== Employee Scheduling System - Test Runner ===" -ForegroundColor Cyan
Write-Host "JavaFX lib: $JavaFXLib" -ForegroundColor Gray

# Ensure output directory
New-Item -ItemType Directory -Path out -Force | Out-Null

# Compile all sources
Write-Host "`nCompiling all Java sources..." -ForegroundColor Yellow
$srcFiles = Get-ChildItem -Recurse -Filter *.java -Path src | ForEach-Object { $_.FullName }
javac --module-path "$JavaFXLib" --add-modules javafx.controls -d out $srcFiles

if ($LASTEXITCODE -ne 0) {
    Write-Error "Compilation failed. See output above."
    exit 1
}

Write-Host "Compilation successful!`n" -ForegroundColor Green

# Define test classes
$tests = @{
    'dashboard' = 'com.intramural.scheduling.TestDashboard'
    'schedule'  = 'com.intramural.scheduling.view.TestSchedule'
    'employees' = 'com.intramural.scheduling.view.TestEmployees'
    'analytics' = 'com.intramural.scheduling.view.TestAnalytics'
}

function Run-Test {
    param([string]$Name, [string]$ClassName)
    
    Write-Host "Running $Name test..." -ForegroundColor Cyan
    Write-Host "Main class: $ClassName" -ForegroundColor Gray
    Write-Host "Press Ctrl+C to stop and run next test`n" -ForegroundColor Yellow
    
    $javaArgs = @(
        "--module-path", $JavaFXLib,
        "--add-modules", "javafx.controls",
        "-cp", "out;lib\*",
        $ClassName
    )
    
    java @javaArgs
    
    Write-Host "`n$Name test finished.`n" -ForegroundColor Green
}

# Run tests based on parameter
if ($Test -eq 'all') {
    Write-Host "Running all tests sequentially. Close each window to proceed to next test.`n" -ForegroundColor Yellow
    
    Run-Test "Dashboard" $tests['dashboard']
    Run-Test "Schedule" $tests['schedule']
    Run-Test "Employees" $tests['employees']
    Run-Test "Analytics" $tests['analytics']
    
    Write-Host "All tests completed!" -ForegroundColor Green
} else {
    if ($tests.ContainsKey($Test.ToLower())) {
        Run-Test $Test $tests[$Test.ToLower()]
    } else {
        Write-Error "Unknown test: $Test. Valid options: all, dashboard, schedule, employees, analytics"
        exit 1
    }
}
