# DataShare — Conformité aux maquettes Figma

## 1. Objectif et preuve visuelle

Ce document présente la comparaison entre les maquettes Figma de DataShare et l'application développée, sur ordinateur et sur mobile.

Il explicite également les différences conservées afin de distinguer :

- les écarts corrigés ;
- les choix fonctionnels volontaires ;
- les différences de présentation justifiées.

**Capture comparative :**  
[DataShare_comparaison.png](reports/evidence/figma/DataShare_comparaison.png)

**Légende de la capture :**

- **Encadré vert** : application DataShare réalisée.
- **Sans encadré vert** : maquette Figma originale.

La capture comparative est conservée dans sa résolution d'origine afin de permettre au lecteur de zoomer et d'examiner les différents écrans.

## 2. Correspondance avec les maquettes

Les principaux parcours comparés sont :

- l'accueil ;
- la connexion ;
- l'inscription ;
- le téléversement d'un fichier ;
- le formulaire de téléversement ;
- la confirmation du partage ;
- le téléchargement ;
- l'espace personnel ;
- l'affichage ordinateur ;
- l'affichage mobile.

La réalisation reprend notamment :

- la palette pêche / corail ;
- les cartes blanches ;
- les boutons principaux ;
- les icônes de téléversement ;
- les informations d'expiration ;
- le cadenas indiquant un fichier protégé ;
- la navigation adaptée à la largeur de l'écran ;
- le menu compact d'actions dans l'historique mobile.

L'historique permet également de distinguer les fichiers actifs et expirés.

## 3. Tableau des écarts et corrections

| Élément | Maquette originale / attente | Application réalisée | État / justification |
|---|---|---|---|
| Bouton principal de l'en-tête | Le bouton « Se connecter » apparaît sur plusieurs écrans de démonstration. | L'en-tête dépend de la page et de l'état de connexion : « Se connecter » depuis l'inscription, « Créer un compte » depuis la connexion ; après authentification, accès à « Mon espace » et/ou « Déconnexion » selon l'écran et le format. | **Écart conservé volontairement.** Une action cohérente avec l'état réel de l'utilisateur évite notamment d'afficher « Se connecter » lorsqu'il est déjà authentifié. |
| Navigation dans « Mon espace » sur mobile | Le rideau présente notamment « Mes fichiers ». | Un accès permettant de revenir au téléversement est disponible depuis la navigation mobile. | **Écart conservé volontairement.** Il garantit l'accès aux fonctions principales malgré l'espace réduit sur mobile. |
| Filtre initial de l'historique | Certaines vues Figma présentent « Tous » comme filtre sélectionné. | Le filtre « Actifs » est sélectionné par défaut ; « Tous » et « Expirés » restent disponibles. | **Écart conservé volontairement.** Le comportement respecte la règle fonctionnelle consistant à présenter les fichiers non expirés en priorité. |
| Fichiers et dates affichés | Les maquettes utilisent des noms de fichiers et des dates d'exemple. | Les noms, dates, états d'expiration et protections proviennent réellement de l'API. | **Écart normal.** Les données affichées sont dynamiques et reflètent les fichiers réellement téléversés. |
| Page de téléchargement | Présentation illustrative de l'accès au fichier dans Figma. | La page conserve les informations utiles, l'état d'expiration, la protection éventuelle et l'action de téléchargement. | **Écart conservé volontairement.** La présentation a été simplifiée sans supprimer de fonctionnalité. |
| Formats acceptés | Les formats doivent être compréhensibles pour l'utilisateur lors du téléversement. | Le formulaire indique désormais clairement : « Formats acceptés : TXT, PDF, PNG, JPG, JPEG, MP3, MP4 et ZIP — 1 Go maximum. » | **Corrigé.** Le texte d'aide est placé directement dans la carte de téléversement, sous le choix de durée d'expiration. |
| MP3, MP4 et ZIP | Les formats supplémentaires devaient être intégrés au parcours de téléversement. | L'input de sélection accepte `.mp3`, `.mp4` et `.zip`, en complément de TXT, PDF, PNG, JPG et JPEG. | **Corrigé.** Ces formats sont désormais explicitement proposés dans l'interface et pris en charge par l'application. |
| Nom de fichier très long sur mobile | Un nom très long ne doit pas déformer une carte ou rendre l'historique difficile à lire. | Sur mobile, un nom dépassant la longueur prévue est raccourci en conservant son début et sa fin, par exemple : `rapport-projet-devops.....jhvgvvyufvu.pdf`. | **Corrigé.** Le début et l'extension restent identifiables tout en évitant un débordement ou une carte anormalement haute. |
| Actions de fichier sur mobile | Les actions doivent rester accessibles malgré la largeur réduite. | Les actions sont regroupées dans un menu compact `⋮`. | **Conforme au besoin responsive.** La carte reste lisible et les actions restent accessibles. |
| Fichier protégé | Le caractère protégé du fichier doit être identifiable. | Un cadenas est affiché lorsqu'un fichier possède une protection par mot de passe. | **Conforme.** L'information reste visible dans l'historique, y compris sur mobile. |

## 4. Formats pris en charge

Les formats actuellement proposés dans l'interface sont :

- TXT ;
- PDF ;
- PNG ;
- JPG ;
- JPEG ;
- MP3 ;
- MP4 ;
- ZIP.

Les formats MP3, MP4 et ZIP ont été ajoutés aux cinq formats initiaux.

Le backend contrôle également les fichiers afin d'éviter qu'un simple changement d'extension permette de faire accepter un type de fichier non autorisé.

Le texte d'aide visible dans le formulaire est désormais :

> Formats acceptés : TXT, PDF, PNG, JPG, JPEG, MP3, MP4 et ZIP — 1 Go maximum.

Cette information est affichée directement dans la carte de téléversement.

## 5. Gestion des noms longs sur mobile

Une vérification spécifique a été réalisée sur l'historique mobile avec des noms de fichiers volontairement très longs.

L'affichage complet provoquait auparavant une augmentation excessive de la hauteur de la carte.

Le comportement a été corrigé afin de conserver :

- le début du nom ;
- des points de troncature au centre ;
- la fin du nom et son extension.

Exemple :

```text
Nom réel :
rapport-projet-devops-version-finale-corrigee-avec-un-nom-vraiment-tres-long-jhvgvvyufvu.pdf

Affichage mobile :
rapport-projet-devops.....jhvgvvyufvu.pdf
```

Le nom original enregistré côté application n'est pas modifié : seule sa représentation visuelle mobile est raccourcie.

## 6. Typographie

La maquette Figma référence un style nommé **« Main Font »**.

L'application utilise actuellement la pile CSS :

```css
Arial, Helvetica, sans-serif
```

Le libellé « Main Font » visible dans Figma ne permet pas, à lui seul, de déterminer avec certitude la famille de police réellement configurée dans la maquette.

Il serait donc incorrect d'affirmer une conformité typographique exacte sans disposer de la propriété `font-family` de l'élément Figma.

La typographie actuelle est visuellement proche des écrans de référence, mais cette correspondance est documentée comme une **différence potentielle restant à vérifier**, et non comme une conformité au pixel près.

## 7. Vérification responsive

Les écrans ont été vérifiés en affichage mobile, notamment sur une largeur proche de **393 px**.

Les éléments contrôlés comprennent :

- l'en-tête ;
- le menu mobile ;
- la carte de téléversement ;
- le texte d'aide des formats ;
- l'historique ;
- le cadenas des fichiers protégés ;
- le menu d'actions `⋮` ;
- les noms de fichiers longs.

Les corrections apportées évitent désormais que les noms exceptionnellement longs déforment la présentation de l'historique.

## 8. Conclusion

La comparaison avec les maquettes Figma montre un alignement visuel général de l'application DataShare avec les écrans de référence.

Les écarts encore présents dans le tableau sont documentés et correspondent principalement à des choix fonctionnels ou responsive volontaires.

Les deux anomalies d'interface identifiées lors de la revue ont été corrigées :

- l'aide présentant les formats acceptés est désormais correctement placée dans le formulaire de téléversement ;
- les noms de fichiers très longs sont désormais adaptés à l'affichage mobile.

La typographie reste documentée avec prudence tant que la famille exacte associée au style Figma « Main Font » n'a pas été identifiée.

La comparaison visuelle ne remplace pas les tests fonctionnels et techniques, qui sont consignés séparément dans `TESTING.md`.
