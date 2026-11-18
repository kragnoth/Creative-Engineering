"""Writes Creative Engineering's hand-authored data: models, blockstates, recipes, tags, loot, advancements, lang.
Run from the repo root: python3 dev/gen_data.py
No game launch (no runData): every JSON here is plain and reviewable. Edit this file, not the outputs.
Lang: keys this script owns are rewritten; other keys (Ponder, written by dev/gen_ponder_lang.py) are kept."""
import json, os, collections

MODID = 'creativeengineering'
RES = 'mod/src/main/resources'
A = '%s/assets/%s' % (RES, MODID)
D = '%s/data/%s' % (RES, MODID)
written = []

def put(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write('\n')
    written.append(path)

def ce(p): return '%s:%s' % (MODID, p)
def item(i): return {'item': i}
def tag(t): return {'tag': t}

# ---------------------------------------------------------------------------------------- content
SIMPLE_ITEMS = ['carbon_packed_iron', 'blister_steel_ingot', 'blister_steel_nugget', 'blister_steel_sheet',
                'coal_coke', 'coke_dust', 'creosote_bucket']
TOOLS = ['sword', 'pickaxe', 'axe', 'shovel', 'hoe']
ARMOR = ['helmet', 'chestplate', 'leggings', 'boots']
BLOCKS = ['blister_steel_block', 'coke_block']

LANG = collections.OrderedDict([
    ('itemGroup.%s' % MODID, 'Creative Engineering'),
    ('item.%s.carbon_packed_iron' % MODID, 'Carbon-Packed Iron'),
    ('item.%s.blister_steel_ingot' % MODID, 'Blister Steel Ingot'),
    ('item.%s.blister_steel_nugget' % MODID, 'Blister Steel Nugget'),
    ('item.%s.blister_steel_sheet' % MODID, 'Blister Steel Sheet'),
    ('block.%s.blister_steel_block' % MODID, 'Block of Blister Steel'),
    ('tooltip.%s.shear_forged' % MODID, 'Shear-forged blister steel'),
    ('advancements.%s.root.title' % MODID, 'Creative Engineering'),
    ('advancements.%s.root.description' % MODID, 'Industry, the Create way'),
    ('advancements.%s.carbon_packed_iron.title' % MODID, 'Packed and Ready'),
    ('advancements.%s.carbon_packed_iron.description' % MODID, 'Press iron and coal together in a basin'),
    ('advancements.%s.blister_steel.title' % MODID, 'Cementation'),
    ('advancements.%s.blister_steel.description' % MODID, 'Bake carbon-packed iron into blister steel with a fan through lava'),
    ('advancements.%s.shear_forged.title' % MODID, 'Shear-Forged'),
    ('advancements.%s.shear_forged.description' % MODID, 'Make a blister steel tool or piece of armour'),
    ('item.%s.coal_coke' % MODID, 'Coal Coke'),
    ('item.%s.coke_dust' % MODID, 'Coke Dust'),
    ('item.%s.creosote_bucket' % MODID, 'Creosote Bucket'),
    ('block.%s.coke_oven_bricks' % MODID, 'Coke Oven Bricks'),
    ('block.%s.coke_block' % MODID, 'Block of Coal Coke'),
    ('block.%s.creosote' % MODID, 'Creosote Oil'),
    ('fluid_type.%s.creosote' % MODID, 'Creosote Oil'),
    ('%s.jei.coking' % MODID, 'Coking'),
    ('%s.jei.coking.creosote_bonus' % MODID, 'Bonus: vented as smoke if the oven\'s tank is full'),
    ('%s.coke_oven.size' % MODID, 'Size: %sx%sx%s'),
    ('%s.coke_oven.heat' % MODID, 'Heat: %s'),
    ('%s.coke_oven.heat.none' % MODID, 'none'),
    ('%s.coke_oven.slots' % MODID, 'Coking: %s / %s slots'),
    ('%s.coke_oven.creosote' % MODID, 'Creosote: %s / %s mB'),
    ('%s.coke_oven.blocker.no_heat' % MODID, 'No heat: put a heat source under every column'),
    ('%s.coke_oven.blocker.output_full' % MODID, 'Output full'),
    ('advancements.%s.coke_oven.title' % MODID, 'Purity in Pyrolysis'),
    ('advancements.%s.coke_oven.description' % MODID, 'Coke coal in a Coke Oven'),
])
for t in TOOLS + ARMOR:
    LANG['item.%s.blister_steel_%s' % (MODID, t)] = 'Blister Steel %s' % t.capitalize()

# ---------------------------------------------------------------------------------------- models
for n in SIMPLE_ITEMS + ['blister_steel_' + a for a in ARMOR]:
    put('%s/models/item/%s.json' % (A, n), {'parent': 'minecraft:item/generated', 'textures': {'layer0': ce('item/' + n)}})
for t in TOOLS:
    n = 'blister_steel_' + t
    put('%s/models/item/%s.json' % (A, n), {'parent': 'minecraft:item/handheld', 'textures': {'layer0': ce('item/' + n)}})
for b in BLOCKS:
    put('%s/models/block/%s.json' % (A, b), {'parent': 'minecraft:block/cube_all', 'textures': {'all': ce('block/' + b)}})
    put('%s/models/item/%s.json' % (A, b), {'parent': ce('block/' + b)})
    put('%s/blockstates/%s.json' % (A, b), {'variants': {'': {'model': ce('block/' + b)}}})
    put('%s/loot_table/blocks/%s.json' % (D, b), {
        'type': 'minecraft:block',
        'pools': [{'rolls': 1, 'bonus_rolls': 0, 'entries': [{'type': 'minecraft:item', 'name': ce(b)}],
                   'conditions': [{'condition': 'minecraft:survives_explosion'}]}],
        'random_sequence': ce('blocks/' + b)})

# Coke oven bricks: lit / unlit models
for name, tex in [('coke_oven_bricks', 'coke_oven_bricks'), ('coke_oven_bricks_lit', 'coke_oven_bricks_lit')]:
    put('%s/models/block/%s.json' % (A, name), {'parent': 'minecraft:block/cube_all', 'textures': {'all': ce('block/' + tex)}})
put('%s/models/item/coke_oven_bricks.json' % A, {'parent': ce('block/coke_oven_bricks')})
put('%s/blockstates/coke_oven_bricks.json' % A, {'variants': {
    'lit=false': {'model': ce('block/coke_oven_bricks')}, 'lit=true': {'model': ce('block/coke_oven_bricks_lit')}}})
put('%s/loot_table/blocks/coke_oven_bricks.json' % D, {
    'type': 'minecraft:block',
    'pools': [{'rolls': 1, 'bonus_rolls': 0, 'entries': [{'type': 'minecraft:item', 'name': ce('coke_oven_bricks')}],
               'conditions': [{'condition': 'minecraft:survives_explosion'}]}],
    'random_sequence': ce('blocks/coke_oven_bricks')})
# Creosote fluid block: model only carries the particle texture
put('%s/models/block/creosote.json' % A, {'textures': {'particle': ce('block/creosote_still')}})
put('%s/blockstates/creosote.json' % A, {'variants': {'': {'model': ce('block/creosote')}}})

# ---------------------------------------------------------------------------------------- recipes
R = D + '/recipe'
# Tier 1, step 1: packing (press over a basin, unheated).
put(R + '/compacting/carbon_packed_iron.json', {
    'type': 'create:compacting',
    'ingredients': [tag('c:ingots/iron'), tag('minecraft:coals'), tag('minecraft:coals')],
    'results': [{'id': ce('carbon_packed_iron')}]})
# With coke (ours from tier 2, or any mod's c:coal_coke): 4x less carbon.
put(R + '/compacting/carbon_packed_iron_from_coke.json', {
    'neoforge:conditions': [{'type': 'neoforge:not', 'value': {'type': 'neoforge:tag_empty', 'tag': 'c:coal_coke'}}],
    'type': 'create:compacting',
    'ingredients': [tag('c:ingots/iron'), tag('c:ingots/iron'), tag('c:coal_coke')],
    'results': [{'id': ce('carbon_packed_iron'), 'count': 2}]})
# Tier 1, step 2: cementation. A blasting recipe: Create's fan (through lava or a lit burner) does it in bulk,
# a vanilla blast furnace slowly; a plain furnace can't (blasting recipes only).
put(R + '/blasting/blister_steel_ingot.json', {
    'type': 'minecraft:blasting', 'category': 'misc', 'cookingtime': 600, 'experience': 0.35,
    'ingredient': item(ce('carbon_packed_iron')), 'result': {'id': ce('blister_steel_ingot'), 'count': 1}})
put(R + '/pressing/blister_steel_sheet.json', {
    'type': 'create:pressing', 'ingredients': [tag('c:ingots/blister_steel')], 'results': [{'id': ce('blister_steel_sheet')}]})

def shapeless(name, ingredients, result, count=1, category='misc'):
    put('%s/crafting/%s.json' % (R, name), {'type': 'minecraft:crafting_shapeless', 'category': category,
        'ingredients': ingredients, 'result': {'id': result, 'count': count}})

def shaped(name, pattern, key, result, count=1, category='equipment'):
    put('%s/crafting/%s.json' % (R, name), {'type': 'minecraft:crafting_shaped', 'category': category,
        'pattern': pattern, 'key': key, 'result': {'id': result, 'count': count}})

shapeless('blister_steel_ingot_from_nuggets', [item(ce('blister_steel_nugget'))] * 9, ce('blister_steel_ingot'))
shapeless('blister_steel_nugget', [item(ce('blister_steel_ingot'))], ce('blister_steel_nugget'), 9)
shaped('blister_steel_block', ['###', '###', '###'], {'#': item(ce('blister_steel_ingot'))}, ce('blister_steel_block'), category='building')
shapeless('blister_steel_ingot_from_block', [item(ce('blister_steel_block'))], ce('blister_steel_ingot'), 9)

# Gear takes any steel grade (#steel_grade: blister steel now, steel later), so nothing becomes a dead end.
M = tag(ce('steel_grade'))
S = item('minecraft:stick')
for t, pat in [('sword', [' # ', ' # ', ' S ']), ('pickaxe', ['###', ' S ', ' S ']), ('axe', ['##', '#S', ' S']),
               ('shovel', ['#', 'S', 'S']), ('hoe', ['##', ' S', ' S'])]:
    shaped('blister_steel_' + t, pat, {'#': M, 'S': S}, ce('blister_steel_' + t))
for a, pat in [('helmet', ['###', '# #']), ('chestplate', ['# #', '###', '###']), ('leggings', ['###', '# #', '# #']),
               ('boots', ['# #', '# #'])]:
    shaped('blister_steel_' + a, pat, {'#': M}, ce('blister_steel_' + a))

# Tier 2: coking (docs/steelworks-spec.md 3). time = ticks per coking slot at x1 heat.
put(R + '/coking/coal_coke.json', {'type': ce('coking'), 'ingredient': item('minecraft:coal'),
    'result': {'id': ce('coal_coke'), 'count': 1}, 'creosote': 250, 'time': 400})
put(R + '/coking/coke_block.json', {'type': ce('coking'), 'ingredient': item('minecraft:coal_block'),
    'result': {'id': ce('coke_block'), 'count': 1}, 'creosote': 2250, 'time': 3600})
put(R + '/coking/charcoal.json', {'type': ce('coking'), 'ingredient': tag('minecraft:logs_that_burn'),
    'result': {'id': 'minecraft:charcoal', 'count': 1}, 'creosote': 100, 'time': 400})
put(R + '/milling/coke_dust.json', {'type': 'create:milling', 'ingredients': [tag('c:coal_coke')],
    'processing_time': 100, 'results': [{'id': ce('coke_dust')}]})
shaped('coke_oven_bricks', ['BBB', 'BSB', 'BBB'], {'B': item('minecraft:brick'), 'S': tag('c:plates/blister_steel')},
       ce('coke_oven_bricks'), count=2, category='building')
shaped('coke_block', ['###', '###', '###'], {'#': item(ce('coal_coke'))}, ce('coke_block'), category='building')
shapeless('coal_coke_from_block', [item(ce('coke_block'))], ce('coal_coke'), 9)
# Fuel: coke burns twice as long as coal (furnaces and Create's blaze burners both read this data map).
put('%s/data/neoforge/data_maps/item/furnace_fuels.json' % RES, {'values': {
    ce('coal_coke'): {'burn_time': 3200}, ce('coke_block'): {'burn_time': 32000}}})

# ---------------------------------------------------------------------------------------- tags
def tagfile(ns, kind, path, values, replace=False):
    put('%s/data/%s/tags/%s/%s.json' % (RES, ns, kind, path), {'replace': replace, 'values': values})

tagfile('c', 'item', 'ingots/blister_steel', [ce('blister_steel_ingot')])
tagfile('c', 'item', 'nuggets/blister_steel', [ce('blister_steel_nugget')])
tagfile('c', 'item', 'plates/blister_steel', [ce('blister_steel_sheet')])
tagfile('c', 'item', 'storage_blocks/blister_steel', [ce('blister_steel_block')])
tagfile('c', 'block', 'storage_blocks/blister_steel', [ce('blister_steel_block')])
tagfile('c', 'item', 'ingots', ['#c:ingots/blister_steel'])
tagfile('c', 'item', 'nuggets', ['#c:nuggets/blister_steel'])
tagfile('c', 'item', 'plates', ['#c:plates/blister_steel'])
tagfile('c', 'item', 'storage_blocks', ['#c:storage_blocks/blister_steel', '#c:storage_blocks/coal_coke'])
tagfile('c', 'block', 'storage_blocks', ['#c:storage_blocks/blister_steel', '#c:storage_blocks/coal_coke'])
tagfile('c', 'item', 'coal_coke', [ce('coal_coke')])
tagfile('c', 'item', 'dusts/coal_coke', [ce('coke_dust')])
tagfile('c', 'item', 'dusts', ['#c:dusts/coal_coke'])
tagfile('c', 'item', 'storage_blocks/coal_coke', [ce('coke_block')])
tagfile('c', 'block', 'storage_blocks/coal_coke', [ce('coke_block')])
tagfile('c', 'fluid', 'creosote', [ce('creosote'), ce('flowing_creosote')])
# Tarbound (optional, soft): jobs may burn coke (docs/tarbound-compat.md A3). Ignored when Tarbound is absent.
put('%s/data/tarbound/tags/item/pool_fuel.json' % RES, {'replace': False, 'values': [{'id': '#c:coal_coke', 'required': False}]})
# Grade tags (docs/steelworks-spec.md 1): higher grades join these as they arrive.
tagfile(MODID, 'item', 'steel_grade', ['#c:ingots/blister_steel'])
tagfile(MODID, 'item', 'cast_grade', [])
tagfile(MODID, 'item', 'creosoted_sleepers', [])  # rail & timber module, later
# Vanilla: tool/armour tags drive enchanting tables, anvils and other mods' checks.
for t in TOOLS:
    tagfile('minecraft', 'item', t + 's', [ce('blister_steel_' + t)])
for a, slot in zip(ARMOR, ['head', 'chest', 'leg', 'foot']):
    tagfile('minecraft', 'item', slot + '_armor', [ce('blister_steel_' + a)])
tagfile('minecraft', 'item', 'trimmable_armor', [ce('blister_steel_' + a) for a in ARMOR])
tagfile('minecraft', 'item', 'beacon_payment_items', [ce('blister_steel_ingot')])
tagfile('minecraft', 'block', 'mineable/pickaxe', [ce(b) for b in BLOCKS] + [ce('coke_oven_bricks')])
tagfile('minecraft', 'block', 'needs_stone_tool', [ce('blister_steel_block')])
tagfile('minecraft', 'block', 'beacon_base_blocks', [ce('blister_steel_block')])

# ---------------------------------------------------------------------------------------- advancements
ADV = D + '/advancement'
def has(i): return {'trigger': 'minecraft:inventory_changed', 'conditions': {'items': [{'items': i}]}}
def adv(name, parent, icon, criteria, frame='task'):
    obj = {'display': {'icon': {'id': icon}, 'title': {'translate': 'advancements.%s.%s.title' % (MODID, name)},
                       'description': {'translate': 'advancements.%s.%s.description' % (MODID, name)},
                       'frame': frame, 'show_toast': True, 'announce_to_chat': True, 'hidden': False},
           'criteria': criteria, 'requirements': [list(criteria)]}
    if parent:
        obj['parent'] = ce(parent)
    else:
        obj['display']['background'] = 'create:textures/block/brass_casing.png'
        obj['display']['announce_to_chat'] = False
        obj['display']['show_toast'] = False
    put('%s/%s.json' % (ADV, name), obj)

adv('root', None, ce('blister_steel_ingot'), {'iron': has('minecraft:iron_ingot')})
adv('carbon_packed_iron', 'root', ce('carbon_packed_iron'), {'packed': has(ce('carbon_packed_iron'))})
adv('blister_steel', 'carbon_packed_iron', ce('blister_steel_ingot'), {'blister': has(ce('blister_steel_ingot'))}, 'goal')
adv('coke_oven', 'blister_steel', ce('coal_coke'), {'coke': has(ce('coal_coke'))})
adv('shear_forged', 'blister_steel', ce('blister_steel_pickaxe'),
    {t: has(ce('blister_steel_' + t)) for t in TOOLS + ARMOR})

# ---------------------------------------------------------------------------------------- lang
lang_path = '%s/lang/en_us.json' % A
lang = collections.OrderedDict()
if os.path.exists(lang_path):
    lang = json.load(open(lang_path, encoding='utf-8'), object_pairs_hook=collections.OrderedDict)
    for k in [k for k in lang if not k.startswith(MODID + '.ponder.')]:
        del lang[k]
merged = collections.OrderedDict(LANG)
merged.update(lang)
put(lang_path, merged)

print('%d files written' % len(written))
