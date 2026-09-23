# Scaffolding de juegos — Parcial Programación (UTN-FRC TUP)

Base full stack para parciales de **juegos por turnos**: Spring Boot 4 + Java 21 · Angular 21 ·
PostgreSQL · Docker Compose · agentes de IA (Claude Code, Antigravity, Cursor…).

Trae un **juego base funcionando** ("carrera de dados": partida, jugadores hot-seat, tablero con
casillas especiales, dado, vidas, turnos, historial) que se **adapta** al enunciado en vez de
empezar de cero. Tiene State, Strategy + Registry, Factory y Chain of Responsibility, cada uno
con sus tests.

| Capa | Stack |
|---|---|
| Backend | Spring Boot 4.0 · Java 21 · Maven · JPA · H2 (dev/test) · PostgreSQL (docker) · JWT (cookie HttpOnly + Bearer) · Springdoc · JaCoCo ≥ 95% |
| Frontend | Angular 21 standalone zoneless · Signals · `httpResource` · Reactive Forms · Tailwind 4 · Vitest |
| Infra | Docker multi-stage · Nginx (SPA + proxy `/api`) · docker-compose con healthchecks |

## Arranque rápido

### Opción A — Docker (todo junto, como se entrega)

```bash
cp .env.example .env          # completar APP_JWT_SECRET (≥ 32 caracteres)
docker compose up --build -d
# http://localhost:4200
```

### Opción B — Desarrollo (hot reload)

```bash
# Terminal 1 — backend en :8080 (H2 en memoria)
cd BE && ./mvnw spring-boot:run          # o: mvn spring-boot:run
# Terminal 2 — frontend en :4200 (proxy /api → :8080)
cd FE && npm install && npm start
```

Swagger: http://localhost:8080/swagger-ui/index.html (opción B) · H2: http://localhost:8080/h2-console

## Calidad

```bash
bash scripts/verify.sh     # tests BE + JaCoCo ≥95% · tests FE · build · auditoría · compose
bash scripts/audit.sh      # solo la búsqueda de patrones prohibidos
```

## El día del parcial (flujo con agente)

1. Crear el repo del parcial y copiar este scaffold (sin `.git`).
2. Pegar la consigna cruda en `ENUNCIADO.md`.
3. `/arranque` → el agente propone modelo, reglas, endpoints, patrones y fases en `PLAN.md`.
   Revisar y aprobar.
4. `/fase 1`, `/fase 2`… → una rebanada vertical por vez. Commitear al final de cada fase.
5. `/verificar` cuando quieras; `/entrega` antes de entregar (clone limpio + Docker + smoke).

Sin Claude Code: `AGENTS.md` tiene las reglas y `.agents/workflows/*.md` los mismos pasos
para seguirlos a mano o con otra herramienta.

## Estructura

```
├── AGENTS.md · CLAUDE.md          reglas para agentes (fuente de verdad) · entrada de Claude Code
├── ENUNCIADO.md · PLAN.md · PROGRESS.md   consigna cruda · plan táctico · estado entre sesiones
├── docker-compose.yml · .env.example
├── scripts/                       verify.sh · audit.sh · sync-agents.sh/.ps1
├── .claude/ skills/ commands/ settings.json     (fuente)
├── .agents/ skills/ workflows/                  (copia portable, generada por sync-agents)
├── BE/  controllers · services(+impl) · services/game/{state,effects,validation} · repositories
│        entities · dtos · mappers · exceptions · external · configs
└── FE/  src/app/core/{auth,http,logger,models,layout} · features/{auth,game}/{pages,ui,data-access}
```

## `.gitignore` y la entrega

`.claude/` y `.agents/` están en `.gitignore`: **la configuración de IA no se entrega** en el
repo del parcial. En ESTE repo (el scaffold) están versionadas porque se agregaron con
`git add -f`. Si editás una skill o un comando acá:

```bash
bash scripts/sync-agents.sh              # .claude → .agents
git add -f .claude .agents && git commit -m "docs(agents): ..."
```

El juego base **sí** se versiona: se adapta al enunciado (ver skill `game-engine`,
`references/adaptar-juegos.md`). `.env` nunca se commitea (solo `.env.example`).

## Skills incluidas

`springboot-layered-api` · `jpa-modelado` · `java21-clean-code` · `game-engine` · `java-testing` ·
`angular-spa` · `html-accesible-responsive` · `docker-stack` · `fechas-argentina-fullstack` ·
`cli-first-scaffolding`. Cada `SKILL.md` es corto; el detalle está en su carpeta `references/`
y se lee solo cuando hace falta.
