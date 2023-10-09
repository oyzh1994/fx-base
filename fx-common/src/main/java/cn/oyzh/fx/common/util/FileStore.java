package cn.oyzh.fx.common.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.oyzh.fx.common.dto.Paging;
import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 文件储存
 *
 * @author oyzh
 * @since 2022/8/26
 */
@Data
@Accessors(chain = true, fluent = true)
public abstract class FileStore<T> {

    /**
     * 储存文件
     */
    private File storeFile;

    /**
     * 文件位置
     */
    private String filePath;

    /**
     * 数据字符集
     */
    private Charset charset = StandardCharsets.UTF_8;

    /**
     * 执行初始化
     *
     * @return 结果
     */
    protected synchronized boolean init() {
        // 初始化路径
        this.storeFile = FileUtil.touch(this.filePath);
        return this.storeFile != null;
    }

    /**
     * 保存数据
     *
     * @param list 数据列表
     * @return 结果
     */
    protected synchronized boolean save(List<T> list) {
        return this._save(list);
    }

    /**
     * 保存数据
     *
     * @param obj 数据
     * @return 结果
     */
    protected synchronized boolean save(T obj) {
        return this._save(obj);
    }

    /**
     * 保存业务
     *
     * @param obj 对象
     * @return 结果
     */
    private boolean _save(Object obj) {
        if (obj != null) {
            try {
                String content = JSON.toJSONString(obj);
                return FileUtil.writeString(content, this.storeFile, this.charset) != null;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 加载数据
     *
     * @return 数据列表
     */
    public abstract List<T> load();

    /**
     * 加载单个数据
     *
     * @return 单个书记
     */

    public T loadOne() {
        List<T> list = this.load();
        if (list != null && list.size() == 1) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 添加数据
     *
     * @param t 数据
     * @return 结果
     */
    public abstract boolean add(@NonNull T t);

    /**
     * 修改数据
     *
     * @param t 数据
     * @return 结果
     */
    public abstract boolean update(@NonNull T t);

    /**
     * 删除数据
     *
     * @param t 数据
     * @return 结果
     */
    public abstract boolean delete(@NonNull T t);

    /**
     * 查询，并转换为分页对象
     *
     * @param limit  每页数量
     * @param params 查询参数
     * @return 分页数据
     */
    public Paging<T> getPage(int limit, Map<String, Object> params) {
        // 加载数据
        List<T> list = this.load();
        // 分页对象
        Paging<T> paging = new Paging<>(list, limit);
        // 数据为空
        if (CollUtil.isEmpty(list)) {
            paging.dataList(new ArrayList<>());
        }
        return paging;
    }
}
