package cn.oyzh.fx.plus.controls.toggle;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;
import cn.oyzh.fx.plus.i18n.I18nManager;

/**
 *
 * @author oyzh
 * @since 2024/04/10
 */
public class EnableToggleSwitch extends FXToggleSwitch  {

    {
        this.setSelectedText(BaseResourceBundle.getBaseString("base.toggle.enable.selected"));
        this.setUnselectedText(BaseResourceBundle.getBaseString("base.toggle.enable.unselected"));
    }

}
