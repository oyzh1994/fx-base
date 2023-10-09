package cn.oyzh.fx.plus.rich;

import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TextAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
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
        TipAdapter.super._setTipText(tipTitle);
    }

    @Override
    public String getTipText() {
        return TipAdapter.super._getTipText();
    }

}
