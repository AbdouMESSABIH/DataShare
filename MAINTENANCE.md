# Procédures de maintenance - DataShare

## 1. Objectif

Ce document décrit les principales procédures de maintenance de DataShare.

Les objectifs sont de pouvoir :

- vérifier l'état de l'application ;
- diagnostiquer un incident ;
- consulter les logs ;
- corriger un défaut ;
- vérifier la non-régression ;
- contrôler la sécurité ;
- suivre les performances ;
- maintenir les dépendances ;
- maintenir PostgreSQL et le stockage local ;
- contrôler les migrations Flyway.

---

## 2. Architecture à garder en tête

```text
Utilisateur
    │
    ▼
Frontend Angular
    │
    │ API REST / JSON
    ▼
Backend Spring Boot
    │
    ├── PostgreSQL
    │
    └── stockage local
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

Lors d'un incident, cette séparation permet d'identifier plus facilement la couche concernée.

---

## 3. Vérification de PostgreSQL

Vérifier l'état du service :

```bash
systemctl status postgresql
```

Démarrer PostgreSQL si nécessaire :

```bash
sudo systemctl start postgresql
```

La base utilisée par DataShare est :

```text
datashare
```

Vérifier l'accès :

```bash
sudo -u postgres psql -d datashare
```

Quitter PostgreSQL :

```text
\q
```

---

## 4. Variables d'environnement

Le backend utilise notamment :

```text
DB_PASSWORD
JWT_SECRET
```

Sur la machine de développement :

```bash
source ~/.config/datashare/env
```

Pour vérifier uniquement leur présence :

```bash
printenv DB_PASSWORD >/dev/null && echo "DB_PASSWORD définie"
printenv JWT_SECRET >/dev/null && echo "JWT_SECRET définie"
```

Ne pas afficher les valeurs réelles dans :

- les captures d'écran ;
- les logs ;
- la documentation ;
- le dépôt Git.

---

## 5. Démarrage du backend

```bash
cd ~/Projets/DataShare/backend
source ~/.config/datashare/env
./mvnw spring-boot:run
```

Adresse :

```text
http://localhost:8080
```

Après toute modification Java du backend, le serveur Spring Boot en cours d'exécution doit être redémarré afin de tester réellement la nouvelle version.

---

## 6. Démarrage du frontend

```bash
cd ~/Projets/DataShare/frontend
npm start
```

Adresse :

```text
http://localhost:4200
```

---

## 7. Consultation des logs

Le backend écrit des logs structurés dans :

```text
backend/logs/datashare.log
```

Afficher les dernières lignes :

```bash
tail -n 50 "$HOME/Projets/DataShare/backend/logs/datashare.log"
```

Suivre les logs en temps réel :

```bash
tail -f "$HOME/Projets/DataShare/backend/logs/datashare.log"
```

Événements métier principaux :

```text
file_upload
file_download
file_delete
```

Les logs ne doivent pas contenir :

```text
mots de passe
hash de mots de passe
JWT
tokens de téléchargement
secrets
```

---

## 8. Diagnostic d'un incident

Ordre recommandé :

### Étape 1 - PostgreSQL

```bash
systemctl status postgresql
```

Vérifier :

- que le service fonctionne ;
- que la base `datashare` existe ;
- que l'utilisateur applicatif peut se connecter.

### Étape 2 - Backend

Vérifier :

```text
démarrage Spring Boot
logs
exceptions Java
connexion PostgreSQL
migrations Flyway
droits sur le stockage local
```

### Étape 3 - API

Contrôler :

```text
code HTTP
corps de réponse
headers
JWT si route protégée
X-Download-Password si téléchargement protégé
```

### Étape 4 - Frontend

Contrôler :

- console du navigateur ;
- onglet Network ;
- erreurs Angular ;
- requêtes vers l'API.

---

## 9. Diagnostic d'un téléchargement protégé

Le téléchargement utilise :

```text
GET /api/download/{token}/file
```

Pour un fichier protégé, le mot de passe est transmis avec :

```text
X-Download-Password
```

Comportements attendus :

```text
token inconnu ou métadonnées déjà supprimées par la purge
→ 404 Not Found

token expiré avec métadonnées encore présentes
→ 410 Gone

mot de passe absent
→ 403 Forbidden

mot de passe incorrect
→ 403 Forbidden

mot de passe correct
→ 200 OK
```

Le mot de passe est comparé au hash BCrypt stocké en base.

---

## 10. Content-Type invalide

Lors d'un téléchargement, un type MIME valide est utilisé normalement.

Si le Content-Type stocké est invalide, le backend utilise le fallback :

```text
application/octet-stream
```

Cela évite qu'une erreur de parsing du type MIME bloque le téléchargement d'un fichier valide.

Ce comportement est couvert par un test automatisé.

---

## 11. Procédure de correction d'un bug

Lorsqu'un bug est identifié :

1. reproduire le problème ;
2. relever le message d'erreur ;
3. identifier la couche concernée ;
4. consulter les logs ;
5. vérifier les données concernées ;
6. appliquer une correction ciblée ;
7. ajouter ou adapter un test si nécessaire ;
8. relancer les tests ;
9. vérifier le parcours fonctionnel ;
10. contrôler `git diff` ;
11. créer un commit explicite.

Une règle métier doit principalement être traitée dans la couche Service.

Une erreur de réponse HTTP doit être examinée dans le Controller et les handlers concernés.

Une erreur de persistance doit être examinée côté Repository, JPA, Flyway et PostgreSQL.

---

## 12. Tests backend

Commande :

```bash
cd ~/Projets/DataShare/backend
source ~/.config/datashare/env
./mvnw clean test
```

Dernière validation :

```text
58 tests réussis
0 échec
0 erreur
BUILD SUCCESS
```

---

## 13. Couverture JaCoCo

Commande :

```bash
cd ~/Projets/DataShare/backend
source ~/.config/datashare/env
./mvnw clean test jacoco:report
```

Rapport :

```text
backend/target/site/jacoco/index.html
```

Derniers résultats :

```text
Instructions : 91,64 % (1930 / 2106)
Branches     : 72,31 % (175 / 242)
Lignes       : 91,89 % (612 / 666)
```

Une baisse importante après une modification doit être analysée.

La couverture ne garantit pas à elle seule l'absence de bugs.

---

## 14. SpotBugs

Commande :

```bash
cd ~/Projets/DataShare/backend
./mvnw spotbugs:check
```

Dernière validation :

```text
BugInstance size is 0
Error size is 0
No errors/warnings found
BUILD SUCCESS
```

---

## 15. Tests frontend

Lint :

```bash
cd ~/Projets/DataShare/frontend
npx ng lint
```

Dernier résultat :

```text
All files pass linting.
```

Tests Angular :

```bash
npx ng test --watch=false
```

Dernier résultat :

```text
44 tests réussis (3 octobre 2026)
```

---

## 16. Tests End-to-End

Commande :

```bash
cd ~/Projets/DataShare/frontend
npx playwright test
```

Dernière validation :

```text
3 tests réussis
```

Le parcours principal couvre notamment :

```text
Inscription
→ Connexion
→ Upload
→ Protection par mot de passe
→ Historique
→ Mauvais mot de passe
→ Bon mot de passe
→ Téléchargement
→ Suppression
```

---

## 17. Build Angular

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

## 18. Maintenance des dépendances frontend

Afficher les dépendances obsolètes :

```bash
cd ~/Projets/DataShare/frontend
npm outdated
```

Analyser les dépendances :

```bash
npm audit
```

Uniquement les dépendances de production :

```bash
npm audit --omit=dev
```

Ne pas utiliser automatiquement :

```bash
npm audit fix --force
```

Une mise à jour majeure doit être analysée avant intégration.

Après une mise à jour :

```bash
npx ng lint
npx ng test --watch=false
npm run build
npx playwright test
```

---

## 19. Maintenance des dépendances backend

Les dépendances Maven sont définies dans :

```text
backend/pom.xml
```

Avant une mise à jour importante :

- lire les notes de version ;
- vérifier Java 21 ;
- vérifier Spring Boot ;
- effectuer la modification sur une branche ;
- relancer les tests ;
- relancer SpotBugs.

Commandes principales :

```bash
cd ~/Projets/DataShare/backend
./mvnw clean test
./mvnw spotbugs:check
```

---

## 20. Maintenance de sécurité

Vérifier régulièrement :

```text
authentification JWT
hash BCrypt utilisateur
hash BCrypt fichier
contrôle du propriétaire
expiration des fichiers
X-Download-Password
validation des fichiers
taille maximale
formats autorisés
rate limiting
variables d'environnement
logs
dépendances
```

La documentation complète est disponible dans :

```text
SECURITY.md
```

---

## 21. Test de charge k6

Script : `performance/download-test.js`.

Le scénario actuel teste le téléchargement d'un fichier
protégé par mot de passe :

`GET /api/download/{token}/file`

Commande de reproduction :

```bash
cd ~/Projets/DataShare

DOWNLOAD_TOKEN='<token>' \
DOWNLOAD_PASSWORD='<mot-de-passe>' \
BASE_URL='http://localhost:8080' \
k6 run performance/download-test.js
```

Seuils :

- `http_req_failed < 1 %` ;
- `p95 < 1000 ms`.

### Dernier résultat — 1er octobre 2026

| Indicateur | Résultat |
|---|---:|
| Utilisateurs virtuels | 10 |
| Itérations partagées | 20 |
| Téléchargements HTTP 200 | 20/20 |
| Vérifications | 40/40 |
| Échecs | 0 |
| p95 | 475,39 ms |

Les seuils définis ont été respectés.

Contrôle complémentaire du rate limiting :
30 téléchargements autorisés, puis HTTP 429
à la 31e tentative dans la fenêtre de limitation.

L'ancien scénario, avec 2 569 requêtes et un
p95 de 91,47 ms, reste documenté dans `PERF.md`.

Ces mesures correspondent à l'environnement local
et ne représentent pas une capacité maximale
en production.

Documentation détaillée : `PERF.md`.

---

## 22. Lighthouse

Générer d'abord le build Angular de production :

```bash
cd ~/Projets/DataShare/frontend
npm run build
```

Le build peut ensuite être servi localement avec :

```bash
python3 -m http.server 4173 \
  --directory dist/frontend/browser \
  --bind 127.0.0.1
```

Ouvrir `http://localhost:4173/` dans Chrome,
puis effectuer les audits Mobile et Desktop
depuis DevTools > Lighthouse.

Une mesure en ligne de commande est également possible :

```bash
npx lighthouse http://localhost:4173/ \
  --output=json \
  --output-path=./lighthouse-final.json
```

### Dernière campagne — 1er octobre 2026

Lighthouse 13.4.0, build Angular de production.

| Catégorie | Mobile | Desktop |
|---|---:|---:|
| Performance | 91/100 | 100/100 |
| Accessibilité | 100/100 | 100/100 |
| Bonnes pratiques | 100/100 | 100/100 |
| SEO | 100/100 | 100/100 |

| Métrique | Mobile | Desktop |
|---|---:|---:|
| FCP | 2,7 s | 0,5 s |
| LCP | 2,9 s | 0,6 s |
| TBT (valeur JSON) | 14 ms | 0 ms |
| CLS | 0 | 0 |
| Speed Index | 2,7 s | 0,5 s |

Les mesures précédentes restent disponibles
dans `PERF.md` à titre historique.

Ces résultats sont issus d'un test local ponctuel.

---

## 23. Maintenance PostgreSQL

Sauvegarde :

```bash
pg_dump -U datashare datashare > datashare_backup.sql
```

Restauration :

```bash
psql -U datashare datashare < datashare_backup.sql
```

Les sauvegardes contenant des données réelles ne doivent pas être versionnées dans Git.

---

## 24. Flyway

Les migrations sont stockées dans :

```text
backend/src/main/resources/db/migration/
```

Hibernate utilise :

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Vérifier l'historique :

```bash
sudo -u postgres psql -d datashare \
  -c 'SELECT installed_rank, version, description, type, success FROM flyway_schema_history ORDER BY installed_rank;'
```

La migration ajoutant la protection par mot de passe des fichiers fait partie de l'historique Flyway.

Une migration déjà appliquée ne doit pas être modifiée arbitrairement.

Une évolution de schéma doit être ajoutée avec une nouvelle migration.

---

## 25. Purge des fichiers expirés

Une tâche planifiée recherche les fichiers expirés.

Lors de la purge, l'application retire :

```text
fichier physique
+
métadonnées PostgreSQL
```

Lors d'un diagnostic, vérifier la cohérence entre :

```text
base de données
uploads/
date d'expiration
```

---

## 26. Stockage local

Les fichiers sont stockés localement.

Vérifier :

- existence du répertoire ;
- droits du backend ;
- espace disque disponible ;
- cohérence avec PostgreSQL.

Le stockage local convient au MVP mono-instance.

Une architecture distribuée nécessiterait un stockage partagé ou objet.

---

## 27. Rate limiting

Le rate limiting actuel est conservé en mémoire.

Cela convient à une instance unique.

Une architecture multi-instance nécessiterait un mécanisme partagé, par exemple :

```text
Redis
```

---

## 28. Utilisation de l'IA

Documentation :

```text
AI_USAGE.md
AI_REVIEW.md
```

Le processus appliqué est :

```text
proposition
→ compréhension
→ relecture
→ test
→ correction
→ non-régression
→ traçabilité Git
```

Le code proposé avec assistance IA ne doit pas être intégré sans contrôle humain.

---

## 29. Processus Git

Avant une modification :

```bash
git status
git diff
```

Après validation :

```bash
git add <fichiers>
git commit -m "type: description"
```

Conventions utilisées :

```text
feat:
fix:
test:
docs:
perf:
chore:
```

Ne pas versionner :

```text
secrets
logs
uploads
node_modules
sauvegardes contenant des données
artefacts temporaires
```

---

## 30. Calendrier de maintenance préventive

Ce calendrier est une proposition d'exploitation pour DataShare.
Il distingue les traitements déjà automatisés des contrôles
à organiser par le mainteneur.

| Fréquence | Opération | Mode | Responsable |
|---|---|---|---|
| Au démarrage, puis environ toutes les heures | Purge des fichiers expirés | Automatique, Spring Boot | Application |
| Chaque jour | Contrôler les logs, erreurs HTTP, espace disque et état de PostgreSQL | Manuel, à planifier | Mainteneur |
| Chaque jour en exploitation réelle | Sauvegarder PostgreSQL et le répertoire de fichiers de manière cohérente | À automatiser | Mainteneur |
| Chaque semaine | Vérifier les échecs de purge et la cohérence entre fichiers physiques et métadonnées | Manuel | Mainteneur |
| Chaque semaine | Exécuter les audits npm et OWASP Dependency-Check avec des données actualisées | Manuel ou future CI | Mainteneur |
| Chaque mois | Examiner les nouvelles versions Angular, Spring Boot et les dépendances Java | Manuel | Mainteneur |
| Chaque mois | Vérifier la taille, la protection et la rétention des sauvegardes | Manuel | Mainteneur |
| Chaque trimestre | Tester une restauration sur un environnement isolé | Manuel | Mainteneur |
| Après chaque modification significative | Relancer les tests, les contrôles statiques, le build et les vérifications fonctionnelles | Commandes existantes | Développeur |

### Tâche actuellement automatisée

Le service `ExpiredFileCleanupService` utilise `@Scheduled`
avec les paramètres configurables suivants :

```properties
datashare.cleanup.initial-delay-ms=60000
datashare.cleanup.fixed-delay-ms=3600000
```

Par défaut, le premier lancement intervient après 60 secondes.
Le délai fixe entre deux exécutions est ensuite d'une heure,
calculée à partir de la fin de l'exécution précédente.

Cette tâche nécessite que l'application Spring Boot soit
démarrée et fonctionne correctement.

### Sauvegardes : procédure à industrialiser

La commande `pg_dump` est documentée dans la section 23,
mais aucun ordonnancement automatique des sauvegardes
n'est revendiqué dans ce MVP.

Une future exploitation devra prévoir :

- la sauvegarde cohérente de PostgreSQL et des fichiers stockés ;
- un emplacement de sauvegarde distinct du stockage applicatif ;
- des droits d'accès restrictifs et un chiffrement adapté ;
- une durée de conservation définie ;
- une vérification régulière de l'intégrité des sauvegardes ;
- un test de restauration sur un environnement isolé ;
- le respect des dates d'expiration et des règles de suppression
  des données, y compris dans les sauvegardes.

Une sauvegarde de la base seule ne permet pas nécessairement
de restaurer les fichiers physiques associés.

### Contrôles à effectuer après une mise à jour

```bash
cd ~/Projets/DataShare/backend
source ~/.config/datashare/env
./mvnw clean verify
```

Puis, depuis `frontend/` :

```bash
cd ~/Projets/DataShare/frontend
npx ng lint
npx ng test --watch=false --code-coverage
npm run build
npx playwright test
```

Les audits de sécurité et les tests de performance doivent
également être renouvelés après les changements concernés.

---

## 31. Analyse des risques et mesures de maintenance

Cette matrice identifie les principaux risques techniques
du MVP. Les niveaux indiqués constituent une appréciation
préventive pour organiser les contrôles, et non le résultat
d'une analyse quantitative en production.

| Risque | Conséquence possible | Prévention | Réaction en cas d'incident |
|---|---|---|---|
| Indisponibilité de PostgreSQL | Authentification et gestion des fichiers perturbées | Surveillance du service et sauvegardes | Vérifier le journal PostgreSQL, rétablir le service et contrôler l'intégrité |
| Saturation du stockage local | Échec des téléversements | Surveiller l'espace libre et la purge | Libérer de l'espace sans supprimer arbitrairement les fichiers actifs |
| Incohérence base/fichiers | Métadonnées sans fichier physique, ou inversement | Sauvegarde cohérente et contrôle régulier | Identifier les écarts, restaurer les éléments disponibles |
| Défaillance de la purge | Conservation excessive de fichiers expirés | Vérifier les événements de nettoyage dans les logs | Diagnostiquer le service planifié et relancer après correction |
| Vulnérabilité d'une dépendance | Exposition à une faille connue | Audits npm/OWASP et veille de sécurité | Évaluer le risque, appliquer une version corrigée et refaire les tests |
| Régression après mise à jour | Fonctionnalité indisponible ou incorrecte | Tests unitaires, intégration, E2E et validation Maven | Revenir à une version applicative validée après analyse |
| Migration Flyway incorrecte | Schéma ou données incompatibles | Relecture, migration additive et sauvegarde préalable | Stopper le déploiement et appliquer une procédure de récupération vérifiée |
| Fuite de secrets dans Git ou les logs | Accès non autorisé | Variables d'environnement et revue des modifications | Révoquer les secrets exposés, les renouveler et rechercher les traces |
| Abus des liens publics | Surcharge ou téléchargements excessifs | Expiration, mot de passe optionnel et rate limiting | Examiner les logs, ajuster les protections et bloquer l'abus si nécessaire |
| Déploiement sur plusieurs instances | Limitation de débit incohérente et stockage non partagé | Conserver l'architecture mono-instance du MVP | Prévoir des compteurs partagés et un stockage adapté avant distribution |

### Risque actuellement identifié : dépendances Angular

L'audit du 1er octobre 2026 signale encore sept
vulnérabilités dans les dépendances de production
du frontend.

La mise à jour majeure proposée par npm n'a pas été
appliquée automatiquement pour éviter une régression
non maîtrisée.

Le traitement devra prévoir l'étude des avis de sécurité,
l'analyse de leur applicabilité, une stratégie de migration
Angular compatible et des tests complets.

Les résultats détaillés figurent dans `SECURITY.md`.

---

## 32. Checklist après maintenance

Avant de considérer une maintenance comme terminée :

```text
PostgreSQL fonctionne
backend démarre
frontend démarre
tests backend réussissent
tests frontend réussissent
lint réussit
SpotBugs réussit
Playwright réussit
build Angular réussit
logs contrôlés
git diff contrôlé
git status contrôlé
documentation mise à jour
```

---

## Conclusion

La maintenance de DataShare repose sur :

- une architecture séparée en couches ;
- des tests automatisés ;
- une couverture mesurée ;
- des analyses statiques ;
- des contrôles de sécurité ;
- des migrations Flyway ;
- des tests de performance ;
- une gestion contrôlée de PostgreSQL et du stockage ;
- une traçabilité Git ;
- une revue humaine des modifications.
