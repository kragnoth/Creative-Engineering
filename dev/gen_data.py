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

def load_json(path):
    with open(path) as f:
        return json.load(f)

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
                'coal_coke', 'coke_dust', 'cast_iron_ingot', 'cast_iron_nugget', 'cast_iron_sheet',
                'steel_ingot', 'steel_nugget', 'steel_sheet', 'slag',
                'rod_die', 'gear_die', 'copper_rod', 'iron_rod', 'steel_rod', 'iron_gear', 'steel_gear', 'copper_wire', 'steel_wire']
TOOLS = ['sword', 'pickaxe', 'axe', 'shovel', 'hoe']
ARMOR = ['helmet', 'chestplate', 'leggings', 'boots']
BLOCKS = ['blister_steel_block', 'coke_block', 'cast_iron_block', 'steel_block']

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
    ('block.%s.blast_furnace_bricks' % MODID, 'Blast Furnace Bricks'),
    ('item.%s.cast_iron_ingot' % MODID, 'Cast Iron Ingot'),
    ('item.%s.cast_iron_nugget' % MODID, 'Cast Iron Nugget'),
    ('item.%s.cast_iron_sheet' % MODID, 'Cast Iron Sheet'),
    ('block.%s.cast_iron_block' % MODID, 'Block of Cast Iron'),
    ('item.%s.steel_ingot' % MODID, 'Steel Ingot'),
    ('item.%s.steel_nugget' % MODID, 'Steel Nugget'),
    ('item.%s.steel_sheet' % MODID, 'Steel Sheet'),
    ('block.%s.steel_block' % MODID, 'Block of Steel'),
    ('item.%s.slag' % MODID, 'Slag'),
    ('%s.blast_furnace.air.on' % MODID, 'Air blast: %s fans (makes steel)'),
    ('%s.blast_furnace.air.off' % MODID, 'No air blast (makes cast iron)'),
    ('%s.blast_furnace.speed' % MODID, 'Speed: x%s'),
    ('%s.blast_furnace.slots' % MODID, 'Smelting: %s / %s slots'),
    ('%s.blast_furnace.blocker.no_heat' % MODID, 'No heat: needs a heated Blaze Burner below'),
    ('%s.blast_furnace.blocker.no_air' % MODID, 'No air blast: add fans blowing into the bottom layer'),
    ('%s.blast_furnace.blocker.no_fuel' % MODID, 'No fuel: add coke, charcoal or coal'),
    ('%s.jei.blast_smelting' % MODID, 'Blast Furnace'),
    ('%s.jei.blast.no_air' % MODID, 'Without an air blast'),
    ('%s.jei.blast.air' % MODID, 'With fans blowing into the bottom layer'),
    ('%s.jei.blast.slag' % MODID, '%s%% chance; voided when the slag slot is full'),
    ('%s.jei.blast.cast_label' % MODID, 'no air'),
    ('%s.jei.blast.steel_label' % MODID, 'air blast'),
    ('%s.jei.blast.needs' % MODID, 'Heated burner, fans, fuel:'),
    ('%s.jei.blast.fuel' % MODID, 'Fuel: %s iron units'),
    ('%s.jei.blast.needs_tip' % MODID, 'Heated Blaze Burner below (superheated x2). Fans blowing into the bottom layer turn cast iron into steel, +25%% per extra fan. One coke smelts 2 iron, charcoal 1, coal half of one.'),
    ('%s.jade.blast_in' % MODID, 'In: %s  Fuel: %s'),
    ('%s.jade.blast_out' % MODID, 'Metal: %s  Slag: %s'),
    ('config.jade.plugin_%s.blast_furnace' % MODID, 'Blast Furnace'),
    ('advancements.%s.blast_furnace.title' % MODID, 'Pig Iron and Air'),
    ('advancements.%s.blast_furnace.description' % MODID, 'Cast iron in a Blast Furnace'),
    ('advancements.%s.steel.title' % MODID, 'Proper Steel'),
    ('block.%s.alloy_furnace_bricks' % MODID, 'Alloy Furnace Bricks'),
    ('item.%s.rod_die' % MODID, 'Rod Die'),
    ('block.%s.diesel_crankcase' % MODID, 'Diesel Crankcase'),
    ('block.%s.diesel_cylinder' % MODID, 'Diesel Cylinder'),
    ('block.%s.diesel_flywheel' % MODID, 'Flywheel'),
    ('%s.engine.title' % MODID, 'Diesel Engine'),
    ('%s.jei.engine_fuel' % MODID, 'Diesel Engine Fuel'),
    ('%s.jade.engine_su' % MODID, 'Stress capacity: %s SU at 64 RPM'),
    ('config.jade.plugin_%s.diesel_engine' % MODID, 'Diesel Engine'),
    ('%s.jei.engine_fuel.stress' % MODID, '%s SU per cylinder'),
    ('%s.jei.engine_fuel.burn' % MODID, '%s mB/s per cylinder'),
    ('%s.jei.engine_fuel.rpm' % MODID, 'at 64 RPM'),
    ('advancements.%s.diesel_engine.title' % MODID, 'Compression Ignition'),
    ('advancements.%s.diesel_engine.description' % MODID, 'Make a Diesel Cylinder'),
    ('%s.engine.layout' % MODID, '%s crankcase(s), %s cylinder(s)'),
    ('%s.engine.fuel' % MODID, 'Fuel: %s'),
    ('%s.engine.no_fuel' % MODID, 'none (pipe or touch a tank of creosote, plant oil, ethanol... to a crankcase side)'),
    ('%s.engine.state.running' % MODID, 'Running'),
    ('%s.engine.state.needs_start' % MODID, 'Stopped: turn the front shaft (e.g. a Hand Crank) to start'),
    ('%s.engine.state.no_cylinders' % MODID, 'No cylinders: place them diagonally above, leaning out'),
    ('%s.engine.state.no_fuel_state' % MODID, 'No fuel'),
    ('block.%s.treated_planks' % MODID, 'Treated Planks'),
    ('block.%s.treated_stairs' % MODID, 'Treated Stairs'),
    ('block.%s.treated_slab' % MODID, 'Treated Slab'),
    ('block.%s.treated_log' % MODID, 'Treated Log'),
    ('block.%s.treated_wood' % MODID, 'Treated Wood'),
    ('block.%s.treated_beam' % MODID, 'Treated Beam'),
    ('advancements.%s.treated_wood.title' % MODID, 'Smells Like a Railway'),
    ('advancements.%s.treated_wood.description' % MODID, 'Treat wood with creosote'),
    ('item.%s.gear_die' % MODID, 'Gear Die'),
    ('item.%s.copper_rod' % MODID, 'Copper Rod'),
    ('item.%s.iron_rod' % MODID, 'Iron Rod'),
    ('item.%s.steel_rod' % MODID, 'Steel Rod'),
    ('item.%s.iron_gear' % MODID, 'Iron Gear'),
    ('item.%s.steel_gear' % MODID, 'Steel Gear'),
    ('item.%s.copper_wire' % MODID, 'Copper Wire'),
    ('item.%s.steel_wire' % MODID, 'Steel Wire'),
    ('%s.die.fitted' % MODID, '%s fitted'),
    ('%s.die.removed' % MODID, 'Die removed'),
    ('%s.jade.die' % MODID, 'Die: %s'),
    ('%s.jei.die_pressing' % MODID, 'Die Pressing'),
    ('%s.jei.die.kept' % MODID, 'Fitted in the press; never used up'),
    ('%s.jei.die.fit' % MODID, 'Right-click a Mechanical Press with the die'),
    ('%s.jei.chance' % MODID, '%s%% chance'),
    ('config.jade.plugin_%s.press_die' % MODID, 'Press Die'),
    ('advancements.%s.press_die.title' % MODID, 'Die Cast'),
    ('advancements.%s.press_die.description' % MODID, 'Make a press die'),
    ('%s.alloy_furnace.heat.0' % MODID, 'Heat: not every column (needs a heated Blaze Burner under each)'),
    ('%s.alloy_furnace.heat.1' % MODID, 'Heat: heated'),
    ('%s.alloy_furnace.heat.2' % MODID, 'Heat: superheated'),
    ('%s.alloy_furnace.fans' % MODID, 'Fans: %s (+25%% speed each, max 4)'),
    ('%s.alloy_furnace.batches' % MODID, 'Batches: %s / %s'),
    ('%s.alloy_furnace.filtered' % MODID, 'Recipe filter: %s output hatch(es)'),
    ('%s.alloy_furnace.blocker.loose' % MODID, 'Not formed: build a solid box, 2 to 4 blocks on each side'),
    ('%s.alloy_furnace.blocker.no_heat' % MODID, 'No heat: a heated Blaze Burner under every column'),
    ('%s.jade.alloy_items' % MODID, 'Ingredients: %s  Output: %s'),
    ('%s.jei.alloying' % MODID, 'Alloy Furnace'),
    ('%s.jei.alloy.heat.1' % MODID, 'Heated'),
    ('%s.jei.alloy.heat.2' % MODID, 'Superheated'),
    ('%s.jei.alloy.time' % MODID, '%ss per batch'),
    ('%s.jei.alloy.inherited' % MODID, 'from %s'),
    ('%s.jei.alloy.needs_tip' % MODID, 'A heated (or superheated) Blaze Burner under every column. Fans blowing into the bottom layer: +25%% speed each. No fuel: the burners are the running cost. Runs half its brick count of batches at once. To pick between alloys that share ingredients, wrench a brick to an output hatch and set its filter.'),
    ('config.jade.plugin_%s.alloy_furnace' % MODID, 'Alloy Furnace'),
    ('%s.display_source.machine_status' % MODID, 'Machine Status'),
    ('%s.display_source.machine_progress' % MODID, 'Machine Progress'),
    ('%s.display_source.machine_output' % MODID, 'Finished Output'),
    ('%s.display_source.creosote_level' % MODID, 'Creosote Level'),
    ('%s.display_source.engine_fuel_level' % MODID, 'Engine Fuel Level'),
    ('%s.display_source.engine_capacity' % MODID, 'Engine Stress Capacity'),
    ('%s.readout.working' % MODID, 'Working'),
    ('%s.readout.idle' % MODID, 'Idle'),
    ('advancements.%s.alloy_furnace.title' % MODID, 'Industrial Alloying'),
    ('advancements.%s.alloy_furnace.description' % MODID, 'Make Alloy Furnace Bricks'),
    ('advancements.%s.steel.description' % MODID, 'Blow air into a Blast Furnace to make steel'),
    ('item.%s.creosote_bucket' % MODID, 'Creosote Bucket'),
    ('fluid_type.%s.plant_oil' % MODID, 'Plant Oil'),
    ('block.%s.plant_oil' % MODID, 'Plant Oil'),
    ('item.%s.plant_oil_bucket' % MODID, 'Plant Oil Bucket'),
    ('fluid_type.%s.ethanol' % MODID, 'Ethanol'),
    ('block.%s.ethanol' % MODID, 'Ethanol'),
    ('item.%s.ethanol_bucket' % MODID, 'Ethanol Bucket'),
    ('fluid_type.%s.hooch' % MODID, 'Hooch'),
    ('block.%s.hooch' % MODID, 'Hooch'),
    ('item.%s.hooch_bucket' % MODID, 'Hooch Bucket'),
    ('fluid_type.%s.wine' % MODID, 'Wine'),
    ('block.%s.wine' % MODID, 'Wine'),
    ('item.%s.wine_bucket' % MODID, 'Wine Bucket'),
    ('fluid_type.%s.cider' % MODID, 'Cider'),
    ('block.%s.cider' % MODID, 'Cider'),
    ('item.%s.cider_bucket' % MODID, 'Cider Bucket'),
    ('fluid_type.%s.mead' % MODID, 'Mead'),
    ('block.%s.mead' % MODID, 'Mead'),
    ('item.%s.mead_bucket' % MODID, 'Mead Bucket'),
    ('fluid_type.%s.ale' % MODID, 'Ale'),
    ('block.%s.ale' % MODID, 'Ale'),
    ('item.%s.ale_bucket' % MODID, 'Ale Bucket'),
    ('fluid_type.%s.vodka' % MODID, 'Vodka'),
    ('block.%s.vodka' % MODID, 'Vodka'),
    ('item.%s.vodka_bucket' % MODID, 'Vodka Bucket'),
    ('fluid_type.%s.brandy' % MODID, 'Brandy'),
    ('block.%s.brandy' % MODID, 'Brandy'),
    ('item.%s.brandy_bucket' % MODID, 'Brandy Bucket'),
    ('fluid_type.%s.whiskey' % MODID, 'Whiskey'),
    ('block.%s.whiskey' % MODID, 'Whiskey'),
    ('item.%s.whiskey_bucket' % MODID, 'Whiskey Bucket'),
    ('fluid_type.%s.applejack' % MODID, 'Applejack'),
    ('block.%s.applejack' % MODID, 'Applejack'),
    ('item.%s.applejack_bucket' % MODID, 'Applejack Bucket'),
    ('block.%s.distilling_tank' % MODID, 'Distilling Tank'),
    ('%s.still.boiler' % MODID, 'Boiler: %2$s %3$s / %4$s mB'),
    ('%s.still.layer' % MODID, 'Level %1$s: %2$s %3$s / %4$s mB'),
    ('%s.still.progress' % MODID, 'Distilling %s batch(es): %s%%'),
    ('%s.still.blocker.too_short' % MODID, 'Stack it at least 2 high: boiler + one level per fraction'),
    ('%s.still.blocker.empty' % MODID, 'Fill the boiler (bottom level) with something to distil'),
    ('%s.still.blocker.height' % MODID, 'Too short for this: needs %s levels'),
    ('%s.still.blocker.cool' % MODID, 'Not hot enough: needs heat x%s under the whole boiler'),
    ('%s.still.blocker.full' % MODID, 'A level is full or holds another fluid: drain the taps'),
    ('%s.still.blocker.input' % MODID, 'Filling the boiler: %s / %s mB for a batch'),
    ('%s.jei.distilling' % MODID, 'Distilling'),
    ('%s.jei.distilling.level' % MODID, 'Level %s'),
    ('%s.jei.distilling.needs' % MODID, '%s+ levels, heat x%s'),
    ('%s.jei.distilling.time' % MODID, '%ss at heat x1'),
    ('%s.jei.distilling.tip' % MODID, 'Fluid in at the bottom (boiler); each fraction collects in its own level, lightest at the top. Wider columns run more batches at once.'),
    ('%s.jade.still_layer' % MODID, 'This level: %s'),
    ('advancements.%s.distilling.title' % MODID, 'Fractions of a Fraction'),
    ('advancements.%s.distilling.description' % MODID, 'Make a Distilling Tank'),
    ('config.jade.plugin_%s.distilling_tank' % MODID, 'Distilling Tank'),
    ('block.%s.fermenting_vat' % MODID, 'Fermenting Vat'),
    ('%s.vat.heat' % MODID, 'Heat: x%s (campfire x1, lit burner x2, superheated x3)'),
    ('%s.vat.tank.0' % MODID, 'In: %s %s / %s mB'),
    ('%s.vat.tank.1' % MODID, 'Out: %s %s / %s mB'),
    ('%s.vat.empty' % MODID, 'empty'),
    ('%s.vat.progress' % MODID, 'Fermenting %s batch(es): %s%%'),
    ('%s.vat.blocker.no_heat' % MODID, 'No heat: a campfire or Blaze Burner under it'),
    ('%s.vat.blocker.no_fluid' % MODID, 'Needs water (or the recipe fluid)'),
    ('%s.vat.blocker.no_items' % MODID, 'Needs something to ferment'),
    ('%s.vat.blocker.no_recipe' % MODID, 'Nothing ferments from this (or the output is full)'),
    ('%s.jei.fermenting' % MODID, 'Fermenting'),
    ('advancements.%s.fermenting.title' % MODID, 'Something Brewing'),
    ('advancements.%s.fermenting.description' % MODID, 'Make a Fermenting Vat'),
    ('%s.jei.fermenting.time' % MODID, '%ss at passive heat'),
    ('%s.jei.fermenting.tip' % MODID, 'Any heat under the vat: campfire or unlit Blaze Burner x1, lit x2, superheated x3. Bigger vats run more batches at once.'),
    ('%s.jade.vat_items' % MODID, 'Ingredients: %s'),
    ('config.jade.plugin_%s.fermenting_vat' % MODID, 'Fermenting Vat'),
    ('block.%s.coke_oven_bricks' % MODID, 'Coke Oven Bricks'),
    ('block.%s.coke_block' % MODID, 'Block of Coal Coke'),
    ('block.%s.creosote' % MODID, 'Creosote Oil'),
    ('fluid_type.%s.creosote' % MODID, 'Creosote Oil'),
    ('%s.jei.coking' % MODID, 'Coking'),
    ('%s.jei.coking.creosote_bonus' % MODID, 'Bonus: vented as smoke if the oven\'s tank is full'),
    ('%s.jei.coking.time' % MODID, '%ss each, 4 at a time per brick'),
    ('%s.jei.coking.heat' % MODID, 'Needs heat under the oven:'),
    ('%s.jei.coking.heat.passive' % MODID, 'Fire, campfire, magma or lava: half speed'),
    ('%s.jei.coking.heat.burner' % MODID, 'Blaze Burner (fuelled): full speed'),
    ('%s.jei.coking.heat.superheated' % MODID, 'Superheated Blaze Burner (Blaze Cake): double speed'),
    ('%s.jei.coking.heat.every_column' % MODID, 'Speed is the average over every column of the oven\'s bottom'),
    ('%s.coke_oven.size' % MODID, 'Size: %sx%sx%s'),
    ('%s.coke_oven.heat' % MODID, 'Heat: %s'),
    ('%s.coke_oven.heat.none' % MODID, 'none'),
    ('%s.coke_oven.slots' % MODID, 'Coking: %s / %s slots'),
    ('%s.coke_oven.creosote' % MODID, 'Creosote: %s / %s mB'),
    ('%s.coke_oven.blocker.no_heat' % MODID, 'No heat: put a heat source under every column'),
    ('%s.coke_oven.blocker.output_full' % MODID, 'Output full'),
    ('%s.jade.heat.passive' % MODID, 'fire/magma'),
    ('%s.jade.heat.burner' % MODID, 'blaze burner'),
    ('%s.jade.heat.superheated' % MODID, 'superheated'),
    ('%s.jade.in_out' % MODID, 'In: %s  Out: %s'),
    ('%s.jade.venting' % MODID, 'Tank full: extra creosote vents'),
    ('config.jade.plugin_%s.coke_oven' % MODID, 'Coke Oven'),
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

# Furnace bricks: lit / working show as particles and sound, not a texture swap. The wrench cycles `port`
# (bricks / input / output), a look only. Sides and top are connected textures (client/ct/, Create's RECTANGLE
# sheets *_connected.png); these models carry the lone-block textures the CT sheets replace.
def furnace_models(n):
    for port, suffix in (('bricks', ''), ('input', '_input'), ('output', '_output')):
        put('%s/models/block/%s%s.json' % (A, n, suffix), {'parent': 'minecraft:block/cube_column',
            'textures': {'end': ce('block/%s_top' % n), 'side': ce('block/%s%s' % (n, suffix))}})
    put('%s/models/item/%s.json' % (A, n), {'parent': ce('block/' + n)})
    put('%s/blockstates/%s.json' % (A, n), {'multipart': [
        {'when': {'port': p}, 'apply': {'model': ce('block/%s%s' % (n, sfx))}}
        for p, sfx in (('bricks', ''), ('input', '_input'), ('output', '_output'))]})
furnace_models('coke_oven_bricks')
furnace_models('blast_furnace_bricks')
furnace_models('alloy_furnace_bricks')
put('%s/loot_table/blocks/coke_oven_bricks.json' % D, {
    'type': 'minecraft:block',
    'pools': [{'rolls': 1, 'bonus_rolls': 0, 'entries': [{'type': 'minecraft:item', 'name': ce('coke_oven_bricks')}],
               'conditions': [{'condition': 'minecraft:survives_explosion'}]}],
    'random_sequence': ce('blocks/coke_oven_bricks')})
put('%s/loot_table/blocks/alloy_furnace_bricks.json' % D, {
    'type': 'minecraft:block',
    'pools': [{'rolls': 1, 'bonus_rolls': 0, 'entries': [{'type': 'minecraft:item', 'name': ce('alloy_furnace_bricks')}],
               'conditions': [{'condition': 'minecraft:survives_explosion'}]}],
    'random_sequence': ce('blocks/alloy_furnace_bricks')})
put('%s/loot_table/blocks/blast_furnace_bricks.json' % D, {
    'type': 'minecraft:block',
    'pools': [{'rolls': 1, 'bonus_rolls': 0, 'entries': [{'type': 'minecraft:item', 'name': ce('blast_furnace_bricks')}],
               'conditions': [{'condition': 'minecraft:survives_explosion'}]}],
    'random_sequence': ce('blocks/blast_furnace_bricks')})
# Creosote bucket: built at runtime by NeoForge from whatever bucket texture is loaded (vanilla, Excalibur, any
# pack) with our creosote texture masked into it. No bucket texture of our own.
# Tinted fluids (ModFluids.Tinted): bucket models fill from the fluid at runtime, like creosote's.
TINTED = [('plant_oil', 'Plant Oil'), ('ethanol', 'Ethanol'), ('hooch', 'Hooch'), ('wine', 'Wine'), ('cider', 'Cider'),
          ('mead', 'Mead'), ('ale', 'Ale'), ('vodka', 'Vodka'), ('brandy', 'Brandy'), ('whiskey', 'Whiskey'),
          ('applejack', 'Applejack'), ('crude_oil', 'Crude Oil'), ('diesel', 'Diesel'), ('kerosene', 'Kerosene'),
          ('gasoline', 'Gasoline'), ('heavy_oil', 'Heavy Oil'), ('lubricant', 'Lubricant')]
for n, _ in TINTED:
    put('%s/models/item/%s_bucket.json' % (A, n), {'parent': 'neoforge:item/bucket_drip', 'loader': 'neoforge:fluid_container', 'fluid': ce(n)})
    put('%s/blockstates/%s.json' % (A, n), {'variants': {'': {'model': ce('block/' + n)}}})
    put('%s/models/block/%s.json' % (A, n), {'textures': {'particle': 'minecraft:block/water_still'}})
put('%s/models/item/creosote_bucket.json' % A, {'parent': 'neoforge:item/bucket_drip',
    'loader': 'neoforge:fluid_container', 'fluid': ce('creosote')})
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

# Tier 3: blast smelting (docs/steelworks-spec.md 4). result_cast without air, result_blast with fans.
# iron_units = fuel units used per item (data map blast_furnace_fuel). time = ticks at x1 speed.
def blast(name, ingredient, cast, steel, units=1, slag=0.0, time=200):
    put(R + '/blast_smelting/%s.json' % name, {'type': ce('blast_smelting'), 'ingredient': ingredient,
        'result_cast': {'id': cast}, 'result_blast': {'id': steel}, 'iron_units': units, 'slag_chance': slag, 'time': time})
blast('iron_ingot', tag('c:ingots/iron'), ce('cast_iron_ingot'), ce('steel_ingot'))
blast('raw_iron', tag('c:raw_materials/iron'), ce('cast_iron_ingot'), ce('steel_ingot'), slag=0.25)
blast('crushed_raw_iron', item('create:crushed_raw_iron'), ce('cast_iron_ingot'), ce('steel_ingot'), slag=0.25)
blast('blister_steel', tag('c:ingots/blister_steel'), ce('steel_ingot'), ce('steel_ingot'), units=0.5, time=100)
blast('iron_block', tag('c:storage_blocks/iron'), ce('cast_iron_block'), ce('steel_block'), units=9, time=1800)
put('%s/data/%s/data_maps/item/blast_furnace_fuel.json' % (RES, MODID), {'values': {
    '#c:coal_coke': {'iron_units': 2}, ce('coke_block'): {'iron_units': 18},
    'minecraft:charcoal': {'iron_units': 1}, 'minecraft:coal': {'iron_units': 0.5}}})
for metal in ('cast_iron', 'steel'):
    put(R + '/pressing/%s_sheet.json' % metal, {'type': 'create:pressing', 'ingredients': [tag('c:ingots/' + metal)],
        'results': [{'id': ce(metal + '_sheet')}]})
    shaped(metal + '_block', ['###', '###', '###'], {'#': tag('c:ingots/' + metal)}, ce(metal + '_block'), category='building')
    shapeless(metal + '_ingot_from_block', [item(ce(metal + '_block'))], ce(metal + '_ingot'), 9)
    shaped(metal + '_ingot_from_nuggets', ['###', '###', '###'], {'#': tag('c:nuggets/' + metal)}, ce(metal + '_ingot'), category='misc')
    shapeless(metal + '_nugget', [tag('c:ingots/' + metal)], ce(metal + '_nugget'), 9)
shaped('blast_furnace_bricks', ['NBN', 'BSB', 'NBN'], {'B': item('minecraft:bricks'), 'N': item('minecraft:nether_brick'),
       'S': tag('c:plates/blister_steel')}, ce('blast_furnace_bricks'), count=2, category='building')

# Tier 4: alloying (docs/steelworks-spec.md 4b). Ingredients use the flat form Ender IO uses ({"tag": ..., "count": n}).
# Create's heated mixing alloys (brass...) and Ender IO's alloy_smelting ingots (base + Evolution) are read at runtime,
# so they need no JSON here. These are optional extras for other mods' metals, loaded only with that mod.
def alloy(name, ingredients, result, count, mod, heat=1, time=200):
    put(R + '/alloying/%s.json' % name, {'neoforge:conditions': [{'type': 'neoforge:mod_loaded', 'modid': mod}],
        'type': ce('alloying'), 'ingredients': [dict(i, count=n) for i, n in ingredients],
        'result': {'id': result, 'count': count}, 'heat': heat, 'time': time})
# Our own: bulk cementation. Same ratio as compacting with coke (2 iron + 1 coke -> 2 carbon-packed iron -> 2
# blister steel), in one step: the furnace does the baking the fan would.
put(R + '/alloying/blister_steel.json', {'type': ce('alloying'),
    'ingredients': [dict(tag('c:ingots/iron'), count=2), dict(tag('c:coal_coke'), count=1)],
    'result': {'id': ce('blister_steel_ingot'), 'count': 2}, 'heat': 1, 'time': 200})
alloy('ie_electrum', [(tag('c:ingots/gold'), 1), (tag('c:ingots/silver'), 1)], 'immersiveengineering:ingot_electrum', 2, 'immersiveengineering')
alloy('ie_constantan', [(tag('c:ingots/copper'), 1), (tag('c:ingots/nickel'), 1)], 'immersiveengineering:ingot_constantan', 2, 'immersiveengineering')
shaped('alloy_furnace_bricks', ['SBS', 'BFB', 'SBS'], {'S': tag('c:plates/steel'), 'B': item(ce('blast_furnace_bricks')),
       'F': item('minecraft:blast_furnace')}, ce('alloy_furnace_bricks'), count=4, category='building')

# Metalworking: press dies. A die fitted in a Mechanical Press makes it run only these recipes (results in Create's
# output format). Dies are one-time steel work and never wear out.
def die_press(name, die, ingredient, result, count):
    put(R + '/die_pressing/%s.json' % name, {'type': ce('die_pressing'), 'die': item(ce(die)), 'ingredient': ingredient,
        'results': [{'id': ce(result), 'count': count}]})
for metal in ('copper', 'iron', 'steel'):
    die_press(metal + '_rod', 'rod_die', tag('c:ingots/' + metal), metal + '_rod', 2)
for metal in ('iron', 'steel'):
    die_press(metal + '_gear', 'gear_die', tag('c:plates/' + metal), metal + '_gear', 1)
# Copper and steel wire items exist (art + c:wires tags) but have no recipe yet: kept for later modules (wire rope).
for d, core in (('rod_die', item('minecraft:stick')), ('gear_die', item('create:cogwheel'))):
    shaped(d, ['SSS', 'S#S', 'SSS'], {'S': tag('c:plates/steel'), '#': core}, ce(d), category='equipment')

# Treated timber: coke-oven creosote soaked into wood. Spout it onto planks / stripped logs on a belt, or (early
# game) craft 8 around a creosote bucket. Beams use Create's girder logic with our own heavy-timber models.
def simple_loot(b, slab=False):
    entry = {'type': 'minecraft:item', 'name': ce(b)}
    if slab:
        entry['functions'] = [{'function': 'minecraft:set_count', 'count': 2, 'add': False, 'conditions': [
            {'condition': 'minecraft:block_state_property', 'block': ce(b), 'properties': {'type': 'double'}}]},
            {'function': 'minecraft:explosion_decay'}]
    put('%s/loot_table/blocks/%s.json' % (D, b), {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'bonus_rolls': 0,
        'entries': [entry], 'conditions': [] if slab else [{'condition': 'minecraft:survives_explosion'}]}],
        'random_sequence': ce('blocks/' + b)})

TP = ce('block/treated_planks')
put('%s/models/block/treated_planks.json' % A, {'parent': 'minecraft:block/cube_all', 'textures': {'all': TP}})
for n, parent in (('treated_stairs', 'stairs'), ('treated_stairs_inner', 'inner_stairs'), ('treated_stairs_outer', 'outer_stairs'),
                  ('treated_slab', 'slab'), ('treated_slab_top', 'slab_top')):
    put('%s/models/block/%s.json' % (A, n), {'parent': 'minecraft:block/' + parent, 'textures': {'bottom': TP, 'top': TP, 'side': TP}})
put('%s/blockstates/treated_planks.json' % A, {'variants': {'': {'model': TP}}})
put('%s/blockstates/treated_slab.json' % A, {'variants': {'type=bottom': {'model': ce('block/treated_slab')},
    'type=top': {'model': ce('block/treated_slab_top')}, 'type=double': {'model': TP}}})
stairs = {}
for facing, ry in (('east', 0), ('south', 90), ('west', 180), ('north', 270)):
    for half in ('bottom', 'top'):
        for shape in ('straight', 'inner_left', 'inner_right', 'outer_left', 'outer_right'):
            model = ce('block/treated_stairs' + ('_inner' if shape.startswith('inner') else '_outer' if shape.startswith('outer') else ''))
            y = ry
            if shape in ('inner_left', 'outer_left'):
                y -= 90 if half == 'bottom' else 0
            if shape in ('inner_right', 'outer_right') and half == 'top':
                y += 90
            v = {'model': model}
            if half == 'top':
                v['x'] = 180
            if y % 360:
                v['y'] = y % 360
            if half == 'top' or y % 360:
                v['uvlock'] = True
            stairs['facing=%s,half=%s,shape=%s' % (facing, half, shape)] = v
put('%s/blockstates/treated_stairs.json' % A, {'variants': stairs})
for n, side, end in (('treated_log', 'treated_log', 'treated_log_top'), ('treated_wood', 'treated_log', 'treated_log')):
    put('%s/models/block/%s.json' % (A, n), {'parent': 'minecraft:block/cube_column', 'textures': {'end': ce('block/' + end), 'side': ce('block/' + side)}})
    put('%s/models/block/%s_horizontal.json' % (A, n), {'parent': 'minecraft:block/cube_column_horizontal', 'textures': {'end': ce('block/' + end), 'side': ce('block/' + side)}})
    put('%s/blockstates/%s.json' % (A, n), {'variants': {'axis=y': {'model': ce('block/' + n)},
        'axis=z': {'model': ce('block/%s_horizontal' % n), 'x': 90},
        'axis=x': {'model': ce('block/%s_horizontal' % n), 'x': 90, 'y': 90}}})
# Beam models (our own geometry): a heavy 8x8 post, 6x10 joists along X / Z, stubs up and down, a bolster at crossings.
B, BE = ce('block/treated_beam'), ce('block/treated_beam_end')
def faces(end_axis='y', along=None):
    """Ends get the end grain; sides get grain running along the beam: '#side' (vertical grain) or '#side_h'
    (grain turned on its side). Pre-rotated textures, never face rotation: these faces aren't square, so rotating
    the UV squeezes the texture."""
    f = {}
    for d in ('north', 'south', 'east', 'west', 'up', 'down'):
        axis = 'z' if d in ('north', 'south') else 'x' if d in ('east', 'west') else 'y'
        if axis == end_axis:
            f[d] = {'texture': '#end'}
        elif along == 'x':
            f[d] = {'texture': '#side_h'}                        # every side face runs left-right
        elif along == 'z':
            f[d] = {'texture': '#side' if axis == 'y' else '#side_h'}   # top/bottom: grain along v = z
        else:
            f[d] = {'texture': '#side'}
    return f
def beam_model(name, elements):
    put('%s/models/block/treated_beam_%s.json' % (A, name), {'parent': 'minecraft:block/block',
        'textures': {'side': B, 'side_h': ce('block/treated_beam_horizontal'), 'end': BE, 'particle': B}, 'elements': elements})
beam_model('pole', [{'from': [4, 0, 4], 'to': [12, 16, 12], 'faces': faces(end_axis='y')}])
beam_model('x', [{'from': [0, 3, 5], 'to': [16, 13, 11], 'faces': faces(end_axis='x', along='x')}])
beam_model('z', [{'from': [5, 3, 0], 'to': [11, 13, 16], 'faces': faces(end_axis='z', along='z')}])
beam_model('top', [{'from': [5, 13, 5], 'to': [11, 16, 11], 'faces': faces(end_axis='y')}])
beam_model('bottom', [{'from': [5, 0, 5], 'to': [11, 3, 11], 'faces': faces(end_axis='y')}])
beam_model('cross', [{'from': [4, 2, 4], 'to': [12, 14, 12], 'faces': faces(end_axis='y')}])
mp = [{'when': {'x': 'false', 'z': 'false'}, 'apply': {'model': ce('block/treated_beam_pole')}},
      {'when': {'x': 'true'}, 'apply': {'model': ce('block/treated_beam_x')}},
      {'when': {'z': 'true'}, 'apply': {'model': ce('block/treated_beam_z')}},
      {'when': {'x': 'true', 'z': 'true'}, 'apply': {'model': ce('block/treated_beam_cross')}}]
for xx in (True, False):
    for part in ('top', 'bottom'):
        mp.append({'when': {part: 'true', 'x': str(xx).lower(), 'z': str(not xx).lower()}, 'apply': {'model': ce('block/treated_beam_' + part)}})
put('%s/blockstates/treated_beam.json' % A, {'multipart': mp})
for n in ('treated_planks', 'treated_stairs', 'treated_slab', 'treated_log', 'treated_wood'):
    put('%s/models/item/%s.json' % (A, n), {'parent': ce('block/' + n)})
    simple_loot(n, slab=n == 'treated_slab')
put('%s/models/item/treated_beam.json' % A, {'parent': ce('block/treated_beam_pole')})
simple_loot('treated_beam')

CREOSOTE_IN = lambda mb: {'type': 'neoforge:tag', 'tag': 'c:creosote', 'amount': mb}
for name, src, out, mb in (('treated_planks', tag('minecraft:planks'), 'treated_planks', 125),
                           ('treated_log', tag('c:stripped_logs'), 'treated_log', 250),
                           ('treated_wood', tag('c:stripped_woods'), 'treated_wood', 250)):
    put(R + '/filling/%s.json' % name, {'type': 'create:filling', 'ingredients': [src, CREOSOTE_IN(mb)],
        'results': [{'id': ce(out)}]})
    shaped(name + '_by_bucket', ['###', '#B#', '###'], {'#': src, 'B': item(ce('creosote_bucket'))}, ce(out), count=8, category='building')
shapeless('treated_planks_from_log', [tag(ce('treated_logs'))], ce('treated_planks'), 4)
shaped('treated_wood', ['##', '##'], {'#': item(ce('treated_log'))}, ce('treated_wood'), count=3, category='building')
shaped('treated_stairs', ['#  ', '## ', '###'], {'#': item(ce('treated_planks'))}, ce('treated_stairs'), count=4, category='building')
shaped('treated_slab', ['###'], {'#': item(ce('treated_planks'))}, ce('treated_slab'), count=6, category='building')
shaped('treated_beam', ['T', 'N', 'T'], {'T': tag(ce('treated_logs')), 'N': tag('c:nuggets/iron')}, ce('treated_beam'), count=4, category='building')

# Module 1: diesel engine (docs/engines-spec.md). FIRST PASS: blocks + models for Kragnoth's look approval; no
# behaviour yet. Engine axis = X. Cylinders sit in the block diagonally above the crankcase and lean 45 degrees
# outward (+Z bank and -Z bank), their axis passing through the crankshaft. ALL textures are Create's own (Kragnoth,
# 2026-11-22): a resource pack that reskins Create reskins the engine too. Starting: any rotation into the crankcase's
# front shaft (e.g. Create's Hand Crank); there is no starter block.
CR = lambda t: 'create:block/' + t
def _auto_uv(e, d):
    """Vanilla's position-derived UV for face d, moved back inside 0..16. Parts that stick out of the block (flywheel
    rim, cylinder heads) otherwise sample outside their sprite: black faces with stray specks in game."""
    (x0, y0, z0), (x1, y1, z1) = e['from'], e['to']
    uv = {'north': [16 - x1, 16 - y1, 16 - x0, 16 - y0], 'south': [x0, 16 - y1, x1, 16 - y0],
          'east': [16 - z1, 16 - y1, 16 - z0, 16 - y0], 'west': [z0, 16 - y1, z1, 16 - y0],
          'up': [x0, z0, x1, z1], 'down': [x0, 16 - z1, x1, 16 - z0]}[d]
    out = []
    for lo, hi in ((uv[0], uv[2]), (uv[1], uv[3])):
        span = hi - lo
        if span > 16:
            lo, hi = 0, 16
        else:
            while lo < 0:
                lo, hi = lo + 16, hi + 16
            while hi > 16:
                lo, hi = lo - 16, hi - 16
            if lo < 0:
                lo, hi = 0, span
        out.append((round(lo, 4), round(hi, 4)))
    return [out[0][0], out[1][0], out[0][1], out[1][1]]

def eng(name, textures, elements):
    for e in elements:
        for v in e['from'] + e['to']:
            assert -16 <= v <= 32, '%s: element coordinate %s outside -16..32 (vanilla drops the whole model)' % (name, v)
        for d, f in e['faces'].items():
            if 'uv' not in f:
                f['uv'] = _auto_uv(e, d)
    t = dict(textures)
    t.setdefault('particle', t[next(iter(t))])
    put('%s/models/block/%s.json' % (A, name), {'parent': 'minecraft:block/block', 'textures': t, 'elements': elements})
def box(f, t, tex, faces=None, rot=None):
    fs = {d: {'texture': '#' + (faces.get(d, tex) if faces else tex)} for d in ('north', 'south', 'east', 'west', 'up', 'down')}
    e = {'from': f, 'to': t, 'faces': fs}
    if rot:
        e['rotation'] = rot
    return e
def axis_x(x0=0, x1=16):
    """Create's shaft, laid along X: 4x4 at 6..10 with axis / axis_top and Create's own UVs, so it mates with any Create
    shaft or kinetic block exactly."""
    e = box([x0, 6, 6], [x1, 10, 10], 'shaft', {'east': 'shaft_end', 'west': 'shaft_end'})
    for d in ('north', 'south', 'up', 'down'):
        e['faces'][d].update({'uv': [6, 16 - x1, 10, 16 - x0], 'rotation': 90})
    for d in ('east', 'west'):
        e['faces'][d]['uv'] = [6, 6, 10, 10]
    return e

def oct_y(c, y0, y1, w, ch, tex, faces=None, rot=None):
    """An octagonal prism along Y (two crossed boxes), centred on (c, c) in X/Z."""
    h = w / 2
    return [box([c - h, y0, c - h + ch], [c + h, y1, c + h - ch], tex, faces, rot),
            box([c - h + ch, y0, c - h], [c + h - ch, y1, c + h], tex, faces, rot)]

def oct_x(x0, x1, w, ch, tex, faces=None, rot=None, cy=8, cz=8):
    """An octagonal prism along X (two crossed boxes), centred on (cy, cz)."""
    h = w / 2
    return [box([x0, cy - h, cz - h + ch], [x1, cy + h, cz + h - ch], tex, faces, rot),
            box([x0, cy - h + ch, cz - h], [x1, cy + h - ch, cz + h], tex, faces, rot)]

ENG_T = {'iron': CR('industrial_iron_block'), 'iron_top': CR('industrial_iron_block_top'), 'dark': CR('dark_metal_block'),
         'casing': CR('railway_casing'), 'brass': CR('brass_block'), 'shaft': CR('axis'), 'shaft_end': CR('axis_top')}

# Crankcase: stepped bedplate with hold-down bolts, cast-iron housing with strengthening ribs and riveted access doors,
# octagonal bearing rings at both ends (two crankcases side by side read as a flanged joint), cylinder saddles leaning
# to both banks, a rounded camshaft cover with brass oil cups. The crankshaft (Create's shaft) runs through: front =
# starting input, back = output.
cc = [box([0, 0, 0], [16, 2, 16], 'dark'), box([0, 2, 0.5], [16, 3, 15.5], 'dark'),
      box([2, 3, 1], [14, 8, 15], 'iron', {'up': 'iron_top', 'down': 'dark'}),          # housing recessed 2 px at the
      box([2, 8, 2.5], [14, 13, 13.5], 'iron', {'up': 'iron_top', 'down': 'dark'})]     # ends: the crankshaft shows
for x in (1.5, 13):                                            # hold-down bolts
    for z in (0.75, 14):
        cc.append(box([x, 2, z], [x + 1.5, 3, z + 1.25], 'shaft', {'up': 'shaft_end', 'down': 'shaft_end'}))
for z0, z1, z2, z3 in ((15, 15.6, 13.5, 14.1), (0.4, 1, 1.9, 2.5)):   # ribs, lower and upper, both sides
    for x in (2, 12.5):
        cc.append(box([x, 3, z0], [x + 1.5, 8, z1], 'dark'))
        cc.append(box([x, 8, z2], [x + 1.5, 13, z3], 'dark'))
cc += [box([5, 3.5, 15], [11, 7.5, 15.6], 'casing'), box([5, 3.5, 0.4], [11, 7.5, 1], 'casing')]   # access doors
cc += oct_x(1, 2, 9, 2.5, 'casing') + oct_x(14, 15, 9, 2.5, 'casing')   # bearing rings against the recessed housing
cc += [box([0.5, 11, 11], [15.5, 14, 17], 'dark', rot={'origin': [8, 12.5, 14], 'axis': 'x', 'angle': 45}),
       box([0.5, 11, -1], [15.5, 14, 5], 'dark', rot={'origin': [8, 12.5, 2], 'axis': 'x', 'angle': -45}),
       box([0, 13, 5.5], [16, 15, 10.5], 'casing'), box([0, 15, 6.5], [16, 16, 9.5], 'casing')]      # cam cover
for x in (3, 11):                                                                                      # oil cups
    cc += [box([x + 0.5, 16, 7.5], [x + 1.5, 16.5, 8.5], 'brass'), box([x, 16.5, 7], [x + 2, 18, 9], 'brass')]
eng('diesel_crankcase', ENG_T, cc)
# The crankshaft is a separate partial spun like any Create shaft (0.9.9; Kragnoth noticed it stood still): Create's
# shaft through the housing, an off-centre crank web pair and pin so the turning reads, and stubs poking 2 px past
# both faces. The stubs are a hair thinner than a shaft (3.5 vs 4), so with anything attached (crankcase, Create
# shaft, flywheel, hand crank) they sit inside its shaft: hidden, no z-fighting.
crank = [axis_x(),
         box([4.5, 5.5, 6], [6, 12, 10], 'dark'), box([10, 5.5, 6], [11.5, 12, 10], 'dark'),     # crank webs
         box([6, 10, 7.25], [10, 11.5, 8.75], 'iron')]                                         # crank pin (big end)
for x0, x1 in ((-2, 0), (16, 18)):
    st = box([x0, 6.25, 6.25], [x1, 9.75, 9.75], 'shaft', {'east': 'shaft_end', 'west': 'shaft_end'})
    for d in ('north', 'south', 'up', 'down'):
        st['faces'][d].update({'uv': [6.25, 0, 9.75, 2], 'rotation': 90})
    for d in ('east', 'west'):
        st['faces'][d]['uv'] = [6.25, 6.25, 9.75, 9.75]
    crank.append(st)
eng('diesel_crankshaft', ENG_T, crank)

# Cylinder: octagonal flange with bolts, octagonal barrel with cooling bands, octagonal head with studs, domed rocker
# cover, brass injector and fuel line, pushrod tube. Built upright, then leaned 45 deg about X through the block centre
# (the axis runs through the crankshaft diagonally below). Smoke comes off the head while running (particles).
def cylinder(angle):
    r = {'origin': [8, 8, 8], 'axis': 'x', 'angle': angle}
    e = oct_y(8, -5, -3, 12, 2.5, 'dark', rot=r)
    for x, z in ((3, 3), (12, 3), (3, 12), (12, 12)):
        e.append(box([x - 0.5, -3, z - 0.5], [x + 0.5, -2.5, z + 0.5], 'shaft', rot=r))
    e += oct_y(8, -3, 13, 9, 2, 'iron', {'up': 'dark', 'down': 'dark'}, rot=r)
    for y in (0, 4.5, 9):
        e += oct_y(8, y, y + 1, 10, 2.5, 'dark', rot=r)
    e += oct_y(8, 13, 17, 12, 2.5, 'dark', rot=r)
    for x, z in ((4, 4), (11, 4), (4, 11), (11, 11)):
        e.append(box([x - 0.75, 17, z - 0.75], [x + 0.75, 18, z + 0.75], 'shaft', {'up': 'shaft_end'}, rot=r))
    e += [box([5, 17, 4.5], [11, 19, 11.5], 'casing', rot=r), box([6, 19, 5.5], [10, 20, 10.5], 'casing', rot=r),
          box([10.5, 17, 10.5], [12, 19.5, 12], 'brass', rot=r),                    # injector at the head's corner
          box([10.75, 13, 12], [11.75, 18, 13], 'brass', rot=r),                    # its fuel line
          box([3.75, 18.5, 7.75], [4.75, 20, 8.75], 'shaft', rot=r),                # valve stems under the rocker ends
          box([11.25, 18.5, 7.75], [12.25, 20, 8.75], 'shaft', rot=r),
          box([12.5, -3, 7.5], [13.5, 17, 8.5], 'shaft', rot=r)]
    return e
eng('diesel_cylinder', ENG_T, cylinder(45))            # leans toward +Z (south bank)
# Rocker arm: a PARTIAL model (drawn moving by the cylinder's visual, not baked into the block). Upright model space;
# the visual leans it 45 deg like the block and nods it about the pivot (8, 20.5, 8.25), axis Z.
eng('diesel_cylinder_rocker', ENG_T, [box([3, 20, 7.5], [13, 21, 9], 'iron', {'up': 'iron_top'}),
                                      box([7.25, 19.75, 7], [8.75, 21.25, 9.5], 'shaft', {'east': 'shaft_end', 'west': 'shaft_end'}),
                                      box([3, 19.5, 7.75], [4.5, 20, 8.75], 'brass'), box([11.5, 19.5, 7.75], [13, 20, 8.75], 'brass')])
eng('diesel_cylinder_mirrored', ENG_T, cylinder(-45))  # leans toward -Z (north bank)

# Flywheel: 3-block cast-iron wheel on Create's shaft (mates exactly; passes rotation on). Octagonal hub with boss rings,
# 8 spokes with thick roots, 16-segment rim with an inner lip.
fw = [axis_x()] + oct_x(5, 11, 9, 2.5, 'dark') + oct_x(4, 5, 7, 2, 'casing') + oct_x(11, 12, 7, 2, 'casing')
def ring(y0, y1, x0, x1, half, tex):
    out = []
    for base in (([x0, 8 + y0, 8 - half], [x1, 8 + y1, 8 + half]), ([x0, 8 - y1, 8 - half], [x1, 8 - y0, 8 + half]),
                 ([x0, 8 - half, 8 + y0], [x1, 8 + half, 8 + y1]), ([x0, 8 - half, 8 - y1], [x1, 8 + half, 8 - y0])):
        for ang in (-22.5, 0, 22.5, 45):
            out.append(box(base[0], base[1], tex, rot={'origin': [8, 8, 8], 'axis': 'x', 'angle': ang} if ang else None))
    return out
fw += ring(18, 22, 5.5, 10.5, 4.6, 'dark') + ring(17, 18, 6.5, 9.5, 3.8, 'casing')
for ang in (0, 45):
    rr = {'origin': [8, 8, 8], 'axis': 'x', 'angle': ang} if ang else None
    fw += [box([7, -10, 7], [9, 26, 9], 'iron', rot=rr), box([7, 7, -10], [9, 9, 26], 'iron', rot=rr),
           box([6.5, 0, 6.5], [9.5, 16, 9.5], 'iron', rot=rr), box([6.5, 6.5, 0], [9.5, 9.5, 16], 'iron', rot=rr)]
eng('diesel_flywheel', ENG_T, fw)
# Drive Sprocket (id drive_pulley; 0.11.2, was a drum): Create's own small cogwheel model, laid on the block's axis.
# The block renders through its block entity (spinning); this model is for particles / the item. The Drive Chain (id
# drive_belt) is drawn with Create's chain-conveyor chain.
put('%s/models/block/drive_pulley.json' % A, {'parent': 'create:block/cogwheel'})
put('%s/blockstates/drive_pulley.json' % A, {'variants': {'axis=x': {'model': ce('block/drive_pulley'), 'x': 90, 'y': 90},
                                                          'axis=z': {'model': ce('block/drive_pulley'), 'x': 90}}})
put('%s/models/item/drive_pulley.json' % A, {'parent': 'create:block/cogwheel'})
put('%s/models/item/drive_belt.json' % A, {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'minecraft:item/chain'}})
simple_loot('drive_pulley')
shapeless('drive_pulley', [item('create:cogwheel'), item('minecraft:iron_nugget')], ce('drive_pulley'), category='redstone')
shapeless('drive_belt', [item('minecraft:chain'), item('minecraft:chain'), item('create:andesite_alloy')], ce('drive_belt'))
for k, v in (('block.%s.drive_pulley', 'Drive Sprocket'), ('item.%s.drive_belt', 'Drive Chain'),
             ('%s.drive_belt.started', 'Chain started: now click the other wheel'),
             ('%s.drive_belt.taken', 'That wheel already has a chain'),
             ('%s.drive_belt.not_wheels', 'A Drive Chain goes between two wheels: flywheels or Drive Sprockets'),
             ('%s.drive_belt.plane', 'Both wheels must sit in the same plane across their shafts'),
             ('%s.drive_belt.too_close', 'Too close: the wheels would overlap'),
             ('%s.drive_belt.too_far', 'Too far: 32 blocks at most'),
             ('%s.drive_belt.axis', 'Both wheels must turn on the same axis')):
    LANG[k % MODID] = v

# Fermenting Vat (Kragnoth, 2026-11-22: "start over from Create's tanks, colour-shifted to industrial steel").
# vat_tank* are NOT shipped: the client atlas source creativeengineering:recolor makes them at load time from Create's
# own fluid tank textures (desaturated, cool steel tint), so no Create pixels are in the jar and resource packs that
# restyle Create's tanks restyle the vat too. Connected exactly like Create's tank (VatCTBehaviour). One brass
# pressure-release valve top-middle. Every face is cull-tagged: skipRendering hides walls between vat blocks.
VAT_TANK = [('create:block/fluid_tank', 'vat_tank'), ('create:block/fluid_tank_connected', 'vat_tank_connected'),
            ('create:block/fluid_tank_top', 'vat_tank_top'), ('create:block/fluid_tank_top_connected', 'vat_tank_top_connected'),
            # the Steel Storage Tank's window and inner sheets
            ('create:block/fluid_tank_inner', 'vat_tank_inner'), ('create:block/fluid_tank_inner_connected', 'vat_tank_inner_connected'),
            ('create:block/fluid_tank_window', 'vat_tank_window'), ('create:block/fluid_tank_window_single', 'vat_tank_window_single'),
            # Steel Pipe: Create's pipe sheets
            ('create:block/pipes', 'steel_pipes'), ('create:block/pipes_connected', 'steel_pipes_connected'),
            # Tap: Create's fluid valve sheets
            ('create:block/fluid_valve', 'steel_valve'), ('create:block/valve_open', 'steel_valve_open'), ('create:block/valve_closed', 'steel_valve_closed')]
# The Tap has no shaft: blank Create's valve shaft socket (the face at px 18..30 x 2..14 of the 32px sheet) into a
# flat bolted cover plate: rim and bolts in the frame's dark line colour, plate in the frame colour. Fractions of the image.
_f = lambda x0, y0, x1, y1, fx, fy: {'rect': [x0 / 32, y0 / 32, x1 / 32, y1 / 32], 'from': [fx / 32, fy / 32]}
VAT_FILLS = {'steel_valve': [_f(20, 3, 28, 12, 19.5, 3.5), _f(21, 4, 27, 11, 24.5, 2.5)]
             + [_f(x, y, x + 1, y + 1, 19.5, 3.5) for x in (22, 25) for y in (5, 9)]}   # four bolt heads
put('%s/assets/minecraft/atlases/blocks.json' % RES, {'sources': [{
    'type': ce('recolor'), 'tint': '#C9D3DE', 'saturation': 0.12, 'contrast': 1.1, 'brightness': -0.04,
    'sprites': [dict({'source': src, 'target': ce('block/' + dst)}, **({'fills': VAT_FILLS[dst]} if dst in VAT_FILLS else {}))
                for src, dst in VAT_TANK]}]})
VAT_T = {'side': ce('block/vat_tank'), 'top': ce('block/vat_tank_top'), 'dark': CR('dark_metal_block'), 'brass': CR('brass_block')}
eng('fermenting_vat_body', VAT_T, [{'from': [0, 0, 0], 'to': [16, 16, 16], 'faces': {
    d: {'texture': '#top' if d in ('up', 'down') else '#side', 'uv': [0, 0, 16, 16], 'cullface': d}
    for d in ('north', 'south', 'east', 'west', 'up', 'down')}}])
eng('fermenting_vat_valve', VAT_T, [box([7, 16, 7], [9, 18.5, 9], 'brass'),
                                    box([5, 18.5, 7.5], [11, 19.25, 8.5], 'dark'), box([7.5, 18.5, 5], [8.5, 19.25, 11], 'dark'),
                                    box([7.25, 19.25, 7.25], [8.75, 20, 8.75], 'brass'),
                                    box([9, 16.5, 7.5], [11.5, 17.5, 8.5], 'brass')])          # vent spout
put('%s/blockstates/fermenting_vat.json' % A, {'multipart': [
    {'apply': {'model': ce('block/fermenting_vat_body')}},
    {'when': {'valve': 'true'}, 'apply': {'model': ce('block/fermenting_vat_valve')}}]})
vat_item = load_json('%s/models/block/fermenting_vat_body.json' % A)
vat_item['elements'] += load_json('%s/models/block/fermenting_vat_valve.json' % A)['elements']
for e in vat_item['elements']:
    for f in e['faces'].values():
        f.pop('cullface', None)
put('%s/models/item/fermenting_vat.json' % A, vat_item)
simple_loot('fermenting_vat')

# Distilling Tank (docs/distilling-spec.md, 0.12.1): the vat's steel Create tank, plus industrial plumbing greebles
# picked per column (Plumbing.java): vertical steel pipe lanes against the left / right edge of a face block, bending
# into the wall at the boiler and over the roof edge at the top, header pipes across a face, a relief valve with a
# gooseneck top-middle. Greebles are modelled on the north face and turned for the others.
ST_T = {'side': ce('block/vat_tank'), 'top': ce('block/vat_tank_top'), 'pipe': CR('pipes'),   # Create's own copper pipes
        'brass': CR('brass_block'), 'dark': CR('dark_metal_block'), 'iron': CR('industrial_iron_block')}
PIPE_SIDE, PIPE_END = [0, 0, 4, 8], [1, 11, 6, 16]   # Create's pipe sheet: straight body / end disc
def pipe_box(frm, to, along):
    """A steel pipe segment; 'along' is its axis (y, x or z) so the body texture runs the right way."""
    faces = {}
    for d in ('north', 'south', 'east', 'west', 'up', 'down'):
        end = (along == 'y' and d in ('up', 'down')) or (along == 'x' and d in ('east', 'west')) or (along == 'z' and d in ('north', 'south'))
        f = {'texture': '#pipe', 'uv': PIPE_END if end else PIPE_SIDE}
        if not end and (along == 'x' or (along == 'z' and d in ('up', 'down'))):
            f['rotation'] = 90
        faces[d] = f
    return {'from': frm, 'to': to, 'faces': faces}
def clamp(x0, y, x1):   # pipe bracket bolted to the wall
    return box([x0, y, -1], [x1, y + 1, 0], 'dark')
LANES = {'left': (2, 5), 'right': (11, 14)}
for lane, (x0, x1) in LANES.items():
    eng('distilling_tank_pipe_%s' % lane, ST_T, [pipe_box([x0, 0, -3.5], [x1, 16, -0.5], 'y'),
                                                 clamp(x0 - 0.5, 5, x1 + 0.5), clamp(x0 - 0.5, 12, x1 + 0.5)])
    eng('distilling_tank_pipe_%s_top' % lane, ST_T, [pipe_box([x0, 0, -3.5], [x1, 15.5, -0.5], 'y'),
                                                     pipe_box([x0 - 0.25, 15.5, -3.75], [x1 + 0.25, 18.5, -0.25], 'y'),   # elbow
                                                     pipe_box([x0, 15.5, -0.25], [x1, 18.5, 4], 'z'),                     # over the edge
                                                     pipe_box([x0 - 0.5, 15.75, 4], [x1 + 0.5, 16.75, 5], 'y'),            # roof flange
                                                     clamp(x0 - 0.5, 6, x1 + 0.5)])
    eng('distilling_tank_pipe_%s_boiler' % lane, ST_T, [pipe_box([x0, 7, -3.5], [x1, 16, -0.5], 'y'),
                                                        pipe_box([x0 - 0.25, 4, -3.75], [x1 + 0.25, 7, -0.25], 'y'),     # elbow
                                                        pipe_box([x0, 4.25, -0.25], [x1, 6.75, 0], 'z'),
                                                        box([x0 - 1, 3.5, -0.75], [x1 + 1, 7.5, 0], 'iron'),               # wall flange
                                                        clamp(x0 - 0.5, 12, x1 + 0.5)])
eng('distilling_tank_pipe_band', ST_T, [pipe_box([0, 9, -4], [16, 12.5, -0.5], 'x'),
                                        box([3.5, 8.5, -1], [5, 13, 0], 'dark'), box([11, 8.5, -1], [12.5, 13, 0], 'dark')])
# brass collar band round the column every few levels, and one gauge high up (as in Kragnoth's reference picture)
eng('distilling_tank_collar', ST_T, [box([0, 6, -0.75], [16, 10, 0], 'brass'),
                                     box([3.5, 7.5, -1], [4.5, 8.5, -0.75], 'dark'), box([11.5, 7.5, -1], [12.5, 8.5, -0.75], 'dark')])
_g = box([4.5, 4.5, -1.25], [11.5, 11.5, 0], 'brass', {'north': 'dial'})
_g['faces']['north']['uv'] = [0, 8, 8, 16]   # the dial quadrant of Create's boiler_gauge sheet
eng('distilling_tank_gauge', dict(ST_T, dial=CR('boiler_gauge')), [_g, box([7.75, 7.75, -1.5], [8.25, 10.5, -1.25], 'dark'),
                                                                   box([7.5, 11.5, -0.75], [8.5, 13.5, -0.25], 'brass')])
# coupling where a tank touches a fraction level: bolted flange ring in the seam (the tank hides the rest)
eng('distilling_tank_outlet', ST_T, [box([3, 3, -1], [13, 13, 0], 'iron'), box([5, 5, -1.5], [11, 11, -1], 'brass')]
    + [box([x, y, -1.25], [x + 1, y + 1, -1], 'dark') for x, y in ((3.5, 3.5), (11.5, 3.5), (3.5, 11.5), (11.5, 11.5))])
eng('distilling_tank_body', ST_T, [{'from': [0, 0, 0], 'to': [16, 16, 16], 'faces': {
    d: {'texture': '#top' if d in ('up', 'down') else '#side', 'uv': [0, 0, 16, 16], 'cullface': d}
    for d in ('north', 'south', 'east', 'west', 'up', 'down')}}])
eng('distilling_tank_valve', ST_T, [box([7, 16, 7], [9, 18.5, 9], 'brass'),                    # relief valve
                                    box([5, 18.5, 7.5], [11, 19.25, 8.5], 'dark'), box([7.5, 18.5, 5], [8.5, 19.25, 11], 'dark'),
                                    box([7.25, 19.25, 7.25], [8.75, 20, 8.75], 'brass'),
                                    pipe_box([9, 16, 6.75], [11.5, 21.5, 9.25], 'y'),          # gooseneck: riser
                                    pipe_box([11.5, 19.5, 6.75], [14, 22, 9.25], 'x'),         # elbow
                                    pipe_box([12, 16.5, 7], [14, 19.5, 9], 'y'),               # downcomer
                                    box([11.75, 16, 6.75], [14.25, 16.75, 9.25], 'iron')])     # collar
# The dressing is drawn by the client from Plumbing.java (block states would need ~65k combinations): the block
# itself is just the connected steel body.
parts = [{'apply': {'model': ce('block/distilling_tank_body')}}]
put('%s/blockstates/distilling_tank.json' % A, {'multipart': parts})
still_item = load_json('%s/models/block/distilling_tank_body.json' % A)
for extra in ('distilling_tank_valve', 'distilling_tank_pipe_left', 'distilling_tank_pipe_band'):
    still_item['elements'] += load_json('%s/models/block/%s.json' % (A, extra))['elements']
for e in still_item['elements']:
    for f in e['faces'].values():
        f.pop('cullface', None)
put('%s/models/item/distilling_tank.json' % A, still_item)
simple_loot('distilling_tank')

# Steel Storage Tank (content/storage): Create's own tank models re-pointed at the steel sheets (vat_tank*, made at load
# time from Create's textures). The blockstate mirrors Create's generated fluid_tank one (read from the Create jar).
STEEL_TANK_TEX = {'0': ce('block/vat_tank_top'), '1': ce('block/vat_tank'), '3': ce('block/vat_tank_window'),
                  '4': ce('block/vat_tank_inner'), '5': ce('block/vat_tank_window_single'), 'particle': ce('block/vat_tank')}
import glob as _glob, zipfile as _zip
_cjar = sorted(p for p in _glob.glob(os.path.expanduser('~/.gradle/caches/**/create-6.0.10*.jar'), recursive=True) if 'sources' not in p)
if _cjar:
    with _zip.ZipFile(_cjar[0]) as z:
        tank_bs = json.loads(z.read('assets/create/blockstates/fluid_tank.json'))
    for v in tank_bs['variants'].values():
        name = v['model'].split('/')[-1]
        # cutout like Create's tank: the window glass is see-through (solid drew it black and hid the fluid)
        put('%s/models/block/steel_tank/%s.json' % (A, name), {'parent': 'create:block/fluid_tank/' + name,
            'render_type': 'minecraft:cutout_mipped', 'textures': STEEL_TANK_TEX})
        v['model'] = ce('block/steel_tank/' + name)
    put('%s/blockstates/steel_tank.json' % A, tank_bs)
put('%s/models/item/steel_tank.json' % A, {'parent': ce('block/steel_tank/block_single_window')})
simple_loot('steel_tank')
shapeless('steel_tank', [item('create:fluid_tank'), item('create:iron_sheet')], ce('steel_tank'), category='misc')
LANG['block.%s.steel_tank' % MODID] = 'Steel Storage Tank'

# Steel Pipe (content/storage): Create's generated pipe models re-pointed at the steel sheet; same blockstate. No window.
if _cjar:
    with _zip.ZipFile(_cjar[0]) as z:
        pipe_bs = json.loads(z.read('assets/create/blockstates/fluid_pipe.json'))
    seen_models = set()
    for part in pipe_bs['multipart']:
        apply = part['apply'] if isinstance(part['apply'], list) else [part['apply']]
        for a in apply:
            name = a['model'].split('/')[-1]
            if name not in seen_models:
                seen_models.add(name)
                put('%s/models/block/steel_pipe/%s.json' % (A, name), {'parent': 'create:block/fluid_pipe/' + name,
                    'render_type': 'minecraft:cutout_mipped', 'textures': {'0': ce('block/steel_pipes_connected'), 'particle': ce('block/steel_pipes')}})
            a['model'] = ce('block/steel_pipe/' + name)
    put('%s/blockstates/steel_pipe.json' % A, pipe_bs)
# the pipe's arms, rims and drains: Create draws these through PipeAttachmentModel from partials; ours are steel
# children of Create's, used by client/storage/SteelPipeModel (without them the pipe is just its core: broken bits)
for kind in ('connection', 'rim', 'rim_connector', 'drain'):
    for d in ('north', 'south', 'east', 'west', 'up', 'down'):
        put('%s/models/block/steel_pipe/%s/%s.json' % (A, kind, d), {'parent': 'create:block/fluid_pipe/%s/%s' % (kind, d),
            'render_type': 'minecraft:cutout_mipped', 'textures': {'0': ce('block/steel_pipes'), 'particle': ce('block/steel_pipes')}})
put('%s/models/item/steel_pipe.json' % A, {'parent': 'create:block/fluid_pipe/item', 'textures': {'0': ce('block/steel_pipes_connected'),
                                                                                                  '1': ce('block/steel_pipes')}})
simple_loot('steel_pipe')
put(R + '/crafting/steel_pipe.json', {'type': 'minecraft:crafting_shapeless', 'category': 'misc',
    'ingredients': [item('create:fluid_pipe'), item('create:fluid_pipe'), item('create:iron_sheet')],
    'result': {'id': ce('steel_pipe'), 'count': 2}})
LANG['block.%s.steel_pipe' % MODID] = 'Steel Pipe'

# Tap (content/distilling/TapBlock), built from Create's own parts (Kragnoth, 2026-11-22: "follow the theming"):
# Create's fluid valve housing and its open / closed indicator, Create's pointer lever (along the pipe = open, across
# = shut, like a ball valve), Create's pipe rim where it meets the column and Create's pipe arm out to the pipe. All of
# it in the steel of the Steel Pipe (load-time recolour of Create's valve sheets; Create's models re-pointed).
for st in ('open', 'closed'):
    put('%s/models/block/tap/body_%s.json' % (A, st), {'parent': 'create:block/fluid_valve/block_horizontal_' + st,
        'render_type': 'minecraft:cutout_mipped',
        'textures': {'2': ce('block/steel_valve'), '3': ce('block/steel_valve_' + st), 'particle': ce('block/steel_valve')}})
put('%s/models/block/tap/pointer.json' % A, {'parent': 'create:block/fluid_valve/pointer', 'render_type': 'minecraft:cutout_mipped',
    'textures': {'2': ce('block/steel_valve'), 'particle': ce('block/steel_valve')}})
_tap_parts = []
for f, rot in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
    back = {'north': 'south', 'south': 'north', 'east': 'west', 'west': 'east'}[f]
    for st, o in (('open', 'true'), ('closed', 'false')):
        # Create's valve model has its pipe along Y and its lever plates on Z: x 90 lays the pipe along the tap and puts
        # the lever under the ring plate on top (Create's own "shaft up" orientation)
        _tap_parts.append({'when': {'facing': f, 'open': o}, 'apply': {'model': ce('block/tap/body_' + st), 'x': 90, 'y': rot}})
        # the pointer (modelled along Z on the top face) lies along the pipe when open, across it when shut
        _tap_parts.append({'when': {'facing': f, 'open': o}, 'apply': {'model': ce('block/tap/pointer'), 'y': rot if st == 'open' else (rot + 90) % 360}})
    _tap_parts.append({'when': {'facing': f}, 'apply': {'model': ce('block/steel_pipe/rim/' + back)}})       # flange on the column
    _tap_parts.append({'when': {'facing': f}, 'apply': {'model': ce('block/steel_pipe/connection/' + f)}})  # arm to the pipe
put('%s/blockstates/tap.json' % A, {'multipart': _tap_parts})
put('%s/models/item/tap.json' % A, {'parent': ce('block/tap/body_open')})
simple_loot('tap')
shapeless('tap', [item('create:fluid_valve'), item('create:brass_sheet')], ce('tap'))
LANG['block.%s.tap' % MODID] = 'Tap'
shaped('distilling_tank', [' V ', 'PTP', 'PBP'], {'V': item('create:fluid_valve'), 'T': item('create:fluid_tank'),
       'P': tag('c:plates/copper'), 'B': item('create:blaze_burner')}, ce('distilling_tank'), category='misc')

# Distilling recipes. Drinks: water drops out low, the spirit rises (biodiesel-tier fuel: distilling is the upgrade).
def distil(name, inp, amount, fractions, heat=1, time=200, residue=None, chance=1.0, conditions=None):
    j = {'type': ce('distilling'), 'input': dict(inp, amount=amount),
         'fractions': [{'id': f, 'amount': a} for f, a in fractions], 'heat': heat, 'time': time}
    if residue:
        j['residue'] = {'id': residue, 'count': 1}
        j['chance'] = chance
    if conditions:
        j['neoforge:conditions'] = conditions
    put(R + '/distilling/%s.json' % name, j)
for drink, spirit, keep in (('hooch', 'vodka', 100), ('wine', 'brandy', 80), ('ale', 'whiskey', 80), ('cider', 'applejack', 80)):
    distil(spirit, {'fluid': ce(drink)}, 250, [('minecraft:water', 250 - keep), (ce(spirit), keep)])
distil('ethanol_from_mead', {'fluid': ce('mead')}, 250, [('minecraft:water', 170), (ce('ethanol'), 80)])
# Crude (docs/petroleum-spec.md): any c:crude_oil (ours from desert pools and pumpjacks, or another mod's) splits into
# our three fuels, heaviest lowest: diesel, kerosene, gasoline. Three fractions need a column four blocks tall
# (boiler + three levels). Lit Blaze Burner or better. Replaces the 0.12 IP / CDG conditional recipes.
# 0.19.0: the bottom cut, heavy oil, joins (asphalt, paraffin, lubricant): four fractions, a column five tall.
distil('crude_oil', {'tag': 'c:crude_oil'}, 100, [(ce('heavy_oil'), 20), (ce('diesel'), 30), (ce('kerosene'), 25), (ce('gasoline'), 25)],
       heat=2, time=100)

# Campfire Stands are fan catalysts (Kragnoth: like Create Connected's): fans blow through them; a lit regular
# campfire makes the current smoke, a lit soul campfire haunt (mixin/FanCatalyst*Mixin decides per state).
# Campfire Stands: Create's own scaffolding model (parent, referenced) with Create's per-metal textures, and the
# vanilla campfire / soul campfire model inside. Drops itself, plus the fitted campfire.
for metal, label, extra in (('andesite', 'Andesite', 'create:andesite_alloy'), ('brass', 'Brass', 'create:brass_sheet'),
                            ('copper', 'Copper', 'create:copper_sheet')):
    name = metal + '_campfire_stand'
    put('%s/models/block/%s.json' % (A, name), {'parent': 'create:block/scaffold/block', 'render_type': 'minecraft:cutout',
        'textures': {'top': 'create:block/funnel/%s_funnel_frame' % metal, 'inside': 'create:block/scaffold/%s_scaffold_inside' % metal,
                     'side': 'create:block/scaffold/%s_scaffold' % metal, 'casing': 'create:block/%s_casing' % metal,
                     'particle': 'create:block/scaffold/%s_scaffold' % metal}})
    parts = [{'apply': {'model': ce('block/' + name)}}]
    # vanilla's campfire models carry no render_type, so inside our multipart they'd fall back to solid (black
    # where the texture is transparent): wrap each one with cutout
    for vanilla in ('campfire', 'campfire_off', 'soul_campfire'):
        put('%s/models/block/stand_%s.json' % (A, vanilla), {'parent': 'minecraft:block/' + vanilla, 'render_type': 'minecraft:cutout'})
    for fire, lit, model in (('campfire', 'true', ce('block/stand_campfire')), ('campfire', 'false', ce('block/stand_campfire_off')),
                             ('soul', 'true', ce('block/stand_soul_campfire')), ('soul', 'false', ce('block/stand_campfire_off'))):
        parts.append({'when': {'fire': fire, 'lit': lit}, 'apply': {'model': model}})
    put('%s/blockstates/%s.json' % (A, name), {'multipart': parts})
    put('%s/models/item/%s.json' % (A, name), {'parent': ce('block/' + name)})
    put('%s/data/%s/loot_table/blocks/%s.json' % (RES, MODID, name), {'type': 'minecraft:block', 'pools': [
        {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': ce(name)}], 'conditions': [{'condition': 'minecraft:survives_explosion'}]},
        {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'minecraft:campfire'}], 'conditions': [
            {'condition': 'minecraft:block_state_property', 'block': ce(name), 'properties': {'fire': 'campfire'}}]},
        {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'minecraft:soul_campfire'}], 'conditions': [
            {'condition': 'minecraft:block_state_property', 'block': ce(name), 'properties': {'fire': 'soul'}}]}]})
    shapeless(name, [item('create:%s_scaffolding' % metal), item(extra)], ce(name))
    LANG['block.%s.%s' % (MODID, name)] = '%s Campfire Stand' % label


# Engine blockstates: models are drawn for engine axis X / facing south (cylinder leans +Z); rotate the rest.
FACE_Y = {'south': 0, 'west': 90, 'north': 180, 'east': 270}          # cylinder model leans +Z (south)
OUT_Y = {'east': 0, 'south': 90, 'west': 180, 'north': 270}            # crankcase model's output end is +X (east)
put('%s/blockstates/diesel_cylinder.json' % A, {'variants': {'facing=%s' % f: dict({'model': ce('block/diesel_cylinder')}, **({'y': y} if y else {}))
                                                            for f, y in FACE_Y.items()}})
put('%s/blockstates/diesel_crankcase.json' % A, {'variants': {'facing=%s' % f: dict({'model': ce('block/diesel_crankcase')}, **({'y': y} if y else {}))
                                                             for f, y in OUT_Y.items()}})
put('%s/blockstates/diesel_flywheel.json' % A, {'variants': {'axis=x': {'model': ce('block/diesel_flywheel')},
                                                            'axis=z': {'model': ce('block/diesel_flywheel'), 'y': 90}}})

for n in ('diesel_crankcase', 'diesel_cylinder', 'diesel_flywheel'):
    put('%s/models/item/%s.json' % (A, n), {'parent': ce('block/' + n)})
    simple_loot(n)

# Diesel engine recipes: cast iron bodies, steel sheet, and the metalworking parts (gears in the crankcase, rods in the
# cylinder), so the press dies feed straight into the engine.
shaped('diesel_crankcase', ['SGS', 'CXC', 'CCC'], {'S': tag('c:plates/steel'), 'G': tag('c:gears/steel'), 'C': tag('c:ingots/cast_iron'),
       'X': item('create:shaft')}, ce('diesel_crankcase'), category='redstone')
shaped('diesel_cylinder', [' B ', 'SPS', 'CRC'], {'B': tag('c:ingots/brass'), 'S': tag('c:plates/steel'), 'P': item('minecraft:piston'),
       'C': tag('c:ingots/cast_iron'), 'R': tag('c:rods/steel')}, ce('diesel_cylinder'), category='redstone')
shaped('diesel_flywheel', ['CCC', 'CXC', 'CCC'], {'C': tag('c:ingots/cast_iron'), 'X': item('create:shaft')}, ce('diesel_flywheel'), category='redstone')

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
tagfile('c', 'item', 'storage_blocks', ['#c:storage_blocks/blister_steel', '#c:storage_blocks/coal_coke', '#c:storage_blocks/cast_iron', '#c:storage_blocks/steel'])
tagfile('c', 'block', 'storage_blocks', ['#c:storage_blocks/blister_steel', '#c:storage_blocks/coal_coke', '#c:storage_blocks/cast_iron', '#c:storage_blocks/steel'])
tagfile('c', 'item', 'coal_coke', [ce('coal_coke')])
tagfile('c', 'item', 'dusts/coal_coke', [ce('coke_dust')])
tagfile('c', 'item', 'dusts', ['#c:dusts/coal_coke'])
tagfile('c', 'item', 'storage_blocks/coal_coke', [ce('coke_block')])
tagfile('c', 'block', 'storage_blocks/coal_coke', [ce('coke_block')])
for _n in ('crude_oil', 'diesel', 'kerosene', 'gasoline', 'heavy_oil', 'lubricant'):
    tagfile('c', 'fluid', _n, [ce(_n), ce('flowing_' + _n)])
tagfile('c', 'fluid', 'creosote', [ce('creosote'), ce('flowing_creosote')])
tagfile('c', 'fluid', 'plantoil', [ce('plant_oil'), ce('flowing_plant_oil')])
tagfile('c', 'fluid', 'ethanol', [ce('ethanol'), ce('flowing_ethanol')])
for _n in ('hooch', 'wine', 'cider', 'mead', 'ale'):
    tagfile('c', 'fluid', _n, [ce(_n), ce('flowing_' + _n)])
tagfile('c', 'fluid', 'vodka', [ce('vodka'), ce('flowing_vodka')])
tagfile('c', 'fluid', 'brandy', [ce('brandy'), ce('flowing_brandy')])
tagfile('c', 'fluid', 'whiskey', [ce('whiskey'), ce('flowing_whiskey')])
tagfile('c', 'fluid', 'applejack', [ce('applejack'), ce('flowing_applejack')])
# Campfire Stands: fans blow through them (fan catalysts; see the stand section)
tagfile('create', 'block', 'fan_transparent', [ce(m + '_campfire_stand') for m in ('andesite', 'brass', 'copper')])
# Fermenting Vat: 250 mB water + produce -> 250 mB, 20 s at passive heat (docs/fermentation-spec.md).
def ferment(name, ingredient, count, result, time=400):
    put(R + '/fermenting/%s.json' % name, {'type': ce('fermenting'), 'ingredients': [dict(ingredient, count=count)],
        'fluid': {'fluid': 'minecraft:water', 'amount': 250}, 'result': {'id': ce(result), 'amount': 250}, 'time': time})
ferment('ethanol_from_sugar_cane', item('minecraft:sugar_cane'), 4, 'ethanol')
ferment('ethanol_from_beetroot', tag('c:crops/beetroot'), 4, 'ethanol')
ferment('hooch', tag('c:crops/potato'), 4, 'hooch')
ferment('wine', item('minecraft:sweet_berries'), 8, 'wine')
ferment('wine_from_glow_berries', item('minecraft:glow_berries'), 8, 'wine')
ferment('cider', item('minecraft:apple'), 4, 'cider')
ferment('mead', item('minecraft:honey_bottle'), 2, 'mead', 600)
ferment('ale', tag('c:crops/wheat'), 4, 'ale')
shaped('fermenting_vat', [' V ', 'CBC', 'C C'], {'V': item('create:fluid_valve'), 'B': item('create:basin'),
       'C': tag('c:plates/copper')}, ce('fermenting_vat'), category='misc')

# Plant oil: seeds pressed in a Basin under a Mechanical Press (Create compacting). 4 seeds -> 250 mB.
put(R + '/compacting/plant_oil.json', {'type': 'create:compacting', 'ingredients': [tag('c:seeds')] * 4,
    'results': [{'id': ce('plant_oil'), 'amount': 250}]})

# Engine fuels (data map creativeengineering:engine_fuel on fluids, keyed by our fuel tags). Each tag lists the common
# c: tag and other mods' fluids as OPTIONAL entries (Crafts & Additions, Create Diesel Generators, Immersive
# Engineering / Petroleum), so they work when present and nothing errors when they aren't.
# stress = SU each cylinder adds; burn = mB per cylinder per second. Baseline (Kragnoth): one Create steam engine on a
# lava-fed blaze burner = 16384 SU; one creosote cylinder (one crankcase, one cylinder, one fuel) = 3/4 of that =
# 12288. Better fuels keep their ratios above creosote.
opt = lambda i: {'id': i, 'required': False}
FUELS = [('creosote', ['#c:creosote', 'immersiveengineering:creosote'], 12288, 2),
         ('plant_oil', [ce('plant_oil'), '#c:plantoil', '#c:plant_oil', '#c:seed_oil', 'createaddition:seed_oil', 'createdieselgenerators:plant_oil',
                        'immersiveengineering:plantoil'], 15360, 2),
         ('ethanol', [ce('ethanol'), ce('hooch'), ce('wine'), ce('cider'), ce('mead'), ce('ale'), '#c:ethanol', '#c:bioethanol', 'createaddition:bioethanol', 'createdieselgenerators:ethanol',
                      'immersiveengineering:ethanol'], 18432, 2),
         # Tarbound's tar burns at biodiesel level: "it contains spirits" (Kragnoth, 2026-11-22). Optional entry.
         ('biodiesel', ['#c:biodiesel', 'createdieselgenerators:biodiesel', 'immersiveengineering:biodiesel', 'tarbound:tar',
                        ce('vodka'), ce('brandy'), ce('whiskey'), ce('applejack')], 24576, 1.5),   # spirits: distilled drinks
         # Petroleum (docs/petroleum-spec.md): raw crude burns like creosote but thirstier; kerosene sits between
         # biodiesel and diesel. Our diesel and gasoline join through the c: tags below.
         ('crude_oil', ['#c:crude_oil', 'createdieselgenerators:crude_oil', 'immersivepetroleum:oil'], 12288, 2.5),
         ('kerosene', ['#c:kerosene', 'immersivepetroleum:kerosene'], 27648, 1.25),
         ('diesel', ['#c:diesel', 'createdieselgenerators:diesel', 'immersivepetroleum:diesel', 'immersivepetroleum:diesel_sulfur'], 30720, 1),
         ('gasoline', ['#c:gasoline', 'createdieselgenerators:gasoline', 'immersivepetroleum:gasoline'], 24576, 2)]
values = {}
for name, members, stress, burn in FUELS:
    entries = [ce('creosote')] if name == 'creosote' else []
    tagfile(MODID, 'fluid', 'engine_fuels/' + name, entries + [opt(m) for m in members])
    values['#%s:engine_fuels/%s' % (MODID, name)] = {'stress': stress, 'burn': burn}
put('%s/data/%s/data_maps/fluid/engine_fuel.json' % (RES, MODID), {'values': values})
# Tarbound (optional, soft): jobs may burn coke (docs/tarbound-compat.md A3). Ignored when Tarbound is absent.
put('%s/data/tarbound/tags/item/pool_fuel.json' % RES, {'replace': False, 'values': [{'id': '#c:coal_coke', 'required': False}]})
# Grade tags (docs/steelworks-spec.md 1): higher grades join these as they arrive.
for metal in ('cast_iron', 'steel'):
    tagfile('c', 'item', 'ingots/' + metal, [ce(metal + '_ingot')])
    tagfile('c', 'item', 'nuggets/' + metal, [ce(metal + '_nugget')])
    tagfile('c', 'item', 'plates/' + metal, [ce(metal + '_sheet')])
    tagfile('c', 'item', 'storage_blocks/' + metal, [ce(metal + '_block')])
    tagfile('c', 'block', 'storage_blocks/' + metal, [ce(metal + '_block')])
tagfile('c', 'item', 'ingots', ['#c:ingots/blister_steel', '#c:ingots/cast_iron', '#c:ingots/steel'])
tagfile('c', 'item', 'nuggets', ['#c:nuggets/blister_steel', '#c:nuggets/cast_iron', '#c:nuggets/steel'])
tagfile('c', 'item', 'plates', ['#c:plates/blister_steel', '#c:plates/cast_iron', '#c:plates/steel'])
tagfile('c', 'item', 'slag', [ce('slag')])
tagfile(MODID, 'item', 'dies', [ce('rod_die'), ce('gear_die')])
for kind, metals in (('rods', ('copper', 'iron', 'steel')), ('gears', ('iron', 'steel')), ('wires', ('copper', 'steel'))):
    for m in metals:
        tagfile('c', 'item', '%s/%s' % (kind, m), [ce('%s_%s' % (m, kind[:-1]))])
    tagfile('c', 'item', kind, ['#c:%s/%s' % (kind, m) for m in metals])
tagfile(MODID, 'item', 'steel_grade', ['#c:ingots/blister_steel', '#c:ingots/steel'])
tagfile(MODID, 'item', 'cast_grade', ['#c:ingots/cast_iron', '#c:ingots/steel'])
tagfile(MODID, 'item', 'creosoted_sleepers', [ce('treated_slab')])  # rail module, later
tagfile(MODID, 'item', 'treated_logs', [ce('treated_log'), ce('treated_wood')])
tagfile('minecraft', 'item', 'planks', [ce('treated_planks')])
tagfile('minecraft', 'block', 'planks', [ce('treated_planks')])
for kind in ('item', 'block'):
    tagfile('minecraft', kind, 'wooden_stairs', [ce('treated_stairs')])
    tagfile('minecraft', kind, 'wooden_slabs', [ce('treated_slab')])
    tagfile('minecraft', kind, 'slabs', [ce('asphalt_slab')])
    tagfile('minecraft', kind, 'stairs', [ce('asphalt_stairs')])
tagfile('minecraft', 'block', 'mineable/axe', [ce(b) for b in ('treated_planks', 'treated_stairs', 'treated_slab', 'treated_log', 'treated_wood', 'treated_beam')])
# Vanilla: tool/armour tags drive enchanting tables, anvils and other mods' checks.
for t in TOOLS:
    tagfile('minecraft', 'item', t + 's', [ce('blister_steel_' + t)])
for a, slot in zip(ARMOR, ['head', 'chest', 'leg', 'foot']):
    tagfile('minecraft', 'item', slot + '_armor', [ce('blister_steel_' + a)])
tagfile('minecraft', 'item', 'trimmable_armor', [ce('blister_steel_' + a) for a in ARMOR])
tagfile('minecraft', 'item', 'beacon_payment_items', [ce('blister_steel_ingot'), ce('steel_ingot')])
tagfile('minecraft', 'block', 'mineable/pickaxe', [ce(b) for b in BLOCKS] + [ce('coke_oven_bricks'), ce('blast_furnace_bricks'), ce('alloy_furnace_bricks')] + [ce('pumpjack'), ce('walking_beam_bearing'), ce('wellhead'), ce('asphalt'), ce('asphalt_slab'), ce('asphalt_stairs')]
        + [ce(n) for n in ('diesel_crankcase', 'diesel_cylinder', 'diesel_flywheel', 'drive_pulley', 'fermenting_vat', 'distilling_tank', 'steel_tank', 'steel_pipe', 'tap', 'andesite_campfire_stand', 'brass_campfire_stand', 'copper_campfire_stand')])
tagfile('minecraft', 'block', 'needs_stone_tool', [ce('blister_steel_block'), ce('cast_iron_block')])
tagfile('minecraft', 'block', 'needs_iron_tool', [ce('steel_block')])
tagfile('minecraft', 'block', 'beacon_base_blocks', [ce('blister_steel_block'), ce('steel_block')])

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
adv('blast_furnace', 'coke_oven', ce('cast_iron_ingot'), {'cast': has(ce('cast_iron_ingot'))})
adv('steel', 'blast_furnace', ce('steel_ingot'), {'steel': has(ce('steel_ingot'))}, 'goal')
adv('alloy_furnace', 'steel', ce('alloy_furnace_bricks'), {'bricks': has(ce('alloy_furnace_bricks'))})
adv('press_die', 'steel', ce('rod_die'), {d: has(ce(d)) for d in ('rod_die', 'gear_die')})
adv('treated_wood', 'coke_oven', ce('treated_planks'), {n: has(ce(n)) for n in ('treated_planks', 'treated_log', 'treated_wood')})
adv('diesel_engine', 'press_die', ce('diesel_cylinder'), {'cylinder': has(ce('diesel_cylinder'))}, 'goal')
adv('fermenting', 'coke_oven', ce('fermenting_vat'), {'vat': has(ce('fermenting_vat'))})
adv('distilling', 'fermenting', ce('distilling_tank'), {'still': has(ce('distilling_tank'))})
adv('shear_forged', 'blister_steel', ce('blister_steel_pickaxe'),
    {t: has(ce('blister_steel_' + t)) for t in TOOLS + ARMOR})

# ---------------------------------------------------------------------------------------- petroleum
# docs/petroleum-spec.md. Crude from small desert pools (worldgen below) and from infinite chunk veins found with the
# Seismic Probe and drawn by a kinetic Pumpjack; distilled into diesel, kerosene and gasoline (distilling section).
for k, v in (('item.%s.seismic_probe', 'Seismic Probe'),
             ('%s.probe.here', 'Oil vein right here: %s mB/s'),
             ('%s.probe.none', 'No oil within %s blocks'),
             ('%s.probe.nearest', 'Oil vein ~%s blocks %s'),
             ('%s.probe.dir.north', 'north'), ('%s.probe.dir.north_east', 'north-east'), ('%s.probe.dir.east', 'east'),
             ('%s.probe.dir.south_east', 'south-east'), ('%s.probe.dir.south', 'south'), ('%s.probe.dir.south_west', 'south-west'),
             ('%s.probe.dir.west', 'west'), ('%s.probe.dir.north_west', 'north-west'),
             ('block.%s.pumpjack', 'Pumpjack'), ('block.%s.walking_beam_bearing', 'Walking Beam Bearing'), ('block.%s.wellhead', 'Wellhead'),
             ('%s.pumpjack.driving', 'Stroking: %s'),
             ('%s.pumpjack.stroke', 'Column %s, rear arm %s, reach %s: stroke %sx'),
             ('%s.pumpjack.no_target', 'Nothing under the horsehead: a Wellhead goes 1-6 blocks past the column'),
             ('%s.pumpjack.no_post', 'No beam bearing: build a girder column 2-6 high, 1-3 blocks in front, bearing on top'),
             ('%s.pumpjack.no_column', 'The bearing needs a column of 2-6 girders or industrial iron under it'),
             ('%s.pumpjack.no_gearbox', 'No Crank Gearbox 1-3 blocks behind the column'),
             ('%s.pumpjack.too_short', 'Column too short for this reach: the horsehead would hit the well'),
             ('%s.pumpjack.has_post', 'This Pumpjack already has its bearing'),
             ('%s.pumpjack.has_well', 'This Pumpjack already strokes a Wellhead'),
             ('%s.pumpjack.post_first', 'Build the column and its bearing first'),
             ('%s.pumpjack.blocked', 'Something is in the way'),
             ('%s.pumpjack.slow', 'Full stroke at %s RPM'),
             ('%s.wellhead.yield', 'Below: %s, %s mB/s'),
             ('%s.wellhead.dry', 'Nothing to draw here'),
             ('%s.wellhead.idle', 'Idle: needs a turning Pumpjack over it'),
             ('%s.wellhead.rate', 'Drawing %s mB/s'),
             ('%s.wellhead.slow', 'Full rate at %s RPM'),
             ('%s.wellhead.tank', '%s: %s / %s mB'),
             ('%s.wellhead.empty', 'Empty: 0 / %s mB')):
    LANG[k % MODID] = v
for n, label in (('crude_oil', 'Crude Oil'), ('diesel', 'Diesel'), ('kerosene', 'Kerosene'), ('gasoline', 'Gasoline')):
    LANG['fluid_type.%s.%s' % (MODID, n)] = label
    LANG['block.%s.%s' % (MODID, n)] = label
    LANG['item.%s.%s_bucket' % (MODID, n)] = label + ' Bucket'

# Seismic Probe: our own drawn texture (tools/stylekit/make_petroleum_textures.py).
put('%s/models/item/seismic_probe.json' % A, {'parent': 'minecraft:item/handheld', 'textures': {'layer0': ce('item/seismic_probe')}})
shaped('seismic_probe', ['  E', ' B ', 'R  '], {'E': item('create:electron_tube'), 'B': tag('c:plates/brass'),
       'R': tag('c:rods/steel')}, ce('seismic_probe'), category='equipment')

# Pumpjack: Crank Gearbox + a column the player builds (Create girders / industrial iron, 2-6 high) crowned by the
# Walking Beam Bearing + a stroked block (Wellhead), each its own block, in a row (PumpjackRig). Kragnoth: real-life
# size, Create-centric looks, no forming. Rear arm 1-3, reach 1-6; the horsehead grows with the reach. Models face south. Moving parts are partials modelled about a
# local origin (pivot, beam ends, lip) and posed by PumpjackRig.pose (client/petroleum/*Visual). eng() asserts every
# element stays inside -16..32. Create textures referenced, never copied.
PJ_T = dict(ENG_T, steel=ce('block/steel_block'), casing=CR('andesite_casing'))
eng('pumpjack', PJ_T, [                                                                # Crank Gearbox
    box([0, 0, 0], [16, 2, 16], 'iron', {'up': 'iron_top'}),                          # skid
    box([4, 2, 3], [12, 13, 13], 'casing'),                                           # reduction gearbox (Create's shaft at y 8)
    box([3.5, 13, 3.5], [12.5, 14, 12.5], 'iron', {'up': 'iron_top'}),                 # its cover
    box([5, 2, 1.5], [11, 4, 3], 'dark'), box([5, 2, 13], [11, 4, 14.5], 'dark')])     # hold-down feet
# Walking Beam Bearing: crowns a column the player builds (Create metal girders / industrial iron, tag pumpjack_column).
eng('walking_beam_bearing', PJ_T, [
    box([1, 0, 2], [15, 3, 14], 'iron', {'up': 'iron_top'}),                          # sole plate on the column
    box([3, 3, 5], [6, 11, 11], 'dark'), box([10, 3, 5], [13, 11, 11], 'dark'),       # pillow-block pedestals (pivot y 10)
    box([3, 11, 5.5], [6, 12.5, 10.5], 'brass'), box([10, 11, 5.5], [13, 12.5, 10.5], 'brass'),   # bearing caps
    box([2, 3, 3], [14, 4, 4], 'dark'), box([2, 3, 12], [14, 4, 13], 'dark')])        # hold-down bars
tagfile(MODID, 'block', 'pumpjack_column', ['create:metal_girder', 'create:industrial_iron_block', 'create:weathered_iron_block',
                                            ce('steel_block'), ce('cast_iron_block')])
eng('wellhead', PJ_T, [
    box([1, 0, 1], [15, 1, 15], 'iron', {'up': 'iron_top'}),                          # pad
    box([3, 1, 3], [13, 13, 13], 'steel'),                                             # casing head
    box([0, 4, 4], [16, 12, 12], 'steel'), box([4, 4, 0], [12, 12, 16], 'steel'),     # flow cross: Create-pipe-size ports (8 px, centred)
    box([0, 3, 3], [1, 13, 13], 'dark'), box([15, 3, 3], [16, 13, 13], 'dark'),
    box([3, 3, 0], [13, 13, 1], 'dark'), box([3, 3, 15], [13, 13, 16], 'dark'),       # port flanges
    box([4, 13, 4], [12, 15, 12], 'dark'),                                             # bonnet
    box([6, 15, 6], [10, 17, 10], 'brass')])                                           # stuffing box
# animated partials
eng('pumpjack_crank', PJ_T, [                                                          # gearbox space, axis (y 8, z 8) along X, 4 px throw
    box([2.5, 8, 7], [4, 12.5, 9], 'iron'), box([12, 8, 7], [13.5, 12.5, 9], 'iron'),   #   (scaled out to 6 px for longer rear arms)
    box([2.5, 3, 5], [4, 6.5, 11], 'dark'), box([12, 3, 5], [13.5, 6.5, 11], 'dark'),   # counterweights
    box([1, 11, 7], [2, 13, 9], 'brass'), box([14, 11, 7], [15, 13, 9], 'brass')])      # crank pins (x 1.5 / 14.5)
_beam = box([6, -1, 0], [10, 3, 16], 'girder', {'up': 'iron_top', 'down': 'iron_top', 'north': 'dark', 'south': 'dark'})
for _d in ('east', 'west'):
    _beam['faces'][_d]['uv'] = [0, 0, 8, 4]                                            # Create girder's riveted flange strip
eng('pumpjack_beam', dict(PJ_T, girder=CR('girder')), [_beam])                       # one block of box-girder walking beam
eng('pumpjack_saddle', PJ_T, [box([6.5, -3, -2], [9.5, -1, 2], 'brass'), box([4.5, -1.5, -0.5], [11.5, 0.5, 0.5], 'dark')])   # at the pivot
eng('pumpjack_horsehead', PJ_T, [box([5.5, 0, -5], [10.5, 12, -0.5], 'steel'),       # lip at the origin; scaled with the reach
                                 box([5, -0.5, -0.5], [11, 12.5, 0], 'dark'),         # its arc face
                                 box([6, 5, -6], [10, 9, -5], 'dark')])               # bolted to the beam end
eng('pumpjack_equalizer', PJ_T, [box([1, -3, -1], [15, -1, 1], 'iron', {'up': 'iron_top'}), box([6, -1.5, -1.5], [10, 0, 0], 'dark')])   # rear pin y -2
eng('pumpjack_pitman', PJ_T, [box([-0.75, 0, -0.75], [0.75, 16, 0.75], 'iron'),      # one 16 px segment, repeated pin to equalizer
                              box([-1, 0, -1], [1, 1, 1], 'brass')])
eng('pumpjack_rod', PJ_T, [box([-0.5, 0, -0.5], [0.5, 16, 0.5], 'steel')])            # one 16 px segment of polished rod
eng('pumpjack_bridle', PJ_T, [box([6, 0, -0.75], [10, 1, 0.75], 'dark'),              # carrier bar, origin at the rod top
                              box([6.5, 1, -0.25], [7, 7, 0.25], 'dark'), box([9, 1, -0.25], [9.5, 7, 0.25], 'dark')])   # wire-rope bridle
_PJ_Y = {'south': 0, 'west': 90, 'north': 180, 'east': 270}
_yr = lambda f: {'y': _PJ_Y[f]} if _PJ_Y[f] else {}
put('%s/blockstates/pumpjack.json' % A, {'variants': {'facing=%s' % f: dict({'model': ce('block/pumpjack')}, **_yr(f)) for f in _PJ_Y}})
put('%s/blockstates/walking_beam_bearing.json' % A, {'variants': {'facing=%s' % f: dict({'model': ce('block/walking_beam_bearing')}, **_yr(f))
                                                                 for f in _PJ_Y}})
put('%s/blockstates/wellhead.json' % A, {'variants': {'': {'model': ce('block/wellhead')}}})
for b in ('pumpjack', 'walking_beam_bearing', 'wellhead'):
    simple_loot(b)
_disp = lambda s, ty=0: {'gui': {'rotation': [30, 225, 0], 'translation': [0, ty, 0], 'scale': [s, s, s]},
                         'ground': {'translation': [0, 3, 0], 'scale': [0.25, 0.25, 0.25]},
                         'fixed': {'scale': [0.5, 0.5, 0.5]},
                         'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [0.375, 0.375, 0.375]},
                         'firstperson_righthand': {'rotation': [0, 45, 0], 'scale': [0.4, 0.4, 0.4]}}
put('%s/models/item/pumpjack.json' % A, {'parent': ce('block/pumpjack'), 'display': _disp(0.625)})
put('%s/models/item/walking_beam_bearing.json' % A, {'parent': ce('block/walking_beam_bearing')})
put('%s/models/item/wellhead.json' % A, {'parent': ce('block/wellhead')})
shaped('pumpjack', [' G ', 'SAS', 'PPP'], {'G': tag('c:gears/steel'), 'S': item('create:shaft'), 'A': item('create:andesite_casing'),
       'P': tag('c:plates/steel')}, ce('pumpjack'), category='misc')
shaped('walking_beam_bearing', [' S ', 'B B', 'PPP'], {'S': item('create:shaft'), 'B': tag('c:plates/brass'), 'P': tag('c:plates/steel')},
       ce('walking_beam_bearing'), category='misc')
shaped('wellhead', [' V ', 'PFP', ' S '], {'V': item('create:fluid_valve'), 'P': item(ce('steel_pipe')), 'F': tag('c:plates/steel'),
       'S': item('create:industrial_iron_block')}, ce('wellhead'), category='misc')
# Heavy oil products (0.19.0, docs/petroleum-spec.md): asphalt roads, paraffin wax, lubricant + the Oil Can.
for k, v in (('block.%s.asphalt', 'Asphalt'), ('block.%s.asphalt_slab', 'Asphalt Slab'), ('block.%s.asphalt_stairs', 'Asphalt Stairs'),
             ('item.%s.paraffin_wax', 'Paraffin Wax'), ('item.%s.oil_can', 'Oil Can'),
             ('%s.oil_can.amount', 'Lubricant: %s / %s mB'), ('%s.oil_can.doses', '%s doses (use on a Create machine)'),
             ('%s.oil_can.empty', 'The can is dry: fill it with lubricant (Spout, or use it on a tank)'),
             ('%s.oil_can.full', 'Already well oiled'), ('%s.lubricated', 'Lubricated: -25%% stress, ~%s min left')):
    LANG[k % MODID] = v
AS = ce('block/asphalt')
put('%s/models/block/asphalt.json' % A, {'parent': 'minecraft:block/cube_all', 'textures': {'all': AS}})
for n, parent in (('asphalt_stairs', 'stairs'), ('asphalt_stairs_inner', 'inner_stairs'), ('asphalt_stairs_outer', 'outer_stairs'),
                  ('asphalt_slab', 'slab'), ('asphalt_slab_top', 'slab_top')):
    put('%s/models/block/%s.json' % (A, n), {'parent': 'minecraft:block/' + parent, 'textures': {'bottom': AS, 'top': AS, 'side': AS}})
put('%s/blockstates/asphalt.json' % A, {'variants': {'': {'model': AS}}})
put('%s/blockstates/asphalt_slab.json' % A, {'variants': {'type=bottom': {'model': ce('block/asphalt_slab')},
    'type=top': {'model': ce('block/asphalt_slab_top')}, 'type=double': {'model': AS}}})
_st = {}
for facing, ry in (('east', 0), ('south', 90), ('west', 180), ('north', 270)):
    for half in ('bottom', 'top'):
        for shape in ('straight', 'inner_left', 'inner_right', 'outer_left', 'outer_right'):
            y = ry - (90 if shape in ('inner_left', 'outer_left') and half == 'bottom' else 0) + (90 if shape in ('inner_right', 'outer_right') and half == 'top' else 0)
            v = {'model': ce('block/asphalt_stairs' + ('_inner' if shape.startswith('inner') else '_outer' if shape.startswith('outer') else ''))}
            if half == 'top':
                v['x'] = 180
            if y % 360:
                v['y'] = y % 360
            if half == 'top' or y % 360:
                v['uvlock'] = True
            _st['facing=%s,half=%s,shape=%s' % (facing, half, shape)] = v
put('%s/blockstates/asphalt_stairs.json' % A, {'variants': _st})
for n in ('asphalt', 'asphalt_slab', 'asphalt_stairs'):
    put('%s/models/item/%s.json' % (A, n), {'parent': ce('block/' + n)})
    simple_loot(n, slab=n == 'asphalt_slab')
for n in ('paraffin_wax', 'oil_can'):
    put('%s/models/item/%s.json' % (A, n), {'parent': 'minecraft:item/generated', 'textures': {'layer0': ce('item/' + n)}})
# asphalt: heated mixing of heavy oil with gravel and sand; slabs / stairs by crafting or stonecutter
put(R + '/mixing/asphalt.json', {'type': 'create:mixing', 'heat_requirement': 'heated', 'ingredients': [
    {'type': 'neoforge:tag', 'tag': 'c:heavy_oil', 'amount': 250}, item('minecraft:gravel'), item('minecraft:gravel'), tag('c:sands')],
    'results': [{'id': ce('asphalt'), 'count': 4}]})
shaped('asphalt_slab', ['###'], {'#': item(ce('asphalt'))}, ce('asphalt_slab'), count=6, category='building')
shaped('asphalt_stairs', ['#  ', '## ', '###'], {'#': item(ce('asphalt'))}, ce('asphalt_stairs'), count=4, category='building')
for n, c in (('asphalt_slab', 2), ('asphalt_stairs', 1)):
    put(R + '/stonecutting/%s.json' % n, {'type': 'minecraft:stonecutting', 'ingredient': item(ce('asphalt')),
        'result': {'id': ce(n), 'count': c}})
# paraffin: compact heavy oil; waxes copper like honeycomb (ParaffinWaxItem) and makes candles
put(R + '/compacting/paraffin_wax.json', {'type': 'create:compacting', 'ingredients': [{'type': 'neoforge:tag', 'tag': 'c:heavy_oil', 'amount': 100}],
    'results': [{'id': ce('paraffin_wax')}]})
shaped('candle_from_paraffin', ['S', 'W'], {'S': item('minecraft:string'), 'W': item(ce('paraffin_wax'))}, 'minecraft:candle', category='misc')
# lubricant: heavy oil thinned with kerosene; the Oil Can carries it to the machines
put(R + '/mixing/lubricant.json', {'type': 'create:mixing', 'ingredients': [
    {'type': 'neoforge:tag', 'tag': 'c:heavy_oil', 'amount': 50}, {'type': 'neoforge:tag', 'tag': 'c:kerosene', 'amount': 50}],
    'results': [{'id': ce('lubricant'), 'amount': 100}]})
shaped('oil_can', [' N ', 'S S', 'SSS'], {'N': tag('c:nuggets/brass'), 'S': tag('c:plates/iron')}, ce('oil_can'), category='equipment')
adv('heavy_oil', 'crude_oil', ce('asphalt'), {'asphalt': has(ce('asphalt'))})
LANG['advancements.%s.heavy_oil.title' % MODID] = 'Paving the Way'
LANG['advancements.%s.heavy_oil.description' % MODID] = 'Mix heavy oil, gravel and sand into asphalt'

# What a Wellhead brings up (content/petroleum/WellSources): highest priority that applies wins.
WS = D + '/well_sources'
put(WS + '/oil_vein.json', {'fluid': ce('crude_oil'), 'oil_vein': True, 'dimension': 'minecraft:overworld', 'priority': 100})
put(WS + '/dry_ground.json', {'fluid': 'minecraft:water', 'rate': 5, 'dimension': 'minecraft:overworld',
                              'biomes': ['#c:is_desert', '#c:is_badlands', '#c:is_dry'], 'priority': 10})
put(WS + '/water.json', {'fluid': 'minecraft:water', 'rate': 20, 'dimension': 'minecraft:overworld', 'priority': 0})
put(WS + '/nether_lava.json', {'fluid': 'minecraft:lava', 'rate': 10, 'dimension': 'minecraft:the_nether', 'priority': 0})

# Desert crude pools: vanilla's lake feature filled with crude, walled in sandstone, on desert surfaces (c:is_desert).
WG = D + '/worldgen'
put(WG + '/configured_feature/crude_pool.json', {'type': 'minecraft:lake', 'config': {
    'fluid': {'type': 'minecraft:simple_state_provider', 'state': {'Name': ce('crude_oil'), 'Properties': {'level': '0'}}},
    'barrier': {'type': 'minecraft:simple_state_provider', 'state': {'Name': 'minecraft:sandstone'}}}})
put(WG + '/placed_feature/crude_pool.json', {'feature': ce('crude_pool'), 'placement': [
    {'type': 'minecraft:rarity_filter', 'chance': 48}, {'type': 'minecraft:in_square'},
    {'type': 'minecraft:heightmap', 'heightmap': 'WORLD_SURFACE_WG'}, {'type': 'minecraft:biome'}]})
put('%s/neoforge/biome_modifier/crude_pools.json' % D, {'type': 'neoforge:add_features', 'biomes': '#c:is_desert',
    'features': ce('crude_pool'), 'step': 'lakes'})
adv('crude_oil', 'distilling', ce('crude_oil_bucket'), {'crude': has(ce('crude_oil_bucket'))})
adv('pumpjack', 'crude_oil', ce('pumpjack'), {'pumpjack': has(ce('pumpjack'))}, 'goal')
LANG['advancements.%s.crude_oil.title' % MODID] = 'Black Gold'
LANG['advancements.%s.crude_oil.description' % MODID] = 'Bucket some crude oil from a desert pool'
LANG['advancements.%s.pumpjack.title' % MODID] = 'Nodding Donkey'
LANG['advancements.%s.pumpjack.description' % MODID] = 'Stroke a Wellhead with a Pumpjack'

# ---------------------------------------------------------------------------------------- drink bottles
# One 250 mB bottle per alcoholic drink (ModItems.DRINKS). Model: vanilla's potion bottle with its liquid overlay
# tinted from the fluid (CEClient.itemColors), so resource packs restyle it. Create's Spout fills a glass bottle,
# the Item Drain empties it; by hand: a bucket and four glass bottles make four bottles (the bucket comes back).
DRINKS = [('hooch', 'Bottle of Hooch'), ('wine', 'Bottle of Wine'), ('cider', 'Bottle of Cider'), ('mead', 'Bottle of Mead'),
          ('ale', 'Bottle of Ale'), ('vodka', 'Bottle of Vodka'), ('brandy', 'Bottle of Brandy'), ('whiskey', 'Bottle of Whiskey'),
          ('applejack', 'Bottle of Applejack')]
for n, label in DRINKS:
    b = n + '_bottle'
    LANG['item.%s.%s' % (MODID, b)] = label
    put('%s/models/item/%s.json' % (A, b), {'parent': 'minecraft:item/generated',
        'textures': {'layer0': 'minecraft:item/potion_overlay', 'layer1': 'minecraft:item/potion'}})
    put(R + '/filling/%s.json' % b, {'type': 'create:filling', 'ingredients': [item('minecraft:glass_bottle'),
        {'type': 'neoforge:tag', 'tag': 'c:' + n, 'amount': 250}], 'results': [{'id': ce(b)}]})
    put(R + '/emptying/%s.json' % b, {'type': 'create:emptying', 'ingredients': [item(ce(b))],
        'results': [{'id': 'minecraft:glass_bottle'}, {'id': ce(n), 'amount': 250}]})
    shapeless(b + '_from_bucket', [item(ce(n + '_bucket'))] + [item('minecraft:glass_bottle')] * 4, ce(b), count=4, category='misc')
adv('drink', 'fermenting', ce('wine_bottle'), {n: {'trigger': 'minecraft:consume_item', 'conditions': {'item': {'items': ce(n + '_bottle')}}}
                                              for n, _ in DRINKS})
LANG['advancements.%s.drink.title' % MODID] = 'One for the Road'
LANG['advancements.%s.drink.description' % MODID] = 'Drink a bottle of something you brewed'

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
