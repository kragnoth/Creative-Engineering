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
