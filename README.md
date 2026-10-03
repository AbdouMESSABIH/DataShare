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
MP3
MP4
ZIP

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
export DB_PASSWORD='votre_mot_de_passe'
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

La valeur de `DB_PASSWORD` doit être identique au mot de passe
attribué à l'utilisateur PostgreSQL `datashare`.

Pour `JWT_SECRET`, utiliser un secret aléatoire robuste,
par exemple généré avec `openssl rand -hex 32`.

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

La migration V3 ajoute notamment deux index PostgreSQL :

- `files.owner_id` : utilisé pour rechercher les fichiers
  appartenant à un utilisateur ;
- `files.expires_at` : utilisé pour rechercher les fichiers
  expirés lors du nettoyage automatique.

Ces index accompagnent les requêtes d'historique et de purge.

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

<!-- Synthèse README : 3 octobre 2026 -->

## 12. Protection par mot de passe

Le mot de passe de partage est facultatif. Lorsqu'il est
renseigné, il doit contenir au minimum six caractères.

Le backend conserve uniquement son hash BCrypt.
Lors d'un téléchargement protégé, le mot de passe
est transmis dans l'en-tête `X-Download-Password`.

Un fichier non protégé ne nécessite aucun mot de passe.

Détails : [API.md](API.md) et [SECURITY.md](SECURITY.md).

---

## 13. API REST

Principaux endpoints :

| Méthode | Endpoint | Fonction |
|---|---|---|
| POST | `/api/auth/register` | Inscription |
| POST | `/api/auth/login` | Connexion |
| POST | `/api/files/upload` | Téléversement authentifié |
| GET | `/api/files` | Historique paginé |
| DELETE | `/api/files/{id}` | Suppression |
| GET | `/api/download/{token}` | Informations publiques |
| GET | `/api/download/{token}/file` | Téléchargement |

Le contrat complet, les champs, les exemples JSON
et les codes HTTP sont documentés dans [API.md](API.md).

---

## 14. Sécurité

DataShare applique notamment :

- JWT pour les routes privées ;
- contrôle du propriétaire lors de la suppression ;
- BCrypt pour les mots de passe ;
- expiration des liens entre 1 et 7 jours ;
- vérification des extensions et signatures des fichiers ;
- limitation de débit par adresse IP et par jeton ;
- secrets conservés hors du dépôt Git.

Formats autorisés : TXT, PDF, PNG, JPG, JPEG, MP3, MP4 et ZIP.

Le dernier scan OWASP backend documenté n'a détecté
aucune vulnérabilité connue dans les dépendances analysées.

L'audit npm des dépendances de production a relevé
sept alertes, documentées avec la décision de traitement.

Voir [SECURITY.md](SECURITY.md) et [reports/README.md](reports/README.md).

---

## 15. Tests backend

Dernière validation du 3 octobre 2026 :

- 58 tests réussis ;
- aucune erreur ni aucun échec ;
- Maven `BUILD SUCCESS`.

Commande de validation complète :

```bash
cd "$(git rev-parse --show-toplevel)/backend"
source ~/.config/datashare/env
./mvnw clean verify
```

Détails et critères d'acceptation : [TESTING.md](TESTING.md).

---

## 16. Tests frontend

Dernière validation : **44 tests Angular réussis**.

Commande depuis `frontend/` :

```bash
npx ng test --watch=false
```

Le plan de tests et les résultats sont détaillés
dans [TESTING.md](TESTING.md).

---

## 17. Tests End-to-End

La dernière campagne Playwright documentée
comprend trois scénarios réussis.

Consulter [TESTING.md](TESTING.md) pour leur description
et leur contexte d'exécution.

---

## 18. Couverture JaCoCo

Dernière mesure du 3 octobre 2026 :

| Indicateur | Couverture |
|---|---:|
| Instructions | 91,64 % |
| Branches | **72,31 % (175/242)** |
| Lignes | 91,89 % |

Le seuil minimal de 70 % des branches est contrôlé
automatiquement pendant la phase Maven `verify`.

Rapport et capture : [reports/README.md](reports/README.md).

---

## 19. SpotBugs

Dernière validation backend :

- 0 bug ;
- 0 erreur.

Le contrôle fait partie de la validation Maven.

Voir [TESTING.md](TESTING.md).

---

## 20. Build Angular

Dernière compilation de production du 3 octobre 2026 :

| Mesure | Résultat |
|---|---:|
| Bundle initial brut | 380,46 kB |
| Transfert estimé | 96,89 kB |
| Erreur de compilation | 0 |

Le budget de performance et l'évolution du bundle
sont détaillés dans [PERF.md](PERF.md).

---

## 21. Performance backend avec k6

Les campagnes k6 mesurent notamment la latence,
le débit et le taux d'erreur du backend sous charge.

Le coût de BCrypt sur les téléchargements protégés,
les différences entre scénarios et leurs limites
d'interprétation sont analysés dans [PERF.md](PERF.md).

---

## 22. Lighthouse

Les mesures Lighthouse portent sur les performances
du navigateur, l'accessibilité, les bonnes pratiques
et le référencement.

Les résultats historiques mobile et desktop sont
conservés dans [PERF.md](PERF.md).

Ces mesures ne doivent pas être confondues
avec les résultats des tests de charge k6.

---

## 23. Logs

Les journaux sont utiles au diagnostic des erreurs
et aux contrôles techniques, sans exposer les secrets.

Captures disponibles dans [reports/evidence/](reports/evidence/).

---

## 24. Maintenance

Le plan de maintenance précise :

- la fréquence des mises à jour ;
- les risques liés aux dépendances ;
- les sauvegardes et la restauration ;
- la purge des fichiers expirés ;
- les migrations Flyway ;
- la surveillance et les contrôles réguliers.

Consulter [MAINTENANCE.md](MAINTENANCE.md).

---

## 25. Intelligence artificielle

La démarche d'utilisation, de revue, de correction
et de supervision humaine est décrite dans :

- [AI_USAGE.md](AI_USAGE.md) ;
- [AI_REVIEW.md](AI_REVIEW.md).

La revue du téléchargement par jeton est notamment
accompagnée d'un test automatisé de non-régression.

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
FIGMA_CONFORMITE.md
reports/README.md
backend/HELP.md
frontend/README.md
docs/Documentation_technique.pdf
```

La conformité visuelle est détaillée dans
[FIGMA_CONFORMITE.md](FIGMA_CONFORMITE.md).

Les captures, rapports JUnit et exports JaCoCo sont répertoriés
dans [reports/README.md](reports/README.md).

Les guides spécifiques aux deux modules sont disponibles dans
[backend/HELP.md](backend/HELP.md) et
[frontend/README.md](frontend/README.md).

La documentation technique actualisée du projet est disponible ici :
[Documentation technique DataShare](docs/Documentation_technique.pdf).

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
