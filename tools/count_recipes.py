import sys,os,json,glob,collections,gzip,struct
def recipes(root):
    c=collections.Counter()
    for f in glob.glob(root+'/**/recipe*/**/*.json',recursive=True):
        try: j=json.load(open(f))
        except Exception: continue
        t=j.get('type','?')
        if t=='neoforge:conditional': 
            try:t=j['recipes'][0]['recipe']['type']
            except: pass
        c[t]+=1
    return c
for r in sys.argv[1:]:
    print('==',r); [print(f'  {n:4} {t}') for t,n in recipes(r).most_common()]
