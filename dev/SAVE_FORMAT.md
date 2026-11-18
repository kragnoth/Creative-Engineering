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
