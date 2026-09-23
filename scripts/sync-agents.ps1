# Windows PowerShell version of sync-agents.sh.
# Copies .claude/skills and .claude/commands to the portable .agents/ folder.
$ErrorActionPreference = 'Stop'
Set-Location (Join-Path $PSScriptRoot '..')
Remove-Item -Recurse -Force .agents/skills, .agents/workflows -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force .agents | Out-Null
Copy-Item -Recurse .claude/skills .agents/skills
Copy-Item -Recurse .claude/commands .agents/workflows
Write-Output 'Synced .claude/{skills,commands} -> .agents/{skills,workflows}'
