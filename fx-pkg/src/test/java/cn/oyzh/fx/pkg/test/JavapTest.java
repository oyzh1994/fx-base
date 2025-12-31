package cn.oyzh.fx.pkg.test;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.system.RuntimeUtil;
import org.junit.Test;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author oyzh
 * @since 2025-12-03
 */
public class JavapTest {

    @Test
    public void test() {
        String dir = "/Users/oyzh/IdeaProjects/oyzh/easyshell/target/easyshell-1.1.70";
        Set<String> classes = new HashSet<String>();
        List<File> files = FileUtil.getAllFiles(dir);
        for (File file : files) {
            if (!file.isFile() || !file.getName().endsWith(".class")) {
                continue;
            }
            // String[] cmd = new String[]{"javap", file.getPath()};
            String[] cmd = new String[]{"javap", "-verbose", file.getPath()};
            String res = RuntimeUtil.execForStr(cmd);
            System.out.println(res);
            Set<String> set = JavapUtil.findClasses(res);
            classes.addAll(set);
        }

        System.out.println(classes);

    }
}
