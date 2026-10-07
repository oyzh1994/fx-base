# fx-plus 窗口（window）

覆盖 `cn.oyzh.fx.plus.window` 包 16 个正式类。跳过的整文件死代码：`StagePopup`、`PopupMask`。

## FXStageStyle
- 职责：窗口风格枚举，映射到 JavaFX `StageStyle`。
- 字段（枚举常量）：`DECORATED`(带系统标题栏)、`UNDECORATED`(无装饰)、`TRANSPARENT`(透明)、`UTILITY`(弃用)、`UNIFIED`(弃用)、`EXTENDED`(内容扩展到标题栏)、`CUSTOM`(弃用)
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `toStageStyle()` | 转 JavaFX 风格 | switch 映射，CUSTOM→UNDECORATED |
  | `isExtended()` | 是否扩展 | `== EXTENDED` |
  | `isCustom()` | 是否自定义 | `== CUSTOM` |
- 调用链：`StageAdapter.init → attribute.stageStyle().toStageStyle()`

---

## WindowListener
- 职责：窗口事件监听接口。
- 字段：无
- 方法：`onWindowShowing/onWindowHiding/onWindowHidden/onWindowShown/onWindowCloseRequest(WindowEvent)`（抽象）
- 调用链：无

---

## WindowAdapter
- 职责：窗口能力适配接口，提供 controller、场景/根节点、光标、ESC 隐藏、Tab 切换等。
- 字段：无（使用 prop 键 `_controller`/`escHideHandler`/`tabSwitchHandler`）
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `onWindowClosed()` | 关闭处理 | `unSwitchOnTab` + `unHideOnEscape` |
  | `handCursor()/waitCursor()/defaultCursor()` | 光标（抽象） | |
  | `controller()/controllerClass()` | 控制器 | prop `_controller` |
  | `hideOnEscape()/unHideOnEscape()/isHideOnEscape()` | ESC 隐藏 | 增删 `EscHideHandler` |
  | `switchOnTab()/unSwitchOnTab()/isSwitchOnTab()` | Tab 切换 | 增删 `TabSwitchHandler` |
  | `scene()`(抽象) / `root()` | 场景/根 | 由 scene 取 root |
- 调用链：`WindowAdapter.onWindowClosed → unHideOnEscape/unSwitchOnTab`

---

## StageListener
- 职责：舞台事件监听接口，继承 `WindowListener`。
- 字段：无
- 方法：`onStageInitialize(StageAdapter)`、`onSystemExit()`（抽象）
- 调用链：`StageAdapter.init → listener.onStageInitialize`

---

## StageAttribute
- 职责：舞台注解，声明 fxml 地址与窗口行为。
- 字段（注解元素）：`value`(fxml)、`title`(弃用)、`cssUrls`、`iconUrl`、`modality`、`maximumAble`、`resizable`、`multipliable`、`fullScreenAble`、`alwaysOnTopAble`、`stageStyle`、`usePrimary`、`sceneTransparent`
- 方法：无
- 调用链：`StageManager.parseStage → clazz.getAnnotation(StageAttribute.class)`

---

## StageAdapter
- 职责：舞台适配接口，定义窗口显示/隐藏/尺寸/标题/主题/拖拽/监听等完整能力。
- 字段：无
- 方法（要点）：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `stage()` | 舞台（抽象） | |
  | `onWindowClosed()` | 关闭 | 清理拖拽、`NodeLifeCycleUtil.onStageDestroy`、销毁节点/场景/监听 |
  | `hasBeenVisible()` | 是否显示过 | 反射读 `Window.hasBeenVisible` |
  | `scene()/clearScene/scene(Scene)/root(Parent)` | 场景/根 | |
  | `getX/setX/getY/setY/setLocation/getWidth/setWidth/setHeight/setSize` | 位置尺寸 | 委托 stage |
  | `isFullScreen/isMaximized/setMaximized` | 全屏/最大化 | |
  | `getHeaderBar()/isExtendedHeader()` | 扩展标题栏 | style==EXTENDED |
  | `init(StageAttribute, Window)` | 初始化 | `FXMLLoaderExt` 加载 fxml→建 Scene（扩展/透明时填充透明）→设 controller→initStyle→icon→owner/modality→注册 showing/maximized/fullScreen 监听→加载 css→初始化监听器 |
  | `updateContent()/updateContentLater()` | 更新内容 | 先加减/先减加根尺寸，触发重排解决边框/白屏 |
  | `resizeStage/resizeRoot` | 尺寸 | |
  | `clearListener()/initListener(StageListener)` | 监听 | 绑定/解绑 stage 的 shown/hiding/hidden/showing/closeRequest 回调 |
  | `clearTitle/title(String)/title()/appendTitle/restoreTitle` | 标题 | 扩展标题栏时操作头栏，否则 stage |
  | `toFront()` | 前置 | `runLater` toFront + 取消最小化 |
  | `handCursor/waitCursor/defaultCursor/hideOnEscape/switchOnTab` | 光标/行为 | 委托 CursorUtil 或安装 Handler |
  | `initDragFile(String, Consumer<List<File>>)` | 文件拖拽 | 建匿名 `DragFileHandler`，dragover 追加标题、drop 回调文件 |
  | `isShowing/setIconified/isIconified/isFocused/requestFocus/getOpacity/setOpacity` | 状态 | |
  | `hide/close/show/showAndWait` | 显示控制 | close/show 走 `FXUtil.runWait` |
  | `changeTheme(ThemeStyle)` | 主题 | 扩展标题栏时按明暗设场景填充色 |
- 调用链：`StageManager.parseStage → StageExt/PrimaryStage.init → FXMLLoaderExt.load`；`ThemeManager.apply → StageAdapter.changeTheme`

---

## StageExt
- 职责：舞台扩展，继承 `Stage` 并实现 `StageAdapter`，是普通窗口的默认实现。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `(StageAttribute,Window)` | 按属性构造 | `init(attribute, owner)`、写 `REF_ATTR`、`ObjectWatcherManager.watch` |
  | 构造 `(Window owner)` | 无属性构造 | 委托四参 |
  | 构造 `(Window,Parent,Double,Double)` | 直接构造 | initOwner、写 REF_ATTR、`NodeManager.init`、设 root/宽高 |
  | `stage()` | 自身 | `return this` |
- 调用链：`StageManager.parseStage → new StageExt(attribute, owner)`

---

## PrimaryStage
- 职责：主舞台适配器，实现 `StageAdapter`，包装外部传入的主 `Stage`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `stage` | `Stage` | 被包装的主舞台 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `(Stage, StageAttribute, Window)` | 构造 | 写 `REF_ATTR` 后 `init` |
  | `stage()` | 舞台 | 返回包装对象 |
  | `onWindowClosed()` | 关闭 | 空实现（主舞台不销毁） |
- 调用链：`StageManager.parseStage(usePrimary) → new PrimaryStage`

---

## StageMask
- 职责：舞台遮罩，继承 `Stage`，在目标窗口上覆盖半透明遮罩 + 加载动画并执行回调。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `target` | `Window` | 被遮罩的目标窗口 |
  | `callback` | `Runnable` | 遮罩期间执行的业务 |
  | `xFunc/yFunc/wFunc/hFunc` | `ChangeListener<Number>` | 跟随目标窗口位置/尺寸的监听器 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `(Window, Runnable)` | 构造 | 透明样式、StackPane 遮罩 + ProgressIndicator、绑定目标窗口 x/y/w/h、监听 showing |
  | `doCallback()`(protected) | 执行回调 | 运行 callback 后隐藏自身 |
  | `onWindowClosed()` | 关闭 | 移除目标监听并请求焦点恢复 |
  | `stage()` | 自身 | |
  | `showMask(Window, Runnable)`(static) | 显示遮罩 | `runLater` 新建并显示 |
- 调用链：`StageManager.showMask → StageMask.showMask → doCallback`

---

## StageManager
- 职责：舞台工具类，统一创建/查找/显示舞台，管理主舞台、退出、遮罩、前台窗口、任务栏隐藏。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `REF_ATTR` | `String`(常量) | 窗口上挂载适配器的属性键 `stage:ref` |
  | `primaryStage` | `volatile Stage` | 主舞台 |
  | `EXITED` | `AtomicBoolean`(常量) | 退出标志 |
  | `MASK_SHOWING_KEY` | `String`(常量) | 遮罩显示标志键 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `get/setPrimaryStage` | 主舞台 | |
  | `exit()` | 退出系统 | 去重后遍历 `allStages` 调 `onSystemExit`，`Platform.exit()` |
  | `allStages()` | 所有适配器 | 扫描 `Window.getWindows()` 中带 REF_ATTR 的 |
  | `allWindows()` | 所有窗口 | `Window.getWindows()` 副本 |
  | `getStage(Class)/listStage(Class)` | 按 controller 查找 | |
  | `isAdapter(Window)/getAdapter(Window)` | 适配器判定/获取 | |
  | `showStage(...)` 多重载 | 显示舞台 | `parseStage` 后 `display` |
  | `newStage(Window)` | 新建 | `new StageExt(owner)` |
  | `parseStage(Class[,Window])` | 解析舞台 | 读 `@StageAttribute`；非多实例复用已有；`runWait` 内按 usePrimary 创建 PrimaryStage/StageExt |
  | `hideTaskbar(Stage)/getTaskbarStage()` | 隐藏任务栏 | 用 UTILITY 透明空舞台作 owner |
  | `showMask(...)` 多重载 | 显示遮罩 | 加 `MASK_SHOWING_KEY` 去重，`StageMask.showMask` 执行回调后移除标志 |
  | `getFrontWindow()/hasFocusedWindow()` | 前台窗口 | 跳过 StageMask，取聚焦且显示者 |
- 调用链：`StageManager.showStage → parseStage → StageExt/PrimaryStage.init`；`exit → StageListener.onSystemExit`

---

## WindowManager
- 职责：窗口工具类，查询所有/活跃窗口与关闭全部窗口。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `allWindows()` | 所有窗口 | 副本列表 |
  | `closeWindows()` | 关闭全部 | `runLater(window::hide)` |
  | `getActiveWindow()` | 活跃窗口 | 聚焦且显示者 |
- 调用链：`WindowManager.closeWindows → Window.hide`

---

## PopupListener
- 职责：弹窗事件监听接口，继承 `WindowListener`。
- 字段：无
- 方法：`onPopupInitialize(PopupAdapter)`（抽象）
- 调用链：`PopupAdapter.init → listener.onPopupInitialize`

---

## PopupAttribute
- 职责：弹窗注解，声明 fxml 地址、css、箭头位置与锚点。
- 字段（注解元素）：`value`(fxml)、`cssUrls`、`arrowLocation`(默认 TOP_LEFT)、`anchorLocation`(默认 WINDOW_TOP_LEFT)
- 方法：无
- 调用链：`PopupManager.parsePopup → clazz.getAnnotation(PopupAttribute.class)`

---

## PopupAdapter
- 职责：弹窗适配接口，定义弹窗内容/显示/提交/handler/监听等能力。
- 字段：无（prop 键 `_controller`/`escHideHandler`/`tabSwitchHandler`/`_submitHandler`）
- 方法（要点）：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `onWindowClosed()` | 关闭 | `WindowAdapter.super` + 清空内容 |
  | `popup()` | 弹窗窗口（抽象） | |
  | `initListener(PopupListener)` | 监听 | 绑定 popup 的 shown/hiding/hidden/showing/closeRequest |
  | `handCursor/waitCursor/defaultCursor/hideOnEscape/switchOnTab` | 光标/行为 | 委托 CursorUtil 或安装 Handler |
  | `showPopup(...)` 多重载 | 显示 | 按 owner Node/Window + 坐标显示 |
  | `content()/content(Node)` | 内容（抽象） | |
  | `getController()` | 控制器 | prop `_controller` |
  | `init(PopupAttribute)` | 初始化 | `FXMLLoaderExt` 加载 fxml→设内容/controller/css→初始化监听 |
  | `setSubmitHandler(Consumer<T>)/getSubmitHandler()/submit(T)` | 提交 | 存于 prop，submit 调用消费器 |
  | `getOwnerNode()/getOwnerWindow()/isShowing()/show(Node)/hide()/scene()` | 访问 | 委托 popup |
- 调用链：`PopupManager.showPopup → PopupExt.init → PopupListener.onPopupInitialize`

---

## PopupExt
- 职责：弹窗扩展，继承 atlantafx `Popover` 并实现 `PopupAdapter`。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `()` / `(PopupAttribute)` | 构造 | `initDefault`、写 `REF_ATTR`、（有属性则 `init`）、`ObjectWatcherManager.watch` |
  | `initDefault()`(protected) | 默认属性 | autoFix/animated/autoHide/hideOnEscape、淡入淡出 350ms、监听显示触发 `onWindowClosed`/`onWindowShown` |
  | `popup()` | 自身 | |
  | `showPopup(Node)` | 显示 | 按 owner 屏幕坐标 + 4px 偏移显示 |
  | `content()/content(Node)` | 内容 | 委托 contentNode |
- 调用链：`PopupManager.parsePopup → new PopupExt(attribute) → init`

---

## PopupManager
- 职责：弹窗工具类，解析/查找/显示弹窗（全局仅保留一个实例）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `REF_ATTR` | `String`(常量) | 弹窗引用属性键 `_popup_window_reference` |
  | `popup` | `PopupExt` | 单例弹窗实例 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getPopup(Class)` | 查找弹窗 | 扫描窗口按 REF_ATTR 匹配 controller |
  | `showPopup(Class, Node)` | 显示 | `parsePopup` 后 `showPopup(owner)` |
  | `parsePopup(Class)` | 解析 | 读 `@PopupAttribute`；已有同 controller 弹窗则 `disappear`；创建/初始化单例并设置箭头/锚点 |
- 调用链：`PopupManager.showPopup → parsePopup → PopupExt.init`

---
