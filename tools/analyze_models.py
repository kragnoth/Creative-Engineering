import sys,json,glob,collections,os
A=sys.argv[1]
fs=glob.glob(A+'/models/block/**/*.json',recursive=True)
ne=[];ts=collections.Counter();tex=collections.Counter();rot=collections.Counter();parents=collections.Counter();uvsz=collections.Counter()
for f in fs:
    try:j=json.load(open(f))
    except Exception: continue
    e=j.get('elements')
    if e:
        ne.append(len(e))
        for el in e:
            if 'rotation' in el: rot[el['rotation'].get('angle')]+=1
    ts[str(j.get('texture_size','-'))]+=1
    if 'parent' in j: parents[j['parent']]+=1
    for v in j.get('textures',{}).values():
        if isinstance(v,str) and not v.startswith('#'): tex[v]+=1
ne.sort()
print('models',len(fs),'with elements',len(ne),'median els',ne[len(ne)//2],'p90',ne[int(len(ne)*.9)],'max',ne[-1])
print('texture_size',ts.most_common(8))
print('rot angles',rot.most_common())
print('top parents',parents.most_common(10))
print('top textures',tex.most_common(30))
