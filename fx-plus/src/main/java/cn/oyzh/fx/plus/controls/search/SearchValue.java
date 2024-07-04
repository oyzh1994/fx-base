package cn.oyzh.fx.plus.controls.search;

import javafx.scene.control.TreeItem;
import lombok.Data;

import java.util.Objects;

/**
 * 搜索值
 *
 * @author oyzh
 * @since 2023/11/24
 */
@Data
public class SearchValue {

    /**
     * 节点等级
     */
    private Integer level;

    /**
     * 节点
     */
    private TreeItem<?> item;

    /**
     * 匹配类型
     * 1. name 名称
     * 2. data 值
     * 3. all 名称及值
     */
    private String matchType;

    /**
     * 是否匹配数据
     *
     * @return 结果
     */
    public boolean isMatchData() {
        return Objects.equals(this.matchType, "data") || Objects.equals(this.matchType, "all");
    }
}
