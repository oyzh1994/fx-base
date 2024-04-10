package cn.oyzh.fx.plus.controls.toggle;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;
import cn.oyzh.fx.plus.i18n.I18nManager;

/**
 *
 * @author oyzh
 * @since 2024/04/10
 */
public class SwitchToggleSwitch extends FXToggleSwitch  {

    {
        this.setSelectedText(BaseResourceBundle.getBaseString("toggle.switch.selected"));
        this.setUnselectedText(BaseResourceBundle.getBaseString("toggle.switch.unselected"));
    }

}
