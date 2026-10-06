# DataShare — Contrat d’interface API

## 1. Objectif

Ce document décrit le contrat principal entre :

- le front-end Angular ;
- le back-end Spring Boot.

Préfixe de l’API :

```text
/api
```

L’API permet :

- l’inscription ;
- la connexion ;
- le téléversement ;
- la consultation de l’historique ;
- la suppression ;
- la consultation d’un partage ;
- le téléchargement d’un fichier.

---

# 2. Authentification

DataShare utilise une authentification JWT.

Après une connexion réussie, le backend retourne un token JWT.

Pour les routes protégées, Angular envoie :

```text
Authorization: Bearer <token-jwt>
```

## Routes publiques

```text
POST /api/auth/register
POST /api/auth/login

GET /api/download/{token}
GET /api/download/{token}/file
```

## Routes protégées

```text
POST   /api/files/upload
GET    /api/files
DELETE /api/files/{id}
```

---

# 3. Résumé des endpoints

| Méthode | Endpoint | Fonction | Authentification |
|---|---|---|---|
| POST | `/api/auth/register` | Créer un compte | Non |
| POST | `/api/auth/login` | Se connecter | Non |
| POST | `/api/files/upload` | Téléverser un fichier | JWT |
| GET | `/api/files` | Consulter ses fichiers | JWT |
| DELETE | `/api/files/{id}` | Supprimer un fichier | JWT |
| GET | `/api/download/{token}` | Consulter un partage | Non |
| GET | `/api/download/{token}/file` | Télécharger le fichier | Non |

---

# 4. Inscription

## Endpoint

```text
POST /api/auth/register
```

## Requête

```json
{
  "email": "user@mail.com",
  "password": "motdepasse123"
}
```

Contraintes principales :

- email obligatoire ;
- format email valide ;
- email unique ;
- mot de passe obligatoire ;
- minimum 8 caractères.

Le mot de passe utilisateur est hashé avant son stockage.

## Réponse réussie

```text
201 Created
```

## Erreurs principales

```text
400 Bad Request
409 Conflict
```

`409 Conflict` est notamment utilisé lorsqu’un email existe déjà.

---

# 5. Connexion

## Endpoint

```text
POST /api/auth/login
```

## Requête

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

## Erreurs principales

```text
400 Bad Request
401 Unauthorized
429 Too Many Requests
```

---

# 6. Téléversement d’un fichier

## Endpoint

```text
POST /api/files/upload
```

JWT obligatoire :

```text
Authorization: Bearer <token-jwt>
```

Type de contenu :

```text
multipart/form-data
```

## Champs

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

## Formats acceptés

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

Taille maximale :

```text
1 Go
```

Le backend contrôle également le contenu réel du fichier afin de vérifier sa cohérence avec le format annoncé.

Un simple changement d’extension ne suffit donc pas à rendre un fichier valide.

---

## Expiration

Valeurs autorisées :

```text
1 à 7 jours
```

Valeur par défaut :

```text
7 jours
```

---

## Mot de passe du fichier

Le mot de passe de partage est facultatif.

Lorsqu’il est renseigné :

```text
minimum 6 caractères
```

Le mot de passe n’est jamais stocké en clair.

Il est hashé avec BCrypt avant d’être enregistré.

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

## Erreurs principales

```text
400 Bad Request
401 Unauthorized
413 Payload Too Large
415 Unsupported Media Type
429 Too Many Requests
500 Internal Server Error
```

---

# 7. Historique des fichiers

## Endpoint

```text
GET /api/files
```

Exemple :

```text
GET /api/files?page=0&size=10
```

JWT obligatoire.

## Pagination

| Paramètre | Valeur |
|---|---|
| `page` | entier >= 0 |
| `size` | entre 1 et 50 |
| page par défaut | 0 |
| taille par défaut | 10 |

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

L’utilisateur reçoit uniquement ses propres fichiers.

## Erreurs principales

```text
400 Bad Request
401 Unauthorized
```

---

# 8. Suppression d’un fichier

## Endpoint

```text
DELETE /api/files/{id}
```

JWT obligatoire.

Le backend recherche le fichier en fonction :

```text
id du fichier
+
utilisateur connecté
```

Un utilisateur ne peut donc supprimer que ses propres fichiers.

La suppression concerne :

```text
fichier physique
+
métadonnées PostgreSQL
```

## Réponse réussie

```text
204 No Content
```

## Erreurs principales

```text
401 Unauthorized
404 Not Found
500 Internal Server Error
```

---

# 9. Consultation d’un partage

## Endpoint

```text
GET /api/download/{token}
```

Cette route est publique.

Elle permet de récupérer les informations du partage avant le téléchargement.

Le mot de passe n’est pas nécessaire pour consulter ces métadonnées.

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

## Erreurs principales

```text
404 Not Found
410 Gone
```

`410 Gone` indique que le lien a expiré mais que ses métadonnées existent encore.

Après suppression des métadonnées par la purge, le token retourne :

```text
404 Not Found
```

---

# 10. Téléchargement

## Endpoint

```text
GET /api/download/{token}/file
```

Cette route est publique.

Le téléchargement nécessite :

- un token valide ;
- un lien non expiré ;
- un mot de passe correct si le fichier est protégé.

---

## Transmission du mot de passe

Pour un fichier protégé :

```text
X-Download-Password: Secret123!
```

Le mot de passe n’est pas placé dans l’URL.

Le backend le compare au hash BCrypt enregistré.

### Mot de passe correct

```text
200 OK
```

### Mot de passe absent ou incorrect

```text
403 Forbidden
```

### Fichier non protégé

Aucun en-tête `X-Download-Password` n’est nécessaire.

---

## Réponse réussie

```text
200 OK
```

Le serveur retourne notamment :

```text
Content-Type
Content-Length
Content-Disposition
```

Exemple :

```text
Content-Disposition: attachment; filename="document.pdf"
```

## Erreurs principales

```text
403 Forbidden
404 Not Found
410 Gone
429 Too Many Requests
```

---

# 11. Validation des fichiers

| Extension | Type MIME attendu |
|---|---|
| TXT | `text/plain` |
| PDF | `application/pdf` |
| PNG | `image/png` |
| JPG / JPEG | `image/jpeg` |
| MP3 | `audio/mpeg` |
| MP4 | `video/mp4` |
| ZIP | `application/zip` |

Le backend contrôle l’extension et analyse le contenu du fichier.

Les contrôles portent notamment sur :

```text
PDF
PNG
JPEG
MP3
MP4
ZIP
TXT UTF-8
```

Une incohérence entre le fichier et son format entraîne :

```text
415 Unsupported Media Type
```

Ces contrôles ne constituent pas une analyse antivirus complète.

Voir `SECURITY.md` pour les détails de sécurité.

---

# 12. Expiration et purge

Chaque partage possède une date d’expiration.

Durée :

```text
1 à 7 jours
```

Valeur par défaut :

```text
7 jours
```

Lorsqu’un partage est expiré mais encore présent en base :

```text
410 Gone
```

Après purge de ses métadonnées :

```text
404 Not Found
```

Une purge planifiée supprime les fichiers expirés.

---

# 13. Limitation de débit

Le backend applique plusieurs quotas.

| Endpoint | Limitation |
|---|---:|
| `POST /api/auth/login` | 10 requêtes/minute/IP |
| `POST /api/files/upload` | 20 requêtes/minute/IP |
| `GET /api/download/{token}/file` | 60 requêtes/minute/IP |
| `GET /api/download/{token}/file` | 30 requêtes/minute/token |

En cas de dépassement :

```text
429 Too Many Requests
```

Les compteurs sont actuellement conservés en mémoire.

Dans un déploiement multi-instance, un stockage partagé des compteurs serait nécessaire.

Les tests de performance et l’impact de BCrypt sont détaillés dans `PERF.md`.

---

# 14. Codes HTTP principaux

| Code | Signification |
|---|---|
| `200` | Requête réussie |
| `201` | Ressource créée |
| `204` | Suppression réussie |
| `400` | Requête ou paramètre invalide |
| `401` | Authentification absente ou invalide |
| `403` | Accès refusé ou mot de passe fichier incorrect |
| `404` | Ressource ou token introuvable |
| `409` | Conflit, notamment email déjà utilisé |
| `410` | Lien expiré |
| `413` | Fichier trop volumineux |
| `415` | Format ou contenu non autorisé |
| `429` | Limitation de débit dépassée |
| `500` | Erreur interne |

---

# 15. Parcours principal

```text
Utilisateur
    │
    ▼
Connexion
    │
    ▼
JWT
    │
    ▼
Téléversement
    │
    ▼
Contrôle du fichier
    │
    ▼
Stockage physique + PostgreSQL
    │
    ▼
Token de partage
    │
    ▼
/download/{token}
    │
    ▼
Téléchargement
```

Le parcours détaillé est :

1. l’utilisateur s’authentifie ;
2. le backend retourne un JWT ;
3. Angular envoie le fichier à `/api/files/upload` ;
4. le backend contrôle le fichier, l’expiration et le mot de passe ;
5. le fichier est stocké ;
6. ses métadonnées sont enregistrées dans PostgreSQL ;
7. un token de téléchargement est généré ;
8. le partage est consultable via `/api/download/{token}` ;
9. le téléchargement utilise `/api/download/{token}/file` ;
10. `X-Download-Password` est envoyé uniquement si le fichier est protégé.

---

# 16. Documents complémentaires

| Document | Contenu |
|---|---|
| `README.md` | Installation et architecture |
| `SECURITY.md` | Sécurité et contrôles |
| `TESTING.md` | Tests et couverture |
| `PERF.md` | Performance et charge |
| `FIGMA_CONFORMITE.md` | Conformité de l’interface |
| `MAINTENANCE.md` | Maintenance |

Ce document décrit le contrat API utile à la compréhension et à l’utilisation de DataShare.

Les détails de tests, de performance et de sécurité sont volontairement placés dans leurs documents respectifs afin d’éviter les répétitions.
