package cn.oyzh.fx.plus.test.window;

import cn.oyzh.fx.plus.information.AlertStage;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.PopupMask;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * PopupMask smoke test. Run under Xvfb/X11 with a window manager.
 */
public class PopupMaskSmokeApp {

    private static final List<String> FAILURES = new ArrayList<>();

    private static final boolean LINUX = System.getProperty("os.name", "").toLowerCase().contains("linux");

    public static void main(String[] args) {
        Application.launch(SmokeApp.class, args);
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            System.out.println("PASS " + name);
        } else {
            FAILURES.add(name);
            System.out.println("FAIL " + name);
        }
    }

    private static void checkNear(String name, double expected, double actual) {
        check(name + " expected=" + expected + " actual=" + actual, Math.abs(expected - actual) <= 1);
    }

    public static class SmokeApp extends Application {

        private Stage target;

        private Stage other;

        private PopupMask mask;

        private Alert alert;

        private CountDownLatch maskLatch;

        @Override
        public void start(Stage stage) {
            this.target = stage;
            stage.setTitle("POPUP_MASK_SMOKE_TARGET");
            stage.setX(80);
            stage.setY(60);
            stage.setWidth(800);
            stage.setHeight(600);
            StackPane root = new StackPane(new Button("target"));
            stage.setScene(new Scene(root));
            stage.show();
            StageManager.setPrimaryStage(stage);
            this.step(500, this::testBasicMask);
        }

        private void testBasicMask() {
            this.maskLatch = new CountDownLatch(1);
            CountDownLatch latch = this.maskLatch;
            StageManager.showMask(this.target, () -> {
                try {
                    latch.await(15, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            });
            this.step(700, () -> {
                this.mask = this.findMask();
                check("mask shown", this.mask != null && this.mask.isShowing());
                if (this.mask == null) {
                    this.finish();
                    return;
                }
                checkNear("mask x", this.target.getX(), this.mask.getX());
                checkNear("mask y", this.target.getY(), this.mask.getY());
                checkNear("mask width", this.target.getWidth(), this.mask.getWidth());
                checkNear("mask height", this.target.getHeight(), this.mask.getHeight());
                this.testAlertOverMask();
            });
        }

        private void testAlertOverMask() {
            Window owner = StageManager.resolveDialogOwner(this.target);
            check("dialog owner is platform appropriate", owner == (LINUX ? this.target : this.mask));
            this.alert = new Alert(Alert.AlertType.WARNING);
            this.alert.setTitle("POPUP_MASK_SMOKE_ALERT");
            this.alert.setHeaderText("warning");
            this.alert.setContentText("alert must be above popup mask");
            this.alert.initOwner(owner);
            this.alert.show();
            this.step(700, () -> {
                check("alert shown", this.alert != null && this.alert.isShowing());
                check("alert owner is platform appropriate",
                        this.alert.getOwner() == (LINUX ? this.target : this.mask));
                Window alertWindow = this.alert.getDialogPane().getScene().getWindow();
                System.out.println("FOCUS alert target=" + this.target.isFocused()
                        + " mask=" + this.mask.isFocused()
                        + " alert=" + alertWindow.isFocused());
                if (LINUX) {
                    check("mask yields to native dialog", !this.mask.isShowing());
                } else {
                    check("mask remains under owned dialog", this.mask.isShowing());
                }
                this.alert.close();
                this.step(500, () -> {
                    check("mask restored after dialog closes", this.mask.isShowing());
                    this.testFocusSwitch();
                });
            });
        }

        private void testFocusSwitch() {
            this.other = new Stage();
            this.other.setTitle("POPUP_MASK_SMOKE_OTHER");
            this.other.setX(950);
            this.other.setY(60);
            this.other.setWidth(250);
            this.other.setHeight(150);
            this.other.setScene(new Scene(new StackPane()));
            this.other.show();
            this.other.requestFocus();
            this.step(600, () -> {
                if (LINUX) {
                    check("mask hidden when target loses focus", !this.mask.isShowing());
                }
                this.target.requestFocus();
                this.step(700, () -> {
                    check("mask shown after target refocus", this.mask.isShowing());
                    this.other.close();
                    this.step(400, this::testGeometry);
                });
            });
        }

        private void testGeometry() {
            this.target.setX(210);
            this.target.setY(140);
            this.target.setWidth(700);
            this.target.setHeight(500);
            this.step(700, () -> {
                checkNear("mask x after move", this.target.getX(), this.mask.getX());
                checkNear("mask y after move", this.target.getY(), this.mask.getY());
                checkNear("mask width after resize", this.target.getWidth(), this.mask.getWidth());
                checkNear("mask height after resize", this.target.getHeight(), this.mask.getHeight());
                this.testMinimize();
            });
        }

        private void testMinimize() {
            this.target.setIconified(true);
            this.step(1000, () -> {
                check("mask hidden while target minimized", !this.mask.isShowing());
                this.target.setIconified(false);
                this.step(1200, () -> {
                    check("mask restored with target", this.mask.isShowing());
                    this.testMessageBox();
                });
            });
        }

        private void testMessageBox() {
            PauseTransition close = new PauseTransition(Duration.millis(900));
            close.setOnFinished(event -> {
                AlertStage alertStage = null;
                for (Window window : Window.getWindows()) {
                    if (window instanceof AlertStage stage && stage.isShowing()) {
                        alertStage = stage;
                        break;
                    }
                }
                check("MessageBox stage shown", alertStage != null);
                if (LINUX) {
                    check("mask hidden while MessageBox shown", !this.mask.isShowing());
                } else {
                    check("mask remains under MessageBox", this.mask.isShowing());
                }
                if (alertStage != null) {
                    alertStage.close();
                }
            });
            close.play();
            new Thread(() -> MessageBox.warn("message box must be above popup mask", this.target),
                    "popup-mask-messagebox").start();
            this.step(1800, () -> {
                check("mask restored after MessageBox", this.mask.isShowing());
                this.testCloseTarget();
            });
        }

        private void testCloseTarget() {
            this.maskLatch.countDown();
            this.step(500, () -> {
                check("mask hidden after callback", !this.mask.isShowing());
                CountDownLatch closeLatch = new CountDownLatch(1);
                this.maskLatch = closeLatch;
                Stage closeTarget = new Stage();
                closeTarget.setTitle("POPUP_MASK_SMOKE_CLOSE_TARGET");
                closeTarget.setX(100);
                closeTarget.setY(100);
                closeTarget.setWidth(500);
                closeTarget.setHeight(400);
                closeTarget.setScene(new Scene(new StackPane()));
                closeTarget.show();
                StageManager.showMask(closeTarget, () -> {
                    try {
                        closeLatch.await(10, TimeUnit.SECONDS);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                });
                this.step(600, () -> {
                    PopupMask closeMask = this.findMask();
                    check("close-target mask shown", closeMask != null && closeMask.isShowing());
                    closeTarget.close();
                    this.step(800, () -> {
                        check("mask hidden after target close",
                                closeMask == null || !closeMask.isShowing());
                        closeLatch.countDown();
                        this.step(500, this::testCallbackCompletionWhileUnfocused);
                    });
                });
            });
        }

        private void testCallbackCompletionWhileUnfocused() {
            CountDownLatch completionLatch = new CountDownLatch(1);
            this.maskLatch = completionLatch;
            StageManager.showMask(this.target, () -> {
                try {
                    completionLatch.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            });
            this.step(700, () -> {
                PopupMask completionMask = this.findMask();
                check("completion test mask shown", completionMask != null && completionMask.isShowing());
                Stage focusStage = new Stage();
                focusStage.setTitle("POPUP_MASK_SMOKE_FOCUS_STAGE");
                focusStage.setX(900);
                focusStage.setY(200);
                focusStage.setWidth(250);
                focusStage.setHeight(150);
                focusStage.setScene(new Scene(new StackPane()));
                focusStage.show();
                focusStage.requestFocus();
                this.step(500, () -> {
                    completionLatch.countDown();
                    this.step(800, () -> {
                        check("mask hidden after background completion",
                                completionMask == null || !completionMask.isShowing());
                        check("background completion does not steal focus", focusStage.isFocused());
                        focusStage.close();
                        this.step(300, this::testCallbackException);
                    });
                });
            });
        }

        private void testCallbackException() {
            StageManager.showMask(this.target, () -> {
                throw new IllegalStateException("expected smoke test exception");
            });
            this.step(1000, () -> {
                check("mask hidden after callback exception", this.findMask() == null);
                this.finish();
            });
        }

        private PopupMask findMask() {
            for (Window window : Window.getWindows()) {
                if (window instanceof PopupMask popupMask && popupMask.isShowing()) {
                    return popupMask;
                }
            }
            return null;
        }

        private void step(double millis, Runnable action) {
            PauseTransition pause = new PauseTransition(Duration.millis(millis));
            pause.setOnFinished(event -> action.run());
            pause.play();
        }

        private void finish() {
            if (this.maskLatch != null) {
                this.maskLatch.countDown();
            }
            if (this.alert != null) {
                this.alert.close();
            }
            if (this.other != null) {
                this.other.close();
            }
            System.out.println("SUMMARY failures=" + FAILURES.size() + " " + FAILURES);
            Platform.exit();
            System.exit(FAILURES.isEmpty() ? 0 : 1);
        }
    }
}
