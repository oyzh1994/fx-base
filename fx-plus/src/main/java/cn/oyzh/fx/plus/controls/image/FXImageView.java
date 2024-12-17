package cn.oyzh.fx.plus.controls.image;

import cn.oyzh.fx.plus.adapter.PropAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.NonNull;

/**
 * @author oyzh
 * @since 2020/10/29
 */
public class FXImageView extends ImageView implements NodeAdapter, PropAdapter, TipAdapter {

    {
        NodeManager.init(this);
    }

    // @Getter
    // private String url;
    //
    // @Getter
    // @Setter
    // private String name;

    public FXImageView() {
        super();
    }

    public FXImageView(@NonNull Image image) {
        super(image);
    }

    public FXImageView(@NonNull String url) {
        this.setUrl(url);
    }

    public FXImageView(@NonNull String url, double size) {
        this.setUrl(url);
        this.setFitWidth(size);
        this.setFitHeight(size);
    }

    public FXImageView(@NonNull Image image, double size) {
        this.setImage(image);
        this.setFitWidth(size);
        this.setFitHeight(size);
    }

    public FXImageView(@NonNull Image image, double w, double h) {
        this.setImage(image);
        this.setFitWidth(w);
        this.setFitHeight(h);
    }

    public void setUrl(@NonNull String url) {
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
        this.setFocusTraversable(false);
    }
}
