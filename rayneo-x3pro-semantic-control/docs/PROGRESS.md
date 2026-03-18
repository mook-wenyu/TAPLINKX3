# 最新进度

## 2026-03-17

### 已完成

- 新建独立 Android 项目 `rayneo-x3pro-semantic-control/`
- 反思当前阶段需求后，将工程从多模块收敛为单 `app` 模块
- 在单模块内保留包级分层，先固定 `SemanticAction`、`GestureSignal`、`FocusCandidate`
- 建立 `SemanticAccessibilityService` 骨架
- 建立 `PinchConfirmationStateMachine` 骨架
- 建立 `FocusHudOverlayController` 骨架
- 收紧权限边界，移除当前 MVP 尚未使用的相机/前台服务/通知/悬浮层权限
- 生成新项目本地 Gradle wrapper
- 完成单模块重构后的 `:app:assembleDebug` 构建验证
- 实现真正的 `FocusNext / FocusPrevious` 遍历逻辑
- 将 `ActivateFocused` 收紧为“基于当前焦点及其可点击祖先”的安全激活路径
- 为遍历决策增加本地单测，并通过 `testDebugUnitTest`
- 实现 `pinch -> activate focused target` 的最小适配层
- 为 pinch-confirm 路径增加本地单测，并验证不会在未确认或 cooldown 状态下重复触发
- 完成对 `RayNeoIPCSDK` 与 `MercuryAndroidSDK` 的本地反编译级接口检查
- 确认 `MercuryAndroidSDK` 更接近输入/焦点/UI 适配层，`RayNeoIPCSDK` 更接近系统服务/IPC 层
- 对比 `MercuryAndroidSDK` v0.2.2 与 v0.2.5，确认关键公开 API 基本稳定，且新版新增 `FocusConfig` / `FocusViewHandle` 焦点相关信号
- 建立 `vendor.rayneo` 包边界，确保 `com.ffalcon*` 只在适配层内出现
- 接入 `MercurySDK.init(...)` 与 RayNeo manifest handshake，并保持主线单测/构建通过
- 实现 `TouchDispatcherX3 + CommonTouchCallback` 到内部语义动作的最小输入适配层
- 保持 `TouchDispatcherX3` 只经由 `vendor.rayneo` 包进入主线，不改写现有语义导航模型
- 为输入映射 bridge 增加本地单测，并通过主线单测和 `assembleDebug`
- 同步 `docs/IMPLEMENTATION_PLAN.md` 与启动页文案，明确下一步是目标 App 审计、`FocusTracker / RecyclerViewFocusTracker` 判断与 `TouchDispatcherX3` 真机验证，而不是继续扩张新 feature
- 写入产品策略、实施计划和项目 README

### 未完成

- App 审计与白名单策略
- 是否需要 `FocusTracker / RecyclerViewFocusTracker` 的真机判断
- 真实相机手势 provider 接入

说明：当前 feature work 已完成到 MVP 所需的确认链路、Mercury bootstrap 和 `TouchDispatcherX3` 输入 spike。下一步不应继续扩张输入 feature，而应先做 App 审计，并判断是否真的需要 vendor 焦点辅助。

### 当前判断

- 路线仍然维持：`MVP = Accessibility-first + pinch confirm + focus navigation`
- 工程结构收敛为：`单模块 + 包级分层`
- 不扩张到全局空气鼠标
- 不提前绑定厂商私有深度接口
- 不在没有 App 审计结果前承诺“全局支持”
- 在 App 审计前，不继续扩张新的交互 feature。
- `MercuryAndroidSDK` 可作为 X3-native input shim 候选，但不应上升为应用主架构。
- `RayNeoIPCSDK` 暂不进入 MVP 主路径。
