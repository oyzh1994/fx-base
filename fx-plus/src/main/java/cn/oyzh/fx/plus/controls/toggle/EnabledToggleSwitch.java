package cn.oyzh.fx.plus.controls.toggle;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 *
 * @author oyzh
 * @since 2024/04/10
 */
public class EnabledToggleSwitch extends FXToggleSwitch  {

    {
        this.setSelectedText(BaseResourceBundle.getBaseString("base.toggle.enabled.selected"));
        this.setUnselectedText(BaseResourceBundle.getBaseString("base.toggle.enabled.unselected"));
    }

}
