package cn.oyzh.fx.plus.controls.text;

import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.MouseAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.FontWeight;
import lombok.NonNull;

/**
 * @author oyzh
 * @since 2020/10/29
 */
public class AccentLabel extends FlexLabel {

    {
        this.addClass("accent");
    }

    public AccentLabel() {
        super("");
    }

    public AccentLabel(String text) {
        super(text);
    }

    public AccentLabel(String text, Node graphic) {
        super(text, graphic);
    }
}
