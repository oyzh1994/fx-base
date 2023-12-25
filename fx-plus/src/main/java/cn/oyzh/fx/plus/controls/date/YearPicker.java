package cn.oyzh.fx.plus.controls.date;

import cn.oyzh.fx.plus.util.ResourceUtil;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author oyzh
 * @since 2023/12/24
 */
public class YearPicker extends HBox {

    private final DateTimeFormatter formatter;

    // DateTime value
    private ObjectProperty<LocalDateTime> dateTime;

    private Popup popupContainer;

    private TimePickerSelect dateTimePickerSelect;

    public Boolean showLocalizedDateTime = false;

    private final TextField textField;

    private final Button button;

    public YearPicker() {
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

        this.formatter = DateTimeFormatter.ofPattern("yyyy");

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
                this.dateTimePickerSelect = new TimePickerSelect(this);
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

    /**
     * @author oyzh
     * @since 2023/12/24
     */
    static class TimePickerSelect extends VBox {

        private final YearPicker dateTimePicker;

        private final List<Button> dayList;

        private Calendar calendar;

        private LocalDateTime cursorDateTime;

        private final ComboBox<String> year;

        private final Button buttonCancel;

        private final Button buttonNow;

        private final Button buttonOK;

        private final Button buttonReset;

        public TimePickerSelect(final YearPicker parentCont) {
            this.dateTimePicker = parentCont;
            dayList = new ArrayList<Button>();

            this.setStyle("-fx-background-color: #efefef;");

            HBox ch4 = new HBox();
            ch4.setAlignment(Pos.CENTER);
            ch4.setPrefHeight(30);
            ch4.setMinHeight(30);
            ch4.setMaxHeight(30);
            this.getChildren().add(ch4);

            this.year = new ComboBox<>();
            this.year.setId("fontStyle");
            ch4.getChildren().add(year);

            HBox ch5 = new HBox();
            ch5.setAlignment(Pos.BASELINE_CENTER);
            this.getChildren().add(ch5);

            this.buttonReset = new Button("清除");
            buttonReset.setOnAction(this::buttonResetOnAction);
            buttonReset.setOnMousePressed(this::buttonResetOnMousePressed);
            buttonReset.setOnMouseReleased(this::buttonResetOnMouseReleased);
            buttonReset.setMaxWidth(58.6);
            buttonReset.setPrefWidth(58.6);
            buttonReset.setMinHeight(25);
            buttonReset.setMaxHeight(25);
            buttonReset.setStyle("-fx-background-color:#96e561;-fx-font-size: 12;-fx-cursor: HAND;");
            ch5.getChildren().add(buttonReset);

            Label label5 = new Label();
            label5.setPrefWidth(5);
            ch5.getChildren().add(label5);

            this.buttonCancel = new Button("取消");
            buttonCancel.setOnAction(this::buttonCancelOnAction);
            buttonCancel.setOnMousePressed(this::buttonCancelOnMousePressed);
            buttonCancel.setOnMouseReleased(this::buttonCancelOnMouseReleased);
            buttonCancel.setMaxWidth(58.6);
            buttonCancel.setPrefWidth(58.6);
            buttonCancel.setMinHeight(25);
            buttonCancel.setMaxHeight(25);
            buttonCancel.setStyle("-fx-background-color:#FFD2D2;-fx-font-size: 12;-fx-cursor: HAND;");
            ch5.getChildren().add(buttonCancel);

            Label label6 = new Label();
            label6.setPrefWidth(5);
            ch5.getChildren().add(label6);

            this.buttonNow = new Button("当前");
            buttonNow.setOnAction(this::buttonNowOnAction);
            buttonNow.setOnMousePressed(this::buttonNowOnMousePressed);
            buttonNow.setOnMouseReleased(this::buttonNowOnMouseReleased);
            buttonNow.setMaxWidth(58.6);
            buttonNow.setPrefWidth(58.6);
            buttonNow.setMinHeight(25);
            buttonNow.setMaxHeight(25);
            buttonNow.setStyle("-fx-background-color:#FFD2D2;-fx-font-size: 12;-fx-cursor: HAND;");
            ch5.getChildren().add(buttonNow);

            Label label7 = new Label();
            label7.setPrefWidth(5);
            ch5.getChildren().add(label7);

            this.buttonOK = new Button("确定");
            buttonOK.setOnAction(this::buttonOKOnAction);
            buttonOK.setOnMousePressed(this::buttonOKOnMousePressed);
            buttonOK.setOnMouseReleased(this::buttonOKOnMouseReleased);
            buttonOK.setMaxWidth(58.6);
            buttonOK.setPrefWidth(58.6);
            buttonOK.setMinHeight(25);
            buttonOK.setMaxHeight(25);
            buttonOK.setStyle("-fx-background-color:#ACD6FF;-fx-font-size: 12;-fx-cursor: HAND;");
            ch5.getChildren().add(buttonOK);

            Label ch6 = new Label();
            ch6.setMinHeight(5);
            ch6.setPrefHeight(5);
            this.getChildren().add(ch6);

            if (this.dateTimePicker.dateTimeProperty() != null) {
                this.cursorDateTime = this.dateTimePicker.dateTimeProperty().getValue();
            }
            upDataCalendar(true);
        }

        public String strValue(int i) {
            String res;
            if (i < 10) {
                res = "0" + i;
            } else {
                res = i + "";
            }
            return res;
        }


        public void upDataCalendar(boolean open) {
            dayList.clear();

            this.cursorDateTime = this.dateTimePicker.dateTimeProperty() != null ? this.dateTimePicker.dateTimeProperty().get() : null;
            int nowYear = this.cursorDateTime != null ? this.cursorDateTime.getYear() : -1;
            int nowMonth = this.cursorDateTime != null ? this.cursorDateTime.getMonthValue() : -1;

            Calendar tmpCalendar = this.cursorDateTime != null ?
                    GregorianCalendar.from(ZonedDateTime.of(this.cursorDateTime, ZoneId.systemDefault())) :
                    GregorianCalendar.from(ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault()));
            if (open) {
                calendar = tmpCalendar;
            } else {
                tmpCalendar = this.calendar;
            }

            int mouthDays = tmpCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            tmpCalendar.set(tmpCalendar.get(Calendar.YEAR), tmpCalendar.get(Calendar.MONTH), mouthDays, tmpCalendar.get(Calendar.HOUR_OF_DAY), tmpCalendar.get(Calendar.MINUTE), tmpCalendar.get(Calendar.SECOND));
            int weekMouthLastDay = tmpCalendar.get(Calendar.DAY_OF_WEEK);
            tmpCalendar.set(tmpCalendar.get(Calendar.YEAR), tmpCalendar.get(Calendar.MONTH), 1, tmpCalendar.get(Calendar.HOUR_OF_DAY), tmpCalendar.get(Calendar.MINUTE), tmpCalendar.get(Calendar.SECOND));
            int weekMouthFirstDay = tmpCalendar.get(Calendar.DAY_OF_WEEK);
            tmpCalendar.add(Calendar.MONTH, -1);
            int lastMouthDays = tmpCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            tmpCalendar.add(Calendar.MONTH, 1);

            setTime();
        }

        /**
         * �������º������ڱ�����ʾ��������ʽ��������Ϊ���ɵ��
         *
         * @param btn ���ڰ�ťButton
         */
        public void setDisable(Button btn) {
            btn.setDisable(true);
            btn.setStyle("-fx-text-fill: black;-fx-background-color: transparent;;-fx-font-size: 10");
        }

        /**
         * ���ñ������ڵĵ���¼�����ʽ�����е��ʱ����Զ���¼ʱ��
         *
         * @param btn ���ڰ�ťButton
         */
        public void setAble(Button btn) {
            btn.setStyle("-fx-text-fill: black;-fx-background-color: #fff;-fx-font-size: 10");
            btn.setOnAction(event -> {
                dayList.forEach(e -> {
                    e.setStyle("-fx-text-fill: black;-fx-background-color: #fff;-fx-font-size: 10");
                });
                btn.setStyle("-fx-text-fill: white;-fx-background-color: #5b8cff;-fx-font-size: 10");
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), Integer.valueOf(btn.getText()),
                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
                ;
            });
        }

        private void setTime() {
            for (int i = 1901; i <= 2155; i++) {
                year.getItems().add(strValue(i));
            }
            year.getSelectionModel().select(calendar.get(Calendar.YEAR));
        }

        protected void buttonOKOnMousePressed(MouseEvent event) {
            buttonOK.setStyle("-fx-background-color:#FFFFB9;-fx-font-size: 12;-fx-cursor: HAND;");
        }

        protected void buttonOKOnMouseReleased(MouseEvent event) {
            buttonOK.setStyle("-fx-background-color:#ACD6FF;-fx-font-size: 12;-fx-cursor: HAND;");
        }

        protected void buttonOKOnAction(ActionEvent event) {
            if (this.calendar != null) {
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                        Integer.parseInt(year.getSelectionModel().getSelectedItem()), 0, 0);
                LocalDateTime localDateTime = LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault());
                this.dateTimePicker.setTimeProperty(localDateTime);
            } else {
                //            System.out.println("����ѡ������");
            }
            this.dateTimePicker.hide();
        }

        protected void buttonNowOnMousePressed(MouseEvent event) {
            buttonOK.setStyle("-fx-background-color:#FFFFB9;-fx-font-size: 12;-fx-cursor: HAND;");
        }

        protected void buttonNowOnMouseReleased(MouseEvent event) {
            buttonOK.setStyle("-fx-background-color:#e7c269;-fx-font-size: 12;-fx-cursor: HAND;");
        }

        protected void buttonNowOnAction(ActionEvent event) {
            LocalDateTime localDateTime = LocalDateTime.now();
            calendar = Calendar.getInstance();
            if (this.calendar != null) {
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                        Integer.parseInt(year.getSelectionModel().getSelectedItem()), 0, 0);
            } else {
                //            System.out.println("����ѡ������");
            }
            this.dateTimePicker.setTimeProperty(localDateTime);
            this.dateTimePicker.hide();
        }

        protected void buttonCancelOnMousePressed(MouseEvent event) {
            buttonCancel.setStyle("-fx-background-color:#FFFFB9;-fx-font-size: 12;-fx-cursor: HAND;");
        }

        protected void buttonCancelOnMouseReleased(MouseEvent event) {
            buttonCancel.setStyle("-fx-background-color:#FFD2D2;-fx-font-size: 12;-fx-cursor: HAND;");
        }

        protected void buttonCancelOnAction(ActionEvent event) {
            this.dateTimePicker.hide();
        }

        protected void buttonResetOnMousePressed(MouseEvent event) {
            buttonReset.setStyle("-fx-background-color:#FFFFB9;-fx-font-size: 12;-fx-cursor: HAND;");
        }

        protected void buttonResetOnMouseReleased(MouseEvent event) {
            buttonReset.setStyle("-fx-background-color:#96e561;-fx-font-size: 12;-fx-cursor: HAND;");
        }

        protected void buttonResetOnAction(ActionEvent event) {
            this.calendar = null;
            this.cursorDateTime = null;
            this.dateTimePicker.clearTimeProperty();
        }
        //    /**
        //     *
        //     * ������������ѡ��ť������ʱ�͵���ʱ����ɫ
        //     * @param btn  ��������ѡ��ťButton
        //     */
        //    public void btnMouthPress(Button btn){
        //        btn.setOnMousePressed(new EventHandler<MouseEvent>() {
        //            @Override
        //            public void handle(MouseEvent event) {
        //                btn.setStyle("-fx-text-fill: black;-fx-background-color: #FFD306;");
        //            }
        //        });
        //        btn.setOnMouseReleased(new EventHandler<MouseEvent>() {
        //            @Override
        //            public void handle(MouseEvent event) {
        //                btn.setStyle("-fx-text-fill: black;-fx-background-color: #c66;");
        //            }
        //        });
        //    }
        //    public void btnYearPress(Button btn){
        //        btn.setOnMousePressed(new EventHandler<MouseEvent>() {
        //            @Override
        //            public void handle(MouseEvent event) {
        //                btn.setStyle("-fx-text-fill: black;-fx-background-color:#FF0000;");
        //            }
        //        });
        //        btn.setOnMouseReleased(new EventHandler<MouseEvent>() {
        //            @Override
        //            public void handle(MouseEvent event) {
        //                btn.setStyle("-fx-text-fill: black;-fx-background-color: #6cc;");
        //            }
        //        });
        //    }

        @Override
        public String getUserAgentStylesheet() {
            return ResourceUtil.toExternalUrl("/fx-plus/css/date-time-picker.css");
        }
    }
}
