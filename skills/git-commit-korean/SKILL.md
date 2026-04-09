---
name: git-commit-korean
description: Inspect the repository state, recent git history, and both staged and unstaged diffs, then draft or create small Korean commits that match the local convention. Use when the user asks to write a commit message, make a git commit, summarize changes into commits, redo a mistaken commit, keep commit messages in Korean, or split a mixed diff into very small logical commits aligned with recent repository history.
---

# Git Commit Korean

Follow this workflow when preparing commits for this repository.

## Bootstrap Context

Before deciding commit boundaries:

- Read the repository root `AGENTS.md` if it exists.
- Read [references/commit-style.md](references/commit-style.md) before choosing final messages.
- Read [references/gitmoji-cheatsheet.md](references/gitmoji-cheatsheet.md) when you need a broader set of emoji/type combinations or the change does not fit the core examples.

Do not assume the current staged state is already correct.

## Inspect Full Repo State

Run these commands before writing a message unless the user already gave the exact diff scope:

- `git status --short`
- `git diff --staged`
- `git diff`
- `git log --oneline -n 10`

If the user asked to redo or re-split a recent commit, also inspect the most recent commit before acting.

- `git show --stat --summary HEAD`

Treat `git diff --staged` as the current staging state, not as proof that the grouping is correct.

## Decide Boundaries Before Staging

Build commit boundaries first, then stage to match them.

Default split axes:

- build, dependency, and environment setup
- shared DTO, exception, and response infrastructure
- entity, repository, converter, and persistence model changes
- auth utility and security domain model changes
- filter, middleware, handler, and wiring changes
- OAuth or third-party integration flow changes
- runtime behavior changes unrelated to the main feature
- tests
- cleanup, rename, or format-only changes

Strong rules:

- If one commit subject would naturally contain `and`, `및`, or `그리고`, split it.
- If one change can be reviewed independently from another, split it.
- If one commit introduces infrastructure and another commit starts consuming it, prefer two commits: infrastructure first, consumer second.
- If one file contains multiple concerns, use partial staging.
- Never pull unrelated pre-existing user changes into the same commit just because they are nearby in the tree.

## Split Commits Aggressively

Default to the smallest coherent commit units you can make.

Apply these rules before staging:

- Split unrelated changes into separate commits.
- Split large refactors from behavior changes whenever practical.
- Split tests from production changes when the test commit still passes or clearly belongs to one logical change.
- Split config, proto, wiring, domain logic, tests, docs, and cleanup into separate commits when they can stand on their own.
- If one file contains multiple unrelated edits, stage partial hunks to keep each commit narrow.
- If you are unsure whether to split, split.

Avoid broad “do everything” commits. If the current diff mixes multiple concerns, propose or create multiple Korean commits instead of one broad commit.

## Commit Style

Apply these rules:

- Write the subject in Korean.
- Use a single-line subject.
- Match the repository pattern: optional emoji prefix, then `type: `, then a short Korean summary.
- Prefer the observed types from history first, but use a more specific type when the change is clearly build, deploy, or rename oriented.
- Keep the subject concrete and scoped to the actual change. Avoid vague summaries like `수정` or `변경`.
- Describe the user-visible or reviewer-visible unit of change, not the low-level edit sequence.

Choose the most specific matching pattern when possible:

- dependency added: `:heavy_plus_sign: build:`
- dependency upgraded: `:arrow_up: build:`
- dependency downgraded: `:arrow_down: build:`
- build script or toolchain change without dependency movement: `:hammer: build:`
- deploy pipeline, release config, or runtime deployment wiring: `:rocket: deploy:`
- directory move, file move, package move, class rename, or file rename: `:truck: rename:`
- bug fix: `:bug: fix:`
- urgent production-only fix: `:ambulance: hotfix:`
- docs or usage guide change: `:memo: docs:`
- CI workflow change: `:construction_worker: ci:`
- CI repair to recover a broken pipeline: `:green_heart: ci:`
- security hardening: `:lock: security:`
- performance improvement: `:zap: perf:`
- DB schema or migration change: `:card_file_box: db:`
- chore or local tooling cleanup with no product effect: `:wrench: chore:`
- new user-facing or system-facing behavior: `:sparkles: feat:`
- behavior-preserving structure cleanup: `:recycle: refactor:`
- test-only change: `:white_check_mark: test:`
- removal only: `:fire: remove:`

Good pattern examples:

- `:sparkles: feat: 센서 스트리밍 서비스 분리`
- `:recycle: refactor: 서버 부팅 로직을 sensor.go로 이동`
- `:white_check_mark: test: 센서 스트리밍 테스트에 Close 구현 추가`
- `:heavy_plus_sign: build: OAuth2 클라이언트 의존성을 추가`
- `:arrow_up: build: Spring Boot 버전을 4.0.6으로 올림`
- `:arrow_down: build: protobuf 플러그인 버전을 0.9.4로 내림`
- `:rocket: deploy: 운영 배포 환경 변수를 분리`
- `:truck: rename: oauth 패키지를 oauth2로 이동`
- `:bug: fix: 만료된 JWT를 401로 처리`
- `:lock: security: 민감 정보 로그 출력을 제거`
- `:construction_worker: ci: GitHub Actions 배포 워크플로를 추가`

## Commit Execution

When the user asked to actually commit:

1. Inspect the full repo state.
2. Decide the smallest sensible commit boundaries before staging anything.
3. If files are already staged but mixed, unstage and rebuild the staging set.
4. Stage only the intended files or hunks for the current logical unit.
5. Create the commit with the final Korean message.
6. Re-run `git status --short` and `git diff --staged` after each commit.
7. Repeat until no intended changes remain.
8. Report the resulting commit hash, subject, and scope for each commit.

Execution guardrails:

- If nothing is staged and the user asked to commit, stage the first logical unit yourself.
- If the user explicitly asks to rewrite a mistaken commit, prefer `git reset --soft` for regrouping and then re-stage by logical unit.
- Do not amend, rebase, squash, or rewrite history unless the user explicitly asked.
- Do not use a single catch-all commit just to make the worktree clean.

## Output Expectations

If the user asked only for a message draft, return 1 to 3 candidate Korean commit messages and explain the recommended one briefly. If the diff should be split, provide message candidates per logical commit.

If the user asked to commit, perform the commit and then report:

- committed files or scope
- final commit subject
- short commit hash
