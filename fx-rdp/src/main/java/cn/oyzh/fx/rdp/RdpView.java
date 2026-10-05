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
        rdpClient.setOnFirstFrame(this::attachDesktopAfterFirstFrame);
    }

    /**
     * 首帧处理
     */
    private void attachDesktopAfterFirstFrame() {
        Node nextView = this.frontend.getView();
        if (nextView != null) {
            this.addChild(nextView);
            FXUtil.runLater(this::requestFocus);
        }
    }

    /**
     * 设置按比例缩放
     *
     * @param scaleToFit 按比例缩放
     */
    public void setScaleToFit(boolean scaleToFit) {
        FXUtil.runLater(() -> {
            FxRdpDisplay display = this.frontend.getDisplay();
            if (display != null) {
                display.setScaleToFit(scaleToFit);
            }
            Node nextView = this.frontend.getView();
            if (nextView instanceof Pane parent) {
                if (!parent.prefWidthProperty().isBound()) {
                    parent.prefWidthProperty().bind(this.widthProperty());
                }
                if (!parent.prefHeightProperty().isBound()) {
                    parent.prefHeightProperty().bind(this.heightProperty());
                }
            }
        });
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
