import { defineConfig, devices } from '@playwright/test';

// Tests de bout en bout : l'application complète (serveur Spring Boot + interface compilée) doit tourner sur BASE_URL.
// En local, Edge installé sert de navigateur ; en intégration continue, Chromium téléchargé par Playwright.
const canal = process.env['CI'] ? undefined : 'msedge';

export default defineConfig({
  testDir: './e2e',
  timeout: 45_000,
  expect: { timeout: 10_000 },
  fullyParallel: false,
  workers: 1,
  retries: process.env['CI'] ? 1 : 0,
  reporter: process.env['CI'] ? [['github'], ['html', { open: 'never' }]] : 'list',
  use: {
    baseURL: process.env['BASE_URL'] ?? 'http://localhost:8090',
    locale: 'fr-FR',
    trace: 'retain-on-failure',
  },
  projects: [
    { name: 'telephone', use: { ...devices['Pixel 7'], channel: canal } },
    { name: 'ordinateur', use: { ...devices['Desktop Chrome'], channel: canal } },
  ],
});
