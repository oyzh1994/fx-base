package cn.oyzh.fx.plus.test.menu;

import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.controls.tree.view.FXTreeItem;
import cn.oyzh.fx.plus.controls.tree.view.FXTreeItemValue;
import cn.oyzh.fx.plus.controls.tree.view.FXTreeView;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.Themes;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义标题栏测试，演示原生 HeaderBar 与窗口样式
 *
 * @author oyzh
 * @since 2025-08-18
 */
public class MenuTest extends Application {

    public static void main(String[] args) {
        System.setProperty("javafx.enablePreview", "true");
        System.setProperty("javafx.suppressPreviewWarning", "true");
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // 应用主题
        //ThemeManager.apply(Themes.DRACULA);
        ThemeManager.apply(Themes.PRIMER_LIGHT);

        RichTreeView treeView = new RichTreeView() {

        };

        RichTreeItem root = new RichTreeItem(treeView) {

            @Override
            public int compareTo(Object o) {
                return 0;
            }

            @Override
            public List<? extends MenuItem> getMenuItems() {
                List<FXMenuItem> menuItems = new ArrayList<>();
                FXMenuItem item1 = new FXMenuItem("test1");
                item1.setAccelerator(KeyboardUtil.delete_keyCombination);
                FXMenuItem item2 = new FXMenuItem("test2");
                item2.setAccelerator(KeyboardUtil.rename_keyCombination);
                menuItems.add(item1);
                menuItems.add(item2);
                return menuItems;
            }

        };
        root.setValue(new RichTreeItemValue(){
            @Override
            public String name() {
                return "test";
            }
        });
        treeView.setRoot(root);
        Scene scene = new Scene(treeView, (double) 800.0F, (double) 600.0F);
        stage.setScene(scene);
        stage.show();
    }

    public static class MenuTestStarter {
        public static void main(String[] args) {
            MenuTest.main(args);
        }
    }
}
