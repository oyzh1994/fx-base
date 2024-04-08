package cn.oyzh.fx.plus.controls;

import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.util.ResourceUtil;
import javafx.scene.CacheHint;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * @author oyzh
 * @since 2020/10/29
 */
public class FlexImageView extends ImageView implements FlexAdapter, TipAdapter {

    {
        NodeManager.init(this);
    }

    @Getter
    private String url;

    @Getter
    @Setter
    private String name;

    public FlexImageView() {
        super();
    }

    public FlexImageView(@NonNull Image image) {
        super(image);
    }

    public FlexImageView(@NonNull String url) {
        this.setUrl(url);
    }

    public FlexImageView(@NonNull String url, double size) {
        this.setUrl(url);
        this.setFitWidth(size);
        this.setFitHeight(size);
    }

    public FlexImageView(@NonNull Image image, double size) {
        this.setImage(image);
        this.setFitWidth(size);
        this.setFitHeight(size);
    }

    public FlexImageView(@NonNull Image image, double w, double h) {
        this.setImage(image);
        this.setFitWidth(w);
        this.setFitHeight(h);
    }

    public void setUrl(@NonNull String url) {
        this.url = url;
        super.setImage(ResourceUtil.getImage(url));
    }

    @Override
    public void setTipText(String tipText) {
        TipAdapter.super.tipText(tipText);
    }

    @Override
    public String getTipText() {
        return TipAdapter.super.tipText();
    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }

    @Override
    public String getFlexWidth() {
        return FlexAdapter.super.flexWidth();
    }

    @Override
    public void setFlexWidth(String flexWidth) {
        FlexAdapter.super.flexWidth(flexWidth);
    }

    public String getFlexHeight() {
        return FlexAdapter.super.flexHeight();
    }

    @Override
    public void setFlexHeight(String flexHeight) {
        FlexAdapter.super.flexHeight(flexHeight);
    }

    @Override
    public String getFlexX() {
        return FlexAdapter.super.flexX();
    }

    @Override
    public void setFlexX(String flexX) {
        FlexAdapter.super.flexX(flexX);
    }

    @Override
    public String getFlexY() {
        return FlexAdapter.super.flexY();
    }

    @Override
    public void setFlexY(String flexY) {
        FlexAdapter.super.flexY(flexY);
    }

    @Override
    public double getRealWidth() {
        return FlexAdapter.super.realWidth();
    }

    @Override
    public void setRealWidth(double width) {
        FlexAdapter.super.realWidth(width);
    }

    @Override
    public double getRealHeight() {
        return FlexAdapter.super.realHeight();
    }

    @Override
    public void setRealHeight(double height) {
        FlexAdapter.super.realHeight(height);
    }

    @Override
    public void setStateManager(StateManager manager) {
        FlexAdapter.super.stateManager(manager);
    }

    @Override
    public StateManager getStateManager() {
        return FlexAdapter.super.stateManager();
    }

    @Override
    public void initNode() {
        this.setPickOnBounds(true);
        this.setPreserveRatio(true);
        this.setFocusTraversable(false);
    }
}
