package cn.oyzh.fx.plus.ext;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.ResourceUtil;
import cn.oyzh.fx.plus.controller.ControllerFactory;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import lombok.NonNull;

import java.io.IOException;

/**
 * fxml加载器扩展
 *
 * @author oyzh
 * @since 2023/1/3
 */
public class FXMLLoaderExt extends FXMLLoader {

    {
        this.setBuilderFactory(new JavaFXBuilderFactory());
        this.setControllerFactory(new ControllerFactory());
    }

    /**
     * 加载
     *
     * @param fxmlUrl  fxml地址
     * @return 内容
     */
    public <T> T load(@NonNull String fxmlUrl) {
        try {
            this.setLocation(ResourceUtil.getResource(fxmlUrl));
            this.setResources(I18nResourceBundle.INSTANCE);
            return super.load();
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.warn("load fxml fail, url:{}", fxmlUrl);
        }
        return null;
    }

    /**
     * 从地址加载
     *
     * @param fxmlUrl 资源地址
     * @return 内容
     */
    public static <T> T loadFromUrl(@NonNull String fxmlUrl) {
        return new FXMLLoaderExt().load(fxmlUrl);
    }
}
