import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { I18n } from '../core/i18n';
import { CadreVisiteur } from '../ui/cadre-visiteur';

@Component({
  selector: 'lm-introuvable',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CadreVisiteur, RouterLink],
  template: `
    <lm-cadre-visiteur [titre]="t('introuvable.titre')" [sousTitre]="t('introuvable.texte')">
      <a routerLink="/" class="btn btn-principal">{{ t('introuvable.retour') }}</a>
    </lm-cadre-visiteur>
  `,
})
export class Introuvable {
  protected readonly t = inject(I18n).t;
}
