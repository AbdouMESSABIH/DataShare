# Portabilité de DataShare

## Objectif

DataShare est développé et testé principalement sous Fedora.

Afin de ne pas dépendre exclusivement de cette distribution, le projet fournit également une procédure d'installation compatible avec plusieurs distributions Linux.

## Installation sous Fedora

Le script spécifique Fedora reste disponible :

```bash
./scripts/install-fedora.sh
```

Il utilise le gestionnaire de paquets `dnf`.

## Installation Linux multi-distribution

Le script suivant détecte automatiquement le gestionnaire de paquets disponible :

```bash
./scripts/install-linux.sh
```

Il prend actuellement en charge :

- Fedora et les distributions utilisant `dnf` ;
- Debian et Ubuntu utilisant `apt`.

Les principaux prérequis installés sont :

- Java 21 ;
- Node.js ;
- npm ;
- PostgreSQL ;
- Git ;
- curl.

## Prérequis génériques

Au niveau applicatif, DataShare ne dépend pas directement de Fedora.

Les principaux composants nécessaires sont :

- Java 21 ;
- Maven Wrapper fourni avec le backend ;
- Node.js ;
- npm ;
- PostgreSQL ;
- un navigateur web moderne.

## Lancement du backend

```bash
cd backend
source ~/.config/datashare/env
./mvnw spring-boot:run
```

Les variables sensibles utilisées par le backend sont notamment :

```text
DB_PASSWORD
JWT_SECRET
```

Elles ne doivent pas être enregistrées dans le dépôt Git.

## Lancement du frontend

```bash
cd frontend
npm install
npm start
```

Le frontend de développement est accessible sur `http://localhost:4200`.

## Base de données

DataShare utilise PostgreSQL.

Le schéma est versionné avec Flyway dans :

```text
backend/src/main/resources/db/migration/
```

Hibernate utilise :

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Hibernate vérifie ainsi la cohérence du schéma sans le modifier automatiquement.

## Limites du stockage local

Dans le MVP actuel, les fichiers téléversés sont stockés sur le disque local du serveur dans le répertoire `uploads/`.

Cette solution est adaptée à une exécution mono-instance. Elle ne permet pas de déployer directement plusieurs instances indépendantes du backend, car chacune disposerait de son propre stockage local.

Pour une architecture distribuée, une évolution possible serait d'utiliser un stockage objet partagé, par exemple Amazon S3, Azure Blob Storage ou une solution compatible S3.

## Limites du rate limiting

La limitation de débit est actuellement conservée en mémoire dans l'instance du backend.

Cela convient au MVP mono-instance. Dans une architecture multi-instance, les compteurs devraient être partagés, par exemple avec Redis.

## Conteneurisation

Une évolution possible serait de fournir une installation entièrement conteneurisée avec :

- un conteneur backend Java ;
- un conteneur frontend ;
- un conteneur PostgreSQL ;
- éventuellement un stockage partagé externe.

Le projet actuel conserve une installation Linux classique tout en proposant un script multi-distribution.
