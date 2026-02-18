#!/usr/bin/env bash
set -e
echo "Basic verification: build only"
./gradlew -q clean build
echo "OK"
