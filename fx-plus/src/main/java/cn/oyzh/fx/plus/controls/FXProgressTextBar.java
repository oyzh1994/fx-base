package cn.oyzh.fx.plus.controls;

import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import javafx.geometry.Pos;

public class FXProgressTextBar extends FXHBox {

    {
        this.addChild(new FXProgressBar());
        this.addChild(new FXLabel());
        this.setAlignment(Pos.CENTER_LEFT);
    }

    public FXProgressBar progressBar() {
        return (FXProgressBar) this.getFirstChild();
    }

    public FXLabel label() {
        return (FXLabel) this.getChild(1);
    }

    public void setProgress(double progress) {
        FXProgressBar progressBar = this.progressBar();
        progressBar.progress(progress);
    }

    public void setProgress(double current, double total) {
        this.setProgress(current / total);
    }

    public void setText(String text) {
        FXLabel label = this.label();
        label.text(text);
    }

    public void setText(double current, double total) {
        int value = (int) (current / total * 100);
        this.setText(value + "%");
    }

    public void setValue(double current, double total) {
        this.setText(current, total);
        this.setProgress(current, total);
    }

    public double getProgress() {
        return this.progressBar().getProgress();
    }
}
