package cn.oyzh.fx.plus.information;

import cn.oyzh.fx.gui.svg.glyph.alert.ErrorSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.alert.QuestionSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.alert.WarningSVGGlyph;
import cn.oyzh.fx.plus.controls.FXHeaderBar;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.button.FXButton;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.font.FontUtil;
import cn.oyzh.fx.plus.util.ScreenUtil;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.i18n.I18nHelper;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author oyzh
 * @since 2026-01-30
 */
public class AlertStage extends Stage implements StageAdapter {

    /**
     * 结果
     */
    private Button result;

    /**
     * 内容
     */
    private FXLabel content;

    /**
     * 图标
     */
    private SVGGlyph graphic;

    /**
     * 按钮列表
     */
    private List<Button> buttons;

    /**
     * 提示类型
     */
    private Alert.AlertType type;

    private static final Insets DEFAULT_MARGIN = new Insets(10, 0, 0, 10);

    private static final Insets BUTTON_DEFAULT_MARGIN = new Insets(30, 10, 0, 0);

    public AlertStage(Alert.AlertType type) {
        this(type, "");
    }

    public AlertStage(Alert.AlertType type, String content) {
        this(type, content, new ArrayList<>());
    }

    public AlertStage(Alert.AlertType type, String content, List<Button> buttons) {
        this.type = type;
        this.initModality(Modality.APPLICATION_MODAL);
        this.initStyle(FXStageStyle.EXTENDED.toStageStyle());
        this.setMaximized(false);
        this.buttons = buttons;
        if (type == Alert.AlertType.CONFIRMATION) {
            this.graphic = new QuestionSVGGlyph();
            this.graphic.setColor(Color.DODGERBLUE);
        } else {
            FXButton button = new FXButton(I18nHelper.ok());
            this.buttons.add(button);
            if (type == Alert.AlertType.WARNING) {
                this.graphic = new WarningSVGGlyph();
                this.graphic.setColor(Color.ORANGE);
            } else if (type == Alert.AlertType.ERROR) {
                this.graphic = new ErrorSVGGlyph();
                this.graphic.setColor(Color.ORANGERED);
            } else if (type == Alert.AlertType.INFORMATION) {
                this.graphic = new ErrorSVGGlyph();
                this.graphic.setColor(Color.DODGERBLUE);
            }
        }
        FXVBox root = new FXVBox();
        root.addChild(new FXHeaderBar());

        FXHBox node = new FXHBox();
        root.addChild(node);

        this.content = new FXLabel(content);
        this.content.setWrapText(true);
        this.content.setMaxWidth(320);

        if (this.graphic != null) {
            this.graphic.setSize(80);
            node.addChild(this.graphic);
            FXHBox.setMargin(this.graphic, DEFAULT_MARGIN);
        }

        FXVBox vbox = new FXVBox();
        vbox.addChild(this.content);
        VBox.setMargin(this.content, DEFAULT_MARGIN);

        FXHBox btnBox = new FXHBox();
        btnBox.addChild(this.buttons);
        btnBox.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        if (this.graphic == null) {
            btnBox.setPrefWidth(455);
        } else {
            btnBox.setPrefWidth(365);
        }
        for (Button button : buttons) {
            FXHBox.setMargin(button, BUTTON_DEFAULT_MARGIN);
            button.setOnAction(actionEvent -> {
                this.result = button;
                this.hide();
            });
        }
        vbox.addChild(btnBox);

        node.addChild(vbox);

        this.setScene(new Scene(root));
        this.setSize(480, 150);
        this.setResizable(false);
        this.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                double fHeight = FontUtil.calcFontHeight(this.content.getFont());
                double nHeight = this.content.getRealHeight();
                double fixedHeight = nHeight - fHeight;
                double primaryHeight = ScreenUtil.getPrimaryHeight();
                if (fixedHeight < 0) {
                    fixedHeight = 0;
                } else if (fixedHeight > primaryHeight - 150) {
                    fixedHeight = primaryHeight - 150;
                }
                this.setHeight(150 + fixedHeight);
                this.centerOnScreen();
            }
        });
    }

    @Override
    public Stage stage() {
        return this;
    }

    @Override
    public void showAndWait() {
        super.showAndWait();
        this.type = null;
        this.graphic = null;
        this.buttons = null;
        this.setScene(null);
    }

    public Button getResult() {
        this.showAndWait();
        return this.result;
    }

    public void setContent(String text) {
        if (this.content != null) {
            this.content.setText(text);
        }
    }

    public String getContent() {
        if (this.content != null) {
            return this.content.getText();
        }
        return null;
    }

    @Override
    public void hide() {
        if (this.type == Alert.AlertType.CONFIRMATION && this.result == null) {
            return;
        }
        super.hide();
    }
}
