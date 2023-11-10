package cn.oyzh.fx.plus.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 树分组
 *
 * @author oyzh
 * @since 2023/6/16
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TreeGroup implements Comparable<TreeGroup> {

    /**
     * 分组id
     */
    @Getter
    @Setter
    private String gid;

    /**
     * 分组名称
     */
    @Getter
    @Setter
    private String name;

    /**
     * 是否展开分组
     */
    @Getter
    @Setter
    private Boolean expand;

    @Override
    public int compareTo(TreeGroup o) {
        if (o == null) {
            return 1;
        }
        return this.name.compareToIgnoreCase(o.getName());
    }

    /**
     * 是否展开分租
     *
     * @return 结果
     */
    public boolean isExpand() {
        return Boolean.TRUE == this.expand;
    }
}
