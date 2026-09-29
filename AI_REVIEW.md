# Revue technique du code développé avec l'assistance de l'IA - DataShare

## 1. Objectif

Ce document présente une revue technique du code associé à la User Story développée avec l'assistance d'une IA.

L'objectif est de montrer que le code proposé n'a pas été intégré sans contrôle.

La démarche appliquée est :

```text
proposition
→ relecture
→ test
→ identification des risques
→ correction
→ test de non-régression
```

---

## 2. User Story concernée

La fonctionnalité concernée est :

**Télécharger un fichier partagé à partir de son token.**

Cette fonctionnalité permet notamment :

- de rechercher le fichier grâce au token ;
- de contrôler son existence ;
- de contrôler son expiration ;
- de vérifier le fichier physique ;
- de retourner les métadonnées ;
- de construire la réponse HTTP ;
- de télécharger le fichier.

---

## 3. Architecture revue

Le backend respecte l'organisation :

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL / stockage local
```

Responsabilités vérifiées :

### Controller

```text
requête HTTP
headers
réponse HTTP
Content-Type
Content-Disposition
```

### Service

```text
règles métier
token
expiration
mot de passe fichier
accès au fichier
```

### Repository

```text
recherche des métadonnées
persistance
```

---

## 4. Points vérifiés

La revue a porté notamment sur :

- codes HTTP ;
- token valide ;
- token invalide ;
- token expiré ;
- existence du fichier ;
- type MIME ;
- Content-Disposition ;
- taille ;
- accès au fichier physique ;
- erreurs ;
- mot de passe du fichier dans la version actuelle.

---

## 5. Anomalie identifiée

Une anomalie avait été identifiée concernant le type MIME utilisé pendant le téléchargement.

Une valeur invalide passée directement à :

```text
MediaType.parseMediaType(...)
```

pouvait provoquer une exception.

Le téléchargement pouvait donc échouer alors que le fichier physique était valide.

---

## 6. Correction humaine

La correction consiste à tenter de parser le Content-Type.

Si la valeur est invalide, le backend utilise :

```text
application/octet-stream
```

Le téléchargement peut alors continuer avec un type générique.

Cette correction correspond au commit historique :

```text
fix(download): handle invalid content type after human review
```

---

## 7. État actuel

La version actuelle du `DownloadController` conserve ce principe.

Le comportement est :

```text
Content-Type valide
→ MediaType correspondant

Content-Type invalide
→ application/octet-stream
```

Ce comportement est maintenant vérifié par un test automatisé du contrôleur.

---

## 8. Protection par mot de passe

Le téléchargement a ensuite évolué pour supporter les fichiers protégés par mot de passe.

Header utilisé :

```text
X-Download-Password
```

Le backend compare le mot de passe fourni avec le hash BCrypt associé au fichier.

Comportements attendus :

```text
fichier protégé + mot de passe absent
→ 403

fichier protégé + mauvais mot de passe
→ 403

fichier protégé + bon mot de passe
→ 200
```

Les anciens fichiers sans mot de passe restent pris en charge.

---

## 9. Tests associés

Les tests couvrent notamment :

```text
métadonnées de téléchargement
téléchargement réussi
mot de passe
403
Content-Type
fallback application/octet-stream
fichier physique
tokens
expiration
```

Dernier résultat backend :

```text
41 tests
0 échec
0 erreur
BUILD SUCCESS
```

---

## 10. Tests globaux

Validation frontend :

```text
27 tests Angular réussis
ESLint : succès
```

Validation End-to-End :

```text
3 tests Playwright réussis
```

Analyse backend :

```text
SpotBugs : 0 bug
SpotBugs : 0 erreur
```

---

## 11. Couverture

Résultats JaCoCo finaux :

```text
Instructions : 89,01 % (1515 / 1702)
Branches : 67,19 % (86 / 128)
Lignes : 90,56 % (547 / 604)
```

La couverture ne prouve pas que le code est sans erreur.

Elle permet seulement de savoir quelles parties ont été exécutées pendant les tests.

---

## 12. Traçabilité Git

Implémentation initiale assistée par IA :

```text
3207354 feat(ai): implement file download by token
```

Correction après revue humaine :

```text
40e879e fix(download): handle invalid content type after human review
```

Cette séparation permet de montrer concrètement :

```text
contribution assistée par IA
        ↓
analyse humaine
        ↓
problème identifié
        ↓
correction humaine
```

---

## 13. Contrôle humain

Les propositions de l'IA ont été évaluées à partir de :

- la compréhension du code ;
- l'architecture du projet ;
- les exigences fonctionnelles ;
- les tests ;
- les résultats d'exécution ;
- les erreurs observées.

Le code n'est pas considéré correct simplement parce qu'il compile ou parce qu'il est proposé par un outil d'IA.

---

## 14. Limites

Les tests automatisés et les analyses statiques réduisent le risque de régression mais ne garantissent pas :

```text
absence totale de bugs
absence totale de vulnérabilités
couverture de tous les scénarios
validité d'un déploiement en production
```

Une revue humaine reste nécessaire.

---

## Conclusion

L'utilisation de l'IA sur cette fonctionnalité est accompagnée d'une supervision humaine identifiable.

Le cas du Content-Type montre une démarche complète :

```text
implémentation assistée
→ revue
→ défaut potentiel détecté
→ correction
→ test
→ non-régression
→ traçabilité Git
```

La responsabilité finale du code conservé dans DataShare reste humaine.
