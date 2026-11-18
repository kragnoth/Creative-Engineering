"""Creative Engineering style kit: draws original textures in Create's style (docs/create-art-style.md).
Run from the repo root: python3 tools/stylekit/make_textures.py   (needs Pillow)

How it works: each texture is an ASCII mask (one char per pixel, material letters). Every material has a
dark->light ramp. Shading follows Create's conventions: a dark outline where a material meets empty space,
light from the top-left (light rim on top/left edges, dark rim on bottom/right), a soft diagonal gradient
inside, and small surface details (blisters, rivets, grain). Nothing here is copied from Create or Mojang;
only the palette approach is shared. Writes PNGs into the mod's assets and a preview sheet into tools/stylekit/out/."""
import os
from PIL import Image

MODID = 'creativeengineering'
TEX = 'mod/src/main/resources/assets/%s/textures' % MODID
PREVIEW = 'tools/stylekit/out'

def hexrgb(h): return tuple(int(h[i:i + 2], 16) for i in (1, 3, 5)) + (255,)

# --------------------------------------------------------------------------------------------- palettes
RAMPS = {
    # Blister steel: warm grey with a faint temper sheen. 0 = outline ... 8 = highlight
    'M': ['#2b2724', '#3d3833', '#514b45', '#686159', '#7f776d', '#9a9185', '#b6ad9f', '#d3cabb', '#ece4d4'],
    # Handle wood: matched by eye to Create's andesite-casing wood tones (not copied pixels)
    'W': ['#2e2013', '#3d2b18', '#553a1f', '#614b2e', '#70522e', '#7a5a34', '#886539', '#977443', '#a58350'],
    # Carbon packing (coal/charcoal crust)
    'C': ['#141312', '#1f1d1b', '#2b2826', '#363230', '#423d3a', '#4e4844', '#5b5550', '#69625c', '#78706a'],
    # Plain iron core (cooler than blister steel), used inside carbon-packed iron
    'I': ['#2c2e30', '#3d4044', '#53575c', '#6a6f75', '#82888e', '#9ba1a7', '#b5bac0', '#cfd3d8', '#e6e9ec'],
}
TEMPER = [hexrgb('#c2a36b'), hexrgb('#94664a'), hexrgb('#66688c')]  # straw, bronze, blue: heat-tint flecks

def write_gpl(path, name, ramps):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w') as f:
        f.write('GIMP Palette\nName: %s\nColumns: 9\n#\n' % name)
        for key, ramp in ramps.items():
            for i, h in enumerate(ramp):
                r, g, b, _ = hexrgb(h)
                f.write('%3d %3d %3d\t%s%d\n' % (r, g, b, key, i))
        for i, (r, g, b, _) in enumerate(TEMPER):
            f.write('%3d %3d %3d\ttemper%d\n' % (r, g, b, i))

# --------------------------------------------------------------------------------------------- shading
def shade(mask, details=(), w=16, h=16, gradient=True):
    """mask: list of strings. Letters = material, '.' = empty. Lowercase = recessed/darker face of that material.
    The outline is drawn OUTSIDE the shape (on empty pixels touching it), so 1-2 px parts keep their colour.
    details: list of (x, y, kind) with kind in blister, rivet, temper0..2, hl, dark."""
    img = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    def mat(x, y):
        if 0 <= y < len(mask) and 0 <= x < len(mask[y]):
            c = mask[y][x]
            return None if c == '.' else c
        return None
    # outline ring (4-neighbour) in the darkest tone of the touching material
    for y in range(h):
        for x in range(w):
            if mat(x, y) is not None:
                continue
            for dx, dy in ((0, 1), (1, 0), (0, -1), (-1, 0)):
                c = mat(x + dx, y + dy)
                if c is not None:
                    img.putpixel((x, y), hexrgb(RAMPS[c.upper()][0]))
                    break
    for y in range(h):
        for x in range(w):
            c = mat(x, y)
            if c is None:
                continue
            ramp = RAMPS[c.upper()]
            same = lambda dx, dy: (mat(x + dx, y + dy) or '').upper() == c.upper()
            if not same(0, -1) or not same(-1, 0):
                tone = 6                                   # lit rim (top/left)
            elif not same(0, 1) or not same(1, 0):
                tone = 3                                   # shadow rim (bottom/right)
            else:
                tone = 5 if gradient and x + y < (w + h) // 4 else 4   # small lit corner, flat body (no diagonal seam)
            if c.islower():
                tone = max(2, tone - 2)                    # recessed / underside faces sit darker
            img.putpixel((x, y), hexrgb(ramp[tone]))
    for x, y, kind in details:
        c = mat(x, y)
        if c is None:
            continue
        ramp = RAMPS[c.upper()]
        if kind == 'blister':                              # raised blister: light dot, shadow below-right
            img.putpixel((x, y), hexrgb(ramp[7]))
            if mat(x + 1, y + 1):
                img.putpixel((x + 1, y + 1), hexrgb(ramp[3]))
        elif kind == 'rivet':
            img.putpixel((x, y), hexrgb(ramp[8]))
            if mat(x + 1, y + 1):
                img.putpixel((x + 1, y + 1), hexrgb(ramp[2]))
        elif kind.startswith('temper'):
            img.putpixel((x, y), TEMPER[int(kind[-1])])
        elif kind == 'hl':
            img.putpixel((x, y), hexrgb(ramp[8]))
        elif kind == 'dark':
            img.putpixel((x, y), hexrgb(ramp[2]))
    return img

def save(img, rel):
    path = '%s/%s.png' % (TEX, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)
    return path

# --------------------------------------------------------------------------------------------- items
def diag_handle(rows, x0, y0, x1, y1, ch='W'):
    """Draws a 1 px diagonal handle into a mask (list of str), from (x0,y0) down-left to (x1,y1)."""
    rows = [list(r) for r in rows]
    x, y = x0, y0
    while y <= y1 and x >= x1:
        if rows[y][x] == '.':
            rows[y][x] = ch
        x -= 1; y += 1
    return [''.join(r) for r in rows]

E = '................'
INGOT = [E, E, E, E,
    '......MMMMMMMM..',
    '.....MMMMMMMMMM.',
    '....MMMMMMMMMMM.',
    '...mmmmmmmmmmmm.',
    '..mmmmmmmmmmmm..',
    '.mmmmmmmmmmmm...',
    E, E, E, E, E, E]
INGOT_D = [(7, 4, 'hl'), (8, 5, 'blister'), (12, 5, 'blister'), (5, 8, 'temper0'), (10, 8, 'temper2')]

NUGGET = [E, E, E, E, E, E,
    '......MMM.......',
    '.....MMMMM......',
    '....mmmmmmm.....',
    '....mmmmmm......',
    E, E, E, E, E, E]
NUGGET_D = [(6, 6, 'hl'), (7, 7, 'blister'), (8, 8, 'temper0')]

SHEET = [E, E, E,
    '......MMMMMMMM..',
    '.....MMMMMMMMM..',
    '....MMMMMMMMMM..',
    '...MMMMMMMMMMM..',
    '..MMMMMMMMMMMM..',
    '..MMMMMMMMMMMM..',
    '..MMMMMMMMMMMm..',
    '..MMMMMMMMMMm...',
    '..MMMMMMMMMm....',
    '..mmmmmmmmm.....',
    E, E, E]
SHEET_D = [(5, 5, 'rivet'), (11, 4, 'rivet'), (3, 10, 'rivet'), (10, 10, 'rivet'), (7, 7, 'blister'), (8, 9, 'temper2')]

PACKED = [
    E,
    '......CC.C......',
    '...CCCCCCCCCC...',
    '..CCCCCCCCCCCC..',
    '.CCCIIIIIIIICCC.',
    '.CCIIIIIIIIIICC.',
    '.CCIIIIIIIIIICC.',
    '.CCiiiiiiiiiiCC.',
    '.CCCiiiiiiiiCCC.',
    '..CCCCCCCCCCCC..',
    '...CCCCC.CCCC...',
    '....CC....C.....',
    E, E, E, E]
PACKED_D = [(4, 2, 'hl'), (10, 3, 'hl'), (2, 6, 'hl'), (13, 8, 'hl'), (6, 10, 'hl'), (5, 4, 'hl'), (8, 5, 'hl')]

SWORD = [E,
    '.............MM.',
    '............MMM.',
    '...........MMM..',
    '..........MMM...',
    '.........MMM....',
    '........MMM.....',
    '.......MMM......',
    '......MMM.......',
    '..mm.MMM........',
    '...mmMM.........',
    '....mmm.........',
    '....W.mm........',
    '...W............',
    '..W.............',
    '.m..............']
SWORD_D = [(13, 1, 'hl'), (10, 4, 'blister'), (8, 6, 'temper0'), (6, 8, 'temper2')]

PICKAXE = diag_handle([E,
    '....MMMMMM......',
    '...MMMMMMMMM....',
    '..MMm....mMMM...',
    '..Mm.......mMM..',
    '..m.........mMM.',
    '.............mM.',
    '..............m.',
    E, E, E, E, E, E, E, E], 9, 3, 1, 11)
PICKAXE_D = [(5, 1, 'hl'), (7, 2, 'blister'), (12, 4, 'temper0')]

AXE = diag_handle([E,
    '.....MMMM.......',
    '....MMMMMM......',
    '....MMMMMM......',
    '.....MMMmm......',
    '......mm........',
    E, E, E, E, E, E, E, E, E, E], 10, 3, 1, 12)
AXE_D = [(5, 1, 'hl'), (6, 2, 'blister'), (8, 3, 'temper0')]

SHOVEL = diag_handle([E,
    '..........MMM...',
    '.........MMMMM..',
    '.........MMMMM..',
    '..........mmm...',
    E, E, E, E, E, E, E, E, E, E, E], 10, 5, 2, 13)
SHOVEL_D = [(10, 1, 'hl'), (11, 2, 'blister'), (12, 3, 'temper0')]

HOE = diag_handle([E,
    '.......MMMMM....',
    '......MMMMMMM...',
    '..........mmm...',
    E, E, E, E, E, E, E, E, E, E, E, E], 9, 3, 1, 11)
HOE_D = [(7, 1, 'hl'), (9, 1, 'blister'), (11, 2, 'temper0')]

HELMET = [
    '................',
    '................',
    '................',
    '....MMMMMMMM....',
    '...MMMMMMMMMM...',
    '..MMMMMMMMMMMM..',
    '..MMMMMMMMMMMM..',
    '..MMmmmmmmmmMM..',
    '..MM........MM..',
    '..MM........MM..',
    '..mm........mm..',
    '................',
    '................',
    '................',
    '................',
    '................',
]
HELMET_D = [(5, 4, 'hl'), (8, 5, 'blister'), (4, 6, 'rivet'), (11, 6, 'rivet')]

CHESTPLATE = [
    '................',
    '................',
    '..MMMM....MMMM..',
    '.MMMMMMMMMMMMMM.',
    '.MMMMMMMMMMMMMM.',
    '.MMMMMMMMMMMMMM.',
    '.mmMMMMMMMMMMmm.',
    '...MMMMMMMMMM...',
    '...MMMMMMMMMM...',
    '...MMMMMMMMMM...',
    '...MMMMMMMMMM...',
    '...MMMMMMMMMM...',
    '...mmmmmmmmmm...',
    '................',
    '................',
    '................',
]
CHESTPLATE_D = [(3, 3, 'hl'), (4, 4, 'rivet'), (11, 4, 'rivet'), (7, 8, 'blister'), (9, 10, 'temper0')]

LEGGINGS = [
    '................',
    '................',
    '...MMMMMMMMMM...',
    '...MMMMMMMMMM...',
    '...mmmmmmmmmm...',
    '...MMMM..MMMM...',
    '...MMMM..MMMM...',
    '...MMMM..MMMM...',
    '...MMMM..MMMM...',
    '...MMMM..MMMM...',
    '...MMMM..MMMM...',
    '...mmmm..mmmm...',
    '................',
    '................',
    '................',
    '................',
]
LEGGINGS_D = [(4, 2, 'hl'), (5, 3, 'rivet'), (10, 3, 'rivet'), (5, 7, 'blister'), (11, 8, 'temper0')]

BOOTS = [
    '................',
    '................',
    '................',
    '................',
    '..MMMM....MMMM..',
    '..MMMM....MMMM..',
    '..MMMM....MMMM..',
    '..MMMM....MMMM..',
    '..MMMMM...MMMMM.',
    '.MMMMMM..MMMMMM.',
    '.mmmmmm..mmmmmm.',
    '................',
    '................',
    '................',
    '................',
    '................',
]
BOOTS_D = [(3, 4, 'hl'), (11, 4, 'hl'), (3, 7, 'rivet'), (11, 7, 'rivet'), (12, 9, 'temper0')]

BLOCK = ['M' * 16 for _ in range(16)]

def storage_block():
    """Block of blister steel: a bevelled plate with a recessed centre panel, corner rivets, blisters."""
    rows = []
    for y in range(16):
        row = ''
        for x in range(16):
            inner = 3 <= x <= 12 and 3 <= y <= 12
            row += 'm' if inner and (x in (3, 12) or y in (3, 12)) else 'M'
        rows.append(row)
    img = shade(rows, gradient=True)
    ramp = RAMPS['M']
    # block edges: lit top/left, shadowed bottom/right (tiles seamlessly as a full cube)
    for i in range(16):
        img.putpixel((i, 0), hexrgb(ramp[7])); img.putpixel((0, i), hexrgb(ramp[7]))
        img.putpixel((i, 15), hexrgb(ramp[1])); img.putpixel((15, i), hexrgb(ramp[1]))
    for x, y, k in [(1, 1, 'rivet'), (13, 1, 'rivet'), (1, 13, 'rivet'), (13, 13, 'rivet'),
                    (6, 6, 'blister'), (9, 8, 'blister'), (5, 10, 'blister'), (10, 5, 'blister'),
                    (7, 9, 'temper0'), (10, 10, 'temper2'), (5, 5, 'temper1')]:
        if k == 'rivet':
            img.putpixel((x, y), hexrgb(ramp[8])); img.putpixel((x + 1, y + 1), hexrgb(ramp[1]))
        elif k == 'blister':
            img.putpixel((x, y), hexrgb(ramp[7])); img.putpixel((x + 1, y + 1), hexrgb(ramp[3]))
        else:
            img.putpixel((x, y), TEMPER[int(k[-1])])
    return img

# --------------------------------------------------------------------------------------------- armour layers
def armour_layers():
    """64x32 humanoid armour layers drawn face by face. Layer 1: helmet, chestplate, arms, boots.
    Layer 2: leggings (belt + upper legs)."""
    ramp = [hexrgb(c) for c in RAMPS['M']]
    def face(img, x0, y0, w, h, eye_slit=False, rows=None):
        for y in range(h):
            if rows is not None and y not in rows:
                continue
            for x in range(w):
                edge_top, edge_left = y == (min(rows) if rows else 0), x == 0
                edge_bot, edge_right = y == (max(rows) if rows else h - 1), x == w - 1
                if edge_top or edge_left:
                    t = 6
                elif edge_bot or edge_right:
                    t = 2
                else:
                    t = 5 - ((x + y) * 3 // max(1, w + h))
                if eye_slit and y == 4 and 1 <= x <= w - 2:
                    continue                                # visor slit stays open
                img.putpixel((x0 + x, y0 + y), ramp[t])
        # a blister and a temper fleck per face, deterministic
        bx, by = x0 + w // 3, y0 + (min(rows) + 1 if rows else h // 3)
        if 0 <= by - y0 < h:
            img.putpixel((bx, by), ramp[7])
        tx, ty = x0 + (2 * w) // 3, y0 + (max(rows) - 1 if rows else (2 * h) // 3)
        if 0 <= ty - y0 < h and img.getpixel((tx, ty))[3]:
            img.putpixel((tx, ty), TEMPER[(x0 + y0) % 3])

    def box(img, u, v, w, h, d, eye_slit=False, rows=None):
        # standard Minecraft box UV: top, bottom, then right/front/left/back strip
        face(img, u + d, v, w, d)                           # top
        face(img, u + d + w, v, w, d)                       # bottom
        face(img, u, v + d, d, h, rows=rows)                # right
        face(img, u + d, v + d, w, h, eye_slit, rows=rows)  # front
        face(img, u + d + w, v + d, d, h, rows=rows)        # left
        face(img, u + d + w + d, v + d, w, h, rows=rows)    # back

    l1 = Image.new('RGBA', (64, 32), (0, 0, 0, 0))
    box(l1, 0, 0, 8, 8, 8, eye_slit=True)                   # helmet (head)
    box(l1, 16, 16, 8, 12, 4)                               # chestplate body
    box(l1, 40, 16, 4, 12, 4)                               # chestplate arms
    box(l1, 0, 16, 4, 12, 4, rows=range(7, 12))             # boots: lower legs only
    l2 = Image.new('RGBA', (64, 32), (0, 0, 0, 0))
    box(l2, 16, 16, 8, 12, 4, rows=range(8, 12))            # leggings belt (lower body)
    box(l2, 0, 16, 4, 12, 4, rows=range(0, 9))              # leggings upper legs
    return l1, l2

# --------------------------------------------------------------------------------------------- main
ITEMS = {
    'item/carbon_packed_iron': (PACKED, PACKED_D),
    'item/blister_steel_ingot': (INGOT, INGOT_D),
    'item/blister_steel_nugget': (NUGGET, NUGGET_D),
    'item/blister_steel_sheet': (SHEET, SHEET_D),
    'item/blister_steel_sword': (SWORD, SWORD_D),
    'item/blister_steel_pickaxe': (PICKAXE, PICKAXE_D),
    'item/blister_steel_axe': (AXE, AXE_D),
    'item/blister_steel_shovel': (SHOVEL, SHOVEL_D),
    'item/blister_steel_hoe': (HOE, HOE_D),
    'item/blister_steel_helmet': (HELMET, HELMET_D),
    'item/blister_steel_chestplate': (CHESTPLATE, CHESTPLATE_D),
    'item/blister_steel_leggings': (LEGGINGS, LEGGINGS_D),
    'item/blister_steel_boots': (BOOTS, BOOTS_D),
}

if __name__ == '__main__':
    write_gpl('tools/stylekit/palettes/creative_engineering.gpl', 'Creative Engineering', RAMPS)
    made = []
    for rel, (mask, det) in ITEMS.items():
        assert len(mask) == 16 and all(len(r) == 16 for r in mask), rel
        made.append((rel, save(shade(mask, det), rel)))
    made.append(('block/blister_steel_block', save(storage_block(), 'block/blister_steel_block')))
    l1, l2 = armour_layers()
    save(l1, 'models/armor/blister_steel_layer_1')
    save(l2, 'models/armor/blister_steel_layer_2')
    # preview sheet: every 16x16 texture at 8x, plus the armour layers
    os.makedirs(PREVIEW, exist_ok=True)
    cols, scale = 7, 8
    rows = (len(made) + cols - 1) // cols
    sheet = Image.new('RGBA', (cols * 16 * scale + (cols + 1) * 8, rows * 16 * scale + (rows + 1) * 8 + 32 * 4 + 16), (58, 48, 40, 255))
    for n, (_, p) in enumerate(made):
        im = Image.open(p).resize((16 * scale, 16 * scale), Image.NEAREST)
        x = 8 + (n % cols) * (16 * scale + 8); y = 8 + (n // cols) * (16 * scale + 8)
        sheet.alpha_composite(im, (x, y))
    y0 = 8 + rows * (16 * scale + 8)
    sheet.alpha_composite(l1.resize((64 * 4, 32 * 4), Image.NEAREST), (8, y0))
    sheet.alpha_composite(l2.resize((64 * 4, 32 * 4), Image.NEAREST), (16 + 64 * 4, y0))
    sheet.save('%s/tier1_preview.png' % PREVIEW)
    print('%d textures + 2 armour layers; preview at %s/tier1_preview.png' % (len(made), PREVIEW))
