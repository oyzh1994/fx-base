# fx-tty UI 回滚与实现边界记录

- 日期：2026-10-27
- 范围：`fx-tty` 的 JavaFX 终端 UI
- 结论：撤销上一轮对 `FXTerminalPanel` 和 `FXJediTermWidget` 的结构性改动，恢复原有稳定实现

## 回滚原因

上一轮试图按照 Swing `jediterm-ui 3.76` 的方法结构同步 JavaFX 实现，但 JavaFX UI 模块并不是 Swing UI 的逐行移植，
两者在菜单生命周期、滚动刷新和事件传播上的边界不同。实际复测出现两个回归：

1. 右键菜单改为每次创建新菜单后，JavaFX 没有复用旧菜单的显示/隐藏语义，导致菜单不断叠加。
2. 删除 `FXJediTermWidget.findText()` 中的 `myScrollBar.requestLayout()` 后，终端内容变化不会继续触发搜索刷新，
   例如先搜索 `a`，清屏再输入 `top` 时没有出现新的匹配结果。

因此不再把 Swing 的 `TerminalPanel` 方法结构直接套用到 JavaFX 实现。稳定性优先，全部 UI 行为改动回滚。

## 历史 P0 标记澄清

此前分析中曾把“Canvas 绘制后没有真正进入 JavaFX pulse”临时标为 P0/P1，但后续核实 `WeakRedrawTimer` 是由
JavaFX `Timeline` 在 FX 线程回调，`doRepaint()` 直接写入持久化 `GraphicsContext`，属于 JavaFX 的正常绘制方式。
该结论是误报，最终源码对照结论中没有 P0；本次只处理当时唯一确定的问题，即终端鼠标监听的事件作用域。

## 最终保留内容

### 输入法 committed 文本 null 安全

- 文件：`src/main/java/com/jediterm/terminal/ui/FXTerminalPanel.java`
- 方法：`processInputMethodEvent`
- 修改：`getCommitted() == null` 时按空字符串继续处理组合输入，不再提前返回。
- 边界：仅处理 JavaFX 输入法事件可能给出的 null committed 文本，不改变原有输入法组合显示和发送流程。

### 终端鼠标监听事件作用域

- 文件：`src/main/java/com/jediterm/terminal/ui/FXTerminalPanel.java`
- 方法：`addTerminalMouseListener`
- 上游对照：Swing 的 `TerminalPanel.addTerminalMouseListener()` 把按压、释放、滚轮、移动和拖动监听注册在终端组件
  `TerminalPanel` 自身；`JScrollBar` 是独立兄弟节点，不会进入终端鼠标事件。
- 修复前：JavaFX 版本把同类监听注册在 `FXTerminalPanel`（包含 Canvas 和 ScrollBar 的 `FXHBox`），事件捕获阶段会收到
  滚动条事件，远端程序开启 mouse reporting 后可能把滚动条操作误编码成终端鼠标事件。
- 修复：将 5 个事件过滤器从 `this` 移到终端组件 `this.canvas`，事件处理、坐标转换和远端 mouse reporting 逻辑均不变。
- 边界：不修改滚动条自身的本地滚动处理，不修改菜单、搜索、输入法和生命周期代码。

## 已回滚内容

以下改动已全部撤销，恢复仓库原有实现：

- `FXTerminalPanel.init()` 的事件注册、监听器字段和右键菜单改造。
- 查找结果滚动条覆盖画布及 `paintFindResultScrollBarTrack()`。
- `setFindResult()`、`selectPrevOrNextFindResultItem()` 的行为调整。
- `addTerminalMouseListener()` 从面板改挂 Canvas。
- `scrollArea()` 的 JavaFX 线程切换改造。
- `createPopupMenu()` 的每次新建菜单改造。
- `dispose()` / `destroy()` 的生命周期扩展清理。
- `FXJediTermWidget.findText()` 中 `myScrollBar.requestLayout()` 的删除。
- `FXJediTermWidget.destroy()` 中额外调用面板 `dispose()`。
- `FXPreConnectHandler.keyTyped()` 的空字符保护也一并回滚。
- `TtyZModemSshIntegrationTest` 中测试密码读取方式的改动也一并回滚。

## JavaFX UI 边界

- 右键菜单必须遵循 `ContextMenuManager` 和现有 `popup` 复用逻辑，不能照搬 Swing 每次新建菜单的方式。
- `myScrollBar.requestLayout()` 是 JavaFX 搜索刷新链路的一部分，不是 Swing 中可删除的冗余调用。
- JavaFX `ScrollBar` 没有 Swing `BasicScrollBarUI#paintTrack` 的直接对应物；后续如需查找标记，应单独验证菜单、
  输入、布局和搜索刷新，不应顺带重构事件或生命周期代码。
- JavaFX 的 Canvas 事件过滤器、ContextMenuEvent、输入法事件和线程模型与 Swing 不同，不能按 Swing 方法行逐行替换。

## 验证命令

```bash
mvn -pl fx-tty -DskipTests package
mvn -pl fx-tty -Dtest='!TtyZModemSshIntegrationTest' test
```

## 验证结果

- `mvn -pl fx-tty -DskipTests package`：`BUILD SUCCESS`。
- `mvn -pl fx-tty -Dtest='!TtyZModemSshIntegrationTest' test`：`Tests run: 13, Failures: 0, Errors: 0`。
- JavaFX 右键冒烟：在同一个终端上连续请求右键 3 次，窗口数保持 `baseline + 1`，只有 1 个右键菜单，没有叠加。
- JavaFX 搜索冒烟：搜索框保持打开时修改终端内容和查询词，查找结果数量同步更新。
- JavaFX 鼠标事件域冒烟：滚动条按压/滚轮进入终端监听 `0` 次，Canvas 按压/滚轮进入 `2` 次。
- 生产代码差异仅保留输入法 null 安全和终端鼠标监听事件作用域修复；`FXJediTermWidget` 与仓库基线一致。

## 人工复测重点

1. 连续多次右键，确认始终只有一个菜单，不叠加、不复用陈旧内容。
2. 搜索 `a`，清屏后输入 `top`，确认匹配结果随终端内容变化刷新。
3. 验证普通输入、中文输入法组合输入和 SSH 首次密码输入。
4. 验证终端鼠标选择、滚轮、右键、搜索导航和关闭终端标签。
