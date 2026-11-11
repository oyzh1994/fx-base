# Ghostty 终端（JavaFX + FFM）

本模块在 `fx-tty` 中提供了一套与 [ghosttyfx](https://github.com/vlaaad/ghosttyfx) 等价的
Ghostty 终端实现：使用 [libghostty-vt](https://github.com/ghostty-org/ghostty) 作为终端
模拟器内核，通过 Java 25 的 **FFM（Foreign Function & Memory API）** 调用本机动态库，
并使用 JavaFX `Canvas` 完成渲染。

原有的 JediTerm 终端实现保持不变；Ghostty 终端位于
`cn.oyzh.fx.tty.ghostty` 包中，可以按需替换或并存。

## 1. 特性

- 真实 Ghostty 终端模拟：VT 转义序列、滚动缓冲、重排（reflow）、选择、搜索、超链接、
  Kitty 图形协议、OSC 8/9/22/133/1337、语义提示符（semantic prompt）、进度上报等。
- 完整 JavaFX 控件：`TerminalView` 继承 `AnchorPane`，支持字体、主题、光标闪烁、
  鼠标上报、滚轮、剪贴板、输入法、快捷键、搜索框、滚动条。
- Shell 集成：自动为 bash / zsh / fish / PowerShell / cmd 注入 Ghostty Shell Integration，
  支持提示符跳转、当前目录、标题、命令边界。
- 多平台：Linux / macOS / Windows，x86_64 与 aarch64 共 6 个平台的本机库全部内置于
  `src/main/resources/ghostty`，运行时按平台自动加载，无需额外安装。
- PTY 后端：内置基于 pty4j 的 `PtyTerminal`，并提供 `GhosttyTerminals` 一键创建本地终端。

## 2. 二进制库

### 2.1 来源

6 个平台的 `libghostty-vt` 均由 **Ghostty 源码交叉编译** 得到，而不是从 Maven / npm
下载第三方预编译产物：

| 项目 | 值 |
| --- | --- |
| Ghostty 提交 | `c3203ea4b169a18eb2ccfe92847e426d8afea858`（1.3.2-dev） |
| 编译工具链 | Zig 0.16.0 |
| 编译参数 | `-Doptimize=ReleaseFast -Dapp-runtime=none -Demit-lib-vt=true -Dvt-features=-snapshot,-color,-glyph-protocol` |
| 目标 | `x86_64/aarch64` × `macos/linux/windows` |

之所以自行编译，是因为 GhosttyFX 官方只发布了 5 个平台（缺少 **windows-aarch64**）。
Windows 目标使用 `*-windows-gnu`（Zig 自带 MinGW-w64 运行时），只依赖系统 DLL
（`KERNEL32.dll`、`ntdll.dll` 与 Universal CRT），不需要额外分发 MinGW 运行库。

### 2.2 目录结构

```
src/main/resources/ghostty/
├── LICENSE                                  # Ghostty MIT 许可证
├── NOTICE                                   # 来源、提交、编译参数说明
├── linux-x86_64/libghostty-vt-linux-x86_64.so
├── linux-aarch64/libghostty-vt-linux-aarch64.so
├── macos-x86_64/libghostty-vt-macos-x86_64.dylib
├── macos-aarch64/libghostty-vt-macos-aarch64.dylib
├── windows-x86_64/libghostty-vt-windows-x86_64.dll
├── windows-aarch64/libghostty-vt-windows-aarch64.dll
└── shell/                                   # Shell 集成脚本（zip + pwsh 脚本）
```

运行时 `NativeLibrary` 会把对应平台的本机库复制到用户缓存目录
（macOS：`~/Library/Caches/fx-tty`，Linux：`$XDG_CACHE_HOME` 或 `~/.cache`，
Windows：`%LOCALAPPDATA%`），再调用 `System.load` 加载。缓存按文件内容 SHA-256 分目录，
升级本机库后会自动使用新文件。

### 2.3 SHA-256

| 文件 | SHA-256 |
| --- | --- |
| `linux-aarch64/libghostty-vt-linux-aarch64.so` | `3b6a899362a888d73a4514e8d1b768a2b64159f3e30a8cd09023b5610114ee93` |
| `linux-x86_64/libghostty-vt-linux-x86_64.so` | `0236039786680cb97b26a982c94c5e019c00db6cd36efdee348fca915d4331e8` |
| `macos-aarch64/libghostty-vt-macos-aarch64.dylib` | `7dbf9207c5ad711720751ffa9726613c82c34b7dedf4b4ff164672b86e7848f0` |
| `macos-x86_64/libghostty-vt-macos-x86_64.dylib` | `1682ebd2bdce3f295b5428b7b6c9409f1e5991dd5d1b267740bb75aa57c19f40` |
| `windows-aarch64/libghostty-vt-windows-aarch64.dll` | `a41e822617064119d4949ecd5dd4dd2e77fdf4e04467396f3c93698efe1994bf` |
| `windows-x86_64/libghostty-vt-windows-x86_64.dll` | `79223b5d7af24e0b2962b254d8254c7676aba3f2f03168a0e9d68a50f7ee5195` |

### 2.4 平台验证情况

- **macOS aarch64**：完整测试通过（146 个 Ghostty 相关测试）。
- **Linux / Windows / x86_64**：已完成
  - 目标架构正确（`file` / PE 头校验）；
  - 动态库导出符号完整（与 jextract 绑定所需符号逐一比对，除被 `-Dvt-features`
    禁用的 24 个 color/snapshot 符号外全部存在，且 Java 层未调用这些符号）；
  - C 结构体大小/字段偏移在 6 个目标上全部一致（编译期 `_Static_assert` 校验）；
  - jextract 绑定已把 `C_LONG` 固定为 64 位布局。Ghostty API 中的 `C_LONG` 只用于
    `size_t`/`intptr_t` 等 64 位值，因此同一套绑定可同时用于 LP64（macOS/Linux）
    与 LLP64（Windows）；否则 Windows 上 `long` 为 32 位，会在类初始化时因
    `ValueLayout.OfIntImpl` 无法转换为 `ValueLayout.OfLong` 而崩溃；
  - Windows x86_64 已完成 `GhosttyTerminalsTest` 冒烟测试；
  - Windows DLL 仅依赖系统 DLL。
- **Windows aarch64** 尚未在真实 ARM64 Windows 设备上运行验证，若有环境建议补充
  冒烟测试。

## 3. Java API

### 3.1 快速开始：本地 Shell

```java
import cn.oyzh.fx.tty.ghostty.GhosttyTerminals;
import cn.oyzh.fx.tty.ghostty.TerminalView;
import javafx.scene.control.Tab;

TerminalView view = GhosttyTerminals.localShell();
tab.setContent(view);
tab.textProperty().bind(view.titleProperty());

// 关闭终端（结束 Shell 进程并释放本机资源）
view.close();
```

指定命令 / 工作目录 / 环境变量：

```java
var view = GhosttyTerminals.view(
        List.of("bash", "-l"),
        Path.of(System.getProperty("user.home")),
        System.getenv());
```

### 3.2 自定义后端

`TerminalView` 只依赖 `TerminalFactory`，PTY 后端可以是 pty4j、SSH、Telnet 或任何
自定义进程/流：

```java
var view = new TerminalView((columns, rows) -> {
    var launcher = Shell.integrate(List.of("zsh"), System.getenv());
    return new PtyTerminal(launcher.command(), cwd, launcher.environment(), columns, rows);
});
```

实现 `Terminal` 接口即可接入自己的后端：

```java
public interface Terminal extends AutoCloseable {
    InputStream output() throws Exception;             // 终端输出 -> 渲染
    OutputStream input() throws Exception;             // 用户输入 -> 后端
    void resize(int cols, int rows, int widthPx, int heightPx) throws Exception;
    void close() throws Exception;
}
```

### 3.3 常用属性

| API | 说明 |
| --- | --- |
| `titleProperty()` | 终端标题（OSC 0/2） |
| `currentDirectoryProperty()` | 当前目录（OSC 7/9/1337） |
| `terminalStateProperty()` | `Running` / `Closed` / `Failed` |
| `terminalSizeProperty()` | 当前行列数 |
| `progressProperty()` | 进度（OSC 9;4） |
| `shellStateProperty()` | Shell 集成状态 |
| `themeProperty()` / `fontProperty()` | 主题、字体 |
| `copySelection()` / `pasteClipboard()` / `selectAll()` | 剪贴板操作 |
| `sendText(String)` / `sendEsc(String)` | 主动发送输入 |
| `searchPromptTextProperty()` | 搜索框提示文字 |
| `isMouseTrackingEnabled()` | 应用是否开启鼠标上报 |

### 3.4 线程与生命周期

- `TerminalView` 必须在 **JavaFX 应用线程** 创建和访问。
- `TerminalFactory.open(...)` 在后台线程调用，可执行阻塞的进程启动。
- `TerminalView.close()` 可从 JavaFX 线程调用，实际后端清理在后台任务中完成，重复调用安全。
- `NativeLibrary` 在首次创建 `TerminalView` 时加载本机库。

### 3.5 JVM 参数

FFM 调用本机库需要开启原生访问（JDK 24+）：

```
--enable-native-access=ALL-UNNAMED
```

本模块的 Surefire 已配置该参数；应用打包时请加到启动参数中（`jpackage` 使用
`--java-options`）。

## 4. 测试

```bash
# 仅 Ghostty 相关测试（146 个）
mvn -pl fx-tty -Dtest='cn.oyzh.fx.tty.ghostty.*Test' test

# 整个 fx-tty 模块
mvn -pl fx-tty test
```

Ghostty 测试覆盖：

- `TerminalViewTest`：106 个用例，覆盖启动/关闭、渲染、输入、鼠标、选择、搜索、
  主题、字体缩放、光标、Kitty 图形、OSC、链接等。
- `InputModelTest`：键盘/鼠标编码模型。
- `ShellTest`：Shell 集成参数与资源打包。
- `TerminalThemeTest`：主题校验。
- `PtyTerminalTest`：pty4j 后端读写与 resize。
- `GhosttyTerminalsTest`：便捷工厂创建/关闭终端视图。

手动体验（需要图形环境）：

```bash
# 在 IDE 中运行 cn.oyzh.fx.tty.ghostty.GhosttyTerminalDemo
# 或使用 Maven（MAVEN_OPTS 传入 FFM 开关）
MAVEN_OPTS="--enable-native-access=ALL-UNNAMED" \
  mvn -pl fx-tty test-compile exec:java \
  -Dexec.classpathScope=test \
  -Dexec.mainClass=cn.oyzh.fx.tty.ghostty.GhosttyTerminalDemo
```

## 5. 重新编译本机库

需要一份 Ghostty 源码 checkout（提交 `c3203ea4...`）和 Zig 0.16.0。脚本会
自动下载 Zig（也可通过 `ZIG` 环境变量指定），并交叉编译 6 个目标：

```bash
GHOSTTY_SRC=/path/to/ghostty ./fx-tty/tools/build-ghostty-vt.sh
```

脚本会把产物直接复制到 `fx-tty/src/main/resources/ghostty/<platform>/`。

重新生成 FFM 绑定（需要 jextract 25）：

```bash
GHOSTTY_SRC=/path/to/ghostty \
JEXTRACT=/path/to/jextract-25/bin/jextract \
./fx-tty/tools/generate-bindings.sh
```

生成脚本会在复制绑定后把 `ghostty_vt_h$shared.java` 中的 `C_LONG` 归一化为 64 位
布局，避免重新生成后再次引入 Windows 上的 `ClassCastException`。

## 6. 与 GhosttyFX 的差异

- 包名改为 `cn.oyzh.fx.tty.ghostty`，接入 fx-base / fx-tty 的构建与 JavaFX 版本。
- 本机库从“按平台拆分的 Maven 构件 + zip”改为“模块内 6 平台资源目录”，并新增
  **windows-aarch64**。
- 新增 `PtyTerminal` 与 `GhosttyTerminals`，开箱即用地创建本地终端。
- Shell 集成资源统一放在 `/ghostty/shell`。
- 测试迁移到本模块，保留原 GhosttyFX 的完整 `TerminalViewTest`。

## 7. easyshell 双引擎接入

`fx-tty` 通过 `TtyTermWidget` 把两套终端引擎统一到同一个组件中：

```
ShellXxxTermWidget (easyshell, 继承 TtyTermWidget)
        │
        ▼
   TtyTermWidget ── 选择引擎 ──> JediTerm（原实现）
        │
        └──────────────────────> Ghostty TerminalView
                                    ▲
                        TtyConnectorTerminal
                                    ▲
                        TtyConnector（本地/SSH/Telnet/串口/Mosh/RLogin）
```

- 所有既有后端仍然实现 `TtyConnector`，不需要改造成 Ghostty 专用接口。
- `TtyConnectorTerminal` 负责把 `TtyConnector` 的字符读写转换成 Ghostty 需要的字节流：
  - 输出：`read(char[])` → Unicode → UTF-8，交给 libghostty-vt；
  - 输入：Ghostty 的 UTF-8 按键字节 → Unicode → `write(String)`，由连接器按自身字符集写回；
  - resize 转发到 `TtyConnector.resize(TermSize)`，复用 SSH/Mosh/Telnet 的远端 PTY 尺寸更新。
- ZModem 的终端进度通过 `TtyZModemTerminal` 抽象：JediTerm 和 Ghostty 各自实现
  换行、保存光标、写字符、恢复光标。Ghostty 通过 `TerminalView.writeLocalOutput`
  把进度直接写入 libghostty-vt 的终端状态，不发送给远端进程，因此两套引擎都能显示
  ZModem 传输进度。

### 7.1 选择引擎

打开会话前指定（优先级最高）：

```java
widget.setEngine(TtyTerminalEngine.GHOSTTY);
widget.openSession(connector);
```

全局默认（只影响之后打开的会话）：

```java
TtyTermWidget.setDefaultEngine(TtyTerminalEngine.GHOSTTY);
// 或 TtyTerminalEngine.setDefaultEngine(...)
```

也可以通过系统属性启动：

```bash
java -Dfx.tty.engine=ghostty --enable-native-access=ALL-UNNAMED ...
```

### 7.2 easyshell 中的使用

easyshell 在“设置 → 终端 → 终端引擎”中提供 `JediTerm / Ghostty` 选择，保存后对新打开的终端生效。
现有 FXML、控制器、连接器、分屏、标签页结构均不需要改动。

### 7.3 尺寸稳定

`TtyTermWidget` 是 `FXStackPane`，其 `layoutChildren()` 会对子节点调用 `autosize()`。
`TerminalView` 默认的 `prefWidth/prefHeight` 来自 80x24 的 Canvas，如果不接管，布局会
在“80 列宽度”和“父容器宽度”之间反复切换，导致终端行列数和 PTY resize 高频变化。
因此 Ghostty 视图的 `prefWidth/prefHeight` 会绑定到 `TtyTermWidget` 的宽高，并在关闭时解绑，
保证布局与终端尺寸稳定。

### 7.4 背景透明

Ghostty 默认会用自己的主题背景填充整个 Canvas。为了让 EasyShell 的窗口背景图透出，
`ShellWindowBackgroundManager` 会把 Ghostty 主题的默认背景 alpha 置 0，并清除宿主
`TtyTermWidget`/`TerminalView` 的底色；关闭背景图时恢复原始主题。ANSI 背景色、
选中背景和光标颜色保持不透明，行为与 JediTerm 的 `backgroundOpacity=0` 一致。

### 7.5 搜索、右键菜单与字体缩放

`TtyTermWidget` 会为 Ghostty 视图安装与 JediTerm 相同的终端动作：

- 搜索：搜索栏结构与 JediTerm 对齐（输入框、`n of N` 匹配计数、上一个/下一个、关闭按钮），
  使用 `SettingsProvider.getFindActionPresentation()` 的快捷键，右键菜单提供“查找”；
- 右键菜单：打开 URL、复制、粘贴、全选、清屏、向上/向下翻页、查找、字体增大/减小/重置；
- 字体缩放：范围与 JediTerm 一致（增大上限 30，减小下限 10），并通过
  `TtyTermSettingsProvider.setTerminalFontSize` 持久化；重置回到打开终端时的初始字号。

### 7.6 限制

- 已经打开的会话不会在两种引擎之间热切换（两种引擎的屏幕状态无法无损迁移）；
  切换设置后打开新标签页即可使用新引擎。
- ZModem 传输进度已支持两套引擎（通过 `TtyZModemTerminal` 抽象）。
- Ghostty 已接入 easyshell 终端快捷键（查找、复制、粘贴、全选、清屏、翻页、
  字体增大/减小/重置）和右键菜单，与 JediTerm 对齐；搜索框提示文字使用查找动作名称。
- 终端背景图等 JediTerm 专有视觉特性暂未映射到 Ghostty。
