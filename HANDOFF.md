# Handoff — where we left off

_Last updated: 2026-11-18 by Claude (session with Kragnoth)._ Update this file at the end of every session.

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

## Next goal
**Scaffold the addon and build the first module: Steelworks.** That means a coking oven, a blast furnace, slag and steel. Kragnoth asked for furnaces and the arc furnace, and the arc crucible needs steel, so Steelworks comes first.

### Blocking decisions (ask Kragnoth before coding)
- [ ] **Electricity:** stay pure SU (recommended; every module works without it) or add an FE wire layer?
- [ ] **Mod id / package:** proposal: modid `creativeengineering`, package `com.kragnoth.creativeengineering`.
- [ ] **First module:** confirm Steelworks, or start with Fuels & Engines (the diesel engine) instead.

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
4. Steelworks content (designs are on the Features tab, IE-59 coke oven and IE-61 blast furnace):
   - Coking Oven: placement-formed brick chamber heated by a blaze burner. Coal → coke plus creosote fluid.
   - Blast Furnace: fed by funnels, heated by superheated burners, encased fans act as preheaters. Steel out the front, slag out the back.
   - Recipe types `coking` and `blast_smelting`, plus heated-mixing alloys for electrum and constantan.
   - `c:ingots/steel` and `c:` tags throughout.
5. Ponder scenes and goggle info for both machines.
6. Build: `./gradlew build`. **Do not launch a client or server unless Kragnoth asks for a full compile and load-up.**

## Rules for every session
- Publish as **Kragnoth**, never his real name.
- Shell commands for his PC are **fish** syntax: CachyOS, Prism Launcher, RX 570 with 4 GB.
- Never commit third-party code or assets (Create assets are All Rights Reserved; Mojang code; IE/IP manual text). Commit only scripts that fetch them.
- "Can you do X?" means answer feasibility briefly. Simple requests mean just deliver.
- In KubeJS scripts, use `let` rather than `const` inside loops.
