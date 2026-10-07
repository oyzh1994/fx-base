package cn.oyzh.fx.editor.test;

import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.json.JSONUtil;
import org.junit.Test;

import java.io.File;

/**
 * 资源压缩测试，将指定目录下的 JSON 资源批量压缩为 gz.json 文件。
 *
 * @author oyzh
 * @since 2025-09-07
 */
public class CompressResourceTest {

    private final String dir = "C:\\Users\\Administrator\\IdeaProjects\\fx-base\\fx-editor\\src\\main\\resources";

    @Test
    public void testCompressResource() throws Exception {
        FileUtil.getAllFiles(new File(dir), file -> {
            String fileName = file.getName();
            String suffix = FileNameUtil.getSuffix(fileName);
            if (FileNameUtil.isJsonType(suffix)) {
                File file1 = new File(file.getParentFile(), fileName+".gz.json");
                String json = FileUtil.readUtf8String(file);
                String compressJson = JSONUtil.toCompress(json);
                //String compressJson = JsonFormatter.removeJsonFormatting(json);
                //System.out.println(compressJson);
                FileUtil.writeUtf8String(compressJson, file1);
            }
        });

    }
}
