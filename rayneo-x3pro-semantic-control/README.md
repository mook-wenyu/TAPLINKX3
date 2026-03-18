# RayNeo X3 Pro Semantic Control

独立 Android 原生项目骨架，目标设备为 `RayNeo X3 Pro`。

当前产品方向：

- 北极星目标：`无外设 + 基于摄像机视觉 + 第三方后台常驻 + 用手势控制第三方应用`
- 当前可交付轨道：`X3-native binocular UI + temple gestures`，`Accessibility` 作为增强层
- 长期：`全局语义控制 + 白名单 App 深适配`

当前版本已经完成单模块工程骨架、语义焦点遍历、`pinch -> activate focused target` 的最小确认适配层、第一版 `RayNeo vendor adapter boundary + Mercury bootstrap`，以及 `TouchDispatcherX3` 到内部语义动作的最小输入 spike。

这些能力不等于已经实现了北极星目标；它们更准确地说，是在当前 `RayNeo X3 Pro` 平台约束下，为未来摄像机视觉 / 后台常驻 / 跨第三方控制铺的设备壳层与调试底座。

当前首页也已经从静态说明页收敛为一个单卡 AR 引导页，只围绕无障碍服务状态与设置跳转展开，不提前暴露悬浮窗主开关。

当前首页还实现了一个可选无障碍模式状态机：默认以 `native-only` 运行；只有在服务真实可用时才提升到 `accessibility-enhanced`，并在服务失活后回到诚实的恢复状态。

基于 `RayNeo X3 Pro` 官方能力文档、Mercury 样例与最新路线复核，首页已经迁移到原生合目双屏壳层；当前它更适合作为状态入口和内部调试宿主，而不是继续扩张为多能力控制台。

同时也要明确：摄像机视觉手势仍然是北极星目标，但在标准第三方 Android app 上，后台摄像头与 camera foreground service 仍受平台强约束，所以当前近期轨道不会直接把“长期后台常驻摄像机视觉”当成已可交付主线。

最新的本地 SDK 逆向分析表明：

- `MercuryAndroidSDK` 更接近 X3 Pro 的输入 / 焦点 / 镜像 UI 适配层。
- `RayNeoIPCSDK` 更接近系统服务、speech、GPS、ring 外设相关的 IPC 层。

因此，下一阶段的“深度适配”重点不应是直接扩张通用相机手势 provider，而应先建立一个 **RayNeo vendor adapter boundary**，只把高价值、低耦合的 Mercury 输入能力引入项目。

出于 MVP 边界控制，当前清单里只保留无障碍服务接入所需的最小能力，尚未提前申请相机、悬浮层或前台服务权限。

## 工程结构

- 单 `app` 模块，避免在 MVP 阶段过早引入 Gradle 模块复杂度
- 包级分层：
  - `dev.wenyu.semanticcontrol.app`
  - `dev.wenyu.semanticcontrol.core.contracts`
  - `dev.wenyu.semanticcontrol.feature.gesture`
  - `dev.wenyu.semanticcontrol.feature.overlay`
  - `dev.wenyu.semanticcontrol.feature.semantic`
  - `dev.wenyu.semanticcontrol.vendor.rayneo`

## 当前边界

- 不做全局空气鼠标
- 不做任意屏幕坐标点击
- 不做复杂手势词汇
- 不以厂商私有接口为前置依赖

## 下一步

1. 先在真机上运行 `Task 15` 的前台相机可行性探针，验证前台取流、短时稳定性和手是否真的进入画面。
2. 将目标 App 审计继续作为并行证据轨道，而不是阻塞 camera feasibility spike 的硬门槛。
3. 基于相机探针和 App 审计的双证据，再决定是否真的需要 `FocusTracker / RecyclerViewFocusTracker` 或后续单手势识别切片。
4. 在前台相机可行性未验证前，不扩张到后台常驻宣称、MediaPipe 集成、多手势词汇、悬浮窗主开关或白名单深适配实现。
5. 无障碍服务当前不再作为量产启用链默认主路径，而作为增强层保留；量产替代方案优先级为：`enhancement-only` > `OEM/预装` > `手机伴生端` > `ADB仅开发用`。

审计基线与支持分层规则见：`docs/app-audit.md`。
ADB 真机调试记录与已知设备经验见：`docs/adb-debug-log.md`。

## 构建

工程是独立单模块 Gradle 项目，根目录在：`rayneo-x3pro-semantic-control/`。

直接使用该目录下的 Gradle wrapper：`./gradlew` 或 `gradlew.bat`。
