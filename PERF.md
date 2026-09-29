# Rapport de performance - DataShare

## 1. Objectif

L'objectif de cette analyse est d'évaluer les performances de l'application DataShare sur deux aspects complémentaires :

- les performances du back-end Spring Boot sous charge avec k6 ;
- les performances du front-end Angular dans le navigateur avec Lighthouse.

Les mesures présentées dans ce document ont été réalisées sur la version actuelle de l'application, incluant notamment la protection des fichiers par mot de passe.

Les tests ont été effectués dans un environnement local de développement.

Ils permettent de vérifier le comportement de l'application et de détecter d'éventuels problèmes de performance, mais ils ne constituent pas un benchmark représentatif d'un environnement de production.

---

# 2. Environnement de test

Les tests ont été réalisés localement sur la machine de développement.

Les deux outils suivent des chemins différents :

```text
Navigateur                         k6
    │                               │
    ▼                               │ HTTP direct
Frontend Angular                    │
http://localhost:4173               │
    │                               │
    │ API REST                      │
    └──────────────┬────────────────┘
                   ▼
          Backend Spring Boot
          http://localhost:8080
                   │
             ┌─────┴─────┐
             ▼           ▼
        PostgreSQL   stockage local
                     des fichiers
```

Le navigateur charge l'application Angular, qui communique ensuite avec le back-end via l'API REST.

k6 ne passe pas par Angular : il envoie directement ses requêtes HTTP au back-end Spring Boot.

Le back-end fonctionne actuellement en mono-instance.

PostgreSQL et le stockage des fichiers sont locaux.

---

# 3. Test de charge back-end avec k6

## 3.1 Objectif

Le test k6 permet d'observer le comportement du back-end lorsque plusieurs utilisateurs effectuent simultanément des téléchargements.

Le scénario mesure notamment :

```text
temps de réponse
taux d'erreur
nombre de requêtes
débit
validation fonctionnelle des réponses
```

---

## 3.2 Endpoint testé

L'endpoint utilisé est :

```text
GET /api/download/{token}/file
```

Le back-end est accessible sur :

```text
http://localhost:8080
```

Le fichier utilisé pour le test est protégé par mot de passe.

Le mot de passe est envoyé dans l'en-tête HTTP :

```text
X-Download-Password
```

Le scénario testé correspond donc au fonctionnement actuel de DataShare.

---

## 3.3 Scénario k6

Configuration :

```text
10 utilisateurs virtuels simultanés
20 secondes de test
téléchargement répété d'un fichier existant
fichier protégé par mot de passe
```

Chaque utilisateur virtuel effectue continuellement :

```text
GET /api/download/{token}/file
```

avec :

```text
X-Download-Password: <mot-de-passe>
```

Pendant chaque requête, deux vérifications fonctionnelles sont effectuées :

```text
statut HTTP = 200
fichier téléchargé non vide
```

---

## 3.4 Commande utilisée

Le test est exécuté avec des variables d'environnement afin de ne pas stocker le token et le mot de passe directement dans le script.

Exemple :

```bash
DOWNLOAD_TOKEN='<token>' \
DOWNLOAD_PASSWORD='<mot-de-passe>' \
BASE_URL='http://localhost:8080' \
k6 run performance/download-test.js
```

---

# 4. Seuils de performance k6

Deux seuils ont été définis dans le script.

## Taux d'erreur

```text
http_req_failed < 1 %
```

Cela signifie que moins de 1 % des requêtes HTTP peuvent échouer.

## Temps de réponse p95

```text
http_req_duration p(95) < 1000 ms
```

Cela signifie qu'au moins 95 % des requêtes doivent terminer en moins d'une seconde.

Ces seuils permettent d'avoir un critère objectif pour déterminer si le scénario respecte les attentes définies.

---

# 5. Résultat final k6

Le test final a été exécuté avec :

```text
10 VUs
20 secondes
fichier protégé par mot de passe
```

Résultats obtenus :

```text
Requêtes HTTP : 2 569

Débit :
128,05 requêtes/s

Checks :
5 138 / 5 138 réussis

Checks réussis :
100 %

Checks échoués :
0 %

Taux d'erreur HTTP :
0 %

Temps moyen :
77,84 ms

Temps médian :
81,57 ms

p90 :
89,61 ms

p95 :
91,47 ms

Temps maximal :
207,4 ms
```

---

## 5.1 Résultat des checks

Les deux validations fonctionnelles ont réussi pendant tout le test :

```text
✓ status HTTP 200
✓ fichier non vide
```

Nombre total de checks :

```text
5 138
```

Nombre de checks réussis :

```text
5 138
```

Nombre de checks échoués :

```text
0
```

---

## 5.2 Validation des seuils

### Taux d'erreur

Seuil défini :

```text
rate < 1 %
```

Résultat :

```text
0,00 %
```

Statut :

```text
✓ seuil respecté
```

### Temps de réponse p95

Seuil défini :

```text
p95 < 1000 ms
```

Résultat :

```text
91,47 ms
```

Statut :

```text
✓ seuil respecté
```

Les deux seuils définis dans le scénario k6 sont donc respectés dans l'environnement local utilisé.

---

# 6. Interprétation du test k6

Le résultat montre que, pendant ce scénario local :

```text
aucune requête HTTP n'a échoué
100 % des checks fonctionnels ont réussi
95 % des requêtes ont répondu en moins de 91,47 ms
le débit observé est d'environ 128 requêtes par seconde
```

Le test montre donc que le back-end reste stable dans ce scénario de charge local avec 10 utilisateurs virtuels simultanés.

Il ne permet cependant pas de conclure que DataShare pourrait supporter ce même débit en production.

---

# 7. Évolution du scénario de performance

Une ancienne version du test de performance téléchargeait un fichier sans vérification de mot de passe.

Après l'ajout de la protection des fichiers, le scénario a été mis à jour afin d'envoyer :

```text
X-Download-Password
```

La version actuelle effectue donc également côté serveur une vérification BCrypt du mot de passe avant chaque téléchargement.

Les anciens résultats obtenus sans cette vérification et les nouveaux résultats ne correspondent donc pas exactement au même scénario.

Ils ne doivent pas être comparés directement pour conclure à une régression de performance.

Le résultat retenu comme référence finale est celui de la version actuelle :

```text
2 569 requêtes
128,05 requêtes/s
0 % d'erreur
p95 = 91,47 ms
```

---

# 8. Test de performance front-end avec Lighthouse

## 8.1 Objectif

Lighthouse permet de mesurer les performances de rendu du front-end Angular dans le navigateur.

Contrairement à k6, Lighthouse ne mesure pas la capacité du back-end à supporter plusieurs utilisateurs simultanés.

Il mesure principalement l'expérience de chargement de la page côté navigateur.

---

# 9. Mesure initiale Lighthouse

Une première analyse réalisée avant les optimisations avait donné :

```text
Performance : 82/100
FCP : 2,6 s
LCP : 4,2 s
TBT : 0 ms
CLS : 0
```

Le principal point d'amélioration concernait le LCP.

Le LCP mesurait alors :

```text
4,2 s
```

---

# 10. Mesure finale Lighthouse

Après les modifications du front-end et la mise à jour finale de l'interface, une nouvelle analyse Lighthouse a été réalisée sur le build Angular de production.

URL testée :

```text
http://localhost:4173
```

Résultat final :

```text
Performance : 90/100

First Contentful Paint (FCP) :
2,7 s

Largest Contentful Paint (LCP) :
3,0 s

Total Blocking Time (TBT) :
10 ms

Cumulative Layout Shift (CLS) :
0

Speed Index :
2,7 s
```

---

# 11. Interprétation des métriques Lighthouse

## Performance

Résultat :

```text
90/100
```

Le score Lighthouse final atteint 90 sur 100 dans l'environnement de test.

---

## First Contentful Paint - FCP

Résultat :

```text
2,7 s
```

Le FCP correspond au moment où le navigateur affiche le premier contenu visible de la page.

---

## Largest Contentful Paint - LCP

Résultat initial :

```text
4,2 s
```

Résultat final :

```text
3,0 s
```

Le LCP s'est donc amélioré par rapport à la mesure initiale.

Cette métrique correspond au temps nécessaire pour afficher le plus grand élément visible de la page.

---

## Total Blocking Time - TBT

Résultat :

```text
10 ms
```

Le TBT mesure le temps pendant lequel le thread principal du navigateur est bloqué par des tâches longues.

La valeur observée reste faible pendant cette mesure.

---

## Cumulative Layout Shift - CLS

Résultat :

```text
0
```

Aucun déplacement visuel significatif n'a été détecté pendant le chargement de la page testée.

---

## Speed Index

Résultat :

```text
2,7 s
```

Le Speed Index mesure la vitesse à laquelle le contenu visible de la page apparaît progressivement.

---

# 12. Comparaison Lighthouse avant / après

| Métrique | Avant | Final |
|---|---:|---:|
| Performance | 82/100 | 90/100 |
| FCP | 2,6 s | 2,7 s |
| LCP | 4,2 s | 3,0 s |
| TBT | 0 ms | 10 ms |
| CLS | 0 | 0 |
| Speed Index | non relevé | 2,7 s |

Le principal gain visible concerne le LCP :

```text
4,2 s
→
3,0 s
```

Le score global Lighthouse passe également de :

```text
82/100
→
90/100
```

Certaines métriques peuvent légèrement varier entre deux exécutions Lighthouse en fonction de la machine et de la charge système.

---

# 13. Différence entre k6 et Lighthouse

Les deux outils ne répondent pas au même besoin.

## k6

k6 mesure le comportement du serveur sous charge.

Dans ce projet, il permet d'observer :

```text
le nombre de requêtes
le taux d'erreur
les temps de réponse
le p95
le débit
la stabilité avec plusieurs utilisateurs virtuels
```

Le test k6 utilisé ici concerne principalement le back-end Spring Boot.

---

## Lighthouse

Lighthouse mesure principalement les performances du front-end dans un navigateur.

Il analyse notamment :

```text
FCP
LCP
TBT
CLS
Speed Index
score de performance
```

Il permet donc d'évaluer la rapidité de chargement et la stabilité visuelle de l'interface.

---

## Complémentarité

Les deux outils sont complémentaires :

```text
k6
→ performance et charge côté serveur

Lighthouse
→ performance de rendu côté navigateur
```

Un bon résultat Lighthouse ne garantit pas qu'un serveur supportera une forte charge.

De la même manière, un bon résultat k6 ne garantit pas que l'interface sera rapide ou agréable à afficher dans un navigateur.

---

# 14. Performance et test de charge

Il est important de distinguer les deux notions.

## Test de performance

Un test de performance mesure notamment :

```text
temps de réponse
latence
débit
consommation de ressources
```

## Test de charge

Un test de charge vérifie le comportement d'un système lorsqu'il reçoit plusieurs requêtes ou utilisateurs simultanément.

Le scénario k6 de DataShare est donc un test de charge simple qui fournit également plusieurs métriques de performance.

Il utilise :

```text
10 utilisateurs virtuels simultanés
pendant 20 secondes
```

Il ne constitue pas un test de stress ni un test permettant de déterminer la capacité maximale de l'application.

---

# 15. Limites de l'analyse

Les résultats doivent être interprétés dans leur contexte.

Les tests ont été réalisés :

```text
sur une seule machine
en environnement local
avec une seule instance Spring Boot
avec une base PostgreSQL locale
avec un stockage de fichiers local
avec un fichier de test de petite taille
sur une période de 20 secondes pour k6
avec 10 utilisateurs virtuels
```

Les résultats dépendent également :

```text
du processeur
de la mémoire disponible
du système d'exploitation
de la charge de la machine
du navigateur
de la taille du fichier
de PostgreSQL
du stockage disque
de la configuration Java
```

Le débit observé :

```text
128,05 requêtes/s
```

ne doit donc pas être présenté comme la capacité maximale de DataShare en production.

Il s'agit uniquement du débit observé pendant ce scénario local précis.

---

# 16. Limites du scénario k6

Le scénario actuel vérifie principalement un téléchargement répété.

Il ne reproduit pas l'ensemble du comportement d'un environnement réel.

Il ne teste notamment pas simultanément :

```text
l'inscription
la connexion
l'upload
l'historique
la suppression
plusieurs tailles de fichiers
plusieurs fichiers différents
plusieurs instances du back-end
un stockage réseau ou objet
une base de données distante
```

Une analyse plus poussée pourrait utiliser plusieurs scénarios k6 représentant différents parcours utilisateurs.

---

# 17. Améliorations possibles

Pour aller plus loin dans une analyse de production, il serait possible de tester :

```text
plusieurs niveaux de VUs
des tests plus longs
différentes tailles de fichiers
plusieurs endpoints simultanément
un environnement proche de la production
la consommation CPU
la consommation mémoire
les performances PostgreSQL
les performances du stockage
```

Exemple de montée en charge progressive :

```text
10 utilisateurs
25 utilisateurs
50 utilisateurs
100 utilisateurs
```

Cela permettrait d'observer à quel moment les temps de réponse ou le taux d'erreur commencent à se dégrader.

---

# 18. Résumé des résultats finaux

## Back-end - k6

```text
Scénario :
10 VUs pendant 20 secondes

Endpoint :
GET /api/download/{token}/file

Protection :
X-Download-Password

Requêtes :
2 569

Débit :
128,05 requêtes/s

Checks :
5 138 / 5 138

Checks réussis :
100 %

Erreurs HTTP :
0 %

Temps moyen :
77,84 ms

p95 :
91,47 ms

Temps maximum :
207,4 ms

Seuil erreurs < 1 % :
RESPECTÉ

Seuil p95 < 1000 ms :
RESPECTÉ
```

## Front-end - Lighthouse

```text
Performance :
90/100

FCP :
2,7 s

LCP :
3,0 s

TBT :
10 ms

CLS :
0

Speed Index :
2,7 s
```

---

# 19. Conclusion

Les mesures finales montrent que les seuils définis pour le scénario k6 sont respectés dans l'environnement local de test.

Pendant le test avec 10 utilisateurs virtuels :

```text
0 % d'erreur HTTP
100 % des checks réussis
p95 = 91,47 ms
```

Le test concerne la version actuelle du téléchargement avec protection par mot de passe et vérification BCrypt.

Du côté du front-end, la mesure Lighthouse finale donne :

```text
90/100
```

avec notamment :

```text
LCP = 3,0 s
CLS = 0
TBT = 10 ms
```

Le LCP est passé de :

```text
4,2 s
```

à :

```text
3,0 s
```

entre la mesure initiale et la mesure finale.

Ces résultats permettent de valider les objectifs définis pour l'environnement local de développement.

Ils ne permettent cependant pas de déterminer la capacité maximale de DataShare ni de garantir les mêmes performances dans un environnement de production.
