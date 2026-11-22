# Module 1: Fuels & Engines — the diesel engine (design, 2026-11-20)

Status: **first pass (0.7.0): blocks + models only, for look approval.** No behaviour yet.

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

## Decisions (Kragnoth, 2026-11-22)
- **Fuels:** creosote, plant oil and ethanol to start (diesel/biodiesel from refining later). Read by fluid **tags** shared with Create Crafts & Additions, Create Diesel Generators and Immersive Engineering (to verify per mod when fuels are built), so their fluids work too. Values in a data map.
- **Output scales with fuel quality.** The weakest fuel starts about as good as or better than lava (which is easily renewable), and better fuels scale up from there.
- **Flywheel:** cosmetic, but sits on the output shaft and passes rotation on; its shaft is Create's exactly (4x4 at 6..10, axis textures and UVs) so it mates like any modded flywheel.
- **Exhaust:** particles only, straight off each running cylinder's head. No exhaust blocks (tried in 0.7.0, removed: Kragnoth).
- **Starting:** no starter block. The crankcase's front shaft is an input; any rotation turns it over (a Create Hand Crank on that shaft is the classic way). It can take any stress input.
- **Create methods:** models built from JSON elements like Create's, with **Create's own textures** (industrial iron, dark metal, railway casing, brass, axis), so a resource pack that reskins Create reskins the engine. Moving parts (pistons, linkage) later via partial models, as Create's steam engine does.

- **Exhaust particles:** vanilla `CAMPFIRE_COSY_SMOKE` from the cylinder's client `animateTick` (like the coke oven): occasional, rate rising with RPM, hard-capped per engine, so no server cost and no custom particle.
- **Power out:** off the flywheel / back end of the crankshaft **only** (for now). Going under the floor or sideways is left to Create's Mechanical Belt, Encased Chain Drive and Gearbox (authentic too: 1890s mills belted the flywheel down to basement line shafts).
- **Starting:** the front crankshaft end is a **read-only starter port**: the crankcase does not connect kinetically to the front block, it only reads whether that block turns (e.g. a Hand Crank). Two rotation sources on one network at different speeds would break Create's network, so the starter never joins the output network.

## First-pass blocks (0.7.0)
`diesel_crankcase` (axis), `diesel_cylinder` (facing = bank it leans to), `diesel_flywheel` (axis, 3-block wheel). 0.7.1: detail pass (octagonal barrels/heads/hub from crossed boxes, cooling bands, studs, ribs, doors, bearing rings, oil cups); exhaust blocks removed. Renders: `python3 tools/stylekit/render_engine_stages.py` (writes to tools/stylekit/out/engine/, git-ignored; uses `render_scene.py`).

## Still open
1. **Fuel before oil exists:** creosote at a poor rate (proposed: gives the coke oven another payoff), plant oil or ethanol (IE biodiesel), or nothing until refining. Fuel values go in a data map either way, so packs can change them.
2. **Output:** fixed RPM with stress capacity per cylinder (steam-engine style), or better fuel giving more SU per cylinder (fuel tiers make refining worth it).
3. **Flywheel:** cosmetic, functional, or not at all.
4. **Exhaust:** particles only, or a cosmetic exhaust or chimney block.
