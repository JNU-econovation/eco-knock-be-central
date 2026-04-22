# AGENTS.md

## Skills
A skill is a set of local instructions to follow that is stored in a `SKILL.md` file.
Below is the list of repository-local skills that can be used in this project.

### Available skills
- `git-commit-korean`: Inspect this repository's git history and current diff, then draft or create git commits that match the local convention. Use when the user asks to write a commit message, make a git commit, summarize changes into a commit, keep commit messages in Korean, or split changes into small logical commits aligned with recent repository history. (file: `./skills/git-commit-korean/SKILL.md`)
- `readme-maintainer`: Update this repository's README so it matches the current implemented scope. Use when the user asks to create, rewrite, or refresh README content, document new features, update setup or deployment instructions, or keep the README in sync with current code, configuration, Docker deployment, Raspberry Pi behavior, or project limitations. (file: `./skills/readme-maintainer/SKILL.md`)
- `eco-knock-maintainer`: Maintain eco-knock-be-central code in the repository's established style. Use when modifying, reviewing, refactoring, testing, or organizing project code, especially around layered architecture, airquality CQRS boundaries, Spring Boot/Kotlin/Java style, controllers, services, repositories, Flyway migrations, and E2E tests. (file: `./skills/eco-knock-maintainer/SKILL.md`)

## How to use skills
- Discovery: The list above is the repository-local skill registry for this project.
- Trigger rules: If the user names a skill directly, or the task clearly matches a listed skill, read that `SKILL.md` and follow it for the current turn.
- Scope: Do not carry a skill across turns unless the user mentions it again or the next task still clearly matches it.
- Missing or blocked: If a listed skill file cannot be opened, say so briefly and continue with the best fallback.

## Local guidance
- Prefer repository-local skills in `./skills` before inventing ad-hoc workflow rules.
- When adding a new local skill under `./skills`, also add it to the `Available skills` list in this file so it can be auto-discovered in future sessions.
- Keep this file short. Put detailed task instructions in the skill's `SKILL.md`, not here.
