package cn.oyzh.fx.plus.controls.media;

import cn.oyzh.fx.plus.adapter.PropAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

/**
 * @author oyzh
 * @since 2025-07-17
 */
public class FXMediaView extends MediaView implements FlexAdapter, NodeAdapter, PropAdapter, TipAdapter {

    {
        NodeManager.init(this);
    }

    public FXMediaView() {
        super();
    }

    public FXMediaView(MediaPlayer player) {
        super(player);
    }

    public FXMediaView(Media media) {
        this(new MediaPlayer(media));
    }

    public FXMediaView(String url) {
        this.setUrl(url);
    }

    public void setUrl(String url) {
        this.setProp("url", url);
        MediaPlayer player = new MediaPlayer(FXUtil.getMedia(url));
        super.setMediaPlayer(player);
    }

    public String getUrl() {
        return this.getProp("url");
    }

    @Override
    public void initNode() {
        this.setPickOnBounds(true);
        this.setPreserveRatio(true);
    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    /**
     * 播放
     */
    public void play() {
        if (this.getMediaPlayer() != null) {
            this.getMediaPlayer().play();
        }
    }

    /**
     * 停止
     */
    public void stop() {
        if (this.getMediaPlayer() != null) {
            this.getMediaPlayer().stop();
        }
    }

    /**
     * 销毁
     */
    public void dispose() {
        if (this.getMediaPlayer() != null) {
            this.getMediaPlayer().dispose();
        }
    }
}

