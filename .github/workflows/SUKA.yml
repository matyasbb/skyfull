name: Build SkyfullTools

# Builds the plugin JAR automatically in the cloud (no local setup needed).
# After it runs, open the run -> "Artifacts" -> download "SkyfullTools-jar".
# The build step locates pom.xml automatically, so it works whether the
# project sits at the repo root or inside a subfolder (e.g. skyfull-tools/).

on:
  push:
    branches: [ main, master ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
          cache: maven

      - name: Build with Maven
        run: |
          POM_DIR="$(dirname "$(find . -name pom.xml -not -path '*/target/*' | head -n1)")"
          echo ">> Found project in: $POM_DIR"
          cd "$POM_DIR"
          mvn -B package

      - name: Upload JAR
        uses: actions/upload-artifact@v4
        with:
          name: SkyfullTools-jar
          path: '**/target/SkyfullTools-*.jar'
          if-no-files-found: error
