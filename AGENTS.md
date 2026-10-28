# PROJECT KNOWLEDGE BASE

**Generated:** 2026-10-28
**Commit:** 5e954f6
**Branch:** master

## OVERVIEW

Micronaut microservice (part of the OpenDonationAssistant platform) managing stream-donation recipients: OAuth token storage/refresh for ~15 donation platforms, recipient settings, daily contribution awards, and webhooks. Single Maven module, **Java 25**, Micronaut platform `5.1.0`. Entry point: `io.github.opendonationassistant.Application`. Version `0.18.0`.

## STRUCTURE

```
oda-recipient-service/
├── src/main/java/io/github/opendonationassistant/
│   ├── Application.java        # main() + @Factory: Rabbit topology, Infinispan, RabbitClient facades
│   ├── token/                  # OAuth token subsystem (largest) → see token/AGENTS.md
│   │   └── repository/         # 15 platform Token+Repository pairs → see token/repository/AGENTS.md
│   ├── recipient/              # recipient settings + daily contributions → see recipient/AGENTS.md
│   ├── webhook/                # webhook management (CRUD + validation)
│   ├── otp/command/            # OTP exchange flow (unauthenticated — TODO)
│   ├── integration/            # one HTTP client per external platform (twitch, kick, ...)
│   ├── info/                   # service info endpoint
│   └── KeycloakConfiguration  # Keycloak admin RealmResource bean
├── src/main/resources/
│   ├── application*.yml        # base + allinone (dev) + standalone (prod) profiles
│   ├── logback.xml             # JSON structured logging
│   └── db/migration/V1..V15    # Flyway migrations, schema `recipient`
├── src/test/java/              # JUnit5 tests mirroring main packages
├── pom.xml                     # version 0.18.0, dynamic ${packaging} (jar default)
├── Dockerfile                  # eclipse-temurin:25
├── aot-jar.properties          # Micronaut AOT (disabled in pom)
└── openapi-config.json         # npm client codegen (@opendonationassistant/oda-recipient-service-client)
```

## WHERE TO LOOK

| Task | Location |
|------|----------|
| Add a new platform token | `token/repository/` (see its AGENTS.md checklist) |
| Add an OAuth link endpoint | `token/command/Link{Platform}.java` template |
| RabbitMQ topology / named clients | `Application.rabbitConfiguration()` |
| Add a cache | `*CacheConfiguration` `@Factory` (see `OtpCacheConfiguration`, `DonatersCacheConfiguration`) |
| DB schema change | `src/main/resources/db/migration/V{n}__*.sql` |
| Contribution/payment logic | `recipient/donater/` |
| Webhook management | `webhook/` (CRUD commands/view/repository/validation) |
| Auth helpers | `BaseController.getOwnerId/isAdmin` from `oda-commons` |

## CODE MAP

| Symbol | Type | Location | Role |
|--------|------|----------|------|
| `Application` | class/@Factory | root | entry point, Rabbit topology, Infinispan, `@Named("commands"/"events")` RabbitClient |
| `TokenRepository` | @Singleton | token/repository | facade over `TokenDataRepository`, dispatches to `TokenProvider` by `system()` |
| `TokenDataRepository` | @JdbcRepository | token/repository | Postgres CRUD for `token` table |
| `GenericTokenProvider` | abstract | token/repository | base for all 15 platform repositories |
| `TokenRequestHandler` | @RabbitListener | token/listener/handlers | RPC on `rpc` exchange queue `token` → `TokenResponse` |
| `ContributionListener` | @RabbitListener | recipient/donater | consumes `HistoryItemEvent`, upserts contributions |
| `SettingsController` | @Controller | recipient/view | recipient settings CRUD |
| `WebhookController` | @Controller | webhook/view | webhook CRUD endpoints |
| `OtpCommandController` | @Controller | otp/command | `/otp/create`, `/otp/exchange` (IS_ANONYMOUS) |
| `KeycloakConfiguration` | @Factory | root | Keycloak admin `RealmResource` bean |

## CONVENTIONS

- **Compile gate**: Error Prone + **NullAway in JSpecify mode = ERROR** on `io.github.opendonationassistant.*`. Null-unsafe code fails the build. Use `Optional`, `@Nullable`/`@NonNull`. `.mvn/jvm.config` `--add-exports` are required — never remove.
- **Layout**: CQRS-style feature packages — `command/` (HTTP command controllers), `view/` (read controllers), `repository/`, `listener/`, `events/`.
- **Controllers**: extend `BaseController` (oda-commons), `@Secured(IS_AUTHENTICATED)`, derive owner from JWT via `getOwnerId(auth)` — **never trust path `{recipientId}`**. Empty owner → `HttpResponse.unauthorized()`.
- **DTOs**: records + `@Serdeable` everywhere (HTTP, Rabbit, JSONB columns). Serde inclusion `ALWAYS`. google-java-format style, 2-space indent.
- **Persistence**: `@JdbcRepository(dialect = POSTGRES)`, schema `recipient`, soft-delete via `deleted` boolean, JSONB via `@MappedProperty(type = DataType.JSON)`. IDs from `Generators.timeBasedEpochGenerator()`.
- **Messaging**: commands → `@Named("commands")` RabbitClient (exchange `commands`); events → `@Named("events")` RabbitClient (exchange `recipient`, payloads implement `HasRecipientId`). Consumers → `@RabbitListener` + `@Queue`. Reusable payloads live in `oda-rabbit-conf`; local ones are nested `@Serdeable` records.
- **Caches**: `@Factory` producing a `Map<K,V>` bean with two `@Requires(env=...)` methods — `standalone` → Infinispan HotRod, `allinone` → `HashMap`. Non-String values use `SerdeableEntryMarshaller`.
- **Logging**: `ODALogger` (`new ODALogger(this)`), never slf4j directly.
- **Async**: commands return `CompletableFuture`, chained with `thenApply/thenCompose`, aggregated with `allOf`, blocked at RPC boundary with `.join()`.
- **Shared libs**: `oda-commons`, `oda-rabbit-conf`, `oda-test-utils` pinned by `${oda.version}` (0.11.232). Check those artifacts before writing new infra.

## ANTI-PATTERNS (THIS PROJECT)

- **Token ops must check ownership** — `DeleteToken`, `ToggleToken`, and `GetAccessToken` filter by `token.data().recipientId()` against `getOwnerId(auth)`; mismatches return 401. Keep this pattern for new token operations.
- **Unauthenticated endpoints** — `OtpCommandController` is `IS_ANONYMOUS` (TODO: add auth). Don't add new anonymous endpoints.
- **`Settings.setFeatureStatus()`** — filter-remove + add can create duplicate features; check existence before adding.
- **Mocking `Authentication` in tests without exercising real security rules** — flagged coverage gap.
- **Never commit**: `target/`, `.micronaut/`, `.settings/`, `.classpath`, `.factorypath`, `local.sh`, `private.key`, `public.pub`. (`.vscode/tasks.json` currently leaks a CLIENT_SECRET — do not add more secrets to tracked files.)

## COMMANDS

```bash
mvn compile                          # compile (NullAway gate)
mvn test                             # all tests (needs Docker/Podman for @MicronautTest)
mvn test -Dtest=FooTest              # single test
mvn mn:run                           # run locally (defaults to allinone env)
mvn clean package -Dpackaging=jar    # build jar
```

## NOTES

- `@MicronautTest(environments = "allinone")` boots Testcontainers (postgres:18.3, rabbitmq:4.3.0, keycloak:26.6.1) — requires Docker/Podman, ~30-40s per suite. Prefer pure unit tests.
- HTTP-client tests use MockServer on port 8080 (matches `allinone` URLs) + Instancio (`@Given`/`@WithSettings`).
- Flyway: `baseline-on-migrate=true`, validation disabled. New schema → new `V{n}__*.sql`.
- Release: push to `master` → shared `oda-libraries` workflow → GHCR image + git tag (tag = CI run number, not pom version). Bump `<version>` in pom as standalone commit.
- `TEST-REPORT.md` at root is a generated coverage-gap report (slightly stale — token tests have grown since).
