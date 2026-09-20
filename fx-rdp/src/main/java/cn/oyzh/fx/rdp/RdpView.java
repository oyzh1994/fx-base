package cn.oyzh.fx.rdp;

import cn.oyzh.fx.plus.controls.pane.FXPane;
import cn.oyzh.fx.plus.util.FXUtil;
import com.tangluobo.rdp4j.RdpClient;
import com.tangluobo.rdp4j.frontend.FxRdpDisplay;
import com.tangluobo.rdp4j.frontend.FxRdpFrontend;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * rdp视图
 *
 * @author oyzh
 * @since 2026/09/20
 */
public class RdpView extends FXPane {

    private FxRdpFrontend frontend;

    public void steup(RdpClient rdpClient, FxRdpFrontend frontend) {
        this.frontend = frontend;
        rdpClient.setOnFirstFrame(ignored -> FXUtil.runLater(this::attachDesktopAfterFirstFrame));
    }

    private void attachDesktopAfterFirstFrame() {
        Node nextView = this.frontend.getView();
        if (nextView == null) {
            return;
        }
        if (nextView instanceof Pane parent) {
            parent.prefWidthProperty().bind(this.widthProperty());
            parent.prefHeightProperty().bind(this.heightProperty());
        }
        FxRdpDisplay display = this.frontend.getDisplay();
        if (display != null) {
            display.setScaleToFit(true);
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
}
