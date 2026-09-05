# Missing Test Coverage Report

Generated: 2026-07-28

A comprehensive analysis of missing test cases and scenarios across all modules of `oda-recipient-service`.

---

## Overall Coverage Summary

| Package | Source Files | Test Files | Coverage Status |
|---|---|---|---|
| `token/command/` | 13 (Set, Delete, GetAccessToken, Toggle, 9× Link*) | 4 | **Partial** — 9 Link commands have **zero** tests |
| `token/repository/` | 18+ (Token, TokenData, GenericToken, RefreshToken, 12× platform tokens/providers) | 2 | **Minimal** — only VKLive create + basic CRUD tested |
| `token/listener/` | 1 (TokenRequestHandler) | 1 | **Moderate** — happy path + not-found tested |
| `token/view/` | 1 (TokenController) | 0 | **None** — used implicitly by SetTokenTest only |
| `recipient/repository/` | 4 (Settings, SettingsData, Repo, Event) | 1 | **Minimal** — only feature-add scenario |
| `recipient/commands/` | 4 (CreateRecipient, ToggleFeature, UpdateEmail, ChangePassword) | 0 | **None** |
| `recipient/view/` | 1 (SettingsController) | 0 | **None** |
| `recipient/donater/` | 5 (Contribution, Listener, Sender, Controller, Cache) | 0 | **None** |
| `otp/command/` | 4 (Controller, Create, Exchange, Config) | 0 | **None** |
| `info/` | 1 (InfoController) | 0 | **None** |
| `integration/*/` | 7+ clients (Twitch, Kick, VKLive, GG, DA, Streamlabs, StreamElements, Discord) | 0 | **None** |

---

## 1. Token Commands

### 1a. `SetToken` — partial coverage

**Tested:** Create new token ✅, Update existing token ✅

**Missing:**
- [ ] Token with missing/empty authentication → unauthorized
- [ ] Existing token owned by different user → unauthorized
- [ ] Token with empty/null fields in command
- [ ] Token creation for every supported system (only DonateX tested)
- [ ] Concurrent create + update race

### 1b. `DeleteToken` — partial coverage

**Tested:** Generic delete ✅, Kick delete with RabbitMQ command ✅

**Missing:**
- [ ] Token not found → 404
- [ ] Token owned by different user (🚨 **no ownership check exists in code**)
- [ ] Already deleted token
- [ ] **TwitchToken** delete → sends `UnsubscribeAllTwitchEventsCommand`
- [ ] **VkliveToken** delete → sends `UnlinkVkAccount`
- [ ] All other platform token deletes (GoodGame, DA, DP, DX, SE, SL, Discord, Tribute)

### 1c. `GetAccessToken` — moderate coverage

**Tested:** Generic token ✅, Kick refresh ✅, Unauthorized scenarios ✅

**Missing:**
- [ ] Disabled token (enabled=false)
- [ ] Deleted token (deleted=true)
- [ ] Refresh token for **Twitch, VKLive, GoodGame, Streamlabs, StreamElements, Discord**
- [ ] Exception during refresh token failure

### 1d. `ToggleToken` — minimal coverage

**Tested:** Toggle enabled→disabled ✅

**Missing:**
- [ ] Toggle disabled→enabled
- [ ] Auth missing → unauthorized
- [ ] Token not found (currently returns 200 anyway)
- [ ] Token owned by different user (🚨 **no ownership check exists in code**)
- [ ] Already deleted token
- [ ] Multiple sequential toggles

### 1e. Link Controllers — **ZERO TESTS** across all 9 platforms

| Controller | Auth fail | API error | Empty user | Happy path | Rabbit commands sent |
|---|---|---|---|---|---|
| `LinkTwitch` | ❌ | ❌ | ❌ | ❌ | ❌ |
| `LinkVklive` | ❌ | ❌ | ❌ | ❌ | ❌ |
| `LinkKick` | ❌ | ❌ | ❌ | ❌ | ❌ |
| `LinkGoodGame` | ❌ | ❌ | ❌ | ❌ | ❌ |
| `LinkDonationAlerts` | ❌ | ❌ | ❌ | ❌ | ❌ |
| `LinkStreamlabs` | ❌ | ❌ | ❌ | ❌ | ❌ |
| `LinkStreamElements` | ❌ | ❌ | ❌ | ❌ | ❌ |
| `LinkDiscord` | ❌ | ❌ | ❌ | ❌ | ❌ |
| `LinkDonatePay` (implied) | ❌ | ❌ | ❌ | ❌ | ❌ |
| `LinkTribute` (implied) | ❌ | ❌ | ❌ | ❌ | ❌ |

---

## 2. Token Repository

### 2a. `TokenRepository` — minimal coverage

**Tested:** VKLive create ✅

**Missing:**
- [ ] `findById()` — existing token
- [ ] `findById()` — non-existing token → empty
- [ ] `findByRecipientId()` — tokens found
- [ ] `findByRecipientId()` — empty result
- [ ] `findByRecipientId()` — filters deleted tokens
- [ ] `findByRecipientIdAndSystemAndType()` — filters correctly
- [ ] `create()` with **unknown system** → IllegalArgumentException
- [ ] `create()` with **custom ID**
- [ ] `create()` with **settings map**
- [ ] **Token conversion** fallback to GenericToken for unknown systems

### 2b. `GenericToken` — **zero direct tests**

- [ ] `save()` — insert vs update path
- [ ] `update(String token)`
- [ ] `update(Map<String, Object> settings)` — merge logic
- [ ] `toggle()`
- [ ] `delete()` → sets deleted=true
- [ ] Event `TokenSettingsChanged` sent on every save
- [ ] **Settings merging** on construction (defaultSettings + data.settings)
- [ ] `defaultSettings()` returns empty map

### 2c. `RefreshToken` — **no direct unit test**

- [ ] `obtainAccessToken()` → calls oauth, updates token, returns refreshed
- [ ] OAuth client error handling / propagation
- [ ] Token update after successful refresh

### 2d. Platform Token Deletion Logic

- [ ] `TwitchToken.delete()` → sends `UnsubscribeAllTwitchEventsCommand` + super
- [ ] `VkliveToken.delete()` → sends `UnlinkVkAccount` + super
- [ ] `GoodGameToken` — zero tests at all
- [ ] `DonationAlertsToken` — zero tests at all
- [ ] `DonatePayToken` — zero tests at all
- [ ] `DonateXToken` — zero tests at all
- [ ] `StreamElementsToken` — zero tests at all
- [ ] `StreamlabsToken` — zero tests at all
- [ ] `DiscordToken` — zero tests at all
- [ ] `TributeToken` — zero tests at all

### 2e. `GenericTokenProvider` — **zero tests**

- [ ] `create(token, recipientId, settings)` with type→system mapping
- [ ] `create(id, token, recipientId, settings)`
- [ ] `findById()`

### 2f. `TokenData` — `with*` methods

- [ ] `withEnabled(boolean)`
- [ ] `withDeleted(boolean)`
- [ ] `withToken(String)`
- [ ] `withSettings(Map)`

---

## 3. Recipient / Settings

### 3a. `Settings` — minimal coverage

**Tested:** `setFeatureStatus` add ✅

**Missing:**
- [ ] **Update existing feature** status (🚨 code may produce duplicate features instead)
- [ ] `setFeatureStatus` with **DISABLED** status
- [ ] Multiple features
- [ ] `save()` → calls `repository.save()`
- [ ] **Event sending** via RabbitClient on feature change
- [ ] RabbitMQ send failure → RuntimeException

### 3b. `SettingsRepository` — **zero tests**

- [ ] `get()` with existing settings
- [ ] `get()` with no settings → creates default
- [ ] UUID generation in `defaultSettings`

### 3c. `SettingsController` — **zero tests**

- [ ] Authenticated user → 200 + correct DTO
- [ ] Unauthenticated → 401
- [ ] Empty features/logLevels → empty lists in response
- [ ] DTO conversion (Feature, LogLevel mapping)

### 3d. `ToggleFeature` — **zero tests**

- [ ] Happy path (admin user toggles feature)
- [ ] Non-admin user → 401
- [ ] Missing auth → 401
- [ ] Non-existent recipient

### 3e. `UpdateEmail` — **zero tests**

- [ ] Happy path (email updated + verify sent)
- [ ] User not found in Keycloak
- [ ] Keycloak API error
- [ ] Missing auth

### 3f. `ChangePassword` — **zero tests**

- [ ] Happy path (valid old password)
- [ ] Invalid old password (Keycloak returns no token → throws `Problem`)
- [ ] User not found → 404
- [ ] Keycloak token API error
- [ ] Missing auth

### 3g. `CreateRecipient` — **zero tests**

- [ ] Happy path with auto-generated password
- [ ] With email + custom password
- [ ] Rabbit command `CreateBucketCommand` sent
- [ ] Keycloak API error

---

## 4. Contributions — **ZERO TESTS**

### 4a. `ContributionListener`

- [ ] Payment event → updates monthly + yearly + daily + sends reload
- [ ] Non-payment type → ignored
- [ ] Missing nickname → ignored
- [ ] Missing amount → ignored
- [ ] Existing contribution → amount accumulates
- [ ] New contribution → saved fresh
- [ ] Daily contribution map updates correctly
- [ ] Reload command sent via Rabbit

### 4b. `DonatersController`

- [ ] `list()` for `"day"` → returns daily cache
- [ ] `list()` for `"month"` → builds period key, queries DB
- [ ] `list()` for `"year"` → builds period key, queries DB
- [ ] Empty contributions → empty map
- [ ] Sorting by amount (major asc)
- [ ] Unknown period → empty key

### 4c. `DonatersCacheCleaner`

- [ ] `execute()` → clears dailyContribution map
- [ ] Scheduled cron runs

### 4d. `ContributionCommandSender`

- [ ] `send(recipientId)` → sends with correct routing key

---

## 5. OTP — **ZERO TESTS**

### 5a. `CreateOtpCommand`

- [ ] Execute → stores refreshToken in cache, returns UUID
- [ ] Empty refreshToken

### 5b. `ExchangeOtpCommand`

- [ ] Valid OTP → returns stored token
- [ ] Invalid OTP → returns empty string
- [ ] Expired / missing OTP

### 5c. `OtpCommandController`

- [ ] `create` endpoint
- [ ] `exchange` endpoint
- [ ] E2E create → exchange flow

---

## 6. Info Controller — **ZERO TESTS**

### 6a. `InfoController`

- [ ] Authenticated user → `Info(true, username)`
- [ ] Anonymous / null auth → `Info(false, "")`
- [ ] Missing `preferred_username` attribute

---

## 7. Integration Clients — **ZERO TESTS**

| Client | `link()` | `obtainAccessToken()` | `getUser()` |
|---|---|---|---|
| `TwitchClient` | ❌ | ❌ | ❌ |
| `VKLiveClient` | ❌ | ❌ | ❌ |
| `KickClient` | ❌ | ❌ | ❌ |
| `GoodGameClient` | ❌ | ❌ | ❌ |
| `DonationAlertsClient` | ❌ | ❌ | ❌ |
| `StreamlabsClient` | ❌ | ❌ | ❌ |
| `StreamElementsClient` | ❌ | ❌ | ❌ |
| `DiscordClient` | ❌ | ❌ | ❌ |

---

## 8. Cross-Cutting Issues Found

| Issue | Location | Severity |
|---|---|---|
| **No ownership check** on `DeleteToken` | `DeleteToken.java:L36` — `findById()` ignores owner | 🚨 **Bug** |
| **No ownership check** on `ToggleToken` | `ToggleToken.java:L33` — updates any token by ID | 🚨 **Bug** |
| `setFeatureStatus` may **produce duplicate features** instead of updating | `Settings.java:L33-38` — filter removes old but add doesn't check | 🐛 **Likely bug** |
| Auth is mocked in most tests but **never tests real security** | All integration tests mock `Authentication` | ⚠️ Gap |
| Link controllers have **complex async chains** with no error path tests | All 9 Link* controllers | ⚠️ Risk |

---

## 9. Prioritization

### Priority 1 — Bugs & Security (critical gaps)

1. **DeleteToken ownership check** — currently any authenticated user can delete any token by ID
2. **ToggleToken ownership check** — any user can toggle any token
3. **Duplicate features bug** in `Settings.setFeatureStatus()`
4. Auth edge cases across all controllers

### Priority 2 — Zero-coverage modules (high risk)

5. **Link controllers** (all 9 platforms) — complex OAuth flows
6. **ContributionListener** — payment processing core logic
7. **ChangePassword** — security-sensitive operation
8. **CreateRecipient** — user registration

### Priority 3 — Missing entity/domain tests

9. `GenericToken` — core persistence logic
10. `RefreshToken.obtainAccessToken()`
11. Platform-specific token deletion behavior
12. `TokenRepository` all query methods
13. `SettingsRepository.get()` with default creation

### Priority 4 — Edge cases & error paths

14. Disabled/deleted token handling
15. Exception propagation in async chains
16. OTP create/exchange
17. InfoController authentication states
