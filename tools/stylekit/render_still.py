"""Look-approval renders for the Distilling Tank (docs/distilling-spec.md, 0.12.1 direction): the vat's steel Create
tank with outside plumbing (Create copper pipes), brass collars, a gauge, the gooseneck valve, and catwalk rings
where the player placed Create scaffolding. The pipe routing here mirrors content/distilling/Plumbing.java in spirit
(Python's RNG differs, so a given column won't match the game's exactly)."""
import json
import os
import random
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(__file__))
import render_vat as rv  # noqa: E402
from render_scene import Scene  # noqa: E402

rv.ATLAS = json.load(open(os.path.join(rv.REPO, 'mod/src/main/resources/assets/minecraft/atlases/blocks.json')))['sources'][0]
rv.SHEETS = {n: rv.recolor(Image.open(rv.CREATE + n + '.png')) for n in ('fluid_tank_connected', 'fluid_tank_top_connected')}
OUT = rv.OUT.replace('/vat', '/still')
os.makedirs(OUT, exist_ok=True)
rv.CT = os.path.join(OUT, 'ct')
os.makedirs(os.path.join(rv.CT, 'textures'), exist_ok=True)
rv.render_scene.ROOTS['preview'] = rv.CT
CE = 'creativeengineering:block/'
FACES = ['north', 'east', 'south', 'west']
ROT = {'north': 0, 'east': 90, 'south': 180, 'west': 270}


def plumbing(seed, w, h, d):
    rnd = random.Random(seed)
    lanes, band = {}, {}
    for f in FACES:
        width = w if f in ('north', 'south') else d
        n = rnd.randrange(3)
        lanes[f] = [(rnd.randrange(width), rnd.choice(('left', 'right'))) for _ in range(n)]
        band[f] = 1 + rnd.randrange(h - 2) if h >= 3 and rnd.random() < 0.6 else -1
    if not any(lanes.values()):
        lanes['north'] = [(w // 2, 'left')]
    return lanes, band


def still(sc, w, h, d, catwalks=(), seed=7, heat='active', working=True, metal='andesite'):
    inside = lambda x, y, z: 0 <= x < w and 0 <= y < h and 0 <= z < d
    for x in range(-2, w + 2):
        for z in range(-2, d + 2):
            sc.add(rv.GROUND, (x, -1, z))
    lanes, band = plumbing(seed, w, h, d)
    for x in range(w):
        for z in range(d):
            sc.add('create:block/blaze_burner/block', (x, 0, z))
            sc.add('create:block/blaze_burner/blaze/' + heat, (x, 0, z))
            for y in range(h):
                faces, tex = {}, {}
                for face, (l, r) in rv.SIDES.items():
                    n = rv.NORMAL[face]
                    if inside(x + n[0], y, z + n[2]):
                        continue
                    tex[face] = rv.tile('fluid_tank_connected', inside(x + l[0], y, z + l[2]), inside(x + r[0], y, z + r[2]),
                                        inside(x, y + 1, z), inside(x, y - 1, z))
                    faces[face] = {'texture': '#' + face}
                if y == h - 1:
                    tex['up'] = rv.tile('fluid_tank_top_connected', inside(x - 1, y, z), inside(x + 1, y, z),
                                        inside(x, y, z - 1), inside(x, y, z + 1))
                    faces['up'] = {'texture': '#up'}
                sc.add({'textures': tex, 'elements': [{'from': [0, 0, 0], 'to': [16, 16, 16], 'faces': faces}]}, (x, 1 + y, z))
                on = {'north': z == 0, 'east': x == w - 1, 'south': z == d - 1, 'west': x == 0}
                along = {'north': x, 'east': z, 'south': w - 1 - x, 'west': d - 1 - z}
                for f in FACES:
                    if not on[f]:
                        continue
                    for idx, side in lanes[f]:
                        if idx == along[f]:
                            suffix = '_boiler' if y == 0 else '_top' if y == h - 1 else ''
                            sc.add(CE + 'distilling_tank_pipe_%s%s' % (side, suffix), (x, 1 + y, z), yrot=ROT[f])
                    if band[f] == y:
                        sc.add(CE + 'distilling_tank_pipe_band', (x, 1 + y, z), yrot=ROT[f])
                    if y % 3 == 2 and y != h - 1:
                        sc.add(CE + 'distilling_tank_collar', (x, 1 + y, z), yrot=ROT[f])
                    if y == h - 1 and f == 'north' and along[f] == w // 2:
                        sc.add(CE + 'distilling_tank_gauge', (x, 1 + y, z), yrot=ROT[f])
    sc.add(CE + 'distilling_tank_valve', (w // 2, h, d // 2))
    ring = [(x, z) for x in range(-1, w + 1) for z in range(-1, d + 1) if not (0 <= x < w and 0 <= z < d)]
    for y in catwalks:
        for x, z in ring:
            sc.add(CE + '%s_catwalk_deck' % metal, (x, 1 + y, z))
            for f, (dx, dz) in (('north', (0, -1)), ('east', (1, 0)), ('south', (0, 1)), ('west', (-1, 0))):
                nx, nz = x + dx, z + dz
                if (nx, nz) not in ring and not (0 <= nx < w and 0 <= nz < d):
                    sc.add(CE + '%s_catwalk_rail' % metal, (x, 1 + y, z), yrot=ROT[f])
    if working:
        cx, cy, cz = w // 2 + 0.5 + 0.34, h + 1 + 0.28, d // 2 + 0.5
        for i, (dy, r, a) in enumerate(((0.05, 0.08, 0.45), (0.3, 0.13, 0.3), (0.6, 0.18, 0.15))):
            sc.puff((cx + 0.05 * i, cy + dy, cz), r, a)


if __name__ == '__main__':
    for f in os.listdir(OUT):
        if f.endswith('.png'):
            os.remove(os.path.join(OUT, f))
    for name, kw, scale in (('1_tower_2x2x7_catwalks', dict(w=2, h=7, d=2, catwalks=(2, 5), seed=3), 6),
                            ('2_tower_1x1x5', dict(w=1, h=5, d=1, catwalks=(3,), seed=11, metal='brass'), 8),
                            ('3_tower_3x3x8', dict(w=3, h=8, d=3, catwalks=(3, 6), seed=5, metal='copper'), 5)):
        sc = Scene(yaw=-35, pitch=20, scale=scale)
        still(sc, **kw)
        print(sc.render(os.path.join(OUT, name + '.png')))
