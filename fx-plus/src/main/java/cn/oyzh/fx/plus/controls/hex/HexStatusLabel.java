package cn.oyzh.fx.plus.controls.hex;

import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;

import java.lang.ref.WeakReference;

/**
 *
 * @author oyzh
 * @since 2026-07-13
 */
public class HexStatusLabel extends FXLabel {

    private AnimationTimer statusTimer;

    private WeakReference<HexView> reference;

    public void init(HexView hexView) {
        this.reference = new WeakReference<>(hexView);
        this.statusTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateStatus();
            }
        };
        this.statusTimer.start();
        this.setPadding(new Insets(0, 0, 0, 10));
    }

    public void stop() {
        this.clear();
        if (this.statusTimer != null) {
            this.statusTimer.stop();
        }
    }

    private void updateStatus() {
        HexView hexView = this.reference.get();
        if (hexView == null) {
            this.statusTimer.stop();
            return;
        }
        long size = hexView.getFileSize();
        long focus = hexView.getFocusByte();
        long selSize = hexView.getSelectionSize();
        String info = String.format(
                "Offset: 0x%X / 0x%X (%s) | %d columns",
                focus, size, NumberUtil.formatSize(size), hexView.getBytesPerRow()
        );
        if (selSize > 0) {
            info += String.format(" | selected: %d bytes", selSize);
        }
        String finalInfo = info;
        this.text(finalInfo);
    }
}
