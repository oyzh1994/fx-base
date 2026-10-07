# fx-db 查询、SQL 解析、条件与状态监听（`query`/`sql`/`condition`/`listener`）

覆盖 `cn.oyzh.fx.db.query`（含 `ui`/`util`）、`cn.oyzh.fx.db.sql`、`cn.oyzh.fx.db.condition`（含 `ui`）、`cn.oyzh.fx.db.listener` 共 18 个正式类/接口。

---

## sql 包

## DBSqlParser
- 职责：SQL 解析器抽象基类，定义解析、美化、压缩、去注释、单语句判断等能力与静态门面。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `sqlContent` | `String`（final, protected） | 待解析 SQL |
  | `dialect` | `DBDialect`（final, protected） | 数据库方言 |
  | `sqlParserType` | `static String` | 解析器类型，默认 `"base"` |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造(sqlContent,dialect)` | 构造 | 保存内容与方言 |
  | `isSingle()` / `parseSql()` / `parseSingleSql()` / `isSelect(sql)` / `prettySql(sql)` / `compressSql(sql)` / `removeComment(sql)` / `isFullColumn(sql)` | 抽象能力 | 子类实现 |
  | `static prettySql(sql,dialect)` / `compressSql` / `parseSql` / `parseSingleSql` | 静态门面 | 经 `getParser` 取解析器后调用 |
  | `setSqlParserType(type)` | 设置解析器类型 | 静态字段赋值 |
  | `static getParser(sql,dialect)` | 取解析器 | `sqlParserType` 为 `durid` 时返回 `DBDruidSqlParser`，否则 `DBBaseSqlParser` |
- 调用链：`DBSqlParser.parseSql(sql,dialect) → getParser → DBBase/DruidSqlParser.parseSql`

## DBBaseSqlParser
- 职责：默认 SQL 解析器，基于通用 `SqlUtil` 实现解析/美化/压缩/去注释。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `database` | `SqlDatabase`（final） | 由方言映射的通用数据库类型 |
  | `sqlList` | `List<String>` | 解析后的 SQL 列表 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造(sqlContent,dialect)` | 映射数据库 | `switch` 方言 → `SqlDatabase.MYSQL/DM/ANSI` |
  | `removeComment(sql)` | 去注释 | `SqlUtil.removeComments(sql,database)` |
  | `isSingle()` | 是否单语句 | `sqlList.size() == 1` |
  | `isSelect(sql)` | 是否查询 | `SqlUtil.isQuery` |
  | `isFullColumn(sql)` | 是否全字段 | `SqlUtil.isAllFieldQuery` |
  | `parseSql()` | 解析多条 | 先去注释，`SqlUtil.split` 后逐条换行替换为空格；异常时原文入列表 |
  | `parseSingleSql()` | 解析单条 | `SqlUtil.singleStatement`，成功则重置 `sqlList` |
  | `prettySql(sql)` / `compressSql(sql)` | 美化 / 压缩 | `SqlUtil.format` / `compressSql`，异常返回原串 |
- 调用链：`parseSql → removeComment → SqlUtil.split`

## DBDruidSqlParser
- 职责：基于 Druid 的 SQL 解析器，借助 Druid 解析与 SchemaStatVisitor 识别查询、全字段。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `dbType` | `DbType`（final） | Druid 数据库类型 |
  | `sqlStatements` | `List<SQLStatement>` | 解析后的语句集合 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造(sqlContent,dialect)` | 映射 DbType | MYSQL→mysql，DAMENG→dm，其他 null |
  | `removeComment(sql)` | 去注释 | `SQLUtils.parseSingleStatement` 后 `SqlUtil.removeComments` |
  | `isSingle()` | 是否单语句 | MySQL 下 `SHOW VARIABLES LIKE`/`SHOW CREATE EVENT` 直接返回 true；否则 `sqlStatements.size()==1` |
  | `isSelect(sql)` | 是否查询 | 特殊语句短路；否则 `SchemaStatVisitor` 取表统计判断是否 `Select` |
  | `isFullColumn(sql)` | 是否全字段 | 遍历 `visitor.getColumns()`，存在列名 `*` 即真 |
  | `parseSql()` | 解析多条 | `SQLUtils.parseStatements`（SkipComments），逐条 `toString` 并去换行 |
  | `parseSingleSql()` | 解析单条 | `parseSingleStatement`，替换换行为空格 |
  | `prettySql(sql)` | 美化 | `SQLUtils.format` 保留注释 |
  | `compressSql(sql)` | 压缩 | `FormatOption` 大写、非美化后 format |
- 调用链：`isFullColumn → SchemaStatVisitor.getColumns`

## query 包

## DBQueryEditor
- 职责：SQL 查询编辑器基类，联动按键交互、注释操作与提示弹窗。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `promptPopup()` | 取提示弹窗 | 抽象 |
  | `initNode()` | 初始化 | 鼠标释放/失焦隐藏弹窗；按键释放时若 `Ctrl+/` 则 `doComment`，否则 `promptPopup().prompt(this,event)` |
  | `doComment()` | 执行注释 | 空实现，子类覆写 |
- 调用链：`initNode → promptPopup().prompt / doComment`

## DBQueryPromptItem
- 职责：查询提示项，封装类型、内容、相关度与扩展内容。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `type` | `byte` | 类型 |
  | `content` | `String` | 内容 |
  | `correlation` | `double` | 相关度 |
  | `extContent` | `String` | 额外内容 |
- 方法：`getType/setType`、`getContent/setContent`、`getCorrelation/setCorrelation`、`getExtContent/setExtContent` 标准读写。
- 调用链：`DBQueryTokenAnalyzer.initPrompts → List<DBQueryPromptItem>`

## DBQueryToken
- 职责：查询词元，记录光标处文本片段及起止位置与识别符。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `endIndex` / `startIndex` | `int` | 结束 / 起始位置 |
  | `content` | `String` | 内容 |
  | `token` | `Character` | 词元标识符 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `isEmpty()` / `isNotEmpty()` | 是否空 | 判断 `content` |
  | 各 `getXxx/setXxx` | 读写 | |
- 调用链：`DBQueryTokenAnalyzer.currentToken → DBQueryToken`

## DBQueryTokenAnalyzer
- 职责：查询词元解析器抽象基类，解析当前词元并生成候选提示。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `currentToken(input,currentIndex)` | 取当前词元 | 抽象 |
  | `initPrompts(token,minCorr)` | 生成提示项 | 抽象 |
- 调用链：`DBQueryPromptPopup.currentToken → tokenAnalyzer().currentToken`

## DBQueryPromptPopup
- 职责：查询提示弹窗基类，展示并选择 SQL 提示项，含键盘导航与延迟显示。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `token` | `T extends DBQueryToken` | 当前词元 |
  | `onItemSelected` | `Consumer<E>` | 选中回调 |
  | `promptFlag` | `AtomicInteger`（final） | 提示标志位（防竞态） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initContent()` | 初始化内容 | 建列表组件并绑定 `onItemPicked` → `pickItem/hide` |
  | `initListView()` / `tokenAnalyzer()` | 抽象 | 列表组件 / 词元解析器 |
  | `listView()` | 取列表 | `super.content()` 强转 |
  | `initPrompts(token)` | 初始化提示 | `tokenAnalyzer().initPrompts(token,0.5f)` → `listView().init`；返回是否非空 |
  | `prompt(editor,event)` | 处理按键 | 常规键/更新键/主修饰键直接隐藏；弹窗已显示时处理上下/回车导航；否则取当前词元 `handlToken` |
  | `handlToken(editor)` | 处理词元 | 可用则自增 `promptFlag`，`TaskManager.startDelay` 延迟 30ms，标志一致才 `initPrompts` 并显示 |
  | `tokenAvailable()` | 词元是否可用 | 非空且 `isNotEmpty` |
  | `currentToken(editor)` | 取当前词元 | 由 `editor.caretPosition()` 与文本经分析器获取 |
  | `autoComplete(editor,item)` | 自动完成 | 有 token 时 `replaceText` |
  | `replaceText(editor,content)` | 替换文本 | `editor.replaceText(start,end,content)` 并按需定位光标 |
  | `show(editor)` | 显示 | `RenderService.submitFXLater` 按光标 bounds 定位显示 |
  | `hide()` | 隐藏 | 显示中则 FX 线程 `hide`，并清空 token |
  | `pickItem()` | 选中项 | 回调 `onItemSelected.accept(pickedItem)` |
  | `isGeneralKeyEvent(event)` | 是否常规键 | 屏蔽 Ctrl+S/X/V/C/A/Z/Y 及 Ctrl+`/` |
  | `initNode()` | 初始化 | `initContent` + 应用当前主题 |
- 调用链：`prompt → currentToken → handlToken → initPrompts → listView.init`；`pickItem → onItemSelected`

## DBQueryPromptListView
- 职责：查询提示列表，以列表展示并可选择提示项（含背景高亮）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `currentPickIndex` | `volatile int` | 当前选中索引，-1 未选中 |
  | `onItemPicked` | `Runnable` | 节点选中事件 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `select(index)` | 选中 | 索引钳制到 `[0,size-1]`，应用背景色 |
  | `pickNext()` / `pickPrev()` | 下移 / 上移 | `select(currentPickIndex ± 1)`，synchronized |
  | `hasPicked()` | 是否有选中 | 有选中项且索引非 -1 |
  | `getPickedItem()` | 取选中项 | 从 `FXHBox` prop `"item"` 取并清除背景 |
  | `applyBackground(pickedIndex)` | 应用背景 | 清除旧项背景，新项设 `DEEPSKYBLUE` 背景 |
  | `init(items)` | 初始化 | 每题项建 `FXHBox`（`initPromptLabel`+`initExtLabel`），存 prop `"item"` |
  | `initPromptLabel/initExtLabel/initBox` | 抽象/构造 | 提示标签 / 扩展标签 / 盒子样式与鼠标事件 |
  | `getOnItemPicked/setOnItemPicked` | 读写回调 | |
  | `initNode()` | 初始化 | 设置宽 360 高 240 |
- 调用链：`init → initBox(点击) → applyBackground / onItemPicked.run`

## DBQueryUtil
- 职责：SQL 查询编辑器按键工具类，维护触发提示与触发更新的按键集合。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `PROMPT_CODES` | `static List<KeyCode>` | 触发提示的按键（字母、数字、小键盘、`-`/`_`/空格等） |
  | `UPDATE_CODES` | `static List<KeyCode>` | 触发内容更新的按键（退格、删除） |
- 方法：无（静态块初始化集合）
- 调用链：`DBQueryPromptPopup.prompt → DBQueryUtil.PROMPT_CODES/UPDATE_CODES.contains`

## DBQueryResult
- 职责：查询结果基类，封装内容、耗时、变更数量、成功状态等。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `content` | `String` | 内容 |
  | `used` | `long` | 耗时（纳秒） |
  | `msg` | `String` | 消息 |
  | `updateCount` | `long` | 变更总数 |
  | `success` | `boolean` | 是否成功 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getCount()` | 取数量 | 抽象 |
  | `hasResult()` | 是否有结果 | `updateCount <= 0 && getCount() > 0` |
  | `parseResult(resultSet,connection)` / `parseResult(resultSet,connection,readonly)` | 解析结果 | 前者转调后者（readonly=true），后者抽象 |
  | `getUsedMs()` | 耗时（毫秒） | `used / 1_000_000` |
  | 各 `getXxx/setXxx` | 读写 | |
- 调用链：`hasResult → getCount`

## DBQueryResults
- 职责：查询结果集合，聚合同一次执行的多个结果及错误信息。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `errMsg` | `String` | 错误信息 |
  | `results` | `List<R>` | 结果列表 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `addResult(result)` | 添加结果 | 懒建列表后 add |
  | `isEmpty()` | 是否空 | `CollectionUtil.isEmpty` |
  | `isSuccess()` | 是否成功 | `errMsg` 为空 |
  | `parseError(ex)` | 解析错误 | 取 `ex.getMessage()` |
  | `getErrMsg/setErrMsg` / `getResults/setResults` | 读写 | |
- 调用链：`isSuccess → StringUtil.isEmpty(errMsg)`

## condition 包

## DBCondition
- 职责：数据库条件基类，封装名称、值及是否必需条件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `name` | `String` | 名称 |
  | `value` | `String` | 值 |
  | `requireCondition` | `boolean` | 是否必需条件，默认 true |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `DBCondition()` / `DBCondition(name,value)` / `DBCondition(name,value,require)` | 构造 | |
  | `wrapCondition()` / `wrapCondition(columnName,condition)` | 包装条件 | 前者转调后者（空参），后者默认返回 null，子类覆写 |
  | `getName/setName` / `getValue/setValue` / `isRequireCondition/setRequireCondition` | 读写 | |
- 调用链：`wrapCondition() → wrapCondition(null,null)`

## DBConditionManager
- 职责：按数据库方言注册、懒初始化并获取条件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `INITIALIZERS` | `static Map<DBDialect,Runnable>` | 方言条件初始化器 |
  | `CONDITIONS` | `static Map<DBDialect,List<DBCondition>>` | 方言条件列表 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `registerInitializer(dialect,func)` | 注册初始化器 | |
  | `putCondition(dialect,condition)` | 添加条件 | 参数判空抛 NPE，追加到列表 |
  | `conditions(dialect)` | 取条件列表 | 同步块内缺失则触发一次初始化器并移除；缺失返回空列表 |
- 调用链：`DBConditionComboBox 构造 → conditions`

## DBConditionComboBox
- 职责：条件下拉框，选择指定方言下的查询条件。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造(dialect)` | 构造 | `setItem(DBConditionManager.conditions(dialect))` |
  | `initNode()` | 初始化 | 设置转换器（显示 `getName`） |
- 调用链：`DBConditionComboBox → DBConditionManager.conditions`

## listener 包

## DBStatusListener
- 职责：数据库对象状态监听器抽象基类，维护键值并在销毁时自动移除。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `key` | `String`（final） | 监听器键值 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `DBStatusListener()` | 无参构造 | 以随机 UUID 为键，注册到管理器 |
  | `DBStatusListener(key)` | 指定键构造 | 注册到 `DBStatusListenerManager` |
  | `DBStatusListener(dbName,tableName)` | 库表键构造 | 键 `dbName::tableName` |
  | `DBStatusListener(dbName,schema,tableName)` | 库模式表键构造 | 键 `dbName:schema:tableName` |
  | `destroy()` | 销毁 | 从管理器移除 |
  | `getKey()` | 取键 | |
- 调用链：`DBStatusListener 构造 → DBStatusListenerManager.addListener`；`destroy → removeListener`

## DBStatusListenerManager
- 职责：状态监听器的注册、移除、查找及节点绑定/解绑。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `LISTENERS` | `static Map<String,DBStatusListener>` | 监听器缓存 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `addListener(listener)` / `removeListener(listener)` | 注册 / 移除 | 按 `getKey` 存取 |
  | `getListener(key)` | 查找 | |
  | `bindListener(node,listener)` | 绑定 | 按节点类型分派：`TextInputControl.textProperty` / `ComboBox.selectedItemProperty` / `CheckBox.selectedProperty` / `Property` |
  | `unbindListener(node,listener)` | 解绑 | 同类型分派移除监听器 |
- 调用链：`DBStatusTableView.itemList 变更 → statusProperty().addListener(statusListener)`
