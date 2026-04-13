<#
.SYNOPSIS
    TicketHub API Performance Benchmark Script
.DESCRIPTION
    Tu dong test hieu nang tat ca API endpoints chinh:
    - Response time tung endpoint (Cold vs Cached)
    - Concurrent load test (dong thoi)
    - So sanh hieu qua Redis Cache
    - Xuat bao cao HTML + Console
.NOTES
    Yeu cau: Server dang chay tai http://localhost:8081
             Redis dang chay tai localhost:6379
#>

param(
    [string]$BaseUrl = "http://localhost:8081",
    [int]$ConcurrentUsers = 50,
    [int]$WarmupRounds = 2,
    [int]$TestRounds = 5,
    [string]$ReportDir = ".\benchmark-reports"
)

$ErrorActionPreference = "Continue"
$timestamp = Get-Date -Format "yyyy-MM-dd_HH-mm-ss"
$reportFile = "$ReportDir\benchmark_$timestamp.html"

# ---- Console helpers ----
function Write-Header { param([string]$text); Write-Host ("`n" + ("=" * 60)) -ForegroundColor Cyan; Write-Host ("  " + $text) -ForegroundColor Cyan; Write-Host ("=" * 60) -ForegroundColor Cyan }
function Write-Info   { param([string]$text); Write-Host ("  [i] " + $text) -ForegroundColor Gray }
function Write-Ok     { param([string]$text); Write-Host ("  [OK] " + $text) -ForegroundColor Green }
function Write-Warn   { param([string]$text); Write-Host ("  [!] " + $text) -ForegroundColor Yellow }
function Write-Fail   { param([string]$text); Write-Host ("  [X] " + $text) -ForegroundColor Red }

# ---- Endpoint definitions ----
$endpoints = @(
    @{ Name = "Published Events (page 1)";    Url = '/api/events/published?page=0&size=10';                                        Cached = $true  }
    @{ Name = "Published Events (page 2)";    Url = '/api/events/published?page=1&size=10';                                        Cached = $true  }
    @{ Name = "Filter by Category";           Url = '/api/events/published?page=0&size=10&category=%C3%82m+nh%E1%BA%A1c';           Cached = $true  }
    @{ Name = "Filter by City";               Url = '/api/events/published?page=0&size=10&city=H%C3%A0+N%E1%BB%99i';                Cached = $true  }
    @{ Name = "Filter Featured";              Url = '/api/events/published?page=0&size=10&featured=true';                           Cached = $true  }
    @{ Name = "Sort by Date DESC";            Url = '/api/events/published?page=0&size=10&sort=date_desc';                          Cached = $true  }
    @{ Name = "Sort by Price ASC";            Url = '/api/events/published?page=0&size=10&sort=price_asc';                          Cached = $true  }
    @{ Name = "Event Detail (ID=1)";          Url = '/api/events/1';                                                                Cached = $false }
    @{ Name = "Event Detail (ID=5)";          Url = '/api/events/5';                                                                Cached = $false }
    @{ Name = "Search Events";                Url = '/api/events/search?q=event';                                                   Cached = $false }
    @{ Name = "Categories List";              Url = '/api/events/categories';                                                       Cached = $false }
    @{ Name = "Stats: Total Events";          Url = '/api/events/stats/total';                                                      Cached = $false }
    @{ Name = "Stats: Published Count";       Url = '/api/events/stats/published';                                                  Cached = $false }
    @{ Name = "Stats: Pending Count";         Url = '/api/events/stats/pending';                                                    Cached = $false }
    @{ Name = "City Events";                  Url = '/api/events/city/H%C3%A0+N%E1%BB%99i';                                         Cached = $false }
    @{ Name = "Platform Sales Active";        Url = '/api/platform-sales/active';                                                   Cached = $false }
    @{ Name = "Active Vouchers";              Url = '/api/platform-sales/active-vouchers';                                          Cached = $false }
    @{ Name = "Health Check";                 Url = '/api/health';                                                                  Cached = $false }
)

# ---- Helper functions ----
function Invoke-TimedRequest {
    param([string]$Url)
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    try {
        $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 30
        $sw.Stop()
        return @{
            StatusCode = $response.StatusCode
            Ms         = $sw.ElapsedMilliseconds
            BodySize   = $response.Content.Length
            Success    = $true
        }
    } catch {
        $sw.Stop()
        $code = 0
        if ($_.Exception.Response) { $code = [int]$_.Exception.Response.StatusCode }
        return @{
            StatusCode = $code
            Ms         = $sw.ElapsedMilliseconds
            BodySize   = 0
            Success    = $false
        }
    }
}

function Get-Percentile {
    param([double[]]$Values, [int]$Percentile)
    $sorted = $Values | Sort-Object
    $index = [math]::Floor($sorted.Count * $Percentile / 100)
    if ($index -ge $sorted.Count) { $index = $sorted.Count - 1 }
    return $sorted[$index]
}

function Get-MsHtml {
    param([double]$ms)
    $rounded = [math]::Round($ms, 1)
    if ($ms -lt 100)  { return "<span style='color:#22c55e;font-weight:bold'>$rounded ms</span>" }
    if ($ms -lt 500)  { return "<span style='color:#eab308;font-weight:bold'>$rounded ms</span>" }
    if ($ms -lt 1000) { return "<span style='color:#f97316;font-weight:bold'>$rounded ms</span>" }
    return "<span style='color:#ef4444;font-weight:bold'>$rounded ms</span>"
}

# ═════════════════════════════════════════════════════
#  PHASE 0: HEALTH CHECK
# ═════════════════════════════════════════════════════
Write-Header "TicketHub Performance Benchmark"
Write-Info "Server: $BaseUrl"
Write-Info "Concurrent Users: $ConcurrentUsers"
Write-Info "Test Rounds: $TestRounds"
Write-Info ("Timestamp: " + (Get-Date -Format 'yyyy-MM-dd HH:mm:ss'))
Write-Host ""

Write-Info "Checking server availability..."
$healthUrl = $BaseUrl + '/api/health'
$healthCheck = Invoke-TimedRequest -Url $healthUrl
if (-not $healthCheck.Success) {
    Write-Fail "Server is not responding at $BaseUrl"
    Write-Fail "Please start the server first: mvnw spring-boot:run"
    exit 1
}
Write-Ok ("Server is running (responded in " + $healthCheck.Ms + " ms)")

# ═════════════════════════════════════════════════════
#  PHASE 1: INDIVIDUAL ENDPOINT BENCHMARKS
# ═════════════════════════════════════════════════════
Write-Header "Phase 1: Individual Endpoint Benchmarks"

$results = @()

foreach ($ep in $endpoints) {
    $fullUrl = $BaseUrl + $ep.Url
    Write-Host ""
    Write-Info ("Testing: " + $ep.Name)

    # Warmup
    for ($w = 0; $w -lt $WarmupRounds; $w++) {
        Invoke-TimedRequest -Url $fullUrl | Out-Null
    }

    # Cold call
    $coldUrl = $fullUrl
    if ($ep.Cached) {
        $coldUrl = $fullUrl + '&_bc=' + (Get-Random)
    }
    $coldResult = Invoke-TimedRequest -Url $coldUrl

    # Warm calls
    $warmTimes = @()
    for ($r = 0; $r -lt $TestRounds; $r++) {
        $result = Invoke-TimedRequest -Url $fullUrl
        $warmTimes += $result.Ms
        Start-Sleep -Milliseconds 50
    }

    $avgWarm = ($warmTimes | Measure-Object -Average).Average
    $minWarm = ($warmTimes | Measure-Object -Minimum).Minimum
    $maxWarm = ($warmTimes | Measure-Object -Maximum).Maximum

    $speedup = "-"
    if ($avgWarm -gt 0 -and $coldResult.Ms -gt 0) {
        $speedup = [math]::Round($coldResult.Ms / $avgWarm, 1)
    }

    $resultObj = [PSCustomObject]@{
        Name       = $ep.Name
        Cached     = $ep.Cached
        ColdMs     = $coldResult.Ms
        AvgMs      = [math]::Round($avgWarm, 1)
        MinMs      = $minWarm
        MaxMs      = $maxWarm
        Speedup    = $speedup
        Status     = $coldResult.StatusCode
        BodyKB     = [math]::Round($coldResult.BodySize / 1024, 1)
        Success    = $coldResult.Success
    }
    $results += $resultObj

    if ($resultObj.Success) {
        $cacheLabel = "NO CACHE"
        if ($ep.Cached) { $cacheLabel = "CACHED" }
        Write-Ok ("$cacheLabel | Cold: " + $resultObj.ColdMs + " ms | Avg: " + $resultObj.AvgMs + " ms | Speedup: " + $speedup + "x | Body: " + $resultObj.BodyKB + " KB")
    } else {
        Write-Fail ("FAILED (HTTP " + $resultObj.Status + ")")
    }
}

# ═════════════════════════════════════════════════════
#  PHASE 2: REDIS CACHE EFFECTIVENESS
# ═════════════════════════════════════════════════════
Write-Header "Phase 2: Redis Cache Effectiveness"

$cacheTestUrl = $BaseUrl + '/api/events/published?page=0&size=10'
Write-Info "Target: GET /api/events/published?page=0&size=10"
Write-Info "Running 10 sequential requests to measure cache warmup..."

$cacheSequence = @()
for ($i = 1; $i -le 10; $i++) {
    $r = Invoke-TimedRequest -Url $cacheTestUrl
    $cacheSequence += [PSCustomObject]@{
        Request = $i
        Ms      = $r.Ms
        Status  = $r.StatusCode
    }
    Start-Sleep -Milliseconds 100
}

Write-Host ""
Write-Host "  Request# | Response Time" -ForegroundColor White
Write-Host "  ---------+--------------" -ForegroundColor DarkGray
foreach ($cs in $cacheSequence) {
    $barLen = [math]::Max(1, [math]::Min(50, [int]($cs.Ms / 10)))
    $bar = [string]::new([char]0x2588, $barLen)
    $color = "Green"
    if ($cs.Ms -ge 200) { $color = "Red" }
    elseif ($cs.Ms -ge 50) { $color = "Yellow" }
    Write-Host ("  #" + $cs.Request.ToString().PadLeft(7) + " | " + $cs.Ms.ToString().PadLeft(6) + " ms  " + $bar) -ForegroundColor $color
}

$firstCall = $cacheSequence[0].Ms
$subsequentItems = $cacheSequence | Select-Object -Skip 1
$avgSubsequent = ($subsequentItems | Measure-Object -Property Ms -Average).Average
$cacheSpeedup = "-"
if ($avgSubsequent -gt 0) {
    $cacheSpeedup = [math]::Round($firstCall / $avgSubsequent, 1)
}
Write-Host ""
Write-Ok ("First call (cold): " + $firstCall + " ms")
Write-Ok ("Avg subsequent (cached): " + [math]::Round($avgSubsequent, 1) + " ms")
Write-Ok ("Cache speedup: " + $cacheSpeedup + "x")

# ═════════════════════════════════════════════════════
#  PHASE 3: CONCURRENT LOAD TEST
# ═════════════════════════════════════════════════════
Write-Header "Phase 3: Concurrent Load Test ($ConcurrentUsers users)"

$loadTestEndpoints = @(
    @{ Name = "Published Events";   Url = ($BaseUrl + '/api/events/published?page=0&size=10') }
    @{ Name = "Event Detail";       Url = ($BaseUrl + '/api/events/1') }
    @{ Name = "Search";             Url = ($BaseUrl + '/api/events/search?q=event') }
)

$loadResults = @()

foreach ($ltep in $loadTestEndpoints) {
    Write-Host ""
    Write-Info ("Load testing: " + $ltep.Name + " with $ConcurrentUsers concurrent requests...")

    # Warmup
    Invoke-TimedRequest -Url $ltep.Url | Out-Null
    Start-Sleep -Milliseconds 200

    $jobs = 1..$ConcurrentUsers | ForEach-Object {
        Start-Job -ScriptBlock {
            param($targetUrl)
            $sw = [System.Diagnostics.Stopwatch]::StartNew()
            try {
                $resp = Invoke-WebRequest -Uri $targetUrl -UseBasicParsing -TimeoutSec 30
                $sw.Stop()
                [PSCustomObject]@{ Success=$true; Ms=$sw.ElapsedMilliseconds; StatusCode=$resp.StatusCode }
            } catch {
                $sw.Stop()
                $code = 0
                if ($_.Exception.Response) { $code = [int]$_.Exception.Response.StatusCode }
                [PSCustomObject]@{ Success=$false; Ms=$sw.ElapsedMilliseconds; StatusCode=$code }
            }
        } -ArgumentList $ltep.Url
    }

    $jobResults = $jobs | Wait-Job | Receive-Job
    $jobs | Remove-Job -Force

    $successResults = @($jobResults | Where-Object { $_.Success -eq $true })
    $failResults    = @($jobResults | Where-Object { $_.Success -ne $true })
    $allTimes       = @($jobResults | ForEach-Object { $_.Ms })

    $avgTime = 0; $minTime = 0; $maxTime = 0; $p50 = 0; $p95 = 0; $p99 = 0; $throughput = 0
    if ($allTimes.Count -gt 0) {
        $avgTime    = [math]::Round(($allTimes | Measure-Object -Average).Average, 1)
        $minTime    = ($allTimes | Measure-Object -Minimum).Minimum
        $maxTime    = ($allTimes | Measure-Object -Maximum).Maximum
        $p50        = Get-Percentile -Values $allTimes -Percentile 50
        $p95        = Get-Percentile -Values $allTimes -Percentile 95
        $p99        = Get-Percentile -Values $allTimes -Percentile 99
        $throughput = [math]::Round(($ConcurrentUsers / ($maxTime / 1000)), 1)
    }

    $loadObj = [PSCustomObject]@{
        Name         = $ltep.Name
        TotalReqs    = $ConcurrentUsers
        SuccessCount = $successResults.Count
        FailCount    = $failResults.Count
        AvgMs        = $avgTime
        MinMs        = $minTime
        MaxMs        = $maxTime
        P50Ms        = $p50
        P95Ms        = $p95
        P99Ms        = $p99
        Throughput   = $throughput
    }
    $loadResults += $loadObj

    $successRate = [math]::Round(($loadObj.SuccessCount / $loadObj.TotalReqs) * 100, 1)
    Write-Ok ("Success: " + $loadObj.SuccessCount + "/" + $loadObj.TotalReqs + " (" + $successRate + "%)")
    Write-Ok ("Avg: " + $loadObj.AvgMs + " ms | P50: " + $loadObj.P50Ms + " ms | P95: " + $loadObj.P95Ms + " ms | P99: " + $loadObj.P99Ms + " ms")
    Write-Ok ("Throughput: ~" + $loadObj.Throughput + " req/s")
    if ($loadObj.FailCount -gt 0) {
        Write-Warn ("Failed requests: " + $loadObj.FailCount)
    }
}

# ═════════════════════════════════════════════════════
#  PHASE 4: GENERATE HTML REPORT
# ═════════════════════════════════════════════════════
Write-Header "Phase 4: Generating Report"

if (-not (Test-Path $ReportDir)) { New-Item -ItemType Directory -Path $ReportDir -Force | Out-Null }

# Build endpoint rows
$endpointRowsHtml = ""
foreach ($r in $results) {
    $statusBadge = "<span class='badge badge-fail'>" + $r.Status + "</span>"
    if ($r.Success) { $statusBadge = "<span class='badge badge-success'>" + $r.Status + "</span>" }
    $cacheBadge = "<span class='badge badge-nocache'>NO CACHE</span>"
    if ($r.Cached) { $cacheBadge = "<span class='badge badge-cache'>CACHED</span>" }
    $coldHtml  = Get-MsHtml -ms $r.ColdMs
    $avgHtml   = Get-MsHtml -ms $r.AvgMs
    $minHtml   = Get-MsHtml -ms $r.MinMs
    $maxHtml   = Get-MsHtml -ms $r.MaxMs
    $endpointRowsHtml += "<tr><td>" + $r.Name + "</td><td>" + $cacheBadge + "</td><td>" + $coldHtml + "</td><td>" + $avgHtml + "</td><td>" + $minHtml + "</td><td>" + $maxHtml + "</td><td><strong>" + $r.Speedup + "x</strong></td><td>" + $r.BodyKB + " KB</td><td>" + $statusBadge + "</td></tr>`n"
}

# Build cache rows
$cacheRowsHtml = ""
foreach ($cs in $cacheSequence) {
    $barWidth = [math]::Max(2, [math]::Min(100, [int]($cs.Ms / 5)))
    $barColor = "#22c55e"
    if ($cs.Ms -ge 200) { $barColor = "#ef4444" }
    elseif ($cs.Ms -ge 50) { $barColor = "#eab308" }
    $cacheRowsHtml += "<tr><td>#" + $cs.Request + "</td><td>" + $cs.Ms + " ms</td><td><div class='bar' style='width:" + $barWidth + "%;background:" + $barColor + "'></div></td></tr>`n"
}

# Build load rows
$loadRowsHtml = ""
foreach ($lr in $loadResults) {
    $successRate = [math]::Round(($lr.SuccessCount / $lr.TotalReqs) * 100, 1)
    $rateColor = "#ef4444"
    if ($successRate -ge 99) { $rateColor = "#22c55e" }
    elseif ($successRate -ge 90) { $rateColor = "#eab308" }
    $loadRowsHtml += "<tr><td>" + $lr.Name + "</td><td>" + $lr.TotalReqs + "</td><td><span style='color:" + $rateColor + ";font-weight:bold'>" + $successRate + "%</span></td><td>" + (Get-MsHtml -ms $lr.AvgMs) + "</td><td>" + (Get-MsHtml -ms $lr.P50Ms) + "</td><td>" + (Get-MsHtml -ms $lr.P95Ms) + "</td><td>" + (Get-MsHtml -ms $lr.P99Ms) + "</td><td>" + (Get-MsHtml -ms $lr.MinMs) + "</td><td>" + (Get-MsHtml -ms $lr.MaxMs) + "</td><td><strong>" + $lr.Throughput + " req/s</strong></td></tr>`n"
}

# Summary stats
$cachedEndpoints   = @($results | Where-Object { $_.Cached -and $_.Success })
$uncachedEndpoints = @($results | Where-Object { (-not $_.Cached) -and $_.Success })
$avgCachedMs  = "N/A"
$avgUncachedMs = "N/A"
if ($cachedEndpoints.Count -gt 0)   { $avgCachedMs   = [math]::Round(($cachedEndpoints   | Measure-Object -Property AvgMs -Average).Average, 1) }
if ($uncachedEndpoints.Count -gt 0) { $avgUncachedMs = [math]::Round(($uncachedEndpoints | Measure-Object -Property AvgMs -Average).Average, 1) }
$totalSuccess = ($results | Where-Object { $_.Success }).Count
$totalFail    = ($results | Where-Object { -not $_.Success }).Count

$dateStr = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'

$htmlContent = @"
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>TicketHub Benchmark Report</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:'Segoe UI',system-ui,sans-serif;background:#0f172a;color:#e2e8f0;padding:2rem;line-height:1.6}
.container{max-width:1400px;margin:0 auto}
h1{font-size:2rem;margin-bottom:.5rem;background:linear-gradient(135deg,#6366f1,#8b5cf6,#a855f7);-webkit-background-clip:text;-webkit-text-fill-color:transparent}
h2{font-size:1.3rem;margin:2rem 0 1rem;color:#818cf8;border-bottom:2px solid #1e293b;padding-bottom:.5rem}
.meta{color:#64748b;margin-bottom:2rem;font-size:.9rem}
.summary-cards{display:grid;grid-template-columns:repeat(auto-fit,minmax(200px,1fr));gap:1rem;margin-bottom:2rem}
.card{background:#1e293b;border-radius:12px;padding:1.5rem;border:1px solid #334155;transition:transform .2s}
.card:hover{transform:translateY(-2px);border-color:#6366f1}
.card-label{font-size:.8rem;color:#64748b;text-transform:uppercase;letter-spacing:1px}
.card-value{font-size:2rem;font-weight:bold;margin-top:.25rem}
.green{color:#22c55e}.blue{color:#6366f1}.yellow{color:#eab308}.red{color:#ef4444}.purple{color:#a855f7}
table{width:100%;border-collapse:collapse;background:#1e293b;border-radius:12px;overflow:hidden;margin-bottom:2rem}
th{background:#334155;padding:12px 16px;text-align:left;font-size:.8rem;text-transform:uppercase;letter-spacing:1px;color:#94a3b8}
td{padding:10px 16px;border-bottom:1px solid #263348}
tr:hover td{background:#263348}
.badge{padding:2px 8px;border-radius:4px;font-size:.75rem;font-weight:bold;display:inline-block}
.badge-cache{background:#6366f120;color:#818cf8;border:1px solid #6366f140}
.badge-nocache{background:#64748b20;color:#94a3b8;border:1px solid #64748b40}
.badge-success{background:#22c55e20;color:#22c55e;border:1px solid #22c55e40}
.badge-fail{background:#ef444420;color:#ef4444;border:1px solid #ef444440}
.bar{height:20px;border-radius:4px;min-width:4px}
.footer{margin-top:3rem;color:#475569;font-size:.85rem;text-align:center}
.note{color:#64748b;font-size:.85rem;margin-bottom:1rem}
</style>
</head>
<body>
<div class="container">
<h1>TicketHub Performance Report</h1>
<div class="meta">Generated: $dateStr | Server: $BaseUrl | Data: ~200 users, ~100 events | Redis TTL: 60s</div>

<div class="summary-cards">
<div class="card"><div class="card-label">Endpoints Tested</div><div class="card-value blue">$($results.Count)</div></div>
<div class="card"><div class="card-label">Success / Fail</div><div class="card-value green">$totalSuccess <span class="red" style="font-size:1rem">/ $totalFail</span></div></div>
<div class="card"><div class="card-label">Avg Cached Response</div><div class="card-value green">$avgCachedMs ms</div></div>
<div class="card"><div class="card-label">Avg Uncached Response</div><div class="card-value yellow">$avgUncachedMs ms</div></div>
<div class="card"><div class="card-label">Cache Speedup</div><div class="card-value purple">${cacheSpeedup}x</div></div>
<div class="card"><div class="card-label">Concurrent Users</div><div class="card-value blue">$ConcurrentUsers</div></div>
</div>

<h2>Phase 1: Individual Endpoint Benchmarks</h2>
<p class="note">Cold = first request (DB/cache miss) | Avg/Min/Max = subsequent $TestRounds requests</p>
<table>
<thead><tr><th>Endpoint</th><th>Cache</th><th>Cold</th><th>Avg</th><th>Min</th><th>Max</th><th>Speedup</th><th>Size</th><th>Status</th></tr></thead>
<tbody>$endpointRowsHtml</tbody>
</table>

<h2>Phase 2: Redis Cache Effectiveness</h2>
<p class="note">10 sequential requests to /api/events/published - observe the cache warmup pattern</p>
<table>
<thead><tr><th style="width:100px">Request</th><th style="width:120px">Time</th><th>Visual</th></tr></thead>
<tbody>$cacheRowsHtml</tbody>
</table>

<h2>Phase 3: Concurrent Load Test ($ConcurrentUsers users)</h2>
<p class="note">All requests fired simultaneously - measures server capacity under pressure</p>
<table>
<thead><tr><th>Endpoint</th><th>Reqs</th><th>Success%</th><th>Avg</th><th>P50</th><th>P95</th><th>P99</th><th>Min</th><th>Max</th><th>Throughput</th></tr></thead>
<tbody>$loadRowsHtml</tbody>
</table>

<div class="footer">TicketHub Benchmark Report - Generated by benchmark.ps1</div>
</div>
</body>
</html>
"@

$htmlContent | Out-File -FilePath $reportFile -Encoding UTF8
Write-Ok ("HTML report saved: " + $reportFile)

# ═════════════════════════════════════════════════════
#  CONSOLE SUMMARY
# ═════════════════════════════════════════════════════
Write-Header "BENCHMARK SUMMARY"
Write-Host ""
Write-Host "  Endpoints Tested:    $($results.Count)" -ForegroundColor White
Write-Host "  Passed:              $totalSuccess" -ForegroundColor Green
if ($totalFail -gt 0) {
    Write-Host "  Failed:              $totalFail" -ForegroundColor Red
} else {
    Write-Host "  Failed:              0" -ForegroundColor Green
}
Write-Host "  Avg Cached:          $avgCachedMs ms" -ForegroundColor Cyan
Write-Host "  Avg Uncached:        $avgUncachedMs ms" -ForegroundColor Yellow
Write-Host "  Cache Speedup:       ${cacheSpeedup}x" -ForegroundColor Magenta
Write-Host ""

$fullPath = (Resolve-Path $reportFile).Path
Write-Ok ("Full report: " + $fullPath)
Write-Host ""

# Open report
Start-Process $reportFile
