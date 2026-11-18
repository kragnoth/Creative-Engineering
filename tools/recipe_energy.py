"""Energy/time spread per recipe type in an unpacked mod jar's data folder.
Usage: python3 -I tools/recipe_energy.py ~/ref/jars/unpacked/immersiveengineering/data/immersiveengineering/recipe
"""
import json, glob, collections, statistics, sys

d = collections.defaultdict(list)
for f in glob.glob(sys.argv[1] + '/**/*.json', recursive=True):
    try:
        j = json.load(open(f))
    except Exception:
        continue
    t = j.get('type', '')
    if t == 'neoforge:conditional':
        continue
    for k in ('energy', 'time'):
        if k in j:
            d[(t.split(':')[-1], k)].append(j[k])
for (t, k), v in sorted(d.items()):
    print(f"{t:22} {k:6} n={len(v):3} min={min(v)} med={statistics.median(v)} max={max(v)}")
