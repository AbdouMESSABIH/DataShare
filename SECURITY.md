# Rapport de sécurité - DataShare

## Objectif

Ce document synthétise les principaux mécanismes de sécurité appliqués dans DataShare et les contrôles réalisés pendant le développement.

## Authentification et mots de passe

- les mots de passe sont hachés avant enregistrement ;
- aucun mot de passe en clair ne doit être stocké en base ;
- les routes privées sont protégées par Spring Security ;
- l'authentification utilise un JWT ;
- le JWT est ajouté aux appels protégés côté frontend par un interceptor ;
- les routes frontend protégées utilisent un guard.

## Secrets

Les secrets sont fournis par variables d'environnement, notamment :

```text
DB_PASSWORD
JWT_SECRET
```

Les valeurs réelles ne doivent pas être versionnées dans Git.

## Contrôle des fichiers téléversés

Les fichiers sont soumis aux contrôles suivants :

- taille maximale : 1 Go ;
- durée d'expiration : 1 à 7 jours ;
- formats autorisés : TXT, PDF, PNG, JPG et JPEG ;
- vérification de l'extension ;
- vérification du contenu réel du fichier ;
- vérification de la cohérence entre extension et contenu.

Un fichier dont le contenu ne correspond pas au type attendu est refusé.

Ces contrôles constituent une protection applicative du MVP ; ils ne remplacent pas un antivirus ou un moteur d'analyse spécialisé dans un environnement de production.

## Autorisations

L'historique est associé à l'utilisateur authentifié.

La suppression d'un fichier vérifie que le fichier appartient bien à l'utilisateur connecté.

Les liens publics utilisent un token de téléchargement distinct du JWT et sont limités par une date d'expiration.

## Rate limiting

Une limitation de débit en mémoire est appliquée par adresse IP :

```text
Connexion : 10 requêtes/minute/IP
Upload : 20 requêtes/minute/IP
```

En cas de dépassement, l'API renvoie `429 Too Many Requests`.

Ce mécanisme est adapté au MVP mono-instance. Une architecture multi-instance nécessiterait un compteur partagé, par exemple avec Redis.

## Analyse statique

Le frontend est contrôlé avec ESLint.

Le backend est contrôlé avec SpotBugs.

Dernière validation :

```text
ESLint : succès
SpotBugs : 0 bug / 0 erreur
```

## Dépendances frontend

Les dépendances peuvent être analysées avec :

```bash
cd frontend
npm audit
npm audit --omit=dev
```

Une correction forcée avec `npm audit fix --force` ne doit pas être appliquée sans analyser les changements de versions et les régressions possibles.

## Logs

Les logs ne doivent pas contenir :

- de mot de passe ;
- de JWT ;
- de token de téléchargement.

## Limites

DataShare est un MVP pédagogique.

Pour une mise en production, des mesures complémentaires pourraient être nécessaires : HTTPS obligatoire, reverse proxy, antivirus ou analyse de fichiers, rate limiting distribué, stockage objet partagé, rotation des secrets, supervision et politique de sauvegarde adaptée.
