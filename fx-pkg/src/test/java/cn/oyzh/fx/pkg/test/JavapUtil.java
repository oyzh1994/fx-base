package cn.oyzh.fx.pkg.test;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author oyzh
 * @since 2025-12-03
 */
public class JavapUtil {

    public static Set<String> findClasses(String javap) {
        Set<String> classes = new HashSet<String>();
        javap.lines().forEach(line -> {
            if (line.startsWith("class ")) {
                handleClassLine(line, classes);
            }
        });
        return classes;
    }

    private static void handleClassLine(String line, Set<String> classes) {
        String[] arr = line.split(" ");
        boolean isClass = false;
        boolean isExtends = false;
        boolean isImplements = false;
        for (String s : arr) {
            if (s.equals("class")) {
                isClass = true;
                continue;
            }
            if (s.equals("extends")) {
                isExtends = true;
                continue;
            }
            if (s.equals("implements")) {
                isImplements = true;
                continue;
            }
            if (isClass || isExtends) {
                classes.addAll(parseClass(s));
                continue;
            }
            if (isImplements) {
                String[] arr1 = s.split(",");
                for (String s1 : arr1) {
                    classes.addAll(parseClass(s1));
                }
                continue;
            }
        }
    }

    private static Set<String> parseClass(String classItem) {
        Set<String> classes = new HashSet<>();
        classes.add(classItem);
        return classes;
    }
}
