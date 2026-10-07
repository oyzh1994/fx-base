# fx-plus 数据控件（table / tree / chart / list / media）

覆盖 table(6)、tree/table(2)、tree/view(4)、chart(2)、list(1)、media(1)，共 16 个正式类。跳过的整文件死代码：`table/FakerResizeTableColumn`、`tree/table/FXTreeTableView`。

## FXTableView<S>
- 职责：表格视图控件，继承 `TableView<S>`，支持列管理、行高/表头、双击复制、选择模式、销毁等。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `reorderable` | `boolean` | 是否可重排列 |
  | `ctrlSAction` | `Runnable` | Ctrl+S 动作 |
  | `font` | `Font` | 字体 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initEvenListener()`(protected) | 事件监听钩子 | 默认空，供子类覆盖 |
  | `getSelectCellData()` | 选中列数据 | `TableViewUtil.getSelectCellData` |
  | `initNode()` | 初始化 | `initEvenListener` + `FlexAdapter.super.initNode` |
  | `setReorderable/isReorderable` | 可重排 | |
  | `get/setCtrlSAction` / `onCtrl_S()` | Ctrl+S | 执行动作 |
  | `itemList()` | 数据列表 | `itemsProperty().get()` |
  | `setCopyCellDataOnDoubleClicked(boolean)/isCopyCellDataOnDoubleClicked()` | 双击复制 | 注册/移除 `TableViewUtil.copyCellDataOnDoubleClicked` 事件 |
  | `selectedItemChanged(ChangeListener<S>)` | 选中事件 | 监听选中项属性 |
  | `resize` / `resizeNode` | 尺寸 | `resizeNode` 对 `FlexAdapter` 列按 flex 计算宽 |
  | `getHeader()/getHeaderHeight()/setHeaderHeight(double)` | 表头 | 委托 `TableViewUtil`；setHeaderHeight 绑定列头高度或延迟重试 |
  | `refresh()` | 刷新 | `runLater(super::refresh)` |
  | `addColumn/setColumn/clearColumn` | 列管理 | 均走 `FXUtil.runWait` |
  | `setFont/getFont/setFontSize/setFontFamily/setFontWeight/changeFont` | 字体 | 用 `FontUtil` 派生 |
  | `changeTheme(ThemeStyle)` | 主题变更 | `super` 后 `refresh()` |
  | `destroyItems/destroyItemsOnRemoved/destroyColumn` | 销毁 | 对 `Destroyable` 元素/列调 `destroy` |
  | `showGraphicOnly()/showGraphicOnlyLater()` | 仅显示图标 | 遍历 FXTableColumn |
  | `setSelectionMode(SelectionMode)` | 选择模式 | `getSelectionModel().setSelectionMode` |
  | `destroy()` | 销毁 | 销毁元素/列 + `NodeDestroyUtil` |
- 调用链：`FXTableView.destroy → destroyItems/destroyColumn`；`changeTheme → refresh`

---

## FXTableColumn<S,T>
- 职责：表格列，继承 `TableColumn<S,T>`，管理单元格工厂、对齐、行高、值名称、仅图标等。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `VALUE_NAME_PROP` | `String`(常量) | 值名称属性键 `value_name` |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `()`/`(String)` | 创建 | |
  | `cellFactory()`(protected) | 单元格工厂 | 默认 `new FXTableCell<>()` |
  | `setCell/getCell` | 单元格 | 通过 `PropertiesUtil` 存于 tableView |
  | `setLineHeight/getLineHeight` | 行高 | 委托 cell |
  | `setAlignment(Pos)/getAlignment` | 对齐 | 委托 cell |
  | `setValueName(String)/getValueName` | 值名称 | `setCellValueFactory(new PropertyValueFactory(...))` |
  | `text(String)` | 设文本 | `runWait` 内 setText |
  | `setRealWidth(double)` | 实际宽度 | 可调整时 setPrefWidth |
  | `showGraphicOnly()` / `showGraphicOnlyLater()` | 仅显示图标 | 查找列头 `.label` 设 `GRAPHIC_ONLY`，监听列头变化 |
  | `destroy()` | 销毁 | `NodeDestroyUtil.destroyObject` |
- 调用链：`FXTableColumn.setValueName → PropertyValueFactory`；`showGraphicOnly → TableViewUtil.getHeaderColumn`

---

## FXTableCell<S,T>
- 职责：表格单元格，继承 `TableCell<S,T>`，支持行高、行数据获取与节点渲染。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `lineHeight` | `double` | 行高 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getLineHeight()/setLineHeight(double)` | 行高 | |
  | `getTableItem()` | 当前行数据 | `getTableRow().getItem()` |
  | `updateItem(T,boolean)` | 更新单元格 | 值相同直接返回；节点类型设 graphic，否则设文本；`initLineHeight` |
  | `initLineHeight()`(protected) | 初始化行高 | 设置所在行 min/max/pref 高 |
- 调用链：`FXTableCell.updateItem → initLineHeight → TableRow.setMinHeight`

---

## FXTableRow<T>
- 职责：表格行，继承 `TableRow<T>`，支持右键菜单。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | 注册右键菜单事件，有菜单项则 `showContextMenu`，否则 `clearContextMenu` |
- 调用链：`FXTableRow.initNode → showContextMenu`

---

## IconTableCell<S,T>
- 职责：图标表格单元格，继承 `FXTableCell`，按扩展名显示系统/SVG 图标。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `imageView` | `ImageView` | 复用的图片视图 |
  | `iconFunc` | `BiFunction<S,T,Object>` | 自定义图标函数 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `()`/`(BiFunction)` | 创建 | 可指定图标函数 |
  | `get/setIconFunc` | 图标函数 | |
  | `getIcon(S,T)`(私有) | 取图标 | 有函数用函数；否则按扩展名：Windows 用 `IconUtil.getSystemIcon`，否则 `IconUtil.getSVGIcon` |
  | `updateItem(T,boolean)` | 更新 | 空则清空；否则设文本 + 图标（Image 或 Node） |
- 调用链：`IconTableCell.getIcon → IconUtil.getSystemIcon/getSVGIcon`

---

## SingleRowTableView<S>
- 职责：单行表格视图，继承 `FXTableView`，使用固定最后一列弹性策略。
- 字段：无
- 方法：`initNode()` → `setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN)` 后 `super.initNode()`
- 调用链：`SingleRowTableView.initNode → FXTableView.initNode`

---

## FXTreeTableColumn<S,T>
- 职责：树形表格列，继承 `TreeTableColumn<S,T>`，支持 flex/主题/字体/提示/状态/布局/节点适配。
- 字段：无
- 方法：构造 `()`/`(String)`（无额外逻辑）
- 调用链：无

---

## FXTreeTableRow<T>
- 职责：树形表格行（抽象），继承 `TreeTableRow<T>`，用 graphic 显示内容。
- 字段：无（实例块设 `ContentDisplay.GRAPHIC_ONLY`）
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initGraphic()` | 抽象方法 | 由子类提供图形 |
  | `updateItem(T,boolean)` | 更新 | 空则清 graphic；否则 `setGraphic(initGraphic())` |
- 调用链：`FXTreeTableRow.updateItem → initGraphic`

---

## FXTreeView
- 职责：树形视图控件，继承 `TreeView`，支持右键菜单、F2 重命名、Delete 删除、单击/双击、滚动、展开收起等。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `dragContent` | `String` | 拖动内容标识（默认 `tree_view_drag`） |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `getDragContent()` | 拖动标识 | |
  | `initTreeView()/initRoot()/initEvenListener()`(protected) | 初始化 | 注册右键菜单、F2/DELETE 键、鼠标单击/双击事件 |
  | `reselect()` | 重选 | 记录→clear→select |
  | `selectItemChanged(Consumer<TreeItem<?>>)` | 选中事件 | `isIgnoreChanged()` 时忽略 |
  | `scrollTo(int)` / `scrollTo(TreeItem<?>)` | 滚动 | |
  | `selectAndScroll/positionItem` | 选中并滚动 | |
  | `isSelected(TreeItem<?>)` | 是否选中 | |
  | `changeTheme` | 主题变更 | `super` 后 `refresh` |
  | `initNode()` | 初始化 | flex 初始化 + `initTreeView` + `initRoot` |
  | `getSelectedItem()` | 选中项 | 强转 `FXTreeItem<?>` |
  | `expand()/collapse()/reload()` | 展开/收缩/重载 | `synchronized`，操作选中项 |
  | `resize` | 尺寸调整 | 通用模式 |
  | `getAllItem()` | 全部节点 | `TreeViewUtil.getAllItem` |
  | `destroy()` | 销毁 | `setRoot(null)` + `NodeDestroyUtil` |
- 调用链：`FXTreeView.initEvenListener → KeyListener.listenReleased / item.rename/delete`

---

## FXTreeItem<V extends FXTreeItemValue>
- 职责：富功能树节点（抽象），继承 `TreeItem<V>`，支持等待动画、子节点管理、展开/收缩、拖拽效果等。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `treeView` | `FXTreeView` | 所属树视图 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `(FXTreeView)` | 创建 | |
  | `set/getTreeView` | 树视图 | |
  | `getChildrenSize()` | 子节点数 | |
  | `stopWaiting()/startWaiting(Runnable[,autoClose|delay])/isWaiting()` | 等待动画 | 用 `itemGraphic()` 的 `SVGGlyph` 启停，`TaskManager.startDelay` 执行任务 |
  | `itemGraphic()` | 当前图标 | 优先 `getValue().graphic()`，否则 graphic |
  | `reExpanded()` | 重展开 | 收起再展开 |
  | `delete()/rename()/reloadChild()` | 钩子 | 默认空，供子类覆盖 |
  | `remove()` | 移除自身 | 从父节点移除 |
  | `firstChild/clearChild/containsChild/setChild(多个重载)/addChild/removeChild/isChildEmpty` | 子节点管理 | 均 `FXUtil.runWait` 操作 children |
  | `getDragEffect()/getDropEffect()` | 拖拽效果 | `DropShadow` |
  | `expend()/collapse()/collapseAll/expandAll` | 展开收缩 | |
  | `refresh()` | 刷新 | `treeView.refresh()` |
  | `onPrimarySingleClick()/onPrimaryDoubleClick()` | 点击钩子 | 默认空 |
  | `parent()/window()/isSelected()/clearSelection()` | 便捷 | |
  | `destroy()` | 销毁 | value.destroy + 解绑 + `NodeDestroyUtil` |
- 调用链：`FXTreeView 单击/双击 → item.onPrimarySingleClick/onPrimaryDoubleClick`；`startWaiting → SVGGlyph.startWaiting`

---

## FXTreeItemValue
- 职责：树节点值对象，承载节点名称/额外内容/图标，用弱引用避免内存泄漏。
- 字段：
  | 字段 | 类型 | 含义 |
  |---|---|---|
  | `graphic` | `WeakReference<SVGGlyph>` | 图形弱引用 |
  | `item` | `WeakReference<FXTreeItem<?>>` | 节点弱引用 |
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `graphic()/graphic(SVGGlyph)` | 图形读写 | |
  | `item()/构造(FXTreeItem)` | 节点 | |
  | `name()/extra()/extraColor()` | 名称/额外内容/颜色 | 默认为空，`extraColor` 返回主题前景色 |
  | `text()` | 显示文本 | name + extra 拼接 |
  | `graphicColor()` | 图标颜色 | 主题前景色 |
  | `destroy()` | 销毁 | 清空弱引用 |
- 调用链：`FXTreeItemValue.text → name/extra`

---

## FXTreeCell<T>
- 职责：树形单元格（抽象），继承 `TreeCell<T>`，空项时清空文本与图形。
- 字段：无
- 方法：`updateItem(T,boolean)` → 空时 `runWait` 清空 text/graphic
- 调用链：`FXTreeCell.updateItem → FXUtil.runWait`

---

## ChartHelper
- 职责：图表辅助工具，增改数据与裁剪。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `addData(Series, X, Y, Integer limit)` | 添加数据 | `runWait` 添加后 `clipData` |
  | `addOrUpdateData(Series, X, Y, Integer limit)` | 增或改 | 并行流查找 x，存在则改 y，否则添加并裁剪 |
  | `clipData(Series, Integer)`(私有) | 裁剪 | 超出 limit 移除头部 |
  | `initLegend(Chart)` | 图例 | 已废弃，改用 css |
- 调用链：`ChartHelper.addData → clipData`

---

## FXLineChart<X,Y>
- 职责：折线图控件，继承 `LineChart<X,Y>`，支持 flex/提示/字体/主题与数据系列管理。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `(Axis,Axis)` / `(Axis,Axis,ObservableList<Series>)` | 创建 | |
  | `addChartData(Series)` / `setChartData(Collection<Series>)` | 数据系列 | `runWait` 操作 `getData()` |
  | `getChartData(int)` | 取系列 | 越界返回 null |
  | `resize` | 尺寸调整 | 通用模式 |
- 调用链：`FXLineChart.addChartData → FXUtil.runWait → getData().add`

---

## FXListView<T>
- 职责：列表控件，继承 `ListView<T>`，支持选中事件、按索引选中、主题刷新与销毁。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | `initNode()` | 初始化 | hand 光标 + flex 初始化 |
  | `selectedItemChanged(ChangeListener<T>)` | 选中事件 | 包装，`isIgnoreChanged()` 时忽略 |
  | `selectIndex(int)` | 选中索引 | `getSelectionModel().select(index)` |
  | `resize` | 尺寸调整 | 通用模式 |
  | `changeTheme(ThemeStyle)` | 主题变更 | `super` 后 `refresh` |
  | `destroy()` | 销毁 | `NodeDestroyUtil.destroyObject` |
- 调用链：`FXListView.selectIndex → SelectionModel.select`

---

## FXMediaView
- 职责：媒体视图控件，继承 `MediaView`，支持按 url 播放、停止、释放。
- 字段：无
- 方法：
  | 方法 | 说明 | 关键逻辑/调用 |
  |---|---|---|
  | 构造 `()`/`(MediaPlayer)`/`(Media)`/`(String url)` | 创建 | |
  | `setUrl(String)/getUrl()` | 地址 | `setProp("url")` + 新建 `MediaPlayer(FXUtil.getMedia(url))` |
  | `initNode()` | 初始化 | 等比例、`pickOnBounds` |
  | `resize` | 尺寸调整 | 通用模式 |
  | `isResizable()` | 可调整 | `true` |
  | `play()/stop()/dispose()` | 播放控制 | 委托 mediaPlayer |
  | `destroy()` | 销毁 | `NodeDestroyUtil` + `removeNode` |
- 调用链：`FXMediaView.setUrl → FXUtil.getMedia → new MediaPlayer`

---
