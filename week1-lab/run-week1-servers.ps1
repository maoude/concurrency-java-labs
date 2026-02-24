# run-week1-servers.ps1
$ErrorActionPreference = "Stop"

# --- Encoding (prevents weird symbols) ---
try {
  chcp 65001 | Out-Null
  $OutputEncoding = [System.Text.UTF8Encoding]::new()
  [Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()
} catch {}

# --- Helpers ---
function Ensure-Build {
  Write-Host "`n=== BUILD (classes) ===`n"
  .\gradlew -q clean classes --console=plain --no-daemon | Out-Host
}

function Run-Java([string]$mainClass, [string[]]$args = @(), [int]$timeoutSec = 30) {
  $cp = "build\classes\java\main"
  $argList = @("-cp", $cp, $mainClass) + $args

  Write-Host "`n>>> RUN: $mainClass $($args -join ' ')`n"

  $outFile = Join-Path $env:TEMP "java_out.txt"
  $errFile = Join-Path $env:TEMP "java_err.txt"

  $p = Start-Process -FilePath "java" `
        -ArgumentList $argList `
        -NoNewWindow `
        -PassThru `
        -RedirectStandardOutput $outFile `
        -RedirectStandardError $errFile

  if (-not $p.WaitForExit($timeoutSec * 1000)) {
    Stop-Process -Id $p.Id -Force
    throw "Timeout running $mainClass after ${timeoutSec}s"
  }

  if (Test-Path $outFile) { Get-Content $outFile | Out-Host }
  if (Test-Path $errFile) { Get-Content $errFile | Out-Host }
}

function Start-JavaServer([string]$mainClass, [string[]]$args = @(), [int]$startupWaitMs = 800) {
  $cp = "build\classes\java\main"
  $argList = @("-cp", $cp, $mainClass) + $args

  Write-Host "`n>>> START SERVER: $mainClass $($args -join ' ')`n"

  $p = Start-Process -FilePath "java" `
        -ArgumentList $argList `
        -NoNewWindow `
        -PassThru

  Start-Sleep -Milliseconds $startupWaitMs
  return $p
}

function Stop-JavaServer($p) {
  if ($p -and -not $p.HasExited) {
    Write-Host "`n>>> STOP SERVER (pid $($p.Id))`n"
    Stop-Process -Id $p.Id -Force
  }
}

# --- Main run ---
Push-Location $PSScriptRoot
try {
  Ensure-Build

  # --- Scenario A: SingleThreadedServer + LoadClient ---
  $port = "8080"
  $server = Start-JavaServer "edu.lu.concurrency.week1.lab1.SingleThreadedServer" @($port)
  try {
    Run-Java "edu.lu.concurrency.week1.lab1.LoadClient" @("localhost", $port, "100") 30
  } finally {
    Stop-JavaServer $server
  }

  # --- Scenario B: MultiThreadedServer + LoadClient ---
  $port2 = "8081"
  $server2 = Start-JavaServer "edu.lu.concurrency.week1.lab1.MultiThreadedServer" @($port2)
  try {
    Run-Java "edu.lu.concurrency.week1.lab1.LoadClient" @("localhost", $port2, "200") 30
  } finally {
    Stop-JavaServer $server2
  }

  Write-Host "`n✅ Week1 server/client scenarios finished.`n"
}
finally {
  Pop-Location
}
