"""Creative Engineering tier 1 art, Excalibur-based (Kragnoth's direction, 2026-11-18): Excalibur shapes and value
structure as the base, with Immersive Engineering touches as loan styling (steel collars on handles, butt caps,
reddish treated-wood handles). Used with Maffhew's permission (Excalibur is CC BY-NC-ND 3.0 otherwise); credit
Maffhew in the mod's credits. The Excalibur pack itself is NEVER committed: point EXCALIBUR_DIR at an unpacked copy.

Run from the repo root:  EXCALIBUR_DIR=~/ref/excalibur python3 tools/stylekit/make_textures_excalibur.py
Method: each Excalibur iron pixel is classified (metal / wood / outline) and recoloured to the tone of a Creative
Engineering ramp with the nearest brightness, so Excalibur's painterly shading survives. Details are then added
on top. Writes PNGs into the mod's assets and a preview sheet into tools/stylekit/out/."""
import colorsys, os, sys
from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from make_textures import RAMPS, TEMPER, TEX, PREVIEW, hexrgb, write_gpl  # noqa: E402

EXC = os.path.expanduser(os.environ.get('EXCALIBUR_DIR', '~/ref/excalibur'))
ITEM = EXC + '/assets/minecraft/textures/item/'
BLOCK = EXC + '/assets/minecraft/textures/block/'
ARMOR = EXC + '/overlay_1.21.1/assets/minecraft/textures/models/armor/'

# Treated-wood handle ramp: IE-style reddish brown, darkened toward Excalibur's values
RAMPS['T'] = ['#1a0f0a', '#2a1810', '#3b2216', '#4d2c1b', '#5e3722', '#70432a', '#834f31', '#965c38', '#a96a42']

def lum(c):
    r, g, b = c[:3]
    return (0.299 * r + 0.587 * g + 0.114 * b) / 255

def nearest(ramp, l, lift=1.0):
    target = min(1.0, l * lift)
    tones = [hexrgb(h) for h in ramp]
    return min(tones, key=lambda t: abs(lum(t) - target))

def classify(c):
    r, g, b, a = c
    if a < 128:
        return None
    h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
    if s >= 0.22 and 0.02 <= h <= 0.14 and v > 0.12:
        return 'wood'
    return 'metal'

def recolour(src, metal='M', wood='T', lift=1.08):
    im = src if isinstance(src, Image.Image) else Image.open(src).convert('RGBA')
    out = Image.new('RGBA', im.size, (0, 0, 0, 0))
    kinds = {}
    for y in range(im.height):
        for x in range(im.width):
            c = im.getpixel((x, y))
            k = classify(c)
            if k is None:
                continue
            kinds[(x, y)] = k
            ramp = RAMPS[metal if k == 'metal' else wood]
            out.putpixel((x, y), nearest(ramp, lum(c), lift))
    return out, kinds

def ie_tool_touches(img, kinds):
    """IE loan styling: a bright steel collar where the handle meets the head, and a metal butt cap."""
    steel = [hexrgb(h) for h in RAMPS['M']]
    collar = [p for p, k in kinds.items() if k == 'wood' and any(kinds.get((p[0] + dx, p[1] + dy)) == 'metal'
                                                                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))]
    for p in collar[:2]:
        img.putpixel(p, steel[6])
    woods = [p for p, k in kinds.items() if k == 'wood']
    if woods:
        cap = max(woods, key=lambda p: (p[1] - p[0]))      # furthest down-left end of the handle
        img.putpixel(cap, steel[4])

def flecks(img, kinds, n_temper=2, n_blister=2, seed=0):
    """Blister-steel character: a couple of raised blisters and temper tints on mid-tone metal."""
    metal = sorted(p for p, k in kinds.items() if k == 'metal' and 0.30 < lum(img.getpixel(p)) < 0.62)
    if not metal:
        return
    step = max(1, len(metal) // (n_temper + n_blister + 1))
    picks = metal[step // 2 + seed::step]
    steel = [hexrgb(h) for h in RAMPS['M']]
    for i, p in enumerate(picks[:n_temper + n_blister]):
        if i < n_blister:
            img.putpixel(p, steel[7])
        else:
            img.putpixel(p, TEMPER[(i + seed) % 3])

def save(img, rel):
    path = '%s/%s.png' % (TEX, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)
    return path

def carbon_packed():
    """Excalibur's ingot shape in plain iron tones, wrapped in a crust sampled from Excalibur's coal."""
    core, kinds = recolour(ITEM + 'iron_ingot.png', metal='I', lift=1.0)
    coal = Image.open(ITEM + 'coal.png').convert('RGBA')
    coal_px = [coal.getpixel((x, y)) for y in range(coal.height) for x in range(coal.width) if coal.getpixel((x, y))[3] > 128]
    coal_px.sort(key=lum)
    out = Image.new('RGBA', core.size, (0, 0, 0, 0))
    filled = set(kinds)
    def edge(p):
        return any((p[0] + dx, p[1] + dy) not in filled for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
    ring = set()
    for (x, y) in filled:                                   # crust: 1 px outside the ingot...
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            q = (x + dx, y + dy)
            if 0 <= q[0] < 16 and 0 <= q[1] < 16 and q not in filled:
                ring.add(q)
    ring |= {p for p in filled if edge(p) and (p[0] + 2 * p[1]) % 3 == 0}   # ...and lumps over its edge
    for p in filled:
        out.putpixel(p, core.getpixel(p))
    for q in ring:
        out.putpixel(q, coal_px[(q[0] * 7 + q[1] * 13) % len(coal_px)])
    return out

def sheet_plate():
    """Our own sheet, drawn on the rules Create's sheets follow (no Create pixels used): a square plate lying flat,
    seen from above and turned ~45 degrees, so a wide squashed diamond; a 1 px darker thickness rim along its lower
    edges; light from the top-left with a highlight along the upper-left edge and a soft dent in the middle.
    Then recoloured at Excalibur's values like the ingot, plus two IE-style rivets."""
    face = [
        '................', '................', '................', '................',
        '.......###......',
        '....########....',
        '..############..',
        '.##############.',
        '..#############.',
        '...###########..',
        '....#########...',
        '......#####.....',
        '................', '................', '................', '................']
    face = [list(r) for r in face]
    mask = [['.'] * 16 for _ in range(16)]
    for y in range(16):
        for x in range(16):
            if face[y][x] == '#':
                mask[y][x] = 'M'
    for y in range(15, -1, -1):                          # thickness rim: one row under every lower edge
        for x in range(16):
            if face[y][x] == '#' and (y + 1 > 15 or face[y + 1][x] != '#'):
                if y + 1 <= 15:
                    mask[y + 1][x] = 'm'
    mask = [''.join(r) for r in mask]
    from make_textures import shade
    img = shade(mask, gradient=True)
    steel = [hexrgb(h) for h in RAMPS['M']]
    for x, y in [(5, 5), (6, 5), (4, 6), (3, 7)]:          # highlight along the upper-left edge
        img.putpixel((x, y), steel[7])
    for x, y in [(8, 8), (9, 8), (8, 9)]:                  # soft dent in the middle
        img.putpixel((x, y), steel[3])
    img, kinds = recolour(img, lift=0.85)
    img.putpixel((7, 5), steel[8]); img.putpixel((8, 6), steel[2])   # rivet top
    img.putpixel((11, 9), steel[8]); img.putpixel((12, 10), steel[2])  # rivet right
    img.putpixel((5, 9), TEMPER[0])
    img.putpixel((10, 7), steel[7])                      # a blister
    return img

TOOLS = ['sword', 'pickaxe', 'axe', 'shovel', 'hoe']
ARMOUR = ['helmet', 'chestplate', 'leggings', 'boots']

if __name__ == '__main__':
    if not os.path.isdir(ITEM):
        sys.exit('Excalibur not found at %s (set EXCALIBUR_DIR)' % EXC)
    write_gpl('tools/stylekit/palettes/creative_engineering.gpl', 'Creative Engineering', RAMPS)
    made = []
    for t in TOOLS:
        img, kinds = recolour(ITEM + 'iron_%s.png' % t)
        ie_tool_touches(img, kinds)
        flecks(img, kinds, n_temper=1, n_blister=1, seed=len(t))
        made.append(save(img, 'item/blister_steel_' + t))
    for a in ARMOUR:
        img, kinds = recolour(ITEM + 'iron_%s.png' % a)
        flecks(img, kinds, n_temper=1, n_blister=2, seed=len(a))
        made.append(save(img, 'item/blister_steel_' + a))
    for src, dst, nt, nb in [('iron_ingot', 'blister_steel_ingot', 2, 2), ('iron_nugget', 'blister_steel_nugget', 1, 1)]:
        img, kinds = recolour(ITEM + src + '.png')
        flecks(img, kinds, n_temper=nt, n_blister=nb)
        made.append(save(img, 'item/' + dst))
    made.append(save(carbon_packed(), 'item/carbon_packed_iron'))
    img, kinds = recolour(BLOCK + 'iron_block.png')
    flecks(img, kinds, n_temper=3, n_blister=4)
    made.append(save(img, 'block/blister_steel_block'))
    # Sheet: Excalibur has no Create sheet; keep the hand-drawn one from make_textures.py (same blister-steel ramp).
    made.append(save(sheet_plate(), 'item/blister_steel_sheet'))
    layers = []
    for n in (1, 2):
        img, kinds = recolour(ARMOR + 'iron_layer_%d.png' % n)
        flecks(img, kinds, n_temper=4, n_blister=6, seed=n)
        layers.append(img)
        save(img, 'models/armor/blister_steel_layer_%d' % n)

    os.makedirs(PREVIEW, exist_ok=True)
    cols, scale = 8, 8
    rows = (len(made) + cols - 1) // cols
    W = cols * (16 * scale + 8) + 8
    out = Image.new('RGBA', (max(W, 2 * 64 * 4 + 24), rows * (16 * scale + 8) + 8 + 32 * 4 + 16), (58, 48, 40, 255))
    for i, p in enumerate(made):
        im = Image.open(p).convert('RGBA').resize((16 * scale, 16 * scale), Image.NEAREST)
        out.alpha_composite(im, (8 + (i % cols) * (16 * scale + 8), 8 + (i // cols) * (16 * scale + 8)))
    y0 = 8 + rows * (16 * scale + 8)
    for i, l in enumerate(layers):
        out.alpha_composite(l.resize((64 * 4, 32 * 4), Image.NEAREST), (8 + i * (64 * 4 + 8), y0))
    out.save('%s/tier1_preview.png' % PREVIEW)
    print('%d textures + 2 armour layers (Excalibur-based); preview at %s/tier1_preview.png' % (len(made), PREVIEW))
