package cn.oyzh.fx.plus.domain;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;

import java.io.Serializable;

/**
 * app分组
 *
 * @author oyzh
 * @since 2023/6/16
 */
public class AppGroup implements ObjectCopier<Object>, Comparable<AppGroup>, Serializable {

    /**
     * 分组id
     */
    @Column
    @PrimaryKey
    private String gid;

    /**
     * 分组名称
     */
    @Column
    private String name;

    /**
     * 是否展开分组
     */
    @Column
    private Boolean expand;

    @Override
    public int compareTo(AppGroup o) {
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

    public AppGroup() {
    }

    @Override
    public void copy(Object obj) {
        if (obj instanceof AppGroup t1) {
            this.gid = t1.gid;
            this.name = t1.name;
            this.expand = t1.expand;
        }
    }

    public AppGroup(String gid, String name, Boolean expand) {
        this.gid = gid;
        this.name = name;
        this.expand = expand;
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getExpand() {
        return expand;
    }

    public void setExpand(Boolean expand) {
        this.expand = expand;
    }
}
