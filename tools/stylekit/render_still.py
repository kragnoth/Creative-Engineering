"""Look-approval renders for the Distilling Tank (docs/distilling-spec.md): copper-shifted Create tank (same maths as
RecolorSpriteSource, settings from the atlas JSON's second source) with connected tiles, plus the greebles."""
import json
import os
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(__file__))
import render_vat as rv  # noqa: E402
from render_scene import Scene  # noqa: E402

rv.ATLAS = json.load(open(os.path.join(rv.REPO, 'mod/src/main/resources/assets/minecraft/atlases/blocks.json')))['sources'][1]
rv.SHEETS = {n: rv.recolor(Image.open(rv.CREATE + n + '.png')) for n in ('fluid_tank_connected', 'fluid_tank_top_connected')}
OUT = rv.OUT.replace('/vat', '/still')
os.makedirs(OUT, exist_ok=True)
rv.CT = os.path.join(OUT, 'ct')
os.makedirs(os.path.join(rv.CT, 'textures'), exist_ok=True)
rv.render_scene.ROOTS['preview'] = rv.CT
CE = 'creativeengineering:block/'
ROT = {'north': 0, 'east': 90, 'south': 180, 'west': 270}


def still(sc, w, h, d, heat='active', working=True):
    rv.vat.__wrapped__ if False else None
    inside = lambda x, y, z: 0 <= x < w and 0 <= y < h and 0 <= z < d
    for x in range(-1, w + 1):
        for z in range(-1, d + 1):
            sc.add(rv.GROUND, (x, -1, z))
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
                taps = {'north': z == 0 and x == w // 2, 'south': z == d - 1 and x == w // 2,
                        'west': x == 0 and z == d // 2, 'east': x == w - 1 and z == d // 2}
                for face, on in taps.items():
                    if on:
                        sc.add(CE + ('distilling_tank_gauge' if y == 0 else 'distilling_tank_tap'), (x, 1 + y, z), yrot=ROT[face])
    sc.add(CE + 'distilling_tank_valve', (w // 2, h, d // 2))
    if working:
        cx, cy, cz = w // 2 + 0.5 + 0.34, h + 1 + 0.28, d // 2 + 0.5
        for i, (dy, r, a) in enumerate(((0.05, 0.08, 0.45), (0.3, 0.13, 0.3), (0.6, 0.18, 0.15))):
            sc.puff((cx + 0.05 * i, cy + dy, cz), r, a)


if __name__ == '__main__':
    for name, kw, scale in (('1_column_1x4', dict(w=1, h=4, d=1), 9), ('2_column_2x5', dict(w=2, h=5, d=2), 7),
                            ('3_column_3x6', dict(w=3, h=6, d=3), 6)):
        sc = Scene(yaw=-35, pitch=22, scale=scale)
        still(sc, **kw)
        print(sc.render(os.path.join(OUT, name + '.png')))
