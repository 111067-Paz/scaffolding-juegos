---
description: Chequeo previo a entregar — auditoría, clone limpio con tests, docker compose y smoke test
---

# /entrega — lo que recibe el corrector

1. `git status`: si hay cambios sin commitear, listalos y preguntá antes de seguir.
2. `bash scripts/audit.sh` → tiene que dar `AUDIT OK`.
3. **Clone limpio** (lo que ve el corrector, sin archivos ignorados ni builds locales):
   ```bash
   TMP=$(mktemp -d) && git clone -q . "$TMP/entrega" && cd "$TMP/entrega"
   (cd BE && ./mvnw -B -q verify || mvn -B -q verify)       # tests + JaCoCo ≥95%
   npm --prefix FE ci && npm --prefix FE run test:ci && npm --prefix FE run build
   ```
   Si falla ahí y no en tu carpeta, algo commiteado depende de un archivo no versionado.
4. **Docker** (en el clone): `cp .env.example .env` (poner un `APP_JWT_SECRET` real),
   `docker compose up --build -d`, esperar `healthy` en `docker compose ps`.
5. **Smoke** contra `http://localhost:${FE_PORT:-4200}` con curl y un cookie jar:
   `GET /` → 200 · `GET /games` (ruta profunda) → 200 · `POST /api/auth/register` →
   201 + `Set-Cookie` HttpOnly · `GET /api/auth/me` con la cookie → 200 · crear partida →
   `start` → `roll` hasta el final → `docker compose restart backend` → la partida sigue.
6. `docker compose down -v` y borrar la carpeta temporal.
7. Reporte final: checklist ✓/✗ de cada paso + recordatorios: `.env` NO commiteado,
   `.claude/` y `.agents/` no se entregan (están en `.gitignore`), `PROGRESS.md` actualizado.
