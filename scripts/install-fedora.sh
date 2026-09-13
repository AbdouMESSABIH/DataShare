#!/usr/bin/env bash

set -euo pipefail

echo "=== Installation de l'environnement DataShare sous Fedora ==="

if ! command -v dnf >/dev/null 2>&1; then
    echo "Erreur : ce script est prévu pour Fedora."
    exit 1
fi

echo
echo "Installation des dépendances système..."

sudo dnf install -y \
    git \
    java-21-openjdk-devel \
    nodejs \
    npm \
    postgresql-server \
    postgresql-contrib \
    python3

echo
echo "Vérification de PostgreSQL..."

if [[ ! -f /var/lib/pgsql/data/PG_VERSION ]]; then
    echo "Initialisation de PostgreSQL..."

    sudo postgresql-setup --initdb --unit postgresql
else
    echo "PostgreSQL est déjà initialisé."
fi

echo
echo "Activation et démarrage de PostgreSQL..."

sudo systemctl enable --now postgresql

echo
echo "Vérification des outils..."

echo
echo "--- Git ---"
git --version

echo
echo "--- Java ---"
java --version

echo
echo "--- Node.js ---"
node --version

echo
echo "--- npm ---"
npm --version

echo
echo "--- PostgreSQL ---"
psql --version

echo
echo "=== Installation terminée ==="
echo
echo "Étape suivante :"
echo
echo "1. Définir DB_PASSWORD :"
echo "   export DB_PASSWORD='votre_mot_de_passe'"
echo
echo "2. Configurer la base :"
echo "   ./scripts/setup-db.sh"
echo
echo "3. Définir JWT_SECRET avant de lancer le backend."
echo
echo "Aucun secret n'est enregistré par ce script."