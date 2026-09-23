# Scaffolding Backend — Parcial Programación III

Spring Boot 4.0 · Java 21 · Maven · H2 · Lombok · Springdoc · JaCoCo

## Arranque rápido

```bash
mvn clean test          # compila y corre todos los tests
mvn spring-boot:run     # levanta en http://localhost:8080
```

| URL | Qué es |
|-----|--------|
| http://localhost:8080/api/products | CRUD de ejemplo |
| http://localhost:8080/swagger-ui/index.html | Swagger UI |
| http://localhost:8080/h2-console | Consola H2 (JDBC: `jdbc:h2:mem:parcialdb`, user `sa`) |

## Estructura

```
ar.edu.utn.frc.tup.p4
├── configs/        RestClientConfig (cliente HTTP para API externa)
├── controllers/    ProductController — thin, delega al service
├── dtos/           ProductDTO, ProductCreateRequest (Bean Validation), ErrorApi
├── entities/       Product (JPA, @PrePersist para createdAt)
├── exceptions/     ProductNotFoundException, DuplicateProductException, GlobalExceptionHandler
├── external/       ExternalApiClient (ÚNICO módulo que llama APIs externas) + DTOs propios
├── mappers/        ProductMapper (entity ↔ DTO)
├── repositories/   ProductRepository (queries, orden en la query)
└── services/       ProductService (interfaz) + impl/ProductServiceImpl (lógica)
```

Tests en `src/test/java`:
- `ProductServiceImplTest` — unitario puro (Mockito), límites y caminos de error
- `ProductControllerTest` — MockMvc standalone + GlobalExceptionHandler (200/201/204/400/404)
- `ProductIntegrationTest` — @SpringBootTest flujo completo contra H2

## El día del parcial

1. Copiá esta carpeta y renombrala.
2. Renombrá la feature `Product` al dominio del enunciado (Refactor → Rename en IntelliJ
   renombra clase + usos): entity → repository → DTOs → mapper → service → controller →
   excepciones → tests. `data.sql` con los seeds del enunciado.
3. Reglas innegociables (ver skills): sin `var`, sin `record`, sin `@Autowired` en NINGUNA
   forma (tampoco en tests — usar `@TestConstructor`), `private final` + `@RequiredArgsConstructor`,
   DTOs siempre, un solo contrato de error (`ErrorApi`), status HTTP semánticos.
4. Si hay API externa: solo `external/` la llama; en tests SIEMPRE mockear `ExternalApiClient`.
5. Antes de entregar: `mvn clean test` y checklist de las skills.

## Cobertura

```bash
mvn clean test jacoco:report
# abre target/site/jacoco/index.html — mirar BRANCH coverage
```
