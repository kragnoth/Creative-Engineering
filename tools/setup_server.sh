#!/usr/bin/env bash
# Headless NeoForge 1.21.1 server with Create + IE + IP, then dumps registries to docs/registry-dumps.
# Needs Java 21. Usage: bash tools/setup_server.sh [SERVER_DIR] [REF_DIR]
# Note: Create 6.0.10 / Ponder need NeoForge >= 21.1.219 — 21.1.164 (IE/IP's dev pin) will NOT boot with Create.
set -euo pipefail
SRV="${1:-$HOME/srv}"; REF="${2:-$HOME/ref}"; REPO="$(cd "$(dirname "$0")/.." && pwd)"
NEO="${NEO_VERSION:-21.1.267}"
mkdir -p "$SRV/mods" && cd "$SRV"
[ -f run.sh ] || { curl -sSLo installer.jar "https://maven.neoforged.net/releases/net/neoforged/neoforge/$NEO/neoforge-$NEO-installer.jar"
                   java -jar installer.jar --installServer . > install.log 2>&1; }
cp -n "$REF"/jars/*.jar mods/
echo eula=true > eula.txt
printf 'online-mode=false\nlevel-type=minecraft\\:flat\nspawn-protection=0\ngenerate-structures=false\n' > server.properties
echo "-Xmx3G" > user_jvm_args.txt

rm -f in.fifo; mkfifo in.fifo; (sleep 99999 > in.fifo &)
./run.sh nogui < in.fifo > server.out 2>&1 &
for i in $(seq 1 90); do sleep 5; grep -qE "Done \(|Failed to start" server.out && break; done
grep -q "Done (" server.out || { echo "Server failed — see $SRV/server.out"; exit 1; }

for r in item block block_entity_type recipe_type recipe_serializer fluid entity_type menu villager_profession particle_type; do
  echo "neoforge dump registry minecraft:$r true" > in.fifo
done
sleep 6; echo stop > in.fifo; sleep 5
mkdir -p "$REPO/docs/registry-dumps"; cp dumps/registry/minecraft/*.txt "$REPO/docs/registry-dumps/"
for f in "$REPO"/docs/registry-dumps/*.txt; do
  printf "%-22s ie=%-4s ip=%-4s create=%s\n" "$(basename "$f" .txt)" \
    "$(grep -c '^immersiveengineering:' "$f")" "$(grep -c '^immersivepetroleum:' "$f")" "$(grep -c '^create:' "$f")"
done
