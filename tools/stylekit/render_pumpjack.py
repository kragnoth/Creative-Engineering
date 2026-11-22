"""Look preview for the Pumpjack and Wellhead (docs/petroleum-spec.md): static models plus the animated
partials posed with the same linkage maths as PumpjackBlockEntity.pose. Two machines: crank up and crank down."""
import math
import os
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(__file__))
from render_scene import Scene  # noqa: E402

OUT = os.path.join(os.path.dirname(__file__), 'out', 'pumpjack')
os.makedirs(OUT, exist_ok=True)
CE = 'creativeengineering:block/'


R, PY, PZ, REAR, LY, LZ = 2.5, 28, 15, 7, -6, 9     # PumpjackBlockEntity constants


def pose(crank):
    pin_y, pin_z = 8 + R * math.cos(crank), 8 + R * math.sin(crank)
    phi = math.asin((pin_y - 8) / REAR)
    c, s = math.cos(phi), math.sin(phi)
    rear_y, rear_z = PY + (-1 * c + REAR * s), PZ + (-1 * s - REAR * c)
    dy, dz = rear_y - pin_y, rear_z - pin_z
    lip_y = PY + (LY * c - LZ * s)
    return phi, pin_y, pin_z, math.hypot(dy, dz), math.atan2(dz, dy), lip_y


def rx(p, a, oy, oz, ty=None, tz=None):
    """Rotate p (px) about the X axis through (oy, oz) by a radians (y' = y cos - z sin), then move that origin to (ty, tz)."""
    y, z = p[1] - oy, p[2] - oz
    c, s = math.cos(a), math.sin(a)
    return np.array([p[0], y * c - z * s + (oy if ty is None else ty), y * s + z * c + (oz if tz is None else tz)])


def machine(sc, x, crank):
    phi, pin_y, pin_z, plen, psi, rod_top = pose(crank)
    sc.add(CE + 'pumpjack', (x, 0, 0))
    sc.add(CE + 'wellhead', (x, 0, 1))
    # Create's shaft through the gearbox, along X
    sc.add('create:block/shaft', (x, 0, 0), xform=lambda p: np.array([16 - p[1], p[0], p[2]]))
    sc.add(CE + 'pumpjack_beam', (x, 0, 0), xform=lambda p: rx(p, phi, PY, PZ))
    sc.add(CE + 'pumpjack_crank', (x, 0, 0), xform=lambda p: rx(p, crank, 8, 8))
    for px in (2, 14):
        sc.add(CE + 'pumpjack_pitman', (x, 0, 0), xform=lambda p, px=px: rx(np.array([p[0], p[1] * plen / 18, p[2]]), psi, 0, 0, pin_y, pin_z)
               + np.array([px, 0, 0]))
    sc.add(CE + 'pumpjack_rod', (x, 0, 0), xform=lambda p: np.array([p[0], p[1] + rod_top - 16, p[2] + 16]))


if __name__ == '__main__':
    view = sys.argv[1] if len(sys.argv) > 1 else 'three_quarter'
    yaw, pitch = {'three_quarter': (-140, 22), 'side': (-90, 8), 'front': (-160, 18)}[view]
    sc = Scene(yaw=yaw, pitch=pitch, scale=30)
    machine(sc, 0, 0.0)            # crank up: horsehead down
    machine(sc, 2, math.pi)        # crank down: horsehead up
    print(sc.render(os.path.join(OUT, 'pumpjack_%s.png' % view)))
