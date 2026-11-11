# 稳定性回归问题记录

- 日期：2026-11-11
- 范围：`fx-plus` 富树节点、`fx-tty` ZModem 与终端进度显示
- 目的：记录实际故障链路、修复边界和回归验证方式，避免后续标签、终端或协议调整再次引入同类问题。

## RichTreeItemBox 节点高度漂移

### 现象

树中存在 `a`、`b`、`c` 三个节点时，点击或展开、收缩 `c`，`a`、`b` 的行高会发生轻微变化。标签颜色功能引入后，问题更明显。

### 原因

`RichTreeItemBox` 更新标签和额外信息时会动态创建或移除尾部 `HBox`。JavaFX 虚拟化树单元会根据子节点的 preferred height 重新测量整行；
尾部节点的 min/pref/max 高度没有固定，标签圆点等内容加入后会改变 preferred height，从而让其他可见行在重新布局时发生高度漂移。

### 修复边界

- 根据图标和名称内容计算一次稳定的内容高度，并固定 `RichTreeItemBox` 的 min/pref/max 高度。
- 尾部 `HBox` 使用同一高度，标签圆点和额外文字只参与水平布局，不再改变树行高度。
- 标签数量、颜色或额外文字变化时，只能改变尾部内容，不能改变树单元高度。

相关实现：`fx-plus/src/main/java/cn/oyzh/fx/gui/tree/view/RichTreeItemBox.java`。

## ZModem 传输与结束序列

### 帧头分片

`**ZDLE` 后的 6 字节帧头可能分片到达。不能依赖 `InputStream.available()` 和短超时判断帧头是否完整，否则会把协议头交给终端或漏检传输。
检测器必须在确认 ZModem 前缀后阻塞读取完整帧头，再校验 `ZRQINIT`/`ZRINIT` 并启动处理器。

### 同尺寸文件导致 BufferOverflowException

目标文件已存在且长度与远端文件相同时，旧实现返回 `ZSKIP`。GNU `sz` 随后输出的
`sz: skipped: ...` 状态文本会继续进入 ZModem 包解析器，最终在固定包缓冲区写入时出现
`java.nio.BufferOverflowException`。

接收端对同尺寸文件统一发送 `ZRPOS 0` 并覆盖写入，不再让 `ZSKIP` 状态文本混入协议流。
文件名仍需保持安全名校验和目标目录限制。

### ZFIN 与 OO

ZModem 的结束序列是 ASCII `OO`，不是数字 `00`。收发方向的握手边界不同：

- 本地接收远端 `sz`：收到 `ZFIN` 后回复一次 `ZFIN`，随后消费远端发送的 `OO`，不能再向远端回复 `Finish`。
- 本地发送给远端 `rz`：收到远端 `ZFIN` 后发送一次 `OO` 并立即结束，不等待远端再发 `OO`。

接收端多发 `Finish` 会让已经退出的远端 shell 把 `OO` 当作命令执行；发送端等待不存在的远端 `Finish` 则会阻塞传输线程。
任何修改都必须保持“一端发送 `OO`、另一端消费，且同一方向只出现一次”的约束。

相关实现：

- `fx-tty/src/main/java/cn/oyzh/fx/tty/zmodem/TtyZModemDetectingInputStream.java`
- `fx-tty/src/main/java/zmodem/xfer/zm/util/ZModemReceive.java`
- `fx-tty/src/main/java/zmodem/xfer/zm/util/ZModemSend.java`

## sz/rz 进度残留

进度文本直接覆盖提示符所在行时，旧实现只用回车返回行首，没有清除整行；当后一次进度比前一次短，行尾会残留字符。
传输结束时也没有稳定换行，shell 提示符可能从进度行中间继续输出。

修复规则：

- 每次刷新前回到行首并清除整行，再写入当前进度。
- 最后一次进度也必须刷新，不能因为“全部结束”提前返回。
- 完成后显式换行，让 shell 提示符从新行开始。
- JediTerm 使用 `carriageReturn + eraseInLine(2)`；Ghostty 使用本地 VT 序列 `\r\u001B[2K`，不能发送给远端。

相关实现：

- `fx-tty/src/main/java/cn/oyzh/fx/tty/zmodem/TtyZModemProcessor.java`
- `fx-tty/src/main/java/cn/oyzh/fx/tty/zmodem/TtyZModemTerminal.java`
- `fx-tty/src/main/java/cn/oyzh/fx/tty/TtyTerminalUtil.java`

## 回归验证

单元测试至少覆盖：

- 帧头分片仍能完整识别 ZModem。
- 同尺寸文件覆盖写入，且不产生 `ZSKIP` 文本解析。
- 接收端消费远端 `OO`，发送端发送 `OO` 后立即结束。
- 进度刷新先清行，最终刷新后换行。

建议命令：

```bash
mvn -pl fx-plus -DskipTests package
mvn -pl fx-tty -Dtest=TtyZModemTerminalTest,TtyZModemTtyConnectorTest,ZModemReceiveTest,ZModemSendTest test
mvn -pl fx-tty -Dtest='*Test,!TtyZModemSshIntegrationTest' test
```

`RichTreeItemBox` 另需执行 UI 回归：创建带名称、图标、标签和额外文字的 `a`、`b`、`c` 节点，反复点击并展开、收缩 `c`，
确认 `a`、`b` 的高度始终不变。

真实 SSH 环境另行运行集成测试，并通过系统属性传入测试主机：

```bash
mvn -pl fx-tty \
  -Dtest='TtyZModemSshIntegrationTest#jschSupportsSzAndRz+sshjSupportsSzAndRz' \
  -Dzmodem.ssh.host=HOST -Dzmodem.ssh.port=PORT \
  -Dzmodem.ssh.user=USER -Dzmodem.ssh.password=PASSWORD \
  -Dzmodem.ssh.downloadFile=/remote/path/file test
```

验证 `sz`、`rz`、空文件、同尺寸覆盖和重复上传；传输结束后的终端中不得出现独立 `OO`、`OO: command not found` 或进度行尾残字。
