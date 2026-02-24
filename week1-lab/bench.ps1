# bench.ps1
# Run from: week1-lab\
# Generates:
#   results\bench-runs\*.csv   (per-run)
#   results\bench_master.csv   (merged)
#
# Examples:
#   .\bench.ps1
#   .\bench.ps1 -Server MultiThreadedServer -Client LoadTestClient
#   .\bench.ps1 -Server CPUBoundServer -FibN 38
#   .\bench.ps1 -PoolSizes @(1,2,4,8) -Clients @(1,10,50) -RequestsPerClient @(10,50) -Warmup 5

param(
  [string]$Server = "MultiThreadedServer",      # MultiThreadedServer | ImprovedMultiThreadedServer | CPUBoundServer
  [string]$Client = "LoadTestClient",           # LoadTestClient | EnhancedLoadClient
  [int]$Port = 8080,
  [string]$HostName = "localhost",

  # Sweeps
  [int[]]$PoolSizes = @(1,2,4,8,16,32,64),
  [int[]]$Clients = @(1,5,10,25,50,100),
  [int[]]$RequestsPerClient = @(10,50,100),
  [int]$Warmup = 20,

  # For CPUBoundServer
  [int]$FibN = 38,

  # Robustness
  [int]$ServerStartupTimeoutMs = 6000
)

$ErrorActionPreference = "Stop"

function Ensure-GradleBuild {
  Write-Host "==> Building (classes)..." -ForegroundColor Cyan
  if (Test-Path ".\gradlew.bat") {
    .\gradlew.bat -q clean classes
  } elseif (Test-Path ".\gradlew") {
    .\gradlew -q clean classes
  } else {
    throw "gradlew/gradlew.bat not found. Run this from the week1-lab folder."
  }
}

function Get-ClassPath {
  $mainClasses   = Join-Path $PWD "build\classes\java\main"
  $mainResources = Join-Path $PWD "build\resources\main"
  if (!(Test-Path $mainClasses)) { throw "Missing $mainClasses. Build failed?" }
  return "$mainClasses;$mainResources"  # Windows separator ;
}

function Start-ServerProcess([string]$cp, [string]$fqcn, [string[]]$serverArgs) {
  $javaArgs = @("-cp", $cp, $fqcn) + $serverArgs
  Write-Host "   Starting server: java $($javaArgs -join ' ')" -ForegroundColor DarkGray
  return Start-Process -FilePath "java" -ArgumentList $javaArgs -PassThru -WindowStyle Hidden
}

function Stop-ServerProcess($proc) {
  if ($null -eq $proc) { return }
  try {
    if (!$proc.HasExited) {
      Write-Host "   Stopping server PID=$($proc.Id)" -ForegroundColor DarkGray
      Stop-Process -Id $proc.Id -Force
      Start-Sleep -Milliseconds 200
    }
  } catch { }
}

function Wait-ForPort([string]$hostname, [int]$port, [int]$timeoutMs) {
  $sw = [Diagnostics.Stopwatch]::StartNew()
  while ($sw.ElapsedMilliseconds -lt $timeoutMs) {
    try {
      $client = New-Object System.Net.Sockets.TcpClient
      $iar = $client.BeginConnect($hostname, $port, $null, $null)
      if ($iar.AsyncWaitHandle.WaitOne(200, $false)) {
        $client.EndConnect($iar)
        $client.Close()
        return $true
      }
      $client.Close()
    } catch { }
    Start-Sleep -Milliseconds 150
  }
  return $false
}

function Run-Client([string]$cp, [string]$fqcn, [string[]]$clientArgs) {
  $javaArgs = @("-cp", $cp, $fqcn) + $clientArgs
  Write-Host "   Running client: java $($javaArgs -join ' ')" -ForegroundColor DarkGray

  # Capture output for troubleshooting
  $out = & java @javaArgs 2>&1
  if ($LASTEXITCODE -ne 0) {
    Write-Host $out
    throw "Client exited with code $LASTEXITCODE"
  }
  return $out
}

function Get-FirstCsvHeaderAndDataLines([string[]]$lines) {
  # Some clients prepend metadata like "# host=...".
  # Find first non-empty, non-comment line as header.
  $headerIndex = -1
  for ($i=0; $i -lt $lines.Count; $i++) {
    $line = $lines[$i].Trim()
    if ($line.Length -eq 0) { continue }
    if ($line.StartsWith("#")) { continue }
    $headerIndex = $i
    break
  }
  if ($headerIndex -lt 0) {
    return @{ Header = $null; Data = @() }
  }
  $header = $lines[$headerIndex]
  $data = @()
  for ($j=$headerIndex+1; $j -lt $lines.Count; $j++) {
    $ln = $lines[$j].Trim()
    if ($ln.Length -eq 0) { continue }
    if ($ln.StartsWith("#")) { continue }
    $data += $lines[$j]
  }
  return @{ Header = $header; Data = $data }
}

function Append-RunCsvToMaster([string]$runCsvAbs, [string]$masterCsvAbs, [hashtable]$meta) {
  if (!(Test-Path $runCsvAbs)) {
    Write-Host "   WARNING: client did not produce CSV: $runCsvAbs" -ForegroundColor Yellow
    return
  }

  $lines = Get-Content $runCsvAbs
  if ($lines.Count -eq 0) { return }

  $parsed = Get-FirstCsvHeaderAndDataLines $lines
  if ($null -eq $parsed.Header) {
    Write-Host "   WARNING: CSV has no header/data (maybe only comments): $runCsvAbs" -ForegroundColor Yellow
    return
  }

  $metaHeader = "serverType,poolSize,host,port,clients,requestsPerClient,warmupSec,fibN"
  $metaRow    = "$($meta.serverType),$($meta.poolSize),$($meta.host),$($meta.port),$($meta.clients),$($meta.requests),$($meta.warmup),$($meta.fibN)"

  if (!(Test-Path $masterCsvAbs)) {
    "$metaHeader,$($parsed.Header)" | Out-File -FilePath $masterCsvAbs -Encoding UTF8
  }

  foreach ($row in $parsed.Data) {
    "$metaRow,$row" | Out-File -FilePath $masterCsvAbs -Encoding UTF8 -Append
  }
}

# ----------------------------
# Main
# ----------------------------
Ensure-GradleBuild
$cp = Get-ClassPath

$serverFqcn = "edu.lu.concurrency.week1.lab1.$Server"
$clientFqcn = "edu.lu.concurrency.week1.lab1.$Client"

$resultsDirAbs = Join-Path $PWD "results"
$runsDirAbs    = Join-Path $resultsDirAbs "bench-runs"
New-Item -ItemType Directory -Force -Path $runsDirAbs | Out-Null

$masterCsvAbs = Join-Path $resultsDirAbs "bench_master.csv"
if (Test-Path $masterCsvAbs) { Remove-Item $masterCsvAbs -Force }

Write-Host "`n==> Benchmark sweep starting..." -ForegroundColor Cyan
Write-Host "Server=$serverFqcn | Client=$clientFqcn | Host=$HostName | Port=$Port`n" -ForegroundColor Cyan

foreach ($pool in $PoolSizes) {
  foreach ($c in $Clients) {
    foreach ($r in $RequestsPerClient) {

      $tag = "{0}_pool{1}_c{2}_r{3}_w{4}" -f $Server, $pool, $c, $r, $Warmup

      # IMPORTANT:
      # Pass RELATIVE CSV path to Java (robust parsing),
      # use ABS path for file existence checks
      $runCsvRel = "results/bench-runs/$tag.csv"
      $runCsvAbs = Join-Path $PWD $runCsvRel

      $serverProc = $null
      try {
        # Start server (server type determines args)
        if ($Server -eq "ImprovedMultiThreadedServer") {
          $serverProc = Start-ServerProcess $cp $serverFqcn @("$Port")
        }
        elseif ($Server -eq "CPUBoundServer") {
          $serverProc = Start-ServerProcess $cp $serverFqcn @("$pool", "$FibN")
        }
        else {
          # MultiThreadedServer
          $serverProc = Start-ServerProcess $cp $serverFqcn @("$pool")
        }

        # Wait until server is actually listening
        $ok = Wait-ForPort -hostname $HostName -port $Port -timeoutMs $ServerStartupTimeoutMs
        if (!$ok) {
          throw "Server did not open port $Port within ${ServerStartupTimeoutMs}ms"
        }

        # Run client
        $clientArgs = @(
          "--host=$HostName",
          "--port=$Port",
          "--clients=$c",
          "--requests=$r",
          "--warmup=$Warmup",
          "--csv=$runCsvRel"
        )

        $clientOut = Run-Client $cp $clientFqcn $clientArgs

        # Merge
        Append-RunCsvToMaster -runCsvAbs $runCsvAbs -masterCsvAbs $masterCsvAbs -meta @{
          serverType = $Server
          poolSize   = $pool
          host       = $HostName
          port       = $Port
          clients    = $c
          requests   = $r
          warmup     = $Warmup
          fibN       = $FibN
        }

        Write-Host "✅ Done: $tag" -ForegroundColor Green
      }
      catch {
        Write-Host "❌ Failed: $tag -> $($_.Exception.Message)" -ForegroundColor Red
      }
      finally {
        Stop-ServerProcess $serverProc
        Start-Sleep -Milliseconds 300
      }
    }
  }
}

Write-Host "`n==> Finished." -ForegroundColor Cyan
Write-Host "Per-run CSVs: $runsDirAbs" -ForegroundColor Cyan
Write-Host "Master CSV:   $masterCsvAbs" -ForegroundColor Cyan