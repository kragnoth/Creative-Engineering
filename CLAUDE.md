# Claude: read HANDOFF.md first
`HANDOFF.md` says where the last session stopped, the next goal, the decisions still open and the exact steps to take. Update it before you end a session.

- Pinned reference sources and jars: `bash tools/setup_refs.sh` (they go in ~/ref, never into this repo).
- Gap worksheet: `docs/ie-ip-gap/`. Edit `tools/rows.py`, then run `tools/export.py` and `tools/build_xlsx.py`.
- Create art rules: `docs/create-art-style.md`. Reference `create:` textures; never copy Create assets.
- Commit as Kragnoth. Fish-shell syntax for anything Kragnoth runs locally. No client/server launches unless asked.
