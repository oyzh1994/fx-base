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

## 3. Fast-Path 加密包未使用解密结果，签名偏移也不正确

### 问题现象

本次协议审查发现一个潜在但影响很大的 Fast-Path 解析错误：

- `Rdp.rdp5_process` 在 `encryption=true` 时执行了 RC4 解密，但解密结果没有写回 `Packet`。
- 旧代码按 `shortform ? 6 : 7` 跳过签名，而 `FASTPATH_OUTPUT_ENCRYPTED` 的 `dataSignature` 固定为 8 字节。
- `RdpIsoFix` 捕获 Fast-Path 解析异常后只记录日志并继续接收，导致数据流已经错位时仍然继续解析后续包。

在标准 RDP 安全模式下，这会把密文当成位图/指针/订单数据解析，出现画面损坏、指针异常或后续 PDU 连续解析失败。当前测试连接使用 TLS，TLS 已经处理了链路加密，因此没有直接暴露成用户可见的崩溃，但底层实现仍然是错误的。

### 定位过程

1. 对照 MS-RDPBCGR 2.2.9.1.2.1 检查 Fast-Path 输出头和安全头。
2. 对比 `RdpPatch.rdp5_process` 与父类 `Rdp.rdp5_process`，确认两者对签名长度的处理不一致。
3. 检查 `Secure.decrypt`，确认它返回新的字节数组，不会原地修改传入的密文。
4. 检查 `RdpIsoFix` 的异常处理，确认 Fast-Path 解析失败被宽泛的 `catch (Exception)` 吞掉。

### 根因

`secureLayer.decrypt(data)` 是一个返回新数组的方法。父类只把返回值赋给局部变量 `packet`，既没有执行 `copyFromByteArray`，也没有调整后续读取位置。随后循环继续读取原来的密文，属于明确的协议实现错误。

同时，Fast-Path 加密数据的签名长度固定为 8 字节，旧的 6/7 字节兼容逻辑会把 RC4 解密起点提前，导致解密结果从第一个字节起就是错误的。

### 修复内容

文件：

- `fx-rdp/src/main/java/com/tangluobo/rdp4j/layers/Rdp.java`
- `fx-rdp/src/main/java/com/tangluobo/rdp4j/RdpPatch.java`
- `fx-rdp/src/main/java/com/tangluobo/rdp4j/RdpIsoFix.java`

修复点：

- 加密前校验至少还有 8 字节签名。
- 固定跳过 8 字节 `dataSignature`。
- 解密后检查返回数组非空、长度与密文一致，并写回原 `Packet`。
- 父类和 `RdpPatch` 使用相同逻辑，避免其他调用方走父类时继续解析密文。
- `RdpIsoFix` 不再吞掉 Fast-Path 解析异常；`RdesktopException` 直接向上抛出，运行时越界/畸形数据转换为 `RdesktopException`，避免流边界错位后继续消费数据。

## 4. TPKT 16 位长度被符号扩展，且接收长度没有上限

### 问题现象

`Packet.getBigEndian16()` 原实现返回 `ByteBuffer.getShort()`，Java 会把 `0x8000..0xffff` 变成负数。TPKT 长度字段是无符号 16 位，因此长度大于等于 32768 的包会被解析成负数，随后可能执行 `new byte[length]` 并抛出 `NegativeArraySizeException`。

另外，`Transport.receivePacket` 直接按传入长度分配数组，没有上限校验；TPKT 长度字段本身最多 16 位，超过 `0xffff` 或小于 0 的长度都应当直接拒绝。

### 定位过程

1. 检查 `Packet` 的 16 位读写方法，发现大端 16 位字段被按有符号 `short` 返回。
2. 检查 `ISO.receiveMessageex` 和 `RdpIsoFix.RdpIso.receive` 的 TPKT 解析，确认长度没有 `>= 4` 校验。
3. 检查 `Transport.receivePacket`，确认没有 `length < 0` 和最大包长校验。

### 根因

RDP 的 TPKT/MCS/位图字段大多是无符号 16 位，但底层读取接口把 `short` 直接提升为 `int`，丢失了无符号语义。上层又直接使用该长度分配数组，形成远端可触发的负长度/异常分配路径。

### 修复内容

文件：

- `fx-rdp/src/main/java/com/tangluobo/rdp4j/Packet.java`
- `fx-rdp/src/main/java/com/tangluobo/rdp4j/layers/Transport.java`
- `fx-rdp/src/main/java/com/tangluobo/rdp4j/layers/ISO.java`
- `fx-rdp/src/main/java/com/tangluobo/rdp4j/RdpIsoFix.java`

修复点：

- `getBigEndian16()` 和 `getBigEndian16(int)` 统一使用 `& 0xffff`，恢复无符号语义。
- `Transport` 增加 `MAX_PACKET_SIZE = 0xffff`，拒绝负长度以及“已缓存头 + 新负载”超过 16 位长度的包。
- `ISO` 和 `RdpIsoFix` 在读取 TPKT/Fast-Path 头后校验 `length >= 4`，避免 `length - 4` 为负。
- `getLittleEndian16()` 暂不全局改为无符号，因为订单坐标等字段需要保留有符号语义；涉及长度的读取点在 `Rdp`/`Orders` 中显式使用 `& 0xffff`。

## 5. 虚拟通道分片重组可被无限放大，且跨通道会互相覆盖

### 问题现象

`FixedVChannels` 已经修复了旧库首次分片对 `null` 缓冲做拼接导致的 `NullPointerException`，但审查仍然发现三个问题：

1. 分片数据无限拼接到内存，没有总长度上限。
2. 只保留一个全局缓冲；当不同虚拟通道的分片交错到达时，后一个通道的 `FIRST` 会覆盖前一个通道尚未完成的消息。
3. 没有 `FIRST` 的中间/最后分片会被当成新消息起点拼接，损坏消息仍会送到上层解析。

### 定位过程

1. 逐行检查 `channel_process` 的 `FIRST/LAST` 状态机。
2. 检查 `CHANNEL_PDU_HEADER` 的 length/flags 读取，确认 chunk 头没有最小长度校验。
3. 对照虚拟通道协议，确认不同静态虚拟通道允许出现独立的分片消息，不能共用一个重组缓冲。

### 根因

重组状态设计成了全局单缓冲，且把“收到任意非完整分片”当作重新开始。协议字段又完全来自服务端，攻击者或故障服务端可以持续发送中间分片，使 `fragmentBuffer` 无限增长。

### 修复内容

文件：`fx-rdp/src/main/java/com/tangluobo/rdp4j/FixedVChannels.java`

修复点：

- 使用 `Map<Integer, byte[]>` 按 MCS 通道保存独立重组状态。
- chunk 头不足 8 字节时丢弃并记录警告。
- 对 `CHANNEL_PDU_HEADER.length` 和实际拼接长度都设置 64MB 上限。
- `FIRST` 总是重置该通道缓冲；非 `FIRST` 且没有同通道进行中消息时直接丢弃。
- 收到 `LAST` 后先移除状态，再构造完整 `Packet` 交给通道处理。

## 6. CLIPRDR 消息长度未校验，文件内容请求可放大内存

### 问题现象

`FixedClipChannel.process` 直接使用服务端提供的 `dataLen` 计算数组长度和复制偏移。`FILECONTENTS_RESPONSE` 中还会执行 `new byte[dataLen - 4]`。当 `dataLen` 大于实际剩余数据时，会触发 `ArrayIndexOutOfBoundsException`；当数值异常大时，会在校验前尝试分配大数组。

另外，服务端可以发送 `FILECONTENTS_REQUEST.cbRequested` 接近 4GB；原实现直接 `new byte[cbRequested]` 读取本地文件，远端即可要求客户端分配超大内存。

### 定位过程

1. 检查 `CLIPRDR_HEADER` 的 `dataLen` 与实际 `packet.getEnd() - payloadStart` 的关系。
2. 检查 `handleFileContentsResponse` 的 `dataLen - 4` 和 `lastPacketStart + 12` 复制逻辑。
3. 检查 `sendFileContentsRange` 的 `new byte[cbRequested]`。

### 根因

CLIPRDR 的 `dataLen`、文件偏移和请求长度都来自远端，但本地实现把它们当作可信值，先分配后复制，缺少“声明长度必须不超过当前消息剩余长度”的约束。

### 修复内容

文件：`fx-rdp/src/main/java/com/tangluobo/rdp4j/clipboard/FixedClipChannel.java`

修复点：

- 在 `process` 中先校验完整 8 字节 CLIPRDR 头。
- 校验 `dataLen >= 0` 且 `dataLen <= packet.getEnd() - (lastPacketStart + 8)`，非法消息直接丢弃。
- 文件内容读取增加 `offset >= 0` 校验。
- 单次 `FILECONTENTS_RESPONSE` 数据块限制为 1MB；更大的请求返回一个合法块，由服务端继续请求后续区间，避免 `new byte[cbRequested]` 直接按远端请求分配。

## 7. 位图、调色板、订单和缓存索引缺少边界校验

### 问题现象

审查发现多处协议字段直接用于数组分配或索引：

- `processBitmapUpdates` 直接使用服务端位图宽高分配 `width * height * bytesPerPixel`，可能整数溢出或 OOM。
- 压缩位图的 `size`、未压缩数据的剩余长度没有在使用前校验。
- `processPalette` 的颜色数没有限制为 256，也没有校验剩余颜色数据。
- `Orders.processColorCache`、`processFontCache` 没有校验颜色数、字形尺寸和实际剩余数据。
- `Cache` 的 bitmap/cursor/font/text 索引只检查上界，负数索引仍会触发数组越界。
- 远程光标宽高来自服务端，可能构造超大 `int[nWidth * nHeight]`。

### 定位过程

1. 搜索所有由服务端字段驱动的 `new byte[]`、`new int[]` 和 `copyToByteArray`。
2. 检查 `Bitmap.decompress`、`decompressInt`、`decompressImg` 三个入口，确认它们在分配前没有统一参数校验。
3. 检查 `Cache` 的 get/put 方法，确认只做了 `< length` 判断，没有 `>= 0`。
4. 检查 `RdesktopCanvas.createCursor`，确认没有光标像素数量上限。

### 根因

协议解析层默认服务端总是发送合法数据，缺少“长度、尺寸、索引必须落在当前包和本地资源上限内”的统一防线。

### 修复内容

文件：

- `fx-rdp/src/main/java/com/tangluobo/rdp4j/layers/Rdp.java`
- `fx-rdp/src/main/java/com/tangluobo/rdp4j/graphics/Bitmap.java`
- `fx-rdp/src/main/java/com/tangluobo/rdp4j/Orders.java`
- `fx-rdp/src/main/java/com/tangluobo/rdp4j/Cache.java`
- `fx-rdp/src/main/java/com/tangluobo/rdp4j/graphics/RdesktopCanvas.java`

修复点：

- `processBitmapUpdates`：校验 2 字节更新数、每条更新 18 字节头、宽高非零、目标矩形合法、色深属于 8/15/16/24/32；解码后像素总字节限制为 160MB；未压缩和压缩数据都校验剩余长度。
- `Bitmap`：三个解压入口统一校验宽高、像素数、Bpp 和压缩数据长度；单张位图最多 4000 万像素（覆盖 8K）。
- `processPalette`：颜色数限制为 256，并校验 `n_colors * 3` 不超过剩余字节。
- `Orders`：校验订单 PDU 边界、color cache 最大 256 色、font glyph 头和字形数据长度、secondary order 长度。
- `Cache`：所有缓存索引增加负数检查；桌面缓存用 `long` 计算范围并校验源数组；`saveState` 跳过空 bitmap，避免断开连接时 NPE。
- `RdesktopCanvas.createCursor`：光标尺寸必须为正，像素数不超过 1M。

## 8. Bitmap Cache2 未压缩订单从错误偏移读取像素

### 问题现象

`Orders.process_bmpcache2` 的未压缩分支中，源偏移写成：

```java
data.copyToByteArray(bmpdata, y * rowBytes, (height - y - 1) * rowBytes, rowBytes);
```

`copyToByteArray` 的第三个参数是相对于 `Packet` 底层的绝对偏移，不是当前订单数据中的行偏移。因此 `height-y-1` 会从 PDU 头、其他订单字段甚至包外读取，导致未压缩 bitmap cache 订单画面错误或越界。

另外，带 `PERSIST` 标志的 bitmap_id 被读取后没有推进读取位置，后续宽高会从 bitmap_id 中解析。

### 定位过程

1. 对照 `processBitmapUpdates` 的逐行读取方式，确认位图数据应从当前 `data.getPosition()` 连续读取。
2. 检查 `Packet.copyToByteArray` 的语义，确认第三个参数是 packet 绝对偏移。
3. 检查 `PERSIST` 分支，确认 `copyToByteArray` 不会自动移动 position。

### 修复内容

文件：`fx-rdp/src/main/java/com/tangluobo/rdp4j/Orders.java`

修复点：

- 未压缩 bitmap cache2 严格从当前 position 逐行读取，并写入自底向上的目标位置。
- 读取前校验 `rowBytes * height` 不超过剩余数据。
- `PERSIST` 的 8 字节 bitmap_id 读取后显式 `incrementPosition(8)`。

## 9. 152 Linux 主机 TLS 间歇失败被错误降级为 STANDARD

### 问题现象

连接 `192.168.22.152`（GNOME Remote Desktop，强制 NLA）时偶发失败：

```text
Starting Bouncy Castle TLS handshake...
SSL协商失败，尝试回退到Standard RDP Security（无TLS）重连...
Standard RDP Security重连也失败: HYBRID_REQUIRED_BY_SERVER
```

同一个客户端随后再次连接又能正常完成 TLS + CredSSP + 桌面。失败时 TLS 握手在 1ms 内结束，且没有收到服务器证书，说明是服务端/网络瞬态重置，而不是证书或 TLS 算法不兼容。

### 根因

两个问题叠加：

1. `RdpClient` 把所有 `RDP "SSL negotiation failed"` 都解释为“服务器不支持 TLS”，并回退到 STANDARD。152 的服务端只接受 Enhanced RDP Security + CredSSP，因此回退必然得到 `HYBRID_REQUIRED_BY_SERVER`，把一次可重试的瞬态失败变成了最终失败。
2. `DefaultIO` 的 TCP `connect()` 没有超时，`disconnect()` 在尚未 connected 时又直接返回。关闭页签时如果连接仍卡在 TCP connect / TLS handshake，旧线程不会被取消，可能与下一次连接同时争抢同一个 Linux RDP 会话，增加服务端重置新握手概率。

### 修复内容

文件：`fx-rdp/src/main/java/com/tangluobo/rdp4j/RdpClient.java`

- TLS 握手失败时按已协商的安全类型重建全新连接重试，HYBRID 场景不再降级 STANDARD。
- HYBRID 重试默认 2 次，间隔 500ms/1000ms，可通过 `-Drdp.tls.retryLimit` 和 `-Drdp.tls.retryDelayMillis` 调整。
- STANDARD 回退路径如果收到 `HYBRID_REQUIRED_BY_SERVER`，重新进入 HYBRID 流程。
- 跟踪正在建立的 `IO`；`disconnect()` 会关闭它并设置取消标记，使阻塞中的 TCP connect/TLS handshake 尽快退出，重试循环也不会在用户关闭页签后继续建立新连接。

文件：`fx-rdp/src/main/java/com/tangluobo/rdp4j/io/DefaultIO.java`

- TCP connect 增加默认 10 秒超时，可通过 `-Drdp.connect.timeoutMillis` 调整。
- `closeIO()` 可在连接过程中从其他线程关闭候选 socket。
- IO 关闭后不允许再次隐式重连。

### 验证

新增测试：

- `RdpHostTest`：JavaFX 桌面测试，默认 152，可用 `-Drdp.host=192.168.22.157` 切换。
- `RdpConnectDiagnostics`：无头连续连接/断开诊断，输出每轮就绪与失败原因。
- `RdpCancelDiagnostics`：验证连接过程中 `disconnect()` 能取消旧线程。
- `RdpTlsRetryDiagnostics`：本地假 RDP 服务端稳定触发 TLS 握手失败，确认客户端只用 HYBRID 重试、不再降级 STANDARD。

实际主机回归：

- 修复前基线：152 连续 20 轮零间隔重连 `ready=20/20`，说明正常路径本身可用，故障为间歇性。
- 152 修复后连续 20 轮零间隔重连：`ready=20/20`。
- 157 连续 3 轮：`ready=3/3`。
- TLS 假服务端：`tls retry connections=4, expected=4`，日志中无 STANDARD 重连。

## 10. 152 桌面就绪后立即断开（0x0001）

### 问题现象

152 已完成 TLS、CredSSP 和 RDP Demand Active，随后立即收到 `Deactivate All`：

```text
RDP桌面就绪，触发onConnected回调, rdp5=true
Got deactivate all.
Re-negotiate capabilities
服务器发送错误PDU! lastReason=0x1
RdesktopDisconnectException: Server initiated disconnect
```

服务端 `gnome-remote-desktop` 日志同时出现：

```text
input_recv_sync_event: State CONNECTION_STATE_ACTIVE (or higher) requested,
  but CONNECTION_STATE_FINALIZATION_SYNC found. Aborting.
peer_unexpected_client_message: Unexpected client message in state
  CONNECTION_STATE_FINALIZATION_SYNC, missing flag
  FINALIZE_CS_SYNCHRONIZE_PDU [0x00000010]
Failed to start remote desktop session:
  GDBus.Error:org.freedesktop.DBus.Error.Failed: Session creation inhibited
ERRINFO_RPC_INITIATED_DISCONNECT [0x00010001]
```

### 根因

有两个独立原因：

1. `sendConfirmActive()` 在 Confirm Active 后立刻调用 `context.ready(INPUT)`。JavaFX 输入层随即发送 fast-path `TS_SYNC_EVENT`，但服务端仍处于 `CONNECTION_STATE_FINALIZATION_SYNC`，尚未完成 Synchronize / Cooperate / Granted Control / Font Map 四步，于是把该输入视为 unexpected client message。
2. 152 的 GNOME 本地图形会话处于锁屏状态。`ScreenSaver.GetActive=true` 时，Mutter/GNOME Remote Desktop 会拒绝创建远程桌面会话并返回 `Session creation inhibited`。RDP 登录凭据不能绕过这个本地会话保护。

### 修复内容

文件：`fx-rdp/src/main/java/com/tangluobo/rdp4j/layers/Rdp.java`

- 将 `inputEnabled=true` 和 `context.ready(ReadyType.INPUT)` 从 Confirm Active 之后移动到 finalization 四步完成之后。
- finalization 期间不再允许鼠标/键盘 PDU 写入，避免 `TS_SYNC_EVENT` 抢在 Client Synchronize 前面。

文件：`fx-rdp/src/test/java/cn/oyzh/fx/rdp/test/rdp4j/RdpConnectDiagnostics.java`

- 新增 `-Drdp.simulateInput=true`，无头诊断也会在 INPUT ready 时发送锁键同步事件，覆盖 JavaFX 的真实关键路径。
- 只有连接在 hold 时段内保持存活才算 `ready`，不再把“刚回调 connected 就被服务端断开”误计为成功。

文件：`fx-rdp/src/main/java/com/tangluobo/rdp4j/RdpClient.java`

- `0x0001` 断开原因明确提示 GNOME 锁屏/已有会话导致的 `Session creation inhibited`，并给出解锁命令。

### 验证

- 152 在 `ScreenSaver.GetActive=false` 时，带模拟 input 连续 20 轮零间隔重连：`ready=20/20`。
- 157 Windows，带模拟 input 连续 3 轮：`ready=3/3`。
- 152 锁屏时：`ready=0/1`，在 hold 阶段正确识别为断开并输出 `Session creation inhibited` 提示。
- 修复后服务端日志不再出现 `input_recv_sync_event` / `CONNECTION_STATE_FINALIZATION_SYNC` 警告。

锁屏时需在 152 上先解锁再连接：

```bash
loginctl unlock-session <会话ID>
```

如需长期避免自动锁屏，应由主机使用者决定是否关闭 GNOME 自动锁屏；客户端不会尝试绕过服务端的会话保护。

## 审查结论

本次共修复了 8 类高置信度协议问题，重点集中在：

- Fast-Path 加密和异常传播。
- TPKT 无符号长度与包长上限。
- 虚拟通道分片重组。
- CLIPRDR 文件传输内存放大。
- 位图、调色板、订单、缓存和光标的远端字段边界。
- Bitmap Cache2 的偏移解析错误。

修改遵循“远端字段先校验、再分配、再使用”的原则。没有修改 TLS 的宽松证书策略，也没有调整服务端能力协商逻辑。

## 修改文件汇总

| 问题 | 文件 | 修改点 |
| --- | --- | --- |
| RDPGFX 缓存失效 | `fx-rdp/src/main/java/com/tangluobo/rdp4j/DrdynvcChannel.java` | `RESET_GRAPHICS` 时不再清空 `bitmapCache` |
| I-beam 光标不可见 | `fx-rdp/src/main/java/com/tangluobo/rdp4j/graphics/RdesktopCanvas.java` | 反色光标返回带不透明 alpha 的黑/白 |
| Fast-Path 解密结果丢失 | `fx-rdp/src/main/java/com/tangluobo/rdp4j/layers/Rdp.java`、`RdpPatch.java`、`RdpIsoFix.java` | 固定 8 字节签名、解密回写、异常向上传播 |
| TPKT 长度错误 | `Packet.java`、`layers/Transport.java`、`layers/ISO.java` | 大端 16 位无符号读取、包长 0xffff 上限、长度至少 4 |
| 虚拟通道重组 | `FixedVChannels.java` | 每通道独立缓冲、64MB 上限、孤立分片丢弃、chunk 头校验 |
| CLIPRDR 长度/内存放大 | `clipboard/FixedClipChannel.java` | `dataLen` 与剩余字节校验、单次文件块限制 1MB |
| 位图/调色板/订单/缓存边界 | `layers/Rdp.java`、`graphics/Bitmap.java`、`Orders.java`、`Cache.java`、`graphics/RdesktopCanvas.java` | 尺寸、长度、色数、索引、光标和字形边界校验 |
| Bitmap Cache2 偏移错误 | `Orders.java` | 未压缩行数据从当前 position 读取、PERSIST 字段正确跳过 |
| TLS 瞬态失败错误降级 | `RdpClient.java`、`io/DefaultIO.java` | HYBRID 原位重试、连接超时、连接中可取消、关闭后禁止隐式重连 |
| finalization 期间提前发送输入 | `layers/Rdp.java` | INPUT ready 推迟到服务端四步 finalization 完成之后 |

## 验证命令

编译 `fx-rdp`：

```bash
mvn -o -q -pl fx-rdp compile -DskipTests
```

安装到本地仓库并编译测试端：

```bash
mvn -o -q -pl fx-rdp install -DskipTests
cd /Users/oyzh/IdeaProjects/oyzh/easyshell
mvn -o -q test-compile -DskipTests
```

运行 `RdpTest1`：

```bash
mvn -o -q -Dexec.mainClass=cn.oyzh.easyshell.test.rdp.RdpTest1 exec:java
```

`fx-rdp` 测试入口（在 IDE 中直接运行 main，或加入模块测试类路径后运行）：

- `cn.oyzh.fx.rdp.test.rdp4j.RdpHostTest`：JavaFX 桌面，`-Drdp.host=192.168.22.152|192.168.22.157`。
- `cn.oyzh.fx.rdp.test.rdp4j.RdpConnectDiagnostics`：连续连接，`-Drdp.host`、`-Drdp.attempts`、`-Drdp.holdMillis`、`-Drdp.gapMillis`；加 `-Drdp.simulateInput=true` 可覆盖真实输入路径。
- `cn.oyzh.fx.rdp.test.rdp4j.RdpCancelDiagnostics`：连接中取消，`-Drdp.cancelAfterMillis=1`。
- `cn.oyzh.fx.rdp.test.rdp4j.RdpTlsRetryDiagnostics`：本地假服务端验证 TLS 重试，无需真实主机。

本次验证结果：

- `fx-rdp` 编译通过。
- `fx-rdp` 本地安装通过。
- `easyshell` 测试代码编译通过。
- `RdpTest1` 能完成 TCP、TLS、CredSSP 协商的启动阶段，但当前测试类中用户名/密码为空，服务端在 NLA 阶段返回 `HResultException [0x6d:109]`，未进入桌面和图形解析阶段。该失败发生在本次修改涉及的 Fast-Path/图形解析之前。
- 在提供有效凭据的 IDE 运行配置中，仍需按原回归步骤验证：箭头光标、I-beam 光标、桌面持续刷新、剪贴板文本和大小文件复制。
