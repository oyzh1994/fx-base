package cn.oyzh.fx.plus.util;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.ClassUtil;
import cn.oyzh.common.util.CoverUtil;
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
public class FXCoverChecker {

    private String projectPath;

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    /**
     * 获取项目类
     *
     * @return 结果
     * @throws Exception 异常
     */
    public List<Class<?>> getClasses() throws Exception {
        String pkName = "";
        String f = CoverUtil.getClassesPath(this.projectPath);
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
    public void tabCheck() throws Exception {
        JulLog.info("tab check start");
        for (Class<?> aClass : this.getClasses()) {
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
    public void viewCheck() throws Exception {
        JulLog.info("stage check start");
        for (Class<?> aClass : this.getClasses()) {
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
    public void popupCheck() throws Exception {
        JulLog.info("popup check start");
        for (Class<?> aClass : this.getClasses()) {
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
