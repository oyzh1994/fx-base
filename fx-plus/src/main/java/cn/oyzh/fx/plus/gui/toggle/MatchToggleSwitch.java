package cn.oyzh.fx.plus.gui.toggle;

import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 *
 * @author oyzh
 * @since 2024/04/10
 */
public class MatchToggleSwitch extends FXToggleSwitch {

    {
        this.setSelectedText(I18nResourceBundle.i18nString("base.toggle.match.selected"));
        this.setUnselectedText(I18nResourceBundle.i18nString("base.toggle.match.unselected"));
    }

}
