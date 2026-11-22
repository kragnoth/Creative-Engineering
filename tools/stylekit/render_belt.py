"""Look-approval render for the Drive Belt (flywheel -> Drive Pulley). The belt path mirrors
client/engine/DriveBeltRenderer.loop(); it is drawn here as small boxes along the path."""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(__file__))
from render_scene import Scene, cube  # noqa: E402

OUT = os.path.join(os.path.dirname(__file__), 'out', 'engine')
CE = 'creativeengineering:block/'
GROUND = cube('minecraft:block/smooth_stone', frm=(0, 15, 0), to=(16, 16, 16))
R1, R2, LIFT = 22.4, 5.6, 0.25


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


def belt(sc, gap):
    pts = loop(R1 + LIFT, R2 + LIFT, gap * 16)
    els = []
    for i in range(len(pts)):
        (w0, y0), (w1, y1) = pts[i], pts[(i + 1) % len(pts)]
        n = max(1, int(math.hypot(w1 - w0, y1 - y0)))
        for k in range(n):
            w, y = w0 + (w1 - w0) * k / n, y0 + (y1 - y0) * k / n
            els.append({'from': [5.75, 8 + y - 0.6, 8 + w - 0.6], 'to': [10.25, 8 + y + 0.6, 8 + w + 0.6],
                        'faces': {d: {'texture': '#b'} for d in ('north', 'south', 'east', 'west', 'up', 'down')}})
    sc.add({'textures': {'b': 'create:block/belt'}, 'elements': els}, (0, 0, 0))


if __name__ == '__main__':
    for gap in (4, 2):
        sc = Scene(yaw=-62, pitch=18, scale=11)
        for x in range(-2, 3):
            for z in range(-3, 4):
                sc.add(GROUND, (x, -3, z))
        sc.add(CE + 'diesel_flywheel', (0, 0, 0))
        sc.add(CE + 'drive_pulley', (0, gap, 0))
        sc.add('create:block/shaft', (1, gap, 0), zrot=90)
        belt(sc, gap)
        print(sc.render(os.path.join(OUT, 'drive_belt_gap%d.png' % gap)))
