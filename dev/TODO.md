# Creative Engineering: test checklists and ideas

Convention (borrowed from Tarbound):
- Every change is a release: bump `mod_version`, update README, `dev/SAVE_FORMAT.md` (additions) and this file.
- Add a short in-game test list for the release at the **top** of this file.
- Kragnoth builds and tests; mark items done when he reports back.

## 0.1.0-alpha: Steelworks tier 1, blister steel (2026-11-18) - test in game
- [ ] The mod loads with Create 6.0.10 on NeoForge 21.1.219 or newer. The Creative Engineering tab lists 14 items.
- [ ] A press over a basin (unheated) turns 1 iron ingot + 2 coal → carbon-packed iron. Same with 2 charcoal.
- [ ] With Immersive Engineering (or any mod tagging `c:coal_coke`): 2 iron + 1 coke → 2 carbon-packed iron. Without one, the recipe doesn't show.
- [ ] A fan through lava, or a lit blaze burner, bulk-blasts carbon-packed iron on a belt or depot into blister steel. A vanilla blast furnace works too (30 s). A regular furnace doesn't.
- [ ] A press turns a blister steel ingot into a sheet. Nugget ↔ ingot ↔ block crafting works.
- [ ] Tools mine at iron level, last 500 uses, and say "Shear-forged blister steel" in the tooltip. They enchant and repair with blister steel ingots.
- [ ] Armour gives 17 defence + 1 toughness in total, renders with the Excalibur-style layers, and can be trimmed.
- [ ] Ponder: hold W over carbon-packed iron or a blister steel ingot to see the "Cementation" scene. The tag appears in the Ponder index.
- [ ] The advancement tab appears once you pick up iron: Packed and Ready → Cementation → Shear-Forged.
- [ ] The art reads well in game: Excalibur shapes, IE-style handles. Note anything to redraw.

## Ideas / deferred
- Creosoted Create track (rail & timber module): tag framework already reserved.
- Built-in resource pack variant (Excalibur style).
