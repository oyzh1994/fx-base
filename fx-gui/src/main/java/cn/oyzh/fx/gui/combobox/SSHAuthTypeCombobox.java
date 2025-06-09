package cn.oyzh.fx.gui.combobox;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2025-03-18
 */
public class SSHAuthTypeCombobox extends FXComboBox<String> {

    {
        this.addItem(I18nHelper.password());
        this.addItem(I18nHelper.certificate());
        this.addItem("SSH Agent");
    }

    public boolean isPasswordAuth() {
        return this.getSelectedIndex() == 0;
    }

    public boolean isCertificateAuth() {
        return this.getSelectedIndex() == 1;
    }

    public boolean isSSHAgentAuth() {
        return this.getSelectedIndex() == 2;
    }

    public String getAuthType() {
        if (this.isSSHAgentAuth()) {
            return "sshAgent";
        }
        if (this.isCertificateAuth()) {
            return "certificate";
        }
        return "password";
    }
}
