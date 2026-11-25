import { expect, test } from '@playwright/test';
import { MOT_DE_PASSE, choisirLangue, creerMembre, liker, sansViolation } from './outils';

test.describe('Membre', () => {
  test('connexion, like réciproque, match et message en direct', async ({ page }) => {
    // deux membres dans une ville où personne d'autre ne vit : Akureyri (Islande)
    const elle = await creerMembre({ genre: 'FEMME', cherche: 'HOMMES', ville: 'akureyri' });
    const lui = await creerMembre({ genre: 'HOMME', cherche: 'FEMMES', ville: 'akureyri' });
    await liker(lui, elle);

    await page.goto('/connexion');
    await choisirLangue(page, 'fr');
    await page.getByLabel('Adresse e-mail').fill(elle.email);
    await page.getByLabel('Mot de passe', { exact: true }).fill(MOT_DE_PASSE);
    await page.getByRole('button', { name: 'Se connecter' }).click();
    await expect(page).toHaveURL(/\/decouvrir/);

    // passer les éventuels membres laissés par d'anciens essais, jusqu'à lui
    const carte = page.locator('article.carte h2');
    for (let i = 0; i < 20 && !(await carte.textContent())?.startsWith(lui.prenom); i++) {
      await page.getByRole('button', { name: 'Passer' }).click();
    }
    await expect(carte).toContainText(lui.prenom);
    await expect(page.locator('lm-tissage .bande')).toHaveAttribute('aria-label', /^Compatibilité \d+ sur 100/);
    await sansViolation(page);

    await page.getByRole('button', { name: 'J’aime' }).click();
    const match = page.getByRole('dialog', { name: 'C’est réciproque' });
    await expect(match).toBeVisible();
    await match.getByRole('link', { name: `Écrire à ${lui.prenom}` }).click();

    const zone = page.getByRole('textbox', { name: `Écrire à ${lui.prenom}` });
    await zone.fill('Bonjour depuis les tests de bout en bout');
    await zone.press('Enter');
    await expect(page.getByRole('log')).toContainText('Bonjour depuis les tests de bout en bout');
    await sansViolation(page);
  });

  test('démonstration : score détaillé, conversation, profil et export des données', async ({ page }) => {
    await page.goto('/');
    await choisirLangue(page, 'fr');
    await page.getByRole('button', { name: 'Essayer sans compte' }).click();
    await expect(page).toHaveURL(/\/decouvrir/);
    await expect(page.locator('lm-tissage .legende > div')).toHaveCount(5);

    await page.goto('/matchs');
    await page.getByRole('link', { name: /Mamy/ }).click();
    await expect(page.getByRole('log')).toContainText('romazava');

    await page.goto('/profil');
    await expect(page.getByRole('combobox', { name: 'Ville' })).toHaveValue('Antananarivo, Madagascar');
    const telechargement = page.waitForEvent('download');
    await page.getByRole('link', { name: 'Télécharger mes données' }).click();
    expect((await telechargement).suggestedFilename()).toBe('lovematch-mes-donnees.json');
    await sansViolation(page);
  });

  test('modération : signalement à traiter', async ({ page }) => {
    await page.goto('/');
    await choisirLangue(page, 'fr');
    await page.getByRole('button', { name: 'Ouvrir l’espace de modération' }).click();
    await expect(page).toHaveURL(/\/admin/);
    await expect(page.getByRole('heading', { level: 1 })).toHaveText('Modération');
    await sansViolation(page);
  });
});
