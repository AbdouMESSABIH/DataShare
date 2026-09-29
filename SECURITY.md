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
```

Un fichier dont le contenu ne correspond pas au type attendu est refusé.

Ces contrôles ne remplacent pas un antivirus ou un moteur d'analyse spécialisé.

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

Un lien expiré retourne :

```text
410 Gone
```

Une tâche planifiée supprime ensuite les fichiers expirés et leurs métadonnées.

---

## 11. Rate limiting

Une limitation de débit en mémoire est utilisée par adresse IP.

Configuration documentée :

```text
Connexion
10 requêtes / minute / IP

Upload
20 requêtes / minute / IP
```

Lorsque la limite est dépassée :

```text
429 Too Many Requests
```

Cette implémentation convient au MVP mono-instance.

Une architecture multi-instance nécessiterait un compteur partagé, par exemple Redis.

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

Dernier résultat backend :

```text
41 tests réussis
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
