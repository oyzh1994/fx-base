package cn.oyzh.fx.plus.test.table;

import cn.oyzh.fx.plus.controller.Controller;
import cn.oyzh.fx.plus.controls.table.FlexTableColumn;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.theme.Themes;
import cn.oyzh.fx.plus.theme.ThemeManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Pagination;
import javafx.stage.Modality;

/**
 * @author oyzh
 * @since 2023/11/21
 */
@StageAttribute(
        title = "table测试",
        modality = Modality.WINDOW_MODAL,
        value = "/table/test.fxml"
)
public class TableTestController extends Controller {

    /**
     * 分数列
     */
    @FXML
    private FlexTableColumn<String, String> value;

    /**
     * 分数列
     */
    @FXML
    private FlexTableColumn<String, Double> score;

    /**
     * 分数列
     */
    @FXML
    private FlexTableColumn<String, Double> longitude;

    /**
     * 分数列
     */
    @FXML
    private FlexTableColumn<String, Double> latitude;

    public void test1(ActionEvent actionEvent) {
        value.setText("成员名称");
        value.setFlexWidth("50%");
        score.setVisible(true);
        latitude.setVisible(false);
        longitude.setVisible(false);
    }

    public void test2(ActionEvent actionEvent) {
        value.setText("坐标名称");
        value.setFlexWidth("30%");
        score.setVisible(false);
        latitude.setVisible(true);
        longitude.setVisible(true);
    }

    public void theme1(ActionEvent actionEvent) {
        // this.stage.switchPrimerLightTheme();
        ThemeManager.apply(Themes.PRIMER_LIGHT);
    }

    public void theme2(ActionEvent actionEvent) {
        // this.stage.switchPrimerDarkTheme();
        ThemeManager.apply(Themes.PRIMER_DARK);
    }

    public void theme3(ActionEvent actionEvent) {
        // this.stage.switchNordLightTheme();
        ThemeManager.apply(Themes.NORD_LIGHT);
    }

    public void theme4(ActionEvent actionEvent) {
        // this.stage.switchNordDarkTheme();
        ThemeManager.apply(Themes.NORD_DARK);
    }

    public void theme5(ActionEvent actionEvent) {
        // this.stage.switchCupertinoLightTheme();
        ThemeManager.apply(Themes.CUPERTINO_LIGHT);
    }

    public void theme6(ActionEvent actionEvent) {
        // this.stage.switchCupertinoDarkTheme();
        ThemeManager.apply(Themes.CUPERTINO_DARK);
    }

    public void theme7(ActionEvent actionEvent) {
        // this.stage.switchDraculaTheme();
        ThemeManager.apply(Themes.DRACULA);
    }


    public void test(){
        Pagination pagination;
    }
}
