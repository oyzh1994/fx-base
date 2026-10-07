# fx-plus 输入类控件（controls 之 box/button/combo/picker/toggle/hex/text/label/image）

覆盖 `controls` 下的 box(2)、button(6)、combo(1)、picker(2)、toggle(2)、hex(2)、text(6)、label(1)、image(1)，共 23 个正式类。跳过的整文件死代码：`text/FXSeparator1`、`text/field/FXCustomTextField`。
通用模式：多数控件继承标准控件 + 组合能力适配接口，实例块 `NodeManager.init(this)`，`resize` 走 `computeSize → super.resize → resizeNode`。

## FXHBox
- 职责：水平布局容器，继承 `HBox`，支持 flex/主题/字体/状态/布局/节点组适配。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `FXHBox()` / `FXHBox(Node...)` | 创建 | 可带子节点 |
  | `resize(double,double)` | 尺寸调整 | `computeSize → super.resize → resizeNode` |
  | `layoutChildren()` | 布局子节点 | 先对每个子节点 `autosize()` 再 `super.layoutChildren()` |
- 调用链：`FXHBox.layoutChildren → Node.autosize`

---

## FXVBox
- 职责：垂直布局容器，继承 `VBox`，能力同 `FXHBox`。
- 字段：无
- 方法：构造（可带子节点）、`resize`、`layoutChildren`（同 `FXHBox`）
- 调用链：`FXVBox.layoutChildren → Node.autosize`

---

## FXButton
- 职责：按钮控件，继承 `Button`，支持 flex/节点组/主题/鼠标/提示/状态/布局/字体适配。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 实例块 `NodeManager.init(this)`；`initNode()` | 初始化 | 手型光标、`pickOnBounds=true`、`mnemonicParsing=false`，再 `FlexAdapter.super.initNode()` |
  | 构造 `FXButton()` / `FXButton(String)` | 创建 | |
  | `resize(double,double)` | 尺寸调整 | `computeSize → super.resize → resizeNode` |
- 调用链：`FXButton.initNode → FlexAdapter.initNode`

---

## FXCheckBox
- 职责：复选框控件，继承 `CheckBox`，支持节点组/主题/提示/状态/字体适配。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 4 重载 `()`/`(boolean)`/`(String)`/`(String,boolean)` | 创建并可设选中 | |
  | `selectedChanged(ChangeListener<Boolean>)` | 选中变更事件 | `selectedProperty().addListener` |
  | `initNode()` | 初始化 | 手型光标、`mnemonicParsing=false` |
  | `reversalSelected()` | 反转选中 | `setSelected(!isSelected())` |
- 调用链：`FXCheckBox.reversalSelected → setSelected`

---

## FXHyperlink
- 职责：超链接控件，继承 `Hyperlink`，点击用浏览器打开文本中的地址。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `()` / `(String)` | 创建 | |
  | `initNode()` | 初始化 | 手型光标、去内边距；注册 `OnMousePrimaryClicked` 事件，非空 URL 时 `FXUtil.showDocument(url)` |
- 调用链：`FXHyperlink.initNode → 点击 → FXUtil.showDocument`

---

## FXRadioButton
- 职责：单选按钮控件，继承 `RadioButton`，支持多适配接口。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 手型光标、`mnemonicParsing=false` |
- 调用链：`FXRadioButton.initNode → NodeAdapter.initNode`

---

## FXViaFolder
- 职责：文件夹打开控件，继承 `Hyperlink`，点击用系统命令打开文本中的文件夹路径。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `()`/`(String)` | 创建 | |
  | `initNode()` | 初始化 | 注册点击事件，非空路径时 `SystemUtil.openFolderViaCommand(url)` |
- 调用链：`FXViaFolder.initNode → 点击 → SystemUtil.openFolderViaCommand`

---

## IconButton
- 职责：图标按钮，继承 `FXButton`，图形用 `SVGGlyph` 渲染并随字体/前景色同步。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | `initListener()` + `super.initNode()` |
  | `init(String)` / `init(SVGGlyph)`(protected) | 初始化图标 | 用图标地址创建 `SVGGlyph` 设为 graphic 后 `initGlyph()` |
  | `setIconUrl(String)` / `getIconUrl()` | 读写图标地址 | 基于 graphic 是否为 `SVGGlyph` |
  | `initListener()`(protected) | 注册监听 | 监听 font/graphic/textFill/background 变化触发 `initGlyph()` |
  | `initGlyph()`(私有) | 同步图标 | `glyph.disableTheme()`，用 `getTextFill()` 设置 glyph 颜色 |
- 调用链：`IconButton.initNode → initListener / initGlyph`；`字体/颜色变化 → initGlyph`

---

## FXComboBox<T>
- 职责：下拉框控件，继承 `ComboBox<T>`，支持 flex/主题/验证/选择/提示/状态/字体/布局适配与销毁。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `require` | `boolean` | 是否必填 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `isRequire()/setRequire(boolean)` | 必填读写 | |
  | `validate()` | 校验 | `require` 且未选时 `ValidatorUtil.validFail`；否则 `Verifiable.super` |
  | `selectedItemChanged(ChangeListener<T>)` | 选中变更事件 | 包装 `selectedItemProperty`，`isIgnoreChanged()` 为真时忽略 |
  | `selectedItemProperty()` | 选中属性 | `getSelectionModel().selectedItemProperty()` |
  | `containsItem(T)` | 是否含项 | `getItems().contains` |
  | `addItems(Collection<T>)` / `addItems(T[])` | 批量添加 | `FXUtil.runWait` 内 `getItems().addAll` |
  | `initNode()` | 初始化 | hand 光标、flex 初始化 |
  | `resize` | 尺寸调整 | 同通用模式 |
  | `setTipText(String)` | 设置提示 | 覆盖，prompt 为空时同步 promptText |
  | `destroy()` | 销毁 | `NodeDestroyUtil.destroyNode/destroyObject` |
- 调用链：`FXComboBox.setRequire + validate → ValidatorUtil.validFail`；`addItems → FXUtil.runWait`

---

## FXColorPicker
- 职责：颜色选择器控件，继承 `ColorPicker`，支持 flex/提示/字体/主题。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `resize` | 尺寸调整 | 通用模式 |
  | `setColor(String)` | 设颜色 | `Color.valueOf(color)` 异常兜底 |
  | `getColor()` | 取颜色 16 进制 | `FXColorUtil.getColorHex(getValue())` |
  | `initNode()` | 初始化 | hand 光标 |
- 调用链：`FXColorPicker.getColor → FXColorUtil.getColorHex`

---

## FXDatePicker
- 职责：日期选择器控件，继承 `DatePicker`，支持主题/flex/提示。
- 字段：无
- 方法：`resize`（通用模式）
- 调用链：`FXDatePicker.resize → resizeNode`

---

## FXToggleGroup
- 职责：单选框组控件，继承 `ToggleGroup`，提供泛型化的选中取值。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `selectedUserData()` | 选中项用户数据 | 泛型转换 `getSelectedToggle().getUserData()` |
  | `selectedToggle()` | 选中节点 | 泛型转换为 `RadioButton` |
- 调用链：无

---

## FXToggleSwitch
- 职责：开关控件，继承 atlantafx `ToggleSwitch`，支持选中/未选中显示不同文本。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `selectedText` / `unselectedText` | `String` | 选中/未选中时显示文本 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getSelectedText()/getUnselectedText()` | 读取文本 | |
  | `setSelectedText(String)` | 设选中文本 | 若当前选中立即 setText |
  | `setUnselectedText(String)` | 设未选中文本 | 若当前未选中立即 setText |
  | `selectedChanged(ChangeListener<Boolean>)` | 选中变更事件 | `selectedProperty().addListener` |
  | `initNode()` | 初始化 | hand 光标、标签右置；注册选中变化切换文本 |
  | `setTipText(String)` | 提示 | 文本为空时用提示填充 |
- 调用链：`ToggleSwitch 选中变化 → 监听器 → setText`

---

## HexStatusLabel
- 职责：十六进制视图的状态标签，继承 `FXLabel`，定时刷新显示偏移/大小/选中信息。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `statusTimer` | `AnimationTimer` | 状态刷新定时器 |
  | `reference` | `WeakReference<HexView>` | 目标 HexView 弱引用 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `init(HexView)` | 初始化 | 建弱引用与 `AnimationTimer`，启动定时刷新 |
  | `stop()` | 停止 | 清空文本并停止定时器 |
  | `updateStatus()`(私有) | 更新状态文本 | 拼 `Offset: 0x%X / 0x%X (size) | N columns`，有选中追加 selected 字节数；引用失效则停止 |
  | `destroy()` | 销毁 | 调 `stop()` |
- 调用链：`statusTimer.handle → HexStatusLabel.updateStatus → HexView.getFileSize/getFocusByte/getSelectionSize`

---

## HexView
- 职责：十六进制查看器，继承 `FXVBox`，Canvas 渲染三列（偏移|十六进制|原始文本），支持大文件窗口缓存、滚动、拖动多区域选择、键盘导航、明暗主题自适应。
- 字段（分组）：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `D_*`(11 个) / `L_*`(11 个) | `Color`(常量) | 暗色/亮色各列、选区、分隔线、焦点颜色 |
  | `bytesPerRow` | `int` | 每行字节数（默认 32，取值 8/16/32） |
  | `bytesPerRow_OPTIONS` | `int[]`(常量) | 每行字节数可选值 |
  | `PAD_LEFT`/`PAD_TOP`/`CACHE_SIZE` | `int`(常量) | 边距与文件缓存大小(65536) |
  | `font` | `Font` | 等宽渲染字体 |
  | `charW`/`lineH` | `double` | 字符宽/行高 |
  | `hexX`/`hexGapX`/`divX`/`textX` | `double` | 各列布局坐标 |
  | `OFFSET_X` | `double`(常量) | 偏移列起始 X |
  | `selections` | `List<long[]>` | 已固定选区列表 `{lo,hi}` |
  | `dragAnchor`/`dragCurrent` | `long` | 拖动起止字节 |
  | `focusByte`/`hoverByte` | `long` | 焦点/悬停字节 |
  | `hoverRegion` | `int` | 悬停区(0偏移/1hex/2文本) |
  | `clickTime`/`clickOff` | `long` | 双击检测 |
  | `dragFinished` | `boolean` | 拖动刚结束标记 |
  | `dataBytes` | `byte[]` | 内存数据源 |
  | `fileChannel`/`raf` | `FileChannel`/`RandomAccessFile` | 文件数据源 |
  | `fileSize` | `long` | 总字节数 |
  | `cacheBuf`/`cacheStart` | `ByteBuffer`/`long` | 文件读取缓存 |
  | `canvas`/`scrollBar` | `FXCanvas`/`FXScrollBar` | 绘制与滚动 |
  | `rowsPerPage`/`scrollPos` | `long` | 可视行数/滚动行 |
- 方法（要点）：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `HexView()` | 初始化 | 初始化字体、创建 canvas/scrollBar、绑定鼠标/滚轮/键盘事件、宽度绑定、`applyThemeColors` |
  | `recalcLayout()/initFont()`(私有) | 布局与字体 | 用 `Text` 测量字符宽高，算各列坐标 |
  | `getBytesPerRow()/getFileSize()/getFocusByte()/getSelectionSize()` | 状态查询 | 供 `HexStatusLabel` 使用 |
  | `getSelectionHex()` | 选中字节 hex 串 | 遍历 selections `String.format("%02X")` |
  | `readByte/readBytes(long,int)`(私有) | 数据读取 | 内存源直接拷贝；文件源用 `CACHE_SIZE` 窗口缓存 |
  | `gotoOffset(long)` | 跳转偏移 | 截断范围、必要时居中滚动、重绘 |
  | `toggleBytesPerRow()` | 切换每行字节数 | 在 options 中循环 |
  | `openBytes(byte[]/ByteBuffer)` / `openStream(InputStream)` / `openFile(File)` / `close()` | 数据源管理 | 关闭旧源、设置新源、计算行数、更新滚动条、重绘 |
  | `computeRows()/updateScrollBar()`(私有) | 行数与滚动条 | 按 canvas 高度算行数；按总行数控制滚动条可见量与 thumb |
  | `resolveByteAt(double,double)`(私有) | 坐标转字节 | 按偏移/hex/文本三列分别解析 |
  | `repaint()`(私有) | 重绘 | 逐行 drawOffset/drawHex/drawText + 选区边线 + 焦点虚线框 |
  | `drawSelectionEdges/drawSingleEdge/drawOffset/drawHex/drawText`(私有) | 绘制细节 | |
  | `onMouseMoved/Pressed/Dragged/Released/Clicked` / `onScrollWheel` / `onKeyPressed`(私有) | 交互 | 选择、双击选行、Ctrl+C 复制、Ctrl+A 全选、F2 切列、ESC 清选区、方向/翻页键导航 + Shift 扩选 |
  | `changeFont(Font)` / `changeTheme(ThemeStyle)` | 字体/主题 | 重新初始化字体/应用主题底色 |
  | `requestFocus()` | 请求焦点 | `super` 后 `runLater` 让 canvas 获焦 |
  | `destroy()` | 销毁 | `close()` 并置空 canvas/scrollBar |
- 调用链：`HexView 构造 → 事件绑定`；`鼠标/键盘 → repaint → readBytes → 绘制`；`HexStatusLabel.updateStatus → HexView 查询 API`

---

## FXSeparator
- 职责：分割线控件，继承 `Separator`，支持 flex/主题。
- 字段：无
- 方法：`resize`（通用模式）
- 调用链：`FXSeparator.resize → resizeNode`

---

## FXSlider
- 职责：滑块控件，继承 `Slider`，支持 flex/主题/提示，当前值实时显示为提示。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `resize` | 尺寸调整 | 通用模式 |
  | `initNode()` | 初始化 | hand 光标；监听 value 变化 `setTipText(value)` |
- 调用链：`FXSlider.valueProperty 变化 → setTipText`

---

## FXText
- 职责：文本控件，继承 `Text`，支持多适配，随主题应用前景色。
- 字段：无（实例块 `NodeManager.init` + `applyTheme()`）
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `()`/`(String)` | 创建 | |
  | `isResizable()` | 可调整 | `true` |
  | `changeTheme(ThemeStyle)` | 主题变更 | 启用主题时 `super.changeTheme` 并 `setFill(ThemeManager.currentForegroundColor())` |
  | `text(String)` | 设置文本 | `FXUtil.runWait` 内 `setText` |
  | `clear()` | 清空 | `setText("")` |
  | `resize` | 尺寸调整 | 通用模式 |
- 调用链：`changeTheme → ThemeManager.currentForegroundColor`

---

## Splitter
- 职责：分割器文本，继承 `FXText`，按可用宽度自动生成分隔符填充的标题行。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `titleText` | `String` | 标题文字 |
  | `splitText` | `String` | 分隔符文字（默认 `-`） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getTitleText()/setTitleText` `getSplitText()/setSplitText` | 读写 | |
  | `resize(double,double)` | 尺寸调整 | `super.resize` 后按 `computeWidth()` 调 `renderText` |
  | `renderText(double)`(私有) | 渲染 | 用 `FontUtil.textWidth` 计算标题/分隔符宽度，按 3:7 拼接分隔符与标题 |
- 调用链：`Splitter.resize → renderText → FontUtil.textWidth`

---

## FXTextArea
- 职责：多行文本输入框，继承 `TextArea`，支持长度/行数限制、必填校验、追加行、滚动到底等。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `maxLen` | `Long` | 最大长度 |
  | `maxLine` | `Long` | 最大行数 |
  | `require` | `boolean` | 是否必填 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getMaxLen()/getMaxLine()` | 读取限制 | 实现 `LimitLenControl`/`LimitLineControl` |
  | `setMaxLine(Long)/setMaxLen(Long)` | 设置限制 | 值非空且无 formatter 时加 `TextFormatter(new LimitOperator())` |
  | `isRequire()/setRequire` | 必填 | |
  | `setTipText` | 提示 | prompt 为空时同步 prompt |
  | `appendLines(Collection<String>)` / `appendLine(String)` | 追加行 | 以系统换行符拼接 |
  | `deleteText/appendText/clear` | 文本操作 | 均走 `FXUtil.runWait` 在 FX 线程 |
  | `isEmpty()/lineCount()` | 查询 | |
  | `scrollToEnd()/_scrollToEnd()` | 滚动尾部 | `ExecutorUtil.start` 延迟 150ms 定位光标 |
  | `validate()` | 校验 | require 且空则 `requestFocus` 并 false |
  | `flushCaret()` | 光标复位 | `positionCaret` + `requestFocus` |
  | `initNode()` | 初始化 | 自动换行；Ctrl+滚轮缩放字体 |
  | `text(String)/text()` | 文本读写 | |
  | `requestFocus()` | 请求焦点 | `TaskManager.startDelay` 延迟 1ms |
  | `resize` / `destroy` | 尺寸/销毁 | 通用模式 |
- 调用链：`setMaxLen/setMaxLine → TextFormatter(LimitOperator)`；`validate → Verifiable.validate`

---

## FXTextField
- 职责：单行文本输入框，继承 `TextField`，支持必填/非空校验、值格式化、皮肤懒创建。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `require` / `notEmpty` | `boolean` | 必填 / 不能为空白 |
  | `value` | `Object` | 实际值 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `onBlur()/onFocus()`(protected) | 焦点钩子 | 空实现供子类覆盖 |
  | `isRequire/setRequire` `isNotEmpty/setNotEmpty` | 校验标志 | |
  | 构造 `()`/`(String)` | 创建 | |
  | `setTipText` | 提示 | prompt 同步 |
  | `isEmpty()` | 是否空 | `getLength()==0 || getText().isEmpty()` |
  | `validate()` | 校验 | require 空 / notEmpty 空白时 `ValidatorUtil.validFail` |
  | `initNode()` | 初始化 | flex 初始化 + `pickOnBounds` |
  | `value()/value(Object)`(protected) | 实际值 | |
  | `getValue()/setValue(Object)` | 值读写 | 有文本返回文本否则实际值；set 后 `formatValue()` |
  | `formatValue()` / 静态 `format(Object)` | 格式化 | CharSequence/byte[]/toString |
  | `text(String)` | 设置文本 | `FXUtil.runWait` 内 setText |
  | `skin()` | 皮肤 | 空则 `createDefaultSkin()` |
  | `resize` / `destroy` | 尺寸/销毁 | 通用模式 |
- 调用链：`FXTextField.setValue → formatValue → format`；`validate → ValidatorUtil.validFail`

---

## FXLabel
- 职责：标签控件，继承 `Label`，支持 flex/主题/鼠标/文本/提示/状态/字体/布局适配。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `()`/`(Node)`/`(String)`/`(String,Node)` | 创建 | |
  | `isEmpty()` | 文本是否空 | `StringUtil.isEmpty` |
  | `clear()` | 清空 | `text("")` |
  | `text(String)` | 设置文本 | `FXUtil.runWait` 内 `setText` |
  | `resize` | 尺寸调整 | 通用模式 |
  | `changeTheme(ThemeStyle)` | 主题变更 | 若 graphic 为 `ThemeAdapter` 也 `changeTheme` |
- 调用链：`FXLabel.changeTheme → graphic.changeTheme`

---

## FXImageView
- 职责：图片控件，继承 `ImageView`，支持 flex/节点/属性/提示适配与快照。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 6 重载 | 创建（可设 url/size/image） | |
  | `setUrl(String)/getUrl()` | 读写图片地址 | `setProp("url", ...)` + `FXUtil.getImage(url)` |
  | `initNode()` | 初始化 | 等比例、`pickOnBounds` |
  | `resize` | 尺寸调整 | 通用模式 |
  | `snapshot()` / `snapshot(SnapshotParameters,WritableImage)` | 截图 | 后者 `FXUtil.runWait` 内截图并用 `AtomicReference` 回传 |
  | `isResizable()` | 可调整 | `true` |
  | `destroy()` | 销毁 | `NodeDestroyUtil.destroyNode` |
- 调用链：`FXImageView.setUrl → FXUtil.getImage`

---
