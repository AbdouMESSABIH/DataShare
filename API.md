# DataShare — Contrat d’interface API

## 1. Objectif

Ce document décrit le contrat d’interface entre le front-end Angular et le back-end Spring Boot de l’application DataShare.

Il présente :

- les endpoints REST ;
- les méthodes HTTP ;
- les données envoyées ;
- les réponses retournées ;
- l’authentification JWT ;
- les principaux codes HTTP ;
- la protection des fichiers par mot de passe ;
- la pagination ;
- l’expiration des fichiers ;
- les règles principales de validation.

Le préfixe principal de l’API est :

```text
/api
```

---

## 2. Authentification

DataShare utilise une authentification par JWT.

Après une connexion réussie, le back-end retourne un token JWT.

Pour accéder aux routes protégées, Angular envoie :

```text
Authorization: Bearer <token-jwt>
```

Les routes publiques sont :

```text
POST /api/auth/register
POST /api/auth/login

GET /api/download/{token}
GET /api/download/{token}/file
```

Les routes suivantes nécessitent une authentification JWT :

```text
POST   /api/files/upload
GET    /api/files
DELETE /api/files/{id}
```

---

## 3. Résumé des endpoints

| Méthode | Endpoint | Fonction | Authentification |
|---|---|---|---|
| POST | `/api/auth/register` | Créer un compte utilisateur | Non |
| POST | `/api/auth/login` | Se connecter | Non |
| POST | `/api/files/upload` | Téléverser un fichier | JWT |
| GET | `/api/files` | Consulter l’historique de ses fichiers | JWT |
| DELETE | `/api/files/{id}` | Supprimer un fichier | JWT |
| GET | `/api/download/{token}` | Consulter les informations d’un fichier partagé | Non |
| GET | `/api/download/{token}/file` | Télécharger un fichier partagé | Non |

---

# 4. Création d’un compte

## Endpoint

```text
POST /api/auth/register
```

## Content-Type

```text
application/json
```

## Corps de la requête

```json
{
  "email": "user@mail.com",
  "password": "motdepasse123"
}
```

## Contraintes

### Email

L’adresse email :

- est obligatoire ;
- doit respecter un format email valide ;
- doit être unique.

### Mot de passe

Le mot de passe :

- est obligatoire ;
- doit contenir au minimum 8 caractères.

Le mot de passe utilisateur est hashé avant son stockage en base de données.

## Réponse réussie

```text
201 Created
```

La réponse ne contient pas de corps.

## Erreurs principales

### 400 Bad Request

La requête contient des données invalides.

Exemples :

```text
email invalide
mot de passe trop court
champ obligatoire absent
```

### 409 Conflict

L’adresse email est déjà utilisée.

---

# 5. Connexion

## Endpoint

```text
POST /api/auth/login
```

## Content-Type

```text
application/json
```

## Corps de la requête

```json
{
  "email": "user@mail.com",
  "password": "motdepasse123"
}
```

## Réponse réussie

```text
200 OK
```

Exemple :

```json
{
  "token": "eyJhbGciOi..."
}
```

Le token JWT est ensuite utilisé par Angular pour accéder aux routes protégées.

## Erreurs principales

### 400 Bad Request

La requête est invalide.

### 401 Unauthorized

Les identifiants sont incorrects.

### 429 Too Many Requests

Trop de requêtes ont été effectuées dans la fenêtre autorisée lorsque le mécanisme de limitation de débit est déclenché.

---

# 6. Téléversement d’un fichier

## Endpoint

```text
POST /api/files/upload
```

## Authentification

JWT obligatoire :

```text
Authorization: Bearer <token-jwt>
```

## Content-Type

```text
multipart/form-data
```

## Champs envoyés

La requête contient :

```text
file
expirationDays
password
```

Exemple :

```text
file = document.pdf
expirationDays = 7
password = Secret123!
```

---

## Champ `file`

Ce champ contient le fichier à téléverser.

Il est obligatoire.

Les formats actuellement acceptés sont :

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

La taille maximale autorisée est :

```text
1 Go
```

Le back-end contrôle également le contenu réel du fichier afin de vérifier sa cohérence avec le format annoncé.

Par exemple :

```text
document.pdf
```

doit réellement correspondre à un fichier PDF valide.

---

## Champ `expirationDays`

Ce champ définit la durée de validité du lien de téléchargement.

Valeurs autorisées :

```text
1 à 7 jours
```

Si aucune valeur n’est fournie, la durée utilisée est :

```text
7 jours
```

---

## Champ `password`

Ce champ contient le mot de passe protégeant le téléchargement.

Le paramètre est optionnel au niveau de l’API.

Lorsqu'un mot de passe est renseigné, il doit contenir
au minimum six caractères. Un champ absent ou vide
correspond à un partage sans protection par mot de passe.

Dans l'interface Angular actuelle, le mot de passe est également optionnel. Un fichier peut être téléversé avec ou sans protection par mot de passe.

Le mot de passe du fichier n’est jamais stocké en clair.

Il est hashé avec BCrypt avant d’être enregistré dans PostgreSQL.

---

## Réponse réussie

```text
201 Created
```

Exemple :

```json
{
  "id": 12,
  "originalName": "document.pdf",
  "size": 2048000,
  "downloadToken": "395dbacc-401c-44bd-94c3-6f04038cf8ce",
  "expiresAt": "2026-10-06T01:40:00"
}
```

### `id`

Identifiant du fichier en base de données.

### `originalName`

Nom original du fichier envoyé par l’utilisateur.

### `size`

Taille du fichier en octets.

### `downloadToken`

Token unique utilisé pour accéder au partage.

### `expiresAt`

Date et heure d’expiration du fichier.

---

## Erreurs principales

### 400 Bad Request

Exemples :

```text
fichier vide
durée d’expiration invalide
paramètres invalides
```

### 401 Unauthorized

L’utilisateur n’est pas authentifié.

### 413 Payload Too Large

Le fichier dépasse la taille maximale autorisée.

### 415 Unsupported Media Type

Le format du fichier n’est pas accepté ou son contenu réel ne correspond pas au format attendu.

### 429 Too Many Requests

Trop de requêtes ont été effectuées lorsque le mécanisme de limitation de débit est déclenché.

### 500 Internal Server Error

Une erreur est survenue pendant l’enregistrement du fichier.

---

# 7. Consultation de l’historique

## Endpoint

```text
GET /api/files
```

Exemple avec pagination :

```text
GET /api/files?page=0&size=10
```

## Authentification

JWT obligatoire :

```text
Authorization: Bearer <token-jwt>
```

---

## Paramètre `page`

Numéro de la page demandée.

La première page correspond à :

```text
0
```

Valeur par défaut :

```text
0
```

La valeur ne peut pas être négative.

---

## Paramètre `size`

Nombre de fichiers retournés par page.

Valeur par défaut :

```text
10
```

Valeurs autorisées :

```text
1 à 50
```

---

## Réponse réussie

```text
200 OK
```

Exemple :

```json
{
  "content": [
    {
      "id": 12,
      "originalName": "document.pdf",
      "size": 2048000,
      "contentType": "application/pdf",
      "downloadToken": "395dbacc-401c-44bd-94c3-6f04038cf8ce",
      "createdAt": "2026-09-29T01:40:00",
      "expiresAt": "2026-10-06T01:40:00",
      "passwordProtected": true
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

L’utilisateur reçoit uniquement les fichiers associés à son propre compte.

---

## Erreurs principales

### 400 Bad Request

Exemples :

```text
page négative
size inférieur à 1
size supérieur à 50
```

### 401 Unauthorized

L’utilisateur n’est pas authentifié.

---

# 8. Suppression d’un fichier

## Endpoint

```text
DELETE /api/files/{id}
```

Exemple :

```text
DELETE /api/files/12
```

## Authentification

JWT obligatoire :

```text
Authorization: Bearer <token-jwt>
```

## Fonctionnement

Le back-end recherche le fichier à partir de :

```text
id du fichier
+
utilisateur connecté
```

Un utilisateur ne peut donc supprimer que ses propres fichiers.

Lors de la suppression :

```text
fichier physique
        +
métadonnées PostgreSQL
        ↓
    supprimés
```

## Réponse réussie

```text
204 No Content
```

## Erreurs principales

### 401 Unauthorized

L’utilisateur n’est pas authentifié.

### 404 Not Found

Le fichier :

```text
n’existe pas
```

ou :

```text
n’appartient pas à l’utilisateur connecté
```

Dans l’implémentation actuelle, ces deux situations sont traitées comme une ressource introuvable et retournent :

```text
404 Not Found
```

### 500 Internal Server Error

Une erreur est survenue pendant la suppression physique du fichier.

---

# 9. Consultation des informations d’un fichier partagé

## Endpoint

```text
GET /api/download/{token}
```

Exemple :

```text
GET /api/download/395dbacc-401c-44bd-94c3-6f04038cf8ce
```

## Authentification

Aucun JWT n’est nécessaire.

Cette route est publique.

## Mot de passe

Le mot de passe du fichier n’est pas nécessaire pour consulter les métadonnées du partage.

Le mot de passe est vérifié uniquement lors du téléchargement réel du fichier.

---

## Réponse réussie

```text
200 OK
```

Exemple :

```json
{
  "originalName": "document.pdf",
  "size": 2048000,
  "contentType": "application/pdf",
  "expiresAt": "2026-10-06T01:40:00",
  "passwordProtected": true
}
```

### `originalName`

Nom original du fichier.

### `size`

Taille du fichier en octets.

### `contentType`

Type MIME du fichier.

### `expiresAt`

Date et heure d’expiration du partage.

---

## Erreurs principales

### 404 Not Found

Le token est inconnu, le fichier physique n’existe plus, ou les métadonnées du partage ont déjà été supprimées par la purge.

### 410 Gone

Le lien a expiré, mais ses métadonnées sont encore présentes en base.

---

# 10. Téléchargement du fichier

## Endpoint

```text
GET /api/download/{token}/file
```

Exemple :

```text
GET /api/download/395dbacc-401c-44bd-94c3-6f04038cf8ce/file
```

## Authentification

Aucun JWT n’est nécessaire.

Cette route est publique.

Le téléchargement repose sur un token valide.

Si le fichier est protégé, un mot de passe correct
est également nécessaire. Aucun mot de passe n'est
requis pour un fichier partagé sans protection.

---

## Transmission du mot de passe

Le mot de passe est transmis dans l’en-tête HTTP :

```text
X-Download-Password: Secret123!
```

Le mot de passe n’est donc pas placé dans l’URL.

Le back-end compare le mot de passe reçu avec le hash BCrypt enregistré dans PostgreSQL.

---

## Mot de passe correct

Le téléchargement est autorisé :

```text
200 OK
```

---

## Mot de passe incorrect

Le serveur retourne :

```text
403 Forbidden
```

---

## Mot de passe absent

Pour un fichier protégé, le serveur retourne :

```text
403 Forbidden
```

Tout fichier partagé sans mot de passe, qu'il provienne
d'un ancien ou d'un nouvel upload, reste téléchargeable
sans l'en-tête `X-Download-Password`.

---

## Réponse réussie

```text
200 OK
```

Le corps de la réponse contient le fichier physique.

Le serveur retourne notamment les en-têtes :

```text
Content-Type
Content-Length
Content-Disposition
```

Exemple :

```text
Content-Disposition: attachment; filename="document.pdf"
```

Cela permet au navigateur de télécharger le fichier avec son nom original.

---

## Erreurs principales

### 403 Forbidden

Le fichier est protégé et le mot de passe est absent ou incorrect.

### 404 Not Found

Le token est inconnu, le fichier physique n’existe plus, ou les métadonnées du partage ont déjà été supprimées par la purge.

### 410 Gone

Le lien a expiré, mais ses métadonnées sont encore présentes en base.

### 429 Too Many Requests

Le quota de téléchargement par IP ou par token
est dépassé. Voir la section 15.

---

<!-- Synthèse API : 3 octobre 2026 -->

# 11. Protection des fichiers par mot de passe

Le mot de passe de partage est facultatif lors du téléversement.

Lorsqu'il est renseigné, il doit contenir au moins six
caractères utiles. Le backend conserve uniquement son hash
BCrypt dans PostgreSQL, jamais sa valeur en clair.

Le téléchargement d'un fichier protégé nécessite l'en-tête
HTTP `X-Download-Password`.

Le backend compare le mot de passe transmis au hash enregistré :

- mot de passe correct : téléchargement autorisé ;
- mot de passe incorrect ou absent : HTTP 403 ;
- fichier non protégé : aucun mot de passe nécessaire.

Les réponses de consultation et d'historique exposent
l'indicateur booléen `passwordProtected`. Il permet notamment
à Angular d'afficher le cadenas et le champ de mot de passe
uniquement lorsque cela est nécessaire.

---

# 12. Expiration des fichiers

Chaque fichier possède une date de création et une date
d'expiration.

La durée est comprise entre 1 et 7 jours, avec 7 jours
par défaut si aucune durée n'est transmise.

Un lien expiré retourne `410 Gone` tant que ses métadonnées
existent encore dans PostgreSQL. Après leur suppression par
la purge automatique, le token n'est plus retrouvé et l'API
retourne `404 Not Found`, comme pour un token inconnu.

Le backend dispose également d'une purge planifiée
des fichiers expirés.

---

# 13. Pagination

L'historique utilise une pagination côté serveur.

Exemple : `GET /api/files?page=0&size=10`

| Paramètre | Contrainte |
|---|---|
| `page` | Entier supérieur ou égal à 0 |
| `size` | Entre 1 et 50 |

La réponse contient notamment `content`, `page`, `size`,
`totalElements` et `totalPages`.

L'accès à l'historique nécessite un JWT valide.

---

# 14. Validation des fichiers

La taille maximale autorisée est de 1 Go.

| Extension | Type MIME attendu |
|---|---|
| TXT | `text/plain` |
| PDF | `application/pdf` |
| PNG | `image/png` |
| JPG, JPEG | `image/jpeg` |
| MP3 | `audio/mpeg` |
| MP4 | `video/mp4` |
| ZIP | `application/zip` |

Le backend contrôle l'extension et détecte le type
à partir du contenu, indépendamment du MIME déclaré
par le navigateur.

Les contrôles portent notamment sur les signatures
PDF, PNG, JPEG, MP3, MP4 et ZIP, ainsi que sur
la validité du contenu texte UTF-8.

Une incohérence entraîne un refus HTTP 415.

La détection par signature ne constitue pas une analyse
antivirus ni une validation exhaustive des fichiers.

Voir `SECURITY.md` pour les limites de ces contrôles.

---

# 15. Limitation de débit

Le backend applique des quotas pour limiter les abus
sur les endpoints sensibles.

| Endpoint | Limitation |
|---|---|
| `POST /api/auth/login` | 10 requêtes/minute/IP |
| `POST /api/files/upload` | 20 requêtes/minute/IP |
| `GET /api/download/{token}/file` | 60 requêtes/minute/IP |
| `GET /api/download/{token}/file` | 30 requêtes/minute/token |

Les deux limites du téléchargement sont complémentaires.

En cas de dépassement, le serveur retourne
`429 Too Many Requests`.

Pour la connexion et le téléversement, la réponse
de limitation comporte `Retry-After: 60`.

Une vérification fonctionnelle du téléchargement
réalisée le 1er octobre 2026 a confirmé :

| Tentatives avec un même token valide | Résultat |
|---|---|
| 1 à 30 | HTTP 200 |
| 31e tentative | HTTP 429 |

Les compteurs sont actuellement conservés en mémoire.
En cas de déploiement multi-instance, un compteur partagé
serait nécessaire, par exemple avec Redis.

Voir `PERF.md` et `SECURITY.md` pour l'analyse détaillée,
notamment le coût du calcul BCrypt.

---

# 16. Codes HTTP principaux

| Code | Signification dans DataShare |
|---|---|
| `200` | Requête réussie |
| `201` | Ressource créée |
| `204` | Suppression réussie |
| `400` | Paramètre ou requête invalide |
| `401` | Authentification absente ou invalide |
| `403` | Mot de passe de téléchargement absent ou incorrect |
| `404` | Ressource ou token introuvable |
| `409` | Conflit, notamment email déjà utilisé |
| `410` | Lien expiré |
| `413` | Taille maximale dépassée |
| `415` | Format ou contenu non autorisé |
| `429` | Quota dépassé |
| `500` | Erreur interne |

Les réponses et paramètres propres à chaque endpoint
sont décrits dans les sections 4 à 10 de ce document.

---

# 17. Parcours principal de partage

1. L'utilisateur s'authentifie et reçoit un JWT.
2. Angular transmet le fichier à `POST /api/files/upload`.
3. Spring Boot contrôle la taille, le format, la durée
   d'expiration et le mot de passe facultatif.
4. Le fichier physique est enregistré sur le stockage local.
   Ses métadonnées et son token sont conservés dans PostgreSQL.
5. Le lien public `/download/{token}` est communiqué.
6. Angular consulte `GET /api/download/{token}` pour
   récupérer les informations, dont `passwordProtected`.
7. Le téléchargement utilise
   `GET /api/download/{token}/file`, avec
   `X-Download-Password` uniquement si nécessaire.

Le backend vérifie le token, l'expiration, les quotas
et la protection éventuelle avant de transmettre le fichier.

L'architecture générale est représentée dans `README.md`
et dans la documentation technique PDF.

---

# 18. Documents complémentaires

| Document | Informations détaillées |
|---|---|
| `README.md` | Architecture, installation et utilisation |
| `SECURITY.md` | Contrôles de sécurité et audits |
| `TESTING.md` | Tests et critères d'acceptation |
| `PERF.md` | Métriques, budgets et performances |
| `MAINTENANCE.md` | Maintenance et exploitation |

Ce contrat correspond à la version DataShare
validée le 3 octobre 2026.
