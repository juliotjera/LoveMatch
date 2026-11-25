import {
  ChangeDetectionStrategy, Component, ElementRef, HostListener, OnInit, computed, inject, signal, viewChild,
} from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { Api } from '../core/api';
import { Avertisseur } from '../core/avis';
import { I18n } from '../core/i18n';
import { messageErreur } from '../core/intercepteur';
import { Decision, ProfilPublic, Suggestion } from '../core/modeles';
import { Session } from '../core/session';
import { Icone } from '../ui/icone';
import { Photo } from '../ui/photo';
import { FenetreSignalement } from '../ui/signalement';
import { Tissage } from '../ui/tissage';

type Etat = 'chargement' | 'pret' | 'incomplet' | 'erreur';

@Component({
  selector: 'lm-decouvrir',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, Icone, Photo, Tissage, FenetreSignalement],
  template: `
    <div class="page">
      <div class="entete">
        <h1>{{ t('decouvrir.titre') }}</h1>
        <div class="filtres" role="group" [attr.aria-label]="t('decouvrir.filtres')">
          <label class="visuellement-cache" for="distance">{{ t('decouvrir.distance') }}</label>
          <select id="distance" class="saisie filtre" [value]="distanceMax() ?? ''" (change)="changerDistance($event)">
            <option value="">{{ t('decouvrir.distanceToutes') }}</option>
            @for (d of distances; track d) {
              <option [value]="d">{{ t('decouvrir.distanceMax', { km: i18n.nombre(d) }) }}</option>
            }
          </select>
          <button type="button" class="pastille" [attr.aria-pressed]="interetsCommuns()" (click)="basculerInterets()">
            {{ t('decouvrir.interetsCommuns') }}
          </button>
        </div>
      </div>

      @if (likesRecus() > 0) {
        <p class="recus">
          <lm-icone nom="coeur" [plein]="true" [taille]="18" />
          <span>{{ t(likesRecus() === 1 ? 'decouvrir.recusUn' : 'decouvrir.recus', { n: likesRecus() }) }}</span>
        </p>
      }

      <p class="visuellement-cache" aria-live="polite">{{ annonce() }}</p>

      @switch (etat()) {
        @case ('chargement') {
          <div class="carte squelette" aria-hidden="true"><div class="photo-vide"></div><div></div></div>
        }
        @case ('incomplet') {
          <div class="vide">
            <h2>{{ t('decouvrir.incompletTitre') }}</h2>
            <p>{{ t('decouvrir.incomplet') }}</p>
            <a routerLink="/profil" class="btn btn-principal">{{ t('decouvrir.completer') }}</a>
          </div>
        }
        @case ('erreur') {
          <div class="vide">
            <p>{{ erreur() }}</p>
            <button type="button" class="btn btn-contour" (click)="recharger()">{{ t('action.reessayer') }}</button>
          </div>
        }
        @default {
          @if (actuel(); as s) {
            <article class="carte" [attr.aria-labelledby]="'nom-' + s.profil.id" #carte tabindex="-1">
              <div class="galerie">
                <lm-photo [id]="s.profil.photos[photo()]" [prenom]="s.profil.prenom"
                  [alt]="t('decouvrir.photoDe', { prenom: s.profil.prenom, n: photo() + 1, total: nbPhotos() })" />
                <div class="sur-photo" aria-hidden="true">
                  <span class="sur-nom">{{ s.profil.prenom }}, {{ s.profil.age }}</span>
                  <span>{{ lieu(s) }}</span>
                </div>
                @if (nbPhotos() > 1) {
                  <div class="points" aria-hidden="true">
                    @for (p of s.profil.photos; track p; let i = $index) {
                      <span [class.actif]="i === photo()"></span>
                    }
                  </div>
                  <button type="button" class="zone gauche" (click)="changerPhoto(-1)" [attr.aria-label]="t('decouvrir.photoPrec')"></button>
                  <button type="button" class="zone droite" (click)="changerPhoto(1)" [attr.aria-label]="t('decouvrir.photoSuiv')"></button>
                }
              </div>

              <div class="infos">
                <div class="identite">
                  <h2 [id]="'nom-' + s.profil.id">{{ s.profil.prenom }}, {{ s.profil.age }}</h2>
                  <p class="lieu">
                    <lm-icone nom="lieu" [taille]="18" />
                    {{ lieu(s) }}
                  </p>
                  <p class="cherche">{{ t('recherche.' + s.profil.recherche) }}</p>
                  <p class="parle">{{ t('decouvrir.parle', { langues: i18nPublic.listeLangues(s.profil.langues) }) }}</p>
                </div>

                <lm-tissage [score]="s.score" />

                <div class="interets">
                  <h3 class="visuellement-cache">{{ t('profil.interets') }}</h3>
                  <ul class="pastilles">
                    @for (i of interetsTries(); track i.id) {
                      <li class="pastille" [class.pastille-commune]="estCommun(i.code)">
                        {{ libelle(i) }}
                        @if (estCommun(i.code)) {
                          <span class="visuellement-cache">({{ t('profil.enCommun') }})</span>
                        }
                      </li>
                    }
                  </ul>
                </div>

                @if (s.profil.bio) {
                  <p class="bio" dir="auto">{{ s.profil.bio }}</p>
                }

                <button type="button" class="btn btn-discret signaler" (click)="signalement()?.ouvrir()">
                  <lm-icone nom="drapeau" [taille]="18" />{{ t('signaler.lien') }}
                </button>
              </div>
            </article>

            <div class="decision">
              <button type="button" class="btn btn-contour rond" (click)="reagir('PASSE')" [disabled]="occupe()"
                [attr.aria-keyshortcuts]="'ArrowLeft'">
                <lm-icone nom="passer" />{{ t('decouvrir.passer') }}
              </button>
              <button type="button" class="btn btn-aimer rond" (click)="reagir('LIKE')" [disabled]="occupe()"
                [attr.aria-keyshortcuts]="'ArrowRight'">
                <lm-icone nom="coeur" [plein]="true" />{{ t('decouvrir.aimer') }}
              </button>
            </div>
            <p class="raccourcis">{{ t('decouvrir.raccourcis') }}</p>

            <lm-signalement [cibleId]="s.profil.id" [prenom]="s.profil.prenom" (traite)="apresSignalement($event.bloque)" />
          } @else {
            <div class="vide">
              <h2>{{ t('decouvrir.finTitre') }}</h2>
              <p>{{ t('decouvrir.fin') }}</p>
              <div class="boutons">
                <a routerLink="/profil" fragment="preferences" class="btn btn-principal">{{ t('decouvrir.elargir') }}</a>
                @if (distanceMax() || interetsCommuns()) {
                  <button type="button" class="btn btn-contour" (click)="retirerFiltres()">{{ t('decouvrir.sansFiltre') }}</button>
                }
              </div>
            </div>
          }
        }
      }
    </div>

    <dialog class="match" #fenetreMatch aria-labelledby="titre-match" (close)="match.set(null)">
      @if (match(); as m) {
        <div class="duo" aria-hidden="true">
          <lm-photo class="rond-photo gauche" [id]="moi()?.photos?.[0]" [prenom]="moi()?.prenom ?? ''" />
          <lm-photo class="rond-photo droite" [id]="m.profil.photos[0]" [prenom]="m.profil.prenom" />
        </div>
        <h2 id="titre-match">{{ t('match.titre') }}</h2>
        <p>{{ t('match.texte', { prenom: m.profil.prenom }) }}</p>
        <div class="boutons">
          <a class="btn btn-aimer" [routerLink]="['/matchs', m.correspondanceId]" (click)="fermerMatch()">
            {{ t('match.ecrire', { prenom: m.profil.prenom }) }}
          </a>
          <button type="button" class="btn btn-contour" (click)="fermerMatch()">{{ t('match.continuer') }}</button>
        </div>
      }
    </dialog>
  `,
  styles: `
    .page {
      max-width: 980px;
    }
    .entete {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      justify-content: space-between;
      gap: 10px 12px;
      margin-bottom: 14px;
    }
    .entete h1 {
      font-size: 1.7rem;
    }
    .filtres {
      display: flex;
      gap: 8px;
    }
    .filtres .pastille {
      white-space: nowrap;
    }
    .filtre {
      width: auto;
      min-height: 44px;
      padding: 6px 12px;
      border-radius: 999px;
    }
    .recus {
      display: flex;
      align-items: flex-start;
      gap: 8px;
      margin-bottom: 14px;
      padding: 8px 12px;
      font-size: 0.92rem;
      line-height: 1.4;
      border-radius: 12px;
      background: var(--flamboyant-pale);
      color: #7c2a0f;
      font-weight: 700;
    }
    .carte {
      display: grid;
      gap: 18px;
      background: var(--carte);
      border-radius: 28px;
      padding: 12px;
      box-shadow: var(--ombre-carte);
      animation: arrivee 0.32s ease-out;
    }
    .carte:focus {
      outline: none;
    }
    @keyframes arrivee {
      from { opacity: 0; transform: translateY(8px); }
    }
    .galerie {
      position: relative;
      --format: 4 / 5;
    }
    /* téléphone : prénom et ville sur la photo, pour les voir sans faire défiler */
    .sur-photo {
      position: absolute;
      inset: auto 0 0 0;
      display: grid;
      gap: 2px;
      padding: 60px 18px 16px;
      border-radius: 0 0 var(--rayon-photo) var(--rayon-photo);
      background: linear-gradient(180deg, transparent, rgb(20 14 36 / 78%));
      color: #fff;
      pointer-events: none;
    }
    .sur-nom {
      font-family: var(--police-titre);
      font-size: 2rem;
      font-weight: 720;
      line-height: 1.05;
    }
    .identite h2,
    .identite .lieu {
      position: absolute;
      width: 1px;
      height: 1px;
      overflow: hidden;
      clip-path: inset(50%);
    }
    .identite .cherche {
      margin-top: 0;
    }
    .points {
      position: absolute;
      top: 10px;
      inset-inline: 12px;
      display: flex;
      gap: 4px;
    }
    .points span {
      flex: 1;
      height: 4px;
      border-radius: 4px;
      background: rgb(255 255 255 / 45%);
    }
    .points span.actif {
      background: #fff;
    }
    .zone {
      position: absolute;
      top: 0;
      bottom: 0;
      width: 40%;
      border: 0;
      background: transparent;
      cursor: pointer;
    }
    .zone.gauche { inset-inline-start: 0; }
    .zone.droite { inset-inline-end: 0; }
    .zone:focus-visible {
      outline-offset: -4px;
      border-radius: var(--rayon-photo);
    }
    .infos {
      display: grid;
      gap: 18px;
      align-content: start;
      padding: 4px 8px 8px;
    }
    h2 {
      font-size: clamp(1.9rem, 6vw, 2.6rem);
      font-weight: 720;
    }
    .lieu {
      display: flex;
      align-items: center;
      gap: 6px;
      color: var(--encre-douce);
      margin-top: 6px;
    }
    .cherche {
      display: inline-block;
      margin-top: 10px;
      padding: 3px 12px;
      border-radius: 999px;
      background: #f1e4cc;
      color: #6b4510;
      font-weight: 700;
      font-size: 0.92rem;
    }
    .parle {
      margin-top: 8px;
      color: var(--encre-douce);
      font-size: 0.95rem;
    }
    .bio {
      font-size: 1.05rem;
      max-width: 60ch;
    }
    .signaler {
      justify-self: start;
      color: var(--encre-douce);
      font-size: 0.92rem;
      min-height: 40px;
    }
    .decision {
      position: sticky;
      bottom: calc(var(--barre-basse) + 10px);
      z-index: 5;
      display: grid;
      grid-template-columns: 1fr 1.4fr;
      gap: 10px;
      margin-top: 14px;
      padding: 10px;
      border-radius: 999px;
      background: rgb(255 255 255 / 92%);
      backdrop-filter: blur(6px);
      box-shadow: 0 8px 30px -10px rgb(31 24 51 / 35%);
    }
    .rond {
      min-height: 56px;
      font-size: 1.05rem;
    }
    .raccourcis {
      display: none;
      margin-top: 10px;
      color: var(--encre-douce);
      font-size: 0.9rem;
      text-align: center;
    }
    .squelette {
      min-height: 420px;
    }
    .photo-vide {
      aspect-ratio: 4 / 5;
      border-radius: var(--rayon-photo);
      background: linear-gradient(100deg, var(--jacaranda-pale) 30%, #f4f0fa 50%, var(--jacaranda-pale) 70%);
      background-size: 300% 100%;
      animation: lueur 1.4s linear infinite;
    }
    @keyframes lueur {
      to { background-position: -150% 0; }
    }
    .boutons {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
    }
    .match {
      width: min(420px, calc(100% - 24px));
      padding: 28px 22px 24px;
      border: 0;
      border-radius: 28px;
      text-align: center;
      background: linear-gradient(180deg, #fff 55%, var(--flamboyant-pale));
    }
    .match::backdrop {
      background: rgb(31 24 51 / 70%);
    }
    .match[open] {
      display: grid;
      gap: 12px;
      justify-items: center;
    }
    .duo {
      position: relative;
      width: 210px;
      height: 120px;
      margin-bottom: 6px;
    }
    .rond-photo {
      position: absolute;
      top: 0;
      width: 120px;
      height: 120px;
      --rayon: 50%;
      --format: 1;
      --taille-initiale: 2.4rem;
      border: 4px solid #fff;
      box-shadow: 0 10px 24px -8px rgb(31 24 51 / 40%);
    }
    .rond-photo.gauche {
      left: 0;
      animation: rapprocher-g 0.7s cubic-bezier(0.2, 0.9, 0.25, 1.15) both;
    }
    .rond-photo.droite {
      right: 0;
      animation: rapprocher-d 0.7s cubic-bezier(0.2, 0.9, 0.25, 1.15) both;
    }
    @keyframes rapprocher-g {
      from { transform: translateX(-60px) rotate(-8deg); opacity: 0; }
    }
    @keyframes rapprocher-d {
      from { transform: translateX(60px) rotate(8deg); opacity: 0; }
    }
    .match h2 {
      font-size: 2rem;
    }
    .match p {
      color: var(--encre-douce);
    }
    .match .boutons {
      justify-content: center;
      margin-top: 8px;
    }
    @media (min-width: 760px) {
      .carte {
        grid-template-columns: minmax(0, 0.95fr) minmax(0, 1.05fr);
        padding: 14px;
        gap: 26px;
      }
      .infos {
        padding: 16px 12px 12px 0;
      }
      .sur-photo {
        display: none;
      }
      .identite h2,
      .identite .lieu {
        position: static;
        width: auto;
        height: auto;
        overflow: visible;
        clip-path: none;
      }
      .identite .lieu {
        display: flex;
      }
      .identite .cherche {
        margin-top: 10px;
      }
      .decision {
        width: min(460px, 100%);
        margin-inline: auto;
      }
      .raccourcis {
        display: block;
      }
    }
  `,
})
export class Decouvrir implements OnInit {
  private readonly api = inject(Api);
  private readonly avis = inject(Avertisseur);
  protected readonly i18n = inject(I18n);
  private readonly session = inject(Session);
  protected readonly t = this.i18n.t;
  protected readonly libelle = this.i18n.interet;
  protected readonly i18nPublic = this.i18n;
  protected readonly moi = this.session.moi;

  protected readonly distances = [25, 50, 100, 200, 500, 1000, 5000];
  protected readonly etat = signal<Etat>('chargement');
  protected readonly erreur = signal('');
  protected readonly file = signal<Suggestion[]>([]);
  protected readonly photo = signal(0);
  protected readonly occupe = signal(false);
  protected readonly distanceMax = signal<number | null>(null);
  protected readonly interetsCommuns = signal(false);
  protected readonly likesRecus = signal(0);
  protected readonly annonce = signal('');
  protected readonly match = signal<{ profil: ProfilPublic; correspondanceId: number } | null>(null);

  private readonly fenetreMatch = viewChild.required<ElementRef<HTMLDialogElement>>('fenetreMatch');
  private readonly carte = viewChild<ElementRef<HTMLElement>>('carte');
  protected readonly signalement = viewChild(FenetreSignalement);

  protected readonly actuel = computed(() => this.file()[0] ?? null);
  protected readonly nbPhotos = computed(() => this.actuel()?.profil.photos.length ?? 0);
  // les centres d'intérêt partagés d'abord
  protected readonly interetsTries = computed(() => {
    const s = this.actuel();
    if (!s) {
      return [];
    }
    return [...s.profil.interets].sort((a, b) => Number(this.estCommun(b.code)) - Number(this.estCommun(a.code)));
  });

  async ngOnInit() {
    await this.recharger();
    this.api.likesRecus().then((r) => this.likesRecus.set(r.nombre), () => undefined);
  }

  // « Paris, France, à 8 735 km » : le pays n'est précisé que s'il diffère du mien
  lieu(s: Suggestion) {
    const monPays = this.session.moi()?.ville.pays;
    const ville = s.profil.pays === monPays ? s.profil.ville : this.i18n.joindre(s.profil.ville, this.i18n.nomPays(s.profil.pays));
    return this.t('decouvrir.lieu', { ville, km: this.i18n.nombre(s.score.distanceKm) });
  }

  estCommun(code: string) {
    return this.actuel()?.score.interetsCommuns.includes(code) ?? false;
  }

  async recharger() {
    this.etat.set('chargement');
    try {
      const page = await this.api.suggestions(0, this.distanceMax(), this.interetsCommuns());
      this.file.set(page.elements);
      this.photo.set(0);
      this.etat.set('pret');
    } catch (e) {
      if (e instanceof HttpErrorResponse && e.status === 409 && !this.session.moi()?.profilComplet) {
        this.etat.set('incomplet');
      } else {
        this.erreur.set(messageErreur(e, this.t('erreur.serveur')));
        this.etat.set('erreur');
      }
    }
  }

  changerDistance(evenement: Event) {
    const valeur = (evenement.target as HTMLSelectElement).value;
    this.distanceMax.set(valeur ? Number(valeur) : null);
    this.recharger();
  }

  basculerInterets() {
    this.interetsCommuns.update((v) => !v);
    this.recharger();
  }

  retirerFiltres() {
    this.distanceMax.set(null);
    this.interetsCommuns.set(false);
    this.recharger();
  }

  changerPhoto(sens: number) {
    const n = this.nbPhotos();
    this.photo.update((i) => (i + sens + n) % n);
  }

  @HostListener('document:keydown', ['$event'])
  raccourci(evenement: KeyboardEvent) {
    const cible = evenement.target as HTMLElement;
    if (this.match() || cible.closest('input, select, textarea, dialog') || !this.actuel() || this.occupe()) {
      return;
    }
    if (evenement.key === 'ArrowRight') {
      evenement.preventDefault();
      this.reagir('LIKE');
    } else if (evenement.key === 'ArrowLeft') {
      evenement.preventDefault();
      this.reagir('PASSE');
    }
  }

  async reagir(decision: Decision) {
    const s = this.actuel();
    if (!s) {
      return;
    }
    this.occupe.set(true);
    try {
      const reponse = await this.api.reagir(s.profil.id, decision);
      this.suivant();
      if (reponse.match && reponse.correspondanceId) {
        this.likesRecus.update((n) => Math.max(0, n - 1));
        this.match.set({ profil: s.profil, correspondanceId: reponse.correspondanceId });
        this.fenetreMatch().nativeElement.showModal();
      } else if (decision === 'PASSE') {
        this.avis.info(this.t('decouvrir.passe', { prenom: s.profil.prenom }), {
          libelle: this.t('decouvrir.revoir'),
          faire: () => this.revoir(s),
        });
      }
    } catch (e) {
      this.avis.erreur(messageErreur(e, this.t('erreur.serveur')));
      // profil devenu indisponible (déjà vu, bloqué) : on passe au suivant
      if (e instanceof HttpErrorResponse && e.status === 409) {
        this.suivant();
      }
    } finally {
      this.occupe.set(false);
    }
  }

  private suivant() {
    this.file.update((f) => f.slice(1));
    this.photo.set(0);
    const prochain = this.actuel();
    this.annonce.set(prochain ? this.t('decouvrir.annonce', { prenom: prochain.profil.prenom, score: prochain.score.total }) : this.t('decouvrir.finTitre'));
    queueMicrotask(() => this.carte()?.nativeElement.focus({ preventScroll: true }));
    if (this.file().length < 3) {
      this.completer();
    }
  }

  // plus que deux profils : on demande la suite (les profils déjà vus ne reviennent pas)
  private async completer() {
    try {
      const page = await this.api.suggestions(0, this.distanceMax(), this.interetsCommuns());
      const presents = new Set(this.file().map((s) => s.profil.id));
      this.file.update((f) => [...f, ...page.elements.filter((s) => !presents.has(s.profil.id))]);
    } catch {
      // on réessaiera au prochain profil
    }
  }

  private async revoir(s: Suggestion) {
    try {
      await this.api.annulerPasse(s.profil.id);
      this.file.update((f) => [s, ...f.filter((x) => x.profil.id !== s.profil.id)]);
      this.photo.set(0);
    } catch (e) {
      this.avis.erreur(messageErreur(e, this.t('erreur.serveur')));
    }
  }

  apresSignalement(bloque: boolean) {
    if (bloque) {
      this.suivant();
    }
  }

  fermerMatch() {
    this.fenetreMatch().nativeElement.close();
    this.match.set(null);
  }
}
