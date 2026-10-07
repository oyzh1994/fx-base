package cn.oyzh.fx.plus.test.table;

import cn.oyzh.common.object.ObjectWatcher;
import cn.oyzh.common.object.ObjectWatcherManager;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageAttribute;
import javafx.fxml.FXML;
import javafx.stage.Modality;

/**
 * 表格测试界面控制器，验证标签页关闭后的内存回收
 *
 * @author oyzh
 * @since 2023/11/21
 */
@StageAttribute(
        title = "table测试",
        modality = Modality.WINDOW_MODAL,
        value = "/table/test1.fxml"
)
public class TableTest1Controller extends StageController {


    @FXML
    private FXTab tab1;

    @Override
    public void onStageInitialize(StageAdapter stage) {
        super.onStageInitialize(stage);
        ObjectWatcherManager.watch(tab1);

        tab1.setOnClosed(event -> {
            this.tab1 = null;
        });

        // ThreadUtil.start(new Runnable() {
        //     @Override
        //     public void run() {
        //         while (true) {
        //             ThreadUtil.sleep(3000);
        //             SystemUtil.gc();
        //             System.runFinalization();
        //             System.out.println("----gc");
        //         }
        //     }
        // });
    }

    @FXML
    private void test1( ) {
    }

    @FXML
    private void test2( ) {
    }

    @FXML
    private void theme1( ) {
    }

    @FXML
    private void theme2( ) {
    }

    @FXML
    private void theme3( ) {
    }

    @FXML
    private void theme4( ) {
    }

    @FXML
    private void theme5( ) {
    }

    @FXML
    private void theme6( ) {
    }

    @FXML
    private void theme7( ) {
    }
}
