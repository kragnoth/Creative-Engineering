# Petroleum: crude, veins, pumpjack, fuels (0.14.0, pumpjack split 0.15.0)

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

## Pumpjack and Wellhead (0.15.0: two blocks, the Create way)
Kragnoth, 2026-11-22: keep it "Create-centric, not magical multiblock", no glue or contraptions, and true to Create's
ethos in each part. 0.14.0's one-item, three-block pumpjack is gone. The pattern now is Create's steam engine: a drive
block whose linkage hangs outside its own block and does its work on a separate block you place yourself.
Nothing forms, nothing is hidden. Every piece is placed, wrenched (turn / pick up) and broken on its own.

### Pumpjack (`pumpjack`)
- One kinetic block. Its model is the skid, a reduction gearbox on Create's shaft line and the samson post.
- The walking beam, horsehead, cranks and pitman arms are partials. The beam overhangs the block in front
  (`horizontal_facing`, away from you when placed; sneak to flip).
- **Power:** a shaft on either side (the crank axle runs across the machine). Stress 8 SU per RPM. One crank turn is
  four shaft turns, so 64 RPM gives 16 strokes a minute.
- **Driving:** every tick it turns, it strokes the block in front if that block is `StrokeDriven`, with power
  min(1, RPM / 64). With nothing there it still nods (no rod fitted), and its goggles say so.
- **Look:** Create textures referenced, never copied.
  - Skid, post and beam: industrial iron. Gearbox: andesite casing. Horsehead: steel. Bearings: brass.
  - One linkage (`PumpjackBlockEntity.pose`): crank radius 2.5 px on the shaft; the beam pivots on the post
    (y 28, z 15); pitman arms run from the pins to the equalizer; the polished rod hangs from the horsehead lip over
    the centre of the block in front.
  - Drawn by `PumpjackVisual` (Flywheel, with Create's shaft), or by `PumpjackRenderer` when instancing is off.
  - Preview: `tools/stylekit/render_pumpjack.py`.

### Wellhead (`wellhead`)
- A casing head on the ground, with a flow cross giving outlets on all four sides. It does nothing on its own, like a
  steam engine with no boiler.
- **While stroked** it brings up its well source x power into an 8 B tank.
- **Output:** it pushes down any pipe run leaving its sides (`PipeRuns`: line pressure, no pump needed), up to 250 mB
  per 10 ticks. It also drains to pumps (drain-only capability).
- **Goggles:** what is below and its rate, idle or drawing, a hint below 64 RPM, and the tank.

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
- Distilling recipe `distilling/crude_oil`: 100 mB of any `c:crude_oil` gives diesel 40 (level 1), kerosene 30
  (level 2) and gasoline 30 (level 3).
- Needs heat 2 (a lit Blaze Burner) and a column 4 tall.
- Replaces the 0.12 conditional IP / CDG crude recipes.

## Demo
`/test runrefinery` (op 2) builds the whole chain in front of you:
- a Pumpjack on a creative motor stroking a Wellhead, whose vein is forced to 60 mB/s until restart;
- Steel Pipe up into a 1x1x4 Distilling Tank on a creative Blaze Burner;
- three taps into tall Steel Storage Tanks.

The game test `refineryDemoRuns` keeps it honest.

## Not yet
- Worldgen pools are untested headless (data loads cleanly); check in a desert.
- JEI page for veins / probe; Ponder scenes for the pumpjack and probe.
