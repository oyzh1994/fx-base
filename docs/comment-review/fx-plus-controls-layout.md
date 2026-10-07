# fx-plus 容器与图形控件（controls 之 pane/tab/popup/svg）

覆盖 pane(6)、tab(4)、popup(3)、svg(7)，共 20 个正式类。跳过的整文件死代码：`web/WebViewPane`、`swing/FXSwingNode`。
通用模式：继承标准控件 + 能力适配接口，实例块 `NodeManager.init(this)`，`resize` 走 `computeSize → super.resize → resizeNode`。

## FXFlowPane
- 职责：流式布局面板，继承 `FlowPane`，支持 flex/主题/字体/状态/提示/布局/节点组。
- 字段：无
- 方法：`resize`（通用模式）
- 调用链：`FXFlowPane.resize → resizeNode`

---

## FXPane
- 职责：基础面板容器，继承 `Pane`，支持 flex/布局/主题/字体/节点适配。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `FXPane()` / `FXPane(Node...)` | 创建 | |
  | `resize(double,double)` | 尺寸调整 | 通用模式 |
  | `layoutChildren()` | 布局 | 子节点先 `autosize()` 再 `super.layoutChildren()` |
- 调用链：`FXPane.layoutChildren → Node.autosize`

---

## FXScrollPane
- 职责：滚动面板，继承 `ScrollPane`，支持 flex/主题，内容随面板尺寸自适应。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `(Node content)` / `()` | 创建 | |
  | `resize` | 尺寸调整 | 通用模式 |
  | `resizeNode(Double,Double)` | 节点尺寸调整 | 内容为 `FlexAdapter` 时按 `FlexUtil.compute` 设真实宽高，否则 `NodeUtil.setWidth/Height` |
- 调用链：`FXScrollPane.resizeNode → FlexUtil.compute / NodeUtil.setWidth`

---

## FXSplitPane
- 职责：分割面板，继承 `SplitPane`，支持分割条显隐、位置记录与固定子项尺寸。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `showDivider` | `boolean` | 是否显示分割条 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `resize` | 尺寸调整 | 通用模式 |
  | `layoutChildren()` | 布局 | 对 `*Content` 的 Region 子节点按方向重设尺寸 |
  | `getPosition0()` / `getPosition0(double)` | 首个分割条位置 | 从 prop `position_0` 读取 |
  | `recordPosition0()` | 记录位置 | `getDividerPositions()[0]` 写入 prop |
  | `setShowDivider(boolean)/isShowDivider()` | 分割条显隐 | 遍历 `*ContentDivider` 子节点设 visible/managed |
  | `initNode()` | 初始化 | 监听 items 新增设 `setResizableWithParent(false)`；监听 children 新增按 `showDivider` 设置分割条 |
- 调用链：`FXSplitPane.setShowDivider → 子 ContentDivider.visible/managed`

---

## FXStackPane
- 职责：堆叠面板，继承 `StackPane`，支持 flex/布局/主题/字体/节点适配。
- 字段：无
- 方法：`resize`、`layoutChildren`（子节点 autosize 后 super）
- 调用链：`FXStackPane.layoutChildren → Node.autosize`

---

## FXTitledPane
- 职责：可折叠标题面板，继承 `TitledPane`，支持追加标题文本与内容 flex 自适应。
- 字段：无（`showDivider` 等无关）
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 动画开启、hand 光标、`pickOnBounds`、`mnemonicParsing=false` |
  | `setAppendText(String)` | 追加标题文本 | 用 prop `text`/`appendText` 缓存原文，拼接后 setText |
  | `getAppendText()` | 取追加文本 | prop `appendText` |
  | `resize` | 尺寸调整 | 通用模式 |
  | `resizeNode(Double,Double)` | 内容尺寸 | 内容为 `FlexAdapter` 时按 flex 计算，否则 `NodeUtil` 设宽高 |
- 调用链：`FXTitledPane.setAppendText → setText`

---

## FXTab
- 职责：tab 页签，继承 `Tab`，支持关闭、选中、刷新标题/图标、右键菜单与追加文本。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `()`/`(String)`/`(String,Node)` | 创建 | |
  | `disable()/enable()` | 禁用/启用 | `StateAdapter.super` 并同步 graphic 禁用态 |
  | `tabs()` | 所属 tab 列表 | `getTabPane().getTabs()` |
  | `closeTab()/closeTabs(List<Tab>)` | 关闭 | `FXUtil.runWait` 移除并 `Event.fireEvent(Tab.CLOSED_EVENT)` |
  | `selectTab()` | 选中 | `runLater` 选择模型 select |
  | `flush()/flushTitle()/flushGraphic()/flushGraphicColor()` | 刷新 | `flush` 内依次调用三个钩子（默认空，供子类覆盖） |
  | `initNode()` | 初始化 | 设置 onClosed/onCloseRequest 回调 |
  | `onTabClosed/onTabCloseRequest(Event)`(protected) | 回调钩子 | 默认空 |
  | `setAppendText/getAppendText` | 追加文本 | 同 `FXTitledPane` |
  | `destroy()` | 销毁 | 清 content/回调、`ContextMenuManager.clearContextMenu`、`NodeDestroyUtil.destroyObject` |
- 调用链：`FXTab.closeTab → closeTabs → Event.fireEvent(Tab.CLOSED_EVENT)`

---

## FXTabLine
- 职责：tab 行控件，继承 atlantafx `TabLine`，管理 tab 增删选与选中回调。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `setInitIndex/getInitIndex()` | 初始索引 | 委托 `SelectAdapter` |
  | `removeTabClass/addTabClass(String)` | 样式类 | 增删 `tab-active` |
  | `reselect()` | 重选 | 记录当前选中→clear→select |
  | `selectedItemChanged(ChangeListener<Tab>)` | 选中事件 | 包装，`isIgnoreChanged()` 时忽略 |
  | `tabsSize()/tabsEmpty()`(protected) | 数量/空 | |
  | `addTab/setTab(多个重载)/removeTab(多个重载)/removeTabs(String...)` | 增删改 | 均走 `FXUtil.runWait/runLater` |
  | `selectTab(String)/isSelectedTab(String)/getSelectTabId()` | 选中查询 | 按 tabId 查找 |
  | `onTabSelected(String,Runnable)` | 选中回调 | 注册监听，匹配 id 时执行 task |
  | `tabSize()` | 数量 | |
  | `resize` | 尺寸调整 | 通用模式 |
  | `initNode()` | 初始化 | 加 `Styles.TABS_CLASSIC` |
  | `destroy()` | 销毁 | `clearChild` + `NodeDestroyUtil.destroyObject` |
- 调用链：`FXTabLine.onTabSelected → selectedItemChanged → task.run`

---

## FXTabPane
- 职责：tab 面板，继承 `TabPane`，功能同 `FXTabLine`，另含禁用 tab、高度设置、刷新。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `setInitIndex/getInitIndex` | 初始索引 | 委托 `SelectAdapter` |
  | `removeTabClass/addTabClass/reselect/selectedItemChanged/tabsSize/tabsEmpty` | 同 `FXTabLine` | |
  | `addTab/setTab/removeTab/removeTabs/selectTab/onTabSelected/isSelectedTab/tabSize/getSelectTabId` | tab 管理 | 同 `FXTabLine` |
  | `disableOtherTab(Tab)` | 禁用其他 tab | `runLater` 遍历设置 disable |
  | `enableTabs()` | 启用全部 | `runLater` 遍历启用 |
  | `setTabHeight(double)` | tab 高度 | `setTabMaxHeight/setTabMinHeight` |
  | `resize` / `resizeNode` | 尺寸 | `resizeNode` 对各 tab 内容按 flex 计算尺寸 |
  | `initNode()` | 初始化 | tab 最大高 30、去内边距；监听 tabs 增删创建/清除右键菜单 |
  | `setupRefreshListener()` | 安装刷新监听 | 选中变化时 `refresh()` |
  | `setTabRealHeight/getTabRealHeight` | 真实高度 | 取 max(最大,最小) |
  | `refresh()` | 刷新 | `applyCss + autosize + requestLayout` |
  | `destroy()` | 销毁 | `clearChild` + `NodeDestroyUtil.destroyObject` |
- 调用链：`FXTabPane.initNode → tabs 监听 → ContextMenuManager.setContextMenu`；`setupRefreshListener → refresh`

---

## FixedTab
- 职责：固定 tab 页签，继承 `FXTab`，不可关闭。
- 字段：无
- 方法：`initNode()` → `setClosable(false)` 后 `super.initNode()`
- 调用链：`FixedTab.initNode → FXTab.initNode`

---

## FXPopup
- 职责：弹出框控件，继承 `Popup`，支持相对节点显示与固定偏移显示。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `content(Node)/content()` | 内容读写 | `getContent().setAll(...)` / 取首元素 |
  | `show(Node)` | 在节点下方显示 | 计算节点屏幕坐标 + 高度后 `show` |
  | `showFixed(Node,double,double)` | 固定偏移显示 | 依据 `NodeUtil.isOrientationRightToLeft` 决定 X 方向，`runLater` 显示 |
  | `hide()` | 隐藏 | `FXUtil.runWait(super::hide)` |
  | `initNode()` | 初始化 | `autoFix/autoHide=true`；显示时 `NodeManager.init` |
- 调用链：`FXPopup.show → Popup.show(ownerNode, x, y)`

---

## ListViewPopup<E>
- 职责：列表弹出框，继承 `FXPopup`，内置 `FXListView` 展示可选项并回传选中项/索引。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `onItemSelected` / `onIndexSelected` | `Consumer<E>` / `Consumer<Integer>` | 选中项/索引回调 |
  | `selectedItem` / `selectedIndex` | `E` / `Integer` | 当前选中项/索引 |
  | `cellLineHeight` | `double` | 单行高（默认 20） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initPopup()`(protected) | 初始化弹窗 | 显示时初始化内容并算尺寸，隐藏时延迟清空 |
  | `initContent()`(protected) | 初始化内容 | 懒建 `FXListView`，注册选中回调，cellFactory 中选中项前加 `✔` |
  | `initListView(FXListView)`(protected) | 列表样式 | 字号 11、hand 光标 |
  | `listView()/getItems()/setItems(List)/clearItems()` | 列表数据 | 子类覆盖 `getItems` 提供数据 |
  | `calcListViewSize()` | 计算尺寸 | 按最宽文本算宽（上限 300），高=`cellLineHeight*min(size,10)+3` |
  | `show(Node,MouseEvent)` | 显示 | 按屏幕坐标显示 |
  | ctor 各属性访问器 | | getter/setter |
- 调用链：`ListViewPopup.showingProperty 变化 → initContent → calcListViewSize`；`选中 → onItemSelected.accept`

---

## SearchHistoryPopup
- 职责：搜索历史弹出框，继承 `FXPopup`，用列表展示历史并支持上/下一条导航。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `onHistorySelected` | `Consumer<String>` | 历史选中回调 |
  | `cellDataHeight` | `double` | 单行高（默认 20） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initContent()`(protected) | 初始化内容 | 懒建 `FXListView<String>`，注册选中回调与 cellFactory |
  | `listView()` | 列表组件 | 取内容首元素 |
  | getter/setter（回调/行高） | | |
  | `getHistories()/setHistories(List)/clearHistories()` | 历史数据 | 子类覆盖 `getHistories` 提供数据 |
  | `getPrevHistory(String)/getNextHistory(String)` | 上/下一条 | 按当前关键词索引查找相邻项 |
  | `calcListViewSize()` | 计算尺寸 | 同 ListViewPopup 逻辑 |
  | `show(Node,MouseEvent)` | 显示 | 按屏幕坐标 |
- 调用链：`SearchHistoryPopup.showingProperty 变化 → initContent/clearHistories`

---

## SVGGlyph
- 职责：SVG 图标控件，继承 `StackPane`，加载 svg 路径并按主题/激活态/尺寸渲染。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `url` | `String` | 图标地址 |
  | `color` | `Paint` | 图标颜色 |
  | `activeColor` | `Color` | 激活色（默认 ORANGERED） |
  | `waiting` | `Boolean` | 是否等待中 |
  | `enableWaiting` | `boolean` | 是否开启动画 |
  | `original` | `FXSVGPath` | 原始 svg 组件 |
  | `contentFunc` | `InvalidationListener` | 内容更新监听（延迟创建） |
  | `activeProperty` | `BooleanProperty` | 激活状态属性（延迟创建） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `isActive()/isWaiting()` | 状态查询 | |
  | `enableTheme()/disableTheme()` | 主题开关 | 增删样式类 `svg-glyph` |
  | `updateContent()`(私有) | 更新内容 | 同步鼠标光标、透明度，按激活/指定色/主题前景色更新颜色 |
  | `updateColor(FXSVGPath,Paint)`(protected) | 更新颜色 | setFill + setStroke |
  | `startWaiting()/stopWaiting()` | 等待动画 | 用 `ProgressIndicator` 替换内容/恢复原图标 |
  | `setOnMousePrimaryClicked(EventHandler)` | 单击事件 | 启用等待时点击前后自动 start/stopWaiting |
  | 构造 5 重载 | 创建 | 支持 url/color/size |
  | `setUrl(String)` | 加载 svg | `SVGManager.load` 得到 `FXSVGPath`，绑定 scale 到宽高 |
  | `setColor/getColor` | 颜色 | |
  | `setSize/setSizeStr/getSize/getSizeStr/calculateSize` | 尺寸 | 字符串支持 "宽" 或 "宽,高" |
  | `initNode()` | 初始化 | hand 光标、注册 cursor/disable/disabled 监听 |
  | `changeTheme(ThemeStyle)` | 主题变更 | 启用主题且未激活时更新内容 |
  | `active()/setActive(boolean)` | 激活 | 设置 `activeProperty` |
  | `disable()` | 禁用 | `StateAdapter.super.disable` + `setActive(false)` |
  | `clone()` | 克隆 | 用 url/color/size 复制 |
  | `disableWaiting/setEnableWaiting` `get/setWaiting` `get/setActiveColor` `get/setOriginal` `setStrokeWidth` | 访问器 | |
  | `destroy()` | 销毁 | 移除监听、销毁 original |
- 调用链：`SVGGlyph.setUrl → SVGManager.load → SVGLoader.load`；`changeTheme/激活/颜色变化 → updateContent`

---

## SVGLabel
- 职责：SVG 标签控件，继承 `FXLabel`，标签图形为 `SVGGlyph`。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `()`/`(String)`/`(String,SVGGlyph)` | 创建 | hand 光标 |
  | `graphic()` | 图形 | 强转 `getGraphic()` 为 `SVGGlyph` |
  | `setUrl/getUrl` `setSize/getSize` `setSizeStr/getSizeStr` `setColor/getColor` | 代理 | 全部转发到 `graphic()` |
- 调用链：`SVGLabel.setUrl → graphic().setUrl`

---

## FXSVGPath
- 职责：SVG 路径控件，继承 `SVGPath`，支持属性与销毁。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `()`/`(String content)` | 创建 | 可设路径内容 |
  | `setColor(Paint)` | 设颜色 | setFill + setStroke |
  | `destroy()` | 销毁 | 解绑 fill/fillRule/content 属性 |
- 调用链：无

---

## SVGPane
- 职责：svg 面板，继承 `FXPane`，承载单个 svg 组件并管理尺寸字符串。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `size` | `String` | 尺寸（"宽" 或 "宽,高"） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getSize/setSize(String)` | 尺寸 | set 时同步到首个子节点的 `SVGGlyph`/`SVGLabel` |
  | `getSizeWidth()/getSizeHeight()` | 尺寸宽/高 | 解析 `size` |
  | `initNode()` | 初始化 | 去内边距 |
- 调用链：`SVGPane.setSize → 子节点 setSizeStr`

---

## ScalingSVGGlyph
- 职责：可缩放 SVG 图标，继承 `SVGGlyph`，按缩放因子计算实际尺寸并修正内边距。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 4 重载 | 创建 | |
  | `calculateSize(double)`(protected) | 计算尺寸 | 按 size/width/height 缩放因子计算宽高，必要时用内边距补足 |
  | `sizeScaling()/widthScaling()/heightScaling()` | 缩放因子 | 默认 1.0，供子类覆盖 |
- 调用链：`ScalingSVGGlyph.setSize → SVGGlyph.setSizeStr → calculateSize`

---

## SVGLoader
- 职责：svg 加载器（单例），解析 svg 文件的 path 与填充/描边属性，构造 `FXSVGPath`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `INSTANCE` | `SVGLoader`(常量) | 单例实例 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `load(String url)` | 加载 | `ResourceUtil.getResource` 定位文件，`XMLReader` 解析 `<path>`（或 `<g><path>`），拼接 d 内容并读取 fill/stroke/opacity/stroke-width，构造 `FXSVGPath` 并套用颜色（支持 `#hex` 与 `rgb(...)`） |
  | `parseRgb(String)`(私有) | 解析 rgb | 解析 `rgb(r,g,b)` 为 int[] |
- 调用链：`SVGManager.load → SVGLoader.INSTANCE.load → FXSVGPath`

---

## SVGManager
- 职责：svg 管理器，静态入口，委托 `SVGLoader` 加载。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `load(String url)` | 加载 svg | `SVGLoader.INSTANCE.load(url)` |
- 调用链：`SVGManager.load → SVGLoader.load`

---
