import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { I18n } from '../core/i18n';

// Date courte et relative : « 14:32 », « hier », « lun. 3 nov. »
@Component({
  selector: 'lm-quand',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<time [attr.datetime]="date()">{{ texte() }}</time>`,
})
export class Quand {
  private readonly i18n = inject(I18n);
  readonly date = input.required<string>();
  readonly heureSeule = input(false);

  protected readonly texte = computed(() => formaterDate(this.date(), this.i18n.locale(), this.i18n.t, this.heureSeule()));
}

export function formaterDate(iso: string, locale: string, t: (cle: string) => string, heureSeule = false) {
  const date = new Date(iso);
  const heure = date.toLocaleTimeString(locale, { hour: '2-digit', minute: '2-digit' });
  if (heureSeule) {
    return heure;
  }
  const aujourdhui = new Date();
  const jour = (d: Date) => new Date(d.getFullYear(), d.getMonth(), d.getDate()).getTime();
  const ecart = Math.round((jour(aujourdhui) - jour(date)) / 86_400_000);
  if (ecart === 0) {
    return heure;
  }
  if (ecart === 1) {
    return t('date.hier');
  }
  return date.toLocaleDateString(locale, { weekday: 'short', day: 'numeric', month: 'short' });
}

export function jourDe(iso: string, locale: string, t: (cle: string) => string) {
  const date = new Date(iso);
  const aujourdhui = new Date();
  const jour = (d: Date) => new Date(d.getFullYear(), d.getMonth(), d.getDate()).getTime();
  const ecart = Math.round((jour(aujourdhui) - jour(date)) / 86_400_000);
  if (ecart === 0) {
    return t('date.aujourdhui');
  }
  if (ecart === 1) {
    return t('date.hier');
  }
  return date.toLocaleDateString(locale, { weekday: 'long', day: 'numeric', month: 'long' });
}
