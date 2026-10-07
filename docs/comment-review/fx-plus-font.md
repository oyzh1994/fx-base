# fx-plus 字体（font）

覆盖 `cn.oyzh.fx.plus.font` 包全部 8 个类。

## FontManager
- 职责：字体管理器，维护当前字体并全局应用到所有窗口节点，带字体缓存。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `defaultFont` | `static Font` | 默认字体（`Font.getDefault()`） |
  | `CACHE` | `static FontCache` | 字体缓存 |
  | `currentFont` | `static Font` | 当前字体 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `currentFont()` | 当前字体 | 为空回 default |
  | `currentFontSize()` | 当前字号 | `currentFont().getSize()` |
  | `apply(FontConfig)` | 应用配置 | 校验 family/weight/size 后 `apply(toFont(config))` |
  | `apply(Font)` | 应用字体 | `cacheFont` 后遍历 `StageManager.allStages()` 递归 `applyCycle`，记录 `currentFont` |
  | `applyCycle(EventTarget,Font)`(私有) | 递归应用 | 命中 `FontAdapter` 调 `changeFont`，并递归 Parent/Popup/Stage/Window/Scene |
  | `cacheFont(Font)` | 缓存字体 | 缓存命中返回缓存实例，否则加入 |
  | `toFont(FontConfig)` | 配置转字体 | `Font.font(family, weight, size)` 后缓存 |
- 调用链：`FontManager.apply → applyCycle → FontAdapter.changeFont`

---

## FontAdapter
- 职责：字体能力适配接口，提供字体家族/字号/粗细读写、启用开关与变更处理。
- 字段：无（prop 键 `enable:font`、`enable:font:size`、`enable:font:weight`）
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `setFont/getFont` | 字体 | 委托 `FontUtil.setFont/getFont` |
  | `setFontSize/getFontSize/setFontFamily/getFontFamily/setFontWeight(±2)/getFontWeight` | 属性 | 委托 `FontUtil` |
  | `disableFont/enableFont/setEnableFont/isEnableFont` | 字体开关 | prop `enable:font` |
  | `disableFontSize/enableFontSize/setEnableFontSize/isEnableFontSize` | 字号开关 | prop `enable:font:size` |
  | `disableFontWeight/enableFontWeight/setEnableFontWeight/isEnableFontWeight` | 粗细开关 | prop `enable:font:weight` |
  | `changeFont(Font)` | 变更字体 | 启用时按现有 style/size 保真后 `setFont` |
- 调用链：`NodeManager.init / FontManager.applyCycle → FontAdapter.changeFont → FontUtil.setFont`

---

## FontUtil
- 职责：字体工具类，创建派生字体、字重解析、字体测量与按控件类型的字体读写。
- 字段：无
- 方法（要点）：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getFamilies()` | 字体族列表 | |
  | `newFontBySize/newFontByFamily/newFontByWeight/newFont(...)` | 创建派生字体 | 基于 `getTrueFamily` + `FontManager.cacheFont` |
  | `getWeight(Font/String)/getWeight2(String)` | 字重解析 | 按 style 分词 `FontWeight.findByName` |
  | `isSameFont(Font,Font)` | 是否同字体 | 比较 family/style/size |
  | `stringWidth(...)` / `calcFontHeight(...)` / `fontMetrics(...)` | 文本测量 | 基于 AWT `FontMetrics` / `JLabel` |
  | `getFont/setFont/getFontSize/setFontSize/getFontFamily/setFontFamily/getFontWeight/setFontWeight(Object,...)` | 字体读写 | 按 Text/Labeled/TextInputControl/TabPane/Styleable 分派；Styleable 走 `StyleUtil` 内联样式 |
  | `getTrueFamily(String)` | 真实字体族 | 在系统字体族中取最长前缀匹配 |
  | `textWidth/textHeight(String[,Font])` | 文本宽高 | 用临时 `Text` 测量 |
- 调用链：`FontAdapter.setFont → FontUtil.setFont`；`Splitter.renderText → FontUtil.textWidth`

---

## FontCache
- 职责：字体缓存，用列表存储字体并按“同字体”判等查找（实现 `AutoCloseable`）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `fonts` | `List<Font>` | 字体列表（CopyOnWriteArrayList） |
- 方法：`get(Font)`、`add(Font)`、`remove(Font)`、`contains(Font)`、`close()`
- 调用链：`FontManager.cacheFont → FontCache.contains/get/add`

---

## FontConfig
- 职责：字体配置 DTO，承载字号/字体名/字重。
- 字段：`size`(`Integer`)、`family`(`String`)、`weight`(`Integer`)
- 方法：`get/setSize`、`get/setFamily`、`get/setWeight`
- 调用链：`FontManager.apply(FontConfig) → toFont`

---

## FontFamilyComboBox
- 职责：字体名称下拉框，继承 `FXComboBox<String>`。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 实例块 | 初始化 | `addItems(FontUtil.getFamilies())`、选中默认、设提示 |
  | `getDefault()` | 默认字体名 | `FontManager.defaultFont.getFamily()` |
  | `select(String)` | 选中 | 空/异常回退默认 |
- 调用链：`FontFamilyComboBox.select → FontManager.defaultFont`

---

## FontSizeComboBox
- 职责：字号下拉框，继承 `FXComboBox<Integer>`，实现 `I18nSelectAdapter<Integer>`。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `selectSize(Byte)` | 选中字号 | 空清选，否则 `selectObj` |
  | `byteValue()` | 字节字号 | |
  | `values(Locale)` | 可选字号列表 | 10~25，`setItem` |
  | `initNode()` | 初始化 | 清选、设提示 |
- 调用链：`NodeManager.init → I18nSelectAdapter.values → FontSizeComboBox.values`

---

## FontWeightComboBox
- 职责：字重下拉框，继承 `FXComboBox<FontWeight>`。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getDefault()` | 默认字重 | `FontUtil.getWeight(defaultFont.getStyle())` |
  | `select(FontWeight)` / `selectWeight(Integer)` | 选中 | 空回默认/按 weight 查找 |
  | `getWeight()` | 当前字重值 | `getSelectedItem().getWeight()` |
  | `initNode()` | 初始化 | 加 `FontWeight.values()`、设提示、转换器显示数值、选中默认 |
  | `destroy()` | 销毁 | 置空转换器后 `super.destroy` |
- 调用链：`FontWeightComboBox.initNode → FontUtil.getWeight`

---
