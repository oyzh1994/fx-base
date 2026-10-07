# fx-db 核心类（`cn.oyzh.fx.db`）

覆盖根包 `cn.oyzh.fx.db` 下 24 个正式类/接口。DB 对象状态、命名校验、连接管理、方言与字段类型能力查询、SQL 生成基类等。子包（`data`/`query`/`sql`/`condition`/`listener`/`ui`/`util`）见 `fx-db-data.md`、`fx-db-query.md`、`fx-db-ui.md`。

---

## DBCheck
- 职责：检查接口，用于校验数据库对象名称是否有效。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `isInvalid()` | 名称是否无效 | `StringUtil.isBlank(getName())` |
- 调用链：`DBCheck.isInvalid → DBName.getName → StringUtil.isBlank`

## DBClient
- 职责：数据库客户端接口，定义 JDBC 通用能力（连接、版本、特性、批量插入、自增键）。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getConnManager()` / `getProperties()` | 连接管理器 / 属性表 | |
  | `getProperty/hasProperty/putProperty` | 属性读写 | 基于 `getProperties()` 判空 |
  | `isReadonly()` / `dialect()` | 只读模式 / 方言 | |
  | `tableSize/viewSize/procedureSize/functionSize(dbName)` | 各类对象数量 | |
  | `selectVersion()` / `selectProduct()` | 数据库版本 / 产品 | |
  | `insertBatch(dbName,sqlList)` | 批量插入 | 返回插入行数 |
  | `isSupportFeature(feature)` | 特性支持判断 | 抽象，子类实现 |
  | `isSupportCheckFeature()` / `isSupportEventFeature()` | 便捷特性判断 | 转调 `isSupportFeature(CHECK/EVENT)` |
  | `getGeneratedKeys(Statement)` | 获取自增键 | 抽象，子类实现 |
- 调用链：`isSupportCheckFeature → isSupportFeature → DBFeature.CHECK`

## DBColumn
- 职责：数据库列接口，提供名称/类型/长度读写及类型能力（大小、无符号、json、时间等）判断。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getType/setType` / `getSize/setSize` | 类型 / 长度读写 | |
  | `supportXXX()` 系列 | 是否支持某能力 | default 全部 `return false`，子类覆写 |
  | `minValue()` / `maxValue()` | 取值上下限 | default `null` |
  | `exampleValue()` | 示例值 | default `null` |
  | `isYearType/isTimeType/isDateType/isDateTimeType()` | 类型归类判断 | default `false` |
  | `getDigits()` | 浮点位 | default `null` |
  | `isInvalid()` | 是否无效 | 名称或类型任一空白 |
- 调用链：`DBColumnFieldManager.supportSize → DBColumnField.supportSize`（能力查询链路）

## DBColumnField
- 职责：数据库字段定义，描述某数据类型的特性开关与取值范围。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `name` | `String` | 字段/类型名称 |
  | `alias` | `String` | 别名 |
  | `maxValue` / `minValue` | `Long` | 最大 / 最小值 |
  | `suggestSize` | `Integer` | 推荐字段长度 |
  | `exampleValue` | `String` | 示例值 |
  | `defaultValue` | `Object` | 默认值 |
  | `supportBit/supportJson/supportJsonArray/supportText/supportEnum/supportValue/supportBinary/supportDigits/supportString/supportBoolean/supportKeySize/supportInteger/supportBigInteger/supportCharset/supportUnsigned/supportZeroFill/supportGeometry/supportTimestamp/supportDefaultValue/supportAutoIncrement/supportSize` | `boolean` | 各能力开关，默认 false |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造 DBColumnField(String)` | 以名称构造 | 仅设置 `name` |
  | `getName()` | 获取名称 | |
- 调用链：`DBColumnFieldManager.fields → List<DBColumnField>`

## DBColumnFieldManager
- 职责：按方言维护字段定义，提供类型能力/取值查询的静态门面。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `INITIALIZERS` | `static Map<DBDialect,Runnable>` | 方言字段定义的懒初始化器 |
  | `COLUMN_FIELD` | `static Map<DBDialect,List<DBColumnField>>` | 方言对应的字段定义列表 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `registerInitializer(dialect,func)` | 注册初始化器 | 存入 `INITIALIZERS` |
  | `putFiled(dialect,columnField)` | 添加字段定义 | 参数判空抛 NPE，追加到列表 |
  | `fields(dialect)` | 取字段定义列表 | 同步块内若无则触发一次初始化器并移除；缺失返回空列表 |
  | `fieldNames(dialect)` | 字段名列表 | `fields().parallelStream().map(getName)` |
  | `supportXXX(dialect,type)` 系列 | 类型能力查询 | 遍历 `fields`，`equalsAnyIgnoreCase(type,name,alias)` 命中后返回对应 `supportXXX` |
  | `suggestSize(dialect,type)` | 推荐长度 | 同上匹配返回 `suggestSize` |
  | `exampleValue/defaultValue/minValue/maxValue(dialect,type)` | 取值查询 | 同上匹配返回对应字段 |
- 调用链：`supportSize → fields → （命中）DBColumnField.supportSize`

## DBConnConfig
- 职责：数据库连接配置，保存地址、认证、SSL、代理、超时等信息。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `env` | `String` | 环境标识 |
  | `host` | `String` | 主机地址 |
  | `port` | `Integer` | 端口 |
  | `user` | `String` | 用户名 |
  | `password` | `String` | 密码 |
  | `useSSL` | `boolean` | 是否启用 SSL |
  | `proxyHost` | `String` | 代理主机 |
  | `proxyPort` | `Integer` | 代理端口 |
  | `proxyUser` | `String` | 代理用户名 |
  | `proxyType` | `String` | 代理类型 |
  | `proxyPassword` | `String` | 代理密码 |
  | `socketFactory` | `String` | 套接字工厂 |
  | `connectTimeout` | `int` | 连接超时(秒)，默认 5 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 各字段 `getXxx/setXxx`（含 `isUseSSL/setUseSSL`） | 标准读写 | |
- 调用链：`DBConnManager.connection → initConnection(config.user, config.password)`

## DBConnManager
- 职责：数据库连接管理器（抽象），维护并复用服务/库/函数/过程连接。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `config` | `DBConnConfig` | 连接配置（protected） |
  | `connections` | `Map<String,Connection>` | 以带前缀的键缓存的连接（ConcurrentHashMap） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `addConnection/addFunctionConnection/addProcedureConnection(dbName,conn)` | 缓存连接 | 键前缀 `db_/function_/procedure_connection_` |
  | `getConnection/getFunctionConnection/getProcedureConnection(dbName)` | 取连接 | |
  | `getServerConnection/setServerConnection` | 服务连接读写 | 键 `server_connection` |
  | `getConnections()` | 连接表 | |
  | `isValid(conn)` | 连接是否有效 | 判空/关闭，`isValid(timeout/1000)` |
  | `connection()` | 取服务连接（无效则重建） | `initConnection(null,user,password)` |
  | `connection(name)` | 取库连接 | 无效则重建并 `setAutoCommit(true)`，记录日志 |
  | `functionConnection(dbName)` / `procedureConnection(name)` | 取函数/过程连接 | 同上，无效重建 |
  | `newConnection(name)` | 新建连接 | 直接 `initConnection` 并 `setAutoCommit(true)` |
  | `initConnection(...)` / `getConnectionString()` | 抽象：建连 / 连接串 | 子类实现 |
  | `close()` | 关闭全部 | 遍历关闭并清空 |
  | `getConfig/setConfig` / `getConnectTimeout/setConnectTimeout` | 配置 / 超时 | 超时读写委托 `config` |
- 调用链：`connection(name) → getConnection → isValid → initConnection → addConnection`

## DBDatabse
- 职责：数据库接口，校验数据库名称是否有效。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `isInvalid()` | 名称是否无效 | `StringUtil.isBlank(getName())` |
- 调用链：同 `DBCheck`

## DBDialect
- 职责：数据库类型（方言）枚举。
- 字段（枚举常量）：`MYSQL`（MySQL）、`MONGODB`（MongoDB）、`DAMENG`（达梦）
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `valueList()` | 所有方言列表 | `Collections.addAll(values())` |
- 调用链：`DBColumnFieldManager.registerInitializer(dialect,...)`

## DBFeature
- 职责：数据库特性枚举。
- 字段（枚举常量）：`CHECK`（检查约束）、`EVENT`（事件/调度器）
- 方法：无
- 调用链：`DBClient.isSupportCheckFeature → isSupportFeature(CHECK)`

## DBForeignKey
- 职责：外键接口，校验外键名称是否有效。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `isInvalid()` | 名称是否无效 | `StringUtil.isBlank(getName())` |
- 调用链：同 `DBCheck`

## DBName
- 职责：命名接口，提供名称读写与“是否新对象”判断，是各 DB 对象接口的基接口。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getName()` / `setName(name)` | 名称读写 | 抽象 |
  | `isNew()` | 是否新数据 | `StringUtil.isBlank(getName())` |
- 调用链：`DBName ← DBCheck/DBDatabse/DBColumn/DBTable/DBView/...`

## DBObject
- 职责：数据库对象基类，维护变更/新增/删除状态、状态标识与原始数据快照。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `changedProperty` | `SimpleBooleanProperty` | 是否变更（懒建） |
  | `deletedProperty` | `SimpleBooleanProperty` | 是否删除（懒建） |
  | `createdProperty` | `SimpleBooleanProperty` | 是否新增（懒建） |
  | `changedFlag` | `Map<String,Boolean>` | 逐字段变更标记 |
  | `originalData` | `Map<String,Object>` | 原始数据快照 |
  | `statusProperty` | `SimpleStringProperty` | 状态标识（`+`新增 / `*`变更 / 空） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `changedProperty()/deletedProperty()/createdProperty()` | 状态属性懒建 | |
  | `setChangedFlag(key,value)` | 设置单键变更标记 | 值真则 put，否则 remove；据集合空否刷新 `changed` |
  | `putOriginalData(key,value)` | 记录原始数据 | 首次仅存；已存在则比较差异设置变更标记 |
  | `getOriginalData/hasOriginalData/checkOriginalData(key[,cur])` | 原始数据读/判/比 | `Objects.equals` 比较 |
  | `clearChangedFlag()/clearOriginalData()` | 清空标记/原始数据 | |
  | `setChanged/isChanged`、`setDeleted/isDeleted`、`setCreated/isCreated` | 三态读写 | set 后调用 `updateStatus()` |
  | `updateStatus()` | 刷新状态标识 | created→`+`，changed→`*`，否则空 |
  | `clearStatus()` | 清空三态与标记 | |
  | `initStatus()` | 空实现（预留） | |
  | `destroy()` | 销毁 | 解绑各属性、清空标记与原始数据 |
- 调用链：`setCreated → updateStatus → statusProperty.set("+")`

## DBObjectList
- 职责：数据库对象列表基类（继承 `ArrayList<DBObject>`），按新增/变更/删除状态筛选。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `TYPE_NORMAL/TYPE_DELETED/TYPE_CREATED/TYPE_CHANGED` | `static byte`（0/1/2/3） | 筛选类型常量 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `isChanged()` | 是否存在状态变化对象 | 任一 `getStatus()` 非空 |
  | `createdList/changedList/deletedList/normalList()` | 按状态取子列表 | `stream().filter(DBObjectList::isXxx)` |
  | `filterList(types...)` | 按类型集合筛选 | 按 type 分派到上述列表，空参数返回自身 |
  | `add(s)` / `remove(s)` / `contains(s)` | 增删判 | add 对 null 直接返回 false |
  | `hasDeleted/hasCreated/hasChanged/hasNormal()` | 是否存在某状态对象 | 遍历判断 |
  | 静态 `isDeleted/isCreated/isChanged/isNormal(DBObject)` | 状态判定 | created 需非 deleted，changed 需非 created/deleted |
- 调用链：`filterList → changedList → isChanged(静态)`

## DBObjects
- 职责：DBObjectList 的具体集合实现，提供扩展构造与工厂方法。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `DBObjects()` / `DBObjects(Collection<E>)` | 构造 | 集合构造 `super.addAll` |
  | `static of(list)` | 工厂 | `new DBObjects<>(list)` |
- 调用链：`DBObjects.of(list) → DBObjectList`

## DBRecordData
- 职责：数据库记录数据，保存 `DBColumn → 值` 映射，支持按列名查值/判空。
- 字段：继承 `HashMap<DBColumn,Object>`
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `columns()` | 列名集合 | `keySet` 并行流取 `DBColumn::getName` |
  | `notNullColumns()` | 有值列名集合 | `columns()` 过滤 `hasValue` |
  | `column(column)` | 按名查列 | `equalsAnyIgnoreCase` 匹配 |
  | `hasValue/notNull(column)` | 列是否有值 | `value(column) != null` |
  | `value(column)` | 按名列取值 | 遍历 `entrySet` 忽略大小写匹配 |
  | `remove(column)` | 移除列 | 先查列再 `super.remove` |
  | `entries()` | 键值对集合 | `super.entrySet()` |
  | `columnSize()` | 列数 | `super.size()` |
  | `isTypeGeometry(column)` | 列是否几何类型 | 列 `supportGeometry()` |
- 调用链：`notNullColumns → columns → value/hasValue`

## DBRecordFilter
- 职责：记录过滤条件，维护启用状态、连接符、列与值，并可生成对应编辑控件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `value` | `Object` | 过滤值 |
  | `enabled` | `boolean` | 是否启用，默认 true |
  | `joinSymbol` | `String` | 连接符号（AND/OR 等） |
  | `column` | `DBColumn` | 单列 |
  | `columns` | `List<? extends DBColumn>` | 列集合 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getEnabledControl()` | 生成启用复选框 | `FXCheckBox` + `selectedChanged` 回写 + 行点击选中 |
  | `getJoinSymbolControl()` | 生成连接符下拉 | `DBJoinSymbolComboBox`，空则选首项并回写 |
  | `getValue/setValue`、`isEnabled/setEnabled`、`getJoinSymbol/setJoinSymbol`、`getColumn/setColumn`、`getColumns/setColumns` | 标准读写 | |
- 调用链：`getJoinSymbolControl → DBJoinSymbolComboBox.selectFirstIfNull → setJoinSymbol`

## DBRecordProperty
- 职责：数据库表记录属性，作为绑定记录值编辑控件的可观察属性并跟踪变更。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `node` | `Node` | 绑定的编辑控件 |
  | `original` | `Object` | 原始值 |
  | `setToNullFlag` | `boolean` | 是否被设为 null 标志 |
  | `readonly` | `boolean` | 只读模式 |
  | `changedProperty` | `SimpleBooleanProperty` | 是否变更（懒建） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `discard()` | 抛弃变更 | `setChanged(false)` |
  | `changedProperty()/isChanged()/setChanged(b)` | 变更状态 | |
  | `getControl()/getNode()` | 取控件 | |
  | `vCopy()` / `vPaste()` | 复制 / 粘贴 | `ClipboardUtil.copy/paste(node)` |
  | `getOriginal/setOriginal` | 原始值读写 | |
  | `isReadonly()` | 是否只读 | |
  | `vSetToNull()` | 置为 null | TextField 空则直接标变更，否则清空；设提示文本、失焦，置标志 |
  | `vSetToEmptyString()` | 置空串 | TextField 空则标变更；`setText("")` |
  | `destroy()` | 销毁 | 销毁节点、解绑变更属性 |
- 调用链：`vSetToNull → DBUtil.nullPromptText`；`vCopy/vPaste → ClipboardUtil`

## DBRoutineSchema
- 职责：存储过程/函数模式接口，校验名称是否有效。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `isInvalid()` | 名称是否无效 | `StringUtil.isBlank(getName())` |
- 调用链：同 `DBCheck`

## DBSchema
- 职责：数据库模式接口，校验模式名称是否有效。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `isInvalid()` | 名称是否无效 | `StringUtil.isBlank(getName())` |
- 调用链：同 `DBCheck`

## DBSqlGenerator
- 职责：SQL 生成器基类，负责收集与拼接 SQL 语句。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `sqlList` | `List<String>` | SQL 列表（protected） |
  | `sqlBuilder` | `StringBuilder` | SQL 构建器（protected） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `buildSql()` | 构建 SQL 列表 | 将 builder 内容 trim 后 `addFirst` 到列表头部并返回 |
  | `buildSqlSingle()` | 构建单条 SQL | builder 内容 + 换行拼接列表各 SQL |
- 调用链：`子类生成器 → buildSql/buildSqlSingle`

## DBTable
- 职责：数据表接口，提供表注释读写。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `setComment/getComment` | 注释读写 | 抽象 |
  | `hasComment()` | 是否有注释 | `getComment() != null` |
  | `isInvalid()` | 名称是否无效 | `StringUtil.isBlank(getName())` |
- 调用链：同 `DBCheck`

## DBTrigger
- 职责：触发器接口，校验触发器名称是否有效。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `isInvalid()` | 名称是否无效 | `StringUtil.isBlank(getName())` |
- 调用链：同 `DBCheck`

## DBView
- 职责：视图接口，提供注释与可更新属性读写。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `setComment/getComment` | 注释读写 | 抽象 |
  | `hasComment()` | 是否有注释 | `getComment() != null` |
  | `setUpdatable/isUpdatable` | 可更新读写 | 抽象 |
  | `isInvalid()` | 名称是否无效 | `StringUtil.isBlank(getName())` |
- 调用链：同 `DBCheck`
