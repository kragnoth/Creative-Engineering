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


def engine(sc, length, banks, exhaust=True, starter=True, flywheel=False, tanks=(), shaft=1, running=False):
    for x in range(length):
        sc.add(CE + 'diesel_crankcase', (x, 0, 0))
        for side in banks:
            sc.add(CE + ('diesel_cylinder' if side > 0 else 'diesel_cylinder_mirrored'), (x, 1, side))
            if exhaust:
                sc.add(CE + 'diesel_exhaust', (x, 2, 2 * side), yrot=0 if side > 0 else 180)
    if exhaust:
        for side in banks:
            # +Z bank's stack at the back end, -Z bank's at the front (the stack model turns up from a -X manifold)
            sx = length if side > 0 else -1
            sc.add(CE + 'diesel_exhaust_stack', (sx, 2, 2 * side), yrot=0 if side > 0 else 180)
            if running:
                cx = sx + (5.5 / 16 if side > 0 else 1 - 5.5 / 16)
                cz = 2 * side + (5.5 / 16 if side > 0 else 1 - 5.5 / 16)
                for i, (dy, r, a) in enumerate(((0.1, 0.14, 0.55), (0.45, 0.2, 0.42), (0.85, 0.27, 0.3), (1.3, 0.34, 0.18), (1.8, 0.42, 0.08))):
                    sc.puff((cx + 0.12 * i, 4 + dy, cz), r, a)
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
    ('2_vtwin', 'V-twin: both banks on one crankcase, exhaust stacks',
     dict(length=1, banks=(1, -1), tanks=[(0, 0, -1)]), -38),
    ('3_inline3', 'Inline three: three crankcases, one bank, two tanks',
     dict(length=3, banks=(1,), tanks=[(0, 0, -1), (1, 0, -1)]), -32),
    ('4_v6_flywheel', 'V6 with flywheel: six cylinders, flywheel on the output end',
     dict(length=3, banks=(1, -1), flywheel=True, tanks=[(0, 0, -1)]), 32),
    ('5_v8_running', 'V8 running: eight cylinders, flywheel, shaft on to the factory, exhaust smoke',
     dict(length=4, banks=(1, -1), flywheel=True, tanks=[(0, 0, -1), (1, 0, -1)], shaft=3, running=True), 30),
]

if __name__ == '__main__':
    for name, caption, kw, yaw in STAGES:
        sc = Scene(yaw=yaw, pitch=26, scale=9)
        n = kw['length']
        ground(sc, -2, n + 1 + kw.get('shaft', 1), -3, 4)
        engine(sc, **kw)
        print(sc.render(os.path.join(OUT, name + '.png')))
