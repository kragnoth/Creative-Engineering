"""Look preview for the Pumpjack (docs/petroleum-spec.md): Crank Gearbox, Samson Post and Wellhead as separate
blocks, with the moving partials posed by the same maths as PumpjackRig.pose. Two rigs: short (rear arm 1, reach 1,
crank up) and long reach (rear arm 1, reach 2, crank down). Usage: render_pumpjack.py [three_quarter|side|front]"""
import math
import os
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(__file__))
from render_scene import Scene  # noqa: E402

OUT = os.path.join(os.path.dirname(__file__), 'out', 'pumpjack')
os.makedirs(OUT, exist_ok=True)
CE = 'creativeengineering:block/'
R, PY, PZ, LIP_Y = 4, 41, 8, -6          # PumpjackRig constants


def pose(theta, a, b):
    pin_y, pin_z = 8 + R * math.cos(theta), 8 - 16 * a + R * math.sin(theta)
    phi = math.asin((pin_y - 8) / (16 * a))
    c, s = math.cos(phi), math.sin(phi)
    rear_y, rear_z = PY - c + 16 * a * s, PZ - s - 16 * a * c
    dy, dz = rear_y - pin_y, rear_z - pin_z
    return phi, pin_y, pin_z, math.hypot(dy, dz), math.atan2(dz, dy), PY + LIP_Y * c - 16 * b * s


def rot_x(p, a):
    c, s = math.cos(a), math.sin(a)
    return np.array([p[0], p[1] * c - p[2] * s, p[1] * s + p[2] * c])


def on_beam(phi, dy, dz):
    """Partial modelled about its local origin, placed dz along the beam and dy below its pivot line, turned by phi."""
    return lambda p: rot_x(np.array([p[0], p[1] + dy, p[2] + dz]) - np.array([0, 0, 0]), phi) + np.array([0, PY, PZ])


def rig(sc, x, theta, a, b):
    post = (x, 0, a)
    phi, pin_y, pin_z, plen, psi, rod_top = pose(theta, a, b)
    sc.add(CE + 'pumpjack', (x, 0, 0))
    sc.add('create:block/shaft', (x, 0, 0), xform=lambda p: np.array([16 - p[1], p[0], p[2]]))
    sc.add(CE + 'pumpjack_crank', (x, 0, 0), xform=lambda p: rot_x(p - np.array([0, 8, 8]), theta) + np.array([0, 8, 8]))
    sc.add(CE + 'samson_post_lower', post)
    sc.add(CE + 'samson_post_upper', (x, 1, a))
    sc.add(CE + 'wellhead', (x, 0, a + b))
    sc.add(CE + 'pumpjack_saddle', post, xform=on_beam(phi, 0, 0))
    for i in range(a + b):
        sc.add(CE + 'pumpjack_beam', post, xform=on_beam(phi, 0, -16 * a + 16 * i))
    sc.add(CE + 'pumpjack_horsehead', post, xform=on_beam(phi, LIP_Y, 16 * b))
    sc.add(CE + 'pumpjack_equalizer', post, xform=on_beam(phi, 0, -16 * a))
    for px in (1.5, 14.5):
        sc.add(CE + 'pumpjack_pitman', post, xform=lambda p, px=px: rot_x(np.array([p[0], p[1] * plen / 32, p[2]]), psi)
               + np.array([px, pin_y, pin_z]))
    sc.add(CE + 'pumpjack_rod', post, xform=lambda p: np.array([p[0], p[1] + rod_top - 16, p[2] + 16 * b]))


if __name__ == '__main__':
    view = sys.argv[1] if len(sys.argv) > 1 else 'three_quarter'
    yaw, pitch = {'three_quarter': (-140, 22), 'side': (-90, 6), 'front': (-160, 18)}[view]
    sc = Scene(yaw=yaw, pitch=pitch, scale=26)
    rig(sc, 0, 0.0, 1, 1)            # short rig, crank up: horsehead down
    rig(sc, 3, math.pi, 1, 2)        # long reach, crank down: horsehead up
    print(sc.render(os.path.join(OUT, 'pumpjack_%s.png' % view)))
