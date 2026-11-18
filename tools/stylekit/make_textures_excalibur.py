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
# Coke-oven brick: dusky fired brick, darker than vanilla brick (IE coke-brick flavour, Excalibur values)
RAMPS['B'] = ['#1c0f0b', '#2d1912', '#40231a', '#542e21', '#673a29', '#7b4632', '#8e553d', '#a26649', '#b57957']
# Coke: porous silver-grey carbon
RAMPS['K'] = ['#151617', '#212325', '#2e3134', '#3c3f43', '#4b4f53', '#5b5f64', '#6c7176', '#7e8389', '#92979d']
# Creosote oil: dark amber-brown
RAMPS['O'] = ['#0d0805', '#170e08', '#22140b', '#2e1b0e', '#3b2311', '#4a2c14', '#5a3617', '#6b411b', '#7d4d20']
EMBER = [hexrgb('#ff9a3c'), hexrgb('#e8661c'), hexrgb('#b8400f')]
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

def sheet_plate(ramp='M', fleck=True):
    """Our own sheet, drawn on the rules Create's sheets follow (no Create pixels used): a square plate lying flat,
    seen from above and turned ~45 degrees (a wide squashed diamond); a dark 1 px thickness rim under its lower
    edges; light from the top-left with a bright highlight along the upper-left edge. Thin-metal read comes from
    a LIGHT outline on the upper edges and the dark only underneath. One IE-style rivet, kept small."""
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
    F = lambda x, y: 0 <= x < 16 and 0 <= y < 16 and face[y][x] == '#'
    st = [hexrgb(h) for h in RAMPS[ramp]]
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            if not F(x, y):
                continue
            top_edge = not F(x, y - 1) or not F(x - 1, y)
            d = (x - 1) + (y - 4) * 2                        # distance from the top-left corner
            if top_edge and (x + 2 * y) < 22:
                t = 7                                        # highlight along the upper-left edge
            elif top_edge:
                t = 6
            elif d < 10:
                t = 6
            elif d < 18:
                t = 5
            else:
                t = 4
            img.putpixel((x, y), st[t])
    for y in range(16):                                      # thickness rim under the lower edges
        for x in range(16):
            if F(x, y) and not F(x, y + 1) and y + 1 < 16:
                img.putpixel((x, y + 1), st[2])
    solid = {(x, y) for y in range(16) for x in range(16) if img.getpixel((x, y))[3]}   # snapshot first
    for y in range(16):                                      # outline: soft above the plate, dark below it
        for x in range(16):
            if (x, y) in solid:
                continue
            n = [(x, y + 1), (x, y - 1), (x + 1, y), (x - 1, y)]
            if not any(q in solid for q in n):
                continue
            img.putpixel((x, y), st[3] if (x, y + 1) in solid or ((x + 1, y) in solid and y < 8) or ((x - 1, y) in solid and y < 8) else st[0])
    img.putpixel((11, 8), st[7]); img.putpixel((12, 9), st[2])   # one rivet
    if fleck:
        img.putpixel((6, 8), TEMPER[0])                      # a single temper fleck
    return img

def recolour_all(src, ramp, lift=1.0):
    """Every opaque pixel to the nearest-brightness tone of one ramp (used for coke, creosote, bricks)."""
    im = src if isinstance(src, Image.Image) else Image.open(src).convert('RGBA')
    out = Image.new('RGBA', im.size, (0, 0, 0, 0))
    for y in range(im.height):
        for x in range(im.width):
            c = im.getpixel((x, y))
            if c[3] >= 128:
                out.putpixel((x, y), nearest(RAMPS[ramp], lum(c), lift))
    return out

def coke_oven_bricks(lit):
    """Excalibur's bricks in coke-brick tones; the lit variant glows in the mortar joints (ember)."""
    src = Image.open(BLOCK + 'bricks.png').convert('RGBA')
    img = recolour_all(src, 'B', lift=0.95)
    if lit:
        # mortar = the darkest quarter of the source: those joints glow
        ls = sorted(lum(src.getpixel((x, y))) for y in range(16) for x in range(16))
        cut = ls[len(ls) // 4]
        for y in range(16):
            for x in range(16):
                if lum(src.getpixel((x, y))) <= cut:
                    img.putpixel((x, y), EMBER[(x + 2 * y) % 3])
    return img

def coke_item(src, lift=1.35):
    """Excalibur shapes recoloured to coke, with a few dark pores and bright glints for the porous look."""
    img = recolour_all(src, 'K', lift=lift)
    pts = [(x, y) for y in range(16) for x in range(16) if img.getpixel((x, y))[3]]
    k = [hexrgb(h) for h in RAMPS['K']]
    for i, p in enumerate(pts[3::7]):
        img.putpixel(p, k[1] if i % 2 == 0 else k[7])
    return img

def creosote_strip(src):
    return recolour_all(src, 'O', lift=0.42)

def creosote_bucket():
    """Excalibur's lava bucket with the lava (saturated orange pixels) turned to creosote; the bucket stays."""
    im = Image.open(ITEM + 'lava_bucket.png').convert('RGBA')
    out = im.copy()
    for y in range(16):
        for x in range(16):
            c = im.getpixel((x, y))
            if c[3] < 128:
                continue
            h, sat, v = colorsys.rgb_to_hsv(c[0] / 255, c[1] / 255, c[2] / 255)
            if sat > 0.45 and (h < 0.16 or h > 0.95):
                out.putpixel((x, y), nearest(RAMPS['O'], lum(c), 0.7))
    return out

def tier2(made):
    made.append(save(coke_oven_bricks(False), 'block/coke_oven_bricks'))
    made.append(save(coke_item(ITEM + 'coal.png', lift=2.3), 'item/coal_coke'))
    made.append(save(coke_item(ITEM + 'gunpowder.png'), 'item/coke_dust'))
    made.append(save(coke_item(BLOCK + 'coal_block.png', lift=1.9), 'block/coke_block'))
    import shutil
    for name in ('still', 'flow'):
        strip = creosote_strip(BLOCK + 'lava_%s.png' % name)
        save(strip, 'block/creosote_%s' % name)
        shutil.copy(BLOCK + 'lava_%s.png.mcmeta' % name, '%s/block/creosote_%s.png.mcmeta' % (TEX, name))
        made.append(None)

# Tier 3 ramps: cast iron (dark graphite grey, a touch warm), steel (cool blue-grey, brighter top),
# blast-furnace brick (grey fireclay), slag (glassy dark grey-green)
RAMPS['C'] = ['#141414', '#1f1e1d', '#2b2a28', '#383634', '#464441', '#55524f', '#65625e', '#77736e', '#8a8580']
RAMPS['S'] = ['#12161b', '#1c2229', '#273039', '#333e49', '#404d5a', '#4f5e6c', '#62727f', '#7a8a96', '#a3b1ba']
RAMPS['F'] = ['#17171a', '#232327', '#303035', '#3e3e44', '#4d4c52', '#5d5c61', '#6e6c70', '#817e80', '#959192']
RAMPS['G'] = ['#0e1210', '#171d1a', '#212924', '#2c3530', '#38423c', '#455049', '#535f57', '#636f66', '#768178']

def tier3(made):
    made.append(save(recolour_all(BLOCK + 'deepslate_bricks.png', 'F', lift=1.25), 'block/blast_furnace_bricks'))
    for metal, name in (('C', 'cast_iron'), ('S', 'steel')):
        img, _ = recolour(ITEM + 'iron_ingot.png', metal=metal, lift=1.0)
        made.append(save(img, 'item/%s_ingot' % name))
        img, _ = recolour(ITEM + 'iron_nugget.png', metal=metal, lift=1.0)
        made.append(save(img, 'item/%s_nugget' % name))
        img, _ = recolour(BLOCK + 'iron_block.png', metal=metal, lift=1.0)
        made.append(save(img, 'block/%s_block' % name))
        made.append(save(sheet_plate(metal, fleck=False), 'item/%s_sheet' % name))
    made.append(save(recolour_all(ITEM + 'flint.png', 'G', lift=1.2), 'item/slag'))

# ---------------------------------------------------------------------------------------- connected textures
# Create's RECTANGLE layout (AllCTTypes): a 64x64 sheet of 4x4 tiles. Column = left/right neighbours
# (0 none, 1 right only, 2 both, 3 left only); row = up/down neighbours (0 down only, 1 both, 2 up only, 3 none).
# Tile (3,3) is the lone block, saved as the plain texture the model and item use.
# Look: brick infill, framed by a riveted metal strap only along the OUTER edge of the formed furnace, so a 1x1
# and a 3x6 read as the same object (same idea as Create's tanks and vaults). Rivets fall every 16 px, so the
# rhythm is the same at any size. Ports (wrench) are an 8x8 hatch in the middle of the tile, clear of the strap.
def _darker(c, f):
    return (int(c[0] * f), int(c[1] * f), int(c[2] * f), 255)

def ct_tile(base, band, L, R, T, B, port=None):
    b = [hexrgb(h) for h in RAMPS[band]]
    img = base.copy()
    edge = lambda x, y: (L and x < 3) or (R and x > 12) or (T and y < 3) or (B and y > 12)
    for y in range(16):                                   # 1 px soot shadow on the bricks inside the strap
        for x in range(16):
            if edge(x, y):
                continue
            if (L and x == 3) or (T and y == 3) or (R and x == 12) or (B and y == 12):
                img.putpixel((x, y), _darker(img.getpixel((x, y)), 0.72))
    for y in range(16):
        for x in range(16):
            if not edge(x, y):
                continue
            # distance into the strap from the outside, and whether this strap faces the light (top/left)
            d = min([v for v, on in ((x, L), (15 - x, R), (y, T), (15 - y, B)) if on])
            lit = (L and x == d) or (T and y == d)
            img.putpixel((x, y), b[1] if d == 0 else (b[6] if lit and d == 1 else b[4] if d == 1 else b[3]))
    def rivet(x, y):
        img.putpixel((x, y), b[7]); img.putpixel((x + 1, y), b[5])
        img.putpixel((x, y + 1), b[5]); img.putpixel((x + 1, y + 1), b[2])
    if L: rivet(1, 7)
    if R: rivet(13, 7)
    if T: rivet(7, 1)
    if B: rivet(7, 13)
    for cx, cy, on in ((0, 0, L and T), (11, 0, R and T), (0, 11, L and B), (11, 11, R and B)):
        if not on:                                        # corner plate where two straps meet
            continue
        for y in range(5):
            for x in range(5):
                outer = (cx == 0 and x == 0) or (cx == 11 and x == 4) or (cy == 0 and y == 0) or (cy == 11 and y == 4)
                img.putpixel((cx + x, cy + y), b[1] if outer else b[5])
        rivet(cx + 2 - (cx == 11), cy + 2 - (cy == 11))
    if port:
        hatch(img, b, port)
    return img

def hatch(img, b, port):
    """8x8 hatch at (4..11): input = barred charging grate, output = tap hole glowing inside with a spout lip."""
    void = b[0]
    for y in range(3, 13):
        for x in range(3, 13):
            if x in (3, 12) or y in (3, 12):
                img.putpixel((x, y), b[1])                 # dark seat around the hatch
            elif x in (4, 11) or y in (4, 11):
                img.putpixel((x, y), b[6] if (x == 4 or y == 4) else b[3])
            else:
                img.putpixel((x, y), void)
    if port == 'input':
        for x in (6, 9):
            for y in range(5, 11):
                img.putpixel((x, y), b[5] if y > 5 else b[7])
        img.putpixel((7, 4), b[2]); img.putpixel((8, 4), b[2])
    else:
        for x in range(5, 11):
            img.putpixel((x, 10), EMBER[(x) % 3])
            if 6 <= x <= 9:
                img.putpixel((x, 9), EMBER[2])
        img.putpixel((7, 8), EMBER[2])
        for x in range(2, 14):                             # spout lip under the hole
            img.putpixel((x, 13), b[6] if x < 13 else b[3])
            img.putpixel((x, 14), b[2])

def ct_sheet(base, band, port=None):
    sheet = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    for ty in range(4):
        for tx in range(4):
            L, R = tx in (0, 1), tx in (0, 3)
            T, B = ty in (0, 3), ty in (2, 3)
            sheet.alpha_composite(ct_tile(base, band, L, R, T, B, port), (tx * 16, ty * 16))
    return sheet

def ct_set(made, name, base, top_base, band):
    for suffix, b, port in (('', base, None), ('_input', base, 'input'), ('_output', base, 'output'), ('_top', top_base, None)):
        sheet = ct_sheet(b, band, port)
        save(sheet, 'block/%s%s_connected' % (name, suffix))
        made.append(save(sheet.crop((48, 48, 64, 64)), 'block/%s%s' % (name, suffix)))

def connected(made):
    coke = coke_oven_bricks(False)
    ct_set(made, 'coke_oven_bricks', coke, recolour_all(coke, 'B', lift=0.75), 'M')
    blast = recolour_all(BLOCK + 'deepslate_bricks.png', 'F', lift=1.25)
    ct_set(made, 'blast_furnace_bricks', blast, recolour_all(blast, 'F', lift=0.8), 'S')

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
    tier2(made)
    tier3(made)
    connected(made)
    made = [m for m in made if m]
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
