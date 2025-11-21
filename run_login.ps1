param(
    [string]$JavaFXLib = 'C:\Users\Misha\Downloads\openjfx-21.0.9_windows-x64_bin-sdk\javafx-sdk-21.0.9\lib',
    [string]$NativeLibPath = '.\lib'
)

Write-Host "JavaFX lib: $JavaFXLib"
Write-Host "Native lib path: $NativeLibPath"

# compile
New-Item -ItemType Directory -Path out -Force | Out-Null
$srcFiles = Get-ChildItem -Recurse -Filter *.java -Path src | ForEach-Object { $_.FullName }
Write-Host "Compiling sources..."
javac --module-path "$JavaFXLib" --add-modules javafx.controls -d out $srcFiles
if($LASTEXITCODE -ne 0){
    Write-Error "Compilation failed; fix errors and retry."; exit 1
}

Write-Host "Running Main (shows LoginView)..."
$args = @()
if(Test-Path $NativeLibPath){ $args += "-Djava.library.path=$NativeLibPath" }
$args += "--module-path"; $args += $JavaFXLib
$args += "--add-modules"; $args += "javafx.controls"
$args += "-cp"; $args += "out;lib/*"
$args += "com.intramural.scheduling.Main"

Start-Process -NoNewWindow -Wait -FilePath java -ArgumentList $args
