# ADB Debug Log

## 2026-03-18 - RayNeo X3 Pro first live-device session

### Device facts

- Device detected by `adb devices -l`
- Manufacturer: `RayNeo`
- Model: `ARGF20`
- Android: `12`
- Fingerprint: `RayNeo/RayNeoX3Pro/MercuryLiteXR:12/SKQ1.250204.001/2BA4:user/release-keys`

### What succeeded

1. **ADB connectivity works**
   - The glasses are reachable and stable over ADB.
2. **Debug APK install works**
   - `adb install -r .../app-debug.apk` succeeded.
3. **Accessibility service can be enabled over ADB**
   - After the app is installed, the following writes succeed:
     - `settings put secure accessibility_enabled 1`
     - `settings put secure enabled_accessibility_services dev.wenyu.semanticcontrol.debug/dev.wenyu.semanticcontrol.feature.semantic.SemanticAccessibilityService`
4. **Service binding works on device**
   - `dumpsys accessibility` shows the service as both enabled and bound.
5. **ADB debug command bridge works**
   - Explicit foreground broadcasts to `SemanticDebugReceiver` succeed.
   - `dump-root` returns structured data for the current active window.

### What failed

1. **Implicit debug broadcasts fail when the app is backgrounded**
   - Symptom: `Background execution not allowed` in logcat.
   - Root cause: Android 12 background broadcast restrictions.
   - Working path: use an explicit component name plus `--receiver-foreground`.
2. **The first debug command after reinstall can race service binding**
   - Symptom: `service-unavailable`.
   - Root cause: the receiver can run before `SemanticAccessibilityService` has finished binding.
   - Working path: wait for `Semantic accessibility service connected` before issuing semantic debug commands.
3. **System Settings homepage currently fails semantic navigation and activation**
   - `focus-next` returned `focus-next:failed`
   - `activate-focused` returned `activate-focused:failed`

### Code-side fix made during this session

We added an ADB-driven debug control path:

- `app/src/main/java/dev/wenyu/semanticcontrol/feature/semantic/debug/SemanticDebugCommandRouter.kt`
- `app/src/main/java/dev/wenyu/semanticcontrol/feature/semantic/debug/SemanticDebugReceiver.kt`

We also fixed one real traversal bug exposed by device debugging:

- Previous behavior treated `node.isFocusable` as if it implied support for `ACTION_FOCUS`.
- That assumption is false on real devices.
- Fix: split `supportsInputFocusAction` from plain `isFocusable`, and only request focus when the action is actually supported.

Relevant files:

- `app/src/main/java/dev/wenyu/semanticcontrol/feature/semantic/FocusTraversalPlanner.kt`
- `app/src/main/java/dev/wenyu/semanticcontrol/feature/semantic/SemanticNavigator.kt`
- `app/src/test/java/dev/wenyu/semanticcontrol/feature/semantic/FocusTraversalPlannerTest.kt`

### Current audit evidence: Settings homepage

- Package: `com.android.settings`
- Screen: `Settings homepage`
- `dump-root` result:
  - `root=com.android.settings/android.widget.FrameLayout`
  - `a11yFocus=null:null`
  - `inputFocus=android.widget.LinearLayout:null`
- `uiautomator dump` shows a readable tree with many clickable list rows.
- However, current MVP semantic commands still fail on this screen:
  - `focus-next:failed`
  - `activate-focused:failed`

### Current interpretation

- **tree quality**: `Good`
  - The node tree is readable and structurally rich.
- **focusability**: `Poor`
  - Current semantic focus movement cannot obtain a usable next target on this screen.
- **activation quality**: `Poor`
  - Current focused target does not activate through the existing focused-click path.
- **support tier for this screen with current MVP**: `Unsupported`

This is a **screen-level** finding, not yet a whole-app judgment for all of Settings.

### Commands that worked reliably

```bash
adb shell am broadcast \
  --receiver-foreground \
  -n dev.wenyu.semanticcontrol.debug/dev.wenyu.semanticcontrol.feature.semantic.debug.SemanticDebugReceiver \
  -a dev.wenyu.semanticcontrol.debug.ACTION_COMMAND \
  --es command dump-root
```

```bash
adb shell am broadcast \
  --receiver-foreground \
  -n dev.wenyu.semanticcontrol.debug/dev.wenyu.semanticcontrol.feature.semantic.debug.SemanticDebugReceiver \
  -a dev.wenyu.semanticcontrol.debug.ACTION_COMMAND \
  --es command focus-next
```

```bash
adb shell am broadcast \
  --receiver-foreground \
  -n dev.wenyu.semanticcontrol.debug/dev.wenyu.semanticcontrol.feature.semantic.debug.SemanticDebugReceiver \
  -a dev.wenyu.semanticcontrol.debug.ACTION_COMMAND \
  --es command activate-focused
```

### Next recommended device steps

1. Audit a more standard list-detail screen where `ACTION_FOCUS` may be better supported.
2. Audit a form/search screen to compare focus and activation behavior.
3. If multiple high-value screens keep showing `tree=Good` but `focusability=Poor`, then evaluate `FocusTracker / RecyclerViewFocusTracker` with the same ADB debug harness.
