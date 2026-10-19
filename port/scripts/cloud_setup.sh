#!/bin/bash
# Prepares a fresh Linux machine (e.g. a Claude Code cloud session) for the 26.1 port:
# installs JDK 25 if missing and downloads the NeoForge reference jar to ../ref/neo/.
# Needs network access to api.adoptium.net, github.com, maven.neoforged.net.
set -e
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
if ! java -version 2>&1 | grep -q '"25'; then
    if [ ! -x /opt/jdk25/bin/java ]; then
        URL="$(curl -fsSL 'https://api.adoptium.net/v3/assets/latest/25/hotspot?architecture=x64&image_type=jdk&os=linux' \
            | python3 -c 'import json,sys; print(json.load(sys.stdin)[0]["binary"]["package"]["link"])')"
        mkdir -p /opt/jdk25
        curl -fsSL "$URL" | tar -xz -C /opt/jdk25 --strip-components=1
    fi
    echo "JDK 25 at /opt/jdk25 - use: export JAVA_HOME=/opt/jdk25 PATH=/opt/jdk25/bin:\$PATH"
fi
NEO_VERSION=26.1.2.74
mkdir -p "$ROOT/ref/neo"
[ -f "$ROOT/ref/neo/neoforge.jar" ] || curl -fsSL -o "$ROOT/ref/neo/neoforge.jar" \
    "https://maven.neoforged.net/releases/net/neoforged/neoforge/$NEO_VERSION/neoforge-$NEO_VERSION-universal.jar"
echo "ref/neo/neoforge.jar ready"
