package cn.oyzh.fx.plus.controls.search;

import lombok.Data;

import java.util.Objects;

/**
 * 搜索参数
 *
 * @author oyzh
 * @since 2023/11/24
 */
@Data
public class SearchParam {

    /**
     * 搜索模式
     * 0 搜索模式
     * 1 过滤模式
     */
    private int mode;

    /**
     * 关键词
     */
    private String kw;

    /**
     * 全文匹配
     */
    private boolean fullMatch;

    /**
     * 匹配大小写
     */
    private boolean compareCase;

    /**
     * 是否匹配
     *
     * @param data 数据
     * @return 结果
     */
    public boolean isMatch(String data) {
        if (data == null) {
            return false;
        }
        // 处理大小写，如果不需要匹配大小写，则直接把数据转小写即可
        String kw = this.getKw();
        // 搜索词大于数据长度，直接返回false
        if (kw.length() > data.length()) {
            return false;
        }
        // 如果不匹配大小写，则全部转小写比较
        if (!this.compareCase) {
            kw = kw.toLowerCase();
            data = data.toLowerCase();
        }
        // 全文匹配
        if (this.fullMatch) {
            return data.equals(kw);
        }
        // 部分匹配
        return data.contains(kw);
    }

    /**
     * 比较另外一个搜索参数，判断参数是否一致
     *
     * @param param 搜索参数
     * @return 比较结果
     */
    public boolean equalsTo(Object param) {
        if (param == this) {
            return true;
        }
        if (param instanceof SearchParam param1) {
            if (!Objects.equals(this.kw, param1.kw)) {
                return false;
            }
            if (!Objects.equals(this.fullMatch, param1.fullMatch)) {
                return false;
            }
            if (this.mode != param1.mode) {
                return false;
            }
            return Objects.equals(this.compareCase, param1.compareCase);
        }
        return false;
    }

    public boolean isEmpty() {
        return this.kw == null || this.kw.isEmpty();
    }

    public boolean isFilterMode() {
        return this.mode == 1;
    }
}
