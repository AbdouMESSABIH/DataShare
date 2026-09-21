#!/usr/bin/env bash

set -euo pipefail

echo "======================================"
echo " Installation des prérequis DataShare"
echo "======================================"

if command -v dnf >/dev/null 2>&1; then
    echo "Distribution Fedora / dnf détectée."

    sudo dnf install -y \
        java-21-openjdk-devel \
        nodejs \
        npm \
        postgresql-server \
        postgresql-contrib \
        git \
        curl

elif command -v apt-get >/dev/null 2>&1; then
    echo "Distribution Debian / Ubuntu détectée."

    sudo apt-get update
    sudo apt-get install -y \
        openjdk-21-jdk \
        nodejs \
        npm \
        postgresql \
        postgresql-contrib \
        git \
        curl
else
    echo "Distribution non prise en charge automatiquement."
    echo "Installez manuellement : Java 21, Node.js, npm, PostgreSQL, Git et curl."
    exit 1
fi

echo
echo "Versions détectées :"
java -version
node --version
npm --version
psql --version

echo
echo "Les prérequis DataShare sont installés."
echo "Configurez ensuite PostgreSQL, DB_PASSWORD et JWT_SECRET."
