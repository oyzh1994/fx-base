package cn.oyzh.fx.plus.controls.image;

import cn.oyzh.fx.plus.adapter.PropAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * @author oyzh
 * @since 2020/10/29
 */
public class FXImageView extends ImageView implements FlexAdapter, NodeAdapter, PropAdapter, TipAdapter {

    {
        NodeManager.init(this);
    }

    public FXImageView() {
        super();
    }

    public FXImageView( Image image) {
        super(image);
    }

    public FXImageView( String url) {
        this.setUrl(url);
    }

    public FXImageView( String url, double size) {
        this.setUrl(url);
        this.setFitWidth(size);
        this.setFitHeight(size);
    }

    public FXImageView( Image image, double size) {
        this.setImage(image);
        this.setFitWidth(size);
        this.setFitHeight(size);
    }

    public FXImageView( Image image, double w, double h) {
        this.setImage(image);
        this.setFitWidth(w);
        this.setFitHeight(h);
    }

    public void setUrl( String url) {
        this.setProp("url", url);
        super.setImage(FXUtil.getImage(url));
    }

    public String getUrl() {
        return this.getProp("url");
    }

    @Override
    public void initNode() {
        this.setPickOnBounds(true);
        this.setPreserveRatio(true);
//        this.setFocusTraversable(false);
    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }
}
