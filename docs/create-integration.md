# Create integration: Ponder, item descriptions, goggles (0.20.0)

Kragnoth, 2026-11-22: "work on the next phase, the ponders, shift text, etc". This is what makes the addon feel native
to Create players.

## Ponder (hold the Ponder key over an item)
| Scene | Items | Shows |
|---|---|---|
| cementation | Carbon-Packed Iron, Blister Steel | press packing, bulk blasting |
| coke_oven | Coke Oven Bricks, Coal Coke | forming, heat, funnels |
| blast_furnace | Blast Furnace Bricks, Steel, Cast Iron | the furnace |
| alloy_furnace | Alloy Furnace Bricks | the furnace |
| press_dies | Rod / Gear Die | dies in a press |
| diesel_engine | Crankcase, Cylinder, Flywheel | building and starting |
| campfire_stand (0.20) | all three stands | fitting a campfire, passive heat, smoking / haunting fan catalyst |
| fermenting_vat (0.20) | Fermenting Vat | forming, heat tiers, ingredients, output |
| distilling (0.20) | Distilling Tank, Tap, Steel Pipe, Steel Tank | column, boiler, levels, taps, gravity feed |
| drive_chain (0.20) | Drive Chain, Drive Sprocket, Flywheel | fitting a chain, 4x step-up, relaying |
| pumpjack (0.20) | Pumpjack, Walking Beam Bearing, Wellhead, Seismic Probe | probing, building the rig, reach vs height, output |
| lubrication (0.20) | Oil Can | oiling a machine, the stress cut, filling the can |

- Schematics: `dev/ponder_nbt.py`. Scene code: `compat/ponder/CEScenes.java`. Lang: `dev/gen_ponder_lang.py`, re-run
  it after `gen_data.py`.
- All are in the "Creative Engineering" Ponder tag.

## Item descriptions ("Hold [Shift]")
- Create's `ItemDescription` is registered for every item of ours in `CEClient.clientSetup`, together with Create's
  `KineticStats`, so kinetic blocks also show their stress lines.
- Text lives in `dev/gen_data.py` (`TIPS`): `<id>.tooltip.summary`, `.conditionN` / `.behaviourN` for what it does,
  `.controlN` / `.actionN` for how to use it.
- _Underscored_ words are highlighted.
- Covered: every machine, block and tool, plus asphalt shapes, the campfire stand variants and the drink bottles.
- Plain materials (ingots, nuggets, sheets) have none, as in Create.

## Goggles
- Have goggle lines: Coke Oven, Blast Furnace, Alloy Furnace, Diesel Engine, Fermenting Vat, Distilling Tank,
  Pumpjack and Walking Beam Bearing (rig, stroke, missing parts), Wellhead (source, rate, tank), any lubricated Create
  machine.
- Tap (0.20): open / closed / held by redstone, what it pours, or why it doesn't.
- Steel tanks use Create's own tank goggles.

## Not yet
- Display Link sources for the newer machines.
- Mechanical Arm points for the vat and distiller.
- A Jade pass over the petroleum blocks.
- Ponder for asphalt / paraffin; recipe viewers cover those.
