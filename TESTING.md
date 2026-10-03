# Plan de tests - DataShare

## Objectif

La stratégie de tests de DataShare vise à vérifier les fonctionnalités critiques et à limiter les régressions avant livraison.

Elle couvre notamment :

- inscription ;
- connexion ;
- authentification JWT ;
- téléversement ;
- validation des fichiers ;
- protection des fichiers par mot de passe ;
- téléchargement ;
- expiration ;
- historique paginé ;
- suppression ;
- purge automatique ;
- rate limiting ;
- composants frontend ;
- parcours End-to-End.

---

## 1. Outils

### Backend

```text
JUnit 5
Mockito
MockMvc
Spring Boot Test
PostgreSQL
JaCoCo
SpotBugs
```

### Frontend

```text
Angular TestBed
Karma
Chrome
ESLint
Playwright
```

---

## 2. Tests backend

Commande :

```bash
cd ~/Projets/DataShare/backend
source ~/.config/datashare/env
./mvnw clean test
```

Dernière validation :

```text
Tests run: 58
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

---

## 3. Principaux tests backend

Les tests couvrent notamment :

### Authentification

```text
inscription
email déjà utilisé
connexion
mauvais identifiants
JWT
```

### Fichiers

```text
upload valide
fichier vide
extension interdite
faux PDF
PDF déguisé
archive ZIP valide
MP3 avec en-tête ID3 ou MPEG
MP4 avec en-tête ftyp
faux MP3, MP4 et ZIP
signature ZIP tronquée
archive ZIP déguisée en MP4
taille supérieure à 1 Go
expiration invalide
token inconnu
token expiré
```

### Protection par mot de passe

Les tests vérifient :

```text
hash BCrypt pendant l'upload
mot de passe correct
mot de passe incorrect
mot de passe absent
compatibilité avec un ancien fichier sans mot de passe
```

### Téléchargement

Les tests vérifient :

```text
métadonnées
Content-Type
Content-Disposition
fichier physique
mot de passe
fallback application/octet-stream
```

Le fallback est utilisé lorsqu'un Content-Type stocké est invalide.

### Autres fonctionnalités

```text
pagination
suppression
purge des fichiers expirés
rate limiting
Spring Security
contrôleurs
```

---

## 4. Tests frontend

Lint :

```bash
cd ~/Projets/DataShare/frontend
npx ng lint
```

Résultat :

```text
All files pass linting.
```

Tests unitaires :

```bash
npx ng test --watch=false
```

Dernier résultat :

```text
44 SUCCESS (3 octobre 2026)
```

Les tests couvrent notamment :

```text
AppComponent
AuthInterceptor
HomeComponent
LoginComponent
RegisterComponent
UploadComponent
DownloadComponent
HistoryComponent
AuthService
FileService
```

### Fonctionnalités récentes couvertes

Les tests unitaires vérifient notamment :

- l'affichage de la page d'accueil publique et son lien vers l'upload ;
- la déconnexion et la redirection vers la connexion ;
- le filtrage de l'historique : tous, actifs et expirés ;
- l'upload avec ou sans mot de passe ;
- la réinitialisation du formulaire d'upload ;
- la gestion d'un token JWT expiré (HTTP 401) ;
- la gestion des liens de téléchargement expirés (HTTP 410).

### Vérifications manuelles complémentaires

La présentation responsive doit également être contrôlée
dans le navigateur, notamment :

- affichage de la navigation sur mobile ;
- adaptation des pages aux différentes largeurs d'écran ;
- lisibilité du pied de page ;
- affichage de l'état d'expiration des fichiers.

Ces vérifications manuelles ne sont pas comptabilisées
dans les 44 tests Angular automatisés.

Le composant de téléchargement teste notamment :

```text
chargement des informations
token absent
404
410
mot de passe requis uniquement pour un fichier protégé
téléchargement réussi
mot de passe incorrect
erreurs de téléchargement
```

---

## 5. Tests End-to-End

Outil :

```text
Playwright
```

Le backend et le frontend doivent être démarrés avant l'exécution.

Commande :

```bash
cd ~/Projets/DataShare/frontend
npx playwright test
```

Dernier résultat :

```text
3 passed
```

Scénarios principaux :

### Parcours complet

```text
Inscription
→ Connexion
→ Upload
→ Mot de passe fichier
→ Historique
→ Page de téléchargement
→ Mauvais mot de passe
→ Bon mot de passe
→ Téléchargement réel
→ Suppression
```

### Authentification incorrecte

Le test vérifie le refus d'une connexion avec de mauvais identifiants.

### Token invalide

Le test vérifie l'affichage de l'erreur correspondant à un lien de téléchargement invalide.

---

## 6. Couverture JaCoCo

Commande :

```bash
cd ~/Projets/DataShare/backend
source ~/.config/datashare/env
./mvnw clean test jacoco:report
```

Rapport HTML :

```text
backend/target/site/jacoco/index.html
```

Dernière mesure :

```text
Instructions
91,64 %
1930 / 2106

Branches
72,31 %
175 / 242

Lignes
91,89 %
612 / 666
```

---

### Contrôle du seuil backend — 1er octobre 2026

Le fichier `backend/pom.xml` configure JaCoCo avec :

- élément contrôlé : `BUNDLE` ;
- compteur : `BRANCH` ;
- valeur : `COVEREDRATIO` ;
- minimum : `0.70` ;
- phase Maven : `verify`.

Le contrôle a été exécuté avec :

```bash
cd backend
source ~/.config/datashare/env
./mvnw clean verify
```

Résultat :

```text
49 tests réussis
Couverture branches : 70,48 % (117/166)
All coverage checks have been met.
SpotBugs : 0 bug, 0 erreur
BUILD SUCCESS
```

Classes nécessitant une attention particulière :

| Classe | Branches couvertes |
|---|---:|
| FileService | 64,71 % (66/102) |
| DownloadController | 62,50 % (5/8) |

`FileService` contient notamment les validations liées aux
fichiers et à leur protection. `DownloadController` traite
les réponses HTTP du téléchargement.

Le seuil global est respecté, mais ces classes restent
des cibles pertinentes pour de futurs tests de non-régression.

### Couverture frontend — 3 octobre 2026

Commande exécutée depuis `frontend/` :

```bash
npx ng test --watch=false --code-coverage
```

Résultat : 44/44 tests Angular réussis.

| Métrique | Couverture |
|---|---:|
| Statements | 68,86 % (188/273) |
| Branches | 54,87 % (45/82) |
| Functions | 64,51 % (40/62) |
| Lines | 68,63 % (186/271) |

Rapport HTML généré localement :

```text
frontend/coverage/frontend/index.html
```

La couverture frontend reste perfectible, notamment
sur les conditions alternatives (branches). Ces résultats
ne doivent pas être confondus avec ceux de JaCoCo,
qui concernent exclusivement le backend Java.


La campagne précédente du 1er octobre 2026 mesurait
67,95 % des instructions (statements) et 53,84 % des branches.
La couverture frontend n'est pas soumise au seuil JaCoCo
configuré pour le backend Java.

---

## 7. Interprétation de la couverture

La couverture indique quelles parties du code ont été exécutées pendant les tests.

Elle ne prouve pas :

```text
qu'il n'existe aucun bug
que toutes les règles métier sont correctes
que tous les cas possibles sont testés
que les assertions sont suffisantes
```

Elle doit être interprétée avec la qualité des scénarios de tests.

La couverture des branches est inférieure à celle des lignes car certaines conditions alternatives ne sont pas toutes parcourues.

---

## 8. SpotBugs

Commande :

```bash
cd ~/Projets/DataShare/backend
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

## 9. Build frontend

Commande :

```bash
cd ~/Projets/DataShare/frontend
npm run build
```

Le build Angular de production est généré dans :

```text
frontend/dist/frontend/
```

---

## 10. Tests de performance

k6 est utilisé pour tester les téléchargements du back-end.

### Campagne actualisée du 1er octobre 2026

Scénario :

- 10 utilisateurs virtuels (VUs) ;
- 20 itérations partagées ;
- téléchargement d'un fichier protégé par mot de passe ;
- endpoint `GET /api/download/{token}/file` ;
- mot de passe transmis avec `X-Download-Password`.

Résultats obtenus :

| Indicateur | Résultat |
|---|---:|
| Téléchargements HTTP 200 | 20/20 |
| Vérifications k6 | 40/40 |
| Échecs | 0 |
| p95 | 475,39 ms |

Les seuils `http_req_failed < 1 %` et `p95 < 1000 ms`
ont été respectés.

Le rate limiting a également été vérifié : 30 téléchargements
autorisés, puis une réponse HTTP 429 à la 31e tentative
avec le même token valide dans la fenêtre de limitation.

La campagne précédente (2 569 requêtes, p95 de 91,47 ms)
est conservée comme mesure historique dans `PERF.md`.
Les scénarios ne sont pas directement comparables.

Documentation détaillée : `PERF.md`.

---

## 11. Lighthouse

Nouvelle campagne du 1er octobre 2026, réalisée sur
le build Angular de production avec Lighthouse 13.4.0.

### Scores

| Catégorie | Mobile | Desktop |
|---|---:|---:|
| Performance | 91/100 | 100/100 |
| Accessibilité | 100/100 | 100/100 |
| Bonnes pratiques | 100/100 | 100/100 |
| SEO | 100/100 | 100/100 |

### Métriques

| Métrique | Mobile | Desktop |
|---|---:|---:|
| FCP | 2,7 s | 0,5 s |
| LCP | 2,9 s | 0,6 s |
| TBT (valeur JSON) | 14 ms | 0 ms |
| CLS | 0 | 0 |
| Speed Index | 2,7 s | 0,5 s |

La balise meta description est validée sur les deux profils.

Ces résultats correspondent à des mesures locales ponctuelles.

Documentation détaillée : `PERF.md`.

---

### Validation actualisée — 3 octobre 2026

Après l'ajout des formats MP3, MP4 et ZIP :

```text
58 tests backend réussis
0 échec
0 erreur

Instructions : 91,64 % (1930 / 2106)
Branches     : 72,31 % (175 / 242)
Lignes       : 91,89 % (612 / 666)

All coverage checks have been met.
SpotBugs : 0 bug, 0 erreur
BUILD SUCCESS
```

Neuf tests supplémentaires couvrent les signatures des
nouveaux formats et le rejet des fichiers déguisés.

La campagne du 1er octobre (49 tests) est conservée
ci-dessus comme résultat historique.

### Couverture actualisée des classes backend sensibles

Dernière campagne JaCoCo du 3 octobre 2026 :

| Classe | Branches couvertes |
|---|---:|
| FileService | 69,66 % (124/178) |
| DownloadController | 62,50 % (5/8) |

La couverture globale du backend atteint 72,31 %,
soit 175 branches couvertes sur 242.

FileService reste légèrement sous 70 % individuellement.
DownloadController conserve trois branches non couvertes.

Le seuil Maven de 70 % porte sur le bundle backend,
pas sur chacune de ses classes. Ces deux classes restent
des priorités pour les futurs tests de non-régression.

---

## 12. Non-régression finale

État validé :

```text
Backend JUnit
58 / 58 réussis

Frontend Angular
44 / 44 réussis (dernière exécution : 3 octobre 2026)

Playwright
3 / 3 réussis

ESLint
succès

SpotBugs
0 bug
0 erreur

JaCoCo lignes
91,89 %
```

Cette combinaison permet de vérifier :

- les règles métier ;
- l'authentification ;
- les contrôles d'accès ;
- PostgreSQL ;
- le stockage des fichiers ;
- les mots de passe fichiers ;
- les erreurs principales ;
- le frontend ;
- les principaux parcours utilisateurs.

---

## Critères d'acceptation fonctionnels

Cette matrice relie les fonctionnalités principales aux
vérifications réalisées. Les tests automatisés sont distingués
des contrôles manuels.

| Fonctionnalité | Critère d'acceptation | Vérification |
|---|---|---|
| Authentification | Un utilisateur peut créer un compte et se connecter | Tests AuthService et AuthIntegration |
| Accès privé | Les pages privées nécessitent une authentification | Guard Angular et tests d'intégration |
| Upload sans fichier | La soumission est refusée | Test UploadComponent |
| Upload sans mot de passe | Le fichier peut être envoyé sans protection optionnelle | Test UploadComponent |
| Upload protégé | Un mot de passe non vide de moins de 6 caractères est refusé | Test UploadComponent |
| Upload valide | Un fichier sélectionné est envoyé avec sa durée d'expiration | Tests frontend et backend |
| Limitation de débit | Le dépassement du quota est signalé par HTTP 429 | Tests backend et test UploadComponent |
| Historique | Les filtres Tous, Actifs et Expirés fonctionnent | Tests Angular et contrôle manuel |
| Lien de partage | Le fichier reste accessible par son token valide | Tests backend et parcours E2E |
| Suppression | Le propriétaire peut supprimer son fichier | Tests backend et contrôle manuel |
| Téléchargement protégé | Le mot de passe est vérifié côté serveur | Tests backend |
| Expiration | Un lien expiré est refusé | Tests backend |
| Interface responsive | Les écrans restent utilisables sur ordinateur et mobile | Comparaison manuelle avec Figma |

Les six tests unitaires spécifiques à `UploadComponent`
vérifient : absence de fichier, mot de passe trop court,
upload sans mot de passe, upload avec mot de passe valide,
réponse HTTP 429 et réinitialisation du formulaire.

Cette matrice décrit les contrôles existants ; elle ne
signifie pas que chaque critère dispose de son propre
test automatisé indépendant.

---

## 13. Limites

Même avec une couverture élevée, les tests automatisés ne remplacent pas :

```text
la revue de code
les tests manuels ciblés
l'analyse de sécurité
les tests de charge
la validation fonctionnelle
```

Une nouvelle fonctionnalité doit être accompagnée de tests adaptés et d'une vérification de non-régression.
