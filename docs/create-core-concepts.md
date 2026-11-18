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

## 4. What this means for Steelworks (first module)
- **Coking Oven:** a brick chamber formed by placement, sized like a vault/boiler, heated by blaze burners underneath.
  - Coal goes in through funnels; coke comes out through funnels.
  - Creosote is drained with a pipe and mechanical pump (Create's normal fluid output).
  - No rotation needed; heat level sets speed.
  - Goggles show contents and progress.
- **Blast Furnace:** a tall placement-formed stack.
  - Iron and coke are loaded from the top by funnel or chute.
  - Superheated burners provide heat.
  - Encased fans blowing in act as the preheaters/air blast: more fans mean faster, and their RPM costs stress.
  - Steel and slag come out of separate faces onto belts or depots.
- **Not new blocks:**
  - electrum and constantan → heated mixing recipes
  - coke dust → milling
  - slag gravel → crushing
  - coke as burner fuel → `BlazeBurnerFuel` data map
