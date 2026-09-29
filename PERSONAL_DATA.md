# Données personnelles et durée de conservation - DataShare

## 1. Objectif

Ce document décrit les principales données manipulées par DataShare et leur cycle de vie dans le MVP.

Il s'agit d'une documentation technique du projet.

Elle ne constitue pas à elle seule une analyse juridique complète ni une certification de conformité réglementaire.

---

## 2. Données du compte utilisateur

Lors de l'inscription, DataShare conserve notamment :

```text
adresse e-mail
hash du mot de passe
date de création du compte
```

Le mot de passe utilisateur n'est pas enregistré en clair.

Il est hashé avec BCrypt avant stockage.

---

## 3. Données associées aux fichiers

Lors du téléversement, DataShare conserve notamment :

```text
nom original
nom interne de stockage
taille
Content-Type
date de création
date d'expiration
token de téléchargement
propriétaire
hash éventuel du mot de passe fichier
```

Le mot de passe protégeant un fichier n'est pas stocké en clair.

Il est transformé en hash BCrypt.

---

## 4. Contenu des fichiers

Le contenu physique des fichiers est stocké localement dans le répertoire de stockage du backend.

Le fichier peut lui-même contenir des données personnelles ou confidentielles.

DataShare ne réalise pas d'analyse sémantique du contenu des documents.

---

## 5. Durée de conservation

Lors de l'upload, la durée de validité est comprise entre :

```text
1 et 7 jours
```

La date d'expiration est enregistrée dans PostgreSQL.

Une fois le fichier expiré, son lien de téléchargement n'est plus utilisable.

L'API retourne :

```text
410 Gone
```

---

## 6. Purge des fichiers expirés

Une tâche planifiée recherche les fichiers expirés.

Lors de la purge, elle supprime :

```text
le fichier physique
+
les métadonnées associées
```

La suppression intervient lors du passage de la tâche de nettoyage suivant l'expiration.

---

## 7. Suppression manuelle

Un utilisateur authentifié peut supprimer l'un de ses fichiers avant expiration.

La suppression concerne :

```text
fichier physique
+
métadonnées
```

Le backend vérifie que le fichier appartient à l'utilisateur connecté.

---

## 8. Historique utilisateur

L'historique ne retourne que les fichiers associés à l'utilisateur authentifié.

L'accès utilise le JWT.

Un utilisateur ne doit pas recevoir l'historique d'un autre utilisateur.

---

## 9. Liens de partage

Chaque fichier possède un token de téléchargement.

Ce token est distinct du JWT de l'utilisateur.

Il permet d'accéder à la page publique correspondant au fichier.

La protection peut également inclure :

```text
expiration
+
mot de passe fichier
```

Le token de téléchargement doit être considéré comme une donnée sensible et ne doit pas être écrit inutilement dans les logs.

---

## 10. Mot de passe fichier

Lorsque le fichier est protégé :

```text
mot de passe saisi
→ BCrypt
→ hash stocké en base
```

Pendant le téléchargement, le mot de passe fourni est comparé au hash.

Le mot de passe en clair :

- n'est pas enregistré en base ;
- ne doit pas être écrit dans les logs ;
- ne doit pas être versionné dans Git.

---

## 11. Données présentes dans les logs

Les logs techniques peuvent contenir des informations utiles au diagnostic.

Ils ne doivent pas contenir :

```text
mot de passe utilisateur
mot de passe fichier
JWT
JWT_SECRET
DB_PASSWORD
token de téléchargement
```

Les événements métier principaux sont :

```text
file_upload
file_download
file_delete
```

---

## 12. Sécurité des fichiers

Les fichiers téléversés sont contrôlés.

Contraintes principales :

```text
taille maximale : 1 Go
expiration : 1 à 7 jours

formats :
TXT
PDF
PNG
JPG
JPEG
```

Le backend vérifie notamment :

- extension ;
- contenu réel ;
- cohérence entre extension et contenu.

Ces contrôles ne remplacent pas une solution antivirus de production.

---

## 13. Authentification

Les fonctions privées utilisent :

```text
Spring Security
JWT
```

Le JWT permet d'identifier l'utilisateur pour les opérations protégées.

---

## 14. Suppression du compte

Le MVP actuel ne fournit pas de fonction d'auto-suppression du compte dans l'interface.

La suppression complète d'un compte nécessiterait de traiter correctement :

```text
fichiers du propriétaire
métadonnées des fichiers
relations PostgreSQL
compte utilisateur
```

Cette fonction constitue une évolution possible.

---

## 15. Conservation des comptes

Les informations du compte sont conservées tant que le compte existe.

Le mécanisme d'expiration automatique concerne actuellement les fichiers, et non les comptes utilisateurs.

---

## 16. Rate limiting

Le MVP applique notamment une limitation de débit sur certaines opérations sensibles.

La documentation de sécurité décrit ce mécanisme dans :

```text
SECURITY.md
```

Le rate limiting actuel est local à l'instance du backend.

---

## 17. PostgreSQL

PostgreSQL contient notamment :

```text
utilisateurs
hash des mots de passe utilisateurs
métadonnées des fichiers
hash éventuel des mots de passe fichiers
tokens de téléchargement
dates d'expiration
```

Les sauvegardes PostgreSQL peuvent donc contenir des données sensibles.

Elles doivent être protégées et ne doivent pas être ajoutées au dépôt Git.

---

## 18. Stockage local

Le stockage local convient au MVP pédagogique.

Une mise en production nécessiterait une réflexion supplémentaire concernant :

- chiffrement ;
- contrôle d'accès au stockage ;
- sauvegarde ;
- restauration ;
- stockage objet ;
- disponibilité ;
- suppression garantie ;
- supervision.

---

## 19. Limites

DataShare est un MVP pédagogique.

Il ne fournit pas actuellement :

```text
auto-suppression du compte
stockage distribué
rate limiting distribué
antivirus spécialisé
gestion complète du cycle de vie juridique des données
```

Une mise en production réelle nécessiterait une analyse spécifique des obligations réglementaires et organisationnelles.

---

## Conclusion

DataShare limite la conservation des fichiers à une durée choisie entre 1 et 7 jours et dispose d'une purge automatique.

Les mots de passe utilisateurs et fichiers sont stockés uniquement sous forme de hash BCrypt.

Le projet applique également :

```text
JWT
contrôle du propriétaire
expiration
validation des fichiers
protection par mot de passe
suppression manuelle
purge automatique
```

Ces mécanismes constituent la gestion technique des données dans le cadre du MVP.
