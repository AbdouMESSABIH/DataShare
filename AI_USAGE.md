# Utilisation de l'intelligence artificielle - DataShare

## 1. Objectif

L'intelligence artificielle a été utilisée comme outil d'assistance au développement et à l'apprentissage.

Elle n'a pas remplacé la compréhension, la validation ou la responsabilité humaine sur le code intégré au projet.

---

## 2. Périmètre de l'utilisation de l'IA

L'intelligence artificielle a notamment été utilisée pour :

- expliquer des notions techniques ;
- analyser des messages d'erreur ;
- aider au diagnostic ;
- proposer des pistes d'implémentation ;
- expliquer du code existant ;
- proposer des commandes de vérification ;
- aider à préparer ou améliorer certains tests ;
- aider à interpréter les résultats de tests ;
- relire certains choix techniques ;
- aider à structurer et réviser la documentation.

Certaines propositions ont pu contenir du code, des commandes ou des exemples de correction.

Ces propositions n'ont pas été considérées comme automatiquement correctes.

Elles ont été :

```text
relues
↓
comprises
↓
adaptées si nécessaire
↓
testées localement
↓
corrigées en cas d'erreur
```

La traçabilité Git historique explicite de code développé avec assistance IA concerne principalement la User Story :

**Télécharger un fichier partagé à partir de son token.**

Elle est associée au commit :

```text
3207354 feat(ai): implement file download by token
```

Une correction issue de la revue humaine est ensuite tracée par :

```text
40e879e fix(download): handle invalid content type after human review
```

Les autres interventions de l'IA correspondent principalement à de l'assistance au diagnostic, à l'explication, aux tests et à la documentation.

Elles ne doivent pas être présentées comme du code historiquement généré par IA lorsqu'aucune trace Git spécifique ne permet de l'établir.

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

La responsabilité des commandes exécutées, du code intégré et des décisions techniques reste humaine.

---

## 4. User Story historiquement tracée

La User Story spécifiquement identifiée dans Git pour l'utilisation de l'IA est :

**Télécharger un fichier partagé à partir de son token.**

Commit d'implémentation :

```text
3207354 feat(ai): implement file download by token
```

Cette fonctionnalité constitue l'exemple historique explicitement traçable de code développé avec assistance IA.

---

## 5. Fonctionnalités concernées par cette User Story

L'assistance IA a contribué à proposer une implémentation permettant notamment :

- de rechercher un fichier avec son token ;
- de vérifier l'existence du token ;
- de vérifier l'expiration du fichier ;
- de retrouver le fichier physique ;
- de construire la réponse de téléchargement ;
- d'exposer les endpoints nécessaires ;
- d'intégrer le parcours frontend correspondant.

Ces propositions ont ensuite été relues et validées sur l'environnement local.

---

## 6. Contrôle fonctionnel

Après l'implémentation, plusieurs scénarios ont été contrôlés :

```text
token valide
→ téléchargement

token inconnu
→ erreur 404

token expiré
→ erreur 410
```

Le fonctionnement côté Angular a également été vérifié.

---

## 7. Relecture humaine

Le code n'a pas été accepté uniquement parce qu'il avait été proposé avec l'assistance de l'IA.

Une relecture a notamment été réalisée sur :

```text
codes HTTP
gestion des erreurs
Content-Type
headers de téléchargement
accès au fichier physique
comportement avec les tokens
```

Cette relecture permet de vérifier que le comportement correspond réellement aux exigences fonctionnelles.

---

## 8. Exemple d'anomalie détectée après assistance IA

Pendant la revue, un problème potentiel a été identifié concernant le `Content-Type`.

Une valeur MIME invalide pouvait provoquer une erreur pendant la construction de la réponse HTTP.

Une correction humaine a donc été réalisée afin de prévoir un fallback :

```text
application/octet-stream
```

Commit historique de correction :

```text
40e879e fix(download): handle invalid content type after human review
```

Cet exemple montre qu'une proposition assistée par IA peut nécessiter une correction après analyse humaine.

---

## 9. État actuel de cette correction

Le comportement corrigé est toujours présent dans la version actuelle du projet.

Le contrôleur de téléchargement tente d'utiliser le `Content-Type` associé au fichier.

Si celui-ci est invalide ou inexploitable, le backend utilise :

```text
application/octet-stream
```

Cette logique est également couverte par un test automatisé.

---

## 10. Évolution de la fonctionnalité

Depuis l'implémentation initiale de la User Story, le projet a évolué.

Le téléchargement prend désormais également en charge la protection des fichiers par mot de passe.

Le mot de passe est transmis avec l'en-tête :

```text
X-Download-Password
```

Le backend vérifie ensuite le mot de passe avec BCrypt.

Cette évolution ne change pas le principe appliqué pendant le développement :

```text
proposition
↓
compréhension
↓
vérification
↓
tests
↓
validation humaine
```

---

## 11. Tests actuels

La validation actuelle du backend comprend :

```text
58 tests
0 échec
0 erreur
```

Le frontend comprend :

```text
52 tests Angular
```

Les tests End-to-End comprennent :

```text
7 scénarios Playwright
7 réussis
```

Ces tests ne prouvent pas que le code est parfait, mais ils permettent de vérifier les comportements attendus et de limiter les régressions après les modifications.

---

## 12. Couverture et contrôles de qualité

La couverture backend mesurée lors de la dernière campagne comprend notamment :

```text
Instructions : 91,64 %
Branches : 72,31 %
Lignes : 91,89 %
```

Les classes spécifiquement vérifiées dans le cadre des corrections comprennent notamment :

```text
FileService
DownloadController
```

Le frontend dispose également d'une couverture supérieure au seuil de 70 % sur les lignes.

Ces métriques sont utilisées comme éléments de contrôle et non comme preuve unique de qualité du code.

---

## 13. Traçabilité Git

Les commits historiques explicitement liés à l'assistance IA sont :

```text
3207354 feat(ai): implement file download by token
40e879e fix(download): handle invalid content type after human review
```

Cette séparation permet d'illustrer le cycle suivant :

```text
proposition assistée
        ↓
implémentation
        ↓
relecture humaine
        ↓
anomalie identifiée
        ↓
correction
        ↓
validation
```

La traçabilité Git explicite ne doit pas être étendue artificiellement à des parties du projet pour lesquelles aucune trace historique spécifique n'existe.

---

## 14. Assistance au diagnostic et aux tests

En dehors de la User Story historiquement tracée, l'IA a également été utilisée comme aide pour :

- comprendre des erreurs de compilation ;
- comprendre des erreurs de démarrage du backend ;
- diagnostiquer des problèmes PostgreSQL ;
- interpréter les résultats Maven ;
- interpréter les résultats Angular ;
- préparer des commandes k6 ;
- analyser les résultats de tests de charge ;
- comparer les performances avec et sans BCrypt ;
- aider à lire les rapports de couverture ;
- structurer certains documents techniques.

Dans ces cas, l'IA intervient principalement comme outil d'explication et d'assistance.

Les résultats réellement observés sur la machine de développement restent la référence pour valider ou rejeter une proposition.

---

## 15. Ce que l'IA n'a pas décidé seule

L'IA n'a pas eu l'autorité finale sur :

- l'intégration du code ;
- les commandes exécutées ;
- la validation fonctionnelle ;
- la conservation d'une solution ;
- les corrections ;
- les commits Git ;
- l'acceptation finale d'une fonctionnalité.

La validation repose sur :

```text
le code
les tests
le comportement observé
les exigences du projet
la compréhension du développeur
```

---

## 16. Principes retenus

L'utilisation de l'IA dans DataShare repose sur les principes suivants :

- ne pas intégrer une proposition sans la comprendre ;
- vérifier le comportement réel du code ;
- exécuter les tests nécessaires ;
- corriger les propositions erronées ;
- ne pas inventer de traçabilité Git ;
- distinguer assistance au développement et code historiquement tracé comme assisté par IA ;
- rester capable d'expliquer le code présenté en soutenance.

---

## Conclusion

L'intelligence artificielle a été utilisée dans DataShare comme outil d'assistance et d'apprentissage.

La User Story de téléchargement par token constitue l'exemple historiquement tracé dans Git de code développé avec assistance IA.

Les autres usages concernent principalement l'explication, le diagnostic, les tests, l'interprétation des résultats et la documentation.

Le fonctionnement retenu reste :

```text
IA comme assistant
        ↓
compréhension humaine
        ↓
relecture
        ↓
tests
        ↓
correction si nécessaire
        ↓
validation humaine
```

L'exemple de la correction du `Content-Type` montre concrètement qu'une proposition assistée par IA peut être incorrecte ou incomplète et doit être vérifiée avant d'être conservée.
