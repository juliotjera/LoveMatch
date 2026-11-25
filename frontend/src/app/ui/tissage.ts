import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { I18n } from '../core/i18n';
import { Score } from '../core/modeles';

interface Brin {
  cle: 'age' | 'distance' | 'interets' | 'langues' | 'recherche';
  points: number;
  max: number;
  detail: string;
}

/**
 * Le score de compatibilité tissé en quatre brins, comme les bandes d'un lamba :
 * chaque brin a la largeur de ses points possibles et se remplit selon les points obtenus.
 */
@Component({
  selector: 'lm-tissage',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="tete">
      <span class="total" aria-hidden="true">{{ score().total }}</span>
      <span class="libelle" aria-hidden="true">{{ t('score.surCent') }}</span>
    </div>
    <div class="bande" role="img" [attr.aria-label]="resume()">
      @for (brin of brins(); track brin.cle) {
        <span class="brin" [style.flex-grow]="brin.max" [attr.data-brin]="brin.cle">
          <span class="rempli" [style.width.%]="(brin.points / brin.max) * 100"></span>
        </span>
      }
    </div>
    @if (!compact()) {
      <dl class="legende">
        @for (brin of brins(); track brin.cle) {
          <div [attr.data-brin]="brin.cle">
            <dt>{{ t('score.' + brin.cle) }}</dt>
            <dd>
              <strong>{{ brin.points }}/{{ brin.max }}</strong>
              <span>{{ brin.detail }}</span>
            </dd>
          </div>
        }
      </dl>
    }
  `,
  styles: `
    :host {
      display: grid;
      gap: 10px;
    }
    .tete {
      display: flex;
      align-items: baseline;
      gap: 8px;
    }
    .total {
      font-family: var(--police-titre);
      font-weight: 750;
      font-size: 2.6rem;
      line-height: 1;
      letter-spacing: -0.03em;
      font-variant-numeric: tabular-nums;
    }
    :host(.compact) .total {
      font-size: 1.7rem;
    }
    .libelle {
      color: var(--encre-douce);
      font-weight: 700;
    }
    .bande {
      display: flex;
      gap: 4px;
      height: 16px;
    }
    :host(.compact) .bande {
      height: 10px;
    }
    .brin {
      position: relative;
      flex-basis: 0;
      overflow: hidden;
      border-radius: 4px;
      background:
        repeating-linear-gradient(-45deg, transparent 0 5px, rgb(255 255 255 / 55%) 5px 7px),
        var(--pale);
    }
    .rempli {
      position: absolute;
      inset-block: 0;
      inset-inline-start: 0;
      background:
        repeating-linear-gradient(-45deg, transparent 0 5px, rgb(255 255 255 / 22%) 5px 7px),
        var(--couleur);
      transform-origin: left;
    }
    :host-context([dir='rtl']) .rempli {
      transform-origin: right;
      animation: tisser 0.9s cubic-bezier(0.2, 0.7, 0.2, 1) both;
    }
    .brin:nth-child(2) .rempli { animation-delay: 0.08s; }
    .brin:nth-child(3) .rempli { animation-delay: 0.16s; }
    .brin:nth-child(4) .rempli { animation-delay: 0.24s; }
    @keyframes tisser {
      from { transform: scaleX(0); }
    }
    [data-brin='age'] { --couleur: var(--brin-age); --pale: #e4ddf1; }
    [data-brin='distance'] { --couleur: var(--brin-distance); --pale: #d6e9ed; }
    [data-brin='interets'] { --couleur: var(--brin-interets); --pale: #f8ddd3; }
    [data-brin='langues'] { --couleur: var(--brin-langues); --pale: #d7e9de; }
    [data-brin='recherche'] { --couleur: var(--brin-recherche); --pale: #f1e4cc; }
    .legende {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 10px 16px;
      margin: 4px 0 0;
    }
    .legende div {
      border-inline-start: 4px solid var(--couleur);
      padding-inline-start: 10px;
    }
    dt {
      font-size: 0.85rem;
      font-weight: 700;
      color: var(--encre-douce);
    }
    dd {
      margin: 0;
      display: grid;
      font-size: 0.92rem;
      line-height: 1.3;
    }
    dd strong {
      font-variant-numeric: tabular-nums;
    }
  `,
  host: { '[class.compact]': 'compact()' },
})
export class Tissage {
  private readonly i18n = inject(I18n);
  protected readonly t = this.i18n.t;

  readonly score = input.required<Score>();
  readonly compact = input(false);

  protected readonly brins = computed<Brin[]>(() => {
    const s = this.score();
    const t = this.i18n.t;
    const communs = s.interetsCommuns.length;
    return [
      { cle: 'age', points: s.age, max: 25, detail: t(s.age >= 20 ? 'score.age.proche' : 'score.age.tranche') },
      { cle: 'distance', points: s.distance, max: 25, detail: t('score.distance.km', { km: this.i18n.nombre(s.distanceKm) }) },
      {
        cle: 'interets', points: s.interets, max: 25,
        detail: communs === 0 ? t('score.interets.aucun') : t(communs === 1 ? 'score.interets.un' : 'score.interets.n', { n: communs }),
      },
      {
        cle: 'langues', points: s.langues, max: 15,
        detail: s.languesCommunes.length ? this.i18n.listeLangues(s.languesCommunes) : t('score.langues.aucune'),
      },
      { cle: 'recherche', points: s.recherche, max: 10, detail: t(s.recherche === 10 ? 'score.recherche.meme' : s.recherche > 0 ? 'score.recherche.ouverte' : 'score.recherche.differente') },
    ];
  });

  protected readonly resume = computed(() => {
    const t = this.i18n.t;
    const s = this.score();
    return t('score.resume', { total: s.total, age: s.age, distance: s.distance, interets: s.interets, recherche: s.recherche });
  });
}
