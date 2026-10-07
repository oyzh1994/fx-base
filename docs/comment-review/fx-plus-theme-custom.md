# fx-plus 定制主题（theme/custom）

覆盖 `cn.oyzh.fx.plus.theme.custom` 包全部 14 个类。其中 12 个为同构的纯色主题（实现 `ThemeStyle`，返回固定名称/样式表与三种颜色）；`CustomTheme`、`SystemTheme` 有动态逻辑，单独详述。

## 同构纯色主题（12 个）
- 通用模式：`implements ThemeStyle`；`getName()` 返回主题名；`getUserAgentStylesheet()` 返回 `/fx-plus/css/theme/<name>.css`；`isDarkMode()` 返回固定布尔；`getAccentColor/getForegroundColor/getBackgroundColor` 返回 `Color.valueOf(hex)`。字段：无。方法：上述 6 个重写方法（关键逻辑即返回常量）。

| 类名 | 名称 | 样式表 | 暗色 | 强调色 | 前景色 | 背景色 |
|---|---|---|---|---|---|---|
| AnimeWarmDarkTheme | Anime Warm Dark | anime-warm-dark.css | 是 | #ff8848 | #f0d8cc | #1c100c |
| AnimeWarmLightTheme | Anime Warm Light | anime-warm-light.css | 否 | #d87830 | #382c20 | #fef8f4 |
| BusinessDarkTheme | Business Dark | business-dark.css | 是 | #4890d8 | #dce2e8 | #121a24 |
| BusinessLightTheme | Business Light | business-light.css | 否 | #2870b8 | #222a34 | #f8fafc |
| CyberpunkDarkTheme | Cyberpunk Dark | cyberpunk-dark.css | 是 | #00e5ff | #e0e8f0 | #0a0e17 |
| CyberpunkLightTheme | Cyberpunk Light | cyberpunk-light.css | 否 | #0097a7 | #1a2332 | #f5f7fa |
| IntelliJDarkTheme | IntelliJ Dark | intellij-dark.css | 是 | #82b1ff | #bbbbbb | #2b2b2b |
| IntelliJLightTheme | IntelliJ Light | intellij-light.css | 否 | #3489e3 | #2b313a | #ffffff |
| LiquidGlassDarkTheme | Liquid Glass Dark | liquid-glass-dark.css | 是 | #589cff | #dce8f8 | #0f1820 |
| LiquidGlassLightTheme | Liquid Glass Light | liquid-glass-light.css | 否 | #3078d8 | #1c2c48 | #f8fbff |
| VSCodeDarkTheme | VS Code Dark | vscode-dark.css | 是 | #4fc1ff | #d4d4d4 | #1e1e1e |
| VSCodeLightTheme | VS Code Light | vscode-light.css | 否 | #0078d4 | #333333 | #ffffff |

- 调用链：`Themes.<常量> → ThemeStyle.*`（无进一步内部调用）

---

## CustomTheme
- 职责：用户定制主题，基于某个内置主题动态生成颜色替换后的 css，实现运行时改色。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `theme` | `ThemeStyle` | 定制所基于的内置主题 |
  | `accentColor` / `backgroundColor` / `foregroundColor` | `Color` | 定制三色（可空） |
  | `themePath` | `String` | 生成的定制 css 文件 URL |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `updateTheme(themeName, bgColor, fgColor, accentColor)` | 更新定制 | `Themes.getTheme(themeName)` 取底；解析三色；删除旧 css；`ThemeUtil.updateThemeCss` 生成新 css 并转 URL 存入 `themePath` |
  | `getName()` | 名称 | 返回 `"CUSTOM"` |
  | `getDesc(Locale)` | 描述 | `I18nHelper.custom()` |
  | `getUserAgentStylesheet()` | 样式表 | `themePath` 为空用底层主题，否则用定制 css |
  | `isDarkMode()` | 暗色 | 委托 `theme.isDarkMode()` |
  | `getTheme()/getThemePath()` | 访问器 | 返回底层主题/定制路径 |
  | `getAccentColor()/getBackgroundColor()/getForegroundColor()` | 定制色 | 返回字段值 |
- 调用链：`ThemeManager.apply(ThemeConfig) → CustomTheme.updateTheme → ThemeUtil.updateThemeCss`

---

## SystemTheme
- 职责：跟随操作系统的主题，监听系统配色/强调色变化并动态生成相近主题 css。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `colorListener` | `ChangeListener<Color>` | 三色变化监听，触发 `changeTheme()` |
  | `colorSchemeChangeListener` | `ChangeListener<ColorScheme>` | 明暗方案变化监听，触发 `changeTheme()` |
  | `following` | `boolean` | 是否正在跟随系统 |
  | `themePath` | `String` | 生成的系统主题 css 路径 |
  | `baseTheme` | `ThemeStyle` | 生成的相近基础主题 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getBaseTheme()` | 基础主题 | 返回 `baseTheme` |
  | `updateThemeCss()` | 更新 css | `ThemeUtil.getSystemNear()` 取相近主题，删除旧文件后以其色重新生成 css 并记录 `baseTheme` |
  | `getName()` | 名称 | `"SYSTEM"` |
  | `getDesc(Locale)` | 描述 | `I18nHelper.followSystem()` |
  | `getUserAgentStylesheet()` | 样式表 | 懒加载 `updateThemeCss()` 后返回文件 URI |
  | `getUserAgentStylesheetBSS()` | 压缩样式 | 返回 `baseTheme` 的样式表 |
  | `isDarkMode()` | 暗色 | `FXUtil.getPreferences().getColorScheme() == DARK` |
  | `getAccentColor()/getForegroundColor()/getBackgroundColor()` | 系统色 | 取 `FXUtil.getPreferences()` 对应属性 |
  | `listener()` / `unListener()` | 注册/注销监听 | 对 `Preferences` 的 accent/foreground/background/colorScheme 属性增删监听器 |
  | `changeTheme()`(protected) | 变更主题 | `FXUtil.runLater` 延迟 100ms 后 `updateThemeCss()` + `ThemeManager.apply(this)` |
- 调用链：`系统配色变化 → colorListener/colorSchemeChangeListener → SystemTheme.changeTheme → updateThemeCss → ThemeManager.apply → 全局重应用`

---
