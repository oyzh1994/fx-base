# fx-plus 扩展/原生主题（theme/ext、theme/original）

覆盖 `cn.oyzh.fx.plus.theme.ext`（25 个，包装第三方 `com.dlsc.atlantafx.themes` 主题）与 `cn.oyzh.fx.plus.theme.original`（7 个，包装 atlantafx-base 原生主题），共 32 个类。

## 通用模式（全部 32 类相同）
- 职责：作为第三方/原生 atlantafx 主题的 `ThemeStyle` 适配器，委托底层主题实例提供名称与样式表，并补充前景/背景/强调三色。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `THEME` | `private static final <底层主题>` | 底层主题实例（如 `new ArmyDark()`） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getName()` | 主题名 | `THEME.getName()` |
  | `getUserAgentStylesheet()` | 样式表 | `THEME.getUserAgentStylesheet()` |
  | `getUserAgentStylesheetBSS()` | 压缩样式表 | `THEME.getUserAgentStylesheetBSS()` |
  | `isDarkMode()` | 暗色 | `THEME.isDarkMode()` |
  | `getAccentColor()/getForegroundColor()/getBackgroundColor()` | 三色 | 返回固定 `Color` |
- 调用链：`Themes.<常量> → ThemeStyle.* → 底层 atlantafx 主题`（无进一步内部调用）

## class: ext 包主题差异表

| 类名 | 底层主题 | 暗色 | 强调色 | 前景色 | 背景色 |
|---|---|---|---|---|---|
| ArmyDarkTheme | ArmyDark | 是 | #7daa2a | #d4d8b0 | #1c2214 |
| ArmyLightTheme | ArmyLight | 否 | #486610 | #0e1208 | #e8eccc |
| AutumnTheme | Autumn | 是 | #f9a86b | #b8dee9 | #304c64 |
| BlackyTheme | Blacky | 是 | #D98C42 | #f2f2f2 | #000000 |
| BlueDarkTheme | BlueDark | 是 | #5aabdc | #f4f6f9 | #142342 |
| BlueLightTheme | BlueLight | 否 | #004273 | #0a1530 | #ffffff |
| BrownyTheme | Browny | 是 | #f3da91 | #f5efe8 | #332524 |
| FallDarkTheme | FallDark | 是 | #f09810 | #f8e0b8 | #1e0c06 |
| FallLightTheme | FallLight | 否 | #98300c | #100604 | #fdf8f0 |
| GithubDarkColorblindTheme | GithubDarkColorblind | 是 | #4493f8 | #f0f6fc | #0d1117 |
| GithubDarkTritanopiaTheme | GithubDarkTritanopia | 是 | #4493f8 | #f0f6fc | #0d1117 |
| GithubLightColorblindTheme | GithubLightColorblind | 否 | #0969da | #1f2328 | #ffffff |
| GithubLightDefaultTheme | GithubLightDefault | 否 | #0969da | #24292f | #ffffff |
| GithubLightTritanopiaTheme | GithubLightTritanopia | 否 | #0969da | #1f2328 | #ffffff |
| GithubSoftDarkTheme | GithubSoftDark | 是 | #539bf5 | #adbac7 | #22272e |
| NavyDarkTheme | NavyDark | 是 | #e8cf82 | #eef0f6 | #1a2744 |
| NavyLightTheme | NavyLight | 否 | #3d4f73 | #111b33 | #ffffff |
| NewsTheme | News | 是 | #818cf8 | #f1f5f9 | #0f172a |
| SpringDarkTheme | SpringDark | 是 | #f060b0 | #c8f0c8 | #0c1a10 |
| SpringLightTheme | SpringLight | 否 | #8c2058 | #081408 | #ffffff |
| SummerDarkTheme | SummerDark | 是 | #d48800 | #d0e8ff | #081430 |
| SummerLightTheme | SummerLight | 否 | #005aac | #04102e | #ffffff |
| WinterDarkTheme | WinterDark | 是 | #1898d4 | #d0eeff | #080c18 |
| WinterLightTheme | WinterLight | 否 | #1a4cae | #060e22 | #ffffff |
| YachtTheme | Yacht | 否 | #245f73 | #262928 | #f2f0ef |

## class: original 包主题差异表

| 类名 | 底层主题 | 暗色 | 强调色 | 前景色 | 背景色 |
|---|---|---|---|---|---|
| CupertinoDarkTheme | CupertinoDark | 是 | #2f96ff | rgb(255,255,255) | rgb(28,28,30) |
| CupertinoLightTheme | CupertinoLight | 否 | rgb(0,122,255) | rgb(0,0,0) | rgb(255,255,255) |
| DraculaTheme | Dracula | 是 | #9580ff | #f8f8f2 | #282a36 |
| NordDarkTheme | NordDark | 是 | #98aeca | #ECEFF4 | #2E3440 |
| NordLightTheme | NordLight | 否 | #537297 | #2E3440 | #fafafc |
| PrimerDarkTheme | PrimerDark | 是 | #58a6ff | #c9d1d9 | #0d1117 |
| PrimerLightTheme | PrimerLight | 否 | #0969da | #24292f | #ffffff |

> 注：以上 32 个类字段均仅含底层 `THEME` 常量，方法仅上述 6 个重写方法，故不逐一展开。

---
