# fx-rdp 远程桌面（`cn.oyzh.fx.rdp`）

覆盖 `cn.oyzh.fx.rdp` 共 1 个正式类。基于 rdp4j 的 RDP 远程桌面视图。

无整文件死代码。

---

## RdpView
- 职责：RDP 远程桌面视图（继承 `FXPane`），在首帧到达后挂载远端桌面画面，并处理缩放与焦点。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `frontend` | `FxRdpFrontend` | RDP 前端对象 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `steup(rdpClient,frontend)` | 初始化 | 保存前端并注册 `rdpClient.setOnFirstFrame(this::attachDesktopAfterFirstFrame)` |
  | `attachDesktopAfterFirstFrame()`（私有） | 首帧处理 | 取 `frontend.getView()` 加入子节点并 `FXUtil.runLater(requestFocus)` |
  | `setScaleToFit(scaleToFit)` | 设置按比例缩放 | FX 线程设置 `FxRdpDisplay.setScaleToFit`；若视图为 `Pane` 则把其 pre 宽高绑定到本视图宽高 |
  | `requestFocus()` | 请求焦点 | `super.requestFocus` 后对 `display.getImageView()` 请求焦点 |
- 调用链：`steup → rdpClient.setOnFirstFrame → attachDesktopAfterFirstFrame → addChild(requestFocus)`
- 调用链：`setScaleToFit → FxRdpDisplay.setScaleToFit + parent.prefWidth/Height.bind`
