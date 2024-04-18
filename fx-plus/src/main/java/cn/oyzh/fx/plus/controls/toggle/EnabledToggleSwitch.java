package cn.oyzh.fx.plus.controls.toggle;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 *
 * @author oyzh
 * @since 2024/04/10
 */
public class EnabledToggleSwitch extends FXToggleSwitch  {

    {
        this.setSelectedText(I18nResourceBundle.i18nString("base.toggle.enabled.selected"));
        this.setUnselectedText(I18nResourceBundle.i18nString("base.toggle.enabled.unselected"));
    }

}
