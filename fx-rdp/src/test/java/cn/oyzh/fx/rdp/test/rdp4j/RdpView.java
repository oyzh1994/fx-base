package cn.oyzh.fx.rdp.test.rdp4j;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.fx.plus.controls.pane.FXPane;
import cn.oyzh.fx.plus.util.FXUtil;
import com.tangluobo.rdp4j.RdpClient;
import com.tangluobo.rdp4j.frontend.FxRdpDisplay;
import com.tangluobo.rdp4j.frontend.FxRdpFrontend;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * Pure JavaFX RDP container. Swing remains available through SwingRdpFrontend.
 */
public class RdpView extends FXPane {

    //    private static final double FULL_SCREEN_TOP_EDGE_HEIGHT = 12;
    //    private static final double CONTROL_BAR_HANDLE_HEIGHT = 3;

    //    private final FxRdpFrontend frontend = new FxRdpFrontend();
    //    private final RdpClient rdpClient = new RdpClient(frontend);
    private FxRdpFrontend frontend;
    //    private ImageView nativeKeyboardTarget;
    //    private ChangeListener<Boolean> nativeKeyboardFocusListener;
    //    private int bestSceneEdgeBand = Integer.MAX_VALUE;
    //    private int bestLocalRemoteEdgeBand = Integer.MAX_VALUE;
    //    private int bestServerRemoteEdgeBand = Integer.MAX_VALUE;
    //    private int bestRobotEdgeBand = Integer.MAX_VALUE;

    public void steup(RdpClient rdpClient, FxRdpFrontend frontend) {
        this.frontend = frontend;
        rdpClient.setOnConnected(() -> JulLog.info("RDP显示通道已就绪，等待首帧"));
        rdpClient.setOnFirstFrame(() -> FXUtil.runLater(this::attachDesktopAfterFirstFrame));
    }

    private void attachDesktopAfterFirstFrame() {
        Node nextView = this.frontend.getView();
        if (nextView == null) {
            return;
        }
        FxRdpDisplay display = this.frontend.getDisplay();
        if (display != null) {
            display.setScaleToFit(true);
        }
        if (nextView instanceof Pane parent) {
            parent.prefWidthProperty().bind(this.widthProperty());
            parent.prefHeightProperty().bind(this.heightProperty());
        }
        this.addChild(nextView);
        this.requestFocus();
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        FxRdpDisplay display = frontend.getDisplay();
        if (display != null) {
            display.getImageView().requestFocus();
        }
    }

    //    private void onRemotePointerMoved(int remoteX, int remoteY) {
    //        onProtocolPointerMoved("rdp-local", remoteX, remoteY, false);
    //    }
    //
    //    private void onServerPointerMoved(int remoteX, int remoteY) {
    //        onProtocolPointerMoved("rdp-server", remoteX, remoteY, true);
    //    }

    //    private void onProtocolPointerMoved(String source, int remoteX, int remoteY,
    //                                        boolean serverPointer) {
    //        FxRdpDisplay display = frontend.getDisplay();
    //        if (display == null) {
    //            return;
    //        }
    //        double renderedHeight = display.getImageView().getBoundsInParent().getHeight();
    //        logCloserEdgeObservation(source, remoteY, serverPointer ? 2 : 1);
    //    }

    //    private void logCloserEdgeObservation(String source, double y, int sourceIndex) {
    //        int band = edgeDiagnosticBand(y);
    //        int previous = switch (sourceIndex) {
    //            case 0 -> bestSceneEdgeBand;
    //            case 1 -> bestLocalRemoteEdgeBand;
    //            case 2 -> bestServerRemoteEdgeBand;
    //            default -> bestRobotEdgeBand;
    //        };
    //        if (band >= previous) {
    //            return;
    //        }
    //        switch (sourceIndex) {
    //            case 0 -> bestSceneEdgeBand = band;
    //            case 1 -> bestLocalRemoteEdgeBand = band;
    //            case 2 -> bestServerRemoteEdgeBand = band;
    //            default -> bestRobotEdgeBand = band;
    //        }
    //    }

    //    static int edgeDiagnosticBand(double y) {
    //        if (!Double.isFinite(y) || y < 0 || y > 200)
    //            return Integer.MAX_VALUE;
    //        if (y <= FULL_SCREEN_TOP_EDGE_HEIGHT)
    //            return 0;
    //        if (y <= 25)
    //            return 1;
    //        if (y <= 50)
    //            return 2;
    //        if (y <= 100)
    //            return 3;
    //        return 4;
    //    }
}
