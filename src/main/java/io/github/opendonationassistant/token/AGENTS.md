# AGENTS.md — token

**Generated:** 2026-09-12
**Commit:** 209fe29

## OVERVIEW

OAuth token subsystem: storage/refresh for 15 donation platforms, link endpoints, token RPC, settings-change events. Largest subsystem.

## STRUCTURE

- `repository/` (39 files) — persistence + 15 platform pairs → see `repository/AGENTS.md` (new-platform checklist)
  - Core: `TokenData` (`@MappedEntity "token"`), `TokenDataRepository` (`@JdbcRepository`), `Token` interface, `TokenRepository` (`@Singleton` facade dispatching to `TokenProvider` by `system()`), `GenericToken` (default impl, emits `TokenSettingsChanged`), `GenericTokenProvider` (abstract base), `TokenProvider`, `OauthClient`, `RefreshToken` (`GenericToken` + OAuth refresh)
  - 15 pairs `{Platform}Token` + `{Platform}TokenRepository`: Twitch, Kick, Discord, Vklive, IHAQ, Streamlabs, StreamElements, StreamElementsJWT, GoodGame, DonationAlerts, UnofficialDonationAlerts, DonatePay, DonateX, Tribute, GoogleApiKey
- `command/` (14 files) — `SetToken`, `GetAccessToken`, `ToggleToken`, `DeleteToken`, `RefreshTokens` (admin-only), 9 `Link*` commands (Twitch, Kick, Discord, Vklive, IHAQ, StreamElements, Streamlabs, GoodGame, DonationAlerts)
- `listener/handlers/TokenRequestHandler` — `@RabbitListener` RPC on queue `recipient.token-request`; consumes `TokenRPC.TokenRequest` → `TokenResponse`
- `events/TokenSettingsChanged` — `@Serdeable` record implementing `HasRecipientId`; enum `Event {TOKEN_CREATED (unused), TOKEN_UPDATED, SETTINGS_CHANGED, TOKEN_TOGGLED, TOKEN_DELETED}`
- `view/TokenController` — read controller (`GET /recipients/tokens`)

## WHERE TO LOOK

| Task | Location |
|------|----------|
| Add a platform token | `repository/` — checklist in `repository/AGENTS.md` |
| Add a link endpoint | copy `command/LinkTwitch.java` template |
| Token RPC | `listener/handlers/TokenRequestHandler.java` |
| Token settings events | `events/TokenSettingsChanged.java` |

## CONVENTIONS (subsystem-specific)

- Platform tokens extend `RefreshToken` (OAuth) or `GenericToken` (static/access); each nests `@Serdeable record Settings(...)` implementing `JsonConvertable` with `asJsonMap()`
- Platform repos: `@Singleton` extending `GenericTokenProvider<{Platform}Token, {Platform}Token.Settings>`; override `system()` (DB `system` column), `getType()` (`"refreshToken"`/`"accessToken"`), `convert(TokenData)`
- `TokenRepository` auto-discovers providers via injected `List<TokenProvider>` — no manual registration when adding a platform
- Commands: `@Controller` + `BaseController`, `@Secured(IS_AUTHENTICATED)`, nested `@Serdeable record XxxCommand` body, owner guard via `getOwnerId(auth)` → unauthorized if empty
- `Link*` flow: `client.link(code)` → `client.getUser(accessToken)` → `repository.create(refreshToken, ownerId, new XxxToken.Settings(...))` → optional Rabbit commands on `@Named("commands")`
- Some tokens override `delete()` to send unlink/unsubscribe commands (Twitch, Kick, Vklive)

## ANTI-PATTERNS

- `DeleteToken` deletes by ID without recipient ownership filter (flagged in TEST-REPORT.md) — unlike `GetAccessToken`/`ToggleToken`/`SetToken`, which all filter by owner
- Adding a platform without the full checklist (client + Token + TokenRepository + Link command) — see `repository/AGENTS.md`