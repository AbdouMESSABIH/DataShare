# Rapports de tests et de couverture - DataShare

Ce dossier regroupe les rapports générés lors des validations du backend DataShare.

Les valeurs présentées correspondent à la dernière validation finale documentée du projet.

## Rapports de tests backend

Les résultats Maven Surefire concernent les tests unitaires et les tests d'intégration du backend.

Dernière validation :

```text
Tests run: 58
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
Instructions : 91,64 % (1930 / 2106)
Branches     : 72,31 % (175 / 242)
Lignes       : 91,89 % (612 / 666)
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
Backend JUnit    : 58 / 58
Frontend Angular : 44 / 44
Playwright       : 3 / 3
ESLint           : succès
SpotBugs         : 0 bug / 0 erreur
```

## Confidentialité des rapports JUnit archivés

Les preuves JUnit ont été actualisées le 3 octobre 2026,
après l'exécution réussie de `./mvnw clean verify`.

Les fichiers du dossier `reports/backend-tests/`
sont des exports simplifiés des rapports Maven Surefire.

Ils conservent uniquement :

- les noms des suites et des méthodes de test ;
- les durées d'exécution ;
- les compteurs de tests, échecs, erreurs et tests ignorés ;
- le type de résultat, lorsqu'il est applicable.

Les propriétés système, les sorties console, les traces
d'exception et les autres informations d'environnement
ne sont pas recopiées dans ces exports.

Bilan de cette validation :

| Indicateur | Résultat |
|---|---:|
| Suites JUnit | 11 |
| Tests exécutés | 58 |
| Échecs | 0 |
| Erreurs | 0 |
| Tests ignorés | 0 |

Les rapports complets originaux sont générés localement
dans `backend/target/surefire-reports/`.

Les exports JaCoCo (`jacoco.csv` et `jacoco.xml`)
ont également été actualisés à partir de la validation
du 3 octobre 2026.

Les rapports archivés constituent des preuves ponctuelles
et non un remplacement de l'exécution effective des tests.

---

## Important

Les fichiers de rapport générés automatiquement peuvent être recréés.

La source de vérité pour les résultats finaux et leur interprétation reste la documentation versionnée du projet.


---

## Captures des rapports de validation

Les validations backend, JaCoCo et Angular correspondent aux
résultats du 3 octobre 2026. Le scan OWASP a été effectué
le 2 octobre 2026. La comparaison Figma documente les
écrans réalisés par rapport aux maquettes.

| Preuve | Fichier | Résultat |
|---|---|---|
| Validation backend Maven | `evidence/backend-validation-log.png` | 58 tests réussis, seuil JaCoCo respecté, SpotBugs : 0 bug |
| Couverture backend JaCoCo | `evidence/jacoco-coverage.png` | 72,31 % des branches (175/242) |
| Audit OWASP Dependency-Check | `evidence/owasp-after-fix.png` | 0 vulnérabilité connue détectée lors du scan |
| Build Angular | `evidence/frontend-bundle-metrics.png` | 380,46 kB, transfert estimé : 96,89 kB |
| Comparaison Figma | `evidence/figma/DataShare_comparaison.png` | Comparaison visuelle entre les maquettes et la réalisation |

La capture OWASP concerne les dépendances du backend
analysées au moment du scan. Elle ne constitue pas
une garantie d'absence de toute vulnérabilité.
