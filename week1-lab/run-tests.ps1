# run-tests.ps1 (Week1 Lab)
$ErrorActionPreference = "Stop"

$proj = $PSScriptRoot
Push-Location $proj
try {
  Write-Host "`n=== WEEK 1 LAB: CLEAN TEST ===`n"
  .\gradlew clean test --console=plain --no-daemon | Out-Host

  $report = Join-Path $proj "build\reports\tests\test\index.html"
  if (Test-Path $report) {
    Write-Host "`nReport: $report`n"
  }

  Write-Host "`n Week1 tests finished.`n"
}
finally {
  Pop-Location
}
