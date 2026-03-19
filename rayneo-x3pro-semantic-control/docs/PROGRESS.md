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
- 曾在 `TouchDispatcherX3` 输入 spike 之后把计划同步成“App 审计优先”；后续路线复核与设备证据表明，这个顺序已经偏离北极星的最大不确定性，需要收回
- 新增 `docs/app-audit.md`，固化第一轮目标 App 类别、审计维度、支持分层与 `FocusTracker` 试验门槛
- 新增 `AppSupportTier` / `AppAuditSnapshot` 纯领域类型与单测，把 `generic / whitelist-candidate / unsupported` 判断收敛为可测试规则
- 新增 ADB 调试控制面：可通过显式 foreground broadcast 触发 `dump-root`、`focus-next`、`focus-previous`、`activate-focused`
- 完成第一轮真机调试，并在 `docs/adb-debug-log.md` 记录成功路径、失败路径、可复用命令与系统设置首页的首个审计样本
- 修复一个真机暴露出来的焦点遍历问题：不再把 `isFocusable` 误判为支持 `ACTION_FOCUS`
- 完成首页入口产品决策收敛：当前只推荐一个围绕无障碍服务的主状态卡，不建议提前加入悬浮窗主开关
- 将首页从静态说明页替换为单卡 AR 引导页，加入 `未启用 / 已开启 / 需检查` 三态与无障碍设置跳转
- 为首页服务状态解析新增本地单测，并通过主线单测与 `assembleDebug`
- 基于 `能力介绍 _ API.pdf`、Mercury 样例和外部资料完成路线复核：对 X3 Pro 来说，用户可见壳层应优先走原生合目双屏 + 镜腿手势，而不是继续沿单屏手机式外壳扩张
- 将首页壳层迁移为 `BaseMirrorActivity` 驱动的 Mercury 合目双屏实现，并保留现有单卡内容模型
- 为首页新增镜腿手势路由单测；主线测试和 `assembleDebug` 已通过，真机已完成无崩溃启动验证
- 为首页补充 ADB 调试入口，并完成 `click -> 无障碍设置页`、`double-click -> 返回 Launcher` 的真机验证
- 完成无障碍启用链阶段性审计：已确认“首页 -> 系统无障碍设置页”可达，但尚不能证明用户能仅靠镜腿手势完成系统设置内的完整开启流程
- 明确下一步不是再猜启用链，而是扩展 settings 页语义快照细节，用更细粒度证据验证镜腿端到端可操作性
- 真机复测表明：当顶部 Activity 切到 `com.android.settings` 后，Mercury/系统会强制停掉第三方应用进程，导致 `SemanticAccessibilityService` 失活；因此当前应用内语义辅助无法继续陪跑系统设置流程
- 完成替代启用链策略矩阵：当前默认路线调整为 `Accessibility = enhancement-only`，OEM/预装维持为次级高价值路线，手机伴生端仅保留为探索项
- 已将替代启用链策略翻译成明确后续任务：可选无障碍模式状态机、OEM/预装可行性跟踪、手机伴生端探索边界
- 实现可选无障碍模式状态机：首页现在会持久化 `native-only / accessibility-enhanced` 模式，并在服务失活后回落为诚实的恢复状态而不是假定“必须先开启无障碍”
- 为可选无障碍模式新增本地单测，并通过主线单测与 `assembleDebug`
- 真机已确认 `native-only` 默认态；增强态与恢复态的设备侧更细观察仍受 Mercury 前台/杀进程时序影响，后续继续补证据
- 明确把“无外设 + 摄像机视觉 + 第三方后台常驻 + 手势控制第三方应用”重新写回北极星目标；当前 X3 壳层/镜腿/Accessibility 工作被重新定位为近期交付轨道与基础设施
- 补充最新公开技术约束：MediaPipe/on-device 手势仍是现实候选，但 Android 背景摄像头与 camera foreground service 限制意味着标准第三方 app 不能把“长期后台常驻摄像机视觉”直接当成近期默认主线
- 已把下一条合理任务正式收敛为 `camera-vision feasibility spike`：先验证前台 camera feed 与手在画面中的可用性，而不是直接承诺后台常驻视觉手势
- 写入产品策略、实施计划和项目 README
- 完成路线纠偏后的第一步实现：新增前台双屏 `camera feasibility probe` 宿主页，不对首页产品 CTA 扩张新主流程
- 新增 `CameraFeasibilitySessionTracker` 与对应单测，把相机探针的打开态、首帧时延、已分析帧数与错误状态收敛为可测试纯逻辑
- 为首页调试路由新增 `open-camera-probe` 内部命令，让下一轮真机调试可以直接从当前双屏壳层跳入相机探针页
- 新增 `CAMERA` 权限和可选 `camera.any` feature 声明，但仍然明确限制在前台 activity 探针，不引入前台服务或后台常驻承诺
- 本轮再次执行 `adb kill-server && adb start-server && adb devices -l`，结果仍然没有在线设备，因此 `Task 15` 的真机取流验证尚未开始
- 后续设备重新连上后，已完成 `Task 15` 真机验证：debug APK 安装成功、`open-camera-probe` 广播成功拉起 `CameraFeasibilityActivity`、前台取流稳定进入 `Streaming`
- 首轮取流日志显示 `cameraId=0`、`1280x720`、首帧约 `436ms`，分析帧数增长到 `2130+`，未见 `camera-error` 或 session 失败信号
- 截图 `rayneo-camera-probe.png` 显示了真实桌面/笔记本场景和进入画面的手部，说明“手是否在画面里”这个关键问题得到正向证据
- 退出后再次打开 probe 也成功从 `Idle -> Opening -> Streaming` 重新取流，重开首帧约 `395ms`，说明前台短时生命周期稳定性达到下一步实验要求
- 记录到一个截图层面的 caveat：重开后的第一张即时截图偏黑，但延时截图恢复正常 live preview，因此后续判断应以 `CameraFeasibility` 日志为主、截图为辅

### 未完成

- App 审计与白名单策略
- 是否需要 `FocusTracker / RecyclerViewFocusTracker` 的真机判断
- 相机 hand-tracking provider 是否仍有必要的复盘判断
- 系统无障碍设置页的端到端镜腿可操作性验证
- 替代无障碍启用链方案评估（OEM / 伴生端 / 预装）
- `accessibility-enhanced` 与 `needs-attention` 的更细真机状态可视化验证
- `single-gesture foreground recognizer spike`

说明：当前 feature work 已完成到 MVP 所需的确认链路、Mercury bootstrap、`TouchDispatcherX3` 输入 spike，以及 `Task 15` 的前台 camera feasibility 设备验证。下一步不再是证明“相机能不能开起来”，而是收紧到 `single-gesture foreground recognizer spike`：只验证一个最小手势语义能否在当前前台流里稳定成立；App 审计继续并行，但不再阻塞这条前台视觉主线。

### 当前判断

- 北极星目标：`无外设 + 摄像机视觉 + 第三方后台常驻 + 手势控制第三方应用`
- 近期可交付轨道：`X3-native binocular shell + temple gestures + Accessibility enhancement-only`
- 工程结构收敛为：`单模块 + 包级分层`
- 不扩张到全局空气鼠标
- 不提前绑定厂商私有深度接口
- 不在没有 App 审计结果前承诺“全局支持”
- 不在没有前台 camera probe 设备证据前承诺“摄像机视觉可行”
- App 审计继续并行，但不再阻塞当前 camera feasibility spike。
- 当前可以诚实地说：前台第三方相机流 + 手进入画面已经被设备验证，但后台常驻视觉仍未被证明。
- `MercuryAndroidSDK` 可作为 X3-native input shim 候选，但不应上升为应用主架构。
- `RayNeoIPCSDK` 暂不进入 MVP 主路径。
