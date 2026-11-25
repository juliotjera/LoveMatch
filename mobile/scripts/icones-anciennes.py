# Icônes « anciennes » (Android 7 et avant, sans icône adaptative) : capacitor-assets y pose le logo sans fond.
# On les redessine : tuile jacaranda foncé et logo, comme l'icône de l'application installable (PWA).
from pathlib import Path
from PIL import Image, ImageDraw

RES = Path(__file__).resolve().parent.parent / 'android' / 'app' / 'src' / 'main' / 'res'
LOGO = Path(__file__).resolve().parent.parent / 'assets' / 'logo.png'
TAILLES = {'mdpi': 48, 'hdpi': 72, 'xhdpi': 96, 'xxhdpi': 144, 'xxxhdpi': 192}
GRAND = 1024

fond = Image.new('RGBA', (GRAND, GRAND), (62, 44, 110, 255))
logo = Image.open(LOGO).convert('RGBA').resize((round(GRAND * 0.8), round(GRAND * 0.8)), Image.LANCZOS)
fond.alpha_composite(logo, (round(GRAND * 0.1), round(GRAND * 0.1)))

carre = Image.new('L', (GRAND, GRAND), 0)
ImageDraw.Draw(carre).rounded_rectangle((0, 0, GRAND - 1, GRAND - 1), radius=round(GRAND * 0.22), fill=255)
rond = Image.new('L', (GRAND, GRAND), 0)
ImageDraw.Draw(rond).ellipse((0, 0, GRAND - 1, GRAND - 1), fill=255)

for densite, taille in TAILLES.items():
    for nom, masque in (('ic_launcher.png', carre), ('ic_launcher_round.png', rond)):
        icone = Image.new('RGBA', (GRAND, GRAND), (0, 0, 0, 0))
        icone.paste(fond, (0, 0), masque)
        icone.resize((taille, taille), Image.LANCZOS).save(RES / f'mipmap-{densite}' / nom)
print('icônes anciennes : ', ', '.join(TAILLES))
