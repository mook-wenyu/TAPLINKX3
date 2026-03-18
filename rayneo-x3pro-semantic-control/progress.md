# Progress Log

## 2026-03-17

- Created the standalone project root.
- Added Gradle and version catalog files.
- Added the base Android application module.
- Folded contracts, gesture, overlay, and semantic code into the single app module.
- Added the initial docs set.
- Generated a local Gradle wrapper.
- Verified a baseline `:app:assembleDebug` build after the single-module refactor.
- Implemented real semantic traversal in the semantic navigator.
- Added unit tests for traversal planner behavior.
- Verified `testDebugUnitTest` and `:app:assembleDebug` in the isolated worktree.
- Added a thin pinch-confirm adapter that reuses the existing focused activation path.
- Added unit tests for pinch-confirm routing and cooldown behavior.
- Reverse-inspected `RayNeoIPCSDK` and `MercuryAndroidSDK` to map adaptation-ready capabilities.
- Compared Mercury v0.2.2 and v0.2.5 and found the public adaptation surface largely stable, with new focus-related hints in v0.2.5.
- Added a dedicated `vendor.rayneo` adapter boundary and kept the only `com.ffalcon` import inside it.
- Bootstrapped Mercury through an internal runtime wrapper and added the RayNeo manifest handshake.
- Added a minimal `TouchDispatcherX3 + CommonTouchCallback` input adapter that maps into existing semantic actions.
- Added unit tests for the touch semantic bridge and kept the full build green.
- Synced the implementation plan and launcher copy to the post-TouchDispatcher state so the next round is clearly app audit plus the `FocusTracker` decision, not more feature growth.
- Added `docs/app-audit.md` to define the first-wave audit rubric, target app categories, support tiers, and FocusTracker decision gate.
- Added pure audit tiering types plus unit tests so `Generic`, `WhitelistCandidate`, and `Unsupported` can be derived consistently from tree, focusability, and activation signals.
- Added an ADB-driven semantic debug bridge so device-side audits can trigger `dump-root`, `focus-next`, `focus-previous`, and `activate-focused` from the shell.
- Logged the first live-device session in `docs/adb-debug-log.md`, including the successful enable path, the Android 12 broadcast restriction, and the first failing Settings homepage sample.
- Fixed a real-device traversal bug by separating `isFocusable` from actual `ACTION_FOCUS` support.
- Replaced the static homepage with a single AR-first setup card driven by accessibility-service state and a settings CTA.
- Verified the homepage card on device: one centered card, concise Chinese copy, no overlay control surface, and a working settings handoff CTA.
- Reassessed the device route using the local capability PDF, Mercury sample, and external evidence: for X3 Pro, binocular mirrored UI plus temple gestures should drive the user-visible shell, and the current single-layout homepage should be treated as temporary.
- Migrated the homepage shell onto Mercury's mirrored activity path and added a small temple-action router for the single-card interaction model.
- Verified the new shell compiles, passes tests, and launches on device without crashing; deeper binocular UI inspection still needs follow-up.
- Next: audit target apps before deciding whether vendor focus helpers are actually needed.
