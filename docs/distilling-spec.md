# Distilling Tank (0.12.0)

Kragnoth, 2026-11-22: "work on the distilling tank next". Chosen: **one machine for both drinks and crude**, built as a
**column on a heater**, with **greebles** ("something in between" a plain tank and the IP tower).

## Build
- Block `creativeengineering:distilling_tank`. Recipe: fluid valve over a Create fluid tank, between copper plates, over a Blaze Burner.
- Any box 1–3 wide and 2–8 tall forms one column (flood fill like the vat; min corner = controller). Shapes that aren't a box, or a single layer, just hold fluid.
- **Bottom layer = boiler** (fluid in). **Layer n above it collects fraction n-1** of the running recipe: heaviest low, lightest high. A recipe needs `fractions + 1` layers.
- Every block keeps its own 1000 mB tank, so reshaping loses nothing. Automation and buckets see one tank per layer, from any side of that layer: the boiler is fill-only (recipe inputs only, one fluid at a time), fraction layers are drain-only. Residue items (bitumen) wait in the controller and are extracted from the boiler layer.

## Heat and speed
- Same scale as the vat: campfire, Campfire Stand or unlit burner x1, lit Blaze Burner x2, superheated x3. Speed = the average over the footprint.
- A recipe's `heat` (minimum) must be met under the **whole** footprint: drinks 1, crude 2.
- Batches run as many times at once as the column has footprint blocks (fuel and output room allowing).

## Look
- Create's fluid tank, copper-shifted at load time by the `creativeengineering:recolor` atlas source (second entry in `assets/minecraft/atlases/blocks.json`: tint #F0A47A, saturation 0.35). Connected like a Create tank (VatCTBehaviour "still_tank"). No Create pixels are shipped.
- Greebles, from block states set by forming:
  - a brass tap on the middle of each outside face of every fraction layer (`tap_*`);
  - a pressure gauge (Create's boiler-gauge dial) there on the boiler instead;
  - a relief valve with a copper gooseneck vapour pipe top-middle (`valve`), puffing white smoke with a soft bubbling while working.

## Recipe type `creativeengineering:distilling`
`input` (sized fluid ingredient), `fractions` (1–7 fluid stacks, bottom to top), optional `residue` item + `chance`,
`heat` (1–3, default 1), `time` (ticks per batch at x1, default 200).

| Input (mB) | Fractions, bottom to top | Heat | Time |
|---|---|---|---|
| Hooch 250 | water 150, **vodka** 100 | 1 | 10 s |
| Wine 250 | water 170, **brandy** 80 | 1 | 10 s |
| Ale 250 | water 170, **whiskey** 80 | 1 | 10 s |
| Cider 250 | water 170, **applejack** 80 | 1 | 10 s |
| Mead 250 | water 170, ethanol 80 | 1 | 10 s |
| `#c:crude_oil` 120 (only with Immersive Petroleum) | lubricant 20, sulfur diesel 60, kerosene 40, naphtha 30; bitumen 7% | 2 | 5 s |
| CDG crude 100 (only with Create Diesel Generators and no IP) | diesel 50, gasoline 50 | 2 | 5 s |

The IP split follows the tower's ratios (doubled to whole numbers); the CDG split is provisional. Our own crude and
fractions come with the petroleum phase.

## Spirits
- New fluids `vodka`, `brandy`, `whiskey`, `applejack` (with buckets and `c:` tags). They are **biodiesel-tier engine fuel** (24,576 SU per cylinder, 1.5 mB/s), so distilling is a real fuel upgrade over the raw drinks (ethanol tier).
- Drinkable bottles with effects come in the drinks pass.

## Integrations
JEI "Distilling" (column layout with levels, minimum height and heat), Jade (every level, highlighting the one looked at, progress, why it's stopped), goggles, display links (status, progress), advancement "Fractions of a Fraction", game test `distillingTankSplitsHooch`.
