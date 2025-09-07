package cn.oyzh.fx.editor.test;

import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.file.FileUtil;
import com.alibaba.fastjson2.JSON;
import org.junit.Test;

import java.io.File;

public class CompressResourceTest {

    private String dir = "C:\\Users\\Administrator\\IdeaProjects\\fx-base\\fx-editor\\src\\main\\resources";

    @Test
    public void testCompressResource() throws Exception {
        FileUtil.getAllFiles(new File(dir), file -> {
            String fileName = file.getName();
            String suffix = FileNameUtil.getSuffix(fileName);
            if (FileNameUtil.isJsonType(suffix)) {
                String json = FileUtil.readUtf8String(file);
               String compressJson= JSON.toJSONString(json);
               FileUtil.writeUtf8String(compressJson, file);
            }
        });

    }
}
