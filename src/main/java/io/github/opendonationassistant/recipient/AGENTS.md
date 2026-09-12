# AGENTS.md — recipient

**Generated:** 2026-09-12
**Commit:** 209fe29

## OVERVIEW

Recipient settings (features/log levels), daily contribution awards, and account commands: 18 files in 4 subpackages.

## STRUCTURE

- `commands/` (4) — `@Controller` command classes: `CreateRecipient`, `ToggleFeature`, `UpdateEmail`, `ChangePassword`
- `donater/` (8) — contribution tracking: `Contribution`, `DailyContribution`, `ContributionRepository`, `ContributionListener`, `DonatersController`, `DonatersCacheConfiguration`, `DonatersCacheCleaner`, `ContributionCommandSender`
- `repository/` (5) — settings aggregate: `Settings`, `SettingsData`, `SettingsDataRepository`, `SettingsRepository`, `FeaturesUpdatedEvent`
- `view/` (1) — `SettingsController` (settings CRUD)

### commands/
- `CreateRecipient` — POST `/recipients/commands/create-recipient`; creates Keycloak user, sends `FilesCommand.CreateBucketCommand`, returns generated password. **IS_ANONYMOUS** (see ANTI-PATTERNS).
- `ToggleFeature` — POST `/recipients/commands/toggle-feature`; admin-only (`isAdmin`); calls `Settings.setFeatureStatus()`.
- `UpdateEmail` — POST `/recipients/commands/update-email`; updates Keycloak user + `sendVerifyEmail()`.
- `ChangePassword` — POST `/recipients/commands/changePassword`; verifies old password via KeycloakClient token grant, then `resetPassword()`.

### donater/
- `Contribution` — `@Serdeable @MappedEntity("contribution")` record: id, recipientId, nickname, period, `Amount`; `withAmount()` for upserts.
- `DailyContribution` — mutable `@Serdeable` class wrapping `Map<String, Amount>`; cache value type.
- `ContributionRepository` — `@JdbcRepository(POSTGRES)`; `getByRecipientIdAndPeriod`, `getByRecipientIdAndPeriodAndNickname`.
- `ContributionListener` — `@RabbitListener` on queue `recipient.events`; consumes `HistoryItemEvent`; skips unless `type=="payment"` + nickname/amount present; computes month `"%d_%d"` (year_month) and year `"%d"` keys; upserts DB + mutates daily cache; then `ContributionCommandSender.send(recipientId)`.
- `DonatersController` — GET `/recipients/{recipientId}/donaters?period=day|month|year`; day → cache, month/year → DB.
- `DonatersCacheConfiguration` — `@Factory` `Map<String, DailyContribution>`; standalone → Infinispan (`SerdeableEntryMarshaller`), allinone → HashMap.
- `DonatersCacheCleaner` — `@Scheduled` hourly cron clears daily cache.
- `ContributionCommandSender` — declarative `@RabbitClient("amq.topic")`; `@Binding "<recipientId>donaterstoplist"`.

### repository/
- `Settings` — aggregate facade: `data()`, `setFeatureStatus()`, `save()`; emits `FeaturesUpdatedEvent` via events facade.
- `SettingsData` — `@MappedEntity("settings")` record; features/logLevels as JSONB.
- `SettingsDataRepository` — `@JdbcRepository(POSTGRES)`; `findByRecipientId`.
- `SettingsRepository` — `@Singleton` facade; defaults to empty settings with `Generators.timeBasedEpochGenerator()` id.
- `FeaturesUpdatedEvent` — `@Serdeable` record implementing `HasRecipientId`.

### view/
- `SettingsController` — GET `/recipients/settings`; extends `BaseController`, owner from JWT.

## WHERE TO LOOK

| Task | Location |
|------|----------|
| Contribution/payment logic | `donater/` (listener → repository + cache → command sender) |
| Recipient settings CRUD | `view/SettingsController.java` + `repository/` |
| Registration / account commands | `commands/` |

## CONVENTIONS (package-specific)

- Money = `commons.Amount` (Integer major, Integer minor, String currency) from oda-commons.
- Period keys: day = cache lookup, month = `"%d_%d"` (year_month), year = `"%d"`.
- `SettingsData` features/logLevels stored as JSONB via `@MappedProperty(type = DataType.JSON)`.
- `ContributionListener` consumes `events.history.event.HistoryItemEvent` from oda-rabbit-conf.

## ANTI-PATTERNS

- `Settings.setFeatureStatus()` — filter-remove + add can create duplicate features; check existence before adding (flagged bug).
- `DonatersController` trusts path `{recipientId}` — no `getOwnerId(auth)` check; derive owner from JWT.
- `CreateRecipient` is `IS_ANONYMOUS` — don't add new anonymous endpoints.