# fx-rdp 问题修复记录

本文记录 `fx-rdp` 模块中已经定位并修复的 RDP 兼容性问题，包含问题现象、定位过程、根因、修改内容、验证方式和回归注意事项。

记录日期：2026-10-21

验证客户端：`easyshell` 的 `RdpTest1`，使用 Java 27、JavaFX 27。

## 1. RESET_GRAPHICS 清空位图缓存导致连接断开

### 问题现象

连接 RDP 主机并完成登录后，服务端开始通过 RDPGFX 动态通道发送图形命令。客户端随后抛出异常并主动断开连接：

```text
RdesktopException: RDPGFX引用了不存在的位图缓存: 2
    at com.tangluobo.rdp4j.DrdynvcChannel.processCacheToSurface(DrdynvcChannel.java:593)
    at com.tangluobo.rdp4j.DrdynvcChannel.processGfxPdu(DrdynvcChannel.java:474)
    at com.tangluobo.rdp4j.DrdynvcChannel.dispatchDynamicData(DrdynvcChannel.java:308)
    at com.tangluobo.rdp4j.DrdynvcChannel.processData(DrdynvcChannel.java:218)
    at com.tangluobo.rdp4j.DrdynvcChannel.process(DrdynvcChannel.java:125)
```

从日志顺序看，异常发生前服务端刚刚发送了 `RESET_GRAPHICS`，并重新创建和映射了输出图面：

```text
rdpgfx: 重置桌面=1920x1080
rdpgfx: 创建图面 id=0, size=1920x1080
rdpgfx: 映射图面 id=0 -> 0,0
RDP异常: RDPGFX引用了不存在的位图缓存: 2
```

### 定位过程

1. 使用 `RdpTest1` 连接实际 RDP 主机，确认问题可以稳定复现。
2. 检查异常栈，确认失败点是 RDPGFX 的 `CACHE_TO_SURFACE` 命令，不是连接、认证或图面创建失败。
3. 查看 `DrdynvcChannel.processResetGraphics`，发现处理 `RESET_GRAPHICS` 时会同时清空 `surfaces`、`bitmapCache` 和 `dirtySurfaces`。
4. 对比日志顺序后确认：服务端在 `RESET_GRAPHICS` 后继续引用位图缓存槽 `2`，但客户端已经把该缓存删除，因此将其判定为“不存在的位图缓存”。
5. 保留 `surfaces` 和 `dirtySurfaces` 的清理，取消 `bitmapCache.clear()` 后重新连接验证，异常消失。

### 根因

`RESET_GRAPHICS` 用于重建 RDPGFX 的输出图面及相关显示状态，并不意味着服务端同时废弃了位图缓存。部分服务端实现（本次为 GNOME Remote Desktop）会在重置图形后继续复用重置前已经建立的缓存槽。

原实现将 `bitmapCache` 与输出图面一起清空，导致客户端和服务端对缓存生命周期的理解不一致。服务端认为缓存槽仍然有效，客户端却已经删除对应位图，最终在 `CACHE_TO_SURFACE` 阶段抛出异常并断开连接。

### 修复内容

文件：`fx-rdp/src/main/java/com/tangluobo/rdp4j/DrdynvcChannel.java`

方法：`processResetGraphics`

处理 `RESET_GRAPHICS` 时不再清空 `bitmapCache`：

```java
surfaces.clear();
// RESET_GRAPHICS only recreates the output surfaces. The server keeps
// bitmap cache entries across a reset and can reference them again
// (observed with GNOME Remote Desktop after a display reset).
// bitmapCache.clear();
dirtySurfaces.clear();
clearCodecDecoder.resetSequence();
```

以下状态仍然会在重置时清理或复位，避免继续使用已经失效的图面：

- `surfaces.clear()`：移除旧的输出图面。
- `dirtySurfaces.clear()`：移除旧的脏区域状态。
- `clearCodecDecoder.resetSequence()`：复位 ClearCodec 的解码序列。
- 桌面尺寸变化时继续执行 `backingStoreResize`。

位图缓存仍可通过服务端发送的 `EVICT_CACHE_ENTRY` 命令正常淘汰，不会因为本次修改而失去缓存回收能力。

### 验证结果

- `fx-rdp` 编译通过。
- 使用 `RdpTest1` 连接实际主机，登录和桌面显示正常。
- 连续运行约 20 秒，未再出现“RDPGFX引用了不存在的位图缓存”。
- 图形重置、图面创建和图面映射均可正常完成，连接不再因该异常断开。

### 回归注意事项

- 不应在 `RESET_GRAPHICS` 中清空位图缓存。
- 如果后续需要调整缓存策略，应区分“输出图面状态”和“RDPGFX 位图缓存状态”的生命周期。
- 服务端显式淘汰缓存时，仍应通过 `EVICT_CACHE_ENTRY` 删除对应缓存项。

## 2. 反色 I-beam 光标透明导致编辑器光标不可见

### 问题现象

远程桌面中的 IDE 为活动窗口时：

- 鼠标移动到左侧项目树，箭头光标正常显示。
- 鼠标移动到右侧编辑器，I-beam（“工”形文本光标）完全不可见。
- 同一个远程主机使用 Windows App 连接时，左侧箭头和右侧 I-beam 均正常显示。

这说明问题不在服务端光标资源本身，而在客户端对 24bpp 反色光标的解码和透明度处理。

### 定位过程

1. 通过 `RdpTest1` 连接主机，确认问题仅在远程编辑器区域出现。
2. 检查 MS-RDPEMSC 动态光标通道日志，确认服务端分别下发了两种缓存光标：
   - 箭头：`32x32 @ 32bpp`
   - I-beam：`32x32 @ 24bpp`，热点为 `8,9`
3. 临时转储 I-beam 的 AND/XOR 掩码并统计解码结果：
   - AND 掩码全部为 `0xFF`。
   - XOR 掩码绝大部分为黑色，只有少量像素为白色。
   - 解码后的不透明像素为 `0/1024`，整张光标实际完全透明。
4. 对照箭头光标，箭头包含有效颜色或透明度信息，因此不依赖反色路径，显示正常。
5. 检查 `RdesktopCanvas.getInvertedColor()`，发现反色像素返回的是 `0xFFFFFF` 或 `0`，没有 alpha 通道。
6. 将反色结果改为不透明黑/白后，I-beam 被识别为有效自定义光标，日志从隐藏状态变为 `custom-32x32`，界面显示恢复正常。

### 根因

Windows 的部分光标（尤其是文本 I-beam）使用“反色光标”编码：

- AND 掩码用于描述透明区域。
- XOR 掩码中的白色像素表示需要按目标背景反色，而不是普通白色。

客户端内部光标像素使用 `0xAARRGGBB` 格式。原实现的反色颜色为：

```java
return ((x + y) & 1) != 0 ? 0xFFFFFF : 0;
```

这两个值的 alpha 都是 `0x00`。当它们被写入光标像素后，本应参与反色显示的像素全部变成透明像素。I-beam 的 XOR 图形只由这些反色像素组成，因此最终整张光标不可见。

箭头光标使用 32bpp 数据和有效 alpha/掩码，不经过相同的反色透明路径，所以左侧箭头一直正常。

### 修复内容

文件：`fx-rdp/src/main/java/com/tangluobo/rdp4j/graphics/RdesktopCanvas.java`

方法：`getInvertedColor`

将反色颜色改为带完整 alpha 的不透明黑/白：

```java
private int getInvertedColor(int x, int y) {
    return ((x + y) & 1) != 0 ? 0xFFFFFFFF : 0xFF000000;
}
```

修改后：

- `0xFFFFFFFF` 表示不透明白色反色像素。
- `0xFF000000` 表示不透明黑色反色像素。
- 原有的棋盘格交替方式保持不变。
- 1bpp 光标分支仍通过 `getInvertedColor(x, y) & 0x01` 取值，不受 alpha 修改影响。

### 验证结果

- `fx-rdp` 编译通过。
- 使用 `RdpTest1` 连接实际主机，远程 IDE 正常显示。
- 左侧项目树区域：箭头光标正常显示。
- 右侧编辑器区域：I-beam 光标正常显示。
- 日志确认 24bpp I-beam 被设置为 `custom-32x32` 自定义光标。
- 验证过程中未出现光标解码异常或 RDPGFX 异常。

### 回归注意事项

- 24bpp/32bpp 光标像素必须始终使用 `0xAARRGGBB` 表示，不能返回缺少 alpha 的 RGB 值。
- 反色光标的黑/白像素必须是不透明颜色，透明语义应由 AND 掩码决定。
- 后续如果增加新的光标色深或光标编码，应同时验证普通箭头、反色 I-beam、透明光标和系统隐藏光标。

## 修改文件汇总

| 问题 | 文件 | 修改点 |
| --- | --- | --- |
| RDPGFX 缓存失效 | `fx-rdp/src/main/java/com/tangluobo/rdp4j/DrdynvcChannel.java` | `RESET_GRAPHICS` 时不再清空 `bitmapCache` |
| I-beam 光标不可见 | `fx-rdp/src/main/java/com/tangluobo/rdp4j/graphics/RdesktopCanvas.java` | 反色光标返回带不透明 alpha 的黑/白 |

## 验证命令

编译 `fx-rdp`：

```bash
mvn -o -q -pl fx-rdp compile -DskipTests
```

运行方式：启动 `easyshell` 中的 `RdpTest1`，连接测试 RDP 主机，分别把鼠标移动到远程桌面左侧项目树和右侧编辑器区域，确认箭头光标与 I-beam 光标均正常显示；同时保持连接一段时间，确认不再出现 RDPGFX 缓存异常。
