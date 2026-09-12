# Utilisation de l'IA dans le développement

## Mon utilisation générale de l'IA

Depuis le début de ma formation Expert DevOps, j'utilise l'IA comme outil
d'accompagnement pédagogique.

Avant de commencer un projet, je lui fournis le contexte nécessaire :
consignes du projet, architecture, technologies utilisées et captures utiles.

Cela permet à l'IA de connaître l'état du projet lorsque je rencontre un
problème au cours du développement.

Je l'utilise principalement pour :

- comprendre une notion technique ;
- analyser un message d'erreur ;
- diagnostiquer un problème étape par étape ;
- comprendre le rôle d'un fichier ou d'une classe ;
- obtenir des explications sur du code ;
- vérifier une solution ;
- apprendre pendant la résolution du problème.

Je reste responsable des commandes exécutées, du code intégré dans le projet
et des tests réalisés.

## User Story identifiée pour l'utilisation de l'IA

Dans le cadre de la consigne spécifique du projet demandant de tracer
l'utilisation d'un copilote IA sur une User Story, j'ai choisi l'US02 :

**Télécharger un fichier partagé à partir de son token.**

Cette User Story a été spécifiquement identifiée et tracée dans Git comme
travail réalisé avec l'aide de l'IA.

## Tâches réalisées sur l'US02

L'IA a participé à la proposition de l'implémentation permettant :

- de rechercher un fichier à partir de son token de téléchargement ;
- de vérifier si le token existe ;
- de vérifier si le lien est expiré ;
- de récupérer le fichier physique sur le disque ;
- d'exposer les endpoints REST de téléchargement ;
- d'intégrer la page Angular de téléchargement.

## Vérifications réalisées

Après l'implémentation, j'ai testé manuellement plusieurs situations :

- token valide : HTTP 200 et téléchargement du fichier ;
- token inexistant : HTTP 404 ;
- token expiré : HTTP 410 ;
- ouverture du lien depuis Angular ;
- téléchargement réel du fichier depuis le navigateur.

## Relecture humaine et correction

J'ai ensuite relu le code proposé.

Pendant cette revue, j'ai identifié un problème potentiel dans le traitement
du Content-Type.

Le code supposait que le Content-Type enregistré était toujours valide.
Une valeur invalide pouvait provoquer une exception pendant le téléchargement.

J'ai ajouté une gestion d'erreur avec une valeur de repli :

`application/octet-stream`

J'ai ensuite testé cette correction avec un Content-Type volontairement
invalide et vérifié que le téléchargement continuait à fonctionner.

## Traçabilité Git

Le développement identifié comme contribution IA est tracé avec le commit :

`feat(ai): implement file download by token`

La correction réalisée après ma revue est tracée avec le commit :

`fix(download): handle invalid content type after human review`

## Supervision

L'IA est utilisée comme un outil d'aide et d'apprentissage.

Je vérifie les propositions avant de les intégrer et je teste les
fonctionnalités afin de comprendre leur fonctionnement et de pouvoir
les expliquer lors de la soutenance.