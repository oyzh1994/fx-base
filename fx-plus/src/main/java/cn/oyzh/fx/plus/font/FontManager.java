package cn.oyzh.fx.plus.font;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.fx.plus.window.StageAdapter;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 字体管理器
 *
 * @author oyzh
 * @since 2023/12/18
 */
@UtilityClass
public class FontManager {

    /**
     * 默认字体
     */
    public static Font defaultFont = Font.getDefault();

    /**
     * 当前字体
     */
    private static Font currentFont;

    /**
     * 获取当前字体
     *
     * @return 当前字体
     */
    public static Font currentFont() {
        if (currentFont == null) {
            return defaultFont;
        }
        return currentFont;
    }

    /**
     * 应用字体
     *
     * @param config 字体配置
     */
    public static void apply(FontConfig config) {
        if (config != null && StrUtil.isNotEmpty(config.getFamily()) && config.getWeight() != null && config.getSize() != null) {
            try {
                apply(Font.font(config.getFamily(), FontWeight.findByWeight(config.getWeight()), config.getSize()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 应用字体
     *
     * @param font 字体
     */
    public static void apply(Font font) {
        if (font == null) {
            return;
        }
        try {
            // 变更颜色
            List<StageAdapter> wrappers = StageManager.allStages();
            for (StageAdapter wrapper : wrappers) {
                applyCycle(wrapper.root(), font);
            }
            // 设置当前字体
            currentFont = font;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 应用字体，循环处理
     *
     * @param root 根节点
     * @param font 字体
     */
    private static void applyCycle(EventTarget root, Font font) {
        if (root instanceof FontAdapter adapter) {
            adapter.changeFont(font);
        }
        if (root instanceof Parent parent) {
            for (Node node : new CopyOnWriteArrayList<>(parent.getChildrenUnmodifiable())) {
                applyCycle(node, font);
            }
        } else if (root instanceof Popup popup) {
            for (Node node : new CopyOnWriteArrayList<>(popup.getContent())) {
                applyCycle(node, font);
            }
        } else if (root instanceof Stage stage) {
            applyCycle(stage.getScene(), font);
        } else if (root instanceof Window window) {
            applyCycle(window.getScene(), font);
        } else if (root instanceof Scene scene) {
            applyCycle(scene.getRoot(), font);
        }
    }
}
