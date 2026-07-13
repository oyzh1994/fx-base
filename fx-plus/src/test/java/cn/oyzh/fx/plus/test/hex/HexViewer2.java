package cn.oyzh.fx.plus.test.hex;

import cn.oyzh.fx.plus.controls.hex.HexView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

/**
 * JavaFX Hex 查看器
 * <p>
 * 快捷键: Ctrl+O 打开 | Ctrl+G 跳转 | Ctrl+F 搜索 | Ctrl+C 复制
 * | F2 切换列数 | ESC 清除选区 | Ctrl+A 全选
 */
public class HexViewer2 extends Application {

    private HexView hexViewer;
    private Label statusLabel;
    private HBox statusBar;
    private Button themeBtn;

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) {
        hexViewer = new HexView();
        VBox.setVgrow(hexViewer, Priority.ALWAYS);

        // ---- 状态栏 ----
        statusLabel = new Label("Ctrl+O 打开文件 | Ctrl+G 跳转 | Ctrl+F 搜索 | Ctrl+C 复制 | F2 切换列数");

        themeBtn = new Button("🌙"); // 月亮
        themeBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 16;");
        themeBtn.setOnAction(e -> {
//            hexViewer.toggleTheme();
//            themeBtn.setText(hexViewer.getTheme() == HexViewerComponent.Theme.LIGHT ? "☀️" : "🌙");
            refreshStatusBar();
        });

        statusBar = new HBox(statusLabel, themeBtn);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(statusLabel, Priority.ALWAYS);
        statusBar.setPadding(new Insets(2, 4, 2, 4));
        refreshStatusBar();

        // ---- 回调绑定 ----
//        hexViewer.setOnGotoRequest(() -> showGotoDialog(primaryStage));
//        hexViewer.setOnSearchRequest(() -> showSearchDialog(primaryStage));
//        hexViewer.setOnCopyRequest(this::copySelection);

        // 定时更新状态栏
        javafx.animation.AnimationTimer statusTimer = new javafx.animation.AnimationTimer() {
            @Override public void handle(long now) {
                updateStatus();
            }
        };
        statusTimer.start();

        BorderPane root = new BorderPane(hexViewer);
        root.setBottom(statusBar);

        Scene scene = new Scene(root, 900, 600);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.O) {
                FileChooser chooser = new FileChooser();
                chooser.setTitle("打开文件");
                File file = chooser.showOpenDialog(primaryStage);
                if (file != null) {
                    try { hexViewer.openFile(file); primaryStage.setTitle("Hex Viewer - " + file.getName()); }
                    catch (IOException ex) { ex.printStackTrace(); }
                }
                e.consume();
            }
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("Hex Viewer");
        primaryStage.show();
    }

    private void refreshStatusBar() {
        javafx.scene.paint.Color bg = hexViewer.getStatusBg();
        javafx.scene.paint.Color fg = hexViewer.getStatusFg();
        String bgStyle = String.format("-fx-background-color: #%02X%02X%02X;",
                (int)(bg.getRed()*255), (int)(bg.getGreen()*255), (int)(bg.getBlue()*255));
        String fgStyle = String.format("-fx-text-fill: #%02X%02X%02X; -fx-font-size: 12; -fx-padding: 2 6;",
                (int)(fg.getRed()*255), (int)(fg.getGreen()*255), (int)(fg.getBlue()*255));
        Platform.runLater(() -> {
            statusBar.setStyle(bgStyle);
            statusLabel.setStyle(fgStyle);
        });
    }

    private void updateStatus() {
        long size = hexViewer.getFileSize();
        if (size == 0) {
            statusLabel.setText("Ctrl+O 打开文件 | Ctrl+G 跳转 | Ctrl+F 搜索 | Ctrl+C 复制 | F2 切换列数");
            return;
        }
        long focus = hexViewer.getFocusByte();
        long selSize = hexViewer.getSelectionSize();
        String info = String.format("Offset: 0x%X / 0x%X (%s) | %d 列",
                focus, size, formatSize(size), hexViewer.getBytesPerRow());
        if (selSize > 0) info += String.format(" | 选中: %d bytes", selSize);
        String finalInfo = info;
        Platform.runLater(() -> statusLabel.setText(finalInfo));
    }

    // ====== 对话框 ======

    private void showGotoDialog(Stage owner) {
        TextInputDialog dlg = new TextInputDialog(String.format("%X", hexViewer.getFocusByte()));
        dlg.setTitle("跳转到偏移");
        dlg.setHeaderText("输入偏移地址 (十六进制)");
        dlg.setContentText("偏移:");
        dlg.showAndWait().ifPresent(input -> {
            try {
                long off = Long.parseLong(input.trim(), 16);
                hexViewer.gotoOffset(off);
            } catch (NumberFormatException ignored) {}
        });
    }

    private void showSearchDialog(Stage owner) {
        Dialog<byte[]> dlg = new Dialog<>();
        dlg.setTitle("搜索");
        dlg.setHeaderText("输入搜索内容");

        TextField field = new TextField();
        field.setPromptText("十六进制如 FF 00 AB，或纯文本字符串");
        field.setPrefWidth(300);
        ToggleGroup group = new ToggleGroup();
        RadioButton hexBtn = new RadioButton("十六进制");
        hexBtn.setToggleGroup(group);
        hexBtn.setSelected(true);
        RadioButton textBtn = new RadioButton("文本");
        textBtn.setToggleGroup(group);
        HBox opts = new HBox(10, hexBtn, textBtn);
        VBox content = new VBox(8, field, opts);
        dlg.getDialogPane().setContent(content);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dlg.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            String s = field.getText().trim();
            if (s.isEmpty()) return null;
            return hexBtn.isSelected() ? parseHex(s) : s.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        });
        dlg.showAndWait().ifPresent(pattern -> {
            long start = hexViewer.getFocusByte();
            long found = hexViewer.search(pattern, start);
            if (found < 0 && start > 0) found = hexViewer.search(pattern, 0); // 从头重试
            if (found >= 0) hexViewer.gotoOffset(found);
            else new Alert(Alert.AlertType.INFORMATION, "未找到匹配内容").showAndWait();
        });
    }

    private byte[] parseHex(String s) {
        String[] parts = s.trim().replaceAll("[^0-9A-Fa-f\\s]", "").split("\\s+");
        byte[] out = new byte[parts.length];
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) continue;
            out[i] = (byte) Integer.parseInt(parts[i], 16);
        }
        return out;
    }

    private void copySelection() {
        long selSize = hexViewer.getSelectionSize();
        if (selSize == 0) return;
        String hexText = hexViewer.getSelectionHex();
        ClipboardContent cc = new ClipboardContent();
        cc.putString(hexText);
        Clipboard.getSystemClipboard().setContent(cc);
        statusLabel.setText("已复制: " + hexText);
    }

    private static String formatSize(long sz) {
        if (sz < 1024) return sz + " B";
        if (sz < 1048576) return String.format("%.1f KB", sz / 1024.0);
        return String.format("%.1f MB", sz / 1048576.0);
    }
}
