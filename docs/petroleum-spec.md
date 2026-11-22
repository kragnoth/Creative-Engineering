# Petroleum: crude, veins, pumpjack, fuels (0.14.0)

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

## Pumpjack (nodding donkey)
- Place the **Pumpjack** item: the gearbox goes where you click; the Frame (samson post) and the Wellhead follow in
  a row away from you. The placement fails with a message when the two spaces aren't free.
- Breaking any of the three breaks the whole machine and drops one Pumpjack.
- **Power:** a shaft on either side of the gearbox (the crank axle runs across the machine). Stress 8 SU per RPM.
- **Rate:** vein rate x min(1, RPM / 64). Full rate at 64 RPM; a 16 RPM creative motor gives a quarter.
- Crude goes into an 8 B internal tank.
- **Output:**
  - The Wellhead pushes crude down any pipe run leaving its sides or front. It uses the shared `PipeRuns` tracer, so no
    pump is needed (line pressure), up to 250 mB per 10 ticks shared between the run's tanks.
  - Pumps and hoses may also drain the Wellhead directly. Its capability is a block capability, drain-only.
- **No vein:** it still nods, but nothing comes up. Goggles on the gearbox show the vein, the current rate, a hint when
  below 64 RPM, and the tank.
- **Look:** Create textures referenced, never copied.
  - Skid: industrial iron. Gearbox: andesite casing. A-frame post and walking beam: industrial iron. Horsehead: steel.
    Bearings: brass.
  - Moving parts are partials posed by one linkage (`PumpjackBlockEntity.pose`): crank (radius 4 px, 1/4 of shaft
    speed), beam (follows the crank pin), pitman arms (stretched between the pin and the beam's rear pin), polished
    rod (hangs from the horsehead lip into the stuffing box).
  - Drawn by `PumpjackVisual` (Flywheel, with Create's shaft), or by `PumpjackRenderer` when instancing is off.
  - Preview: `tools/stylekit/render_pumpjack.py`.

## Refining
- Distilling recipe `distilling/crude_oil`: 100 mB of any `c:crude_oil` gives diesel 40 (level 1), kerosene 30
  (level 2) and gasoline 30 (level 3).
- Needs heat 2 (a lit Blaze Burner) and a column 4 tall.
- Replaces the 0.12 conditional IP / CDG crude recipes.

## Demo
`/test runrefinery` (op 2) builds the whole chain in front of you:
- a pumpjack on a creative motor, with its vein forced to 60 mB/s until restart;
- Steel Pipe up into a 1x1x4 Distilling Tank on a creative Blaze Burner;
- three taps into tall Steel Storage Tanks.

The game test `refineryDemoRuns` keeps it honest.

## Not yet
- Worldgen pools are untested headless (data loads cleanly); check in a desert.
- JEI page for veins / probe; Ponder scenes for the pumpjack and probe.
