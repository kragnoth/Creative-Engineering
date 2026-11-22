"""Look-approval renders for the diesel engine (docs/engines-spec.md): five builds from smallest to a full plant.
Create's tank / shaft textures appear only in these local previews (out/ is git-ignored)."""
import os
import sys

sys.path.insert(0, os.path.dirname(__file__))
from render_scene import Scene, cube  # noqa: E402

OUT = os.path.join(os.path.dirname(__file__), 'out', 'engine')
os.makedirs(OUT, exist_ok=True)
CE = 'creativeengineering:block/'
TANK = cube('create:block/fluid_tank', 'create:block/fluid_tank_top')
GROUND = cube('minecraft:block/smooth_stone', frm=(0, 15, 0), to=(16, 16, 16))


def ground(sc, x0, x1, z0, z1):
    for x in range(x0, x1):
        for z in range(z0, z1):
            sc.add(GROUND, (x, -1, z))


def engine(sc, length, banks, starter=True, flywheel=False, tanks=(), shaft=1, running=False):
    for x in range(length):
        sc.add(CE + 'diesel_crankcase', (x, 0, 0))
        for side in banks:
            sc.add(CE + ('diesel_cylinder' if side > 0 else 'diesel_cylinder_mirrored'), (x, 1, side))
            if running:   # exhaust particles straight off the head (top of the head is 13.5 px out along the 45 deg axis)
                hx, hy = x + 0.5, 1 + (8 + 9.5) / 16
                hz = side + (8 + 9.5) / 16 if side > 0 else side + (8 - 9.5) / 16
                for i, (dy, r, a) in enumerate(((0.05, 0.1, 0.55), (0.3, 0.15, 0.4), (0.6, 0.2, 0.28), (0.95, 0.26, 0.15), (1.35, 0.32, 0.06))):
                    sc.puff((hx + 0.06 * i, hy + dy, hz + side * 0.1 * i), r, a)
    if starter:   # Create's own Hand Crank on the crankcase's front shaft input (any rotation would do)
        sc.add('create:block/hand_crank/block', (-1, 0, 0), zrot=90)
        sc.add('create:block/hand_crank/handle', (-1, 0, 0), zrot=90)
    for t in tanks:
        sc.add(TANK, t)
    end = length
    if flywheel:
        sc.add(CE + 'diesel_flywheel', (end, 0, 0))
        end += 1
    for i in range(shaft):
        sc.add('create:block/shaft', (end + i, 0, 0), zrot=90)


STAGES = [
    ('1_single', 'Single cylinder: one crankcase, one cylinder, tank, Create hand crank, output shaft',
     dict(length=1, banks=(1,), tanks=[(0, 0, -1)]), -38),
    ('2_vtwin', 'V-twin: both banks on one crankcase',
     dict(length=1, banks=(1, -1), tanks=[(0, 0, -1)]), -38),
    ('3_inline3', 'Inline three: three crankcases, one bank, two tanks',
     dict(length=3, banks=(1,), tanks=[(0, 0, -1), (1, 0, -1)]), -32),
    ('4_v6_flywheel', 'V6 with flywheel: six cylinders, flywheel on the output end',
     dict(length=3, banks=(1, -1), flywheel=True, tanks=[(0, 0, -1)]), 32),
    ('5_v8_running', 'V8 running: eight cylinders, flywheel, shaft on to the factory, smoke off the heads',
     dict(length=4, banks=(1, -1), flywheel=True, tanks=[(0, 0, -1), (1, 0, -1)], shaft=3, running=True), 30),
]

if __name__ == '__main__':
    for name, caption, kw, yaw in STAGES:
        sc = Scene(yaw=yaw, pitch=26, scale=9)
        n = kw['length']
        ground(sc, -2, n + 1 + kw.get('shaft', 1), -2, 3)
        engine(sc, **kw)
        print(sc.render(os.path.join(OUT, name + '.png')))
