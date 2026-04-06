---
name: git-commit-korean
description: Inspect this repository's git history and current diff, then draft or create git commits that match the local convention. Use when the user asks to write a commit message, make a git commit, summarize changes into a commit, keep commit messages in Korean, or split changes into very small logical commits aligned with recent repository history.
---

# Git Commit Korean

Follow this workflow when preparing a commit for this repository.

## Inspect First

Run these commands before writing a message unless the user already gave the exact diff scope:

- `git status --short`
- `git diff --staged`
- `git diff`
- `git log --oneline -n 10`

Use `git diff --staged` as the source of truth when files are already staged. If nothing is staged, confirm whether to stage files or only draft the message.

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

Read [references/commit-style.md](references/commit-style.md) before choosing the final message.

Apply these rules:

- Write the subject in Korean.
- Use a single-line subject.
- Match the repository pattern: optional emoji prefix, then `type: `, then a short Korean summary.
- Prefer the observed types from history: `feat`, `refactor`, `test`, `remove`.
- Keep the subject concrete and scoped to the actual change. Avoid vague summaries like `수정` or `변경`.

Good pattern examples:

- `:sparkles: feat: 센서 스트리밍 서비스 분리`
- `:recycle: refactor: 서버 부팅 로직을 sensor.go로 이동`
- `:white_check_mark: test: 센서 스트리밍 테스트에 Close 구현 추가`

## Commit Execution

When the user asked to actually commit:

1. Check which files should be included.
2. Decide the smallest sensible commit boundaries first.
3. Stage only the intended files or hunks for the current logical unit.
4. Create the commit with the final Korean message.
5. Repeat for every remaining logical unit until no mixed changes remain.
6. Report the resulting commit hash and subject for each commit.

Do not amend, rebase, squash, or rewrite history unless the user explicitly asked.

## Output Expectations

If the user asked only for a message draft, return 1 to 3 candidate Korean commit messages and explain the recommended one briefly. If the diff should be split, provide message candidates per logical commit.

If the user asked to commit, perform the commit and then report:

- committed files or scope
- final commit subject
- short commit hash
