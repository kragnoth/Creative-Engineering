# Handoff — where we left off

_Last updated: 2026-11-18 by Claude (session with Kragnoth)._ Update this file at the end of every session, and republish the handoff page (`docs/handoff-page.html`) at https://claude.ai/artifact/8m4JNJxdnfpEGVC6iW5PYN (slug `3ed6bad7-81ab-4f34-ab16-efd5596dc303`).

## The project
**Creative Engineering**: a NeoForge 1.21.1 addon for **Create 6.0.10**. It brings Immersive Engineering and Immersive Petroleum style industry (furnaces, the arc furnace, oil and refining, fuels) into Create the Create way:
- Machines are driven by rotation (SU) with a stress cost.
- Heat comes from blaze burners.
- Items move on belts and through funnels; fluids move through pipes.
- Multiblocks form by placing their blocks (like Create's tanks and vaults), not by hitting them with a hammer.
- Every machine gets Ponder scenes and goggle tooltips.
- Art is in Create's style: models reference `create:` textures, and any new texture uses Create's exact palette ramps.

## Done
1. **Reference workspace + tools.** `tools/setup_refs.sh` fetches the pinned Create, IE and IP sources and jars. `tools/setup_server.sh` boots a headless NeoForge 21.1.267 server with all three mods and dumps the registries.
2. **Create art-style study.** See `docs/create-art-style.md` (palettes, model rules, license rule, Flywheel/partial-model conventions).
3. **Clean-room IE + IP inventory and gap analysis.** 92 features, mapped against Create.
   - Live Sheet: https://claude.ai/artifact/48UN6NEtpD5XpK1GaNhsWt
   - Repo copy: `docs/ie-ip-gap/` (xlsx + CSVs; source of truth is `tools/rows.py`).
   - Result: 35 Missing, 22 Partial, 21 Covered, 14 Skip. The Modules tab groups the gaps into 10 addon modules.
4. **Create core-concept summary + design rules** in `docs/create-core-concepts.md`: stated pillars, SU/heat/processing mechanics confirmed in source, and 8 rules for this addon.

## Released
- **0.1.0-alpha: Steelworks tier 1 (blister steel).**
  - Content: `mod/`, built with `cd mod; ./gradlew build` (compiles only). Items, gear, recipes, tags, advancements and the cementation Ponder scene.
  - Data comes from `dev/gen_data.py`; edit that, not the JSON.
  - Art comes from `tools/stylekit/make_textures_excalibur.py` (Excalibur-based, IE loan styling).
  - Test list: `dev/TODO.md`.
  - **Dev runs:** `./gradlew runClient` pulls Create 6.0.10 and JEI as runtime-only mods (`localRuntime`; never shipped). Switch the recipe viewer with `dev_recipe_viewer` = jei / emi / none. Only Kragnoth launches runs.

- **0.2.0-alpha: Steelworks tier 2 (coke oven).**
  - `content/cokeoven/`: a vault-style multiblock via Create's `ConnectivityHandler`. Each brick holds its own items; the controller holds the shared creosote tank.
  - Heat from Create's `BoilerHeater`. A compact 2-slot automation view. Belt input behaviour, an arm point type, goggles, and `automationBlocker()`.
  - JEI coking category, Ponder scene, 4 game tests.
  - Create, Flywheel and JEI API are compile-only (Create slim, non-transitive).

- **0.2.1–0.2.3:** smoke/ember feedback, JEI heat row, Jade provider, Millstone-style hand use and buckets. 0.2.3 TESTED OK.

- **0.3.0-alpha: Steelworks tier 3 (blast furnace). Built, awaiting Kragnoth's in-game test (`dev/TODO.md`).**
  - `content/blastfurnace/`: same tank-style multiblock as the coke oven (1–3 wide, up to 6 tall). Per brick: iron in, fuel, metal out, slag out, 2 smelting slots, fuel credit.
  - Heat: heated Blaze Burner only (`BoilerHeater` level ≥ 1), averaged over the footprint. Air blast: `EncasedFanBlockEntity.getAirFlowDirection()` pointing into a bottom-layer brick; any fan → `result_blast` (steel), +25% per extra fan up to 4.
  - `blast_smelting` recipe JSON: `ingredient`, `result_cast`, `result_blast`, `iron_units`, `slag_chance`, `time`. Fuel data map `creativeengineering:blast_furnace_fuel` (`iron_units`). Slag is voided when full.
  - 4-slot compact view, belt input, arm point, goggles, Jade, JEI category, Ponder scene. Cast iron + steel ingot/nugget/sheet/block, slag, Excalibur-based textures.

- **0.3.1-alpha: connected textures + wrench ports.** `client/ct/`: Create's `CTModel` + `RECTANGLE` sheets wrapped in at `ModelEvent.ModifyBakingResult` (no Registrate). Bricks connect only within one formed multiblock (`ConnectivityHandler.isConnected`). The wrench cycles the `port` blockstate (bricks / input / output); it changes the look only. Sheets come from `connected()` in `make_textures_excalibur.py`.

- **0.4.0-alpha: Steelworks tier 4 (alloy furnace).** `content/alloyfurnace/`: own forming (flood fill, any solid box 2–4 per side, min corner = controller holding all items; not ConnectivityHandler). Heated burner under every column; weakest burner = recipe tier, average = speed; fans optional +25% each. volume/2 parallel batches. Jobs from `alloying` JSON (Ender IO flat ingredient form), Create heated mixing → ingots, and `enderio:alloy_smelting` → ingots read through Ender IO's own codec (covers Ender IO: Evolution, no compile dep). JEI, Jade, Ponder, CT, wrench ports.

- **0.4.1-alpha: Create 6 integration.** `compat/create/CEDisplaySources`: Display Link sources (status, progress %/bar, output count, creosote level) via `CreateRegistries.DISPLAY_SOURCE` + `DisplaySource.BY_BLOCK`. Machines implement `content/shared/MachineReadout`. Packagers, Stock Links, Threshold Switch, Smart Observer and Frogports work through the item/fluid capabilities; alloy furnace ingredients may use up to 3 slots so multi-stack packages unpack.

- **0.4.2-alpha:** alloy furnace recipe filter: Create `FilteringBehaviour` on output-hatch bricks (Basin-style, tests the job's result); the controller gathers the filters every 20 ticks.

- **0.5.0-alpha: Metalworking (gap module 4).** `content/metalworking/`: `DieBehaviour` attached to Create's press via `BlockEntityBehaviourEvent` (saved + synced, drops on break); `PressDies` handles fitting (RightClickBlock, so deployers work) and the recipe override; `mixin/MechanicalPressRecipeMixin` is one MixinExtras `@ModifyReturnValue` on `MechanicalPressBlockEntity.getRecipe` (require=0, config not required). With a die fitted only `die_pressing` recipes run. JEI, Jade, Ponder.

- **0.5.1–0.5.3:** dies drawn as molds of their product; the wire die was tried as an extrusion die and then removed (Kragnoth). Wire items and art stay without recipes for later.
- **0.6.0-alpha: treated timber.** Creosote's first use: Spout filling (planks 125 mB, stripped logs/wood 250 mB) or 8-around-a-bucket crafting. Planks, stairs, slab, log, wood, and `treated_beam` (`content/timber/TreatedBeamBlock` extends Create's `GirderBlock` for connection logic; our own models; shafts refused). Palette ramp `W` from Kragnoth's reference photo.

- **0.6.1–0.6.6 (2026-11-18; beams and filters confirmed in game, 0.6.5–0.6.6 fuel changes awaiting test):** treated beams use Create's girder placement helper (`TreatedBeamPlacementHelper`); joist faces use a pre-rotated texture (no UV stretch); alloy furnace filters render (Create's `SmartBlockEntityRenderer` registered); bulk blister steel alloying (2 iron + 1 coke → 2); the alloy furnace has **no fuel**: burners are its running cost (slot 9 kept only for old saves). The blast furnace **keeps** burning coke (Kragnoth, 2026-11-20).

## Next goal
1. **Module 1: Fuels & Engines, the diesel engine.** 0.8.0 = it runs (headless game test passes); Kragnoth to test in game. Remaining: animated pistons (partial models), Ponder, Jade, display links. **Read `docs/engines-spec.md` first** (crankcase line, cylinders at 45°, 2 per block, tank feeds directly with no pump, crank jump-start, any-shape parts). Ask Kragnoth only the open questions listed there that he hasn't answered.
2. Carry-overs: game tests for blast and alloy furnaces, EMI categories, die shown on the press head, treated beam Ponder scene.
2. Next module (suggested): 1. Fuels & Engines (diesel engine), which unlocks petroleum.
2. Carry-overs: game tests for blast and alloy furnaces, EMI categories. Fix feedback as 0.3.x.
2. Carry-overs: blast furnace game tests (template with a heated Blaze Burner), display-link sources for both machines, EMI categories.
3. **Tier 4: the Alloy Furnace** (spec §4b, 0.4.0): steel-built bulk alloying, blaze burners + coke + fans; can read Create heated-mixing alloy recipes. The arc crucible is dropped (Create + KubeJS cover recycling and ore doubling).

Earlier goal, kept for history:
- This needs Create compiled in as a dependency (`compileOnly`), so the multiblock can use `ConnectivityHandler`, `BoilerHeater`, `DirectBeltInputBehaviour`, `IHaveGoggleInformation` and the arm-point registry.
- Note: the Registrate Maven (`maven.ithundxr.dev`) is blocked from Claude's sandbox. Use Create's jar with `transitive = false` and avoid Registrate types.

(Tier 1, blister steel, was released as 0.1.0-alpha.)

### Decisions (settled 2026-11-18 by Kragnoth)
- [x] **Power:** pure rotation (SU) only. No FE, no wires, no internal batteries.
- [x] **Mod id / package:** `creativeengineering` / `com.kragnoth.creativeengineering`.
- [x] **First module:** Steelworks.

**Read `docs/create-core-concepts.md` before designing anything.** It has Create's stated pillars, the mechanics confirmed in source, the 8 design rules, Kragnoth's **fun-first rules (9–15)**, and the revised steel ladder (§5).

- [x] **Steel route approved:** blister steel (fan-blasting cementation) → coke oven → blast furnace (cast iron; steel with a fan air blast). The design is in **`docs/steelworks-spec.md`** (draft).
- [x] **Spec approved:** blister steel tools and armour yes; creosoted track deferred (framework only); coal allowed at a poor rate; full base-Create and hopper logistics compatibility (spec §0).

### Steps
1. `bash tools/setup_refs.sh`. Also attach or obtain the NeoForge and decompiled-MC sources if you need to check signatures.
2. Create the ModDevGradle project in `mod/`:
   - NeoForge 21.1.267.
   - Create 6.0.10 as a hard dependency, plus Flywheel and Ponder as Create requires.
   - Parchment mappings.
   - Datagen for models that reference `create:` textures.
3. **Style kit** (`tools/stylekit/`):
   - Palette `.gpl` files built from the ramps in `docs/create-art-style.md`.
   - A PIL texture generator for new parts, plus an offline preview renderer.
4. **Steelworks spec: approved** (`docs/steelworks-spec.md`). Build exactly from it. Start with §0, the integration contract. The spec covers:
   - blocks
   - multiblock sizing rules
   - heat requirements
   - fan/stress numbers
   - I/O faces
   - recipes
   - Ponder scene list
   - art list

   Starting points:
   - `docs/create-core-concepts.md` §4
   - Features tab rows IE-59 (coke oven) and IE-61 (blast furnace)
   - Follow the steel ladder in `docs/create-core-concepts.md` §5. It supersedes the earlier draft, in which superheat and fans were required.
   - Recipe types `coking` and `blast_smelting`. Heated-mixing alloys (electrum, constantan). Coke as a `BlazeBurnerFuel` data-map entry. `c:ingots/steel` and `c:` tags throughout.
5. Ponder scenes and goggle info for both machines.
6. Build: `./gradlew build`. **Do not launch a client or server unless Kragnoth asks for a full compile and load-up.**

## Compat and dev tools
- **Tarbound compat:** `docs/tarbound-compat.md`. CE stays a standard machine (T1–T7, generic). Tarbound-specific adapters (A1–A4) belong in the Tarbound repo, done by soft compat. Neither mod depends on the other.
- **Dev tools** (adapted from Tarbound, MIT):
  - `dev/ponder_nbt.py`: Ponder schematics, no game needed.
  - `dev/gen_ponder_lang.py`: Ponder lang keys from the scene source.
  - `dev/make_gametest_template.py`: game-test floor (needs `nbtlib`).
- **Release conventions:**
  - `dev/SAVE_FORMAT.md` (reserved, then frozen, IDs) and `dev/TODO.md` (an in-game test list per release, newest at the top).
  - Every change is a version bump.
  - Releases go to Kragnoth as a source zip without `build/`, `.gradle/`, `run/` and `.git/`. He builds and tests.

## Rules for every session
- Publish as **Kragnoth**, never his real name.
- **Art:** Excalibur first, IE loan styling, Create palette discipline (spec §9). The Excalibur pack is never committed; credit Maffhew.
- Shell commands for his PC are **fish** syntax: CachyOS, Prism Launcher, RX 570 with 4 GB.
- Never commit third-party code or assets (Create assets are All Rights Reserved; Mojang code; IE/IP manual text). Commit only scripts that fetch them.
- "Can you do X?" means answer feasibility briefly. Simple requests mean just deliver.
- In KubeJS scripts, use `let` rather than `const` inside loops.
