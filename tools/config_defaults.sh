#!/usr/bin/env bash
# Prints the config defaults the IE/IP manuals leave blank (ore Y-ranges, wire limits, capacitors, generator output, IP machines).
# Usage: bash tools/config_defaults.sh [REF_DIR]
REF="${1:-$HOME/ref}"
IE="$REF/ie/src/main/java/blusunrize/immersiveengineering/common/config/IEServerConfig.java"
IP="$REF/ip/src/main/java/flaxbeard/immersivepetroleum/common/cfg/IPServerConfig.java"
echo "== IE wires (name, maxLength, IF/t, loss per 16 blocks)"; grep -oE 'new (Energy)?WireConfig\(builder, "[a-z_]+", [0-9., ]+\)' "$IE"
echo "== IE capacitors (storage, in, out)"; grep -oE 'new CapacitorConfig\(builder, [^,]+, "[a-z]+", [0-9, ]+\)' "$IE" | sed 's/() -> IEBlockEntities\.//'
echo "== IE ore veins (metal, distribution, airExposure, veinSize, minY, maxY, perChunk)"; grep -E '^\s+[A-Z_]+\(EnumMetals' "$IE" | sed 's/^\s*//'
echo "== IE generator / misc"; grep -oE 'defineInRange\("(dynamo_output|thermoelectric_output|lightning_output|dieselGen_output|consumption|speedupConsumption|yield|chance|initial_depletion)", [^,]+' "$IE"
echo "== IP"; grep -oE '\.define\("[a-zA-Z_]+", [0-9.]+\)' "$IP"
