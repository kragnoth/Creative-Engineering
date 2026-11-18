# Creative Engineering: test checklists and ideas

Convention (borrowed from Tarbound):
- Every change is a release: bump `mod_version`, update README, `dev/SAVE_FORMAT.md` (additions) and this file.
- Add a short in-game test list for the release at the **top** of this file.
- Kragnoth builds and tests; mark items done when he reports back.

## 0.2.3-alpha: hand + bucket use (2026-11-18) - test in game
- [ ] Empty hand, right-click any brick: you get all finished output from the whole oven. With no output: the unstarted input. With neither: whatever is mid-coking (progress lost), like Create's Millstone.
- [ ] Right-click holding coal or logs: the held stack goes in (creative: the stack stays in hand). Non-cokeable items do nothing.
- [ ] Empty bucket on any brick: Creosote Bucket (1000 mB taken), with the usual Create tank sound. A filled creosote bucket can't be poured back in.
- [ ] Ponder text mentions hand and bucket use.

## 0.2.2-alpha: Jade (2026-11-18) - test in game
- [ ] `runClient` now has Jade (`dev_jade=true` in gradle.properties).
- [ ] Looking at any coke oven brick shows: size, heat (xN and source type), coking busy/total slots and progress %, In/Out items with counts, creosote mB (red with "vents" when full), and the stop reason (e.g. "No heat") in red.
- [ ] Jade config lists "Coke Oven" under the plugin, and it can be toggled.

## 0.2.1-alpha: coke oven feedback (2026-11-18) - test in game
- [ ] Bricks no longer swap texture. While working (heated + coking), the top layer gives off campfire-style smoke, a heavier puff every few seconds, and a crackle.
- [ ] Heated but idle: embers and the odd lava pop along the bottom edge only, with no smoke.
- [ ] With a full creosote tank: a dark, dense burst from the top (looks different from working smoke).
- [ ] JEI coking page: time per item, "4 at a time per brick", and a heat row (campfire x0.5, blaze burner x1, blaze cake/superheated x2) with tooltips. Checking a Blaze Burner's uses shows coking.
- [ ] Ponder scene mentions smoke vs embers.

## 0.2.0-alpha: Steelworks tier 2, coke oven (2026-11-18) - TESTED: works; feedback → 0.2.1
- [ ] 8 bricks + 1 blister steel sheet → 2 Coke Oven Bricks. Bricks form ovens like Create tanks: 1–3 wide, 1–3 tall. Goggles show the size.
- [ ] Heat under every bottom column: fire/magma/campfire ×0.5, blaze burner ×1, superheated ×2. With no heat the goggles say "No heat". (Lit-mortar glow replaced in 0.2.1.)
- [ ] Coal → coal coke + 250 mB creosote; coal block → coke block; any log → charcoal. 400 ticks per item at ×1, 4 at a time per brick.
- [ ] Funnel, hopper, chute, belt end and mechanical arm all insert from any face. Extracting only ever takes finished output (never the coal). Comparator reads output fill.
- [ ] A mechanical pump pulls creosote from any brick. With a full tank, coking continues and the top vents smoke.
- [ ] Breaking a brick drops only that brick's contents. The rest reforms, and creosote is shared out like a Create tank.
- [ ] JEI "Coking" page shows input, output and creosote, with the oven bricks as catalyst. 2 iron + 1 coke compacting now shows (our coke is `c:coal_coke`).
- [ ] Coke burns 2× coal in furnaces and blaze burners. Millstone: coke → coke dust.
- [ ] Ponder: hold W over the bricks or coke for the "Coking Coal" scene.
- [ ] Game tests: `./gradlew runGameTestServer` → 4 coke oven tests pass (forming, insert-only view, makes coke + lit, vents instead of stalling).
- [ ] Tarbound (if loaded): a Reliquary on any brick binds; the machine window shows 2 slots; a coking contract can be written from JEI.

## 0.1.0-alpha: Steelworks tier 1, blister steel (2026-11-18) - TESTED OK by Kragnoth (2026-11-18)
- [x] The mod loads with Create 6.0.10 on NeoForge 21.1.219 or newer. The Creative Engineering tab lists 14 items.
- [x] A press over a basin (unheated) turns 1 iron ingot + 2 coal → carbon-packed iron. Same with 2 charcoal.
- [x] With Immersive Engineering (or any mod tagging `c:coal_coke`): 2 iron + 1 coke → 2 carbon-packed iron. Without one, the recipe doesn't show.
- [x] A fan through lava, or a lit blaze burner, bulk-blasts carbon-packed iron on a belt or depot into blister steel. A vanilla blast furnace works too (30 s). A regular furnace doesn't.
- [x] A press turns a blister steel ingot into a sheet. Nugget ↔ ingot ↔ block crafting works.
- [x] Tools mine at iron level, last 500 uses, and say "Shear-forged blister steel" in the tooltip. They enchant and repair with blister steel ingots.
- [x] Armour gives 17 defence + 1 toughness in total, renders with the Excalibur-style layers, and can be trimmed.
- [x] Ponder: hold W over carbon-packed iron or a blister steel ingot to see the "Cementation" scene. The tag appears in the Ponder index.
- [x] The advancement tab appears once you pick up iron: Packed and Ready → Cementation → Shear-Forged.
- [x] The art reads well in game: Excalibur shapes, IE-style handles. Note anything to redraw.

## Ideas / deferred
- Display-link source for the coke oven (coke count, creosote %): next tier, along with the blast furnace.
- EMI category for coking (JEI is done; EMI shows JEI categories only through its own plugin).
- Creosoted Create track (rail & timber module): tag framework already reserved.
- Built-in resource pack variant (Excalibur style).
