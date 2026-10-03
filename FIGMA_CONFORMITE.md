# DataShare — Conformité aux maquettes Figma

## 1. Objectif et preuve visuelle

Ce document présente la comparaison entre les maquettes Figma de DataShare et l'application développée, sur ordinateur et sur mobile. Il explicite les différences conservées afin de distinguer les choix fonctionnels volontaires d'éventuels écarts de présentation.

**Capture comparative unique :** [DataShare_comparaison.png](reports/evidence/figma/DataShare_comparaison.png)

**Légende de la capture :**

- **Encadré vert** : application DataShare réalisée.
- **Sans encadré vert** : maquette Figma originale.

La capture globale est conservée dans sa résolution d'origine : le lecteur peut zoomer pour examiner chaque groupe d'écrans. Il n'est pas nécessaire de dupliquer les mêmes images dans deux dossiers séparés.

## 2. Correspondance avec les maquettes

Les principaux parcours présentés sont l'accueil, la connexion, l'inscription, le téléversement (sélection, formulaire et confirmation), le téléchargement et l'espace personnel (ordinateur et mobile).

La réalisation reprend la palette pêche/corail, les cartes et boutons, les icônes de téléversement, les indications d'expiration, le cadenas des fichiers protégés ainsi que la navigation adaptée au format de l'écran. L'historique distingue les fichiers actifs et expirés, avec un menu d'actions compact sur mobile.

## 3. Écarts conservés et justifications

| Élément | Maquette originale | Application réalisée | Justification |
|---|---|---|---|
| Bouton principal de l'en-tête | Le bouton « Se connecter » apparaît sur plusieurs écrans de démonstration. | L'en-tête dépend de la page et de l'état de connexion : « Se connecter » depuis l'inscription, « Créer un compte » depuis la connexion ; après authentification, accès à « Mon espace » et/ou « Déconnexion » selon l'écran et le format. | Proposer une action pertinente au contexte et éviter d'afficher « Se connecter » à un utilisateur déjà authentifié. Sur mobile, les actions privées complémentaires se trouvent dans le rideau de navigation. |
| Navigation dans « Mon espace » sur mobile | Le rideau présente notamment « Mes fichiers ». | Un bouton supplémentaire « Ajouter des fichiers » figure juste sous « Mes fichiers », avec la même taille et le même style. | Permettre de lancer un nouveau téléversement depuis l'historique, puisque l'accès « Ajouter des fichiers » de l'en-tête ordinateur n'était plus directement visible sur mobile. Le bouton ouvre `/upload` et referme le rideau. |
| Filtre initial de l'historique | Certaines vues illustrent le filtre « Tous » sélectionné. | Le filtre « Actifs » est sélectionné par défaut ; « Tous » et « Expirés » restent accessibles. | Respect de la règle fonctionnelle US06 : afficher les fichiers non expirés par défaut. |
| Fichiers et dates affichés | Les maquettes utilisent des exemples illustratifs. | Les noms, états de protection et dates proviennent des fichiers réellement téléversés et de l'API. | Les données doivent refléter le comportement de l'application, et non reproduire artificiellement les exemples statiques de Figma. |
| Page Téléchargement | Présentation illustrative de l'accès au fichier. | L'icône circulaire décorative a été retirée ; la page conserve le titre, les caractéristiques du fichier, l'information ou l'alerte d'expiration et le bouton de téléchargement. | Simplifier la lecture sans supprimer une information ni une action fonctionnelle. Le champ de mot de passe n'apparaît que pour un fichier protégé. |

### Formats désormais pris en charge

MP3, MP4 et ZIP ont été ajoutés aux cinq formats initiaux.
Le backend vérifie leurs signatures initiales et neuf tests
supplémentaires couvrent les fichiers valides et déguisés.

Ces formats ne constituent donc plus un écart fonctionnel
avec les exemples représentés dans Figma.

## 4. Typographie

La maquette référence une police nommée **« Main Font »**. La réalisation utilise la pile CSS `Arial, Helvetica, sans-serif`, visuellement proche dans les écrans comparés. Le nom « Main Font » visible dans Figma ne suffit pas, à lui seul, à démontrer une correspondance exacte de famille de police : cette différence potentielle est signalée plutôt que présentée comme une conformité typographique au pixel près.

## 5. Conclusion

La capture comparative documente l'alignement visuel général des écrans réalisés avec Figma. Les différences listées ci-dessus sont conservées pour des raisons de parcours utilisateur, de respect des spécifications fonctionnelles, de sécurité ou de lisibilité. La comparaison visuelle ne remplace pas les tests fonctionnels et techniques, consignés séparément dans `TESTING.md`.
