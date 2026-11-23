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

def press_dies():
    """Metalworking: a Mechanical Press over a Depot, driven by a shaft from the south."""
    b = plate(5)
    put(b, (2, 1, 2), ('create:depot', {}))
    put(b, (2, 3, 2), ('create:mechanical_press', {'facing': 'north'}))
    for z in (3, 4):
        put(b, (2, 3, z), ('create:shaft', {'axis': 'z', 'waterlogged': 'false'}))
    return structure((5, 4, 5), b)

def diesel_engine():
    """Module 1: a 3-crankcase V6 along X (output east), tank on the north side, hand crank at the front (west),
    flywheel and a Create shaft at the back."""
    b = plate(7)
    for x in (2, 3, 4):
        put(b, (x, 1, 3), (MODID + ':diesel_crankcase', {'facing': 'east'}))
        put(b, (x, 2, 4), (MODID + ':diesel_cylinder', {'facing': 'south', 'running': 'false'}))
        put(b, (x, 2, 2), (MODID + ':diesel_cylinder', {'facing': 'north', 'running': 'false'}))
    put(b, (3, 1, 2), ('create:fluid_tank', {'top': 'true', 'bottom': 'true', 'shape': 'window'}))
    put(b, (1, 1, 3), ('create:hand_crank', {'facing': 'west', 'waterlogged': 'false'}))
    put(b, (5, 1, 3), (MODID + ':diesel_flywheel', {'axis': 'x'}))
    put(b, (6, 1, 3), ('create:shaft', {'axis': 'x', 'waterlogged': 'false'}))
    return structure((7, 4, 7), b)


# ---- 0.20.0: heat, fermenting, distilling, drive chain, pumpjack, lubrication
def stand(fire='campfire', lit=True): return (MODID + ':andesite_campfire_stand', {'fire': fire, 'lit': str(lit).lower()})
def pipe(*dirs):
    return (MODID + ':steel_pipe', dict({d: str(d in dirs).lower() for d in ('north', 'south', 'east', 'west', 'up', 'down')}, waterlogged='false'))
def tank_column(b, x, z, h):
    for y in range(1, h + 1):
        put(b, (x, y, z), (MODID + ':steel_tank', {'top': str(y == h).lower(), 'bottom': str(y == 1).lower(), 'shape': 'window'}))
def motor(facing): return ('create:creative_motor', {'facing': facing})
def shaft(axis): return ('create:shaft', {'axis': axis, 'waterlogged': 'false'})

def campfire_stand():
    """5x5: fan (1,1,2) blowing east through a lit stand (2,1,2) onto a depot (3,1,2); a soul stand at (2,1,4)."""
    b = plate(5)
    put(b, (1, 1, 2), fan('east'))
    put(b, (2, 1, 2), stand())
    put(b, (3, 1, 2), DEPOT)
    put(b, (2, 1, 4), stand('soul'))
    return structure((5, 3, 5), b)

def fermenting_vat():
    """6x6: four lit stands (2..3,1,2..3) under a 2x2x2 vat (2..3,2..3,2..3); funnel in at (1,3,2); pipe out (4,2,3) to a tank (5,1..2,3)."""
    b = plate(6)
    for x in (2, 3):
        for z in (2, 3):
            put(b, (x, 1, z), stand())
            for y in (2, 3):
                put(b, (x, y, z), MODID + ':fermenting_vat')
    put(b, (1, 3, 2), funnel('west'))
    put(b, (4, 2, 3), pipe('west', 'east'))
    tank_column(b, 5, 3, 2)
    return structure((6, 4, 6), b)

def distilling():
    """7x7: a lit stand (3,1,3) under a 1x1x3 column (3,2..4,3); level 1 tap (4,3,3) east -> pipe (5,3,3) -> tank (5,1..2,3);
    level 2 tap (2,4,3) west -> pipe (1,4,3) -> tank (1,1..3,3)."""
    b = plate(7)
    put(b, (3, 1, 3), stand())
    for y in (2, 3, 4):
        put(b, (3, y, 3), MODID + ':distilling_tank')
    put(b, (4, 3, 3), (MODID + ':tap', {'facing': 'east', 'open': 'true', 'powered': 'false', 'flowing': 'false'}))
    put(b, (5, 3, 3), pipe('west', 'down'))
    tank_column(b, 5, 3, 2)
    put(b, (2, 4, 3), (MODID + ':tap', {'facing': 'west', 'open': 'true', 'powered': 'false', 'flowing': 'false'}))
    put(b, (1, 4, 3), pipe('east', 'down'))
    tank_column(b, 1, 3, 3)
    return structure((7, 6, 7), b)

def drive_chain():
    """7x7: creative motor (1,1,3) east -> shaft (2,1,3) -> flywheel (3,1,3); sprocket (3,4,3) chained above, shaft (4,4,3), cog (5,4,3)."""
    b = plate(7)
    put(b, (1, 1, 3), motor('east'))
    put(b, (2, 1, 3), shaft('x'))
    put(b, (3, 1, 3), (MODID + ':diesel_flywheel', {'axis': 'x'}))
    put(b, (3, 4, 3), (MODID + ':drive_pulley', {'axis': 'x'}))
    put(b, (4, 4, 3), shaft('x'))
    put(b, (5, 4, 3), ('create:cogwheel', {'axis': 'x', 'waterlogged': 'false'}))
    return structure((7, 6, 7), b)

def pumpjack():
    """9x9: Crank Gearbox (2,1,1) facing south, creative motor (3,1,1) west; girder column (2,1..2,2), bearing (2,3,2);
    Wellhead (2,1,4) (reach 2); steel pipe (3,1,4) east to a tank (4,1..2,4)."""
    b = plate(9)
    put(b, (2, 1, 1), (MODID + ':pumpjack', {'facing': 'south'}))
    put(b, (3, 1, 1), motor('west'))
    put(b, (2, 1, 2), 'create:metal_girder')
    put(b, (2, 2, 2), 'create:metal_girder')
    put(b, (2, 3, 2), (MODID + ':walking_beam_bearing', {'facing': 'south'}))
    put(b, (2, 1, 4), MODID + ':wellhead')
    put(b, (3, 1, 4), pipe('west', 'east'))
    tank_column(b, 4, 4, 2)
    return structure((9, 6, 9), b)

def lubrication():
    """5x5: creative motor (1,2,2) east -> shaft (2,2,2) -> press (3,2,2) over a depot (3,1,2)."""
    b = plate(5)
    put(b, (1, 2, 2), motor('east'))
    put(b, (2, 2, 2), shaft('x'))
    put(b, (3, 2, 2), PRESS)
    put(b, (3, 1, 2), DEPOT)
    return structure((5, 4, 5), b)

SCENES = [cementation, coke_oven, blast_furnace, press_dies, diesel_engine,
          campfire_stand, fermenting_vat, distilling, drive_chain, pumpjack, lubrication]

if __name__ == '__main__':
    os.makedirs(OUT, exist_ok=True)
    for scene in SCENES:
        with gzip.open('%s/%s.nbt' % (OUT, scene.__name__), 'wb') as f:
            f.write(scene())
    print('%d ponder schematics -> %s' % (len(SCENES), OUT))
