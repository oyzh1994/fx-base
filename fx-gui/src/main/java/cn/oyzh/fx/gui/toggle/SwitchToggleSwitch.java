package cn.oyzh.fx.gui.toggle;

import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 *
 * @author oyzh
 * @since 2024/04/10
 */
public class SwitchToggleSwitch extends FXToggleSwitch {

    {
        this.setSelectedText(I18nResourceBundle.i18nString("base.toggle.switch.selected"));
        this.setUnselectedText(I18nResourceBundle.i18nString("base.toggle.switch.unselected"));
    }

}
