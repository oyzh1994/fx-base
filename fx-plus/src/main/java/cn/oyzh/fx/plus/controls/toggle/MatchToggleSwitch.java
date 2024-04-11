package cn.oyzh.fx.plus.controls.toggle;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 *
 * @author oyzh
 * @since 2024/04/10
 */
public class MatchToggleSwitch extends FXToggleSwitch  {

    {
        this.setSelectedText(BaseResourceBundle.getBaseString("toggle.match.selected"));
        this.setUnselectedText(BaseResourceBundle.getBaseString("toggle.match.unselected"));
    }

}
