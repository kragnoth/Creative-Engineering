"""Look-approval renders for the Fermenting Vat (docs/fermentation-spec.md). Create textures only in local previews.
The renderer has no connected textures, so this picks each face's tile from the vat's RECTANGLE sheets the way
Create's CTModel would (preview tiles cached under out/vat/ct)."""
import copy
import json
import os
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(__file__))
import render_scene  # noqa: E402
from render_scene import Scene, cube  # noqa: E402

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, 'out', 'vat')
CT = os.path.join(OUT, 'ct')
os.makedirs(os.path.join(CT, 'textures'), exist_ok=True)
render_scene.ROOTS['preview'] = CT
TEX = os.path.join(HERE, '..', '..', 'mod', 'src', 'main', 'resources', 'assets', 'creativeengineering', 'textures', 'block')
MODELS = os.path.join(HERE, '..', '..', 'mod', 'src', 'main', 'resources', 'assets', 'creativeengineering', 'models', 'block')
CE = 'creativeengineering:block/'
GROUND = cube('minecraft:block/smooth_stone', frm=(0, 15, 0), to=(16, 16, 16))


def tile(sheet, px, py):
    """16x16 slice at (px, py) of a big-picture sheet, as the client VatModel picks it."""
    name = '%s_%d_%d' % (sheet, px, py)
    path = os.path.join(CT, 'textures', name + '.png')
    if not os.path.exists(path):
        Image.open(os.path.join(TEX, sheet + '.png')).crop((px, py, px + 16, py + 16)).save(path)
    return 'preview:' + name


# face -> (left, right, up, down) neighbour offsets, as seen from outside the face
SIDES = {'north': ((1, 0, 0), (-1, 0, 0)), 'south': ((-1, 0, 0), (1, 0, 0)),
         'east': ((0, 0, 1), (0, 0, -1)), 'west': ((0, 0, -1), (0, 0, 1))}


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
                body = 'fermenting_vat_body' + ('' if y else '_bottom' if h > 1 else '_single')
                with open(os.path.join(MODELS, body + '.json')) as f:
                    m = json.load(f)
                e = m['elements'][0]
                for face, (l, r) in SIDES.items():
                    n = {'north': (0, 0, -1), 'south': (0, 0, 1), 'east': (1, 0, 0), 'west': (-1, 0, 0)}[face]
                    if inside(x + n[0], y, z + n[2]):
                        e['faces'].pop(face)          # skipRendering: walls between vat blocks don't draw
                        continue
                    key = 'f_' + face
                    col, width = {'north': (w - 1 - x, w), 'south': (x, w), 'east': (d - 1 - z, d), 'west': (z, d)}[face]
                    m['textures'][key] = tile('fermenting_vat_big', (width - 1) * 48 + col * 16, (h - 1) * 64 + (h - 1 - y) * 16)
                    e['faces'][face]['texture'] = '#' + key
                if y < h - 1:
                    e['faces'].pop('up')
                else:
                    m['textures']['f_up'] = tile('fermenting_vat_lid_big', (w - 1) * 48 + x * 16, (d - 1) * 48 + z * 16)
                    e['faces']['up']['texture'] = '#f_up'
                if y > 0:
                    e['faces'].pop('down')
                m['textures'] = {k: (v if ':' in v else v) for k, v in m['textures'].items()}
                sc.add(m, (x, 1 + y, z))
            for (a, b), r in ((('north', 'west'), 0), (('north', 'east'), 90), (('south', 'east'), 180), (('south', 'west'), 270)):
                edge = {'north': z == 0, 'south': z == d - 1, 'west': x == 0, 'east': x == w - 1}
                if edge[a] and edge[b]:
                    sc.add(CE + 'fermenting_vat_foot', (x, 1, z), yrot=r)
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
    ('5_3x1x2', dict(w=3, h=1, d=2, heat='campfire')),
]

if __name__ == '__main__':
    for name, kw in SHOTS:
        sc = Scene(yaw=-35, pitch=26, scale=10 if kw['h'] < 3 else 8)
        vat(sc, **kw)
        print(sc.render(os.path.join(OUT, name + '.png')))
