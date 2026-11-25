import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Api } from '../core/api';
import { I18n } from '../core/i18n';
import { messageErreur } from '../core/intercepteur';
import { Session } from '../core/session';
import { CadreVisiteur } from '../ui/cadre-visiteur';

@Component({
  selector: 'lm-connexion',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, RouterLink, CadreVisiteur],
  template: `
    <lm-cadre-visiteur [titre]="t('connexion.titre')" [sousTitre]="t('connexion.sous')">
      <form [formGroup]="formulaire" (ngSubmit)="envoyer()" novalidate class="formulaire">
        @if (erreur()) {
          <p class="alerte" role="alert">{{ erreur() }}</p>
        }
        <div class="champ">
          <label for="email">{{ t('champ.email') }}</label>
          <input id="email" class="saisie" type="email" formControlName="email" autocomplete="email" inputmode="email"
            [attr.aria-invalid]="invalide('email')" aria-describedby="email-erreur" />
          @if (invalide('email')) {
            <p id="email-erreur" class="erreur-champ">{{ t('erreur.email') }}</p>
          }
        </div>
        <div class="champ">
          <label for="mdp">{{ t('champ.motDePasse') }}</label>
          <div class="mot-de-passe">
            <input id="mdp" class="saisie" [type]="voirMdp() ? 'text' : 'password'" formControlName="motDePasse"
              autocomplete="current-password" [attr.aria-invalid]="invalide('motDePasse')" aria-describedby="mdp-erreur" />
            <button type="button" class="voir" [attr.aria-pressed]="voirMdp()" (click)="voirMdp.set(!voirMdp())">
              {{ voirMdp() ? t('mdp.masquer') : t('mdp.afficher') }}
            </button>
          </div>
          @if (invalide('motDePasse')) {
            <p id="mdp-erreur" class="erreur-champ">{{ t('erreur.obligatoire') }}</p>
          }
        </div>
        <button type="submit" class="btn btn-principal btn-bloc" [disabled]="occupe()">
          {{ occupe() ? t('connexion.encours') : t('connexion.bouton') }}
        </button>
        <p class="bas">{{ t('connexion.pasDeCompte') }} <a routerLink="/inscription">{{ t('connexion.creer') }}</a></p>
      </form>
    </lm-cadre-visiteur>
  `,
  styles: `
    .formulaire {
      display: grid;
      gap: 18px;
    }
    .bas {
      color: var(--encre-douce);
      text-align: center;
    }
  `,
})
export class Connexion {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly api = inject(Api);
  private readonly session = inject(Session);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  protected readonly t = inject(I18n).t;

  protected readonly occupe = signal(false);
  protected readonly erreur = signal('');
  protected readonly voirMdp = signal(false);
  protected readonly essaye = signal(false);

  protected readonly formulaire = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    motDePasse: ['', Validators.required],
  });

  invalide(champ: 'email' | 'motDePasse') {
    const c = this.formulaire.controls[champ];
    return c.invalid && (c.touched || this.essaye()) ? true : null;
  }

  async envoyer() {
    this.essaye.set(true);
    this.erreur.set('');
    if (this.formulaire.invalid) {
      this.formulaire.markAllAsTouched();
      return;
    }
    this.occupe.set(true);
    try {
      const { email, motDePasse } = this.formulaire.getRawValue();
      const moi = await this.api.connecter(email, motDePasse);
      this.session.ouvrir(moi);
      const retour = this.route.snapshot.queryParamMap.get('retour');
      const sur = retour && retour.startsWith('/') && !retour.startsWith('//') ? retour : null;
      await this.router.navigateByUrl(sur ?? this.session.accueil(moi));
    } catch (e) {
      this.erreur.set(messageErreur(e, this.t('erreur.serveur')));
    } finally {
      this.occupe.set(false);
    }
  }
}
