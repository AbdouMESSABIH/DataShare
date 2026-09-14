# Performance - DataShare

## Objectif

Ce document décrit les vérifications de performance réalisées sur DataShare.

Les objectifs sont de :

- mesurer les performances d'un endpoint backend critique ;
- vérifier la stabilité du backend sous charge ;
- mesurer le poids du build frontend ;
- analyser les performances du frontend avec Lighthouse ;
- définir un budget de performance ;
- identifier et mettre en œuvre une optimisation mesurable ;
- conserver des valeurs de référence pour détecter les régressions.

Les résultats présentés dans ce document ont été obtenus dans un environnement local.

Ils constituent des références de développement et de non-régression.

Ils ne représentent pas un benchmark d'une infrastructure de production.

---

# 1. Performance backend

## Outil utilisé

Le test de charge backend est réalisé avec :

```text
k6
```

Le scénario est disponible dans :

```text
performance/download-test.js
```

---

## Endpoint testé

Le test porte sur le téléchargement d'un fichier à partir de son token.

Le parcours correspond à l'endpoint :

```http
GET /api/download/{token}/file
```

Le téléchargement a été retenu car il s'agit d'une fonctionnalité importante de DataShare et qu'elle sollicite à la fois :

- l'API Spring Boot ;
- la logique métier ;
- l'accès aux métadonnées ;
- le stockage physique du fichier.

---

## Configuration du test

Le scénario a été exécuté avec :

```text
10 utilisateurs virtuels
Durée : 20 secondes
```

Les seuils définis dans le scénario permettent notamment de surveiller :

- le taux d'erreur ;
- le temps de réponse HTTP.

Les objectifs principaux étaient :

```text
Taux d'erreur < 1 %
p95 < 1000 ms
```

---

## Résultats k6

Résultats obtenus lors de la validation :

```text
Requêtes HTTP : 171 362
Erreurs : 0
Taux d'erreur : 0 %

Temps moyen : 1,08 ms
Médiane : 1,06 ms
Maximum : 5,21 ms

p90 : 1,27 ms
p95 : 1,37 ms

Débit : environ 8 568 requêtes / seconde
Données reçues : environ 88 Mo
```

Les checks définis dans le scénario ont réussi.

---

## Interprétation

Les résultats montrent que, dans cet environnement local et avec le fichier utilisé
pour le scénario de test, l'endpoint de téléchargement reste stable pendant la
charge appliquée.

Le taux d'erreur observé est :

```text
0 %
```

Le p95 observé est :

```text
1,37 ms
```

Il reste donc largement inférieur au seuil défini de :

```text
1000 ms
```

Ces chiffres ne doivent cependant pas être interprétés comme les performances
attendues d'une infrastructure de production.

Le test a été réalisé :

- sur une machine locale ;
- sans latence réseau réelle ;
- sans infrastructure distribuée ;
- avec un nombre limité d'utilisateurs virtuels ;
- avec un fichier de test de petite taille.

Le résultat sert principalement de référence de non-régression.

---

# 2. Performance frontend

## Build de production

Le frontend Angular est compilé en mode production avec :

```bash
cd ~/Projets/DataShare/frontend
npm run build
```

Résultat observé :

```text
Initial chunk files   | Names         | Raw size  | Estimated transfer size
main-XWRLC2ML.js      | main          | 294.10 kB | 75.92 kB
polyfills-5CFQRCPP.js | polyfills     | 34.59 kB  | 11.33 kB
styles-ASBQMRW5.css   | styles        | 1.04 kB   | 286 bytes

Initial total                        | 329.72 kB | 87.54 kB
```

Le build a été généré dans :

```text
frontend/dist/frontend
```

---

## Interprétation du bundle

Le bundle initial représente environ :

```text
329,72 kB
```

avec un transfert estimé à :

```text
87,54 kB
```

Cette mesure permet de suivre l'évolution du poids du frontend.

Une augmentation importante de ce chiffre lors d'une évolution future devra être
analysée afin d'éviter une dégradation progressive du temps de chargement.

---

# 3. Lighthouse - mesure initiale

Le build de production a été servi localement sur :

```text
http://localhost:4173
```

La mesure Lighthouse initiale a donné :

```text
Performance : 82 / 100

FCP : 2,6 s
LCP : 4,2 s
TBT : 0 ms
CLS : 0
Speed Index : 2,6 s
```

---

## Analyse initiale

Les résultats étaient globalement corrects pour le MVP.

Les points positifs étaient :

```text
TBT : 0 ms
CLS : 0
```

Cela signifie notamment que le navigateur ne présentait pas de blocage JavaScript
important ni de déplacement significatif de la mise en page pendant le chargement.

Le principal axe d'amélioration identifié était :

```text
LCP : 4,2 s
```

Le Largest Contentful Paint était donc le premier indicateur à surveiller.

---

# 4. Recherche d'une optimisation

Une analyse des ressources du frontend a permis d'identifier le logo principal :

```text
frontend/public/assets/datashare-logo.png.png
```

Avant optimisation, ses caractéristiques étaient :

```text
Dimensions : 781 x 569 px
Poids : 308 Ko
Format : PNG RGBA
```

Dans l'interface, ce logo est affiché au maximum à environ :

```text
125 x 125 px
```

et sur une taille d'écran plus petite :

```text
100 x 100 px
```

Le fichier source était donc beaucoup plus grand que nécessaire par rapport à sa
taille réelle d'affichage.

---

# 5. Optimisation du logo

Le logo a été redimensionné en conservant :

- son format PNG ;
- ses proportions ;
- sa transparence ;
- son apparence dans l'interface ;
- son nom de fichier ;
- son emplacement dans le projet.

Après optimisation :

```text
Dimensions : 250 x 182 px
Poids : 34 Ko
```

Le poids du fichier est passé de :

```text
308 Ko
```

à :

```text
34 Ko
```

soit une réduction d'environ :

```text
89 %
```

Le HTML et les styles de l'application n'ont pas été modifiés pour cette
optimisation.

Le logo reste référencé par :

```html
<img
    src="/assets/datashare-logo.png.png"
    alt="Logo DataShare"
    class="brand-logo"
>
```

L'interface a ensuite été contrôlée visuellement afin de vérifier l'absence de
régression.

Aucune différence visuelle gênante n'a été constatée.

---

# 6. Lighthouse après optimisation

Après l'optimisation, le frontend a été reconstruit avec :

```bash
cd ~/Projets/DataShare/frontend
npm run build
```

Le build de production a ensuite été servi localement sur le port :

```text
4173
```

Une nouvelle mesure Lighthouse a été réalisée dans les mêmes conditions générales.

Résultats :

```text
Performance : 91 / 100
FCP : 2,6 s
LCP : 3,0 s
TBT : 0 ms
CLS : 0
```

---

# 7. Comparaison avant / après

| Indicateur | Avant | Après | Évolution |
|---|---:|---:|---:|
| Poids du logo | 308 Ko | 34 Ko | environ -89 % |
| Score Lighthouse | 82 / 100 | 91 / 100 | +9 points |
| FCP | 2,6 s | 2,6 s | stable |
| LCP | 4,2 s | 3,0 s | -1,2 s |
| TBT | 0 ms | 0 ms | stable |
| CLS | 0 | 0 | stable |

Le LCP est passé de :

```text
4,2 s
```

à :

```text
3,0 s
```

soit une amélioration d'environ :

```text
29 %
```

---

## Interprétation

L'optimisation du logo constitue une amélioration réelle et mesurable du frontend.

Le poids de la ressource a été réduit d'environ 89 % sans modifier visiblement
l'interface.

La deuxième mesure Lighthouse montre également :

- une amélioration du score Performance ;
- une réduction du LCP ;
- un FCP stable ;
- aucun temps de blocage supplémentaire ;
- aucun déplacement de mise en page.

Il n'est cependant pas possible d'affirmer que la totalité de l'amélioration
Lighthouse provient uniquement du logo.

Les mesures Lighthouse peuvent légèrement varier d'une exécution à une autre.

La réduction du poids du fichier constitue néanmoins une optimisation technique
objective et indépendante du score Lighthouse.

---

# 8. Budget de performance frontend

Pour limiter les régressions futures, les valeurs suivantes servent de références.

## Bundle Angular

Référence actuelle :

```text
Initial total : 329,72 kB
Transfert estimé : 87,54 kB
```

Objectif :

- surveiller toute augmentation importante du bundle ;
- analyser les nouvelles dépendances avant leur ajout ;
- éviter l'ajout de bibliothèques lourdes sans justification.

---

## Lighthouse

Référence actuelle après optimisation :

```text
Performance : 91 / 100
FCP : 2,6 s
LCP : 3,0 s
TBT : 0 ms
CLS : 0
```

Objectifs de suivi :

- maintenir un score Performance proche ou supérieur à 90 lorsque possible ;
- conserver un TBT faible ;
- conserver un CLS proche de 0 ;
- surveiller le LCP ;
- éviter une augmentation inutile du poids des ressources.

Ces valeurs constituent un budget de référence pour le MVP et non un engagement
de performance de production.

---

# 9. Budget de performance backend

Pour le scénario k6 actuel, les seuils définis sont :

```text
Taux d'erreur < 1 %
p95 < 1000 ms
```

Résultat de référence :

```text
Taux d'erreur : 0 %
p95 : 1,37 ms
```

Une évolution future du backend peut être comparée à ces résultats pour détecter
une régression.

---

# 10. Pistes d'amélioration futures

Pour une évolution vers un environnement de production, plusieurs optimisations
pourraient être étudiées.

## Frontend

- continuer à réduire le LCP ;
- surveiller le poids des bundles ;
- utiliser le lazy loading lorsque pertinent ;
- optimiser les images et autres ressources statiques ;
- analyser les nouvelles dépendances avant leur ajout ;
- vérifier régulièrement Lighthouse après les évolutions importantes.

## Backend

- tester avec plusieurs tailles de fichiers ;
- augmenter progressivement le nombre d'utilisateurs virtuels ;
- exécuter les tests sur une infrastructure séparée ;
- mesurer la consommation CPU et mémoire ;
- analyser les performances PostgreSQL ;
- surveiller les temps d'accès au stockage.

## Infrastructure

Pour une mise en production :

- utiliser un stockage objet comme AWS S3 ou équivalent ;
- ajouter HTTPS ;
- utiliser un reverse proxy ;
- ajouter une supervision ;
- centraliser les métriques ;
- ajouter une chaîne CI/CD ;
- effectuer des tests de charge dans un environnement proche de la production.

---

# 11. Régénérer les mesures

## Build frontend

```bash
cd ~/Projets/DataShare/frontend
npm run build
```

---

## Servir le build de production

```bash
cd ~/Projets/DataShare/frontend

python3 -m http.server 4173 -d dist/frontend/browser
```

L'application est alors accessible sur :

```text
http://localhost:4173
```

---

## Lighthouse

Exemple de génération d'un rapport JSON :

```bash
cd ~/Projets/DataShare/frontend

npx lighthouse http://localhost:4173 \
  --only-categories=performance \
  --output=json \
  --output-path=lighthouse-after.json \
  --chrome-flags="--headless=new"
```

Les résultats principaux peuvent être lus avec :

```bash
node -e "
const r=require('./lighthouse-after.json');
console.log('Performance :', Math.round(r.categories.performance.score*100));
console.log('FCP :', r.audits['first-contentful-paint'].displayValue);
console.log('LCP :', r.audits['largest-contentful-paint'].displayValue);
console.log('TBT :', r.audits['total-blocking-time'].displayValue);
console.log('CLS :', r.audits['cumulative-layout-shift'].displayValue);
"
```

Les rapports Lighthouse générés localement sont des fichiers temporaires et ne sont
pas destinés à être versionnés dans Git.

---

## Test k6

Le scénario backend est disponible dans :

```text
performance/download-test.js
```

Avant le test, le backend doit être démarré et le scénario doit utiliser un token
de téléchargement valide.

La commande d'exécution est :

```bash
k6 run performance/download-test.js
```

---

# 12. Bilan

La validation des performances de DataShare repose sur plusieurs mesures
complémentaires :

```text
Backend
  ↓
k6
  ↓
temps de réponse / erreurs / débit

Frontend
  ↓
build Angular
  ↓
poids du bundle

Frontend
  ↓
Lighthouse
  ↓
FCP / LCP / TBT / CLS

Optimisation
  ↓
mesure avant
  ↓
réduction du poids du logo
  ↓
mesure après
```

La campagne de performance a permis :

- de vérifier la stabilité locale du téléchargement sous charge ;
- de mesurer le poids du frontend ;
- d'identifier le LCP comme axe d'amélioration ;
- d'optimiser une ressource frontend réellement surdimensionnée ;
- de réduire le poids du logo d'environ 89 % ;
- d'améliorer le score Lighthouse de 82 à 91 ;
- de réduire le LCP de 4,2 s à 3,0 s ;
- de définir des références permettant de détecter de futures régressions.

Les résultats restent liés à l'environnement local et doivent être complétés par
des mesures sur une infrastructure réelle avant toute conclusion concernant les
performances de production.