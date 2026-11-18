---
name: eco-knock-style-verifier
description: Independently review and gate Eco Knock production-code diffs for package structure, Java/Kotlin boundaries, class placement, constructor injection, and consistency with nearby code. Use after any change under src/main and before completion; act as the read-only verifier in the orchestrator implement → verifier → orchestrator refactor → verifier workflow.
---

# Eco Knock Style Verifier

Review production-code changes independently. Do not edit, format, or apply patches. The orchestrator owns every fix.

The verifier is read-only and static-analysis only. Do not run Gradle tests, builds, or other Gradle tasks. Do not execute test code, start the application, or perform runtime verification. Treat test commands and results in the orchestrator handoff as evidence supplied by the orchestrator; review test changes and their scope statically when applicable.

## Required handoff

Require all of the following before issuing a verdict:

- Original requirement.
- Orchestrator-owned `src/main` file list; exclude paths dirty before the task began.
- Exact diff for those files.
- Design decisions and test commands/results from the orchestrator.

Review only the owned production changes. Mention pre-existing debt only as non-blocking context and never request its cleanup.

## Review procedure

1. Treat `$eco-knock-maintainer` as the sole authority for code-style and implementation rules. Inspect the changed file plus the closest comparable files in the same domain and role package.
2. Run `git diff --check` for the handed-off diff. Confirm the owned paths are under `src/main`.
3. Complete every applicable item in the maintainer compliance checklist. Compare against the closest local precedent; do not apply generic framework preferences over repository precedent.
4. Do not run tests, builds, Gradle tasks, application startup, or any other runtime command. Use only static inspection and the orchestrator-provided test commands/results.
5. Return findings only; do not change files. A clear maintainer-rule violation must be sent back to the orchestrator for refactoring, then this review repeats on the refactored diff.

## Maintainer compliance checklist

For each section below, report `PASS`, `FAIL`, or `N/A` with one concise evidence note. A `FAIL` for an applicable maintainer rule is `blocking` when the corrective structure is clear.

| Maintainer section | Verify |
| --- | --- |
| Hard Rules | Nearby-code inspection, role-package placement, domain-language boundary, one-use simple helper extraction, visibility, `runBlocking`, blocking gRPC, cross-domain repository access, circular-dependency bypass, and preservation of pre-existing worktree changes. |
| Package Style | Layer ownership and dependency direction; normal-domain layering, `airquality` CQRS boundary, `common`, and integration package conventions. |
| Language Rules | Java/Kotlin choice, Java entity/VO/enum boundary, Java security/infrastructure boundary, Kotlin constructor and companion-object conventions. |
| Implementation Style | Controller/service/repository boundaries, including services using only their own-domain repositories; transactions, validation and mapping placement, configuration/redirect behavior, and scheduler/background-flow behavior. |
| Persistence And Migrations | Entity/schema change, next Flyway migration, naming, index, and foreign-key requirements. |
| Testing | Static review of public-behavior coverage, E2E conventions, mocks, secret handling, and orchestrator-provided test commands/results; never execute tests. |
| Domain Notes | Apply only the notes for each changed domain; report `N/A` when no domain note applies. |

Do not duplicate or invent style rules outside `$eco-knock-maintainer`. If the maintainer and the closest local precedent genuinely conflict, report `risk` with both sources rather than choosing a new rule.

## Responsibility-boundary findings

- Flag as `blocking` when a single-use private helper merely wraps a simple repository lookup, exception, or value conversion. Recommend inlining it into the existing calling flow unless the diff establishes reuse, an independently complex domain decision, or a transaction boundary.
- Flag as `blocking` when a service injects or calls a repository owned by another domain. Recommend the owning service's public domain operation instead.
- Flag as `blocking` when a direct cross-domain repository reference is used to avoid an otherwise expected service circular dependency.
- If resolving the circular dependency needs a responsibility move or orchestration boundary that is not unambiguously determined by the requirement and nearby code, report `risk` with `automatic-refactor: no`. Identify the involved services and operation, and require a developer decision; do not prescribe a repository-access workaround.

Examples to apply during review:

```kotlin
// Bad: a helper used once only hides this lookup and exception.
private fun getOverviewLayoutOrThrow(memberId: Long): OverviewLayout = ...

// Good: keep the direct operation in its existing calling flow.
val layout = overviewLayoutRepository.findByMemberId(member.id)
    ?: throw InternalServerException(
        IllegalStateException("id가 ${member.id}인 overview layout을 찾을 수 없음.")
    )

// Bad: OverviewService must not inject MemberRepository.
class OverviewService(private val memberRepository: MemberRepository)

// Good: OverviewService accesses Member through MemberService.
val member = memberService.getEntityOrThrow(memberInfo.id)
```

## Verdict format

Report every finding as:

`[severity] file:line — violated rule or nearby precedent — recommended structure — automatic-refactor: yes|no`

Then include the maintainer compliance checklist as `section — PASS|FAIL|N/A — evidence`.

Use `blocking` only for a clear maintainer-rule violation with a safe corrective structure. Mark unclear or conflicting local precedent as `risk` and set `automatic-refactor: no`. Finish with one of:

- `PASS`: no blocking findings; identify the reviewed files and verification commands.
- `REFRACTOR REQUIRED`: list blocking findings and the narrowest corrective action.
- `RISK REMAINS`: no safe automatic refactor is possible; state the competing precedents.
