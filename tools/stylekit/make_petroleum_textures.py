"""Petroleum textures (docs/petroleum-spec.md), drawn with the style kit's shader (make_textures.shade).
Run from the repo root: python3 tools/stylekit/make_petroleum_textures.py
Seismic Probe: a steel thumper rod with a wooden T-handle, a brass-cased gauge and a hardened tip."""
import os, sys
sys.path.insert(0, os.path.dirname(__file__))
import make_textures as mt

# Brass and steel: Create's brass ramp (docs/create-art-style.md) and a cool steel matched to our Steel Pipe.
mt.RAMPS['B'] = ['#592424', '#724731', '#9e6947', '#bf8e55', '#cea05a', '#e4b763', '#f7cb6c', '#fbdc7d', '#ffeb8c']
mt.RAMPS['S'] = ['#23272b', '#353a40', '#4a5057', '#5f666e', '#767e87', '#8f98a1', '#a9b2ba', '#c4ccd3', '#dfe5ea']
mt.RAMPS['G'] = ['#2a4048', '#3c5a63', '#527a84', '#6c9aa3', '#8bb7bf', '#a9d0d6', '#c3e2e6', '#dcf0f2', '#f0fafb']


def probe():
    g = [['.'] * 16 for _ in range(16)]
    def put(x, y, c):
        if 0 <= x < 16 and 0 <= y < 16:
            g[y][x] = c
    for i in range(11):                      # rod, 2 px, top-right to bottom-left
        put(12 - i, 3 + i, 'S'); put(13 - i, 3 + i, 'S')
    for k in range(5):                       # T-handle across the top
        put(11 + k, 0 + k, 'W'); put(12 + k, 0 + k, 'W')
    for x in range(7, 11):                   # gauge case
        for y in range(5, 9):
            put(x, y, 'B')
    put(8, 6, 'G'); put(9, 6, 'G'); put(8, 7, 'G'); put(9, 7, 'g')   # gauge glass
    put(1, 14, 's'); put(2, 14, 's'); put(1, 15, 's'); put(0, 15, 's')  # hardened tip
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
    for img, rel in ((paraffin(), 'item/paraffin_wax'), (oil_can(), 'item/oil_can'), (asphalt(), 'block/asphalt')):
        mt.save(img, rel)
        mt.Image.open('%s/%s.png' % (mt.TEX, rel)).resize((128, 128), mt.Image.NEAREST).save(os.path.join(mt.PREVIEW, rel.split('/')[1] + '_x8.png'))
    path = mt.save(probe(), 'item/seismic_probe')
    big = mt.Image.open(path).resize((256, 256), mt.Image.NEAREST)
    os.makedirs(mt.PREVIEW, exist_ok=True)
    big.save(os.path.join(mt.PREVIEW, 'seismic_probe_x16.png'))
    print(path)
