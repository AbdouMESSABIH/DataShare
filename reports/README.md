# Rapports de tests et de couverture - DataShare

Ce dossier regroupe les rapports générés lors des validations du backend DataShare.

Les valeurs présentées correspondent à la dernière validation finale documentée du projet.

## Rapports de tests backend

Les résultats Maven Surefire concernent les tests unitaires et les tests d'intégration du backend.

Dernière validation :

```text
Tests run: 49
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

Commande depuis la racine du dépôt :

```bash
cd backend
source ~/.config/datashare/env
./mvnw clean test
```

## Couverture JaCoCo

Commande depuis la racine du dépôt :

```bash
cd backend
source ~/.config/datashare/env
./mvnw clean test jacoco:report
```

Résultats finaux :

```text
Instructions : 89,78 % (1678 / 1869)
Branches     : 70,48 % (117 / 166)
Lignes       : 91,15 % (577 / 633)
```

Le rapport HTML complet est généré localement dans :

```text
backend/target/site/jacoco/index.html
```

Selon les rapports conservés dans ce dossier, des exports peuvent également être présents sous forme :

```text
jacoco.csv
jacoco.xml
```

## Analyse SpotBugs

Dernière validation :

```text
BugInstance size is 0
Error size is 0
No errors/warnings found
BUILD SUCCESS
```

Commande depuis la racine du dépôt :

```bash
cd backend
./mvnw spotbugs:check
```

## Autres validations du projet

Les autres résultats sont documentés dans les fichiers principaux du dépôt :

```text
TESTING.md
PERF.md
SECURITY.md
```

État global documenté :

```text
Backend JUnit    : 49 / 49
Frontend Angular : 40 / 40
Playwright       : 3 / 3
ESLint           : succès
SpotBugs         : 0 bug / 0 erreur
```

## Important

Les fichiers de rapport générés automatiquement peuvent être recréés.

La source de vérité pour les résultats finaux et leur interprétation reste la documentation versionnée du projet.
