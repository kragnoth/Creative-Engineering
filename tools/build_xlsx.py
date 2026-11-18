import csv,sys,os
from openpyxl import Workbook
from openpyxl.styles import Font,PatternFill,Alignment
src=sys.argv[1]; dst=sys.argv[2]
wb=Workbook(); ov=wb.active; ov.title='Overview'
HF=PatternFill('solid',fgColor='EFE3C8')  # brass-tinted header
def tab(name,widths,wrapcols):
    ws=wb.create_sheet(name); rows=list(csv.reader(open(f'{src}/{name.lower().replace(" ","_")}.csv')))
    for r in rows: ws.append([int(x) if x.isdigit() else x for x in r])
    for c in ws[1]: c.fill=HF; c.font=Font(bold=True)
    for i,w in enumerate(widths): ws.column_dimensions[chr(65+i)].width=w
    for row in ws.iter_rows(min_row=2):
        for c in row:
            c.alignment=Alignment(wrap_text=(c.column_letter in wrapcols),vertical='top')
    ws.freeze_panes='B2'; return ws,len(rows)
f,nf=tab('Features',[8,5,13,30,14,48,36,32,32,10,48,8],'DFGHIK')
tab('Modules',[22,26,46,42,12,8],'BCD')
tab('Recipe types',[36,7,9,38,36,9,34],'DEG')
tab('Fluids',[30,5,36,40,16,18],'CD')
# Overview
ov['A1']='IE 12.4.2-194 + IP 4.5.0-39 vs Create 6.0.10: what Create is missing'; ov['A1'].font=Font(bold=True,size=14)
ov['A2']='Clean-room inventory from the mods\' manuals, data files and config defaults, checked against a headless NeoForge 21.1.267 server with all three mods loaded (2026-11-18).'
ov['A4']='Gap'; ov['B4']='IE'; ov['C4']='IP'; ov['D4']='Total'; ov['E4']='High priority'
for c in ov[4][:5]: c.fill=HF; c.font=Font(bold=True)
N=nf
for i,g in enumerate(['Missing','Partial','Covered','Skip'],5):
    ov[f'A{i}']=g
    ov[f'B{i}']=f'=COUNTIFS(Features!B2:B{N},"IE",Features!J2:J{N},A{i})'
    ov[f'C{i}']=f'=COUNTIFS(Features!B2:B{N},"IP",Features!J2:J{N},A{i})'
    ov[f'D{i}']=f'=B{i}+C{i}'
    ov[f'E{i}']=f'=COUNTIFS(Features!J2:J{N},A{i},Features!L2:L{N},"H")'
ov['A9']='Total'; ov['A9'].font=Font(bold=True)
for c in 'BCDE': ov[f'{c}9']=f'=SUM({c}5:{c}8)'
ov['A11']='Gap key'; ov['A11'].font=Font(bold=True)
keys=[('Covered','Create 6.0.10 already does this; at most add recipes.'),('Partial','Similar mechanic exists; pieces missing.'),('Missing','Nothing comparable in Create.'),('Skip','Out of scope for a Create-style industry addon (weapons, cosmetics, electricity-only items).')]
for i,(k,v) in enumerate(keys,12): ov[f'A{i}']=k; ov[f'B{i}']=v
ov['A17']='Decision'; ov['A17'].font=Font(bold=True)
ov['B17']='Power: pure rotation (SU) only, decided 2026-11-18. No FE wire layer. First module: Steelworks. Mod id: creativeengineering.'
ov['A19']='Tabs'; ov['A19'].font=Font(bold=True)
for i,(k,v) in enumerate([('Features','Every IE/IP feature: what it does, defaults, Create equivalent, gap, Create-style idea, priority.'),('Modules','Proposed addon modules grouping the gaps, with dependencies.'),('Recipe types','Each IE/IP recipe type, how many recipes ship, and which Create type can express it.'),('Fluids','Every IE/IP fluid, how it is made and used.')],20): ov[f'A{i}']=k; ov[f'B{i}']=v
ov.column_dimensions['A'].width=16
for c in 'BCDE': ov.column_dimensions[c].width=12
wb.save(dst); print('saved',dst,nf-1,'feature rows')
