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
- 写入产品策略、实施计划和项目 README

### 未完成

- 真正的 pinch 输入接入
- App 审计与白名单策略

说明：当前剩余待办已收敛为 pinch 接入与 App 审计两大块。

### 当前判断

- 路线仍然维持：`MVP = Accessibility-first + pinch confirm + focus navigation`
- 工程结构收敛为：`单模块 + 包级分层`
- 不扩张到全局空气鼠标
- 不提前绑定厂商私有深度接口
- 不在没有 App 审计结果前承诺“全局支持”
