# CLAUDE.md

@AGENTS.md

## Claude Code

- Skills del proyecto en `.claude/skills/` (se cargan solas según el contexto; la tabla
  contexto → skill está en AGENTS.md §7).
- Comandos: `/arranque`, `/fase <N>`, `/verificar`, `/auditar`, `/entrega` (`.claude/commands/`).
- Si editás una skill o un comando, corré `scripts/sync-agents.sh` (o `.ps1`) para actualizar
  la copia portable en `.agents/`.
- Comandos útiles: `./mvnw -f BE/pom.xml verify` · `npm --prefix FE run test:ci` ·
  `npm --prefix FE start` · `docker compose up --build -d`.
