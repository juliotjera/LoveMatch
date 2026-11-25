import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { Api } from '../core/api';
import { Avertisseur } from '../core/avis';
import { I18n } from '../core/i18n';
import { messageErreur } from '../core/intercepteur';
import { Score } from '../core/modeles';
import { Session } from '../core/session';
import { BoutonLangue } from '../ui/bouton-langue';
import { Logo } from '../ui/logo';
import { Tissage } from '../ui/tissage';

@Component({
  selector: 'lm-accueil',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, Logo, BoutonLangue, Tissage],
  template: `
    <header class="barre">
      <lm-logo />
      <div class="outils">
        <lm-bouton-langue />
        <a routerLink="/connexion" class="btn btn-discret">{{ t('accueil.connexion') }}</a>
      </div>
    </header>

    <main id="contenu">
      <section class="heros">
        <div class="texte">
          <h1>{{ t('accueil.titre') }}</h1>
          <p class="intro">{{ t('accueil.intro') }}</p>
          <div class="actions">
            <a routerLink="/inscription" class="btn btn-principal">{{ t('accueil.creer') }}</a>
            @if (demo()) {
              <button type="button" class="btn btn-contour" (click)="essayer('membre')" [disabled]="occupe()">
                {{ t('accueil.essayer') }}
              </button>
            }
          </div>
          @if (demo()) {
            <p class="demo-admin">
              {{ t('accueil.moderationAvant') }}
              <button type="button" class="lien" (click)="essayer('admin')" [disabled]="occupe()">{{ t('accueil.moderation') }}</button>
            </p>
          }
        </div>

        <figure class="exemple" [attr.aria-label]="t('accueil.exempleLegende')">
          <div class="portrait" aria-hidden="true">
            <svg viewBox="0 0 300 400" preserveAspectRatio="xMidYMid slice">
              <defs>
                <linearGradient id="fond-exemple" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0" stop-color="#1F7A8C" />
                  <stop offset="1" stop-color="#9FD8CB" />
                </linearGradient>
              </defs>
              <rect width="300" height="400" fill="url(#fond-exemple)" />
              <circle cx="150" cy="150" r="105" fill="#fff" opacity=".18" />
              <rect x="46" y="292" width="208" height="175" rx="104" fill="#264653" />
              <rect x="131" y="232" width="38" height="72" rx="17" fill="#5f3b26" />
              <ellipse cx="150" cy="187" rx="50" ry="63" fill="#7a4e32" />
              <path d="M100 168c-2-55 102-55 100 0-17-23-80-23-100 0z" fill="#1e1410" />
            </svg>
          </div>
          <figcaption class="fiche">
            <p class="nom">Hery, 30</p>
            <p class="lieu">{{ t('accueil.exempleLieu') }}</p>
            <lm-tissage [score]="exemple" />
            <ul class="pastilles" [attr.aria-label]="t('profil.communs')">
              <li class="pastille pastille-commune">{{ t('accueil.exempleInteret1') }}</li>
              <li class="pastille pastille-commune">{{ t('accueil.exempleInteret2') }}</li>
              <li class="pastille pastille-commune">{{ t('accueil.exempleInteret3') }}</li>
            </ul>
          </figcaption>
        </figure>
      </section>

      <section class="calcul" aria-labelledby="titre-calcul">
        <h2 id="titre-calcul">{{ t('accueil.calculTitre') }}</h2>
        <p class="sous">{{ t('accueil.calculIntro') }}</p>
        <ul class="brins">
          @for (b of brins; track b.cle) {
            <li [attr.data-brin]="b.cle">
              <h3>{{ t('score.' + b.cle) }} <span>{{ t('accueil.points', { n: b.points }) }}</span></h3>
              <p>{{ t('accueil.brin.' + b.cle) }}</p>
            </li>
          }
        </ul>
      </section>

      <section class="regles" aria-labelledby="titre-regles">
        <h2 id="titre-regles">{{ t('accueil.reglesTitre') }}</h2>
        <ul>
          @for (r of regles; track r) {
            <li>{{ t('accueil.regle.' + r) }}</li>
          }
        </ul>
        <a routerLink="/inscription" class="btn btn-aimer">{{ t('accueil.creer') }}</a>
      </section>
    </main>

    <footer class="pied">
      <p>{{ t('accueil.pied') }}</p>
      <a routerLink="/confidentialite">{{ t('accueil.confidentialite') }}</a>
    </footer>
  `,
  styles: `
    :host {
      display: block;
    }
    .barre {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      width: min(1120px, 100%);
      margin: 0 auto;
      padding: 14px 16px;
    }
    .outils {
      display: flex;
      align-items: center;
      gap: 6px;
    }
    .outils .btn {
      white-space: nowrap;
      padding: 0 10px;
    }
    main {
      width: min(1120px, 100%);
      margin: 0 auto;
      padding: 0 16px;
    }
    .heros {
      display: grid;
      gap: 32px;
      padding: 24px 0 56px;
    }
    .texte {
      display: grid;
      gap: 18px;
      align-content: center;
    }
    h1 {
      font-size: clamp(2.3rem, 7.4vw, 4.4rem);
      font-weight: 720;
      font-stretch: 92%;
      letter-spacing: -0.03em;
      max-width: 14ch;
    }
    .intro {
      font-size: 1.15rem;
      color: var(--encre-douce);
      max-width: 48ch;
    }
    .actions {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
    }
    .demo-admin {
      color: var(--encre-douce);
      font-size: 0.95rem;
    }
    .lien {
      padding: 0;
      border: 0;
      background: none;
      color: var(--jacaranda-fonce);
      font-weight: 700;
      text-decoration: underline;
      text-underline-offset: 3px;
      cursor: pointer;
    }
    .exemple {
      margin: 0;
      display: grid;
      grid-template-columns: 76px 1fr;
      align-items: center;
      gap: 6px 14px;
      padding: 16px;
      background: var(--carte);
      border-radius: 26px;
      box-shadow: var(--ombre-carte);
      transform: rotate(-1.2deg);
      max-width: 520px;
    }
    /* téléphone : petit portrait rond à côté du nom, score sur toute la largeur */
    .portrait {
      overflow: hidden;
      border-radius: 50%;
      aspect-ratio: 1;
      grid-row: span 2;
    }
    .portrait svg {
      width: 100%;
      height: 100%;
      display: block;
    }
    .fiche {
      display: contents;
    }
    .nom {
      font-family: var(--police-titre);
      font-size: 1.6rem;
      font-weight: 700;
      line-height: 1.1;
    }
    .lieu {
      color: var(--encre-douce);
      font-size: 0.95rem;
    }
    .fiche lm-tissage,
    .fiche .pastilles {
      grid-column: 1 / -1;
      margin-top: 8px;
    }
    .calcul,
    .regles {
      padding: 48px 0;
      border-top: 1px solid var(--trait);
    }
    .sous {
      color: var(--encre-douce);
      margin-top: 10px;
      max-width: 60ch;
    }
    .brins {
      display: grid;
      gap: 14px;
      margin: 28px 0 0;
      padding: 0;
      list-style: none;
    }
    .brins li {
      display: grid;
      gap: 6px;
      padding: 16px 18px 16px 20px;
      background: var(--carte);
      border-radius: 14px;
      border-start-start-radius: 4px;
      border-end-start-radius: 4px;
      border-inline-start: 8px solid var(--couleur);
    }
    .brins h3 span {
      font-family: var(--police-texte);
      font-size: 0.95rem;
      color: var(--encre-douce);
      font-weight: 700;
      margin-inline-start: 6px;
    }
    .brins p {
      color: var(--encre-douce);
    }
    [data-brin='age'] { --couleur: var(--brin-age); }
    [data-brin='distance'] { --couleur: var(--brin-distance); }
    [data-brin='interets'] { --couleur: var(--brin-interets); }
    [data-brin='langues'] { --couleur: var(--brin-langues); }
    [data-brin='recherche'] { --couleur: var(--brin-recherche); }
    .regles ul {
      display: grid;
      gap: 12px;
      margin: 24px 0 28px;
      padding: 0;
      list-style: none;
      max-width: 62ch;
    }
    .regles li {
      position: relative;
      padding-inline-start: 30px;
    }
    .regles li::before {
      content: '';
      position: absolute;
      inset-inline-start: 0;
      top: 0.35em;
      width: 18px;
      height: 12px;
      background:
        radial-gradient(circle at 6px 6px, var(--jacaranda) 6px, transparent 6.5px),
        radial-gradient(circle at 12px 6px, var(--flamboyant-vif) 6px, transparent 6.5px);
      mix-blend-mode: multiply;
    }
    .pied {
      width: min(1120px, 100%);
      margin: 0 auto;
      padding: 28px 16px 40px;
      display: flex;
      flex-wrap: wrap;
      gap: 8px 20px;
      justify-content: space-between;
      color: var(--encre-douce);
      font-size: 0.92rem;
      border-top: 1px solid var(--trait);
    }
    @media (min-width: 640px) {
      .exemple {
        grid-template-columns: 170px 1fr;
        align-items: start;
        padding: 18px;
        max-width: 560px;
      }
      .portrait {
        border-radius: 18px;
        aspect-ratio: 3 / 4;
        grid-row: auto;
      }
      .fiche {
        display: grid;
        gap: 8px;
        align-content: start;
      }
      .fiche lm-tissage {
        margin-top: 4px;
      }
      .brins {
        grid-template-columns: repeat(2, 1fr);
      }
    }
    @media (min-width: 960px) {
      .heros {
        grid-template-columns: 1.15fr 1fr;
        align-items: center;
        padding: 48px 0 80px;
        min-height: min(78vh, 720px);
      }
      .exemple {
        justify-self: end;
      }
      .brins {
        grid-template-columns: repeat(5, 1fr);
      }
    }
  `,
})
export class Accueil implements OnInit {
  private readonly api = inject(Api);
  private readonly session = inject(Session);
  private readonly router = inject(Router);
  private readonly avis = inject(Avertisseur);
  protected readonly t = inject(I18n).t;

  protected readonly demo = signal(false);
  protected readonly occupe = signal(false);

  // l'exemple de la page d'accueil : un vrai calcul, celui de Fanja et Hery dans la démonstration
  protected readonly exemple: Score = {
    total: 78, age: 20, distance: 24, interets: 9, langues: 15, recherche: 10, distanceKm: 10,
    interetsCommuns: ['photo', 'randonnee', 'voyage'], languesCommunes: ['EN', 'FR', 'MG'],
  };
  protected readonly brins = [
    { cle: 'age', points: 25 },
    { cle: 'distance', points: 25 },
    { cle: 'interets', points: 25 },
    { cle: 'langues', points: 15 },
    { cle: 'recherche', points: 10 },
  ];
  protected readonly regles = ['mutuel', 'vieprivee', 'message', 'majeur'];

  async ngOnInit() {
    try {
      this.demo.set((await this.api.demoActive()).active);
    } catch {
      this.demo.set(false);
    }
  }

  async essayer(role: 'membre' | 'admin') {
    this.occupe.set(true);
    try {
      const moi = await this.api.essayer(role);
      this.session.ouvrir(moi);
      await this.router.navigateByUrl(this.session.accueil(moi));
    } catch (e) {
      this.avis.erreur(messageErreur(e, this.t('erreur.serveur')));
    } finally {
      this.occupe.set(false);
    }
  }
}
