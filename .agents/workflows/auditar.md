---
description: Busca patrones prohibidos por AGENTS.md (BE y FE) y propone cómo corregirlos
---

# /auditar

1. Corré `bash scripts/audit.sh`.
2. Por cada ✗: archivo:línea, qué regla de AGENTS.md rompe y el cambio concreto para
   corregirlo (con el patrón correcto del scaffold como referencia).
3. Además revisá a mano lo que un grep no ve (mirá el diff de la fase actual):
   - ¿Algún controller con lógica o try/catch? ¿Algún service sin interfaz?
   - ¿Ownership con id del body en vez del token?
   - ¿Estado de juego en memoria (`static Map`, singleton con campos mutables)?
   - ¿`switch` sobre tipos que va a crecer? ¿Patrón agregado sin frase del enunciado que lo justifique?
   - ¿Métodos llamados desde templates? ¿`@for` sin `track`/`@empty`?
   - ¿Regex del FE distinto al del BE? ¿Keys JSON distintas al contrato?
4. No corrijas nada sin confirmación salvo que el humano lo pida.
