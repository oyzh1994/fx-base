package cn.oyzh.fx.xserver.tools;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.File;
import java.util.List;

/**
 * A macOS drag source with a fixed, always-on-top window: press on it and drag to start a **real
 * platform drag** carrying a real file - the gesture C5's host half exists for.
 *
 * <pre>
 *   java -cp "$(cat target/tools-cp.txt)" tools/host-drag-source.java &lt;file&gt; [x] [y]
 * </pre>
 *
 * <p><b>Why not Finder.</b> A Finder-sourced drag cannot be driven from a script in this
 * environment: the window is one of several stacked ones, its position moves between runs, and every
 * new shell command raises the IDE over it. This window is `setAlwaysOnTop`, so it cannot be
 * covered, and its position is the caller's.
 *
 * <p><b>The trap, measured 2026-09-27: a synthetic click on a window whose application is not
 * active is swallowed as the activation click.</b> The first `CGEventPost` drag from here did
 * nothing at all and the window's own handlers never fired; three clicks at it first (each
 * `/tmp/host-drag 1250 765 1250 765` is a click when both ends are the same point) made the drag
 * work, and the process then prints its own `drag started`. Drive it with `tools/host-drag.c`
 * (`clang -o /tmp/host-drag tools/host-drag.c -framework ApplicationServices`), and note that the
 * kit's own `host-drag.c` is too abrupt to deliver a *drop* - a variant with ~700 ms before the
 * button goes up is what a drop needs.
 */
public class HostDragSource {

    static String path = "/tmp/xdnddir/dropped.txt";
    static int x = 1150;
    static int y = 700;

    public static void main(String[] args) {
        if (args.length > 0) {
            path = args[0];
        }
        if (args.length > 2) {
            x = Integer.parseInt(args[1]);
            y = Integer.parseInt(args[2]);
        }
        Application.launch(DragApp.class, args);
    }

    /** Public and static because JavaFX reflects on it. */
    public static class DragApp extends Application {
        @Override
        public void start(Stage stage) {
            Label label = new Label("DRAG ME\n" + new File(HostDragSource.path).getName());
            label.setStyle("-fx-font-size: 14px;");
            StackPane root = new StackPane(label);
            root.setStyle("-fx-background-color: #fffbe6; -fx-border-color: #b58900;"
                    + " -fx-border-width: 2;");
            label.setOnMousePressed(e -> System.out.println("press at " + e.getScreenX() + "," + e.getScreenY()));
            label.setOnMouseDragged(e -> System.out.println("dragged to " + e.getScreenX() + "," + e.getScreenY()));
            label.setOnDragDetected(event -> {
                Dragboard board = label.startDragAndDrop(TransferMode.COPY);
                ClipboardContent content = new ClipboardContent();
                content.putFiles(List.of(new File(HostDragSource.path)));
                board.setContent(content);
                System.out.println("drag started with " + HostDragSource.path);
                event.consume();
            });
            stage.setScene(new Scene(root, 200, 110));
            stage.setX(HostDragSource.x);
            stage.setY(HostDragSource.y);
            stage.setAlwaysOnTop(true);
            stage.setTitle("drag source");
            stage.show();
            System.out.println("source at " + HostDragSource.x + "," + HostDragSource.y);
        }
    }
}
