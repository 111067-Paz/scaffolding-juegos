#!/usr/bin/env bash
# Full local quality gate: backend tests + JaCoCo >= 95%, frontend tests + build,
# forbidden-pattern audit and compose file validation. Stops at the first failure.
# Usage: bash scripts/verify.sh
set -euo pipefail
cd "$(dirname "$0")/.."

MVN="./BE/mvnw"
if ! "$MVN" -v >/dev/null 2>&1; then
  MVN="mvn"   # wrapper could not download Maven (offline / proxy): fall back to installed Maven
fi

echo "▶ Backend: tests + JaCoCo check"
"$MVN" -B -q -f BE/pom.xml verify

echo "▶ Frontend: tests"
npm --prefix FE run test:ci

echo "▶ Frontend: production build"
npm --prefix FE run build

echo "▶ Audit"
bash scripts/audit.sh

if command -v docker >/dev/null 2>&1; then
  echo "▶ docker compose config"
  if [ -f .env ]; then docker compose config -q; else APP_JWT_SECRET=verify-only docker compose config -q; fi
fi

echo "✔ VERIFY OK"
