package cn.oyzh.fx.plus.controls.image;

import cn.oyzh.common.object.Destroyable;
import cn.oyzh.fx.plus.adapter.PropAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeDestroyUtil;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 图片控件
 *
 * @author oyzh
 * @since 2020-10-29
 */
public class FXImageView extends ImageView implements FlexAdapter, NodeAdapter, PropAdapter, TipAdapter, Destroyable {

    {
        NodeManager.init(this);
    }

    /**
     * 构造镜像查看对象。
     */
    public FXImageView() {
        super();
    }

    /**
     * 构造镜像查看对象。
     *
     * @param image 镜像
     */
    public FXImageView(Image image) {
        super(image);
    }

    /**
     * 构造镜像查看对象。
     *
     * @param url 地址
     */
    public FXImageView(String url) {
        this.setUrl(url);
    }

    /**
     * 构造镜像查看对象。
     *
     * @param url 地址
     * @param size 大小
     */
    public FXImageView(String url, double size) {
        this.setUrl(url);
        this.setFitWidth(size);
        this.setFitHeight(size);
    }

    /**
     * 构造镜像查看对象。
     *
     * @param image 镜像
     * @param size 大小
     */
    public FXImageView(Image image, double size) {
        this.setImage(image);
        this.setFitWidth(size);
        this.setFitHeight(size);
    }

    /**
     * 构造镜像查看对象。
     *
     * @param image 镜像
     * @param w w
     * @param h h
     */
    public FXImageView(Image image, double w, double h) {
        this.setImage(image);
        this.setFitWidth(w);
        this.setFitHeight(h);
    }

    /**
     * 设置图片地址
     *
     * @param url 图片地址
     */
    public void setUrl(String url) {
        this.setProp("url", url);
        super.setImage(FXUtil.getImage(url));
    }

    /**
     * 获取图片地址
     *
     * @return 图片地址
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

    /**
     * 截取当前图片的快照
     *
     * @return 快照图片
     */
    public WritableImage snapshot() {
        return this.snapshot(null, null);
    }

    @Override
    public WritableImage snapshot(SnapshotParameters params, WritableImage image) {
        AtomicReference<WritableImage> imageRef = new AtomicReference<>();
        FXUtil.runWait(() -> {
            WritableImage image1 = super.snapshot(params, image);
            imageRef.set(image1);
        });
        return imageRef.get();
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public void destroy() {
//        this.setImage(null);
        NodeDestroyUtil.destroyNode(this);
//        DestroyAdapter.super.destroy();
    }
}
