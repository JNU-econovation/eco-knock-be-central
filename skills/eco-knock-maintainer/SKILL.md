---
name: eco-knock-maintainer
description: Maintain eco-knock-be-central code in the repository's established style. Use when modifying, reviewing, refactoring, testing, or organizing project code, especially Spring Boot Kotlin/Java services, controllers, repositories, entities, DTOs, gRPC clients, Flyway migrations, and E2E tests.
---

# Eco Knock Maintainer

Use this skill when working on application code in `eco-knock-be-central`.

## Hard Rules

- Read nearby code and package structure before changing or adding files.
- If the implementation language is unclear, ask the user whether to use Java or Kotlin before creating files. Some maintainers may know Java but not Kotlin.
- If the user specifies Java or Kotlin, follow that choice. If nearby files clearly establish a language, match it.
- Do not put files directly under a domain root package such as `{domain}/SomeClass.kt`. Use role packages.
- Do not copy `airquality`'s CQRS/usecase/readmodel shape into other domains unless the user explicitly asks.
- Keep JPA entities in Java under `{domain}/model/entity`.
- Keep enums/value objects stored by entities in Java under `{domain}/model/vo`.
- Do not create `policy`, `evaluator`, `manager`, `helper`, or similar indirection unless reuse or complexity clearly justifies it.
- Do not extract a private helper used once when it only wraps a simple lookup, exception, or value conversion. Keep it in the calling flow. Extract only for real reuse, an independently complex domain decision, or a clear transaction boundary.
- Keep simple domain decisions inside the owning service as private methods.
- A service may directly use only repositories owned by its own domain. For another domain's entity or persistence operation, call that domain's service; do not inject or call its repository directly.
- Do not bypass an expected service-to-service circular dependency by directly injecting another domain's repository. State the two services and required operation, then ask the developer whether to move the responsibility or introduce an explicit orchestration boundary.
- Do not make production methods `public`, `internal`, companion/static, or otherwise wider only to test private implementation.
- Do not use `runBlocking` in Spring controllers, services, or schedulers.
- With blocking gRPC stubs, expose normal functions for normal Spring code. Use `suspend` only for APIs intended to be called from coroutine loops.
- Preserve user or unrelated worktree changes. Never revert unrelated dirty files.
- When adding an HTTP endpoint or changing an endpoint's authorization policy, ask the user whether `GUEST` should be added to the explicit guest allowlist. Do not add it unless the user explicitly approves.

## Package Style

General domains use simple layered packages:

- `controller`: HTTP or SSR entry points.
- `service`: business flow, validation orchestration, and transaction boundaries.
- `repository`: persistence access.
- `dto`, `dto/request`, `dto/response`: transfer types.
- `model/entity`: Java JPA entities.
- `model/vo`: Java value objects/enums embedded in or stored by entities.
- `exception`: domain client exceptions.
- Use `config`, `client`, `resolver`, `messaging`, `queue`, `command`, or `event` only when the domain already needs that role.

Special cases:

- `airquality` already has CQRS-like boundaries. Keep its command/query/usecase/readmodel split.
- `common` contains shared infrastructure only: security, exceptions, response wrappers, converters, utilities, OpenAPI support.
- External integrations such as `sso`, `whozin`, and `grpc` keep integration details under `client`/`config`/`dto`/`service`.

## Language Rules

- Use Java for JPA entities, entity value objects, entity enums, security filters/helpers, low-level security DTOs/constants, and existing Java infrastructure.
- Use Kotlin for services, controllers, repository interfaces, configuration properties, request/response DTOs, external clients, messaging loops, and simple application DTOs when nearby code is Kotlin.
- For mixed Java/Kotlin features, ask the user where they want the boundary if nearby code does not make it obvious.
- Keep Kotlin constructor-injected and concise. Put `companion object` near the top, but limit it to logger, constants, and real factories.

## Implementation Style

- Keep controllers thin: bind input, delegate to service, return `CommonResponse` or a view.
- Keep repositories behind services. Controllers should not call repositories directly, and services may use only their own-domain repositories.
- Put `@Transactional` on services, not controllers.
- Prefer direct, explicit branches over clever maps or generic helpers.
- Prefer a direct lookup after an already-resolved domain object over a one-use wrapper method:

```kotlin
// Bad: called once and only hides a lookup plus exception.
private fun getOverviewLayoutOrThrow(memberId: Long): OverviewLayout {
    return overviewLayoutRepository.findByMemberId(memberId)
        ?: throw InternalServerException(...)
}
```

```kotlin
// Good: keep the simple operation in the calling flow.
val layout = overviewLayoutRepository.findByMemberId(member.id)
    ?: throw InternalServerException(
        IllegalStateException("id가 ${member.id}인 overview layout을 찾을 수 없음.")
    )
```

- Use the owning service for cross-domain access:

```kotlin
// Bad: OverviewService directly depends on the member domain repository.
class OverviewService(
    private val memberRepository: MemberRepository,
    private val overviewLayoutRepository: OverviewLayoutRepository,
)
```

```kotlin
// Good: OverviewService accesses Member through MemberService.
val member = memberService.getEntityOrThrow(memberInfo.id)
val layout = overviewLayoutRepository.findByMemberId(member.id)
    ?: throw InternalServerException(
        IllegalStateException("id가 ${member.id}인 overview layout을 찾을 수 없음.")
    )
```

- Do not borrow another class's companion/static constant only because its value happens to match. When a value is shared across classes in a domain, put it in that domain's constant object at `{domain}/constant/{Domain}Constant.kt` (a Kotlin `object` with `const val`, like `AuthConstant` and `SSOConstant`). Reuse the existing constant object if one exists; create it only when it does not. Do not keep the shared value in a service/controller companion object. "Nearby code already does this" is not enough when the precedent sits in a different context:

```kotlin
// Bad: the admin home controller depends on the login URL service only to read "/admin".
@GetMapping("/")
fun root(): String {
    return "redirect:${AdminLoginUrlService.ADMIN_HOME_PATH}"
}
```

```kotlin
// Good: the shared path lives in the admin domain's constant object.
object AdminConstant {
    const val ADMIN_HOME_PATH = "/admin"
}

@GetMapping("/")
fun root(): String {
    return "redirect:$ADMIN_HOME_PATH"
}
```

- Avoid silent correction of invalid input. Throw the owning domain exception.
- Keep request-specific validation in request DTOs.
- Prefer `TargetType.from(source)` factories for DTO conversion.
- Avoid `request.toOtherRequest()` and broad converter classes for one-off mapping.
- For `@ConfigurationProperties`, use public constructor `val` properties and no default values for required operational settings.
- For redirect, auth, token, or cookie behavior, keep logic in service/resolver/helper classes rather than controllers.
- When adding a scheduled or background flow, make repeated execution idempotent and avoid overlapping or noisy side effects.

## Persistence And Migrations

- Hibernate uses `ddl-auto=validate`; every new table/column/entity needs a Flyway migration.
- Add new migrations as the next `V{n}__short_description.sql`. Do not edit applied migrations.
- Match existing physical naming: snake_case table/column names such as `member_id`, `sort_order`, `measured_at`.
- Add indexes for query patterns introduced by the feature.
- Prefer explicit foreign keys when a table owns a real relationship.

## Testing

- Run the narrowest relevant Gradle task first.
- Do not change production visibility just to unit-test private logic.
- Prefer testing public behavior, repository queries, service outcomes, controller responses, or Flyway/JPA validation.
- E2E tests should follow the existing style: `@SpringBootTest(webEnvironment = RANDOM_PORT)`, constructor injection, `RestClient`, and bounded test data cleanup.
- Do not mock E2E collaborators unless the user explicitly asks.
- Do not add test-only default values for required secrets such as `ADMIN_MASTER_PASSWORD`, `JWT_SECRET_KEY`, `AES256_KEY`, `SSO_CLIENT_ID`, or `WHOZIN_TOKEN`.

## Domain Notes

- `overview`: default shortcuts and user shortcuts are separate domains. Use default rows only as reset/init source.
- `auth`: store refresh token `jti` values, not raw refresh tokens. Keep refresh token replacement atomic.
- `sso`: auth-econovation role is not this service's admin role. Validate redirect URLs against the allowlist.
- `whozin`: treat the real API response as source of truth. Preserve snake_case response mapping.
- `light`: persistence is simple save/query around `LightReport`; avoid adding architecture ceremony.
- `grpc`: clients wrap generated stubs and map responses into project DTOs close to the client boundary.
