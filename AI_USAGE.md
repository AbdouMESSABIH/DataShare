# Utilisation de l'intelligence artificielle - DataShare

## 1. Objectif

L'intelligence artificielle a été utilisée comme outil d'assistance au développement et à l'apprentissage.

Elle n'a pas remplacé la compréhension, la validation ou la responsabilité humaine sur le code intégré au projet.

---

## 2. Utilisation générale

L'IA a notamment été utilisée pour :

- expliquer des notions techniques ;
- analyser des erreurs ;
- aider au diagnostic ;
- proposer des pistes d'implémentation ;
- expliquer du code ;
- aider à écrire ou améliorer certains tests ;
- relire des choix techniques ;
- aider à structurer la documentation.

Les propositions n'ont pas été considérées comme automatiquement correctes.

### Périmètre complémentaire de l'assistance

Au-delà de la User Story historique consacrée au téléchargement
par token, l'assistance IA a également été utilisée pendant
l'évolution générale de DataShare pour :

- proposer et expliquer certaines modifications Java et Angular ;
- préparer, adapter et analyser des tests automatisés ;
- aider à diagnostiquer les erreurs de compilation et d'exécution ;
- proposer des commandes de contrôle Maven, npm, k6 et Lighthouse ;
- accompagner l'analyse des rapports de sécurité npm et OWASP ;
- contribuer à la rédaction et à la révision des documents
  `API.md`, `TESTING.md`, `SECURITY.md`, `MAINTENANCE.md`
  et `PERF.md` ;
- suggérer des corrections de cohérence entre le code
  et la documentation.

Certaines propositions comprenaient du code, des commandes
ou des paragraphes directement réutilisables. Elles ont été
examinées et vérifiées à partir des résultats réellement
observés sur l'environnement local avant leur conservation.

La User Story décrite dans les sections suivantes demeure
l'exemple historique spécifiquement tracé dans Git.
Elle ne représente pas la totalité des interventions de l'IA.



---

## 3. Responsabilité humaine

Avant de conserver une proposition, la démarche appliquée est :

```text
comprendre le besoin
        ↓
examiner la proposition
        ↓
relire le code
        ↓
exécuter les tests
        ↓
vérifier les erreurs
        ↓
corriger si nécessaire
        ↓
relancer la non-régression
```

La responsabilité des commandes exécutées et du code intégré reste humaine.

---

## 4. User Story tracée

La User Story spécifiquement identifiée pour l'utilisation de l'IA est :

**Télécharger un fichier partagé à partir de son token.**

Le développement correspondant a été tracé dans Git.

Commit d'implémentation :

```text
feat(ai): implement file download by token
```

---

## 5. Fonctionnalités concernées

L'assistance IA a notamment contribué à proposer une implémentation permettant :

- de rechercher un fichier avec son token ;
- de vérifier l'existence du token ;
- de vérifier l'expiration ;
- de retrouver le fichier physique ;
- de construire la réponse de téléchargement ;
- d'exposer les endpoints nécessaires ;
- d'intégrer le parcours frontend correspondant.

---

## 6. Contrôle fonctionnel

Après l'implémentation, plusieurs scénarios ont été contrôlés :

```text
token valide
→ téléchargement

token inconnu
→ 404

token expiré
→ 410
```

Le fonctionnement côté Angular a également été vérifié.

---

## 7. Relecture humaine

Le code n'a pas été accepté uniquement parce qu'il avait été proposé avec l'assistance de l'IA.

Une relecture a été réalisée sur :

```text
codes HTTP
gestion des erreurs
Content-Type
headers de téléchargement
accès au fichier physique
comportement avec les tokens
```

---

## 8. Anomalie détectée

Pendant la revue, un problème potentiel a été identifié concernant le Content-Type.

Une valeur MIME invalide pouvait provoquer une erreur lors de la construction de la réponse HTTP.

Une correction humaine a donc été réalisée pour prévoir un fallback :

```text
application/octet-stream
```

Commit historique de correction :

```text
fix(download): handle invalid content type after human review
```

---

## 9. État actuel de cette correction

Le comportement est toujours présent dans la version actuelle du projet.

Le contrôleur de téléchargement tente d'utiliser le Content-Type du fichier.

Si celui-ci est invalide ou inexploitable, le backend utilise :

```text
application/octet-stream
```

Cette logique est désormais également couverte par un test automatisé.

---

## 10. Évolution du projet

Depuis l'implémentation initiale de la User Story, le projet a évolué.

Le téléchargement prend maintenant également en charge la protection des fichiers par mot de passe.

Le mot de passe est envoyé via :

```text
X-Download-Password
```

et contrôlé par le backend avec BCrypt.

Cette évolution ne change pas le principe de la revue humaine appliquée au code.

---

## 11. Tests actuels

La dernière validation du backend (3 octobre 2026) comprend :

```text
58 tests
0 échec
0 erreur
```

Le frontend comprend :

```text
44 tests Angular (3 octobre 2026)
```

Les tests End-to-End comprennent :

```text
3 scénarios Playwright
```

Les tests ne prouvent pas que le code est parfait, mais ils constituent un moyen de contrôler les propositions et de limiter les régressions.

---

## 12. Analyse statique et qualité

Les contrôles finaux comprennent notamment :

```text
ESLint : succès
SpotBugs : 0 bug / 0 erreur
```

Couverture backend finale :

```text
Instructions : 91,64 % (1930 / 2106)
Branches : 72,31 % (175 / 242)
Lignes : 91,89 % (612 / 666)
```

---

## 13. Traçabilité Git

Les commits historiques permettent de distinguer :

```text
3207354 feat(ai): implement file download by token
40e879e fix(download): handle invalid content type after human review
```

Cette séparation montre :

```text
proposition initiale
→ revue
→ anomalie identifiée
→ correction
```

---

## 14. Ce que l'IA n'a pas décidé seule

L'IA n'a pas eu l'autorité finale sur :

- l'intégration du code ;
- les commandes exécutées ;
- la validation fonctionnelle ;
- la conservation d'une solution ;
- les corrections ;
- les commits Git.

La validation reste réalisée à partir :

```text
du code
des tests
du comportement observé
des exigences du projet
```

---

## Conclusion

L'utilisation de l'IA dans DataShare suit un principe de supervision humaine :

```text
IA comme assistant
        ↓
compréhension humaine
        ↓
relecture
        ↓
tests
        ↓
correction
        ↓
traçabilité
```

L'exemple du Content-Type invalide montre concrètement qu'une proposition assistée par IA peut nécessiter une correction après analyse humaine.
