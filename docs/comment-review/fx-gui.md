# fx-gui 代码审查文档（主文档）

- 覆盖包：`cn.oyzh.fx.gui` 下**非 SVG 图标**全部子包（button / combobox / event / font / label / media / menu / page / setting / skin / tabs / text / toggle / tray / tree），以及 `cn.oyzh.fx.gui.svg.label`、`cn.oyzh.fx.gui.svg.pane`。
- SVG 图标控件（`svg.glyph`，337 个）见 `fx-gui-svg-glyph.md`。
- 已跳过整文件被注释的死代码：`svg/glyph/FileSVGGlyph.java`、`svg/glyph/FolderSVGGlyph.java`、`svg/pane/LayoutSVGPane2.java`、`svg/path/HistorySVGPath.java`。
- 说明：多数控件继承 `cn.oyzh.fx.plus` 下的 `FX*` 基础控件；`initNode()` 为初始化钩子（设置样式类、文本、尺寸、图标等），`skin()` 返回自定义 `Skin`；`I18nHelper.*` 为国际化文本获取。术语保留原始标识符。

---

# 一、button 包（38）

> 共性：绝大多数继承 `cn.oyzh.fx.plus.controls.button.IconButton`，仅重写 `initNode()`：`setRealHeight(30)` → 设样式类 → `setText/setTipText`（国际化） → `init(new XxxSVGGlyph())` → `super.initNode()`。除继承字段外无自有字段。

## AccentButton
- 职责：强调色图标按钮。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 仅继承 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | `addClass("accent")`；`super.initNode()` |
- 调用链：`new AccentButton() → initNode → addClass("accent")`

## AddCollectionsButton
- 职责：添加收藏按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；success；`I18nHelper.addCollections()`；`AddSVGGlyph` |
- 调用链：`new AddCollectionsButton() → initNode → init(AddSVGGlyph)`

## AddConnectButton
- 职责：新增连接按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；success；`I18nHelper.addConnect()`；`AddSVGGlyph` |
- 调用链：`new AddConnectButton() → initNode → init(AddSVGGlyph)`

## AddFolderButton
- 职责：新建文件夹按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；accent；`I18nHelper.addFolder1()`；`AddGroupSVGGlyph` |
- 调用链：`new AddFolderButton() → initNode → init(AddGroupSVGGlyph)`

## AddGroupButton
- 职责：分组按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；accent；`I18nHelper.addGroup()`；`AddGroupSVGGlyph` |
- 调用链：`new AddGroupButton() → initNode → init(AddGroupSVGGlyph)`

## CalcButton
- 职责：计算按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；accent；`I18nHelper.calc()`；`CalcSVGGlyph` |
- 调用链：`new CalcButton() → initNode → init(CalcSVGGlyph)`

## CancelButton
- 职责：取消按钮，点击后隐藏当前窗口。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；danger；`I18nHelper.cancel()`；`CloseSVGGlyph`；`setOnAction` → `getScene().getWindow().hide()` |
- 调用链：`new CancelButton() → initNode → onAction → window.hide()`

## ChangelogButton
- 职责：更新日志按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；danger；`I18nHelper.changelog()`；`ChangelogSVGGlyph` |
- 调用链：`new ChangelogButton() → initNode → init(ChangelogSVGGlyph)`

## ClearButton
- 职责：清空按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；danger；`I18nHelper.clear()`；`ClearSVGGlyph` |
- 调用链：`new ClearButton() → initNode → init(ClearSVGGlyph)`

## CloseButton
- 职责：关闭按钮（继承 CancelButton，复用关闭行为）。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | `super.initNode()` 后覆盖文本为 `I18nHelper.close()` |
- 调用链：`new CloseButton() → CancelButton.initNode → setText(close())`

## CopyButton
- 职责：复制按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；`I18nHelper.copy()`；`CopySVGGlyph` |
- 调用链：`new CopyButton() → initNode → init(CopySVGGlyph)`

## DumpButton
- 职责：转储按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；`I18nHelper.dump()`；`database.DumpSVGGlyph` |
- 调用链：`new DumpButton() → initNode → init(DumpSVGGlyph)`

## ExecuteButton
- 职责：执行按钮，点击后隐藏当前窗口。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；`I18nHelper.execute()`；`ExecuteSVGGlyph`；`setOnAction` → `window.hide()` |
- 调用链：`new ExecuteButton() → initNode → onAction → window.hide()`

## ExportButton
- 职责：导出按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；`I18nHelper.export()`；`ExportSVGGlyph` |
- 调用链：`new ExportButton() → initNode → init(ExportSVGGlyph)`

## FileButton
- 职责：文件按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；`I18nHelper.file()`；`file.FileSVGGlyph` |
- 调用链：`new FileButton() → initNode → init(file.FileSVGGlyph)`

## GenerateButton
- 职责：生成按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；success；`GenerateSVGGlyph`（无文本） |
- 调用链：`new GenerateButton() → initNode → init(GenerateSVGGlyph)`

## GenerateKeyButton
- 职责：生成密钥按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；success；`I18nHelper.generate()`；`key.GenerateKeySVGGlyph` |
- 调用链：`new GenerateKeyButton() → initNode → init(GenerateKeySVGGlyph)`

## ImportButton
- 职责：导入按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；`I18nHelper._import()`；`ImportSVGGlyph` |
- 调用链：`new ImportButton() → initNode → init(ImportSVGGlyph)`

## LocalTerminalButton
- 职责：本地终端按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；default；`I18nHelper.localTerminal()`；`TerminalSVGGlyph` |
- 调用链：`new LocalTerminalButton() → initNode → init(TerminalSVGGlyph)`

## MigrationButton
- 职责：迁移按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；`I18nHelper.migration()`；`MigrationSVGGlyph` |
- 调用链：`new MigrationButton() → initNode → init(MigrationSVGGlyph)`

## NextButton
- 职责：下一个按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；accent；`I18nHelper.next()`；`NextSVGGlyph` |
- 调用链：`new NextButton() → initNode → init(NextSVGGlyph)`

## NextStepButton
- 职责：下一步按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；accent；`I18nHelper.nextStep()`；`NextStepSVGGlyph` |
- 调用链：`new NextStepButton() → initNode → init(NextStepSVGGlyph)`

## OldButton
- 职责：旧版按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；danger；`OldSVGGlyph`（无文本） |
- 调用链：`new OldButton() → initNode → init(OldSVGGlyph)`

## OpenTerminalButton
- 职责：打开终端按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；default；`I18nHelper.openTerminal()`；`TerminalSVGGlyph` |
- 调用链：`new OpenTerminalButton() → initNode → init(TerminalSVGGlyph)`

## PrevStepButton
- 职责：上一步按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；accent；`I18nHelper.prevStep()`；`PrevStepSVGGlyph` |
- 调用链：`new PrevStepButton() → initNode → init(PrevStepSVGGlyph)`

## ResetButton
- 职责：重置按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；`I18nHelper.reset()`；`ResetSVGGlyph` |
- 调用链：`new ResetButton() → initNode → init(ResetSVGGlyph)`

## RunScriptFileButton
- 职责：运行脚本文件按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；`I18nHelper.runScriptFile()`；`database.RunFileSVGGlyph` |
- 调用链：`new RunScriptFileButton() → initNode → init(RunFileSVGGlyph)`

## RunSqlFileButton
- 职责：运行 SQL 文件按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；`I18nHelper.runSqlFile()`；`database.RunSqlFileSVGGlyph` |
- 调用链：`new RunSqlFileButton() → initNode → init(RunSqlFileSVGGlyph)`

## SaveButton
- 职责：保存按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；accent；`I18nHelper.save()`；`SaveSVGGlyph` |
- 调用链：`new SaveButton() → initNode → init(SaveSVGGlyph)`

## SettingButton
- 职责：设置按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；`I18nHelper.setting()`；`SettingSVGGlyph` |
- 调用链：`new SettingButton() → initNode → init(SettingSVGGlyph)`

## SplitViewButton
- 职责：分屏按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；`SplitViewSVGGlyph`（无文本） |
- 调用链：`new SplitViewButton() → initNode → init(SplitViewSVGGlyph)`

## StartButton
- 职责：启动按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；accent；`I18nHelper.start()`；`SubmitSVGGlyph` |
- 调用链：`new StartButton() → initNode → init(SubmitSVGGlyph)`

## StopButton
- 职责：停止按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；default；`I18nHelper.stop()`；`StopSVGGlyph` |
- 调用链：`new StopButton() → initNode → init(StopSVGGlyph)`

## SubmitButton
- 职责：提交按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；accent；`I18nHelper.submit()`；`SubmitSVGGlyph` |
- 调用链：`new SubmitButton() → initNode → init(SubmitSVGGlyph)`

## SuccessButton
- 职责：成功色图标按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | `addClass("success")`；`super.initNode()` |
- 调用链：`new SuccessButton() → initNode → addClass("success")`

## TestButton
- 职责：测试按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；success；`I18nHelper.test()`；`TestSVGGlyph` |
- 调用链：`new TestButton() → initNode → init(TestSVGGlyph)`

## TransportButton
- 职责：传输按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；`I18nHelper.transport()`；`TransportSVGGlyph` |
- 调用链：`new TransportButton() → initNode → init(TransportSVGGlyph)`

## UnLockButton
- 职责：解锁按钮。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 高度 30；accent；`UnLockSVGGlyph`（无文本） |
- 调用链：`new UnLockButton() → initNode → init(UnLockSVGGlyph)`

---

# 二、combobox 包（3）

## CharsetComboBox
- 职责：字符集下拉选择框，支持按显示名选中与默认字符集回退。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | 无 | - | 仅继承 `FXComboBox<String>` |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `setInitDefault(boolean)` | 设置是否选中系统默认字符集 | true → `select(Charset.defaultCharset())` + 提示；false → `clearSelection()` |
  | `isInitDefault()` | 是否已初始化默认值 | 常量返回 false |
  | `getCharset()` | 获取 `Charset` | 空则 `CharsetUtil.defaultCharset()`，否则 `Charset.forName` |
  | `getCharsetName()` | 获取字符集名 | 空则 `CharsetUtil.defaultCharsetName()` |
  | `select(String)` | 选中字符集 | `setIgnoreChanged(true)`，小写化、`_`→`-` 后 `super.select` |
  | `select(Charset)` | 按 `Charset` 选中 | 取 `displayName()` 委托 `select(String)` |
  | `initNode()` | 初始化选项 | `addItem("")` + 全部可用字符集（小写显示名） |
- 调用链：`initNode → addItem(全部字符集)`；`select(Charset) → select(String) → super.select`

## SSHAuthTypeCombobox
- 职责：SSH 认证类型下拉框（密码 / 证书 / SSH Agent）。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `isPasswordAuth()` | 是否密码认证 | `getSelectedIndex()==0` |
  | `isCertificateAuth()` | 是否证书认证 | 索引 ==1 |
  | `isSSHAgentAuth()` | 是否 SSH Agent 认证 | 索引 ==2 |
  | `getAuthType()` | 获取认证类型标识 | 返回 `sshAgent`/`certificate`/`password` |
  | `initNode()` | 初始化选项 | 加入 `password()`、`publicKey()`、"SSH Agent" |
- 调用链：`initNode → addItem×3`；`getAuthType → isSSHAgentAuth/isCertificateAuth`

## StringComboBox
- 职责：字符串下拉框。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `StringComboBox()` | 空构造 | `super()` |
  | `StringComboBox(List<String>)` | 带候选项构造 | `setItem(list)` |
- 调用链：`new StringComboBox(list) → setItem(list)`

---

# 三、event 包（2）

## Layout1Event
- 职责：布局 1 切换事件载体。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 无 | - | 仅继承 `Event<Object>` |
- 调用链：`post(new Layout1Event()) → 监听者处理`

## Layout2Event
- 职责：布局 2 切换事件载体。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 无 | - | 仅继承 `Event<Object>` |
- 调用链：`post(new Layout2Event()) → 监听者处理`

---

# 四、font 包（2）

## FontFamilyTextField
- 职责：可搜索的字体族输入框（带下拉候选）。
- 字段：无（继承 `SelectTextFiled<String>`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `createDefaultSkin()` | 创建皮肤 | `new FontFamilyTextFieldSkin(this)` |
  | `onTextChanged(String)` | 文本变化过滤候选 | `FontUtil.getFamilies()` 过滤 `containsIgnoreCase`；空则显示全部并弹窗；结果空则 `hidePopup()`，否则 `showPopup()` |
  | `initNode()` | 初始化 | 设提示"请选择字体"；`setItemList(FontUtil.getFamilies())` |
- 调用链：`initNode → setItemList → skin().showPopup`；输入 → `onTextChanged → showPopup/hidePopup`

## FontFamilyTextFieldSkin
- 职责：字体选择下拉皮肤，候选项以各自字体渲染。
- 字段：无（继承 `SelectTextFiledSkin<String>`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `FontFamilyTextFieldSkin(TextField)` | 构造 | `super(textField)` |
  | `initPopup()` | 初始化弹窗/单元格 | 读 `FontManager.currentFontSize()`；`setCellFactory` 中 `updateItem` 用 `Font.font(item,...,fontSize)` 渲染并 `ListViewUtil.highlightCell` |
- 调用链：`SelectTextFiled.skin() → initPopup → setCellFactory`

---

# 五、label 包（5）

## AccentLabel
- 职责：强调色标签。
- 字段：无（继承 `FXLabel`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 实例初始化块 | 加样式类 | `addClass("accent")` |
  | `AccentLabel()` / `AccentLabel(String)` / `AccentLabel(String, Node)` | 构造 | `super(...)` |
- 调用链：`new AccentLabel(...) → addClass("accent")`

## InfoLabel
- 职责：信息标签，加粗显示。
- 字段：无（继承 `AccentLabel`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `InfoLabel()` / `InfoLabel(String)` | 构造 | `super(...)` |
  | `initNode()` | 初始化 | `setRealHeight(30)`；`disableFontWeight()`；`setFontWeight(BOLD)` |
- 调用链：`initNode → setFontWeight(BOLD)`

## SuccessLabel
- 职责：成功色标签。
- 字段：无（继承 `FXLabel`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 实例初始化块 | 加样式类 | `addClass("success")` |
  | `SuccessLabel()` / `SuccessLabel(String)` / `SuccessLabel(String, Node)` | 构造 | `super(...)` |
- 调用链：`new SuccessLabel(...) → addClass("success")`

## SystemLabel
- 职责：系统标签，文字颜色随主题前景色。
- 字段：无（继承 `FXLabel`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `SystemLabel()` / `SystemLabel(String)` / `SystemLabel(String, Node)` | 构造 | `super(...)` |
  | `initNode()` | 初始化 | `setTextFill(ThemeManager.currentForegroundColor())` |
- 调用链：`initNode → ThemeManager.currentForegroundColor`

## TipsLabel
- 职责：提示标签，灰色文字。
- 字段：无（继承 `FXLabel`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `TipsLabel()` / `TipsLabel(String)` / `TipsLabel(String, Node)` | 构造 | `super(...)` |
  | `initNode()` | 初始化 | `setTextFill(Color.GRAY)` |
- 调用链：`initNode → setTextFill(GRAY)`

---

# 六、media 包（1）

## MediaControlBox
- 职责：媒体播放控制面板（进度、时间、播放/暂停/停止、音量），实现 `Destroyable`。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | timeLabel | FXLabel | 时间显示（当前/总时长） |
  | play | PlaySVGGlyph | 播放图标 |
  | stop | StopSVGGlyph | 停止图标 |
  | pause | PauseSVGGlyph | 暂停图标 |
  | progress | FXProgressBar | 播放进度条 |
  | box1 / box2 | FXHBox | 上（进度+时间）/下（控制+音量）行 |
  | ended | boolean | 播放结束标志 |
  | volume | FXSlider | 音量滑块 |
  | volumeLabel | FXLabel | 音量百分比文本 |
  | player | MediaPlayer | 关联媒体播放器 |
  | userSeeking | AtomicBoolean | 用户拖动进度条标志 |
  | userVoluming | AtomicBoolean | 用户调节音量标志 |
  | volumeListener | ChangeListener\<Number\> | 播放器音量 → 滑块同步 |
  | currentTimeListener | ChangeListener\<Duration\> | 播放进度 → 进度条/时间同步 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 构建界面与交互 | 组装进度条/时间、播放/停止/音量；进度条按下/松开按比例 `seek`；音量变化 `setVolume` 并更新百分比 |
  | `play()/pause()/stop()` | 播放/暂停/停止 | 结束态重播前 `seek(ZERO)`；切换 box2 首子节点为 pause/play |
  | `setup(MediaPlayer)` | 绑定播放器 | 注册各 `setOnXxx` 状态回调与 `volumeListener`，初始化音量 |
  | `updateTimeLabel(Duration,Duration)` / `formatTime(Duration)` | 时间格式化 | 输出 `HH:mm:ss`，当前 + " / " + 总时长 |
  | `destroy()` | 释放资源 | 移除监听、置空回调、`NodeDestroyUtil.destroyObject` |
- 调用链：`setup → player 回调 → play/pause/stop → box2.setChild`；`player.currentTimeProperty → currentTimeListener → progress + updateTimeLabel → formatTime`

---

# 七、menu 包（1）

## MenuItemHelper
- 职责：菜单/菜单项静态工厂工具类，集中构建带图标、国际化文本、动作的 `Menu`/`MenuItem`。
- 字段：无（常量工具类）。
- 方法（按组归纳）：
  | 方法组 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `menu(text, [graphic], [action])` | 构建 `Menu` | 委托 `MenuItemManager.getMenu` |
  | `menuItem(text, [graphic], action)` | 构建 `MenuItem` | 委托 `MenuItemManager.getMenuItem` |
  | `separator()` | 分隔菜单项 | `MenuItemManager.getSeparatorMenuItem` |
  | 约 190 个业务具名方法（`openView`/`addTable`/`deleteKey`/`cloneConnect`…） | 各业务菜单项 | 统一模式 `(FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.xxx(), new XxxSVGGlyph(), action)`；`*_no_graphic` 变体不带图标 |
  | `closeAllTab/closeCurrTab/closeLeftTab/closeRightTab/closeOtherTab/closeOtherConnectTab` | 标签页关闭菜单项 | 使用 `I18nResourceBundle.i18nString("base.*")` |
- 关键逻辑：方法体几乎均为 `MenuItemManager.getMenuItem(I18nHelper.<key>(), new <Xxx>SVGGlyph(), action)`。
- 调用链：`RichTab.getMenuItems → MenuItemHelper.close*Tab(Runnable) → MenuItemManager.getMenuItem → 右键菜单`

---

# 八、page 包（3）

## PageEvent
- 职责：分页事件类型定义，含跳页事件。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | PAGE_JUMP_EVENT | static final EventType\<PageEvent\> | 跳页事件类型 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `PageEvent(EventType)` | 构造 | `super(eventType)` |
  | `static jump(int)` | 创建跳页事件 | `new PageJumpEvent(page)` |
- 内部类 `PageJumpEvent`：字段 `int page`（目标页码）；方法 `getPage()`；构造 `super(PAGE_JUMP_EVENT)`。
- 调用链：`PageBox 跳页框回车 → PageEvent.jump(n) → onJumpFired.handle`

## PageBox
- 职责：分页面板（HBox），含首页/上一页/跳页/下一页/尾页/设置与分页文本。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | showText / text | boolean / FXLabel | 是否显示分页文本、文本组件 |
  | showJump / jump | boolean / NumberTextField | 是否显示跳页、跳页输入框 |
  | onJumpFired | EventHandler\<PageJumpEvent\> | 跳页回调 |
  | showFirst / firstBtn / onFirstClicked | boolean / PageFirstSVGGlyph / EventHandler | 首页 |
  | showLast / lastBtn / onLastClicked | boolean / PageLastSVGGlyph / EventHandler | 尾页 |
  | prevBtn / onPrevClicked | PagePrevSVGGlyph / EventHandler | 上一页 |
  | nextBtn / onNextClicked | PageNextSVGGlyph / EventHandler | 下一页 |
  | showSetting / settingBtn / onSettingClicked | boolean / PageSettingSVGGlyph / EventHandler | 设置 |
  | bthSize | String | 按钮尺寸 |
  | paging | Paging\<T\> | 分页信息 |
  | hideIfLessPage | boolean | 少于等于 1 页是否隐藏 |
  | pageTextTpl | String | 分页文本模板（按 Locale） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `pageTextTpl()` | 生成默认模板 | 依 `I18nManager.currentLocale()` 返回简/繁/英模板 |
  | `PageBox()` / `PageBox(String)` | 构造并 `init()` | |
  | `init()` | 组装控件 | 创建各 SVG 按钮并绑点击（先 `formatPage` 再回调）；跳页框回车触发 `PageEvent.jump`；高度监听重算内边距 |
  | `formatPage()` | 刷新文本与跳页值 | `paging.formatTpl(pageTextTpl)`；`jump.setValue(currentPage()+1)` |
  | `flush()` | 刷新 | `formatPage()` |
  | `setPaging(Paging)` | 设置分页 | `formatPage` + 依 `hideIfLessPage` 控制可见 |
  | `setShowText/First/Last/Jump/Setting` | 显示开关 | 增删文本节点或 `setVisible` |
  | `setOnFirstClicked/OnLastClicked/OnPrevClicked/OnNextClicked/OnSettingClicked/OnJumpFired` | 注册回调 | 设置回调并自动显示相应控件 |
- 调用链：`构造 → init → 创建按钮/跳页框 → formatPage`；点击 → `formatPage → onXxxClicked.handle`

## PagePane
- 职责：分页面板（`Region`，实现 `LayoutAdapter/ThemeAdapter`），首页/上一页/下一页/尾页 + 分页文本。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | pageText | FXText | 分页信息文本 |
  | showPageText / showFirst / showLast | boolean | 显示开关 |
  | firstSVG / lastSVG / prevSVG / nextSVG | SVGGlyph | 四个导航图标 |
  | pageTextTpl | String | 分页文本模板（默认中文） |
  | paging | Paging\<T\> | 分页信息 |
  | iconSize | String | 图标尺寸 |
  | hideIfLessPage | boolean | 少于等于 1 页是否隐藏 |
  | onNextClicked/onPrevClicked/onFirstClicked/onLastClicked | EventHandler\<MouseEvent\> | 点击回调 |
  | DEFAULT_MARGIN | static final Insets | 默认边距 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 实例初始化块 | 构建 SVG 与 HBox | 创建四图标设提示；`setOnMousePrimaryClicked` 绑 `formatPageText` + 回调；`managedProperty` 绑定可见性；`NodeManager.init` |
  | `formatPageText()` | 刷新文本 | `paging.formatTpl(pageTextTpl)` |
  | `setIconSize(String)` | 设置图标尺寸 | 四图标 `setSizeStr` |
  | `setPaging(Paging)` | 设置分页 | `formatPageText` + 依 `hideIfLessPage` 控制可见 |
  | `setShowFirst/Last`、`setShowPageText`、`setOnXxxClicked` | 显示与回调 | 更新可见性/边距 |
- 调用链：实例初始化 → HBox → `formatPageText`；点击图标 → `formatPageText + onXxxClicked`

---

# 九、setting 包（8）

## SettingLeftContent
- 职责：设置左侧内容容器（`FXVBox`）。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `SettingLeftContent()` | 空构造 | - |
  | `SettingLeftContent(Node)` | 带子节点构造 | `super(node)` |
- 调用链：`SettingMainPane.setLeft → new SettingLeftContent(left)`

## SettingLeftTreeItem
- 职责：设置左侧树节点（继承 `RichTreeItem<SettingLeftTreeItemValue>`）。
- 字段：无（继承）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `SettingLeftTreeItem(treeView, value)` | 构造 | `setValue(value)` |
  | `getTreeView()` | 返回 `SettingLeftTreeView` | 强转 |
  | `addItem(SettingLeftTreeItemValue)` | 新增子节点 | 设 `parentId`，创建子节点并 `addChild` |
  | `findItem(String)` | 递归查找节点值 | 遍历 `unfilteredChildren()`，匹配 `itemId` 或递归 |
  | `getItemId()` | 节点标识 | `getValue().getId()` |
- 调用链：`SettingLeftTreeView.addItem → root().addItem → addChild`

## SettingLeftTreeItemValue
- 职责：设置左侧树节点值（继承 `RichTreeItemValue`）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | id | String | 节点标识 |
  | name | String | 节点名称 |
  | parentId | String | 父节点标识 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `SettingLeftTreeItemValue(String)` / `(String,String)` | 构造 | |
  | `name()` | 节点名称 | 返回 name |
  | `static of(name,id)` | 工厂 | `new SettingLeftTreeItemValue(name,id)` |
  | `get/setId`、`get/setName`、`get/setParentId` | 属性读写 | |
- 调用链：`SettingLeftTreeItem.addItem → value.setParentId`

## SettingLeftTreeView
- 职责：设置左侧树视图，选中联动右侧内容与面包屑。
- 字段：无（继承 `RichTreeView`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `root()` | 返回 `SettingLeftTreeItem` 根 | 强转 |
  | `addItem(value)` | 向根添加节点 | `root().addItem` |
  | `initTreeView()` | 初始化 | 设 `RichTreeCell` 工厂、根、隐藏根、选中监听、id=`left-tree-view` |
  | `findItem(String)` | 递归查节点值 | `root().findItem` |
  | `selectItem(String)` | 选中节点 | 找到则 `doSelect`，否则 `JulLog.warn` |
  | `doSelect(String)` | 选中处理 | 沿 `parentId` 回溯构建面包屑（`items.reversed()`），拼 " > " 后 `mainPane.updateRightContent(itemId, label)` |
  | `getSettingMainPane()` | 向上查找设置主面板 | 沿 parent 链找 `SettingMainPane` |
- 调用链：`选中节点 → selectedItemChanged → doSelect → findItem 递归 → getSettingMainPane → updateRightContent`

## SettingMainPane
- 职责：设置主面板，左右分栏（左 30% 树 + 右 70% 导航/内容/操作），实现 `Destroyable`。
- 字段：无（继承 `FXHBox`）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `setLeft/getLeft/getLeftContent` | 左栏 | 首次 `new SettingLeftContent(left)`（30%×100%）；否则替换子节点 |
  | `getLeftTreeView()` | 左侧树 | `content.lookup("#left-tree-view")` |
  | `setRight/getRight/getRightContent` | 右栏 | 首次创建含 `SettingRightNavBar` 的 `SettingRightContent`（70%） |
  | `setAction/getAction` | 右侧操作区 | `content.setChild(2, action)` / 取 child(2) |
  | `getNavBar()` | 右侧导航栏 | content child(0) |
  | `updateRightContent(fxId,label)` | 切换右侧显示 | `lookup("#"+fxId)`；存在则 enable 导航栏、`NodeGroupUtil.enable/disappear(this,"setting_item")` 并显示节点设文本；否则 disable |
  | `destroy()` | 销毁 | `clearChild` + `NodeDestroyUtil.destroyObject` |
- 调用链：`SettingLeftTreeView.doSelect → updateRightContent → lookup("#id") → NodeGroupUtil.enable/disappear`

## SettingRightAction
- 职责：设置右侧操作区（`FXHBox`）。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 无 | - | 仅继承 `FXHBox` |
- 调用链：由 `SettingMainPane.setAction` 挂载

## SettingRightContent
- 职责：设置右侧内容容器（`FXVBox`；child0=导航栏、child1=内容、child2=操作区）。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `SettingRightContent()` | 空构造 | - |
  | `SettingRightContent(Node...)` | 带子节点构造 | `super(children)` |
- 调用链：`SettingMainPane.setRight → new SettingRightContent(navBar, right)`

## SettingRightNavBar
- 职责：设置右侧导航栏（`FXLabel`），固定高度加粗。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 实例初始化块 | 样式设置 | `disableFont()`、字号 13、高 30、字重 700、id=`right-nav-bar`、`addClass("accent")` |
- 调用链：`SettingMainPane.setRight → 创建 → setText(面包屑)`

---

# 十、skin 包（18）

## ActionTextFieldSkin（抽象）
- 职责：带右侧操作按钮的文本输入框皮肤基类。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | action | Runnable | 按钮触发的操作 |
  | button | SVGGlyph（protected） | 当前按钮 |
  | DEFAULT_LEFT_PADDING | static final Insets | 默认左侧内边距 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getAction/setAction` | 操作读写 | |
  | `initButton(SVGGlyph)` | 初始化按钮 | 设内边距、不可聚焦，注册 EXIT/ENTERED/CLICKED 过滤器 |
  | `getButton()`（抽象） | 返回按钮 | 子类实现 |
  | `onButtonClick/Exit/Enter` | 交互 | 点击执行 action；进入变红；离开复位 |
  | `setButtonSize/getButtonSizeMin/Max` | 尺寸 | min 8 / max 14 |
  | `resetButtonColor()` | 复位颜色 | `button.setColor(getButtonColor())` |
  | `rightProperty()` | 右侧节点 | 首次把按钮放入右侧 |
  | `onSizeChanged()` | 尺寸变化 | 按高度 0.8 计算并限幅按钮大小 |
  | `dispose()` | 释放 | 移除事件过滤器 |
- 调用链：子类 `getButton()` → `initButton`；`onSizeChanged → setButtonSize`

## ChooseTextFieldSkin
- 职责：选择输入框皮肤（右侧"选择"图标，聚焦显示）。
- 字段：无（继承 ActionTextFieldSkin）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getButton()` | 按钮 | `ChooseSVGGlyph` |
  | `updateButtonVisibility()` | 可见性 | 聚焦且非禁用/可见时显示 |
  | `getButtonSizeMax()` | 最大尺寸 | 返回 12 |
- 调用链：`ChooseTextField.skin() → getButton → ChooseSVGGlyph`

## ClearableTextFieldSkin
- 职责：可清除文本输入框皮肤（聚焦且非空显示关闭图标，点击清空）。
- 字段：无（继承 ActionTextFieldSkin）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getButton()` | 按钮 | `CloseSVGGlyph` |
  | `updateButtonVisibility()` | 可见性 | 聚焦且非空显示 |
  | `onButtonClick(MouseEvent)` | 清空 | `setText("")` |
  | `getButtonSizeMax()` | 最大尺寸 | 返回 12 |
- 调用链：`点击 → setText("")`

## ExampleTextFieldSkin
- 职责：示例文本输入框皮肤（点击按钮填入示例文本）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | exampleText | String（protected） | 示例文本 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getExampleText/setExampleText` | 示例文本读写 | |
  | `getButton()` | 按钮 | `ExampleSVGGlyph` |
  | `onButtonClicked(MouseEvent)` | 填入示例 | `setText(exampleText)` |
  | `updateButtonVisibility()` | 可见性 | 聚焦显示 |
  | `dispose()` | 释放 | 置空 exampleText |
- 调用链：`ExampleTextField.setExampleText → 点击 → setText(exampleText)`

## SaveFileTextFieldSkin
- 职责：保存文件输入框皮肤（点击打开保存对话框）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | initFileName | String | 初始文件名 |
  | extension | FileExtensionFilter | 文件扩展名过滤器 |
  | onFileSelected | Consumer\<File\> | 文件选中回调 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getButton()` | 按钮 | `ChooseSVGGlyph` |
  | `onButtonClick(MouseEvent)` | 打开保存对话框 | 无过滤器则 `FXChooser.allExtensionFilter()`；`FileChooserHelper.save`；选中后有回调则 `accept`，否则 `setText/setTipText(path)` |
  | `get/setExtension`、`get/setInitFileName`、`get/setOnFileSelected` | 属性读写 | |
  | `updateButtonVisibility()` | 可见性 | 聚焦显示 |
  | `dispose()` | 释放 | 置空 |
- 调用链：`onButtonClick → FileChooserHelper.save → onFileSelected.accept / setText`

## ChooseDirTextFieldSkin
- 职责：目录输入框皮肤（继承 ChooseTextFieldSkin，点击选择目录）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dir | File（protected） | 选中目录 |
  | alwaysShowGraphic | boolean（protected） | 是否常显图标 |
  | initDir | String（protected） | 初始目录 |
  | onSelectedDir | Consumer\<File\> | 目录选中回调 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `onButtonClick(MouseEvent)` | 选择目录 | `DirChooserHelper.choose(...)`；有回调则 `accept`，否则 `setText/setTipText` |
  | `updateButtonVisibility()` | 可见性 | `alwaysShowGraphic` 时常显 |
  | `setAlwaysShowGraphic(boolean)` | 设置常显 | 更新可见性 |
  | `get/setDir`、`get/setInitDir`、`get/setOnSelectedDir` | 属性读写 | |
  | `dispose()` | 释放 | 置空 |
- 调用链：`onButtonClick → DirChooserHelper.choose → setDir / onSelectedDir.accept`

## ChooseFileTextFieldSkin
- 职责：文件输入框皮肤（继承 ChooseTextFieldSkin，点击选择文件）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | file | File（protected） | 选中文件 |
  | alwaysShowGraphic | boolean（protected） | 是否常显图标 |
  | filters | List\<FileExtensionFilter\>（protected） | 过滤器集合 |
  | onSelectedFile | Consumer\<File\> | 文件选中回调 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `onButtonClick(MouseEvent)` | 选择文件 | 空过滤器则补 `allExtensionFilter()`；`FileChooserHelper.choose`；有回调则 `accept`，否则 `setText/setTipText` |
  | `updateButtonVisibility()` | 可见性 | 常显或聚焦 |
  | `get/setFilters`、`get/setFile`、`is/setAlwaysShowGraphic`、`get/setOnSelectedFile` | 属性读写 | |
  | `dispose()` | 释放 | 置空 |
- 调用链：`onButtonClick → FileChooserHelper.choose → setFile / onSelectedFile.accept`

## EnlargeTextFiledSkin
- 职责：可展开文本输入框皮肤（弹窗内文本域编辑）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | enlargeWidth | double（protected，默认 350） | 展开宽 |
  | enlargeHeight | double（protected，默认 280） | 展开高 |
  | popup | PopupExt（protected） | 编辑弹窗 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getButton()` | 按钮 | `EnlargeSVGGlyph` |
  | `onButtonClick(MouseEvent)` | 打开编辑弹窗 | 禁用输入框，创建 `FXTextArea` + 提交/取消图标，`popup.showPopup` |
  | `handleHide()` | 关闭弹窗 | `popup.hide`、恢复可用、复位按钮色 |
  | `onSubmit(String)` | 提交 | `setText(text)` + `handleHide` |
  | `get/setEnlargeWidth/Height` | 尺寸读写 | |
  | `updateButtonVisibility()` | 可见性 | 聚焦显示 |
  | `dispose()` | 释放 | 销毁 popup |
- 调用链：`onButtonClick → 弹窗 → 提交/隐藏 → onSubmit → setText`

## DateTextFieldSkin
- 职责：日期输入框皮肤（弹窗日历选择）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | formatter | DateTimeFormatter | 日期格式化器 |
  | popup | PopupExt | 日历弹窗 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `get/setFormatter` | 格式化器读写 | |
  | `formatter()` | 取格式化器 | 默认 `yyy-MM-dd` |
  | `getButton()` | 按钮 | `DateSVGGlyph` |
  | `onButtonClick(MouseEvent)` | 打开日历 | 创建 `Calendar` + 提交/取消，提交时 `setText(formatter().format(date))` |
  | `getLocalDateTime()` | 解析文本 | `LocalDateTime.parse` |
  | `handleHide()` | 关弹窗 | 恢复控件 |
  | `updateButtonVisibility()` | 可见性 | 聚焦显示 |
- 调用链：`onButtonClick → Calendar → 提交 → setText → handleHide`

## DateTimeTextFieldSkin
- 职责：日期时间输入框皮肤（日历 + 时/分/秒下拉）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | formatter | DateTimeFormatter | 日期时间格式化器 |
  | popup | PopupExt | 弹窗 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `get/setFormatter` / `formatter()` | 格式化器 | 默认 `yyyy-MM-dd HH:mm:ss` |
  | `onButtonClick(MouseEvent)` | 组装弹窗 | `Calendar` + 24/60/60 时分秒下拉 + 提交/取消；提交时 `LocalDateTimeUtil.of(date,time)` 格式化写回；监听日历值启用/禁用时间区 |
  | `getLocalDateTime()` | 解析文本 | `LocalDateTime.parse` |
  | `handleHide()` | 关弹窗 | 恢复控件 |
  | `getButton()` / `updateButtonVisibility()` | 按钮与可见性 | `DateSVGGlyph`；聚焦显示 |
- 调用链：`onButtonClick → Calendar+时分秒 → 提交 → LocalDateTimeUtil.of → setText`

## TimeTextFieldSkin
- 职责：时间输入框皮肤（时/分/秒下拉选择）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | formatter | DateTimeFormatter | 时间格式化器 |
  | popup | PopupExt | 弹窗 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `get/setFormatter` / `formatter()` | 格式化器 | 默认 `HH:mm:ss` |
  | `onButtonClick(MouseEvent)` | 组装弹窗 | 生成 0-23/0-59/0-59 下拉并选中当前时间；提交时 `LocalTime.now().withHour/withMinute/withSecond` 格式化写回 |
  | `getLocalTime()` | 解析文本 | `LocalTime.parse` |
  | `handleHide()` | 关弹窗 | 恢复控件 |
  | `getButton()` / `updateButtonVisibility()` / `dispose()` | 按钮/可见性/释放 | `DateSVGGlyph`；销毁 popup、置空 formatter |
- 调用链：`onButtonClick → 下拉 → 提交 → formatter().format → setText`

## DigitalTextFieldSkin
- 职责：数字输入框皮肤，带上下增减按钮。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | incrButton | SVGGlyph（protected） | 增加按钮 |
  | decrButton | SVGGlyph（protected） | 减少按钮 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `DigitalTextFieldSkin(TextField,Runnable onIncr,Runnable onDecr)` | 构造 | 绑定按钮点击回调；注册 KEY_PRESSED（UP→incr、DOWN→decr） |
  | `rightProperty()` | 右侧节点 | 惰性创建 Up/Down 图标放入 `FXVBox`，高度绑定输入框 |
  | `updateButtonVisibility()` | 可见性 | 聚焦且非禁用显示两按钮 |
  | `disable/EnableDecrButton`、`disable/EnableIncrButton` | 按钮启停 | 注意 `disableIncrButton` 实现为 `setDisable(false)` |
  | `onSizeChanged()` | 尺寸调整 | 按高度算按钮大小并设边距 |
  | `dispose()` | 释放 | 销毁按钮 |
- 调用链：`DigitalTextField.createDefaultSkin → new DigitalTextFieldSkin(this, incrValue, decrValue)`；上下键 → onIncr/onDecr

## SearchTextFieldSkin
- 职责：搜索输入框皮肤（继承 ClearableTextFieldSkin，左侧历史图标 + 搜索历史弹窗）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | history | SVGGlyph（protected） | 历史图标 |
  | popup | SearchHistoryPopup（protected） | 搜索历史弹窗 |
  | onKeyPressed | EventHandler\<KeyEvent\> | 回车搜索、上下键切换历史 |
  | onMousePressed | EventHandler\<MouseEvent\> | 关闭弹窗 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `get/setPopup` | 弹窗读写 | 设置时 `setOnHistorySelected` |
  | `showPopup/closePopup` | 显示/隐藏 | 委托 popup |
  | `onSearch(String)` | 搜索 | `closePopup()` |
  | `onHistorySelected(String)` | 选历史 | `setText(text)` + `closePopup` |
  | `leftProperty()` | 左侧节点 | 创建 `HistorySVGGlyph`，点击 `showPopup` |
  | `dispose()` | 释放 | 移除过滤器、销毁组件 |
- 调用链：`回车 → onSearch → closePopup`；`UP/DOWN → popup.getPrev/NextHistory → onHistorySelected → setText`

## MatchCaseTextFieldSkin
- 职责：匹配大小写输入框皮肤（右侧图标切换激活态）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | matchCaseProperty | BooleanProperty | 匹配大小写属性 |
  | matchCasePropertyWrapper | ReadOnlyBooleanWrapper | 只读包装 |
  | activeBackground / focusBackground | Background | 激活/悬停背景 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `is/setMatchCase`、`matchCasePropery` | 属性读写 | |
  | `getButton()` | 按钮 | `MatchCaseSVGGlyph` |
  | `onButtonClick(MouseEvent)` | 切换 | `setMatchCase(!...)` 并换背景 |
  | `onButtonEnter/Exit` | 悬停 | 换/复位背景 |
  | `activeBackground()/focusBackground()` | 生成背景 | 基于颜色生成 `Background` |
  | `updateButtonVisibility()` | 可见性 | 常显 |
  | `dispose()` | 释放 | 解绑 |
- 调用链：`点击 → setMatchCase → button.setBackground(active/focus)`

## FilterTextFieldSkin
- 职责：过滤输入框皮肤（清除/全词匹配/匹配大小写 按钮）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | clear | CloseSVGGlyph | 清除按钮 |
  | wholeWord | WholeWordSVGGlyph | 全词匹配按钮 |
  | matchCase | MatchCaseSVGGlyph | 匹配大小写按钮 |
  | wholeWordProperty / matchCaseProperty | BooleanProperty | 属性 |
  | wholeWordPropertyWrapper / matchCasePropertyWrapper | ReadOnlyBooleanWrapper | 只读包装 |
  | wholeWord/matchCase 三组 Mouse 处理器 | EventHandler | 进入/离开/点击 |
  | heightListener | ChangeListener\<Number\> | 高度变化调边距 |
  | activeBackground / focusBackground | Background | 激活/悬停背景 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `is/setWholeWord`、`wholeWordPropery`、`is/setMatchCase`、`matchCasePropery` | 属性读写 | |
  | `updateClearStatus()` | 清除按钮显示 | 依聚焦与文本 |
  | `doInit()` | 初始化监听与处理器 | 注册文本/聚焦监听与按钮事件 |
  | `rightProperty()` | 组装三按钮 HBox | 首次 `doInit` + 创建按钮 + 高度监听 |
  | `activeBackground()/focusBackground()` | 背景 | 生成 `Background` |
  | `dispose()` | 释放 | 移除过滤器、解绑 |
- 调用链：`rightProperty → doInit → 点击 → setWholeWord/setMatchCase → 换背景`

## HighlightTextFieldSkin
- 职责：高亮输入框皮肤（清除/匹配大小写/全词/正则 按钮）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | clear | CloseSVGGlyph | 清除按钮 |
  | regex | RegexSVGGlyph | 正则按钮 |
  | wholeWord | WholeWordSVGGlyph | 全词按钮 |
  | matchCase | MatchCaseSVGGlyph | 匹配大小写按钮 |
  | regexProperty / wholeWordProperty / matchCaseProperty | BooleanProperty | 属性 |
  | 三个 ReadOnlyBooleanWrapper | ReadOnlyBooleanWrapper | 只读包装 |
  | regex/wholeWord/matchCase 三组 Mouse 处理器 | EventHandler | 进入/离开/点击 |
  | heightListener | ChangeListener\<Number\> | 高度变化调边距 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `is/setRegex`、`regexPropery`、`is/setWholeWord`、`wholeWordPropery`、`is/setMatchCase`、`matchCasePropery` | 属性读写 | |
  | `updateClearStatus()` | 清除按钮显示 | 依聚焦与文本 |
  | `doInit()` | 初始化 | 注册监听与处理器 |
  | `rightProperty()` | 组装四按钮 HBox | 首次 `doInit` + 创建按钮 + 高度监听 |
  | `activeBackground(glyph)/focusBackground(glyph)` | 背景 | 按图标高度差计算 |
  | `dispose()` | 释放 | 移除过滤器、解绑 |
- 调用链：`rightProperty → doInit → 点击 → setRegex/setWholeWord/setMatchCase → 换背景`

## PasswordTextFieldSkin
- 职责：密码输入框皮肤，眼睛图标切换明文显示。
- 字段：无（继承 ActionTextFieldSkin）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getButton()` | 按钮 | `EyeSVGGlyph` |
  | `onButtonClick(MouseEvent)` | 切明文 | 切换 `setRevealPassword` 并设提示 |
  | `updateButtonVisibility()` | 可见性 | 聚焦显示 |
- 调用链：`点击 → PasswordTextField.setRevealPassword(!reveal)`

## SelectTextFiledSkin（泛型）
- 职责：可选择文本输入框皮肤，带下拉候选列表 `FXListView` 弹窗。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | popup | FXPopup（protected） | 下拉弹窗 |
  | lineHeight | double（protected，默认 25） | 行高 |
  | converter | StringConverter\<T\> | 值↔文本转换器 |
  | selectItemChanged | Consumer\<T\>（protected） | 选中项变更回调 |
  | itemList | List\<T\> | 内容列表 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `onButtonClick(MouseEvent)` | 切换弹窗 | 弹窗显示中或空则 `hidePopup`，否则 `showPopup` |
  | `showPopup/hidePopup` | 显示/隐藏 | `showFixed` + `listView.setItem(getItemList())`；处理 RTL 朝向 |
  | `initPopup()` | 初始化弹窗 | 创建 `FXListView`（点击/回车确认），单元格用 converter 渲染，`FXScrollPane` 绑宽 |
  | `doPreview()` | 预览 | 选中项写回文本（`setTexting`） |
  | `doSelected([item])` | 确认选择 | `doPreview` + `selectItemChanged.accept(item)` + 隐藏 |
  | `listView()/scrollPane()/calcSize()` | 组件与尺寸 | 高度=项数×行高+4，上限 300 |
  | `selectItem/selectFirst/selectIndex/getSelectedItem/clearSelection/getSelectedIndex` | 选中操作 | 委托 listView / `doSelected` |
  | `isTexting/setTexting/clearTexting` | "输入中"标志 | 借 `PropertiesUtil` 在控件上打标记 |
  | `set/getItemList`、`clearItemList`、`getItemSize`、`isItemEmpty`、`isPopupShowing` | 列表管理 | |
  | `onPopupHide/onPopupShowing` | 弹窗事件钩子 | 子类可重写 |
  | `getButton()` | 按钮 | `SelectSVGGlyph` |
  | `getButtonSizeMax()` | 最大尺寸 | 返回 12 |
  | `layoutChildren` | 布局 | 将文本节点强制 LTR |
  | `dispose()` | 释放 | 解绑、置空 |
- 调用链：`SelectTextFiled.selectItem → skin().selectItem → doSelected → doPreview + selectItemChanged`；点击图标 → `onButtonClick → showPopup/hidePopup`

---

# 十一、tabs 包（6）

## RichTab（抽象）
- 职责：动态标签页基类，加载 FXML 内容、绑定控制器、右键菜单、关闭行为与销毁。
- 字段：无显式字段（控制器以 `setProp("_controller", ...)` 存放于属性）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `loadContent()` | 加载内容 | `FXMLLoaderExt.load(url())`，加 `FXStyle.FX_BASE`；`setProp("_controller",controller)`；`controller.onTabInit(this)` |
  | `controller()` | 取控制器 | `getProp("_controller")` |
  | `url()` / `reload()` | 子类覆盖 | 默认 null / 空 |
  | `getTitle/setTitle` | 标题 | 设文本/提示并同步图标提示 |
  | `graphic(Node)/fill(Paint)` | 图标与颜色 | 支持 SVGGlyph/SVGLabel |
  | `getMenuItems()` | 右键菜单 | `MenuItemHelper.close*Tab` 组装关闭当前/左/右/其他/全部 |
  | `closeLeftTab/closeRightTab/closeAllTab/closeOtherTab` | 关闭逻辑 | 依 `tabs()` 顺序收集可关闭页后 `closeTabs` |
  | `flushTitle()/getTabTitle()` | 刷新标题 | 标题变化时 `setTitle` |
  | `onTabClosed/onTabCloseRequest` | 生命周期 | 委托控制器 |
  | `initNode()` | 初始化 | `loadContent` + `ObjectWatcherManager.watch` |
  | `destroy()` | 销毁 | 控制器 destroy + super |
- 调用链：`initNode → loadContent → FXMLLoaderExt.load → controller.onTabInit`；关闭 → `onTabClosed → controller.onTabClosed → destroy`

## RichTabController（抽象）
- 职责：动态标签页控制器基类，持有标签弱引用并提供标签操控。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | tabRef | WeakReference\<FXTab\> | 标签页弱引用 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `setTab/getTab` | 标签读写 | 弱引用 |
  | `getTabPane/getWindow` | 上级容器 | 经 Tab→TabPane→Scene→Window |
  | `closeTab/disableTab/enableTab/flushTab` | 标签操作 | 委托 FXTab |
  | `flushTabGraphic/Color` | 图标刷新 | 委托 FXTab |
  | `bindListeners()` | 绑定监听（子类覆盖） | 空 |
  | `onTabInit(FXTab)` | 初始化 | `setTab` + `bindListeners` + `EventListener.register` |
  | `onTabClosed(Event)` | 关闭 | `unregister` + `EventUtil.post(TabClosedEvent)` |
  | `onTabCloseRequest(Event)` | 请求关闭（子类覆盖） | 空 |
  | `changeLocale/initialize` | 适配 | 空 |
  | `getTabContent/getTabGraphic` | 内容/图标 | 委托 FXTab |
  | `destroy()` | 销毁 | 清弱引用 + 销毁对象 |
- 调用链：`onTabInit → setTab + bindListeners + register`；`onTabClosed → post(TabClosedEvent)`

## ParentTabController
- 职责：父标签页控制器，转发生命周期事件给子控制器。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `setTab(FXTab)` | 设置标签 | 遍历子控制器，`SubTabController.parent(this)` 并 `setTab` |
  | `onTabInit(FXTab)` | 初始化转发 | 逐个 `controller.onTabInit` |
  | `onTabClosed(Event)` | 关闭转发 | 逐个 `controller.onTabClosed` |
  | `initialize(...)` | 初始化 | 调用 super |
  | `getSubControllers()` | 子控制器列表 | 默认空列表 |
- 调用链：`ParentTabController.onTabInit → 逐个子控制器 onTabInit`

## SubTabController
- 职责：子标签页控制器，持有父控制器引用。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | parent | ParentTabController | 父控制器 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `parent()` | 取父控制器 | |
  | `parent(ParentTabController)` | 设父控制器 | |
- 调用链：`ParentTabController.setTab → subTabController.parent(this)`

## RichTabPane
- 职责：动态标签页面板（`FXTabPane`）。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getTab(Class)` | 按类型取标签 | 遍历 `getTabs()` 匹配 class |
  | `closeTab(Class)` / `closeTab(Tab)` | 关闭标签 | 可关闭才 `FXUtil.runLater remove` |
  | `reload()` | 重载 | 选中页 `reload()` |
  | `initNode()` | 初始化 | `setupRefreshListener` |
- 调用链：`closeTab(Class) → getTab → closeTab(Tab)`

## TabClosedEvent
- 职责：标签页关闭事件（`Event<Tab>`）。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `TabClosedEvent(Tab)` | 构造 | `super(tab)` |
- 调用链：`RichTabController.onTabClosed → EventUtil.post(new TabClosedEvent(tab))`

---

# 十二、text 包（32）

## AccentText
- 职责：强调色文本。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 实例初始化块 | 加样式 | `addClass("accent")` |
  | `AccentText()` / `AccentText(String)` | 构造 | `super(...)` |
- 调用链：`new AccentText(...) → addClass("accent")`

## SuccessText
- 职责：成功色文本。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 实例初始化块 | 加样式 | `addClass("success")` |
  | `SuccessText()` / `SuccessText(String)` | 构造 | `super(...)` |
- 调用链：`new SuccessText(...) → addClass("success")`

## MsgTextArea
- 职责：消息文本域（只读、自动追加、行数限制）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | LINE_MAX_LENGTH | static int | 单行最大长度（20×1024） |
  | lineLimit | int | 最大行数（默认 3000） |
  | limitPolicy | byte | 限制策略 1-6 |
  | queue | Queue\<String\> | 消息队列 |
  | appending | AtomicBoolean | 拼接中标志 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `appendLines(Collection)/appendLine(String)/appendText(String)` | 追加文本 | 超长行替换 `contentTooLarge()`；超 `lineLimit` 则 `deleteLimitLine`；`doAppend` 异步拼接 |
  | `doAppend(String)` | 异步批量拼接 | 入队；`ThreadUtil.start` 合并队列后 `super.appendText` |
  | `text(String)` | 设置文本 | 清空队列后 `super.text` |
  | `deleteLimitLine(long)` | 删除超限行 | 按策略保留全部/清空/90%/70%/50%/30% |
  | `get/setLineLimit`、`get/setLimitPolicy` | 配置读写 | |
- 调用链：`appendLine/appendLines → appendText → deleteLimitLine + doAppend → 队列 → super.appendText`

## ReadOnlyTextArea
- 职责：只读文本域。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 实例初始化块 | 只读设置 | `setRequire(false)`、`setEditable(false)`、`setPickOnBounds(true)`、`setFocusTraversable(false)` |
- 调用链：`new ReadOnlyTextArea() → 实例块`

## LimitTextField
- 职责：带最大长度限制的文本输入框基类（实现 `LimitLenControl`）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | maxLen | Long（protected） | 最大长度 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 四个构造（text/maxLen 组合） | 构造 | |
  | `getMaxLen()` | 取最大长度 | |
  | `setMaxLen(Long)` | 设最大长度 | 非空且无 formatter 时设 `TextFormatter(LimitOperator)` |
  | `validate()` | 校验 | 超长 `ValidatorUtil.validFail` |
- 调用链：`setMaxLen → TextFormatter(LimitOperator)`；`validate → validFail`

## ChooseTextField
- 职责：选择输入框（点击右侧按钮触发操作，不可编辑）。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `setAction(Runnable)` | 设置按钮操作 | 委托 skin |
  | `skin()` / `createDefaultSkin()` | 皮肤 | `ChooseTextFieldSkin` |
  | `initNode()` | 初始化 | `setEditable(false)` |
- 调用链：`setAction → ChooseTextFieldSkin.action`

## ChooseFileTextField
- 职责：文件选择框（可承载字节或路径）。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getValue()` | 取值 | 优先读 `skin().getFile()` 的字节，否则 `super.value()` |
  | `setValue(Object)` | 设值 | 处理 byte[]/Byte[]/String |
  | `setFilter/setFilters` | 过滤器 | 委托 skin |
  | `is/setAlwaysShowGraphic`、`getFile` | 属性 | |
  | `skin()` / `createDefaultSkin()` | 皮肤 | `ChooseFileTextFieldSkin` |
  | `setOnSelectedFile(Consumer<File>)` | 选中回调 | |
- 调用链：`点击按钮 → ChooseFileTextFieldSkin.onButtonClick → 文件 → onSelectedFile / setText`

## ChooseDirTextField
- 职责：目录选择框。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `setInitDir`、`is/setAlwaysShowGraphic`、`getDir` | 属性 | 委托 skin |
  | `skin()` / `createDefaultSkin()` | 皮肤 | `ChooseDirTextFieldSkin` |
  | `setOnSelectedDir(Consumer<File>)` | 选中回调 | |
- 调用链：`ChooseDirTextFieldSkin.onButtonClick → DirChooserHelper.choose`

## ClearableTextField
- 职责：可清除文本输入框。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 四个构造（text/maxLen 组合） | 构造 | |
  | `createDefaultSkin()` | 皮肤 | `ClearableTextFieldSkin` |
- 调用链：`ClearableTextField → ClearableTextFieldSkin`

## DisabledTextField
- 职责：禁用文本控件。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 禁用 | `setDisable(true)`、`setEditable(false)`、`setPickOnBounds(true)` |
- 调用链：`initNode → setDisable(true)`

## ReadOnlyTextField
- 职责：只读文本控件。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 只读 | `setEditable(false)`、`setPickOnBounds(true)`、`setFocusTraversable(false)` |
- 调用链：`initNode → setEditable(false)`

## PortTextField
- 职责：端口输入框（1-65535）。
- 字段：无（继承 NumberTextField）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化范围 | `setMin(1)`、`setMax(65535)`、提示"1-65535" |
- 调用链：`initNode → setMin/setMax`

## YearTextField
- 职责：年份输入框（继承 NumberTextField）。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `YearTextField()` | 构造 | `super(null)` |
  | `setValue(Object)` | 设值 | 含 "-" 的字符串取首段，Date 取 `1900+year` |
  | `static format(Object)` | 格式化 | 归一化为年份字符串 |
- 调用链：`setValue → value(...)`

## DigitalTextField（抽象）
- 职责：数字文本输入框基类，含最大/最小值、步进、值转换与增减。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | maxVal / minVal | Number（protected） | 最大/最小值 |
  | step | Number（protected，默认 1L） | 步进值 |
  | textFormatter | TextFormatter\<String\>（protected final） | 文本格式器 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `get/setMaxVal`、`get/setMinVal`、`get/setStep` | 属性 | |
  | `textFormatter()` | 取格式器 | |
  | `getConverter()`（抽象） | 数字转换器 | 子类实现 |
  | `valueChanged(String)` | 值变化 | 依 min/max 启停皮肤增减按钮 |
  | `getByteValue/ShortValue/IntValue/LongValue/FloatValue/DoubleValue` | 数值转换 | 经 `value()` |
  | `value()` / `value(Number)` | 解析/限幅 | 限幅后写 `textFormatter` |
  | `setValue(Object)` | 设值 | Number/CharSequence |
  | `skin()`/`createDefaultSkin()` | 皮肤 | `DigitalTextFieldSkin(this, incrValue, decrValue)` |
  | `incrValue/decrValue`（抽象）、`createFilter`（抽象） | 增减/过滤 | 子类实现 |
  | `onBlur()` | 失焦 | 越界归位 |
  | `initNode()` | 初始化 | 聚焦监听 onFocus/onBlur |
- 调用链：`textFormatter.valueProperty → valueChanged → skin.disable/enableDecr/IncrButton`；`initNode → onFocus/onBlur → value(...)`

## NumberTextField
- 职责：整数文本域（继承 DigitalTextField）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | converter | DigitalConverter | 数字转换器 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造（含 min/max） | 构造 | |
  | `getConverter()` | 转换器 | 惰性 `new DigitalConverter()` |
  | `incrValue/decrValue` | 增减 | ±`step.longValue()` |
  | `createFilter()` | 输入过滤 | `RegexUtil.isNumber` + `checkLenLimit` |
  | `getValue()` | 取值 | 返回 Long |
  | `value()` | 解析 | 空/"+"/"-" 返回 null |
  | `static format(Object)` | 格式化 | 整数字符串 |
  | `set/getMin`、`set/getMax` | 范围 | |
- 调用链：输入 → `createFilter` → `valueChanged`

## DecimalTextField
- 职责：小数文本域（继承 DigitalTextField），支持小数位数。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | scaleLen | Integer（protected） | 小数位数 |
  | converter | DigitalConverter | 数字转换器 |
  | format | DigitalFormat | 数字格式化器 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造（含 scaleLen/min/max） | 构造 | |
  | `get/setScaleLen` | 小数位 | `setScaleLen` 触发 `format()` |
  | `getConverter()` | 转换器 | 惰性创建并同步 scaleLen |
  | `incrValue/decrValue` | 增减 | ±`step.doubleValue()` |
  | `createFilter()` | 输入过滤 | 允许小数/整数；超位数 `BigDecimal.setScale(scaleLen, HALF_UP)` |
  | `getValue()` | 取值 | 返回 Double |
  | `value()` | 解析 | 空/±/-/. 返回 0D |
  | `format()` | 格式化器 | 同步 `DigitalFormat` |
  | `static format(Object)` | 格式化 | 小数字符串 |
  | `set/getMin`、`set/getMax` | 范围 | |
- 调用链：输入 → `createFilter → BigDecimal.setScale(HALF_UP)`

## BooleanTextFiled
- 职责：布尔文本输入框（下拉 true/false）。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getValue()` | 取值 | 选中项 == "true" |
  | `formatValue()` | 格式化显示 | `selectItem(format(super.value()))` |
  | `initNode()` | 初始化 | 加 true/false、不可编辑 |
  | `static format(Object)` | 归一化 | 依 CharSequence/Boolean/Number 归一为 true/false |
- 调用链：`formatValue → selectItem(format(value))`

## BinaryTextFiled
- 职责：二进制（BLOB）文本框（继承 ChooseFileTextField）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | scale | Integer（默认 2） | 文件大小换算基数 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `get/setScale` | 换算基数 | |
  | `formatValue()` | 格式化 | `format(super.value(), scale)` |
  | `static format(Object,Integer)` | 格式化 | 输出 `(BLOB) 大小`（`NumberUtil.formatSize`） |
- 调用链：`formatValue → NumberUtil.formatSize`

## BitTextField
- 职责：位文本框（仅允许 0/1，与字节互转）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | textFormatter | TextFormatter\<Number\>（protected final） | 文本格式器 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `()` / `(Long maxLen)` | 构造 | 设长度并安装格式器 |
  | `createFilter()` | 过滤 | 仅允许 `^[01]+$` + `checkLenLimit` |
  | `getValue()` | 取值 | `TextUtil.bitStrToByte` |
  | `formatValue()` | 格式化 | `format(super.value())` |
  | `static format(Object)` | 格式化 | `TextUtil.byteToBitStr` |
- 调用链：输入 → `createFilter → checkLenLimit`

## CharsetTextField
- 职责：字符集选择输入框（继承 SelectTextFiled<String>，可搜索）。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `charsets()` | 枚举字符集 | `Charset.availableCharsets()` |
  | `setInitDefault/isInitDefault` | 默认值 | 选中默认字符集或清空 |
  | `getCharset/getCharsetName` | 取值 | 空则回退默认 |
  | `selectItem(String)` | 选中 | `setIgnoreChanged(true)`，`_`→`-` 后 `super.selectItem` |
  | `select(Charset)` | 按字符集选中 | 委托 `selectItem` |
  | `onTextChanged(String)` | 过滤 | 依输入过滤并 `showPopup/hidePopup` |
  | `initNode()` | 初始化 | 加空项 + 全部字符集 |
- 调用链：输入 → `onTextChanged → charsets().filter → skin().showPopup/hidePopup`

## PasswordTextField
- 职责：密码文本域（继承 atlantafx `PasswordTextField`，实现多适配器）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | require | boolean | 是否必填 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `is/setRequire` | 必填标志 | |
  | 构造 `()` / `(String)` | 构造 | |
  | `setTipText(String)` | 提示 | 无 promptText 时同步 |
  | `isEmpty()` | 是否为空 | |
  | `validate()` | 校验 | 必填为空则 `ValidatorUtil.validFail` |
  | `initNode()` | 初始化 | `FlexAdapter.initNode` + `setPickOnBounds(true)` |
  | `setValue(Object)` / `static format` | 值 | |
  | `resize(double,double)` | 尺寸 | 计算并 `resizeNode` |
  | `text(String)` | 设文本 | `FXUtil.runWait` |
  | `createDefaultSkin()` | 皮肤 | `PasswordTextFieldSkin` |
- 调用链：`validate → require&&isEmpty → validFail`

## SearchTextField
- 职责：搜索文本域（含搜索历史弹窗）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | onSearch | EventHandler\<SearchEvent\> | 搜索触发事件处理器 |
  | onHistorySelected | EventHandler\<SearchEvent\> | 历史选中事件处理器 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `set/getHistoryPopup` | 弹窗 | 委托 skin |
  | `get/setOnSearch`、`get/setOnHistorySelected` | 回调 | |
  | `skin()` | 皮肤 | `SearchTextFieldSkin` |
  | `createDefaultSkin()` | 皮肤 | 匿名子类转发 `onSearch/onHistorySelected` 事件 |
- 内部类 `SearchEvent`：`SEARCH_TRIGGER_EVENT`/`SEARCH_HISTORY_SELECTED_EVENT`/`SEARCH_SETTING_EVENT` 类型；工厂 `searchTrigger/historySelected/searchSetting`；`getSource()` 返回文本。
- 调用链：`SearchTextFieldSkin.onSearch → onSearch.handle(SearchEvent.searchTrigger(text))`

## SelectTextFiled（泛型）
- 职责：可选择文本输入框（下拉候选，继承 LimitTextField）。
- 字段：无（状态在 skin）。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `skin()` / `createDefaultSkin()` | 皮肤 | `SelectTextFiledSkin<T>` |
  | `addItem/set/getItemList/clearItemList/getItemSize` | 候选列表 | 委托 skin |
  | `set/getLineHeight` | 行高 | |
  | `selectItem/selectIndex/getSelectedItem/selectedItemChanged` | 选中 | 委托 skin |
  | `clearSelection/selectFirstItem` | 选区 | |
  | `onTextChanged(String)` | 文本变化 | 命中/失焦/禁用时隐藏弹窗 |
  | `initNode()` | 初始化 | 文本变化延迟 50ms 触发 `onTextChanged` |
  | `destroy()` | 销毁 | 清空候选列表 |
- 调用链：文本变化 → 延迟 `onTextChanged → skin().hidePopup`；`selectItem → skin().selectItem → text(...)`

## DateTextField
- 职责：日期文本框。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | FORMAT | static final SimpleDateFormat | 默认格式 `yyyy-MM-dd` |
  | dateFormat | SimpleDateFormat | 自定义格式 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `get/setDateFormat` | 格式读写 | 同步 skin formatter |
  | `getValue()` | 取值 | 解析为 Date |
  | `formatValue()` | 格式化显示 | |
  | `skin()`/`createDefaultSkin()` | 皮肤 | `DateTextFieldSkin` |
  | `static format(Object)` | 格式化 | |
- 调用链：`getValue → FORMAT.parse`

## TimeTextField
- 职责：时间文本框。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | FORMAT | static final SimpleDateFormat | 默认格式 `HH:mm:ss` |
  | dateFormat | SimpleDateFormat | 自定义格式 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `get/setDateFormat` | 格式 | 同步 skin formatter |
  | `getValue()` | 取值 | 解析为 `Timestamp` |
  | `formatValue()` | 格式化显示 | |
  | `skin()`/`createDefaultSkin()` | 皮肤 | `TimeTextFieldSkin` |
  | `static format(Object)` | 格式化 | |
- 调用链：`getValue → FORMAT.parse → new Timestamp`

## DateTimeTextField
- 职责：日期时间文本框。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | FORMAT / FORMAT_1 | static final SimpleDateFormat | `yyyy-MM-dd HH:mm:ss` / 不含秒 |
  | FORMAT_T / FORMAT_T_1 | static final SimpleDateFormat | ISO `yyyy-MM-dd'T'HH:mm:ss` / 不含秒 |
  | dateFormat | SimpleDateFormat | 自定义格式 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `get/setDateFormat` | 格式 | 同步 skin formatter |
  | `getTimestamp()` | 取时间戳 | 依自定义/T/默认格式解析 |
  | `getValue()` | 取值 | 依格式解析为 Date |
  | `formatValue()` | 格式化显示 | `LocalDateTimeUtil.format` / `format.format` |
  | `static getFormat(Object)` | 选格式 | 依值形态（含 T、冒号数）选格式 |
  | `static format(Object)` | 格式化 | |
  | `skin()`/`createDefaultSkin()` | 皮肤 | `DateTimeTextFieldSkin` |
- 调用链：`formatValue → getFormat → LocalDateTimeUtil.format / format.format`

## ExampleTextField
- 职责：示例文本输入框。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `setExample(Object)` | 设示例 | 非空则 `setExampleText(toString())` |
  | `set/getExampleText` | 示例文本 | 委托 skin |
  | `skin()`/`createDefaultSkin()` | 皮肤 | `ExampleTextFieldSkin` |
- 调用链：`setExample → skin.setExampleText`

## EnlargeTextFiled
- 职责：可展开文本输入框。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `set/getEnlargeWidth`、`set/getEnlargeHeight` | 展开尺寸 | 委托 skin |
  | `skin()`/`createDefaultSkin()` | 皮肤 | `EnlargeTextFiledSkin` |
- 调用链：`setEnlargeWidth → skin.setEnlargeWidth`

## FilterTextField
- 职责：过滤文本输入框。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `is/setMatchCase`、`matchCasePropery` | 匹配大小写 | 委托 skin |
  | `is/setWholeWord`、`wholeWordPropery` | 全词匹配 | 委托 skin |
  | `skin()`/`createDefaultSkin()` | 皮肤 | `FilterTextFieldSkin` |
- 调用链：`setMatchCase → skin.setMatchCase`

## HighlightTextField
- 职责：高亮文本输入框。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `is/setRegex`、`regexPropery` | 正则匹配 | 委托 skin |
  | `is/setMatchCase`、`matchCasePropery` | 匹配大小写 | 委托 skin |
  | `is/setWholeWord`、`wholeWordPropery` | 全词匹配 | 委托 skin |
  | `skin()`/`createDefaultSkin()` | 皮肤 | `HighlightTextFieldSkin` |
- 调用链：`setRegex → skin.setRegex`

## MatchCaseTextField
- 职责：匹配大小写输入框。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `is/setMatchCase`、`matchCasePropery` | 匹配大小写 | 委托 skin |
  | `skin()`/`createDefaultSkin()` | 皮肤 | `MatchCaseTextFieldSkin` |
- 调用链：`setMatchCase → skin.setMatchCase`

## SaveFileTextField
- 职责：文件保存输入框。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `setInitFileName(String)` | 初始文件名 | 委托 skin |
  | `setExtension(FileExtensionFilter)` | 扩展名过滤 | 委托 skin |
  | `setOnSelectedFile(Consumer<File>)` | 选中回调 | 委托 skin |
  | `skin()`/`createDefaultSkin()` | 皮肤 | `SaveFileTextFieldSkin` |
  | `initNode()` | 初始化 | 不可编辑 |
- 调用链：`点击按钮 → SaveFileTextFieldSkin.onButtonClick → FileChooserHelper.save`

---

# 十三、toggle 包（4）

> 共性：均继承 `cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch`，仅含实例初始化块设置选中/未选中文本（`I18nResourceBundle.i18nString`）。

## EnableToggleSwitch
- 职责：启用切换开关。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 实例初始化块 | 设文本 | `setSelectedText("base.toggle.enable.selected")`；`setUnselectedText("base.toggle.enable.unselected")` |
- 调用链：`new EnableToggleSwitch() → 实例块`

## EnabledToggleSwitch
- 职责：已启用切换开关。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 实例初始化块 | 设文本 | `base.toggle.enabled.selected` / `base.toggle.enabled.unselected` |
- 调用链：`new EnabledToggleSwitch() → 实例块`

## MatchToggleSwitch
- 职责：匹配切换开关。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 实例初始化块 | 设文本 | `base.toggle.match.selected` / `base.toggle.match.unselected` |
- 调用链：`new MatchToggleSwitch() → 实例块`

## SwitchToggleSwitch
- 职责：切换开关。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 实例初始化块 | 设文本 | `base.toggle.switch.selected` / `base.toggle.switch.unselected` |
- 调用链：`new SwitchToggleSwitch() → 实例块`

---

# 十四、tray 包（3）

> 共性：继承 `cn.oyzh.fx.plus.tray.TrayItem`，构造时以国际化文本 + SVG 图标 + 动作创建托盘菜单项。

## DesktopTrayItem
- 职责：显示桌面托盘菜单项。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `DesktopTrayItem(Runnable)` | 构造 | `super(I18nHelper.open(), new DesktopSVGGlyph(), action)` |
- 调用链：`new DesktopTrayItem(action) → TrayItem(text, glyph, action)`

## QuitTrayItem
- 职责：退出托盘菜单项。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `QuitTrayItem(Runnable)` | 构造 | `super(I18nHelper.quit(), new QuitSVGGlyph(), action)` |
- 调用链：`new QuitTrayItem(action) → TrayItem(text, glyph, action)`

## SettingTrayItem
- 职责：设置托盘菜单项。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `SettingTrayItem(Runnable)` | 构造 | `super(I18nHelper.setting(), new SettingSVGGlyph(), action)` |
- 调用链：`new SettingTrayItem(action) → TrayItem(text, glyph, action)`

---

# 十五、tree.view 包（6）

## RichTreeItem（抽象）
- 职责：富功能树节点，支持位标志、过滤、排序、可见性与子节点管理。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | bitValue | BitSet（protected） | 位标志（默认 18 位） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `bitValue()` | 惰性位集 | 默认值 `0b000_000_000_001_100_101` |
  | `set/isVisible`、`set/isSorting`、`set/isSortable`、`set/isLoaded`、`set/isLoading`、`set/isFilterable`、`set/isSortAsc`、`isSortDesc` | 位标志读写 | 各占一位 |
  | `loadChild()` | 加载子节点（子类覆盖） | 空 |
  | `getTreeView()` | 返回 `RichTreeView` | 强转 |
  | `getChildren()` | 过滤后可见子节点 | `filtered` 依 `itemVisible` |
  | `unfilteredChildren()/unfilteredChildrenSize()/richChildren()` | 真实/富子节点 | 绕过过滤 |
  | `remove()/firstChild()/clearChild()/containsChild()` | 子节点操作 | `clearChild` 递归 destroy 并 `refresh` |
  | `setChild/addChild/removeChild/isChildEmpty` | 子节点增删 | `FXUtil.runWait` 中操作 un-filtered 列表 |
  | `doSort/sortAsc/sortDesc/sortChild(boolean)` | 排序 | asc 用 `RichTreeItem::compareTo`；desc 用 `Comparator.reverseOrder()` |
  | `doFilter([filter[,items]])` | 过滤 | `BackgroundService.submitFX` 中遍历子节点 `setVisible(filter.test)` 递归 |
  | `itemVisible([item])` | 有效可见性 | 自身可见或任一子节点可见 |
  | `compareTo(Object)` | 比较 | 按 `getValue().name()` |
  | `destroy()` | 销毁 | 置空 bitValue |
- 调用链：`RichTreeView.filter → root.doFilter → 递归 doFilter → setVisible`；`sortAsc → sortChild → children.sort`

## RichTreeItemBox
- 职责：富树节点内容盒（图标 + 富文本名称 + 额外信息）。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `()` / `(value,highlight,highlightMatchCase)` | 构造 | 后者调用 `init` |
  | `init(value,highlight,highlightMatchCase)` | 构建/更新子节点 | 首次创建 `graphic`/`name`/`extra`；否则按需更新 |
  | `get/setGraphic`、`get/setName`、`get/setExtra` | 子节点读写 | 按 id 查找 |
  | `layoutChildren()` | 布局 | 横向排布并计算宽度 |
  | `initNode()` | 初始化 | 清边距 |
- 调用链：`RichTreeCell.updateItem → box.init(value, highlight, matchCase)`

## RichTreeCell（泛型）
- 职责：富树单元格，按富文本/标准模式渲染，支持拖拽。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | dragNodeHandler | DragNodeHandler | 拖动处理 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 实例初始化块 | 光标 | `setCursor(HAND)` |
  | `updateItem(T,boolean)` | 渲染 | 富模式用 `RichTreeItemBox`；标准模式设图标与文本；对可拖拽节点初始化 `DragNodeHandler` |
- 调用链：`updateItem → value.graphic()/text()/graphicColor()`

## RichTreeItemFilter（抽象）
- 职责：树节点过滤器（`Predicate<RichTreeItem<?>>`）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | kw | String | 关键字 |
  | matchCase | boolean | 匹配大小写 |
  | wholeWord | boolean | 全词 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `get/setKw`、`is/setMatchCase`、`is/setWholeWord` | 属性 | |
- 调用链：`RichTreeItem.doFilter → filter.test(child)`

## RichTreeItemValue
- 职责：富树节点值（继承 `FXTreeItemValue`）。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | richMode | boolean | 是否富文本模式 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `is/setRichMode` | 富文本模式 | |
  | 构造 `()` / `(RichTreeItem<?>)` | 构造 | |
- 调用链：`RichTreeCell.updateItem → value.isRichMode()`

## RichTreeView
- 职责：富功能树视图，支持排序、过滤、高亮、字体适配。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | highlight | String（protected） | 高亮文本 |
  | highlightMatchCase | boolean（protected） | 高亮是否匹配大小写 |
  | itemFilter | RichTreeItemFilter（protected） | 节点过滤器 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `get/setItemFilter`、`is/setHighlightMatchCase`、`get/setHighlight` | 属性 | |
  | `getSelectedItem()` | 选中节点 | 强转 |
  | `sortAsc/sortDesc` | 排序 | 对根或选中节点排序后重新选中并刷新 |
  | `root()` / `root(TreeItem)` | 根节点 | 设置根并 `doFilter` |
  | `filter()` | 过滤 | 清选 → `doFilter` → `selectAndScroll` → `refresh` |
  | `selectedItemChanged(ChangeListener)` | 选中监听 | 委托 selectionModel |
  | `changeFont(Font)` | 字体适配 | 遍历节点更新图标尺寸 |
- 调用链：`filter() → root.doFilter → selectAndScroll + refresh`

---

# 十六、svg.label 包（21）

> 共性：继承 `cn.oyzh.fx.plus.controls.svg.SVGLabel`，构造中以 `setGraphic(new XxxSVGGlyph())` 绑定图标，`initNode()` 中 `setText(I18nHelper.xxx())`；多数有 `(String size)` 构造调用 `setSizeStr(size)`。

## AboutSVGLabel
- 职责：关于标签。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `AboutSVGLabel()` / `(String)` | 构造 | `setGraphic(new AboutSVGGlyph())` |
  | `initNode()` | 初始化 | `setText(I18nHelper.about())` |
- 调用链：`new AboutSVGLabel() → setGraphic → initNode → setText(about())`

## BoxSVGLabel
- 职责：盒状标签。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `BoxSVGLabel()` / `(String)` | 构造 | `setGraphic(new BoxSVGGlyph())`（无文本） |
- 调用链：`new BoxSVGLabel() → setGraphic(BoxSVGGlyph)`

## CollapseSVGLabel
- 职责：折叠标签。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `CollapseSVGLabel()` / `(String)` | 构造 | `setGraphic(new CollapseSVGGlyph())` |
  | `initNode()` | 初始化 | `setText(I18nHelper.collapse())` |
- 调用链：`initNode → setText(collapse())`

## CopySVGLabel
- 职责：复制标签。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `CopySVGLabel()` / `(String)` | 构造 | `setGraphic(new CopySVGGlyph())` |
  | `initNode()` | 初始化 | `setText(I18nHelper.copy())` |
- 调用链：`initNode → setText(copy())`

## DeleteSVGLabel
- 职责：删除标签。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `DeleteSVGLabel()` / `(String)` | 构造 | `setGraphic(new DeleteSVGGlyph())` |
  | `initNode()` | 初始化 | `setText(I18nHelper.delete())` |
- 调用链：`initNode → setText(delete())`

## EditSVGLabel
- 职责：编辑标签。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `EditSVGLabel()` / `(String)` | 构造 | `setGraphic(new EditSVGGlyph())` |
  | `initNode()` | 初始化 | `setText(I18nHelper.edit())` |
- 调用链：`initNode → setText(edit())`

## ExpandSVGLabel
- 职责：展开标签。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ExpandSVGLabel()` / `(String)` | 构造 | `setGraphic(new ExpendSVGGlyph())` |
  | `initNode()` | 初始化 | `setText(I18nHelper.expand())` |
- 调用链：`initNode → setText(expand())`

## FilterSVGLabel
- 职责：过滤标签。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `FilterSVGLabel()` / `(String)` | 构造 | `setGraphic(new FilterSVGGlyph())` |
  | `initNode()` | 初始化 | `setText(I18nHelper.filter())` |
- 调用链：`initNode → setText(filter())`

## KeySVGLabel
- 职责：密钥标签。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `KeySVGLabel()` / `(String)` | 构造 | `setGraphic(new key.KeySVGGlyph())` |
  | `initNode()` | 初始化 | `setText(I18nHelper.key1())` |
- 调用链：`initNode → setText(key1())`

## Layout1SVGLabel
- 职责：布局1标签。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `Layout1SVGLabel()` / `(String)` | 构造 | `setGraphic(new layout.Layout1SVGGlyph())` |
  | `initNode()` | 初始化 | `setText(I18nHelper.layout()+"1")` |
- 调用链：`initNode → setText(layout()+"1")`

## Layout2SVGLabel
- 职责：布局2标签。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `Layout2SVGLabel()` / `(String)` | 构造 | `setGraphic(new layout.Layout2SVGGlyph())` |
  | `initNode()` | 初始化 | `setText(I18nHelper.layout()+"2")` |
- 调用链：`initNode → setText(layout()+"2")`

## MessageSVGLabel
- 职责：消息标签。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MessageSVGLabel()` / `(String)` | 构造 | `setGraphic(new MessageSVGGlyph())` |
  | `initNode()` | 初始化 | `setText(I18nHelper.message())` |
- 调用链：`initNode → setText(message())`

## MigrationSVGLabel
- 职责：迁移标签。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `MigrationSVGLabel()` / `(String)` | 构造 | `setGraphic(new MigrationSVGGlyph())` |
  | `initNode()` | 初始化 | `setText(I18nHelper.migration())` |
- 调用链：`initNode → setText(migration())`

## QuitSVGLabel
- 职责：退出标签。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `QuitSVGLabel()` / `(String)` | 构造 | `setGraphic(new QuitSVGGlyph())` |
  | `initNode()` | 初始化 | `setText(I18nHelper.quit())` |
- 调用链：`initNode → setText(quit())`

## SearchSVGLabel
- 职责：搜索标签。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `SearchSVGLabel()` / `(String)` | 构造 | `super("/fx-svg/search.svg")`；尺寸经 `graphic().setSizeStr` |
  | `initNode()` | 初始化 | `setText(I18nHelper.search())` |
- 调用链：`initNode → setText(search())`

## SettingSVGLabel
- 职责：设置标签。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `SettingSVGLabel()` / `(String)` | 构造 | `setGraphic(new SettingSVGGlyph())` |
  | `initNode()` | 初始化 | `setText(I18nHelper.setting())` |
- 调用链：`initNode → setText(setting())`

## SnippetSVGLabel
- 职责：代码片段标签。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `SnippetSVGLabel()` / `(String)` | 构造 | `setGraphic(new SnippetSVGGlyph())` |
  | `initNode()` | 初始化 | `setText(I18nHelper.snippet())` |
- 调用链：`initNode → setText(snippet())`

## ThemeSVGLabel
- 职责：主题切换标签。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ThemeSVGLabel()` / `(String)` | 构造 | `setGraphic(new SVGGlyph("/fx-svg/contrast.svg"))`（无文本） |
- 调用链：`new ThemeSVGLabel() → setGraphic(contrast.svg)`

## ToolsSVGLabel
- 职责：工具标签。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `ToolsSVGLabel()` / `(String)` | 构造 | `setGraphic(new ToolsSVGGlyph())` |
  | `initNode()` | 初始化 | `setText(I18nHelper.tools())` |
- 调用链：`initNode → setText(tools())`

## TransportSVGLabel
- 职责：传输标签。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `TransportSVGLabel()` / `(String)` | 构造 | `setGraphic(new TransportSVGGlyph())` |
  | `initNode()` | 初始化 | `setText(I18nHelper.transport())` |
- 调用链：`initNode → setText(transport())`

## UploadDownloadSVGLabel
- 职责：上传下载标签。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `UploadDownloadSVGLabel()` / `(String)` | 构造 | `setGraphic(new UploadDownloadSVGGlyph())`（无文本） |
- 调用链：`new UploadDownloadSVGLabel() → setGraphic(UploadDownloadSVGGlyph)`

---

# 十七、svg.pane 包（4）

> 说明：均为可切换图标面板（`SVGPane`），通过 `setChild` 在两个 SVG 图标间切换，`changeFont(Font)` 时按当前状态重建图标以适配尺寸。
> 审查提示：`CollectSVGPane.collect()/unCollect()` 与 `SortSVGPane.asc()/desc()` 中设置的图标与语义相反（如 `collect()` 设的是 `UnCollectSVGGlyph`、`desc()` 设的是 `SortAscSVGGlyph`）。这是"图标表示点击后的动作/下一状态"的写法，若期望"图标表示当前状态"则属命名与图标映射错位，使用方需注意。

## CollectSVGPane
- 职责：收藏/未收藏图标切换面板。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `CollectSVGPane()` | 构造 | 默认 `unCollect()` |
  | `collect()` | 切换为已收藏 | `setChild(new UnCollectSVGGlyph(getSize()))` |
  | `unCollect()` | 切换为未收藏 | `setChild(new CollectSVGGlyph(getSize()))` |
  | `isCollect()` | 是否已收藏 | 子图标 Url 含 `star.svg` |
  | `setCollect(boolean)` | 设置状态 | `collect/unCollect` |
  | `changeFont(Font)` | 字体适配 | 按状态重建子图标 |
- 调用链：`setCollect → collect/unCollect → setChild`

## HiddenSVGPane
- 职责：显隐（睁眼/闭眼）图标切换面板。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `HiddenSVGPane()` | 构造 | 默认 `hidden()` |
  | `show()` | 显示 | `setChild(new EyeOpenSVGGlyph(getSize()))` |
  | `hidden()` | 隐藏 | `setChild(new EyeCloseSVGGlyph(getSize()))` |
  | `isHidden()` | 是否隐藏 | 子图标 Url 含 `eye-close.svg` |
  | `setHidden(boolean)` | 设置状态 | `hidden/show` |
  | `changeFont(Font)` | 字体适配 | 按状态重建 |
- 调用链：`setHidden → hidden/show → setChild`

## LayoutSVGPane
- 职责：布局1/布局2 图标切换面板。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `LayoutSVGPane()` | 构造 | 默认 `layout2()` |
  | `layout1()` | 布局1 | `setChild(new layout.Layout1SVGGlyph(getSize()))` |
  | `layout2()` | 布局2 | `setChild(new layout.Layout2SVGGlyph(getSize()))` |
  | `isLayout1()` | 是否布局1 | 子图标 Url 含 `layout1.svg` |
  | `changeFont(Font)` | 字体适配 | 按状态重建 |
- 调用链：`changeFont → isLayout1 → layout1/layout2`

## SortSVGPane
- 职责：升序/降序排序图标切换面板。
- 字段：无。
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `SortSVGPane()` | 构造 | 默认 `desc()` |
  | `desc()` | 降序 | `setChild(new SortAscSVGGlyph(getSize()))` |
  | `asc()` | 升序 | `setChild(new SortDescSVGGlyph(getSize()))` |
  | `isAsc()` | 是否升序 | 子图标 Url 含 `sort-descending.svg` |
  | `setAsc(boolean)` | 设置状态 | `asc/desc` |
  | `changeFont(Font)` | 字体适配 | 按状态重建 |
- 调用链：`setAsc → asc/desc → setChild`
