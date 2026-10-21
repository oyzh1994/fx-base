package cn.oyzh.fx.rdp.test.rdp4j;

import cn.oyzh.fx.plus.controls.pane.FXScrollPane;
import cn.oyzh.fx.plus.ext.FXApplication;
import com.tangluobo.rdp4j.RdpClient;
import com.tangluobo.rdp4j.frontend.FxRdpFrontend;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX RDP 桌面测试，默认连接 Linux 主机 192.168.22.152。
 *
 * <p>可通过 JVM 参数切换主机：</p>
 * <pre>
 * -Drdp.host=192.168.22.157
 * -Drdp.port=3389
 * -Drdp.username=oyzh
 * -Drdp.password=123456
 * </pre>
 */
public class RdpHostTest extends FXApplication {

    private static final String HOST = System.getProperty("rdp.host", "192.168.22.152");
    private static final int PORT = Integer.getInteger("rdp.port", 3389);
    private static final String USERNAME = System.getProperty("rdp.username", "oyzh");
    private static final String PASSWORD = System.getProperty("rdp.password", "123456");

    static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        super.start(primaryStage);

        RdpView view = new RdpView();
        view.setFlexWidth("100%");
        view.setFlexHeight("100%");
        primaryStage.setScene(new Scene(new FXScrollPane(view)));
        primaryStage.setWidth(1024);
        primaryStage.setHeight(768);
        primaryStage.setMaximized(true);
        primaryStage.show();

        FxRdpFrontend frontend = new FxRdpFrontend();
        RdpClient client = new RdpClient(frontend);
        view.steup(client, frontend);
        client.connect(HOST, PORT, USERNAME, PASSWORD, null,
                1920, 1080, 32, true, true, false);
    }

    public static class RdpHostStarter {
        static void main(String[] args) {
            RdpHostTest.main(args);
        }
    }
}
