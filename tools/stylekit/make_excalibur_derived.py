"""Item textures derived from Excalibur art (used with Maffhew's permission; see CREDITS.md). Reads the pack from
~/ref/excalibur (tools/setup_refs.sh; never committed) and writes our altered versions into the mod:
  - paraffin_wax: Excalibur's resin brick, its shading remapped onto a soft waxy cream ramp;
  - oil_can: Excalibur's bucket with its open top closed into a domed lid, plus a brass thumb pump and a long spout
    (brass taken from Excalibur's lantern).
Run from the repo root: python3 tools/stylekit/make_excalibur_derived.py"""
import os
from PIL import Image

EX = os.path.expanduser('~/ref/excalibur/assets/minecraft/textures/item/')
OUT = 'mod/src/main/resources/assets/creativeengineering/textures/item/'
PREVIEW = 'tools/stylekit/out'


def lum(c):
    return (0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2]) / 255


def paraffin():
    src = Image.open(EX + 'resin_brick.png').convert('RGBA')
    ramp = [(150, 138, 108), (186, 175, 142), (214, 205, 172), (232, 225, 196), (244, 239, 216), (252, 249, 234)]
    ls = [lum(src.getpixel((x, y))) for y in range(16) for x in range(16) if src.getpixel((x, y))[3]]
    lo, hi = min(ls), max(ls)
    out = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            c = src.getpixel((x, y))
            if c[3]:
                out.putpixel((x, y), ramp[min(len(ramp) - 1, int((lum(c) - lo) / (hi - lo) * len(ramp)))] + (255,))
    return out


def oil_can():
    can = Image.open(EX + 'bucket.png').convert('RGBA')
    lan = Image.open(EX + 'lantern.png').convert('RGBA')

    def grey(v):          # the bucket's own pixel of that brightness, so the hue stays Excalibur's
        for y in range(16):
            for x in range(16):
                c = can.getpixel((x, y))
                if c[3] and int(sum(c[:3]) / 3) == v:
                    return c
        return (v, v, v, 255)
    g = {v: grey(v) for v in (31, 46, 62, 74, 93)}
    brass = sorted({lan.getpixel((x, y))[:3] for y in range(16) for x in range(16)
                    if lan.getpixel((x, y))[3] and lan.getpixel((x, y))[0] > lan.getpixel((x, y))[2] + 40}, key=sum)
    bd, bm, bl = brass[0] + (255,), brass[len(brass) // 2] + (255,), brass[-2] + (255,)
    for y in range(3, 8):                  # close the opening: a lid dome lit from the upper left
        for x in range(3, 13):
            c = can.getpixel((x, y))
            if c[3] and int(sum(c[:3]) / 3) <= 31:
                can.putpixel((x, y), g[93 if (y == 3 or (y == 4 and x < 8)) else 74 if (y <= 5 and x < 9) else 62 if x < 10 else 46])
    for x, y, c in ((5, 2, g[46]), (6, 2, g[31]), (5, 1, bl), (6, 1, bm)):                    # thumb pump
        can.putpixel((x, y), c)
    for x, y, c in ((9, 2, bd), (10, 2, bm), (10, 1, bl), (11, 1, bm), (11, 0, bl), (12, 0, bm), (12, 1, bd), (13, 0, bd), (11, 2, bd)):
        can.putpixel((x, y), c)                                                                   # long spout
    return can


if __name__ == '__main__':
    os.makedirs(PREVIEW, exist_ok=True)
    made = {'paraffin_wax': paraffin(), 'oil_can': oil_can()}
    sheet = Image.new('RGBA', (16 * len(made) + 2 * (len(made) + 1), 20), (44, 48, 54, 255))
    for i, (name, img) in enumerate(made.items()):
        img.save(OUT + name + '.png')
        sheet.alpha_composite(img, (2 + i * 18, 2))
    sheet.resize((sheet.width * 10, sheet.height * 10), Image.NEAREST).save(os.path.join(PREVIEW, 'excalibur_derived.png'))
    print(', '.join(made))
