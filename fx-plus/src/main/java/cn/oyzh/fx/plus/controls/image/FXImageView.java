package cn.oyzh.fx.plus.controls.image;

import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * @author oyzh
 * @since 2020/10/29
 */
public class FXImageView extends ImageView implements NodeAdapter, TipAdapter {

    {
        NodeManager.init(this);
    }

    @Getter
    private String url;

    @Getter
    @Setter
    private String name;

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
        this.url = url;
        super.setImage(FXUtil.getImage(url));
    }

    @Override
    public void initNode() {
        this.setPickOnBounds(true);
        this.setPreserveRatio(true);
        this.setFocusTraversable(false);
    }

}
