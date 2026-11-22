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


if __name__ == '__main__':
    os.chdir(os.path.join(os.path.dirname(__file__), '..', '..'))
    path = mt.save(probe(), 'item/seismic_probe')
    big = mt.Image.open(path).resize((256, 256), mt.Image.NEAREST)
    os.makedirs(mt.PREVIEW, exist_ok=True)
    big.save(os.path.join(mt.PREVIEW, 'seismic_probe_x16.png'))
    print(path)
