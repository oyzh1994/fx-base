# fx-tty UI 修复与实现边界记录

- 日期：2026-10-27，搜索与右键菜单更新：2026-10-28
- 范围：`fx-tty` 的 JavaFX 终端 UI
- 结论：保留已确认的输入法、鼠标事件域和搜索修复；右键菜单按 Swing 的“每次新建”语义重新实现，
  并通过显示前同步销毁旧菜单解决 JavaFX 菜单叠加问题

## 回滚原因

上一轮试图按照 Swing `jediterm-ui 3.76` 的方法结构同步 JavaFX 实现，但 JavaFX UI 模块并不是 Swing UI 的逐行移植，
两者在菜单生命周期、滚动刷新和事件传播上的边界不同。实际复测出现两个回归：

1. 右键菜单改为每次创建新菜单后，JavaFX 没有复用旧菜单的显示/隐藏语义，导致菜单不断叠加。
2. 删除 `FXJediTermWidget.findText()` 中的 `myScrollBar.requestLayout()` 后，终端内容变化不会继续触发搜索刷新，
   例如先搜索 `a`，清屏再输入 `top` 时没有出现新的匹配结果。

因此当时不再把 Swing 的 `TerminalPanel` 方法结构直接套用到 JavaFX 实现，全部 UI 行为改动回滚；
后续只重新实现经过事件和生命周期验证的搜索修复及右键菜单修复。

## 历史 P0 标记澄清

此前分析中曾把“Canvas 绘制后没有真正进入 JavaFX pulse”临时标为 P0/P1，但后续核实 `WeakRedrawTimer` 是由
JavaFX `Timeline` 在 FX 线程回调，`doRepaint()` 直接写入持久化 `GraphicsContext`，属于 JavaFX 的正常绘制方式。
该结论是误报，最终源码对照结论中没有 P0；该阶段只处理当时唯一确定的问题，即终端鼠标监听的事件作用域。

## 最终保留内容

### 右键菜单创建与防叠加

- 文件：`src/main/java/com/jediterm/terminal/ui/FXTerminalPanel.java`
- 上游对照：Swing `TerminalPanel.mouseClicked()` 在 `BUTTON3` 分支查找当前超链接、创建新的 `JPopupMenu`，
  然后填充 `TerminalAction` 并在鼠标位置显示；`createPopupMenu()` 本身不复用菜单。
- JavaFX 实测事件顺序：`MOUSE_PRESSED(SECONDARY) -> CONTEXT_MENU_REQUESTED -> MOUSE_RELEASED(SECONDARY)
  -> MOUSE_CLICKED(SECONDARY)`。鼠标右键统一在 `MOUSE_CLICKED(SECONDARY)` 进入菜单创建，避免和
  `ContextMenuEvent` 重复弹窗。
- 鼠标路径：`MOUSE_CLICKED(SECONDARY)` 使用真实 `MouseEvent` 获取当前超链接上下文，调用
  `showPopupMenu(provider, canvas, event)`。
- 键盘路径：`setOnContextMenuRequested` 始终消费事件；仅当 `event.isKeyboardTrigger()` 为真时创建菜单，
  并传入 `ContextMenuEvent` 的坐标。鼠标生成的同类型事件不再显示第二份菜单。
- 生命周期：`showPopupMenu()` 在创建新菜单前同步调用 `disposePopupMenu()`。后者先清空 `popup` 字段，
  再隐藏旧菜单并调用 `ContextMenuManager.clearContextMenu(menu, false)` 清理菜单项、动作和快捷键。
  因此每次打开都使用新鲜菜单和当前超链接上下文，但不会保留或叠加旧窗口。
- 关闭路径：主键按压、主键点击、`dispose()` 和 `destroy()` 都会清理当前菜单。`ContextMenuManager` 对
  `ContextMenu` 使用 `ObjectWatcherManager` 的弱引用观察，不会因频繁创建菜单而强引用保留对象。
- 边界：JavaFX 仍使用 `ContextMenu`/`MenuItem`，没有照搬 Swing 的 `JPopupMenu` 类型和 AWT 坐标 API；
  防叠加依赖 JavaFX 侧的同步 `disposePopupMenu()`，不能只照抄 Swing 的 `new JPopupMenu()`。

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

### 搜索状态失效与清屏刷新

- 文件：`src/main/java/com/jediterm/terminal/ui/FXJediTermWidget.java`、
  `src/main/java/com/jediterm/terminal/ui/FXTerminalPanel.java`
- 问题：搜索完成后只保存一次 `FindResult`。终端执行 `clear` 并输出 `top` 后，旧结果仍引用清屏前的行坐标和
  `CharBuffer`，因此向下查找会跳到旧内容，当前内容也无法命中高亮。
- 上游对齐：`TerminalPanel.setFindResult()` 只保存结果并重绘，不自动修改选区；上下查找只执行一次选区更新和滚动。
  本地实现已恢复相同语义，并移除重复的选区/滚动设置。
- 刷新修复：搜索框打开且查询非空时监听 `TerminalTextBuffer` 的模型变化；使用 100ms 去抖，在 JavaFX 线程上
  重新执行 `TerminalSearchUtil.searchInTerminalTextBuffer()`。刷新时使用 `tryLock()`，避免阻塞终端解析线程。
- 生命周期：关闭或隐藏搜索、关闭终端、销毁组件时移除模型监听并取消待执行刷新，重新打开搜索可正常重建监听。
- 边界：保留 JavaFX 所需的 `myScrollBar.requestLayout()`；滚动条命中标记由独立覆盖层实现，见下一节。

### 搜索滚动条命中标记

- 文件：`src/main/java/com/jediterm/terminal/ui/FXTerminalPanel.java`
- 上游对照：Swing `FindResultScrollBarUI.paintTrack()` 使用
  `where = trackHeight * item.start.y / modelHeight` 计算标记位置，使用
  `Math.max(2, trackHeight / modelHeight)` 计算标记高度，并以 `getFoundPatternColor().getBackground()`
  经终端调色板转换后的颜色绘制。
- JavaFX 实现：在 `FXTerminalPanel` 中增加一个 `Canvas` 覆盖层，覆盖层 `managed=false`、
  `mouseTransparent=true`，作为滚动条兄弟节点放在画布和滚动条之后。`layoutChildren()` 每次布局后把覆盖层
  对齐到滚动条的父坐标、尺寸，并重绘标记，不修改 `ScrollBar` 的 skin、thumb、事件和滚动值。
- 刷新链路：`setFindResult()`、滚动条宽高变化、`min/max` 变化、主题切换、终端缩放和 `dispose()` 都会触发
  覆盖层重绘或清理；覆盖层透明度跟随滚动条透明度，因此在主题变暗、滚动条半透明时仍保持一致。
- 边界：标记绘制在覆盖层上，不参与终端 Canvas 的文本绘制和搜索算法；JavaFX 没有 Swing 的
  `BasicScrollBarUI#paintTrack`，因此使用覆盖层而不是替换 ScrollBar skin。覆盖层不接收鼠标事件，不影响拖动滑块、
  右键菜单和终端 mouse reporting。

## 已回滚内容（右键菜单除外）

以下历史改动仍保持撤销状态；右键菜单已按本节新增设计重新实现：

- 上一轮 `FXTerminalPanel.init()` 的结构性事件重构；本轮只重新实现右键鼠标/键盘分流，不复用该重构的其他部分。
- 上一轮“把命中标记直接画到终端 Canvas 上”的方案已回滚；本轮改为独立滚动条覆盖层，不改变终端文本绘制。
- 上一轮对 `setFindResult()`、`selectPrevOrNextFindResultItem()` 的结构性重写已回滚；本轮搜索修复见上文。
- `scrollArea()` 的 JavaFX 线程切换改造。
- `FXJediTermWidget.findText()` 中 `myScrollBar.requestLayout()` 的删除。
- `FXJediTermWidget.destroy()` 中额外调用面板 `dispose()`。
- `FXPreConnectHandler.keyTyped()` 的空字符保护也一并回滚。
- `TtyZModemSshIntegrationTest` 中测试密码读取方式的改动也一并回滚。

## JavaFX UI 边界

- 右键菜单要保留 Swing “每次打开创建新菜单”的语义，但必须在 JavaFX 侧显示新菜单前同步销毁旧菜单；
  不能只创建不清理，也不能为了省对象而复用带有过期超链接上下文和启用状态的旧菜单。
- 鼠标右键和键盘上下文菜单必须分流：鼠标事件已在 `MOUSE_CLICKED` 处理，`ContextMenuEvent` 只负责清理与
  键盘触发，否则同一次右键会产生两份菜单。
- `myScrollBar.requestLayout()` 是 JavaFX 搜索刷新链路的一部分，不是 Swing 中可删除的冗余调用。
- JavaFX `ScrollBar` 没有 Swing `BasicScrollBarUI#paintTrack` 的直接对应物；当前命中标记使用独立 Canvas
  覆盖层，后续修改应单独验证菜单、输入、布局和搜索刷新，不应顺带重构事件或生命周期代码。
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
- JavaFX 真实机器人右键冒烟：在终端不同位置连续右键 4 次，窗口数始终保持 `baseline + 1`，每次均为可见、
  非空菜单；主键按压后菜单窗口消失。
- JavaFX 键盘菜单冒烟：连续触发 3 次 `ContextMenuEvent(keyboardTrigger=true)`，窗口数保持 `baseline + 1`；
  随后合成鼠标型 `ContextMenuEvent` 不会重复弹窗，主键按压后 `popup` 字段和菜单窗口均被清理。
- JavaFX 菜单内容/替换冒烟：使用与真实应用一致的 FX 动作配置，菜单包含 12 个动作和 4 个分隔项；第二次右键
  创建新菜单后，旧菜单已隐藏且菜单项已清空。
- JavaFX 搜索清屏冒烟：旧内容命中 `6` 项，清屏写入新内容后自动刷新为 `1` 项，高亮区间 `1` 个，向下查找命中新内容。
- JavaFX 搜索冒烟：搜索框保持打开时修改终端内容和查询词，查找结果数量同步更新。
- JavaFX 搜索生命周期冒烟：关闭搜索后状态清空，重新打开后搜索正常。
- JavaFX 鼠标事件域冒烟：滚动条按压/滚轮进入终端监听 `0` 次，Canvas 按压/滚轮进入 `2` 次。
- JavaFX 滚动条标记冒烟：200 行内容搜索 `needle` 命中 10 项，覆盖层 700px 高时生成 30 行像素标记；追加输出后
  标记自动刷新为 33 行；清屏并输出 3 行 `top` 后只保留 3 项对应标记；窗口缩放后标记位置重新计算；清空搜索后
  覆盖层像素全部清除。
- JavaFX 滚动条主题/交互冒烟：覆盖层透明度跟随滚动条；真实右键连续 3 次窗口数保持 `baseline + 1`；标记覆盖层
  `mouseTransparent=true`，不拦截滚动条操作。
- 生产代码差异保留输入法 null 安全、终端鼠标监听事件作用域、搜索刷新修复、滚动条命中标记和右键菜单生命周期修复；
  其他 UI 逻辑保持仓库基线。

## 人工复测重点

1. 连续多次右键，确认始终只有一个菜单，不叠加、不复用陈旧内容。
2. 搜索 `a`，清屏后输入 `top`，确认匹配结果随终端内容变化刷新。
3. 验证普通输入、中文输入法组合输入和 SSH 首次密码输入。
4. 验证终端鼠标选择、滚轮、右键、搜索导航和关闭终端标签。

## 人工复测：滚动条命中标记

1. 连接任意可用终端，执行 `seq 1 200` 或输出足够多的行，让滚动条出现历史区。
2. 打开搜索，输入 `1` 或 `needle` 等能命中多行的内容，确认右侧滚动条上出现与命中数量对应的色块标记。
3. 保持搜索框打开，执行 `clear`，再执行 `top` 或输出几行新内容，确认旧标记消失、只显示当前内容的命中标记，
   且上下查找仍能定位当前内容。
4. 拖动窗口改变高度，确认标记随滚动条重新定位。
5. 关闭搜索或清空搜索词，确认滚动条标记全部消失。
6. 在滚动条上拖动滑块、滚轮滚动并右键终端，确认操作正常，不被标记层拦截。
