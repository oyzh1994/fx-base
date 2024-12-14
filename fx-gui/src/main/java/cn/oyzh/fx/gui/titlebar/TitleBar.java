package cn.oyzh.fx.gui.titlebar;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;

public class TitleBar extends HBox {

    public TitleBar(TitleBarConfig config) {
        if (config.getShowType() == 1) {
            List<Node> actions = config.getActions();
            if (actions == null) {
                actions = new ArrayList<>();
            }
            if (config.isShowMinimum()) {
                actions.add(new SVGGlyph("/fx-svg/titlebar/minimum.svg"));
            }
            if (config.isShowMaximum()) {
                actions.add(new SVGGlyph("/fx-svg/titlebar/maximum.svg"));
            }
            if (config.isShowClose()) {
                actions.add(new SVGGlyph("/fx-svg/titlebar/close.svg"));
            }
            this.getChildren().addAll(actions);
        }
    }

    private Stage stage() {
        Window window = this.window();
        if (window instanceof Stage stage) {
            return stage;
        }
        return null;
    }

    private Window window() {
        Scene scene = this.getScene();
        if (scene != null) {
            return scene.getWindow();
        }
        return null;
    }

    public void close() {
        Window window = this.window();
        if (window != null) {
            window.hide();
        } else {
            JulLog.warn("window is null!");
        }
    }

    public void maximum(boolean maximum) {
        Stage stage = this.stage();
        if (stage != null) {
            stage.setMaximized(maximum);
        } else {
            JulLog.warn("window is null!");
        }
    }

    public void minimum(boolean minimum) {
        Stage stage = this.stage();
        if (stage != null) {
            stage.setIconified(minimum);
        } else {
            JulLog.warn("window is null!");
        }
    }


}
