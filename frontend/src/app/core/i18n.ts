import { DOCUMENT } from '@angular/common';
import { Injectable, computed, effect, inject, signal } from '@angular/core';
import { Interet } from './modeles';

// Les cinq langues les plus parlées au monde, plus le français et le malagasy (Madagascar)
export const LANGUES = [
  { code: 'mg', nom: 'Malagasy', locale: 'fr-MG', rtl: false },
  { code: 'fr', nom: 'Français', locale: 'fr-FR', rtl: false },
  { code: 'en', nom: 'English', locale: 'en-GB', rtl: false },
  { code: 'es', nom: 'Español', locale: 'es-ES', rtl: false },
  { code: 'zh', nom: '中文', locale: 'zh-CN', rtl: false },
  { code: 'hi', nom: 'हिन्दी', locale: 'hi-IN', rtl: false },
  { code: 'ar', nom: 'العربية', locale: 'ar-u-nu-latn', rtl: true },
] as const;

export type Langue = (typeof LANGUES)[number]['code'];
type Dictionnaire = Record<string, string>;

const CLE_STOCKAGE = 'lm-langue';

/**
 * Interface traduite à chaud. Chaque dictionnaire (public/i18n/xx.json) n'est téléchargé que lorsqu'on choisit
 * sa langue ; celui de départ est chargé avant le premier affichage (provideAppInitializer).
 */
@Injectable({ providedIn: 'root' })
export class I18n {
  private readonly document = inject(DOCUMENT);
  private readonly dictionnaire = signal<Dictionnaire>({});
  private readonly chargés = new Map<Langue, Promise<Dictionnaire>>();

  readonly langue = signal<Langue>('fr');
  readonly infos = computed(() => LANGUES.find((l) => l.code === this.langue())!);
  readonly locale = computed(() => this.infos().locale);

  constructor() {
    effect(() => {
      const { code, rtl } = this.infos();
      const racine = this.document.documentElement;
      racine.lang = code;
      racine.dir = rtl ? 'rtl' : 'ltr';
      try {
        localStorage.setItem(CLE_STOCKAGE, code);
      } catch {
        // stockage indisponible (navigation privée) : la langue vaut pour la session
      }
    });
  }

  // t('cle', { prenom: 'Fanja' }) : remplace {prenom} dans le texte
  readonly t = (cle: string, valeurs?: Record<string, string | number>): string => {
    const texte = this.dictionnaire()[cle] ?? cle;
    return valeurs ? texte.replace(/\{(\w+)\}/g, (_, nom: string) => String(valeurs[nom] ?? '')) : texte;
  };

  readonly interet = (i: Interet) => this.dictionnaire()['interet.' + i.code] ?? i.libelleFr;

  // nom d'une langue parlée (« MG », « ZH »…) dans la langue de l'interface
  readonly nomLangue = (code: string): string => {
    const propre = this.dictionnaire()['langueNom.' + code];
    if (propre) {
      return propre;
    }
    try {
      const nom = new Intl.DisplayNames([this.locale()], { type: 'language' }).of(code.toLowerCase()) ?? code;
      return nom.charAt(0).toLocaleUpperCase(this.locale()) + nom.slice(1);
    } catch {
      return code;
    }
  };

  // « FR » → « France », « 法国 », « फ़्रांस »…
  readonly nomPays = (code: string): string => {
    try {
      return new Intl.DisplayNames([this.locale()], { type: 'region' }).of(code) ?? code;
    } catch {
      return code;
    }
  };

  readonly nombre = (n: number) => new Intl.NumberFormat(this.locale()).format(n);

  // « Paris, France » ; virgule arabe « ، » et chinoise « ， » selon la langue
  readonly joindre = (...morceaux: (string | null | undefined)[]) => {
    const virgule = this.langue() === 'ar' ? '، ' : this.langue() === 'zh' ? '，' : ', ';
    return morceaux.filter((m) => !!m).join(virgule);
  };

  readonly listeLangues = (codes: readonly string[]) => {
    const noms = codes.map(this.nomLangue);
    try {
      return new Intl.ListFormat(this.locale(), { style: 'long', type: 'conjunction' }).format(noms);
    } catch {
      return noms.join(', ');
    }
  };

  // au démarrage : la langue déjà choisie, sinon celle du téléphone si elle est proposée, sinon le français
  async demarrer() {
    await this.choisir(this.initiale());
  }

  async choisir(langue: Langue) {
    try {
      this.dictionnaire.set(await this.charger(langue));
      this.langue.set(langue);
    } catch {
      // réseau coupé : on garde la langue affichée
    }
  }

  private charger(langue: Langue) {
    let promesse = this.chargés.get(langue);
    if (!promesse) {
      promesse = fetch(`/i18n/${langue}.json`).then((r) => {
        if (!r.ok) {
          throw new Error(`Dictionnaire ${langue} indisponible`);
        }
        return r.json() as Promise<Dictionnaire>;
      });
      promesse.catch(() => this.chargés.delete(langue));
      this.chargés.set(langue, promesse);
    }
    return promesse;
  }

  private initiale(): Langue {
    const proposee = (code: string | null | undefined) => LANGUES.find((l) => l.code === code)?.code;
    try {
      const enregistree = proposee(localStorage.getItem(CLE_STOCKAGE));
      if (enregistree) {
        return enregistree;
      }
    } catch {
      // ignoré
    }
    for (const preferee of navigator.languages ?? [navigator.language]) {
      const trouvee = proposee(preferee.toLowerCase().split('-')[0]);
      if (trouvee) {
        return trouvee;
      }
    }
    return 'fr';
  }
}
