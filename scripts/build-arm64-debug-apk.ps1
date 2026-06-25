param(
    [string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path,
    [switch]$RerunTasks
)

$ErrorActionPreference = 'Stop'

$ProjectRoot = (Resolve-Path -LiteralPath $ProjectRoot).Path
$outputDir = Join-Path $ProjectRoot 'app\build\outputs\apk\debug'
$targetName = 'app-arm64-v8a-debug.apk'
$targetPath = Join-Path $outputDir $targetName
$backupRoot = Join-Path $ProjectRoot 'app\build\tmp\arm64-debug-apk-output-backup'
$allowedTempRoot = Join-Path $ProjectRoot 'app\build\tmp'

function Assert-UnderDirectory([string]$Path, [string]$Parent) {
    $fullPath = [System.IO.Path]::GetFullPath($Path)
    $fullParent = [System.IO.Path]::GetFullPath($Parent).TrimEnd('\') + '\'
    if (!$fullPath.StartsWith($fullParent, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Unexpected path outside allowed directory: $fullPath"
    }
}

Assert-UnderDirectory -Path $backupRoot -Parent $allowedTempRoot
if (Test-Path -LiteralPath $backupRoot) {
    Remove-Item -LiteralPath $backupRoot -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $backupRoot | Out-Null

$preserved = @()
if (Test-Path -LiteralPath $outputDir) {
    Get-ChildItem -LiteralPath $outputDir -File | Where-Object { $_.Name -ne $targetName } | ForEach-Object {
        $backupPath = Join-Path $backupRoot $_.Name
        Copy-Item -LiteralPath $_.FullName -Destination $backupPath -Force
        $preserved += [pscustomobject]@{
            Name = $_.Name
            Path = $_.FullName
            BackupPath = $backupPath
            LastWriteTimeUtc = $_.LastWriteTimeUtc
        }
    }
}

$buildError = $null
$pushedLocation = $false
try {
    Push-Location $ProjectRoot
    $pushedLocation = $true
    $gradleArgs = @(':app:assembleDebug')
    if ($RerunTasks) {
        $gradleArgs += '--rerun-tasks'
    }
    & .\gradlew @gradleArgs
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle build failed with exit code $LASTEXITCODE"
    }
} catch {
    $buildError = $_
} finally {
    if ($pushedLocation) {
        Pop-Location
    }
    foreach ($file in $preserved) {
        Copy-Item -LiteralPath $file.BackupPath -Destination $file.Path -Force
        [System.IO.File]::SetLastWriteTimeUtc($file.Path, $file.LastWriteTimeUtc)
    }
}

if ($buildError -ne $null) {
    throw $buildError
}

if (!(Test-Path -LiteralPath $targetPath)) {
    throw "Target APK was not produced: $targetPath"
}

Get-Item -LiteralPath $targetPath | Select-Object FullName, Length, LastWriteTime
