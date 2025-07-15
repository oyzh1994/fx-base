package cn.oyzh.fx.plus.tray;

import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageExt;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.stage.StageStyle;

import java.awt.Image;
import java.awt.TrayIcon;
import java.awt.event.MouseEvent;

/**
 * 托盘图标
 *
 * @author oyzh
 * @since 2023/3/2
 */
public class TrayImage extends TrayIcon implements ThemeAdapter {

    /**
     * 窗口
     */
    private StageExt stage;

    /**
     * 菜单
     */
    private TrayMenu menu;

    // private static Class<?> clazz;
    //
    // static {
    //     try {
    //         clazz = java.lang.Class.forName("sun.awt.windows.WTrayIconPeer");
    //         // Field field1 = clazz.getDeclaredField("TRAY_ICON_WIDTH");
    //         // field1.setAccessible(true);
    //         // field1.set(null, 64);
    //         // Field field2 = clazz.getDeclaredField("TRAY_ICON_HEIGHT");
    //         // field2.setAccessible(true);
    //         // field2.set(null, 64);
    //         // int maskSize = 64 * 64 / 8;
    //         // Field field3 = clazz.getDeclaredField("TRAY_ICON_MASK_SIZE");
    //         // field3.setAccessible(true);
    //         // field3.set(null, maskSize);
    //     } catch (ClassNotFoundException e) {
    //         e.printStackTrace();
    //     }
    // }

    public TrayImage(Image image) {
        super(image);
        // ThreadUtil.start(() -> {
        //     this.setImage1(image);
        //
        // }, 3000);
        // ThreadUtil.start(() -> {
        //     this.setImage1(image);
        //
        // }, 6000);
    }

    // Object peer = null;
    //
    // int size = 32;
    // // @Override
    // // public void setImage(Image image) {
    // //     super.setImage(image);
    // //     if (peer == null) {
    // //         Field field = null;
    // //         try {
    // //             field = TrayIcon.class.getDeclaredField("peer");
    // //             field.setAccessible(true);
    // //             peer = field.get(this);
    // //         } catch (NoSuchFieldException e) {
    // //             throw new RuntimeException(e);
    // //         } catch (IllegalAccessException e) {
    // //             throw new RuntimeException(e);
    // //         }
    // //     }
    // //     if (peer == null) {
    // //         return;
    // //     }
    // //
    // //     BufferedImage bufImage = new BufferedImage(size,
    // //             size, BufferedImage.TYPE_INT_ARGB);
    // //     Graphics2D gr = bufImage.createGraphics();
    // //     if (gr != null) {
    // //         try {
    // //             gr.setPaintMode();
    // //
    // //             gr.drawImage(image, 0, 0, size,
    // //                     size, null);
    // //             createNativeImage(bufImage);
    // //
    // //
    // //             // Method method = clazz.getDeclaredMethod("updateNativeIcon");
    // //             // method.setAccessible(true);
    // //             // method.invoke(peer, false);
    // //
    // //             Method[] methods = clazz.getDeclaredMethods();
    // //             for (Method method : methods) {
    // //                 if (peer != null && method.getName().equals("updateNativeIcon")) {
    // //                     method.setAccessible(true);
    // //                     // method.invoke(peer, false);
    // //                     method.invoke(peer, true);
    // //                     System.out.println("-------------------");
    // //                     break;
    // //                 }
    // //             }
    // //         } catch (InvocationTargetException e) {
    // //             throw new RuntimeException(e);
    // //         } catch (IllegalAccessException e) {
    // //             throw new RuntimeException(e);
    // //         } catch (NoSuchMethodException e) {
    // //             throw new RuntimeException(e);
    // //         } catch (NoSuchFieldException e) {
    // //             throw new RuntimeException(e);
    // //         } finally {
    // //             gr.dispose();
    // //         }
    // //     }
    // // }
    //
    // @Override
    // public void setImage(Image image) {
    //     super.setImage(image);
    // }
    //
    // void setImage1(Image image){
    //     if (peer == null) {
    //         Field field = null;
    //         try {
    //             field = TrayIcon.class.getDeclaredField("peer");
    //             field.setAccessible(true);
    //             peer = field.get(this);
    //         } catch (NoSuchFieldException e) {
    //             throw new RuntimeException(e);
    //         } catch (IllegalAccessException e) {
    //             throw new RuntimeException(e);
    //         }
    //     }
    //     if (peer == null) {
    //         return;
    //     }
    //
    //     boolean autosize = this.isImageAutoSize();
    //     AffineTransform tx = GraphicsEnvironment.getLocalGraphicsEnvironment().
    //             getDefaultScreenDevice().getDefaultConfiguration().
    //             getDefaultTransform();
    //     int w = Region.clipScale(size, tx.getScaleX());
    //     int h = Region.clipScale(size, tx.getScaleY());
    //     int imgWidth = Region.clipScale(image.getWidth(null), tx.getScaleX());
    //     int imgHeight = Region.clipScale(image.getHeight(null), tx.getScaleY());
    //     BufferedImage bufImage = new BufferedImage(w,
    //             h, BufferedImage.TYPE_INT_ARGB);
    //     Graphics2D gr = bufImage.createGraphics();
    //     if (gr != null) {
    //         try {
    //             gr.setPaintMode();
    //
    //             gr.drawImage(image, 0, 0, (autosize ? w : imgWidth),
    //                     (autosize ? h : imgHeight), null);
    //             createNativeImage(bufImage);
    //
    //
    //             // Method method = clazz.getDeclaredMethod("updateNativeIcon");
    //             // method.setAccessible(true);
    //             // method.invoke(peer, false);
    //
    //             Method[] methods = clazz.getDeclaredMethods();
    //             for (Method method : methods) {
    //                 if (peer != null && method.getName().equals("updateNativeIcon")) {
    //                     method.setAccessible(true);
    //                     // method.invoke(peer, false);
    //                     method.invoke(peer, false);
    //                     System.out.println("-------------------");
    //                     break;
    //                 }
    //             }
    //         } catch (InvocationTargetException e) {
    //             throw new RuntimeException(e);
    //         } catch (IllegalAccessException e) {
    //             throw new RuntimeException(e);
    //         } catch (NoSuchMethodException e) {
    //             throw new RuntimeException(e);
    //         } catch (NoSuchFieldException e) {
    //             throw new RuntimeException(e);
    //         } finally {
    //             gr.dispose();
    //         }
    //     }
    // }
    // void createNativeImage(BufferedImage bimage) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
    //     Raster raster = bimage.getRaster();
    //     byte[] andMask = new byte[size * size / 8];
    //     // byte[] andMask = new byte[64 * 64 / 8];
    //     int[] pixels = ((DataBufferInt) raster.getDataBuffer()).getData();
    //     int npixels = pixels.length;
    //     int ficW = raster.getWidth();
    //
    //     for (int i = 0; i < npixels; i++) {
    //         int ibyte = i / 8;
    //         int omask = 1 << (7 - (i % 8));
    //
    //         if ((pixels[i] & 0xff000000) == 0) {
    //             // Transparent bit
    //             if (ibyte < andMask.length) {
    //                 andMask[ibyte] |= omask;
    //             }
    //         }
    //     }
    //
    //     if (raster instanceof IntegerComponentRaster) {
    //         ficW = ((IntegerComponentRaster) raster).getScanlineStride();
    //     }
    //
    //
    //     Method[] methods = clazz.getDeclaredMethods();
    //     for (Method method : methods) {
    //         if (peer != null && method.getName().equals("setNativeIcon")) {
    //             // Method method = clazz.getMethod("setNativeIcon");
    //             method.setAccessible(true);
    //             method.invoke(peer, ((DataBufferInt) bimage.getRaster().getDataBuffer()).getData(),
    //                     andMask, ficW, raster.getWidth(), raster.getHeight());
    //             System.out.println("-------------------1");
    //             break;
    //         }
    //     }
    // }

    /**
     * 获取菜单
     *
     * @return 菜单
     */
    public TrayMenu getMenu() {
        if (this.menu == null) {
            this.menu = new TrayMenu();
            // 监听鼠标
            TrayMouseListener listener = new TrayMouseListener();
            listener.setMouseClicked(e -> {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    FXUtil.runLater(this::showMenu);
                }
            });
            this.addMouseListener(listener);
        }
        return this.menu;
    }

    /**
     * 显示菜单
     */
    protected void showMenu() {
        // 初始化窗口
        if (this.stage == null) {
            this.stage = StageManager.newStage(null);
            Scene scene = new Scene(this.menu);
            scene.setFill(Color.TRANSPARENT);
            this.stage.setScene(scene);
            this.stage.setMaximized(false);
            this.stage.setResizable(false);
            this.stage.initStyle(StageStyle.TRANSPARENT);
            // 窗口失焦时，隐藏窗口
            this.stage.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    this.stage.setAlwaysOnTop(false);
                    this.stage.hide();
                }
            });
            this.stage.showingProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    this.menu.init();
                    Robot robot = FXUtil.getRobot();
                    this.stage.setX(robot.getMouseX() - 2);
                    this.stage.setY(robot.getMouseY() - this.stage.getHeight() - 12);
                    this.stage.setWidth(this.menu.realWidth());
                    this.stage.setHeight(this.menu.realHeight());
                    this.stage.setAlwaysOnTop(true);
                    this.stage.toFront();
                    this.stage.requestFocus();
                }
            });
            // 隐藏窗口的任务栏图标
            StageManager.hideTaskbar(this.stage);
        }
        // 显示窗口
        if (!this.stage.isShowing()) {
            // Robot robot = FXUtil.getRobot();
            // this.stage.setX(robot.getMouseX() - 5);
            // this.stage.setY(robot.getMouseY() - this.stage.getHeight() - 15);
            // this.stage.setAlwaysOnTop(true);
            // this.menu.changeTheme(ThemeManager.currentTheme());
            this.stage.show();
        }
    }

    @Override
    public void changeTheme(ThemeStyle style) {
        ThemeAdapter.super.changeTheme(style);
        if (this.menu != null) {
            this.menu.changeTheme(style);
        }
    }
}
