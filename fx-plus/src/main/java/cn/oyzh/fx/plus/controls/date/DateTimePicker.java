package cn.oyzh.fx.plus.controls.date;

import cn.oyzh.fx.plus.util.ResourceUtil;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import javafx.stage.Window;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

/**
 * @author oyzh
 * @since 2023/12/24
 */
public class DateTimePicker extends HBox {

    private final DateTimeFormatter formatter;

    // DateTime value
    private ObjectProperty<LocalDateTime> dateTime;

    private Popup popupContainer;

    private DateTimePickerSelect dateTimePickerSelect;

    public Boolean showLocalizedDateTime = false;

    private final TextField textField;

    private final Button button;

    public DateTimePicker() {
        this.textField = new TextField();
        this.textField.setDisable(true);
        this.textField.setFocusTraversable(false);
        this.textField.setId("dt_text_field");
        this.getChildren().add(this.textField);

        this.button = new Button();
        this.button.setId("dt_button");
        this.button.setGraphic(new ImageView("/fx-plus/img/calendar.png"));
        this.button.setOnAction(this::handleButtonAction);
        this.getChildren().add(this.button);

        this.formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);


        if (showLocalizedDateTime) {
            textField.setText(formatter.format(dateTime.get()));
            this.dateTime = new SimpleObjectProperty<>(LocalDateTime.now());
        }
        button.prefHeightProperty().bind(textField.heightProperty());
    }

    protected void handleButtonAction(ActionEvent event) {
        if (popupContainer != null && popupContainer.isShowing()) {
            popupContainer.hide();
        } else {
            if (this.popupContainer == null) {
                this.popupContainer = new Popup();
                this.dateTimePickerSelect = new DateTimePickerSelect(this);
                popupContainer.getContent().add(dateTimePickerSelect);
                popupContainer.autoHideProperty().set(true);
            }
            final Window window = button.getScene().getWindow();
            final double x = window.getX()
                    + textField.localToScene(0, 0).getX()
                    + textField.getScene().getX();
            final double y = window.getY()
                    + button.localToScene(0, 0).getY()
                    + button.getScene().getY()
                    + button.getHeight();

//            popupContainer.setWidth(100);
//            popupContainer.setHeight(100);

            System.out.println(this.getParent());
            popupContainer.show(this.getParent(), x, y);
            dateTimePickerSelect.upDataCalendar(true);
        }
    }

    /**
     * Gets the current LocalDateTime value
     *
     * @return The current LocalDateTime value
     */
    public ObjectProperty<LocalDateTime> dateTimeProperty() {
        return dateTime;
    }

    /**
     * @Description: setTimeProperty
     * @Params
     * @Return
     * @Author wzy
     * @Date 2023/4/19 11:11
     **/
    public void setTimeProperty(LocalDateTime localDateTime) {
        this.dateTime = new SimpleObjectProperty<LocalDateTime>(localDateTime);
        textField.setText(formatter.format(this.dateTime.get()));
    }

    /**
     * @Description: clearTimeProperty
     * @Params
     * @Return
     * @Author wzy
     * @Date 2023/4/19 11:11
     **/
    public void clearTimeProperty() {
        this.dateTime = null;
        textField.setText("");
    }

    public void hide() {
        if (popupContainer.isShowing()) {
            popupContainer.hide();
        }
    }

    public Boolean getShowLocalizedDateTime() {
        return showLocalizedDateTime;
    }

    public void setShowLocalizedDateTime(Boolean show) {
        this.showLocalizedDateTime = show;
        if (show) {
            setTimeProperty(LocalDateTime.now());
        }

    }

    @Override
    public String getUserAgentStylesheet() {
        return ResourceUtil.toExternalUrl("/fx-plus/css/date-time-picker.css");
    }
}
