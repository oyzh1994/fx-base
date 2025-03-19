package cn.oyzh.fx.plus.chooser;


import java.util.ArrayList;
import java.util.List;

/**
 * 文件扩展
 *
 * @author oyzh
 * @since 2024/08/27
 */
public class FileExtensionFilter {

    private String desc;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    private List<String> extensions;

    public void setExtensions(List<String> extensions) {
        this.extensions = extensions;
    }

    public List<String> getExtensions() {
        return extensions;
    }

    public FileExtensionFilter() {

    }

    public FileExtensionFilter(String desc, String extension) {
        this.desc = desc;
        this.addExtension(extension);
    }

    public FileExtensionFilter(String desc, String... extensions) {
        this.desc = desc;
        this.addExtensions(extensions);
    }

    public void addExtension(String extension) {
        this.addExtensions(extension);
    }

    public void addExtensions(String... extensions) {
        if (this.extensions == null) {
            this.extensions = new ArrayList<>(8);
        }
        this.extensions.addAll(List.of(extensions));
    }

    public String getExtension() {
        if (this.extensions == null || this.extensions.isEmpty()) {
            return null;
        }
        return this.extensions.getFirst();
    }
}
