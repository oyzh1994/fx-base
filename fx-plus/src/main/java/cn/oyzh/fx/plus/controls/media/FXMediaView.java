package cn.oyzh.fx.plus.controls.media;

import cn.oyzh.common.object.Destroyable;
import cn.oyzh.fx.plus.adapter.PropAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeDestroyUtil;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

/**
 * 媒体视图控件
 *
 * @author oyzh
 * @since 2025-07-17
 */
public class FXMediaView extends MediaView implements FlexAdapter, NodeAdapter, PropAdapter, TipAdapter, Destroyable {

    {
        NodeManager.init(this);
    }

    /**
     * 构造媒体查看对象。
     */
    public FXMediaView() {
        super();
    }

    /**
     * 构造媒体查看对象。
     *
     * @param player player
     */
    public FXMediaView(MediaPlayer player) {
        super(player);
    }

    /**
     * 构造媒体查看对象。
     *
     * @param media 媒体
     */
    public FXMediaView(Media media) {
        this(new MediaPlayer(media));
    }

    /**
     * 构造媒体查看对象。
     *
     * @param url 地址
     */
    public FXMediaView(String url) {
        this.setUrl(url);
    }

    /**
     * 设置媒体地址
     *
     * @param url 媒体地址
     */
    public void setUrl(String url) {
        this.setProp("url", url);
        MediaPlayer player = new MediaPlayer(FXUtil.getMedia(url));
        super.setMediaPlayer(player);
    }

    /**
     * 获取媒体地址
     *
     * @return 媒体地址
     */
    public String getUrl() {
        return this.getProp("url");
    }

    @Override
    public void initNode() {
        this.setPickOnBounds(true);
        this.setPreserveRatio(true);
        FlexAdapter.super.initNode();
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

    @Override
    public void destroy() {
//        this.stop();
//        this.dispose();
//        this.mediaPlayerProperty().unbind();
//        this.setMediaPlayer(null);
        NodeDestroyUtil.destroyNode(this);
        this.removeNode();
        NodeDestroyUtil.destroyObject(this);
    }
}

