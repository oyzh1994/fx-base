package cn.oyzh.fx.common.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 类工具类
 *
 * @author oyzh
 * @since 2023/05/18
 */
@UtilityClass
public class ClassUtil {

    /**
     * 获取类的所有接口
     *
     * @param clazz 类
     * @return 接口列表
     */
    public static List<Class<?>> getInterfaces(@NonNull Class<?> clazz) {
        Set<Class<?>> interfaces = new HashSet<>();
        do {
            getInterfaces(clazz, interfaces);
            clazz = clazz.getSuperclass();
        } while (clazz != Object.class);
        return new ArrayList<>(interfaces);
    }

    /**
     * 获取接口
     *
     * @param clazz      类
     * @param interfaces 接口列表
     */
    private static void getInterfaces(Class<?> clazz, Set<Class<?>> interfaces) {
        for (Class<?> aClass : clazz.getInterfaces()) {
            interfaces.add(aClass);
            getInterfaces(aClass, interfaces);
        }
    }

    public static List<Class<?>> scanClasses(String packageName, Predicate<Class<?>> predicate) throws ClassNotFoundException, IOException {
        List<Class<?>> classes = new ArrayList<>();
        String packagePath = packageName.replace(".", "/");
        Enumeration<URL> resources;
        resources = Thread.currentThread().getContextClassLoader().getResources(packagePath);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String protocol = resource.getProtocol();
            if ("file".equals(protocol)) {
                String filePath = URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8);
                findClassesInDirectory(new File(filePath), packageName, classes, predicate);
            } else if ("jar".equals(protocol)) {
                JarFile jar = ((JarURLConnection) resource.openConnection()).getJarFile();
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (name.charAt(0) != '/') {
                        continue;
                    }
                    if (name.startsWith(packagePath) && name.length() > packagePath.length() + 1) {
                        String className = name.substring(packagePath.length() + 1, name.lastIndexOf('.')).replace('/', '.');
                        Class<?> clazz = Class.forName(packageName + '.' + className);
                        if (predicate == null || predicate.test(clazz)) {
                            classes.add(clazz);
                        }
                    }
                }
            }
        }
        return classes;
    }

    private static void findClassesInDirectory(File directory, String packageName, List<Class<?>> classes, Predicate<Class<?>> predicate) throws ClassNotFoundException {
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }
        File[] files = directory.listFiles(file -> (file.isFile() && file.getName().endsWith(".class") || file.isDirectory()));
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                    Class<?> clazz = Class.forName(className);
                    if (predicate == null || predicate.test(clazz)) {
                        classes.add(clazz);
                    }
                } else {
                    findClassesInDirectory(file, packageName + "." + file.getName(), classes, predicate);
                }
            }
        }
    }
}
