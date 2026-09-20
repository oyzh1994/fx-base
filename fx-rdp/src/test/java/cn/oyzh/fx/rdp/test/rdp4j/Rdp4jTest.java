package cn.oyzh.fx.rdp.test.rdp4j;

import cn.oyzh.fx.plus.controls.pane.FXScrollPane;
import cn.oyzh.fx.plus.ext.FXApplication;
import com.tangluobo.rdp4j.RdpClient;
import com.tangluobo.rdp4j.frontend.FxRdpFrontend;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author oyzh
 * @since 2026-09-20
 */
public class Rdp4jTest extends FXApplication {

    static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        super.start(primaryStage);
        //        this.test1(primaryStage);
        this.test2(primaryStage);
    }

//    private void test1(Stage primaryStage) {
//        RdpPane rdpPane = new RdpPane();
//        primaryStage.setScene(new Scene(rdpPane));
//        primaryStage.show();
//
//        rdpPane.connect("192.168.22.149",
//                3389,
//                "oyzh",
//                "123456",
//                null,
//                1920,
//                1080, 32, true, false, false);
//    }

    private void test2(Stage primaryStage) {
        RdpView view = new RdpView();
        view.setFlexWidth("100%");
        view.setFlexHeight("100%");
        primaryStage.setScene(new Scene(new FXScrollPane(view)));

        primaryStage.setWidth(800);
        primaryStage.setHeight(600);

        primaryStage.setMaximized(true);
        primaryStage.show();

        FxRdpFrontend frontend = new FxRdpFrontend();
        RdpClient client = new RdpClient(frontend);
        view.steup(client, frontend);

        client.connect("192.168.22.149",
                3389,
                "oyzh",
                "123456",
                null,
                1920,
                1080, 32, true, true, false);
    }

    public static class Rdp4jStarter {
        static void main(String[] args) {
            Rdp4jTest.main(args);
        }
    }

}
