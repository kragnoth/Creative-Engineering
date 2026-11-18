# Creative Engineering

A planned NeoForge 1.21.1 addon for **Create 6.0.10**. It brings Immersive Engineering and Immersive Petroleum style industry (coke ovens, blast furnaces, an arc crucible, oil extraction and refining, liquid-fuel engines) into Create, built the Create way.

By Kragnoth.

| Path | What |
|---|---|
| `HANDOFF.md` | Current status, next goal, open decisions: **start here** (also published as a [Claude page](https://claude.ai/artifact/8m4JNJxdnfpEGVC6iW5PYN)) |
| `docs/ie-ip-gap/` | IE + IP vs Create gap worksheet (xlsx + CSV), method and versions |
| `docs/create-art-style.md` | Create's palettes, model and code conventions, asset-license rule |
| `docs/registry-dumps/` | Registry IDs from a headless server running Create + IE + IP |
| `tools/` | Reference setup, headless server, palette/model/recipe/config analysis, worksheet build |

## Quick start (bash; from fish run `bash tools/...`)
```
bash tools/setup_refs.sh        # pinned Create/IE/IP sources + jars into ~/ref
bash tools/setup_server.sh      # optional: headless server + registry dumps (Java 21)
bash tools/config_defaults.sh   # IE/IP config defaults
```

Third-party sources, jars and assets are never committed. The scripts fetch the exact pinned versions.
