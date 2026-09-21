# Procédures de maintenance - DataShare

## Objectif

Ce document décrit les principales procédures de maintenance de DataShare.

Il permet de :

- vérifier l'état de l'application ;
- diagnostiquer un incident ;
- consulter les logs ;
- appliquer une correction ;
- vérifier qu'une modification n'introduit pas de régression ;
- contrôler la sécurité ;
- surveiller les performances ;
- maintenir les dépendances ;
- maintenir PostgreSQL et le stockage local.

---

## 1. Vérification de l'état de l'application

Avant toute intervention, vérifier que les différents composants sont
correctement démarrés.

### Backend

Le backend Spring Boot doit être disponible sur :

```text
http://localhost:8080
```

Variables d'environnement nécessaires :

```bash
export DB_PASSWORD='votre_mot_de_passe_postgresql'
export JWT_SECRET='votre_secret_jwt'
```

Commande de démarrage :

```bash
cd ~/Projets/DataShare/backend
./mvnw spring-boot:run
```

Ces variables doivent être redéfinies dans chaque nouveau terminal.

---

### Frontend

Le frontend Angular doit être disponible sur :

```text
http://localhost:4200
```

Commande de démarrage :

```bash
cd ~/Projets/DataShare/frontend
npm start
```

---

### PostgreSQL

Vérifier l'état du service :

```bash
systemctl status postgresql
```

Si nécessaire, démarrer PostgreSQL :

```bash
sudo systemctl start postgresql
```

La base utilisée par DataShare est :

```text
datashare
```

---

## 2. Consultation des logs

Le backend produit des logs structurés au format JSON.

Le fichier principal est :

```text
backend/logs/datashare.log
```

Pour afficher les dernières lignes :

```bash
tail -n 50 backend/logs/datashare.log
```

Pour suivre les logs en temps réel :

```bash
tail -f backend/logs/datashare.log
```

Les principaux événements métier journalisés sont :

```text
file_upload
file_download
file_delete
```

Ces événements permettent notamment de suivre :

- l'opération réalisée ;
- l'identifiant du fichier ;
- la taille du fichier ;
- certaines informations nécessaires au diagnostic.

Les données sensibles ne doivent pas être enregistrées dans les logs.

En particulier :

- les mots de passe ;
- les JWT ;
- les tokens de téléchargement.

---

## 3. Diagnostic d'un incident

En cas de dysfonctionnement, vérifier les composants dans l'ordre suivant.

### Étape 1 - PostgreSQL

```bash
systemctl status postgresql
```

Vérifier que la base `datashare` est accessible.

---

### Étape 2 - Backend

Vérifier que Spring Boot démarre sans erreur et consulter :

```text
backend/logs/datashare.log
```

Identifier notamment :

- les erreurs HTTP ;
- les exceptions Java ;
- les erreurs d'accès à PostgreSQL ;
- les erreurs d'accès au stockage local.

---

### Étape 3 - Frontend

Vérifier que le serveur Angular fonctionne :

```bash
cd ~/Projets/DataShare/frontend
npm start
```

Contrôler également la console du navigateur et les requêtes réseau
si une erreur apparaît dans l'interface.

---

### Étape 4 - Variables d'environnement

Vérifier que les variables nécessaires sont présentes :

```bash
printenv DB_PASSWORD
printenv JWT_SECRET
```

Ne pas afficher leur valeur dans une capture destinée à être partagée.

---

## 4. Procédure de correction d'un bug

Lorsqu'un bug est identifié :

1. reproduire le problème ;
2. identifier la couche concernée ;
3. consulter les logs ;
4. corriger uniquement le comportement concerné ;
5. relancer les tests ;
6. vérifier le parcours utilisateur concerné ;
7. vérifier qu'aucune régression majeure n'a été introduite ;
8. créer un commit Git explicite.

Architecture backend à garder en tête :

```text
Controller -> Service -> Repository -> PostgreSQL / stockage local
```

Une règle métier doit principalement être corrigée dans la couche Service.

Un problème d'accès aux données doit être vérifié dans la couche Repository.

Un problème lié aux requêtes ou réponses HTTP doit être vérifié dans
le Controller.

---

## 5. Tests de non-régression

Après une correction backend :

```bash
cd ~/Projets/DataShare/backend

export DB_PASSWORD='votre_mot_de_passe_postgresql'
export JWT_SECRET='votre_secret_jwt'

./mvnw clean test
```

Lors de la validation du projet :

```text
32 tests backend réussis
```

Après une modification du frontend ou d'un parcours utilisateur critique :

```bash
cd ~/Projets/DataShare/frontend
npx playwright test
```

Lors de la dernière validation :

```text
3 tests End-to-End réussis
```

Le scénario E2E vérifie notamment :

```text
Inscription
-> Connexion
-> Upload
-> Historique
-> Téléchargement
-> Suppression
```

La stratégie complète est décrite dans :

```text
TESTING.md
```

---

## 6. Couverture du code

La couverture du backend est mesurée avec JaCoCo.

Commande :

```bash
cd ~/Projets/DataShare/backend
./mvnw clean test
```

Le rapport est généré dans :

```text
backend/target/site/jacoco/index.html
```

Résultats observés pendant la validation :

```text
Couverture des instructions : 80 %
Couverture des branches : 73 %
```

Une baisse importante de couverture après une modification doit être analysée.

---

## 7. Maintenance des dépendances

Les dépendances doivent être revues régulièrement.

### Fréquence recommandée

- vérification mensuelle des dépendances ;
- vérification avant une livraison importante ;
- traitement prioritaire lorsqu'une vulnérabilité critique concerne
  directement une dépendance utilisée en production.

---

### Frontend

Afficher les dépendances obsolètes :

```bash
cd ~/Projets/DataShare/frontend
npm outdated
```

Analyser les vulnérabilités :

```bash
npm audit
```

Analyser uniquement les dépendances utilisées en production :

```bash
npm audit --omit=dev
```

Ne pas exécuter automatiquement :

```bash
npm audit fix --force
```

sans analyser les conséquences.

Une mise à jour forcée peut provoquer :

- une migration majeure d'Angular ;
- des incompatibilités ;
- des modifications d'API ;
- des régressions fonctionnelles.

Après toute mise à jour :

```bash
npm install
npx playwright test
```

Puis vérifier manuellement les principales pages de l'application.

---

### Backend

Les versions des dépendances sont définies dans :

```text
backend/pom.xml
```

Avant une mise à jour importante :

- lire les notes de version ;
- vérifier la compatibilité avec Java 21 ;
- vérifier la compatibilité avec Spring Boot ;
- effectuer la mise à jour sur une branche dédiée ;
- relancer l'ensemble des tests backend.

Commande de validation :

```bash
./mvnw clean test
```

---

## 8. Maintenance de sécurité

La sécurité des dépendances frontend est vérifiée avec :

```bash
cd ~/Projets/DataShare/frontend
npm audit
```

Les résultats détaillés et leur analyse sont documentés dans :

```text
SECURITY.md
```

Lors d'une maintenance de sécurité, vérifier également :

- le hachage des mots de passe ;
- le fonctionnement de l'authentification JWT ;
- les droits d'accès aux fichiers ;
- la vérification du propriétaire avant suppression ;
- l'expiration des liens ;
- la limite de taille des fichiers ;
- la whitelist des formats autorisés ;
- la vérification du contenu réel et de la cohérence extension/contenu ;
- le rate limiting sur la connexion et l'upload ;
- l'absence de secrets dans Git ;
- l'absence de mots de passe, JWT ou tokens dans les logs.

---

## 9. Maintenance des performances

Le test de performance backend est réalisé avec k6.

Le script est situé dans :

```text
performance/download-test.js
```

Le test porte sur :

```text
GET /api/download/{token}/file
```

Il utilise notamment les seuils suivants :

```text
taux d'erreur < 1 %
p95 < 1000 ms
```

Exemple d'exécution :

```bash
cd ~/Projets/DataShare

DOWNLOAD_TOKEN='token_de_test_valide' \
BASE_URL='http://localhost:8080' \
k6 run performance/download-test.js
```

Ne jamais utiliser un token sensible destiné à être publié dans le repository.

Lors de la validation réalisée localement :

```text
10 utilisateurs virtuels
20 secondes
161 545 requêtes HTTP
0 % d'erreur
p95 : 1,44 ms
environ 8 076,9 requêtes par seconde
```

Ces résultats correspondent à un environnement local de développement
et ne représentent pas les performances d'une infrastructure de production.

Les détails sont disponibles dans :

```text
PERF.md
```

---

## 10. Maintenance de PostgreSQL

Vérifier régulièrement que PostgreSQL fonctionne :

```bash
systemctl status postgresql
```

Les données importantes sont :

- les comptes utilisateurs ;
- les métadonnées des fichiers ;
- les tokens associés aux fichiers ;
- les dates de création et d'expiration.

Avant une opération importante sur la base, une sauvegarde doit être prévue.

Exemple de sauvegarde :

```bash
pg_dump -U datashare datashare > datashare_backup.sql
```

Exemple de restauration :

```bash
psql -U datashare datashare < datashare_backup.sql
```

Les fichiers contenant des sauvegardes de données ne doivent pas être
ajoutés au repository Git.

---

## 11. Maintenance du stockage des fichiers

DataShare utilise un stockage local pour les fichiers téléversés.

Lors d'un diagnostic, vérifier :

- que le répertoire de stockage existe ;
- que le backend possède les droits nécessaires ;
- que l'espace disque est suffisant ;
- que les fichiers présents correspondent aux métadonnées stockées en base.

Le répertoire de stockage ne doit pas être versionné dans Git.

Lors d'une suppression via l'application, le fichier physique et
les métadonnées associées doivent être supprimés de manière cohérente.

---

## 12. Utilisation et revue du code produit avec l'IA

L'utilisation de l'intelligence artificielle dans le projet est documentée dans :

```text
AI_USAGE.md
```

La revue technique du code développé avec l'assistance de l'IA est documentée dans :

```text
AI_REVIEW.md
```

Cette revue comprend notamment :

- une relecture humaine ;
- la vérification des codes HTTP ;
- la vérification de la gestion des erreurs ;
- la détection d'un problème potentiel sur le Content-Type ;
- l'ajout d'une valeur de repli `application/octet-stream` ;
- des tests de non-régression.

Le code proposé avec l'aide de l'IA ne doit pas être intégré sans
compréhension, relecture et validation.

---

## 13. Processus Git pour une maintenance

Avant une modification :

```bash
git status
```

Créer une modification ciblée puis vérifier :

```bash
git diff
```

Relancer les tests nécessaires avant de créer le commit.

Exemples de conventions utilisées :

```text
fix: ...
feat: ...
test: ...
docs: ...
```

Après validation :

```bash
git add <fichiers>
git commit -m "type: description"
git push
```

Les éléments suivants ne doivent pas être versionnés :

- secrets ;
- fichiers de logs ;
- fichiers téléversés ;
- rapports temporaires ;
- dossiers de build ;
- `node_modules`.

---

## 14. Contrôle après maintenance

Avant de considérer une opération de maintenance comme terminée :

- vérifier que PostgreSQL fonctionne ;
- vérifier que le backend démarre ;
- vérifier que le frontend démarre ;
- relancer les tests backend ;
- relancer le scénario E2E si nécessaire ;
- vérifier les logs ;
- vérifier l'absence de régression visible ;
- vérifier `git status` ;
- documenter toute décision importante.

---

---

## 15. Migrations Flyway et purge des fichiers expirés

Le schéma PostgreSQL est versionné avec Flyway dans :

```text
backend/src/main/resources/db/migration/
```

Hibernate est configuré avec :

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Il ne crée ni ne modifie automatiquement le schéma.

Pour vérifier l'historique Flyway :

```bash
sudo -u postgres psql -d datashare   -c 'SELECT installed_rank, version, description, type, success FROM flyway_schema_history ORDER BY installed_rank;'
```

Les fichiers expirés sont supprimés par une tâche planifiée du backend. La purge retire le fichier physique ainsi que ses métadonnées.

Le stockage et le rate limiting étant locaux à l'instance, le MVP est conçu pour une exécution mono-instance. Une architecture multi-instance nécessiterait un stockage partagé et un mécanisme distribué de rate limiting.


## Conclusion

La maintenance de DataShare repose sur plusieurs contrôles complémentaires :

- disponibilité des composants ;
- analyse des logs ;
- tests automatisés ;
- couverture du code ;
- surveillance des dépendances ;
- contrôles de sécurité ;
- tests de performance ;
- maintenance de PostgreSQL et du stockage ;
- utilisation maîtrisée de Git ;
- revue humaine du code produit avec l'assistance de l'IA.

Ces procédures permettent de corriger et faire évoluer l'application
tout en limitant les risques de régression.