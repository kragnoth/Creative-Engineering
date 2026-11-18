import csv,sys,os
sys.path.insert(0,os.path.dirname(os.path.abspath(__file__)))
from rows import *
out=sys.argv[1]; os.makedirs(out,exist_ok=True)
feat=[]; ids={}
for i,r in enumerate(F,1):
    fid=f"{r[0]}-{i:02d}"; ids[r[2]]=fid; feat.append([fid]+r)
mods=[[m[0],', '.join(ids[n] for n in m[1])]+m[2:] for m in MODS]
def w(name,hdr,rows):
    with open(f'{out}/{name}.csv','w',newline='') as f: c=csv.writer(f); c.writerow(hdr); c.writerows(rows)
w('features',FEAT_HDR,feat); w('recipe_types',RT_HDR,RT); w('fluids',FL_HDR,FL); w('modules',MOD_HDR,mods)
import collections
c=collections.Counter((r[1],r[10]) for r in feat)
print(len(feat),'features'); print(sorted(c.items()))
