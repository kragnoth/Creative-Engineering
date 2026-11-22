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
