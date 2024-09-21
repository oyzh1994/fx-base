//package cn.oyzh.fx.common.dip;
//
//import java.io.File;
//import java.io.FileFilter;
//import java.io.IOException;
//import java.net.JarURLConnection;
//import java.net.URL;
//import java.net.URLDecoder;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.Enumeration;
//import java.util.List;
//import java.util.jar.JarEntry;
//import java.util.jar.JarFile;
//
//public class BeanClassUtil {
//
//    public static List<Class<?>> scanClasses(String packageName) throws ClassNotFoundException, IOException {
//        List<Class<?>> classes = new ArrayList<>();
//        String packagePath = packageName.replace(".", "/");
//        Enumeration<URL> resources;
//        resources = Thread.currentThread().getContextClassLoader().getResources(packagePath);
//        while (resources.hasMoreElements()) {
//            URL resource = resources.nextElement();
//            String protocol = resource.getProtocol();
//            if ("file".equals(protocol)) {
//                String filePath = URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8);
//                findClassesInDirectory(new File(filePath), packageName, classes);
//            } else if ("jar".equals(protocol)) {
//                JarFile jar = ((JarURLConnection) resource.openConnection()).getJarFile();
//                Enumeration<JarEntry> entries = jar.entries();
//                while (entries.hasMoreElements()) {
//                    JarEntry entry = entries.nextElement();
//                    String name = entry.getName();
//                    if (name.charAt(0) != '/') {
//                        continue;
//                    }
//                    if (name.startsWith(packagePath) && name.length() > packagePath.length() + 1) {
//                        String className = name.substring(packagePath.length() + 1, name.lastIndexOf('.')).replace('/', '.');
//                        classes.add(Class.forName(packageName + '.' + className));
//                    }
//                }
//            }
//        }
//        return classes;
//    }
//
//    private static void findClassesInDirectory(File directory, String packageName, List<Class<?>> classes) throws ClassNotFoundException {
//        if (!directory.exists() || !directory.isDirectory()) {
//            return;
//        }
//        File[] files = directory.listFiles(file -> (file.isFile() && file.getName().endsWith(".class") || file.isDirectory()));
//        if (files != null) {
//            for (File file : files) {
//                if (file.isFile()) {
//                    String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
//                    classes.add(Class.forName(className));
//                } else {
//                    findClassesInDirectory(file, packageName + "." + file.getName(), classes);
//                }
//            }
//        }
//    }
//}
