#!/usr/bin/env bash
# Build SkyfullTools locally. Requires JDK 21+ and Maven with internet access.
set -e
cd "$(dirname "$0")"

echo ">> Building SkyfullTools..."
mvn -B package

echo
echo ">> Done. Your plugin JAR is:"
ls -1 target/SkyfullTools-*.jar
echo ">> Copy it into your server's  plugins/  folder and restart."
