package cn.oyzh.fx.plus.controls;

import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import javafx.geometry.Pos;

/**
 * 带文本的进度条控件，继承自 FXHBox，由进度条与文本标签组合而成，用于展示进度及其百分比文本
 *
 * @author oyzh
 * @since 2025-03-21
 */
public class FXProgressTextBar extends FXHBox {

    {
        this.addChild(new FXProgressBar());
        this.addChild(new FXLabel());
        this.setAlignment(Pos.CENTER_LEFT);
    }

    /**
     * 获取进度条组件
     *
     * @return 进度条组件
     */
    public FXProgressBar progressBar() {
        return (FXProgressBar) this.getFirstChild();
    }

    /**
     * 获取文本标签组件
     *
     * @return 文本标签组件
     */
    public FXLabel label() {
        return (FXLabel) this.getChild(1);
    }

    /**
     * 设置进度值
     *
     * @param progress 进度值
     */
    public void setProgress(double progress) {
        FXProgressBar progressBar = this.progressBar();
        progressBar.progress(progress);
    }

    /**
     * 根据当前值与总量设置进度值
     *
     * @param current 当前值
     * @param total   总量
     */
    public void setProgress(double current, double total) {
        this.setProgress(current / total);
    }

    /**
     * 设置文本
     *
     * @param text 文本
     */
    public void setText(String text) {
        FXLabel label = this.label();
        label.text(text);
    }

    /**
     * 根据当前值与总量设置百分比文本
     *
     * @param current 当前值
     * @param total   总量
     */
    public void setText(double current, double total) {
        int value = (int) (current / total * 100);
        this.setText(value + "%");
    }

    /**
     * 根据当前值与总量同时更新文本与进度
     *
     * @param current 当前值
     * @param total   总量
     */
    public void setValue(double current, double total) {
        this.setText(current, total);
        this.setProgress(current, total);
    }

    /**
     * 获取进度值
     *
     * @return 进度值
     */
    public double getProgress() {
        return this.progressBar().getProgress();
    }
}
