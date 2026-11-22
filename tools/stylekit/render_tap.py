"""Look preview for the Tap (Create fluid valve parts in steel) on a Distilling Tank block. Resolves our child models'
parents from Create's assets and swaps our steel texture ids for previews made with RecolorSpriteSource's maths."""
import json
import os
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(__file__))
import render_vat as rv  # noqa: E402
from render_scene import Scene  # noqa: E402

CREATE = os.path.expanduser('~/ref/create/src/main/resources/assets/create/')
MOD = os.path.join(rv.REPO, 'mod/src/main/resources/assets/creativeengineering/')
OUT = os.path.join(rv.OUT.replace('/vat', '/tap'))
os.makedirs(os.path.join(OUT, 'ct', 'textures'), exist_ok=True)
rv.CT = os.path.join(OUT, 'ct')
rv.render_scene.ROOTS['preview'] = rv.CT
STEEL = {'steel_valve': 'fluid_valve', 'steel_valve_open': 'valve_open', 'steel_valve_closed': 'valve_closed',
         'steel_pipes': 'pipes', 'steel_pipes_connected': 'pipes_connected', 'vat_tank': 'fluid_tank'}


def steel(name):
    path = os.path.join(rv.CT, 'textures', name + '.png')
    if not os.path.exists(path):
        im = rv.recolor(Image.open(CREATE + 'textures/block/' + STEEL[name] + '.png')).convert('RGBA')
        for sp in json.load(open(MOD.split('/assets/')[0] + '/assets/minecraft/atlases/blocks.json'))['sources'][0]['sprites']:
            if sp['target'].endswith('/' + name):
                for f in sp.get('fills', []):     # same flat fills as RecolorSpriteSource
                    w, h = im.size
                    c = im.getpixel((min(w - 1, int(f['from'][0] * w)), min(h - 1, int(f['from'][1] * h))))
                    for y in range(round(f['rect'][1] * h), round(f['rect'][3] * h)):
                        for x in range(round(f['rect'][0] * w), round(f['rect'][2] * w)):
                            im.putpixel((x, y), c)
        im.save(path)
    return 'preview:' + name


def load(rl):
    ns, p = rl.split(':')
    base = MOD if ns == 'creativeengineering' else CREATE
    m = json.load(open(base + 'models/' + p + '.json'))
    if 'elements' not in m and 'parent' in m and not m['parent'].startswith('block/'):
        parent = load(m['parent'])
        tex = dict(parent.get('textures', {}))
        tex.update(m.get('textures', {}))
        m = {'textures': tex, 'elements': parent['elements']}
    t = {}
    for k, v in m.get('textures', {}).items():
        if v.startswith('creativeengineering:block/') and v.split('/')[-1] in STEEL:
            v = steel(v.split('/')[-1])
        t[k] = v
    m['textures'] = t
    return m


def tap(sc, pos, open_):
    st = 'open' if open_ else 'closed'
    import numpy as np
    from render_scene import _rot
    xr = lambda p: _rot(np.array(p, float), {'origin': [8, 8, 8], 'axis': 'x', 'angle': -90})   # blockstate x 90
    sc.add(load('creativeengineering:block/tap/body_' + st), pos, yrot=90, xform=xr)   # facing east
    sc.add(load('creativeengineering:block/tap/pointer'), pos, yrot=90 if open_ else 180)
    sc.add(load('creativeengineering:block/steel_pipe/rim/west'), pos)
    sc.add(load('creativeengineering:block/steel_pipe/connection/east'), pos)


if __name__ == '__main__':
    sc = Scene(yaw=55, pitch=30, scale=26)
    side = steel('vat_tank')
    for z in (0, 2):
        sc.add({'textures': {'s': side}, 'elements': [{'from': [0, 0, 0], 'to': [16, 16, 16], 'faces': {
            d: {'texture': '#s'} for d in ('north', 'south', 'east', 'west', 'up', 'down')}}]}, (0, 0, z))
    tap(sc, (1, 0, 0), True)
    tap(sc, (1, 0, 2), False)
    print(sc.render(os.path.join(OUT, 'tap_open_and_closed.png')))
