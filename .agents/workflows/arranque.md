---
description: Arranque del parcial — lee el enunciado, propone el modelo y el plan en fases, y se detiene
---

# /arranque — del enunciado al plan (NO escribe código)

Sos arquitecto de software y desarrollador full stack senior (Java 21 + Spring Boot 4,
Angular 21). El humano dirige la arquitectura; vos proponés, justificás y esperás.

## Pasos

1. Leé `AGENTS.md`, `PROGRESS.md`, `PLAN.md` y `ENUNCIADO.md`. Si `ENUNCIADO.md` está vacío,
   pedí que peguen la consigna y **detenete**.
2. Leé la skill `game-engine` (y su `references/adaptar-juegos.md`). Recorré el juego base:
   `BE/src/main/java/ar/edu/utn/frc/tup/p4/services/game/**`, `entities/`, `FE/src/app/features/game/`.
3. Extraé del enunciado y escribí en `PLAN.md`:
   - **Glosario**: sustantivos del enunciado → entidad / atributo / enum.
   - **Modelo**: entidades, relaciones (cardinalidad, dueño, cascade), `@Version` si aplica.
   - **Reglas de juego numeradas** (R1, R2…), cada una con su caso límite.
   - **Endpoints**: método, ruta, request/response JSON (keys exactas del contrato), status.
   - **Patrones**: qué frase del enunciado justifica cada uno (o "no aplica").
   - **Juego base**: qué se reutiliza, qué se renombra, qué se borra.
   - **Fases** (rebanadas verticales, backend antes que frontend), cada una con criterio de
     terminado y tests esperados.
   - **Dudas / ambigüedades**: lista explícita. NO inventes requisitos.
4. Presentá un resumen corto del plan y las dudas. **Detenete y esperá aprobación.**
   No crees ni modifiques código en este comando.
