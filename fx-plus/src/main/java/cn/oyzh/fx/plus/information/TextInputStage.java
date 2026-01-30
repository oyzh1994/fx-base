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
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


/**
 *
 * @author oyzh
 * @since 2026-01-30
 */
public class TextInputStage extends Stage implements StageAdapter {

    private static final Insets DEFAULT_MARGIN = new Insets(10, 0, 0, 5);

    private static final Insets OK_DEFAULT_MARGIN = new Insets(0, 0, 0, 10);

    private FXTextField textField;

    public TextInputStage() {
        this("");
    }

    public TextInputStage(String initText) {
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

        FXButton cancel = new FXButton(I18nHelper.cancel());
        cancel.setOnAction(actionEvent -> this.cancel());
        hbox.addChild(cancel);

        FXButton ok = new FXButton(I18nHelper.ok());
        ok.setOnAction(actionEvent -> this.ok());
        ok.addClass("accent");
        hbox.addChild(ok);
        HBox.setMargin(ok, OK_DEFAULT_MARGIN);

        root.addChild(hbox);
        VBox.setMargin(hbox, DEFAULT_MARGIN);

        this.setScene(new Scene(root));
        this.setSize(300, 130);
        this.setResizable(false);
        this.centerOnScreen();

        this.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                this.ok();
            } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
                this.cancel();
            }
        });
    }

    public void cancel() {
        this.textField.clear();
        this.close();
    }

    public void ok() {
        this.close();
    }

    @Override
    public Stage stage() {
        return this;
    }

    public String getResult() {
        this.showAndWait();
        String text = this.textField.getText();
        this.textField = null;
        this.setScene(null);
        return text;
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
}
