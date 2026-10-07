# fx-plus 国际化（i18n）

覆盖 `cn.oyzh.fx.plus.i18n` 包。经核查，该包内 `I18nManager`、`I18nHelper`、`Locales` 三个类被整体注释（死代码），**仅 4 个类为正式代码**：`I18nAdapter`、`I18nSelectAdapter`、`I18nResourceBundle`、`LocaleComboBox`。国际化运行能力复用外部库 `cn.oyzh.i18n.*`（如 `I18nManager`、`I18nLocales`、`I18nUtil`）。

## I18nAdapter
- 职责：国际化能力适配接口，提供资源字符串获取与语言变更入口。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `i18nString(String)` | 取资源 | `I18nResourceBundle.i18nString(key)` |
  | `changeLocale(Locale)` | 变更语言（抽象） | 由实现覆盖 |
- 调用链：`NodeManager.init → I18nAdapter.changeLocale`

---

## I18nSelectAdapter<V>
- 职责：国际化下拉项适配接口，按地区提供可选项列表。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `values(Locale)` | 取值列表（抽象） | 由实现覆盖 |
  | `default_Values()` / `zh_CN_Values()` / `zh_TW_Values()` / `english_Values()` | 常用地区便捷取值 | 分别调用 `values(Locale.getDefault()/PRC/TAIWAN/ENGLISH)` |
- 调用链：`NodeManager.init → I18nSelectAdapter.values`（如 `FontSizeComboBox`）

---

## I18nResourceBundle
- 职责：国际化资源绑定器，继承 `ResourceBundle`，按 key 前缀分发到 base/project 资源包并按当前地区缓存。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `INSTANCE` | `I18nResourceBundle`(常量) | 单例实例 |
  | `base_resources` | `Map<Locale,ResourceBundle>` | base 资源缓存（key 以 `base.` 开头） |
  | `i18n_resources` | `Map<Locale,ResourceBundle>` | 项目资源缓存 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 | 私有构造 | 单例模式 |
  | `handleGetObject(String)` | 取资源对象 | `initResource` 后 `getObject` |
  | `getKeys()` | 键枚举 | 返回空枚举 |
  | `initResource(String)`(私有) | 初始化资源 | key 以 `base.` 开头用 `base_i18n` 包，否则 `i18n` 包；按 `I18nManager.currentLocale()` 缓存 |
  | `containsKey(String)` | 是否含键 | |
  | `i18nString(String)` | 取字符串 | 委托 `INSTANCE.getString` |
  | `i18nString(String...)` | 多键拼接 | 英文地区用空格连接且非首词首字母小写，中文直接连接 |
  | `i18nObject(String)` | 取对象 | |
  | `containsI18nKey(String)` | 是否含键 | |
- 调用链：`I18nAdapter.i18nString → I18nResourceBundle.i18nString → ResourceBundle.getBundle(base_i18n/i18n)`

---

## LocaleComboBox
- 职责：区域下拉框，继承 `FXComboBox<Locale>`，展示并可切换语言地区。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `select(String)` | 按名称选中 | 空则取默认地区 `I18nUtil.corrLocale`，异常选首项 |
  | `name()` | 地区名 | `I18nLocales.getLocaleName` |
  | `initNode()` | 初始化 | `addItems(I18nLocales.locales())`、设提示、转换器显示描述 |
- 调用链：`LocaleComboBox 选中 → 外部 I18nManager 切换语言 → 节点 I18nAdapter.changeLocale`

---
