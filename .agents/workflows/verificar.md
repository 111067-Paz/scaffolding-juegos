---
description: Corre el quality gate completo (tests BE + JaCoCo 95%, tests FE, build, auditoría, compose)
---

# /verificar

1. Corré `bash scripts/verify.sh` desde la raíz del repo.
2. Si algo falla, **no lo tapes**: mostrá el error real, identificá la causa raíz y proponé
   el arreglo mínimo. Para cobertura, abrí `BE/target/site/jacoco/index.html` (o el CSV
   `BE/target/site/jacoco/jacoco.csv`) y listá las clases/líneas sin cubrir.
3. Reportá una tabla: paso · resultado · detalle (tests corridos, % de cobertura de líneas).
