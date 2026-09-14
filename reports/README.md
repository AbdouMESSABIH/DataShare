# Rapports de tests et de couverture - DataShare

Ce dossier contient les rapports générés lors de la validation finale du backend DataShare.

## Rapports de tests

Le dossier `backend-tests/` contient les résultats générés par Maven Surefire.

Ces rapports correspondent aux tests unitaires et aux tests d'intégration du backend.

Lors de la validation finale :

- 22 tests exécutés
- 0 échec
- 0 erreur
- 0 test ignoré

## Rapports de couverture

Le dossier `coverage/` contient les rapports générés par JaCoCo :

- `jacoco.csv` : résultats de couverture au format CSV
- `jacoco.xml` : résultats de couverture au format XML

Résultats obtenus lors de la validation finale :

- couverture des instructions : 80 %
- couverture des branches : 73 %

Le rapport HTML complet est généré localement dans :

```text
backend/target/site/jacoco/index.html
