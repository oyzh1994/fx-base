# fx-plus 工具类（util）

覆盖 `cn.oyzh.fx.plus.util` 包 16 个正式类。跳过的整文件死代码：`FXBeanUtil`。

## FXUtil
- 职责：JavaFX 核心工具类，线程调度、坐标换算、图片/媒体加载、屏幕信息、滚动条查找等。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `robot` | `static Robot` | 懒加载的机器人对象 |
  | `preferences` | `static Platform.Preferences` | 懒加载的平台偏好设置 |
- 方法（要点）：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getRobot()` / `getPreferences()` | 懒加载单例 | `getPreferences` 通过 `runWait` 在 FX 线程取 |
  | `computePos(Window,Window)` | 居中定位 | 目标窗口相对 owner 居中 |
  | `getAbsoluteX/getAbsoluteY(EventTarget)` | 绝对坐标 | 递归 Window/Scene/Node |
  | `runWait(Runnable)` | 同步运行 | FX 线程直接执行，否则 `Platform.runLater` + `DownLatch` 等待，异常重抛 |
  | `runLater(Runnable[,delay])` | 稍后执行 | `Platform.runLater`；带 delay 用 `TaskManager.startDelay` |
  | `runAsync(Runnable)` | 异步执行 | `runLater(task, 1)` |
  | `getFXThread()` | 取 FX 线程 | 遍历线程栈按名匹配 |
  | `runPulse(Runnable)` | 脉冲后执行 | 注册 `TKPulseListener`，pulse 后移除 |
  | `runTimer(Runnable,int)` | 指定脉冲数后执行 | `AnimationTimer` 计数 |
  | `disableCSSLogger()` | 禁用 css 日志 | `Logging.getCSSLogger().disableLogging()` |
  | `getImages(String[]/List)` / `getImage(String)` / `getMedia(String)` / `toImage(BufferedImage)` | 资源加载 | 基于 `ResourceUtil` 与 `ImageIO` |
  | `isInitialized()` | FX 是否可用 | 试运行 `runLater` 捕获异常 |
  | `showDocument(String)` | 打开链接 | `HostServices.showDocument` |
  | `screenRefreshRate()/screenScale()` | 屏幕信息 | AWT/Screen |
  | `getScrollBar/getVScrollBar/getHScrollBar/getScrollBars` | 滚动条查找 | 递归 `getChildrenUnmodifiable` |
  | `isPointInNode(MouseEvent,Node)` | 点是否在节点内 | `screenToLocal` + `contains` |
  | `isPulseFromQueue()/isEnablePreview()/enablePreview()/disablePreview()` | 运行环境 | 系统属性 `javafx.enablePreview` |
- 调用链：`runWait → Platform.runLater + DownLatch`；`runPulse → Toolkit.addPostSceneTkPulseListener`

---

## PropertiesUtil
- 职责：属性工具类，为 Node/Tab/Scene/Window/TableColumnBase 存取 properties 键值。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `has(EventTarget,Object)` | 是否存在 | 按类型分派 `getProperties().containsKey` |
  | `get/remove(EventTarget,Object[,Object])` | 读/删 | 分派到各宿主 |
  | `set(EventTarget,Object,Object)` | 写 | 分派到各宿主 `getProperties().put` |
  | `clear(EventTarget)` | 清空 | 分派到各宿主 |
- 调用链：`StageManager.allStages → PropertiesUtil.get(window, REF_ATTR)`

---

## StyleUtil
- 职责：样式工具类，样式字符串拼接/分割、样式类增删、内联样式读写、样式表重应用。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `join(String...)/split(String...)` | 拼接/分割 | |
  | `toggleStyleClass/addStyleClass(Styleable,String[,excludes])` | 样式类 | |
  | `appendStyle/removeStyle/replaceStyle/getStyle/setStyle` | 内联样式 | 解析 `prop:value` 片段 |
  | `removeStylesheet/reapplyStylesheet(Parent,String)` | 样式表 | reapply 先删再加 |
- 调用链：`ThemeStyle.handleStyle → StyleUtil.reapplyStylesheet(node, FXStyle.FX_BASE)`

---

## ControlUtil
- 职责：控件工具类，生成边框/背景、文本选中处理、滚动条尺寸与 thumb 管理。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `BW_HALF` | `BorderWidths`(常量) | 0.5 宽边框 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `strokeOfWidth/strokeOfWidthBottom/strokeOfThick/strokeOfMedium/borderOfThin/borderOfThinRight` | 生成边框 | 组合 `BorderStroke` |
  | `background(Paint)` | 生成背景 | `BackgroundFill` |
  | `deselect(Text/TextInputControl)/isSelect(TextInputControl)` | 选中处理 | |
  | `backgroundFill(Region)` | 取背景色 | 首个填充色 |
  | `getVBarWidth/getHBarHeight(ScrollPane)` | 滚动条尺寸 | 反射取 skin 的 vsb/hsb |
  | `setupMinVisibleAmount(ScrollBar,double)` | 最小 thumb | 监听 visibleAmount 下限 |
- 调用链：`HexView.applyThemeColors → ControlUtil.background`

---

## IconUtil
- 职责：图标工具类，加载缓存图片图标、系统文件图标、按扩展名返回 SVG 图标。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `ICON_CACHE` | `static TimedCache<String, WeakReference<byte[]>>` | 图片字节缓存（60s） |
  | `SYSTEM_ICON_CACHE` | `static Map<String,Image>` | 系统图标缓存 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getIcons(String[]/List)` | 批量图标 | 逐个 `getIcon` |
  | `getIcon(String)` | 图片图标 | 缓存命中用缓存字节，否则读流缓存 |
  | `iconToFxImage(Icon)` | AWT→FX 图片 | 绘制 BufferedImage 后 `PixelWriter` |
  | `getSystemIcon(String)` | 系统图标 | 建临时文件用 `FileSystemView.getSystemIcon`，按扩展名缓存 |
  | `getSVGIcon(String)` | SVG 图标 | 按大量 `FileNameUtil.isXxxType` 分派到对应 `FileXxxSVGGlyph` |
- 调用链：`IconTableCell.getIcon → IconUtil.getSystemIcon/getSVGIcon`；`StageAdapter.init → IconUtil.getIcon`

---

## FXColorUtil
- 职责：颜色工具类，颜色转 16 进制、样式色转 web 色、颜色距离与最接近色查找。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getColorHex(Color)` | 转 16 进制 | `#RRGGBB` |
  | `styleColorToWebColor(String)` | 样式色→web | 解析 `-fx-fill:` |
  | `colorDistance(Color,Color)` | 颜色距离 | 欧几里得 |
  | `findClosestColor(Color,List)` | 最近色 | 距离小于 0.35 才返回 |
- 调用链：`ThemeStyle.getAccentColorHex → FXColorUtil.getColorHex`

---

## Counter
- 职责：计数器，统计任务的成功/失败/忽略数量与耗时并格式化输出。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `sum` | `Integer` | 总数 |
  | `startTime` | `Long` | 开始时间 |
  | `failCount/ignoreCount/successCount` | `AtomicInteger` | 失败/忽略/成功计数 |
  | `extraMsg` | `String` | 额外信息 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `incrFail/incrSuccess/incrIgnore(int)` | 递增 | 委托 `update(type,val)` |
  | `incr(int)` | 智能递增 | 正数成功/负数失败/0 忽略 |
  | `update(int[,int])` | 更新 | 首次记录 startTime |
  | `updateFail/updateSuccess/updateIgnore` | 单次更新 | |
  | `reset()` | 重置 | |
  | `getElapsed()/getTotalCount()` | 耗时/总数 | |
  | `format(String)/knownFormat()/unknownFormat()` | 格式化 | 模板变量替换 `$sum` 等 |
- 调用链：`knownFormat → format → I18nHelper.*`

---

## ScreenUtil
- 职责：屏幕工具类，获取所有屏幕总宽与主屏高。
- 字段：无
- 方法：`getAllWidth()`（累加各屏视觉宽）、`getPrimaryHeight()`（主屏视觉高）
- 调用链：`FXHeaderBar.setTitleLabel → ScreenUtil.getAllWidth`

---

## CursorUtil
- 职责：鼠标光标工具类，为窗口/节点设置等待/手型/默认光标。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `waitCursor/handCursor/defaultCursor(Window/Node)` | 设置光标 | 委托 `setCursor` |
  | `setCursor(Node,Cursor)` | 设节点光标 | `runWait` 内 setCursor，去重 |
  | `setCursor(Window,Cursor)` | 设窗口光标 | 委托根节点 |
- 调用链：`WindowAdapter.handCursor → CursorUtil.handCursor`

---

## AnimationUtil
- 职责：动画工具类，创建旋转动画与节点移动缩放动画。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `rotate(Node)` | 旋转动画 | 3s、360°、无限、线性 |
  | `move(Node,Node,Node)` / `move(Window,...)` | 移动动画 | 计算起止坐标 |
  | `move(Window,Node,startX,startY,endX,endY)` | 移动并缩小 | `Timeline` 关键帧，结束移除节点 |
- 调用链：`AnimationUtil.move → Timeline.play`

---

## ClipboardUtil
- 职责：剪贴板工具类，复制/粘贴文本并可选提示。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `copy(Object)` | 复制 | TextInputControl/CharSequence |
  | `paste(Object[,Object])` | 粘贴 | TextInputControl.paste |
  | `setString(String)` | 置入剪贴板 | `Clipboard.getSystemClipboard().setContent` |
  | `setStringAndTip(String)` | 置入并提示 | 成功 `MessageBox.okToast` |
  | `getString()/hasString()` | 读/判定 | |
- 调用链：`HexView.onKeyPressed(Ctrl+C) → ClipboardUtil.setString`

---

## FXCoverChecker
- 职责：FX 覆盖检查器，扫描项目类并检查 Tab/页面/弹窗能否正常实例化。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `projectPath` | `String` | 项目路径 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getProjectPath/setProjectPath` | 路径 | |
  | `getClasses()` | 扫描类 | `ClassUtil.findClassesInDirectory`，过滤抽象/非 public |
  | `tabCheck()` | Tab 检查 | RichTab 子类逐一新实例化 |
  | `viewCheck()` | 页面检查 | 带 `@StageAttribute` 的类 `StageManager.parseStage` |
  | `popupCheck()` | 弹窗检查 | 带 `@PopupAttribute` 的类 `PopupManager.parsePopup` |
- 调用链：`viewCheck → StageManager.parseStage`；`popupCheck → PopupManager.parsePopup`

---

## HeaderBarUtil
- 职责：扩展标题栏工具类，从根节点查找 `FXHeaderBar` 并生成标题栏图标。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getHeaderBar(Parent)` | 查标题栏 | `lookup("#headerBar")` / `lookup("FXHeaderBar")` |
  | `getIcon(String)` | 图标 | `IconUtil.getIcon` 包成 `ImageView` |
- 调用链：`StageAdapter.getHeaderBar → HeaderBarUtil.getHeaderBar`

---

## TabPaneUtil
- 职责：TabPane 工具类，获取 tab 所属窗口并在窗口就绪时回调。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getWindow(Tab)` | 所属窗口 | tab→tabPane→scene→window |
  | `onWindowReady(Tab,Consumer<Window>)` | 窗口就绪回调 | 就绪立即回调，否则监听 tabPane/scene/window 属性直至就绪 |
- 调用链：`TabPaneUtil.onWindowReady → callback.accept(window)`

---

## TreeViewUtil
- 职责：树组件工具类，展开祖先、收集节点、遍历、可见性判断。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `expandAll(TreeItem)` | 展开祖先链 | 逐级 setExpanded(true) |
  | `getAllItem(TreeView,Function)` | 收集全部节点 | 递归，可过滤 |
  | `filterItem(TreeView,Consumer)` | 遍历 | 递归消费 |
  | `getVisibleItems(TreeView)` | 可见节点 | 依据展开状态 |
  | `isVisible(TreeView,TreeItem)` | 是否可见 | 选中或已展开 |
- 调用链：`FXTreeView.getAllItem → TreeViewUtil.getAllItem`

---

## ListViewUtil
- 职责：列表视图工具类，行上/下移、高亮、查找所属列表、点击选中。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `moveUp/moveDown(ListView)` | 移动行 | `Collections.swap` 后重选 |
  | `highlightCell(ListCell)` | 高亮行 | 进出改变背景色 |
  | `findListView(Node)` | 找所属列表 | 向上遍历 parent |
  | `selectRowOnMouseClicked(Node[,Node])` | 点击选中 | 事件过滤器选中对应行 |
- 调用链：`ListViewPopup cellFactory → ListViewUtil.highlightCell`

---
