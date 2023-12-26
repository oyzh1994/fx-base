package cn.oyzh.fx.plus.controls.date;

import atlantafx.base.controls.Popover;
import cn.oyzh.fx.plus.controls.FXHBox;
import cn.oyzh.fx.plus.controls.FXVBox;
import cn.oyzh.fx.plus.controls.FlexHBox;
import cn.oyzh.fx.plus.controls.button.FXButton;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.controls.text.FXLabel;
import cn.oyzh.fx.plus.controls.textfield.ClearableTextField;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author oyzh
 * @since 2023/12/24
 */
public class YearPicker extends CalendarPicker<YearSelector> {

    public YearPicker() {
        super();
        this.setPattern("yyyy");
    }

    @Override
    public void showPopup() {
        if (this.selector == null) {
            this.selector = new YearSelector(this);
        }
        super.showPopup();
    }
}
