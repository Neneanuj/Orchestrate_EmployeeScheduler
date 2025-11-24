<#
run.ps1
Compile and run the project with JavaFX.

Default JavaFX SDK path is set to the SDK you provided. You can override by
passing `-JavaFXLib` when invoking the script.

Examples:
> .\run.ps1
> .\run.ps1 -JavaFXLib 'C:\path\to\javafx-sdk\lib'
> .\run.ps1 -JavaFXLib 'C:\...\lib' -NativeLibPath '.\\lib'
#>

param(
    [string]$JavaFXLib = 'C:\Users\Misha\Downloads\openjfx-21.0.9_windows-x64_bin-sdk\javafx-sdk-21.0.9\lib',
    [string]$NativeLibPath = '.\lib'
)

Write-Host "JavaFX lib: $JavaFXLib"
Write-Host "Native lib path: $NativeLibPath"

New-Item -ItemType Directory -Path out -Force | Out-Null

$srcFiles = Get-ChildItem -Recurse -Filter *.java -Path src | ForEach-Object { $_.FullName }

Write-Host "Compiling Java sources..."
javac --module-path "$JavaFXLib" --add-modules javafx.controls -d out $srcFiles
if($LASTEXITCODE -ne 0){
    Write-Error "Compilation failed. See output above."; exit 1
}

Write-Host "Running application..."

$libcp = "out;lib/*"

$javaArgs = @()
if(Test-Path $NativeLibPath){
    $javaArgs += "-Djava.library.path=$NativeLibPath"
}
$javaArgs += "--module-path"; $javaArgs += $JavaFXLib
$javaArgs += "--add-modules"; $javaArgs += "javafx.controls"
$javaArgs += "-cp"; $javaArgs += $libcp
$javaArgs += "com.intramural.scheduling.Main"

Start-Process -NoNewWindow -Wait -FilePath java -ArgumentList $javaArgs
