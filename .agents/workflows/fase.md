---
description: Implementa UNA fase (rebanada vertical) de PLAN.md, la verifica y se detiene
argument-hint: <número de fase>
---

# /fase $ARGUMENTS — una rebanada vertical

1. Leé `AGENTS.md`, `PLAN.md` (fase **$ARGUMENTS**) y `PROGRESS.md`. Si la fase no existe o
   la anterior no está marcada `[x]`, avisá y detenete.
2. Leé las skills del contexto (tabla §7 de AGENTS.md) ANTES de escribir código.
3. Marcá la fase `[/]` en `PLAN.md`.
4. Implementá en este orden, copiando el patrón hermano del juego base:
   entidad/repo → strategy/state/validator → service (interfaz + impl) → controller →
   tests (unit + slice + integración) → FE (models → service → ui → page → ruta) → specs.
5. Verificá: `bash scripts/verify.sh` (o sus pasos por separado si algo falla). Si la
   cobertura no llega al 95%, agregá los tests que faltan — nunca excludes.
6. Marcá la fase `[x]` en `PLAN.md` y anotá decisiones en su sección.
7. **Detenete**: resumí qué cambió (archivos y por qué), el resultado de la verificación y
   sugerí el mensaje de commit (Conventional Commits, en inglés, sin atribución de IA).
   No empieces la fase siguiente.
