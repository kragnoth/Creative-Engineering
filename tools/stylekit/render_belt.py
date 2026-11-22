"""Look-approval render for the Drive Chain (flywheel -> Drive Sprocket = Create cogwheel). The chain path mirrors
client/engine/DriveBeltRenderer.loop(); in game it is Create's chain-conveyor chain, here small boxes in the chain texture."""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(__file__))
from render_scene import Scene, cube  # noqa: E402

OUT = os.path.join(os.path.dirname(__file__), 'out', 'engine')
CE = 'creativeengineering:block/'
GROUND = cube('minecraft:block/smooth_stone', frm=(0, 15, 0), to=(16, 16, 16))
R1, R2, LIFT = 23, 8, 0


def loop(r1, r2, dy):
    s = 1 if dy > 0 else -1
    ny = (r1 - r2) / dy
    nw = math.sqrt(max(0, 1 - ny * ny))
    ap, am = math.atan2(ny, nw), math.atan2(ny, -nw)
    pts = []

    def arc(cy, r, frm, to, via, steps):
        n = lambda a: a % (2 * math.pi)
        span, vs = n(to - frm), n(via - frm)
        if vs > span:
            span -= 2 * math.pi
        for i in range(steps + 1):
            a = frm + span * i / steps
            pts.append((r * math.cos(a), cy + r * math.sin(a)))
    arc(0, r1, ap, am, math.atan2(-s, 0), 20)
    arc(dy, r2, am, ap, math.atan2(s, 0), 8)
    return pts


def belt(sc, gap, ow=0, r1=None, r2=None):
    oy = gap * 16
    ow *= 16
    d = math.hypot(ow, oy)
    uw, uy = ow / d, oy / d
    pts = [(uy * w + uw * y, -uw * w + uy * y) for w, y in loop(r1 or R1 + LIFT, r2 or R2 + LIFT, d)]
    els = []
    for i in range(len(pts)):
        (w0, y0), (w1, y1) = pts[i], pts[(i + 1) % len(pts)]
        n = max(1, int(math.hypot(w1 - w0, y1 - y0)))
        for k in range(n):
            w, y = w0 + (w1 - w0) * k / n, y0 + (y1 - y0) * k / n
            els.append({'from': [7, 8 + y - 0.75, 8 + w - 0.75], 'to': [9, 8 + y + 0.75, 8 + w + 0.75],
                        'faces': {d: {'texture': '#b'} for d in ('north', 'south', 'east', 'west', 'up', 'down')}})
    sc.add({'textures': {'b': 'create:block/dark_metal_block'}, 'elements': els}, (0, 0, 0))


def scene(name, wheels, chains):
    sc = Scene(yaw=-62, pitch=18, scale=10)
    for x in range(-2, 3):
        for z in range(-4, 6):
            sc.add(GROUND, (x, -3, z))
    for kind, (y, z) in wheels:
        if kind == 'fly':
            sc.add(CE + 'diesel_flywheel', (0, y, z))
        else:
            sc.add('create:block/cogwheel', (0, y, z), zrot=90)
    for (y0, z0), (y1, z1), r1, r2 in chains:
        sub = Scene.__new__(Scene)
        els_before = len(sc.__dict__.get('_items', [])) if hasattr(sc, '_items') else 0
        belt_at(sc, (y0, z0), (y1, z1), r1, r2)
    print(sc.render(os.path.join(OUT, name + '.png')))


def belt_at(sc, a, b, r1, r2):
    (y0, z0), (y1, z1) = a, b
    oy, ow = (y1 - y0) * 16, (z1 - z0) * 16
    d = math.hypot(ow, oy)
    uw, uy = ow / d, oy / d
    pts = [(uy * w + uw * y, -uw * w + uy * y) for w, y in loop(r1, r2, d)]
    els = []
    for i in range(len(pts)):
        (w0, yy0), (w1, yy1) = pts[i], pts[(i + 1) % len(pts)]
        n = max(1, int(math.hypot(w1 - w0, yy1 - yy0)))
        for k in range(n):
            w, y = w0 + (w1 - w0) * k / n, yy0 + (yy1 - yy0) * k / n
            els.append({'from': [7, 8 + y - 0.75, 8 + w - 0.75], 'to': [9, 8 + y + 0.75, 8 + w + 0.75],
                        'faces': {f: {'texture': '#b'} for f in ('north', 'south', 'east', 'west', 'up', 'down')}})
    sc.add({'textures': {'b': 'create:block/dark_metal_block'}, 'elements': els}, (0, y0, z0))


if __name__ == '__main__':
    scene('drive_chain_sideways_and_on', [('fly', (0, 0)), ('cog', (0, 3)), ('cog', (3, 5))],
          [((0, 0), (0, 3), 23, 8), ((0, 3), (3, 5), 8, 8)])
    for gap in (4, 2):
        sc = Scene(yaw=-62, pitch=18, scale=11)
        for x in range(-2, 3):
            for z in range(-3, 4):
                sc.add(GROUND, (x, -3, z))
        sc.add(CE + 'diesel_flywheel', (0, 0, 0))
        sc.add('create:block/cogwheel', (0, gap, 0), zrot=90)
        sc.add('create:block/shaft', (1, gap, 0), zrot=90)
        belt(sc, gap)
        print(sc.render(os.path.join(OUT, 'drive_chain_gap%d.png' % gap)))
