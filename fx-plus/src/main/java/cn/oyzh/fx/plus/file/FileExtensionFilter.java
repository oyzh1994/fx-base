package cn.oyzh.fx.plus.file;

import cn.oyzh.fx.plus.i18n.I18nHelper;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件扩展
 *
 * @author oyzh
 * @since 2024/08/27
 */
@Getter
public class FileExtensionFilter {

    @Setter
    private String desc;

    private List<String> extensions;

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
            this.extensions = new ArrayList<>();
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
