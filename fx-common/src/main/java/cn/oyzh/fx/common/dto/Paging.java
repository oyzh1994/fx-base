package cn.oyzh.fx.common.dto;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;

/**
 * 分页信息
 *
 * @author oyzh
 * @since 2020/9/15
 */
public class Paging<T> {

    /**
     * 每页显示数量
     */
    @Getter
    @Accessors(fluent = true, chain = true)
    private int limit = 10;

    /**
     * 数据总数
     */
    @Getter
    @Accessors(fluent = true, chain = true)
    private int count;

    /**
     * 总页数
     */
    @Getter
    @Accessors(fluent = true, chain = true)
    private int countPage;

    /**
     * 当前页
     */
    @Getter
    @Accessors(fluent = true, chain = true)
    private int currentPage;

    /**
     * 数据列表
     */
    @Getter
    @Accessors(fluent = true, chain = true)
    private List<T> dataList;

    /**
     * 空数据实例
     */
    public static final Paging<Object> EMPTY = new Paging<>(Collections.emptyList());

    public Paging(List<T> dataList) {
        this.dataList(dataList);
    }

    public Paging(List<T> dataList, int limit) {
        if (limit <= 0) {
            limit = 10;
        }
        this.limit = limit;
        this.dataList(dataList);
    }

    public Paging(List<T> dataList, int limit, int count) {
        if (limit <= 0) {
            limit = 10;
        }
        this.limit = limit;
        this.count = count;
        this.dataList = dataList;
        if (this.count <= this.limit) {
            this.countPage = 1;
        } else if (this.count % this.limit == 0) {
            this.countPage = this.count / this.limit;
        } else {
            this.countPage = this.count / this.limit + 1;
        }
        this.currentPage = 0;
    }

    /**
     * 设置数据列表
     *
     * @param dataList 数据列表
     * @return 分页对象
     */
    public Paging<T> dataList(List<T> dataList) {
        if (dataList == null) {
            dataList = Collections.emptyList();
        }
        this.dataList = dataList;
        this.count = this.dataList.size();
        if (this.count <= this.limit) {
            this.countPage = 1;
        } else if (this.count % this.limit == 0) {
            this.countPage = this.count / this.limit;
        } else {
            this.countPage = this.count / this.limit + 1;
        }
        this.currentPage = 0;
        return this;
    }

    /**
     * 首页
     *
     * @return 分页内容
     */
    public List<T> first() {
        this.currentPage = 0;
        return this.pageData();
    }

    /**
     * 尾页
     *
     * @return 分页内容
     */
    public List<T> last() {
        this.currentPage = this.countPage - 1;
        return this.pageData();
    }

    /**
     * 上一页
     *
     * @return 分页内容
     */
    public List<T> prev() {
        if (this.currentPage != 0) {
            --this.currentPage;
        }
        return this.pageData();
    }

    /**
     * 下一页页
     *
     * @return 分页内容
     */
    public List<T> next() {
        if (this.currentPage < this.countPage - 1) {
            ++this.currentPage;
        }
        return this.pageData();
    }

    /**
     * 上一页页码
     *
     * @return 页码
     */
    public int prevPage() {
        if (this.currentPage != 0) {
            return this.currentPage - 1;
        }
        return this.currentPage;
    }

    /**
     * 下一页页码
     *
     * @return 页码
     */
    public int nextPage() {
        if (this.currentPage < this.countPage - 1) {
            return this.currentPage + 1;
        }
        return this.currentPage;
    }

    /**
     * 更新当前分页
     *
     * @param pageNo 页码
     */
    public void currentPage(int pageNo) {
        if (pageNo <= 0) {
            this.currentPage = 0;
        } else if (pageNo >= this.countPage) {
            this.currentPage = this.countPage - 1;
        } else {
            this.currentPage = pageNo;
        }
    }

    /**
     * 获取分页数据
     *
     * @param pageNo 页码
     * @return 分页内容
     */
    public List<T> page(int pageNo) {
        this.currentPage(pageNo);
        return this.pageData();
    }

    /**
     * 是否为空
     *
     * @return 结果
     */
    public boolean isEmpty() {
        return this.dataList == null || this.dataList.size() == 0;
    }

    /**
     * 格式化分页
     *
     * @param tpl 模板内容
     * @return 格式化后的内容
     */
    public String formatTpl(String tpl) {
        if (tpl == null) {
            return "";
        }
        tpl = tpl.replaceFirst("#count", String.valueOf(this.count));
        tpl = tpl.replaceFirst("#limit", String.valueOf(this.limit));
        if (!this.isEmpty()) {
            tpl = tpl.replaceFirst("#currentPage", String.valueOf(this.currentPage + 1));
            tpl = tpl.replaceFirst("#countPage", String.valueOf(this.countPage));
        } else {
            tpl = tpl.replaceFirst("#currentPage", "0");
            tpl = tpl.replaceFirst("#countPage", "0");
        }
        return tpl;
    }

    /**
     * 分页数据
     *
     * @return 分页数据
     */
    private List<T> pageData() {
        int start = this.limit * this.currentPage;
        int end = Math.min(start + this.limit, this.count);
        return this.dataList.subList(start, end);
    }
}
