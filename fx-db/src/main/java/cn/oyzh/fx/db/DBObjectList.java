package cn.oyzh.fx.db;


import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据库对象列表基类，支持按新增、变更、删除状态筛选对象
 *
 * @author oyzh
 * @since 2026-09-01
 */
public abstract class DBObjectList<S extends DBObject> extends ArrayList<S> {

    /**
     * 普通类型
     */
    public static final byte TYPE_NORMAL = 0;

    /**
     * 删除类型
     */
    public static final byte TYPE_DELETED = 1;

    /**
     * 新增类型
     */
    public static final byte TYPE_CREATED = 2;

    /**
     * 变更类型
     */
    public static final byte TYPE_CHANGED = 3;

    // protected void valueList(List<S> list) {
    //     if (CollUtil.isNotEmpty(list)) {
    //         this.clear();
    //         this.addAll(list);
    //     }
    // }

    /**
     * 是否存在状态变化的对象
     *
     * @return 结果
     */
    public boolean isChanged() {
        for (S s : this) {
            if (StringUtil.isNotBlank(s.getStatus())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取新增对象列表
     *
     * @return 新增对象列表
     */
    public List<S> createdList() {
        if (this.isEmpty()) {
            return Collections.emptyList();
        }
        return this.stream().filter(DBObjectList::isCreated).collect(Collectors.toList());
    }

    /**
     * 获取变更对象列表
     *
     * @return 变更对象列表
     */
    public List<S> changedList() {
        if (this.isEmpty()) {
            return Collections.emptyList();
        }
        return this.stream().filter(DBObjectList::isChanged).collect(Collectors.toList());
    }

    /**
     * 获取删除对象列表
     *
     * @return 删除对象列表
     */
    public List<S> deletedList() {
        if (this.isEmpty()) {
            return Collections.emptyList();
        }
        return this.stream().filter(DBObjectList::isDeleted).collect(Collectors.toList());
    }

    /**
     * 获取正常对象列表
     *
     * @return 正常对象列表
     */
    public List<S> normalList() {
        if (this.isEmpty()) {
            return Collections.emptyList();
        }
        return this.stream().filter(DBObjectList::isNormal).collect(Collectors.toList());
    }

    /**
     * 按指定类型筛选对象列表
     *
     * @param types 类型，可传多个，为空时返回当前列表
     * @return 对象列表
     */
    public List<S> filterList(byte... types) {
        if (this.isEmpty()) {
            return Collections.emptyList();
        }
        if (types == null || types.length == 0) {
            return this;
        }
        List<S> list = new ArrayList<>();
        for (byte type : types) {
            List<S> list1 = null;
            if (type == TYPE_NORMAL) {
                list1 = this.normalList();
            } else if (type == TYPE_CHANGED) {
                list1 = this.changedList();
            } else if (type == TYPE_CREATED) {
                list1 = this.createdList();
            } else if (type == TYPE_DELETED) {
                list1 = this.deletedList();
            }
            if (CollectionUtil.isNotEmpty(list1)) {
                list.addAll(list1);
            }
        }
        return list;
    }

    @Override
    public boolean add(S s) {
        if (s != null) {
            return super.add(s);
        }
        return false;
    }

    /**
     * 移除对象
     *
     * @param s 对象
     */
    public void remove(S s) {
        super.remove(s);
    }

    /**
     * 是否包含对象
     *
     * @param s 对象
     * @return 结果
     */
    public boolean contains(S s) {
        return super.contains(s);
    }

    /**
     * 是否存在删除对象
     *
     * @return 结果
     */
    public boolean hasDeleted() {
        if (this.isEmpty()) {
            return false;
        }
        for (S s : this) {
            if (isDeleted(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否存在新增对象
     *
     * @return 结果
     */
    public boolean hasCreated() {
        if (!this.isEmpty()) {
            for (S s : this) {
                if (isCreated(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 是否存在变更对象
     *
     * @return 结果
     */
    public boolean hasChanged() {
        if (!this.isEmpty()) {
            for (S s : this) {
                if (isChanged(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 是否存在正常对象
     *
     * @return 结果
     */
    public boolean hasNormal() {
        if (!this.isEmpty()) {
            for (S s : this) {
                if (isNormal(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断对象是否删除
     *
     * @param status 对象
     * @return 结果
     */
    public static boolean isDeleted(DBObject status) {
        return status != null && status.isDeleted();
    }

    /**
     * 判断对象是否新增
     *
     * @param status 对象
     * @return 结果
     */
    public static boolean isCreated(DBObject status) {
        return status != null && !status.isDeleted() && status.isCreated();
    }

    /**
     * 判断对象是否变更
     *
     * @param status 对象
     * @return 结果
     */
    public static boolean isChanged(DBObject status) {
        return status != null && !status.isCreated() && !status.isDeleted() && status.isChanged();
    }

    /**
     * 判断对象是否正常
     *
     * @param status 对象
     * @return 结果
     */
    public static boolean isNormal(DBObject status) {
        if (status == null) {
            return false;
        }
        return !status.isChanged() && !status.isDeleted() && !status.isCreated();
    }
}

