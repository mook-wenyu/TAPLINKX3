# RayNeo X3 Pro Semantic Control Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build an independent Android-native project for RayNeo X3 Pro that preserves the north-star of camera-vision, no-peripheral, background-resident third-party gesture control, while shipping a realistic near-term track based on binocular shell, native temple gestures, and optional accessibility enhancement.

**Architecture:** Use a single Android app module with package-level layering for contracts, gesture, semantic execution, and focus HUD feedback. Treat the current X3-native shell and accessibility-enhancement work as enabling infrastructure, not the final interaction model, and keep future camera-vision / background-resident control paths decoupled from today’s shell logic.

**Tech Stack:** Kotlin, Android SDK 36, AccessibilityService, Foreground Service (future), MediaPipe-compatible gesture abstraction, Material 3, Coroutines.

---

## Current status

- [x] Created standalone Android project at `rayneo-x3pro-semantic-control/`
- [x] Collapsed the project into a single `app` module with package-level layering
- [x] Added initial AccessibilityService skeleton
- [x] Added pinch confirmation state machine skeleton
- [x] Routed pinch confirmation into the existing safe activation path
- [x] Added HUD overlay controller placeholder
- [x] Generated local Gradle wrapper for the new project
- [x] Verified clean build of the standalone project
- [x] Implemented real focus traversal and safer focused activation behavior
- [x] Completed local reverse inspection of `RayNeoIPCSDK` and `MercuryAndroidSDK`
- [x] Compared `MercuryAndroidSDK` v0.2.2 and v0.2.5 public API surface
- [x] Introduced a minimal RayNeo vendor adapter boundary and Mercury bootstrap
- [x] Implemented a minimal `TouchDispatcherX3 + CommonTouchCallback` input spike
- [x] Added an app-audit baseline document and support-tier heuristics for the next device round
- [x] Added an ADB-driven debug command bridge for real-device semantic audits
- [x] Clarified the homepage product decision: one accessibility-service setup card, no main overlay toggle yet
- [x] Replaced the static homepage with a single AR-first accessibility-service setup card
- [x] Reassessed the device path and decided the single-layout homepage is only a temporary slice for X3 Pro
- [x] Migrated the homepage shell to `BaseMirrorActivity` with Mercury-driven binocular setup wiring
- [x] Audited the accessibility enable path and downgraded user-driven settings enablement from assumed path to high-risk pending validation
- [x] Reframed the project into a north-star goal versus a realistic near-term delivery track
- [x] Recorded that camera-vision remains the north-star route, but that standard Android background camera constraints block treating it as the immediate default implementation path
- [x] Started a minimal foreground camera feasibility spike with a binocular probe host and unit-tested session tracking
- [ ] Completed target app accessibility audit
- [ ] Verified whether `FocusTracker / RecyclerViewFocusTracker` materially improve on the existing accessibility-first focus model
- [ ] Completed real-device validation of the `TouchDispatcherX3` input spike on X3 Pro hardware
- [ ] Reassessed whether a camera-backed gesture provider is still necessary after audit findings

## Task 1: Foundation Scaffold

**Files:**
- Create: `rayneo-x3pro-semantic-control/settings.gradle.kts`
- Create: `rayneo-x3pro-semantic-control/build.gradle.kts`
- Create: `rayneo-x3pro-semantic-control/gradle/libs.versions.toml`
- Create: `rayneo-x3pro-semantic-control/app/build.gradle.kts`
- Create: `rayneo-x3pro-semantic-control/app/src/main/java/dev/wenyu/semanticcontrol/core/contracts/SemanticAction.kt`
- Create: `rayneo-x3pro-semantic-control/app/src/main/java/dev/wenyu/semanticcontrol/feature/semantic/SemanticAccessibilityService.kt`

- [x] Create standalone project root and single-module graph
- [x] Add stable contract types for semantic actions and gesture signals
- [x] Add placeholder service and overlay classes
- [x] Run Gradle wrapper generation for the new project
- [x] Run a baseline build to verify the scaffold compiles

## Task 2: MVP Semantic Layer

**Files:**
- Modify: `rayneo-x3pro-semantic-control/app/src/main/java/dev/wenyu/semanticcontrol/feature/semantic/SemanticNavigator.kt`
- Modify: `rayneo-x3pro-semantic-control/app/src/main/java/dev/wenyu/semanticcontrol/feature/semantic/SemanticAccessibilityService.kt`
- Test: `rayneo-x3pro-semantic-control/app/src/test/...`

- [x] Implement node traversal for next / previous candidate selection
- [x] Implement activate on focused candidate via clickable ancestor fallback
- [x] Keep global back / home actions available in the semantic navigator
- [ ] Add broader unit tests for semantic action routing beyond planner logic
- [ ] Validate behavior against at least one well-structured sample app

## Task 3: Gesture Confirmation Layer

**Files:**
- Modify: `rayneo-x3pro-semantic-control/app/src/main/java/dev/wenyu/semanticcontrol/feature/gesture/GestureEngine.kt`
- Modify: `rayneo-x3pro-semantic-control/app/src/main/java/dev/wenyu/semanticcontrol/feature/gesture/PinchConfirmationStateMachine.kt`
- Create: `rayneo-x3pro-semantic-control/app/src/main/java/dev/wenyu/semanticcontrol/feature/gesture/mediapipe/...`

- [x] Keep only one primary confirmation gesture for MVP
- [x] Route pinch confirm into the existing focused activation path
- [ ] Reassess whether a camera-backed gesture provider is still necessary after the foreground camera spike and first-wave device evidence
- [x] Add cancel and cooldown handling to the confirmation path
- [x] Add deterministic tests for the pinch confirm controller path

## Task 4: Focus HUD and Recovery UX

**Files:**
- Modify: `rayneo-x3pro-semantic-control/app/src/main/java/dev/wenyu/semanticcontrol/feature/overlay/FocusHudOverlayController.kt`
- Modify: `rayneo-x3pro-semantic-control/app/src/main/java/dev/wenyu/semanticcontrol/feature/overlay/FocusHudState.kt`

- [ ] Implement `candidate` and `armed` HUD states
- [ ] Show action hint and fallback hint
- [ ] Add stable recovery messages: no target, target changed, action unavailable
- [ ] Verify overlay behavior under app switches and service reconnects

## Task 8: Homepage Setup Card

**Files:**
- Modify: `rayneo-x3pro-semantic-control/app/src/main/java/dev/wenyu/semanticcontrol/app/MainActivity.kt`
- Modify: `rayneo-x3pro-semantic-control/app/src/main/res/layout/activity_main.xml`
- Modify: `rayneo-x3pro-semantic-control/app/src/main/res/values/strings.xml`
- Modify: `rayneo-x3pro-semantic-control/docs/PRODUCT_STRATEGY.md`

- [x] Replace the static homepage summary with a single AR-first setup card centered on accessibility service status
- [x] Model homepage states as `not enabled / enabled / needs attention` instead of a fake in-app system toggle
- [x] Deep-link from the CTA into the relevant accessibility settings path
- [x] Keep overlay out of the main control surface until it becomes a real permission-backed feature
- [x] Verify the homepage still respects RayNeo X3 Pro single-goal and safe-attention constraints

## Task 9: X3 Binocular Homepage Shell

**Files:**
- Modify: `rayneo-x3pro-semantic-control/app/src/main/java/dev/wenyu/semanticcontrol/app/MainActivity.kt`
- Modify: `rayneo-x3pro-semantic-control/app/src/main/res/layout/activity_main.xml`
- Create or modify: `rayneo-x3pro-semantic-control/app/src/main/java/dev/wenyu/semanticcontrol/vendor/rayneo/...`
- Modify: `rayneo-x3pro-semantic-control/docs/PRODUCT_STRATEGY.md`

- [x] Replace the temporary single-layout homepage shell with an X3-native binocular mirrored shell
- [x] Rehost the current setup-card content inside a mirrored left/right presentation path
- [x] Promote native temple gestures to the homepage interaction primary path
- [ ] Keep accessibility as a compatibility/fallback path rather than the shell architecture driver
- [ ] Finish deeper on-device binocular homepage verification before adding more user-visible pages

## Task 10: Accessibility Enable Path Audit

**Files:**
- Modify: `rayneo-x3pro-semantic-control/docs/PRODUCT_STRATEGY.md`
- Modify: `rayneo-x3pro-semantic-control/docs/adb-debug-log.md`
- Modify: `rayneo-x3pro-semantic-control/docs/PROGRESS.md`

- [x] Verify homepage `click` reaches Android accessibility settings on device
- [x] Verify homepage `double-click` exits back to launcher on device
- [x] Confirm that the full user-driven settings enable path is still unproven
- [ ] Determine whether the system accessibility settings page itself can be operated end-to-end via temple gestures
- [x] Define the replacement enable strategy matrix (`enhancement-only` default, OEM/system secondary, companion exploratory)
- [ ] Expand settings-page semantic snapshots so temple-driven enablement can be audited step by step instead of inferred from coarse root data
- [x] Confirm that switching to Android Settings can force-stop the third-party app process on X3 Pro, breaking any in-process semantic assist path

## Task 11: Replacement Enablement Strategy

**Files:**
- Modify: `rayneo-x3pro-semantic-control/docs/PRODUCT_STRATEGY.md`
- Modify: `rayneo-x3pro-semantic-control/docs/IMPLEMENTATION_PLAN.md`
- Modify: `rayneo-x3pro-semantic-control/docs/PROGRESS.md`
- Modify: `rayneo-x3pro-semantic-control/README.md`

- [x] Compare `enhancement-only`, `OEM/preinstall/system`, `companion phone`, and `ADB/dev-only` against current repo constraints
- [x] Set `Accessibility = enhancement-only` as the current product default
- [x] Keep OEM/system integration as the secondary strategic track
- [x] Keep companion phone as exploratory only until it proves it can materially change enablement outcomes
- [x] Translate this strategy into concrete follow-up implementation tickets

## Task 12: Optional Accessibility Mode State Machine

**Files:**
- Modify: `rayneo-x3pro-semantic-control/app/src/main/java/dev/wenyu/semanticcontrol/app/...`
- Modify: `rayneo-x3pro-semantic-control/docs/PRODUCT_STRATEGY.md`
- Test: `rayneo-x3pro-semantic-control/app/src/test/...`

- [x] Persist whether the user is in `native-only` or `accessibility-enhanced` mode
- [x] Treat accessibility as optional enhancement, not a blocking bootstrap prerequisite
- [x] Recover cleanly after returning from Settings or after process death
- [x] Keep homepage messaging honest when accessibility is unavailable or unenabled
- [x] Add deterministic tests for mode transitions and relaunch recovery

## Task 13: OEM / Preinstall Feasibility Track

**Files:**
- Modify: `rayneo-x3pro-semantic-control/docs/RAYNEO_SDK_ADAPTATION.md`
- Modify: `rayneo-x3pro-semantic-control/docs/PRODUCT_STRATEGY.md`
- Create: `rayneo-x3pro-semantic-control/docs/oem-enable-path.md`

- [ ] Document the minimum OEM/system capabilities needed to replace the broken Settings bootstrap path
- [ ] Separate public SDK evidence from privileged/preload assumptions
- [ ] Define the exact vendor questions required before spending engineering effort on OEM integration

## Task 14: Companion Phone Exploratory Track

**Files:**
- Modify: `rayneo-x3pro-semantic-control/docs/PRODUCT_STRATEGY.md`
- Modify: `rayneo-x3pro-semantic-control/docs/IMPLEMENTATION_PLAN.md`

- [ ] Define a narrow success criterion for companion-phone assistance
- [ ] Limit companion scope to onboarding support unless it proves it can materially improve enablement outcomes
- [ ] Avoid promoting companion-phone flow to the mainline product path without device-side evidence

## Task 15: Camera-Vision Feasibility Spike

**Files:**
- Create: `rayneo-x3pro-semantic-control/app/src/main/java/dev/wenyu/semanticcontrol/feature/camera/CameraFeasibilitySessionTracker.kt`
- Create: `rayneo-x3pro-semantic-control/app/src/main/java/dev/wenyu/semanticcontrol/feature/camera/CameraFeasibilitySnapshotFormatter.kt`
- Create: `rayneo-x3pro-semantic-control/app/src/main/java/dev/wenyu/semanticcontrol/app/CameraFeasibilityActivity.kt`
- Create: `rayneo-x3pro-semantic-control/app/src/main/res/layout/activity_camera_feasibility.xml`
- Modify: `rayneo-x3pro-semantic-control/app/src/main/java/dev/wenyu/semanticcontrol/app/HomepageDebugCommandRouter.kt`
- Modify: `rayneo-x3pro-semantic-control/app/src/main/java/dev/wenyu/semanticcontrol/app/MainActivity.kt`
- Modify: `rayneo-x3pro-semantic-control/app/src/main/AndroidManifest.xml`
- Modify: `rayneo-x3pro-semantic-control/app/src/main/res/values/strings.xml`
- Modify: `rayneo-x3pro-semantic-control/docs/PRODUCT_STRATEGY.md`
- Modify: `rayneo-x3pro-semantic-control/docs/IMPLEMENTATION_PLAN.md`
- Modify: `rayneo-x3pro-semantic-control/docs/PROGRESS.md`
- Test: `rayneo-x3pro-semantic-control/app/src/test/java/dev/wenyu/semanticcontrol/feature/camera/CameraFeasibilitySessionTrackerTest.kt`
- Test: `rayneo-x3pro-semantic-control/app/src/test/java/dev/wenyu/semanticcontrol/app/HomepageDebugCommandRouterTest.kt`

- [x] Add a foreground-only binocular camera probe host without changing the homepage product CTA surface
- [x] Add unit-tested session tracking for opening state, first-frame latency, analyzed-frame count, and error reporting
- [x] Keep the spike explicitly scoped away from background residency, MediaPipe integration, multi-gesture vocabulary, HUD polish, and cross-app control
- [ ] Answer one narrow question only on physical hardware: can a standard third-party X3 Pro app, while foregrounded inside the current binocular shell, access a usable camera stream that actually shows the wearer’s hand well enough for a later single-gesture semantic prototype?
- [ ] Define success as: public foreground camera access works, frames are lifecycle-stable for a short session, and the hand is visible enough to justify later egocentric gesture inference
- [ ] Define failure as: camera access is blocked/fragile, hand visibility is physically unusable, or the spike only works through private/vendor-only assumptions
- [ ] Route success to a follow-up `single-gesture foreground recognizer` spike; route failure to OEM/preload dependency escalation instead of more app-side CV work

## Task 5: App Audit and Whitelist Strategy

**Files:**
- Create: `rayneo-x3pro-semantic-control/docs/app-audit.md`
- Create: `rayneo-x3pro-semantic-control/app/src/main/java/dev/wenyu/semanticcontrol/core/contracts/AppSupportTier.kt`
- Test: `rayneo-x3pro-semantic-control/app/src/test/java/dev/wenyu/semanticcontrol/core/contracts/AppSupportTierTest.kt`

- [x] Define target app categories for MVP evaluation
- [ ] Audit accessibility tree quality and action reachability
- [ ] Tag apps as `generic`, `whitelist-candidate`, or `unsupported`
- [x] Document whitelist triggers and non-goals

## Task 7: Device Debugging Harness

**Files:**
- Create: `rayneo-x3pro-semantic-control/app/src/main/java/dev/wenyu/semanticcontrol/feature/semantic/debug/SemanticDebugCommandRouter.kt`
- Create: `rayneo-x3pro-semantic-control/app/src/main/java/dev/wenyu/semanticcontrol/feature/semantic/debug/SemanticDebugReceiver.kt`
- Modify: `rayneo-x3pro-semantic-control/app/src/main/java/dev/wenyu/semanticcontrol/feature/semantic/SemanticAccessibilityService.kt`
- Modify: `rayneo-x3pro-semantic-control/app/src/main/java/dev/wenyu/semanticcontrol/feature/semantic/FocusTraversalPlanner.kt`
- Modify: `rayneo-x3pro-semantic-control/app/src/main/java/dev/wenyu/semanticcontrol/feature/semantic/SemanticNavigator.kt`
- Modify: `rayneo-x3pro-semantic-control/app/src/main/AndroidManifest.xml`
- Test: `rayneo-x3pro-semantic-control/app/src/test/java/dev/wenyu/semanticcontrol/feature/semantic/debug/SemanticDebugCommandRouterTest.kt`
- Test: `rayneo-x3pro-semantic-control/app/src/test/java/dev/wenyu/semanticcontrol/feature/semantic/FocusTraversalPlannerTest.kt`

- [x] Add an explicit ADB-triggerable debug command bridge for dump and semantic actions
- [x] Record first live-device successes, failures, and command recipes in `docs/adb-debug-log.md`
- [x] Fix the false assumption that `isFocusable` implies support for `ACTION_FOCUS`
- [ ] Expand device-side snapshot detail if the next audit round still lacks enough evidence

## Task 6: RayNeo X3 Pro Vendor Adaptation Boundary

**Files:**
- Create: `rayneo-x3pro-semantic-control/docs/RAYNEO_SDK_ADAPTATION.md`
- Modify: `rayneo-x3pro-semantic-control/README.md`
- Modify: `rayneo-x3pro-semantic-control/docs/IMPLEMENTATION_PLAN.md`

- [x] Reverse inspect local `RayNeoIPCSDK` and `MercuryAndroidSDK` AARs
- [x] Bucket vendor capabilities into MVP / prototype / defer groups
- [x] Introduce an internal vendor adapter package boundary in code
- [x] Bootstrap Mercury through an internal runtime wrapper and manifest handshake
- [x] Prototype `TouchDispatcherX3 + CommonTouchCallback` as an X3-native input shim
- [ ] Verify whether `FocusTracker / RecyclerViewFocusTracker` materially improve on the existing accessibility-first focus model after target-app audit
