# Create's core concept: what Creative Engineering must respect

Sources: Create's README and Modrinth page (stated intent), and Create 6.0.10 source at tag `mc1.21.1-6.0.10` (what it actually does). File paths below are relative to `src/main/java/com/simibubi/create/`.

## 1. What Create says it is
- **"Aesthetic Technology that empowers the Player."** (Modrinth tagline)
- Tools and blocks for **"Building, Decoration, and Aesthetic Automation."** (README)
- **Player choice:** the tech is "designed to leave as many design choices to the player as possible."
- **No GUIs:** "the game isn't played inside a bunch of UIs". Instead you set up "contraptions of animated components working together in many possible arrangements."
- **Ponder:** visual in-game documentation guides you through every mechanic.

**In one line:** visible, animated, physical machines that you arrange yourself in the world, explained by Ponder rather than by menus or a manual.

## 2. What the source shows

### Rotation is the only power (the kinetic network)
- **Speed (RPM):** set by sources, changed by cogs, gearboxes and belts. Config defaults:
  - maximum 256 RPM (`maxRotationSpeed`)
  - "medium" from 30 RPM, "fast" from 100 RPM
- **Stress (SU) is per RPM:**
  - a consumer uses `impact × |RPM|`
  - a source provides `capacity × |generated RPM|`
  - Both come from `KineticNetwork.getStressMultiplierForSpeed` = `Math.abs(speed)`.
  - So running faster costs proportionally more; speed is never free.
- **Overstress:** a network whose stress exceeds its capacity stops entirely (`isOverStressed`). It does not slow down.
- **Default impacts** (SU per RPM, `AllBlocks` via `CStress.setImpact/setCapacity`):

  | Consumer | Impact |
  |---|---|
  | Crushing wheel, mechanical press | 8 |
  | Millstone, mixer, pump, drill, saw, deployer, bearings, pulleys | 4 |
  | Fan, crafter, arm, ejector | 2 |
  | Chain conveyor | 1 |

- **Default capacities** (SU per RPM):

  | Source | Capacity |
  |---|---|
  | Hand crank, valve handle | 8 |
  | Water wheel | 32 |
  | Large water wheel | 128 |
  | Windmill bearing (scales with sails) | 512 |
  | Steam engine (scales with boiler level) | 1,024 |

- Players see this on the stressometer, the speedometer and goggle tooltips.

### Heat is a block, not a number in a GUI
- **Blaze burner heat levels:** `NONE → SMOULDERING → FADING → KINDLED → SEETHING`.
- **Recipe heat conditions** (`HeatCondition`):

  | Condition | Accepts |
  |---|---|
  | NONE | anything |
  | HEATED | FADING or above |
  | SUPERHEATED | SEETHING only (needs blaze cake) |

- **Passive heaters:** the block tag `create:passive_boiler_heaters` (fire, magma and so on) gives a basin only SMOULDERING heat. That is not enough for HEATED.
- **Boilers** (`BoilerData`): up to **18** levels, capped by three things:
  - size: tank blocks ÷ 4
  - water supply
  - heat: a burner gives +1 when heated and +2 when SEETHING; passive heat counts as 1
- **Public API:** `api/boiler/BoilerHeater` lets addons register their own blocks as heaters. Burner fuels are a data map (`BlazeBurnerFuel(burnTime)`).

### Processing happens in the world, on things you can see
- **Item processing is mostly a block acting on what is in front of it.** Kinetic blocks act on items on belts and depots or in basins:
  - press → pressing; press over basin → compacting
  - mixer over basin → mixing
  - crushing wheels and millstone → crushing/milling
  - saw → cutting
  - deployer → deploying and item application
  - spout → filling; item drain → emptying
- **Bulk processing:** an encased fan through a catalyst block treats everything in its air stream. There are 4 types: washing (splashing), smoking, blasting and haunting.
- **Multi-step parts:** sequenced assembly runs one item through press → deploy → spout → … as a chain of in-world steps. It replaces blueprints and crafting GUIs.
- **17 recipe types:** basin, compacting, conversion, crushing, cutting, deploying, emptying, filling, haunting, item_application, mechanical_crafting, milling, mixing, pressing, sandpaper_polishing, sequenced_assembly, splashing.
- **Few GUIs:** Create registers only **14 menus** (filters, schematics, toolbox, logistics), against IE's **34**. Machines don't open screens; they are configured in-world with the wrench, value boxes and filters on the block face.

### Everything is modular and physical
- **Multiblocks form by placement and grow by adding blocks:** fluid tanks, item vaults, boilers (tank + engines + burners). There is no hammer step and no fixed structure (`api/connectivity/ConnectivityHandler`).
- **Logistics are visible:** belts, funnels, tunnels, chutes, mechanical arms, ejectors, chain conveyors, and in 6.0 packages, frogports and stock tickers. Fluids move through pipes driven by mechanical pumps.
- **Contraptions:** any structure can be glued and moved by bearings, pistons, pulleys, gantries, carts or trains, and keeps working while it moves. There is a public API for moving behaviours and mounted storage.

### Information comes from the world
- Engineer's Goggles tooltips (`IHaveGoggleInformation`).
- Value boxes on block faces; the wrench rotates and configures (`IWrenchable`, ~45 implementors).
- Display links and stockpile switches turn machine state into signs and redstone.
- About 161 Ponder scenes: every block is taught by an animated diorama.

### Rendering and art
- Moving parts are partial models animated by Flywheel instancing: `SingleAxisRotatingVisual.shaft` / `.of(partial)`.
- Art follows tight shared palettes and chunky, simple models. See `create-art-style.md`.

## 3. Rules for Creative Engineering (derived)
1. **Rotation is the only power.** Machines declare a stress impact. Speed scales throughput and cost. No FE, no wires, no internal batteries. *(Decided 2026-11-18.)*
2. **Heat comes from blaze burners.** Use `HeatCondition` and burner `HeatLevel`. Register new heat sources through the `BoilerHeater` API rather than inventing a parallel heat number.
3. **No machine GUIs.** I/O happens through faces, funnels, belts, pipes and arms. Configuration uses the wrench, value boxes and filters. A screen is allowed only where Create also uses one (filters, schematics).
4. **Multiblocks form by placement,** the way tanks, vaults and boilers do: size scales capability, and blocks can be added to or removed from a running machine. No hammer.
5. **Reuse Create's processing first.** If a basin, press, fan, spout or sequenced assembly can express a step, add a recipe, not a block. A new block is justified only by a mechanic Create lacks. Steel smelting and coking qualify.
6. **Show the process.** Every machine visibly moves or changes state (flames, glowing contents, pistons), has goggle info and display-link support, and ships with Ponder scenes.
7. **Art:** reference `create:` textures. New textures use Create's exact palettes, and models stay chunky and simple.
8. **Interoperate.** Use `c:` tags for every ingot, dust and fluid. Use Create's data maps and registries where they exist.

## 4. Fun-first progression (Kragnoth, 2026-11-18)
The addon stays fun and functional. These rules sit beside the eight above and win any tie.
9. **Every stage pays off on its own.** A player who stops at any tier already has steel or something useful. Nothing is worth building only once the whole chain is done.
10. **Byproducts never stall a machine.** Creosote, slag and the like are bonuses. If they aren't extracted, the machine keeps running: it voids or ejects them.
11. **Upgrades multiply, they don't gate.** Coke beats coal, more heat beats less, a bigger build beats a small one. Each makes things faster or higher-yield; none is required to get started.
12. **No mandatory fluid balancing.** No air, CO₂ or hot-air loops, and no multi-fluid juggling just to keep a machine alive.
13. **Use machines the player already has.** Each tier adds at most one new block, and early tiers add none.
14. **Forgiving multiblocks.** Any reasonable size works and bigger is faster. No exact patterns, no reinforcement checks, no new worldgen ore just for the building blocks.
15. **Steel arrives around brass time** (early–mid game), not as a late-game wall.

## 5. Steelworks: revised route (proposal, awaiting approval)
For contrast, TFMG (1.21.1 @ e8fefaf) makes steel like this:
- Mine fireclay (its own worldgen) and build firebrick walls.
- Coke ovens: 1,200 ticks per coal, and CO₂ must be pumped out.
- A blast stove: needs air pumped in, burns creosote or furnace gas, CO₂ pumped out, outputs hot air.
- A blast furnace: a brick cylinder plus reinforcements, a hatch for hot air, coke as fuel, flux.
- Molten steel, slag and furnace gas all have to be handled.
- A casting basin turns molten steel into ingots (200 ticks each).
- Steel_from_raw_iron uses 40 mB of hot air per operation. Nothing pays off until every part works.

Our route is a ladder you can stop on at any rung:

| Tier | What the player builds | Gives | New blocks |
|---|---|---|---|
| 1 · Carburising | **Compacting** iron + coal/charcoal (press over basin) → *carbon-packed iron*; then **bulk blasting** with an encased fan through lava → steel ingot | Steel, slowly, from machines they already own | 0 (one item) |
| 2 · Coke | **Coke oven**: any size, placement-formed, sitting on any heat (passive heat works, burners are faster). Coal → coke. Creosote is an optional bonus pumped from any face, voided when full. | Coke: a better carbon source (more steel per craft) and a strong blaze-burner fuel | 1 |
| 3 · Blast furnace | **Blast furnace**: placement-formed stack over blaze burners, fed from the top by funnels or chutes. Crushed iron + coke → steel at the ore-processing bonus yield. Heated works; superheated is faster; fans blowing in add speed (and cost stress). Slag comes out as a bonus item and is never required. | Bulk steel. The project's showpiece | 1 |
| Side | Heated **mixing**: electrum, constantan. **Milling** coke → coke dust. **Crushing** slag → slag gravel | Alloys and building blocks | 0 |

Molten metal and casting are left out of Steelworks. They belong to the later, optional Arc Crucible.
