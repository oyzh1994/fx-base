package cn.oyzh.fx.pkg.clip.clipper;

import cn.hutool.core.io.FileUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * jre裁剪工具
 *
 * @author oyzh
 * @since 2023/03/09
 */
@Slf4j
public class JreClipper extends BaseClipper {

    @Override
    protected void checkPath(String src, String dest) {
        // 检查src
        if (!FileUtil.exist(src) || !FileUtil.isDirectory(src)) {
            throw new RuntimeException("src " + src + " is invalid!");
        }

        // 检查dest
        if (FileUtil.exist(dest)) {
            if (!FileUtil.isDirectory(dest)) {
                throw new RuntimeException("dest " + dest + " exist but not dir!");
            }
            if (new File(src).getPath().equals(new File(dest).getPath())) {
                throw new RuntimeException("src and dest is same file!");
            }
        }
    }

    /**
     * 裁剪
     */
    public void clip(@NonNull JreClipConfig config) throws IOException {
        this.addExcludeFiles(config.getExcludeFiles());
        this.clip(config.getSrc(), config.getDest());
    }

    @Override
    public void clip(@NonNull String src, String dest) {
        this.checkPath(src, dest);
        log.info("clip start.");
        long start = System.currentTimeMillis();
        // 删除旧文件
        FileUtil.del(dest);
        FileUtil.copyContent(new File(src), new File(dest), false);
        List<File> fileList = FileUtil.loopFiles(dest);
        for (File file : fileList) {
            if (!this.filterName(file.getName())) {
                file.delete();
            }
        }
        long end = System.currentTimeMillis();
        log.info("clip end, used time: {}ms.", end - start);
    }
}
