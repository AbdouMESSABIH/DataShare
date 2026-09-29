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
- téléchargement public via token et mot de passe ;
- validation du contenu réel des fichiers ;
- purge automatique des fichiers expirés ;
- rate limiting ;
- logs structurés ;
- tests automatisés ;
- analyse de couverture ;
- tests de charge ;
- analyse de performance frontend.

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
Choisir un mot de passe
        ↓
Choisir la durée d'expiration
        ↓
Téléverser
        ↓
Récupérer le lien de partage
        ↓
Ouvrir le lien
        ↓
Saisir le mot de passe
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

Lors de l'upload, le frontend envoie :

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
Tests run: 41
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
27 SUCCESS
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
Instructions : 89,01 % (1515 / 1702)
Branches     : 67,19 % (86 / 128)
Lignes       : 90,56 % (547 / 604)
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

Script :

```text
performance/download-test.js
```

Scénario final :

```text
10 utilisateurs virtuels
20 secondes
fichier protégé par mot de passe
```

Variables utilisées :

```text
DOWNLOAD_TOKEN
DOWNLOAD_PASSWORD
BASE_URL
```

Commande :

```bash
DOWNLOAD_TOKEN='<token>' \
DOWNLOAD_PASSWORD='<mot-de-passe>' \
BASE_URL='http://localhost:8080' \
k6 run performance/download-test.js
```

Dernier résultat :

```text
2 569 requêtes
128,05 requêtes/s
0 % d'erreur
5 138 / 5 138 checks réussis
p95 : 91,47 ms
temps maximum : 207,4 ms
```

Seuils :

```text
http_req_failed < 1 %
p95 < 1000 ms
```

Les deux seuils sont respectés dans l'environnement local.

Les résultats ne représentent pas la capacité maximale d'une infrastructure de production.

Documentation :

```text
PERF.md
```

---

## 22. Lighthouse

Mesure finale sur le build Angular :

```text
Performance : 90/100
FCP         : 2,7 s
LCP         : 3,0 s
TBT         : 10 ms
CLS         : 0
Speed Index : 2,7 s
```

Lighthouse mesure principalement les performances de rendu côté navigateur, alors que k6 mesure le comportement du serveur sous charge.

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
