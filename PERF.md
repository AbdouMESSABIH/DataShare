# DataShare

DataShare est une application web de partage sécurisé de fichiers.

Elle permet à un utilisateur de créer un compte, de s’authentifier,
de téléverser des fichiers, de générer un lien de téléchargement temporaire,
de consulter son historique et de supprimer ses fichiers.

Le projet a été réalisé dans le cadre de la formation
**Expert DevOps - OpenClassrooms**.

---

## 1. Fonctionnalités principales

DataShare permet notamment :

- la création d’un compte utilisateur ;
- l’authentification avec JWT ;
- le téléversement de fichiers ;
- la génération d’un token de téléchargement ;
- la définition d’une durée d’expiration comprise entre 1 et 7 jours ;
- le téléchargement d’un fichier à partir d’un token ;
- la consultation de l’historique des fichiers ;
- la suppression d’un fichier par son propriétaire ;
- la gestion des erreurs ;
- la journalisation structurée des opérations principales.

Contraintes principales :

- taille maximale d’un fichier : **1 Go** ;
- les fichiers `.exe` et `.bat` sont refusés ;
- les liens expirés ne permettent plus le téléchargement.

---

## 2. Architecture

L’application est organisée autour de deux parties principales :

```text
Utilisateur
    |
    v
Frontend Angular
    |
    | API REST / JSON
    v
Backend Spring Boot
    |
    +--> PostgreSQL
    |
    +--> Stockage local des fichiers
```

Le backend utilise une architecture en couches :

```text
Controller
    |
    v
Service
    |
    v
Repository
    |
    v
PostgreSQL
```

Responsabilités principales :

- **Controller** : reçoit les requêtes HTTP et retourne les réponses ;
- **Service** : contient la logique métier ;
- **Repository** : gère l’accès aux données ;
- **PostgreSQL** : stocke les utilisateurs et les métadonnées ;
- **stockage local** : contient les fichiers physiques.

---

## 3. Technologies utilisées

### Backend

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT
- Maven
- JUnit
- MockMvc
- JaCoCo

### Frontend

- Angular
- TypeScript
- HTML
- SCSS
- Playwright

### Qualité, sécurité et performance

- JaCoCo
- Playwright
- npm audit
- k6
- Lighthouse
- logs structurés JSON

---

## 4. Prérequis

Les outils suivants sont nécessaires :

- Git
- Java 21
- Node.js
- npm
- PostgreSQL

Sous Fedora :

```bash
sudo dnf install git java-21-openjdk-devel nodejs npm postgresql-server
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

## 5. Installation du projet

Cloner le repository :

```bash
git clone https://github.com/AbdouMESSABIH/DataShare.git
cd DataShare
```

---

## 6. Configuration de PostgreSQL

Initialiser PostgreSQL si nécessaire :

```bash
sudo postgresql-setup --initdb --unit postgresql
```

Démarrer PostgreSQL :

```bash
sudo systemctl enable --now postgresql
```

Vérifier son état :

```bash
systemctl status postgresql
```

Ouvrir PostgreSQL :

```bash
sudo -u postgres psql
```

Exemple de création de l’utilisateur et de la base :

```sql
CREATE USER datashare WITH PASSWORD 'votre_mot_de_passe';
CREATE DATABASE datashare OWNER datashare;
```

Quitter PostgreSQL :

```text
\q
```

La base utilisée par l’application est :

```text
datashare
```

L’utilisateur PostgreSQL utilisé est :

```text
datashare
```

Le mot de passe ne doit jamais être enregistré directement dans Git.

---

## 7. Variables d’environnement

Le backend utilise des variables d’environnement pour les données sensibles.

Dans le terminal qui servira à lancer le backend :

```bash
export DB_PASSWORD='votre_mot_de_passe_postgresql'
export JWT_SECRET='votre_secret_jwt'
```

Ces variables doivent être redéfinies dans chaque nouveau terminal
avant de démarrer le backend.

Ne jamais enregistrer les valeurs réelles dans le repository Git.

---

## 8. Configuration du backend

La configuration principale se trouve dans :

```text
backend/src/main/resources/application.properties
```

Elle utilise notamment :

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/datashare
spring.datasource.username=datashare
spring.datasource.password=${DB_PASSWORD}

jwt.secret=${JWT_SECRET}
```

---

## 9. Lancer le backend

Depuis la racine du projet :

```bash
cd backend
./mvnw spring-boot:run
```

Le backend est accessible sur :

```text
http://localhost:8080
```

---

## 10. Lancer le frontend

Dans un second terminal :

```bash
cd ~/Projets/DataShare/frontend
npm install
npm start
```

Le frontend est accessible sur :

```text
http://localhost:4200
```

---

## 11. Utilisation de DataShare

Une fois PostgreSQL, le backend et le frontend démarrés :

1. ouvrir `http://localhost:4200` ;
2. créer un compte ;
3. se connecter ;
4. sélectionner un fichier ;
5. choisir sa durée d’expiration ;
6. téléverser le fichier ;
7. récupérer le lien de téléchargement ;
8. consulter l’historique ;
9. télécharger ou supprimer le fichier.

---

## 12. API REST

Le frontend communique avec le backend via une API REST utilisant JSON.

La documentation détaillée des endpoints est disponible dans :

```text
API.md
```

Les principales fonctionnalités exposées concernent :

- l’inscription ;
- la connexion ;
- l’upload ;
- l’historique ;
- le téléchargement ;
- la suppression.

---

## 13. Sécurité

Plusieurs mécanismes ont été mis en place :

- hachage des mots de passe ;
- authentification JWT ;
- contrôle des accès aux ressources protégées ;
- vérification du propriétaire avant suppression ;
- expiration des liens de téléchargement ;
- limitation de la taille des fichiers ;
- blocage des extensions `.exe` et `.bat` ;
- stockage des secrets dans des variables d’environnement ;
- analyse des dépendances frontend avec `npm audit`.

La documentation détaillée est disponible dans :

```text
SECURITY.md
```

---

## 14. Accessibilité et ergonomie

Plusieurs bonnes pratiques d’accessibilité ont été ajoutées au frontend :

- labels associés aux champs de formulaire ;
- texte alternatif sur le logo ;
- boutons HTML explicites ;
- attributs ARIA pour certains champs et messages ;
- messages d’erreur annoncés aux technologies d’assistance ;
- utilisation de `aria-invalid` et `aria-describedby` ;
- utilisation de `role="alert"` et `aria-live` ;
- autocomplétion adaptée aux champs d’authentification ;
- interface responsive.

Ces améliorations s’inscrivent dans les bonnes pratiques d’accessibilité
issues notamment des recommandations WCAG et du référentiel RGAA.

---

## 15. Tests

Le projet utilise plusieurs niveaux de tests.

### Tests unitaires

Les tests unitaires vérifient notamment les règles métier du backend :

- rejet des fichiers interdits ;
- rejet d’un fichier vide ;
- validation de la durée d’expiration ;
- limite de taille ;
- gestion des tokens invalides ;
- gestion des tokens expirés ;
- inscription ;
- authentification.

### Tests d’intégration

Les tests d’intégration vérifient les interactions entre :

- les contrôleurs ;
- les services ;
- PostgreSQL ;
- le stockage des fichiers ;
- Spring Security.

### Test End-to-End

Playwright vérifie un parcours utilisateur complet :

```text
Inscription
    ->
Connexion
    ->
Upload
    ->
Historique
    ->
Téléchargement
    ->
Suppression
```

Résultats validés pendant le développement :

```text
22 tests backend réussis
1 test End-to-End Playwright réussi
```

Pour lancer les tests backend :

```bash
cd ~/Projets/DataShare/backend

export DB_PASSWORD='votre_mot_de_passe_postgresql'
export JWT_SECRET='votre_secret_jwt'

./mvnw clean test
```

Le backend et le frontend doivent être démarrés avant l’exécution
du scénario End-to-End.

Pour lancer le test End-to-End :

```bash
cd ~/Projets/DataShare/frontend
npx playwright test
```

La stratégie de tests est détaillée dans :

```text
TESTING.md
```

---

## 16. Couverture du code

JaCoCo est utilisé pour mesurer la couverture du backend.

Commande :

```bash
cd ~/Projets/DataShare/backend
./mvnw clean test
```

Le rapport HTML est généré dans :

```text
backend/target/site/jacoco/index.html
```

Résultats obtenus lors de la validation :

```text
Couverture des instructions : 80 %
Couverture des branches : 73 %
```

---

## 17. Analyse des dépendances

Le frontend peut être analysé avec :

```bash
cd ~/Projets/DataShare/frontend
npm audit
```

Pour analyser uniquement les dépendances utilisées en production :

```bash
npm audit --omit=dev
```

Une mise à jour majeure forcée n’est pas appliquée automatiquement
lorsqu’elle risque d’introduire des incompatibilités.

Les résultats et décisions sont documentés dans :

```text
SECURITY.md
```

---

## 18. Performance

Les performances de DataShare ont été mesurées côté backend et côté frontend.

### Backend

Un test de charge a été réalisé avec k6 sur le téléchargement d’un fichier.

Le script se trouve dans :

```text
performance/download-test.js
```

Scénario utilisé :

```text
10 utilisateurs virtuels
durée : 20 secondes
```

Résultats observés :

```text
171 362 requêtes HTTP
0 % d’erreur
temps moyen : 1,08 ms
p95 : 1,37 ms
environ 8 568 requêtes par seconde
```

Ces mesures ont été réalisées dans un environnement local de développement
et ne constituent pas un benchmark de production.

### Frontend

Un build Angular de production a également été analysé.

Commande :

```bash
cd ~/Projets/DataShare/frontend
npm run build
```

Résultats du bundle initial :

```text
Taille brute : 329,72 kB
Transfert estimé : 87,54 kB
```

Une mesure Lighthouse a été réalisée sur le build de production.

Résultats :

```text
Performance : 82 / 100
FCP : 2,6 s
LCP : 4,2 s
TBT : 0 ms
CLS : 0
Speed Index : 2,6 s
```

Le LCP constitue le principal axe d’amélioration identifié.

Les résultats détaillés, les budgets de performance et les limites
des mesures sont documentés dans :

```text
PERF.md
```

---

## 19. Logs structurés

Le backend produit des logs structurés au format JSON.

Le fichier de logs est :

```text
backend/logs/datashare.log
```

Les principaux événements métier journalisés sont :

```text
file_upload
file_download
file_delete
```

Les logs permettent notamment de suivre :

- le type d’opération ;
- l’identifiant du fichier ;
- la taille du fichier ;
- certaines informations utiles au diagnostic.

Les mots de passe, JWT et tokens de téléchargement ne doivent pas être
écrits dans les logs.

---

## 20. Maintenance

Les procédures de maintenance sont décrites dans :

```text
MAINTENANCE.md
```

Elles couvrent notamment :

- le diagnostic d’incidents ;
- l’analyse des logs ;
- la correction d’un bug ;
- les tests de non-régression ;
- les mises à jour des dépendances ;
- les contrôles de sécurité ;
- les tests de performance ;
- la maintenance de PostgreSQL ;
- le stockage local des fichiers ;
- le processus Git.

---

## 21. Utilisation de l’intelligence artificielle

L’intelligence artificielle a été utilisée comme outil d’assistance
au développement et à l’apprentissage.

Son utilisation est documentée dans :

```text
AI_USAGE.md
```

Une revue technique spécifique du code développé avec l’assistance
de l’IA est disponible dans :

```text
AI_REVIEW.md
```

Le code proposé avec l’aide de l’IA n’a pas été accepté automatiquement.

Il a été :

- relu ;
- testé ;
- comparé aux besoins fonctionnels ;
- corrigé lorsque nécessaire.

Une anomalie concernant la gestion d’un type MIME invalide pendant
le téléchargement a notamment été détectée lors de cette revue puis corrigée.

---

## 22. Documentation du projet

Les principaux documents du repository sont :

```text
README.md
API.md
AI_USAGE.md
AI_REVIEW.md
TESTING.md
SECURITY.md
PERF.md
MAINTENANCE.md
```

Ils couvrent notamment :

- l’installation ;
- l’utilisation ;
- l’architecture ;
- l’API ;
- les tests ;
- la sécurité ;
- les performances ;
- la maintenance ;
- l’utilisation de l’IA.

---

## 23. Structure simplifiée du repository

```text
DataShare/
|
|-- backend/
|   |-- src/
|   |-- pom.xml
|   `-- mvnw
|
|-- frontend/
|   |-- src/
|   |-- e2e/
|   |-- public/
|   |-- package.json
|   `-- playwright.config.ts
|
|-- performance/
|   `-- download-test.js
|
|-- API.md
|-- AI_USAGE.md
|-- AI_REVIEW.md
|-- TESTING.md
|-- SECURITY.md
|-- PERF.md
|-- MAINTENANCE.md
`-- README.md
```

---

## 24. Repository GitHub

Le code source du projet est disponible sur :

```text
https://github.com/AbdouMESSABIH/DataShare
```

---

## 25. Auteur

Projet réalisé dans le cadre de la formation :

**Expert DevOps - OpenClassrooms**