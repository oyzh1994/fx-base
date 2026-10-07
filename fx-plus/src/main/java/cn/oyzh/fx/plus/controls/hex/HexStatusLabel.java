package cn.oyzh.fx.plus.controls.hex;

import cn.oyzh.common.object.Destroyable;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;

import java.lang.ref.WeakReference;

/**
 * 十六进制视图状态标签控件
 *
 * @author oyzh
 * @since 2026-07-13
 */
public class HexStatusLabel extends FXLabel implements Destroyable {

    /**
     * 状态刷新定时器
     */
    private AnimationTimer statusTimer;

    /**
     * 十六进制视图弱引用
     */
    private WeakReference<HexView> reference;

    /**
     * 初始化状态标签
     *
     * @param hexView 十六进制视图
     */
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

    /**
     * 停止状态刷新并清空文本
     */
    public void stop() {
        this.clear();
        if (this.statusTimer != null) {
            this.statusTimer.stop();
        }
    }

    /**
     * 更新状态信息文本
     */
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

    @Override
    public void destroy() {
        this.stop();
    }
}
