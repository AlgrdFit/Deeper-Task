# DeeperTask Engineering Instructions

## Project requirements

- These instructions apply to the whole repository.
- Jira project `KAN` on `ektif.atlassian.net` is the live source of task-specific requirements.
- Before planning, implementing, or reviewing Jira-backed work, use the `deeper-jira-task` skill to
  read the relevant issue's description, status, parent and links, expected branch, and acceptance
  criteria.
- Do not copy the roadmap or issue details into this file. If Jira and the repository conflict,
  report the conflict rather than guessing.
- A direct user instruction takes precedence when it intentionally changes Jira scope or policy.
- Keep Jira access read-only unless the user explicitly requests a comment, edit, worklog, or status
  transition.

## Architecture and code quality

- Apply Clean Architecture and respect the module boundaries established by the repository and the
  relevant Jira issue.
- Keep dependencies pointing inward: framework and data implementations may depend on domain
  contracts, while domain code must not depend on UI, persistence, transport, or framework details.
- Keep business decisions out of Activities, composables, DTOs, database entities, and
  infrastructure adapters.
- Map external and persistence models at their boundaries instead of exposing them to domain or
  presentation code.
- Prefer cohesive units, explicit dependencies, immutable state, descriptive domain names, and clear
  ownership.
- Introduce shared abstractions only when more than one real consumer needs them; avoid speculative
  layers and generic dumping-ground modules.
- Centralize dependency versions in `gradle/libs.versions.toml` and keep repeated build
  configuration out of feature code.

## Compose UI

- Separate each user-facing feature screen into a screen composable that obtains injected
  dependencies and collects state, and a stateless content composable that receives immutable state
  and event callbacks.
- Preview the stateless content composable so previews do not require Hilt, ViewModels, navigation
  state, or real services.
- Provide one default preview for every new user-facing feature screen.
- Wrap previews in `DeeperTaskTheme(dynamicColor = false)` and use deterministic state with no-op
  callbacks.
- Add extra state previews only when explicitly requested or required by the task's acceptance
  criteria.

## Unit testing

- Write unit tests alongside new or changed business behavior and critical transformations. Prefer
  meaningful scenario coverage over an arbitrary percentage target.
- Structure every unit test with visible `// Arrange`, `// Act`, and `// Assert` sections.
- Test a real instance of the class under test and use MockK for its direct injected collaborators.
  Avoid handwritten fakes and recorders unless the task explicitly requires a specialized in-memory
  implementation or test engine.
- In this JUnit 4 project, declare reusable immutable test values first and reusable mocks next as
  private instance properties at the top of the test class. JUnit 4 creates a fresh test-class
  instance for every test method, so these properties remain isolated between tests.
- Never place mocks or mutable fixtures in companion objects, Kotlin objects, static fields, or
  other state shared across test instances.
- Do not use `@Before`, `@After`, `@BeforeEach`, or `@AfterEach`. Each test must obtain a fresh
  fixture instance from a fixture factory.
- Declare reusable MockK collaborators as private class-level instance properties. Configure
  behavior shared by multiple tests in the `mockk { ... }` declaration; configure one-time scenario
  behavior with `every`/`coEvery` in that test's `// Arrange` section. Never create or stub mocks
  inside fixture factories. Use `verify` for regular calls and `coVerify` for suspend calls.
- Avoid relaxed mocks when they could hide unexpected interactions. Verify calls only when the
  interaction is required behavior.
- Define reusable fixture factory functions with deterministic defaults and named overrides. A
  fixture only constructs a fresh real instance of the class under test from class-level
  collaborators or explicit dependency overrides; it must not create mocks, stub behavior, or select
  a test scenario. Keep one-off scenario values local to the test.
- Keep tests deterministic and independent of real services, credentials, wall-clock timing,
  execution order, and other tests.
- Cover success, failure, validation, empty, boundary, and state-transition scenarios when they
  apply.

## Security and verification

- Never commit credentials, session tokens, API keys, service secrets, `local.properties`, or
  generated local configuration. Do not expose secrets in logs or test output.
- Run the narrowest meaningful tests and static checks for the affected modules, then broader checks
  when changes cross module or build boundaries.
- Report which checks ran, their results, and any check that could not run.
