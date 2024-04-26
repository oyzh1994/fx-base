package cn.oyzh.fx.plus.search;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import lombok.Data;

import java.util.Objects;

/**
 * 搜索结果
 *
 * @author oyzh
 * @since 2023/11/24
 */
@Data
public class SearchResult {

    /**
     * 匹配总数
     */
    private int count;

    /**
     * 当前索引
     */
    private int index;

    /**
     * 匹配类型
     * 1. name 名称
     * 2. data 值
     * 3. all 名称及值
     */
    private String matchType;

    /**
     * 获取匹配类型文本
     *
     * @return 匹配类型文本
     */
    public String getMatchTypeText() {
        if (this.matchType == null) {
            return "";
        }
        return switch (this.matchType) {
            case "name":
                yield I18nResourceBundle.i18nString("base.name");
                // yield "名称";
            case "data":
                // yield "值";
                yield I18nResourceBundle.i18nString("base.value");
            case "all":
                // yield "名称、值";
                yield I18nResourceBundle.i18nString("base.name") + "、" + I18nResourceBundle.i18nString("base.value");
            default:
                yield "";
        };
    }

    /**
     * 是否匹配全部
     *
     * @return 结果
     */
    public boolean isMatchAll() {
        return Objects.equals(this.matchType, "all");
    }

    /**
     * 是否匹配名称
     *
     * @return 结果
     */
    public boolean isMatchName() {
        return Objects.equals(this.matchType, "name") || Objects.equals(this.matchType, "all");
    }

    /**
     * 是否匹配值
     *
     * @return 结果
     */
    public boolean isMatchData() {
        return Objects.equals(this.matchType, "data") || Objects.equals(this.matchType, "all");
    }
}
