package cn.oyzh.fx.plus.tray;//package cn.oyzh.fx.plus.tray;
//
//import cn.oyzh.fx.plus.util.FXUtil;
//import javafx.embed.swing.JFXPanel;
//import javafx.scene.Scene;
//import javafx.scene.control.Button;
//import javafx.scene.layout.VBox;
//import javafx.scene.robot.Robot;
//import lombok.Setter;
//
//import javax.swing.JDialog;
//import javax.swing.JFrame;
//import java.awt.Dimension;
//import java.awt.Image;
//import java.awt.TrayIcon;
//import java.awt.Window;
//import java.awt.event.FocusEvent;
//import java.awt.event.FocusListener;
//import java.awt.event.MouseEvent;
//
///**
// * 托盘图标
// *
// * @author oyzh
// * @since 2023/3/2
// */
//public class TrayIconExt extends TrayIcon {
//
//    private JFrame frame;
//
//    private JFXPanel panel;
//
//    /**
//     * 菜单
//     */
//    @Setter
//    private FXTrayMenu menu;
//
//    public TrayIconExt(Image image) {
//        super(image);
//    }
//
//    /**
//     * 获取菜单
//     *
//     * @return 菜单
//     */
//    public FXTrayMenu getMenu() {
//        if (this.menu == null) {
//            this.menu = new FXTrayMenu();
//            // 监听鼠标
//            FXTrayMouseListener listener = new FXTrayMouseListener();
//            listener.setMouseClicked(e -> {
//                if (e.getButton() == MouseEvent.BUTTON3) {
//                    FXUtil.runLater(this::showMenu);
//                }
//            });
//            this.addMouseListener(listener);
//            this.panel = new JFXPanel();
//            this.panel.setScene(new Scene(this.menu));
//            //this.panel.setSize((int) this.menu.getPrefWidth(), (int) this.menu.getPrefHeight());
//        }
//        return this.menu;
//    }
//
//    /**
//     * 显示菜单
//     */
//    private void showMenu() {
//        if (frame == null) {
//            frame = new JFrame();
//            frame.setType(Window.Type.UTILITY);
//            frame.setUndecorated(true);
//            frame.add(panel);
//            frame.setMaximumSize(new Dimension((int) this.menu.getPrefWidth(), (int) this.menu.getPrefHeight()));
//            frame.setMinimumSize(new Dimension((int) this.menu.getPrefWidth(), (int) this.menu.getPrefHeight()));
//            frame.addFocusListener(new FocusListener() {
//                @Override
//                public void focusGained(FocusEvent e) {
//
//                }
//
//                @Override
//                public void focusLost(FocusEvent e) {
//                    System.out.println("focusLost==========");
//                    //panel.setVisible(false);
//                    frame.setVisible(false);
//                }
//            });
//        }
//        this.panel.setVisible(true);
//        frame.setVisible(true);
//        Robot robot = FXUtil.getRobot();
//        int x = (int) (robot.getMouseX() - 5);
//        int y = (int) (robot.getMouseY() - this.menu.getPrefHeight() - 15);
//        frame.setLocation(x, y);
//        frame.pack();
//        frame.setAlwaysOnTop(true);
//        frame.requestFocus();
//    }
//
//}
