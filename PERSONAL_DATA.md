# Données personnelles et durée de conservation

## Finalité du traitement

DataShare traite les données nécessaires au fonctionnement du service de partage de fichiers.

Ces données permettent notamment de :

- créer et authentifier un compte utilisateur ;
- associer les fichiers téléversés à leur propriétaire ;
- afficher l'historique des fichiers d'un utilisateur ;
- permettre le téléchargement et la suppression des fichiers ;
- assurer la sécurité et le bon fonctionnement de l'application.

## Données liées au compte utilisateur

Lors de la création d'un compte, DataShare conserve :

- l'adresse e-mail de l'utilisateur ;
- le hash du mot de passe ;
- la date de création du compte.

Le mot de passe n'est jamais enregistré en clair dans la base de données.

## Données liées aux fichiers

Lorsqu'un fichier est téléversé, l'application conserve notamment :

- le nom original du fichier ;
- son nom de stockage interne ;
- sa taille ;
- son type de contenu ;
- sa date de création ;
- sa date d'expiration ;
- son token de téléchargement ;
- son association avec le compte propriétaire.

Le contenu du fichier est stocké localement sur le serveur dans le répertoire `uploads/`.

Le contenu d'un fichier peut lui-même contenir des données personnelles. DataShare ne réalise pas d'analyse fonctionnelle du contenu des documents.

## Durée de conservation des fichiers

Lors du téléversement, l'utilisateur choisit une durée de validité comprise entre 1 et 7 jours.

Une tâche planifiée du backend recherche régulièrement les fichiers dont la date d'expiration est dépassée. Lors de la purge, l'application supprime :

- le fichier physique présent sur le disque ;
- les métadonnées correspondantes dans PostgreSQL.

La suppression intervient donc après expiration, lors de la prochaine exécution de la tâche planifiée.

Un utilisateur authentifié peut également supprimer manuellement l'un de ses fichiers avant sa date d'expiration.

## Conservation des données du compte

Les informations liées au compte utilisateur sont conservées tant que le compte existe.

## Suppression du compte

Dans la version actuelle du MVP, DataShare ne propose pas encore d'auto-suppression du compte depuis l'interface utilisateur.

Cette limitation est documentée. Dans l'état actuel du projet, la suppression d'un compte nécessite une intervention administrative prenant notamment en compte :

- les fichiers encore associés au compte ;
- les métadonnées correspondantes ;
- les contraintes de relation entre les fichiers et leur propriétaire dans PostgreSQL ;
- puis la suppression du compte utilisateur.

Une évolution future pourra ajouter une fonctionnalité d'auto-suppression directement dans l'application.

## Sécurité des données

Les mots de passe sont stockés sous forme de hash et non en clair.

L'accès aux fonctionnalités protégées repose sur une authentification par JWT et Spring Security côté backend.

Les fichiers téléversés sont soumis à plusieurs contrôles :

- taille maximale de 1 Go ;
- liste blanche de formats autorisés ;
- vérification du contenu réel du fichier ;
- vérification de la cohérence entre l'extension et le contenu ;
- durée d'expiration comprise entre 1 et 7 jours ;
- limitation du débit des téléversements.

Les formats actuellement autorisés sont : TXT, PDF, PNG, JPG et JPEG.

## Limitation de débit

Dans le MVP actuel :

- les tentatives de connexion sont limitées à 10 requêtes par minute et par adresse IP ;
- les téléversements sont limités à 20 requêtes par minute et par adresse IP.

Lorsque la limite est dépassée, l'API renvoie `HTTP 429 Too Many Requests`.

La limitation de débit est actuellement conservée en mémoire dans l'instance du backend. Dans une architecture multi-instance, un mécanisme partagé tel que Redis serait nécessaire.

## Limites du MVP

DataShare est un MVP réalisé dans un cadre pédagogique.

Le MVP ne propose notamment pas encore :

- d'auto-suppression du compte ;
- de stockage distribué des fichiers ;
- de gestion distribuée du rate limiting.

Ce document décrit le fonctionnement technique du projet. Il ne constitue pas à lui seul une mise en conformité juridique complète pour une application déployée en production.
