# RayNeo X3 Pro Semantic Control

独立 Android 原生项目骨架，目标设备为 `RayNeo X3 Pro`。

当前产品方向：

- 短期：`Accessibility-first + pinch confirm + focus navigation`
- 长期：`全局语义控制 + 白名单 App 深适配`

当前版本只完成了单模块工程骨架与包级职责分层，没有开始接入真实手势识别、厂商 SDK 或白名单适配逻辑。

出于 MVP 边界控制，当前清单里只保留无障碍服务接入所需的最小能力，尚未提前申请相机、悬浮层或前台服务权限。

## 工程结构

- 单 `app` 模块，避免在 MVP 阶段过早引入 Gradle 模块复杂度
- 包级分层：
  - `dev.wenyu.semanticcontrol.app`
  - `dev.wenyu.semanticcontrol.core.contracts`
  - `dev.wenyu.semanticcontrol.feature.gesture`
  - `dev.wenyu.semanticcontrol.feature.overlay`
  - `dev.wenyu.semanticcontrol.feature.semantic`

## 当前边界

- 不做全局空气鼠标
- 不做任意屏幕坐标点击
- 不做复杂手势词汇
- 不以厂商私有接口为前置依赖

## 下一步

1. 验证 X3 Pro 上 `AccessibilityService`、overlay、前台服务与相机权限策略。
2. 接入 `pinch -> activate` 的最小手势链路。
3. 实现 `next / previous / activate / back / home` 五个 MVP 动作。
4. 审计首批目标 App 的 accessibility tree 质量。

## 构建

工程是独立单模块 Gradle 项目，根目录在：`rayneo-x3pro-semantic-control/`。

直接使用该目录下的 Gradle wrapper：`./gradlew` 或 `gradlew.bat`。
