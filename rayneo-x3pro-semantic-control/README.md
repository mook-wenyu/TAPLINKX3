# RayNeo X3 Pro Semantic Control

独立 Android 原生项目骨架，目标设备为 `RayNeo X3 Pro`。

当前产品方向：

- 短期：`Accessibility-first + pinch confirm + focus navigation`
- 长期：`全局语义控制 + 白名单 App 深适配`

当前版本已经完成单模块工程骨架、语义焦点遍历、`pinch -> activate focused target` 的最小确认适配层、第一版 `RayNeo vendor adapter boundary + Mercury bootstrap`，以及 `TouchDispatcherX3` 到内部语义动作的最小输入 spike。

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

1. 在真机上审计首批目标 App 的 accessibility tree、focusability 与 activation 质量。
2. 基于审计结果决定是否真的需要 `FocusTracker / RecyclerViewFocusTracker`，而不是先把 vendor 焦点体系并入主线。
3. 在完成审计前，不扩张到 `RingIPCHelper`、原始 `IRemoteService` 消息协议、多手势词汇、相机 provider 或白名单深适配实现。

审计基线与支持分层规则见：`docs/app-audit.md`。
ADB 真机调试记录与已知设备经验见：`docs/adb-debug-log.md`。

## 构建

工程是独立单模块 Gradle 项目，根目录在：`rayneo-x3pro-semantic-control/`。

直接使用该目录下的 Gradle wrapper：`./gradlew` 或 `gradlew.bat`。
