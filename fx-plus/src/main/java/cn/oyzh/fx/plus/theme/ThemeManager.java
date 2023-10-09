package cn.oyzh.fx.plus.theme;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.List;

public class ThemeManager {

   public static Theme Current_Theme = Theme.LIGHT;

   public static void changeTheme(Theme theme) {
       if (theme == Current_Theme) {
           return;
       }
       Current_Theme = theme;
       List<Window> windows = Window.getWindows();
       for (Window window : windows) {
           if (!window.isShowing()) {
               continue;
           }
           if (window instanceof Stage stage && stage.getScene() != null && stage.getScene().getRoot() != null) {
               changeTheme(stage.getScene().getRoot(), theme);
           }
       }
   }

   private static void changeTheme(Node node, Theme theme) {
       if (node instanceof ThemeAdapter adapter) {
           adapter.onThemeChanged(theme);
       }
       if (node instanceof Parent parent) {
           for (Node node1 : parent.getChildrenUnmodifiable()) {
               changeTheme(node1, theme);
           }
       }
   }
}
