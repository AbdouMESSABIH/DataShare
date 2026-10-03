# Backend DataShare

Ce dossier contient le back-end Spring Boot de l'application DataShare.

La documentation générale du projet se trouve à la racine du dépôt dans :

```text
README.md
API.md
SECURITY.md
TESTING.md
MAINTENANCE.md
PERF.md
```

## Technologies principales

```text
Java 21
Spring Boot
Spring Security
Spring Data JPA
PostgreSQL
Flyway
JWT
BCrypt
Maven
JUnit 5
Mockito
MockMvc
JaCoCo
SpotBugs
```

## Variables d'environnement

Le backend utilise notamment :

```text
DB_PASSWORD
JWT_SECRET
```

Sur la machine de développement :

```bash
source ~/.config/datashare/env
```

Les valeurs réelles ne doivent pas être ajoutées au dépôt Git.

## Lancer le backend

Depuis le dossier `backend` :

```bash
source ~/.config/datashare/env
./mvnw spring-boot:run
```

Le serveur est accessible sur :

```text
http://localhost:8080
```

## Base de données

DataShare utilise PostgreSQL.

Le schéma est versionné avec Flyway :

```text
src/main/resources/db/migration/
```

Hibernate vérifie la cohérence du schéma avec :

```properties
spring.jpa.hibernate.ddl-auto=validate
```

## Tests

Exécuter les tests :

```bash
source ~/.config/datashare/env
./mvnw clean test
```

Dernière validation :

```text
58 tests réussis (3 octobre 2026)
0 échec
0 erreur
BUILD SUCCESS
```

## Couverture JaCoCo

```bash
source ~/.config/datashare/env
./mvnw clean test jacoco:report
```

Rapport HTML :

```text
target/site/jacoco/index.html
```

Derniers résultats :

```text
Instructions : 91,64 % (1930 / 2106)
Branches     : 72,31 % (175 / 242)
Lignes       : 91,89 % (612 / 666)
```

## SpotBugs

```bash
./mvnw spotbugs:check
```

Dernière validation :

```text
BugInstance size is 0
Error size is 0
No errors/warnings found
BUILD SUCCESS
```

## Téléchargement protégé

Les fichiers peuvent être protégés par mot de passe.

Le mot de passe est stocké sous forme de hash BCrypt.

Lors du téléchargement, le client transmet :

```text
X-Download-Password
```

Un mot de passe absent ou incorrect pour un fichier protégé retourne :

```text
403 Forbidden
```

La documentation complète de l'API est disponible dans :

```text
../API.md
```
