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

## 2026-03-18 - Binocular homepage shell verification

### What changed before verification

- The homepage shell moved onto Mercury's mirrored activity path.
- A lightweight homepage debug receiver was added so ADB can simulate `click` and `double-click` without waiting for manual temple input.

Relevant files:

- `app/src/main/java/dev/wenyu/semanticcontrol/app/MainActivity.kt`
- `app/src/main/java/dev/wenyu/semanticcontrol/app/HomepageTempleActionRouter.kt`
- `app/src/main/java/dev/wenyu/semanticcontrol/app/HomepageDebugCommandRouter.kt`
- `app/src/main/java/dev/wenyu/semanticcontrol/app/HomepageDebugReceiver.kt`

### Verified behavior

1. **Homepage launch is stable enough for continued work**
   - The mirrored homepage launches on device without `AndroidRuntime` / `FATAL` crash logs.
2. **Homepage click path works**
   - ADB command:
     ```bash
     adb shell am broadcast --receiver-foreground \
       -n dev.wenyu.semanticcontrol.debug/dev.wenyu.semanticcontrol.app.HomepageDebugReceiver \
       -a dev.wenyu.semanticcontrol.debug.ACTION_HOMEPAGE_COMMAND \
       --es command click
     ```
   - Result: `click:ok`
   - Foreground activity changed to `com.android.settings/.Settings$AccessibilitySettingsActivity`
3. **Homepage double-click exit works**
   - ADB command:
     ```bash
     adb shell am broadcast --receiver-foreground \
       -n dev.wenyu.semanticcontrol.debug/dev.wenyu.semanticcontrol.app.HomepageDebugReceiver \
       -a dev.wenyu.semanticcontrol.debug.ACTION_HOMEPAGE_COMMAND \
       --es command double-click
     ```
   - Result: `double-click:ok`
   - Foreground activity returned to `com.ffalconxr.mercury.launcher/.home.LauncherHomeActivity`

### Important findings

1. **The previous inability to verify homepage temple actions was a tooling gap, not a product failure**
   - The original accessibility-service debug bridge cannot validate homepage actions because it targets `SemanticAccessibilityService`, not the homepage activity.
   - A separate homepage debug receiver solved this.
2. **Homepage `click` and `double-click` behavior now have direct device evidence**
   - We no longer have to infer these paths from code alone.
3. **`uiautomator dump` remains weak evidence for mirrored UI inspection**
   - For the mirrored homepage shell, it still does not give a reliable binocular-specific view of the page.
   - It is usable for coarse presence checks, but not enough for left/right mirrored validation.

### Notes / caveats

- During verification, a `WindowManager` log mentioned `WRITE_SECURE_SETTINGS` while the launcher took focus back. This did not block the homepage double-click exit path and did not appear as an app crash.
- Earlier `app died, no saved state` lines observed around install/replace were associated with package replacement and process restart, not a reproducible homepage runtime crash.

### Updated next recommended device steps

1. Keep the homepage debug receiver for device verification of the shell layer.
2. Use the same ADB pattern on the next binocular pages before trusting temple-action behavior.
3. Continue with target-app audit now that homepage shell startup, click, and double-click are verified.

## 2026-03-18 - Accessibility enable path audit

### Verified

1. Homepage `click` opens Android accessibility settings on device.
2. Homepage `double-click` exits the homepage and returns to the RayNeo launcher.

### Not yet verified

The full user-driven enable path inside Android accessibility settings is still unproven.

We have not yet demonstrated, with temple gestures only, that a user can:

1. navigate the accessibility settings list,
2. enter the app's service detail page,
3. switch the service on,
4. complete any required confirmation dialogs.

### Current conclusion

- `Homepage -> Settings` handoff is valid.
- `Settings -> fully enabled service by temple gestures only` remains a high-risk / pending path.
- Product docs should no longer treat manual settings enablement as an already validated mainstream onboarding path.

### Additional evidence gathered later the same day

- Re-enabling the service via ADB confirms that the service can still bind correctly before settings-page tests.
- Public attempts to land directly on an app-specific accessibility detail page did not yield a reliable deeper entry point; the system continued resolving to generic Settings.
- Current `SemanticDebugReceiver` instrumentation is still too coarse for end-to-end enablement proof on the settings side:
  - it can prove page handoff,
  - but it cannot yet explain, step by step, which list item / dialog / toggle is currently being targeted.

### Refined conclusion

- The missing proof is no longer “can we open settings?” — that is already solved.
- The missing proof is “can we observe and complete the settings flow with enough fidelity to trust temple-only enablement?”
- The smallest justified next step is richer semantic snapshot detail for settings-page audits, not a brand-new debug subsystem.

## 2026-03-18 - Settings-side process death finding

### What was tested

1. Re-enable the accessibility service via ADB and confirm it is bound.
2. Launch the app homepage and wait until `Semantic accessibility service connected` appears.
3. Open Android Settings from the app flow.
4. Observe process/activity logs while attempting to continue semantic inspection.

### What happened

- The service did bind correctly before entering Settings.
- After the top activity switched to `com.android.settings`, Mercury/system logs showed:
  - `forceStopPackage dev.wenyu.semanticcontrol.debug`
  - `Force stopping dev.wenyu.semanticcontrol.debug`
  - the app process was killed,
  - then later restarted only when explicitly relaunched.
- Once the process died, `SemanticDebugReceiver` returned `service-unavailable`, because the service was no longer active in-process.

### Interpretation

- This is a stronger blocker than coarse settings-page snapshots alone.
- Even if the settings tree itself is readable, the current third-party app process does not remain alive reliably after switching into system Settings.
- Therefore, the existing architecture cannot assume that the app will keep assisting the user throughout the Settings enable flow.

### Updated conclusion

- `Homepage -> Settings` is verified.
- `App remains alive and continues semantic assistance inside Settings` is disproven under the current device/system behavior.
- The enablement problem is now partially architectural/platform-level, not just a missing focus rule.

## 2026-03-19 - Camera feasibility spike preflight (not yet device-verified)

### What changed before the next device round

- Added a foreground-only binocular camera probe activity:
  - `app/src/main/java/dev/wenyu/semanticcontrol/app/CameraFeasibilityActivity.kt`
- Added a pure session tracker for first-frame latency, analyzed-frame count, and error reporting:
  - `app/src/main/java/dev/wenyu/semanticcontrol/feature/camera/CameraFeasibilitySessionTracker.kt`
- Added an internal homepage debug command to launch the probe without changing the user-facing homepage CTA:
  - `app/src/main/java/dev/wenyu/semanticcontrol/app/HomepageDebugCommandRouter.kt`
- Added `CAMERA` manifest permission for the spike host activity only.

### Commands prepared for the next device round

```bash
adb shell am broadcast --receiver-foreground \
  -n dev.wenyu.semanticcontrol.debug/dev.wenyu.semanticcontrol.app.HomepageDebugReceiver \
  -a dev.wenyu.semanticcontrol.debug.ACTION_HOMEPAGE_COMMAND \
  --es command open-camera-probe
```

```bash
adb shell pm grant dev.wenyu.semanticcontrol.debug android.permission.CAMERA
```

### Important note

- These commands were prepared after the code spike, but they are **not yet verified on device**.
- Do not treat this section as evidence that camera access already works; it only records the exact preflight path for the next session.
- During this session, `adb kill-server && adb start-server && adb devices -l` still returned **no connected devices**, so the probe could not be run on hardware yet.

## 2026-03-19 - Camera feasibility spike device validation

### Device state

- `adb devices -l` returned a live X3 Pro device:
  - serial: `BC942490E32E972`
  - model: `ARGF20`
  - product: `RayNeoX3Pro`

### Commands that succeeded

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

```bash
adb shell pm grant dev.wenyu.semanticcontrol.debug android.permission.CAMERA
```

```bash
adb shell am start -n dev.wenyu.semanticcontrol.debug/dev.wenyu.semanticcontrol.app.MainActivity
```

```bash
adb shell am broadcast --receiver-foreground \
  -n dev.wenyu.semanticcontrol.debug/dev.wenyu.semanticcontrol.app.HomepageDebugReceiver \
  -a dev.wenyu.semanticcontrol.debug.ACTION_HOMEPAGE_COMMAND \
  --es command open-camera-probe
```

Result:

- broadcast returned `result=0, data="open-camera-probe:ok"`
- `ActivityTaskManager` logged launch of `dev.wenyu.semanticcontrol.debug/dev.wenyu.semanticcontrol.app.CameraFeasibilityActivity`

### Foreground stream evidence

Logcat showed stable foreground streaming from the probe:

- status: `Streaming`
- camera: `cameraId=0`
- resolution: `1280x720`
- first frame latency, first run: about `436ms`
- analyzed frames on first long run: grew past `2130`

Representative log line:

```text
I CameraFeasibility: Camera probe snapshot: CameraFeasibilitySnapshot(status=Streaming, cameraId=0, resolution=CameraProbeResolution(width=1280, height=720), analyzedFrames=2130, firstFrameLatencyMs=436, ...)
```

### Hand-in-frame evidence

- Screenshot `rayneo-camera-probe.png` showed:
  - the probe overlay text rendered correctly,
  - a live desk/laptop scene behind it,
  - a clearly visible hand entering the lower center-right of the frame.

This is enough evidence to say the wearer’s hand can enter the usable foreground camera view for a later single-gesture prototype.

### Reopen / short-session lifecycle evidence

After exiting the probe and reopening it:

- the activity relaunched successfully,
- the probe moved through `Idle -> Opening -> Streaming`,
- first frame latency on reopen was about `395ms`,
- analyzed frames continued growing (`1`, `30`, `60`, ... `1230`) without `camera-error`, `session-config-failed`, or `preview-surfaces-unavailable`.

Representative reopen log lines:

```text
I CameraFeasibility: Camera probe snapshot: CameraFeasibilitySnapshot(status=Opening, cameraId=0, resolution=CameraProbeResolution(width=1280, height=720), analyzedFrames=0, firstFrameLatencyMs=null, ...)
I CameraFeasibility: Camera probe snapshot: CameraFeasibilitySnapshot(status=Streaming, cameraId=0, resolution=CameraProbeResolution(width=1280, height=720), analyzedFrames=1, firstFrameLatencyMs=395, ...)
```

### Screenshot caveat

- The first immediate screenshot taken after reopen captured only a dark/status-bar-dominant frame.
- A delayed screenshot taken a few seconds later again showed the live camera scene and visible hand.
- Interpretation: screenshot timing around reopen is less stable than log evidence, so stream validity should be judged primarily by `CameraFeasibility` logs and secondarily by screenshots.

### Conclusion

- **Foreground camera access works** for the current X3 Pro third-party binocular-shell app.
- **Short-session lifecycle stability is good enough** for the next prototype step.
- **Hand visibility is sufficient** to justify a tightly scoped follow-up foreground recognizer task.
- This does **not** prove background-resident camera viability.
