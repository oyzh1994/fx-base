package cn.oyzh.fx.gui.titlebar;

import cn.oyzh.common.util.OSUtil;
import javafx.scene.Node;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TitleBarConfig {

    private String icon;

    private String title;

    private Node content;

    private boolean showClose;

    private List<Node> actions;

    private boolean showMaximum;

    private boolean showMinimum;

    public TitleBarConfig() {

    }

    public static TitleBarConfig ofPlatformCommon(String icon, String title) {
        TitleBarConfig config = new TitleBarConfig();
        if (OSUtil.isWindows()) {
            config.icon = icon;
        }
        config.title = title;
        config.showClose = true;
        config.showMaximum = true;
        config.showMinimum = true;
        return config;
    }


}
