package cn.oyzh.fx.plus.util;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import lombok.NonNull;
import lombok.experimental.UtilityClass;


/**
 * fx控件工具类
 *
 * @author oyzh
 * @since 2022/1/19
 */
@UtilityClass
public class ControlUtil {

    // /**
    //  * 初始化提示组件
    //  *
    //  * @param tooltip 提示组件
    //  */
    // public static void initTooltip(Tooltip tooltip) {
    //     String style = tooltip.getStyle() +
    //             ";-fx-background-color: #000000;" +
    //             "-fx-background-radius: 5;" +
    //             "-fx-border-radius: 5;" +
    //             "-fx-text-fill: #ffffff;" +
    //             "-fx-font-size: 10;";
    //     tooltip.setStyle(style);
    //     tooltip.setShowDelay(Duration.millis(500));
    //     tooltip.setOpacity(0.75);
    // }

    // /**
    //  * 获取提示组件
    //  *
    //  * @param target 组件
    //  * @return 提示组件
    //  */
    // public static Tooltip getTooltip(EventTarget target) {
    //     Tooltip tooltip = null;
    //     if (target instanceof Node node) {
    //         tooltip = (Tooltip) node.getProperties().get(FXConst.TOOLTIP_PROP_KEY);
    //     } else if (target instanceof Tab tab) {
    //         tooltip = (Tooltip) tab.getProperties().get(FXConst.TOOLTIP_PROP_KEY);
    //     }
    //     return tooltip;
    // }
    //
    // /**
    //  * 设置提示标题
    //  *
    //  * @param target 组件
    //  * @param text   提示文本
    //  */
    // public static void setTipText(@NonNull EventTarget target, String text) {
    //     Tooltip tooltip = getTooltip(target);
    //     if (StringUtil.isNotBlank(text)) {
    //         // if (tooltip != null) {
    //         //     tooltip.setText(text);
    //         // } else {
    //         //     tooltip = new Tooltip(text);
    //         //     initTooltip(tooltip);
    //         //     if (target instanceof Node node) {
    //         //         Tooltip.install(node, tooltip);
    //         //     } else if (target instanceof Tab tab) {
    //         //         tab.setTooltip(tooltip);
    //         //         tab.getProperties().put(FXConst.TOOLTIP_PROP_KEY, tooltip);
    //         //     }
    //         // }
    //         if (target instanceof Node node) {
    //             node.setOnMouseEntered(event -> {
    //                 Tooltip tip = new Tooltip(text);
    //                 initTooltip(tip);
    //                 Tooltip.install(node, tip);
    //             });
    //             node.setOnMouseExited(event -> {
    //                 Tooltip tip = getTooltip(node);
    //                 if (tip != null) {
    //                     Tooltip.uninstall(node, tip);
    //                 }
    //             });
    //         } else if (target instanceof Tab node) {
    //             node.selectedProperty().addListener((observable, oldValue, newValue) -> {
    //                 if (newValue) {
    //                     Tooltip tip = new Tooltip(text);
    //                     initTooltip(tip);
    //                     node.setTooltip(tip);
    //                 } else {
    //                     node.setTooltip(null);
    //                 }
    //             });
    //         }
    //     } else {
    //         if (target instanceof Node node) {
    //             Tooltip.uninstall(node, tooltip);
    //         } else if (target instanceof Tab tab) {
    //             tab.setTooltip(null);
    //         }
    //     }
    // }
    //
    // /**
    //  * 获取提示文本
    //  *
    //  * @param target 组件
    //  * @return 提示文本
    //  */
    // public static String getTipText(EventTarget target) {
    //     Tooltip tooltip = getTooltip(target);
    //     if (tooltip != null) {
    //         return tooltip.getText();
    //     }
    //     return null;
    // }

//    /**
//     * 配置清除按钮
//     *
//     * @param inputField 定制输入框
//     */
//    public static void setupClearButtonField(@NonNull CustomTextField inputField) {
//        ObjectProperty<Node> rightProperty = inputField.rightProperty();
//
//        inputField.getStyleClass().add("clearable-field"); //$NON-NLS-1$
//
//        Region clearButton = new Region();
//        clearButton.getStyleClass().add("graphic"); //$NON-NLS-1$
//        StackPane clearButtonPane = new StackPane(clearButton);
//        clearButtonPane.getStyleClass().add("clear-button"); //$NON-NLS-1$
//        clearButtonPane.setOpacity(0.0);
//        clearButtonPane.setCursor(Cursor.DEFAULT);
//        clearButtonPane.setOnMouseReleased(e -> inputField.clear());
//        clearButtonPane.managedProperty().bind(inputField.editableProperty());
//        clearButtonPane.visibleProperty().bind(inputField.editableProperty());
//
//        rightProperty.set(clearButtonPane);
//
//        final FadeTransition fader = new FadeTransition(Duration.millis(350), clearButtonPane);
//        fader.setCycleCount(1);
//
//        inputField.textProperty().addListener(new InvalidationListener() {
//
//            private boolean isButtonVisible = false;
//
//            @Override
//            public void invalidated(Observable arg0) {
//                String text = inputField.getText();
//                boolean isTextEmpty = text == null || text.isEmpty();
//
//                if (isTextEmpty == isButtonVisible) {
//                    isButtonVisible = !isTextEmpty;
//                    fadeTo(isButtonVisible);
//                }
//            }
//
//            private void fadeTo(boolean visible) {
//                fader.stop();
//                fader.setFromValue(visible ? 0.0 : 1.0);
//                fader.setToValue(visible ? 1.0 : 0.0);
//                fader.play();
//            }
//        });
//    }

    // /**
    //  * 初始化数字组件按钮
    //  *
    //  * @param type   类型
    //  * @param action 操作
    //  */
    // public static SVGGlyph initNumberButton(int type, Runnable action) {
    //     // 初始化增加、减少按钮
    //     SVGGlyph svgGlyph;
    //     if (type == 1) {
    //         svgGlyph = new SVGGlyph("/fx-plus/font/arrow-up-filling.svg");
    //     } else {
    //         svgGlyph = new SVGGlyph("/fx-plus/font/arrow-down-filling.svg");
    //     }
    //     if (action != null) {
    //         svgGlyph.setOnMousePrimaryClicked(e -> action.run());
    //     }
    //     svgGlyph.setSize(8);
    //     svgGlyph.setVisible(false);
    //     svgGlyph.setColor("#000000");
    //     svgGlyph.managedBindVisible();
    //     svgGlyph.setPadding(new Insets(0));
    //     return svgGlyph;
    // }
    //
    // /**
    //  * 初始化数字组件
    //  *
    //  * @param textField 文本域
    //  * @param incr      增加操作
    //  * @param decr      减少操作
    //  */
    // public static void stepNumberField(TextField textField, Runnable incr, Runnable decr) {
    //     // 过滤按键事件
    //     textField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
    //         if (event.getCode() == KeyCode.UP) {
    //             event.consume();
    //             incr.run();
    //         } else if (event.getCode() == KeyCode.DOWN) {
    //             event.consume();
    //             decr.run();
    //         }
    //     });
    // }

    /**
     * 初始化开关组件按钮
     *
     * @param type 类型
     */
    public static SVGGlyph initSwitchButton(int type) {
        // 初始化增加、减少按钮
        SVGGlyph svgGlyph;
        if (type == 1) {
            svgGlyph = new SVGGlyph("/fx-gui/font/switch-ON.svg");
            svgGlyph.setColor("#0e932e");
        } else {
            svgGlyph = new SVGGlyph("/fx-gui/font/switch-OFF.svg");
            svgGlyph.setColor("#000000");
        }
        svgGlyph.managedBindVisible();
        svgGlyph.setEnableWaiting(false);
        svgGlyph.setPadding(new Insets(0));
        return svgGlyph;
    }

//    /**
//     * 设置高度
//     *
//     * @param target 事件目标对象
//     * @param height 高度
//     */
//    public static void setHeight(EventTarget target, double height) {
//        if (target != null && Double.isNaN(height)) {
//            if (target instanceof Region region) {
//                region.setMinHeight(height);
//                region.setPrefHeight(height);
//                region.setMaxHeight(height);
//            } else if (target instanceof Node node) {
//                node.maxHeight(height);
//                node.minHeight(height);
//                node.prefHeight(height);
//            } else if (target instanceof Stage stage) {
//                stage.setHeight(height);
//                stage.setMaxHeight(height);
//                stage.setMinHeight(height);
//            } else if (target instanceof Window window) {
//                window.setHeight(height);
//            }
//        }
//    }

    /***
     * 生成指定宽度的边框
     * @param stroke 颜色
     * @param width 宽度
     * @return 指定宽度边框
     */
    public static Border strokeOfWidth(@NonNull Paint stroke, double width) {
        return new Border(new BorderStroke(stroke, BorderStrokeStyle.SOLID, null, new BorderWidths(width)));
    }

    /***
     * 生成指定宽度的边框，仅底部
     * @param stroke 颜色
     * @param width 宽度
     * @return 指定宽度边框
     */
    public static Border strokeOfWidthBottom(@NonNull Paint stroke, double width) {
        return new Border(new BorderStroke(stroke, BorderStrokeStyle.SOLID, null, new BorderWidths(0, 0, width, 0)));
    }

    /***
     * 生成较大宽度的边框
     * @param stroke 颜色
     * @return 中等宽度边框
     */
    public static Border strokeOfThick(@NonNull Paint stroke) {
        return new Border(new BorderStroke(stroke, BorderStrokeStyle.SOLID, null, BorderStroke.THICK));
    }

    /***
     * 生成中等宽度的边框
     * @param stroke 颜色
     * @return 中等宽度边框
     */
    public static Border strokeOfMedium(@NonNull Paint stroke) {
        return new Border(new BorderStroke(stroke, BorderStrokeStyle.SOLID, null, BorderStroke.MEDIUM));
    }

    /***
     * 生成默认宽度的边框
     * @param stroke 颜色
     * @return 中等宽度边框
     */
    public static Border borderOfThin(@NonNull Paint stroke) {
        return new Border(new BorderStroke(stroke, BorderStrokeStyle.SOLID, null, BorderStroke.THIN));
    }

    /**
     * 生成默认背景
     *
     * @param paint 颜色
     * @return 背景
     */
    public static Background background(Paint paint) {
        if (paint == null) {
            return null;
        }
        return new Background(new BackgroundFill(paint, null, null));
    }

    /**
     * 生成高亮背景
     *
     * @return 高亮背景
     */
    public static Background hilightBackground() {
        return background(Color.LIGHTBLUE);
    }

    /**
     * 取消选中
     *
     * @param text 文本组件
     */
    public static void deselect(Text text) {
        if (text != null) {
            text.setSelectionEnd(-1);
            text.setSelectionStart(-1);
        }
    }

    /**
     * 取消选中
     *
     * @param control 组件
     */
    public static void deselect(TextInputControl control) {
        if (control != null) {
            control.deselect();
        }
    }

    /**
     * 是否选中
     *
     * @param control 组件
     */
    public static boolean isSelect(@NonNull TextInputControl control) {
        IndexRange range = control.getSelection();
        return range != null && range.getLength() > 0;
    }

    /**
     * 获取组件宽度
     *
     * @param node 节点
     * @return 组件宽度
     */
    public static double boundedWidth(@NonNull Node node) {
        double min = node.minWidth(-1);
        double max = node.maxWidth(-1);
        return Math.min(Math.max(node.prefWidth(-1), min), Math.max(min, max));
    }

    /**
     * 获取组件高度
     *
     * @param node 节点
     * @return 组件高度
     */
    public static double boundedHeight(@NonNull Node node) {
        double min = node.minHeight(-1);
        double max = node.maxHeight(-1);
        return Math.min(Math.max(node.prefHeight(-1), min), Math.max(min, max));
    }

    /**
     * 获取背景颜色
     *
     * @param region 组件
     * @return 背景颜色
     */
    public static Paint backgroundFill(Region region) {
        if (region == null) {
            return null;
        }
        Background bg = region.getBackground();
        if (bg == null || bg.getFills() == null || bg.getFills().isEmpty()) {
            return null;
        }
        return bg.getFills().getFirst().getFill();
    }
}
