# Petroleum: crude, veins, pumpjack, fuels (0.14.0; pumpjack rebuilt from parts 0.15.0-0.17.0)

Kragnoth, 2026-11-22: "a way to get proper diesel, kerosene and gas now. Small pools that spawn in the desert, a way
to prospect for infinite veins that make it so one can place down a nodding donkey in a chunk and create oil."
Chosen: kinetic, 3-block pumpjack; handheld seismic probe.

## Fluids
| Fluid | Tint | Tag | Engine fuel tier (SU per cylinder, mB/s burn) |
|---|---|---|---|
| `crude_oil` | near-black | `c:crude_oil` | 12288 SU @ 2.5 (creosote's output, thirstier) |
| `kerosene` | pale | `c:kerosene` | 27648 SU @ 1.25 |
| `diesel` | amber | `c:diesel` | 30720 SU @ 1 (existing diesel tier) |
| `gasoline` | gold | `c:gasoline` | 24576 SU @ 2 (existing gasoline tier) |

All are `ModFluids.Tinted` (water textures, tinted; bucket models fill at runtime). Other mods' crude / diesel /
kerosene / gasoline join through the `c:` tags and the optional tier entries.

## Desert crude pools
- Vanilla `minecraft:lake` filled with crude oil, walled in sandstone (`worldgen/configured_feature/crude_pool`).
- Placed 1 in 48 chunks on the surface (`placed_feature/crude_pool`), added to `#c:is_desert` at the `lakes` step by
  `neoforge/biome_modifier/crude_pools`. A starter supply: bucket it, pipe it.

## Oil veins (infinite)
- `content/petroleum/OilVeins`: the Overworld is cut into 8x8-chunk regions. Each region holds one vein with chance
  0.85, in one of its chunks, flowing 20–60 mB/s forever.
- Everything is derived from the world seed and the region, so nothing is saved and every server agrees.
- `OilVeins.force` / `unforce` are test and demo hooks (not saved).

## Seismic Probe
- Item `seismic_probe` (64 uses, 1 s cooldown). Right-click the ground: a thump, dust and an actionbar report.
- Reports "Oil vein right here: N mB/s" when this chunk has one. Otherwise it gives the nearest vein within 8 chunks,
  rounded to 8 blocks with an 8-way compass direction, or "No oil within 128 blocks".
- Recipe: electron tube, brass sheet, steel rod (diagonal).

## Pumpjack: Crank Gearbox + built column + Walking Beam Bearing + Wellhead (0.17.0)
Kragnoth, 2026-11-22:
- keep it "Create-centric, not magical multiblock", with no glue or contraptions, true to Create's ethos in each part;
- scale it up so the well's outlets line up with Create pipes (look at Oritech / IE, stay Create);
- use Create-centric looks (girders, industrial iron) at real-life sizes; and it must animate.

Each part is its own block, placed, wrenched and broken on its own. The parts find each other by where they stand
(`PumpjackRig`), like Create's steam engine finding its boiler. Nothing forms and nothing is hidden. The tall part is
built by the player out of real Create blocks.

### Layout (in a row along the gearbox's facing, all at ground level)
| Part | Where | Does |
|---|---|---|
| Crank Gearbox (`pumpjack`) | the start; faces away from you when placed | Kinetic: shaft on either side; counterweighted cranks at 1/4 shaft speed, throw 4 px (6 px from rear arm 2) |
| Column | 1–3 blocks on (the **rear arm**, a) | 2–6 blocks of anything in `#creativeengineering:pumpjack_column`: Create metal girder, industrial iron, weathered iron, our steel / cast iron blocks |
| Walking Beam Bearing (`walking_beam_bearing`) | on top of the column (its height, h) | Pillow-block bearing; the beam pivots in it (10 px up); draws the beam and linkage |
| Wellhead, or any `StrokeDriven` block | 1–6 blocks past the column (the **reach**, b) | Brings up its well source while stroked |

### Rules
- **Stroke** = (crank throw / a) x b / 4. It is 1 for the smallest rig (2-high column, a 1, b 1) and up to 6 (a 1, b 6).
  - Draw = well rate x min(1, RPM / 64) x stroke.
  - Stress = 8 SU per RPM per unit of stroke, or 2 SU per RPM when nothing runs.
- **Height gates reach.** A longer reach swings the horsehead lower. If its lowest point would come within 6 px of the
  wellhead's stuffing box, the rig is "too short" and does not run (goggles say so). Raise the column to fix it.
  - Reach 6 needs a 5-high column.
  - The field-size preview rig is column 5, a 2, b 4.
- **Size follows the build.** The beam is drawn one block per block of a + b, as a box girder with Create's girder
  flange; its section thickens with length (x1 up to x2). The horsehead scales with the reach (x1 at b 1, x2.25 at b 6).
  - Pitman arms and the polished rod are 16 px segments repeated along their length.
- **Animation:**
  - the cranks turn with the shaft (1/4 speed);
  - the beam rocks on the bearing;
  - the equalizer rides the rear end and the pitman arms follow pin to equalizer;
  - the bridle and polished rod ride the horsehead lip down into the stuffing box.
  - Drawn by `PumpjackVisual` (gearbox: Create's shaft + cranks) and `BeamBearingVisual` (everything else), with
    renderer fallbacks.
  - All poses come from `PumpjackRig.pose`; every model stays inside vanilla's -16..32 (`eng()` asserts it).
- **Oritech-style guidance, no assembly:**
  - Right-click the gearbox with an empty hand: Create's outliner shows the column, bearing and well spots. Green means
    already in place.
  - Right-click the gearbox holding a bearing (with a column already built 1 on) or a Wellhead: it places it in the
    next spot.
- **Goggles** on the gearbox or the bearing show the column height, rear arm, reach, stroke, what is stroked, and any
  missing part or "too short".
- Preview: `tools/stylekit/render_pumpjack.py [three_quarter|side|front]`.

### Wellhead
- A full block. The casing head has a flow cross whose four ports are **Create pipe size** (8 px, centred, flanged),
  so a pipe on any side meets one square.
- Bonnet and brass stuffing box on top, for the rod.
- While stroked it fills its 8 B tank. It pushes into pipe runs on its sides (`PipeRuns`, no pump needed, up to 500 mB
  per 10 ticks) and drains to pumps.
- Goggles show what is below, the draw, and the tank.

### Well sources (data-driven: `data/<ns>/well_sources/*.json`)
Fields: `fluid`, `rate` (mB/s at full stroke) or `oil_vein: true`, optional `dimension`, `biomes` (ids or `#tags`) and
`priority`. The highest priority that applies wins.

| Source | Fluid | Rate | Where | Priority |
|---|---|---|---|---|
| `oil_vein` | crude oil | the vein's 20–60 | Overworld chunks with a vein | 100 |
| `dry_ground` | water | 5 | deserts, badlands, dry biomes | 10 |
| `water` | water | 20 | anywhere in the Overworld | 0 |
| `nether_lava` | lava | 10 | the Nether | 0 |

### StrokeDriven
`content/petroleum/StrokeDriven` is the interface a block entity implements to be worked by a Pumpjack. It's the hook
for later stroke machines (a stamp mill on the block below, bellows for the furnaces, a pipeline booster).

### Desert pools
Pools are finite. Drain them the Create way: a hose pulley or a pump, or by bucket.

## Refining
- Distilling recipe `distilling/crude_oil` (0.19.0): 100 mB of any `c:crude_oil` gives four levels, heaviest lowest:
  heavy oil 20 (level 1), diesel 30, kerosene 25, gasoline 25.
- Needs heat 2 (a lit Blaze Burner) and a column 5 tall.
- Replaces the 0.12 conditional IP / CDG crude recipes.

## Heavy oil and its products (0.19.0)
Kragnoth chose this pass: uses for the fuels' byproducts, namely the heavy fraction and lubricant (gas cap later).

- **Heavy oil** (`heavy_oil`, `c:heavy_oil`): the bottom cut. Not a fuel; it is the feedstock for everything below.
- **Asphalt** (`asphalt`, `asphalt_slab`, `asphalt_stairs`):
  - heated Create mixing of 250 mB heavy oil + 2 gravel + 1 sand gives 4 asphalt;
  - slabs and stairs by crafting or stonecutter;
  - a road surface: walking on it is faster (block speed factor 1.25).
  - Texture: drawn tar-black with aggregate (`tools/stylekit/make_petroleum_textures.py`).
- **Textures:**
  - Paraffin Wax is altered Excalibur art (resin brick, `tools/stylekit/make_excalibur_derived.py`).
  - The Oil Can (0.19.2) is a classic dome-bottom oiler after Kragnoth's reference photo: steel dome, knurled brass
    collar, long bent spout. Drawn in Excalibur's palettes (`tools/stylekit/make_oil_can.py`).
- **Paraffin Wax** (`paraffin_wax`):
  - Create compacting presses 100 mB heavy oil into 1;
  - waxes copper exactly like honeycomb, by hand or in a Deployer (`ParaffinWaxItem` extends `HoneycombItem`);
  - string over wax makes a candle.
- **Lubricant** (`lubricant`, `c:lubricant`): Create mixing, 50 mB heavy oil + 50 mB kerosene gives 100 mB.
- **Oil Can** (`oil_can`):
  - holds 1 B of lubricant; fill it with a Spout, or use it on any tank holding lubricant;
  - use it on any Create kinetic block, by hand or in a Deployer, to spend a 50 mB dose. The machine is then
    **lubricated** for a Minecraft day (stacks to 3 days), and its share of the network's stress drops by a quarter.
  - Goggles show "Lubricated: -25% stress, n min left".
  - It works on every Create machine: the state lives on `KineticBlockEntity` (`KineticBlockEntityLubeMixin`: saved,
    synced, expires on its own, goggle line), and the cut is applied where Create sums network stress
    (`KineticNetworkLubeMixin` on `getActualStressOf`). See `content/petroleum/Lubrication`.

## Demo
`/test runrefinery` (op 2) builds the whole chain in front of you:
- a Crank Gearbox on a creative motor, a 2-high Create girder column with the Walking Beam Bearing, and a Wellhead
  (the smallest rig), with the vein forced to 60 mB/s until restart;
- Steel Pipe up into a 1x1x5 Distilling Tank on a creative Blaze Burner;
- four taps into Steel Storage Tanks: heavy oil, diesel, kerosene, gasoline.

The game test `refineryDemoRuns` keeps it honest.

## Not yet
- Worldgen pools are untested headless (data loads cleanly); check in a desert.
- JEI page for veins / probe; Ponder scenes for the pumpjack and probe.
