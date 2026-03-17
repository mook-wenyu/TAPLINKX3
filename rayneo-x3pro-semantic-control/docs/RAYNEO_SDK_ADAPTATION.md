# RayNeo X3 Pro SDK Adaptation Note

## 1. 结论先行

基于对本地 AAR 的直接解包、类表分析、`javap` 方法签名检查，以及现有开源项目与官方样例的真实用法追踪：

- `MercuryAndroidSDK` 应视为 **X3 Pro 的输入 / 焦点 / 镜像 UI 适配层**。
- `RayNeoIPCSDK` 应视为 **系统服务 / IPC / speech / GPS / ring 外设层**。

这两个 SDK 都不是“直接提供语义控制”的框架。

对 `rayneo-x3pro-semantic-control/` 来说，最安全的深度适配路线不是把它们变成应用主架构，而是：

1. 保持现有 `Accessibility-first + pinch confirm + focus navigation` 为主路径。
2. 只把 `MercuryAndroidSDK` 中与 X3 Pro 输入强相关的能力，作为 **hardware adaptation shim** 接入。
3. 把 `RayNeoIPCSDK` 放到更后面的可选适配层，除非真机验证证明它对产品价值有直接提升。

## 2. 直接证据

### 2.1 RayNeoIPCSDK

本地 AAR 反编译表明包名为 `com.ffalconxr.mercury.ipc`，关键公开类包括：

- `Launcher`
  - `connect()`
  - `disConnect()`
  - `request(String)`
  - `getSpeech()`
  - 响应与连接状态 listener
- `IRemoteService`
  - `register(callback, String)`
  - `request(Request)`
  - `unRegister(callback, String)`
- `Speech` / `ISpeechInterface`
  - `startDialog`
  - `stopDialog`
  - `startTTS`
  - `stopTTS`
  - `setAIUIListener`
- `GPSIPCHelper`
  - `registerGPSInfo(Context)`
  - `unRegisterGPSInfo(Context)`
- `RingIPCHelper`
  - ring IMU、long click、separate button、QR helper
- `TopPackageHelper`
  - top package 相关 helper

进一步检查常量值后，可以确认：

- `TopPackageHelper` 使用 `top_package / add_top_package / remove_top_package`
- `RingIPCHelper` 使用 `register_ring_info / enable_ring_imu / separate_button / send_login_qr` 等字符串协议

这说明 `RayNeoIPCSDK` 明显偏向字符串协议驱动的系统服务通道，而不是高层稳定业务 API。

### 2.2 MercuryAndroidSDK

本地 AAR 反编译表明包名为 `com.ffalcon.mercury.android.sdk`，关键公开类包括：

- `MercurySDK`
  - `init(Application)`
- `ITouchDispatcher`
  - `onMotionEvent(...)`
  - `onKeyEvent(...)`
  - `toggleClickSoundEffect(...)`
- `TouchDispatcherX3`
  - 内部支持 click / pinch / long pinch / double / triple 等输入路径
- `CommonTouchCallback`
  - `onTPClick()`
  - `onKeyPinch()`
  - `onKeyLongPinch()`
  - 多个 slide / move / double-finger 回调
- `TempleAction`
  - click / long click / slide / pinch 相关动作层级
- 焦点与镜像 UI 相关类
  - `FocusTracker`
  - `RecyclerViewFocusTracker`
  - `RecyclerViewTracker`
  - `BaseMirrorActivity`
  - `MirroringView`

这说明 `MercuryAndroidSDK` 更接近“X3 原生输入与 UI 行为抽象”。

## 3. 样例项目证明了什么

### 3.1 MercuryAndroidSample

本地样例显示：

- `MercuryDemoApplication` 直接调用 `MercurySDK.init(this)`。
- `AndroidManifest.xml` 使用 `<meta-data android:name="com.rayneo.mercury.app" android:value="true" />`。
- 主要 Activity 普遍继承 `BaseMirrorActivity`。
- 交互主模式不是手写 MotionEvent 翻译，而是：
  - `TempleActionViewModel`
  - `TempleAction`
  - `FixPosFocusTracker / FocusTracker / RecyclerViewFocusTracker`
  - `handleFocusTargetEvent(...)`

这说明官方推荐的 X3 / Mercury 交互方式，至少在 sample 里，是：

`Mercury bootstrap -> mirror activity -> temple gesture -> focus tracker`

### 3.2 IPCDemo

本地样例显示：

- 通过 `Launcher.getInstance(this)` 建 IPC 入口
- 注册 `Launcher.OnResponseListener`
- 使用 `RingIPCHelper.registerRingInfo(this)` 与 `GPSIPCHelper.registerGPSInfo(this)`
- 在回调里解析 `Response.getData()` 的 JSON 字符串

这说明 `RayNeoIPCSDK` 的真实使用方式是：

`Launcher + listener + JSON payload parsing + helper toggles`

它更像系统服务桥接，而不是输入控制主路径。

## 4. v0.2.2 与 v0.2.5 的差异

对比旧版 `v0.2.2` 与新版 `v0.2.5`：

- 关键公开类和方法签名基本稳定：
  - `MercurySDK`
  - `TouchDispatcherX3`
  - `CommonTouchCallback`
  - `TempleAction`
  - `FocusTracker`
  - `RecyclerViewFocusTracker`
  - `RecyclerViewTracker`
  - `BaseMirrorActivity`
- 两个版本的类数都为 `68`。
- 旧版独有类：
  - `MicAudioMode`
  - `KeyguardUtils`
  - `LedBroadcastUtils`
- 新版独有类：
  - `BindingPairExtensionsKt`
  - `FocusConfig`
  - `FocusViewHandle`

这说明新版 `MercuryAndroidSDK` 不是大规模 API 改版，更像是在焦点配置与视图处理上有增强信号。这对当前项目的语义焦点方向非常相关。

## 5. 能力分桶

### 5.1 MVP 可纳入

- `MercurySDK.init(Application)`
- RayNeo manifest handshake
- `TouchDispatcherX3`
- `CommonTouchCallback`
- vendor 事件到内部语义动作的映射层

### 5.2 原型验证后再决定

- `TempleAction`
- `FocusTracker`
- `RecyclerViewFocusTracker`
- `RecyclerViewTracker`
- `Launcher.getSpeech()`
- `Speech`

### 5.3 明确延后

- `IRemoteService`
- 原始 `Request` / `Response`
- `request(String)` 协议层直连
- `GPSIPCHelper`
- `RingIPCHelper`
- `TopPackageHelper`
- `BaseMirrorActivity`
- `MirroringView`

## 6. 推荐适配顺序

### 阶段 A：建立 vendor 边界

在 `rayneo-x3pro-semantic-control/` 内新增一个 RayNeo vendor adapter 层，要求：

- 所有 `com.ffalcon*` 引用都收敛在这一层
- 对上只暴露内部语义概念，例如：
  - `Confirm`
  - `MoveNext`
  - `MovePrevious`
  - `Back`
- 不让 vendor 类型泄漏到语义层、HUD 层或应用层

### 阶段 B：最低风险接入

- 接入 `MercurySDK.init(...)`
- 补上 `<meta-data android:name="com.rayneo.mercury.app" android:value="true" />`
- 验证应用在 X3 Pro 与普通 Android 环境上的容错表现

当前状态：已完成。

### 阶段 C：X3-native input spike

- 用 `TouchDispatcherX3 + CommonTouchCallback` 只验证：
  - pinch -> confirm
  - slide -> next / previous
- 仍然复用当前已经验证过的语义焦点遍历与安全激活路径
- 不创建第二条执行链

当前状态：未开始。这是下一轮最合适的实现切片。

### 阶段 D：真机审计

- 对目标 App 做 accessibility tree、focusability、activation 质量审计
- 再决定是否需要：
  - `FocusTracker`
  - `RecyclerViewFocusTracker`
  - 更进一步的 X3 专有焦点策略

## 7. 风险与边界

### 7.1 支持性风险

- `request(String)` 明显像字符串协议入口，支持性最差
- `RingIPCHelper` 与外设模式高度耦合
- `TopPackageHelper` 与系统策略耦合较深

### 7.2 工程风险

- 过早把 vendor SDK 当成应用架构中心，会破坏当前项目的可测试性和可移植性
- 过早接 `FocusTracker` 可能让现有 accessibility-first 模型失真
- 过早接 `Speech` / `Launcher` 会把产品从“输入控制”拉回“多能力平台工程”

### 7.3 结论性约束

- 这两个 AAR 应被当作 **硬件适配 shim**，而不是应用主架构。
- 在没有真机审计结果之前，不能让 `RayNeoIPCSDK` 进入 MVP 主路径。

## 8. 下一步

不是继续堆 feature，而是：

1. 先在计划里加入 RayNeo vendor adapter 边界
2. 再做一个很小的 `TouchDispatcherX3` 适配 spike
3. 然后立刻做 App 审计

在 App 审计之前，不继续扩张：

- 不做多手势词汇
- 不做 ring 适配
- 不做 GPS 适配
- 不做原始 IPC 协议封装
- 不做 mirroring UI 重构
