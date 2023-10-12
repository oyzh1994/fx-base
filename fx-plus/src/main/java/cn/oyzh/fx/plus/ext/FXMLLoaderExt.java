package cn.oyzh.fx.plus.ext;

import cn.oyzh.fx.plus.spring.SpringControllerFactory;
import cn.oyzh.fx.plus.util.ResourceUtil;
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
        this.setControllerFactory(new SpringControllerFactory());
    }

    /**
     * 加载
     *
     * @param fxmlUrl 资源地址
     * @return 内容
     */
    public <T> T load(@NonNull String fxmlUrl) {
        try {
            this.setLocation(ResourceUtil.getResource(fxmlUrl));
            return super.load();
        } catch (IOException e) {
            e.printStackTrace();
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
