#!/usr/bin/env bash
# Rebuilds the reference workspace used by this repo's tools.
# Third-party sources/jars are NOT committed to this repo (licenses) — this script re-fetches the exact pinned versions.
# Usage: bash tools/setup_refs.sh [REF_DIR]   (default: ~/ref)
set -euo pipefail
REF="${1:-$HOME/ref}"
mkdir -p "$REF" && cd "$REF"

clone_pinned () { # dir url branch_or_tag commit
  if [ ! -d "$1/.git" ]; then git clone -q --depth 1 --branch "$3" "$2" "$1"; fi
  echo "$1 @ $(git -C "$1" rev-parse --short HEAD) (expected ${4:0:7})"
}
# Create 6.0.10 — code MIT, assets All Rights Reserved: reference textures by resource location, never copy them.
clone_pinned create https://github.com/Creators-of-Create/Create.git mc1.21.1-6.0.10 ac0c444d9828da3453ae8cc65338e8de063286fb
# Immersive Engineering 1.21.1 branch (snapshot used: 75a27f0, 2025-08-18)
clone_pinned ie https://github.com/BluSunrize/ImmersiveEngineering.git 1.21.1 75a27f03e4243544243567e8d5c38d336f4f10f4
# Immersive Petroleum 1.21.1 branch (snapshot used: 11fe4dc, 2026-11-11)
clone_pinned ip https://github.com/TwistedGate/ImmersivePetroleum.git 1.21.1 11fe4dc78c436a7ff041c0544c52bc93afb7c546

# Release jars (same versions loaded on the headless server)
mkdir -p jars && cd jars
dl () { [ -f "$1" ] || curl -sSLo "$1" "$2"; }
dl create-1.21.1-6.0.10.jar https://cdn.modrinth.com/data/LNytGWDc/versions/UjX6dr61/create-1.21.1-6.0.10.jar
dl ImmersiveEngineering-1.21.1-12.4.2-194.jar https://cdn.modrinth.com/data/tIm2nV03/versions/uNRARSH2/ImmersiveEngineering-1.21.1-12.4.2-194.jar
dl ImmersivePetroleum-1.21.1-4.5.0-39.jar https://cdn.modrinth.com/data/MOw5TN6u/versions/1nu6ZI52/ImmersivePetroleum-1.21.1-4.5.0-39.jar
for j in create Immersive{Engineering,Petroleum}; do
  d="unpacked/$(echo $j | tr 'A-Z' 'a-z')"; mkdir -p "$d"; unzip -qo ${j}*.jar -d "$d"
done
cd ..

cat <<'EOF'
Done. Not fetched automatically (attach or generate yourself):
  - NeoForge 1.21.1 source  : git clone -b 1.21.x https://github.com/neoforged/NeoForge  (or Kragnoth's zip)
  - Decompiled MC 1.21.1    : run ModDevGradle setup in any 1.21.1 project, or attach Kragnoth's minecraft_decompiled.zip
    (Mojang code — never commit it to this repo)
EOF
