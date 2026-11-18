# Creative Engineering: save-format freeze

**Status: 0.1.0-alpha (alpha, not yet public).** IDs in use since 0.1.0: `carbon_packed_iron`, the `blister_steel_*` items and block, and the tags `steel_grade`, `cast_grade` and `creosoted_sleepers`. Nothing is frozen until the first public release. These are the IDs **reserved** by `docs/steelworks-spec.md`.
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
| `coke_oven_bricks`, `blast_furnace_bricks` | `lit` | `true` / `false` (true while heated; see docs/tarbound-compat.md T4) |
