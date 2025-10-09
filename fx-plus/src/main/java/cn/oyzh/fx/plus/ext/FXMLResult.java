package cn.oyzh.fx.plus.ext;


/**
 * @author oyzh
 * @since 2025-08-21
 */
public class FXMLResult {

    private Object node;

    private Object controller;

    public FXMLResult(Object node, Object controller) {
        this.node = node;
        this.controller = controller;
    }

    public Object getNode() {
        return node;
    }

    public void setNode(Object node) {
        this.node = node;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public <T> T node() {
        return (T) this.node;
    }

    public <T> T controller() {
        return (T) this.controller;
    }
}
