# Creative Engineering: test checklists and ideas

Convention (borrowed from Tarbound):
- Every change is a release: bump `mod_version`, update README, `dev/SAVE_FORMAT.md` (additions) and this file.
- Add a short in-game test list for the release at the **top** of this file.
- Kragnoth builds and tests; mark items done when he reports back.

## 0.6.3-alpha: visible alloy furnace filters (2026-11-18) - look in game
- [ ] On an output-hatch brick with a filter set, the filter item now renders over the hatch on each side (like a Basin's), sitting just proud of the face. Empty filters show nothing until you look at the slot.

## 0.6.2-alpha: beam bolts no longer stretch (2026-11-18) - look in game
- [ ] Joists along X and Z: bolts are square 2x2 heads on every face (no smeared streaks on tops or sides); grain runs along the beam. Posts unchanged.

## 0.6.1-alpha: beam placement helper (2026-11-18) - test in game
- [ ] Holding a Treated Beam and looking at a placed beam shows Create's ghost preview; right-click extends the run in the direction you're facing (along X/Z for joists, up/down for posts), same reach and feel as Create's Metal Girders.
- [ ] Doesn't affect Create's own girders; a shaft still can't go into a treated beam.

## 0.6.0-alpha: treated timber (2026-11-18) - test in game
- [ ] Spout + creosote (from the coke oven): planks on a belt/depot → Treated Planks (125 mB); stripped logs → Treated Log, stripped wood → Treated Wood (250 mB). JEI shows them under Create's Filling.
- [ ] Early game: 8 planks / stripped logs / stripped wood around a Creosote Bucket → 8 treated (empty bucket returned).
- [ ] Treated log → 4 planks; 4 logs → 3 wood; stairs and slabs (double slab drops 2). Axe mines them. Treated planks work in vanilla plank recipes.
- [ ] Colours: deep oxblood-brown with warm orange grain; near-black, sooty end grain.
- [ ] Treated Beam (treated log, iron nugget, treated log → 4): place in a line → joists along X / Z; alone or stacked → an 8x8 post; touching a block above/below grows stubs; crossing beams get a bolster. Connects to Create Metal Girders. Bolt pairs show on posts and joists.
- [ ] A Shaft can NOT be put into a treated beam (would turn it into Create's metal girder).
- [ ] Advancement "Smells Like a Railway".

## 0.5.3-alpha: wire die removed (2026-11-18) - test in game
- [ ] No Extrusion/Wire Die anywhere (JEI, creative tab, recipes). Copper and Steel Wire items still exist (creative tab) with no recipe: kept for later (wire rope).
- [ ] Rod Die still makes copper, iron and steel rods; Gear Die unchanged.

## 0.5.2-alpha: Extrusion Die (2026-11-18) - test in game
- [ ] The Wire Die is now the Extrusion Die (8 steel sheets around string): a die block with a round hole and wire coming out. Old `wire_die` items from 0.5.0 are gone.
- [ ] Rod Die now also makes Copper Rods (copper ingot → 2).
- [ ] Extrusion Die: copper rod → copper wire, steel rod → steel wire (1 each, so still 2 wire per ingot). Ingots no longer make wire directly.
- [ ] JEI shows the rod → wire chain; Ponder text updated.

## 0.5.1-alpha: die art (2026-11-18) - look in game
- [ ] Dies are steel molds with the product's shape sunk into the face (rod groove, gear pocket with a centre pin, wire coil channel). Check they read at inventory size and in hand.

## 0.5.0-alpha: Metalworking, press dies (2026-11-18) - test in game
- [ ] Dies: 8 steel sheets around a stick / cogwheel / string → Rod / Gear / Wire Die.
- [ ] Right-click a Mechanical Press with a die: "Rod Die fitted", a click sound; right-click with another die swaps (old one back to you). Sneak-right-click with an empty hand removes it. A Deployer can fit dies too. Breaking the press drops the die.
- [ ] With a Rod Die: iron/steel ingot → 2 rods. Gear Die: iron/steel sheet → gear. Wire Die: copper/steel ingot → 2 wire. Works on a Depot, on a belt, and on items dropped under the press.
- [ ] With a die fitted, plain pressing stops (a steel ingot under a Rod Die does NOT become a sheet). Without a die the press is normal again (sheets, sequenced assembly).
- [ ] Jade on the press shows "Die: …". JEI "Die Pressing" page; checking a die's uses shows its recipes.
- [ ] Ponder "press_dies" from any die. Advancement "Die Cast".
- [ ] Mixin check: the log has no Mixin errors for `creativeengineering.mixins.json`. Test alongside any other press addon in your pack.

## 0.4.2-alpha: alloy furnace recipe filter (2026-11-18) - test in game
- [ ] Wrench an alloy furnace brick to the output-hatch look: a Create filter slot appears over the hatch on its sides (other looks show none).
- [ ] Set it to an ingot: only alloys with that result start (e.g. brass vs another copper+zinc alloy). A List or Attribute Filter works too. Two hatches with different filters allow either.
- [ ] Clear every filter, or wrench the hatch back to bricks: everything runs again. Goggles and Jade show "Recipe filter: N output hatch(es)".
- [ ] Batches already running keep going; the filter only decides what starts next. Ponder scene ends with the filter step.

## 0.4.1-alpha: Display Links + Create 6 logistics (2026-11-18) - test in game
- [ ] Display Link on any brick of a coke oven, blast furnace or alloy furnace offers: Machine Status (Working / Idle / the stop reason), Machine Progress (percent or progress bar, like Create's fill level), Finished Output (count). The coke oven also offers Creosote Level. Try a Display Board and a sign.
- [ ] Threshold Switch on a furnace reads its items (outputs and inputs) and the coke oven's creosote.
- [ ] Create 6 packager on a furnace: packages finished output. Packages unpacked into the alloy furnace: a package of 3 stacks of copper fits (one ingredient may now use up to 3 of the 9 slots).
- [ ] Stock Link / Factory Gauge / Frogport chains that feed or empty a furnace through a packager.
- [ ] Smart Observer and Smart Chute filters see furnace contents.

## 0.4.0-alpha: Steelworks tier 4, alloy furnace (2026-11-18) - test in game
- [ ] Recipe: 4 steel sheets + 4 blast furnace bricks + a vanilla blast furnace → 4 Alloy Furnace Bricks.
- [ ] Forming: any SOLID box, 2–4 on each side (2x2x2, 2x3x4, 4x4x4…) forms with an anvil clunk; the strap frame wraps the whole box. 1-thick walls, holes, L-shapes or anything over 4 stay loose bricks (each framed alone; goggles "Not formed"). Breaking a brick unforms it; replacing it reforms with items intact.
- [ ] Heat: a heated Blaze Burner under EVERY bottom column. All superheated → superheated recipes too; speed = average (x1–x2).
- [ ] Fans blowing into the bottom layer: optional, +25% speed each (max 4). Jade shows fans and speed.
- [ ] Batches: volume/2 at once (2x2x2 = 4, 4x4x4 = 32). Fuel: coke = 2 batches, charcoal 1, coal ½.
- [ ] Create brass (copper + zinc, heated) shows in JEI "Alloy Furnace" as "from create" and runs.
- [ ] With Ender IO (and Ender IO: Evolution): its alloy-smelter ingot recipes appear (from enderio / evolution's namespace) and run; 10000+ FE ones need superheated. Three-ingredient ones (dark steel, energetic, vibrant) work.
- [ ] With Immersive Engineering: electrum and constantan alloying recipes appear.
- [ ] Automation: funnels/hoppers/chutes/belts/arms insert ingredients (one slot per kind, 9 kinds) and fuel from any face; extraction only gives output. Comparator reads output fill. Loose bricks expose nothing.
- [ ] Hand: ingredient/fuel in hand goes in; empty hand takes output, then ingredients, then fuel.
- [ ] Breaking the controller (lowest corner) drops all items, including ingredients in running batches.
- [ ] Wrench ports and connected textures as on the other furnaces. Ponder "alloy_furnace" scene forms in the preview.

## 0.3.1-alpha: connected textures + wrench ports (2026-11-18) - test in game
- [ ] Coke ovens and blast furnaces draw a riveted metal strap only around the OUTER edge of each formed multiblock (Create tank/vault style). Check 1x1, 2x2x3 and 3x3x6: interior bricks are seamless, corners get corner plates, top face is framed too.
- [ ] Two separate furnaces touching stay two framed boxes (connections only within one formed multiblock). Breaking a brick reframes the pieces.
- [ ] Wrench (right-click) a brick: bricks → input hatch (barred grate) → output hatch (glowing tap hole with lip) → bricks, with Create's wrench sound. Sneak-wrench still picks it up. Look only: every brick still takes input/gives output on every face.
- [ ] Item in hand / JEI shows the framed single block. Ponder scenes still render (CT in Ponder).
- [ ] Existing 0.3.0 worlds load (new `port` blockstate defaults to bricks).

## 0.3.0-alpha: Steelworks tier 3, blast furnace (2026-11-18) - test in game
- [ ] Recipe: bricks + nether brick + blister steel sheet → 2 Blast Furnace Bricks. Stack them 1–3 wide, up to 6 tall; goggles show the size.
- [ ] Heat: a **heated** Blaze Burner under the bottom layer (magma/campfire don't count). Superheated = x2 (averaged over the footprint). No heat → goggles/Jade "No heat".
- [ ] Fuel: coke smelts 2 iron, charcoal 1, coal ½ (data map `blast_furnace_fuel`). No fuel → "No fuel".
- [ ] No fans: iron ingot / raw iron / crushed raw iron → Cast Iron Ingot. Iron block → cast iron block (9 fuel units).
- [ ] An encased fan next to the bottom layer, blowing **in**: same inputs make Steel. 2–4 fans: +25% each past the first (Jade "Speed").
- [ ] Blister steel → steel at half fuel, with or without fans.
- [ ] Raw/crushed iron: 25% slag. Fill the slag slot: the furnace keeps going (slag voided).
- [ ] Funnels/hoppers/chutes/belts/arms insert iron and fuel from any face (each goes to its slot); extraction gives metal, then slag. Comparator reads the metal output.
- [ ] Hand: right-click with iron or fuel puts it in; empty hand takes metal, then slag, then input, then fuel, then mid-smelt items.
- [ ] Working: flames + smoke from the top, crackle; heated idle: embers only.
- [ ] Cast iron/steel nugget ↔ ingot ↔ block crafting; press ingots into sheets. Gear recipes accept steel (`steel_grade`).
- [ ] JEI "Blast Furnace" page: cast vs steel output, slag %, burner/fan/fuel row with tooltip. Jade shows size, air, speed, slots %, in/fuel, metal/slag, stop reason.
- [ ] Ponder `blast_furnace` scene on the bricks, steel and cast iron.
- [ ] Advancements "Pig Iron and Air" and "Proper Steel".
- Not done yet (carry over): blast furnace game tests (need a Blaze Burner set to heated in the template), display-link sources, EMI category.

## 0.2.3-alpha: hand + bucket use (2026-11-18) - TESTED OK by Kragnoth
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
