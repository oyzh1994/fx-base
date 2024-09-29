package cn.oyzh.fx.common.store;

import cn.oyzh.fx.common.json.JSONUtil;
import cn.oyzh.fx.common.util.FileUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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
    public synchronized boolean init() {
        // 初始化路径
        this.storeFile = FileUtil.touch(this.filePath);
        return this.storeFile != null;
    }

    /**
     * 保存数据
     *
     * @param obj 对象
     * @return 结果
     */
    protected synchronized boolean saveData(Object obj) {
        try {
            if (obj != null) {
                String content = JSONUtil.toJsonStr(obj);
                return FileUtil.writeString(content, this.storeFile, this.charset) != null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 清空数据
     *
     * @return 结果
     */
    public synchronized boolean clear() {
        return this.saveData(null);
    }
}
