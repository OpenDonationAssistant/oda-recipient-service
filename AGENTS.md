# AGENTS.md

Micronaut service (part of the OpenDonationAssistant platform). Single Maven module, **Java 25**, Micronaut platform `5.1.0` (injection `4.5.0`). Entry point: `io.github.opendonationassistant.Application`.

## Build & run

- No Maven wrapper is committed; use system `mvn` (requires JDK 25).
- Compile: `mvn compile` · Test all: `mvn test` · Single test: `mvn test -Dtest=FooTest`
- Run locally: `mvn mn:run`. The app defaults to the `allinone` environment (set in `Application.Configurer`), so it starts with in-memory caches and all HTTP clients pointed at `localhost:8080`. Running a real instance needs `JDBC_URL`, `RABBITMQ_HOST`, `INFINISPAN_HOST/PORT/USER/PASSWORD`, `JWKS_URI` (see `.vscode/tasks.json` for the dev recipe: `MICRONAUT_SERVER_PORT=8084 CLIENT_ID=... CLIENT_SECRET=... mvn mn:run`).

## Compile gate — NullAway is an ERROR (do not miss)

`maven-compiler-plugin` runs **Error Prone + NullAway in JSpecify mode**, failing the build on null-unsafety for `io.github.opendonationassistant.*`. Write null-safe code and use `@Nullable`/`@NonNull` (JSpecify or `io.micronaut.core.annotation`). Generated sources under `target/generated-sources` are excluded. Required JVM `--add-exports` for the compiler live in `.mvn/jvm.config` — do not remove them.

Annotation processors active at compile: `micronaut-inject`, `micronaut-security-annotations`, `micronaut-serde-processor`, `micronaut-data-processor`, `micronaut-openapi`. The OpenAPI spec is generated to `target/classes/META-INF/swagger/service-*.yml` during compilation (the npm client from `openapi-config.json` is produced by the shared CI, not here).

## Tests

- `@MicronautTest(environments = "allinone")` tests boot **Micronaut Test Resources + Testcontainers**, so they **require a running Docker/Podman** and pull `postgres:18.3`, `rabbitmq:4.3.0`, and `keycloak:26.6.1`. Each such test is slow (~30–40s) because containers spin up per suite. Expect the whole suite to take several minutes.
- Pure unit tests (no `@MicronautTest`, e.g. `SettingsTest`) run instantly without infra — prefer them when possible.
- HTTP-client tests use MockServer on port `8080` (matches `allinone` URLs) + Instancio (`@Given`/`@WithSettings`) for fixtures.
- Flyway migrations run against the test Postgres; new schema changes need a new `V{n}__*.sql` in `src/main/resources/db/migration/` (current files go up to `V10`). `baseline-on-migrate=true`, validation disabled.

## Architecture / conventions

- **Persistence**: PostgreSQL via `micronaut-data-jdbc` (`@JdbcRepository(dialect = Dialect.POSTGRES)`, schema `recipient`). Infinispan Hot Rod for some caches — real in `standalone` env, swapped for in-memory `HashMap` in `allinone` (see `DonatersCacheConfiguration`, `OtpCacheConfiguration`, `Application.remoteCacheManager`).
- **Messaging**: RabbitMQ (`micronaut-rabbitmq`) for events / commands / RPC; queues and exchanges are declared in `Application.rabbitConfiguration()`. The rabbit consumer executor is a single fixed thread (`micronaut.executors.rabbit`).
- **Auth**: Keycloak admin client + JWT (JWKS from `JWKS_URI`). Controllers use `@Secured`; `BaseController` (from `oda-commons`) exposes `getOwnerId(Authentication)`.
- **Layout** (CQRS-style, under `io.github.opendonationassistant`): `token/` (commands, repository, view, listener, events), `recipient/` (commands, donater, repository, view), `otp/command`, `info`, and `integration/*` = one HTTP client per external platform (twitch, kick, vklive, goodgame, discord, streamelements, streamlabs, donatepay, donationalerts, ihaq).
- Records + `@Serdeable` for DTOs; serde inclusion is `ALWAYS`. Code is formatted in google-java-format style (2-space indent).

## Shared ODA libraries

`oda-commons`, `oda-rabbit-conf`, `oda-test-utils` (and the `BaseController`, `RabbitClient`, `ODALogger` used throughout) come from `io.github.opendonationassistant`, pinned by the `${oda.version}` property (currently `0.11.232`). If a needed API isn't in this repo, check those artifacts first.

## Release

Push to `master` triggers a shared CI workflow (`OpenDonationAssistant/oda-libraries` `release_service.yml`) that builds and publishes to GHCR. Bump the `<version>` in `pom.xml` (currently `0.13.0`) for a release — version bumps are standalone commits.
