# Backend — Spring Boot 4 · Java 21

Ver el [README principal](../README.md) y las reglas en [AGENTS.md](../AGENTS.md).

```bash
./mvnw spring-boot:run     # :8080, perfil dev (H2). Si el wrapper no descarga Maven: mvn ...
./mvnw verify              # tests + JaCoCo report + check ≥95% (target/site/jacoco/index.html)
```

Perfiles: `default` (H2 create-drop) · tests (`src/test/resources`, H2) · `docker` (PostgreSQL por
variables de entorno, lo activa docker-compose).
