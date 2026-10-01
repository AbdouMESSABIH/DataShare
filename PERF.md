# Rapport de performance - DataShare

# 1. Objectif

L'objectif de cette analyse est d'évaluer les performances de l'application DataShare sur deux aspects complémentaires :

- les performances du back-end Spring Boot sous charge avec k6 ;
- les performances du front-end Angular dans le navigateur avec Lighthouse.

Ce rapport présente les tests k6 et les nouvelles mesures Lighthouse du 1er octobre 2026 sur l'interface Angular actualisée. Les campagnes précédentes sont conservées à titre historique.

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

Mesurer les performances du téléchargement protégé par mot
de passe avec k6.

## 3.2 Configuration du scénario

Date : 1er octobre 2026.

- 10 utilisateurs virtuels (VUs).
- 20 itérations partagées au total.
- Durée maximale configurée : 60 secondes.
- Endpoint : GET /api/download/{token}/file.
- Mot de passe transmis avec X-Download-Password.
- Vérification BCrypt côté serveur (coût 12).

Le scénario tient compte des nouvelles protections :
60 téléchargements/minute/IP et 30/minute/token.

## 3.3 Seuils et vérifications

Seuils de performance configurés dans k6 :

- http_req_failed inférieur à 1 %.
- http_req_duration p95 inférieur à 1 000 ms.

Vérifications fonctionnelles exécutées à chaque téléchargement :

- statut HTTP 200 ;
- fichier téléchargé non vide.

## 3.4 Résultats réels

| Indicateur | Résultat |
|---|---:|
| Téléchargements | 20/20 |
| Vérifications réussies | 40/40 |
| Erreurs HTTP | 0 % |
| Temps moyen | 348,74 ms |
| Temps médian | 316,14 ms |
| p90 | 474,06 ms |
| p95 | 475,39 ms |
| Temps minimal | 222,52 ms |
| Temps maximal | 476,74 ms |
| Débit observé | 25,136 req/s |
| Durée effective | environ 0,8 seconde |
| Code retour k6 | 0 |

Les deux seuils configurés sont respectés.

## 3.5 Interprétation

Les 20 téléchargements protégés ont réussi.

Ce scénario mesure un pic court et non une charge
soutenue de 20 secondes.

L'ancienne campagne (2569 requêtes, p95 de 91,47 ms)
a été réalisée avant les dernières modifications de sécurité.

Ces deux campagnes ne sont pas directement comparables.

## 3.6 Reproduction

Exécuter performance/download-test.js avec les variables :

- DOWNLOAD_TOKEN : token valide et récent.
- DOWNLOAD_PASSWORD : mot de passe du fichier.
- BASE_URL : http://localhost:8080.

Les identifiants ne doivent pas être enregistrés dans Git.

Exemple de commande (avec un token récent) :

```bash
DOWNLOAD_TOKEN="<token-valide>" \
DOWNLOAD_PASSWORD="<mot-de-passe>" \
BASE_URL="http://localhost:8080" \
k6 run performance/download-test.js
```


---

# 4. Méthodologie k6

Le test utilise 10 utilisateurs virtuels et 20 itérations
partagées au total (shared-iterations).

Chaque itération effectue un téléchargement protégé.

Deux vérifications sont réalisées :
- statut HTTP 200 ;
- contenu téléchargé non vide.

---

# 5. Analyse des résultats k6

Le test du 1er octobre 2026 a obtenu :

- 20 téléchargements réussis ;
- 40 vérifications réussies sur 40 ;
- 0 % d'erreurs HTTP ;
- temps moyen : 348,74 ms ;
- p95 : 475,39 ms.

Les seuils configurés sont respectés :
- taux d'erreur inférieur à 1 % ;
- p95 inférieur à 1 000 ms.

---

# 6. Limitations de téléchargement

Le backend applique les protections suivantes :

- 60 téléchargements par minute et par IP ;
- 30 téléchargements par minute et par token.

Une réponse HTTP 429 indique que la limite est atteinte.

Le nouveau scénario utilise seulement 20 téléchargements
afin de rester sous la limite par token, en utilisant
un token récent dont le quota n'est pas déjà consommé.

### Vérification réelle du HTTP 429 — 1er octobre 2026

Un test fonctionnel a été exécuté avec 31 téléchargements
successifs du même fichier protégé, pendant une seule fenêtre
de limitation.

Résultats observés :

| Requêtes | Résultat |
|---|---:|
| 1 à 30 | HTTP 200 |
| 31 | HTTP 429 |
| Autres erreurs | 0 |
| Durée du test | 7 secondes |

Résultat : TEST RATE LIMITING RÉUSSI.

Le filtre refuse bien la 31e requête du même token
pendant une fenêtre de limitation.

---

# 7. Comparaison avec l'ancien scénario

L'ancienne campagne comportait 2 569 requêtes et
un p95 de 91,47 ms.

Le scénario a depuis évolué avec les protections
de téléchargement et la configuration BCrypt.

Les deux campagnes ne sont donc pas directement
comparables pour mesurer une éventuelle régression.

Le nouveau test constitue un pic court d'environ
0,8 seconde, et non une charge soutenue de 20 secondes.

---

# 8. Test de performance front-end avec Lighthouse

Les sections 9 à 12 conservent les mesures historiques. La nouvelle campagne Mobile et Desktop figure en section 12.1.


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

# 10. Dernière mesure historique Lighthouse

Lors de la précédente campagne de mesures, Lighthouse a été exécuté sur le build Angular de production. Il ne s'agit pas d'une mesure de la dernière version de l'interface.

URL testée :

```text
http://localhost:4173
```

Résultat de cette campagne :

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

# 11. Interprétation des métriques Lighthouse — campagne historique

Les valeurs de cette section correspondent à l'ancienne
campagne Lighthouse (Performance : 90/100).

Les derniers résultats, datés du 1er octobre 2026,
figurent dans la section 12.1 : 91/100 sur Mobile
et 100/100 sur Desktop.

## Performance

Résultat :

```text
90/100
```

Le score de cette campagne Lighthouse atteint 90 sur 100 dans l'environnement de test.

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

Résultat de la campagne précédente :

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

| Métrique | Avant | Dernière mesure historique |
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

## 12.1 Nouvelle campagne Lighthouse — 1er octobre 2026

Une nouvelle analyse a été réalisée après les dernières
modifications Angular et l'ajout de la balise meta description.

Conditions :
- Lighthouse 13.4.0 ;
- build Angular de production ;
- URL : http://localhost:4173/ ;
- mode Navigation ;
- profils Mobile et Desktop ;
- aucune alerte d'exécution.

### Scores obtenus

| Catégorie | Mobile | Desktop |
|---|---:|---:|
| Performance | 91/100 | 100/100 |
| Accessibilité | 100/100 | 100/100 |
| Bonnes pratiques | 100/100 | 100/100 |
| SEO | 100/100 | 100/100 |

### Métriques de performance

| Métrique | Mobile | Desktop |
|---|---:|---:|
| FCP | 2,7 s | 0,5 s |
| LCP | 2,9 s | 0,6 s |
| TBT (valeur numérique JSON) | 14 ms | 0 ms |
| CLS | 0 | 0 |
| Speed Index | 2,7 s | 0,5 s |

L'audit meta-description est validé sur les deux profils.

Ces mesures locales ponctuelles ne constituent pas
une garantie de performance en production.

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
10 utilisateurs virtuels
20 téléchargements au total
durée effective : environ 0,8 seconde
```

Il ne constitue pas un test de stress ni un test permettant de déterminer la capacité maximale de l'application.

---

# 15. Limites de l'analyse actualisée

Le test k6 a été exécuté sur une machine locale avec :
- une instance Spring Boot ;
- PostgreSQL et le stockage local ;
- 10 utilisateurs virtuels ;
- seulement 20 téléchargements ;
- un fichier protégé par mot de passe.

La durée effective d'environ 0,8 seconde ne permet
pas de conclure sur la stabilité sous charge prolongée.

---

# 16. Limites du scénario k6

Ce test concerne uniquement les téléchargements protégés.

Il ne reproduit pas simultanément les inscriptions,
connexions, téléversements et suppressions.

Le comportement HTTP 429 a été vérifié séparément et validé.

---

# 17. Améliorations possibles

- Tester une charge soutenue compatible avec les limites.
- Conserver une preuve du test HTTP 429 dans le dossier de validation.
- Tester différentes tailles de fichiers.
- Répéter les mesures Lighthouse pour observer leur variabilité.
- Comparer plusieurs campagnes dans des conditions identiques.

---

# 18. Résumé des résultats actualisés

## Backend : k6, 1er octobre 2026

- 10 VUs.
- 20 téléchargements réussis.
- 40/40 vérifications réussies.
- 0 % d'erreurs HTTP.
- Temps moyen : 348,74 ms.
- p95 : 475,39 ms.
- Seuil p95 inférieur à 1000 ms : respecté.

## Frontend : Lighthouse, 1er octobre 2026

| Catégorie | Mobile | Desktop |
|---|---:|---:|
| Performance | 91/100 | 100/100 |
| Accessibilité | 100/100 | 100/100 |
| Bonnes pratiques | 100/100 | 100/100 |
| SEO | 100/100 | 100/100 |

La balise meta description est désormais validée.

---

# 19. Conclusion

Les seuils k6 sont respectés pendant le nouveau scénario
local de 20 téléchargements protégés.

Ce résultat ne permet pas d'estimer la capacité maximale
du serveur ni sa stabilité sous charge prolongée.
