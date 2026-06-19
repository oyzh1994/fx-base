package cn.oyzh.fx.gui.font;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.gui.text.field.SelectTextFiled;
import cn.oyzh.fx.plus.font.FontUtil;
import cn.oyzh.i18n.I18nHelper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 字体类型输入框，可搜索
 *
 * @author oyzh
 * @since 2025-06-08
 */
public class FontFamilyTextField extends SelectTextFiled<String> {

    @Override
    protected void onTextChanged(String newValue) {
        if (this.skin().isTexting()) {
            this.skin().clearTexting();
            return;
        }
        if (!this.isFocused()) {
            return;
        }
        List<String> fonts = FontUtil.getFamilies();
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
        // 覆盖默认菜单
        //this.setContextMenu(FXContextMenu.EMPTY);
        this.setTipText(I18nHelper.pleaseSelectFont());
        this.setItemList(FontUtil.getFamilies());
        super.initNode();
    }
}
