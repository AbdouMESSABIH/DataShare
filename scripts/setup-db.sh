#!/usr/bin/env bash

set -euo pipefail

DB_NAME="datashare"
DB_USER="datashare"

echo "=== Configuration PostgreSQL pour DataShare ==="

if ! command -v psql >/dev/null 2>&1; then
    echo "Erreur : PostgreSQL n'est pas installé."
    echo "Sous Fedora : sudo dnf install postgresql-server postgresql-contrib"
    exit 1
fi

if [[ -z "${DB_PASSWORD:-}" ]]; then
    echo "Erreur : la variable DB_PASSWORD n'est pas définie."
    echo
    echo "Exemple :"
    echo "export DB_PASSWORD='votre_mot_de_passe_postgresql'"
    exit 1
fi

if ! systemctl is-active --quiet postgresql; then
    echo "PostgreSQL n'est pas démarré."
    echo "Démarrage du service..."
    sudo systemctl start postgresql
fi

echo "Vérification de l'utilisateur PostgreSQL '${DB_USER}'..."

if sudo -u postgres psql -tAc \
    "SELECT 1 FROM pg_roles WHERE rolname='${DB_USER}'" \
    | grep -q 1; then

    echo "Utilisateur '${DB_USER}' déjà présent."

else
    echo "Création de l'utilisateur '${DB_USER}'..."

    sudo -u postgres psql \
        --set=ON_ERROR_STOP=1 \
        --set=app_password="$DB_PASSWORD" <<'SQL'
CREATE ROLE datashare LOGIN PASSWORD :'app_password';
SQL

fi

echo "Mise à jour du mot de passe de l'utilisateur '${DB_USER}'..."

sudo -u postgres psql \
    --set=ON_ERROR_STOP=1 \
    --set=app_password="$DB_PASSWORD" <<'SQL'
ALTER ROLE datashare WITH PASSWORD :'app_password';
SQL

echo "Vérification de la base '${DB_NAME}'..."

if sudo -u postgres psql -tAc \
    "SELECT 1 FROM pg_database WHERE datname='${DB_NAME}'" \
    | grep -q 1; then

    echo "Base '${DB_NAME}' déjà présente."

else
    echo "Création de la base '${DB_NAME}'..."
    sudo -u postgres createdb \
        --owner="${DB_USER}" \
        "${DB_NAME}"
fi

echo
echo "=== Configuration terminée ==="
echo "Base       : ${DB_NAME}"
echo "Utilisateur: ${DB_USER}"
echo
echo "Le mot de passe n'a pas été écrit dans le repository."