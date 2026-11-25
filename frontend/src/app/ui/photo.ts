import { ChangeDetectionStrategy, Component, computed, input, signal } from '@angular/core';
import { photoUrl } from '../core/api';

// Photo d'un membre ; sans photo (ou si elle ne charge pas), l'initiale du prénom sur fond jacaranda
@Component({
  selector: 'lm-photo',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (id() && !echec()) {
      <img [src]="url()" [alt]="alt()" loading="lazy" decoding="async" (error)="echec.set(true)" />
    } @else {
      <span class="initiale" [attr.role]="alt() ? 'img' : null" [attr.aria-label]="alt() || null">
        <span aria-hidden="true">{{ prenom().charAt(0) }}</span>
      </span>
    }
  `,
  styles: `
    :host {
      display: block;
      overflow: hidden;
      background: var(--jacaranda-pale);
      aspect-ratio: var(--format, 3 / 4);
      border-radius: var(--rayon, var(--rayon-photo));
    }
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    .initiale {
      display: grid;
      place-items: center;
      width: 100%;
      height: 100%;
      background: linear-gradient(160deg, var(--jacaranda) 0%, var(--jacaranda-fonce) 100%);
      color: #fff;
      font-family: var(--police-titre);
      font-weight: 700;
      font-size: var(--taille-initiale, 3rem);
    }
  `,
})
export class Photo {
  readonly id = input<number | undefined>();
  readonly prenom = input('');
  readonly alt = input('');

  protected readonly echec = signal(false);
  protected readonly url = computed(() => photoUrl(this.id()!));
}
