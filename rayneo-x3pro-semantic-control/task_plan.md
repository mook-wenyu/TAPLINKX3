# Task Plan

## Goal

Create a standalone Android-native project for RayNeo X3 Pro semantic control, then document the product strategy, implementation plan, and current progress.

## Phases

| Phase | Status | Notes |
|---|---|---|
| Confirm project boundary | completed | New project lives in `rayneo-x3pro-semantic-control/` and stays independent from the existing open-source app architecture. |
| Create project scaffold | completed | Single-module Android project created with package-level layers for contracts, gesture, overlay, and semantic code. |
| Write project docs | completed | README, product strategy, implementation plan, and progress docs created. |
| Generate wrapper and verify build | completed | Local wrapper generated and baseline `:app:assembleDebug` passed. |
| Implement semantic traversal slice | completed | Added deterministic focus traversal and safer focused activation, then verified with unit tests and debug build in an isolated worktree. |
| Implement pinch confirm slice | completed | Added a thin pinch-confirm adapter over the validated focused-activation path and verified unit tests plus debug build in an isolated worktree. |
| Analyze RayNeo vendor SDKs | completed | Reverse-inspected local AARs, compared Mercury versions, and mapped vendor capabilities into MVP, prototype, and deferred buckets. |
| Implement vendor adapter bootstrap slice | completed | Added a RayNeo vendor boundary, wired Mercury bootstrap plus manifest handshake, and verified unit tests plus debug build in an isolated worktree. |
| Implement TouchDispatcher input spike | completed | Added a minimal X3-native input adapter that maps `TouchDispatcherX3 + CommonTouchCallback` into existing semantic actions and verified unit tests plus debug build in an isolated worktree. |
| Reframe enablement and shell route | completed | Confirmed X3 binocular shell as the near-term host, downgraded accessibility to enhancement-only, and added an optional mode state machine with honest recovery states. |
| Start camera feasibility spike | in_progress | Added a foreground-only binocular camera probe activity plus unit-tested session tracking; physical-device validation is still pending. |

## Decisions

- Keep MVP semantic, not spatial.
- Keep the project single-module until there is proven pressure to split it.
- Keep vendor-specific interfaces optional.
- Keep gesture recognition behind replaceable abstractions.
- Keep product scope small until accessibility audits are complete.
- Build focus traversal before wiring pinch confirmation, because confirmation without a stable focus target is product drift.
- Keep the target-app audit as a parallel evidence track, but do not let it block the foreground camera feasibility spike.
- Treat RayNeo SDKs as hardware adaptation shims, not the app architecture.
- Keep all `com.ffalcon*` references inside `vendor.rayneo`.
- Treat the foreground camera feasibility spike as the next north-star blocker after shell/input infrastructure, before deeper vendor focus integration.
