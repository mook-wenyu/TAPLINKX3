# Findings

## Product findings

- The MVP problem is cross-app semantic control, not free-pointing.
- Focus navigation plus explicit confirmation is a safer mental model than air mouse.
- Whitelist deep adaptation is a later layer, not a prerequisite for the first prototype.

## Platform findings

- Android Accessibility APIs provide a legitimate semantic action layer.
- MediaPipe-style hand tracking is available on Android, but device/runtime exposure remains a separate gate.
- Vendor XR SDK signals exist, but should not be treated as the initial dependency boundary.
- `MercuryAndroidSDK` looks like the X3-native input/focus/mirroring layer.
- `RayNeoIPCSDK` looks like a service/IPC/speech/GPS/ring integration layer with string-message risk.
- `MercuryAndroidSDK` v0.2.5 keeps the core public surface stable and adds focus-related signals, which increases confidence in a vendor-adapter-first path.
- Android official foreground-service guidance treats `camera` as a while-in-use restricted capability, which reinforces that this project should validate foreground camera viability first rather than assume background residency is available.
- Public RayNeo developer pages remain thin/gated; the local `MercuryAndroidSample` is still the strongest concrete camera integration reference for X3-native app shape.

## Engineering findings

- Stable contracts should live outside app and service modules.
- Gesture, semantic execution, and overlay feedback should remain loosely coupled.
- Build verification must happen before claiming the scaffold is ready for feature work.
- The shipped app module had no camera permission, preview surface, or frame-analysis seam before this session; the feasibility spike therefore needs a first-class but tightly scoped probe host rather than an incremental tweak.
- `SemanticAccessibilityService.handleGestureSignal(...)`, `handleVendorMotionEvent(...)`, and `handleVendorKeyEvent(...)` currently expose future seams but are not wired end-to-end from a live producer, so the camera spike must not pretend those paths are already active.
