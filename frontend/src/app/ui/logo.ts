import { ChangeDetectionStrategy, Component, input } from '@angular/core';

// Deux cercles qui se recouvrent : ce que deux personnes ont en commun (le cœur du score)
@Component({
  selector: 'lm-logo',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <svg [attr.width]="taille()" [attr.height]="taille()" viewBox="0 0 40 40" aria-hidden="true">
      <circle cx="15" cy="20" r="12" fill="#6c4fa3" />
      <circle cx="25" cy="20" r="12" fill="#e4572e" />
      <path d="M20 9.6a12 12 0 0 1 0 20.8a12 12 0 0 1 0-20.8z" fill="#3e2c6e" />
    </svg>
    @if (avecNom()) {
      <span class="nom">LoveMatch</span>
    }
  `,
  styles: `
    :host {
      display: inline-flex;
      align-items: center;
      gap: 8px;
    }
    .nom {
      font-family: var(--police-titre);
      font-weight: 750;
      font-size: 1.3rem;
      letter-spacing: -0.02em;
      color: var(--encre);
    }
  `,
})
export class Logo {
  readonly taille = input(30);
  readonly avecNom = input(true);
}
