import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { I18n } from '../core/i18n';
import { CadreVisiteur } from '../ui/cadre-visiteur';

@Component({
  selector: 'lm-confidentialite',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CadreVisiteur],
  template: `
    <lm-cadre-visiteur [titre]="t('conf.titre')" [sousTitre]="t('conf.sous')">
      @for (s of sections; track s) {
        <section>
          <h2>{{ t('conf.' + s + '.titre') }}</h2>
          <p>{{ t('conf.' + s + '.texte') }}</p>
        </section>
      }
    </lm-cadre-visiteur>
  `,
  styles: `
    section {
      display: grid;
      gap: 6px;
      margin-top: 14px;
    }
    h2 {
      font-size: 1.2rem;
    }
    p {
      color: var(--encre-douce);
    }
  `,
})
export class Confidentialite {
  protected readonly t = inject(I18n).t;
  protected readonly sections = ['collecte', 'visible', 'photos', 'distance', 'messages', 'effacement', 'cookies', 'credits'];
}
