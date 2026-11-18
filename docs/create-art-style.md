# Create 6.0.10 art style — findings

Source: `Creators-of-Create/Create` tag `mc1.21.1-6.0.10` (ac0c444). Regenerate with `tools/analyze_palette.py` and `tools/analyze_models.py`.

## License rule (hard)
- Create **code** = MIT. Create **assets** (`src/main/resources/assets/`) = **All Rights Reserved**.
- Our blocks **reference** Create textures by resource location (`create:block/brass_casing`). That follows the player's resource pack and needs no permission.
- We never copy or edit Create's pixels. New textures are drawn from scratch in the same style.
- Consequence: anything that references Create textures makes Create a **hard dependency**. Pin a Create version range, since texture paths can move.

## Palettes (exact ramps, dark → light)
| Material | Ramp |
|---|---|
| Brass | `#592424 #724731 #9e6947 #af794f #bf8e55 #c79758 #cea05a #d7aa5e #e4b763 #eec168 #f7cb6c #fbdc7d #ffeb8c` |
| Brass casing (dark frame) | `#291a0c #301e0e #3a2411 #442b15 #492e16 #4f3118 #53381a #5a3e1e` + brass highlights `#cea05a #e4b763 #f7cb6c #ffeb8c` |
| Zinc (green-tinted) | `#4b5249 #5d6255 #6f7262 #7e806c #9b9b87 #aab59d #b5d1ba` |
| Copper (casing) | grey base `#444140 #4b4948 #555453 #5e5b5a #67625f #726c69` + copper `#904931 #9a5038 #a75a40 #b26247 #c26b4c #c87456 #d67b5b #e3826c` |
| Andesite casing | wood frame `#553a1f #5a4424 #614b2e #70522e #7a5a34 #82613a #886539` + metal `#505351 #60635f #686c68 #707570 #828784 #8f9691 #9aa49d #a8b3ab` |
| Shaft (`axis`) | `#5a5d5a #626662 #696e69 #707570 #797e7b #828784` |

Brass casing, the brass gearbox and the brass block all share the **same** brass tones. Matching these ramps exactly is what makes new art read as Create.

## Textures
- 392 block textures; most are 16×16. 48 models use 32×32 texture sheets and 4 use 128×128.
- Most reused textures: `axis` (59), `gearbox` (53), `andesite_casing` (52), `pipes` (52), `fluid_tank` (48), `axis_top` (45), `brass_casing` (38), `brass_block` (35), `industrial_iron_block` (24).
- Prefer referencing textures that are one image per part. If a resource pack rearranges a shared sheet, UV-mapped models break.

## Models
- 721 block model JSONs. Median 3 elements, 90th percentile 10, max 25: chunky and simple.
- Element rotations are only 0, ±22.5 and ±45 degrees.
- 317 models use `block/block` as parent.

## Code conventions (Create 6)
- Moving parts are partial models in `AllPartialModels`, rendered with Flywheel instancing.
  - A plain spinning shaft: `.visual(() -> SingleAxisRotatingVisual::shaft)`.
  - A custom spinning part: `SingleAxisRotatingVisual.of(AllPartialModels.X)`.
- Connected textures go through `AllSpriteShifts`. Shift types: `horizontal`, `vertical`, `OMNIDIRECTIONAL`, `RECTANGLE`.
- Item data uses data components, not NBT.
