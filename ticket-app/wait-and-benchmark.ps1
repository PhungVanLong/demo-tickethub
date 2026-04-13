# Wait for server to be ready, then run benchmark
Write-Host "Waiting for server at http://localhost:8081 ..." -ForegroundColor Yellow

$ready = $false
for ($i = 1; $i -le 40; $i++) {
    Start-Sleep -Seconds 3
    try {
        Invoke-WebRequest -Uri "http://localhost:8081/api/health" -UseBasicParsing -TimeoutSec 3 | Out-Null
        Write-Host "Server is READY!" -ForegroundColor Green
        $ready = $true
        break
    } catch {
        Write-Host "  Attempt $i - not ready yet..."
    }
}

if (-not $ready) {
    Write-Host "TIMEOUT: Server did not start in time." -ForegroundColor Red
    exit 1
}

# Run benchmark
Write-Host "`nStarting benchmark...`n" -ForegroundColor Cyan
& ".\benchmark.ps1"
