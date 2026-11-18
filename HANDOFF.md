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

## Next goal
**Scaffold the addon and build the first module: Steelworks.** That means a coking oven, a blast furnace, slag and steel. Kragnoth asked for furnaces and the arc furnace, and the arc crucible needs steel, so Steelworks comes first.

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
- Shell commands for his PC are **fish** syntax: CachyOS, Prism Launcher, RX 570 with 4 GB.
- Never commit third-party code or assets (Create assets are All Rights Reserved; Mojang code; IE/IP manual text). Commit only scripts that fetch them.
- "Can you do X?" means answer feasibility briefly. Simple requests mean just deliver.
- In KubeJS scripts, use `let` rather than `const` inside loops.
