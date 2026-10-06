# Plan de tests - DataShare

## Objectif

La stratégie de tests de DataShare vise à vérifier les fonctionnalités critiques et à limiter les régressions avant livraison.

Elle couvre notamment :

- inscription et connexion ;
- authentification JWT ;
- téléversement ;
- validation du contenu des fichiers ;
- protection des fichiers par mot de passe ;
- téléchargement ;
- expiration ;
- historique et pagination ;
- suppression ;
- purge automatique ;
- limitation de débit ;
- composants Angular ;
- parcours End-to-End ;
- couverture du code ;
- analyse statique.

---

## 1. Outils utilisés

### Backend

```text
JUnit 5
Mockito
MockMvc
Spring Boot Test
JaCoCo
SpotBugs
```

La base PostgreSQL locale est également utilisée pour les vérifications nécessitant l'environnement de l'application.

### Frontend

```text
Angular TestBed
Karma
Chrome
ESLint
Playwright
```

---

# 2. Tests backend

## Commande

Depuis le dossier `backend` :

```bash
source ~/.config/datashare/env
./mvnw clean test
```

## Dernier résultat validé

```text
Tests run: 58
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

Les **58 tests backend** sont donc réussis.

---

# 3. Périmètre des tests backend

Les tests backend couvrent principalement les comportements suivants.

## Authentification

```text
inscription
email déjà utilisé
connexion
identifiants incorrects
JWT
routes protégées
```

## Téléversement

```text
upload valide
fichier vide
extension interdite
taille maximale
durée d'expiration
mot de passe facultatif
mot de passe invalide
```

## Validation des fichiers

Les formats pris en charge sont :

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

Le backend ne se contente pas de l'extension déclarée.

Des tests vérifient notamment :

```text
PDF valide
PDF déguisé
PNG valide
JPEG valide
MP3 avec signature valide
MP4 avec signature ftyp
ZIP valide
faux MP3
faux MP4
faux ZIP
signature tronquée
fichier déguisé sous une autre extension
```

Ces contrôles permettent de vérifier que le contenu réel du fichier correspond au type annoncé.

---

# 4. Protection par mot de passe

La protection d'un fichier partagé par mot de passe est facultative.

Les tests vérifient notamment :

```text
upload sans mot de passe
upload avec mot de passe
hash BCrypt du mot de passe
mot de passe correct au téléchargement
mot de passe incorrect
mot de passe absent pour un fichier protégé
fichier non protégé téléchargeable sans mot de passe
```

Le mot de passe n'est jamais conservé en clair.

Le backend stocke uniquement son hash BCrypt.

---

# 5. Téléchargement

Les tests du téléchargement vérifient notamment :

```text
token valide
token inconnu
lien expiré
fichier physique présent
Content-Type
Content-Length
Content-Disposition
mot de passe correct
mot de passe incorrect
absence de mot de passe
fallback application/octet-stream
```

Le fallback `application/octet-stream` est utilisé lorsque le type MIME stocké ne peut pas être utilisé correctement pour la réponse HTTP.

---

# 6. Historique, suppression et expiration

Les tests backend couvrent également :

```text
historique utilisateur
pagination
validation de page
validation de taille de page
suppression par le propriétaire
refus d'accès à une ressource appartenant à un autre utilisateur
expiration d'un partage
purge des fichiers expirés
```

L'utilisateur ne reçoit que les fichiers associés à son propre compte.

---

# 7. Limitation de débit

Le backend dispose de mécanismes de limitation de débit sur plusieurs endpoints sensibles.

Les tests vérifient notamment :

```text
connexion
téléversement
téléchargement
limitation par adresse IP
limitation par token
réponse HTTP 429
```

La limitation de débit complète les contrôles fonctionnels et de sécurité de l'application.

Les tests de charge associés sont détaillés dans `PERF.md`.

---

# 8. Tests frontend Angular

## Commande

Depuis le dossier `frontend` :

```bash
npx ng test --watch=false
```

## Dernier résultat validé

```text
Executed 53 of 53 SUCCESS
TOTAL: 53 SUCCESS
```

La dernière campagne Angular comporte donc :

```text
53 tests réussis
0 échec
```

---

# 9. Périmètre des tests frontend

Les tests Angular couvrent notamment :

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

Parmi les comportements vérifiés figurent :

- affichage des composants ;
- connexion et inscription ;
- gestion des erreurs HTTP ;
- gestion d'un JWT expiré ;
- upload avec ou sans mot de passe ;
- refus d'un mot de passe trop court ;
- réinitialisation du formulaire d'upload ;
- historique des fichiers ;
- filtres Tous, Actifs et Expirés ;
- liens expirés ;
- téléchargement protégé ;
- affichage des informations d'un fichier ;
- gestion des noms de fichiers très longs sur mobile.

---

# 10. Vérification des formats dans l'interface

Le formulaire Angular accepte les extensions :

```text
.txt
.pdf
.png
.jpg
.jpeg
.mp3
.mp4
.zip
```

Le texte d'aide affiché dans le formulaire est :

> Formats acceptés : TXT, PDF, PNG, JPG, JPEG, MP3, MP4 et ZIP — 1 Go maximum.

Cette vérification complète les contrôles de sécurité réalisés côté backend.

---

# 11. Noms de fichiers longs sur mobile

Une vérification spécifique a été ajoutée sur l'historique mobile.

Lorsqu'un nom dépasse la longueur prévue pour l'affichage, l'interface conserve :

- le début du nom ;
- une troncature centrale ;
- la fin du nom et son extension.

Exemple :

```text
Nom original :
rapport-projet-devops-version-finale-corrigee-avec-un-nom-vraiment-tres-long-jhvgvvyufvu.pdf

Affichage mobile :
rapport-projet-devops.....jhvgvvyufvu.pdf
```

Le nom réel du fichier n'est pas modifié.

Seule sa représentation visuelle est adaptée à l'affichage mobile.

---

# 12. Tests End-to-End

## Outil

```text
Playwright
```

Le backend et le frontend doivent être démarrés avant l'exécution.

## Commande

Depuis `frontend` :

```bash
npx playwright test
```

## Dernier résultat validé

```text
7 passed
```

La campagne End-to-End finale comporte donc :

```text
7 / 7 scénarios réussis
```

---

# 13. Parcours End-to-End vérifiés

Les parcours Playwright couvrent les chemins principaux de l'application après les modifications de l'interface.

Ils permettent notamment de vérifier les enchaînements entre :

```text
inscription
connexion
téléversement
historique
partage
page de téléchargement
protection par mot de passe
téléchargement
suppression
gestion des erreurs
```

L'objectif des tests E2E est de vérifier le comportement de l'application dans des conditions proches de l'utilisation réelle d'un utilisateur.

---

# 14. Couverture backend avec JaCoCo

## Commande

Depuis `backend` :

```bash
source ~/.config/datashare/env
./mvnw clean test jacoco:report
```

Le rapport HTML est généré dans :

```text
backend/target/site/jacoco/index.html
```

## Dernière mesure globale

| Métrique | Couverture |
|---|---:|
| Instructions | 91,64 % |
| Branches | 72,31 % |
| Lignes | 91,89 % |

Détail :

```text
Instructions : 1930 / 2106
Branches     : 175 / 242
Lignes       : 612 / 666
```

Le seuil global configuré dans Maven est respecté.

---

# 15. Couverture des classes backend sensibles

Une attention particulière est portée à `FileService` et `DownloadController`, car ces classes contiennent une partie importante de la logique de validation et de téléchargement.

## FileService

Dernière mesure :

```text
Instructions : environ 90,8 %
Lignes       : environ 91,0 %
Branches     : environ 69,7 %
Méthodes     : environ 92 %
```

La couverture des lignes de `FileService` est supérieure à 70 %.

## DownloadController

Dernière mesure :

```text
Instructions : environ 97,9 %
Lignes       : environ 97,6 %
Branches     : 62,5 %
Méthodes     : 100 %
```

La couverture des lignes de `DownloadController` est également largement supérieure à 70 %.

La couverture de branches peut être inférieure à la couverture de lignes, car elle mesure spécifiquement les différentes alternatives des conditions.

---

# 16. Couverture frontend Angular

## Commande

Depuis `frontend` :

```bash
npx ng test --watch=false --code-coverage
```

Le rapport HTML est généré dans :

```text
frontend/coverage/frontend/index.html
```

La dernière campagne de couverture validée a obtenu :

| Métrique | Couverture |
|---|---:|
| Statements | 78,02 % |
| Branches | 64,63 % |
| Functions | 70,96 % |
| Lines | 77,85 % |

La couverture des lignes frontend est donc supérieure à l'objectif de **70 %**.

La couverture des branches reste plus faible, car certaines alternatives conditionnelles ne sont pas exécutées par les scénarios actuels.

---

# 17. Interprétation de la couverture

La couverture indique quelles parties du code ont été exécutées pendant les tests.

Elle ne garantit pas :

```text
l'absence totale de bugs
la validité de toutes les règles métier
la couverture de tous les cas possibles
la qualité de toutes les assertions
```

Elle doit donc être interprétée avec les scénarios fonctionnels, les tests End-to-End, les contrôles manuels et la revue du code.

---

# 18. SpotBugs

## Commande

Depuis `backend` :

```bash
./mvnw spotbugs:check
```

## Dernier résultat validé

```text
BugInstance size is 0
Error size is 0
BUILD SUCCESS
```

Aucun bug SpotBugs n'est détecté dans la dernière campagne conservée.

---

# 19. ESLint

Depuis le dossier `frontend` :

```bash
npx ng lint
```

La vérification ESLint complète les tests Angular afin de détecter des problèmes de qualité ou de syntaxe dans le code frontend.

---

# 20. Build frontend

Le build Angular de production est exécuté avec :

```bash
cd ~/Projets/DataShare/frontend
npm run build
```

Le résultat est généré dans :

```text
frontend/dist/frontend/
```

Les budgets de performance du build sont documentés dans `PERF.md`.

---

# 21. Tests de performance

Les tests de performance ne sont pas détaillés dans ce document afin d'éviter les doublons.

La campagne finale est documentée dans :

```text
PERF.md
```

Elle comprend notamment :

- une charge soutenue sur plusieurs liens de téléchargement ;
- un scénario sans vérification BCrypt ;
- un scénario avec BCrypt coût 12 ;
- une comparaison du coût de la vérification du mot de passe ;
- les mesures Lighthouse ;
- les budgets Angular.

Lors de la campagne k6 finale :

```text
31 requêtes sans BCrypt
31 requêtes avec BCrypt
0 % d'erreurs HTTP dans les deux scénarios
```

Le détail des latences et de leur interprétation reste centralisé dans `PERF.md`.

---

# 22. Vérifications manuelles de l'interface

Les tests automatisés sont complétés par des contrôles visuels dans le navigateur.

Les vérifications comprennent notamment :

```text
navigation desktop
navigation mobile
responsive
historique
menu d'actions mobile
fichiers protégés
texte des formats acceptés
noms de fichiers très longs
expiration
upload
téléchargement
```

La comparaison avec les maquettes est documentée dans :

```text
FIGMA_CONFORMITE.md
```

---

# 23. Synthèse de la dernière campagne

| Type de contrôle | Résultat |
|---|---:|
| Backend JUnit | 58 / 58 réussis |
| Frontend Angular | 53 / 53 réussis |
| Playwright | 7 / 7 réussis |
| JaCoCo lignes backend | 91,89 % |
| Couverture lignes frontend | 77,85 % |
| FileService lignes | environ 91 % |
| DownloadController lignes | environ 97,6 % |
| SpotBugs | 0 bug |
| Tests de charge | validés, voir `PERF.md` |

Les différentes suites ne doivent pas être additionnées sans préciser leur nature.

Ainsi :

```text
58 tests backend
53 tests frontend unitaires
7 scénarios End-to-End
```

correspondent à trois catégories de tests différentes.

---

# 24. Critères d'acceptation fonctionnels

| Fonctionnalité | Critère d'acceptation | Vérification |
|---|---|---|
| Authentification | Un utilisateur peut créer un compte et se connecter | Tests backend, frontend et E2E |
| Accès privé | Les pages privées nécessitent une authentification | Tests Spring Security et Angular |
| Upload sans fichier | La soumission est refusée | Tests Angular |
| Upload sans mot de passe | Un fichier peut être partagé sans protection | Tests frontend et backend |
| Upload protégé | Le mot de passe doit respecter les règles définies | Tests frontend et backend |
| Formats | Seuls les formats autorisés sont acceptés | Tests backend et interface Angular |
| Faux formats | Un fichier déguisé est refusé | Tests backend |
| Historique | Les fichiers de l'utilisateur sont affichés | Tests backend, frontend et E2E |
| Filtres | Tous, Actifs et Expirés fonctionnent | Tests Angular |
| Noms longs | L'affichage mobile reste lisible | Test Angular et contrôle visuel |
| Téléchargement public | Un token valide permet l'accès au partage | Tests backend et E2E |
| Téléchargement protégé | Le mot de passe est vérifié côté serveur | Tests backend et E2E |
| Expiration | Un lien expiré est refusé | Tests backend et frontend |
| Suppression | Le propriétaire peut supprimer son fichier | Tests backend et E2E |
| Pagination | Les paramètres sont contrôlés | Tests backend |
| Rate limiting | Le dépassement du quota retourne HTTP 429 | Tests backend et performance |
| Responsive | L'application reste utilisable sur mobile | Contrôle visuel et comparaison Figma |

---

# 25. Limites

Même avec une couverture importante, les tests automatisés ne remplacent pas :

```text
la revue de code
les tests manuels ciblés
l'analyse de sécurité
les tests de charge
la validation fonctionnelle
```

Les résultats présentés dans ce document correspondent aux campagnes finales conservées pour la livraison.

Toute nouvelle fonctionnalité devra être accompagnée de tests adaptés et d'une nouvelle vérification de non-régression.
