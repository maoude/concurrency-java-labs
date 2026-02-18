# Week 2  Day 1 Lab  
## Threads: Creation, Lifecycle & Scheduling Reality

This lab supports:

## Part 1  Thread Mental Model
- Thread vs Process
- Heap vs Stack sharing
- Extending Thread vs Runnable vs Lambda
- start() vs run()
- Interleaving
- OS scheduling reality
- Sleep myth

## Part 2  Thread Lifecycle
- NEW  RUNNABLE  BLOCKED  WAITING  TIMED_WAITING  TERMINATED
- BLOCKED vs WAITING
- join() vs sleep()
- Priority experiment
- Thread dump reading
- Sleep does NOT release locks

---

# IMPORTANT: Gradle Setup Required

This project is provided WITHOUT the gradle wrapper folder (it is large).

You must generate it once.

---

# 1) Requirements

Install JDK 21 (recommended)

Verify installation:

    java -version

---

# 2) Generate Gradle Wrapper (Offline Safe)

If you downloaded Gradle manually (example: 9.3.1):

Example paths:
- Gradle extracted: D:\Downloads\gradle-9.3.1
- Zip file: D:\Downloads\gradle-9.3.1-bin.zip

Run:

    $proj   = "D:\courses\concerent\week2-lab\week2-day1-threads"
    $gradle = "D:\Downloads\gradle-9.3.1\bin\gradle.bat"
    $zip    = "D:\Downloads\gradle-9.3.1-bin.zip"

    $zipUrl = "file:///" + ($zip -replace '\\','/')

    & $gradle -p $proj wrapper --gradle-version 9.3.1 --gradle-distribution-url $zipUrl

After this, you will have:
- gradlew
- gradlew.bat
- gradle/wrapper/*

From now on, use only:

    .\gradlew.bat clean test

---

# 3) Build & Test

    .\gradlew.bat clean test

If dependencies fail:

    .\gradlew.bat --stop
    .\gradlew.bat clean test --refresh-dependencies

---

# 4) Running Demos

Compile:

    .\gradlew.bat classes

Example run:

    java -cp .\build\classes\java\main edu.lu.concurrency.week2.day1.part1_basics.Demo04_Interleaving

---

# 5) Thread Dump Practice

While Demo10 is running:

Find process:

    jps

Create dump:

    jstack -l <PID> > dump.txt

Look for:
- BLOCKED (on object monitor)
- waiting to lock
- TIMED_WAITING (sleeping)

---

# 6) What You Must Submit

1. Priority experiment results (data/priority-results-template.csv)
2. Short explanation:
   - Why run() does not create a new thread
   - Why sleep is unreliable
   - Difference between BLOCKED and WAITING

---

# 7) Folder Structure Check

    tree /F /A

---

# Engineering Mindset

Design as if the scheduler is adversarial.  
Correctness must not depend on timing.
