# fx-tty 终端仿真（`cn.oyzh.fx.tty`）

覆盖 `cn.oyzh.fx.tty` 及其子包 `zmodem` 共 19 个正式类/接口。基于 JediTerm + pty4j 的终端仿真：连接器、渲染画布、颜色调色板、字体度量、超链接与 ZModem 文件传输。

无整文件死代码。

---

## TtyAscii
- 职责：终端 ASCII 控制字符常量定义。
- 字段：均为 `static final byte` 常量，含 `ASCII_NUL`(0)、`ASCII_ESC`(27)、`ASCII_CTRL_A`(0x01)…`ASCII_CTRL_Z`(0x1A)、`ASCII_CTRL_SLASH`(0x1F)、`ASCII_CTRL_BACK_SLASH`(0x1C)。
- 方法：无
- 调用链：被各连接器/处理器引用

## TtyCharsetble
- 职责：字符集提供者接口。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `charset()` | 取字符集 | 抽象 |
- 调用链：`TtyProcessTtyConnector/TtyStreamConnector` 实现

## TtyColorPalette
- 职责：终端颜色调色板，基础前景/背景色取自当前主题，其余取自系统调色板并缓存。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `INSTANCE` | `static final TtyColorPalette` | 单例 |
  | `lastFGColor` / `lastBGColor` | `Color` | 缓存前景 / 背景色 |
  | `lastFGMethod` / `lastBGMethod` | `Method` | 缓存反射方法 |
  | `lastThemeFGColor` / `lastThemeBGColor` | `javafx.scene.paint.Color` | 缓存主题颜色 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getForegroundByColorIndex(i)` / `getBackgroundByColorIndex(i)` | 覆写取色 | 委托 `getPaletteColor(foreground,i)` |
  | `getPaletteColor(foreground,index)`（私有） | 取色 | 索引 0/7/8/15 用主题色（`TtyTerminalUtil.fromFXColor`，带缓存）；否则反射调用 `ColorPaletteImpl.WINDOWS_PALETTE/XTERM_PALETTE` 的方法 |
- 调用链：`getForegroundByColorIndex → getPaletteColor → TtyTerminalUtil.fromFXColor / ReflectUtil.invokeOnly`

## TtyFontMetrics
- 职责：终端字体度量信息，计算文本宽高与基线下降值。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `descent` / `width` / `height` | `double`（final） | 基线下降值 / 文本宽度 / 高度 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static create(font,str)` | 创建度量 | 用 `Text` 测量 `getLayoutBounds`，descent=高-基线偏移 |
  | `getDescent/getWidth/getHeight` | 读取 | |
  | `toString()` | 展示 | |
- 调用链：`create → Text.applyCss → getLayoutBounds`

## TtyHyperlinkFilter
- 职责：终端超链接过滤器，识别文本行中的 URL 并生成可点击链接。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `URL_PATTERN` | `static final Pattern` | 匹配 URL 的正则 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static canContainUrl(line)` | 快否判断 | 含 `mailto:`/`://`/`www.` |
  | `apply(line)` | 应用过滤 | 匹配 URL 生成 `LinkResultItem`（点击 `openUrl`） |
  | `openUrl(url)`（私有） | 打开链接 | `FXConst.getHostServices().showDocument` |
- 调用链：`apply → canContainUrl → LinkInfo(openUrl)`

## TtyKeyListener
- 职责：终端按键监听器接口。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `keyTyped(e)` / `keyPressed(e)` / `keyReleased(e)` | 字符 / 按下 / 释放 | 抽象 |
- 调用链：由终端输入组件回调

## TtyStreamable
- 职责：可流式访问的终端连接器接口（继承 `TtyConnector`），暴露底层输入输出流。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `input()` / `output()` | 输入 / 输出流 | 抽象 |
  | `writeLine(str)` | 写行 | `write(str + "\r")` |
- 调用链：`TtyZModemTtyConnector.read → connector.input/output`

## TtyTerminalSizeable
- 职责：可获取终端尺寸的接口。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getTermSize()` | 取尺寸 | 默认由 `terminalSizeProperty().get()` |
  | `terminalSizeProperty()` | 尺寸属性 | 抽象，返回 `SimpleObjectProperty<TermSize>` |
- 调用链：`TtyTermWidget.getTermSize → TtyTerminalSizeable.getTermSize`

## TtyProcessTtyConnector
- 职责：基于进程的终端连接器抽象类（继承 `ProcessTtyConnector`），提供尺寸/字符集/流访问。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `charset` | `Charset`（final） | 字符集 |
  | `terminalSizeProperty` | `SimpleObjectProperty<TermSize>` | 终端尺寸属性 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造(process,charset,commandLines)` | 构造 | |
  | `read(buf,offset,length)` | 读 | 读后 `doRead` |
  | `doRead(buf,offset,len)` | 读钩子 | 子类可覆写处理 |
  | `write(str)` / `write(bytes)` | 写 | 调试日志后 `super.write` |
  | `resize(termSize)` | 调整尺寸 | `process.setWinSize`，刷新尺寸属性 |
  | `getTermSize()` | 取尺寸 | 优先 `getWinSize`，否则尺寸属性 |
  | `terminalSizeProperty()` | 尺寸属性 | 懒建 |
  | `getWinSize()` | 进程窗尺寸 | `getProcess().getWinSize` |
  | `getProcess()` / `charset()` / `input()` / `output()` | 进程 / 字符集 / 流 | |
- 调用链：`read → super.read → doRead`；`resize → PtyProcess.setWinSize`

## TtyScrollBarUtils
- 职责：终端滚动条工具类。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `static getValueFor(lineIndex,totalLines,scrollBarMin,scrollBarMax)` | 由行索引算滚动条值 | 若 `min==0` 且 `totalLines<max` 直接返回行索引；否则按 `lineIndex/(totalLines-1)` 归一化映射到 `[min,max]` |
- 调用链：由终端滚动条组件调用

## TtyStreamConnector
- 职责：基于输入输出流的终端连接器抽象类，提供读写、尺寸与字符集通用实现。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `charset` | `Charset`（final） | 字符集 |
  | `reader` / `writer` | `InputStreamReader` / `OutputStreamWriter` | 读写器 |
  | `terminalSizeProperty` | `SimpleObjectProperty<TermSize>` | 终端尺寸属性 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造()` / `构造(charset)` | 构造 | 默认字符集为系统默认 |
  | `read(buf,offset,length)` | 读 | reader 空则 `doRead`，否则读后 `doRead` |
  | `doRead(...)` | 读钩子 | 子类可覆写 |
  | `write(bytes)` / `write(str)` | 写 | 经 `writer` 写入并 flush |
  | `waitFor()` / `ready()` | 等待 / 就绪 | 返回 0 / true |
  | `resize(termSize)` / `terminalSizeProperty()` | 尺寸 | |
  | `close()` | 关闭 | 关闭流与读写器并置空 |
  | `charset()` | 字符集 | |
- 调用链：`write(bytes) → write(str) → writer.write+flush`

## TtyTermSettingsProvider
- 职责：终端设置提供者接口，定义字号操作与退格/Alt 设置。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getIncrTermSizePresentation/getDecrTermSizePresentation/getResetTermSizePresentation()` | 字号操作展示 | 抽象 |
  | `setTerminalFontSize(size)` | 设置字号 | 抽象 |
  | `getBackspaceCode()` | 取退格码 | 默认 `{0x08}` |
  | `setBackspaceCode(code)` / `setAltSendsEscape(b)` | 设退格码 / Alt 修饰 | 默认空实现 |
- 调用链：`TtyTermWidget.initBackspaceCode → provider.setBackspaceCode`

## TtyTermWidget
- 职责：终端组件抽象类（继承 `FXJediTermWidget`），封装会话打开、尺寸获取与设置项控制。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造(provider)` | 构造 | |
  | `createTtyConnector()` | 创建连接器 | 抽象 |
  | `openSession()` | 打开会话 | `createTtyConnector` 后 `openSession(connector)` |
  | `openSession(connector)` | 打开会话 | `createTerminalSessionFX(...).start()` |
  | `getTtyConnector()` | 取连接器 | 若为 `TtyZModemTtyConnector` 返回被包裹的真实连接器 |
  | `getTermSize()` | 取尺寸 | 连接器为 `TtyTerminalSizeable` 时取尺寸 |
  | `close()` | 关闭 | `super.close` 后 `SystemUtil.gcLater` |
  | `initBackspaceCode(type)` | 初始化退格码 | provider 为 `TtyTermSettingsProvider` 时设退格码 |
  | `setAltSendsEscape(b)` | 设 Alt 修饰 | 同上委托 provider |
- 调用链：`openSession → createTtyConnector → createTerminalSessionFX → start`

## TtyTerminalCanvas
- 职责：终端渲染组件（继承 `FXPane`），内部绑定 `Canvas`。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造()` | 构造 | 建 `Canvas` 宽高绑定控件后 `addChild` |
  | `getCanvas()` | 取画布 | 首个子节点 |
  | `getGraphicsContext2D()` | 取 2D 上下文 | `canvas.getGraphicsContext2D` |
  | `destroy()` | 销毁 | 清空画布后 `NodeDestroyUtil.destroyObject` |
- 调用链：`destroy → getGraphicsContext2D.clearRect → NodeDestroyUtil`

## TtyTerminalCopyPasteHandler
- 职责：终端复制粘贴处理器（继承 `DefaultTerminalCopyPasteHandler`），复制时移除 ANSI 转义序列。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getContents(useSystemSelectionClipboardIfAvailable)` | 取剪贴板内容 | `super.getContents` 后 `SSHUtil.removeAnsi` |
- 调用链：`getContents → SSHUtil.removeAnsi`

## TtyTerminalUtil
- 职责：终端工具类，提供退格码、颜色转换与 ZModem 连接器创建。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getBackspaceCode(backspaceType)` | 取退格码 | null/1→`{0x08}`，0→`{0x7F}`，2→`"ESC[3~"` |
  | `fromFXColor(color)` | JavaFX 色转终端色 | 各通道 *255 取整 |
  | `createZModemTtyConnector(widget,connector)` | 建 ZModem 连接器 | `new TtyZModemTtyConnector(widget.getTerminal(),connector)` |
- 调用链：`TtyColorPalette.getPaletteColor → fromFXColor`

## zmodem 包

## TtyZModemInputStream
- 职责：ZModem 输入流，在底层流前追加已预读字节。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `input` | `InputStream`（final） | 底层输入流 |
  | `buffer` | `List<Byte>`（final） | 预读字节缓冲区 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造(input,buffer)` | 构造 | 预读字节入缓冲 |
  | `read()` | 读一字节 | 缓冲非空先取，否则读底层流 |
- 调用链：`TtyZModemProcessor 构造 → new TtyZModemInputStream`

## TtyZModemProcessor
- 职责：ZModem 处理器（实现 `CopyStreamListener`），接收(sz)/发送(rz)文件并刷新进度。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `sz` | `boolean`（final） | 是否接收文件 |
  | `zmodem` | `ZModem`（final） | ZModem 协议对象 |
  | `lastRefreshTime` | `long` | 上次刷新进度时间 |
  | `terminal` | `Terminal`（final） | 终端对象 |
  | `curIndex` | `int` | 当前文件索引 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造(frame,input,output,terminal)` | 构造 | 由 frame[5]==48 判断接收；包装预读流建 `ZModem` |
  | `process()` | 处理 | 接收→`receive`，否则 `send`；异常 `MessageBox.exception` |
  | `receive()` / `send()`（私有） | 接收 / 发送 | 分别弹目录/文件选择器，调用 `zmodem.receive/send` |
  | `refreshProgress(event)`（私有） | 刷新进度 | 索引变化换行；拼接索引/文件名/大小/进度；达阈值或完成/跳过时 `saveCursor→writeCharacters→restoreCursor` |
  | `openFileDialog()` / `openDirDialog()`（私有） | 文件 / 目录选择 | 经 `FileChooserHelper`/`DirChooserHelper` |
  | `bytesTransferred(event)` | 进度回调 | 事件为 `FileCopyStreamEvent` 时刷新进度 |
  | `bytesTransferred(total,bytes,size)` | 进度回调 | 空实现 |
  | `cancel()` | 取消 | `zmodem.cancel` |
- 调用链：`process → receive/send → refreshProgress → terminal.writeCharacters`

## TtyZModemTtyConnector
- 职责：ZModem 协议连接器（实现 `TtyConnector`），读取时检测 ZModem 帧并触发文件传输。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `ZMODEM_PREFIX` | `static final char[]` | ZModem 前缀（ZPAD,ZPAD,ZDLE） |
  | `terminal` | `Terminal` | 终端容器 |
  | `connector` | `TtyStreamable` | 被包装连接器 |
  | `processor` | `volatile TtyZModemProcessor` | ZModem 处理器 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造(terminal,connector)` | 构造 | |
  | `getConnector()` | 取真实连接器 | |
  | `read(buffer,offset,length)` | 读 | 先处理挂起 processor；读取后 slice 内查找前缀，命中则建 processor 并返回前缀位置 |
  | `write(bytes)` | 写 | 首字节 0x03 且在处理中则 `cancel`；否则转发 |
  | `write(string)` / `isConnected` / `waitFor` / `ready` / `getName` / `resize` | 委托 | 转发给 connector |
  | `close()` | 关闭 | 置空终端/连接器/处理器 |
  | `static indexOfZModem(a)`（私有） | 查找前缀 | 逐位比对 `ZMODEM_PREFIX`，命中返回位置 |
- 调用链：`read → indexOfZModem → new TtyZModemProcessor → process`
