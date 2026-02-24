# run-demos.ps1 (Week1 Lab)
$ErrorActionPreference = "Stop"

$proj = $PSScriptRoot

function Run-Java([string]$mainClass) {
  Write-Host "`n>>> RUN: $mainClass`n"
  java -cp "build\classes\java\main" $mainClass
}

Push-Location $proj
try {
  Write-Host "`n=== WEEK 1 LAB: BUILD + DEMOS ===`n"
  .\gradlew clean classes --console=plain --no-daemon | Out-Host

  # Pick the ones that have a public static void main(String[] args)
  Run-Java "edu.lu.concurrency.week1.lab1.AmdahlCalculator"
  Run-Java "edu.lu.concurrency.week1.lab1.QueueingLittleLawDemo"
  Run-Java "edu.lu.concurrency.week1.lab1.LatencyThroughputDemo"
  Run-Java "edu.lu.concurrency.week1.lab1.SQLiteConcurrencyDemo"
  Run-Java "edu.lu.concurrency.week1.lab1.ThreadedCounterWithLock"

  # Optional server demos (only if they terminate on their own)
  # Run-Java "edu.lu.concurrency.week1.lab1.SingleThreadedServer"
  # Run-Java "edu.lu.concurrency.week1.lab1.MultiThreadedServer"

  Write-Host "`n✅ Week1 demos finished.`n"
}
finally {
  Pop-Location
}
