# Distilling Tank (0.12.0)

Kragnoth, 2026-11-22: "work on the distilling tank next". Chosen: **one machine for both drinks and crude**, built as a
**column on a heater**, with **greebles** ("something in between" a plain tank and the IP tower).

## Build
- Block `creativeengineering:distilling_tank`. Recipe: fluid valve over a Create fluid tank, between copper plates, over a Blaze Burner. Places whole layers like Create tanks (0.12.1).
- Any box 1–3 wide and 2–8 tall forms one column (flood fill like the vat; min corner = controller). Shapes that aren't a box, or a single layer, just hold fluid.
- **Bottom layer = boiler** (fluid in). **Layer n above it collects fraction n-1** of the running recipe: heaviest low, lightest high. A recipe needs `fractions + 1` layers.
- Every block keeps its own 1000 mB tank, so reshaping loses nothing. Automation and buckets see one tank per layer, from any side of that layer: the boiler is fill-only (recipe inputs only, one fluid at a time), fraction layers are drain-only. Residue items (bitumen) wait in the controller and are extracted from the boiler layer.

## Heat and speed
- Same scale as the vat: campfire, Campfire Stand or unlit burner x1, lit Blaze Burner x2, superheated x3. Speed = the average over the footprint.
- A recipe's `heat` (minimum) must be met under the **whole** footprint: drinks 1, crude 2.
- Batches run as many times at once as the column has footprint blocks (fuel and output room allowing).

## Look (0.12.2, restyled; Kragnoth: "steel tank look, with proper greebling, like industrial pipes routing around")
- The vat's steel Create tank (load-time recolour of Create's fluid tank), connected like a Create tank. The 0.12.0 copper body, taps and side gauge are gone.
- **Dressing**, drawn by the client from the formed column (`Plumbing.java`; static Flywheel instances, fallback renderer). Block states would have needed about 65k combinations.
  - Create copper pipe lanes run up the outside, against the left or right edge of chosen face blocks. They bend into the wall at the boiler and over the roof edge at the top.
  - Header pipes cross some faces, off the block seams.
  - Brass collars every third level, and a gauge high up on the north face.
  - The relief valve with its copper gooseneck sits top-middle (vapour puffs while working).
  - The routing is seeded by the column's position and size: every tower looks plumbed by hand and always the same. At least two faces get a lane.

## How to build
1. Lay the footprint as heat (Blaze Burners, lit for crude; campfires or Campfire Stands for drinks).
2. Place Distilling Tanks on it and keep placing on top. Like Create tanks, one placement fills the whole layer (sneak for one). Up to 8 tall; drinks need 3, IP crude 5.
3. The dressing appears by itself once the column is 2+ tall.
4. Fill the boiler (bottom level) by pipe or bucket from any side.

## Demo: `/test rundistill`
Op level 2, in any world with cheats. It clears a space a few blocks in front of you and builds a running example:
- **Input:** a Create creative fluid tank of hooch -> pipe -> mechanical pump (cogwheel on a creative motor) -> the boiler.
- **Column:** 2x2x4 Distilling Tank on four lit copper Campfire Stands.
- **Outputs:** a Tap on level 1 feeds water along Steel Pipe into a Steel Storage Tank at its height; a Tap on level 2 drops vodka down Steel Pipe into a 2-high tank on the floor. No output pumps. Signs label each part.
- The game test `distillDemoRuns` builds the same thing and checks both storage tanks fill (`DemoCommands.buildDistiller`).

## Steel Storage Tank (0.13.0; Kragnoth: "work in the extra storage tanks as a new tank type and make it look better when combined")
- Block `creativeengineering:steel_tank`: Create's own fluid tank under its own block entity type. It does everything Create's tank does:
  - merges into 1–3 wide, tall multiblocks; windows that the wrench toggles; fluid shown inside; boiler use.
- The look is the column's steel: Create's tank, window and inner sheets recoloured at load time. The models are Create's tank models with the textures re-pointed, rendered cutout like Create's so the windows show the fluid (0.13.2). No Create pixels ship.
- Recipe: a Create fluid tank + an iron sheet (shapeless).
- Joining the column: wherever any fluid container touches a fraction level, the column draws a bolted flange coupling at the join (outlet bits computed on the server, synced, drawn with the rest of the dressing).
- Catwalks were tried in 0.12.2 and removed in 0.13.0 (Kragnoth: unneeded).

## Tap and Steel Pipe (0.13.1; Kragnoth: "a tap block placed before a pipe ... any tank connected by pipe ... below or same level ... fills automatically")
- **Tap** (`creativeengineering:tap`; recipe: Create fluid valve + brass sheet). Placed against a fraction level (its back on the column; the pipe leaves from its front).
  - While open and not powered by redstone, it gravity-feeds that level's fluid down the attached pipe run: 50 mB every 4 ticks (250 mB/s), shared between every tank the pipe enters at the tap's height or lower. Tanks above the tap are skipped.
  - The run is followed by `PipeRuns` (shared with the engine's fuel lines): Create pipes, glass and encased pipes and Steel Pipe, up to Create's pump range; closed valves and pumps end a branch.
  - Look (0.13.3; Kragnoth: "follow the theming" of Create): Create's own fluid valve in the Steel Pipe's steel, laid with its shaft up. The pipe runs through it along the tap, the glass dial and pointer lever are on top, Create's pipe rim sits on the column and Create's pipe arm leads to the line. Models are Create's re-pointed at load-time steel recolours of its valve sheets. 0.13.4: the tap has no shaft, so Create's two side shaft sockets are blanked into bolted cover plates (the recolour source's `fills` paint them flat at load time).
  - Right-click opens or closes it: the lever lies along the pipe when open and across it when shut, like a ball valve. Redstone closes it. Drips and a soft trickle show while it flows.
  - Its front shows an empty fluid handler so pipes connect. On the boiler level it does nothing (the boiler can't be drained). Against any other container it works as a plain gravity tap.
  - Fluid moves straight from the level into the tanks: no flow animation inside the pipes.
- **Steel Pipe** (`creativeengineering:steel_pipe`; recipe: 2 Create fluid pipes + iron sheet -> 2). Create's fluid pipe under its own block entity type, re-skinned steel by the load-time recolour.
  - Connects and carries flow exactly like Create's: pumps, valves, taps.
  - No window form (the wrench does nothing; Kragnoth: these "won't have windows"), and it can't be encased.
  - Drawn like Create's pipe: `SteelPipeModel` adds arms / rims / drains per side from steel children of Create's attachment partials (0.13.2).

## Getting the fractions out (Kragnoth: "each layer easily pumped and put into storage tanks")
- **No pump needed:** every fraction level pushes its fluid, as one tank (100 mB every 5 ticks per touching face), into any container touching its outside faces. A Create fluid tank against level 2 fills with level 2's fraction. Another column's boiler counts, so distillations can be chained. The boiler never pushes.
- **Pumped lines:** Create pipes connect to every side of every level; a pump pulls that level's fraction.
- No other mods needed: Create's own pipes, pumps and tanks do it.

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
JEI "Distilling" (column layout with levels, minimum height and heat), Jade (every level, highlighting the one looked at, progress, why it's stopped), goggles, display links (status, progress), advancement "Fractions of a Fraction", game test `distillingTankSplitsHooch` (also checks the pump-free outlet into a Steel Storage Tank and the coupling at the join).

## 0.21.0 polish
- Distilling Tank recipe: copper sheets, a Fluid Tank and a Fluid Valve; no Blaze Burner, so drinks distil early on campfire heat. Crude oil still needs a lit Blaze Burner under the column.
- Tap recipe: Fluid Valve + copper sheet (was brass), the same copper tier as the column.
- 0.21.1: the copper pipe lanes, header bands and the valve gooseneck are gone (Kragnoth: they looked silly). The column keeps its brass collars, the gauge, the relief valve and the bolted couplings at the fraction tanks.
