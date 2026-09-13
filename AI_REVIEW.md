# Revue technique du code développé avec l'IA - DataShare

## Objectif

Ce document présente la revue technique du code développé avec
l'assistance d'une IA dans le cadre du projet DataShare.

L'IA a été utilisée comme assistant de développement et d'apprentissage.

Le code proposé n'a pas été considéré comme valide automatiquement :
il a été relu, testé et corrigé avant d'être conservé dans le projet.


## Fonctionnalité concernée

La fonctionnalité principalement développée avec l'assistance de l'IA
est la User Story de téléchargement d'un fichier à partir d'un token.

Cette fonctionnalité permet notamment :

- de rechercher un fichier grâce à son token de téléchargement ;
- de vérifier que le token existe ;
- de vérifier que le lien n'est pas expiré ;
- de vérifier que le fichier physique existe ;
- de retourner les métadonnées du fichier ;
- de télécharger le fichier.


## Architecture revue

La fonctionnalité respecte l'architecture du backend :

`Controller -> Service -> Repository -> PostgreSQL / stockage local`

Les responsabilités ont été vérifiées :

- le Controller gère les requêtes et réponses HTTP ;
- le Service contient les règles métier ;
- le Repository gère l'accès aux données ;
- le stockage local contient le fichier physique.


## Vérifications réalisées

La revue humaine a porté notamment sur :

- les codes HTTP retournés ;
- la gestion d'un token invalide ;
- la gestion d'un token expiré ;
- la présence du fichier physique ;
- le type MIME retourné ;
- les headers HTTP du téléchargement ;
- la gestion des erreurs.


## Anomalie détectée pendant la revue

Une anomalie a été identifiée concernant le type MIME du fichier.

Le code utilisait le type de contenu stocké afin de construire
le `MediaType` de la réponse HTTP.

Dans le cas d'un type MIME invalide ou mal formé, une
`InvalidMediaTypeException` pouvait être déclenchée et provoquer
une erreur pendant le téléchargement.


## Correction appliquée

Le code a été corrigé afin d'utiliser :

`application/octet-stream`

comme valeur de repli lorsque le type MIME stocké n'est pas valide.

Cette correction permet d'éviter qu'un type MIME incorrect bloque
le téléchargement d'un fichier valide.


## Tests réalisés après la revue

Plusieurs scénarios ont été vérifiés :

| Scénario | Résultat |
|---|---|
| Téléchargement avec token valide | HTTP 200 |
| Token inconnu | HTTP 404 |
| Token expiré | HTTP 410 |
| Type MIME invalide | HTTP 200 avec type de repli |

La fonctionnalité est également couverte par les tests d'intégration
et par le scénario End-to-End de l'application.


## Validation de non-régression

Après les corrections, les tests globaux du projet ont été relancés.

Résultats observés :

- 22 tests backend réussis ;
- scénario E2E Playwright réussi ;
- couverture JaCoCo supérieure à 70 % ;
- parcours upload / téléchargement / suppression fonctionnel.


## Traçabilité Git

Le développement assisté par l'IA a été identifié dans l'historique Git.

Commit d'implémentation :

`feat(ai): implement file download by token`

La correction issue de la revue humaine a également été séparée :

`fix(download): handle invalid content type after human review`

Cette séparation permet de distinguer la proposition initiale
et la correction réalisée après analyse.


## Conclusion

L'IA a servi d'assistant pour accélérer l'implémentation et proposer
une solution technique.

Le code n'a pas été accepté sans contrôle.

Une revue humaine, des tests et une correction ont permis de détecter
et corriger une anomalie concernant la gestion du type MIME.

Cette démarche permet de conserver la responsabilité humaine sur
le code intégré au projet.