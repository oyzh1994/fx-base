# fx-terminal 终端（`cn.oyzh.fx.terminal`）

覆盖 `cn.oyzh.fx.terminal` 及其子包 `command`/`complete`/`exception`/`execute`/`help`/`histroy`/`key`/`mouse`/`standard`/`util` 共 23 个正式类/接口。

跳过项（整文件被注释的死代码）：`TerminalConst`、`TerminalTextArea`、`TerminalTextAreaPane`、`TerminalHistoryStore`、`TerminalScanner`。另 `TerminalManager2` 已标注 `@Deprecated`（与 `TerminalManager` 的旧版实现）。

---

## Terminal
- 职责：终端接口，定义输入输出、光标/边界、字体/提示符、各类处理器读写等能力。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `clearAll()` / `clearInput()` / `coverInput(input)` | 清全部 / 清输入 / 覆盖输入 | 抽象 |
  | `getInput()` | 取当前输入 | 抽象 |
  | `checkNop()` / `flushNOP()` / `getNOP()` | 边界检查 / 刷新 / 读取 | 抽象 |
  | `output(output)` / `outputLine(output)` / `outputByPrompt/outputByAppend/appendByPrompt` | 各类输出 | 抽象 |
  | `outputLineBreak()` | 输出换行 | 内容未以换行结尾时输出 `lineEndingText` |
  | `enableInput()` / `disableInput()` | 启/禁用输入 | 抽象 |
  | `onCommand(input)` | 收到指令 | 抽象 |
  | `helpHandler/completeHandler/keyHandler/mouseHandler/historyHandler` 的 getter/setter | 处理器读写 | 抽象 |
  | `saveHistory(input)` / `clearHistory()` | 保存 / 清除历史 | 委托 `historyHandler()` |
  | `flushCaret()` / `moveCaretEnd()` / `moveCaretEnd(delay)` | 光标刷新 / 移尾 | 带 delay 经 `ExecutorUtil.start` |
  | 字体 `fontFamily/fontSize/fontSizeIncr/fontSizeDecr`、提示符 `prompt()/promptLength()/outputPrompt()/flushPrompt()` | 字体 / 提示符 | 抽象（`promptLength` 默认取 prompt 长度） |
  | `content()/contentLength()/cutContent()/pasteContent()/selectContent(start,end)` | 内容操作 | 抽象 |
  | `onError(Throwable/String)` | 错误处理 | 委托 `outputByPrompt` |
  | `terminalName()` / `lineEndingText()` | 终端名 / 换行文本 | 抽象 |
- 调用链：`onError → outputByPrompt`；`saveHistory → historyHandler().saveHistory`

## TerminalPane
- 职责：命令行组件（继承 `Editor` 实现 `Terminal`），基于编辑器实现终端交互、边界控制与处理器联动。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `NOP` | `AtomicInteger`（final） | 不可操作边界 |
  | `prompt` | `String` | 提示符 |
  | `keyHandler` | `TerminalKeyHandler<?>` | 键盘处理器 |
  | `helpHandler` | `TerminalHelpHandler` | 帮助处理器 |
  | `mouseHandler` | `TerminalMouseHandler` | 鼠标处理器 |
  | `historyHandler` | `TerminalHistoryHandler` | 历史处理器 |
  | `completeHandler` | `TerminalCompleteHandler` | 补全处理器 |
  | `caretPositionListener` | `InvalidationListener` | 光标位置监听（越界时刷新 NOP，光标 < NOP 则禁输入） |
  | `keyPressedHandler` / `mousePressedHandler` | `EventHandler` | 键盘按下 / 鼠标按下事件过滤器 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `keyHandler(handler)` | 设键盘实现 | 移除旧过滤器防泄漏，注册 KEY_PRESSED 过滤器分派 ENTER/TAB/BACK_SPACE/方向键/翻页/Ctrl+A/E/Z/Y/X/V/C/HOME/END，并处理 Mac/非 Mac 的字体增减键 |
  | `mouseHandler(handler)` | 设鼠标实现 | 注册 MOUSE_PRESSED，主键→`onPrimaryMousePressed`，次键→`onSecondMousePressed` |
  | `checkNop()` | 是否在边界内 | `caretPosition() <= getNOP()` |
  | `flushNOP()` | 刷新边界 | NOP=内容长度，光标移尾 |
  | `getInput()` | 取输入 | 取最后一行，去提示符前缀 |
  | `clearInput()` / `coverInput(input)` | 清 / 覆盖输入 | 基于 `deleteText`/`replaceText`（NOP..len） |
  | `output(output)` | 覆盖输出 | `replaceText(NOP,len,output)` 后滚到底 |
  | `outputLine(output[,endLine])` | 行输出 | `appendLine` |
  | `outputByPrompt(output)` | 提示符模式输出 | 输出行 + `outputPrompt` |
  | `outputByAppend(output)` / `appendByPrompt(output)` | 追加输出 | `appendContent` / 先提示符再 `appendText` |
  | `prompt()/prompt(prompt)` | 提示符读写 | 写入时去换行 |
  | `outputPrompt()` | 输出提示符 | 文本仅换行则置为提示符，否则未以提示符结尾则追加；刷新 NOP |
  | `enableInput/disableInput` | 启/禁用输入 | 设置可编辑 |
  | `onCommand(input)` | 执行命令 | `findHandler` → `parseCommand` → `execute`，按结果输出（空/错误/成功且非忽略输出） |
  | `findHandler(input)` | 查找处理器 | `TerminalManager.findHandler(terminalName,input)` |
  | `flushCaret()` / `flushAndMoveCaretEnd()` | 光标复位 | 后者延迟 50ms 刷新并移尾 |
  | `getPrompts()` | 命令提示集 | 由 `TerminalManager.listHandler` 汇总命令名/子名/全名 |
  | `destroy()` | 销毁 | 移除监听器与事件过滤器 |
  | `clearAll()` | 清空 | `clear()` + `output("")` |
- 调用链：`onCommand → TerminalManager.findHandler → handler.parseCommand → handler.execute → outputByPrompt`

## command 包

## TerminalCommand
- 职责：终端命令，保存完整内容、命令名与参数列表。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `content` | `String` | 完整内容 |
  | `command` | `String` | 命令名 |
  | `args` | `String[]` | 参数列表 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `parseArgs(words)` | 解析参数 | `words[0]` 为命令，其余为 args |
  | `argsList()` | 参数列表 | 空返回空列表，否则 `List.of(args)` |
  | 各 `getXxx/setXxx` | 读写 | |
- 调用链：`BaseTerminalCommandHandler.parseCommand → command.parseArgs`

## TerminalCommandHandler
- 职责：终端命令处理器接口，融合补全与执行能力并定义命令元信息。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `parseCommand(input)` | 解析命令 | 抽象 |
  | `commandName()` / `commandSubName()` / `commandFullName()` | 命令名 / 子名 / 全名 | `commandFullName` 默认 `name` 或 `name subName` |
  | `commandSupportedVersion()` / `commandArg()` / `commandDesc()` / `commandDeprecated()` | 版本 / 参数 / 描述 / 是否过时 | 默认值 |
  | `commandHelp(terminal)` | 命令帮助 | 拼接 `name arg`，空则空串 |
- 调用链：`TerminalPane.onCommand → parseCommand/execute`；`TerminalManager` 汇总元信息

## BaseTerminalCommandHandler
- 职责：基础终端命令处理实现，提供参数校验与命令解析模板。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `parseCommand(line)` | 解析命令 | `TerminalUtil.split` 后 `checkArgs`，不合法抛 `TerminalException` |
  | `completion(line,terminal)` | 补全 | 默认 false |
  | `checkArgs(words)` | 参数校验 | 默认 `words != null && length >= 1` |
  | `parseCommand(line,words)` | 构建命令 | 新建 `TerminalCommand` 并 `parseArgs` |
- 调用链：`parseCommand(line) → split → checkArgs → parseCommand(line,words)`

## complete 包

## TerminalCompleteHandler
- 职责：补全处理器接口。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `completion(input,terminal)` | 补全 | 抽象，返回是否已处理 |
- 调用链：`TerminalKeyHandler.onTabKeyPressed → completeHandler().completion`

## BaseTerminalCompleteHandler
- 职责：基础补全实现，按匹配处理器数量分派补全行为。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `findCommandHandlers(terminal,line)` | 查找处理器 | 含空格用 matchType 3，否则用 1 |
  | `completion(line,terminal)` | 补全 | 空输入→执行 `HelpTerminalCommandHandler` 输出帮助；否则按匹配数量 `noMatch/oneMatch/multiMatch` |
  | `oneMatch(...)` | 单匹配 | 若输入非以全名开头则覆盖输入为全名，否则 `handler.completion` |
  | `multiMatch(...)` | 多匹配 | 并行取全名，`TextUtil.beautifyFormat` 输出，覆盖输入为公共前缀 |
  | `noMatch(...)` | 无匹配 | 空实现 |
- 调用链：`completion → findCommandHandlers → TerminalManager.findHandlers → oneMatch/multiMatch`

## exception 包

## TerminalException
- 职责：终端异常（运行时）。
- 字段：无
- 方法：`TerminalException(msg)` 构造。
- 调用链：`BaseTerminalCommandHandler.parseCommand → throw TerminalException`

## execute 包

## TerminalExecuteHandler
- 职责：终端执行处理器接口。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `execute(command,terminal)` | 执行命令 | 抽象，返回 `TerminalExecuteResult` |
- 调用链：`TerminalPane.onCommand → execute`

## TerminalExecuteResult
- 职责：终端执行结果，封装结果、错误、异常及是否忽略输出。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `result` | `Object` | 结果 |
  | `errMsg` | `String` | 错误信息 |
  | `exception` | `Exception` | 异常 |
  | `ignoreOutput` | `boolean` | 是否忽略输出 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `isSuccess()` | 是否成功 | `errMsg == null && exception == null` |
  | `getErrMsg()` | 错误信息 | 优先 `errMsg`，否则异常消息 |
  | `appendResult(result)` | 追加结果 | 拼接字符串 |
  | `result()` | 结果字符串 | null 时返回空串 |
  | 各 `getXxx/setXxx` | 读写 | |
  | `static ok()` / `static fail(exception)` | 成功 / 失败工厂 | |
- 调用链：`TerminalPane.onCommand → isSuccess/getErrMsg/result`

## help 包

## TerminalHelpHandler
- 职责：终端帮助处理器接口。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `help(input,terminal)` | 帮助 | 抽象 |
- 调用链：`TerminalKeyHandler.onEnterKeyPressed → helpHandler().help`

## BaseTerminalHelpHandler
- 职责：基础终端帮助实现。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `help(input,terminal)` | 帮助 | 截取 ` -?` 前的命令，`TerminalManager.findHandler` 后输出 `commandHelp` |
- 调用链：`help → TerminalManager.findHandler → commandHelp`

## histroy 包

## TerminalHistory
- 职责：命令行历史记录项（可序列化，支持比较）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `line` | `String`（@Column） | 命令行 |
  | `saveTime` | `long`（@Column） | 保存时间 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `equals(obj)` | 相等 | 比较 `line` 与 `saveTime` |
  | `compare(obj)` | 比较 | 委托 `equals` |
  | `getLine/setLine` / `getSaveTime/setSaveTime` | 读写 | |
- 调用链：`TerminalHistoryHandler.saveHistory → new TerminalHistory`

## TerminalHistoryHandler
- 职责：命令历史处理器接口。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `prevCommand(terminal)` / `nextCommand(terminal)` | 上 / 下一条 | 抽象 |
  | `saveHistory(input)` | 保存历史 | 非空则新建 `TerminalHistory` 并 `addHistory` |
  | `clearHistory()` / `listHistory()` / `addHistory(history)` | 清除 / 列表 / 添加 | 抽象 |
- 调用链：`Terminal.saveHistory → historyHandler().saveHistory → addHistory`

## BaseTerminalHistoryHandler
- 职责：基础命令历史处理器，基于历史列表实现上下切换。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `history` | `TerminalHistory` | 当前命令 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `prevCommand(terminal)` | 上一条 | 输入为空则重置；有当前则取前一条，否则取最后一条 |
  | `nextCommand(terminal)` | 下一条 | 输入为空则重置；有当前则取后一条 |
- 调用链：`TerminalKeyHandler.onUpKeyPressed → prevCommand`

## key 包

## TerminalKeyHandler
- 职责：终端键盘按键处理器接口，提供各按键默认处理。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `onTabKeyPressed(terminal)` | Tab | 有补全处理器则 `completion` |
  | `onEnterKeyPressed(terminal)` | 回车 | 含帮助参数走 `helpHandler().help`，否则 `onCommand` + `saveHistory` |
  | `onUpKeyPressed/onDownKeyPressed(terminal)` | 上/下 | 历史 `prevCommand`/`nextCommand` 并 `output` |
  | `onLeftKeyPressed/onBackspaceKeyPressed(terminal)` | 左 / 退格 | `!terminal.checkNop()` |
  | `onHomeKeyPressed/onEndKeyPressed(terminal)` | Home/End | 光标置 NOP / 内容末尾 |
  | `onCtrlAKeyPressed(terminal)` | Ctrl+A | `selectContent(NOP, len)` |
  | `onCtrlEKeyPressed(terminal)` | Ctrl+E | 光标置末尾 |
  | `onCtrlXKeyPressed/onCtrlVKeyPressed(terminal)` | Ctrl+X/V | `cutContent` / `pasteContent` |
  | `onCtrlCKeyPressed/onCtrlYKeyPressed/onCtrlZKeyPressed/onPageUpKeyPressed/onPageDownKeyPressed(terminal)` | 其他 | 默认返回 false/true（不处理） |
- 调用链：`TerminalPane 键盘过滤器 → onEnterKeyPressed → terminal.onCommand`

## mouse 包

## TerminalMouseHandler
- 职责：终端鼠标按键处理器接口。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `onPrimaryMousePressed(terminal)` | 主键 | 默认 true |
  | `onSecondMousePressed(terminal)` | 次键 | 默认 true |
- 调用链：`TerminalPane 鼠标过滤器 → onPrimaryMousePressed/onSecondMousePressed`

## standard 包

## ClearTerminalCommand
- 职责：清除终端命令，含是否清除历史标记。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `clearHis` | `boolean` | 是否清除历史 |
- 方法：`isClearHis/setClearHis` 读写。
- 调用链：`ClearTerminalCommandHandler.parseCommand → new ClearTerminalCommand`

## ClearTerminalCommandHandler
- 职责：`clear` 命令处理器。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `commandName()` | 命令名 | 返回 `clear` |
  | `commandArg()` / `commandDesc()` / `commandHelp(terminal)` | 元信息 | 参数 `[-his]`，帮助追加 `-his 历史` |
  | `execute(command,terminal)` | 执行 | `terminal.clearAll()`，`-his` 时 `clearHistory` |
  | `checkArgs(words)` | 参数校验 | 长度 1 或 2 |
  | `parseCommand(line,words)` | 解析 | 第二参数为 `-his` 时置 `clearHis` |
- 调用链：`execute → terminal.clearAll/clearHistory`

## HelpTerminalCommandHandler
- 职责：`help` 命令处理器，罗列命令清单。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `commandName()` / `commandDesc()` | 元信息 | 命令名 `help` |
  | `execute(command,terminal)` | 执行 | 汇总 `TerminalManager.listHandler` 的命令名/版本/状态/描述，`TextUtil.beautifyFormat` 格式化输出 |
- 调用链：`execute → TerminalManager.listHandler → beautifyFormat`

## util 包

## TerminalManager
- 职责：终端管理类，按终端名称注册、加载与查找命令处理器。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `COMMAND_HANDLERS` | `static Map<String,List<TerminalCommandHandler>>` | 终端名→处理器列表 |
  | `LOAD_HANDLES` | `static Map<String,Runnable>` | 终端名→加载操作 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `setLoadHandler(name,loadHandler)` | 设置加载操作 | |
  | `doLoadHandler(name)` | 执行加载 | 移除并运行加载器 |
  | `listHandler(name)` | 处理器列表 | 先 `doLoadHandler` 再取副本 |
  | `registerHandler(name,Class)` / `registerHandler(name,handler)` | 注册 | 类注册做去重与实例化；实例注册追加 |
  | `findHandler(name,Class)` | 按类型查找 | 遍历比较 `getClass()` |
  | `findHandler(name,input)` | 按输入查找 | 按首词匹配命令名；带第二词时先精确匹配子名，否则用 `TextUtil.clacCorr` 取最相近 |
  | `findHandlers(name,commandText,matchType)` | 批量查找 | matchType 1 前缀 / 2 全等 / 3 双向前缀，按全名排序 |
- 调用链：`TerminalPane.findHandler → TerminalManager.findHandler → listHandler → doLoadHandler`

## TerminalManager2
- 职责：终端管理类的旧版实现（全局单列表，无终端名维度）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `COMMAND_HANDLERS` | `static List<TerminalCommandHandler>` | 全局处理器列表 |
  | `loadHandlerAction` | `static Runnable` | 加载动作 |
  | `loaded` | `static boolean` | 是否已加载 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `setLoadHandlerAction/getLoadHandlerAction` | 加载动作读写 | |
  | `doLoadHandler()` | 执行加载 | 未加载且有动作则执行 |
  | `listHandler()` / `registerHandler(Class/handler)` / `findHandler(Class/input)` / `findHandlers(commandText,matchType)` | 列表 / 注册 / 查找 | 逻辑同 `TerminalManager` 但无终端名参数 |
- 调用链：同 `TerminalManager`（旧版本，`@Deprecated`）

## TerminalUtil
- 职责：终端工具类，提供命令提取、输入切割与帮助判断。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getCommand(input)` | 取命令 | `split` 后第一个词 |
  | `split(input)` | 切割输入 | null 返回空数组，否则 `trim().split(" ")` |
  | `hasHelp(input)` | 是否有帮助参数 | `endsWith(input," -?")` |
- 调用链：`BaseTerminalCommandHandler.parseCommand → TerminalUtil.split`
