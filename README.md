# DataShare

DataShare est une application web de partage sécurisé de fichiers réalisée dans le cadre de la formation **Expert DevOps - OpenClassrooms**.

L'application permet à un utilisateur de :

- créer un compte ;
- s'authentifier avec un JWT ;
- téléverser un fichier ;
- protéger le téléchargement avec un mot de passe ;
- définir une durée d'expiration ;
- générer un lien de partage ;
- consulter l'historique de ses fichiers ;
- télécharger un fichier partagé ;
- supprimer ses propres fichiers.

---

## 1. Fonctionnalités principales

DataShare propose :

- inscription utilisateur ;
- authentification JWT ;
- téléversement de fichiers ;
- protection des fichiers par mot de passe ;
- hash BCrypt des mots de passe ;
- génération d'un token de téléchargement ;
- liens temporaires de 1 à 7 jours ;
- historique paginé ;
- suppression par le propriétaire ;
- téléchargement public via token, avec mot de passe si le fichier est protégé ;
- validation du contenu réel des fichiers ;
- purge automatique des fichiers expirés ;
- rate limiting ;
- logs structurés ;
- tests automatisés ;
- analyse de couverture ;
- tests de charge ;
- analyse de performance frontend ;
- page d'accueil publique accessible sans authentification ;
- navigation responsive adaptée aux écrans mobiles ;
- déconnexion utilisateur ;
- affichage de l'état d'expiration des fichiers ;
- filtrage de l'historique entre fichiers actifs et expirés.

Contraintes principales :

```text
Taille maximale : 1 Go

Formats autorisés :
TXT
PDF
PNG
JPG
JPEG

Expiration :
1 à 7 jours
```

Le contenu réel du fichier est vérifié afin de détecter les fichiers dont l'extension ne correspond pas au contenu.

---

## 2. Architecture

Architecture générale :

```text
┌─────────────────────┐
│     UTILISATEUR     │
│  Navigateur Web     │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│      FRONT-END      │
│       Angular       │
└──────────┬──────────┘
           │
           │ API REST
           │ JSON / HTTP(S)
           │ JWT
           ▼
┌─────────────────────┐
│      BACK-END       │
│    Spring Boot      │
└──────────┬──────────┘
           │
           ├──────────────────────┐
           │                      │
           ▼                      ▼
┌─────────────────────┐  ┌─────────────────────┐
│     PostgreSQL      │  │   Stockage local    │
│ Métadonnées         │  │      uploads/       │
│ Utilisateurs        │  │ Fichiers physiques  │
└─────────────────────┘  └─────────────────────┘
```

Architecture backend :

```text
Controller
    │
    ▼
Service
    │
    ▼
Repository
    │
    ▼
PostgreSQL / stockage local
```

Responsabilités :

- **Controller** : requêtes et réponses HTTP ;
- **Service** : logique métier ;
- **Repository** : accès aux données ;
- **PostgreSQL** : utilisateurs et métadonnées ;
- **stockage local** : fichiers physiques.

---

## 3. Technologies

### Backend

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- PostgreSQL
- Flyway
- JWT
- BCrypt
- Maven
- JUnit 5
- Mockito
- MockMvc
- JaCoCo
- SpotBugs

### Frontend

- Angular
- TypeScript
- HTML
- SCSS
- Angular TestBed
- Karma
- ESLint
- Playwright

### Performance et qualité

- k6
- Lighthouse
- JaCoCo
- SpotBugs
- ESLint
- npm audit

---

## 4. Prérequis

Sous Fedora :

```bash
sudo dnf install git java-21-openjdk nodejs npm postgresql postgresql-server
```

Le projet fournit également :

```text
scripts/install-fedora.sh
scripts/install-linux.sh
```

Vérification :

```bash
java --version
node --version
npm --version
psql --version
git --version
```

---

## 5. Installation

Cloner le dépôt :

```bash
git clone https://github.com/AbdouMESSABIH/DataShare.git
cd DataShare
```

Installer les dépendances frontend :

```bash
cd frontend
npm install
```

Le backend utilise le Maven Wrapper fourni avec le projet.

---

## 6. PostgreSQL sous Fedora

Initialiser PostgreSQL si nécessaire :

```bash
sudo postgresql-setup --initdb --unit postgresql
```

Activer et démarrer le service :

```bash
sudo systemctl enable --now postgresql
```

Vérifier :

```bash
systemctl status postgresql
```

Ouvrir PostgreSQL :

```bash
sudo -u postgres psql
```

Créer l'utilisateur et la base :

```sql
CREATE USER datashare WITH PASSWORD 'votre_mot_de_passe';
CREATE DATABASE datashare OWNER datashare;
```

Quitter :

```text
\q
```

---

## 7. Variables d'environnement

Le backend utilise notamment :

```text
DB_PASSWORD
JWT_SECRET
```

Exemple :

```bash
export DB_PASSWORD='votre_mot_de_passe_postgresql'
export JWT_SECRET='votre_secret_jwt'
```

Sur la machine de développement, elles peuvent être placées dans :

```text
~/.config/datashare/env
```

puis chargées avec :

```bash
source ~/.config/datashare/env
```

Les secrets réels ne doivent jamais être versionnés dans Git.

---

## 8. Base de données et Flyway

Configuration principale :

```text
backend/src/main/resources/application.properties
```

Le schéma PostgreSQL est versionné avec Flyway :

```text
backend/src/main/resources/db/migration/
```

Hibernate vérifie la cohérence du schéma :

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Flyway applique les migrations nécessaires au démarrage.

---

## 9. Lancer le backend

```bash
cd ~/Projets/DataShare/backend
source ~/.config/datashare/env
./mvnw spring-boot:run
```

Backend :

```text
http://localhost:8080
```

---

## 10. Lancer le frontend

Dans un autre terminal :

```bash
cd ~/Projets/DataShare/frontend
npm start
```

Frontend :

```text
http://localhost:4200
```

---

## 11. Utilisation

Parcours principal :

```text
Créer un compte
        ↓
Se connecter
        ↓
Sélectionner un fichier
        ↓
Choisir éventuellement un mot de passe
        ↓
Choisir la durée d'expiration
        ↓
Téléverser
        ↓
Récupérer le lien de partage
        ↓
Ouvrir le lien
        ↓
Saisir le mot de passe si le fichier est protégé
        ↓
Télécharger
```

L'utilisateur connecté peut également :

```text
consulter son historique
supprimer ses fichiers
```

---

## 12. Protection par mot de passe

Lorsqu'une protection est choisie, le frontend peut envoyer le champ :

```text
password
```

Le backend ne stocke pas ce mot de passe en clair.

Il utilise BCrypt :

```text
mot de passe
    ↓
BCrypt
    ↓
hash
    ↓
PostgreSQL
```

Lors du téléchargement, le mot de passe est transmis dans :

```text
X-Download-Password
```

Un mot de passe incorrect ou absent pour un fichier protégé retourne :

```text
403 Forbidden
```

---

## 13. API REST

La documentation complète de l'API se trouve dans :

```text
API.md
```

Endpoints principaux :

```text
POST   /api/auth/register
POST   /api/auth/login

POST   /api/files/upload
GET    /api/files
DELETE /api/files/{id}

GET    /api/download/{token}
GET    /api/download/{token}/file
```

---

## 14. Sécurité

Le projet utilise notamment :

- Spring Security ;
- JWT ;
- BCrypt pour les mots de passe utilisateurs ;
- BCrypt pour les mots de passe fichiers ;
- contrôle du propriétaire ;
- expiration des liens ;
- validation des fichiers ;
- limite de taille de 1 Go ;
- whitelist de formats ;
- vérification du contenu réel ;
- rate limiting ;
- variables d'environnement ;
- contrôle ESLint ;
- contrôle SpotBugs.

La documentation détaillée est disponible dans :

```text
SECURITY.md
```

---

## 15. Tests backend

Commande :

```bash
cd ~/Projets/DataShare/backend
source ~/.config/datashare/env
./mvnw clean test
```

Dernier résultat validé :

```text
Tests run: 49
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

---

## 16. Tests frontend

Lint :

```bash
cd ~/Projets/DataShare/frontend
npx ng lint
```

Résultat :

```text
All files pass linting.
```

Tests Angular :

```bash
npx ng test --watch=false
```

Dernier résultat :

```text
40 SUCCESS (1er octobre 2026)
```

---

## 17. Tests End-to-End

Playwright :

```bash
cd ~/Projets/DataShare/frontend
npx playwright test
```

Dernier résultat :

```text
3 passed
```

Le scénario principal couvre :

```text
Inscription
→ Connexion
→ Upload protégé par mot de passe
→ Historique
→ Mauvais mot de passe
→ Bon mot de passe
→ Téléchargement
→ Suppression
```

---

## 18. Couverture JaCoCo

Génération :

```bash
cd ~/Projets/DataShare/backend
source ~/.config/datashare/env
./mvnw clean test jacoco:report
```

Rapport :

```text
backend/target/site/jacoco/index.html
```

Dernière mesure :

```text
Instructions : 89,78 % (1678 / 1869)
Branches     : 70,48 % (117 / 166)
Lignes       : 91,15 % (577 / 633)
```

La couverture mesure le code exécuté pendant les tests mais ne garantit pas à elle seule l'absence de bugs.

---

## 19. SpotBugs

Commande :

```bash
cd ~/Projets/DataShare/backend
./mvnw spotbugs:check
```

Dernier résultat :

```text
BugInstance size is 0
Error size is 0
No errors/warnings found
BUILD SUCCESS
```

---

## 20. Build Angular

Commande :

```bash
cd ~/Projets/DataShare/frontend
npm run build
```

Le build de production est généré dans :

```text
frontend/dist/frontend/
```

---

## 21. Performance backend avec k6

Script : `performance/download-test.js`.

### Scénario actualisé du 1er octobre 2026

- 10 utilisateurs virtuels ;
- 20 itérations partagées ;
- téléchargement d'un fichier protégé par mot de passe ;
- utilisation d'un token de téléchargement valide.

Variables utilisées : `DOWNLOAD_TOKEN`, `DOWNLOAD_PASSWORD` et `BASE_URL`.

Commande de reproduction :

```bash
DOWNLOAD_TOKEN='<token>' \
DOWNLOAD_PASSWORD='<mot-de-passe>' \
BASE_URL='http://localhost:8080' \
k6 run performance/download-test.js
```

### Résultats du scénario actualisé

| Indicateur | Résultat |
|---|---:|
| Téléchargements HTTP 200 | 20/20 |
| Vérifications k6 | 40/40 |
| Échecs | 0 |
| Temps de réponse p95 | 475,39 ms |

Seuils définis :

- `http_req_failed < 1 %` ;
- `p95 < 1000 ms`.

Les deux seuils sont respectés dans l'environnement local.

Le rate limiting a également été vérifié : 30 téléchargements
autorisés, puis une réponse HTTP 429 à la 31e tentative, avec
un même token valide dans la fenêtre de limitation.

### Ancienne campagne (historique)

| Indicateur | Ancien résultat |
|---|---:|
| Requêtes | 2 569 |
| Débit | 128,05 requêtes/s |
| Taux d'erreur | 0 % |
| Vérifications réussies | 5 138 / 5 138 |
| p95 | 91,47 ms |
| Temps maximum | 207,4 ms |

Cette ancienne campagne précède les dernières modifications
de sécurité, notamment le coût BCrypt et les limitations
de téléchargement. Les scénarios ne sont donc pas
directement comparables.

Ces résultats locaux ne constituent pas une mesure de la
capacité maximale d'une infrastructure de production.

Documentation détaillée : `PERF.md`.

---

## 22. Lighthouse

Nouvelle campagne du 1er octobre 2026 sur le build Angular
de production, servi localement sur `http://localhost:4173/`.

Outil : Lighthouse 13.4.0, mode Navigation.

### Scores

| Catégorie | Mobile | Desktop |
|---|---:|---:|
| Performance | 91/100 | 100/100 |
| Accessibilité | 100/100 | 100/100 |
| Bonnes pratiques | 100/100 | 100/100 |
| SEO | 100/100 | 100/100 |

### Métriques de performance

| Métrique | Mobile | Desktop |
|---|---:|---:|
| FCP | 2,7 s | 0,5 s |
| LCP | 2,9 s | 0,6 s |
| TBT (valeur numérique JSON) | 14 ms | 0 ms |
| CLS | 0 | 0 |
| Speed Index | 2,7 s | 0,5 s |

La correction de la balise `meta description` a permis de
valider l'audit SEO sur les deux profils.

Les précédentes mesures Lighthouse sont conservées dans
`PERF.md` à titre historique.

Lighthouse mesure notamment le rendu dans le navigateur,
tandis que k6 mesure les réponses du serveur sous charge.

Ces résultats correspondent à des mesures locales ponctuelles.

Documentation détaillée : `PERF.md`.

---

## 23. Logs

Le backend utilise des logs structurés.

Fichier :

```text
backend/logs/datashare.log
```

Événements principaux :

```text
file_upload
file_download
file_delete
```

Les logs ne doivent pas contenir :

```text
mots de passe
JWT
tokens de téléchargement
```

---

## 24. Maintenance

Documentation :

```text
MAINTENANCE.md
```

Elle couvre notamment :

- diagnostic ;
- logs ;
- tests de non-régression ;
- dépendances ;
- PostgreSQL ;
- stockage ;
- sécurité ;
- performance ;
- migrations Flyway ;
- Git.

---

## 25. Intelligence artificielle

L'intelligence artificielle a été utilisée comme outil d'assistance au développement et à l'apprentissage.

Documentation :

```text
AI_USAGE.md
AI_REVIEW.md
```

Une User Story de téléchargement a été spécifiquement tracée.

Le code produit avec assistance IA a été :

```text
relu
compris
testé
corrigé
tracé dans Git
```

La revue humaine a notamment permis de traiter un cas de Content-Type invalide avec un fallback :

```text
application/octet-stream
```

---

## 26. Documents

```text
README.md
API.md
PERF.md
TESTING.md
SECURITY.md
MAINTENANCE.md
AI_USAGE.md
AI_REVIEW.md
PERSONAL_DATA.md
PORTABILITY.md
```

---

## 27. Structure simplifiée

```text
DataShare/
│
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── mvnw
│
├── frontend/
│   ├── src/
│   ├── e2e/
│   ├── package.json
│   └── playwright.config.ts
│
├── performance/
│   └── download-test.js
│
├── API.md
├── PERF.md
├── TESTING.md
├── SECURITY.md
├── MAINTENANCE.md
├── AI_USAGE.md
├── AI_REVIEW.md
├── PERSONAL_DATA.md
├── PORTABILITY.md
└── README.md
```

---

## 28. Repository

```text
https://github.com/AbdouMESSABIH/DataShare
```

---

## 29. Contexte

Projet réalisé dans le cadre de la formation :

**Expert DevOps - OpenClassrooms**
