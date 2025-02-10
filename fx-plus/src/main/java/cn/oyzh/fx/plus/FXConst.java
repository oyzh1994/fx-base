package cn.oyzh.fx.plus;

import cn.oyzh.common.log.JulLog;
import javafx.application.Application;
import javafx.application.HostServices;

/**
 * fx常量
 *
 * @author oyzh
 * @since 2022/12/26
 */
public class FXConst {

    // /**
    //  * tab地址
    //  */
    // public final static String TAB_PATH = "/tabs/";

    /**
     * 页面地址
     */
    public final static String FXML_PATH = "/fxml/";

    // /**
    //  * 页面地址
    //  */
    // public final static String MODULE_PATH = "/module/";
    //
    /**
     * popup地址
     */
    public final static String POPUP_PATH = "/popups/";

    /**
     * 提示标题属性
     */
    public final static String TOOLTIP_PROP_KEY = "javafx.scene.control.Tooltip";

    /**
     * 应用实例
     */
    public static Application INSTANCE;

    /**
     * app图标
     */
    public final static String APP_ICON = "app.icon";

    /**
     * 获取app图标
     *
     * @return app图标
     */
    public static String appIcon() {
        return System.getProperty(APP_ICON);
    }

    /**
     * 设置app图标
     *
     * @param appIcon app图标
     */
    public static void appIcon(String appIcon) {
        JulLog.info("appIcon: {}", appIcon);
        System.setProperty(APP_ICON, appIcon);
    }

    /**
     * 获取host服务
     *
     * @return HostServices
     */
    public static HostServices getHostServices() {
        return INSTANCE == null ? null : INSTANCE.getHostServices();
    }
}
