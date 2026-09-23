#!/usr/bin/env bash
# Copies the Claude Code skills and commands to the portable .agents/ folder
# (Antigravity and other agents read .agents/skills and .agents/workflows).
# Source of truth: .claude/. Run after editing a skill or a command.
set -euo pipefail
cd "$(dirname "$0")/.."
rm -rf .agents/skills .agents/workflows
mkdir -p .agents
cp -r .claude/skills .agents/skills
cp -r .claude/commands .agents/workflows
echo "Synced .claude/{skills,commands} → .agents/{skills,workflows}"
