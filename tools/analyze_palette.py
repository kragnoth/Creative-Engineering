import sys, os, json, glob, collections
from PIL import Image
A=sys.argv[1]
T=A+'/textures/block'
def pal(name):
    p=f'{T}/{name}.png'
    if not os.path.exists(p): return None
    im=Image.open(p).convert('RGBA'); c=collections.Counter(px for px in im.getdata() if px[3]>0)
    cols=sorted(c.items(),key=lambda kv:sum(kv[0][:3]))
    return im.size,[('#%02x%02x%02x'%k[:3],n) for k,n in cols]
for n in sys.argv[2:]:
    r=pal(n); 
    if r: print(n,r[0],len(r[1]),' '.join(f'{h}:{c}' for h,c in r[1]))
