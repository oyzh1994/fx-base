package cn.oyzh.fx.plus.domain;

import cn.oyzh.fx.common.sqlite.Column;
import cn.oyzh.fx.common.sqlite.PrimaryKey;
import cn.oyzh.fx.common.sqlite.Table;
import cn.oyzh.fx.common.util.ObjectComparator;
import cn.oyzh.fx.common.util.ObjectCopier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 树分组
 *
 * @author oyzh
 * @since 2023/6/16
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TreeGroup implements ObjectCopier<Object>, Comparable<TreeGroup>, Serializable {

    /**
     * 分组id
     */
    @Getter
    @Setter
    @Column
    @PrimaryKey
    private String gid;

    /**
     * 分组名称
     */
    @Getter
    @Setter
    @Column
    private String name;

    /**
     * 是否展开分组
     */
    @Getter
    @Setter
    @Column
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

    @Override
    public void copy(Object obj) {
        if (obj instanceof TreeGroup t1) {
            this.gid = t1.gid;
            this.name = t1.name;
            this.expand = t1.expand;
        }
    }
}
