package cn.oyzh.fx.plus.handler;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.plus.keyboard.KeyListener;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * tab按键切换处理
 *
 * @author oyzh
 * @since 2023/4/24
 */
public class TabSwitchHandler {

//    /**
//     * 处理器列表
//     */
//    private static final WeakCache<EventTarget, TabSwitchHandler> CACHE = CacheUtil.newWeakCache();
//
//    /**
//     * 执行初始化
//     *
//     * @param target 对写
//     * @return 处理器
//     */
//    public static TabSwitchHandler init( EventTarget target) {
//        TabSwitchHandler handler = null;
//        if (target instanceof Parent parent) {
//            handler = new TabSwitchHandler(parent);
//            CACHE.put(target, new TabSwitchHandler(parent));
//            return handler;
//        } else if (target instanceof Stage stage) {
//            handler = new TabSwitchHandler(stage);
//            CACHE.put(target, new TabSwitchHandler(stage));
//        }
//        return handler;
//    }
//
//    /**
//     * 是否存在处理器
//     *
//     * @param target 对象
//     * @return 结果
//     */
//    public static boolean exists(EventTarget target) {
//        if (target != null) {
//            return CACHE.containsKey(target);
//        }
//        return false;
//    }
//
//    /**
//     * 执行销毁
//     *
//     * @param target 对象
//     */
//    public static void destroy(EventTarget target) {
//        if (target != null) {
//            TabSwitchHandler handler = CACHE.get(target);
//            if (handler != null) {
//                CACHE.remove(target);
//                handler.destroy();
//            }
//        }
//    }

    /**
     * 根节点
     */
    private WeakReference<Parent> rootRef;

    public TabSwitchHandler(Parent root) {
        this.rootRef = new WeakReference<>(root);
        this.init();
    }

    public TabSwitchHandler(Window window) {
        if (window.getScene() == null || window.getScene().getRoot() == null) {
            throw new RuntimeException("stage.getScene().getRoot() is null!");
        }
        this.rootRef = new WeakReference<>(window.getScene().getRoot());
        this.init();
    }

    /**
     * 初始化
     */
    public void init() {
        if (!this.isInvalid()) {
            KeyListener.listenReleased(this.root(), KeyCode.TAB, this::toNextNode);
        }
    }

    /**
     * 销毁
     */
    public void destroy() {
        if (!this.isInvalid()) {
            KeyListener.unListenReleased(this.root(), KeyCode.TAB);
        }
        this.rootRef = null;
    }

    /**
     * 寻找支持tab切换的节点
     *
     * @param root     根节点
     * @param nodeList 节点列表
     */
    protected void findNodes(Parent root, List<Node> nodeList) {
        for (Node n : root.getChildrenUnmodifiable()) {
            if (n.isManaged() && n.isVisible()) {
                if (n instanceof ComboBoxBase<?>
                        || n instanceof TextInputControl
                        || n instanceof ChoiceBox<?>) {
                    nodeList.add(n);
                }
                if (n instanceof Parent p1) {
                    this.findNodes(p1, nodeList);
                }
            }
        }
    }

    /**
     * 获取下一个支持tab切换的节点
     *
     * @param nodeList 节点列表
     * @param current  当前节点
     * @return 支持tab索引的节点
     */
    protected Node getNextNode(List<Node> nodeList, Node current) {
        List<Node> list = nodeList.parallelStream().filter(f -> f.getViewOrder() > current.getViewOrder()).toList();
        if (!list.isEmpty()) {
            return list.getFirst();
        }
        int index = nodeList.indexOf(current);
        if (index + 1 < nodeList.size()) {
            return nodeList.get(index + 1);
        }
        return null;
    }

    /**
     * 跳转到下一个节点
     *
     * @param event 事件
     */
    protected void toNextNode(KeyEvent event) {
        if (this.isInvalid()) {
            return;
        }
        List<Node> nodeList = new ArrayList<>(128);
        // 寻找节点
        this.findNodes(this.root(), nodeList);
        // 执行排序
        nodeList.sort(Comparator.comparingDouble(Node::getViewOrder));
        // 下一个节点
        Node next = null;
        // 获取下一个支持tab切换的节点
        if (event.getTarget() instanceof Node current) {
            next = this.getNextNode(nodeList, current);
        }
        // 节点为空，获取首个节点
        if (next == null) {
            next = CollectionUtil.getFirst(nodeList);
        }
        // 获取焦点
        if (next != null) {
            next.requestFocus();
        }
    }

    protected boolean isInvalid() {
        return this.rootRef == null || this.rootRef.get() == null;
    }

    protected Parent root() {
        return this.rootRef == null ? null : this.rootRef.get();
    }
}
