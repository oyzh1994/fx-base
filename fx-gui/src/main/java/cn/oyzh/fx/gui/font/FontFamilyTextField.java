package cn.oyzh.fx.gui.font;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.fx.plus.menu.FXContextMenu;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ChangeListener;
import javafx.scene.text.Font;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 字体类型输入框，可搜索
 *
 * @author oyzh
 * @since 2025-06-08
 */
public class FontFamilyTextField extends FXTextField {

    {
        // 覆盖默认菜单
        this.setContextMenu(FXContextMenu.EMPTY);
        this.setTipText(I18nHelper.pleaseSelectFont());
    }

    /**
     * 当前皮肤
     *
     * @return 皮肤
     */
    public FontFamilyTextFieldSkin skin() {
        FontFamilyTextFieldSkin skin = (FontFamilyTextFieldSkin) this.getSkin();
        if (skin == null) {
            skin = this.createDefaultSkin();
            this.setSkin(skin);
        }
        return skin;
    }

    @Override
    protected FontFamilyTextFieldSkin createDefaultSkin() {
        FontFamilyTextFieldSkin skin = new FontFamilyTextFieldSkin(this);
        List<String> fonts = Font.getFontNames();
        skin.setItemList(fonts);
        this.addTextChangeListener((observable, oldValue, newValue) -> {
            if (this.skin().isTexting()) {
                return;
            }
            // 移除选区
            this.skin().clearSelection();
            // 隐藏弹窗
            if (StringUtil.isBlank(newValue)) {
                this.skin().setItemList(fonts);
                this.skin().hidePopup();
                return;
            }
            // 过滤内容
            List<String> newList = fonts.stream()
                    .filter(t -> StringUtil.containsIgnoreCase(t, newValue))
                    .collect(Collectors.toList());
            // 设置内容
            this.skin().setItemList(newList);
            // 内容为空，隐藏弹窗
            if (newList.isEmpty()) {
                this.skin().hidePopup();
            } else {
                this.skin().showPopup();
            }
        });
        return skin;
    }

    public void select(String connect) {
        this.skin().selectItem(connect);
    }

    public String getSelectedItem() {
        return this.skin().getSelectedItem();
    }

    public void selectedItemChanged(ChangeListener<String> listener) {
        this.skin().selectItemChanged(listener);
    }

    @Override
    public boolean validate() {
        if (this.isRequire() && this.skin().getSelectedItem() == null) {
            return false;
        }
        return super.validate();
    }

    public String getValue() {
        return this.skin().getSelectedItem();
    }
}
