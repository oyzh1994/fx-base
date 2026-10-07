# fx-plus 基础控件（controls 根包）

覆盖 `cn.oyzh.fx.plus.controls` 根包 7 个类。这些控件均继承 JavaFX/atlantafx 标准控件，并组合若干能力适配接口（`FlexAdapter`/`ThemeAdapter`/`StateAdapter`/`FontAdapter`/`NodeAdapter`/`NodeGroup`/`LayoutAdapter` 等），统一在实例初始化块中调用 `NodeManager.init(this)`，并重写 `resize` 以支持 flex 尺寸计算。

## FXAccordion
- 职责：手风琴容器，继承 `Accordion`，支持 flex/主题/字体/状态/节点/布局适配。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `resize(double, double)` | 尺寸调整 | `computeSize(w,h)` 后 `super.resize` 并 `resizeNode()` |
- 调用链：`FXAccordion.resize → FlexAdapter.computeSize → resizeNode`

---

## FXCanvas
- 职责：画布控件，继承 `Canvas`，支持主题/字体/状态/鼠标/提示/布局适配。
- 字段：无（实例块 `NodeManager.init(this)`）
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `resize(double, double)` | 尺寸调整 | `computeSize` → `super.resize` → `resizeNode()` |
- 调用链：`FXCanvas.resize → FlexAdapter.computeSize → resizeNode`

---

## FXGroup
- 职责：分组容器，继承 `Group`，支持节点与状态适配。
- 字段：无
- 方法：无（仅继承适配接口默认实现）
- 调用链：无

---

## FXHeaderBar
- 职责：标题栏控件，继承 `HeaderBar`（左侧内容、中间标题、右侧操作区）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `TITLE_PADDNG` | `Insets`(常量) | 标题内边距 `(-3,0,0,20)` |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 实例块 | 初始化 | `NodeManager.init`；固定高度 30、`id="headerBar"` |
  | `getContent()/setContent(Node)` | 读写左侧内容 | 委托 `getLeft/setLeft` |
  | `getTitleLabel()/setTitleLabel(FXLabel)`(私有) | 中心标签读写 | 标题为 `FXLabel`，设置内边距、不可鼠标穿透、宽度取屏幕宽、加粗 |
  | `initTitleLabel()`(私有) | 初始化标题 | 无则新建 FXLabel |
  | `getIcon()/setIcon(Node)` | 读写图标 | 操作 `FXLabel.getGraphic` |
  | `getTitle()/setTitle(String)` | 读写标题 | `label.getText()` / `label.text(...)` |
  | `changeTheme(ThemeStyle)` | 主题变更 | 先 `ThemeAdapter.super`，再对 left/right/center 若为 `ThemeAdapter` 逐个 `changeTheme` |
- 调用链：`ThemeManager.apply → FXHeaderBar.changeTheme → 子节点 ThemeAdapter.changeTheme`

---

## FXProgressBar
- 职责：进度条控件，继承 `ProgressBar`，支持 flex/主题/字体/状态/鼠标/提示/布局适配。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `resize(double, double)` | 尺寸调整 | `computeSize` → `super.resize` → `resizeNode()` |
  | `progress(double)` | 设置进度 | `FXUtil.runWait` 内在 FX 线程 `super.setProgress` |
- 调用链：`FXProgressTextBar.setProgress → FXProgressBar.progress → FXUtil.runWait → setProgress`

---

## FXProgressTextBar
- 职责：带文本的进度条，继承 `FXHBox`，由 `FXProgressBar` + `FXLabel` 组合。
- 字段：无（实例块添加进度条与标签，左对齐）
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `progressBar()` | 进度条组件 | `getFirstChild()` |
  | `label()` | 文本标签 | `getChild(1)` |
  | `setProgress(double)` / `setProgress(current,total)` | 设置进度 | 委托 `progressBar().progress`；两参版用 `current/total` |
  | `setText(String)` / `setText(current,total)` | 设置文本 | 两参版计算百分比 `(int)(current/total*100)+"%"` |
  | `setValue(current,total)` | 同时更新文本与进度 | `setText` + `setProgress` |
  | `getProgress()` | 取进度 | `progressBar().getProgress()` |
- 调用链：`FXProgressTextBar.setValue → setText/setProgress → FXProgressBar.progress`

---

## FXScrollBar
- 职责：滚动条控件，继承 `ScrollBar`（无额外逻辑）。
- 字段：无
- 方法：无
- 调用链：无

---
