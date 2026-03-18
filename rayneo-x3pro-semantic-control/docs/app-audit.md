# App Audit Baseline

## 1. 为什么现在做这个切片

当前项目已经完成：

- `Accessibility-first` 语义焦点遍历
- 安全激活路径
- `pinch-confirm`
- `RayNeo vendor boundary + Mercury bootstrap`
- `TouchDispatcherX3` 最小输入 spike

接下来真正决定产品边界的，不是继续扩张输入能力，而是确认：

1. 目标 App 的 accessibility tree 是否足够可用。
2. 当前 focus navigation 是否足够稳定。
3. 当前 `ActivateFocused` 是否足够可靠。
4. `FocusTracker / RecyclerViewFocusTracker` 是否真的值得进入主线。

如果这些问题没有先回答，继续加 `FocusTracker`、相机 provider、更多手势或 IPC 接入，都会变成任务漂移。

补充修正：在最新路线里，`App Audit` 仍然重要，但它不再阻塞 `Task 15: Camera-Vision Feasibility Spike`。原因不是它不重要，而是当前北极星方向里最大的未证实风险已经变成“前台相机到底能不能在 X3 Pro 第三方双屏壳层里稳定工作”。因此，`App Audit` 现在作为并行证据轨道存在，用于后续 `FocusTracker` 和白名单判断，而不是作为 camera spike 的硬门槛。

## 2. 审计目标

这轮审计只回答三个核心问题：

- **tree quality**：当前界面是否向 `AccessibilityService` 暴露了结构稳定、语义可读的节点树。
- **focusability**：当前界面是否可以仅靠 `ACTION_ACCESSIBILITY_FOCUS` / `ACTION_FOCUS` 与已有遍历模型稳定移动焦点。
- **activation quality**：当前界面是否可以仅靠 `ACTION_CLICK` 或可点击祖先路径完成确认动作。

审计结果最终只产出三种支持层级：

- `Generic`
- `WhitelistCandidate`
- `Unsupported`

代码侧对应模型见 `app/src/main/java/dev/wenyu/semanticcontrol/core/contracts/AppSupportTier.kt`。

## 3. 第一轮目标 App 类别

第一轮不要追求覆盖面，而要覆盖代表性。

建议每类先选 1 个代表 App：

1. **系统设置 / Preference 风格界面**
   - 目的：验证标准 Android 节点树质量。
2. **原生列表-详情流界面**
   - 目的：验证顺序遍历、列表项激活、返回路径。
3. **表单 / 搜索 / 输入界面**
   - 目的：验证输入焦点与可点击祖先路径是否稳定。
4. **Compose-heavy 界面**
   - 目的：验证现代 UI 框架下的节点暴露质量。
5. **WebView-heavy 界面**
   - 目的：验证 Web 内容下当前语义路径的边界。
6. **自绘 / 媒体 / 画布型界面**
   - 目的：识别当前 MVP 明确不擅长的场景。

第一轮 MVP 更应该优先做前四类；后两类更适合做边界验证，而不是主成功指标。

## 4. 审计评分维度

每个维度只打三档：`Good / Partial / Poor`。

### 4.1 Accessibility tree

- **Good**
  - 能稳定拿到 `rootInActiveWindow`
  - 节点层级基本反映真实交互结构
  - 关键节点有文本、content description、role 或可识别 class
  - 节点变化不会频繁导致全树失真
- **Partial**
  - 关键节点大多可见，但存在缺标签、层级过深、局部虚拟节点难理解等问题
- **Poor**
  - 核心交互几乎不暴露在树里
  - 重要界面只表现为自绘容器、无意义节点或不稳定虚拟树

### 4.2 Focusability

- **Good**
  - `ACTION_ACCESSIBILITY_FOCUS` 或 `ACTION_FOCUS` 可稳定落到目标项
  - 前后遍历顺序基本符合用户预期
- **Partial**
  - 焦点可以移动，但顺序不稳定、局部跳跃、列表项聚合异常，或需要额外策略才能保持可用
- **Poor**
  - 关键节点无法获得可用焦点
  - 焦点经常落到无意义节点，导致当前遍历模型几乎不可用

### 4.3 Activation quality

- **Good**
  - 焦点节点本身或其可点击祖先可稳定触发 `ACTION_CLICK`
- **Partial**
  - 关键路径可触发，但存在部分控件无响应、需要祖先回退、或成功率波动
- **Poor**
  - 关键动作无法通过当前无障碍激活模型完成

## 5. 支持层级映射

### `Generic`

满足：

- `tree quality = Good`
- `focusability = Good`
- `activation quality = Good`

含义：

- 当前 `Accessibility-first + pinch confirm + focus navigation` 已足够。
- 不需要优先引入 `FocusTracker`。

### `WhitelistCandidate`

满足：

- 不存在决定性 `Poor` 问题
- 但至少有一个维度为 `Partial`

含义：

- 该 App 可能可以通过额外策略变得可用。
- 如果 `tree quality = Good`、`focusability = Partial`、`activation quality != Poor`，可以进入 `FocusTracker` 试验名单。

### `Unsupported`

满足任一条件：

- `tree quality = Poor`
- `focusability = Poor`
- `activation quality = Poor`

含义：

- 当前 MVP 不应承诺支持。
- 不要在没有明确产品价值前，为这类场景提前扩张输入系统。

## 6. 每个 App 的证据模板

建议每个 App 按以下格式记录：

```md
## <App Name>

- Package: `<package.name>`
- Screen: `<screen or task name>`
- Category: `<settings | list-detail | form | compose | webview | custom-rendered>`
- Tree quality: `<Good | Partial | Poor>`
- Focusability: `<Good | Partial | Poor>`
- Activation quality: `<Good | Partial | Poor>`
- Support tier: `<Generic | WhitelistCandidate | Unsupported>`
- FocusTracker candidate: `<yes | no>`
- Evidence:
  - `rootInActiveWindow` 是否稳定
  - 焦点是否落到可理解节点
  - `ACTION_CLICK` / 祖先点击是否有效
  - 是否存在列表、滚动、弹窗或 WebView 特例
- Notes:
  - 失败路径
  - 可恢复路径
  - 是否值得进入白名单深适配
```

## 7. 这轮明确不做什么

- 不在这轮里接 `RayNeoIPCSDK`
- 不在这轮里扩张相机 provider
- 不在这轮里扩张多手势词汇
- 不在这轮里做 HUD 完整重构
- 不在这轮里做白名单深适配实现

## 8. 真机审计完成后的决策门

只有在拿到首批 App 的真实证据后，才允许继续回答下面的问题：

1. `FocusTracker / RecyclerViewFocusTracker` 是否有实际收益。
2. 哪些 App 应进入 `WhitelistCandidate`。
3. 当前项目是否真的需要 camera-backed provider。
4. 哪些场景应明确标记为 `Unsupported`。

## 9. 参考依据

- Android Developers: Create your own accessibility service  
  https://developer.android.com/guide/topics/ui/accessibility/service
- Android Developers: AccessibilityNodeInfo  
  https://developer.android.com/reference/android/view/accessibility/AccessibilityNodeInfo
- Android Developers: AccessibilityAction  
  https://developer.android.com/reference/android/view/accessibility/AccessibilityNodeInfo.AccessibilityAction
- Google Codelab: Developing an Accessibility Service for Android
  https://codelabs.developers.google.com/codelabs/developing-android-a11y-service
