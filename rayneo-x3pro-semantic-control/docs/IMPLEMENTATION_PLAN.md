# RayNeo X3 Pro Semantic Control Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build an independent Android-native project for RayNeo X3 Pro that starts with accessibility-first semantic control and evolves toward global semantic control with whitelisted app deep adaptation.

**Architecture:** Use a single Android app module with package-level layering for contracts, gesture, semantic execution, and focus HUD feedback. Keep the MVP centered on semantic focus traversal and explicit pinch confirmation instead of spatial free-click, and avoid premature Gradle modularization.

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
- [ ] Implemented camera-backed gesture provider pipeline
- [ ] Completed target app accessibility audit

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
- [ ] Integrate a camera-backed gesture provider behind an interface
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

## Task 5: App Audit and Whitelist Strategy

**Files:**
- Create: `rayneo-x3pro-semantic-control/docs/app-audit.md`
- Create: `rayneo-x3pro-semantic-control/app/src/main/java/dev/wenyu/semanticcontrol/core/contracts/AppSupportTier.kt`

- [ ] Define target app categories for MVP evaluation
- [ ] Audit accessibility tree quality and action reachability
- [ ] Tag apps as `generic`, `whitelist-candidate`, or `unsupported`
- [ ] Document whitelist triggers and non-goals
