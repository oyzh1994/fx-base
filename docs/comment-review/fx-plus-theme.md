# fx-plus 主题（theme 根包）

覆盖 `cn.oyzh.fx.plus.theme` 根包内正式代码类（跳过被整体注释掉的 `AtlantaFX`、`ThemeType`、`BlackOnWhiteTheme`、`WthiteOnBlackTheme`、`YellowOnBlackTheme`）。

---

## ThemeStyle
- 职责：主题风格统一接口，继承 atlantafx 的 `Theme`，为所有主题提供强调色/前景色/背景色、暗色判定、样式重应用与相关度计算等默认能力。
- 字段：无（定义了常量语义；`ENABLE_THEME_KEY` 位于 ThemeAdapter）
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getDesc(Locale)` | 主题描述 | 默认返回 `getName()` |
  | `isDarkMode()` | 是否暗色模式 | 默认 `false`，由实现覆盖 |
  | `getAccentColor()` / `getForegroundColor()` / `getBackgroundColor()` | 三种主题色 | 抽象方法，必须实现 |
  | `getAccentColorHex()` / `getForegroundColorHex()` / `getBackgroundColorHex()` | 颜色 16 进制 | 调用 `FXColorUtil.getColorHex(...)` |
  | `handleStyle(Parent)` | 处理样式 | `FXUtil.runWait` 内 `StyleUtil.reapplyStylesheet(node, FXStyle.FX_BASE)` 重新应用基础样式 |
  | `handleStyle(Node)` | 处理节点样式（空实现） | 保留占位 |
  | `corr(ThemeStyle)` | 计算与目标主题的相关度 | 暗色标记不一致返回 -1；否则前景×5 + 背景×3 + 强调×2，调用 `ThemeUtil.calcCorr` |
  | `getUserAgentStylesheetBSS()` | 压缩样式表 | 默认返回 `null` |
- 调用链：`ThemeAdapter.changeTheme → ThemeStyle.handleStyle → StyleUtil.reapplyStylesheet → FXStyle.FX_BASE`

---

## ThemeManager
- 职责：主题管理器，维护当前主题并全局应用到所有窗口与节点。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `defaultTheme` | `ThemeStyle` | 默认主题，取 `Themes.PRIMER_LIGHT` |
  | `currentThemeProperty` | `ReadOnlyObjectWrapper<ThemeStyle>` | 当前主题只读对象属性，静态块中执行 `clearThemeTmp()` 清理临时主题文件 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `currentThemeProperty()` | 当前主题属性 | 返回只读属性 |
  | `isDarkMode()` | 是否暗色 | `currentTheme().isDarkMode()` |
  | `currentTheme()` | 当前主题 | 为空时回退 `defaultTheme` |
  | `apply(ThemeConfig)` | 按配置应用 | 为空用默认；`config.isCustom()` 时 `Themes.CUSTOM.updateTheme(...)` 后应用；否则 `Themes.getTheme(name)` |
  | `apply(ThemeStyle)` | 应用主题 | 系统主题则 `Themes.SYSTEM.listener()` 否则 `unListener()`；设置属性；`Application.setUserAgentStylesheet(...)`；遍历 `StageManager.allWindows()` 对每个 `StageAdapter` 调用 `changeTheme` 并递归 `applyCycle` |
  | `applyCycle(EventTarget, ThemeStyle)` | 递归应用主题 | 对 Parent/Popup/Scene/Stage/Window 递归子节点，命中 `ThemeAdapter` 调用 `changeTheme` |
  | `clearThemeTmp()` | 清理临时主题 css | 删除 `SysConst.storeDir()` 下 `theme*.css` |
  | `currentAccentColor()` / `currentForegroundColor()` / `currentBackgroundColor()` / `currentUserAgentStylesheet()` | 当前主题色/样式表 | 委托当前 `ThemeStyle` |
- 调用链：`ThemeManager.apply → Themes.CUSTOM.updateTheme → ThemeUtil.updateThemeCss`；`ThemeManager.apply → StageAdapter.changeTheme → ThemeAdapter.changeTheme → ThemeStyle.handleStyle`

---

## ThemeConfig
- 职责：主题配置 DTO，保存主题名称与前景/背景/强调色，并判断是否为用户定制。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `name` | `String` | 主题名称 |
  | `fgColor` / `bgColor` / `accentColor` | `String` | 前景色/背景色/强调色（16 进制） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `isCustom()` | 是否定制 | 系统主题返回 false；三色任一为空返回 false；与 `Themes.getTheme(name)` 的默认色比较，任一不同即 true |
  | getter/setter | 属性访问 | 标准访问器 |
- 调用链：`ThemeManager.apply(ThemeConfig) → ThemeConfig.isCustom`

---

## ThemeAdapter
- 职责：主题能力适配接口，节点实现后可按需启用/禁用主题并在主题变化时重应用样式。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `ENABLE_THEME_KEY` | `String`(常量) | 属性键 `"enable:theme"` |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `disableTheme()` / `enableTheme()` | 禁用/启用主题 | 通过 `setProp`/`removeProp` 操作 `ENABLE_THEME_KEY`（继承自 `PropAdapter`） |
  | `setEnableTheme(boolean)` / `isEnableTheme()` | 读写启用标志 | 默认未设置时返回 true |
  | `changeTheme(ThemeStyle)` | 更改主题 | 启用且 style 非空时按类型分派：Canvas/Parent/Popup/Tab/StageAdapter/Stage/Node 分别取其根节点 `handleStyle` |
  | `handleStyle(...)` 私有重载 | 处理 Parent/Node/列表样式 | 委托 `style.handleStyle(...)` |
  | `applyTheme()` | 应用当前主题 | `changeTheme(ThemeManager.currentTheme())` |
- 调用链：`ThemeManager.applyCycle → ThemeAdapter.changeTheme → ThemeStyle.handleStyle`

---

## ThemeUtil
- 职责：主题工具类，颜色相关度计算、系统近似主题挑选、定制 css 生成、明暗主题互转。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `calcCorr(Color, Color)` | 两色相关度 | 转 0-255 RGB，欧几里得距离归一化，返回 `1 - distance/maxDistance`；空返回 -1 |
  | `getSystemNear()` | 接近系统的主题 | 遍历 `Themes.themes()` 取 `corr(Themes.SYSTEM)` 最大者 |
  | `updateThemeCss(ThemeStyle, fg, bg, accent)` | 生成定制样式文件 | 读取原样式表按行替换 `-color-fg-default`/`-color-bg-default`/`-color-accent-fg`，写入 `storeDir()/theme_<uuid>.css`，返回路径 |
  | `getInverseTheme(ThemeStyle)` | 取相反明暗主题 | SystemTheme 递归其 `baseTheme`；按明暗映射同系列成对主题，未知回退 PRIMER |
- 调用链：`CustomTheme.updateTheme → ThemeUtil.updateThemeCss`；`SystemTheme.updateThemeCss → ThemeUtil.getSystemNear → ThemeStyle.corr`

---

## ThemeComboBox
- 职责：主题下拉框控件，展示并可选择全部主题。
- 字段：无（继承 `FXComboBox<ThemeStyle>`）
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `select(String)` | 按名称选择 | 空选第 0 项；否则 `Themes.getTheme(name)`，异常回退第 0 项 |
  | `name()` | 当前主题名 | `getSelectedItem().getName()` |
  | `isSystem()` | 是否系统主题 | 与 `Themes.SYSTEM` 比较 |
  | `getFgColor()/getFgColorHex()/getBgColor()/getBgColorHex()/getAccentColor()/getAccentColorHex()` | 当前主题色 | 委托 `getValue()` 对应方法 |
  | `initNode()` | 初始化 | `addItems(Themes.allThemes())`，转换器用 `I18nManager.currentLocale()` 取描述 |
- 调用链：`ThemeComboBox.initNode → Themes.allThemes`；`选值 → ThemeManager.apply(ThemeConfig)`

---

## Themes
- 职责：主题注册表，集中声明全部主题实例（含系统与定制），并提供按名称查找。
- 字段：大量 `public static final` 主题常量，如 `SYSTEM`(SystemTheme)、`CUSTOM`(CustomTheme)、`DRACULA`、`NORD_DARK/LIGHT`、`PRIMER_DARK/LIGHT`、`CUPERTINO_DARK/LIGHT`、`INTELLIJ_DARK/LIGHT`、`VSCODE_DARK/LIGHT`、`CYBERPUNK_DARK/LIGHT`、`LIQUID_GLASS_DARK/LIGHT`、`ANIME_WARM_DARK/LIGHT`、`BUSINESS_DARK/LIGHT`、`ARMY_DARK/LIGHT`、`BLUE_DARK/LIGHT`、`FALL_DARK/LIGHT`、`NAVY_DARK/LIGHT`、`SPRING_DARK/LIGHT`、`SUMMER_DARK/LIGHT`、`WINTER_DARK/LIGHT`、`GITHUB_*`(6 个)、`AUTUMN`、`BLACKY`、`BROWNY`、`NEWS`、`YACHT`
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `themes()` | 内置主题列表 | 新建 `ArrayList` 并按固定顺序加入 44 个内置主题（不含 SYSTEM） |
  | `allThemes()` | 全部主题 | `themes()` 基础上追加 `SYSTEM` |
  | `getTheme(String)` | 按名称获取 | 空回 default；`switch(name.toUpperCase())` 支持空格/下划线两种写法，未命中回退 `PRIMER_LIGHT` |
- 调用链：`ThemeComboBox.initNode → Themes.allThemes`；`ThemeManager.apply → Themes.getTheme`

---
