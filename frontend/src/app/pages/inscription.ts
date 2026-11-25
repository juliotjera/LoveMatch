import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { AbstractControl, NonNullableFormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Api } from '../core/api';
import { I18n } from '../core/i18n';
import { messageErreur } from '../core/intercepteur';
import { Genre, GenreRecherche, Ville } from '../core/modeles';
import { Session } from '../core/session';
import { CadreVisiteur } from '../ui/cadre-visiteur';
import { ChoixVille } from '../ui/choix-ville';

function majeur(controle: AbstractControl): ValidationErrors | null {
  if (!controle.value) {
    return null;
  }
  const naissance = new Date(controle.value as string);
  const limite = new Date();
  limite.setFullYear(limite.getFullYear() - 18);
  return naissance <= limite ? null : { mineur: true };
}

@Component({
  selector: 'lm-inscription',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, RouterLink, CadreVisiteur, ChoixVille],
  template: `
    <lm-cadre-visiteur [titre]="t('inscription.titre')" [sousTitre]="t('inscription.sous')">
      <form [formGroup]="formulaire" (ngSubmit)="envoyer()" novalidate class="formulaire">
        @if (erreur()) {
          <p class="alerte" role="alert">{{ erreur() }}</p>
        }
        <div class="deux">
          <div class="champ">
            <label for="prenom">{{ t('champ.prenom') }}</label>
            <input id="prenom" class="saisie" formControlName="prenom" autocomplete="given-name" maxlength="60"
              [attr.aria-invalid]="invalide('prenom')" />
            @if (invalide('prenom')) {
              <p class="erreur-champ">{{ t('erreur.obligatoire') }}</p>
            }
          </div>
          <div class="champ">
            <label for="nom">{{ t('champ.nom') }}</label>
            <input id="nom" class="saisie" formControlName="nom" autocomplete="family-name" maxlength="60"
              aria-describedby="nom-aide" [attr.aria-invalid]="invalide('nom')" />
            <p id="nom-aide" class="aide">{{ t('inscription.nomPrive') }}</p>
          </div>
        </div>

        <div class="champ">
          <label for="naissance">{{ t('champ.naissance') }}</label>
          <input id="naissance" class="saisie" type="date" formControlName="dateNaissance" autocomplete="bday"
            [max]="aujourdhui" aria-describedby="naissance-aide" [attr.aria-invalid]="invalide('dateNaissance')" />
          @if (formulaire.controls.dateNaissance.hasError('mineur')) {
            <p id="naissance-aide" class="erreur-champ">{{ t('inscription.majeur') }}</p>
          } @else if (invalide('dateNaissance')) {
            <p id="naissance-aide" class="erreur-champ">{{ t('erreur.obligatoire') }}</p>
          } @else {
            <p id="naissance-aide" class="aide">{{ t('inscription.ageAffiche') }}</p>
          }
        </div>

        <fieldset class="champ">
          <legend>{{ t('champ.genre') }}</legend>
          <div class="pastilles">
            @for (g of genres; track g) {
              <label class="pastille"><input type="radio" formControlName="genre" [value]="g" />{{ t('genre.' + g) }}</label>
            }
          </div>
          @if (invalide('genre')) {
            <p class="erreur-champ">{{ t('erreur.choix') }}</p>
          }
        </fieldset>

        <fieldset class="champ">
          <legend>{{ t('champ.genreRecherche') }}</legend>
          <div class="pastilles">
            @for (g of recherches; track g) {
              <label class="pastille"><input type="radio" formControlName="genreRecherche" [value]="g" />{{ t('genreRecherche.' + g) }}</label>
            }
          </div>
          @if (invalide('genreRecherche')) {
            <p class="erreur-champ">{{ t('erreur.choix') }}</p>
          }
        </fieldset>

        <lm-choix-ville [libelle]="t('champ.ville')" [aide]="t('inscription.villeAide')" [erreur]="!!invalide('villeId')"
          [ville]="ville()" (villeChange)="choisirVille($event)" />

        <div class="champ">
          <label for="email">{{ t('champ.email') }}</label>
          <input id="email" class="saisie" type="email" formControlName="email" autocomplete="email" inputmode="email"
            [attr.aria-invalid]="invalide('email')" />
          @if (invalide('email')) {
            <p class="erreur-champ">{{ t('erreur.email') }}</p>
          }
        </div>
        <div class="champ">
          <label for="mdp">{{ t('champ.motDePasse') }}</label>
          <div class="mot-de-passe">
            <input id="mdp" class="saisie" [type]="voirMdp() ? 'text' : 'password'" formControlName="motDePasse"
              autocomplete="new-password" aria-describedby="mdp-aide" [attr.aria-invalid]="invalide('motDePasse')" />
            <button type="button" class="voir" [attr.aria-pressed]="voirMdp()" (click)="voirMdp.set(!voirMdp())">
              {{ voirMdp() ? t('mdp.masquer') : t('mdp.afficher') }}
            </button>
          </div>
          <p id="mdp-aide" [class]="invalide('motDePasse') ? 'erreur-champ' : 'aide'">{{ t('inscription.mdpAide') }}</p>
        </div>

        <label class="accord">
          <input type="checkbox" formControlName="accord" />
          <span>{{ t('inscription.accordAvant') }} <a routerLink="/confidentialite">{{ t('inscription.accordLien') }}</a>.</span>
        </label>
        @if (invalide('accord')) {
          <p class="erreur-champ">{{ t('inscription.accordRequis') }}</p>
        }

        <button type="submit" class="btn btn-principal btn-bloc" [disabled]="occupe()">
          {{ occupe() ? t('inscription.encours') : t('inscription.bouton') }}
        </button>
        <p class="bas">{{ t('inscription.dejaInscrit') }} <a routerLink="/connexion">{{ t('accueil.connexion') }}</a></p>
      </form>
    </lm-cadre-visiteur>
  `,
  styles: `
    .formulaire {
      display: grid;
      gap: 20px;
    }
    .deux {
      display: grid;
      align-items: start;
      gap: 20px;
    }
    .accord {
      display: flex;
      gap: 10px;
      align-items: flex-start;
    }
    .accord input {
      width: 22px;
      height: 22px;
      margin-top: 2px;
      accent-color: var(--jacaranda-fonce);
      flex: none;
    }
    .bas {
      color: var(--encre-douce);
      text-align: center;
    }
    @media (min-width: 520px) {
      .deux {
        grid-template-columns: 1fr 1fr;
      }
    }
  `,
})
export class Inscription {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly api = inject(Api);
  private readonly session = inject(Session);
  private readonly router = inject(Router);
  private readonly i18n = inject(I18n);
  protected readonly t = this.i18n.t;

  protected readonly occupe = signal(false);
  protected readonly erreur = signal('');
  protected readonly essaye = signal(false);
  protected readonly genres: Genre[] = ['FEMME', 'HOMME', 'NON_BINAIRE'];
  protected readonly voirMdp = signal(false);
  protected readonly ville = signal<Ville | null>(null);
  protected readonly recherches: GenreRecherche[] = ['HOMMES', 'FEMMES', 'TOUS'];
  protected readonly aujourdhui = new Date().toISOString().slice(0, 10);

  protected readonly formulaire = this.fb.group({
    prenom: ['', [Validators.required, Validators.maxLength(60)]],
    nom: ['', [Validators.required, Validators.maxLength(60)]],
    dateNaissance: ['', [Validators.required, majeur]],
    genre: ['' as Genre | '', Validators.required],
    genreRecherche: ['' as GenreRecherche | '', Validators.required],
    villeId: [0, Validators.min(1)],
    email: ['', [Validators.required, Validators.email]],
    motDePasse: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
    accord: [false, Validators.requiredTrue],
  });

  choisirVille(v: Ville | null) {
    this.ville.set(v);
    this.formulaire.controls.villeId.setValue(v?.id ?? 0);
    this.formulaire.controls.villeId.markAsTouched();
  }

  invalide(champ: keyof typeof this.formulaire.controls) {
    const c = this.formulaire.controls[champ];
    return c.invalid && (c.touched || this.essaye()) ? true : null;
  }

  async envoyer() {
    this.essaye.set(true);
    this.erreur.set('');
    if (this.formulaire.invalid) {
      this.formulaire.markAllAsTouched();
      document.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus();
      return;
    }
    this.occupe.set(true);
    try {
      const v = this.formulaire.getRawValue();
      const moi = await this.api.inscrire({
        prenom: v.prenom.trim(), nom: v.nom.trim(), dateNaissance: v.dateNaissance, genre: v.genre as Genre,
        genreRecherche: v.genreRecherche as GenreRecherche, villeId: v.villeId, email: v.email.trim(),
        motDePasse: v.motDePasse,
        // langue parlée par défaut : celle de l'interface (modifiable dans le profil)
        langues: [this.i18n.langue().toUpperCase()],
      });
      this.session.ouvrir(moi);
      await this.router.navigate(['/profil'], { queryParams: { bienvenue: 1 } });
    } catch (e) {
      this.erreur.set(messageErreur(e, this.t('erreur.serveur')));
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } finally {
      this.occupe.set(false);
    }
  }
}
