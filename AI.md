# AI.md

## Skills
A skill is a set of local instructions to follow that is stored in a `SKILL.md` file.
Below is the list of repository-local skills that can be used in this project.

### Available skills
- `git-commit-korean`: Inspect this repository's git history and current diff, then draft or create git commits that match the local convention. Use when the user asks to write a commit message, make a git commit, summarize changes into a commit, keep commit messages in Korean, or split changes into small logical commits aligned with recent repository history. (file: `./skills/git-commit-korean/SKILL.md`)
- `readme-maintainer`: Update this repository's README so it matches the current implemented scope. Use when the user asks to create, rewrite, or refresh README content, document new features, update setup or deployment instructions, or keep the README in sync with current code, configuration, Docker deployment, Raspberry Pi behavior, or project limitations. (file: `./skills/readme-maintainer/SKILL.md`)
- `eco-knock-maintainer`: Maintain eco-knock-be-central code in the repository's established style. Use when modifying, reviewing, refactoring, testing, or organizing project code, especially around layered architecture, airquality CQRS boundaries, Spring Boot/Kotlin/Java style, controllers, services, repositories, Flyway migrations, and E2E tests. (file: `./skills/eco-knock-maintainer/SKILL.md`)
- `eco-knock-style-verifier`: Independently review production-code diffs for Eco Knock package structure, language boundaries, class placement, Java constructor injection, and consistency with nearby code. Use after any `src/main` change and before declaring the work complete. (file: `./skills/eco-knock-style-verifier/SKILL.md`)
- `api-doc-maintainer`: Keep this Spring Boot project's Scalar/OpenAPI documentation accurate when REST or SSE endpoints, controller annotations, request/response DTOs, status codes, ErrorCode entries, auth/security exposure, or API examples are added or changed. Use when adding a new endpoint, changing an existing endpoint contract, reviewing API docs, fixing Scalar or /v3/api-docs output, or updating OpenAPI annotations. (file: `./skills/api-doc-maintainer/SKILL.md`)

## How to use skills
- Discovery: The list above is the repository-local skill registry for this project.
- Trigger rules: If the user names a skill directly, or the task clearly matches a listed skill, read that `SKILL.md` and follow it for the current turn.
- Scope: Do not carry a skill across turns unless the user mentions it again or the next task still clearly matches it.
- Missing or blocked: If a listed skill file cannot be opened, say so briefly and continue with the best fallback.

## Local guidance
- Prefer repository-local skills in `./skills` before inventing ad-hoc workflow rules.
- When adding a new local skill under `./skills`, also add it to the `Available skills` list in this file so it can be auto-discovered in future sessions.
- Keep this file short. Put detailed task instructions in the skill's `SKILL.md`, not here.

## Mandatory production-code agent workflow

Apply this workflow to every task that changes `src/main`. Do not apply it to test-only, documentation-only, or build-only work.

The orchestrator (the main agent talking to the user) implements the change itself. Do not create a separate developer agent.

1. The orchestrator records pre-existing dirty paths, then uses `$eco-knock-maintainer` to implement the requirement and run the narrowest relevant tests.
2. The orchestrator does not declare completion yet. It prepares a handoff containing the requirement, orchestrator-owned production file list, exact diff, design decisions, and test commands/results.
3. The orchestrator automatically creates a fresh, read-only verifier sub-agent. The verifier uses `$eco-knock-style-verifier` and receives only the handoff and current worktree; do not pass the orchestrator's conclusion or prior reasoning.
4. The verifier reports maintainer checklist results and findings. If it reports a blocking finding, the orchestrator applies only the minimal refactoring those findings require and reruns the relevant tests.
   - To prevent over-engineering, the orchestrator may reject a finding that is excessive: it asks for changes beyond the requirement, adds abstraction or defensive code the requirement does not need, or is not backed by a concrete `$eco-knock-maintainer` rule or nearby code precedent.
   - A rejected finding no longer counts as blocking. The orchestrator must list each rejected finding with a one-line reason in its final report to the user.
5. The orchestrator automatically creates a new fresh verifier sub-agent after each refactor. Do not reuse the prior verifier's context.
6. Complete only when the final verifier reports zero blocking findings (excluding findings rejected under step 4) and the relevant tests pass. After two refactor cycles, stop and report the remaining risks instead of declaring completion.

The verifier never edits files. It reviews only orchestrator-owned production changes and must not request cleanup of paths that were dirty before the task began.
