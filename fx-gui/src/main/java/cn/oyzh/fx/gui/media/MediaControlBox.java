package cn.oyzh.fx.gui.media;

import cn.oyzh.fx.gui.svg.glyph.PauseSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.PlaySVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.StopSVGGlyph;
import cn.oyzh.fx.plus.controls.FXProgressBar;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.text.FXSlider;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author oyzh
 * @since 2025-07-17
 */
public class MediaControlBox extends FXVBox {

    /**
     * 媒体时间
     */
    private FXLabel timeLabel;

    /**
     * 播放媒体
     */
    private PlaySVGGlyph play;

    /**
     * 停止媒体
     */
    private StopSVGGlyph stop;

    /**
     * 暂停媒体
     */
    private PauseSVGGlyph pause;

    /**
     * 媒体进度
     */
    private FXProgressBar progress;

    /**
     * 组件1
     */
    private FXHBox box1;

    /**
     * 组件2
     */
    private FXHBox box2;

    /**
     * 媒体结束标志位
     */
    private boolean ended;

    /**
     * 音量组件
     */
    private FXSlider volume;

    /**
     * 音量显示
     */
    private FXLabel volumeLabel;

    /**
     * 媒体播放器
     */
    private MediaPlayer player;

    /**
     * 用户拉伸标志位
     */
    private final AtomicBoolean userSeeking = new AtomicBoolean(false);

    /**
     * 用户音量标志位
     */
    private final AtomicBoolean userVoluming = new AtomicBoolean(false);

    @Override
    public void initNode() {
        super.initNode();

        this.box1 = new FXHBox();
        this.progress = new FXProgressBar();
        this.progress.setProgress(0);
        this.progress.setFlexHeight("50%");
        this.progress.setFlexWidth("100% - 120");
        this.box1.addChild(this.progress);

        this.timeLabel = new FXLabel();
        this.timeLabel.setPrefWidth(110);
        this.box1.addChild(this.timeLabel);
        this.addChild(this.box1);

        this.box2 = new FXHBox();
        this.box2.setFlexHeight("50%");
        this.box2.setFlexWidth("100%");

        this.volume = new FXSlider();
        this.volume.setMax(1D);
        this.volume.setMin(0D);
        this.volumeLabel = new FXLabel();

        this.play = new PlaySVGGlyph();
        this.play.setOnMousePrimaryClicked(e -> this.play());
        this.stop = new StopSVGGlyph();
        this.stop.setOnMousePrimaryClicked(e -> this.stop());
        this.pause = new PauseSVGGlyph();
        this.pause.setOnMousePrimaryClicked(e -> this.pause());

        this.box2.addChild(this.play);
        this.box2.addChild(this.stop);
        this.box2.addChild(this.volume);
        this.box2.addChild(this.volumeLabel);
        this.addChild(this.box2);

        VBox.setMargin(this.box1, new Insets(10, 0, 0, 0));
        VBox.setMargin(this.box2, new Insets(10, 0, 0, 0));
        HBox.setMargin(this.stop, new Insets(0, 0, 0, 5));
        HBox.setMargin(this.volume, new Insets(-2, 0, 0, 5));
        HBox.setMargin(this.progress, new Insets(3, 0, 0, 0));
        HBox.setMargin(this.timeLabel, new Insets(0, 0, 0, 3));
        HBox.setMargin(this.volumeLabel, new Insets(0, 0, 0, 3));

        this.stop.disableProperty().bind(this.play.disableProperty());

        // 处理用户拖动进度条
        this.progress.setOnMousePressed(event -> {
            this.userSeeking.set(true);
        });
        this.progress.setOnMouseReleased(event -> {
            this.userSeeking.set(false);
            if (this.player == null
                    || this.player.getTotalDuration() == Duration.UNKNOWN
                    || this.player.getStatus() == MediaPlayer.Status.UNKNOWN
                    || this.player.getStatus() == MediaPlayer.Status.DISPOSED
            ) {
                return;
            }
            // 计算鼠标点击位置对应的时间
            double mouseX = event.getX();
            double barWidth = this.progress.getWidth();
            double ratio = mouseX / barWidth;
            Duration seekTime = this.player.getTotalDuration().multiply(ratio);
            // 跳转到指定位置
            this.player.seek(seekTime);
        });
        // 音量变化
        this.volume.valueProperty().addListener((observableValue, number, t1) -> {
            if (this.player == null) {
                return;
            }
            this.userVoluming.set(true);
            this.player.setVolume(t1.doubleValue());
            this.userVoluming.set(false);
            int vol = (int) (t1.doubleValue() * 100);
            this.volumeLabel.text(vol + "%");
        });
    }

    /**
     * 播放
     */
    private void play() {
        if (this.player != null) {
            if (this.ended) {
                this.ended = false;
                this.player.setCycleCount(1);
                this.player.seek(Duration.ZERO);
            }
            this.player.play();
            this.box2.setChild(0, this.pause);
        }
    }

    /**
     * 暂停
     */
    private void pause() {
        if (this.player != null) {
            this.player.pause();
            this.box2.setChild(0, this.play);
        }
    }

    /**
     * 停止
     */
    private void stop() {
        if (this.player != null) {
            this.player.stop();
            this.box2.setChild(0, this.play);
        }
    }

    /**
     * 安装
     *
     * @param player 媒体播放器
     */
    public void setup(MediaPlayer player) {
        this.player = player;
        player.setCycleCount(1);
        player.currentTimeProperty().addListener((observableValue, aBoolean, t1) -> {
            if (!this.userSeeking.get()) {
                Duration total = this.player.getTotalDuration();
                double d1 = t1.toMillis() / total.toMillis();
                this.progress.setProgress(d1);
                this.updateTimeLabel(t1, total);
            }
        });
        Runnable failed = () -> this.play.disable();
        Runnable readied = () -> this.play.enable();
        Runnable stopped = () -> this.box2.setChild(0, this.play);
        Runnable started = () -> this.box2.setChild(0, this.pause);
        player.setOnError(() -> {
            this.ended = true;
            failed.run();
            MessageBox.exception(player.getError());
        });
        player.setOnReady(() -> {
            this.ended = false;
            readied.run();
        });
        player.setOnPlaying(() -> {
            this.ended = false;
            started.run();
        });
        player.setOnRepeat(started);
        player.setOnPaused(stopped);
        player.setOnHalted(stopped);
        player.setOnStalled(stopped);
        player.setOnStopped(() -> {
            this.ended = true;
            stopped.run();
        });
        player.setOnEndOfMedia(() -> {
            this.ended = true;
            stopped.run();
        });
        // 音量处理
        player.volumeProperty().addListener((observableValue, number, t1) -> {
            if (!this.userVoluming.get()) {
                this.volume.setValue(t1.doubleValue());
            }
        });
        // 初始化音量
        this.volume.setValue(this.player.getVolume());
    }

    /**
     * 更新时间标签
     *
     * @param currentTime   当前时间
     * @param totalDuration 总时间
     */
    private void updateTimeLabel(Duration currentTime, Duration totalDuration) {
        String currentTimeStr = formatTime(currentTime);
        String totalTimeStr = formatTime(totalDuration);
        this.timeLabel.setText(currentTimeStr + " / " + totalTimeStr);
    }

    /**
     * 格式化时间
     *
     * @param duration 当前位置
     * @return 时间
     */
    private String formatTime(Duration duration) {
        if (duration == null || duration == Duration.UNKNOWN) {
            return "00:00:00";
        }
        int totalSeconds = (int) Math.floor(duration.toSeconds());
        int hours;
        int minutes;
        int seconds;
        if (totalSeconds >= 3600) {
            hours = totalSeconds / 60 / 60;
            int mSec = totalSeconds - hours * 3600;
            minutes = mSec / 60;
            seconds = mSec % 60;
        } else if (totalSeconds >= 60) {
            hours = 0;
            minutes = totalSeconds / 60;
            seconds = totalSeconds % 60;
        } else {
            hours = 0;
            minutes = 0;
            seconds = totalSeconds;
        }
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
