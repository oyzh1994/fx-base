package cn.oyzh.fx.plus.controls.web;

import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

/**
 * @author oyzh
 * @since 2024/8/19
 */
public class WebViewPane extends Pane implements FlexAdapter {

    {
        NodeManager.init(this);
    }

//    @Override
//    public String getFlexWidth() {
//        return FlexAdapter.super.flexWidth();
//    }
//
//    @Override
//    public void setFlexWidth(String flexWidth) {
//        FlexAdapter.super.flexWidth(flexWidth);
//    }
//
//    @Override
//    public String getFlexHeight() {
//        return FlexAdapter.super.flexHeight();
//    }
//
//    @Override
//    public void setFlexHeight(String flexHeight) {
//        FlexAdapter.super.flexHeight(flexHeight);
//    }
//
//    @Override
//    public String getFlexX() {
//        return FlexAdapter.super.flexX();
//    }
//
//    @Override
//    public void setFlexX(String flexX) {
//        FlexAdapter.super.flexX(flexX);
//    }
//
//    @Override
//    public String getFlexY() {
//        return FlexAdapter.super.flexY();
//    }
//
//    @Override
//    public void setFlexY(String flexY) {
//        FlexAdapter.super.flexY(flexY);
//    }

//    @Override
//    public double getRealWidth() {
//        return FlexAdapter.super.realWidth();
//    }
//
//    @Override
//    public void setRealWidth(double width) {
//        FlexAdapter.super.realWidth(width);
//    }
//
//    @Override
//    public double getRealHeight() {
//        return FlexAdapter.super.realHeight();
//    }
//
//    @Override
//    public void setRealHeight(double height) {
//        FlexAdapter.super.realHeight(height);
//    }

    // @Override
    // public void setStateManager(StateManager manager) {
    //     FlexAdapter.super.stateManager(manager);
    // }
    //
    // @Override
    // public StateManager getStateManager() {
    //     return FlexAdapter.super.stateManager();
    // }

    @Override
    public void initNode() {

    }

    public WebView webView() {
        if (this.isChildEmpty()) {
            WebView webView = new WebView();
            this.setChild(webView);
            webView.minWidthProperty().bind(this.minWidthProperty());
            webView.maxWidthProperty().bind(this.maxWidthProperty());
            webView.prefWidthProperty().bind(this.prefWidthProperty());
            webView.minHeightProperty().bind(this.minHeightProperty());
            webView.maxHeightProperty().bind(this.maxHeightProperty());
            webView.prefHeightProperty().bind(this.prefHeightProperty());
            this.initEngine();
        }
        return (WebView) this.firstChild();
    }

    protected void initEngine() {
        this.getEngine().locationProperty().addListener((observable, oldValue, newValue) -> {
            this.jWindow = null;
            this.jDocument = null;
        });
    }

    public WebEngine getEngine() {
        return this.webView().getEngine();
    }

    private JSObject jWindow;

    private JSObject jDocument;

    public JSObject jWindow() {
        if (this.jWindow == null) {
            this.jWindow = (JSObject) this.executeScript("window");
        }
        return this.jWindow;
    }

    public JSObject jDocument() {
        if (this.jDocument == null) {
            this.jDocument = (JSObject) this.executeScript("window.document");
        }
        return this.jDocument;
    }

    public Object executeScript(String script) {
        if (script != null) {
            return this.getEngine().executeScript(script);
        }
        return null;
    }

    public String cookie() {
        JSObject object = this.jDocument();
        return object == null ? null : (String) object.getMember("cookie");
    }

    public String pageUrl() {
        return this.getEngine().getLocation();
    }

    public void loadUrl(String url) {
        if (url != null) {
            this.getEngine().load(url);
        }
    }

    public void loadContent(String content) {
        if (content != null) {
            this.getEngine().loadContent(content);
        }
    }

    public void loadContent(String content, String contentType) {
        if (content != null) {
            this.getEngine().loadContent(content, contentType);
        }
    }
}
