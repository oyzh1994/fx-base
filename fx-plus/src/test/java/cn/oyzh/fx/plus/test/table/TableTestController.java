package cn.oyzh.fx.plus.test.table;

import cn.oyzh.fx.plus.controller.Controller;
import cn.oyzh.fx.plus.controls.table.FlexTableColumn;
import cn.oyzh.fx.plus.spring.SpringApplication;
import cn.oyzh.fx.plus.stage.StageAttribute;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.stage.Modality;

/**
 * @author oyzh
 * @since 2023/11/21
 */
@StageAttribute(
        title = "table测试",
        modality = Modality.WINDOW_MODAL,
        value =  "/table/test.fxml"
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
}
