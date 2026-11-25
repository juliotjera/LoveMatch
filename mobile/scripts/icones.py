# Dessine assets/logo.png (logo de LoveMatch sur fond transparent) pour « capacitor-assets » :
# icône et écran de démarrage = ce logo sur fond jacaranda foncé #3e2c6e.
# Même dessin que frontend/src/app/ui/logo.ts (grille de 40 unités) : deux cercles qui se recouvrent.
from pathlib import Path
from PIL import Image, ImageDraw

TAILLE = 1024
S = TAILLE * 4             # sur-échantillonnage pour des bords lisses
E = S / 40                 # une unité de la grille du logo
LILAS = (201, 168, 232, 255)
FLAMBOYANT = (228, 87, 46, 255)
CROISEMENT = (140, 40, 60, 255)


def cercle(cx, r):
    masque = Image.new('L', (S, S), 0)
    ImageDraw.Draw(masque).ellipse(((cx - r) * E, (20 - r) * E, (cx + r) * E, (20 + r) * E), fill=255)
    return masque


gauche, droite = cercle(15, 12), cercle(25, 12)
image = Image.new('RGBA', (S, S), (0, 0, 0, 0))
image.paste(Image.new('RGBA', (S, S), LILAS), (0, 0), gauche)
image.paste(Image.new('RGBA', (S, S), FLAMBOYANT), (0, 0), droite)
croisement = Image.new('L', (S, S), 0)
croisement.paste(gauche, (0, 0), droite)
image.paste(Image.new('RGBA', (S, S), CROISEMENT), (0, 0), croisement)
image = image.resize((TAILLE, TAILLE), Image.LANCZOS)

dossier = Path(__file__).resolve().parent.parent / 'assets'
dossier.mkdir(exist_ok=True)
image.save(dossier / 'logo.png')
image.save(dossier / 'logo-dark.png')
print('assets/logo.png', image.size)
