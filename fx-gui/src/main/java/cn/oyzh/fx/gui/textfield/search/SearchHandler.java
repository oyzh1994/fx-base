package cn.oyzh.fx.gui.textfield.search;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.plus.trees.RichTreeItem;
import cn.oyzh.fx.plus.trees.RichTreeItemValue;
import cn.oyzh.fx.plus.util.ControlUtil;
import cn.oyzh.fx.plus.util.TreeViewUtil;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索处理器
 *
 * @author oyzh
 * @since 2023/11/24
 */
@Deprecated
@Accessors(chain = true, fluent = true)
public abstract class SearchHandler {

    /**
     * 节点索引
     */
    protected Integer index;

    /**
     * 当前搜索节点
     */
    @Getter
    protected TreeItem<?> currentItem;

    /**
     * 当前搜索参数
     */
    @Getter
    protected SearchParam searchParam;

    /**
     * 当前搜索结果
     */
    protected SearchResult searchResult;

    /**
     * 清除搜索
     */
    public void clear() {
        this.resetSearch();
        this.currentItem = null;
        this.searchParam = null;
        this.searchResult = null;
    }

    /**
     * 重置搜索
     */
    protected void resetSearch() {
        this.index = 0;
        this.updateCurrentItem(null);
    }

    /**
     * 预搜索
     *
     * @param param 搜索参数
     */
    public void preSearch(SearchParam param) {
        // 初始化搜索参数
        if (this.searchParam == null || !this.searchParam.equalsTo(param)) {
            this.searchParam = param;
            this.resetSearch();
        }
        // 获取匹配节点
        List<SearchValue> matchItems = this.getMatchValues();
        // 更新搜索信息
        this.searchResult().setIndex(0);
        this.searchResult().setMatchType(null);
        this.searchResult().setCount(matchItems.size());
    }

    /**
     * 更新搜索结果
     */
    public void updateResult() {
        // 初始化搜索参数
        if (this.searchParam != null) {
            // 获取匹配节点
            List<SearchValue> matchItems = this.getMatchValues();
            // 更新搜索信息
            this.searchResult().setCount(matchItems.size());
            if (matchItems.isEmpty()) {
                this.searchResult().setIndex(0);
                this.searchResult().setMatchType(null);
            }
        }
    }

    /**
     * 搜索下一个
     *
     * @param param 搜索参数
     */
    public void searchNext(SearchParam param) {
        this.doSearch(param, "next");
    }

    /**
     * 搜索上一个
     *
     * @param param 搜索参数
     */
    public void searchPrev(SearchParam param) {
        this.doSearch(param, "prev");
    }

    /**
     * 执行搜索
     *
     * @param param  搜索参数
     * @param action 操作
     */
    protected void doSearch(SearchParam param, String action) {
        try {
            // 初始化搜索参数
            if (this.searchParam == null || !this.searchParam.equalsTo(param)) {
                this.searchParam = param;
                this.resetSearch();
            }
            // 获取匹配节点
            List<SearchValue> matchItems = this.getMatchValues();
            // 更新搜索信息
            this.searchResult().setCount(matchItems.size());
            // 内容为空
            if (CollectionUtil.isEmpty(matchItems)) {
                // 更新节点
                this.updateCurrentItem(null);
                return;
            }
            // 更新参数
            if (this.searchParam == null || !this.searchParam.equalsTo(param)) {
                this.searchParam = param;
                this.resetSearch();
            }
            // 树节点列表
            List<? extends TreeItem<?>> treeItems = matchItems.stream().map(SearchValue::getItem).toList();
            // 获取索引
            int index;
            if (this.currentItem == null || !treeItems.contains(this.currentItem)) {
                if (action.equals("prev")) {
                    index = matchItems.size() - 1;
                } else {
                    index = 0;
                }
            } else {
                index = treeItems.indexOf(this.currentItem);
                if (action.equals("prev")) {
                    --index;
                } else {
                    ++index;
                }
            }
            // 修正索引
            if (index < 0) {
                index = matchItems.size() - 1;
            } else if (index >= matchItems.size()) {
                index = 0;
            }
            // 处理值
            SearchValue searchValue = matchItems.get(index);
            // 应用值
            this.applyValue(searchValue, index);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 应用搜索值
     *
     * @param value  搜索值
     * @param index 索引
     */
    protected void applyValue(SearchValue value,int index) {
        // 获取节点
        TreeItem<?> item = value.getItem();
        // 展开其父节点
        TreeViewUtil.expandAll(item.getParent());
        // 更新节点
        this.updateCurrentItem(item);
        // 更新搜索结果及参数
        this.searchResult().setMatchType(value.getMatchType());
        this.searchResult().setIndex(index + 1);
    }

    /**
     * 更新当前节点
     *
     * @param item 当前节点
     */
    protected void updateCurrentItem(TreeItem<?> item) {
        // 取消文本选中
        if (this.currentItem instanceof RichTreeItem<?> treeItem) {
            RichTreeItemValue value = treeItem.getValue();
            ControlUtil.deselect(value.text());
        }
        // 更新当前节点
        this.currentItem = item;
        // 清除索引信息
        if (item == null) {
            this.index = 0;
            this.searchResult().setIndex(0);
            this.searchResult().setMatchType(null);
        }
    }

    /**
     * 获取当前搜索结果
     *
     * @return 搜索结果
     */
    public SearchResult searchResult() {
        if (this.searchResult == null) {
            this.searchResult = new SearchResult();
        }
        return this.searchResult;
    }

    /**
     * 获取匹配的数据列表
     *
     * @return 匹配的数据列表，扩展了属性
     */
    protected abstract List<SearchValue> getMatchValues();

    /**
     * 获取匹配的数据列表
     *
     * @param root 根节点
     * @return 匹配的数据列表，扩展了属性
     */
    protected List<SearchValue> getMatchValues(TreeItem<?> root) {
        // 匹配值
        List<SearchValue> values = new ArrayList<>();
        // 获取匹配的数据列表
        this.getMatchValues(root, values, 0);
        // 返回数据
        return values;
    }

    /**
     * 获取匹配的数据列表
     *
     * @param item   树节点
     * @param values 值列表
     * @param level  当前深度
     */
    protected void getMatchValues(TreeItem<?> item, List<SearchValue> values, int level) {
        if (item != null) {
            // 获取匹配类型
            String matchType = this.getMatchType(item);
            if (matchType != null) {
                // 生成对象
                SearchValue searchValue = new SearchValue();
                searchValue.setItem(item);
                searchValue.setLevel(level);
                searchValue.setMatchType(matchType);
                values.add(searchValue);
            }
            ObservableList<? extends TreeItem<?>> children = item.getChildren();
            if (children != null && !children.isEmpty()) {
                for (TreeItem<?> child : children) {
                    this.getMatchValues(child, values, level + 1);
                }
            }
        }
    }

    /**
     * 获取可见树节点
     *
     * @param treeView 树组件
     * @return 可见树节点
     */
    public static List<TreeItem<?>> getVisibleItems(@NonNull TreeView<?> treeView) {
        List<TreeItem<?>> result = new ArrayList<>(treeView.getExpandedItemCount());
        int startIndex = -1;
        for (int i = 0; i < treeView.getExpandedItemCount(); i++) {
            TreeItem<?> item = treeView.getTreeItem(i);
            if (item == treeView.getSelectionModel().getSelectedItem()) {
                startIndex = i;
            }
            if (startIndex > -1 && !item.isExpanded()) {
                startIndex = -1;
            }
            if (startIndex > -1) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * 执行分析
     */
    public abstract void doAnalyse();

    /**
     * 获取匹配类型
     *
     * @param item 节点
     * @return 匹配类型
     */
    public abstract String getMatchType(TreeItem<?> item);


}
