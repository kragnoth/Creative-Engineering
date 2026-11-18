"""Writes Creative Engineering's Ponder schematics (gzip NBT structure files) without launching the game.
Run from the repo root: python3 dev/ponder_nbt.py
Adapted from Tarbound's dev/make_ponder_nbt.py (MIT, Kragnoth). Block IDs follow docs/steelworks-spec.md and dev/SAVE_FORMAT.md;
Create blockstate names checked against Create 6.0.10 (blaze burner 'blaze', funnel 'extracting', fan/press 'facing', shaft 'axis')."""
import gzip, os, struct

MODID = 'creativeengineering'
OUT = 'mod/src/main/resources/assets/%s/ponder' % MODID

def s(x): b = x.encode(); return struct.pack('>H', len(b)) + b
def named(tid, name, payload): return bytes([tid]) + s(name) + payload
def i(v): return struct.pack('>i', v)
def compound(items):  # items: list of (tid, name, payload)
    return b''.join(named(t, n, p) for t, n, p in items) + b'\x00'
def lst(tid, payloads): return bytes([tid]) + i(len(payloads)) + b''.join(payloads)
def ints(vals): return lst(3, [i(v) for v in vals])
def string(v): return s(v)

def structure(size, blocks):
    palette, index, out = [], {}, []
    for pos, name, props in blocks:
        key = (name, tuple(sorted(props.items())))
        if key not in index:
            index[key] = len(palette)
            entry = [(8, 'Name', string(name))]
            if props:
                entry.append((10, 'Properties', compound([(8, k, string(v)) for k, v in sorted(props.items())])))
            palette.append(compound(entry))
        out.append(compound([(9, 'pos', ints(pos)), (3, 'state', i(index[key]))]))
    root = compound([
        (3, 'DataVersion', i(3955)),  # 1.21.1
        (9, 'size', ints(size)),
        (9, 'palette', lst(10, palette)),
        (9, 'blocks', lst(10, out)),
        (9, 'entities', lst(10, [])),
    ])
    return named(10, '', root[:-1] + b'\x00')

def plate(n, block='minecraft:smooth_stone'):
    return [((x, 0, z), block, {}) for x in range(n) for z in range(n)]

def put(blocks, pos, block):
    name, props = block if isinstance(block, tuple) else (block, {})
    blocks.append((pos, name, props))

# Palette
COKE = MODID + ':coke_oven_bricks'
BLAST = MODID + ':blast_furnace_bricks'
ALLOY = MODID + ':alloy_furnace_bricks'
def burner(level='kindled'): return ('create:blaze_burner', {'blaze': level, 'facing': 'north'})
def fan(facing): return ('create:encased_fan', {'facing': facing})
def funnel(facing, extracting=False): return ('create:andesite_funnel', {'facing': facing, 'extracting': str(extracting).lower(), 'powered': 'false'})
PRESS = ('create:mechanical_press', {'facing': 'north'})
BASIN = ('create:basin', {'facing': 'down'})
DEPOT = 'create:depot'
LAVA = 'minecraft:lava'

# Scenes (docs/steelworks-spec.md section 8)
def cementation():
    """Tier 1: press packs iron + carbon in a basin; fan through lava bulk-blasts it on a depot."""
    b = plate(7)
    put(b, (1, 1, 3), BASIN)
    put(b, (1, 3, 3), PRESS)
    put(b, (3, 1, 3), DEPOT)
    put(b, (5, 1, 3), fan('west'))   # blows west across the lava toward the depot
    put(b, (4, 1, 3), LAVA)
    return structure((7, 4, 7), b)

def coke_oven():
    """Tier 2: a 2x2x2 oven on burners, funnel in, funnel out."""
    b = plate(6)
    for x in (2, 3):
        for z in (2, 3):
            put(b, (x, 1, z), burner('kindled'))
            for y in (2, 3):
                put(b, (x, y, z), COKE)
    put(b, (1, 3, 2), funnel('west'))           # coal in
    put(b, (4, 2, 3), funnel('east', True))     # coke out
    return structure((6, 4, 6), b)

def blast_furnace():
    """Tier 3: a 1x1x3 stack on a burner; fan blowing into the bottom layer = air blast."""
    b = plate(5)
    put(b, (2, 1, 2), burner('kindled'))
    for y in (2, 3, 4):
        put(b, (2, y, 2), BLAST)
    put(b, (2, 5, 2), funnel('up'))             # charge from the top
    put(b, (1, 2, 2), fan('east'))              # air blast into the bottom layer
    put(b, (3, 2, 2), funnel('east', True))     # steel and slag out
    return structure((5, 6, 5), b)

def alloy_furnace():
    """Tier 4: a 2x2x2 alloy furnace on four burners; fan into the bottom layer; funnels in (top) and out (side)."""
    b = plate(6)
    for x in (2, 3):
        for z in (2, 3):
            put(b, (x, 1, z), burner('kindled'))
            for y in (2, 3):
                put(b, (x, y, z), ALLOY)
    put(b, (2, 4, 2), funnel('up'))
    put(b, (1, 2, 2), fan('east'))
    put(b, (4, 2, 3), funnel('east', True))
    return structure((6, 5, 6), b)

SCENES = [cementation, coke_oven, blast_furnace, alloy_furnace]

if __name__ == '__main__':
    os.makedirs(OUT, exist_ok=True)
    for scene in SCENES:
        with gzip.open('%s/%s.nbt' % (OUT, scene.__name__), 'wb') as f:
            f.write(scene())
    print('%d ponder schematics -> %s' % (len(SCENES), OUT))
