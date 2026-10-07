# fx-rich 富文本（`cn.oyzh.fx.rich`）

覆盖 `cn.oyzh.fx.rich` 及其子包。经逐文件核查，本文档仅展开 **1 个**正式活跃类 `RichMsgTextArea`。

## 跳过项（整文件被注释的死代码，共 41 个类）

这些文件整体被注释（`package` 语句亦被注释），属迁移前的旧实现（原 `richtextfx` 方案，多数标注 `@Deprecated`），不再参与编译：

- 根包：`RichDataType`、`RichDataTypeComboBox`、`RichTextStyle`
- `incubator`：`BaseRichTextArea`、`FXRichTextArea`、`FXRichTextArea1`、`FXRichTextAreaSkin`、`RichDataTextArea`、`RichJsonTextArea`
- `richtextarea.control`：`FlexRichTextArea`
- `richtextarea.ext`：`ActionCmdAppendText`、`ActionCmdClearText`、`ActionCmdDeleteText`、`ActionCmdForgetHistory`、`ActionCmdInsertText`、`ActionCmdPositionCaret`、`ActionCmdReplaceText`、`ActionCmdSelectRange`、`ActionCmdSetStyle`、`ActionCmdSetText`、`AppendTextCmd`、`ClearTextCmd`、`DeleteTextCmd`、`ForgetHistoryCmd`、`InsertTextCmd`、`PositionCaretCmd`、`ReplaceTextCmd`、`RichActionFactory`、`SelectRangeCmd`、`SetStyleCmd`、`SetTextCmd`
- `richtextfx`：`RichLineNumberFactory`、`control/BaseRichTextArea`、`control/BaseRichTextField`、`control/FXVirtualizedScrollPane`、`control/RichTextAreaPane`、`data/RichDataTextArea`、`data/RichDataTextAreaPane`、`json/RichJsonTextArea`、`json/RichJsonTextAreaPane`、`util/RichControlUtil`

---

## RichMsgTextArea
- 职责：只读日志消息文本域（继承 `Editor`），支持逐行拼接文本、限制最大行数并按策略裁剪超出行数。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `LINE_MAX_LENGTH` | `static int` | 单行内容最大长度，默认 5*1024，超出则以“内容过大”占位 |
  | `lineLimit` | `int` | 最大行数，默认 3000 |
  | `limitPolicy` | `byte` | 限制策略（1 保留限制行 / 2 清空 / 3 保留90% / 4 保留70% / 5 保留50% / 6 保留30%），默认 1 |
  | `queue` | `Queue<String>`（final） | 消息队列（`LinkedBlockingQueue`） |
  | `appending` | `AtomicBoolean`（final） | 拼接中标志位 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 实例初始化块 | 初始化 | `setEditable(false)`（只读） |
  | `get/setLineLimit`、`get/setLimitPolicy` | 行数 / 策略读写 | |
  | `appendLines(lines)` | 批量追加 | 拼接各行并补换行符，超长行以“内容过大”占位，末尾 `appendContent` |
  | `appendLine(s)` | 追加单行 | 超长占位，按需补换行后 `appendContent` |
  | `appendContent(s)` | 追加内容 | 行数超过 `lineLimit` 时先 `deleteLimitLine`，再 `doAppend` |
  | `doAppend(text)`（protected） | 异步批量拼接 | 入队；`appending` 标志串行化，异步线程批量取出队列内容后 `super.appendContent`，减少界面刷新 |
  | `text(text)` | 设置文本 | 清空队列后 `super.text` |
  | `deleteLimitLine(lineCount)`（protected） | 裁剪超出行 | 按 `limitPolicy` 累计保留部分的字符长度（各行长度+1），`deleteText(0,endPos)` 删除 |
  | `initNode()` | 初始化 | `super.initNode` 后设格式为 `LOG` |
- 调用链：`appendLine/appendLines → appendContent → deleteLimitLine（按需）→ doAppend → super.appendContent`
