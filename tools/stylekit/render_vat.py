"""Look-approval renders for the Fermenting Vat (docs/fermentation-spec.md). Previews only (out/ is git-ignored).
Mirrors the game: Create's fluid tank textures run through the same recolour as client/texture/RecolorSpriteSource
(settings read from the generated atlas JSON), then RECTANGLE connected tiles picked like Create's CTModel."""
import json
import os
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(__file__))
import render_scene  # noqa: E402
from render_scene import Scene, cube  # noqa: E402

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.join(HERE, '..', '..')
OUT = os.path.join(HERE, 'out', 'vat')
CT = os.path.join(OUT, 'ct')
os.makedirs(os.path.join(CT, 'textures'), exist_ok=True)
render_scene.ROOTS['preview'] = CT
CREATE = os.path.expanduser('~/ref/create/src/main/resources/assets/create/textures/block/')
ATLAS = json.load(open(os.path.join(REPO, 'mod/src/main/resources/assets/minecraft/atlases/blocks.json')))['sources'][0]
CE = 'creativeengineering:block/'
GROUND = cube('minecraft:block/smooth_stone', frm=(0, 15, 0), to=(16, 16, 16))


def recolor(im):
    t = int(ATLAS['tint'].lstrip('#'), 16)
    tint = ((t >> 16 & 255) / 255, (t >> 8 & 255) / 255, (t & 255) / 255)
    sat, con, bri = ATLAS['saturation'], ATLAS['contrast'], ATLAS['brightness']
    im = im.convert('RGBA')
    px = im.load()
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = px[x, y]
            if not a:
                continue
            r, g, b = r / 255, g / 255, b / 255
            l2 = min(1, max(0, ((0.299 * r + 0.587 * g + 0.114 * b) - 0.5) * con + 0.5 + bri))
            px[x, y] = tuple(min(255, max(0, round((l2 * (1 - sat) + c * sat) * k * 255))) for c, k in zip((r, g, b), tint)) + (a,)
    return im


SHEETS = {n: recolor(Image.open(CREATE + n + '.png')) for n in ('fluid_tank_connected', 'fluid_tank_top_connected')}


def tile(sheet, L, R, T, B):
    """RECTANGLE tile for a face with neighbours left / right / up / down (True = connected)."""
    tx = {(False, False): 0, (False, True): 1, (True, True): 2, (True, False): 3}[(L, R)]
    ty = {(False, True): 0, (True, True): 1, (True, False): 2, (False, False): 3}[(T, B)]
    name = '%s_%d_%d' % (sheet, tx, ty)
    path = os.path.join(CT, 'textures', name + '.png')
    if not os.path.exists(path):
        SHEETS[sheet].crop((tx * 16, ty * 16, tx * 16 + 16, ty * 16 + 16)).save(path)
    return 'preview:' + name


SIDES = {'north': ((1, 0, 0), (-1, 0, 0)), 'south': ((-1, 0, 0), (1, 0, 0)),
         'east': ((0, 0, 1), (0, 0, -1)), 'west': ((0, 0, -1), (0, 0, 1))}
NORMAL = {'north': (0, 0, -1), 'south': (0, 0, 1), 'east': (1, 0, 0), 'west': (-1, 0, 0)}


def vat(sc, w, h, d, heat='campfire', working=False):
    inside = lambda x, y, z: 0 <= x < w and 0 <= y < h and 0 <= z < d
    for x in range(-1, w + 1):
        for z in range(-1, d + 1):
            sc.add(GROUND, (x, -1, z))
    for x in range(w):
        for z in range(d):
            if heat == 'campfire':
                sc.add('minecraft:block/campfire', (x, 0, z))
            else:
                sc.add('create:block/blaze_burner/block', (x, 0, z))
                sc.add('create:block/blaze_burner/blaze/' + heat, (x, 0, z))
            for y in range(h):
                faces, tex = {}, {}
                for face, (l, r) in SIDES.items():
                    n = NORMAL[face]
                    if inside(x + n[0], y, z + n[2]):
                        continue
                    tex[face] = tile('fluid_tank_connected', inside(x + l[0], y, z + l[2]), inside(x + r[0], y, z + r[2]),
                                     inside(x, y + 1, z), inside(x, y - 1, z))
                    faces[face] = {'texture': '#' + face}
                if y == h - 1:
                    tex['up'] = tile('fluid_tank_top_connected', inside(x - 1, y, z), inside(x + 1, y, z),
                                     inside(x, y, z - 1), inside(x, y, z + 1))
                    faces['up'] = {'texture': '#up'}
                sc.add({'textures': tex, 'elements': [{'from': [0, 0, 0], 'to': [16, 16, 16], 'faces': faces}]}, (x, 1 + y, z))
    sc.add(CE + 'fermenting_vat_valve', (w // 2, h, d // 2))
    if working:
        cx, cy, cz = w // 2 + 0.5 + 0.17, h + 1 + 0.05, d // 2 + 0.5
        for i, (dy, r, a) in enumerate(((0.05, 0.08, 0.5), (0.3, 0.13, 0.32), (0.6, 0.18, 0.16))):
            sc.puff((cx + 0.05 * i, cy + dy, cz), r, a)


SHOTS = [
    ('1_single', dict(w=1, h=1, d=1)),
    ('2_tall', dict(w=1, h=3, d=1, heat='idle')),
    ('3_2x2x2_working', dict(w=2, h=2, d=2, heat='active', working=True)),
    ('4_3x3x4', dict(w=3, h=4, d=3, heat='campfire', working=True)),
]

if __name__ == '__main__':
    for f in os.listdir(OUT):
        if f.endswith('.png') and f[0].isdigit():
            os.remove(os.path.join(OUT, f))
    for name, kw in SHOTS:
        sc = Scene(yaw=-35, pitch=26, scale=10 if kw['h'] < 3 else 8)
        vat(sc, **kw)
        print(sc.render(os.path.join(OUT, name + '.png')))
