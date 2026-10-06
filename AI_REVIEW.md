# Revue technique du code développé avec l'assistance de l'IA - DataShare

## 1. Objectif

Ce document présente la revue technique du code associé à la User Story historiquement tracée dans Git comme ayant été développée avec l'assistance d'une IA.

L'objectif est de montrer que le code proposé n'a pas été intégré sans contrôle humain.

La démarche suivie est :

```text
proposition assistée
        ↓
relecture
        ↓
test
        ↓
identification des risques
        ↓
correction si nécessaire
        ↓
test de non-régression
        ↓
validation humaine
```

---

## 2. User Story concernée

La fonctionnalité concernée est :

**Télécharger un fichier partagé à partir de son token.**

Cette fonctionnalité permet notamment :

- de rechercher un fichier grâce au token ;
- de contrôler son existence ;
- de contrôler son expiration ;
- de vérifier le fichier physique ;
- de retourner les métadonnées ;
- de construire la réponse HTTP ;
- de télécharger le fichier.

Cette User Story constitue l'exemple historiquement traçable dans Git de code développé avec assistance IA.

---

## 3. Architecture revue

Le backend respecte l'organisation suivante :

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL / stockage local
```

### Controller

Le contrôleur gère notamment :

```text
requête HTTP
headers
réponse HTTP
Content-Type
Content-Disposition
```

### Service

Le service prend en charge notamment :

```text
règles métier
token
expiration
mot de passe du fichier
accès au fichier
```

### Repository

Le repository intervient principalement pour :

```text
recherche des métadonnées
persistance
```

---

## 4. Points vérifiés

La revue a porté notamment sur :

- les codes HTTP ;
- le comportement avec un token valide ;
- le comportement avec un token invalide ;
- le comportement avec un token expiré ;
- l'existence du fichier ;
- le type MIME ;
- le `Content-Disposition` ;
- la taille du fichier ;
- l'accès au fichier physique ;
- la gestion des erreurs ;
- la protection par mot de passe dans la version actuelle.

---

## 5. Anomalie identifiée

Une anomalie avait été identifiée concernant le type MIME utilisé pendant le téléchargement.

Une valeur invalide transmise directement à :

```text
MediaType.parseMediaType(...)
```

pouvait provoquer une exception.

Le téléchargement pouvait donc échouer alors que le fichier physique était valide.

---

## 6. Correction humaine

La correction consiste à tenter d'interpréter le `Content-Type`.

Si la valeur est invalide ou inexploitable, le backend utilise :

```text
application/octet-stream
```

Le téléchargement peut alors continuer avec un type générique.

Cette correction correspond au commit historique :

```text
40e879e fix(download): handle invalid content type after human review
```

Cet exemple montre qu'une proposition assistée par IA n'est pas considérée comme correcte sans vérification.

---

## 7. État actuel

La version actuelle du `DownloadController` conserve ce principe.

Le comportement attendu est :

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

Le header utilisé est :

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

Les fichiers sans mot de passe restent également pris en charge.

Cette évolution est postérieure à l'implémentation historique de la User Story et ne modifie pas le principe de supervision humaine appliqué au code.

---

## 9. Tests associés

Les tests couvrent notamment :

```text
métadonnées de téléchargement
téléchargement réussi
mot de passe
erreurs 403
Content-Type
fallback application/octet-stream
fichier physique
tokens
expiration
```

La validation actuelle du backend comprend :

```text
58 tests
0 échec
0 erreur
BUILD SUCCESS
```

---

## 10. Tests globaux

### Frontend

La validation Angular actuelle comprend :

```text
52 tests Angular réussis
```

### End-to-End

La validation Playwright actuelle comprend :

```text
7 scénarios Playwright
7 réussis
```

### Backend

Les contrôles backend comprennent également les tests automatisés et les mesures de couverture.

Ces validations permettent de limiter le risque de régression après les modifications.

---

## 11. Couverture

La dernière campagne de couverture backend comprend notamment :

```text
Instructions : 91,64 %
Branches : 72,31 %
Lignes : 91,89 %
```

Dans le cadre des corrections demandées, les classes suivantes ont également été vérifiées spécifiquement :

```text
FileService
DownloadController
```

La couverture ne prouve pas que le code est sans erreur.

Elle permet de vérifier quelles parties du code ont été exécutées pendant les tests et de repérer des zones insuffisamment testées.

---

## 12. Traçabilité Git

L'implémentation historique explicitement tracée avec assistance IA est :

```text
3207354 feat(ai): implement file download by token
```

La correction après revue humaine est :

```text
40e879e fix(download): handle invalid content type after human review
```

Cette séparation permet d'illustrer le cycle suivant :

```text
contribution assistée par IA
        ↓
analyse humaine
        ↓
problème identifié
        ↓
correction humaine
        ↓
tests
        ↓
validation
```

Cette traçabilité Git explicite concerne cette User Story.

Elle ne doit pas être étendue artificiellement à d'autres parties du projet lorsqu'aucune trace historique spécifique ne permet de le démontrer.

---

## 13. Contrôle humain

Les propositions de l'IA ont été évaluées à partir de :

- la compréhension du code ;
- l'architecture du projet ;
- les exigences fonctionnelles ;
- les tests ;
- les résultats d'exécution ;
- les erreurs observées ;
- le comportement réel de l'application.

Le code n'est pas considéré comme correct simplement parce qu'il compile ou parce qu'il est proposé par un outil d'IA.

La décision finale de conserver, modifier ou rejeter une proposition reste humaine.

---

## 14. Relation avec les autres usages de l'IA

L'IA a également été utilisée dans le projet comme outil d'assistance pour :

- comprendre des erreurs ;
- diagnostiquer des problèmes ;
- expliquer du code ;
- préparer certaines commandes ;
- aider à interpréter les résultats de tests ;
- aider à structurer la documentation.

Ces usages ne sont pas présentés comme des portions de code historiquement générées par IA lorsqu'aucune trace Git spécifique ne permet de l'établir.

Le document `AI_USAGE.md` décrit ce périmètre plus général.

---

## 15. Limites

Les tests automatisés et les analyses de couverture réduisent le risque de régression, mais ne garantissent pas :

```text
absence totale de bugs
absence totale de vulnérabilités
couverture de tous les scénarios
validité d'un déploiement en production
```

Une revue humaine reste nécessaire.

---

## Conclusion

L'utilisation de l'IA sur la User Story de téléchargement par token est accompagnée d'une supervision humaine identifiable.

Le cas du `Content-Type` montre une démarche complète :

```text
implémentation assistée
        ↓
revue
        ↓
défaut potentiel détecté
        ↓
correction
        ↓
test
        ↓
non-régression
        ↓
traçabilité Git
```

Les résultats actuels comprennent :

```text
58 tests backend réussis
52 tests Angular réussis
7 scénarios Playwright réussis
```

La responsabilité finale du code conservé dans DataShare reste humaine.
