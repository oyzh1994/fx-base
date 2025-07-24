package cn.oyzh.fx.gui.font;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.gui.text.field.SelectTextFiled;
import cn.oyzh.fx.plus.menu.FXContextMenu;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.text.Font;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 字体类型输入框，可搜索
 *
 * @author oyzh
 * @since 2025-06-08
 */
public class FontFamilyTextField extends SelectTextFiled<String> {

    {
        // 覆盖默认菜单
        this.setContextMenu(FXContextMenu.EMPTY);
        this.setTipText(I18nHelper.pleaseSelectFont());
    }

    @Override
    public FontFamilyTextFieldSkin skin() {
        return (FontFamilyTextFieldSkin) super.skin();
    }

    @Override
    protected FontFamilyTextFieldSkin createDefaultSkin() {
        if (this.getSkin() != null) {
            return (FontFamilyTextFieldSkin) this.getSkin();
        }
        return new FontFamilyTextFieldSkin(this);
    }

    @Override
    protected void onTextChanged(String newValue) {
        if (!this.isFocused()) {
            return;
        }
        if (this.skin().isTexting()) {
            this.skin().clearTexting();
            return;
        }
        List<String> fonts = Font.getFontNames();
        // 移除选区
        this.skin().clearSelection();
        // 隐藏弹窗
        if (StringUtil.isBlank(newValue)) {
            this.setItemList(fonts);
            this.skin().hidePopup();
            return;
        }
        // 过滤内容
        List<String> newList = fonts.stream()
                .filter(t -> StringUtil.containsIgnoreCase(t, newValue))
                .collect(Collectors.toList());
        // 设置内容
        this.setItemList(newList);
        // 内容为空，隐藏弹窗
        if (newList.isEmpty()) {
            this.skin().hidePopup();
        } else {
            this.skin().showPopup();
        }
    }

    @Override
    public void initNode() {
        super.initNode();
        this.setItemList(Font.getFontNames());
    }
}
