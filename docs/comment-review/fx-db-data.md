# fx-db 数据导入导出（`cn.oyzh.fx.db.data`）

覆盖 `cn.oyzh.fx.db.data` 及其子包 `dto`/`file`/`handler`/`ui` 共 29 个正式类/接口。

跳过项（整文件被注释的死代码，包名已迁移至 `cn.oyzh.easyshell.data.db.ui`）：`DBBinaryTextFiled`、`DBJsonTextFiled`、`DBJsonTextFiledSkin`。

---

## DataBatchInsertable
- 职责：数据批量插入接口，按批次限制拆分数据并以并行/串行方式执行批量插入。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getBatchLimit()` / `getInsertLimit()` / `getInsertList()` | 批次限制 / 插入限制 / 待插列表 | 抽象 |
  | `addInsert(D)` | 添加单条 | 列表为空抛 NPE；加入后达 `getInsertLimit()` 即 `doBatchInsert()` |
  | `addInsert(List<D>)` | 添加多条 | 逐条 `addInsert` |
  | `doBatchInsert()` | 执行批量插入 | 列表非空则：不超过 `batchLimit` 直接插；否则 `CollectionUtil.split` 分片，`enableParallel()` 为真时经 `ThreadUtil.submit` 并行并聚合异常，finally 清空列表 |
  | `doBatchInsert(List<D>, boolean parallel)` | 单批插入 | 抽象，子类实现 |
  | `enableParallel()` | 是否并行 | 默认 true |
- 调用链：`addInsert → doBatchInsert → CollectionUtil.split → ThreadUtil.submit → doBatchInsert(list,parallel)`

## DBDataExportConfig
- 职责：数据导出配置，控制日期格式、字段/记录分割符、文本识别符、字符集等。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `dateFormat` | `String` | 日期格式 |
  | `fieldToAttr` | `boolean` | 字段作为属性 |
  | `includeFields` | `boolean` | 是否包含列标题，默认 true |
  | `recordSeparator` | `String` | 记录分隔符，默认系统换行 |
  | `fieldSeparator` | `String` | 字段分隔符，默认 `;` |
  | `txtIdentifier` | `String` | 文本识别符，默认 `"` |
  | `charset` | `String` | 字符集，默认 UTF-8 |
  | `earlyVersion` | `boolean` | 是否使用早期版本格式 |
  | `continueWithError` | `boolean` | 出错时是否继续 |
- 方法：各字段 `getXxx/setXxx`（布尔用 `isXxx`）标准读写。
- 调用链：被各导出处理器读取。

## DBDataImportConfig
- 职责：数据导入配置，控制导入模式、行列索引、分割符、字符集等。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `dateFormat` | `String` | 日期格式 |
  | `importMode` | `String` | 导入模式：`1` 追加 / `2` 复制，默认 `2` |
  | `columnIndex` | `int` | 字段标题行索引，默认 0 |
  | `dataStartIndex` | `int` | 数据起始行索引，默认 1 |
  | `recordLabel` | `String` | 记录标签（包装数据的对象键名） |
  | `attrToColumn` | `boolean` | 是否将属性作为字段 |
  | `recordSeparator` / `fieldSeparator` / `txtIdentifier` / `charset` | `String` | 记录/字段分隔符、文本识别符、字符集 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `isAppendMode()` / `isCopyMode()` | 模式判断 | 比较 `importMode` 等于 `"1"`/`"2"` |
  | `fieldSeparatorChar()` / `txtIdentifierChar()` | 取首字符 | `charAt(0)` |
  | 各字段 `getXxx/setXxx` | 标准读写 | |
- 调用链：各 `DBDataXxxTypeFileReader.init → config.getColumnIndex/getDataStartIndex`

## DBDataTransportObject
- 职责：数据传输对象，表示一个可勾选的传输项。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `name` | `String` | 名称 |
  | `selected` | `boolean` | 是否选中，默认 true |
- 方法：`getName/setName`、`isSelected/setSelected` 标准读写。
- 调用链：`DBDataTransportNameListView.of → new DBDataTransportObject`

## DBDataTypeFileReader
- 职责：数据类型文件读取器抽象基类，定义按类型将文件解析为数据对象的通用能力。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `file` | `File` | 待读取文件 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `DBDataTypeFileReader()` / `DBDataTypeFileReader(File)` | 构造 | 后者设置 `file` |
  | `getFile()` | 取文件 | |
  | `init()` | 初始化（可覆写） | 默认空 |
  | `readObject()` | 读取单对象 | 抽象，读完返回 null |
  | `readObjects(count)` | 批量读取 | 循环 `readObject` 直到满 count 或 null |
  | `parseLine(line,txtIdentifier,fieldSeparator)` | 解析单行字段 | 状态机：`txtStart` 标记是否在引号内；遇识别符翻转并收集字段；引号内忽略字段分隔符 |
- 调用链：`readObjects → readObject → parseLine`

## DBDataCsvTypeFileReader
- 职责：CSV 文件读取器，按导入配置将 CSV 解析为数据对象。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `columns` | `List<String>` | 列名列表 |
  | `config` | `DBDataImportConfig` | 导入配置 |
  | `reader` | `SkipAbleFileReader` | 文件读取器 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造(File,config)` | 建读取器 | `SkipAbleFileReader`（指定字符集）后 `init` |
  | `init()` | 读列头 | 跳过 `columnIndex` 行，读一行按 `,` 与识别符 `parseLine` 取列名，再跳过 `dataStartIndex` 行 |
  | `readObject()` | 读一行 | `parseLine` 后按列名装 Map，无数据返回 null |
  | `close()` | 关闭 | 关读取器并置空 |
- 调用链：`readObject → parseLine`

## DBDataTxtTypeFileReader
- 职责：TXT 文件读取器，按配置将定界文本解析为数据对象。
- 字段：`columns`、`config`、`reader`（同 CSV）
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造(File,config)` | 建读取器 | 若记录分隔符非系统换行则 `reader.lineBreak(...)`，再 `init` |
  | `init()` | 读列头 | 用配置的 `fieldSeparatorChar`/`txtIdentifierChar` 解析 |
  | `readObject()` | 读一行 | 同 CSV，字段分隔符来自配置 |
  | `close()` | 关闭 | 关读取器并置空 |
- 调用链：`readObject → parseLine`

## DBDataExcelTypeFileReader
- 职责：Excel 文件读取器，基于 POI 工作簿解析为数据对象。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `workbook` | `Workbook` | POI 工作簿 |
  | `columns` | `List<String>` | 列名列表 |
  | `config` | `DBDataImportConfig` | 导入配置 |
  | `currentRowIndex` | `Integer` | 当前行索引 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造(File,config)` | 建工作簿 | 按 `.xlsx` 后缀经 `WorkbookHelper.create` 创建，`init` |
  | `init()` | 读列头 | 第 0 个 sheet 的 `columnIndex` 行各单元格字符串为列名；`currentRowIndex=dataStartIndex` |
  | `readObject()` | 读一行 | 按 `CellType` 分派取布尔/数值/日期/字符串值，按列名装 Map |
  | `close()` | 关闭 | 关工作簿并置空，异常包装为 RuntimeException |
- 调用链：`readObject → cell.getCellType → 值转换`

## DBDataJsonTypeFileReader
- 职责：JSON 文件读取器，基于 fastjson2 流式解析为数据对象。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `reader` | `JSONReader` | fastjson2 读取器 |
  | `config` | `DBDataImportConfig` | 导入配置 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造(File,config)` | 建读取器 | `JSONReader.of(FileUtil.getReader(...))`，`init` |
  | `init()` | 定位数组 | 无 `recordLabel` 时 `startArray`（纯数组）；否则读字段名匹配后 `startArray`（对象包装） |
  | `readObject()` | 读一个元素 | 流末尾或 `]` 返回 null；`readObject` 后消费逗号 |
  | `close()` | 关闭 | 有 `recordLabel` 时消费 `}`，再关闭 |
- 调用链：`readObject → JSONReader.readObject`

## DBDataXmlTypeFileReader
- 职责：XML 文件读取器，基于 StAX 事件流解析为数据对象。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `reader` | `XMLEventReader` | StAX 事件读取器 |
  | `config` | `DBDataImportConfig` | 导入配置 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造(File,config)` | 建读取器 | `XMLInputFactory.createXMLEventReader`，`init` |
  | `init()` | 定位首个元素 | 读到首个 `StartElement` 即停 |
  | `readObject()` | 读取一个记录 | 事件状态机：`attrToColumn` 真则元素属性作字段；否则子节点名值成对写入 Map，遇记录结束元素退出 |
  | `close()` | 关闭 | 关读取器，异常包装 RuntimeException |
- 调用链：`readObject → config.isAttrToColumn`

## DataHandler
- 职责：数据处理基类，提供中断控制、消息通知与进度上报能力。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `interrupt` | `AtomicBoolean` | 中断标志 |
  | `messageHandler` | `Consumer<String>` | 消息回调 |
  | `processedHandler` | `Consumer<Integer>` | 进度回调 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `interrupt(boolean)` / `interrupt()` | 设置/触发中断 | 懒建 `AtomicBoolean` |
  | `checkInterrupt()` | 检查中断 | 已中断抛 `InterruptedException` |
  | `exception(Exception)` | 上报异常 | 非中断异常交 `messageHandler`，否则打印栈 |
  | `message(String)` / `processed(int)` | 发消息 / 报进度 | 回调非空则 accept |
  | `processedSkip()/processedSkip(int)` | 跳过进度 | skip>=0 时上报 |
  | `processedIncr()/processedIncr(int)` | 递增进度 | 取绝对值后上报 |
  | `processedDecr()/processedDecr(int)` | 递减进度 | 上报负值 |
  | `getInterrupt/setInterrupt` / `getMessageHandler/setMessageHandler` / `getProcessedHandler/setProcessedHandler` | 读写 | |
- 调用链：`checkInterrupt → interrupt.get → throw InterruptedException`

## DataDumpHandler / DataExportHandler / DataImportHandler / DataRunFileHandler / DataTransportHandler
- 职责：分别定义转储 / 导出 / 导入 / 文件运行 / 传输的执行入口的抽象基类，均继承 `DataHandler`。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `doDump()` | 执行转储 | 抽象（DataDumpHandler） |
  | `doExport()` | 执行导出 | 抽象（DataExportHandler） |
  | `doImport()` | 执行导入 | 抽象（DataImportHandler） |
  | `runFile()` | 运行文件 | 抽象（DataRunFileHandler） |
  | `doTransport()` | 执行传输 | 抽象（DataTransportHandler） |
- 调用链：`具体子类 → doXxx()`

## DBDataDumpHandler
- 职责：数据库数据转储处理器抽象基类，将库/表的数据和结构转储到文件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `dataType` | `Byte` | 0 数据和结构 / 1 仅结构 |
  | `dbName` | `String` | 库名 |
  | `dumpFile` | `File` | 转储文件 |
  | `fileWriter` | `FastFileWriter` | 文件写入器 |
  | `dumpType` | `Byte` | 1 库 / 2 表 |
  | `tableName` | `String` | 表名 |
  | `queryLimit` | `int` | 查询限制，默认 1000 |
  | `dialect` | `DBDialect` | 方言 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造(dbName,dialect)` | 构造 | 设库名与方言 |
  | `dumpFile(File)` | 设转储文件 | 关旧写入器，`new FastFileWriter(file)`，链式返回 |
  | `doDump()` / `writeHeader()` / `writeTail()` | 抽象 | 转储主流程 / 头 / 尾 |
  | `isDumpRecord()` | 是否导出数据 | `dataType == 0` |
  | `setDumpType/setTableName/setQueryLimit` | 链式设值 | 返回 this |
  | 各 `getXxx/setXxx` | 读写 | |
- 调用链：`dumpFile → FastFileWriter`；`doDump → writeHeader/writeTail`

## DBDataExportHandler
- 职责：数据导出处理器抽象基类，按文件类型导出。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `name` | `String` | 名称 |
  | `fileType` | `String` | 文件类型（sql/json/csv/xml/html/xls/xlsx/js/txt） |
  | `queryLimit` | `int` | 查询限制，默认 1000 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `isSqlType/isXmlType/isCsvType/isHtmlType/isXlsType/isXlsxType/isJsonType/isTxtType/isJsType()` | 类型判断 | `equalsIgnoreCase` 比较 `fileType` |
  | `isExcelType()` | 是否 Excel | `isXlsType() || isXlsxType()` |
  | `getName/setName` / `getFileType/setFileType` / `getQueryLimit/setQueryLimit` | 读写 | |
- 调用链：`isExcelType → isXlsType/isXlsxType`

## DBDataImportHandler
- 职责：数据导入处理器抽象基类，从指定文件类型导入数据，实现 `DataBatchInsertable`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `name` | `String` | 名称 |
  | `fileType` | `String` | 文件类型（sql/xml/csv/excel/json/txt） |
  | `insertLimit` | `int` | 插入限制，默认 5000 |
  | `readLimit` | `int` | 读取限制，默认 5000 |
  | `batchLimit` | `int` | 批量处理限制，默认 50 |
  | `insertList` | `List<D>` | 插入集合 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造(name)` | 构造 | 设名称 |
  | `isSqlType/isXmlType/isCsvType/isExcelType/isJsonType/isTxtType()` | 类型判断 | `equalsIgnoreCase` |
  | `getInsertList()` | 取插入集合 | 懒建 ArrayList |
  | `getBatchLimit()` / `getInsertLimit()` | 实现接口 | 返回对应字段 |
  | `doImport()` | 执行导入 | 抽象 |
  | 各 `getXxx/setXxx` | 读写 | |
- 调用链：`doImport → DataBatchInsertable.doBatchInsert`

## DBDataRunFileHandler
- 职责：数据文件运行处理器抽象基类，执行 SQL 文件等，实现 `DataBatchInsertable`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `dbName` | `String` | 库名 |
  | `file` | `File` | 文件 |
  | `insertLimit` | `int` | 插入限制，默认 5000 |
  | `batchLimit` | `int` | 批量限制，默认 50 |
  | `continueWithErrors` | `boolean` | 遇错继续，默认 true |
  | `dialect` | `DBDialect` | 方言 |
  | `insertList` | `List<D>` | 插入集合 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造(dbName)` | 构造 | 设库名 |
  | `file(File)` | 设文件 | 链式返回 |
  | `getInsertList()` | 取插入集合 | 懒建 |
  | `runFile()` | 运行文件 | 抽象 |
  | `getBatchLimit()` / `getInsertLimit()` | 实现接口 | |
  | 各 `getXxx/setXxx` | 读写 | |
- 调用链：`runFile → 子类实现`

## DBDataTransportHandler
- 职责：数据传输处理器抽象基类，在源库与目标库间传输数据，实现 `DataBatchInsertable`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `sourceDatabase` | `String` | 源库 |
  | `targetDatabase` | `String` | 目标库 |
  | `insertLimit` | `int` | 插入限制，默认 5000 |
  | `selectLimit` | `int` | 查询限制，默认 5000 |
  | `batchLimit` | `int` | 批量限制，默认 50 |
  | `dialect` | `DBDialect` | 方言 |
  | `insertList` | `List<D>` | 插入集合 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造(dialect)` | 构造 | 设方言 |
  | `getInsertList()` | 取插入集合 | 懒建 |
  | `doTransport()` | 执行传输 | 抽象 |
  | `getBatchLimit()` / `getInsertLimit()` | 实现接口 | |
  | 各 `getXxx/setXxx` | 读写 | |
- 调用链：`doTransport → DataBatchInsertable.doBatchInsert`

## DBDataDateTextFiled
- 职责：日期格式输入框，提供常用日期时间格式可选项。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | `addItem` 一批 `yyyy-MM-dd HH:mm:ss` 等日期时间格式后 `super.initNode()` |
- 调用链：`initNode → SelectTextFiled.addItem`

## DBDataDumpTypeComboBox
- 职责：转储类型下拉框，选择数据与结构或仅结构。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 加 `I18nHelper.dataAndStructure()`、`I18nHelper.structure()` |
  | `isFull()` | 是否完整转储 | `getSelectedIndex() == 0` |
- 调用链：`isFull → getSelectedIndex`

## DBDataFieldSeparatorComboBox
- 职责：字段分隔符下拉框，提供分号/逗号/空格。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 加三个分隔符选项 |
  | `value()` | 取分隔符 | 索引 0/1 返回 `;`/`,`，否则空格 |
- 调用链：`value → getSelectedIndex`

## DBDataRecordLabelComboBox
- 职责：记录标签下拉框，选择记录根标签形式。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 加 `(Root)`、`RECORDS` |
  | `isRoot()` | 是否根标签 | `getSelectedIndex() == 0` |
- 调用链：`isRoot → getSelectedIndex`

## DBDataRecordSeparatorComboBox
- 职责：记录分隔符下拉框，提供 CRLF/LF/CR 并按系统预选。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 加三项，按 `OSUtil` 平台预选（Windows 0，Linux/macOS 1） |
  | `value()` | 取分隔符 | 索引 0/1/其他 → `\r\n`/`\n`/`\r` |
- 调用链：`initNode → OSUtil.isWindows/isLinux/isMacOS`

## DBDataTransportNameListView
- 职责：数据传输名称列表视图，展示并勾选待传输名称集合。
- 字段：无（继承父类）
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `of(names)` | 由名称集合初始化 | 由 `DBName` 列表构造 `DBDataTransportObject` 列表后 `init` |
- 调用链：`of → new DBDataTransportObject → init`（父类）

## DBDataTransportObjectListView
- 职责：数据传输对象列表视图，以复选框展示并可勾选。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `selectedChanged` | `Runnable` | 选中变更回调 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `init(List<DBDataTransportObject>)` | 初始化 | 清空后为每项建 `FXCheckBox`，选中状态绑定对象并回调；末尾触发一次 `selectedChanged` |
  | `getSelectedObjects()` | 取选中对象 | 遍历 `FXCheckBox`，从 prop `"data"` 取对象 |
  | `getSelectedSize()` | 取选中数量 | 遍历计数 |
  | `getSelectedChanged/setSelectedChanged` | 读写回调 | |
- 调用链：`init → FXCheckBox.selectedChanged → event.setSelected → selectedChanged.run`

## DBDataTxtIdentifierComboBox
- 职责：文本定界符下拉框，提供双引号与单引号。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 加 `"` 与 `'` |
- 调用链：`initNode → FXComboBox.addItem`
