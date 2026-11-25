import AxeBuilder from '@axe-core/playwright';
import { APIRequestContext, Page, expect, request } from '@playwright/test';

export const BASE = process.env['BASE_URL'] ?? 'http://localhost:8090';
export const MOT_DE_PASSE = 'motdepasse-de-test-e2e';

// Aucune violation WCAG 2.2 AA (règles axe-core)
export async function sansViolation(page: Page) {
  const resultat = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa']).analyze();
  expect(resultat.violations.map((v) => `${v.id} : ${v.nodes.map((n) => n.target.join(' ')).join(', ')}`)).toEqual([]);
}

export async function choisirLangue(page: Page, code: string) {
  await page.locator('lm-bouton-langue select').first().selectOption(code);
  await expect(page.locator('html')).toHaveAttribute('lang', code);
}

export interface Membre {
  prenom: string;
  email: string;
  id: number;
  api: APIRequestContext;
}

/**
 * Crée un membre complet par l'API (inscription, profil, préférences), comme le ferait l'interface.
 * Chaque test a ses propres membres : les tests ne dépendent ni les uns des autres, ni de la démonstration.
 */
export async function creerMembre(options: {
  genre: 'FEMME' | 'HOMME';
  cherche: 'HOMMES' | 'FEMMES' | 'TOUS';
  ville: string;
  prenom?: string;
}): Promise<Membre> {
  const api = await request.newContext({ baseURL: BASE });
  await api.get('/api/auth/demo');
  const xsrf = async () => (await api.storageState()).cookies.find((c) => c.name === 'XSRF-TOKEN')?.value ?? '';
  const suffixe = Math.random().toString(36).slice(2, 8);
  const prenom = options.prenom ?? `E2e${suffixe}`;
  const email = `e2e-${suffixe}-${Date.now()}@test.lovematch`;
  const ville = (await (await api.get(`/api/referentiels/villes?q=${encodeURIComponent(options.ville)}&limite=1`)).json())[0];
  const interets = (await (await api.get('/api/referentiels/interets')).json()).slice(0, 3).map((i: { id: number }) => i.id);

  const inscription = await api.post('/api/auth/inscription', {
    headers: { 'X-XSRF-TOKEN': await xsrf() },
    data: {
      email, motDePasse: MOT_DE_PASSE, prenom, nom: 'Testeur', dateNaissance: '1998-05-10',
      genre: options.genre, villeId: ville.id, genreRecherche: options.cherche, langues: ['FR'],
    },
  });
  expect(inscription.status()).toBe(201);
  const id = (await inscription.json()).id;
  expect((await api.put('/api/profil', {
    headers: { 'X-XSRF-TOKEN': await xsrf() },
    data: { prenom, nom: 'Testeur', villeId: ville.id, bio: 'Membre créé par les tests.', recherche: 'A_DECOUVRIR', interets, langues: ['FR'] },
  })).status()).toBe(200);
  // rayon minimal : seuls les membres de la même ville se voient
  expect((await api.put('/api/profil/preferences', {
    headers: { 'X-XSRF-TOKEN': await xsrf() },
    data: { genreRecherche: options.cherche, ageMin: 18, ageMax: 60, rayonKm: 10 },
  })).status()).toBe(200);
  return { prenom, email, id, api };
}

export async function liker(auteur: Membre, cible: Membre) {
  const xsrf = (await auteur.api.storageState()).cookies.find((c) => c.name === 'XSRF-TOKEN')?.value ?? '';
  const reponse = await auteur.api.post('/api/reactions', { headers: { 'X-XSRF-TOKEN': xsrf }, data: { cibleId: cible.id, decision: 'LIKE' } });
  expect(reponse.status()).toBe(201);
}
