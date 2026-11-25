import { expect, test } from '@playwright/test';
import { choisirLangue, sansViolation } from './outils';

test.describe('Visiteur', () => {
  test('accueil : promesse, score en cinq brins, accessible', async ({ page }) => {
    await page.goto('/');
    await choisirLangue(page, 'fr');
    await expect(page.getByRole('heading', { level: 1 })).toHaveText('Des rencontres qui commencent par ce que vous partagez.');
    await expect(page.locator('.brins li')).toHaveCount(5);
    await expect(page.getByRole('img', { name: /Compatibilité 78 sur 100/ })).toBeVisible();
    await sansViolation(page);
  });

  test('sept langues, arabe de droite à gauche', async ({ page }) => {
    await page.goto('/');
    for (const [code, titre] of [
      ['en', 'Dating that starts with what you share.'],
      ['zh', '从你们的共同点开始的相遇。'],
      ['mg', 'Fihaonana manomboka amin’izay iraisanareo.'],
    ]) {
      await choisirLangue(page, code);
      await expect(page.getByRole('heading', { level: 1 })).toHaveText(titre);
    }
    await choisirLangue(page, 'ar');
    await expect(page.locator('html')).toHaveAttribute('dir', 'rtl');
    await sansViolation(page);
    await choisirLangue(page, 'fr');
    await expect(page.locator('html')).toHaveAttribute('dir', 'ltr');
  });

  test('inscription : ville du monde au clavier, mot de passe affichable, profil à compléter', async ({ page }) => {
    await page.goto('/inscription');
    await choisirLangue(page, 'en');
    await page.getByLabel('First name').fill('Haruto');
    await page.getByLabel('Surname').fill('Testeur');
    await page.getByLabel('Date of birth').fill('1997-03-21');
    await page.getByText('A man', { exact: true }).click();
    await page.getByText('Women', { exact: true }).click();

    const ville = page.getByRole('combobox', { name: 'Town' });
    await ville.fill('tok');
    await expect(page.getByRole('listbox').getByRole('option').first()).toContainText('Tokyo');
    await ville.press('Enter');
    await expect(ville).toHaveValue('Tokyo, Japan');

    await page.getByLabel('Email address').fill(`e2e-${Date.now()}@test.lovematch`);
    const motDePasse = page.getByLabel('Password', { exact: true });
    await motDePasse.fill('un-mot-de-passe-solide');
    await page.getByRole('button', { name: 'Show' }).click();
    await expect(motDePasse).toHaveAttribute('type', 'text');
    await page.getByRole('checkbox').check();
    await sansViolation(page);
    await page.getByRole('button', { name: 'Create my account' }).click();

    await expect(page).toHaveURL(/\/profil/);
    await expect(page.getByRole('heading', { name: 'Welcome, Haruto' })).toBeVisible();
  });

  test('page introuvable et confidentialité', async ({ page }) => {
    await page.goto('/une-page-qui-n-existe-pas');
    await choisirLangue(page, 'fr');
    await expect(page.getByRole('heading', { level: 1 })).toHaveText('Cette page n’existe pas');
    await page.goto('/confidentialite');
    await expect(page.getByRole('heading', { name: 'Emporter vos données' })).toBeVisible();
    await sansViolation(page);
  });
});
