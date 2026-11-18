# Tarbound compatibility: compat without concessions

Tarbound (Kragnoth's storage and autocrafting mod, 1.21.1 line, checked at 0.67.0-alpha) runs machines through **Reliquaries**. Creative Engineering (CE) must work with it without bending CE's design rules (`create-core-concepts.md` 1–16).

## The principle
**CE machines are honest, standard machines. Tarbound adapts to them, not the other way round.**
- Everything CE does for compat must also help plain Create logistics, vanilla hoppers and other autocrafters (AE2 pattern providers, Refined Storage, and so on). If a change only helps Tarbound and costs CE anything (a screen, FE, Spirit, a dependency), it's out.
- Tarbound-specific behaviour lives **in Tarbound**, behind its existing soft-compat style (reflection / class-name checks, `mod_loaded` conditions, `"required": false` tags), next to its IE, Mekanism and Create adapters.
- **Neither mod depends on the other.** Both build and run alone.

## How Tarbound sees a machine (from its source)
- **It reads the side-less view.** A Reliquary reads `Capabilities.ItemHandler.BLOCK` with `side = null`, as the machine's own screen would (`BoundInventory`, `MachineAccess`). Fluids come the same way.
- **Slot roles are probed with coal and cobblestone** (`BoundInventory.roleOf`):

  | Slot takes coal | Slot takes cobble | Role |
  |---|---|---|
  | yes | no | FUEL |
  | no | no (while empty) | OUTPUT |
  | any other combination | | INPUT |

  A slot also counts as OUTPUT when it holds something it won't take back but will hand out.
- **Loading** (`MachineLoader`):
  - All or nothing: one slot per ingredient, non-fuel slots first, by `insertItem` simulation.
  - **It adds a piece of fuel to an empty FUEL slot unless the block is `LIT`.**
- **Draining:** every second, anything in an OUTPUT slot, plus whatever a contract says it makes (items and fluids) wherever it sits.
- **Contracts are written from JEI/EMI recipes.** The JEI "+" load button appears only if the bound machine is a **catalyst** for that recipe category.
- **Job waiting reasons** (since 0.65): Tarbound already shows why a job can't load (Create crafter grids use this).

## CE side: generic, zero-concession requirements
These go into the Steelworks spec §0 and apply to every CE machine.

| # | Requirement | Why it's generic | Tarbound effect |
|---|---|---|---|
| T1 | **Side-less view = the machine's "screen" view.** Input and fuel buffers can only be inserted; output and slag buffers can only be extracted. Per-face views stay as in §0. | AE2 and Refined Storage interfaces and many pipes query `null` too. | The Reliquary binds and loads correctly. |
| T2 | **Compact automation buffers:** a few big slots (input, fuel, outputs, slag), not the 4-per-brick internal processing slots. The machine spreads work internally. | Funnels and hoppers stay tidy; comparators stay meaningful. | A clean machine window. One-slot-per-ingredient loading works. |
| T3 | **Strict `isItemValid`:** the input buffer only takes recipe inputs, and the fuel buffer only takes fuels from the data map. | Stops funnels jamming wrong items in. | Correct role probes. The blast-furnace fuel buffer reads as FUEL; iron goes to the input buffer. |
| T4 | **`LIT` blockstate while heated** (coke oven, blast furnace). | Honest visual state; drives the glow and smoke. | Stops Tarbound auto-adding extra fuel or coal. |
| T5 | **Every brick of a multiblock exposes the controller's capabilities** (like Create's vault). | Funnels and hoppers work on any brick. | A Reliquary works on any brick, as with IE multiblocks. |
| T6 | **JEI and EMI categories** for `coking` and `blast_smelting`. Our bricks are catalysts. Creosote (fluid) and slag (chance) are listed as outputs. Blast smelting shows **two recipes**: cast iron, and steel with "air blast" noted. | Players need them anyway. | Contracts can be written; "+" fill works; creosote and slag drain into the pool instead of venting or voiding. |
| T7 | **Public readiness method** in a CE `api` package: `@Nullable Component automationBlocker(List<ItemStack> expectedOutputs)`. Returns "No heat", "No air blast", "Output full" or null. | The same text feeds goggles and display links. | Tarbound reads it by reflection and shows it as a job waiting reason. This fixes the steel-vs-cast-iron trap. |

None of T1–T7 adds a screen, a power type, a dependency or any player-facing complexity.

## Tarbound side: small optional adapters (Tarbound repo, later)
| # | Change | Notes |
|---|---|---|
| A1 | When `creativeengineering` is loaded, `MachineAccess` calls CE's `automationBlocker(...)` by reflection and adds it to job waiting reasons. | Same pattern as the Mekanism/IE adapters. |
| A2 | **Fan stream as a machine:** a depot (or belt) in an encased fan's stream counts as a catalyst for Create's fan categories (bulk blasting, smoking, washing, haunting). | Makes CE tier 1 (cementation) and all of Create's fan processing contract-able. Same idea as the existing `tarbound:work_surface` spout/depot rule. |
| A3 | Add `#c:coal_coke` to `tarbound:pool_fuel` via data (`"required": false`). | Jobs can burn coke. |
| A4 | *Optional flavour:* a creosote recipe for tar-soaked planks, gated on `mod_loaded: creativeengineering`. | Ties the two "tar" themes together; data only. |

## Testing
- CE game tests (see `dev/`) verify T1–T5 with hoppers and Create funnels. That covers Tarbound by construction.
- Tarbound's own playtests get CE cases (A1–A2) only when both mods are present in dev.
