# Chemistry slice: plant oil and the Fermenting Vat (design, 2026-11-22)

## Plant oil (built, 0.8.5)
Mechanical Press over a Basin (Create `compacting`): 4 `#c:seeds` -> 250 mB Plant Oil. Tinted fluid (`ModFluids.Tinted`:
vanilla water textures + tint, runtime bucket model), tagged `c:plantoil`, in the engine's plant-oil fuel tag.

## Fermenting Vat (Kragnoth's brief, not built)
- There is no fermentation in base Create, so we add one: a **Basin-like vat with a lid and a pressure-release valve**
  (the valve is the visible "this is working" part).
- **Heat:** needs at least **passive** heat (campfire, smouldering / unlit Blaze Burner, the `create:passive_boiler_heaters`
  tag). Faster with a lit (kindled) Blaze Burner, fastest superheated. Multiple heat sources across the footprint count.
- **Multiblock like Create tanks:** vats placed together form one (same connectivity as our coke oven / Create's tank).
- **Fluids like a tank:** takes and gives liquids through pipes from any side (and items through funnels/chutes, for
  the fermentables). Bucketable.
- **Jade:** contents, progress, heat tier and speed, and why it's stopped. **Full JEI:** a fermenting category with the
  heat requirement and time, catalysts = the vat and heat sources.
- **Flavour fluids** to make the mod interesting, all tinted fluids (one line each):
  ethanol (fuel; sugar cane / sugar / beetroot / potato + water), hooch, wine, cider, mead, ale.
- Recipes are a JSON type (`creativeengineering:fermenting`) so packs add their own.

## Questions before building (for Kragnoth)
1. Drinks: fluids only (flavour, trading, maybe weak fuels), or also **drinkable bottles with small effects** (Create-style,
   e.g. filled with a Spout into glass bottles)?
2. Ingredients per drink: wine = sweet / glow berries, cider = apples, mead = honey, ale = wheat, hooch = potatoes or
   carrots, ethanol = sugar cane / beetroot. Distil hooch -> ethanol later (refining module)?
3. Vat shape limits: like Create tanks (1-3 wide, any height) or smaller (1-2 wide, up to 3 tall)?
4. Look pass first (renders for approval, like the engine), or build straight away?


## Fermenting Vat — built in 0.9.0 (defaults chosen while the questions were open; Kragnoth confirmed "rest is good", 2026-11-22)

- **Block:** `creativeengineering:fermenting_vat`. Recipe: fluid valve over a basin, between copper plates.
- **Forming:** any box of vats 1–3 wide and 1–4 tall forms one vat (flood fill, like the alloy furnace). The min corner is the controller. A shape that isn't a box leaves every block working alone.
- **Storage:** each block keeps its own ingredient slot (1 stack), a 2000 mB input tank and a 2000 mB product tank, so splitting a vat loses nothing. Automation sees one vat: 1 item slot (insert-only, ingredients only) and 2 tanks (0 = input, fill-only, recipe fluids only; 1 = product, drain-only), the same from every side. Belts feed it directly.
- **Hand use:** buckets fill / empty like a tank; any ingredient goes in; an empty hand takes the ingredients back.
- **Heat:** averaged over the footprint below the bottom layer. Campfire or unlit Blaze Burner (passive) x1, lit x2, superheated x3, nothing 0 (stops).
- **Batches:** one recipe at a time, run as many times at once as the vat has blocks (ingredients, fluid and output room allowing).
- **Look (0.9.4, restarted per Kragnoth: "take the Create tanks and colour-shift them to industrial steel, like TFMG"):** the vat is Create's fluid tank in steel. Create's `fluid_tank`, `fluid_tank_connected`, `fluid_tank_top` and `fluid_tank_top_connected` are steel-shifted at load time by the client atlas source `creativeengineering:recolor` (assets/minecraft/atlases/blocks.json: tint #C9D3DE, saturation 0.12, contrast 1.1, brightness -0.04) into `creativeengineering:block/vat_tank*`. No Create pixels ship in the jar, and resource packs that restyle Create's tanks restyle the vat too. Pack makers can tune the shift in that JSON. Connected exactly like Create's tank (VatCTBehaviour = HorizontalCTBehaviour, RECTANGLE), joined only within one formed vat. Full cubes with no feet; walls between vat blocks don't draw. One brass pressure-release valve (Create textures) top-middle. The basin, big-picture and procedural looks of 0.9.0–0.9.3 are gone.
- **Recipe type `creativeengineering:fermenting`:** `ingredients` (1–4 counted), `fluid` (sized fluid ingredient), `result` (fluid stack), `time` (ticks at x1 heat, default 400).
- **Default recipes (250 mB water each, giving 250 mB product):**

  | Product | Ingredients |
  |---|---|
  | Ethanol | 4 sugar cane, or 4 beetroot |
  | Hooch | 4 potatoes |
  | Wine | 8 sweet berries, or 8 glow berries |
  | Cider | 4 apples |
  | Mead | 2 honey bottles, 30 s |
  | Ale | 4 wheat |

- **Fluids:** ethanol, hooch, wine, cider, mead and ale, each with a bucket. All six are in the engine's ethanol fuel tier (`c:ethanol` covers ethanol).
- **Next pass (Kragnoth, 2026-11-22):** drinkable bottles with effects for each drink. The buckets stay.

## Campfire Stand (0.9.5, Kragnoth 2026-11-22)
- **Blocks:** `andesite_campfire_stand`, `brass_campfire_stand` and `copper_campfire_stand`. Each is Create's metal scaffolding model (parent `create:block/scaffold/block`, Create's per-metal textures referenced) with the vanilla campfire / soul campfire model inside. It's a full block, so a vat or boiler sits flush instead of floating. Fits base Create on its own.
- **Use:** right-click with a campfire or soul campfire to fit it (it goes in lit); empty hand takes it back. Flint and steel or a fire charge lights it; a shovel or water bottle douses it. Deployers work through the same use path. Vanilla dispensers don't (their flint-and-steel behaviour only knows real campfires).
- **Heat:** registered in Create's `BoilerHeater.REGISTRY` as passive heat while lit: Fermenting Vat x1, and Create boilers count it as a campfire. Light 15 (soul 10).
- **Effects:** crackle, embers inside, smoke venting out of open sides.
- **Drops:** itself, plus the fitted campfire item.
- **Recipe:** shapeless, the matching Create scaffolding + andesite alloy / brass sheet / copper sheet. Shown as fermenting catalysts in JEI.
- **Fan catalysts (0.9.7, Kragnoth: like Create Connected's catalysts):** stands are `create:fan_transparent`, so an Encased Fan blows straight through them. A lit regular-campfire stand turns the current into **smoking**, and a lit soul-campfire stand into **haunting**. Unlit or empty stands do neither. This allows compact cooking / haunting lines with the heat source in line with the belt. Decided per block state by two optional mixins on Create's `SmokingType` / `HauntingType.isValidAt` (require = 0; other blocks untouched).
- **Not yet:** connected scaffold textures between adjacent stands (Create's scaffold CT behaviour).

- **0.12.1:** like Create tanks, placing a vat on the top or bottom face of a formed vat wider than 1 fills that whole layer (enough items in hand; sneak for one). Shared with the Distilling Tank (`LayerPlacingBlockItem`).

## Drink bottles (0.18.0)
Kragnoth: "add a drink for each alcoholic drink". One bottle (250 mB) per drink:
- the fermented drinks: hooch, wine, cider, mead, ale;
- the distilled spirits (docs/distilling-spec.md): vodka, brandy, whiskey, applejack.

You drink it like a potion (always drinkable) and get the glass bottle back. Stacks of 16.

| Bottle | Effect | Nausea | Food |
|---|---|---|---|
| Hooch | Strength I 60 s | 10 s | 1 |
| Wine | Regeneration I 10 s | 5 s | 2 |
| Cider | Speed I 60 s | 5 s | 2 |
| Mead | Absorption I 120 s, Regeneration I 5 s | 5 s | 3 |
| Ale | Haste I 90 s | 5 s | 3 |
| Vodka | Resistance I 60 s | 15 s | 0 |
| Brandy | Fire Resistance 90 s | 15 s | 0 |
| Whiskey | Strength II 45 s | 15 s | 0 |
| Applejack | Speed II 45 s, Jump Boost I 45 s | 15 s | 0 |

- **Filling:** Create's Spout puts 250 mB of `c:<drink>` into a glass bottle. The Item Drain empties a bottle.
- **By hand:** a bucket and 4 glass bottles make 4 bottles, and the bucket comes back.
- **Look:** vanilla's potion bottle with its liquid overlay tinted from the fluid. No textures of our own, so resource
  packs (Excalibur) restyle it.
- New fluid tags: `c:hooch`, `c:wine`, `c:cider`, `c:mead`, `c:ale`.
- Advancement "One for the Road": drink any bottle.
