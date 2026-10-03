# Rapport de sécurité - DataShare

## 1. Objectif

Ce document décrit les principaux mécanismes de sécurité mis en place dans DataShare.

DataShare est un MVP pédagogique. Les mesures présentées ici améliorent la sécurité applicative mais ne remplacent pas une architecture de production complète.

---

## 2. Authentification utilisateur

L'authentification repose sur :

```text
Spring Security
JWT
```

Lors de la connexion, le backend retourne un JWT.

Angular l'envoie ensuite dans :

```text
Authorization: Bearer <token>
```

Les routes privées sont protégées côté backend.

Le frontend utilise également :

```text
interceptor JWT
guard de navigation
```

---

## 3. Mots de passe utilisateurs

Les mots de passe utilisateurs :

- ne sont pas stockés en clair ;
- sont hashés avant enregistrement ;
- ne doivent jamais apparaître dans les logs.

Le backend utilise BCrypt.

---

## 4. Protection des fichiers par mot de passe

Un fichier partagé peut être protégé par un mot de passe.

Pendant l'upload :

```text
mot de passe
    ↓
BCrypt
    ↓
hash
    ↓
PostgreSQL
```

Le mot de passe original n'est pas stocké.

Lors du téléchargement, Angular envoie :

```text
X-Download-Password
```

Le backend utilise :

```text
PasswordEncoder.matches(...)
```

pour comparer le mot de passe fourni avec le hash.

Pour un fichier protégé :

```text
mot de passe absent
→ 403 Forbidden

mot de passe incorrect
→ 403 Forbidden

mot de passe correct
→ téléchargement autorisé
```

Les anciens fichiers sans mot de passe restent compatibles avec le système.

---

## 5. Secrets applicatifs

Les secrets sont fournis par variables d'environnement.

Variables principales :

```text
DB_PASSWORD
JWT_SECRET
```

Exemple de chargement :

```bash
source ~/.config/datashare/env
```

Les valeurs réelles ne doivent pas être enregistrées dans Git.

---

## 6. Contrôle des fichiers téléversés

Les fichiers sont soumis à plusieurs contrôles :

```text
taille maximale : 1 Go
expiration : 1 à 7 jours
extensions autorisées
contenu réel du fichier
cohérence extension / contenu
```

Formats autorisés :

```text
TXT
PDF
PNG
JPG
JPEG
MP3
MP4
ZIP
```

Un fichier dont le contenu ne correspond pas au type attendu est refusé.

Ces contrôles ne remplacent pas un antivirus ou un moteur d'analyse spécialisé.

---

### Justification du périmètre des formats

Le MVP accepte désormais huit extensions :

- TXT ;
- PDF ;
- PNG ;
- JPG et JPEG ;
- MP3 ;
- MP4 ;
- ZIP.

Le backend vérifie la cohérence entre l'extension et
le type détecté à partir du contenu réel du fichier,
indépendamment du type MIME déclaré par le navigateur.

Contrôles ajoutés :

- MP3 : reconnaissance ID3v2 ou MPEG Audio ;
- MP4 : reconnaissance de la boîte initiale `ftyp` ;
- ZIP : reconnaissance de la signature d'archive.

Les fichiers ZIP sont stockés comme des fichiers ordinaires.
Ils ne sont ni extraits ni exécutés sur le serveur.

La détection utilise un échantillon initial de 8 192 octets.
Elle ne remplace pas une validation structurelle exhaustive
ni un antivirus.

La limite de taille de 1 Go et les autres contrôles
de sécurité restent applicables.

---

## 7. Type MIME

Le backend détecte le contenu réel du fichier.

Types principaux :

```text
application/pdf
image/png
image/jpeg
text/plain
```

Lors du téléchargement, si un Content-Type enregistré est invalide, le contrôleur utilise comme valeur de repli :

```text
application/octet-stream
```

Cela évite qu'une valeur MIME incorrecte empêche le téléchargement d'un fichier valide.

Ce comportement est couvert par un test automatisé.

---

## 8. Autorisations

L'historique est associé à l'utilisateur authentifié.

La suppression utilise :

```text
identifiant fichier
+
utilisateur connecté
```

Un utilisateur ne peut pas supprimer directement le fichier appartenant à un autre utilisateur.

Dans l'implémentation actuelle, un fichier inexistant ou n'appartenant pas à l'utilisateur est traité comme :

```text
404 Not Found
```

---

## 9. Tokens de téléchargement

Les liens publics utilisent un token de téléchargement distinct du JWT.

Le token permet de retrouver le fichier partagé.

Les liens sont également limités par une date d'expiration.

La protection du téléchargement repose donc sur :

```text
token
+
expiration
+
mot de passe du fichier
```

lorsqu'un mot de passe est défini.

---

## 10. Expiration

Les fichiers possèdent une date d'expiration.

Durée autorisée :

```text
1 à 7 jours
```

Un lien expiré dont les métadonnées existent encore dans PostgreSQL retourne :

```text
410 Gone
```

Une tâche planifiée supprime ensuite les fichiers expirés et leurs métadonnées.

Après cette suppression, le token devient introuvable et l'API retourne
`404 Not Found`, comme pour un token inconnu.

---

## 11. Rate limiting

DataShare utilise un mécanisme de limitation de débit
conservé en mémoire dans l'instance Spring Boot.

Configuration documentée :

| Opération | Limitation |
|---|---:|
| Connexion | 10 requêtes/minute/IP |
| Téléversement | 20 requêtes/minute/IP |
| Téléchargement réel | 60 requêtes/minute/IP |
| Téléchargement réel | 30 requêtes/minute/token |

La limitation de téléchargement porte sur :

```text
GET /api/download/{token}/file
```

Lorsqu'un quota est dépassé, le backend répond :

```text
429 Too Many Requests
```

Le 1er octobre 2026, une vérification fonctionnelle
avec le même token valide a donné :

- 30 téléchargements autorisés (HTTP 200) ;
- 31e téléchargement refusé (HTTP 429) ;
- test exécuté dans une même fenêtre de limitation.

Les limites de téléchargement réduisent notamment
le risque d'abus répétés sur un lien de partage.

Le stockage des compteurs en mémoire convient au MVP
mono-instance. Une architecture multi-instance
nécessiterait un compteur partagé, par exemple Redis.

Voir également `API.md` et `PERF.md`.

---

## 12. Logs

Le backend produit des logs structurés.

Ils ne doivent pas contenir :

```text
mots de passe
JWT
tokens de téléchargement
secrets
```

Événements métier principaux :

```text
file_upload
file_download
file_delete
```

---

## 13. Analyse statique frontend

ESLint :

```bash
cd frontend
npx ng lint
```

Dernière validation :

```text
All files pass linting.
```

---

## 14. Analyse statique backend

SpotBugs :

```bash
cd backend
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

## 15. Dépendances frontend

Analyse :

```bash
cd frontend
npm audit
npm audit --omit=dev
```

Une correction forcée :

```bash
npm audit fix --force
```

ne doit pas être appliquée sans analyser les changements de versions et les risques de régression.


### Résultats mesurés des audits de dépendances

Les contrôles suivants ont été réalisés les 1er et 2 octobre 2026.

#### Frontend Angular

Commandes exécutées depuis `frontend/` :

```bash
npm audit --omit=dev
npm audit
```

| Périmètre | Faibles | Modérées | Élevées | Critiques | Total |
|---|---:|---:|---:|---:|---:|
| Dépendances de production | 0 | 3 | 4 | 0 | 7 |
| Audit complet | 2 | 16 | 15 | 2 | 35 |

Les sept alertes de production concernent des dépendances
de l'écosystème Angular 19.

L'audit complet inclut également les outils de développement
et de compilation. Les résultats ne constituent pas à eux
seuls une preuve d'exploitation possible dans DataShare.

Au moment du contrôle, npm proposait notamment des corrections
nécessitant une migration majeure vers Angular 21.

La commande `npm audit fix --force` n'a pas été exécutée,
afin de ne pas introduire de changement incompatible sans
migration préparée et tests de non-régression.

**État : alertes frontend connues, traitement restant à planifier.**

#### Backend Java — OWASP Dependency-Check

Outil utilisé : OWASP Dependency-Check 13.0.0.

Le premier scan a été réalisé le 1er octobre 2026
avec la base NVD synchronisée.

Résultat initial :

| Indicateur | Avant correction |
|---|---:|
| Dépendances analysées | 95 (51 uniques) |
| Dépendances vulnérables détectées | 1 |
| Vulnérabilités détectées | 11 |
| Bibliothèque concernée | tomcat-embed-core 11.0.24 |
| Vulnérabilités supprimées du rapport | 0 |

**Mesure corrective appliquée :**

Spring Boot 4.1.1 gérait initialement Tomcat 11.0.24.
La propriété Maven suivante a été ajoutée à `backend/pom.xml` :

```xml
<tomcat.version>11.0.26</tomcat.version>
```

L'arbre Maven a confirmé que les trois modules
`tomcat-embed-core`, `tomcat-embed-el` et
`tomcat-embed-websocket` utilisent désormais 11.0.26.

Après modification :

```bash
cd backend
source ~/.config/datashare/env
./mvnw clean verify
```

Résultat :

- 58 tests backend réussis ;
- couverture JaCoCo des branches : 72,31 % (175/242) ;
- seuil bloquant JaCoCo de 70 % respecté ;
- SpotBugs : aucune anomalie ;
- BUILD SUCCESS.

Un second scan OWASP a été effectué le 2 octobre 2026
à 00 h 12, sur la base NVD précédemment téléchargée :

```bash
./mvnw org.owasp:dependency-check-maven:13.0.0:check \
  -DautoUpdate=false \
  -DfailBuildOnCVSS=11 \
  -Dformat=HTML
```

| Indicateur | Après correction |
|---|---:|
| Dépendances analysées | 95 (51 uniques) |
| Dépendances vulnérables détectées | 0 |
| Vulnérabilités détectées | 0 |
| Vulnérabilités supprimées du rapport | 0 |

Les 11 alertes initiales ne sont plus détectées après
la mise à jour vers Tomcat 11.0.26.

**Limites de l'analyse :**

- Le second contrôle utilise le cache NVD initial,
  sans nouvelle synchronisation (`autoUpdate=false`).
- L'analyseur Sonatype OSS Index n'a pas été exécuté,
  faute d'identifiants.
- `failBuildOnCVSS=11` désactive volontairement le blocage
  Maven sur les scores CVSS pendant cet audit.
- L'absence d'alerte détectée ne garantit pas l'absence
  de toute vulnérabilité.

Rapport HTML local :

```text
backend/target/dependency-check-report.html
```

Ce fichier est généré par l'outil et peut être régénéré
lors des prochains audits. Les dépendances doivent être
réévaluées régulièrement à partir des avis de sécurité
et des mises à jour disponibles.

---

## 16. Tests de sécurité fonctionnelle

Les tests automatisés couvrent notamment :

```text
authentification
tokens invalides
liens expirés
fichiers interdits
faux PDF
taille maximale
rate limiting
mot de passe fichier correct
mot de passe fichier incorrect
mot de passe fichier absent
fallback Content-Type
```

Résultat historique backend — 1er octobre 2026 :

```text
49 tests réussis (1er octobre 2026)
0 échec
0 erreur
```

---

## 17. Données sensibles

Les éléments suivants ne doivent pas être versionnés :

```text
mots de passe
JWT_SECRET
DB_PASSWORD
tokens sensibles
logs contenant des données privées
sauvegardes PostgreSQL
fichiers téléversés
```

---

## 18. Limites du MVP

DataShare utilise actuellement :

```text
une seule instance Spring Boot
PostgreSQL local
stockage local
rate limiting en mémoire
HTTP en développement
```

Pour une mise en production, il faudrait notamment envisager :

- HTTPS obligatoire ;
- reverse proxy ;
- rotation des secrets ;
- stockage objet partagé ;
- antivirus ou analyse spécialisée des fichiers ;
- rate limiting distribué ;
- sauvegardes automatisées ;
- supervision ;
- centralisation des logs ;
- haute disponibilité ;
- politique de restauration testée.

---

## 19. Conclusion

La sécurité du MVP repose sur plusieurs protections complémentaires :

```text
JWT
Spring Security
BCrypt
mot de passe fichier
expiration
tokens de partage
contrôle du propriétaire
validation du contenu
rate limiting
variables d'environnement
tests
ESLint
SpotBugs
```

Ces mécanismes réduisent les risques principaux du MVP mais ne doivent pas être considérés comme une certification de sécurité pour un déploiement de production.
