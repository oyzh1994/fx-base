package cn.oyzh.fx.plus.domain;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;

import java.io.Serializable;

/**
 * app分组
 *
 * @author oyzh
 * @since 2023-06-16
 */
public class AppGroup implements ObjectCopier<Object>, Comparable<AppGroup>, Serializable {

    /**
     * 分组id
     */
    @Column
    @PrimaryKey
    private String gid;

    /**
     * 父id
     */
    @Column
    private String pid;

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
     * 是否展开分组
     *
     * @return 结果
     */
    public boolean isExpand() {
        return BooleanUtil.isTrue(expand);
    }

    /**
     * 构造应用分组对象。
     */
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

    /**
     * 构造应用分组对象。
     *
     * @param gid GID
     * @param name 名称
     * @param expand 是否展开
     */
    public AppGroup(String gid, String name, Boolean expand) {
        this.gid = gid;
        this.name = name;
        this.expand = expand;
    }

    /**
     * 获取GID。
     *
     * @return GID
     */
    public String getGid() {
        return gid;
    }

    /**
     * 设置GID。
     *
     * @param gid GID
     */
    public void setGid(String gid) {
        this.gid = gid;
    }

    /**
     * 获取名称。
     *
     * @return 名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置名称。
     *
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 设置展开。
     *
     * @param expand 是否展开
     */
    public void setExpand(boolean expand) {
        this.expand = expand;
    }

    /**
     * 获取PID。
     *
     * @return PID
     */
    public String getPid() {
        return pid;
    }

    /**
     * 设置PID。
     *
     * @param pid PID
     */
    public void setPid(String pid) {
        this.pid = pid;
    }
}
