# Handoff — where we left off

_Last updated: 2026-11-22 by Claude (session with Kragnoth)._ Update this file at the end of every session, and republish the handoff page (`docs/handoff-page.html`) at https://claude.ai/artifact/8m4JNJxdnfpEGVC6iW5PYN (slug `3ed6bad7-81ab-4f34-ab16-efd5596dc303`).

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

**0.16.0: Pumpjack from separate parts: Crank Gearbox (kinetic, cranks) + Samson Post (2 tall, draws the beam) + Wellhead (pipe-size ports), found by position (PumpjackRig; rear arm a 1-2, reach b 1-2, stroke b/a scales draw and stress); Oritech-style outline + place-next on the gearbox, no assembly. Next: Kragnoth's in-game pass; then well casing depth / gas cap, stroke machines, drink bottles, Ponder.** **0.15.1: test `refineryRunsFast` (128 RPM) after Kragnoth saw crude stop past 16 RPM in 0.14: headless it keeps flowing; likely the 1x1 column backing up.** **0.15.0: Pumpjack split the Create way (Kragnoth: no magic multiblock): one-block kinetic Pumpjack whose beam strokes the block in front (StrokeDriven), separate Wellhead with data-driven well_sources (oil vein crude, water, desert trickle, Nether lava); rod model range fix (missing cube); distiller 'filling the boiler' status. Next: Kragnoth's in-game pass; later stroke machines (stamp mill, bellows), drink bottles, Ponder.** **0.14.0: Petroleum (docs/petroleum-spec.md): crude / diesel / kerosene / gasoline, desert crude pools, seed-derived infinite oil veins + Seismic Probe, kinetic 3-block Pumpjack (nodding linkage visual, pushes crude down pipes), crude distils into the three fuels (heat 2, 4 tall), `/test runrefinery` demo; 26 game tests. Next: Kragnoth's in-game pass, then drinkable bottles, Ponder for pumpjack/probe.** **0.13.4: Tap shaft sockets blanked to bolted plates (RecolorSpriteSource `fills`).** **0.13.3: Tap rebuilt from Create's fluid valve parts in steel (x 90 shaft-up; pointer along pipe = open).** **0.13.2: render fixes: steel tank models cutout_mipped (windows were black); Steel Pipe gets SteelPipeModel (Create's attachment logic with steel partials; arms were missing). Petroleum in progress: stash 'petroleum-wip'.** **0.13.1: Tap (gravity-feeds a level down a pipe run to tanks at or below it; PipeRuns shared with fuel lines) + Steel Pipe (Create pipe, steel, no window/encasing).** **0.13.0: Steel Storage Tank (Create tank subclass, steel), outlet couplings on the column, catwalks removed (Kragnoth), column CT fix (pointed at removed copper sheets).** **0.12.3: `/test rundistill` demo (DemoCommands, game-tested) + fix: levels push as one tank.** **0.12.2: Distilling Tank restyled (steel; Plumbing dressing drawn by visual/renderer), pump-free outlets into touching containers, Catwalks from placed Create scaffolding.** **0.12.1: vat / distiller place a whole layer like Create tanks (LayerPlacingBlockItem).** **0.12.0: Distilling Tank (docs/distilling-spec.md): copper column with greebles; drinks into spirits (biodiesel-tier) and conditional crude splits. Next: drinkable bottles with effects (drinks + spirits), then petroleum extraction.** **0.11.4: Tarbound tar = biodiesel-tier engine fuel (optional).** **0.11.3: chains anywhere in the wheel plane (any pair: 4x / 1/4 / 1:1), up to 2 chains per wheel for relays, 32 block reach. Kragnoth had meant only 2-8 blocks in any direction, but keeps it as is; no further chain features (Bits 'n' Bobs / Create Kinetics cover that).** **0.11.2: Drive Chain look: Create cogwheel sprocket + Create chain-conveyor chain (ids unchanged).** **0.11.1: game tests strip vanilla's barrier walls first (open(h)) for live demos in runClient.** **0.11.0: Drive Belt: flywheel to Drive Pulley, 4x speed, real Create kinetic link (BeltWheelBlockEntity), cosmetic belt (DriveBeltRenderer).** **0.10.0: crankcase fuel reservoirs + pump-less pulling through tanks and Create pipe runs (FuelLines).** **0.9.9: crankshaft rotates (separate partial + visual).** **0.9.8: flywheel black faces fixed (eng() gives every face an in-range UV; parts outside the block used to sample outside their sprite).** **0.9.7: Campfire Stands are fan catalysts (fan_transparent + smoking / haunting mixins).** **0.9.6: fix black campfire in stands (cutout wrapper models).** **0.9.5: Campfire Stands (andesite / brass / copper Create scaffolding holding a campfire; passive heat via BoilerHeater registry).** **0.9.4: vat restarted as Create's fluid tank, steel-shifted at load time (RecolorSpriteSource atlas source; no Create pixels shipped). Kragnoth found 0.9.0–0.9.3 too busy.** **0.9.3: vat pictures redrawn as a riveted steel tank with a copper lid (procedural per size, no tiling; Kragnoth dislikes tiled fills).** **0.9.2: vat big-picture textures (VatModel slices one picture per face size).** **0.9.1: vat connected textures, plus the instant furnace CT fix (CTRefresh refreshes neighbours' client model data).** **0.9.0 shipped the Fermenting Vat and six drink fluids** (docs/fermentation-spec.md), with headless tests 7/7. Waiting on Kragnoth's in-game test and his approval of the look.
Next pass: drinkable bottles with effects for ethanol / hooch / wine / cider / mead / ale (buckets stay; the drinks are already ethanol-tier engine fuel). Then vat Ponder, then petroleum extraction.

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
