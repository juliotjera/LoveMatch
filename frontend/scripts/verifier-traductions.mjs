// Vérifie que les 7 dictionnaires ont les mêmes clés que le français et les mêmes variables {nom} dans chaque texte.
import { readFileSync } from 'node:fs';

const LANGUES = ['fr', 'mg', 'en', 'es', 'zh', 'hi', 'ar'];
const lire = (l) => JSON.parse(readFileSync(new URL(`../public/i18n/${l}.json`, import.meta.url), 'utf8'));
const variables = (texte) => [...texte.matchAll(/\{(\w+)\}/g)].map((m) => m[1]).sort().join(',');
const reference = lire('fr');
let erreurs = 0;

for (const langue of LANGUES.slice(1)) {
  const dico = lire(langue);
  for (const cle of Object.keys(reference)) {
    if (!(cle in dico)) {
      console.error(`${langue} : clé manquante « ${cle} »`);
      erreurs++;
    } else if (variables(dico[cle]) !== variables(reference[cle])) {
      console.error(`${langue} : variables différentes dans « ${cle} »`);
      erreurs++;
    }
  }
  // seules exceptions admises : les noms de langues propres au malagasy (Intl ne les connaît pas)
  for (const cle of Object.keys(dico)) {
    if (!(cle in reference) && !cle.startsWith('langueNom.')) {
      console.error(`${langue} : clé inconnue « ${cle} »`);
      erreurs++;
    }
  }
}
if (erreurs) {
  process.exit(1);
}
console.log(`Traductions complètes : ${Object.keys(reference).length} textes × ${LANGUES.length} langues.`);
