"""Oil Can item (docs/petroleum-spec.md): a classic dome-bottom oiler with a knurled brass collar and a long, thin,
bent spout, after period pump oilers (Kragnoth's reference photo). Drawn with Excalibur's palettes (iron ingot steel,
lantern brass, copper-ingot rust; used with Maffhew's permission, see CREDITS.md) so it sits with the pack.
Run from the repo root: python3 tools/stylekit/make_oil_can.py"""
import os
from PIL import Image

EX = os.path.expanduser('~/ref/excalibur/assets/minecraft/textures/item/')
OUT = 'mod/src/main/resources/assets/creativeengineering/textures/item/oil_can.png'


def lum(c):
    return 0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2]


def palette(name, pred=lambda c: True):
    im = Image.open(EX + name).convert('RGBA')
    return sorted({im.getpixel((x, y))[:3] for y in range(16) for x in range(16)
                   if im.getpixel((x, y))[3] and pred(im.getpixel((x, y)))}, key=lum)


def lighten(p, k):
    return [tuple(int(v + (255 - v) * k * i / (len(p) - 1)) for v in c) for i, c in enumerate(p)]


steel = lighten(palette('iron_ingot.png'), 0.35)          # tinplate: a touch brighter than the ingot
brass = palette('lantern.png', lambda c: c[0] > c[2] + 40)
rust = palette('copper_ingot.png')


def pick(p, t):
    t = max(0.0, min(1.0, t))
    return p[min(len(p) - 1, int(t * (len(p) - 1) + 0.5))] + (255,)


def oil_can():
    img = Image.new('RGBA', (16, 16))
    put = img.putpixel
    dome = {7: (5, 10), 8: (4, 11), 9: (3, 12), 10: (2, 13), 11: (2, 13), 12: (1, 14), 13: (1, 14)}
    for y, (a, b) in dome.items():
        cx, w = (a + b) / 2, (b - a) / 2
        for x in range(a, b + 1):
            t = 0.98 - 0.62 * ((x - cx) / w + 1) / 2 - 0.035 * (y - 7)   # lit from the upper left
            if x in (a, b):
                t = 0.12 if x == b else t - 0.3                          # silhouette edge, dark on the shadow side
            if y == 7:
                t -= 0.1                                                 # shoulder under the collar
            put((x, y), pick(steel, t))
    for x in range(1, 15):                                               # base rim, rolled seam
        put((x, 14), pick(steel, 0.18 if x in (1, 14) else 0.32 if x > 9 else 0.45))
    for x, y, t in ((4, 9, 1.0), (3, 10, 1.0), (5, 8, 0.95), (3, 11, 0.9)):   # specular streak
        put((x, y), pick(steel, t))
    for x, y in ((11, 12),):                                             # faint rust bloom in the shade
        put((x, y), pick(rust, 0.25))
    for x in range(5, 11):                                               # brass flange
        put((x, 6), pick(brass, 0.25 if x in (5, 10) else 0.45 if x > 7 else 0.7))
    for x in range(6, 10):                                               # knurled nut
        put((x, 5), pick(brass, 0.95 if x % 2 == 0 else 0.55))
    for x, y, t in ((7, 4, 1.0), (8, 4, 0.3), (7, 3, 0.95), (8, 3, 0.35), (8, 2, 0.9), (8, 1, 0.75), (9, 1, 0.3),
                    (9, 0, 0.95), (10, 0, 0.6)):
        put((x, y), pick(steel, t))                                      # tapered spout, lit edge left, bent tip
    return img


if __name__ == '__main__':
    can = oil_can()
    can.save(OUT)
    os.makedirs('tools/stylekit/out', exist_ok=True)
    s = Image.new('RGBA', (20, 20), (44, 48, 54, 255))
    s.alpha_composite(can, (2, 2))
    s.resize((200, 200), Image.NEAREST).save('tools/stylekit/out/oil_can_dome.png')
    print(OUT)
