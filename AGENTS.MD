# AGENTS.md — Rules and Roles for Discord Bot Project

This file defines how AI agents must work with this repository.

The project is a monolithic Discord bot built with:
- Java (Spring Boot)
- JDA (Java Discord API, slash commands)
- PostgreSQL
- Liquibase

The main goals:
- Keep the codebase modular, clean, and maintainable.
- Follow established best practices (SOLID, DRY, KISS, YAGNI).
- Evolve the architecture toward a well-structured modular monolith.
- Implement features through vertical slices (per-feature modules).
- Avoid uncontrolled Many-to-Many relations and “God” classes.
- Gradually introduce a solid test suite.

---

## GLOBAL PRINCIPLES

All agents MUST follow these principles:

- Use **feature-based modular structure**:
    - Group code by feature/domain, not by technical layer only.
    - Each feature gets its own package with controllers/listeners, services, domain and persistence.
- Respect **SOLID**, **DRY**, **KISS**, **YAGNI**.
- Prefer **small, focused classes** and methods.
- Move **business logic out of JDA listeners** and controllers into services/use cases.
- Avoid cyclic dependencies.
- Prefer **constructor injection** over field injection.
- No hardcoded secrets, tokens, guild IDs, channel IDs or magic constants.
- All configuration must go through `application.yml`, profiles, or structured config objects.
- All DB schema changes must go via **Liquibase changeSets**, never by altering old ones.
- Use **Java 21** features when they improve clarity (records, pattern matching, switch expressions).
- Aim for **testability**: design APIs and classes to be testable with JUnit + Mockito.

## PROJECT STRUCTURE & PACKAGING

Target architecture is a **feature-based modular monolith**.

Recommended package structure (example):

- `com.example.bot.features.rolesrotation.*`
- `com.example.bot.features.chatfilter.*`
- `com.example.bot.features.<other_feature>.*`
- `com.example.bot.shared.*` — shared utilities, base classes, common mappers, etc.
- `com.example.bot.config.*` — Spring/JDA configuration.
- `com.example.bot.core.*` — core abstractions (feature toggling, guild context, etc).

Inside each feature package:

- `presentation` — controllers, slash command handlers, listeners.
- `application` — services/use-cases orchestrating domain logic.
- `domain` — entities, value objects, domain services.
- `infrastructure` — repositories, JPA mappings, Liquibase-related logic if needed.

Agents should **gradually** refactor the project toward this structure.

---

## DISCORD / JDA RULES (SLASH COMMANDS)

- Only **slash commands** are used for interactions.
- Each slash command (or closely related group of subcommands) should be defined in its own class inside the feature package.
- Event listeners / command handlers:
    - must be thin,
    - only map Discord request → domain input,
    - call the corresponding service/use case,
    - map domain result → Discord response.
- Avoid any blocking operations on event threads:
    - use async execution where needed (e.g., `CompletableFuture`, scheduler, or dedicated executor).
- Do not leak JDA entities into domain/application layers:
    - map `Member`, `Guild`, `Message`, etc. to your own domain objects or IDs.

---

## 1. FEATURE DEVELOPER AGENT

**Goal:** implement new features and commands following best practices.

Responsibilities:

- Add or modify:
    - slash commands,
    - scheduled tasks,
    - business logic in services/use cases,
    - feature configuration entities.
- Keep Discord-specific logic isolated to **presentation** layer (listeners, controllers).
- Implement feature toggling using existing `feature` and `guild_feature` structures, refactoring them when needed according to the rules above.
- Ensure new code fits into feature-based package structure.

Rules:

- No business logic inside JDA listeners or controllers.
- Methods should be short and focused; extract helpers or new classes when needed.
- Prefer **pure domain services** for complex business rules.
- Use `@Transactional` only where necessary and at the right boundary layer.
- Validate external input (Discord arguments, IDs) before passing to domain logic.
- Always consider how this feature might need per-guild configuration in the future.

---

## 2. REFACTOR & ARCHITECTURE AGENT

**Goal:** improve architecture, readability, and modularity without changing behavior unless requested.

Responsibilities:

- Restructure packages into feature-based modules.
- Split large service methods into smaller methods and/or new classes.
- Extract shared logic into `shared` or domain-specific helpers.
- Simplify or replace problematic ManyToMany relationships.
- Rename classes/methods for better expressiveness and consistency.
- Introduce or improve abstractions for:
    - feature toggling,
    - guild-scoped settings,
    - common Discord workflows.

Rules:

- Do NOT change observable behavior unless explicitly requested.
- After refactoring:
    - ensure all existing behavior remains intact,
    - update tests (when they exist) or add smoke tests to guard functionality.
- When dealing with ManyToMany:
    - prefer explicit join entities with meaningful names if behavior or attributes exist.
    - avoid generic, nameless join tables in the object model.

---

## 3. TESTING AGENT

**Goal:** build and maintain a robust test suite.

Responsibilities:

- Introduce and extend tests using:
    - **JUnit 5** for unit and integration tests,
    - **Mockito** for mocking collaborators,
    - optionally **Testcontainers** for PostgreSQL/Liquibase integration tests.
- Cover:
    - domain logic,
    - application services,
    - key repositories,
    - critical feature flows (e.g., role rotation, chat filter behavior).

Rules:

- For unit tests:
    - use plain JUnit + Mockito, no Spring context.
    - test public behavior, not internals.
- For integration tests:
    - use Spring Boot test + Testcontainers where DB interaction is relevant.
    - ensure Liquibase migrations are applied.
- Name tests clearly:
    - `shouldDoSomething_whenCondition`.
- Any new or changed business rule should be accompanied by tests.
- Gradually add tests to existing legacy code when it is touched for refactoring.

---

## 4. DATABASE & MIGRATION AGENT

**Goal:** keep PostgreSQL schema clean, stable, and migratable.

Responsibilities:

- Use **Liquibase** for any schema changes:
    - new tables,
    - columns,
    - constraints,
    - indexes.
- Ensure each domain/feature has clear and consistent table naming.
- Design join entities and per-feature configuration tables according to feature needs.

Rules:

- Never modify existing changeSets; always add new ones.
- Each changeSet must have:
    - unique, descriptive `id`,
    - `author`,
    - proper rollback where applicable.
- Prefer explicit PostgreSQL types and constraints.
- When changing M2M structures:
    - plan data migration explicitly (if needed),
    - consider backward compatibility during transition.
- Avoid over-normalization if it makes feature configuration unreasonably complex, but also avoid “generic key-value” tables for everything:
    - prefer typed configuration entities per feature.

---

## 5. CONFIGURATION & INFRASTRUCTURE AGENT

**Goal:** ensure predictable runtime behavior and clean configuration.

Responsibilities:

- Manage:
    - `application.yml` and profile-specific configs,
    - logging configuration,
    - bot token and other secrets via environment variables,
    - any integration with external services.
- Provide structured configuration classes using `@ConfigurationProperties` where appropriate.
- Centralize exception handling and logging of errors.

Rules:

- No secrets or tokens in the code or `application.yml` committed to VCS.
- Any new config option must:
    - be added to the appropriate `application-*.yml`,
    - be documented (at least briefly) in README.
- Logging must:
    - use SLF4J,
    - avoid excessive noise,
    - include meaningful context (guildId, userId, commandName) when helpful.
- Errors should be logged once at an appropriate boundary, not in every layer.

---

## 6. DOCUMENTATION AGENT

**Goal:** keep the project understandable for future work.

Responsibilities:

- Update README when:
    - new features are added,
    - configuration is extended,
    - startup/usage flow changes.
- Document:
    - available slash commands,
    - feature toggles (global and per-guild),
    - per-feature guild settings concept.
- Maintain a simple `CHANGELOG.md` with user-facing changes and important refactors.

Rules:

- Documentation should be concise but accurate.
- Remove or update outdated documentation.
- Prefer practical examples over long theoretical sections.

---

## CODE STYLE GUIDELINES

- Follow standard Spring & Java best practices.
- Use meaningful names for classes, methods, and fields.
- Limit method length; extract private methods or new classes when logic grows.
- Avoid static utility classes when dependency injection can provide better design.
- Domain entities:
    - avoid exposing setters for everything,
    - favor constructors and clearly defined modification methods.
- DTOs:
    - may use Java records where suitable.
- Use `Optional` only for return types, not for fields or parameters.

---

## AFTER EACH CHANGE

After an agent finishes a task:

1. Verify that:
    - code compiles,
    - tests pass (or are added/updated),
    - Liquibase migrations are valid.
2. Ensure architecture and package structure move closer to the target model, not further away.
3. Update documentation if behavior or configuration has changed.

---

# END OF AGENTS.MD
