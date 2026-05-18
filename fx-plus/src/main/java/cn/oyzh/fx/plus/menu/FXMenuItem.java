package cn.oyzh.fx.plus.menu;

import cn.oyzh.common.object.Destroyable;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.font.FontUtil;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeDestroyUtil;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.text.Font;

/**
 * 菜单项
 *
 * @author oyzh
 * @since 2023/3/3
 */
public class FXMenuItem extends MenuItem implements FontAdapter, NodeAdapter, StateAdapter, ThemeAdapter, Destroyable {

    {
        NodeManager.init(this);
    }

    private ChangeListener<Boolean> disableListener = (observable, oldValue, newValue) -> {
        if (this.getGraphic() != null) {
            this.getGraphic().setDisable(newValue);
        }
    };

    {
        this.setStyle("-fx-padding: 0 0 0 0;");
        this.disableProperty().addListener(this.disableListener);
//        this.disableProperty().addListener((observable, oldValue, newValue) -> {
//            if (this.getGraphic() != null) {
//                this.getGraphic().setDisable(newValue);
//            }
//        });
    }

    public FXMenuItem() {
        super();
    }

    public FXMenuItem(Node graphic, String text, Runnable action) {
        if (text != null) {
            super.setText(text);
        }
        if (graphic != null) {
            super.setGraphic(graphic);
        }
        // 设置操作代理
        if (action != null) {
            this.setOnAction(event -> action.run());
        }
    }

    /**
     * 获取宽度
     *
     * @return 宽度
     */
    public double getWidth() {
        // String str = this.getText();
        // Node graphic = this.getGraphic();
        // double w = FontUtil.stringWidth(str);
        // if (graphic != null) {
        //     w += NodeUtil.getWidth(graphic);
        // }
        // KeyCombination combination = this.getAccelerator();
        // if (combination != null) {
        //     w = w + 60 + FontUtil.stringWidth(combination.getDisplayText());
        //     System.out.println(w + "====");
        // }
        // return w;
        return getWidth(this);
    }

//    /**
//     * 使用svg和文字来生成菜单项
//     *
//     * @param text   文字
//     * @param glyph  svg内容
//     * @param action 执行业务
//     * @return 菜单项
//     */
//    @Deprecated
//    public static FXMenuItem newItem(String text, SVGGlyph glyph, Runnable action) {
//        // 生成标签
//        SVGLabel label = new SVGLabel(text, glyph);
//        // 设置边距
//        label.setPadding(Insets.EMPTY);
//        // 计算宽度
//        double w = FontUtil.textWidth(text);
//        if (glyph != null) {
//            w += glyph.getWidth() + 25;
//        }
//        // 设置宽度
//        label.setMaxWidth(w);
//        label.setMinWidth(w);
//        label.setPrefWidth(w);
//        // 生成菜单项
//        return new FXMenuItem(label, null, action);
//    }

//    /**
//     * 使用文字来生成菜单项
//     *
//     * @param text   文字
//     * @param action 执行业务
//     * @return 菜单项
//     */
//    @Deprecated
//    public static FXMenuItem newItem(String text, Runnable action) {
//        return new FXMenuItem(null, text, action);
//    }

    @Override
    public void initNode() {
        this.setMnemonicParsing(false);
        NodeAdapter.super.initNode();
    }

    /**
     * 获取宽度
     *
     * @return 宽度
     */
    public static double getWidth(MenuItem item) {
        String str = item.getText();
        Node graphic = item.getGraphic();
        Font font = FontUtil.getFont(item);
        double w = FontUtil.textWidth(str, font);
        if (graphic != null) {
            w += NodeUtil.getWidth(graphic);
        }
        KeyCombination combination = item.getAccelerator();
        if (combination != null) {
            w = w + 20 + FontUtil.stringWidth(combination.getDisplayText(), font);
        }
        w += 80;
        return w;
    }

    @Override
    public void destroy() {
        this.disableProperty().removeListener(this.disableListener);
        NodeDestroyUtil.destroyObject(this);
    }
}
