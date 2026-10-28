# AGENTS.md — token/repository

**Generated:** 2026-10-28
**Commit:** 5e954f6

## OVERVIEW

Token persistence + platform-specific token logic: 39 files = 9 core infra + 15 platform Token/TokenRepository pairs.

## STRUCTURE

Core infra (9 files):
- `TokenData` — `@MappedEntity("token")` record: id/token/type/recipientId/system/enabled/deleted/settings(JSONB)/scopes(JSONB); withers `withEnabled/withDeleted/withToken/withSettings/withScopes`
- `TokenDataRepository` — `@JdbcRepository(POSTGRES)`; finders `findByRecipientIdAndDeletedFalse`, `findByRecipientIdAndSystemAndTypeAndDeletedFalse`, `findBySystemAndDeletedFalse`
- `Token` — interface: data/save/update/toggle/delete
- `TokenRepository` — `@Singleton` facade; dispatches create/convert to `TokenProvider` by `system()`; throws `IllegalArgumentException("Unknown system: ...")`
- `GenericToken` — default impl; merges `defaultSettings()` with stored settings; upsert save; emits `TokenSettingsChanged`
- `GenericTokenProvider` — abstract base; `create` via `Generators.timeBasedEpochGenerator()`; abstract `getType()`
- `TokenProvider` — interface: `create(token, recipientId, S settings)`, `create(id, token, recipientId, Map settings)`, `system()`, `convert(TokenData)`
- `OauthClient` — `obtainAccessToken(refreshToken) → CompletableFuture<RefreshedTokens>`
- `RefreshToken` — `GenericToken` subclass holding `OauthClient`; `obtainAccessToken()` replaces stored token

Platform pairs (30 files): Twitch, Kick, Discord, Vklive, IHAQ, Streamlabs, StreamElements, StreamElementsJWT, GoodGame, DonationAlerts, UnofficialDonationAlerts, DonatePay, DonateX, Tribute, GoogleApiKey.

## WHERE TO LOOK

| Task | Location |
|------|----------|
| Add a new platform token | checklist below |
| Token table schema | `src/main/resources/db/migration/` (V3–V11 touch `token`) |

### Checklist: add a platform token
1. `integration/{platform}/XxxClient.java` implementing `OauthClient` (link, getUser, obtainAccessToken)
2. `{Platform}Token.java` extending `RefreshToken` (OAuth) or `GenericToken` (static), nested `Settings` record
3. `{Platform}TokenRepository.java` `@Singleton` extending `GenericTokenProvider<{Platform}Token, {Platform}Token.Settings>`
4. No manual registration — `TokenRepository` auto-discovers via `List<TokenProvider>`

## CONVENTIONS (package-specific)

- Platform `Token` extends `RefreshToken` (OAuth) or `GenericToken` (static); nested `@Serdeable record Settings(...)` implements `JsonConvertable` with `asJsonMap()` hand-mapping camelCase keys
- Platform `Repository`: `@Singleton`, extends `GenericTokenProvider<{Platform}Token, {Platform}Token.Settings>`, constructor-injects `TokenDataRepository`, `@Named("events")` RabbitClient, platform client, optionally `@Named("commands")` RabbitClient; overrides `system()` (display name = DB `system` column), `getType()` (`"refreshToken"`/`"accessToken"`), `convert(TokenData)`
- `DonatePayTokenRepository` overrides `create(id, ...)` to enrich settings via `client.getUser(token)`
- Some tokens override `delete()` to send unlink/unsubscribe commands (Twitch, Kick, Vklive) and/or `defaultSettings()` (DonationAlerts, DonatePay, Tribute, IHAQ, GoogleApiKey)
- All writes return `CompletableFuture`; `save()` emits `TOKEN_UPDATED` even on insert (`TOKEN_CREATED` enum value unused)

## ANTI-PATTERNS

- Bypassing `GenericTokenProvider` — every platform repo must extend it
- New `system()` value without a matching provider → `create()` throws `IllegalArgumentException`
- Storing secrets in settings JSONB in plaintext without checking existing patterns