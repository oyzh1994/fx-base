package cn.oyzh.fx.gui.svg.path;

import cn.oyzh.fx.plus.controls.svg.SVGManager;
import javafx.scene.shape.SVGPath;

/**
 * @author oyzh
 * @since 2024-11-15
 */
public class HistorySVGPath extends SVGPath {

    {
        super.setContent(SVGManager.loadContent("/fx-svg/history.svg"));
    }
}
