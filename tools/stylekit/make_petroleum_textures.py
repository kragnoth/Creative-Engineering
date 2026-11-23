"""Petroleum textures (docs/petroleum-spec.md), drawn with the style kit's shader (make_textures.shade).
Run from the repo root: python3 tools/stylekit/make_petroleum_textures.py
Seismic Probe: a steel thumper rod with a wooden T-handle, a brass-cased gauge and a hardened tip."""
import os, sys
sys.path.insert(0, os.path.dirname(__file__))
import make_textures as mt

# Brass and steel: Create's brass ramp (docs/create-art-style.md) and a cool steel matched to our Steel Pipe.
mt.RAMPS['B'] = ['#3d2a1a', '#724731', '#9e6947', '#bf8e55', '#cea05a', '#e4b763', '#f7cb6c', '#fbdc7d', '#ffeb8c']
mt.RAMPS['S'] = ['#23272b', '#353a40', '#4a5057', '#5f666e', '#767e87', '#8f98a1', '#a9b2ba', '#c4ccd3', '#dfe5ea']
mt.RAMPS['G'] = ['#26302c', '#323e39', '#3f4c46', '#4d5b55', '#5d6c65', '#6f7e77', '#83918a', '#99a69f', '#b1bcb6']


def probe():
    """0.21.2 art pass: a heavier thumper that reads as a tool, not a wand: a 2 px steel rod, a broad treated-wood
    T-handle, a round brass-cased gauge with dull glass, a steel collar and a hardened tip."""
    g = [['.'] * 16 for _ in range(16)]
    def put(x, y, c):
        if 0 <= x < 16 and 0 <= y < 16:
            g[y][x] = c
    for i in range(12):                      # rod, 2 px, top-right to bottom-left
        put(12 - i, 3 + i, 'S'); put(13 - i, 3 + i, 'S')
    for k in range(6):                       # T-handle across the top, 2 px thick
        put(9 + k, 0 + k, 'W'); put(10 + k, 0 + k, 'W'); put(10 + k, 1 + k, 'W') if k < 5 else None
    for y in range(6, 11):                   # round gauge case
        for x in range(5, 10):
            if (x - 7) ** 2 + (y - 8) ** 2 <= 5:
                put(x, y, 'B')
    for x, y in ((6, 7), (7, 7), (6, 8), (7, 8), (8, 8)):
        put(x, y, 'G')                       # glass
    put(7, 9, 'g')                           # needle
    put(10, 9, 'S'); put(11, 8, 'S')         # collar where the gauge clamps on
    for x, y in ((1, 14), (2, 14), (1, 15), (0, 15), (2, 13)):
        put(x, y, 's')                       # hardened tip
    return mt.shade([''.join(r) for r in g])


mt.RAMPS['A'] = ['#e9dcae', '#f0e5bd', '#f6eecb', '#fbf5d9', '#fffae6', '#fffdf0', '#fffef7', '#ffffff', '#ffffff']   # paraffin
mt.RAMPS['T'] = ['#1f2125', '#2b2e33', '#383c42', '#474c53', '#575d65', '#6a7179', '#80878f', '#979ea5', '#b0b6bc']   # tin can


def paraffin():
    """A pale block of wax, chamfered, with a pressed groove: honeycomb's industrial cousin."""
    rows = ['................',
            '................',
            '................',
            '.....AAAAAA.....',
            '....AAAAAAAA....',
            '...AAAAAAAAAA...',
            '...AAAAAAAAAA...',
            '...AAaaaaaaAA...',
            '...AAAAAAAAAA...',
            '...AAAAAAAAAA...',
            '...aAAAAAAAAa...',
            '....aaaaaaaa....',
            '................',
            '................',
            '................',
            '................']
    return mt.shade(rows)


def oil_can():
    """A tin pump oil can: round body, brass spout reaching up-right, thumb pump on top."""
    rows = ['..............B.',
            '.............B..',
            '............B...',
            '...........B....',
            '.....BB...B.....',
            '.....BB..B......',
            '....TTTTBB......',
            '...TTTTTTB......',
            '..TTTTTTTT......',
            '..TTTTTTTT......',
            '..TTTTTTTT......',
            '..TTTTTTTT......',
            '..tTTTTTTt......',
            '...tttttt.......',
            '................',
            '................']
    return mt.shade(rows)


def asphalt():
    """Dark road surface: tar-black base, grey and tan aggregate, a little glint. Tiles seamlessly."""
    import random
    rnd = random.Random(1909)
    img = mt.Image.new('RGBA', (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            v = rnd.randint(34, 46)
            px[x, y] = (v, v, v + 3, 255)
    for _ in range(26):                      # aggregate stones, 1-2 px
        x, y = rnd.randrange(16), rnd.randrange(16)
        g = rnd.choice([(78, 78, 80), (92, 88, 82), (64, 66, 70), (108, 102, 92)])
        px[x, y] = g + (255,)
        if rnd.random() < 0.35:
            px[(x + 1) % 16, y] = tuple(max(0, c - 14) for c in g) + (255,)
    for _ in range(4):                       # glint
        x, y = rnd.randrange(16), rnd.randrange(16)
        px[x, y] = (128, 128, 132, 255)
    return img


if __name__ == '__main__':
    os.chdir(os.path.join(os.path.dirname(__file__), '..', '..'))
    # paraffin wax and the oil can now come from Excalibur art: make_excalibur_derived.py
    for img, rel in ((asphalt(), 'block/asphalt'),):
        mt.save(img, rel)
        mt.Image.open('%s/%s.png' % (mt.TEX, rel)).resize((128, 128), mt.Image.NEAREST).save(os.path.join(mt.PREVIEW, rel.split('/')[1] + '_x8.png'))
    path = mt.save(probe(), 'item/seismic_probe')
    big = mt.Image.open(path).resize((256, 256), mt.Image.NEAREST)
    os.makedirs(mt.PREVIEW, exist_ok=True)
    big.save(os.path.join(mt.PREVIEW, 'seismic_probe_x16.png'))
    print(path)
