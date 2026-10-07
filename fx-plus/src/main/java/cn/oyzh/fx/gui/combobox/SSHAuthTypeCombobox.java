package cn.oyzh.fx.gui.combobox;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;

/**
 * SSH 认证类型选择框
 *
 * @author oyzh
 * @since 2025-03-18
 */
public class SSHAuthTypeCombobox extends FXComboBox<String> {

    /**
     * 是否为密码认证
     *
     * @return 是否为密码认证
     */
    public boolean isPasswordAuth() {
        return this.getSelectedIndex() == 0;
    }

    /**
     * 是否为证书认证
     *
     * @return 是否为证书认证
     */
    public boolean isCertificateAuth() {
        return this.getSelectedIndex() == 1;
    }

    /**
     * 是否为 SSH Agent 认证
     *
     * @return 是否为 SSH Agent 认证
     */
    public boolean isSSHAgentAuth() {
        return this.getSelectedIndex() == 2;
    }

    /**
     * 获取认证类型
     *
     * @return 认证类型标识
     */
    public String getAuthType() {
        if (this.isSSHAgentAuth()) {
            return "sshAgent";
        }
        if (this.isCertificateAuth()) {
            return "certificate";
        }
        return "password";
    }

    @Override
    public void initNode() {
        super.initNode();
        this.addItem(I18nHelper.password());
        this.addItem(I18nHelper.publicKey());
        this.addItem("SSH Agent");
    }
}
