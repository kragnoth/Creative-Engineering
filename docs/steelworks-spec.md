# Steelworks — design spec (approved 2026-11-18)

Module 1 of Creative Engineering. Read `create-core-concepts.md` first; rules 1–15 apply. Mod id `creativeengineering`, rotation (SU) only.

## 0. Create integration contract (applies to every Creative Engineering machine)
A full setup must be buildable from base Create logistics plus vanilla hoppers alone. Each machine block entity:
- **Items:** exposes a NeoForge `Capabilities.ItemHandler.BLOCK` on every face, with an *insert-only* input view and an *extract-only* output view. That covers:
  - Create: funnels, chutes and smart chutes, brass tunnels, portable storage interfaces, packagers, stockpile/threshold switches
  - vanilla: hoppers and droppers, through NeoForge's item-handler hooks
- **Belts:** adds Create's `DirectBeltInputBehaviour`, so a belt ending at the machine inserts directly, the way basins and millstones accept.
- **Mechanical arm:** registers an `ArmInteractionPointType` for the block. Create's arm has **no** generic inventory target; every supported block is listed in `AllArmInteractionPointTypes`.
- **Fluids:** exposes `Capabilities.FluidHandler.BLOCK` on every face, *extract-only* for byproducts like creosote. Works with pipes, pumps, smart pipes and item drains.
- **Information:** registers a `DisplaySource` (display links) and implements `IHaveGoggleInformation`. Comparator output is proportional to output-slot fill.
- **Wrench:** implements `IWrenchable` (rotate where it makes sense; sneak-wrench picks up a brick).
- **Multiblocks:** form through Create's `ConnectivityHandler` (vault- or tank-style), so they grow and shrink like Create's own.
- **Autocrafter-friendly** (generic, and also what Tarbound needs; see `tarbound-compat.md` T1–T7):
  - The **side-less** view is the machine's "screen" view.
  - The automation slots are **compact buffers** (input / fuel / output / slag), not the internal processing slots.
  - `isItemValid` is strict.
  - `LIT` is true while heated.
  - Every brick exposes the controller.
  - JEI and EMI categories list our bricks as catalysts, with byproducts as outputs.
  - A public `automationBlocker(expectedOutputs)` method gives "No heat", "No air blast" or "Output full".

## 1. The ladder at a glance
Three real-world materials. Each one is useful the moment you have it, and each later tier improves the earlier ones rather than replacing them.

| Tier | Process (real-world basis) | Machines | New blocks | Gives |
|---|---|---|---|---|
| 1 | **Cementation:** iron packed in carbon and baked | Press + basin (compacting), encased fan through lava or a burner (bulk blasting) | none | **Blister steel**: light steel |
| 2 | **Coking** | Coke oven battery on any heat | Coke oven bricks | **Coke** (+ optional **creosote**) |
| 3 | **Blast furnace** | Blast furnace stack on heated burners; optional fans as the air blast | Blast furnace bricks | **Cast iron** (no air) / **Steel** (air blast) + bonus slag |

**Substitution, so nothing becomes a dead end:**
- `#creativeengineering:steel_grade` = blister steel, steel. Light-steel recipes accept either.
- `#creativeengineering:cast_grade` = cast iron, steel. Heavy-part recipes accept either.
- Steel is also `c:ingots/steel`, so other mods' steel recipes see it. Blister steel and cast iron get their own `c:` tags.

## 2. Tier 1: blister steel (no new blocks)
1. **Packing** (`create:compacting`, unheated, press over basin):
   - 1 iron ingot + 2 coal *or* 2 charcoal → 1 **carbon-packed iron**
   - 2 iron ingots + 1 coke → 2 carbon-packed iron *(tier 2 upgrade: 4× less carbon)*
2. **Cementation:** a `minecraft:blasting` recipe, carbon-packed iron → 1 **blister steel ingot**.
   - Encased fan through lava or a lit blaze burner: bulk, about 150 ticks (Create's `fanProcessingTime`). This is the intended route.
   - Vanilla blast furnace: one at a time, `cookingtime` 600 (30 s). A slow fallback.
   - A regular furnace can't do it, because Create's fan reads smelting and blasting recipes and a regular furnace reads only smelting.
3. Variants: blister steel nugget, ingot, block; **blister steel sheet** (`create:pressing`).

**What blister steel unlocks right away:** coke oven bricks, blast furnace bricks, and light-steel parts for later modules (springs, rods, blades).

## 3. Tier 2: coke oven
**Block:** *Coke Oven Bricks* (crafted from bricks + blister steel sheet).

**Forming:** like Create's item vault: placed bricks join into a cuboid, 1–3 wide, 1–3 deep and 1–3 tall. Any size works. Each brick block in the oven adds **4 parallel coking slots**.

**Heat:** heat sources go directly under the bottom layer, any `BoilerHeater` (Create's API). Total heat sets speed:

| Heat under each bottom column | Speed multiplier |
|---|---|
| Passive (fire, magma, campfire) | ×0.5 |
| Heated blaze burner | ×1 |
| Superheated blaze burner | ×2 |

**Speed:** the oven's speed is the average across its bottom footprint. No heat means no coking.

**Recipe type `creativeengineering:coking`** (datapack-extensible), base time 400 ticks per slot at ×1:

| Input | Output | Creosote |
|---|---|---|
| Coal | 1 coal coke | 250 mB |
| Coal block | 1 coke block | 2,250 mB |
| Any log | 1 charcoal | 100 mB |

**Item I/O:** through any face, like a vault. Funnels, hoppers, chutes and arms insert inputs and extract outputs. Inputs and outputs use separate slots, so pulling never grabs coal.

**Creosote:** an internal tank, 1,000 mB per brick block, pumped out of any face by a mechanical pump.
- **When the tank is full, extra creosote vents as smoke and is lost. The oven never stops** (rule 10).

**Hand use (0.2.3, like Create's Millstone/Basin):**
- Empty hand takes all output, else the input, else what's mid-coking.
- Held cokeable items go in.
- A bucket takes 1000 mB of creosote through Create's FluidHelper.
- The blast furnace will do the same.

**Info:** goggles show heat, slots busy, progress and creosote. A display-link source gives coke count and creosote %.

**Visuals (revised 0.2.1, Kragnoth):**
- No texture swap.
- *Working* (heated and coking): campfire-style smoke from the top layer, a heavier puff now and then, and a crackle.
- *Heated*: embers along the bottom edge.
- *Tank full*: a dark burst when creosote is lost.
- JEI shows the heat sources and their speeds.

## 4. Tier 3: blast furnace
**Block:** *Blast Furnace Bricks* (crafted from bricks + blister steel sheet + nether brick).

**Forming:** like Create's fluid tank: a vertical stack with a square footprint of 1×1, 2×2 or 3×3, and 2–6 tall. Bigger means more parallel smelting. No hollow centre and no pattern checks.

**Heat:** at least one **heated** blaze burner under the bottom layer. Passive heat isn't enough, but plain heated is.
- Speed: each heated burner = ×1, each superheated burner = ×2, capped by footprint (one burner per bottom column counts).

**Charge:** insert from **any face** (funnel, chute, belt, hopper, arm). The top hatch is just where it looks best:
- **Iron:** iron ingot, raw iron, crushed raw iron, or nuggets (9 = 1 unit).
- **Fuel:**

  | Fuel | Iron units per fuel item |
  |---|---|
  | Coke | 2 |
  | Charcoal | 1 |
  | Coal | 0.5 (2 coal per iron, the poor rate) |

  Coal works from day one; charcoal is 2× better and coke 4× better. So the coke oven is an efficiency upgrade, not a gate (rule 11).

**Product, decided by the air blast:**

| Air blast | Product |
|---|---|
| None | **Cast iron** ingot |
| ≥1 encased fan **blowing into** a bottom-layer side face | **Steel** ingot |

- 1 iron unit → 1 ingot, matching Create's own crushed ore → 1 ingot.
- The fan works through Create's normal stress cost of 2 SU per RPM. Each extra blowing fan adds +25% speed, up to 4 fans.
- A fan at 0 RPM doesn't count.

**Slag:** 25% chance per iron unit, from raw or crushed iron only.
- Kept in its own output slot, extracted from any face.
- **When the slot is full, new slag is voided. Slag never stops the furnace.**

**Recipe type `creativeengineering:blast_smelting`** (datapack-extensible):
- ingredient
- iron units
- `result_cast`
- `result_blast`
- `slag_chance`
- `base_time`: 200 ticks per unit at ×1

**Item I/O:**
- Metal and slag come out of any face. Funnel or tunnel filters can split them onto separate belts.
- Inputs can't be extracted: the output view only shows metal and slag.

**Info:** goggles show heat, the air-blast state (*"Cast iron: no air blast"* / *"Steel: 2 fans, +25%"*), fuel units, queue and slag. A display-link source too.

**Visuals:** a lit window band glows by heat level, flame and smoke at the top, and a hot-metal glow on the output side when steel is being made.

## 4b. Tier 4: alloy furnace (REMOVED in 0.21.0)
> Kragnoth's 0.21.0 polish pass: in a Create playthrough it only remade blister steel after steel was available, and Create's heated mixer covers alloys. Block, recipe type, JEI / Jade / Ponder / Display Link / Arm support and recipes are gone. The section below is history.

**Why:** Create's mixer does one batch per basin. That's fine early on, but it doesn't scale. This is the industrial bulk route. It is expensive to build and run, so it's an upgrade, not a cheap replacement (rule 11).

**Build:** steel sheets + blast furnace bricks, so it comes after tier 3. It forms a stack like the blast furnace: 1×1 to 3×3 footprint, 2–6 tall. Every brick adds parallel batch slots.

**Running costs, all required:**
- Blaze burners underneath. Heated or superheated, per recipe, like Create's `HeatCondition`.
- **Coke** consumed per batch from a fuel slot (data map `creativeengineering:alloy_furnace_fuel`).
- At least one **encased fan** blowing into the bottom layer. More fans means faster, at their stress cost.

**Inputs:** a mixed buffer, like a basin. Batches are matched by ratio.

**Output:** the same ratios as the mixer. No bonus yield; the pay-off is throughput.

**Recipes:** JSON type `creativeengineering:alloying`. It can optionally inherit Create heated/superheated `mixing` recipes whose ingredients and results are all items and whose result is in `c:ingots` (brass and any modded alloy work in bulk automatically).

**Integration:** the same as every machine: §0 contract, hand use, Jade, JEI, Tarbound, Ponder.

**Not doing:** the arc crucible, recycling and ore doubling. Create's crushing, washing and KubeJS already cover those.

## 5. Recipes reusing Create machines (no new blocks)
| Recipe | Machine |
|---|---|
| Coke → coke dust | Milling |
| Slag → slag gravel (+ chance of iron nugget) | Crushing |
| Steel / blister steel / cast iron sheets | Pressing |
| Steel block ↔ ingots, etc. | Compacting / crafting |
| Coal coke as fuel (burn time 3,200 = 2× coal) | NeoForge furnace-fuel data map, which also makes it a blaze-burner fuel |
| Creosote → treated planks / logs | Filling (spout, 100 mB each) |
| Electrum (gold + silver), constantan (copper + nickel) | Heated mixing; *conditional*, only loads if `c:ingots/silver` / `c:ingots/nickel` exist |

## 6. What each material is for (initial sinks)
- **Blister steel:**
  - Steelworks bricks.
  - Light-steel parts used by later modules.
  - **Blister steel tools and armour** (decided, see §6a).
- **Cast iron:**
  - Cast iron block, grate, pillar and plate (decor).
  - Later modules: heavy cogs, flywheel, engine block for the Fuels & Engines module.
- **Steel:**
  - Steel casing (encasing, like Create's casings).
  - Steel block, girder and sheet.
  - Later: the Alloy Furnace (tier 4), derrick, pumpjack.
- **Creosote:** treated timber set. Creosoted track sleepers are **deferred** to a later rail & timber module, but the framework ships now (§6b).
- **Slag:** slag gravel, slag bricks.

## 6a. Blister steel tools and armour
**Historical basis.** Cementation (blister) steel is carburised bar iron, "suitable for manufacture into a variety of metal wares" (Sheffield steel-industry glossary). Uses:
- Forged into **shear steel** for edge tools: knives, shears, razors.
- Melted into **crucible steel**.
- Huntsman (1740s) went looking for something better because blister steel springs weren't uniform enough for clocks.

It is therefore an authentic 17th–18th-century, early-industrial, steampunk material: good enough for tools, blades and rough springs, not for precision work. That is exactly its role here.

**Stats (vanilla reference):**
- Tools:

  | Tier | Durability | Speed | Attack bonus | Enchantability |
  |---|---|---|---|---|
  | Iron | 250 | 6 | 2 | 14 |
  | **Blister steel** | **500** | **6.5** | **2.5** | **12** |
  | Diamond | 1,561 | 8 | 3 | 10 |

  Blister steel mines at **iron level** (`incorrect_for_iron_tool`). Repair material: blister steel ingot.
- Armour: **17 total defence** (iron 15, diamond 20), **toughness 1**, durability multiplier 22, enchantability 12.

**Flavour:** the tooltip calls them "shear-forged".

**Not included:** full steel tools/armour. That waits for a later module, or other mods' `c:ingots/steel` sets.

## 6b. Creosoted track framework (deferred content)
Ships in Steelworks:
- the creosote fluid
- the treated timber items/blocks
- an empty tag `#creativeengineering:creosoted_sleepers`

The later rail module will register a Create `TrackMaterial` that uses that tag. Nothing in Steelworks depends on it.

## 7. Content list
**Items:**
- carbon_packed_iron
- blister_steel_ingot / nugget / sheet
- blister steel sword, pickaxe, axe, shovel, hoe, plus helmet, chestplate, leggings and boots
- cast_iron_ingot / nugget / sheet
- steel_ingot / nugget / sheet
- coal_coke, coke_dust
- slag
- creosote_bucket

**Blocks:**
- coke_oven_bricks, blast_furnace_bricks (the multiblocks)
- blister_steel_block, cast_iron_block, steel_block
- coke_block, slag_gravel, slag_bricks
- treated planks / log / stairs / slab
- steel_casing, cast_iron_grate

**Fluid:** creosote.

**Recipe types:** `coking`, `blast_smelting`.

**Tags:** `steel_grade`, `cast_grade`, the `c:` ingots / nuggets / plates / storage_blocks for steel, blister_steel, cast_iron and coal_coke, plus `c:creosote`.

## 8. Ponder scenes
1. Cementation: pack iron with a press over a basin, then bulk blast it with a fan.
2. Coke oven: forming, sizes, heat sources and speed.
3. Coke oven: extracting creosote, venting when full.
4. Blast furnace: forming and heated burners.
5. Blast furnace: charging the top, fuel ratios.
6. Blast furnace: air blast with fans → steel; extra fans add speed.
7. Steel casing and the material tags (what substitutes for what).

## 9. Art list
**Art direction (Kragnoth, 2026-11-18):**
- Excalibur first: Excalibur's shapes and value structure are the base for items, tools, armour and resource shapes (ingots, nuggets, blocks).
- Immersive Engineering as loan styling: reddish treated-wood handles, steel collars and butt caps, rivets.
- Create's palette discipline for the metals: exact ramps in `tools/stylekit`.
- Generator: `tools/stylekit/make_textures_excalibur.py`. It needs an unpacked Excalibur via `EXCALIBUR_DIR`, which is never committed. Credit Maffhew (see `CREDITS.md`).

**New textures:**
- **Ingots, nuggets, sheets:**
  - blister steel: warm grey with a faint temper sheen
  - cast iron: dark, matte
  - steel: cool blue-grey
- Blister steel tools (5) and armour (4 items + the armour-layer textures), in the blister steel ramp.
- coke (porous dark grey), coke dust, slag (glassy dark green-grey), carbon-packed iron.
- **Coke oven bricks:** brick + a blister-steel door frame; active/idle glow overlay.
- **Blast furnace bricks:** dark refractory brick with a steel band; heat-glow overlay.
- Steel casing, cast iron grate, treated wood, creosote fluid (dark amber-brown).

**Reused** by reference: `create:block/brass_casing` etc. where trims fit, and Create's fan/burner visuals for the air blast.

## 10. Config (server)
- Coking base time.
- Blast base time.
- Per-fan speed bonus and fan cap.
- Slag chance.
- Creosote per coal.
- The heat-multiplier table.

## 11. Decisions log
- 2026-11-18: Cementation by fan blasting (no new block).
- 2026-11-18: Blister steel tools and armour **yes** (historically grounded).
- 2026-11-18: Creosoted track **deferred**; framework included.
- 2026-11-18: Coal allowed as blast fuel at a poor rate (0.5 iron/coal).
- 2026-11-18: All machines are fully compatible with base Create logistics and vanilla hoppers (§0).
- 2026-11-18 (0.3.0 as built): blast furnace 1–3 wide, up to 6 tall; heated Blaze Burner required (passive heat doesn't count), speed averaged over the footprint; any fan blowing into the bottom layer switches to steel, +25% per extra fan (max 4); fuel paid per item in iron units (coke 2, charcoal 1, coal 0.5); blister steel → steel at half fuel either way; slag voided when full.
- 2026-11-18 (0.3.1): furnaces use Create-style connected textures (strap frame on the outer edge of each formed multiblock). Wrench cycles a brick's look between bricks, input hatch and output hatch; cosmetic only, all faces stay full I/O.
- 2026-11-18 (0.4.0, Kragnoth): alloy furnace is any solid box 2x2x2 to 4x4x4; fans are extra speed only; three-ingredient recipes supported; Ender IO (and Ender IO: Evolution) alloy-smelter ingot recipes inherited when installed; JSON `alloying` for anything else; Create's heated mixing alloys inherited.
- 2026-11-18 (0.5.0, Kragnoth): press dies go IN the Mechanical Press (option 1): one MixinExtras @ModifyReturnValue on getRecipe, stacking with other mods; die stored as a Create behaviour; dies never wear.
- 2026-11-18 (0.5.2, Kragnoth): wire is not stamped: the Wire Die became the Extrusion Die (rod pushed through a hole into wire, 1:1). Copper rods added. A rotation-driven draw bench stays an option if wire rope grows important.
- 2026-11-18 (0.5.3, Kragnoth): no wire die at all. Wire items and art stay, without recipes, for later (wire rope for the petroleum rigs).
- 2026-11-18 (0.6.6, Kragnoth): the alloy furnace has no fuel; Blaze Burners under every column are its running cost (Create's Basin model). The blast furnace keeps burning coke (Kragnoth, 2026-11-20): coke is its fuel and carbon, and the coke oven's main payoff.

## 0.21.0 polish pass (Kragnoth, 2026-11-22: trim dead ends, integrate with Create progression)
- Removed: Alloy Furnace (block, `alloying` recipes, JEI / Jade / Ponder / Display Link / Arm point), copper and iron rods, the iron gear, copper and steel wire, cast iron sheet, coke dust (and their `c:` tags).
- Dies now make only steel rods and steel gears: the parts the diesel engine, pumpjack and probe use.
- Slag has jobs: Create crushing gives gravel + 25% iron nugget; it stands in for gravel in asphalt (`#creativeengineering:asphalt_aggregate`).
- Blister steel's role: the bootstrap steel. Coke Oven Bricks and Blast Furnace Bricks need its sheets, its tools and armour bridge iron and diamond, and the blast furnace refines it into steel.
