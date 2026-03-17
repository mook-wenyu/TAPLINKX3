# Findings

## Product findings

- The MVP problem is cross-app semantic control, not free-pointing.
- Focus navigation plus explicit confirmation is a safer mental model than air mouse.
- Whitelist deep adaptation is a later layer, not a prerequisite for the first prototype.

## Platform findings

- Android Accessibility APIs provide a legitimate semantic action layer.
- MediaPipe-style hand tracking is available on Android, but device/runtime exposure remains a separate gate.
- Vendor XR SDK signals exist, but should not be treated as the initial dependency boundary.

## Engineering findings

- Stable contracts should live outside app and service modules.
- Gesture, semantic execution, and overlay feedback should remain loosely coupled.
- Build verification must happen before claiming the scaffold is ready for feature work.
