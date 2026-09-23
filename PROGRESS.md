# PROGRESS — estado entre sesiones (handoff)

> Se actualiza al cerrar cada sesión o cuando el contexto está por agotarse. Lo lee cualquier
> agente al empezar (AGENTS.md §6).

## Estado actual

| Módulo | Estado | Tests |
|---|---|---|
| Scaffold: auth (cookie HttpOnly + Bearer), juego base, Docker | ✅ | BE 154 · FE 38 · JaCoCo 99% |

## Decisiones tomadas

- H2 `create-drop` en dev/test; PostgreSQL `update` en Docker (las partidas sobreviven).
- Sin `data.sql`: el tablero lo arma `BoardFactory`; usuarios por registro.
- Fechas con `@JsonFormat` por campo (`dd-MM-yyyy HH:mm:ss`), sin config global.
- Auth sin Spring Security: interceptor JWT; la SPA usa cookie HttpOnly, API clients Bearer.

## Próximos pasos

- [ ] Pegar la consigna en `ENUNCIADO.md` y correr `/arranque`.
