# fx-plus 更新日志（changelog）

覆盖 `cn.oyzh.fx.plus.changelog` 包全部 4 个类。

## Changelog
- 职责：更新日志数据模型，承载某个版本的日期、版本号与问题/优化/特性列表。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `date` | `String` | 日期 |
  | `version` | `String` | 版本 |
  | `bugfix` | `List<String>` | 问题处理列表 |
  | `optimize` | `List<String>` | 优化列表 |
  | `features` | `List<String>` | 新功能列表 |
- 方法：`get/setDate`、`get/setVersion`、`get/setBugfix`、`get/setOptimize`、`get/setFeatures`（标准访问器）
- 调用链：`ChangelogManager.load → Changelog`（JSON 反序列化）

---

## ChangelogEvent
- 职责：更新日志事件，继承通用事件 `Event<Changelog>`。
- 字段：无（继承父类 payload）
- 方法：无
- 调用链：`EventBus 发布 → ChangelogEvent`

---

## ChangelogListView
- 职责：更新日志组件，继承 `FXListView<Node>`，将 `Changelog` 渲染为标签列表。
- 字段：无（实例块设默认光标）
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `init(List<Changelog>)` | 初始化 | 空则 `clearItems`，否则逐个 `initChangelog` |
  | `initChangelog(Changelog)`(私有) | 渲染单条 | 标题用 `版本(日期)`；特性/优化/问题分别加 `success`/`accent`/`danger` 样式类逐条添加 |
  | `initTitle(String)`(私有) | 标题标签 | 字号 25、不可用字体 |
  | `initItem(String)`(私有) | 内容标签 | 字号 12、不可用字体 |
- 调用链：`ChangelogListView.init → initChangelog → addItem(FXLabel)`

---

## ChangelogManager
- 职责：更新日志管理器，从 JSON 资源加载更新日志列表。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `load()` | 加载默认 | `load("/changelog.json")` |
  | `load(String url)` | 加载指定 | `ResourceUtil.getResource` 定位，`FileUtil.readString` 读取，`JSONUtil.toList(json, Changelog.class)` |
- 调用链：`ChangelogManager.load → JSONUtil.toList → List<Changelog>`

---
