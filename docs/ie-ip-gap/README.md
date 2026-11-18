# IE + IP vs Create 6.0.10: gap worksheet

Live Sheet: https://claude.ai/artifact/48UN6NEtpD5XpK1GaNhsWt
Repo copies:
- `ie_ip_create_gap_worksheet.xlsx`: same tabs as the Sheet.
- CSVs: `features`, `modules`, `recipe_types`, `fluids`.

## Result
92 features: 35 Missing, 22 Partial, 21 Covered, 14 Skip. 20 of them are high priority (13 Missing, 4 Partial, 3 Covered).

Biggest Create-fashion wins, in order:
1. Liquid-fuel engines.
2. Steelworks (coke oven and blast furnace).
3. Arc crucible.
4. Metalworking dies.
5. Petrochem extraction (reservoirs, derrick, pumpjack).
6. Refining.
7. Mineral-vein excavation.

**Open decision:** electricity. Either stay pure rotation (SU), or add an FE wire layer. Every proposed module works without it.

## Versions and method (clean room: behaviour documented, no code copied)
| Item | Version |
|---|---|
| Immersive Engineering | 12.4.2-194 (Modrinth); source 1.21.1 branch @ 75a27f0 |
| Immersive Petroleum | 4.5.0-39 (Modrinth); source 1.21.1 branch @ 11fe4dc |
| Create | 6.0.10, tag mc1.21.1-6.0.10 @ ac0c444 |
| Server | NeoForge 21.1.267, headless; all three mods loaded cleanly |

Sources, in priority order:
1. **Shipped data:** recipes, reservoirs, fuels. These are taken as the truth.
2. **Config defaults:** `tools/config_defaults.sh`.
3. **In-game manuals:** `tools/manual_digest.py`.
4. **Registry dumps:** `docs/registry-dumps`, via `/neoforge dump registry`.

Known manual-vs-data mismatch: the IP manual says the distillation tower takes 50 mB/t of crude. The shipped recipe uses **60 mB/t**, and the worksheet uses 60.

Registry footprint (IE / IP / Create):

| Registry | IE | IP | Create |
|---|---|---|---|
| Blocks | 390 | 27 | 643 |
| Items | 642 | 42 | 699 |
| Block entities | 131 | 18 | 114 |
| Fluids | 21 | 20 | 8 |
| Recipe types | 20 | 4 | 17 |

## Regenerate
```
bash tools/setup_refs.sh                    # pinned sources + jars
python3 tools/export.py docs/ie-ip-gap      # rows.py -> CSVs (edit tools/rows.py, not the CSVs)
python3 tools/build_xlsx.py docs/ie-ip-gap docs/ie-ip-gap/ie_ip_create_gap_worksheet.xlsx
```
