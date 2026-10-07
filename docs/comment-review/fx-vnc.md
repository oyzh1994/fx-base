# fx-vnc 远程桌面（`cn.oyzh.fx.vnc`）

覆盖 `cn.oyzh.fx.vnc` 共 7 个正式类。基于 TightVNC 协议库改造为 JavaFX 实现：帧缓冲视图、渲染器、软光标、鼠标/键盘事件处理与剪贴板同步。

无整文件死代码（各文件头部为源许可版权注释）。

---

## VncClipboardHandler
- 职责：VNC 剪贴板控制器（实现 `ClipboardController`、`Destroyable`），同步本地与远端剪贴板文本。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `STANDARD_CHARSET` | `static final String` | 标准字符集 `ISO-8859-1`（多字节回退用） |
  | `CLIPBOARD_UPDATE_CHECK_INTERVAL_MILLIS` | `static final long` | 轮询间隔 1000ms |
  | `clipboardText` | `String` | 上次读取到的剪贴板文本 |
  | `isRunning` | `volatile boolean` | 轮询是否运行中 |
  | `isEnabled` | `boolean` | 是否启用同步 |
  | `protocol` | `Protocol`（final） | VNC 协议对象 |
  | `charset` | `Charset` | 编解码字符集 |
  | `scheduler` | `ScheduledExecutorService` | 轮询调度线程池 |
  | `pollingTask` | `ScheduledFuture<?>` | 轮询任务句柄 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造(protocol,charsetName)` | 构造 | 解析字符集：空→默认；`standard`→ISO-8859-1；UTF 开头→ISO-8859-1 |
  | `updateSystemClipboard(bytes)` | 更新本地剪贴板 | 启用时 `ClipboardUtil.setString` |
  | `getClipboardText()` / `getRenewedClipboardText()` | 取剪贴板文本 | 后者比较是否变化 |
  | `updateSavedClipboardContent()`（私有） | 更新缓存 | 实际读取由轮询任务在 FX 线程完成 |
  | `setEnabled(enable)` | 启用/禁用 | 禁用停轮询；启用启动轮询 |
  | `startPolling()` / `stopPolling()` | 启动 / 停止轮询 | 单线程守护池，`scheduleWithFixedDelay` 定期在 FX 线程读系统剪贴板，变化则 `ClientCutTextMessage` 发远端 |
  | `settingsChanged(e)` | 设置变化 | 按 `isAllowClipboardTransfer` 启停 |
  | `destroy()` | 销毁 | `setEnabled(false)` |
- 调用链：`setEnabled(true) → startPolling → FXUtil.runLater(读剪贴板) → protocol.sendMessage(ClientCutTextMessage)`

## VncFramebufferView
- 职责：VNC 帧缓冲视图（继承 `FXPane`，实现 `IRepaintController`、`Destroyable`），渲染远端画面并处理输入。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `fbWidth` / `fbHeight` | `int` | 帧缓冲宽 / 高 |
  | `renderer` | `volatile VncRendererImpl` | 帧缓冲渲染器 |
  | `cursor` | `VncSoftCursorImpl` | 远端光标 |
  | `mouseEventHandler` / `keyEventHandler` | 事件处理器 | 鼠标 / 键盘 |
  | `showCursor` | `boolean` | 是否显示远端光标 |
  | `isUserInputEnabled` | `boolean` | 是否启用用户输入 |
  | `protocol` | `Protocol` | VNC 协议 |
  | `scaleFactor` | `double` | 缩放比例 |
  | `framebufferImageView` / `cursorImageView` | `FXImageView` | 帧缓冲 / 光标图像控件 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造()` / `构造(protocol,scaleFactor,shape)` | 构造 | 后者 `init` |
  | `init(protocol,scaleFactor,shape)` | 初始化 | 建图像控件、绑定点击聚焦，非 viewOnly 时启用输入，设置光标与尺寸 |
  | `setUserInputEnabled(enable,convertToAscii)` | 启用输入 | 按需注册/移除鼠标（PRESSED/RELEASED/DRAGGED/MOVED/Scroll）与键盘（PRESSED/RELEASED/TYPED）事件过滤器 |
  | `createRenderer(transport,w,h,pixelFormat)` | 创建渲染器 | 建 `VncRendererImpl`，FX 线程设图像与尺寸并聚焦 |
  | `repaintBitmap(rect)` / `repaintBitmap(x,y,w,h)` | 重绘位图 | 委托 `renderer.updateBuffer` |
  | `repaintCursor()` | 重绘光标 | FX 线程按光标位置/尺寸设置 `cursorImageView` |
  | `updateCursorPosition(x,y)` | 更新光标位置 | `cursor.updatePosition` 后 `repaintCursor` |
  | `settingsChanged(e)` | 设置变化 | RFB 设置→用户输入与光标显示；UI 设置→缩放/光标形状/尺寸 |
  | `setPixelFormat(pixelFormat)` | 更新像素格式 | `renderer.initColorDecoder` |
  | `setLocalCursorShape(shape)` | 本地光标形状 | 系统默认→`Cursor.DEFAULT`，否则 `Cursor.NONE` |
  | `updateImageViewSize()`（私有） | 更新尺寸 | 按帧缓冲×缩放设置图像与 Pane 尺寸 |
  | `getScaleFactor/getFbWidth/getFbHeight/getProtocol` | 读取 | |
  | `destroy()` | 销毁 | 销毁光标/渲染器/图像控件 |
- 调用链：`createRenderer → VncRendererImpl → framebufferImageView.setImage`；`repaintCursor → cursorImageView.setImage`

## VncKeyEventHandler
- 职责：VNC 键盘事件处理器，将 JavaFX 按键事件转换为 RFB `KeyEventMessage`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `protocol` | `Protocol`（final） | VNC 协议 |
  | `convertToAscii` | `boolean` | 是否转为 ASCII |
  | `convertor` | `VncKeyboardConvertor` | 键盘布局转换器 |
  | `KEYCODE_TO_KEYSYM` | `static final Map<KeyCode,Integer>` | 动作/非字符键→X11 keysym |
  | `NUMPAD_TO_KEYSYM` | `static final Map<KeyCode,Integer>` | 小键盘→keysym |
  | `KEYCODE_TO_CHAR` | `static final Map<KeyCode,Integer>` | KeyCode→美式键盘基础字符 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造(protocol)` | 构造 | |
  | `setConvertToAscii(b)` | 设置转换 | 启用时懒建 `VncKeyboardConvertor` |
  | `handleKeyPressed/handleKeyReleased(e)` | 按下 / 释放 | `processKeyEvent` 后 consume |
  | `handleKeyTyped(e)` | 字符输入 | 直接 consume |
  | `processKeyEvent(e,pressed)`（私有） | 主流程 | CapsLock 跟踪→修饰键→特殊键→动作键→字符键；控制字符加 0x60 或映射特殊键；`convertToAscii` 时用转换器；否则 `unicode2keysym`；最后 `sendKeyEvent` |
  | `keyCodeToChar(code,shiftDown)`（私有 static） | 推导字符 | 字母/数字（含 Shift 符号）/固定映射及 Shift 版本 |
  | `processModifierKeys/processSpecialKeys/processActionKey(e,pressed)`（私有） | 分类处理 | 修饰键 / AltGr 与小键盘 / 动作键，命中即 `sendKeyEvent` |
  | `sendKeyEvent(keyChar,pressed)`（私有） | 发送 | `protocol.sendMessage(new KeyEventMessage(...))` |
- 调用链：`handleKeyPressed → processKeyEvent → processModifierKeys/processSpecialKeys/processActionKey → sendKeyEvent → KeyEventMessage`

## VncKeyboardConvertor
- 职责：面向 JavaFX 的键盘字符转换器，按键盘布局转换字符（替代 AWT 扫描码方案）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `KEY_MAP` | `static final Map<Integer,CodePair>` | AWT 键码→基础/Shift 字符对 |
  | `keyboardLayout` | `String`（final） | 键盘布局语言标识 |
  | `capsLockOn` | `boolean` | 大写锁定是否开启 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造()` / `构造(locale)` | 构造 | 布局取 locale 语言小写，空为 `en` |
  | `setCapsLockOn/isCapsLockOn` | 大小写锁定 | |
  | `convert(keyChar,keyCode,shiftDown)` | 转换 | 德语布局互换 Y/Z；按 `capsLockOn` 与 Shift 选择基础/Shift 字符 |
  | `static keyCodeToAwtCode(code)` | KeyCode→AWT 键码 | 字母/数字取字符值，符号走 switch |
  | `CodePair`（私有静态类） | 字符对 | `code` 基础字符 / `codeShifted` Shift 字符 |
- 调用链：`VncKeyEventHandler.processKeyEvent → convert(keyChar,awtCode,shift) → KEY_MAP`

## VncMouseEventHandler
- 职责：VNC 鼠标事件处理器，将 JavaFX 鼠标/滚轮事件转换为 RFB `PointerEventMessage`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `BUTTON_LEFT/MIDDLE/RIGHT` | `static final byte` | 左(1)/中(2)/右(4) 键掩码 |
  | `WHEEL_UP/WHEEL_DOWN` | `static final byte` | 滚轮上(8)/下(16) 掩码 |
  | `repaintController` | `IRepaintController`（final） | 重绘控制器 |
  | `protocol` | `Protocol`（final） | VNC 协议 |
  | `scaleFactor` | `volatile double` | 缩放比例 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造(repaintController,protocol,scaleFactor)` | 构造 | |
  | `setScaleFactor(scaleFactor)` | 设缩放 | |
  | `handleMousePressed/handleMouseReleased` | 按下 / 释放 | `processMouseEvent(event,false)` |
  | `handleMouseDragged/handleMouseMoved` | 拖拽 / 移动 | `processMouseEvent(event,true)` |
  | `handleScroll(event)` | 滚轮 | `processScrollEvent` |
  | `processMouseEvent(event,moved)`（私有） | 处理 | 坐标按缩放换算；`moved` 时更新光标位置；按按键状态组合掩码后发 `PointerEventMessage` |
  | `processScrollEvent(event)`（私有） | 滚轮处理 | deltaY 换算 notch（每 notch≈40，上限 5），循环发送滚轮按下/释放消息 |
- 调用链：`handleMouseMoved → processMouseEvent → repaintController.updateCursorPosition + PointerEventMessage`

## VncRendererImpl
- 职责：渲染器的 JavaFX 实现（继承 `Renderer`），用 `PixelBuffer<IntBuffer>` 零拷贝渲染到 `WritableImage`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `offscreenImage` | `WritableImage`（final） | 离屏帧缓冲图像 |
  | `pixelBuffer` | `PixelBuffer<IntBuffer>`（final） | 像素缓冲（与 `pixels` 共享数组） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造(transport,w,h,pixelFormat)` | 构造 | `init`，以 `pixels` 包装 `IntBuffer` 建 `PixelBuffer` 与图像，创建软光标 |
  | `getOffscreenImage()` | 取图像 | |
  | `updateBuffer(rect)` / `updateBuffer()` | 标记重绘 | FX 线程 `pixelBuffer.updateBuffer` |
  | `drawJpegImage(bytes,offset,len,rect)` | 绘制 JPEG | JavaFX `Image` 解码，按预乘 ARGB 读像素，加锁拷贝到 `pixels` 指定矩形 |
  | `getCursor()` | 取软光标 | |
  | `destroy()` | 销毁 | 清缓冲并销毁图像 |
- 调用链：`VncFramebufferView.createRenderer → new VncRendererImpl → getOffscreenImage`

## VncSoftCursorImpl
- 职责：软光标的 JavaFX 实现（继承 `SoftCursor`），由光标像素生成叠加图像。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `cursorImage` | `WritableImage` | 光标图像 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `构造(hotX,hotY,width,height)` | 构造 | |
  | `getImage()` | 取图像 | |
  | `createNewCursorImage(cursorPixels,...)` | 创建光标图像 | 尺寸非法置空；否则 `PixelWriter.setPixels`（预乘 ARGB） |
  | `destroy()` | 销毁 | 销毁图像 |
- 调用链：`SoftCursor 更新 → createNewCursorImage → VncFramebufferView.repaintCursor`
