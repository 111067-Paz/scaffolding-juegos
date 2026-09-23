---
name: docker-stack
description: >
  Levantar el parcial con Docker: Dockerfiles multi-stage (Maven → JRE alpine no-root; Node →
  Nginx), nginx.conf que sirve la SPA y hace proxy de /api (mismo origen, cookies sin CORS),
  docker-compose con PostgreSQL + backend (perfil docker) + frontend encadenados por
  healthchecks, secretos por .env, persistencia en volumen, y troubleshooting.
  Trigger: al crear o editar Dockerfile, docker-compose.yml, nginx.conf, .env, perfiles de
  Spring (application-docker.properties), o cuando el usuario diga "docker", "compose",
  "contenedor", "imagen", "levantar todo" o "Postgres".
license: Apache-2.0
metadata:
  author: PazLuciano
  version: "1.0"
---

# docker-stack

## 1. Arquitectura

```
navegador ──► :4200 frontend (Nginx)
                 ├── /            → SPA (index.html, fallback para rutas de Angular)
                 └── /api/*       → http://backend:8080   (red interna de compose)
                                        └── jdbc:postgresql://db:5432/p4  (volumen pgdata)
```

- **Mismo origen** para SPA y API → sin CORS, y la cookie HttpOnly (`Path=/api`) viaja sola.
- El FE usa siempre `environment.apiUrl = '/api'`: en dev lo resuelve `proxy.conf.json`
  (→ `localhost:8080`), en Docker lo resuelve Nginx. El código no cambia entre entornos.
- El backend NO publica puerto (descomentar `ports: 8080:8080` en compose solo para
  Postman/Swagger).

## 2. Comandos

```bash
cp .env.example .env                 # primera vez; editar APP_JWT_SECRET
docker compose up --build -d         # construir y levantar todo
docker compose ps                    # estado + health de cada servicio
docker compose logs -f backend       # logs de un servicio
docker compose restart backend       # las partidas siguen ahí (PostgreSQL)
docker compose down                  # parar (conserva el volumen = datos)
docker compose down -v               # parar y BORRAR datos
docker compose config                # validar el YAML con las variables resueltas
```

## 3. Piezas (ya están en el repo — adaptar, no reescribir)

| Archivo | Clave |
|---|---|
| `BE/Dockerfile` | stage 1 `maven:3.9-eclipse-temurin-21` (`dependency:go-offline` cacheado, `package -DskipTests`); stage 2 `eclipse-temurin:21-jre-alpine`, usuario no-root |
| `FE/Dockerfile` | stage 1 `node:22-alpine` (`npm ci`, `npm run build`); stage 2 `nginx:1.27-alpine` con `dist/FE/browser` |
| `FE/nginx.conf` | `location /api/ { proxy_pass http://backend:8080; }` + `try_files $uri $uri/ /index.html` |
| `docker-compose.yml` | `db` (healthcheck `pg_isready`) → `backend` (`depends_on: service_healthy`, healthcheck `/actuator/health/readiness`) → `frontend` |
| `application-docker.properties` | datasource por env, `ddl-auto=update`, `sql.init.mode=never` |
| `.env.example` | variables; `.env` real está en `.gitignore` |

- Los tests NO corren dentro de la imagen (`-DskipTests`): se corren antes con `./mvnw verify`.
- Distroless (`gcr.io/distroless/java21-debian12`) es más chica pero no tiene shell → el
  healthcheck con `wget` no funciona. Usarla solo si la cátedra la pide, cambiando el healthcheck.
- Si el `name` del proyecto Angular cambia, cambia la carpeta de salida (`dist/<name>/browser`)
  → actualizar `FE/Dockerfile`.

## 4. Reglas

- Secretos **solo** por variables de entorno (`${APP_JWT_SECRET:?...}` hace fallar el compose
  si falta). Nunca en `application*.properties` versionado ni en el Dockerfile.
- Nada de `localhost` entre contenedores: se usan los nombres de servicio (`db`, `backend`).
- El estado del juego vive en PostgreSQL (volumen) → sobrevive a `restart` y a `down` sin `-v`.
- Healthchecks en todo lo que otro servicio espera; `depends_on` con `condition: service_healthy`.
- Imágenes con tag fijo (`postgres:17-alpine`), nunca `latest`.

## 5. Troubleshooting

| Síntoma | Causa probable | Arreglo |
|---|---|---|
| `backend` unhealthy / reinicia | no conecta a la DB, secreto corto | `docker compose logs backend`; revisar `.env` (JWT ≥ 32 bytes) |
| 502 Bad Gateway en `/api` | backend todavía arrancando o caído | esperar el health; `docker compose ps` |
| 404 al refrescar `/games/5` | falta el fallback SPA | `try_files ... /index.html` en `nginx.conf` |
| Login OK pero después 401 | cookie no viaja (otro origen / puerto) | entrar siempre por `:4200` (Nginx), no directo a `:8080` |
| Cambié código y no se ve | imagen vieja | `docker compose up --build` |
| Datos de pruebas viejos | volumen persistente | `docker compose down -v` |
| `port is already allocated` | 4200 ocupado por `ng serve` | cambiar `FE_PORT` en `.env` o parar el dev server |
| `429 Too Many Requests` al bajar imágenes | rate limit de Docker Hub | `docker login` o esperar; las imágenes quedan cacheadas |

## Checklist

- [ ] `docker compose config` sin errores; `.env` creado desde `.env.example`
- [ ] `docker compose up --build -d` → los 3 servicios `healthy`/`running`
- [ ] `http://localhost:4200` carga; refrescar una ruta profunda no da 404
- [ ] Registro → partida → jugar funciona vía Nginx (cookie)
- [ ] `docker compose restart backend` y la partida sigue existiendo
