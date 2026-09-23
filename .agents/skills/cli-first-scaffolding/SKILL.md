---
name: cli-first-scaffolding
description: >
  Flujo CLI-first para crear proyectos y artefactos: el agente entrega los COMANDOS
  (ng new, ng generate, curl a start.spring.io) y el humano los ejecuta; el agente solo
  escribe el CONTENIDO de los archivos después. Ahorra tokens y garantiza estructura
  oficial generada por las herramientas.
  Trigger: al iniciar un proyecto nuevo (Angular o Spring Boot), crear componentes,
  services, guards, pipes, o cuando el usuario pida scaffolding o estructura de proyecto.
license: Apache-2.0
metadata:
  author: PazLuciano
  version: "2.0"
---

# cli-first-scaffolding

**Regla de flujo**: para crear proyectos y artefactos, el agente NO escribe la estructura a
mano — entrega los comandos y el humano los corre. El agente escribe el CONTENIDO de los
archivos una vez que el CLI generó el esqueleto.

**Por qué**: (1) el CLI genera estructura oficial, consistente y con specs incluidos;
(2) cuesta una fracción de los tokens; (3) el humano mantiene el control y aprende los
comandos que va a necesitar en el parcial sin asistencia.

## Cuándo aplicar esta skill

- Al crear un proyecto nuevo de Angular o Spring Boot.
- Al agregar componentes, services, guards, pipes, interfaces.
- Cuando el usuario pida "scaffolding", "estructura" o "crear proyecto".

## Protocolo

1. Entregar los comandos exactos, listos para copiar y pegar, con cada flag explicado.
2. Esperar a que el humano confirme que los corrió.
3. Recién entonces escribir/editar el contenido de los archivos generados.

Excepción: archivos que ningún CLI genera (`proxy.conf.json`, `.postcssrc.json`, Dockerfiles,
`docker-compose.yml`, `nginx.conf`, configs) sí se escriben directo.

**En este repo el proyecto YA existe** (BE + FE + Docker). Lo habitual es solo generar
artefactos nuevos dentro de `features/` y copiar clases Java hermanas del juego base.

---

## Cheat sheet — Frontend (Angular 21)

```bash
# Proyecto nuevo — SIEMPRE pinear la major (latest puede dar Angular 22+)
npx -y @angular/cli@21 new mi-app --style=css --ssr=false --skip-git --defaults
cd mi-app

# Tailwind 4
npm install tailwindcss @tailwindcss/postcss postcss --save-dev
# → crear .postcssrc.json con {"plugins": {"@tailwindcss/postcss": {}}}
# → en styles.css: @import "tailwindcss";

# Artefactos (estructura core/ + features/{x}/pages·ui·data-access)
ng g c features/game/pages/game-board-page   # smart (page)
ng g c features/game/ui/dice-panel           # dumb (ui)
ng g s features/game/data-access/game        # service con httpResource
ng g i features/game/data-access/game --type=models
ng g guard core/auth/auth                    # elegir CanActivate → funcional
ng g interceptor core/http/error             # interceptor funcional
ng g environments                            # environment.ts + .development.ts

# Flags útiles
#   --dry-run          muestra qué crearía SIN escribir nada
#   --inline-template  template en el .ts (componentes chicos)
#   --skip-tests       sin .spec (NO usar en el parcial — los tests suman)

# Correr
npm start                          # ng serve con proxy.conf.json (/api → :8080)
npm run test:ci                    # Vitest una vez (sin watch)
npm run build                      # build de producción (lo que usa el Dockerfile)
```

## Cheat sheet — Backend (Spring Boot 4 + Maven)

```bash
# Proyecto nuevo desde start.spring.io (sin abrir el browser)
curl -s "https://start.spring.io/starter.zip" \
  -d type=maven-project -d language=java \
  -d bootVersion=4.0.0 -d javaVersion=21 \
  -d groupId=ar.edu.utn.frc.tup -d artifactId=p4 \
  -d packageName=ar.edu.utn.frc.tup.p4 \
  -d dependencies=web,data-jpa,h2,postgresql,validation,lombok,devtools,actuator \
  -o parcial.zip && unzip -q parcial.zip -d parcial && rm parcial.zip

# Dependencias que start.spring.io no trae (agregar al pom a mano):
#   springdoc-openapi-starter-webmvc-ui 3.0.0  (Swagger para Boot 4)
#   jacoco-maven-plugin 0.8.12 + regla check 0.95 (cobertura que rompe el build)
#   jjwt 0.12.6 + spring-security-crypto       (JWT + BCrypt, sin Spring Security)

# Ciclo
./mvnw clean test                  # compilar + tests
./mvnw verify                      # tests + JaCoCo report + check ≥95%
./mvnw spring-boot:run             # levantar en :8080 (perfil dev, H2)
# Si el wrapper no descarga Maven (proxy/red), usar un mvn instalado: mvn verify

# Docker (skill docker-stack)
docker compose up --build -d       # Postgres + backend + frontend en :4200
docker compose logs -f backend
```

Las clases Java no tienen generador — se crean a mano siguiendo la estructura de capas de
la skill `springboot-layered-api` (controllers/, services/ + impl/, repositories/,
entities/, dtos/, mappers/, exceptions/, external/, configs/).

## Gotchas verificados

- `npx @angular/cli@latest` instala la ÚLTIMA major (hoy 22) — para el parcial pinear `@21`.
- El CLI de Angular 21 exige Node ≥ 24.15 (o ≥ 22.22.3): verificar con `node --version`.
- El instalador de Node en Windows necesita terminal de administrador (winget falla 1603 sin elevar).
- Paths con espacios: entre comillas en Git Bash (`cd "…/UTN 2026/…"`). No dejar rutas
  absolutas de una PC en docs ni scripts: el repo se usa en otras máquinas.
