#!/usr/bin/env bash
# Greps the code for patterns FORBIDDEN by AGENTS.md. Exit code 1 if anything is found.
# Usage (from the repo root, Git Bash on Windows works too): bash scripts/audit.sh
set -u
cd "$(dirname "$0")/.."

found=0
check() {
  local label="$1" pattern="$2"; shift 2
  local hits
  # Comment lines (Javadoc, //, #) may MENTION a forbidden word: they are not violations.
  hits=$(grep -rnE "$pattern" "$@" 2>/dev/null | grep -vE ':[0-9]+:\s*(\*|//|/\*|#|<!--)')
  if [ -n "$hits" ]; then
    echo "✗ $label"
    echo "$hits" | sed 's/^/    /'
    found=1
  else
    echo "✓ $label"
  fi
}

BE_SRC="BE/src"
FE_APP="FE/src/app"

echo "── Backend ──"
check "@Autowired" '@Autowired' "$BE_SRC"
check "var (use explicit types)" '(^|[;{(])\s*(final\s+)?var\s+[A-Za-z_]\w*\s*[=:]' "$BE_SRC"
check "record (use Lombok classes)" '\brecord\s+[A-Z]\w*\s*[(<]' "$BE_SRC"
check "ModelMapper" 'ModelMapper' "$BE_SRC" BE/pom.xml
check "@EntityGraph / EAGER" '@EntityGraph|FetchType\.EAGER' "$BE_SRC"
check "@Data on entities" '@Data\b' "$BE_SRC/main/java/ar/edu/utn/frc/tup/p4/entities"
check "System.out / printStackTrace" 'System\.(out|err)\.|printStackTrace\(' "$BE_SRC/main"
check "double/float money" '\b(double|float)\s+\w*([Pp]rice|[Aa]mount|[Tt]otal|[Bb]alance|[Cc]ost)' "$BE_SRC/main"

echo "── Frontend ──"
check "any" ':\s*any\b|<any>|\bas any\b' "$FE_APP"
check "@Input/@Output/@ViewChild decorators" '@(Input|Output|ViewChild|ContentChild)\(' "$FE_APP"
check "legacy structural directives" '\*ng(If|For|Switch)' "$FE_APP"
check "NgModule" '@NgModule' "$FE_APP"
check "constructor injection" 'constructor\s*\(\s*(private|public|protected|readonly)' "$FE_APP"
check "BehaviorSubject for UI state" 'BehaviorSubject' "$FE_APP"
check "ngModel (use Reactive Forms)" 'ngModel' "$FE_APP"
check "console.* outside LoggerService" 'console\.(log|debug|info|warn|error)' "$FE_APP" --exclude=logger.service.ts --exclude=*.spec.ts
check "token storage in the browser" '(localStorage|sessionStorage)\.' "$FE_APP" --exclude=*.spec.ts
check "hardcoded hosts" 'https?://(localhost|127\.0\.0\.1|[0-9]+\.[0-9]+\.)' "$FE_APP" FE/src/environments

echo "── Repo ──"
if git ls-files --error-unmatch .env >/dev/null 2>&1; then
  echo "✗ .env is committed (secrets!) → git rm --cached .env"; found=1
else
  echo "✓ .env not committed"
fi
check "absolute local paths in docs" '[A-Za-z]:\\\\[A-Za-z]|file:///[A-Za-z]:' $(git ls-files '*.md') $(git ls-files -o --exclude-standard '*.md')

if [ "$found" -ne 0 ]; then
  echo; echo "AUDIT FAILED: fix the items marked with ✗"; exit 1
fi
echo; echo "AUDIT OK"
