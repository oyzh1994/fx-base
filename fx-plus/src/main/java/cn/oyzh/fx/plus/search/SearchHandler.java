package cn.oyzh.fx.plus.search;

import cn.oyzh.fx.plus.trees.RichTreeItem;
import cn.oyzh.fx.plus.trees.RichTreeItemValue;
import cn.oyzh.fx.plus.util.ControlUtil;
import cn.oyzh.fx.plus.util.TreeViewUtil;
import javafx.scene.control.TreeItem;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 搜索处理器
 *
 * @author oyzh
 * @since 2023/11/24
 */
@Accessors(chain = true, fluent = true)
public abstract class SearchHandler {

    /**
     * 节点索引
     */
    protected Integer index;

    /**
     * 最后操作
     */
    protected String lastAction;

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
        this.lastAction = null;
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
            if (matchItems.isEmpty()) {
                // 更新节点
                this.updateCurrentItem(null);
                return;
            }
            // 操作不一致，更新索引
            if (this.lastAction != null && !Objects.equals(action, this.lastAction)) {
                this.index = "next".equals(this.lastAction) ? this.index - 2 : this.index + 2;
            }
            // 重置索引位置
            if (this.index >= matchItems.size()) {
                this.index = 0;
            } else if (this.index < 0) {
                this.index = matchItems.size() - 1;
            }
            // 数据排序
            matchItems.sort(Comparator.comparing(SearchValue::getLevel));
            // 获取索引数据
            SearchValue searchValue = matchItems.get("next".equals(action) ? this.index++ : this.index--);
            // 应用搜索值
            this.applyValue(searchValue, action);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 应用搜索值
     *
     * @param value  搜索值
     * @param action 操作
     */
    protected void applyValue(SearchValue value, String action) {
        // 获取节点
        TreeItem<?> item = value.getItem();
        // 展开其父节点
        TreeViewUtil.expandAll(item.getParent());
        // 更新节点
        this.updateCurrentItem(item);
        // 更新搜索结果及参数
        this.searchResult().setMatchType(value.getMatchType());
        this.searchResult().setIndex("next".equals(action) ? this.index : this.index + 2);
        this.lastAction = action;
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
     * 获取匹配的节点列表
     *
     * @return 匹配的节点列表，扩展了属性
     */
    protected abstract List<SearchValue> getMatchValues();

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
