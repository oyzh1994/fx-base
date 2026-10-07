# fx-editor 代码编辑器（`cn.oyzh.fx.editor`）

覆盖 `cn.oyzh.fx.editor.incubator`（含 `control`）共 19 个正式类/枚举/记录。基于 JavaFX 孵化 `CodeArea` 的代码编辑器，含语法着色、提示词与搜索高亮、格式化、自动补全成对符号及放大输入框控件。

无整文件死代码；`EditorUtil` 中仅输入法支持方法被整体注释（官方已支持）。

---

## Editor
- 职责：编辑器核心控件（继承 `CodeArea`），整合语法着色、提示词/高亮、格式化、字体与主题、右键菜单、成对符号自动补全。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `DEFAULT_PROMPTS_COLOR` / `DEFAULT_HIGHLIGHT_COLOR` / `DEFAULT_CARET_LINE_COLOR` / `DEFAULT_CARET_LINE_DARK_COLOR` / `DEFAULT_SELECTION_COLOR` / `DEFAULT_SELECTION_DARK_COLOR` | `static final Color` | 各默认颜色常量 |
  | `PAIR_MAP` | `static final Map<String,String>` | 成对符号映射（开始→结束） |
  | `DEFAULT_PADDING` | `static final Insets` | 默认内边距 |
  | `styleProvider` | `StyleProvider`（final） | 样式/主题/语法提供者 |
  | `textFlowModel` / `richTextAreaModel` | `TextFlowModel` / `RichTextAreaModel`（final） | 文本模型 |
  | `syntaxDecorator` | `EditorSyntaxDecorator`（final） | 语法装饰器 |
  | `formatTypeListener` / `promptsListener` / `highlightListener` / `highlightRegexListener` / `highlightWholeWordListener` / `highlightMacthCaseListener` / `fontListener` | `ChangeListener` | 各属性变化监听器 |
  | `syntaxesName` | `String` | 当前语法名（避免重复加载） |
  | `textLen` | `int` | 文本长度缓存 |
  | `textProperty` | `StringProperty` | 文本属性（懒建） |
  | `modelListener` | `StyledTextModel.Listener` | 文本编辑时刷新 `textProperty` |
  | `ignoreChange` | `boolean` | 是否忽略变化 |
  | `formatTypeProperty` / `promptsProperty` / `highlightProperty` / `highlightRegexProperty` / `highlightMacthCaseProperty` / `highlightWholeWordProperty` / `autoPairEnabledProperty` | 各 Property | 格式/提示词/高亮/成对补全属性 |
  | `formatted` | `boolean` | 是否已格式化 |
  | `editorFont` | `Font` | 编辑器字体（覆盖主题字体） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initEditor()` | 初始化编辑器 | 自动换行、行号、当前行高亮、内边距、行号装饰器、边框；绑定 prompts/formatType/highlight 监听；右键菜单事件；注册成对符号事件过滤器；`applyTheme` |
  | `initSyntaxes()` | 初始化语法 | 按格式类型经 `IGrammarSource.fromResource` 加载 `tmLanguage`，刷新装饰器 |
  | `refreshText()` / `initTextStyle()` | 刷新文本 / 样式 | `text(getText())` / `initSyntaxes + refreshText` |
  | `textProperty()` / `addTextChangeListener(listener)` | 文本属性 / 监听 | 监听模型变更刷新属性 |
  | `getFormatType/setFormatType/formatTypeProperty` | 格式类型读写 | |
  | `showData(rawData[,formatType])` | 显示数据 | `ignoreChange` 保护下设置格式与文本并 `forgetHistory` |
  | `text(text)` | 设置文本 | `FXUtil.runWait` 中 `setText` |
  | `showDetectData(rawData)` | 检测并显示 | `TextUtil.detectType` 映射格式后 `showData` |
  | `getPrompts/setPrompts/promptsProperty` | 提示词读写 | |
  | `getHighlight/setHighlight`、`is/setHighlightRegex/WholeWord/MacthCase` 及属性 | 高亮读写 | |
  | `is/setAutoPairEnabled` 及属性 | 成对补全开关 | |
  | `getLength()` / `getTextTrim()` / `isEmpty()` | 长度 / 去空格 / 判空 | |
  | `forgetHistory()` | 遗忘历史 | `clearUndoRedo` |
  | `getParagraphLength(index)` / `getPosByIndex(start,end)` / `getOffsetByPos(pos)` | 段落长 / 位置换算 | 遍历段落按累加长度换算 |
  | `replaceText(start,end,content)` | 替换 | 换算 `EditorTextPos` 后 `super.replaceText` |
  | `appendLine(content[,endLine])` / `appendContent(text)` | 追加行 / 内容 | 按需补换行 |
  | `getCaretBounds()` | 光标边界 | 由 `EditorSkin.getVFlow` 的光标信息计算屏幕坐标 |
  | `selectRange(start,end)` / `positionCaret(int/TextPos)` / `moveCaretStart/moveCaretEnd` | 选区 / 光标 | |
  | `deleteText(start,end)` | 删除 | 以空串替换 |
  | `resize(w,h)` / `createDefaultSkin()` | 尺寸 / 皮肤 | `EditorSkin` |
  | `set/getCaretColor`、`set/getCaretLineColor`、`set/getSelectionColor` | 各颜色 | 委托 `EditorSkin` |
  | `defaultCaretLineColor()` / `defaultSelectionColor()` | 默认颜色 | 暗色模式返回 dark 常量 |
  | `changeTheme(style)` | 应用主题 | 按 `Themes` 映射到 `tm4javafx/themes/*.json`，`StyleHelper.applyThemeSettings`，设置光标/行/选区颜色并刷新样式 |
  | `initNode()` | 初始化节点 | `initEditor`，初始化提示词/高亮，绑定字体监听 |
  | `hideLineNum()/showLineNum()` | 行号开关 | |
  | `caretPosition()` | 光标位置 | 由 `getOffsetByPos` 换算 |
  | `scrollToTop()/scrollToEnd()` | 滚动 | |
  | `fontSizeIncr/fontSizeDecr`、`get/setFontSize/Family/Weight` | 字体 | 优先 `editorFont`，否则系统字体 |
  | `getSelectedText()` / `getSelectionRange()` / `getSelectionLines()` / `isSelectedText()` | 选区读取 | |
  | `formatting()` | 格式化/反格式化 | 在 `EditorFormatter.formatText/unformatText` 间切换 |
  | `wordWrap()` | 自动换行开关 | |
  | `paste([format])` | 粘贴 | 粘贴后 `applyTheme` |
  | `getMenuItems()` | 右键菜单 | 撤销/重做/剪切/复制/粘贴/全选/自动换行/移动/格式化/行号 |
  | `lineCount()` / `lineEndingText()` / `lineEndingLength()` | 行数 / 换行 | |
  | `onAutoPair(e)` / `onAutoPairBackspace(e)` | 成对符号 | 有选区则包裹，无选区则智能跳过或插入；退格时空配对一并删除 |
  | `destroy()` | 销毁 | `NodeDestroyUtil.destroyNode/destroyObject` |
- 调用链：`setFormatType → formatTypeListener → syntaxDecorator.setFormatType → initTextStyle → initSyntaxes → refreshText`
- 调用链：`initNode → initEditor → applyTheme/changeTheme`；`onAutoPair → PAIR_MAP → replaceText`

## EditorFormatType
- 职责：编辑器格式类型枚举，映射名称、文件扩展名与 textmate 语法。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `name` | `String`（final） | 类型名称 |
  | `extension` | `String`（final） | 文件扩展名（逗号分隔） |
  | `textMateType` | `String` | textmate 类型，默认 `json` |
- 字段（枚举常量）：约 130 个，含 `RAW`（原始）、`JSON`、`XML`、`HTML`、`YAML`、`CSS`、`SQL`、`JAVA`、`PYTHON`、`MARKDOWN` 等常见及大量语言/配置文件类型，部分带第三参 textmate 类型（如 `KOTLIN("KOTLIN","kt,kts","xml")`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getName()` / `getExtension()` / `getFirstExtension()` / `getTextMateType()` | 读取元信息 | `getFirstExtension` 取逗号前首个 |
  | `getSyntaxesName()` | 语法名 | 枚举名小写并把 `_` 换 `-` |
  | `getFullSyntaxesName()` | 语法全称 | `name + ".tmLanguage" + ["." + textMateType]` |
  | `static ofExtension(extName)` | 按扩展名匹配 | 遍历常量比对扩展名，未命中返回 `RAW` |
- 调用链：`Editor.initSyntaxes → getFullSyntaxesName → IGrammarSource.fromResource`

## EditorFormatTypeComboBox
- 职责：格式类型下拉框（实现 `I18nSelectAdapter`）。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getFormat()/setFormat(type)` | 格式读写 | 空返回 `RAW` |
  | `values(locale)` | 填充选项 | 清空后加入全部枚举值 |
  | `initNode()` | 初始化 | 提示文本、转换器显示 `getName` |
- 调用链：`values → EditorFormatType.values`

## EditorFormatter
- 职责：编辑器格式化器，按格式类型美化/压缩文本。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `INSTANCE` | `static final EditorFormatter` | 单例 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `format(type,text)` | 格式化 | JSON 用 `JSONUtil.toPretty`，否则原文 |
  | `unformat(type,text)` | 取消格式化 | JSON 用 `JSONUtil.toCompress` |
  | `static formatText/unformatText(type,text)` | 静态门面 | 委托 `INSTANCE` |
- 调用链：`Editor.formatting → EditorFormatter.formatText/unformatText`

## EditorLineNumberDecorator
- 职责：编辑器行号装饰器。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `EditorLineNumberDecorator()` | 构造 | `super(new DecimalFormat("###0"))` |
- 调用链：`Editor.initEditor → setLeftDecorator(new EditorLineNumberDecorator())`

## EditorMachToken
- 职责：编辑器匹配 token 记录（起始/结束位置与样式 token）。
- 字段（记录组件）：`start`(`int`)、`end`(`int`)、`token`(`StyledToken`)
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `length()` | 匹配长度 | `end - start` |
- 调用链：`EditorSyntaxDecorator.machHighlight/machPrompts → EditorMachToken`

## EditorSkin
- 职责：编辑器皮肤（继承 `CodeAreaSkin`），管理光标/光标行/选区颜色并防被主题覆盖。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `path1/path2/path3` | `Path` | 光标 / 光标行 / 选区路径 |
  | `path1Listener/path2Listener/path3Listener` | `ChangeListener<? super Paint>` | 各路径填充色监听 |
  | `vFlow` | `VFlow` | 内部 VFlow 缓存 |
  | `caretColor` / `caretLineColor` / `selectionColor` | `Color` | 各颜色缓存 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造(Editor)` | 构造 | 设默认颜色，绑定三个路径的颜色监听 |
  | `getVFlow()` | 取 VFlow | 遍历子节点查找，否则 `RichTextAreaSkinHelper.getVFlow` |
  | `getCaretInfo()` | 光标信息 | `vFlow.getCaretInfo` |
  | `getCaretPath/getSelectionHighlight/getCaretLineHighlight()` | 各路径 | `vFlow.lookup("Path.caret"/"Path.selection-highlight"/"Path.caret-line")` |
  | `set/getCaretColor/CaretLineColor/SelectionColor` | 各颜色读写 | 设置路径填充与描边 |
  | `dispose()` | 释放 | 移除各监听器后 `super.dispose` |
- 调用链：`Editor.getCaretBounds → EditorSkin.getVFlow → getCaretInfo`

## EditorSyntaxDecorator
- 职责：编辑器语法装饰器（继承 `StatelessSyntaxDecorator`），负责语法着色、提示词着色与搜索高亮，支持同步/异步着色策略。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `highlight` / `prompts` / `formatType` | `volatile` | 高亮文本 / 提示词集合 / 格式类型 |
  | `highlightRegex` / `highlightWholeWord` / `highlightMatchCase` | `volatile boolean` | 高亮匹配选项 |
  | `myParagraphs` | `volatile List<RichParagraph>` | 段落缓存（异步更新用） |
  | `styleVersion` | `AtomicInteger`（final） | 样式版本号（丢弃过期后台结果） |
  | `styleExecutor` | `ExecutorService`（final） | 单线程守护后台着色线程 |
  | `highlightPattern` / `promptsPattern` | `Pattern` | 高亮 / 提示词匹配模式 |
  | `highlightColor` / `promptsColor` / `promptsStyle` | `Color`/`StyleAttributeMap` | 高亮色 / 提示词色与样式 |
  | `syntaxStrategy` / `syntaxAsyncStrategy` | `EditorSyntaxStrategy` / `EditorSyntaxAsyncStrategy` | 着色 / 异步策略，默认 AUTO |
  | `syntaxAsyncMinThreshold`(500) / `syntaxAsyncAutoThreshold`(50000) / `syntaxMaxThreshold`(500000) | `int` | 各阈值 |
  | `firstChange` | `boolean` | 首次变更标志 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `set/getHighlight`、`setHighlightRegex/WholeWord/MatchCase` | 高亮设置 | 变更后 `initHighlightPattern` |
  | `initHighlightPattern()` | 生成高亮模式 | `RegexUtil.createSearchPattern` |
  | `setPrompts(prompts)` | 设置提示词 | 以 `\b(...)\b` 编译模式 |
  | `set/getFormatType` | 格式类型 | |
  | 各颜色/阈值 `get/set` | 读写 | |
  | `isSyntax(lineCount)` | 是否着色 | `lineCount <= syntaxMaxThreshold` |
  | `isAsyncSyntax(lineCount)` | 是否异步着色 | 按策略与阈值判断（AUTO 超最小阈值异步，FIRST/AUTO 对非首次/小于自动阈值回退同步） |
  | `handleChange(model,start,end,...)` | 处理变更 | 空文本清空；非着色→`buildBaseParagraphs`；同步→`buildRichParagraphs`；异步→先 `buildFastParagraphs` 再提交后台线程完整着色（版本号校验，`FXUtil.runLater` 回填） |
  | `createRichParagraph(model,index)` | 取段落 | 从 `myParagraphs` 按索引取，越界返回空 |
  | `buildBaseParagraphs(text)` | 基础段落 | 仅应用样式谓词与高亮 |
  | `buildFastParagraphs(text)` | 快速段落 | 提示词+高亮，跳过语法着色 |
  | `buildRichParagraphs(text)` | 完整段落 | 提示词或 `provider.tokenize` 语法着色 + 高亮 |
  | `machHighlight(line)` / `machPrompts(line)` | 匹配高亮 / 提示词 | 用对应 Pattern 生成 `EditorMachToken` 列表 |
  | `buildTokens(line,machTokens)` | 构建 token | 按匹配位置切分，未匹配部分样式为 null |
- 调用链：`handleChange → isSyntax/isAsyncSyntax → buildXxxParagraphs → machPrompts/machHighlight → provider.tokenize`

## EditorSyntaxStrategy
- 职责：编辑器着色策略枚举。
- 字段（枚举常量）：`SYNC`（同步）、`ASYNC`（异步）、`AUTO`（自动，小于最小阈值同步否则异步）
- 方法：无
- 调用链：`EditorSyntaxDecorator.isAsyncSyntax → syntaxStrategy`

## EditorSyntaxAsyncStrategy
- 职责：编辑器着色异步策略枚举。
- 字段（枚举常量）：`FIRST`（首次）、`ALWAYS`（永远）、`AUTO`（自动，非首次且小于自动阈值回退同步）
- 方法：无
- 调用链：`EditorSyntaxDecorator.isAsyncSyntax → syntaxAsyncStrategy`

## EditorTextPos
- 职责：编辑器文本位置，含起始与结束 `TextPos`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `start` / `end` | `TextPos` | 起始 / 结束位置 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造(start,end)` | 构造 | |
  | `getStart/setStart` / `getEnd/setEnd` | 读写 | |
- 调用链：`Editor.getPosByIndex → new EditorTextPos`

## EditorUtil
- 职责：编辑器工具类，提供高亮组件绑定与高亮搜索。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `bindHighlight(editor,field)` | 绑定高亮 | 监听 `HighlightTextField` 的文本/正则/全字/大小写属性回写编辑器 |
  | `searchNextHighlight(editor,field)` | 搜索下一个 | 由光标位置 `TextUtil.findText` 匹配，未找到则从头再搜，命中后 `selectRange` |
  | `searchHighlight(text,field,index)`（私有） | 搜索 | 委托 `TextUtil.findText` |
  | `setupIMESupport` | 安装输入法支持 | 整方法已注释（官方已支持） |
- 调用链：`bindHighlight → editor.setHighlight/setHighlightRegex/...`；`searchNextHighlight → TextUtil.findText → editor.selectRange`

## control 包

## EditorEnlargeTextFiled
- 职责：可放大的编辑器输入框（继承 `EnlargeTextFiled`）。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `skin()` / `createDefaultSkin()` | 皮肤 | `EditorEnlargeTextFiledSkin` |
  | `setFormatType(type)` | 设格式类型 | 委托皮肤 |
- 调用链：`setFormatType → skin().setFormatType`

## EditorEnlargeTextFiledSkin
- 职责：可放大编辑器输入框皮肤（继承 `EnlargeTextFiledSkin`），弹出编辑器编辑长文本。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `editor` | `Editor` | 弹窗内编辑器 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造(TextField)` | 构造 | |
  | `onButtonClick(e)` | 按钮点击 | 建 `PopupExt`，隐藏行号，装载编辑器与提交/取消按钮，`showPopup` |
  | `setFormatType(type)` | 设格式类型 | 懒建编辑器并设格式 |
- 调用链：`onButtonClick → new Editor → onSubmit → setText`

## JsonEditor
- 职责：JSON 编辑器（继承 `Editor`）。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | `super.initNode` 后设格式为 `JSON` |
- 调用链：`initNode → setFormatType(JSON)`

## SqlEditor
- 职责：SQL 编辑器（继承 `Editor`）。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | `super.initNode` 后设格式为 `SQL` |
- 调用链：`initNode → setFormatType(SQL)`

## JsonTextFiled
- 职责：JSON 文本输入框（继承 `LimitTextField`），支持数组/对象解析与美化。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `array` | `boolean` | 是否为数组 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `skin()` / `createDefaultSkin()` | 皮肤 | `LongTextFiledSkin` |
  | `set/getEnlargeWidth`、`set/getEnlargeHeight` | 展开尺寸 | 委托皮肤 |
  | `isArray/setArray` | 数组标志 | |
  | `getValue()` | 取值 | 数组用 `JSONUtil.parseArray`，否则 `parseObject` |
  | `formatValue()` | 格式化 | `format(super.value())` |
  | `static format(val)` | 格式化对象 | `JSONUtil.toPretty` |
- 调用链：`getValue → JSONUtil.parseArray/parseObject`

## LongTextFiled
- 职责：长文本输入框（继承 `LimitTextField`）。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `skin()` | 皮肤 | `LongTextFiledSkin` |
  | `createDefaultSkin()` | 默认皮肤 | 匿名子类重写 `getFormatType` 返回 `LOG` |
  | `set/getEnlargeWidth`、`set/getEnlargeHeight` | 展开尺寸 | 委托皮肤 |
  | `getValue()` | 取值 | 返回文本 |
- 调用链：`createDefaultSkin → LongTextFiledSkin.getFormatType(LOG)`

## LongTextFiledSkin
- 职责：长文本输入框皮肤（继承 `ActionTextFieldSkin`），点击按钮弹出编辑器。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `enlargeWidth` / `enlargeHeight` | `double` | 展开宽(350) / 高(280) |
  | `popup` | `PopupExt` | 弹窗 |
  | `editor` | `Editor` | 编辑器 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造(TextField)` | 构造 | |
  | `onButtonClick(e)` | 按钮点击 | 建弹窗与编辑器，装载数据与提交/取消按钮并显示 |
  | `handleHide()` / `onSubmit(text)` / `dispose()` | 隐藏 / 提交 / 释放 | |
  | `getButton()` / `updateButtonVisibility()` | 放大按钮 | 按可见/禁用/聚焦决定显示 |
  | `getFormatType()` | 格式类型 | 默认 `JSON`，子类可覆写 |
  | 各尺寸/弹窗 `get/set` | 读写 | |
- 调用链：`onButtonClick → Editor.showData → onSubmit → setText`
