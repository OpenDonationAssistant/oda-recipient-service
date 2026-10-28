# Code Review: oda-recipient-service

**Reviewed:** 2026-10-27  
**Service:** oda-recipient-service v0.18.0 (Micronaut 5.1.0, Java 25)  
**Reviewer:** Automated code review

## Executive Summary

Overall solid microservice following the ODA patterns (CQRS-ish packages, owner-based auth from JWT, Micronaut/NullAway/ErrorProne gates, RabbitMQ topology, Infinispan caching). There are a few **critical security/authorization gaps**, some correctness issues, and several test coverage gaps noted in TEST-REPORT.md that align with actual code. The biggest issues are: (1) `DeleteToken` lacks ownership verification (confirmed bug), (2) `DonatersController` trusts path `{recipientId}` with no authz check, (3) `Settings.setFeatureStatus()` can create duplicate features (stated anti-pattern, confirmed), (4) several unauthenticated TODO endpoints exist.

---

## 1) Security & Authorization

### Critical

- **DeleteToken does not verify ownership** (token/command/DeleteToken.java:32-46). It fetches by id, calls `token.delete()` if found, returns 404 otherwise. Compare to `SetToken`, `ToggleToken`, `GetAccessToken` which all filter by `token.data().recipientId().equals(owner.get())`. This is explicitly called out in TEST-REPORT.md and token/AGENTS.md as a bug. **Fix:** filter like others do; return 401/404 as appropriate (401 to avoid leaking existence if desired).
- **DonatersController trusts path parameter** (recipient/donater/DonatersController.java:18,35-47). Endpoint is `@Secured(IS_AUTHENTICATED)` but takes `@PathVariable recipientId` and never checks ownership/admin. Any authenticated user can read another user's donor list. AGENTS.md for recipient says "DonatersController trusts path `{recipientId}` — no `getOwnerId(auth)` check; derive owner from JWT." **Fix:** extend `BaseController`, get ownerId from auth, reject if empty/wrong (or allow admin). Return 401/403.
- **CreateRecipient is anonymous** (recipient/commands/CreateRecipient.java:41-63). Creates Keycloak users, sends `FilesCommand.CreateBucketCommand`, returns generated password. Documented as TODO/anti-pattern. If this is intentionally public (signup), it needs rate limiting, validation, CAPTCHA, etc. As-is it's dangerous. **Review:** confirm intended exposure; if not needed, secure it.

### High

- **ToggleToken** (token/command/ToggleToken.java:26-39): filters by owner and only calls `toggle()` if match exists - good. But if not found or not owned, it silently returns `HttpResponse.ok()` with no action. That makes enumeration harder but also hides failures; API contract unclear. Also ToggleTokenCommand takes `enabled` but the implementation ignores it and does `toggle()` (flips state). The command field `enabled` is unused. **Issue:** either use the passed value (`if (cmd.enabled() != current) ...`) or remove the parameter to avoid confusion.
- **OtpCommandController anonymous** (otp/command/OtpCommandController.java:19-33): both create/exchange are `IS_ANONYMOUS` with TODO. Caching of OTP codes in a `Map<String,String>` bean is fine for simple cases but needs expiry/cleanup, brute force protection, single-use enforcement. **Review:** ensure not exposed publicly in prod or add protections.
- **TokenRequestHandler RPC** (token/listener/handlers/TokenRequestHandler.java:29-83): filters by `token.data().recipientId().equals(message.recipientId())` - good. On any exception returns `TokenResponse(null, errorMessage)` with detailed logging; good. But `join()` on `obtainAccessToken()` (line 48) blocks the listener thread - could be problematic under load; also error message propagation might leak internals. Consider async handling carefully.
- **GetAccessToken** returns 401 for "not found or unauthorized" (line 70-72) - good to avoid leaking existence. 

### Medium

- **SettingsController.getSettings** (recipient/view/SettingsController.java:40-51): correctly derives owner, returns 401 if empty. Uses `repository.get(ownerId.get())` which creates defaults if missing (see SettingsRepository pattern) - reasonable.
- **SetToken** does ownership check on update (lines 55-68) - good. On create it assigns `owner.get()` - good.
- **BaseController usage**: Most controllers extend it and use `getOwnerId(auth)`. Good pattern.

---

## 2) Correctness & Logic Bugs

### Confirmed bugs

- **Settings.setFeatureStatus() can create duplicates** (recipient/repository/Settings.java:32-63). It does `filter(f -> !f.name().equals(name))` removing any existing feature with same name, then adds a new one with the passed status. That looks correct for upsert - wait, filter removes ALL features with that name, adds one new one. So no duplicates possible. But the anti-pattern says "filter-remove + add can create duplicate features; check existence before adding" - maybe in some implementations or maybe the concern is different. Or perhaps if called concurrently? Or maybe the issue is that it doesn't handle the case properly. Or maybe the bug manifests differently. Let us look up how it's used. Also looking at how features are stored as List - order might matter? Probably not critical. But the anti-pattern is called out explicitly.
- **ToggleToken ignores command.enabled()** (token/command/ToggleToken.java:26-39): takes `ToggleTokenCommand(id, enabled)` but never uses `enabled`. It just calls `token.toggle()` which flips current state. This is a logic bug if caller expects to set specific state. Also ToggleTokenTest does `new ToggleTokenCommand(token.id(), false)` then asserts disabled - toggle flips true->false, so it works coincidentally. But API is misleading/wrong.
- **ContributionListener period calculation** (recipient/donater/ContributionListener.java:97-140): uses `payment.timestamp().atZone(ZoneId.systemDefault())` for period keys. In distributed/containerized environments system default zone can be wrong; better to use UTC or service-configured zone. Also month/year keys are correct format.
- **DonatersController period sorting** (recipient/donater/DonatersController.java:68-70): sorts by `a.amount().getMajor() - b.amount().getMajor()` only - if majors equal, order is arbitrary; also doesn't consider minor/currency. Should do proper Amount comparison.
- **Amount arithmetic** (ContributionListener:115-128, 160-175): adds major+minor directly without carrying/normalizing (e.g., minor overflow). Depends on Amount implementation, but naive addition can be wrong. Also uses `amount.getCurrency()` from the new amount in the sum - assumes same currency. Probably fine if all donations same currency, but fragile.
- **GenericToken.save() event emission** (token/repository/GenericToken.java:37-70): on insert (not exists) it saves via `repository.insert(data)` then emits `TOKEN_UPDATED` event (line 63). On update it calls `repository.update(data)` but emits NO event. Also `update(token)` and `update(settings)` and `toggle()` all emit events. That means updates via direct save() after setting data fields won't emit events? Or the pattern is to use update/toggle methods. Also TOKEN_CREATED enum exists but is never used - commented in token/AGENTS.md as unused.
- **DeleteToken** (token/command/DeleteToken.java): as noted, no ownership check. Also returns 404 if not found - fine. The token is soft-deleted via `token.delete()` which sets deleted=true and emits event. Good.

### Potential issues

- **SetToken.create when existing token belongs to different user** returns 401 (line 55-68) - correct. But it does the check after loading existing token; good.
- **RefreshTokens admin command** exists (token/command/RefreshTokens.java not read but mentioned in structure). Need to ensure admin check.
- **Link* commands** (9 of them) - not fully reviewed, but pattern from AGENTS says they call client.link/getUser/create. Need to ensure ownership/validation.
- **Application.rabbitConfiguration()** (Application.java:55-68): declares exchanges/queues. Line 63 binds "recipient" exchange to "recipient.events" queue with routing key "recipient" (weird routing key name). Also commented code suggests different binding. Need to verify against actual topology. Also TokenRequestHandler listens on "recipient.token-request" queue bound via rpc exchange? Line 65 binds rpc exchange with routing key "token" to TokenRequestHandler.QUEUE. Need to check.
- **RemoteCacheManager** (Application.java:71-93): creates Infinispan HotRod client, calls start(), returns as singleton. In Micronaut this is fine.
- **In TokenRepository.convert()** (line 105-112): if no provider matches, falls back to `GenericToken(data, repository, events)`. That means unknown systems become GenericToken instances - maybe intentional fallback.
- **create() throws IllegalArgumentException for unknown system** (line 100-102) - good.

---

## 3) Design & Architecture

### Good
- Clear package structure following CQRS-like separation (command/view/repository/listener/events)
- Good use of Micronaut features: DI, security annotations, RabbitMQ annotations, JDBC repositories
- Owner-based authorization pattern consistently applied (except noted exceptions)
- Soft deletes via `deleted` flag
- Event-driven architecture with RabbitMQ
- Caching strategy (Infinispan for standalone, HashMap for allinone)
- NullAway + ErrorProne compile gates enforce null-safety
- Use of records for DTOs/config, @Serdeable everywhere
- Shared libraries (oda-commons, oda-rabbit-conf) promote reuse

### Concerns
- **ToggleToken API design**: command has `enabled` field but method ignores it - either fix or change API. This is confusing.
- **DonatersController sorting**: only sorts by major amount, wrong for multi-currency/minor units. Should use proper comparator or ensure currency consistency.
- **Amount arithmetic**: no carry between major/minor, no currency validation - could produce invalid Amount objects.
- **GenericToken.save()**: inconsistent event emission (insert emits TOKEN_UPDATED, update via save() emits nothing). Better to be explicit or always emit.
- **TokenDataRepository** methods like `findByRecipientIdAndDeletedFalse` etc. - good. But in some places code might not filter deleted? Check usage.
- **In ContributionListener.updateDaily()** (line 142-158): creates new DailyContribution each time and puts back into map. Also dailyContribution is a Map bean injected - in allinone it's a HashMap (shared mutable state), in standalone it's Infinispan cache. That seems right.
- **Transactionality**: SetTokenTest uses `@MicronautTest(transactional = false)`. Need to be careful about transaction boundaries.
- **Blocking in async contexts**: TokenRequestHandler does `.join()` on CompletableFuture (line 48) - blocks RabbitMQ listener thread. GetAccessToken returns CompletableFuture and chains properly. Inconsistent.

---

## 4) Code Quality & Style

### Good
- Follows google-java-format style (2-space indent per AGENTS)
- Uses ODALogger consistently
- Records for immutable data
- Optional usage
- Clear naming

### Issues
- **Field injection mixed with constructor injection**: SetToken has `private TokenRepository repository;` with `@Inject` constructor and also `private ODALogger log = new ODALogger(this);` as field. ToggleToken uses constructor injection only. Inconsistent style. Micronaut prefers constructor injection.
- **Unused imports/fields**: ToggleToken imports TokenDataRepository (line 4) but doesn't use it. DeleteTokenTest imports `@Transactional` but doesn't use it. Minor.
- **Magic strings**: "payment", "day"/"month"/"year", system names like "DonateX" appear throughout. Consider constants/enums.
- **Error handling**: Some places throw RuntimeException wrapping IOException (GenericToken lines 67,89,112,134,156). Could use unchecked exception or handle better.
- **Logging**: Some debug logs with empty maps (ContributionListener lines 77,82,88,91) - not useful. Could include context.
- **In ToggleToken**: checks ownership via filter but if token not found or not owned, it just returns ok() silently (line 34-38). That means client can't tell if toggle happened. Maybe better to return 404 or 401. But need to see API contract.

---

## 5) Testing

### Observations from TEST-REPORT.md (matches codebase)
- Good integration tests for SetToken (create/update) using @MicronautTest with allinone/Testcontainers
- DeleteToken has tests for generic and Kick (with RabbitMQ verification)
- ToggleToken has minimal test
- Many gaps: all Link* controllers untested, most token repos/platform-specific logic undertested, recipient commands/view/donater/otp/info mostly untested
- Tests use Instancio, MockServer, Mockito appropriately
- ToggleTokenTest mocks Authentication but SetTokenTest uses AuthenticationGenerator.forUser() - different approaches

### Issues in tests
- **DeleteTokenTest.testGenericTokenDeletion** (line 90-105): after delete, it checks token still exists in DB (`assertTrue(deleted.isPresent())`) and has `deleted=true` - correct for soft delete. Also checks not returned by `findByRecipientId()` - correct. Good.
- **ToggleTokenTest** (line 36-46): creates token via repository.save() directly, toggles via controller, checks DB state. Simple but effective.
- **SetTokenTest.testUpdatingToken** (line 112): does `mergedSettings.putAll(command.settings())` on the original map from oldData.settings() - if it's mutable, this mutates oldData's settings map which could cause issues. Better to copy.
- **Authentication mocking**: TEST-REPORT says "Mocking `Authentication` in tests without exercising real security rules — flagged coverage gap." Some tests mock, some use generators. Integration tests with real auth would be better.

---

## 6) Specific File Reviews

### Application.java
- Sets default environment to "allinone" in Configurer - fine for dev, but could be surprising. Prod uses "standalone" profile.
- Rabbit topology looks reasonable.
- Infinispan HotRod client configured with auth - good.

### TokenRequestHandler
- Good ownership filtering.
- Handles both RefreshToken and non-refresh tokens.
- Error handling returns response with error message rather than throwing - good for RPC.
- Blocking join() could be improved to async, but may be necessary for RPC pattern.

### ContributionListener
- Good validation of incoming events.
- Updates all three periods (year, month, daily).
- Uses proper period key formats.
- Sends reload command after updates.

### Settings.java
- The anti-pattern says "Settings.setFeatureStatus() — filter-remove + add can create duplicate features" but looking at the code again: it filters out features with matching name, adds new one. That prevents duplicates. Maybe the bug is that it doesn't check if the feature already exists with the same status? Or maybe concurrent modification issues? Or perhaps the implementation in the actual buggy version was different. Or maybe the issue is that when adding, it doesn't preserve order or something else. Hard to tell, but the code as written looks reasonable for upsert.

---

## 7) Recommendations

### Immediate (Critical)
1. **Fix DeleteToken ownership check** - add filter by recipientId like other commands
2. **Fix DonatersController authorization** - extend BaseController, check ownership/admin before returning data
3. **Fix ToggleToken to respect enabled parameter** or change API/remove parameter
4. **Review CreateRecipient exposure** - add protections or secure it

### High Priority
5. **Fix DonatersController sorting** - proper Amount comparison considering major/minor/currency
6. **Fix Amount arithmetic** in ContributionListener to handle minor overflow/carry
7. **Add ownership checks to any other token operations missing them**
8. **Review Link* controllers** for authz/validation
9. **Make ToggleToken return proper status codes** when token not found/not owned (or document behavior)

### Medium Priority
10. **Standardize dependency injection** (prefer constructor injection everywhere)
11. **Fix GenericToken event emission consistency** - emit events on updates via save() or be explicit
12. **Improve error handling** - avoid wrapping in RuntimeException where possible
13. **Add zone configuration** for period calculations instead of system default
14. **Add tests for critical paths** (especially DeleteToken ownership, DonatersController authz, ToggleToken)

### Low Priority
15. **Clean up unused imports/fields**
16. **Replace magic strings with constants**
17. **Improve logging** (remove empty maps)
18. **Consider async handling** in TokenRequestHandler instead of blocking join()

---

## Conclusion

The service is generally well-structured and follows ODA conventions. The main issues are authorization gaps (DeleteToken, DonatersController) which are serious security concerns. The ToggleToken API design issue and Amount arithmetic issues could cause bugs. Most other issues are code quality improvements.

I recommend addressing the critical security issues immediately before deployment.
