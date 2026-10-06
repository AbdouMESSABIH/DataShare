# Rapport de performance - DataShare

# 1. Objectif

L'objectif de cette analyse est d'évaluer les performances de l'application DataShare sur deux aspects complémentaires :

- les performances du back-end Spring Boot sous charge avec k6 ;
- les performances du front-end Angular dans le navigateur avec Lighthouse.

Ce rapport conserve uniquement les dernières campagnes de mesure disponibles.

Les tests ont été réalisés dans un environnement local de développement.

Ils permettent de vérifier le comportement de l'application et d'identifier d'éventuels problèmes de performance, mais ils ne constituent pas un benchmark représentatif d'un environnement de production.

---

# 2. Environnement de test

Les tests ont été réalisés localement sur la machine de développement.

Les outils k6 et Lighthouse suivent des parcours différents :

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

Le back-end fonctionne en mono-instance.

PostgreSQL et le stockage des fichiers sont locaux.

---

# 3. Test de charge back-end avec k6

## 3.1 Objectif

L'objectif du test est d'évaluer le comportement du back-end Spring Boot sous une charge soutenue de téléchargements.

Deux campagnes comparables ont été exécutées :

- téléchargement de fichiers sans mot de passe ;
- téléchargement de fichiers protégés par mot de passe avec vérification BCrypt.

Cette comparaison permet d'estimer le surcoût associé à la vérification BCrypt lors d'un téléchargement protégé.

La campagne finale a été réalisée le 6 octobre 2026 avec k6.

---

## 3.2 Configuration du scénario

Le scénario utilise plusieurs liens de téléchargement afin de ne pas concentrer toutes les requêtes sur un seul token.

Configuration commune aux deux campagnes :

- durée : 60 secondes ;
- cadence : 1 requête toutes les 2 secondes ;
- 4 liens de téléchargement différents ;
- 31 téléchargements par campagne ;
- endpoint : `GET /api/download/{token}/file` ;
- seuil d'erreur HTTP : inférieur à 1 % ;
- seuil de latence : p95 inférieur à 1 000 ms.

Le scénario k6 utilise l'exécuteur `constant-arrival-rate`.

Les requêtes sont réparties entre les quatre tokens pendant toute la durée du test.

Le scénario a été dimensionné afin de rester compatible avec les mécanismes de limitation de téléchargement de l'application.

---

## 3.3 Campagne sans vérification BCrypt

La première campagne utilise quatre fichiers sans mot de passe.

Dans ce cas, le back-end récupère le fichier sans exécuter de vérification `PasswordEncoder.matches()`.

Résultats :

| Indicateur | Résultat |
|---|---:|
| Durée | 60 s |
| Liens utilisés | 4 |
| Requêtes HTTP | 31 |
| Vérifications réussies | 62/62 |
| Erreurs HTTP | 0 % |
| Temps moyen | 5,50 ms |
| Temps médian | 5,51 ms |
| p90 | 6,33 ms |
| p95 | 6,56 ms |
| Temps minimal | 3,94 ms |
| Temps maximal | 6,85 ms |

Les deux vérifications fonctionnelles ont réussi pour chaque requête :

- statut HTTP 200 ;
- fichier téléchargé non vide.

Les seuils k6 sont respectés :

- taux d'erreur : 0 % ;
- p95 : 6,56 ms, inférieur au seuil de 1 000 ms.

---

## 3.4 Campagne avec vérification BCrypt

La seconde campagne utilise quatre fichiers protégés par le même mot de passe.

Le back-end vérifie le mot de passe à chaque téléchargement avec un `BCryptPasswordEncoder` configuré avec un coût de 12.

La vérification est réalisée avec `PasswordEncoder.matches()`.

Résultats :

| Indicateur | Résultat |
|---|---:|
| Durée | environ 60 s |
| Liens utilisés | 4 |
| Requêtes HTTP | 31 |
| Vérifications réussies | 62/62 |
| Erreurs HTTP | 0 % |
| Temps moyen | 215,05 ms |
| Temps médian | 214,11 ms |
| p90 | 217,99 ms |
| p95 | 219,29 ms |
| Temps minimal | 211,79 ms |
| Temps maximal | 222,00 ms |

Les deux vérifications fonctionnelles ont réussi pour chaque requête :

- statut HTTP 200 ;
- fichier téléchargé non vide.

Les seuils k6 sont respectés :

- taux d'erreur : 0 % ;
- p95 : 219,29 ms, inférieur au seuil de 1 000 ms.

---

# 4. Comparaison du coût BCrypt

Les deux campagnes utilisent :

- le même endpoint ;
- la même durée ;
- la même cadence ;
- quatre liens de téléchargement ;
- le même nombre de requêtes.

La principale différence fonctionnelle entre les deux parcours est la vérification BCrypt effectuée pour les fichiers protégés.

| Indicateur | Sans BCrypt | Avec BCrypt coût 12 | Écart |
|---|---:|---:|---:|
| Moyenne | 5,50 ms | 215,05 ms | +209,55 ms |
| Médiane | 5,51 ms | 214,11 ms | +208,60 ms |
| p90 | 6,33 ms | 217,99 ms | +211,66 ms |
| p95 | 6,56 ms | 219,29 ms | +212,73 ms |
| Maximum | 6,85 ms | 222,00 ms | +215,15 ms |
| Erreurs HTTP | 0 % | 0 % | 0 |

La latence moyenne observée passe de 5,50 ms sans vérification BCrypt à 215,05 ms avec BCrypt configuré avec un coût de 12.

Le surcoût moyen observé est donc d'environ :

```text
215,05 ms - 5,50 ms = 209,55 ms
```

soit environ 210 ms par téléchargement protégé dans cet environnement de test.

La latence moyenne du scénario avec BCrypt est environ 39 fois supérieure à celle du scénario sans vérification BCrypt.

Ce facteur ne doit cependant pas être considéré comme une valeur universelle.

Il dépend notamment :

- de la machine utilisée ;
- de la charge CPU ;
- de la configuration BCrypt ;
- de la taille des fichiers ;
- du nombre de requêtes ;
- de l'environnement d'exécution.

---

# 5. Interprétation du test de charge

La campagne sans mot de passe montre que le téléchargement lui-même présente une latence faible dans l'environnement local de test.

Lorsque la protection par mot de passe est activée, la vérification BCrypt avec un coût de 12 augmente nettement la latence.

Ce comportement est cohérent avec le rôle de BCrypt : l'algorithme est volontairement coûteux en calcul afin de ralentir les tentatives de recherche de mot de passe.

Malgré ce surcoût, le p95 de la campagne protégée reste à 219,29 ms, soit largement sous le seuil de 1 000 ms défini pour cette campagne.

Les deux campagnes ont terminé avec :

```text
31 requêtes
62/62 vérifications réussies
0 % d'erreurs HTTP
```

Le scénario répond ainsi à deux objectifs :

- vérifier le comportement du téléchargement sous une charge soutenue ;
- mesurer comparativement le coût de la vérification BCrypt.

---

# 6. Limites du test k6

Les résultats proviennent d'un environnement local de développement.

Ils ne constituent pas une mesure de capacité maximale du serveur ni un benchmark d'un environnement de production.

Les performances peuvent varier selon :

- la puissance du serveur ;
- la charge CPU ;
- le stockage ;
- la base de données ;
- la taille des fichiers ;
- le nombre de requêtes simultanées ;
- la configuration BCrypt ;
- l'environnement réseau.

Le scénario teste principalement le parcours de téléchargement.

Il ne reproduit pas simultanément les inscriptions, les connexions, les téléversements et les suppressions de fichiers.

La cadence a également été limitée afin de rester compatible avec les mécanismes de rate limiting de l'application.

---

# 7. Reproduction du test k6

Le script utilisé est :

```text
performance/download-test.js
```

## Campagne sans mot de passe

```bash
DOWNLOAD_TOKENS="token1,token2,token3,token4" \
BASE_URL="http://localhost:8080" \
k6 run performance/download-test.js
```

## Campagne avec mot de passe

```bash
DOWNLOAD_TOKENS="token1,token2,token3,token4" \
DOWNLOAD_PASSWORD="<mot-de-passe>" \
BASE_URL="http://localhost:8080" \
k6 run performance/download-test.js
```

Les tokens et les mots de passe utilisés pendant les tests ne doivent pas être enregistrés dans le dépôt Git.

---

# 8. Test de performance front-end avec Lighthouse

## 8.1 Objectif

Lighthouse permet de mesurer les performances de rendu du front-end Angular dans le navigateur.

Contrairement à k6, Lighthouse ne mesure pas la capacité du back-end à supporter plusieurs utilisateurs simultanés.

Il mesure principalement :

- la vitesse d'affichage ;
- la stabilité visuelle ;
- le blocage du thread principal ;
- l'accessibilité ;
- les bonnes pratiques ;
- le référencement technique.

---

## 8.2 Dernière campagne Lighthouse

La dernière campagne Lighthouse conservée dans ce rapport a été réalisée le 1er octobre 2026.

Elle a été effectuée sur le build Angular de production disponible à cette date.

Conditions :

- Lighthouse 13.4.0 ;
- build Angular de production ;
- URL : `http://localhost:4173/` ;
- mode Navigation ;
- profils Mobile et Desktop ;
- aucune alerte d'exécution.

Cette campagne précède les derniers ajustements visuels effectués les 2 et 3 octobre 2026.

Aucun nouveau score Lighthouse n'est revendiqué pour ces modifications ultérieures.

---

## 8.3 Scores Lighthouse

| Catégorie | Mobile | Desktop |
|---|---:|---:|
| Performance | 91/100 | 100/100 |
| Accessibilité | 100/100 | 100/100 |
| Bonnes pratiques | 100/100 | 100/100 |
| SEO | 100/100 | 100/100 |

---

## 8.4 Métriques de performance

| Métrique | Mobile | Desktop |
|---|---:|---:|
| First Contentful Paint (FCP) | 2,7 s | 0,5 s |
| Largest Contentful Paint (LCP) | 2,9 s | 0,6 s |
| Total Blocking Time (TBT) | 14 ms | 0 ms |
| Cumulative Layout Shift (CLS) | 0 | 0 |
| Speed Index | 2,7 s | 0,5 s |

L'audit de la balise meta-description est validé sur les deux profils.

Les mesures montrent de très bons résultats sur Desktop.

Le profil Mobile reste plus coûteux en temps de rendu, notamment pour le FCP et le LCP, mais le score de performance atteint 91/100.

Ces mesures locales ponctuelles ne constituent pas une garantie de performance en production.

---

# 9. Différence entre k6 et Lighthouse

k6 et Lighthouse sont complémentaires mais ne mesurent pas les mêmes éléments.

## k6

k6 mesure principalement le comportement du serveur sous charge.

Dans ce projet, il permet notamment d'observer :

```text
nombre de requêtes
taux d'erreur
temps de réponse
p90
p95
stabilité du back-end
```

Le test k6 concerne directement le back-end Spring Boot.

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
accessibilité
bonnes pratiques
SEO
```

Un bon résultat Lighthouse ne garantit pas que le serveur supportera une charge importante.

De la même manière, un bon résultat k6 ne garantit pas que l'interface sera rapide ou agréable à afficher.

Les deux outils permettent donc d'analyser deux aspects différents de la performance de DataShare.

---

# 10. Budget de performance Angular

## 10.1 Budgets configurés

Le fichier `frontend/angular.json` définit des budgets pour la compilation de production.

| Type de budget | Avertissement | Erreur bloquante |
|---|---:|---:|
| Bundle initial (`initial`) | 500 kB | 1 MB |
| Style individuel (`anyComponentStyle`) | 5 kB | 8 kB |

Ces budgets permettent de détecter une augmentation excessive de la taille de l'application avant sa livraison.

---

## 10.2 Dernier build de production

La dernière mesure conservée correspond au build réalisé le 3 octobre 2026 après les corrections visuelles et l'ajout des formats.

Commande :

```bash
cd frontend
npm run build
```

Résultats :

| Fichier | Taille brute | Transfert estimé |
|---|---:|---:|
| main | 338,81 kB | 84,04 kB |
| polyfills | 34,59 kB | 11,33 kB |
| styles | 7,06 kB | 1,52 kB |
| **Total initial** | **380,46 kB** | **96,89 kB** |

La compilation est réussie.

Le bundle initial de 380,46 kB reste inférieur au seuil d'avertissement Angular de 500 kB.

Aucun avertissement de budget n'est émis pendant cette compilation.

La taille brute du bundle et le transfert estimé correspondent à deux métriques différentes.

Le budget Angular `initial` doit être comparé à la taille brute générée et non uniquement au transfert estimé.

---

# 11. Limites générales de l'analyse

Les mesures de ce rapport ont été réalisées localement.

Elles permettent de comparer des scénarios et de détecter des régressions, mais elles ne permettent pas de prédire exactement le comportement de l'application en production.

Pour une analyse plus complète, il serait possible de compléter ces tests par :

- des campagnes plus longues ;
- différents niveaux de concurrence ;
- différentes tailles de fichiers ;
- un suivi CPU et mémoire du serveur ;
- un environnement de préproduction ;
- des mesures Lighthouse après chaque modification importante de l'interface.

Les résultats présentés ici correspondent aux dernières campagnes effectivement mesurées et conservées dans le rapport.

---

# 12. Conclusion

La campagne k6 finale repose sur une charge soutenue de 60 secondes répartie sur quatre liens de téléchargement.

Deux scénarios identiques ont été comparés :

- téléchargement sans vérification BCrypt ;
- téléchargement protégé avec BCrypt configuré avec un coût de 12.

Les deux campagnes ont exécuté 31 requêtes avec 0 % d'erreurs HTTP.

La latence moyenne observée est de :

```text
Sans BCrypt : 5,50 ms
Avec BCrypt : 215,05 ms
```

Le surcoût moyen observé pour la vérification BCrypt est donc d'environ 210 ms dans l'environnement local de test.

Le p95 du scénario protégé atteint 219,29 ms et reste inférieur au seuil de 1 000 ms défini pour cette campagne.

La campagne permet ainsi d'isoler de manière comparative le coût de BCrypt tout en vérifiant le comportement du téléchargement pendant une charge soutenue sur plusieurs liens.

La dernière campagne Lighthouse disponible obtient :

```text
Mobile : 91/100
Desktop : 100/100
```

Enfin, le dernier build Angular présente un bundle initial de 380,46 kB, inférieur au seuil d'avertissement de 500 kB.

k6, Lighthouse et les budgets Angular apportent ainsi trois niveaux complémentaires de contrôle des performances de DataShare.
