# fx-plus 消息与提示（information、tooltip）

覆盖 information(5) + tooltip(1，`TooltipUtil`)，共 6 个正式类。跳过的整文件死代码：`tooltip/TooltipPool`。

## AlertStage
- 职责：提示框舞台，继承 `Stage` 并实现 `StageAdapter`，展示确认/警告/错误/信息并返回用户点击的按钮。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `result` | `Button` | 用户点击的按钮 |
  | `content` | `FXLabel` | 内容标签 |
  | `graphic` | `SVGGlyph` | 类型图标 |
  | `buttons` | `List<Button>` | 按钮列表 |
  | `type` | `Alert.AlertType` | 提示类型 |
  | `DEFAULT_MARGIN` / `BUTTON_DEFAULT_MARGIN` | `Insets`(常量) | 内容/按钮外边距 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 3 重载 `(type[,content[,buttons]])` | 构造 | 模态 + EXTENDED 样式；按类型设图标（确认/警告/错误/信息）；装配 HeaderBar+图标+内容+按钮，按钮点击记录 result 并 hide；显示时按内容高度调整窗口并居中 |
  | `stage()` | 自身 | |
  | `showAndWait()` | 显示并等待 | 结束后置空成员、清 scene |
  | `getResult()` | 结果 | `showAndWait` 后返回 result |
  | `setContent(String)/getContent()` | 内容 | |
  | `hide()` | 隐藏 | 确认类型未选择时忽略隐藏 |
- 调用链：`MessageBox.confirm/alert → new AlertStage → getResult`

---

## InputStage
- 职责：输入框舞台，继承 `Stage` 并实现 `StageAdapter`，弹窗获取单行文本输入。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `textField` | `FXTextField` | 输入框（ClearableTextField） |
  | `result` | `String` | 输入结果 |
  | `DEFAULT_MARGIN` / `OK_DEFAULT_MARGIN` | `Insets`(常量) | 外边距 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `()` / `(String initText)` | 构造 | 模态 + EXTENDED；装配 HeaderBar+文本框+确定/取消按钮；ENTER→ok，ESC→cancel |
  | `cancel()` | 取消 | result=null 后 close |
  | `ok()` | 确认 | result=文本框内容后 close |
  | `stage()` | 自身 | |
  | `showAndWait()` | 等待 | 结束后置空 |
  | `getResult()` | 结果 | `showAndWait` 后返回 result |
  | `setText/getText/setPromptText/getPromptText` | 文本 | |
- 调用链：`MessageBox.prompt → new InputStage → getResult`

---

## MessageBox
- 职责：消息盒子工具类，统一提供确认/警告/信息/错误/异常/输入/Toast/Tooltip 等交互。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `enableNewStyle` | `static boolean` | 是否启用新式（FX）对话框，默认 true |
  | `Exception_Parser` | `static Function<Throwable,String>` | 异常解析器 |
- 方法（要点）：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `registerExceptionParser(Function)` | 注册异常解析器 | |
  | `confirm(...)` 多重载 | 确认框 | `runWait` 内新式用 `AlertStage(CONFIRMATION)`，旧式用 JavaFX `Alert`；返回是否点确定 |
  | `warn/info/error/none(String...)` | 各类提示 | 委托 `alert` |
  | `exception(Throwable[,title])` | 异常框 | 用解析器取消息，`alert(WARNING)` |
  | `alert(AlertType,...[,Window])` | 通用提示 | FX 已初始化用 `AlertStage`/`Alert`，否则 `JOptionPane` |
  | `prompt(String[,initText])` | 输入框 | 新式用 `InputStage`，旧式用 `TextInputDialog` |
  | `tipMsg(String,Node[,liveTime])` | 浮动提示 | 卸载旧提示→`TooltipUtil.initStyle`→`Tooltip.install`→按存活时间自动隐藏→显示 |
  | `okToast/warnToast/questionToast(String[,Window])` | Toast 提示 | 用对应图标调 `showToast` |
  | `showToast(String,SVGGlyph,Window)`(私有) | 显示 Toast | 按主题色设边框/背景/文字色后 `toast.show(owner)` |
- 调用链：`MessageBox.confirm → AlertStage`；`MessageBox.alert → AlertStage/JOptionPane`；`MessageBox.tipMsg → TooltipUtil.initStyle`

---

## Toast
- 职责：消息提示条，用透明 `Stage` 显示短暂消息并自动关闭。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `msg` | `String` | 消息 |
  | `duration` | `int` | 持续时间（默认 1500ms） |
  | `font` | `Font` | 字体 |
  | `icon` | `SVGGlyph` | 图标 |
  | `border` | `Border` | 边框 |
  | `textFill` | `Paint` | 文字色 |
  | `background` | `Background` | 背景 |
  | `window` | `Stage` | 承载窗口 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `(String msg)` | 构造 | |
  | `resetDefault()`(protected) | 重置默认 | duration<=0 时设 1500 |
  | `show(Window owner)` | 显示 | 计算文本/图标宽度组装 HBox，建透明 Stage，`setOnShown` 里 `FXUtil.computePos` 定位 + 延迟关闭 |
  | `close()` | 关闭 | 清空各成员，`runWait` 隐藏 window |
  | `isShowing()` | 是否显示 | |
  | `setOnHidden(EventHandler)` | 隐藏事件 | |
  | 各 getter/setter | 访问器 | |
- 调用链：`MessageBox.showToast → new Toast → show → TaskManager.startDelay → close`

---

## TooltipExt
- 职责：提示条扩展，继承 `Tooltip` 并实现 `PropAdapter`，在节点右对齐处显示。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `hide()` | 隐藏 | `FXUtil.runWait(super::hide)` |
  | `show(Node)` | 在节点显示 | 按节点屏幕坐标与文本宽度计算 X，靠右显示 |
- 调用链：`MessageBox.tipMsg → new TooltipExt → show(node)`

---

## TooltipUtil
- 职责：提示组件工具类，为节点或页签设置/获取/卸载提示。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `PROP_KEY` | `String`(常量) | 提示处理器属性键 `_tip_handler` |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initStyle(Tooltip)` | 初始化样式 | 追加黑色背景/圆角/白字/字号 10，延迟 500ms、透明度 0.75 |
  | `getTooltip(EventTarget)` | 取提示 | `PropertiesUtil.get(target, FXConst.TOOLTIP_PROP_KEY)` |
  | `setTipText(EventTarget, String)` | 设置提示 | 卸载旧提示；Node 用 MOUSE_ENTERED/EXITED 过滤器 install/uninstall；Tab 用选中监听设置/清除 tooltip |
  | `uninstall(EventTarget)` | 卸载 | Node `Tooltip.uninstall`；Tab `setTooltip(null)` |
  | `getTipText(EventTarget)` | 取文本 | |
- 调用链：`TooltipUtil.setTipText → Tooltip.install / tab.setTooltip`

---
