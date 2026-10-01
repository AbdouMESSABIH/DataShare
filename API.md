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
      "expiresAt": "2026-10-06T01:40:00"
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
  "expiresAt": "2026-10-06T01:40:00"
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

Le token est invalide ou le fichier physique n’existe plus.

### 410 Gone

Le lien de téléchargement a expiré.

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

Le token est invalide ou le fichier physique n’existe plus.

### 410 Gone

Le lien est expiré.

### 429 Too Many Requests

Le quota de téléchargement par IP ou par token
est dépassé. Voir la section 15.

---

# 11. Protection des fichiers par mot de passe

Lors du téléversement :

```text
Utilisateur
    │
    │ mot de passe
    ▼
Spring Boot
    │
    ▼
BCrypt
    │
    ▼
Hash
    │
    ▼
PostgreSQL
```

Le mot de passe original n’est jamais enregistré directement.

Exemple :

```text
Secret123!
```

n’est pas stocké tel quel dans la base de données.

Une valeur hashée est enregistrée à la place.

Exemple conceptuel :

```text
$2a$10$...
```

Lors du téléchargement :

```text
Mot de passe saisi
        │
        ▼
X-Download-Password
        │
        ▼
Spring Boot
        │
        ▼
BCrypt.matches(...)
        │
        ├── faux
        │     ↓
        │    403
        │
        └── vrai
              ↓
        téléchargement
```

---

# 12. Expiration des fichiers

Chaque fichier possède notamment :

```text
createdAt
expiresAt
```

La durée maximale de partage est :

```text
7 jours
```

Lorsqu’un lien est expiré, l’API retourne :

```text
410 Gone
```

Les fichiers expirés peuvent ensuite être supprimés automatiquement par le service de nettoyage prévu dans le back-end.

---

# 13. Pagination

L’historique des fichiers utilise une pagination côté serveur.

Exemple :

```text
GET /api/files?page=0&size=10
```

Contraintes :

```text
page >= 0
1 <= size <= 50
```

Cette pagination évite de charger l’intégralité de l’historique d’un utilisateur dans une seule réponse.

---

# 14. Validation des fichiers

DataShare contrôle plusieurs caractéristiques avant d’accepter un fichier :

```text
taille
extension
contenu réel
```

Formats actuellement acceptés :

```text
TXT
PDF
PNG
JPG
JPEG
```

Le serveur vérifie que le contenu réel est cohérent avec le format attendu.

Par exemple, un fichier nommé :

```text
document.pdf
```

doit réellement correspondre à un fichier PDF valide.

Si le fichier n’est pas conforme, la requête peut être refusée avec :

```text
415 Unsupported Media Type
```

---

# 15. Limitation de débit

Le backend applique plusieurs limitations de débit afin
de réduire les abus sur les endpoints sensibles.

## Connexion

Endpoint :

```text
POST /api/auth/login
```

Limite :

```text
10 requêtes par minute et par adresse IP
```

## Téléversement

Endpoint :

```text
POST /api/files/upload
```

Limite :

```text
20 requêtes par minute et par adresse IP
```

## Téléchargement réel

Endpoint :

```text
GET /api/download/{token}/file
```

Limites documentées :

```text
60 téléchargements par minute et par adresse IP
30 téléchargements par minute et par token
```

Les limites de téléchargement sont complémentaires :
le dépassement d'un quota entraîne un refus HTTP 429.

Une vérification fonctionnelle réalisée le 1er octobre 2026
a confirmé le comportement suivant avec un même token valide :

| Tentatives | Résultat |
|---|---|
| 1 à 30 | HTTP 200 |
| 31e tentative | HTTP 429 |

La campagne de validation concerne le téléchargement réel
du fichier, et non une mesure de charge de l'endpoint
de consultation des métadonnées.

## Réponse en cas de dépassement

Code HTTP :

```text
429 Too Many Requests
```

La limitation de connexion et de téléversement est
documentée avec l'en-tête :

```text
Retry-After: 60
```

Le mécanisme de limitation est conservé en mémoire dans
l'instance Spring Boot actuelle.

Cette architecture convient au MVP mono-instance.
Une architecture multi-instance nécessiterait un mécanisme
de comptage partagé, par exemple avec Redis.

Les mesures et limites détaillées sont disponibles
dans `PERF.md` et `SECURITY.md`.

---

# 16. Codes HTTP principaux

| Code | Signification dans DataShare |
|---|---|
| `200 OK` | Requête réussie |
| `201 Created` | Ressource créée avec succès |
| `204 No Content` | Suppression réussie sans contenu à retourner |
| `400 Bad Request` | Requête ou paramètres invalides |
| `401 Unauthorized` | Authentification absente ou identifiants incorrects |
| `403 Forbidden` | Mot de passe de téléchargement absent ou incorrect |
| `404 Not Found` | Ressource, fichier ou token introuvable |
| `409 Conflict` | Conflit, par exemple une adresse email déjà utilisée |
| `410 Gone` | Lien de téléchargement expiré |
| `413 Payload Too Large` | Fichier trop volumineux |
| `415 Unsupported Media Type` | Type ou contenu de fichier non autorisé |
| `429 Too Many Requests` | Limite de requêtes dépassée |
| `500 Internal Server Error` | Erreur interne du serveur |

---

# 17. Flux principal de partage

```text
UTILISATEUR CONNECTÉ
        │
        ▼
Frontend Angular
        │
        │ JWT
        ▼
POST /api/files/upload
        │
        ├── file
        ├── expirationDays
        └── password
                │
                ▼
Spring Boot
        │
        ├── vérification du fichier
        ├── génération du token
        ├── calcul de l’expiration
        ├── hash BCrypt du mot de passe
        └── enregistrement du fichier
                │
                ▼
PostgreSQL + stockage local
                │
                ▼
downloadToken
                │
                ▼
Lien de partage
                │
                ▼
/download/{token}
                │
                ▼
GET /api/download/{token}
                │
                ▼
Informations du fichier
                │
                ▼
Utilisateur saisit le mot de passe
                │
                ▼
GET /api/download/{token}/file
                │
                │ X-Download-Password
                ▼
Spring Boot
        │
        ├── mot de passe incorrect
        │          │
        │          ▼
        │         403
        │
        └── mot de passe correct
                   │
                   ▼
                  200
                   │
                   ▼
            téléchargement
```

---

# 18. Architecture simplifiée

```text
┌─────────────────────┐
│     UTILISATEUR     │
│  Navigateur Web     │
└──────────┬──────────┘
           │
           │ utilise
           ▼
┌─────────────────────┐
│      FRONT-END      │
│       Angular       │
│                     │
│ Pages / Composants  │
│ Services HTTP       │
└──────────┬──────────┘
           │
           │ API REST
           │ JSON / HTTP(S)
           │ JWT
           ▼
┌─────────────────────┐
│      BACK-END       │
│    Spring Boot      │
│                     │
│ Controllers         │
│ Services            │
│ Spring Security     │
│ JPA / Hibernate     │
│ BCrypt              │
└──────────┬──────────┘
           │
           ├──────────────────────┐
           │                      │
           ▼                      ▼
┌─────────────────────┐  ┌─────────────────────┐
│     PostgreSQL      │  │   Stockage local    │
│                     │  │      uploads/       │
│ Utilisateurs        │  │                     │
│ Métadonnées fichiers│  │ Fichiers physiques  │
│ Hash mots de passe  │  │                     │
└─────────────────────┘  └─────────────────────┘
```

---

# 19. Points de sécurité principaux

DataShare applique notamment les mesures suivantes :

```text
Authentification JWT
Hash des mots de passe utilisateurs
Hash BCrypt des mots de passe fichiers
Validation des fichiers
Contrôle de leur contenu réel
Expiration des liens
Tokens de téléchargement
Contrôle du propriétaire lors de la suppression
Limitation de débit
```

Le mot de passe protégeant un fichier n’est pas transmis dans l’URL.

Il est transmis dans :

```text
X-Download-Password
```

et comparé au hash enregistré dans la base.

---

# 20. État actuel du contrat API

Ce document correspond à l’implémentation actuelle du projet DataShare.

Les principaux éléments documentés sont :

- `POST /api/auth/register` pour l’inscription ;
- `POST /api/auth/login` pour la connexion ;
- JWT pour les routes protégées ;
- `POST /api/files/upload` pour le téléversement ;
- champs `file`, `expirationDays` et `password` ;
- réponse d’upload avec `id`, `originalName`, `size`, `downloadToken` et `expiresAt` ;
- historique paginé avec `GET /api/files` ;
- suppression avec `DELETE /api/files/{id}` ;
- contrôle du propriétaire du fichier ;
- métadonnées publiques avec `GET /api/download/{token}` ;
- téléchargement avec `GET /api/download/{token}/file` ;
- protection des fichiers par mot de passe ;
- hash BCrypt du mot de passe ;
- header `X-Download-Password` ;
- `403` pour un mot de passe absent ou incorrect ;
- `404` lorsqu’une ressource est introuvable ;
- `410` lorsqu’un lien est expiré ;
- `429` lorsque la limite de requêtes est dépassée ;
- validation du type et du contenu des fichiers ;
- taille maximale de 1 Go ;
- expiration maximale de 7 jours ;
- stockage des métadonnées dans PostgreSQL ;
- stockage physique des fichiers côté serveur.
