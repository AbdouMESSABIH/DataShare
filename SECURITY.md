# Rapport de sécurité - DataShare

## Objectif

L'objectif de cette analyse est d'identifier les vulnérabilités connues
dans les dépendances utilisées par DataShare et d'évaluer leur impact
sur l'application.

L'analyse porte principalement sur le frontend Angular avec `npm audit`.

Elle est complétée par une vérification des principales mesures de sécurité
déjà présentes dans l'application.

---

## 1. Analyse complète des dépendances

Commande utilisée :

```bash
cd ~/Projets/DataShare/frontend
npm audit
```

Résultat observé :

```text
33 vulnérabilités
```

Répartition :

```text
2 low
15 moderate
15 high
1 critical
```

Ce résultat inclut également les dépendances utilisées uniquement
pour le développement.

---

## 2. Analyse des dépendances de production

Afin de distinguer les dépendances réellement utilisées en production,
une seconde analyse a été réalisée :

```bash
npm audit --omit=dev
```

Résultat observé :

```text
7 vulnérabilités
```

Répartition :

```text
4 moderate
3 high
0 critical
```

La vulnérabilité critique observée dans l'analyse complète ne concerne donc
pas les dépendances de production analysées avec cette commande.

---

## 3. Décision prise

La commande suivante n'a pas été exécutée automatiquement :

```bash
npm audit fix --force
```

Cette commande proposait une mise à jour majeure de dépendances Angular.

Une mise à jour forcée de ce type peut provoquer :

- des incompatibilités ;
- des changements d'API ;
- des erreurs de compilation ;
- des régressions fonctionnelles ;
- des modifications nécessaires dans le code frontend.

La décision a donc été de ne pas appliquer une migration majeure
automatiquement juste avant la livraison.

La stratégie retenue est :

1. identifier les dépendances concernées ;
2. préparer une mise à jour contrôlée ;
3. consulter les notes de version ;
4. effectuer la migration sur une branche dédiée ;
5. relancer les tests ;
6. vérifier les parcours critiques avant intégration.

---

## 4. Risque accepté temporairement

Les vulnérabilités restantes ont été documentées.

Cette décision ne signifie pas que l'application est considérée
comme totalement sécurisée.

Elle correspond à une acceptation temporaire du risque dans le cadre
du prototype, en attendant une migration contrôlée des dépendances.

Les vulnérabilités de niveau élevé doivent rester prioritaires
lors d'une future maintenance.

---

## 5. Mesures de sécurité déjà présentes

### Mots de passe

Les mots de passe utilisateurs ne sont pas stockés en clair.

Ils sont hachés avant leur enregistrement dans PostgreSQL.

---

### Authentification

DataShare utilise une authentification basée sur JWT.

Les routes protégées nécessitent un utilisateur authentifié.

Le backend est configuré en mode stateless.

---

### Secrets

Les données sensibles de configuration ne sont pas stockées directement
dans le repository.

Le backend utilise notamment :

```text
DB_PASSWORD
JWT_SECRET
```

Ces valeurs sont fournies par variables d'environnement.

---

### Autorisation sur les fichiers

Lorsqu'un utilisateur souhaite supprimer un fichier, le backend vérifie
que ce fichier appartient bien à l'utilisateur authentifié.

Un utilisateur ne doit donc pas pouvoir supprimer le fichier d'un autre compte.

Ce comportement est également couvert par un test d'intégration.

---

### Expiration des liens

Chaque fichier partagé possède une date d'expiration.

Lorsqu'un token correspond à un fichier expiré, le backend refuse
le téléchargement.

Le comportement attendu est :

```text
HTTP 410 - Gone
```

---

### Token invalide

Lorsqu'un token de téléchargement n'existe pas, le backend retourne :

```text
HTTP 404 - Not Found
```

---

### Taille des fichiers

La taille maximale acceptée est :

```text
1 Go
```

Un fichier dépassant cette limite est refusé.

Le comportement attendu est :

```text
HTTP 413 - Payload Too Large
```

---

### Extensions interdites

Les fichiers avec les extensions suivantes sont refusés :

```text
.exe
.bat
```

Le comportement attendu est :

```text
HTTP 415 - Unsupported Media Type
```

---

## 6. Protection contre les risques applicatifs

Plusieurs risques classiques ont été pris en compte.

### Injection SQL

L'accès aux données utilise Spring Data JPA et des repositories.

L'application ne construit pas directement des requêtes SQL à partir
des valeurs saisies par l'utilisateur dans les fonctionnalités développées.

---

### XSS

Le frontend Angular affiche les données dans les templates Angular.

Aucun mécanisme volontaire de rendu HTML arbitraire provenant d'un utilisateur
n'a été ajouté dans les fonctionnalités principales.

Les mises à jour Angular restent importantes car certaines vulnérabilités
de dépendances peuvent concerner le traitement ou la sanitisation du contenu.

---

### CSRF

Le backend utilise une authentification JWT stateless.

La protection CSRF de Spring Security est désactivée dans cette configuration,
car l'authentification ne repose pas sur une session serveur classique.

Cette configuration doit rester cohérente avec l'architecture JWT utilisée.

---

### Contrôle d'accès

Les endpoints sensibles nécessitent une authentification.

Les contrôles métier complètent cette protection pour les actions
qui dépendent du propriétaire d'une ressource.

---

## 7. Sécurité des logs

Le backend produit des logs structurés au format JSON.

Les informations suivantes ne doivent pas être enregistrées :

- mots de passe ;
- JWT ;
- tokens de téléchargement ;
- secrets d'environnement.

Les logs sont utilisés pour le diagnostic sans exposer volontairement
ces données sensibles.

---

## 8. Sécurité du stockage

Les fichiers téléversés sont stockés localement.

Le répertoire de stockage n'est pas versionné dans Git.

Lors d'une suppression réalisée par le propriétaire :

- les métadonnées sont supprimées ;
- le fichier physique est supprimé.

Le stockage local convient au prototype mais nécessiterait une stratégie
plus robuste pour un environnement de production.

---

## 9. Tests liés à la sécurité

Plusieurs scénarios sont couverts par les tests du projet :

```text
inscription avec email déjà utilisé -> HTTP 409
connexion avec identifiants incorrects -> HTTP 401
accès historique sans authentification -> refusé
token de téléchargement inconnu -> HTTP 404
token expiré -> HTTP 410
suppression du fichier d'un autre utilisateur -> refusée
fichier .exe -> HTTP 415
fichier supérieur à 1 Go -> HTTP 413
```

La stratégie complète est détaillée dans :

```text
TESTING.md
```

---

## 10. Procédure de maintenance sécurité

Les dépendances doivent être vérifiées régulièrement.

Commandes principales :

```bash
cd ~/Projets/DataShare/frontend

npm outdated
npm audit
npm audit --omit=dev
```

Avant une mise à jour majeure :

- consulter les notes de version ;
- identifier les breaking changes ;
- créer une branche dédiée ;
- mettre à jour les dépendances ;
- relancer les tests frontend ;
- relancer le test End-to-End ;
- vérifier les principales fonctionnalités manuellement.

La procédure détaillée est également décrite dans :

```text
MAINTENANCE.md
```

---

## 11. Vérifications après une mise à jour

Après une mise à jour de dépendances :

```bash
cd ~/Projets/DataShare/frontend
npm install
npx playwright test
```

Puis vérifier au minimum :

```text
Inscription
Connexion
Upload
Historique
Téléchargement
Suppression
```

Une nouvelle exécution de :

```bash
npm audit --omit=dev
```

permet de mesurer l'évolution des vulnérabilités restantes.

---

## Conclusion

L'analyse de sécurité a permis d'identifier des vulnérabilités connues
dans les dépendances frontend.

Résultats principaux :

```text
npm audit
33 vulnérabilités
2 low
15 moderate
15 high
1 critical
```

et pour les dépendances de production :

```text
npm audit --omit=dev
7 vulnérabilités
4 moderate
3 high
0 critical
```

Une mise à jour majeure forcée n'a pas été appliquée automatiquement
afin d'éviter d'introduire des régressions non maîtrisées.

Cette décision est temporaire et les dépendances concernées devront être
mises à jour dans le cadre d'une migration contrôlée accompagnée de tests.

DataShare dispose parallèlement de plusieurs mécanismes de protection :
hachage des mots de passe, JWT, contrôle des droits, expiration des liens,
limitation de taille, blocage de certaines extensions et secrets fournis
par variables d'environnement.