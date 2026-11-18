import sys,re,os,json
d=sys.argv[1]; order=sys.argv[2:] 
for n in order:
    p=f'{d}/{n}.txt'
    if not os.path.exists(p): continue
    t=open(p,encoding='utf8').read()
    t=re.sub(r'<link;[^;>]*;([^;>]*)(;[^>]*)?>',r'\1',t); t=re.sub(r'<(np|br)>',' ',t); t=re.sub(r'§.','',t); t=re.sub(r'<[^>]*>','',t)
    t=re.sub(r'\s+',' ',t)
    print(f'### {n}: {t}\n')
