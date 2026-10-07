# fx-plus 节点（node）

覆盖 `cn.oyzh.fx.plus.node` 包全部 12 个类。其中 `NodeResizer`/`NodeHeightResizer`/`NodeWidthResizer` 标注 `@Deprecated`，但仍为正式代码。

## NodeAdapter
- 职责：节点适配接口，为 Node/Tab/TableColumn 等提供父子关系、子节点增删、尺寸、窗口、样式、焦点等通用操作。
- 字段：无
- 方法（要点）：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化钩子 | 默认空 |
  | `parent()/prevSibling()/nextSibling()/parentAutosize()` | 父子/兄弟 | 按 Node/TableColumn/Tab 分派 |
  | `parentWidth()/parentHeight()` | 父尺寸 | `NodeUtil.getWidth/Height` |
  | `isChildEmpty()/firstChild()/getFirstChild()/getChild(int)` | 子节点查询 | 按 Pane/Group/TabPane/TableView/ScrollPane 分派 |
  | `clearChild()/addChild(...)/setChild(...)/removeChild(...)` | 子节点增删改 | 均 `FXUtil.runWait` 内操作 |
  | `removeNode()` | 移除自身 | `NodeUtil.removeNode` |
  | `addClass/removeClass(String)` | 样式类 | |
  | `isEnable()` | 是否启用 | 非 disable/disabled |
  | `stage()/scene()/window()` | 窗口链 | Node/Tab→Scene→Window |
  | `getCursorType/setCursorType(String)` | 光标 | |
  | `clearFocus()/focusNode()` | 焦点 | 反射 `focusCleanup`/`setFocusOwner` |
  | `setOpaque(boolean)` | 不透明度 | true=1.0，false=0.5 |
- 调用链：`NodeManager.init → NodeAdapter.initNode`；`NodeAdapter.removeNode → NodeUtil.removeNode`

---

## NodeLifeCycle
- 职责：节点生命周期标记接口，记录节点已初始化状态并在销毁时清除。
- 字段：无（prop 键 `node:initialize`）
- 方法：`onNodeInitialize()`（置标记）、`onNodeDestroy()`（清标记）、`isNodeInitialize()`（查询）
- 调用链：`NodeManager.init → 监听 parent/scene 变化 → onNodeInitialize/onNodeDestroy`

---

## NodeLifeCycleUtil
- 职责：节点生命周期工具类，销毁时递归通知节点生命周期回调。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `onStageDestroy(Stage)` | 舞台销毁 | 取 scene 根后 `onParentDestroy` |
  | `onParentDestroy(Parent)` | 递归销毁 | 对 `NodeLifeCycle` 子节点调 `onNodeDestroy` 并递归 |
- 调用链：`StageAdapter.onWindowClosed → NodeLifeCycleUtil.onStageDestroy`

---

## NodeGroup
- 职责：节点分组接口，通过分组 id（支持逗号分隔多值）将节点归组。
- 字段：无（prop 键 `_groupId`）
- 方法：`setGroupId/getGroupId/groupId(String)/groupId()`（基于 `PropAdapter`）
- 调用链：`NodeGroupUtil.hasGroupId → NodeGroup.getGroupId`

---

## NodeGroupUtil
- 职责：节点分组工具，在节点树中查找指定分组并统一启用/禁用/显示/隐藏。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `hasGroupId(NodeGroup,String)` | 是否含分组 | id 逗号拆分匹配 |
  | `list(Object,String[,List])` | 列举分组 | 递归 Stage/Scene/TabPane/Tab/TableColumn/Parent |
  | `disable/enable/display/disappear(Object,String)` | 分组操作 | 搜集后逐个 `NodeUtil.disable/enable/...` |
- 调用链：`NodeGroupUtil.disable → NodeUtil.disable`

---

## NodeManager
- 职责：节点管理器，统一初始化节点并应用透明度、字体、国际化、生命周期、亚像素等适配。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `init(Object)` | 初始化 | 依次判断 `NodeAdapter.initNode`、`OpacityAdapter.changeOpacity`、`FontAdapter.changeFont`、`I18nAdapter.changeLocale`、`I18nSelectAdapter.values`；对 `NodeLifeCycle` 监听 parent/scene/tabPane 变化；Region 设 `snapToPixel` |
- 调用链：`NodeManager.init → NodeAdapter.initNode / OpacityAdapter / FontAdapter / I18nAdapter`

---

## NodeDestroyUtil
- 职责：节点销毁工具，释放媒体/图片/皮肤/提示并递归销毁 FXML 注入字段。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `destroyNode(Object)` | 销毁节点资源 | 清上下文菜单；MediaView 停/释放播放器；ImageView 取消图片；Control 释放 skin/提示；Tab 清提示 |
  | `destroyObject(Object)` | 异步销毁对象 | `ThreadUtil.startVirtual` 内 `doDestroyObject` |
  | `doDestroyObject(Object,List)`(私有) | 递归销毁 | 反射遍历非静态字段，`@FXML` 字段移除节点并 `destroy`，置空非 final 字段 |
- 调用链：`FXComboBox.destroy → NodeDestroyUtil.destroyNode/destroyObject`

---

## NodeMutexes
- 职责：节点互斥器，管理一组节点并保证同一时刻仅一个可见。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `nodes` | `List<Node>` | 节点列表 |
  | `manageBindVisible` | `Boolean` | managed 是否随 visible 联动 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getNodes()` | 节点集合 | |
  | `manageBindVisible()/setManageBindVisible/isManageBindVisible` | managed 联动 | |
  | `addNode/addNodes(...)` | 添加节点 | |
  | `visible(Node)` | 显示指定节点 | 其它节点隐藏；未绑定 managed 时按开关同步 |
  | `destroy()` | 销毁 | 清空列表 |
- 调用链：`NodeMutexes.visible → node.setVisible/setManaged`

---

## NodeUtil
- 职责：节点工具类，获取/设置尺寸与布局、启用/显示控制、移除节点、常见交互辅助。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `isWebImport`/`isSwingImport`/`isMediaImport` | `static boolean` | 可选模块是否支持（静态块按 `ConditionalFeature` 探测） |
- 方法（要点）：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getWidth/getHeight(EventTarget)` | 取尺寸 | 按 ImageView/Region/TableColumn/Scene/Stage/StageAdapter/Window/MediaView/Node 分派 |
  | `setSize/setWidth/setHeight(Object,Double)` | 设尺寸 | 已绑定/未托管则跳过；分派到各控件 |
  | `setLayoutX/setLayoutY(EventTarget,Double)` | 设坐标 | 未绑定才设 |
  | `disable/enable(Object)` | 启用禁用 | 分派 Node/MenuItem/Tab/Stage/StageAdapter |
  | `display/disappear(Object)` | 显示隐藏 | Node 联动 managed；Stage/Popup 走 show/close/hide |
  | `nodeOnCtrlS(Object,Runnable)` | Ctrl+S 绑定 | 事件过滤器 `KeyboardUtil.isCtrlS` |
  | `unFocus/clearFocus(Node)` | 焦点 | |
  | `isOrientationRightToLeft(Node)` | 布局方向 | |
  | `removeNode(Object)` | 移除节点 | 从 Pane/TabPane/父 TreeItem 移除 |
- 调用链：`NodeAdapter.removeNode → NodeUtil.removeNode`；`NodeGroupUtil → NodeUtil.display/disappear`

---

## NodeResizer（@Deprecated）
- 职责：组件拉伸器抽象基类，处理鼠标移动/按下/拖动/释放以触发尺寸变化。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `eventNode` | `Node` | 事件节点 |
  | `originalCursor` | `Cursor` | 原始光标 |
  | `resizeIng` | `Boolean` | 拉伸中标志 |
  | `mousePressedTime` | `long` | 按下时间 |
  | `triggerThreshold` | `Byte` | 触发阈值（默认 3） |
  | `mouseMoved/mouseExited/mousePressed/mouseDragged/mouseReleased` | `EventHandler<MouseEvent>` | 各事件处理器 |
  | `resizeTriggered` | `Consumer<Float>` | 拉伸回调 |
  | `minValue`/`maxValue` | `Float` | 最小/最大值 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `(Node,Cursor,Consumer<Float>)` | 构造 | |
  | `widthLimit(float,float)` | 设置范围 | |
  | `mouseXxx()` | 取事件（懒建） | 委托 `defaultMouseXxx()` |
  | `defaultMouseXxx()` | 抽象/默认事件 | moved/pressed/dragged 抽象；exited/released 默认 |
  | `initResizeEvent()` | 注册事件 | 逐个 `addEventFilter` |
  | `isResizeIng()/triggerAble(float)` | 状态判定 | 阈值判定 |
  | `setNodeCursor(Cursor)` | 设光标 | |
  | `resizeAble(MouseEvent)` | 抽象 | 是否可拉伸 |
  | `minValue()/maxValue()/triggerThreshold()` | 取值 | 带默认 |
  | `clear()/destroy()` | 清理 | destroy 移除事件 |
- 调用链：`NodeResizer.initResizeEvent → addEventFilter`；拖动 → `resizeTriggered.accept`

---

## NodeHeightResizer（@Deprecated）
- 职责：高度拉伸器，继承 `NodeResizer`，按垂直偏移调整高度。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `defaultMouseMoved/Pressed/Dragged` | 事件 | moved 设 `S_RESIZE` 光标；dragged 调 `resizeTriggered(calcNodeHeight)` |
  | `calcYOffset(MouseEvent)` | Y 偏移 | 节点屏幕 Y 与鼠标 Y 差 |
  | `calcNodeHeight(MouseEvent)` | 计算高度 | |
  | `resizeAble(MouseEvent)` | 可拉伸 | 高度在 min/max 之间 |
  | `of(...)` 4 重载 | 静态工厂 | 创建并 `initResizeEvent` |
- 调用链：`NodeHeightResizer.of → initResizeEvent`；拖动 → `resizeTriggered`

---

## NodeWidthResizer（@Deprecated）
- 职责：宽度拉伸器，继承 `NodeResizer`，按水平偏移调整宽度。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `defaultMouseMoved/Pressed/Dragged` | 事件 | moved 设 `W_RESIZE` 光标；dragged 调 `resizeTriggered(calcNodeWidth)` |
  | `calcXOffset(MouseEvent)` | X 偏移 | |
  | `calcNodeWidth(MouseEvent)` | 计算宽度 | |
  | `resizeAble(MouseEvent)` | 可拉伸 | 宽度在 min/max 之间 |
  | `of(...)` 4 重载 | 静态工厂 | 创建并 `initResizeEvent` |
- 调用链：`NodeWidthResizer.of → initResizeEvent`；拖动 → `resizeTriggered`

---
