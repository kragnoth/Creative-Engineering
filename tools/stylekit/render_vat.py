"""Look-approval renders for the Fermenting Vat (docs/fermentation-spec.md). Create textures only in local previews."""
import os
import sys

sys.path.insert(0, os.path.dirname(__file__))
from render_scene import Scene, cube  # noqa: E402

OUT = os.path.join(os.path.dirname(__file__), 'out', 'vat')
os.makedirs(OUT, exist_ok=True)
CE = 'creativeengineering:block/'
GROUND = cube('minecraft:block/smooth_stone', frm=(0, 15, 0), to=(16, 16, 16))


def vat(sc, w, h, d, heat='campfire', working=False):
    for x in range(-1, w + 1):
        for z in range(-1, d + 1):
            sc.add(GROUND, (x, -1, z))
    for x in range(w):
        for z in range(d):
            if heat == 'campfire':
                sc.add('minecraft:block/campfire', (x, 0, z))
            else:
                sc.add('create:block/blaze_burner/block', (x, 0, z))
                sc.add('create:block/blaze_burner/blaze/' + heat, (x, 0, z))
            for y in range(h):
                sc.add(CE + ('fermenting_vat_body_bottom' if y == 0 else 'fermenting_vat_body'), (x, 1 + y, z))
                if y == h - 1:
                    sc.add(CE + 'fermenting_vat_lid', (x, 1 + y, z))
    sc.add(CE + 'fermenting_vat_valve', (w // 2, h, d // 2))
    if working:
        cx, cy, cz = w // 2 + 0.5 + 0.17, h + 1 + 0.15, d // 2 + 0.5
        for i, (dy, r, a) in enumerate(((0.05, 0.08, 0.5), (0.3, 0.13, 0.32), (0.6, 0.18, 0.16))):
            sc.puff((cx + 0.05 * i, cy + dy, cz), r, a)


SHOTS = [
    ('1_single', dict(w=1, h=1, d=1)),
    ('2_tall', dict(w=1, h=3, d=1, heat='idle')),
    ('3_2x2x2_working', dict(w=2, h=2, d=2, heat='active', working=True)),
    ('4_3x3x4', dict(w=3, h=4, d=3, heat='campfire', working=True)),
]

if __name__ == '__main__':
    for name, kw in SHOTS:
        sc = Scene(yaw=-35, pitch=26, scale=10 if kw['h'] < 3 else 8)
        vat(sc, **kw)
        print(sc.render(os.path.join(OUT, name + '.png')))
