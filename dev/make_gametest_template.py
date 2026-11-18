#!/usr/bin/env python3
"""Writes the empty playground the dev game tests build on: 16x10x16, a two-block stone floor (y 0-1), air above.
Run from the repo root: python3 dev/make_gametest_template.py   (needs: pip install nbtlib)
The template ships only in dev runs: exclude data/creativeengineering/structure/gametest from the jar.
Adapted from Tarbound's dev/make_gametest_template.py (MIT, Kragnoth)."""
import os
import nbtlib
from nbtlib.tag import Compound, Int, List, String

OUT = 'mod/src/main/resources/data/creativeengineering/structure/gametest/playground.nbt'
W, H, D = 16, 10, 16
palette = [Compound({"Name": String("minecraft:stone")}), Compound({"Name": String("minecraft:air")})]
blocks = [Compound({"pos": List[Int]([Int(x), Int(y), Int(z)]), "state": Int(0 if y < 2 else 1)})
          for x in range(W) for z in range(D) for y in range(H)]
root = Compound({
    "DataVersion": Int(3955),
    "size": List[Int]([Int(W), Int(H), Int(D)]),
    "palette": List[Compound](palette),
    "blocks": List[Compound](blocks),
    "entities": List[Compound]([]),
})
os.makedirs(os.path.dirname(OUT), exist_ok=True)
nbtlib.File(root).save(OUT, gzipped=True)
print("playground.nbt written")
