package cn.oyzh.fx.plus.information;

import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.FXHeaderBar;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.button.FXButton;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.i18n.I18nHelper;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;


/**
 *
 * @author oyzh
 * @since 2026-01-30
 */
public class InputStage extends Stage implements StageAdapter {

    private static final Insets DEFAULT_MARGIN = new Insets(15, 0, 0, 5);

    private static final Insets OK_DEFAULT_MARGIN = new Insets(0, 10, 0, 10);

    /**
     * 组件
     */
    private FXTextField textField;

    /**
     * 结果
     */
    private String result;

    public InputStage() {
        this("");
    }

    public InputStage(String initText) {
        this.initModality(Modality.APPLICATION_MODAL);
        this.initStyle(FXStageStyle.EXTENDED.toStageStyle());

        FXVBox root = new FXVBox();
        root.addChild(new FXHeaderBar());

        this.textField = new ClearableTextField();
        this.setPromptText(I18nHelper.pleaseInputContent());
        this.textField.setText(initText);
        this.textField.setFlexWidth("100% - 10");

        root.addChild(this.textField);
        VBox.setMargin(this.textField, DEFAULT_MARGIN);

        FXHBox hbox = new FXHBox();
        hbox.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        FXButton ok = new FXButton(I18nHelper.ok());
        ok.setOnAction(actionEvent -> this.ok());
        ok.addClass("accent");
        hbox.addChild(ok);
        HBox.setMargin(ok, OK_DEFAULT_MARGIN);

        FXButton cancel = new FXButton(I18nHelper.cancel());
        cancel.setOnAction(actionEvent -> this.cancel());
        hbox.addChild(cancel);

        root.addChild(hbox);
        VBox.setMargin(hbox, DEFAULT_MARGIN);

        this.setScene(new Scene(root));
        this.setSize(400, 150);
        this.setResizable(false);
        this.centerOnScreen();
        StageAdapter.super.applyTheme();
        this.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                this.ok();
            } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
                this.cancel();
            }
        });
    }

    public void cancel() {
        this.result = "";
        this.close();
    }

    public void ok() {
        this.result = this.textField.getText();
        this.close();
    }

    @Override
    public Stage stage() {
        return this;
    }

    @Override
    public void showAndWait() {
        super.showAndWait();
        this.textField = null;
        this.setScene(null);
    }

    public String getResult() {
        this.showAndWait();
        return this.result;
    }

    public void setText(String text) {
        if (this.textField != null) {
            this.textField.setText(text);
        }
    }

    public String getText() {
        if (this.textField != null) {
            return this.textField.getText();
        }
        return null;
    }

    public void setPromptText(String promptText) {
        if (this.textField != null) {
            this.textField.setPromptText(promptText);
        }
    }

    public String getPromptText() {
        if (this.textField != null) {
            return this.textField.getPromptText();
        }
        return null;
    }

    @Override
    public void hide() {
        if (this.result == null) {
            this.result = "";
        }
        super.hide();
    }
}
