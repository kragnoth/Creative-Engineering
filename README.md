# Creative Engineering

A planned NeoForge 1.21.1 addon for **Create 6.0.10**. It brings Immersive Engineering and Immersive Petroleum style industry (coke ovens, blast furnaces, an arc crucible, oil extraction and refining, liquid-fuel engines) into Create, built the Create way.

By Kragnoth.

| Path | What |
|---|---|
| `HANDOFF.md` | Current status, next goal, open decisions: **start here** (also published as a [Claude page](https://claude.ai/artifact/8m4JNJxdnfpEGVC6iW5PYN)) |
| `docs/ie-ip-gap/` | IE + IP vs Create gap worksheet (xlsx + CSV), method and versions |
| `docs/create-core-concepts.md` | Create's core concept (stated and confirmed in source) + the design rules this addon follows |
| `docs/steelworks-spec.md` | Module 1 design: blister steel → coke oven → blast furnace (cast iron / steel) |
| `docs/create-art-style.md` | Create's palettes, model and code conventions, asset-license rule |
| `docs/registry-dumps/` | Registry IDs from a headless server running Create + IE + IP |
| `docs/tarbound-compat.md` | How CE stays compatible with Kragnoth's Tarbound autocrafting, without design concessions |
| `dev/` | Ponder / game-test generators, `SAVE_FORMAT.md`, `TODO.md` (release test lists) |
| `mod/` | The NeoForge 1.21.1 mod (ModDevGradle). Build: `cd mod; ./gradlew build`. Play-test: `./gradlew runClient` (Create, JEI and Jade pulled in for dev runs; `-Pdev_recipe_viewer=emi` or `none` to switch the viewer; `-Pdev_jade=false` drops Jade) |
| `CREDITS.md` | Excalibur (Maffhew, used with permission), Create, Tarbound |
| `tools/` | Reference setup, headless server, palette/model/recipe/config analysis, worksheet build |

## Quick start (bash; from fish run `bash tools/...`)
```
bash tools/setup_refs.sh        # pinned Create/IE/IP sources + jars into ~/ref
bash tools/setup_server.sh      # optional: headless server + registry dumps (Java 21)
bash tools/config_defaults.sh   # IE/IP config defaults
```

Third-party sources, jars and assets are never committed. The scripts fetch the exact pinned versions.

## For pack makers

Everything is data-driven, so datapacks or KubeJS (`event.custom({...})`) can add, remove or change recipes.

**Coking** (Coke Oven), `creativeengineering:coking`:
```json
{ "type": "creativeengineering:coking",
  "ingredient": { "item": "minecraft:coal" },
  "result": { "id": "creativeengineering:coal_coke", "count": 1 },
  "creosote": 250,
  "time": 400 }
```
- `creosote` is the mB of creosote oil per item. It's optional and defaults to 0. When the oven's tank is full, the rest is vented.
- `time` is ticks per item at ×1 heat. It's optional and defaults to 400.
- Speed by heat source: passive heat ×0.5, blaze burner ×1, superheated ×2.

**Other hooks:**
- **Cementation** is a plain `minecraft:blasting` recipe (carbon-packed iron → blister steel). Create's fan runs it in bulk.
- **Packing** is `create:compacting`.
- **Coke as fuel** is set in the NeoForge `furnace_fuels` data map, which Create's blaze burners also read.
- **Heat sources** are anything registered with Create's `BoilerHeater` API, or in `#create:passive_boiler_heaters`.
- **Tags:**
  - `c:coal_coke` (any mod's coke works)
  - `c:creosote` (fluid)
  - `creativeengineering:steel_grade` and `creativeengineering:cast_grade` (higher grades substitute for lower ones)
- **Coming with tier 3:** `creativeengineering:blast_smelting` and the `creativeengineering:blast_furnace_fuel` data map.
