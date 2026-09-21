# Rapport de performance - DataShare

## Objectif

L'objectif est d'évaluer le comportement de DataShare sous charge et de vérifier les performances du frontend après optimisation.

Les mesures ont été réalisées dans un environnement local de développement. Elles ne constituent pas un benchmark de production.

## 1. Test de charge backend avec k6

### Endpoint testé

```text
GET /api/download/{token}/file
```

Le backend était lancé localement sur `http://localhost:8080`.

### Scénario

- 10 utilisateurs virtuels simultanés ;
- durée : 20 secondes ;
- téléchargement répété d'un fichier existant ;
- vérification du statut HTTP ;
- vérification que la réponse n'est pas vide.

### Seuils

```text
http_req_failed: rate < 1 %
http_req_duration: p(95) < 1000 ms
```

### Dernier résultat validé

```text
161 545 requêtes HTTP
0 % d'erreur
p95 : 1,44 ms
environ 8 076,9 requêtes/s
```

Les seuils définis sont respectés dans cet environnement local.

## 2. Performance frontend avec Lighthouse

Une mesure Lighthouse a été réalisée sur le frontend.

### Avant optimisation

```text
Performance : 82/100
FCP : 2,6 s
LCP : 4,2 s
TBT : 0 ms
CLS : 0
```

Le LCP constituait le principal axe d'amélioration.

### Après optimisation

Après optimisation des ressources de l'interface, une nouvelle mesure a donné :

```text
Performance : 91/100
FCP : 2,6 s
LCP : 3,0 s
TBT : 0 ms
CLS : 0
```

Le score de performance et le LCP se sont donc améliorés.

## 3. Limites de l'analyse

Les résultats dépendent de la machine, du navigateur, de la charge système et de l'environnement local.

Le stockage des fichiers est local et l'application fonctionne actuellement en mono-instance. Les résultats ne doivent donc pas être extrapolés directement à une architecture distribuée ou à un environnement de production.
