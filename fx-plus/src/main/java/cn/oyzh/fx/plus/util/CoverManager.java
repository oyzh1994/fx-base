package cn.oyzh.fx.plus.util;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.util.ClassUtil;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupAttribute;
import cn.oyzh.fx.plus.window.PopupManager;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * javafx的覆盖管理器
 *
 * @author oyzh
 * @since 2026/09/06
 */
public class CoverManager {

    private static String projectPath;

    public static String getProjectPath() {
        return projectPath;
    }

    public static void setProjectPath(String projectPath) {
        CoverManager.projectPath = projectPath;
    }

    /**
     * 获取项目类
     *
     * @return 结果
     * @throws Exception 异常
     */
    public static List<Class<?>> getClasses() throws Exception {
        String pkName = "";
        String f = projectPath;
        if (projectPath.contains("test-classes")) {
            f = projectPath.substring(0, f.indexOf("test-classes"));
        } else if (projectPath.contains("classes")) {
            f = projectPath.substring(0, f.indexOf("classes"));
        }
        if (f.startsWith("file:/")) {
            if (OSUtil.isWindows()) {
                f = f.substring(6);
            } else {
                f = f.substring(5);
            }
        }
        f += "classes";
        List<Class<?>> list = new ArrayList<>();
        ClassUtil.findClassesInDirectory(new File(f), pkName, list, (Predicate<Class<?>>) aClass -> {
            if (Modifier.isAbstract(aClass.getModifiers()) || !Modifier.isPublic(aClass.getModifiers())) {
                return false;
            }
            return true;
        });

        return list;
    }

    /**
     * tab检查
     *
     * @throws Exception 异常
     */
    public static void tabCheck() throws Exception {
        JulLog.info("tab check start");
        for (Class<?> aClass : getClasses()) {
            if (RichTab.class.isAssignableFrom(aClass)) {
                RichTab tab = (RichTab) ClassUtil.newInstance(aClass);
                if (tab == null) {
                    throw new RuntimeException("init tab:" + aClass + " fail");
                }
                JulLog.info(aClass + "=" + tab);
            }
        }
        JulLog.info("tab check finish");
    }

    /**
     * 页面检查
     *
     * @throws Exception 异常
     */
    public static void viewCheck() throws Exception {
        JulLog.info("stage check start");
        for (Class<?> aClass : getClasses()) {
            StageAttribute attribute = aClass.getAnnotation(StageAttribute.class);
            if (attribute == null) {
                continue;
            }
            StageAdapter adapter = StageManager.parseStage(aClass);
            if (adapter == null) {
                throw new RuntimeException("init stage:" + aClass + " fail");
            }
            JulLog.info(aClass + "=" + adapter);
        }
        JulLog.info("stage check finish");
    }

    /**
     * 弹窗检查
     *
     * @throws Exception 异常
     */
    public static void popupCheck() throws Exception {
        JulLog.info("popup check start");
        for (Class<?> aClass : getClasses()) {
            PopupAttribute attribute = aClass.getAnnotation(PopupAttribute.class);
            if (attribute == null) {
                continue;
            }
            PopupAdapter adapter = PopupManager.parsePopup(aClass);
            if (adapter == null) {
                throw new RuntimeException("init popup:" + aClass + " fail");
            }
            JulLog.info(aClass + "=" + adapter);
        }
        JulLog.info("popup check finish");
    }
}
