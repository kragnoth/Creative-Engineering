"""Look preview for the Pumpjack (docs/petroleum-spec.md): Crank Gearbox, a built girder column with the Walking Beam
Bearing, and a Wellhead, with the moving partials posed by the same maths as PumpjackRig.pose. Two rigs: the smallest
(column 2, rear arm 1, reach 1, crank up) and a field-size one (column 5, rear arm 2, reach 4, crank down).
Usage: render_pumpjack.py [three_quarter|side|front]"""
import math
import os
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(__file__))
from render_scene import Scene  # noqa: E402

OUT = os.path.join(os.path.dirname(__file__), 'out', 'pumpjack')
os.makedirs(OUT, exist_ok=True)
CE = 'creativeengineering:block/'
PY, PZ = 10, 8                       # PumpjackRig pivot in the bearing block


def crank_r(a):
    return 4 if a <= 1 else 6


def pose(theta, a, b, h):
    r, ay, az = crank_r(a), 8 - 16 * h, 8 - 16 * a
    pin_y, pin_z = ay + r * math.cos(theta), az + r * math.sin(theta)
    phi = math.asin((pin_y - ay) / (16 * a))
    c, s = math.cos(phi), math.sin(phi)
    hs = 1 + (b - 1) * 0.25
    return dict(phi=phi, pin=(pin_y, pin_z), rear=(PY - 2 * c + 16 * a * s, PZ - 2 * s - 16 * a * c),
                lip=PY - 6 * hs * c - 16 * b * s, rod_bottom=-16 * h + 12, hs=hs)


def rot_x(p, a):
    c, s = math.cos(a), math.sin(a)
    return np.array([p[0], p[1] * c - p[2] * s, p[1] * s + p[2] * c])


def on_beam(phi, dy, dz, sc=1.0):
    def f(p):
        q = np.array([(p[0] - 8) * sc + 8, p[1] * sc + dy, p[2] * sc + dz])
        return rot_x(q, phi) + np.array([0, PY, PZ])
    return f


def bar(sc_, model, at, x, p0, p1):
    dy, dz = p1[0] - p0[0], p1[1] - p0[1]
    length = math.hypot(dy, dz)
    n = max(1, min(8, math.ceil(length / 16)))
    psi, step = math.atan2(dz, dy), length / n
    for i in range(n):
        sc_.add(model, at, xform=lambda p, i=i: rot_x(np.array([p[0], p[1] * step / 16 + i * step, p[2]]), psi)
                + np.array([x, p0[0], p0[1]]))


def rig(sc, x, theta, a, b, h):
    q = pose(theta, a, b, h)
    bearing = (x, h, a)
    sc.add(CE + 'pumpjack', (x, 0, 0))
    sc.add('create:block/shaft', (x, 0, 0), xform=lambda p: np.array([16 - p[1], p[0], p[2]]))
    k = crank_r(a) / 4
    sc.add(CE + 'pumpjack_crank', (x, 0, 0), xform=lambda p: rot_x(np.array([p[0], (p[1] - 8) * k, (p[2] - 8) * k]), theta) + np.array([0, 8, 8]))
    for y in range(h):
        sc.add('create:block/metal_girder/block_pole', (x, y, a))
    sc.add(CE + 'walking_beam_bearing', bearing)
    sc.add(CE + 'wellhead', (x, 0, a + b))
    phi = q['phi']
    sc.add(CE + 'pumpjack_saddle', bearing, xform=on_beam(phi, 0, 0))
    k = min(2, 1 + 0.2 * (a + b - 2))
    for i in range(a + b):
        sc.add(CE + 'pumpjack_beam', bearing, xform=lambda p, i=i: rot_x(np.array([(p[0] - 8) * k + 8, (p[1] - 1) * k + 1, p[2] - 16 * a + 16 * i]), phi)
               + np.array([0, PY, PZ]))
    sc.add(CE + 'pumpjack_horsehead', bearing, xform=on_beam(phi, -6 * q['hs'], 16 * b, q['hs']))
    sc.add(CE + 'pumpjack_equalizer', bearing, xform=on_beam(phi, 0, -16 * a))
    for px in (1.5, 14.5):
        bar(sc, CE + 'pumpjack_pitman', bearing, px, q['pin'], q['rear'])
    z, top = 8 + 16 * b, q['lip'] - 1
    bar(sc, CE + 'pumpjack_rod', bearing, 8, (q['rod_bottom'], z), (top, z))
    sc.add(CE + 'pumpjack_bridle', bearing, xform=lambda p: np.array([p[0], p[1] + top, p[2] + z]))


if __name__ == '__main__':
    view = sys.argv[1] if len(sys.argv) > 1 else 'three_quarter'
    yaw, pitch = {'three_quarter': (-140, 22), 'side': (-90, 4), 'front': (-160, 18)}[view]
    sc = Scene(yaw=yaw, pitch=pitch, scale=16)
    rig(sc, 0, 0.0, 1, 1, 2)            # smallest rig, crank up: horsehead down
    rig(sc, 3, math.pi, 2, 4, 5)        # field size, crank down: horsehead up
    print(sc.render(os.path.join(OUT, 'pumpjack_%s.png' % view)))
