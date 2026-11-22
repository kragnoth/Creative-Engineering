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
RAMPS['R'] = ['#160f0a', '#22170f', '#302014', '#3f2a1a', '#4f3520', '#614227', '#74512f', '#896238', '#9f7543']
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
    # Alloy furnace: dark ochre refractory brick (Excalibur's blackstone bricks), steel-strapped: it's built of steel
    alloy = recolour_all(BLOCK + 'polished_blackstone_bricks.png', 'R', lift=1.3)
    ct_set(made, 'alloy_furnace_bricks', alloy, recolour_all(alloy, 'R', lift=0.8), 'S')


# ---------------------------------------------------------------------------------------- fermenting vat
# "Big picture" textures (Kragnoth, 2026-11-22: like a CTM resource pack / Create's tanks, one picture over the whole
# vat). One picture is drawn per face size: walls 1..3 wide x 1..4 tall, lid 1..3 x 1..3. Each block shows its own
# 16x16 slice (client VatModel remaps the UVs). Features sit in the picture, not per block, so they land right at
# any size: copper strap round the outside, copper barrel hoops spaced by the vat's height (off the block seams), a
# brass-framed sight glass centred on the wall, and a copper lid with a steel rim and a bolted hatch under the valve.
# Sheets: cell (W, H) at ((W-1)*48, (H-1)*64) in a 256x256 wall sheet; lid cell (W, D) at ((W-1)*48, (D-1)*48).
RAMPS['Y'] = ['#1f1405', '#3a2709', '#5a3d0e', '#7a5614', '#9a701c', '#b98b27', '#d3a63a', '#e6c25a', '#f3dc8a']

def _tiled(base, w, h, off=8):
    img = Image.new('RGBA', (w, h))
    for y in range(h):
        for x in range(w):
            img.putpixel((x, y), base.getpixel(((x + off) % 16, (y + off) % 16)))
    return img

def _strap(img, band, t=3):
    """Riveted strap round the picture's outer edge (same shading rules as the furnace frames)."""
    b = [hexrgb(h) for h in RAMPS[band]]
    W, H = img.size
    for y in range(H):
        for x in range(W):
            d = min(x, y, W - 1 - x, H - 1 - y)
            if d < t:
                lit = (x == d or y == d)
                img.putpixel((x, y), b[1] if d == 0 else (b[6] if lit and d == 1 else b[4] if d == 1 else b[3]))
            elif d == t:
                img.putpixel((x, y), _darker(img.getpixel((x, y)), 0.72))
    def rivet(x, y):
        img.putpixel((x, y), b[7]); img.putpixel((x + 1, y), b[5]); img.putpixel((x, y + 1), b[5]); img.putpixel((x + 1, y + 1), b[2])
    return rivet

def _frame(img, band, t=3):
    b = [hexrgb(h) for h in RAMPS[band]]
    rivet = _strap(img, band, t)
    W, H = img.size
    for x in range(7, W - 8, 8):
        rivet(x, (t - 2) // 2)
        rivet(x, H - t + (t - 2) // 2)
    for y in range(7, H - 8, 8):
        rivet((t - 2) // 2, y)
        rivet(W - t + (t - 2) // 2, y)
    for cx, cy in ((0, 0), (W - 5, 0), (0, H - 5), (W - 5, H - 5)):       # corner plates
        for y in range(5):
            for x in range(5):
                outer = (cx == 0 and x == 0) or (cx > 0 and x == 4) or (cy == 0 and y == 0) or (cy > 0 and y == 4)
                img.putpixel((cx + x, cy + y), b[1] if outer else b[5])
        rivet(cx + 2 - (cx > 0), cy + 2 - (cy > 0))

def _sight_glass(img):
    """Brass-framed sight glass centred on the wall, height scaled to the wall."""
    y_ = [hexrgb(h) for h in RAMPS['Y']]
    W, H = img.size
    gw = 4 if W < 32 else 6
    gh = max(6, min(H - 14, int(H * 0.55)))
    x0, y0 = W // 2 - gw // 2 - 1, H // 2 - gh // 2 - 1
    glass = [hexrgb('#123b3a'), hexrgb('#1d5652'), hexrgb('#2f7a70'), hexrgb('#7fc9b8')]
    for y in range(y0, y0 + gh + 2):
        for x in range(x0, x0 + gw + 2):
            edge = x in (x0, x0 + gw + 1) or y in (y0, y0 + gh + 1)
            if edge:
                img.putpixel((x, y), y_[6] if (x == x0 or y == y0) else y_[3])
            else:
                gx = x - x0 - 1
                img.putpixel((x, y), glass[3] if gx == 0 and (y - y0) % 5 else glass[2 if gx == 1 else 1 if gx < gw - 1 else 0])
    for y in range(y0 + 3, y0 + gh, 3):                    # graduation ticks on the right of the frame
        img.putpixel((x0 + gw + 2, y), y_[5])
    for y in (y0 - 2, y0 + gh + 3):                        # mounting bolts above and below
        img.putpixel((W // 2 - 1, y), y_[7]); img.putpixel((W // 2, y), y_[4])

def _field(W, H, rnd, base, amp, lf=10.0, grain=0.35):
    """Smooth low-frequency value field (a few random sine waves) plus fine grain: no repeating pattern."""
    import math
    waves = [(rnd.uniform(0.5, 1.6) / lf, rnd.uniform(0.5, 1.6) / lf, rnd.uniform(0, 6.3), rnd.uniform(0.5, 1.0)) for _ in range(4)]
    tot = sum(w[3] for w in waves)
    return [[base + amp * sum(wa * math.sin(fx * x + fy * y + ph) for fx, fy, ph, wa in waves) / tot + rnd.uniform(-grain, grain)
             for x in range(W)] for y in range(H)]

def _paint(img, field, ramp):
    r = [hexrgb(h) for h in RAMPS[ramp]]
    for y, row in enumerate(field):
        for x, v in enumerate(row):
            img.putpixel((x, y), r[max(0, min(8, int(round(v))))])

def _rivet(img, ramp, x, y):
    r = [hexrgb(h) for h in RAMPS[ramp]]
    W, H = img.size
    if 0 <= x < W - 1 and 0 <= y < H - 1:
        img.putpixel((x, y), r[7]); img.putpixel((x + 1, y + 1), r[1])

def vat_wall_picture(base, W, H):
    """Riveted steel tank wall (real-world reference: riveted plate tanks and boilers): plate courses with lapped
    riveted seams and staggered vertical butt joints, angle-iron corners, top curb, bolted base flange, soot toward
    the bottom and a few rust streaks. Drawn per size, never tiled."""
    import random
    rnd = random.Random(1000 + W * 10 + H)
    PW, PH = W * 16, H * 16
    img = Image.new('RGBA', (PW, PH))
    f = _field(PW, PH, rnd, 3.6, 0.9, lf=7.0)
    for y in range(PH):                                     # soot near the fire, faint light near the top
        for x in range(PW):
            f[y][x] += -1.4 * max(0, (y - (PH - 9)) / 9) + 0.5 * max(0, (6 - y) / 6)
    _paint(img, f, 'S')
    st = [hexrgb(h) for h in RAMPS['S']]
    # plate courses: seams off the block seams, so the wall reads as plates, not blocks
    courses = max(1, round(PH / 22))
    seams = [round(PH * k / courses) - 5 for k in range(1, courses)]
    bounds = [3] + seams + [PH - 4]
    for y in seams:                                         # lap seam: upper plate's lit edge, shadow line, rivet row above
        for x in range(2, PW - 2):
            img.putpixel((x, y - 1), st[6]); img.putpixel((x, y), st[1]); img.putpixel((x, y + 1), _darker(img.getpixel((x, y + 1)), 0.8))
        for x in range(4 + (y % 3), PW - 4, 4):
            _rivet(img, 'S', x, y - 3)
    for i in range(len(bounds) - 1):                        # staggered vertical butt joints per course, ~26 px plates
        y0, y1 = bounds[i] + 1, bounds[i + 1] - 1
        x = 13 + (i % 2) * 13
        while x < PW - 8:
            if abs(x - PW // 2) > 6:                         # keep clear of the sight glass
                for y in range(y0, y1):
                    img.putpixel((x, y), st[1]); img.putpixel((x + 1, y), st[5])
                for y in range(y0 + 2, y1 - 1, 4):
                    _rivet(img, 'S', x - 2, y); _rivet(img, 'S', x + 3, y)
            x += 26
    for x in range(PW):                                     # top curb angle and bolted base flange
        img.putpixel((x, 0), st[1]); img.putpixel((x, 1), st[7]); img.putpixel((x, 2), st[5]); img.putpixel((x, 3), st[2])
        for y in range(PH - 3, PH):
            img.putpixel((x, y), st[1] if y == PH - 1 else st[4] if y == PH - 3 else st[2])
    for x in range(5, PW - 4, 6):
        img.putpixel((x, PH - 2), st[7]); img.putpixel((x + 1, PH - 2), st[0])
    for y in range(PH):                                     # angle-iron corners, riveted
        for x, c in ((0, st[1]), (1, st[6]), (2, st[4]), (PW - 1, st[1]), (PW - 2, st[3]), (PW - 3, st[2])):
            img.putpixel((x, y), c)
    for y in range(6, PH - 5, 4):
        _rivet(img, 'S', 3, y); _rivet(img, 'S', PW - 5, y)
    rust = [hexrgb('#5a2e18'), hexrgb('#7a4020'), hexrgb('#93552a')]
    for _ in range(W * H):                                  # a few rust streaks running down from seams
        x = rnd.randrange(5, PW - 6)
        y = rnd.choice(seams) + 1 if seams and rnd.random() < 0.7 else rnd.randrange(5, PH - 8)
        for k in range(rnd.randrange(2, 6)):
            if y + k < PH - 4:
                c = img.getpixel((x, y + k))
                rc = rust[min(2, k // 2)]
                img.putpixel((x, y + k), tuple(int(c[j] * 0.55 + rc[j] * 0.45) for j in range(3)) + (255,))
    _sight_glass(img)
    return img

def vat_lid_picture(base, W, D):
    """Copper lid (reference: copper brewing vessels): hammered copper sheet with soldered seams, faint patina,
    riveted steel rim and a bolted manway under the valve. Drawn per size, never tiled."""
    import math, random
    rnd = random.Random(2000 + W * 10 + D)
    PW, PD = W * 16, D * 16
    img = Image.new('RGBA', (PW, PD))
    _paint(img, _field(PW, PD, rnd, 4.9, 0.9, lf=9.0, grain=0.15), 'U')
    cu = [hexrgb(h) for h in RAMPS['U']]
    for _ in range(PW * PD // 48):                          # hammer dents: small lit / shaded pairs
        x, y = rnd.randrange(2, PW - 3), rnd.randrange(2, PD - 3)
        img.putpixel((x, y), cu[6]); img.putpixel((x + 1, y + 1), cu[3])
    sheets = max(1, round(PD / 20))                         # soldered sheet seams across the lid, off the block seams
    for k in range(1, sheets):
        y = round(PD * k / sheets) - 4
        for x in range(2, PW - 2):
            img.putpixel((x, y), cu[2]); img.putpixel((x, y + 1), cu[7])
    patina = [hexrgb('#3f7f6a'), hexrgb('#5aa58a')]
    for _ in range(W * D):
        x, y = rnd.randrange(2, PW - 2), rnd.choice((2, 3, PD - 3, PD - 4)) if rnd.random() < 0.6 else rnd.randrange(2, PD - 2)
        c = img.getpixel((x, y)); pc = rnd.choice(patina)
        img.putpixel((x, y), tuple(int(c[j] * 0.4 + pc[j] * 0.6) for j in range(3)) + (255,))
    st = [hexrgb(h) for h in RAMPS['S']]
    cx, cy = (W // 2) * 16 + 8, (D // 2) * 16 + 8          # manway under the valve (block W//2, D//2)
    r = min(6 if min(W, D) == 1 else 9, cx - 3, cy - 3, PW - cx - 3, PD - cy - 3)
    for y in range(PD):
        for x in range(PW):
            dist = math.hypot(x + 0.5 - cx, y + 0.5 - cy)
            if r - 2 <= dist < r:
                img.putpixel((x, y), st[6] if (x < cx and y < cy) else st[4] if dist < r - 1 else st[2])
            elif r <= dist < r + 1:
                img.putpixel((x, y), _darker(img.getpixel((x, y)), 0.7))
    for k in range(8):
        a = k * math.pi / 4 + math.pi / 8
        img.putpixel((int(cx + math.cos(a) * (r - 1)), int(cy + math.sin(a) * (r - 1))), st[8])
    for y in range(PD):                                      # riveted steel rim
        for x in range(PW):
            d = min(x, y, PW - 1 - x, PD - 1 - y)
            if d < 2:
                img.putpixel((x, y), st[1] if d == 0 else (st[6] if (x == 1 or y == 1) else st[3]))
            elif d == 2:
                img.putpixel((x, y), _darker(img.getpixel((x, y)), 0.75))
    for x in range(4, PW - 3, 4):
        img.putpixel((x, 1), st[8]); img.putpixel((x, PD - 2), st[7])
    for y in range(4, PD - 3, 4):
        img.putpixel((1, y), st[8]); img.putpixel((PW - 2, y), st[7])
    return img

def vat(made):
    wall = recolour_all(BLOCK + 'iron_block.png', 'S', lift=0.62)
    lid = recolour_all(BLOCK + 'cut_copper.png', 'U', lift=0.95)
    wall_sheet = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
    lid_sheet = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
    for W in (1, 2, 3):
        for H in (1, 2, 3, 4):
            wall_sheet.alpha_composite(vat_wall_picture(wall, W, H), ((W - 1) * 48, (H - 1) * 64))
        for D in (1, 2, 3):
            lid_sheet.alpha_composite(vat_lid_picture(lid, W, D), ((W - 1) * 48, (D - 1) * 48))
    save(wall_sheet, 'block/fermenting_vat_big')
    save(lid_sheet, 'block/fermenting_vat_lid_big')
    made.append(save(wall_sheet.crop((0, 0, 16, 16)), 'block/fermenting_vat'))       # the 1x1 picture: item + placeholder
    made.append(save(lid_sheet.crop((0, 0, 16, 16)), 'block/fermenting_vat_lid'))
    made.append(save(recolour_all(BLOCK + 'iron_block.png', 'S', lift=0.4), 'block/fermenting_vat_bottom'))

# ---------------------------------------------------------------------------------------- metalworking
RAMPS['U'] = ['#1d0d07', '#33160b', '#4d2410', '#6a3416', '#87451c', '#a35a26', '#bb7034', '#d08a48', '#e3a866']
RAMPS['I'] = RAMPS.get('I', ['#18181a', '#26272a', '#36383b', '#484a4e', '#5b5e62', '#707377', '#878a8e', '#a0a3a6', '#c2c4c6'])

def outline(img, ramp):
    k = hexrgb(RAMPS[ramp][0])
    solid = {(x, y) for y in range(16) for x in range(16) if img.getpixel((x, y))[3]}
    for y in range(16):
        for x in range(16):
            if (x, y) not in solid and any(q in solid for q in ((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1))):
                img.putpixel((x, y), k)

def die_item(symbol):
    """A squat steel die block seen at 3/4: lit top face, darker front, the engraved shape on top."""
    st = [hexrgb(h) for h in RAMPS['S']]
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(4, 10):                                  # top face
        for x in range(2, 14):
            img.putpixel((x, y), st[6] if (x + y) % 7 else st[7])
    for y in range(10, 14):                                 # front face
        for x in range(2, 14):
            img.putpixel((x, y), st[4] if y < 12 else st[3])
    for x in range(2, 14):
        img.putpixel((x, 10), st[5])
    groove = {'rod': [(x, 7) for x in range(4, 12)],
              'gear': [(6, 5), (7, 5), (8, 5), (9, 5), (5, 6), (10, 6), (5, 7), (10, 7), (6, 8), (7, 8), (8, 8), (9, 8),
                       (4, 6), (11, 7), (7, 4), (8, 9)],
              'wire': [(4, 7), (5, 6), (6, 7), (7, 8), (8, 7), (9, 6), (10, 7), (11, 8)]}[symbol]
    for p in groove:
        img.putpixel(p, st[1])
    img.putpixel((3, 12), st[7]); img.putpixel((12, 12), st[7])   # two bolts on the front
    outline(img, 'S')
    return img

def gear_item(ramp):
    import math
    r = [hexrgb(h) for h in RAMPS[ramp]]
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 7.5
            d = math.hypot(dx, dy)
            a = math.atan2(dy, dx)
            tooth = math.cos(a * 8) > 0.35
            if d < 1.8:
                continue                                    # axle hole
            if d <= 4.9 or (tooth and d <= 7.3):
                lit = (-dx - dy) / 10.6
                img.putpixel((x, y), r[max(1, min(7, int(4.5 + lit * 3 - (d > 5.2) * 0.5)))])
    outline(img, ramp)
    return img

def rod_item(ramp):
    """A round bar lying diagonally (bottom-left to top-right), 2 px thick: lit upper edge, dark lower edge, end caps."""
    r = [hexrgb(h) for h in RAMPS[ramp]]
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for i in range(2, 13):
        x, y = i, 15 - i
        img.putpixel((x, y - 1), r[7] if i % 4 else r[6])   # upper (lit) row
        img.putpixel((x, y), r[5])
        img.putpixel((x + 1, y), r[3])                      # lower (shadow) row
    img.putpixel((2, 12), r[4]); img.putpixel((13, 2), r[7])
    outline(img, ramp)
    return img

def cavity_mask(product, size=10, sample=Image.BOX):
    """The product's silhouette (alpha), shrunk to size x size and centred: the shape cut into the die."""
    m = product.getchannel('A').point(lambda a: 255 if a > 0 else 0)
    box = m.getbbox()
    if box is None:
        return set()
    m = m.crop(box)
    w, h = m.size
    k = size / max(w, h)
    m = m.resize((max(1, round(w * k)), max(1, round(h * k))), sample).point(lambda a: 255 if a > 110 else 0)
    out = set()
    ox, oy = 8 - m.width // 2, 7 - m.height // 2
    for y in range(m.height):
        for x in range(m.width):
            if m.getpixel((x, y)):
                out.add((ox + x, oy + y))
    return out

def die_mold(product, size=10, sample=Image.BOX):
    """A steel die seen from above at a slight tilt, with the product's shape sunk into its face: press metal in,
    that part comes out. Light from the top-left, so inside the cavity the top/left walls are in shadow and the
    bottom/right walls catch the light."""
    st = [hexrgb(h) for h in RAMPS['S']]
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(1, 13):                                  # top face, bevelled
        for x in range(1, 15):
            edge_tl = x == 1 or y == 1
            edge_br = x == 14 or y == 12
            img.putpixel((x, y), st[7] if edge_tl else st[4] if edge_br else st[6] if (x + y) % 9 else st[5])
    for y in (13, 14):                                      # front face (thickness)
        for x in range(1, 15):
            img.putpixel((x, y), st[3] if y == 13 else st[2])
    img.putpixel((2, 14), st[6]); img.putpixel((13, 14), st[6])  # bolts
    cav = cavity_mask(product, size, sample)
    for (x, y) in cav:
        up, left = (x, y - 1) not in cav, (x - 1, y) not in cav
        down, right = (x, y + 1) not in cav, (x + 1, y) not in cav
        if up or left:
            c = st[0]                                       # shadowed wall
        elif down or right:
            c = st[5]                                       # lit wall
        else:
            c = st[2]                                       # cavity floor
        img.putpixel((x, y), c)
    for (x, y) in cav:                                      # bright lip where the face meets the lit wall
        for q in ((x + 1, y + 1),):
            if q not in cav and 1 <= q[0] <= 14 and 1 <= q[1] <= 12:
                img.putpixel(q, st[8])
    outline(img, 'S')
    return img

def metalworking(made):
    rod = Image.new('RGBA', (16, 16), (0, 0, 0, 0))         # the groove: a clean 2 px diagonal bar
    for i in range(3, 12):
        rod.putpixel((i, 14 - i), (255, 255, 255, 255)); rod.putpixel((i + 1, 14 - i), (255, 255, 255, 255))
    import math
    gear = Image.new('RGBA', (16, 16), (0, 0, 0, 0))        # the gear pocket, drawn at its final size
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 6.5
            d, a = math.hypot(dx, dy), math.atan2(dy, dx)
            if 1.2 < d and (d <= 3.4 or (math.cos(a * 8) > 0.3 and d <= 5.0)):
                gear.putpixel((x, y), (255, 255, 255, 255))
    for d, product, size, sample in (('rod', rod, 10, Image.NEAREST), ('gear', gear, 10, Image.NEAREST)):
        made.append(save(die_mold(product, size, sample), 'item/%s_die' % d))
    made.append(save(rod_item('U'), 'item/copper_rod'))
    for metal, ramp in (('iron', 'I'), ('steel', 'S')):
        made.append(save(rod_item(ramp), 'item/%s_rod' % metal))
        made.append(save(gear_item(ramp), 'item/%s_gear' % metal))
    for metal, ramp in (('copper', 'U'), ('steel', 'S')):
        made.append(save(recolour_all(ITEM + 'string.png', ramp, lift=1.1), 'item/%s_wire' % metal))

# ---------------------------------------------------------------------------------------- treated timber
# Creosote-soaked wood (Kragnoth's reference photo): near-black end grain, deep oxblood-brown faces, warm orange
# where the grain catches the light.
RAMPS['W'] = ['#1c100b', '#2c180f', '#3e2114', '#532a17', '#6a341a', '#82401e', '#9a4e24', '#b05f2d', '#c7773c']

def soot_ends(img, k=0.7):
    """Darken an end-grain texture toward its rim, the way creosote soaks in deepest at the cut ends."""
    out = img.copy()
    for y in range(16):
        for x in range(16):
            c = img.getpixel((x, y))
            if c[3] and (x in (0, 15) or y in (0, 15)):
                out.putpixel((x, y), _darker(c, k))
    return out

def bolt(img, x, y):
    st = [hexrgb(h) for h in RAMPS['S']]
    img.putpixel((x, y), st[7]); img.putpixel((x + 1, y), st[5]); img.putpixel((x, y + 1), st[4]); img.putpixel((x + 1, y + 1), st[2])

def timber(made):
    planks = recolour_all(BLOCK + 'spruce_planks.png', 'W', lift=0.9)
    made.append(save(planks, 'block/treated_planks'))
    side = recolour_all(BLOCK + 'stripped_spruce_log.png', 'W', lift=0.95)
    top = soot_ends(recolour_all(BLOCK + 'stripped_spruce_log_top.png', 'W', lift=0.5), 0.6)
    made.append(save(side, 'block/treated_log'))
    made.append(save(top, 'block/treated_log_top'))
    # beam: the log side with steel bolts through it, and a sooty end
    beam = side.copy()
    for (x, y) in ((5, 7), (9, 7)):   # one through-bolt pair per block, inside the faces the beam models show
        bolt(beam, x, y)
    made.append(save(beam, 'block/treated_beam'))
    # the same timber turned on its side, for joist faces (faces aren't square, so rotating in the model stretches)
    made.append(save(beam.rotate(90), 'block/treated_beam_horizontal'))
    made.append(save(soot_ends(top, 0.75), 'block/treated_beam_end'))

# Engines (module 1) use Create's own textures only (Kragnoth, 2026-11-22): nothing to generate here.

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
    metalworking(made)
    timber(made)
    vat(made)
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
