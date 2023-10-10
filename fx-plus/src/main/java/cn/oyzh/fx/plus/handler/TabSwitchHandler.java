package cn.oyzh.fx.plus.handler;

import cn.hutool.core.collection.CollUtil;
import cn.oyzh.fx.plus.keyboard.KeyListener;
import cn.oyzh.fx.plus.view.FXStage;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * tab按键切换处理
 *
 * @author oyzh
 * @since 2023/4/24
 */
public class TabSwitchHandler {

    /**
     * 处理器列表
     */
    private static final Map<EventTarget, TabSwitchHandler> HANDLERS = new HashMap<>();

    /**
     * 执行初始化
     *
     * @param obj 对象
     * @return 处理器
     */
    public static TabSwitchHandler init(Object obj) {
        TabSwitchHandler handler = null;
        if (obj instanceof FXStage stage) {
            handler = new TabSwitchHandler(stage.getStage());
            HANDLERS.put(stage.getStage(), handler);
        } else if (obj instanceof Stage stage) {
            handler = new TabSwitchHandler(stage);
            HANDLERS.put(stage, new TabSwitchHandler(stage));
        } else if (obj instanceof Parent parent) {
            handler = new TabSwitchHandler(parent);
            HANDLERS.put(parent, new TabSwitchHandler(parent));
        }
        return handler;
    }

    /**
     * 执行销毁
     *
     * @param obj 对象
     */
    public static void destroy(Object obj) {
        TabSwitchHandler handler = null;
        if (obj instanceof FXStage stage) {
            handler = HANDLERS.remove(stage.getStage());
        } else if (obj instanceof Stage stage) {
            handler = HANDLERS.remove(stage);
        } else if (obj instanceof Parent parent) {
            handler = HANDLERS.remove(parent);
        }
        if (handler != null) {
            handler.destroy();
        }
    }

    /**
     * 根节点
     */
    private final Parent root;

    public TabSwitchHandler(@NonNull Parent root) {
        this.root = root;
        this.init();
    }

    public TabSwitchHandler(@NonNull Stage stage) {
        if (stage.getScene() == null || stage.getScene().getRoot() == null) {
            throw new RuntimeException("stage.getScene().getRoot() is null!");
        }
        this.root = stage.getScene().getRoot();
        this.init();
    }

    /**
     * 初始化
     */
    private void init() {
        KeyListener.listenKeyReleased(this.root, KeyCode.TAB, this::toNextNode);
    }

    /**
     * 销毁
     */
    private void destroy() {
        KeyListener.unListenKeyReleased(this.root, KeyCode.TAB);
    }

    /**
     * 寻找支持tab切换的节点
     *
     * @param root     根节点
     * @param nodeList 节点列表
     */
    private void findNodes(Parent root, @NonNull List<Node> nodeList) {
        for (Node n : root.getChildrenUnmodifiable()) {
            if (n.isManaged() && n.isVisible()) {
                if (n instanceof ComboBoxBase<?>
                        || n instanceof TextInputControl
                        || n instanceof ChoiceBox<?>) {
                    nodeList.add(n);
                }
                if (n instanceof Parent p1) {
                    findNodes(p1, nodeList);
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
    private Node getNextNode(@NonNull List<Node> nodeList, @NonNull Node current) {
        List<Node> list = nodeList.parallelStream().filter(f -> f.getViewOrder() > current.getViewOrder()).toList();
        if (!list.isEmpty()) {
            return list.get(0);
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
    private void toNextNode(KeyEvent event) {
        List<Node> nodeList = new ArrayList<>();
        // 寻找节点
        this.findNodes(this.root, nodeList);
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
            next = CollUtil.getFirst(nodeList);
        }
        // 获取焦点
        if (next != null) {
            next.requestFocus();
        }
    }

}
