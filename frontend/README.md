# Frontend DataShare

Ce dossier contient l'interface Angular de DataShare.

La documentation générale du projet se trouve dans :

```text
../README.md
../API.md
../TESTING.md
../PERF.md
```

## Technologies

```text
Angular
TypeScript
HTML
SCSS
Angular TestBed
Karma
ESLint
Playwright
```

## Installation

Depuis le dossier `frontend` :

```bash
npm install
```

## Serveur de développement

```bash
npm start
```

L'application est alors accessible sur :

```text
http://localhost:4200
```

Le backend DataShare doit être démarré séparément sur :

```text
http://localhost:8080
```

## Build de production

```bash
npm run build
```

Le build est généré dans :

```text
dist/frontend/
```

## Lint

```bash
npx ng lint
```

Dernière validation :

```text
All files pass linting.
```

## Tests unitaires

```bash
npx ng test --watch=false
```

Dernière validation :

```text
40 SUCCESS (1er octobre 2026)
```

## Tests End-to-End

DataShare utilise Playwright pour les tests End-to-End.

Le backend et le frontend doivent être démarrés avant leur exécution.

Commande :

```bash
npx playwright test
```

Dernière validation :

```text
3 passed
```

Les scénarios couvrent notamment :

```text
parcours complet avec fichier protégé par mot de passe
connexion avec mauvais mot de passe
lien de téléchargement invalide
```

Le parcours principal vérifie :

```text
Inscription
→ Connexion
→ Upload
→ Mot de passe fichier
→ Historique
→ Mauvais mot de passe
→ Bon mot de passe
→ Téléchargement
→ Suppression
```

## Performance frontend

La mesure finale Lighthouse du build de production est documentée dans :

```text
../PERF.md
```

Dernière campagne Lighthouse — 1er octobre 2026 :

| Catégorie | Mobile | Desktop |
|---|---:|---:|
| Performance | 91/100 | 100/100 |
| Accessibilité | 100/100 | 100/100 |
| Bonnes pratiques | 100/100 | 100/100 |
| SEO | 100/100 | 100/100 |

| Métrique | Mobile | Desktop |
|---|---:|---:|
| FCP | 2,7 s | 0,5 s |
| LCP | 2,9 s | 0,6 s |
| TBT (valeur JSON) | 14 ms | 0 ms |
| CLS | 0 | 0 |
| Speed Index | 2,7 s | 0,5 s |

Mesures locales ponctuelles sur le build de production.
Les campagnes précédentes sont conservées dans `../PERF.md`.
