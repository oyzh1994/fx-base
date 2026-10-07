package cn.oyzh.fx.plus.chooser;


import java.util.ArrayList;
import java.util.List;

/**
 * 文件扩展名过滤器
 *
 * @author oyzh
 * @since 2024-08-27
 */
public class FileExtensionFilter {

    /**
     * 描述
     */
    private String desc;

    /**
     * 获取描述
     *
     * @return 描述
     */
    public String getDesc() {
        return desc;
    }

    /**
     * 设置描述
     *
     * @param desc 描述
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * 扩展名列表
     */
    private List<String> extensions;

    /**
     * 设置扩展名列表
     *
     * @param extensions 扩展名列表
     */
    public void setExtensions(List<String> extensions) {
        this.extensions = extensions;
    }

    /**
     * 获取扩展名列表
     *
     * @return 扩展名列表
     */
    public List<String> getExtensions() {
        return extensions;
    }

    /**
     * 构建文件扩展名过滤器
     */
    public FileExtensionFilter() {

    }

    /**
     * 构建文件扩展名过滤器
     *
     * @param desc      描述
     * @param extension 扩展名
     */
    public FileExtensionFilter(String desc, String extension) {
        this.desc = desc;
        this.addExtension(extension);
    }

    /**
     * 构建文件扩展名过滤器
     *
     * @param desc       描述
     * @param extensions 扩展名
     */
    public FileExtensionFilter(String desc, String... extensions) {
        this.desc = desc;
        this.addExtensions(extensions);
    }

    /**
     * 添加扩展名
     *
     * @param extension 扩展名
     */
    public void addExtension(String extension) {
        this.addExtensions(extension);
    }

    /**
     * 批量添加扩展名
     *
     * @param extensions 扩展名
     */
    public void addExtensions(String... extensions) {
        if (this.extensions == null) {
            this.extensions = new ArrayList<>(8);
        }
        this.extensions.addAll(List.of(extensions));
    }

    /**
     * 获取首个扩展名
     *
     * @return 首个扩展名，扩展名列表为空时返回 null
     */
    public String getExtension() {
        if (this.extensions == null || this.extensions.isEmpty()) {
            return null;
        }
        return this.extensions.getFirst();
    }
}
