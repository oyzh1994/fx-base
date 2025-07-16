package cn.oyzh.fx.plus.controls.toggle;

import atlantafx.base.controls.ToggleSwitch;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import javafx.beans.value.ChangeListener;
import javafx.geometry.HorizontalDirection;
import javafx.geometry.Insets;
import javafx.scene.Cursor;

/**
 * 切换开关组件
 *
 * @author oyzh
 * @since 2023/12/19
 */
public class FXToggleSwitch extends ToggleSwitch implements NodeAdapter, LayoutAdapter, NodeGroup, TipAdapter, StateAdapter, FontAdapter {

    {
        NodeManager.init(this);
    }

    /**
     * 选中时显示的文字
     */
    private String selectedText;

    /**
     * 未选中时显示的文字
     */
    private String unselectedText;

    public String getSelectedText() {
        return selectedText;
    }

    public String getUnselectedText() {
        return unselectedText;
    }

    {
//        this.setCache(false);
        this.setCursor(Cursor.HAND);
        // 设置文字靠右
        this.setLabelPosition(HorizontalDirection.RIGHT);
        // 选中变化事件
        this.selectedChanged((observable, oldValue, t1) -> {
            if (t1) {
                if (StringUtil.isNotBlank(this.selectedText)) {
                    this.setText(this.selectedText);
                }
            } else {
                if (StringUtil.isNotBlank(this.unselectedText)) {
                    this.setText(this.unselectedText);
                }
            }
        });
    }

    /**
     * 设置选中文字
     *
     * @param selectedText 选中文字
     */
    public void setSelectedText(String selectedText) {
        this.selectedText = selectedText;
        if (this.isSelected()) {
            this.setText(this.selectedText);
        }
    }

    /**
     * 设置未选中文字
     *
     * @param unselectedText 未选中文字
     */
    public void setUnselectedText(String unselectedText) {
        this.unselectedText = unselectedText;
        if (!this.isSelected()) {
            this.setText(this.unselectedText);
        }
    }

    /**
     * 选中变化事件
     *
     * @param listener 监听器
     */
    public void selectedChanged(ChangeListener<Boolean> listener) {
        this.selectedProperty().addListener(listener);
//        this.selectedProperty().addListener(new WeakChangeListener<>(listener));
    }

    @Override
    public void initNode() {
        NodeAdapter.super.initNode();
        this.setPadding(Insets.EMPTY);
        this.setPickOnBounds(true);
    }

    @Override
    public void setTipText(String tipText) {
        TipAdapter.super.setTipText(tipText);
        if (this.getText() == null || this.getText().isEmpty()) {
            this.setText(tipText);
        }
    }
}
