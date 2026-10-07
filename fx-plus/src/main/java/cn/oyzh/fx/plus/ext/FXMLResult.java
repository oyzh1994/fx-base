package cn.oyzh.fx.plus.ext;


/**
 * fxml 加载结果，封装加载得到的节点与控制器
 *
 * @author oyzh
 * @since 2025-08-21
 */
public class FXMLResult {

    /**
     * 节点
     */
    private Object node;

    /**
     * 控制器
     */
    private Object controller;

    /**
     * 构造FXML结果对象。
     *
     * @param node 节点
     * @param controller 控制器
     */
    public FXMLResult(Object node, Object controller) {
        this.node = node;
        this.controller = controller;
    }

    /**
     * 获取节点。
     *
     * @return 节点
     */
    public Object getNode() {
        return node;
    }

    /**
     * 设置节点。
     *
     * @param node 节点
     */
    public void setNode(Object node) {
        this.node = node;
    }

    /**
     * 获取控制器。
     *
     * @return 控制器
     */
    public Object getController() {
        return controller;
    }

    /**
     * 设置控制器。
     *
     * @param controller 控制器
     */
    public void setController(Object controller) {
        this.controller = controller;
    }

    /**
     * 获取节点（泛型转换）
     *
     * @param <T> 节点类型
     * @return 节点
     */
    public <T> T node() {
        return (T) this.node;
    }

    /**
     * 获取控制器（泛型转换）
     *
     * @param <T> 控制器类型
     * @return 控制器
     */
    public <T> T controller() {
        return (T) this.controller;
    }
}
