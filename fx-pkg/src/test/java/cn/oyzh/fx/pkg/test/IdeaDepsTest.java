package cn.oyzh.fx.pkg.test;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.xml.XMLDocument;
import cn.oyzh.common.xml.XMLElement;
import cn.oyzh.common.xml.XMLReader;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author oyzh
 * @since 2025-12-03
 */
public class IdeaDepsTest {

    @Test
    public void test1() throws FileNotFoundException {
        File file = new File("/Users/oyzh/Desktop/easyshell_deps1.xml");
        FileInputStream fis = new FileInputStream(file);
        XMLReader xmlReader = new XMLReader();
        XMLDocument document = xmlReader.read(fis);
        XMLElement root = document.getRootElement();
        List<XMLElement> files = root.elements("file");

        Set<String> classes = new HashSet<>();
        for (XMLElement fEle : files) {
            String fPath = fEle.attributeValue("path");
            // if (!StringUtil.endWithAnyIgnoreCase(fPath, ".fxml", ".java", ".class", ".jar")) {
            //     continue;
            // }
            if (StringUtil.endWithAnyIgnoreCase(fPath, ".java", ".class")) {
                String className = this.handleClassName(fPath);
                classes.add(className);
            }
            // System.out.println(fPath);
            List<XMLElement> depsList = fEle.elements("dependency");
            if (depsList == null || depsList.isEmpty()) {
                continue;
            }
            for (XMLElement deps : depsList) {
                String dPath = deps.attributeValue("path");
                // if (!StringUtil.endWithAnyIgnoreCase(dPath, ".fxml", ".java", ".class", ".jar")) {
                //     continue;
                // }
                if (StringUtil.endWithAnyIgnoreCase(dPath, ".java", ".class")) {
                    String className = this.handleClassName(dPath);
                    classes.add(className);
                }
            }
        }
        xmlReader.close();

        List<String> list = classes.parallelStream().sorted(String::compareTo).toList();

        System.out.println(list);

    }

    private String handleClassName(String path) {
        try {
            if (path.startsWith("$PROJECT_DIR$")) {
                path = path.split("/src/main/java/")[1];
            } else if (path.startsWith("$USER_HOME$")) {
                path = path.split("!/")[1];
                path = path.substring(path.indexOf("/") + 1);
            } else if (path.startsWith("$MAVEN_REPOSITORY$")) {
                path = path.split("!/")[1];
            } else {
                throw new RuntimeException("unknown path: " + path);
            }
            path = path.replaceAll("/", ".")
                    .replace(".java", ".class");
            return path;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
