# Plan de tests - DataShare

## Objectif

La stratégie de tests vérifie les fonctionnalités critiques de DataShare et limite les régressions avant livraison.

Elle couvre :

- l'inscription et l'authentification ;
- l'upload ;
- la validation des fichiers ;
- le téléchargement ;
- l'historique paginé ;
- les droits d'accès ;
- la suppression ;
- la purge des fichiers expirés ;
- le rate limiting ;
- les principaux parcours frontend.

## Outils

### Backend

- JUnit 5
- Mockito
- MockMvc
- PostgreSQL
- JaCoCo
- SpotBugs

### Frontend

- Angular TestBed / Karma
- Chrome
- ESLint
- Playwright

## 1. Tests backend

La commande de validation complète est :

```bash
cd ~/Projets/DataShare/backend
source ~/.config/datashare/env
./mvnw clean verify
```

Dernier résultat validé :

```text
32 tests
0 failure
0 error
0 skipped
BUILD SUCCESS
SpotBugs : 0 bug / 0 erreur
```

### Exemples de règles testées

`FileServiceTest` contient notamment des tests vérifiant :

- le rejet d'un fichier `.exe` ;
- le rejet d'un fichier ZIP ;
- le rejet d'un faux PDF ;
- le rejet d'un PDF déguisé en fichier TXT ;
- le rejet d'un fichier vide ;
- le rejet d'une durée supérieure à 7 jours ;
- le rejet d'un fichier dépassant 1 Go ;
- un token de téléchargement inconnu ;
- un token expiré.

D'autres tests couvrent notamment :

- l'inscription et la connexion ;
- les contrôleurs et Spring Security ;
- la pagination de l'historique ;
- la purge des fichiers expirés ;
- le rate limiting.

## 2. Tests frontend

Commande :

```bash
cd ~/Projets/DataShare/frontend
npx ng lint
npx ng test --watch=false
```

Dernier résultat validé :

```text
ESLint : succès
22 tests Angular : SUCCESS
```

Les tests couvrent notamment des composants, services et le fonctionnement de l'interceptor JWT.

## 3. Tests End-to-End

Les tests E2E utilisent Playwright.

Le backend et le frontend doivent être démarrés avant l'exécution :

```bash
cd ~/Projets/DataShare/frontend
npx playwright test
```

Dernier résultat validé :

```text
3 passed
```

Les trois scénarios vérifient :

1. le parcours complet : inscription, connexion, upload, historique, téléchargement puis suppression ;
2. l'affichage d'une erreur avec un mauvais mot de passe ;
3. le comportement avec un token de téléchargement invalide.

## 4. Couverture JaCoCo

JaCoCo génère un rapport dans :

```text
backend/target/site/jacoco/index.html
```

Le rapport doit être régénéré après les modifications du backend.

Les valeurs de couverture peuvent évoluer lorsque de nouvelles classes ou de nouveaux tests sont ajoutés ; le rapport généré par la dernière exécution fait foi.

## 5. Non-régression finale

La validation finale effectuée après les corrections du mentor est :

```text
Backend : 32 tests réussis
SpotBugs : 0 bug / 0 erreur
Frontend : 22 tests Angular réussis
ESLint : succès
Playwright : 3/3 scénarios réussis
Flyway : schéma pris en charge et table flyway_schema_history présente
```

Cette combinaison vérifie les règles métier, l'intégration avec PostgreSQL, la sécurité, les principaux composants frontend et les parcours utilisateur critiques.
