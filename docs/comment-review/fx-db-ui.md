# fx-db UI 组件与工具（`ui`/`util`）

覆盖 `cn.oyzh.fx.db.ui` 与 `cn.oyzh.fx.db.util` 共 9 个正式类。

---

## ui 包

## DBColumnComboBox
- 职责：数据库字段选择框，展示并选择数据库字段。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `DBColumnComboBox()` / `DBColumnComboBox(columns)` | 构造 | 后者 `setItem(columns)` |
  | `select(colName)` | 按名选中 | 忽略大小写匹配 `getName` 后 `select(object)` |
  | `getColumnName()` | 取选中字段名 | `getSelectedItem().getName()` |
  | `initNode()` | 初始化 | 设置转换器（显示 `getName`） |
- 调用链：`select → equalsIgnoreCase → select(object)`

## DBFiledTypeComboBox
- 职责：DB 字段类型选择框，按方言加载字段类型并提供类型能力判断。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `dialect` | `DBDialect` | 数据库方言 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `setDialect(dialect)` | 设方言并加载类型 | `setItem(DBColumnFieldManager.fieldNames(dialect))` |
  | `getDialect()` | 取方言 | |
  | `supportSize/supportDigits/supportAutoIncrement/supportDefaultValue/supportTimestamp/supportJson/supportValue` | 能力判断 | 委托 `DBColumnFieldManager`（注意：前七者硬编码 `DBDialect.DAMENG`，`supportCharset` 用 `MYSQL`） |
  | `supportCharset()` | 是否支持字符集及排序 | `DBColumnFieldManager.supportCharset(MYSQL, selected)` |
  | `exampleValue()` | 示例值 | `DBColumnFieldManager.exampleValue(DAMENG, selected)` |
  | `select(type)` | 按类型选中 | 非空则大写后 `super.select`，否则清空选择 |
- 调用链：`setDialect → DBColumnFieldManager.fieldNames`；`supportXxx → DBColumnFieldManager.supportXxx`

## DBJoinSymbolComboBox
- 职责：连接符选择框，提供 AND、OR。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 加 `AND`、`OR` |
- 调用链：`DBRecordFilter.getJoinSymbolControl → selectFirstIfNull`

## DBNameComboBox
- 职责：数据库名称选择框，展示并选择数据库对象名称。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 设置转换器显示 `DBName.getName` |
- 调用链：`initNode → SimpleStringConverter.toString`

## DBStatusColumn
- 职责：状态列，以图标形式展示数据库对象状态。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造()` | 构造 | 值工厂 `"status"`，宽 25、不可排序/调整/重排，标题 `I18nHelper.status()` |
  | `initNode()` | 初始化 | `showGraphicOnlyLater()` 仅显示图标 |
- 调用链：`DBStatusColumn → DBObject.statusProperty`

## DBStatusTableView
- 职责：带状态管理的表格视图，维护数据项状态监听与已删除项。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `deleteItems` | `List<S>` | 已删除的数据项（CopyOnWriteArrayList 惰建） |
  | `statusListener` | `DBStatusListener` | 状态监听器 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `reset()` | 重置 | 清空 `deleteItems` 并 `clearStatus` |
  | `clearStatus()` | 清除所有项状态 | 遍历 `getItems` 调 `clearStatus` |
  | 实例初始化块 | 监听数据变化 | `itemList().addListener`：replaced/added 时为新项绑定状态监听器，removed 时非 created 项记入 `deleteItems` 并解绑；added 后触发一次监听回调 |
  | `getDeleteItems/setDeleteItems` / `getStatusListener/setStatusListener` | 读写 | |
- 调用链：`itemList 变更 → statusProperty().add/removeListener`；`reset → clearStatus`

## util 包

## DBNodeUtil
- 职责：数据库节点工具类，按字段类型创建节点、读写节点值、计算节点背景色。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getNodeVal(node)` | 取节点值 | 按类型分派 `FXTextField.getValue` / `TextField/TextArea/CodeArea.getText` / `ComboBox.selectedItem` |
  | `setNodeVal(node,val)` | 设节点值 | 同上分派写入 |
  | `getNodeBackground(column)` | 节点背景色 | 按字段能力返回不同颜色（json/jsonArray/text/binary/enum/integer/bigInteger/digits/bit/boolean/时间类/geometry），默认 `#FDD4D3` |
  | `getNode(column,obj)` | 创建带值节点 | 按能力选择 `JsonTextFiled/LongTextFiled/BinaryTextFiled/NumberTextField/DecimalTextField/BitTextField/BooleanTextFiled/DateTimeTextField/ExampleTextField/LimitTextField/DateTextField/TimeTextField/YearTextField/FXTextField`，并设背景 |
  | `generateNode(column)` | 生成空节点 | 同上按能力创建（`ClearableTextField` 为默认），bit 按 `size*8` |
- 调用链：`DBRecordProperty 编辑 → getNode/setNodeVal/getNodeVal`

## DBDataUtil
- 职责：数据库数据工具类，负责数据值转义与 SQL 参数化。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `escapeQuotes(str,dialect)` | 转义引号 | MySQL/达梦：去首尾引号→临时占位 `0x_oyzh_x0` 保护 `''`→`TextUtil.escape` 将 `'` 转 `''`→还原；其他方言 `TextUtil.escape` |
  | `parameterizedForSql(column,value,dialect)` | SQL 参数化 | null→`"NULL"`；几何→`ST_GeomFromText('...')`；二进制→`0x` 十六进制；布尔→`1/0`；bit→`b'...'`；其余 `DBUtil.wrapData` |
- 调用链：`parameterizedForSql → DBUtil.wrapData → escapeQuotes`

## DBUtil
- 职责：数据库工具类，提供 SQL/数据打印、参数设置、数据与名称包装、名称生成等通用方法。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `ENABLE_PRINT_METADATA` | `static boolean` | 是否打印元数据，默认 true |
  | `SQL_KEYWORDS` | `static final String[]` | SQL 关键字数组 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `printMetaData(resultSet)` | 打印列名 | 开启时遍历 `ResultSetMetaData` 输出列名 |
  | `printSql(sql)` / `printInfo(sql,data)` / `printData(data)` | 打印 SQL / 数据 | 经 `JulLog.info` |
  | `setVal(statement,val,index)` | 设置预处理参数 | 按类型分派 `setNull/setBytes/setBoolean/.../setDate/setTimestamp/setObject` |
  | `isSameVal(val,nVal)` | 值是否相同 | 引用相等或 `Objects.equals`；Number 比较 double 值；byte[] 转字符串比较 |
  | `rollback(connection)` | 回滚 | 非自动提交时 rollback |
  | `executeUpdate(statement)` | 执行更新 | `executeUpdate` 后关闭语句 |
  | `wrap(name,dialect)` | 名称包装 | MySQL 加反引号，达梦加双引号（已含则不加） |
  | `wrap(dbName,tableName,dialect)` | 库表名包装 | `wrap(db)+"."+wrap(table)` |
  | `wrapData(val,dialect)` | 数据包装 | MySQL/达梦下字符串转义后加单引号；Date/Temporal 加单引号 |
  | `unwrapData(val,dialect)` | 取消数据包装 | 去首尾引号，空返回 null |
  | `suitableColumnWidth(column)` | 合适列宽 | 名称/类型文本宽度取大 + 50 |
  | `nullPromptText()` | null 提示文本 | 返回 `(Null)` |
  | `genIndexName/genCheckName/genTriggerName/genForeignKeyName/genCopyName/genCloneName` | 生成名称 | 前缀 + `UUIDUtil.uuidSimple()` 前 5 位 |
  | `removeComment(sql)` | 移除注释 | 跳过 `-- `、`#` 开头行，`/* */` 块整体跳过 |
- 调用链：`setVal → PreparedStatement.setXxx`；`wrapData → DBDataUtil.escapeQuotes`
