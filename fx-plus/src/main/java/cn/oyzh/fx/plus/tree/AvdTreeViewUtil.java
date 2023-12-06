package cn.oyzh.fx.plus.tree;

import cn.oyzh.fx.plus.controls.FlexVBox;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.util.NodeUtil;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import lombok.experimental.UtilityClass;

/**
 * @author oyzh
 * @since 2023/12/5
 */
@UtilityClass
public class AvdTreeViewUtil extends FlexVBox {

    public static SVGGlyph initRightIcon(AvdTreeItem<?> item){
        SVGGlyph glyph = new SVGGlyph("/fx-plus/font/arrow-right-filling.svg");
        glyph.setSizeStr("12,14");
        glyph.setColor(Color.GRAY);
        glyph.setOnMousePrimaryClicked(e -> item.expand());
        HBox.setMargin(glyph, new Insets(1, 5, 0, 3));
        return glyph;
    }

    public static SVGGlyph initDownIcon(AvdTreeItem<?> item){
        SVGGlyph glyph = new SVGGlyph("/fx-plus/font/arrow-down-filling.svg");
        glyph.setSizeStr("14,12");
        glyph.setColor(Color.GRAY);
        glyph.setOnMousePrimaryClicked(e -> item.collapse());
        HBox.setMargin(glyph, new Insets(1, 3, 0, 3));
        return glyph;
    }

    public static void updateHeight(Pane pane) {
        if (pane != null) {
            double size = 0;
            for (Node child : pane.getChildren()) {
                if (child.isVisible() && child.isManaged()) {
                    size += NodeUtil.getHeight(child);
                }
            }
            NodeUtil.setHeight(pane, size);
            if(pane.getParent() instanceof Pane pane1){
                updateHeight(pane1);
            }
        }
    }
}
