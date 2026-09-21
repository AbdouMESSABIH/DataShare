# DataShare — Contrat d’interface API

## 1. Objectif

Ce document décrit le contrat d’interface entre le **front-end Angular** et le **back-end Spring Boot** pour le MVP DataShare.

Le contrat précise :
- les endpoints REST ;
- les méthodes HTTP ;
- les données envoyées ;
- les réponses attendues ;
- les besoins d’authentification ;
- les principaux codes HTTP.

> Les noms exacts des routes ne sont pas imposés par les spécifications. Les routes ci-dessous sont le choix retenu pour le projet.

---

## 2. Endpoints principaux

| Méthode | Endpoint | Fonction | Authentification |
|---|---|---|---|
| POST | `/api/auth/register` | Créer un compte utilisateur | Non |
| POST | `/api/auth/login` | Se connecter | Non |
| POST | `/api/files/upload` | Envoyer un fichier | JWT |
| GET | `/api/files` | Consulter l’historique de ses fichiers | JWT |
| DELETE | `/api/files/{id}` | Supprimer un de ses fichiers | JWT |
| GET | `/api/download/{token}` | Consulter les métadonnées d’un fichier partagé | Non |
| GET | `/api/download/{token}/file` | Télécharger le fichier | Non |

---

## 3. Création de compte

### Requête

`POST /api/auth/register`

```json
{
  "email": "user@mail.com",
  "password": "motdepasse123"
}
```

### Règles principales

- email valide ;
- email unique ;
- mot de passe d’au moins 8 caractères ;
- mot de passe stocké sous forme hashée.

### Réponse — 201 Created

```json
{
  "id": 1,
  "email": "user@mail.com"
}
```

### Erreurs possibles

- `400 Bad Request` : données invalides ;
- `409 Conflict` : email déjà utilisé.

---

## 4. Connexion

### Requête

`POST /api/auth/login`

```json
{
  "email": "user@mail.com",
  "password": "motdepasse123"
}
```

### Réponse — 200 OK

```json
{
  "token": "eyJhbGciOi..."
}
```

Le token JWT est ensuite envoyé par Angular dans les requêtes protégées.

### Erreurs possibles

- `400 Bad Request` : format invalide ;
- `401 Unauthorized` : identifiants incorrects.

---

## 5. Upload d’un fichier

### Requête

`POST /api/files`

Authentification JWT obligatoire.

La requête contient notamment :

```text
file = document.pdf
expirationDays = 7
```

### Règles principales

- utilisateur connecté ;
- taille maximale : 1 Go ;
- type de fichier autorisé ;
- expiration maximale : 7 jours ;
- génération d’un token de téléchargement unique et non prédictible.

### Réponse — 201 Created

```json
{
  "id": 12,
  "fileName": "document.pdf",
  "downloadToken": "x8F2a91K",
  "expiresAt": "2026-09-16T18:00:00"
}
```

### Erreurs possibles

- `400 Bad Request` : fichier invalide ou paramètres incorrects ;
- `401 Unauthorized` : utilisateur non authentifié.

---

## 6. Consultation de l’historique

### Requête

`GET /api/files?page=0&size=10`

Authentification JWT obligatoire.

Paramètres :

- `page` : numéro de page à partir de 0 ;
- `size` : nombre d'éléments par page, de 1 à 50 ;
- valeurs par défaut : `page=0` et `size=10`.

### Réponse - 200 OK

```json
{
  "content": [
    {
      "id": 12,
      "originalName": "document.pdf",
      "size": 2048000,
      "contentType": "application/pdf",
      "downloadToken": "x8F2a91K",
      "createdAt": "2026-09-09T18:00:00",
      "expiresAt": "2026-09-16T18:00:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

L'utilisateur ne reçoit que les fichiers qui lui appartiennent.


---

## 7. Suppression d’un fichier

### Requête

`DELETE /api/files/{id}`

Exemple :

`DELETE /api/files/12`

Authentification JWT obligatoire.

### Règles principales

- le fichier doit exister ;
- l’utilisateur connecté doit être propriétaire du fichier ;
- la suppression retire le fichier physique et ses métadonnées ;
- la suppression est irréversible.

### Réponse

`204 No Content`

### Erreurs possibles

- `401 Unauthorized` : utilisateur non authentifié ;
- `403 Forbidden` : fichier appartenant à un autre utilisateur ;
- `404 Not Found` : fichier inexistant.

---

## 8. Consultation d’un lien de téléchargement

### Requête

`GET /api/download/{token}`

Exemple :

`GET /api/download/x8F2a91K`

### Réponse — 200 OK

```json
{
  "fileName": "document.pdf",
  "size": 2048000,
  "type": "application/pdf",
  "expiresAt": "2026-09-16T18:00:00"
}
```

### Règles principales

- le token doit être valide ;
- le lien ne doit pas être expiré ;
- les métadonnées sont affichées avant le téléchargement.

### Erreurs possibles

- `404 Not Found` : token invalide ;
- `410 Gone` : lien expiré.

---

## 9. Téléchargement du fichier

### Requête

`GET /api/download/{token}/file`

Exemple :

`GET /api/download/x8F2a91K/file`

### Réponse

Le back-end renvoie le fichier physique au client.

### Erreurs possibles

- `404 Not Found` : fichier ou token inexistant ;
- `410 Gone` : lien expiré.

---

## 10. Codes HTTP principaux

| Code | Signification |
|---|---|
| 200 | Requête réussie |
| 201 | Ressource créée avec succès |
| 204 | Action réussie sans contenu à renvoyer |
| 400 | Requête invalide |
| 401 | Utilisateur non authentifié |
| 403 | Action interdite |
| 404 | Ressource introuvable |
| 409 | Conflit, par exemple email déjà utilisé |
| 410 | Ressource expirée / plus disponible |
| 500 | Erreur interne du serveur |

---

## 11. Résumé du flux

```text
Angular
   ↓
API REST / JSON / HTTPS
   ↓
Spring Boot
   ↓
PostgreSQL + stockage local
```

Pour les routes protégées, Angular envoie le JWT au back-end afin que Spring Boot identifie l’utilisateur connecté.
