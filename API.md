# Documentation API REST - DataShare

## Objectif

Ce document décrit l’API REST utilisée par le frontend Angular de DataShare.

L’API permet notamment :

- la création d’un compte ;
- l’authentification ;
- le téléversement d’un fichier ;
- la consultation de l’historique ;
- la suppression d’un fichier ;
- la consultation des informations d’un fichier partagé ;
- le téléchargement d’un fichier à partir d’un token.

---

## 1. URL de base

En environnement local :

```text
http://localhost:8080
```

Les endpoints de l’application utilisent le préfixe :

```text
/api
```

---

## 2. Format des échanges

Les échanges applicatifs utilisent principalement :

```text
Content-Type: application/json
```

L’upload de fichiers utilise :

```text
multipart/form-data
```

Le téléchargement retourne directement le contenu binaire du fichier.

---

## 3. Authentification

DataShare utilise une authentification JWT.

Après une connexion réussie, le backend retourne un token.

Exemple :

```json
{
  "token": "eyJ..."
}
```

Pour accéder aux routes protégées, le token doit être envoyé dans le header HTTP :

```text
Authorization: Bearer <JWT>
```

Les routes de gestion des fichiers nécessitent une authentification.

Les routes permettant de consulter ou télécharger un fichier partagé utilisent
le token de téléchargement présent dans l’URL.

---

# 4. Résumé des endpoints

| Méthode | Endpoint | Authentification | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Non | Créer un compte |
| POST | `/api/auth/login` | Non | Se connecter et obtenir un JWT |
| POST | `/api/files/upload` | JWT | Téléverser un fichier |
| GET | `/api/files` | JWT | Consulter son historique |
| DELETE | `/api/files/{id}` | JWT | Supprimer un de ses fichiers |
| GET | `/api/download/{token}` | Token de partage | Consulter les informations du fichier |
| GET | `/api/download/{token}/file` | Token de partage | Télécharger le fichier |

---

# 5. Authentification

## 5.1 Créer un compte

### Endpoint

```http
POST /api/auth/register
```

### Authentification

Aucune authentification requise.

### Content-Type

```text
application/json
```

### Corps de la requête

```json
{
  "email": "utilisateur@example.com",
  "password": "motdepasse"
}
```

### Champs

| Champ | Type | Obligatoire | Validation |
|---|---|---|---|
| `email` | String | Oui | Non vide et format email valide |
| `password` | String | Oui | Minimum 8 caractères |

### Réponse en cas de succès

```text
HTTP 201 Created
```

La réponse ne contient pas de corps.

### Principales erreurs

```text
400 Bad Request
```

Peut être retourné lorsque les données fournies ne respectent pas les règles
de validation.

Exemples :

- email vide ;
- email invalide ;
- mot de passe vide ;
- mot de passe inférieur à 8 caractères.

```text
409 Conflict
```

Retourné lorsqu’un compte existe déjà avec cette adresse email.

---

## 5.2 Se connecter

### Endpoint

```http
POST /api/auth/login
```

### Authentification

Aucune authentification requise.

### Content-Type

```text
application/json
```

### Corps de la requête

```json
{
  "email": "utilisateur@example.com",
  "password": "motdepasse"
}
```

### Champs

| Champ | Type | Obligatoire | Validation |
|---|---|---|---|
| `email` | String | Oui | Non vide et format email valide |
| `password` | String | Oui | Non vide |

### Réponse en cas de succès

```text
HTTP 200 OK
```

Exemple :

```json
{
  "token": "eyJ..."
}
```

Le champ `token` contient le JWT à utiliser pour les routes protégées.

### Principales erreurs

```text
400 Bad Request
```

Lorsque les données fournies sont invalides.

```text
401 Unauthorized
```

Lorsque l’email est inconnu ou que le mot de passe est incorrect.

---

# 6. Gestion des fichiers

Les endpoints de cette section nécessitent un JWT valide.

Header attendu :

```text
Authorization: Bearer <JWT>
```

---

## 6.1 Téléverser un fichier

### Endpoint

```http
POST /api/files/upload
```

### Authentification

JWT obligatoire.

### Content-Type

```text
multipart/form-data
```

### Paramètres

| Paramètre | Type | Obligatoire | Description |
|---|---|---|---|
| `file` | Fichier | Oui | Fichier à téléverser |
| `expirationDays` | Integer | Non | Durée d’expiration du partage |

`expirationDays` est géré par les règles métier du backend.

La durée maximale autorisée est de :

```text
7 jours
```

Si le paramètre n’est pas fourni, le backend applique sa durée d’expiration
par défaut.

### Contraintes

Taille maximale :

```text
1 Go
```

Extensions interdites :

```text
.exe
.bat
```

### Exemple avec curl

```bash
curl -X POST \
  http://localhost:8080/api/files/upload \
  -H "Authorization: Bearer <JWT>" \
  -F "file=@document.pdf" \
  -F "expirationDays=3"
```

### Réponse en cas de succès

```text
HTTP 201 Created
```

Exemple :

```json
{
  "id": 1,
  "originalName": "document.pdf",
  "size": 12345,
  "downloadToken": "token-de-partage",
  "expiresAt": "2026-09-16T15:30:00"
}
```

### Champs retournés

| Champ | Type | Description |
|---|---|---|
| `id` | Long | Identifiant du fichier |
| `originalName` | String | Nom d’origine du fichier |
| `size` | Long | Taille du fichier en octets |
| `downloadToken` | String | Token utilisé pour le partage |
| `expiresAt` | LocalDateTime | Date et heure d’expiration |

### Principales erreurs

```text
400 Bad Request
```

Exemples :

- fichier vide ;
- durée d’expiration invalide ;
- expiration supérieure à 7 jours.

```text
401 Unauthorized
```

Lorsque le JWT est absent ou invalide.

```text
413 Payload Too Large
```

Lorsque le fichier dépasse la taille maximale autorisée.

```text
415 Unsupported Media Type
```

Lorsque le fichier utilise une extension interdite comme `.exe` ou `.bat`.

---

## 6.2 Consulter son historique

### Endpoint

```http
GET /api/files
```

### Authentification

JWT obligatoire.

### Exemple

```bash
curl \
  http://localhost:8080/api/files \
  -H "Authorization: Bearer <JWT>"
```

### Réponse en cas de succès

```text
HTTP 200 OK
```

La réponse est un tableau JSON.

Exemple :

```json
[
  {
    "id": 1,
    "originalName": "document.pdf",
    "size": 12345,
    "contentType": "application/pdf",
    "downloadToken": "token-de-partage",
    "createdAt": "2026-09-13T15:30:00",
    "expiresAt": "2026-09-16T15:30:00"
  }
]
```

### Champs retournés

| Champ | Type | Description |
|---|---|---|
| `id` | Long | Identifiant du fichier |
| `originalName` | String | Nom original |
| `size` | Long | Taille en octets |
| `contentType` | String | Type MIME |
| `downloadToken` | String | Token de téléchargement |
| `createdAt` | LocalDateTime | Date de création |
| `expiresAt` | LocalDateTime | Date d’expiration |

Si l’utilisateur ne possède aucun fichier, l’API retourne un tableau vide :

```json
[]
```

### Principale erreur

```text
401 Unauthorized
```

Lorsque le JWT est absent ou invalide.

---

## 6.3 Supprimer un fichier

### Endpoint

```http
DELETE /api/files/{id}
```

### Authentification

JWT obligatoire.

### Paramètre de chemin

| Paramètre | Type | Description |
|---|---|---|
| `id` | Long | Identifiant du fichier à supprimer |

### Exemple

```bash
curl -X DELETE \
  http://localhost:8080/api/files/1 \
  -H "Authorization: Bearer <JWT>"
```

### Réponse en cas de succès

```text
HTTP 204 No Content
```

La réponse ne contient pas de corps.

Le backend vérifie que le fichier appartient à l’utilisateur authentifié.

Un utilisateur ne doit pas pouvoir supprimer un fichier appartenant
à un autre utilisateur.

---

# 7. Téléchargement par token

Les endpoints suivants permettent d’accéder à un fichier partagé
à partir de son token de téléchargement.

Ils ne nécessitent pas le JWT du propriétaire.

---

## 7.1 Consulter les informations du fichier

### Endpoint

```http
GET /api/download/{token}
```

### Paramètre de chemin

| Paramètre | Type | Description |
|---|---|---|
| `token` | String | Token de téléchargement du fichier |

### Exemple

```bash
curl http://localhost:8080/api/download/<TOKEN>
```

### Réponse en cas de succès

```text
HTTP 200 OK
```

Exemple :

```json
{
  "originalName": "document.pdf",
  "size": 12345,
  "contentType": "application/pdf",
  "expiresAt": "2026-09-16T15:30:00"
}
```

### Champs retournés

| Champ | Type | Description |
|---|---|---|
| `originalName` | String | Nom original du fichier |
| `size` | Long | Taille en octets |
| `contentType` | String | Type MIME |
| `expiresAt` | LocalDateTime | Date d’expiration |

### Principales erreurs

```text
404 Not Found
```

Lorsque le token n’existe pas.

```text
410 Gone
```

Lorsque le lien de téléchargement est expiré.

---

## 7.2 Télécharger le fichier

### Endpoint

```http
GET /api/download/{token}/file
```

### Paramètre de chemin

| Paramètre | Type | Description |
|---|---|---|
| `token` | String | Token de téléchargement |

### Exemple

```bash
curl -OJ \
  http://localhost:8080/api/download/<TOKEN>/file
```

### Réponse en cas de succès

```text
HTTP 200 OK
```

La réponse contient directement le fichier.

Le backend renseigne notamment :

```text
Content-Type
Content-Length
Content-Disposition
```

Le header `Content-Disposition` utilise le nom original du fichier et
indique au navigateur qu’il doit être téléchargé comme pièce jointe.

---

## Gestion du type MIME

Le backend utilise normalement le `contentType` enregistré pour le fichier.

Si cette valeur est absente ou invalide, la valeur de repli utilisée est :

```text
application/octet-stream
```

Cette protection évite qu’un type MIME mal formé bloque le téléchargement
d’un fichier valide.

---

## Principales erreurs

```text
404 Not Found
```

Lorsque le token n’existe pas ou que la ressource demandée est introuvable.

```text
410 Gone
```

Lorsque le token correspond à un fichier dont le lien a expiré.

---

# 8. Principaux codes HTTP utilisés

| Code | Signification | Exemple dans DataShare |
|---:|---|---|
| `200` | OK | Connexion, historique, téléchargement |
| `201` | Created | Inscription, upload |
| `204` | No Content | Suppression réussie |
| `400` | Bad Request | Données ou paramètres invalides |
| `401` | Unauthorized | Authentification absente ou incorrecte |
| `404` | Not Found | Token inconnu |
| `409` | Conflict | Email déjà utilisé |
| `410` | Gone | Lien expiré |
| `413` | Payload Too Large | Fichier supérieur à 1 Go |
| `415` | Unsupported Media Type | Extension de fichier interdite |

---

# 9. Exemple de parcours complet

## 1. Inscription

```http
POST /api/auth/register
```

```json
{
  "email": "utilisateur@example.com",
  "password": "motdepasse123"
}
```

Réponse :

```text
201 Created
```

---

## 2. Connexion

```http
POST /api/auth/login
```

```json
{
  "email": "utilisateur@example.com",
  "password": "motdepasse123"
}
```

Réponse :

```json
{
  "token": "eyJ..."
}
```

---

## 3. Upload

```http
POST /api/files/upload
Authorization: Bearer <JWT>
```

Réponse :

```json
{
  "id": 1,
  "originalName": "document.pdf",
  "size": 12345,
  "downloadToken": "token-de-partage",
  "expiresAt": "2026-09-16T15:30:00"
}
```

---

## 4. Historique

```http
GET /api/files
Authorization: Bearer <JWT>
```

---

## 5. Informations publiques du fichier

```http
GET /api/download/{token}
```

---

## 6. Téléchargement

```http
GET /api/download/{token}/file
```

---

## 7. Suppression

```http
DELETE /api/files/{id}
Authorization: Bearer <JWT>
```

Réponse :

```text
204 No Content
```

---

# 10. Architecture des appels

```text
Angular
   |
   | HTTP / JSON / JWT
   v
Controller Spring Boot
   |
   v
Service
   |
   v
Repository
   |
   +--> PostgreSQL
   |
   +--> Stockage local
```

Les contrôleurs définissent les endpoints HTTP.

Les services contiennent les règles métier.

Les repositories gèrent l’accès aux données PostgreSQL.

Les fichiers physiques sont conservés dans le stockage local de l’application.

---

# Conclusion

L’API DataShare repose sur une architecture REST simple.

Elle sépare :

- l’authentification ;
- la gestion des fichiers privés ;
- le téléchargement public par token.

Les routes protégées utilisent JWT tandis que les liens de partage utilisent
un token spécifique au fichier.

Les principaux cas d’erreur sont traités avec des codes HTTP explicites,
notamment pour les données invalides, les erreurs d’authentification,
les tokens inconnus, les liens expirés et les fichiers interdits.