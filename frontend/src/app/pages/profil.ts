import { ChangeDetectionStrategy, Component, OnInit, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Api } from '../core/api';
import { Avertisseur } from '../core/avis';
import { I18n } from '../core/i18n';
import { messageErreur } from '../core/intercepteur';
import { GenreRecherche, Interet, ProfilPublic, Recherche, Ville } from '../core/modeles';
import { Session } from '../core/session';
import { Icone } from '../ui/icone';
import { ChoixVille } from '../ui/choix-ville';
import { Photo } from '../ui/photo';

const MAX_PHOTOS = 4;

@Component({
  selector: 'lm-profil',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, Icone, Photo, ChoixVille],
  template: `
    @if (moi(); as m) {
      <div class="page">
        <header class="entete-page">
          <h1>{{ t('profil.titre') }}</h1>
          <p>{{ t('profil.sous') }}</p>
        </header>

        @if (!m.profilComplet) {
          <div class="etape" role="status">
            <h2>{{ bienvenue() ? t('profil.bienvenue', { prenom: m.prenom }) : t('profil.incompletTitre') }}</h2>
            <p>{{ t('profil.incomplet') }}</p>
            <ul>
              <li [class.fait]="!!bio.trim()">{{ t('profil.etapeBio') }}</li>
              <li [class.fait]="choisis().size >= 3">{{ t('profil.etapeInterets', { n: choisis().size }) }}</li>
              <li [class.fait]="m.photos.length > 0">{{ t('profil.etapePhoto') }}</li>
            </ul>
          </div>
        }

        <div class="colonnes">
          <section class="panneau" aria-labelledby="titre-photos">
            <h2 id="titre-photos">{{ t('profil.photos') }}</h2>
            <p class="aide">{{ t('profil.photosAide') }}</p>
            <ul class="grille-photos">
              @for (p of m.photos; track p; let i = $index) {
                <li>
                  <lm-photo [id]="p" [prenom]="m.prenom" [alt]="t('profil.maPhoto', { n: i + 1 })" />
                  @if (i === 0) {
                    <span class="principale">{{ t('profil.principale') }}</span>
                  }
                  <div class="actions-photo">
                    @if (i > 0) {
                      <button type="button" class="mini" (click)="principale(p)" [attr.aria-label]="t('profil.rendrePrincipale', { n: i + 1 })">
                        <lm-icone nom="etoile" [taille]="18" />
                      </button>
                    }
                    <button type="button" class="mini" (click)="supprimerPhoto(p)" [attr.aria-label]="t('profil.supprimerPhoto', { n: i + 1 })">
                      <lm-icone nom="poubelle" [taille]="18" />
                    </button>
                  </div>
                </li>
              }
              @if (m.photos.length < max) {
                <li>
                  <label class="ajouter" [class.occupe]="envoiPhoto()">
                    <input type="file" accept="image/jpeg,image/png,image/gif,image/webp" (change)="ajouterPhoto($event)"
                      [disabled]="envoiPhoto()" />
                    <lm-icone nom="plus" [taille]="28" />
                    <span>{{ envoiPhoto() ? t('profil.envoiPhoto') : t('profil.ajouterPhoto') }}</span>
                  </label>
                </li>
              }
            </ul>
          </section>

          <section class="panneau" aria-labelledby="titre-infos">
            <h2 id="titre-infos">{{ t('profil.infos') }}</h2>
            <form class="formulaire" (submit)="$event.preventDefault(); enregistrerProfil()">
              <div class="deux">
                <div class="champ">
                  <label for="prenom">{{ t('champ.prenom') }}</label>
                  <input id="prenom" name="prenom" class="saisie" [(ngModel)]="prenom" maxlength="60" required autocomplete="given-name" />
                </div>
                <div class="champ">
                  <label for="nom">{{ t('champ.nom') }}</label>
                  <input id="nom" name="nom" class="saisie" [(ngModel)]="nom" maxlength="60" required autocomplete="family-name"
                    aria-describedby="nom-aide" />
                  <p id="nom-aide" class="aide">{{ t('inscription.nomPrive') }}</p>
                </div>
              </div>
              <lm-choix-ville [libelle]="t('champ.ville')" [aide]="t('inscription.villeAide')" [erreur]="villeManquante()"
                [(ville)]="ville" />
              <fieldset class="champ">
                <legend>{{ t('champ.recherche') }}</legend>
                <div class="pastilles">
                  @for (r of recherches; track r) {
                    <label class="pastille"><input type="radio" name="recherche" [value]="r" [(ngModel)]="recherche" />{{ t('recherche.' + r) }}</label>
                  }
                </div>
              </fieldset>
              <div class="champ">
                <label for="bio">{{ t('champ.bio') }}</label>
                <textarea id="bio" name="bio" class="saisie" dir="auto" maxlength="500" [(ngModel)]="bio" aria-describedby="bio-aide"
                  [placeholder]="t('profil.bioExemple')"></textarea>
                <p id="bio-aide" class="aide">{{ t('profil.bioAide', { n: 500 - bio.length }) }}</p>
              </div>
              <fieldset class="champ">
                <legend>{{ t('profil.interets') }}</legend>
                <p class="aide" aria-live="polite">{{ t('profil.interetsAide', { n: choisis().size }) }}</p>
                <div class="pastilles">
                  @for (i of interets(); track i.id) {
                    <label class="pastille">
                      <input type="checkbox" [checked]="choisis().has(i.id)" (change)="basculerInteret(i)"
                        [disabled]="!choisis().has(i.id) && choisis().size >= 8" />
                      {{ libelle(i) }}
                    </label>
                  }
                </div>
              </fieldset>
              <fieldset class="champ">
                <legend>{{ t('langues.parlees') }}</legend>
                <p class="aide">{{ t('langues.aide') }}</p>
                <div class="pastilles">
                  @for (code of codesLangues; track code) {
                    <label class="pastille" [attr.lang]="code.toLowerCase()">
                      <input type="checkbox" [checked]="languesChoisies().has(code)" (change)="basculerLangue(code)"
                        [disabled]="!languesChoisies().has(code) && languesChoisies().size >= 5" />
                      {{ nomLangue(code) }}
                    </label>
                  }
                </div>
              </fieldset>
              @if (erreurProfil()) {
                <p class="alerte" role="alert">{{ erreurProfil() }}</p>
              }
              <button type="submit" class="btn btn-principal" [disabled]="occupe()">{{ t('action.enregistrer') }}</button>
            </form>
          </section>

          <section class="panneau" id="preferences" aria-labelledby="titre-preferences">
            <h2 id="titre-preferences">{{ t('profil.preferences') }}</h2>
            <p class="aide">{{ t('profil.preferencesAide') }}</p>
            <form class="formulaire" (submit)="$event.preventDefault(); enregistrerPreferences()">
              <fieldset class="champ">
                <legend>{{ t('champ.genreRecherche') }}</legend>
                <div class="pastilles">
                  @for (g of genres; track g) {
                    <label class="pastille"><input type="radio" name="genreRecherche" [value]="g" [(ngModel)]="genreRecherche" />{{ t('genreRecherche.' + g) }}</label>
                  }
                </div>
              </fieldset>
              <fieldset class="champ">
                <legend>{{ t('profil.tranche') }}</legend>
                <div class="tranche">
                  <label for="age-min" class="visuellement-cache">{{ t('profil.ageMin') }}</label>
                  <input id="age-min" name="ageMin" class="saisie" type="number" min="18" max="99" [(ngModel)]="ageMin" />
                  <span aria-hidden="true">{{ t('profil.a') }}</span>
                  <label for="age-max" class="visuellement-cache">{{ t('profil.ageMax') }}</label>
                  <input id="age-max" name="ageMax" class="saisie" type="number" min="18" max="99" [(ngModel)]="ageMax" />
                  <span>{{ t('profil.ans') }}</span>
                </div>
              </fieldset>
              <div class="champ">
                <label for="rayon">{{ t('profil.rayon') }}</label>
                <select id="rayon" name="rayon" class="saisie" [(ngModel)]="rayonKm">
                  @for (r of rayons(); track r) {
                    <option [ngValue]="r">{{ r >= 20000 ? t('profil.rayonPartout') : t('decouvrir.distanceMax', { km: i18n.nombre(r) }) }}</option>
                  }
                </select>
                <p class="aide">{{ t('profil.rayonAide') }}</p>
              </div>
              @if (erreurPreferences()) {
                <p class="alerte" role="alert">{{ erreurPreferences() }}</p>
              }
              <button type="submit" class="btn btn-principal" [disabled]="occupe()">{{ t('action.enregistrer') }}</button>
            </form>
          </section>

          <section class="panneau" aria-labelledby="titre-bloques">
            <h2 id="titre-bloques">{{ t('profil.bloques') }}</h2>
            @if (bloques().length === 0) {
              <p class="aide">{{ t('profil.aucunBloque') }}</p>
            } @else {
              <ul class="bloques">
                @for (b of bloques(); track b.id) {
                  <li>
                    <span>{{ b.prenom }}, {{ b.age }} ({{ i18n.joindre(b.ville, i18n.nomPays(b.pays)) }})</span>
                    <button type="button" class="btn btn-contour" (click)="debloquer(b)">{{ t('profil.debloquer') }}</button>
                  </li>
                }
              </ul>
            }
          </section>

          <section class="panneau" aria-labelledby="titre-compte">
            <h2 id="titre-compte">{{ t('profil.compte') }}</h2>
            <p class="aide">{{ t('profil.emailPrive', { email: m.email }) }}</p>
            <div class="export">
              <a class="btn btn-contour" href="/api/profil/export" download="lovematch-mes-donnees.json">{{ t('profil.exporter') }}</a>
              <p class="aide">{{ t('profil.exportAide') }}</p>
            </div>
            <div class="boutons">
              <button type="button" class="btn btn-contour" (click)="session.fermer()">{{ t('nav.deconnexion') }}</button>
              @if (!m.demo) {
                <button type="button" class="btn btn-danger" (click)="supprimerCompte()">{{ t('profil.supprimer') }}</button>
              }
            </div>
          </section>
        </div>
      </div>
    }
  `,
  styles: `
    .page {
      max-width: 900px;
    }
    .etape {
      display: grid;
      gap: 8px;
      margin-bottom: 20px;
      padding: 18px 20px;
      border-radius: var(--rayon-panneau);
      border: 1px solid var(--trait);
      background: var(--jacaranda-pale);
    }
    .etape p {
      color: var(--encre-douce);
    }
    .etape ul {
      display: grid;
      gap: 4px;
      margin: 6px 0 0;
      padding: 0;
      list-style: none;
    }
    .etape li::before {
      content: '';
      display: inline-block;
      width: 14px;
      height: 14px;
      margin-inline-end: 10px;
      border: 2px solid var(--trait-fort);
      border-radius: 50%;
      vertical-align: -2px;
    }
    .etape li.fait::before {
      background: var(--flamboyant-vif);
      border-color: var(--flamboyant-vif);
    }
    .colonnes {
      display: grid;
      gap: 18px;
    }
    .panneau {
      display: grid;
      gap: 14px;
      align-content: start;
    }
    .aide {
      color: var(--encre-douce);
    }
    .formulaire {
      display: grid;
      gap: 18px;
    }
    .deux {
      display: grid;
      align-items: start;
      gap: 18px;
    }
    .grille-photos {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 10px;
      margin: 0;
      padding: 0;
      list-style: none;
    }
    .grille-photos li {
      position: relative;
    }
    .principale {
      position: absolute;
      inset-inline-start: 8px;
      top: 8px;
      padding: 2px 10px;
      border-radius: 4px;
      background: rgb(31 24 51 / 78%);
      color: #fff;
      font-size: 0.8rem;
      font-weight: 700;
    }
    .actions-photo {
      position: absolute;
      inset-inline-end: 8px;
      bottom: 8px;
      display: flex;
      gap: 6px;
    }
    .mini {
      display: grid;
      place-items: center;
      width: 44px;
      height: 44px;
      border: 0;
      border-radius: 50%;
      background: var(--voile);
      color: var(--encre);
      cursor: pointer;
    }
    .ajouter {
      display: grid;
      place-items: center;
      align-content: center;
      gap: 6px;
      aspect-ratio: 3 / 4;
      border: 2px dashed var(--trait-fort);
      border-radius: var(--rayon-photo);
      color: var(--jacaranda-fonce);
      font-weight: 700;
      text-align: center;
      padding: 8px;
      cursor: pointer;
    }
    .ajouter:hover {
      background: var(--jacaranda-pale);
    }
    .ajouter:has(input:focus-visible) {
      outline: 3px solid var(--jacaranda);
      outline-offset: 2px;
    }
    .ajouter.occupe {
      opacity: 0.6;
    }
    .ajouter input {
      position: absolute;
      opacity: 0;
      width: 1px;
      height: 1px;
    }
    .tranche {
      display: flex;
      align-items: center;
      gap: 10px;
    }
    .tranche .saisie {
      width: 92px;
    }
    output {
      margin-inline-start: 6px;
      color: var(--jacaranda-fonce);
    }
    .bloques {
      display: grid;
      gap: 8px;
      margin: 0;
      padding: 0;
      list-style: none;
    }
    .bloques li {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 10px;
    }
    .export {
      display: grid;
      justify-items: start;
      gap: 6px;
    }
    .boutons {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
    }
    @media (min-width: 560px) {
      .grille-photos {
        grid-template-columns: repeat(4, minmax(0, 1fr));
      }
      .deux {
        grid-template-columns: 1fr 1fr;
      }
    }
  `,
})
export class Profil implements OnInit {
  private readonly api = inject(Api);
  private readonly avis = inject(Avertisseur);
  private readonly router = inject(Router);
  protected readonly i18n = inject(I18n);
  protected readonly session = inject(Session);
  protected readonly t = this.i18n.t;
  // rayons proposés ; la valeur enregistrée est ajoutée si elle n'en fait pas partie
  protected readonly rayons = () =>
    [...new Set([10, 25, 50, 100, 200, 500, 1000, 5000, 20000, this.rayonKm])].sort((a, b) => a - b);
  protected readonly libelle = this.i18n.interet;
  protected readonly moi = this.session.moi;

  readonly bienvenue = input<string>();

  protected readonly max = MAX_PHOTOS;
  protected readonly recherches: Recherche[] = ['RELATION_SERIEUSE', 'AMITIE', 'A_DECOUVRIR'];
  protected readonly genres: GenreRecherche[] = ['HOMMES', 'FEMMES', 'TOUS'];
  protected readonly ville = signal<Ville | null>(null);
  protected readonly villeManquante = signal(false);
  protected readonly interets = signal<Interet[]>([]);
  protected readonly bloques = signal<ProfilPublic[]>([]);
  protected readonly choisis = signal<Set<number>>(new Set());
  protected readonly languesChoisies = signal<Set<string>>(new Set());
  protected readonly codesLangues = ['MG', 'FR', 'EN', 'ES', 'ZH', 'HI', 'AR', 'DE', 'IT', 'PT', 'RU', 'JA'];
  protected readonly nomLangue = this.i18n.nomLangue;
  protected readonly occupe = signal(false);
  protected readonly envoiPhoto = signal(false);
  protected readonly erreurProfil = signal('');
  protected readonly erreurPreferences = signal('');

  protected prenom = '';
  protected nom = '';
  protected recherche: Recherche = 'A_DECOUVRIR';
  protected bio = '';
  protected genreRecherche: GenreRecherche = 'TOUS';
  protected ageMin = 18;
  protected ageMax = 35;
  protected rayonKm = 200;

  async ngOnInit() {
    const [interets, bloques] = await Promise.all([this.api.interets(), this.api.bloques()]);
    this.interets.set(interets);
    this.bloques.set(bloques);
    await this.session.rafraichir();
    this.remplir();
    if (location.hash === '#preferences') {
      queueMicrotask(() => document.getElementById('preferences')?.scrollIntoView());
    }
  }

  private remplir() {
    const m = this.moi();
    if (!m) {
      return;
    }
    this.prenom = m.prenom;
    this.nom = m.nom;
    this.ville.set(m.ville);
    this.recherche = m.recherche;
    this.bio = m.bio ?? '';
    this.choisis.set(new Set(m.interets.map((i) => i.id)));
    this.languesChoisies.set(new Set(m.langues));
    this.genreRecherche = m.genreRecherche;
    this.ageMin = m.ageMin;
    this.ageMax = m.ageMax;
    this.rayonKm = m.rayonKm;
  }

  basculerInteret(i: Interet) {
    this.choisis.update((s) => {
      const copie = new Set(s);
      if (copie.has(i.id)) {
        copie.delete(i.id);
      } else if (copie.size < 8) {
        copie.add(i.id);
      }
      return copie;
    });
  }

  basculerLangue(code: string) {
    this.languesChoisies.update((s) => {
      const copie = new Set(s);
      if (copie.has(code)) {
        copie.delete(code);
      } else if (copie.size < 5) {
        copie.add(code);
      }
      return copie;
    });
  }

  async enregistrerProfil() {
    this.erreurProfil.set('');
    if (this.languesChoisies().size === 0) {
      this.erreurProfil.set(this.t('langues.min'));
      return;
    }
    this.villeManquante.set(!this.ville());
    if (!this.ville()) {
      this.erreurProfil.set(this.t('ville.choisir'));
      return;
    }
    if (this.choisis().size < 3) {
      this.erreurProfil.set(this.t('profil.interetsMin'));
      return;
    }
    this.occupe.set(true);
    const etaitComplet = this.moi()?.profilComplet;
    try {
      const moi = await this.api.modifierProfil({
        prenom: this.prenom.trim(), nom: this.nom.trim(), villeId: this.ville()!.id, bio: this.bio.trim(),
        recherche: this.recherche, interets: [...this.choisis()], langues: [...this.languesChoisies()],
      });
      this.session.ouvrir(moi);
      if (!etaitComplet && moi.profilComplet) {
        this.avis.info(this.t('profil.visible'), { libelle: this.t('nav.decouvrir'), faire: () => this.router.navigateByUrl('/decouvrir') });
      } else {
        this.avis.info(this.t('profil.enregistre'));
      }
    } catch (e) {
      this.erreurProfil.set(messageErreur(e, this.t('erreur.serveur')));
    } finally {
      this.occupe.set(false);
    }
  }

  async enregistrerPreferences() {
    this.erreurPreferences.set('');
    if (this.ageMin > this.ageMax) {
      this.erreurPreferences.set(this.t('profil.trancheInvalide'));
      return;
    }
    this.occupe.set(true);
    try {
      this.session.ouvrir(await this.api.modifierPreferences({
        genreRecherche: this.genreRecherche, ageMin: this.ageMin, ageMax: this.ageMax, rayonKm: this.rayonKm,
      }));
      this.avis.info(this.t('profil.enregistre'));
    } catch (e) {
      this.erreurPreferences.set(messageErreur(e, this.t('erreur.serveur')));
    } finally {
      this.occupe.set(false);
    }
  }

  async ajouterPhoto(evenement: Event) {
    const champ = evenement.target as HTMLInputElement;
    const fichier = champ.files?.[0];
    champ.value = '';
    if (!fichier) {
      return;
    }
    if (fichier.size > 8 * 1024 * 1024) {
      this.avis.erreur(this.t('profil.photoTropLourde'));
      return;
    }
    this.envoiPhoto.set(true);
    try {
      await this.api.ajouterPhoto(fichier);
      await this.session.rafraichir();
    } catch (e) {
      this.avis.erreur(messageErreur(e, this.t('erreur.serveur')));
    } finally {
      this.envoiPhoto.set(false);
    }
  }

  async supprimerPhoto(id: number) {
    if (!confirm(this.t('profil.supprimerPhotoConfirmer'))) {
      return;
    }
    await this.api.supprimerPhoto(id);
    await this.session.rafraichir();
  }

  async principale(id: number) {
    await this.api.photoPrincipale(id);
    await this.session.rafraichir();
  }

  async debloquer(p: ProfilPublic) {
    await this.api.debloquer(p.id);
    this.bloques.update((l) => l.filter((b) => b.id !== p.id));
    this.avis.info(this.t('profil.debloque', { prenom: p.prenom }));
  }

  async supprimerCompte() {
    if (!confirm(this.t('profil.supprimerConfirmer'))) {
      return;
    }
    await this.api.supprimerCompte();
    this.session.perdue();
    this.avis.info(this.t('profil.supprime'));
    await this.router.navigateByUrl('/');
  }
}
