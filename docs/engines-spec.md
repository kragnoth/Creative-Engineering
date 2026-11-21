# Module 1: Fuels & Engines — the diesel engine (design, 2026-11-20)

Status: **design agreed by Kragnoth, not built.** Build starts next session.

## Kragnoth's brief (2026-11-20)
- A **self-contained multiblock in the essence of Create's steam engine**: no fixed shape, built from parts.
- A **central mechanism** (the crankcase) that everything attaches to.
- **Pistons (cylinders) attach at 45°, up to 2 per crankcase block** (a V layout: one each side).
- Looks: **a real 1890s–1900s diesel engine** (cast-iron A-frame, exposed crossheads, big flywheel) **blended with Immersive Engineering** (steel plate, rivets, hazard trim kept small). Excalibur colour depth, Create palette discipline.
- A **fuel tank connects intelligently** to the crankcase, with **no pump needed**.
- A **crank jump-starts** the engine.
- **Create logic throughout.**
- **"Each part can be used without requiring a static look":** every part looks right in any arrangement (any length, cylinders on one or both sides, tank anywhere it touches), the way Create's tanks and steam engines do.

## How it maps onto Create
| Part | Create analogue | Behaviour |
|---|---|---|
| **Crankcase** (central mechanism) | Steam engine + shaft | Placed in a line along one axis; adjacent crankcases join into one engine (forming like our coke oven / Create tank). One end is the output shaft. Stress capacity = cylinders × per-cylinder SU. |
| **Cylinder** | Steam engine on a boiler | Placed on a crankcase's upper-left or upper-right 45° face (2 slots per crankcase block). Animated piston and con-rod linked to the crank (Flywheel visual), like the steam engine's linkage. |
| **Fuel tank** | Create Fluid Tank feeding a boiler | Any Create fluid tank (or ours) touching the crankcase is read directly through its fluid capability. No pump, no pipes needed (pipes still work for refilling). |
| **Starting crank** | Create Hand Crank | Right-click and hold (or a deployer, or any rotation into the starter) to turn the engine over. Once it fires with fuel, it runs on its own. Stops when fuel runs out or it's overstressed; needs cranking again. |
| **Flywheel** (proposed) | Create's flywheel look | Optional end piece: purely the 1890s look, or adds a little stress buffer. |

## Feedback (the "visibly working" rule)
Pistons stroke in time with the crank, exhaust puffs per firing, and a thump that speeds up with RPM. Goggles and Jade show cylinders, fuel, burn rate and stress capacity. Display Link sources as for the furnaces.

## Still open (decide before building)
1. **Fuel before oil exists:** creosote at a poor rate (proposed: gives the coke oven another payoff), plant oil or ethanol (IE biodiesel), or nothing until refining. Fuel values go in a data map either way, so packs can change them.
2. **Output:** fixed RPM with stress capacity per cylinder (steam-engine style), or better fuel giving more SU per cylinder (fuel tiers make refining worth it).
3. **Flywheel:** cosmetic, functional, or not at all.
4. **Exhaust:** particles only, or a cosmetic exhaust or chimney block.
