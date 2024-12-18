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
