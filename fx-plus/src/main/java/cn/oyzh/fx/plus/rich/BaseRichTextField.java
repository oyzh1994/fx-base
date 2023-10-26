package cn.oyzh.fx.plus.rich;

import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TextAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.handler.StateManager;
import javafx.scene.CacheHint;
import org.fxmisc.richtext.InlineCssTextField;

/**
 * @author oyzh
 * @since 2023/9/15
 */
public class BaseRichTextField extends InlineCssTextField implements  TextAdapter, TipAdapter, StateAdapter {

    {
        this.setCache(true);
        this.setCacheShape(true);
        this.setPickOnBounds(true);
        this.setFocusTraversable(false);
        this.setCacheHint(CacheHint.QUALITY);
    }

    @Override
    public void setTipText(String tipTitle) {
        TipAdapter.super.tipText(tipTitle);
    }

    @Override
    public String getTipText() {
        return TipAdapter.super.tipText();
    }


    @Override
    public void setStateManager(StateManager manager) {
        StateAdapter.super.stateManager(manager);
    }

    @Override
    public StateManager getStateManager() {
        return StateAdapter.super.stateManager();
    }

}
