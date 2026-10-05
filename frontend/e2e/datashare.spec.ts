import {
    test,
    expect
} from '@playwright/test';


test.setTimeout(
    60_000
);


test(
    'parcours complet DataShare avec fichier protégé par mot de passe',
    async ({ page }) => {

        const email =
            `e2e-${Date.now()}@test.com`;

        const accountPassword =
            'Password123';

        const filePassword =
            'SecretFile123!';

        const fileName =
            'e2e-test.txt';

        const fileContent =
            'Test E2E DataShare';


        // ----------------------------------------
        // 1. Création du compte
        // ----------------------------------------

        await page.goto(
            '/register'
        );


        await page
            .locator(
                'input[type="email"]'
            )
            .fill(
                email
            );


        const registerPasswordInputs =
            page.locator(
                'input[type="password"]'
            );


        for (
            let i = 0;
            i <
            await registerPasswordInputs.count();
            i++
        ) {

            await registerPasswordInputs
                .nth(i)
                .fill(
                    accountPassword
                );
        }


        const registerResponsePromise =
            page.waitForResponse(
                response =>
                    response
                        .url()
                        .includes(
                            '/api/auth/register'
                        )
                    &&
                    response
                        .request()
                        .method()
                    === 'POST'
            );


        await page
            .locator(
                'button[type="submit"]'
            )
            .click();


        const registerResponse =
            await registerResponsePromise;


        expect(
            registerResponse.status()
        ).toBe(
            201
        );


        // ----------------------------------------
        // 2. Connexion
        // ----------------------------------------

        await page.goto(
            '/login'
        );


        await page
            .locator(
                'input[type="email"]'
            )
            .fill(
                email
            );


        await page
            .locator(
                'input[type="password"]'
            )
            .fill(
                accountPassword
            );


        const loginResponsePromise =
            page.waitForResponse(
                response =>
                    response
                        .url()
                        .includes(
                            '/api/auth/login'
                        )
                    &&
                    response
                        .request()
                        .method()
                    === 'POST'
            );


        await page
            .locator(
                'button[type="submit"]'
            )
            .click();


        const loginResponse =
            await loginResponsePromise;


        expect(
            loginResponse.status()
        ).toBe(
            200
        );


        const jwtToken =
            await page.evaluate(
                () =>
                    localStorage.getItem(
                        'token'
                    )
            );


        expect(
            jwtToken
        ).not.toBeNull();


        // ----------------------------------------
        // 3. Upload du fichier protégé
        // ----------------------------------------

        await page.goto(
            '/upload'
        );


        await page
            .locator(
                'input[type="file"]'
            )
            .setInputFiles({
                name:
                    fileName,

                mimeType:
                    'text/plain',

                buffer:
                    Buffer.from(
                        fileContent
                    )
            });


        await page
            .locator(
                '#password'
            )
            .fill(
                filePassword
            );


        const uploadResponsePromise =
            page.waitForResponse(
                response =>
                    response
                        .url()
                        .includes(
                            '/api/files/upload'
                        )
                    &&
                    response
                        .request()
                        .method()
                    === 'POST'
            );


        await page
            .locator(
                'button[type="submit"]'
            )
            .click();


        const uploadResponse =
            await uploadResponsePromise;


        expect(
            uploadResponse.status()
        ).toBe(
            201
        );


        const uploadResult =
            await uploadResponse.json();


        expect(
            uploadResult.downloadToken
        ).toBeTruthy();


        // ----------------------------------------
        // 4. Historique
        // ----------------------------------------

        await page.goto(
            '/history'
        );


        await expect(
            page.getByText(
                fileName
            )
        ).toBeVisible();


        // ----------------------------------------
        // 5. Page de téléchargement
        // ----------------------------------------

        await page.goto(
            `/download/${uploadResult.downloadToken}`
        );


        await expect(
            page.getByText(
                fileName
            )
        ).toBeVisible();


        await expect(
            page.locator(
                '#downloadPassword'
            )
        ).toBeVisible();


        // ----------------------------------------
        // 6. Mauvais mot de passe
        //
        // Ici on teste le comportement visible
        // pour l'utilisateur.
        // ----------------------------------------

        await page
            .locator(
                '#downloadPassword'
            )
            .fill(
                'MauvaisMotDePasse'
            );


        await page
            .getByRole(
                'button',
                {
                    name:
                        'Télécharger le fichier'
                }
            )
            .click();


        await expect(
            page.getByText(
                'Mot de passe incorrect'
            )
        ).toBeVisible();


        // ----------------------------------------
        // 7. Bon mot de passe
        //    + téléchargement réel
        // ----------------------------------------

        await page
            .locator(
                '#downloadPassword'
            )
            .fill(
                filePassword
            );


        const downloadPromise =
            page.waitForEvent(
                'download'
            );


        await page
            .getByRole(
                'button',
                {
                    name:
                        'Télécharger le fichier'
                }
            )
            .click();


        const download =
            await downloadPromise;


        expect(
            download.suggestedFilename()
        ).toBe(
            fileName
        );


        // ----------------------------------------
        // 8. Suppression
        // ----------------------------------------

        await page.goto(
            '/history'
        );


        page.once(
            'dialog',

            async dialog => {

                await dialog.accept();
            }
        );


        const deleteResponsePromise =
            page.waitForResponse(
                response =>
                    response
                        .url()
                        .includes(
                            '/api/files/'
                        )
                    &&
                    response
                        .request()
                        .method()
                    === 'DELETE'
            );


        await page
            .getByRole(
                'button',
                {
                    name:
                        'Supprimer'
                }
            )
            .click();


        const deleteResponse =
            await deleteResponsePromise;


        expect(
            deleteResponse.status()
        ).toBe(
            204
        );


        await expect(
            page.getByText(
                fileName
            )
        ).not.toBeVisible();


        await expect(
            page.getByText(
                'Aucun fichier pour le moment.'
            )
        ).toBeVisible();
    }
);


test(
    'refuse la connexion avec un mauvais mot de passe',
    async ({ page }) => {

        const email =
            `e2e-error-${Date.now()}@test.com`;

        const password =
            'Password123';


        // ----------------------------------------
        // Création d'un compte valide
        // ----------------------------------------

        await page.goto(
            '/register'
        );


        await page
            .locator(
                'input[type="email"]'
            )
            .fill(
                email
            );


        const registerPasswordInputs =
            page.locator(
                'input[type="password"]'
            );


        for (
            let i = 0;
            i <
            await registerPasswordInputs.count();
            i++
        ) {

            await registerPasswordInputs
                .nth(i)
                .fill(
                    password
                );
        }


        const registerResponsePromise =
            page.waitForResponse(
                response =>
                    response
                        .url()
                        .includes(
                            '/api/auth/register'
                        )
                    &&
                    response
                        .request()
                        .method()
                    === 'POST'
            );


        await page
            .locator(
                'button[type="submit"]'
            )
            .click();


        const registerResponse =
            await registerResponsePromise;


        expect(
            registerResponse.status()
        ).toBe(
            201
        );


        // ----------------------------------------
        // Mauvais mot de passe utilisateur
        // ----------------------------------------

        await page.goto(
            '/login'
        );


        await page
            .locator(
                'input[type="email"]'
            )
            .fill(
                email
            );


        await page
            .locator(
                'input[type="password"]'
            )
            .fill(
                'MauvaisMotDePasse123'
            );


        const loginResponsePromise =
            page.waitForResponse(
                response =>
                    response
                        .url()
                        .includes(
                            '/api/auth/login'
                        )
                    &&
                    response
                        .request()
                        .method()
                    === 'POST'
            );


        await page
            .locator(
                'button[type="submit"]'
            )
            .click();


        const loginResponse =
            await loginResponsePromise;


        expect(
            loginResponse.status()
        ).toBe(
            401
        );


        await expect(
            page.getByText(
                'Email ou mot de passe incorrect'
            )
        ).toBeVisible();


        const token =
            await page.evaluate(
                () =>
                    localStorage.getItem(
                        'token'
                    )
            );


        expect(
            token
        ).toBeNull();
    }
);


test(
    'affiche une erreur pour un lien de téléchargement invalide',
    async ({ page }) => {

        const invalidToken =
            `token-invalide-${Date.now()}`;


        const responsePromise =
            page.waitForResponse(
                response =>
                    response
                        .url()
                        .includes(
                            `/api/download/${invalidToken}`
                        )
                    &&
                    response
                        .request()
                        .method()
                    === 'GET'
            );


        await page.goto(
            `/download/${invalidToken}`
        );


        const response =
            await responsePromise;


        expect(
            response.status()
        ).toBe(
            404
        );


        await expect(
            page.getByText(
                'Lien de téléchargement invalide'
            )
        ).toBeVisible();
    }
);


test(
    'accueil visiteur permet de naviguer vers connexion et inscription',
    async ({ page }) => {

        await page.goto('/');

        await expect(
            page.getByRole(
                'heading',
                {
                    name: 'Tu veux partager un fichier ?'
                }
            )
        ).toBeVisible();

        await expect(
            page.getByRole(
                'link',
                {
                    name: 'Se connecter'
                }
            ).first()
        ).toBeVisible();

        await expect(
            page.getByRole(
                'link',
                {
                    name: 'Créer un compte'
                }
            ).first()
        ).toBeVisible();


        await page
            .getByRole(
                'link',
                {
                    name: 'Se connecter'
                }
            )
            .first()
            .click();

        await expect(page).toHaveURL(/\/login$/);


        await page.goto('/');

        await page
            .getByRole(
                'link',
                {
                    name: 'Créer un compte'
                }
            )
            .first()
            .click();

        await expect(page).toHaveURL(/\/register$/);
    }
);


test(
    'permet un upload sans mot de passe et un téléchargement direct',
    async ({ page }) => {

        const email =
            `e2e-public-${Date.now()}@test.com`;

        const accountPassword =
            'Password123';

        const fileName =
            `e2e-public-${Date.now()}.txt`;

        const fileContent =
            'Test E2E sans mot de passe';


        // Création du compte
        await page.goto('/register');

        await page
            .locator('input[type="email"]')
            .fill(email);

        const registerPasswordInputs =
            page.locator('input[type="password"]');

        for (
            let i = 0;
            i < await registerPasswordInputs.count();
            i++
        ) {
            await registerPasswordInputs
                .nth(i)
                .fill(accountPassword);
        }

        const registerResponsePromise =
            page.waitForResponse(
                response =>
                    response.url().includes('/api/auth/register')
                    && response.request().method() === 'POST'
            );

        await page
            .locator('button[type="submit"]')
            .click();

        expect(
            (await registerResponsePromise).status()
        ).toBe(201);


        // Connexion
        await page.goto('/login');

        await page
            .locator('input[type="email"]')
            .fill(email);

        await page
            .locator('input[type="password"]')
            .fill(accountPassword);

        const loginResponsePromise =
            page.waitForResponse(
                response =>
                    response.url().includes('/api/auth/login')
                    && response.request().method() === 'POST'
            );

        await page
            .locator('button[type="submit"]')
            .click();

        expect(
            (await loginResponsePromise).status()
        ).toBe(200);


        // Upload volontairement sans mot de passe
        await page.goto('/upload');

        await page
            .locator('input[type="file"]')
            .setInputFiles({
                name: fileName,
                mimeType: 'text/plain',
                buffer: Buffer.from(fileContent)
            });

        await expect(
            page.locator('#password')
        ).toHaveValue('');

        const uploadResponsePromise =
            page.waitForResponse(
                response =>
                    response.url().includes('/api/files/upload')
                    && response.request().method() === 'POST'
            );

        await page
            .getByRole(
                'button',
                {
                    name: 'Téléverser'
                }
            )
            .click();

        const uploadResponse =
            await uploadResponsePromise;

        expect(uploadResponse.status()).toBe(201);

        const uploadResult =
            await uploadResponse.json();

        expect(
            uploadResult.downloadToken
        ).toBeTruthy();


        // Page publique de téléchargement
        await page.goto(
            `/download/${uploadResult.downloadToken}`
        );

        await expect(
            page.getByText(fileName)
        ).toBeVisible();

        await expect(
            page.locator('#downloadPassword')
        ).toHaveCount(0);


        // Téléchargement direct
        const downloadPromise =
            page.waitForEvent('download');

        await page
            .getByRole(
                'button',
                {
                    name: 'Télécharger le fichier'
                }
            )
            .click();

        const download =
            await downloadPromise;

        expect(
            download.suggestedFilename()
        ).toBe(fileName);
    }
);


test(
    'historique sélectionne le filtre Actifs par défaut',
    async ({ page }) => {

        const email =
            `e2e-filter-${Date.now()}@test.com`;

        const password =
            'Password123';

        const fileName =
            `e2e-filter-${Date.now()}.txt`;


        // Création du compte
        await page.goto('/register');

        await page
            .locator('input[type="email"]')
            .fill(email);

        const registerPasswordInputs =
            page.locator('input[type="password"]');

        for (
            let i = 0;
            i < await registerPasswordInputs.count();
            i++
        ) {
            await registerPasswordInputs
                .nth(i)
                .fill(password);
        }

        const registerResponsePromise =
            page.waitForResponse(
                response =>
                    response.url().includes('/api/auth/register')
                    && response.request().method() === 'POST'
            );

        await page
            .locator('button[type="submit"]')
            .click();

        expect(
            (await registerResponsePromise).status()
        ).toBe(201);


        // Connexion
        await page.goto('/login');

        await page
            .locator('input[type="email"]')
            .fill(email);

        await page
            .locator('input[type="password"]')
            .fill(password);

        const loginResponsePromise =
            page.waitForResponse(
                response =>
                    response.url().includes('/api/auth/login')
                    && response.request().method() === 'POST'
            );

        await page
            .locator('button[type="submit"]')
            .click();

        expect(
            (await loginResponsePromise).status()
        ).toBe(200);


        // Il faut au moins un fichier pour afficher la barre de filtres
        await page.goto('/upload');

        await page
            .locator('input[type="file"]')
            .setInputFiles({
                name: fileName,
                mimeType: 'text/plain',
                buffer: Buffer.from(
                    'Test filtre actif E2E'
                )
            });

        const uploadResponsePromise =
            page.waitForResponse(
                response =>
                    response.url().includes('/api/files/upload')
                    && response.request().method() === 'POST'
            );

        await page
            .getByRole(
                'button',
                {
                    name: 'Téléverser'
                }
            )
            .click();

        expect(
            (await uploadResponsePromise).status()
        ).toBe(201);


        // Vérification du filtre par défaut
        await page.goto('/history');

        await expect(
            page.getByText(fileName)
        ).toBeVisible();

        await expect(
            page.getByRole(
                'button',
                {
                    name: 'Actifs'
                }
            )
        ).toHaveAttribute(
            'aria-pressed',
            'true'
        );

        await expect(
            page.getByRole(
                'button',
                {
                    name: 'Tous'
                }
            )
        ).toHaveAttribute(
            'aria-pressed',
            'false'
        );

        await expect(
            page.getByRole(
                'button',
                {
                    name: 'Expirés'
                }
            )
        ).toHaveAttribute(
            'aria-pressed',
            'false'
        );
    }
);


test(
    'déconnexion supprime le token et redirige vers la connexion',
    async ({ page }) => {

        const email =
            `e2e-logout-${Date.now()}@test.com`;

        const password =
            'Password123';


        // Création du compte
        await page.goto('/register');

        await page
            .locator('input[type="email"]')
            .fill(email);

        const registerPasswordInputs =
            page.locator('input[type="password"]');

        for (
            let i = 0;
            i < await registerPasswordInputs.count();
            i++
        ) {
            await registerPasswordInputs
                .nth(i)
                .fill(password);
        }

        const registerResponsePromise =
            page.waitForResponse(
                response =>
                    response.url().includes('/api/auth/register')
                    && response.request().method() === 'POST'
            );

        await page
            .locator('button[type="submit"]')
            .click();

        expect(
            (await registerResponsePromise).status()
        ).toBe(201);


        // Connexion
        await page.goto('/login');

        await page
            .locator('input[type="email"]')
            .fill(email);

        await page
            .locator('input[type="password"]')
            .fill(password);

        const loginResponsePromise =
            page.waitForResponse(
                response =>
                    response.url().includes('/api/auth/login')
                    && response.request().method() === 'POST'
            );

        await page
            .locator('button[type="submit"]')
            .click();

        expect(
            (await loginResponsePromise).status()
        ).toBe(200);


        // JWT présent avant la déconnexion
        const tokenBeforeLogout =
            await page.evaluate(
                () =>
                    localStorage.getItem('token')
            );

        expect(
            tokenBeforeLogout
        ).not.toBeNull();


        await page.goto('/history');

        await page
            .getByRole(
                'button',
                {
                    name: 'Déconnexion'
                }
            )
            .click();


        // Redirection
        await expect(page).toHaveURL(/\/login$/);


        // JWT supprimé
        const tokenAfterLogout =
            await page.evaluate(
                () =>
                    localStorage.getItem('token')
            );

        expect(
            tokenAfterLogout
        ).toBeNull();


        // Une route privée doit maintenant être inaccessible
        await page.goto('/history');

        await expect(page).toHaveURL(/\/login$/);
    }
);

