"""Rebuilds the creativeengineering.ponder.* lang keys in en_us.json from the Ponder scene source and plugin tag.
Run from the repo root after editing scene text. Ponder numbers each scene's texts text_1, text_2...
in the order showText(...).text("...") is called, which is the order they appear in the source.
Adapted from Tarbound's dev/gen_ponder_lang.py (MIT, Kragnoth)."""
import json, re, collections

MODID = 'creativeengineering'
PKG = 'mod/src/main/java/com/kragnoth/creativeengineering/compat/ponder'
SCENES = PKG + '/CEScenes.java'
PLUGIN = PKG + '/CEPonderPlugin.java'
LANG = 'mod/src/main/resources/assets/%s/lang/en_us.json' % MODID

src = open(SCENES, encoding='utf-8').read()
keys = collections.OrderedDict()
token = re.compile(r'scene\.title\("([^"]+)",\s*"([^"]*)"\)|\.text\("((?:[^"\\]|\\.)*)"\)')
scene, n = None, 0
for m in token.finditer(src):
    if m.group(1):
        scene, n = m.group(1), 0
        keys['%s.ponder.%s.header' % (MODID, scene)] = m.group(2)
    else:
        n += 1
        keys['%s.ponder.%s.text_%d' % (MODID, scene, n)] = bytes(m.group(3), 'utf-8').decode('unicode_escape')

plugin = open(PLUGIN, encoding='utf-8').read()
keys['%s.ponder.tag.%s' % (MODID, MODID)] = re.search(r'\.title\("([^"]*)"\)', plugin).group(1)
keys['%s.ponder.tag.%s.description' % (MODID, MODID)] = re.search(r'\.description\("([^"]*)"\)', plugin).group(1)

lang = json.load(open(LANG, encoding='utf-8'), object_pairs_hook=collections.OrderedDict)
for k in [k for k in lang if k.startswith(MODID + '.ponder.')]:
    del lang[k]
lang.update(keys)
with open(LANG, 'w', encoding='utf-8') as f:
    json.dump(lang, f, indent=2, ensure_ascii=False)
    f.write('\n')
print('%d ponder keys' % len(keys))
