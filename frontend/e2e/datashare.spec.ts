import { test, expect } from '@playwright/test';

test('parcours complet DataShare', async ({ page }) => {

    const email =
        `e2e-${Date.now()}@test.com`;

    const password =
        'Password123';

    const fileName =
        'e2e-test.txt';

    const fileContent =
        'Test E2E DataShare';


    // ----------------------------------------
    // 1. Création du compte
    // ----------------------------------------

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


    const registerResponse =
        await registerResponsePromise;


    expect(
        registerResponse.status()
    ).toBe(201);


    // ----------------------------------------
    // 2. Connexion
    // ----------------------------------------

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


    const loginResponse =
        await loginResponsePromise;


    expect(
        loginResponse.status()
    ).toBe(200);


    const token =
        await page.evaluate(
            () => localStorage.getItem('token')
        );


    expect(token).not.toBeNull();


    // ----------------------------------------
    // 3. Upload
    // ----------------------------------------

    await page.goto('/upload');


    await page
        .locator('input[type="file"]')
        .setInputFiles({
            name: fileName,
            mimeType: 'text/plain',
            buffer: Buffer.from(fileContent)
        });



    const uploadResponsePromise =
        page.waitForResponse(
            response =>
                response.url().includes('/api/files/upload')
                && response.request().method() === 'POST'
        );


    await page
        .locator('button[type="submit"]')
        .click();


    const uploadResponse =
        await uploadResponsePromise;


    expect(
        uploadResponse.status()
    ).toBe(201);


    const uploadResult =
        await uploadResponse.json();


    expect(
        uploadResult.downloadToken
    ).toBeTruthy();


    // ----------------------------------------
    // 4. Historique
    // ----------------------------------------

    await page.goto('/history');


    await expect(
        page.getByText(fileName)
    ).toBeVisible();


    // ----------------------------------------
    // 5. Page de partage / téléchargement
    // ----------------------------------------

    await page.goto(
        `/download/${uploadResult.downloadToken}`
    );


    await expect(
        page.getByText(fileName)
    ).toBeVisible();


    // ----------------------------------------
    // 6. Téléchargement réel
    // ----------------------------------------

    const downloadPromise =
        page.waitForEvent('download');


    await page
        .locator('a[href$="/file"]')
        .click();


    const download =
        await downloadPromise;


    expect(
        download.suggestedFilename()
    ).toBe(fileName);


    // ----------------------------------------
    // 7. Suppression
    // ----------------------------------------

    await page.goto('/history');


    page.once(
        'dialog',
        async dialog => {

            await dialog.accept();
        }
    );


    const deleteResponsePromise =
        page.waitForResponse(
            response =>
                response.url().includes('/api/files/')
                && response.request().method() === 'DELETE'
        );


    await page
        .getByRole(
            'button',
            { name: 'Supprimer' }
        )
        .click();


    const deleteResponse =
        await deleteResponsePromise;


    expect(
        deleteResponse.status()
    ).toBe(204);


    await expect(
        page.getByText(fileName)
    ).not.toBeVisible();


    await expect(
        page.getByText('Aucun fichier pour le moment.')
    ).toBeVisible();
});