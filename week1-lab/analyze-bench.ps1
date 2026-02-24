# analyze-bench.ps1
# Reads:  results\bench_master.csv   (per-request rows)
# Writes: results\bench_summary.md
#         results\best_config.txt

$ErrorActionPreference = "Stop"

$resultsDir = Join-Path $PWD "results"
$masterCsv  = Join-Path $resultsDir "bench_master.csv"
$summaryMd  = Join-Path $resultsDir "bench_summary.md"
$bestTxt    = Join-Path $resultsDir "best_config.txt"

if (!(Test-Path $masterCsv)) {
  throw "Missing $masterCsv. Run bench.ps1 first."
}

Write-Host "==> Loading $masterCsv ..." -ForegroundColor Cyan
$data = Import-Csv $masterCsv
if ($data.Count -eq 0) { throw "bench_master.csv is empty." }

# Convert columns + add okBool safely
foreach ($r in $data) {
  $r.poolSize          = [int]$r.poolSize
  $r.port              = [int]$r.port
  $r.clients           = [int]$r.clients
  $r.requestsPerClient = [int]$r.requestsPerClient
  $r.warmupSec         = [int]$r.warmupSec
  $r.fibN              = [int]$r.fibN

  $r.timestamp_ms      = [int64]$r.timestamp_ms
  $r.client_id         = [int]$r.client_id
  $r.req_index         = [int]$r.req_index
  $r.global_req_no     = [int]$r.global_req_no
  $r.latency_ms        = [double]$r.latency_ms

  $okBool = ($r.ok.ToString().ToLower() -eq "true")
  # Add property (or overwrite if it already exists)
  $r | Add-Member -NotePropertyName okBool -NotePropertyValue $okBool -Force
}

function Percentile([double[]]$values, [double]$p) {
  if ($values.Count -eq 0) { return [double]::NaN }
  $sorted = $values | Sort-Object
  if ($sorted.Count -eq 1) { return [double]$sorted[0] }

  $rank = [int][Math]::Ceiling(($p / 100.0) * $sorted.Count)
  if ($rank -lt 1) { $rank = 1 }
  if ($rank -gt $sorted.Count) { $rank = $sorted.Count }
  return [double]$sorted[$rank - 1]
}

# Group by run configuration
$groups = $data | Group-Object -Property serverType,poolSize,host,port,clients,requestsPerClient,warmupSec,fibN

$stats = @()

foreach ($g in $groups) {
  $rows = $g.Group

  $latOk = @($rows | Where-Object { $_.okBool } | ForEach-Object { $_.latency_ms })

  $total = $rows.Count
  $okCnt = ($rows | Where-Object { $_.okBool }).Count
  $fail  = $total - $okCnt
  $errRate = if ($total -gt 0) { $fail / $total } else { 0 }

  $minTs = ($rows | Measure-Object timestamp_ms -Minimum).Minimum
  $maxTs = ($rows | Measure-Object timestamp_ms -Maximum).Maximum
  $durationMs = [double]($maxTs - $minTs)
  if ($durationMs -le 0) { $durationMs = 1 }

  $throughputRps = $total / ($durationMs / 1000.0)

  $avg = if ($latOk.Count -gt 0) { ($latOk | Measure-Object -Average).Average } else { [double]::NaN }
  $p50 = Percentile $latOk 50
  $p95 = Percentile $latOk 95
  $p99 = Percentile $latOk 99

  $k = $rows[0]

  $stats += [pscustomobject]@{
    serverType = $k.serverType
    poolSize   = [int]$k.poolSize
    host       = $k.host
    port       = [int]$k.port
    clients    = [int]$k.clients
    requestsPerClient = [int]$k.requestsPerClient
    warmupSec  = [int]$k.warmupSec
    fibN       = [int]$k.fibN

    totalRequests   = $total
    okRequests      = $okCnt
    failedRequests  = $fail
    errorRate       = [Math]::Round($errRate, 6)

    durationMs      = [int64]$durationMs
    throughputRps   = [Math]::Round($throughputRps, 3)

    avgLatencyMs    = [Math]::Round($avg, 3)
    p50LatencyMs    = [Math]::Round($p50, 3)
    p95LatencyMs    = [Math]::Round($p95, 3)
    p99LatencyMs    = [Math]::Round($p99, 3)
  }
}

# Selection constraints
$MaxP95  = 200.0
$MaxErr  = 0.01

$valid = $stats | Where-Object { $_.p95LatencyMs -le $MaxP95 -and $_.errorRate -le $MaxErr }
if ($valid.Count -eq 0) {
  Write-Host "WARNING: No run satisfies constraints (p95 <= $MaxP95, err <= $MaxErr). Picking best throughput overall." -ForegroundColor Yellow
  $valid = $stats
}

$best  = $valid | Sort-Object throughputRps -Descending | Select-Object -First 1
$top10 = $valid | Sort-Object throughputRps -Descending | Select-Object -First 10

# Write bench_summary.md
Write-Host "==> Writing $summaryMd ..." -ForegroundColor Cyan

$md = @()
$md += "# Benchmark Summary"
$md += ""
$md += "Source: `results/bench_master.csv` (per-request raw data)"
$md += ""
$md += "## Constraints (used for selecting best config)"
$md += "- p95 latency ≤ $MaxP95 ms"
$md += "- error rate ≤ $([int]($MaxErr*100))%"
$md += ""
$md += "## Best configuration"
$md += ""
$md += "| Field | Value |"
$md += "|------:|:------|"
$md += "| Server | $($best.serverType) |"
$md += "| Pool size | $($best.poolSize) |"
$md += "| Clients | $($best.clients) |"
$md += "| Requests/client | $($best.requestsPerClient) |"
$md += "| Warmup (s) | $($best.warmupSec) |"
$md += "| Throughput (RPS) | $($best.throughputRps) |"
$md += "| Avg latency (ms) | $($best.avgLatencyMs) |"
$md += "| p50 latency (ms) | $($best.p50LatencyMs) |"
$md += "| p95 latency (ms) | $($best.p95LatencyMs) |"
$md += "| p99 latency (ms) | $($best.p99LatencyMs) |"
$md += "| Error rate | $([Math]::Round($best.errorRate*100,2))% |"
$md += "| Total requests | $($best.totalRequests) |"
$md += "| Duration (ms) | $($best.durationMs) |"
$md += ""
$md += "## Top 10 (by throughput, after constraints)"
$md += ""
$md += "| Server | Pool | Clients | Req/Client | RPS | p95(ms) | Err% | Total |"
$md += "|--------|------|---------|-----------:|----:|--------:|-----:|------:|"
foreach ($r in $top10) {
  $md += "| $($r.serverType) | $($r.poolSize) | $($r.clients) | $($r.requestsPerClient) | $($r.throughputRps) | $($r.p95LatencyMs) | $([Math]::Round($r.errorRate*100,2)) | $($r.totalRequests) |"
}

$md | Out-File -FilePath $summaryMd -Encoding UTF8

# Write best_config.txt
Write-Host "==> Writing $bestTxt ..." -ForegroundColor Cyan

$txt = @()
$txt += "BEST CONFIGURATION"
$txt += "=================="
$txt += ""
$txt += "Server Type      : $($best.serverType)"
$txt += "Pool Size        : $($best.poolSize)"
$txt += "Host             : $($best.host)"
$txt += "Port             : $($best.port)"
$txt += "Clients          : $($best.clients)"
$txt += "Requests/Client  : $($best.requestsPerClient)"
$txt += "Warmup (s)       : $($best.warmupSec)"
$txt += "FibN             : $($best.fibN)"
$txt += ""
$txt += "Throughput (RPS) : $($best.throughputRps)"
$txt += "Avg Latency (ms) : $($best.avgLatencyMs)"
$txt += "p50 Latency (ms) : $($best.p50LatencyMs)"
$txt += "p95 Latency (ms) : $($best.p95LatencyMs)"
$txt += "p99 Latency (ms) : $($best.p99LatencyMs)"
$txt += "Error Rate       : $([Math]::Round($best.errorRate*100,2))%"
$txt += "Total Requests   : $($best.totalRequests)"
$txt += "Duration (ms)    : $($best.durationMs)"

$txt | Out-File -FilePath $bestTxt -Encoding UTF8

Write-Host "==> Done." -ForegroundColor Green
Write-Host "Summary: $summaryMd"
Write-Host "Best:    $bestTxt"