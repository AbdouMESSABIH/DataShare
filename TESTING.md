# Plan de tests - DataShare

## Objectif

L'objectif de cette stratégie de tests est de vérifier la stabilité
des fonctionnalités principales de DataShare et de détecter les
régressions avant livraison.

Les fonctionnalités critiques testées sont principalement :

- l'inscription ;
- l'authentification ;
- l'upload de fichiers ;
- le téléchargement ;
- la gestion des droits d'accès ;
- la suppression des fichiers.

La stratégie repose sur trois niveaux de tests :

1. tests unitaires ;
2. tests d'intégration ;
3. test End-to-End (E2E).

Une mesure de couverture du code backend est également réalisée avec JaCoCo.

---

## Environnement de test

### Backend

- Java 21
- Spring Boot
- Maven
- JUnit 5
- Mockito
- MockMvc
- PostgreSQL
- JaCoCo

### Frontend / E2E

- Angular
- Playwright
- Chromium

Les variables d'environnement suivantes doivent être définies avant
le lancement du backend ou des tests nécessitant le contexte Spring :

```text
DB_PASSWORD
JWT_SECRET
```

---

# 1. Tests unitaires

## Objectif

Les tests unitaires vérifient les règles métier d'une classe de manière
isolée.

Les dépendances externes sont simulées avec Mockito afin de ne pas
utiliser la base de données réelle pendant ces tests.

---

## FileServiceTest

Les règles suivantes sont testées :

| Test | Résultat attendu |
|---|---|
| Upload d'un fichier `.exe` | HTTP 415 - Unsupported Media Type |
| Upload d'un fichier vide | HTTP 400 - Bad Request |
| Expiration supérieure à 7 jours | HTTP 400 - Bad Request |
| Fichier supérieur à 1 Go | HTTP 413 - Payload Too Large |
| Token de téléchargement inconnu | HTTP 404 - Not Found |
| Token de téléchargement expiré | HTTP 410 - Gone |

---

## AuthServiceTest

Les comportements suivants sont testés :

| Test | Résultat attendu |
|---|---|
| Inscription avec un email déjà utilisé | HTTP 409 - Conflict |
| Inscription valide | Mot de passe haché avant sauvegarde |
| Connexion avec un email inconnu | HTTP 401 - Unauthorized |
| Connexion avec un mauvais mot de passe | HTTP 401 - Unauthorized |
| Connexion valide | Génération d'un JWT |

---

# 2. Tests d'intégration

## Objectif

Les tests d'intégration vérifient que plusieurs couches réelles de
l'application fonctionnent correctement ensemble.

Les principales chaînes testées sont :

```text
HTTP -> Controller -> Service -> Repository -> PostgreSQL
```

et pour les fonctionnalités liées aux fichiers :

```text
HTTP
-> Security
-> Controller
-> Service
-> Repository
-> PostgreSQL
-> stockage physique
```

---

## AuthIntegrationTest

Les scénarios suivants sont testés :

| Scénario | Vérification |
|---|---|
| Inscription valide | Utilisateur créé dans PostgreSQL et mot de passe non stocké en clair |
| Inscription avec email déjà utilisé | Retour HTTP 409 |
| Connexion avec identifiants valides | Retour HTTP 200 |
| Connexion avec mauvais mot de passe | Retour HTTP 401 |

---

## FileIntegrationTest

Les scénarios suivants sont testés :

| Scénario | Vérification |
|---|---|
| Accès à l'historique sans JWT | Accès refusé |
| Upload avec utilisateur authentifié | Métadonnées créées et fichier présent sur disque |
| Téléchargement avec token valide | Contenu réel du fichier retourné |
| Téléchargement avec token invalide | Retour HTTP 404 |
| Suppression de son propre fichier | Suppression en BDD et sur disque |
| Suppression du fichier d'un autre utilisateur | Suppression refusée et fichier conservé |

Ces tests utilisent la base PostgreSQL réelle de l'environnement de test.

Pour les opérations sur fichiers, un nettoyage est effectué afin d'éviter
de laisser des fichiers temporaires après l'exécution.

---

# 3. Test End-to-End (E2E)

## Outil utilisé

Le test End-to-End est réalisé avec :

```text
Playwright + Chromium
```

---

## Objectif

Le test E2E vérifie un parcours utilisateur complet à travers
l'ensemble de l'application :

```text
Navigateur
-> Angular
-> API Spring Boot
-> PostgreSQL
-> stockage fichier
```

---

## Parcours testé

Le scénario automatisé réalise les actions suivantes :

1. création d'un nouveau compte ;
2. connexion ;
3. vérification de l'authentification ;
4. upload d'un fichier texte ;
5. consultation de l'historique ;
6. ouverture de la page de téléchargement ;
7. téléchargement réel du fichier ;
8. suppression du fichier ;
9. vérification de sa disparition de l'historique.

---

## Résultat

Après les dernières modifications de l'interface, le scénario E2E a été
relancé afin de vérifier l'absence de régression.

Résultat :

```text
1 passed
```

Temps observé lors de cette dernière validation :

```text
1.2 s
```

---

## Exécution des tests

### Tests backend

Depuis le dossier `backend` :

```bash
cd ~/Projets/DataShare/backend

export DB_PASSWORD='votre_mot_de_passe_postgresql'
export JWT_SECRET='votre_secret_jwt'

./mvnw clean test
```

Résultat observé lors de la validation :

```text
22 tests backend réussis
```

---

### Test End-to-End

Le backend et le frontend doivent être démarrés.

Depuis le dossier `frontend` :

```bash
cd ~/Projets/DataShare/frontend
npx playwright test
```

Résultat de la dernière exécution :

```text
1 passed (1.2s)
```

Un rapport HTML Playwright peut être ouvert après l'exécution avec :

```bash
npx playwright show-report
```

Les répertoires de rapports temporaires Playwright ne sont pas destinés
à être versionnés dans Git.

---

## Couverture du code

La couverture du backend est mesurée avec JaCoCo.

Le plugin JaCoCo est intégré à la configuration Maven du backend.

Pour exécuter les tests et générer le rapport de couverture :

```bash
cd ~/Projets/DataShare/backend

export DB_PASSWORD='votre_mot_de_passe_postgresql'
export JWT_SECRET='votre_secret_jwt'

./mvnw clean test jacoco:report
```

Le rapport HTML complet est généré localement dans :

```text
backend/target/site/jacoco/index.html
```

Résultats observés lors de la validation :

```text
Couverture des instructions : 80 %
Couverture des branches : 73 %
```

Ces résultats dépassent l'objectif de couverture d'environ 70 % défini
pour le projet.

Le dossier `target` étant un répertoire de build, il n'est pas destiné
à être versionné directement dans le repository.

---

## Rapports versionnés

Une copie des rapports générés lors de la validation finale est conservée
dans le dossier `reports/` du repository.

Les résultats détaillés des tests Maven Surefire sont disponibles dans :

```text
reports/backend-tests/
```

Ce dossier contient les rapports générés par Maven Surefire pour les tests
unitaires et les tests d'intégration du backend.

Les rapports de couverture JaCoCo sont disponibles dans :

```text
reports/coverage/
```

Les fichiers versionnés sont notamment :

```text
reports/coverage/jacoco.csv
reports/coverage/jacoco.xml
```

Le rapport HTML complet reste généré localement dans :

```text
backend/target/site/jacoco/index.html
```

Le dossier `backend/target/` n'est pas versionné car il contient les
fichiers temporaires produits pendant le build.

La méthode permettant de régénérer les rapports ainsi qu'une description
de leur contenu sont documentées dans :

```text
reports/README.md
```

Cette organisation permet de conserver dans Git des preuves des résultats
de tests et de couverture tout en évitant de versionner l'ensemble des
fichiers temporaires de Maven.

---

## Anomalies détectées grâce aux tests

Les tests et les vérifications réalisés pendant le développement ont permis
de détecter plusieurs problèmes ou risques.

Par exemple :

- validation des fichiers interdits ;
- validation des fichiers trop volumineux ;
- gestion des tokens inconnus ou expirés ;
- contrôle du propriétaire lors de la suppression ;
- vérification de l'accès aux routes protégées ;
- problème potentiel avec un Content-Type invalide lors du téléchargement.

Le problème de Content-Type a été corrigé en utilisant :

```text
application/octet-stream
```

comme valeur de repli lorsqu'un type MIME invalide est rencontré.

Cette correction est également décrite dans :

```text
AI_REVIEW.md
```

---

## Validation de non-régression

Après les principales corrections et modifications du projet :

- les tests backend ont été relancés ;
- les tests d'intégration ont été relancés ;
- le scénario E2E a été relancé ;
- la couverture JaCoCo a été vérifiée ;
- les parcours critiques ont été testés.

Derniers résultats validés :

```text
22 tests backend réussis
1 test End-to-End réussi
80 % de couverture des instructions
73 % de couverture des branches
```

La dernière exécution E2E a notamment été réalisée après les améliorations
de l'interface afin de vérifier que les changements UX n'avaient pas cassé
le parcours utilisateur.

---

## Bilan

La stratégie de tests de DataShare combine :

```text
Tests unitaires
      +
Tests d'intégration
      +
Test End-to-End
      +
Couverture JaCoCo
```

Cette combinaison permet de vérifier :

- les règles métier isolées ;
- les interactions entre les différentes couches ;
- la sécurité de certains accès ;
- la persistance PostgreSQL ;
- le stockage physique ;
- le parcours utilisateur complet ;
- l'absence de régression majeure.

Les fonctionnalités critiques d'authentification, upload, téléchargement,
historique et suppression sont couvertes par les tests automatisés du projet.