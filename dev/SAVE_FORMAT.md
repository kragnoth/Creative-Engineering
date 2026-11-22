# Creative Engineering: save-format freeze

**Status: 0.2.0-alpha (alpha, not yet public).**
- IDs in use since 0.1.0: `carbon_packed_iron`, the `blister_steel_*` items and block, and the tags `steel_grade`, `cast_grade` and `creosoted_sleepers`.
- IDs in use since 0.2.0: `coke_oven_bricks` (block entity `coke_oven`, blockstates `lit` and, since 0.2.1, `working`), `coal_coke`, `coke_dust`, `coke_block`, the fluids `creosote`/`flowing_creosote` and `creosote_bucket`, recipe type `coking`, and arm point type `coke_oven`.
- Coke oven NBT keys: `Controller`, `LastKnownPos`, `Uninitialized`, `Width`, `Height`, `Speed`, `Creosote`, `CreosoteCapacity`, `Items`, `Work`, `Progress0`–`Progress3`. Nothing is frozen until the first public release. These are the IDs **reserved** by `docs/steelworks-spec.md`.
From the first public release, everything listed here is permanent:
- **Never rename or remove** a listed ID.
- Adding is fine. Display names, models, textures and recipes may change.
- If something truly must change, keep reading the old name, convert it on load, and say so in the changelog.

Pattern borrowed from Tarbound's `dev/SAVE_FORMAT.md`.

## Registry IDs (namespace `creativeengineering`), reserved for Steelworks
| Kind | IDs |
|---|---|
| Blocks | `coke_oven_bricks`, `blast_furnace_bricks`, `blister_steel_block`, `cast_iron_block`, `steel_block`, `coke_block`, `slag_gravel`, `slag_bricks`, `treated_planks`, `treated_log`, `treated_stairs`, `treated_slab`, `steel_casing`, `cast_iron_grate`, `creosote` (fluid block) |
| Items | the block items above, plus: `carbon_packed_iron`; `blister_steel_ingot`, `_nugget`, `_sheet`; `cast_iron_ingot`, `_nugget`, `_sheet`; `steel_ingot`, `_nugget`, `_sheet`; `coal_coke`, `coke_dust`, `slag`, `creosote_bucket`; blister steel `sword`, `pickaxe`, `axe`, `shovel`, `hoe`, `helmet`, `chestplate`, `leggings`, `boots` (all `blister_steel_<tool>`) |
| Fluids | `creosote`, `flowing_creosote` (fluid type `creosote`) |
| Block entities | `coke_oven`, `blast_furnace` |
| Recipe types / serializers | `coking`, `blast_smelting` |
| Data maps | `blast_furnace_fuel` (item → iron units) |
| Tags | `creativeengineering:steel_grade`, `creativeengineering:cast_grade`, `creativeengineering:creosoted_sleepers` (empty, for the rail module) |

## Blockstate properties
| Block | Property | Values |
|---|---|---|
| `coke_oven_bricks`, `blast_furnace_bricks`, `alloy_furnace_bricks` | `port` | `bricks` / `input` / `output` (0.3.1; wrench-cycled look, default `bricks`) |
| `coke_oven_bricks`, `blast_furnace_bricks` | `lit` | `true` / `false` (true while heated; see docs/tarbound-compat.md T4) |

## Block entity NBT: `blast_furnace` (0.3.0)
Per brick: `Items` (4 slots: 0 iron in, 1 fuel, 2 metal out, 3 slag out), `Work` (2 smelting slots), `Progress0..1` (float ticks), `FuelCredit` (float iron units already paid for), `LastKnownPos`, `Controller` (non-controllers). Controller only: `Width`, `Height`, `Heat` (float, averaged burner level), `Fans` (int). Same `Uninitialized` flag as the coke oven. Blockstate `lit` / `working` as for the coke oven.

## Tier 4 IDs (0.4.0)
Block/item `alloy_furnace_bricks`; block entity `alloy_furnace`; recipe type/serializer `alloying`; arm point `alloy_furnace`.

## Block entity NBT: `alloy_furnace` (0.4.0)
`Controller` (absent = loose brick), `Size` (int[3] x,y,z), `Items` (19 slots: 0–8 ingredients, 9 fuel, 10–18 output), `FuelCredit`, `Work` (list of {`Recipe` id, `Progress`, `Consumed` item list}), `HeatAvg`, `HeatMin`, `Fans`. Only the controller (min corner) works; others keep leftovers until absorbed on the next forming.

- 0.4.2: each `alloy_furnace` brick also saves Create's `Filter` tag (FilteringBehaviour); only used while its `port` is `output`.

## Metalworking (0.5.0)
Items `rod_die`, `gear_die`, (`wire_die` 0.5.0–0.5.1 and `extrusion_die` 0.5.2 were removed in 0.5.3), `copper_rod`, `iron_rod`, `steel_rod`, `iron_gear`, `steel_gear`, `copper_wire`, `steel_wire`; recipe type/serializer `die_pressing`; item tag `creativeengineering:dies`. On Create's `mechanical_press` block entity our behaviour saves `CreativeEngineeringDie` (item stack). Removing the mod leaves that tag unread (harmless) and the die is lost.

## Treated timber (0.6.0)
Blocks/items `treated_planks`, `treated_stairs`, `treated_slab`, `treated_log`, `treated_wood`, `treated_beam` (Create girder states: `x`, `z`, `top`, `bottom`, `axis`, `waterlogged`). Item tag `creativeengineering:treated_logs`; `creosoted_sleepers` now holds the treated slab.

- 0.6.6: `alloy_furnace` no longer saves `FuelCredit`; item slot 9 is unused (legacy fuel, extract-only).

## Diesel engine (0.7.0 / 0.8.0)
Blocks `diesel_crankcase` (`facing` = output end, 0.8.0; was `axis` in 0.7.x), `diesel_cylinder` (`facing`, `running`), `diesel_flywheel` (`axis`). Block entities `diesel_engine` (Create kinetic NBT + `Running`, `Cylinders`, `Length`, `Fuel` fluid id, `BurnDebt`) and `diesel_flywheel` (plain Create kinetic). Data map `engine_fuel`; fluid tags `engine_fuels/*`. Removed in 0.7.1: `diesel_exhaust`, `diesel_exhaust_stack`; in 0.7.0 pass: `diesel_starter`.

- 0.8.1: block entity `diesel_cylinder` (no saved data; drives the rocker animation). Client config `creativeengineering-client.toml`: `visuals.animateEngines`.


## 0.9.0
- New block / BE `creativeengineering:fermenting_vat` (states top, bottom, valve, working).
  - BE NBT: Controller, Size[3], Item, Input, Product, Speed, Active, Batches, Progress.
- New fluids and buckets: ethanol, hooch, wine, cider, mead, ale (`<name>`, `flowing_<name>`, `<name>_bucket`).
- New recipe type `creativeengineering:fermenting`.

## 0.9.1
- `fermenting_vat` gains block states north / east / south / west (outer edges, for feet). They are recomputed on load.

## 0.9.4
- `fermenting_vat` no longer has the north / east / south / west states (feet removed); old worlds just drop them.
