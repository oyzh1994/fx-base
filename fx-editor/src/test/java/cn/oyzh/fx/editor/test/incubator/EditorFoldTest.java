package cn.oyzh.fx.editor.test.incubator;

import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.editor.incubator.EditorFold;
import cn.oyzh.fx.editor.incubator.EditorFormatType;
import cn.oyzh.fx.editor.incubator.EditorHeading;
import cn.oyzh.fx.editor.incubator.EditorSkin;
import com.sun.jfx.incubator.scene.control.richtext.TextCell;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Headful regression for editor paragraph folding.
 */
public class EditorFoldTest {

    @Test
    public void collapsedFoldCanBeExpandedFromItsInlineHeader() {
        startToolkit();
        AtomicReference<TestContext> context = new AtomicReference<>();
        runFxAndWait(() -> {
            Editor editor = new Editor();
            editor.showData("""
                    **思考过程**

                    隐藏的推理内容

                    **回答**

                    可见回答
                    """, EditorFormatType.RAW);
            editor.setFolds(List.of(new EditorFold(
                    "thinking", 0, 3, "思考过程", true, Color.GRAY)));
            StackPane root = new StackPane(editor);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 480, 320));
            stage.show();
            context.set(new TestContext(stage, root, editor));
        });
        try {
            waitForPulse();
            runFxAndWait(() -> {
                Editor editor = context.get().editor();
                HBox header = (HBox) editor.lookup(".editor-fold-header");
                assertNotNull("fold header was not rendered", header);
                assertEquals("隐藏的推理内容", editor.getModel().getPlainText(2));
                assertTrue(editor.getFolds().getFirst().collapsed());
                Label title = (Label) header.lookup(".editor-fold-title");
                assertNotNull(title);
                assertEquals("思考过程", title.getText());

                Event.fireEvent(header, new MouseEvent(MouseEvent.MOUSE_CLICKED,
                        0, 0, 0, 0, MouseButton.PRIMARY, 1,
                        false, false, false, false, true, false, false,
                        true, false, true, null));
            });
            waitForPulse();
            runFxAndWait(() -> {
                Editor editor = context.get().editor();
                assertFalse(editor.getFolds().getFirst().collapsed());
                HBox header = (HBox) editor.lookup(".editor-fold-header");
                assertNotNull(header);
                Label arrow = (Label) header.lookup(".editor-fold-arrow");
                assertNotNull(arrow);
                assertEquals("▼", arrow.getText());
                EditorSkin skin = (EditorSkin) editor.getSkin();
                TextCell cell = skin.getVFlow().getCell(2);
                TextFlow body = (TextFlow) cell.getContent();
                List<Text> thinkingTexts = body.getChildren().stream()
                        .filter(node -> node instanceof Text text
                                && text.getText().contains("隐藏的推理内容"))
                        .map(node -> (Text) node)
                        .toList();
                assertFalse(thinkingTexts.isEmpty());
                assertTrue(thinkingTexts.stream()
                        .allMatch(text -> Color.GRAY.equals(text.getFill())));
            });
        } finally {
            TestContext value = context.get();
            if (value != null) {
                runFxAndWait(value.stage()::close);
            }
        }
    }

    @Test
    public void headingIsRenderedAsAnInlineEditorBlock() {
        startToolkit();
        AtomicReference<TestContext> context = new AtomicReference<>();
        runFxAndWait(() -> {
            Editor editor = new Editor();
            editor.showData("**结果**\n\n查询本机当前时间",
                    EditorFormatType.RAW);
            editor.setHeadings(List.of(new EditorHeading(
                    0, "结果", "editor-block-heading-section")));
            StackPane root = new StackPane(editor);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 480, 240));
            stage.show();
            context.set(new TestContext(stage, root, editor));
        });
        try {
            waitForPulse();
            runFxAndWait(() -> {
                Editor editor = context.get().editor();
                Label heading = (Label) editor.lookup(".editor-block-heading");
                assertNotNull("heading block was not rendered", heading);
                assertEquals("结果", heading.getText());
                assertEquals("**结果**", editor.getModel().getPlainText(0));
            });
        } finally {
            TestContext value = context.get();
            if (value != null) {
                runFxAndWait(value.stage()::close);
            }
        }
    }

    private static void startToolkit() {
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException ignored) {
            // toolkit is already running in the test JVM
        }
    }

    private static void runFxAndWait(Runnable task) {
        if (Platform.isFxApplicationThread()) {
            task.run();
            return;
        }
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch completed = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                task.run();
            } catch (Throwable ex) {
                failure.set(ex);
            } finally {
                completed.countDown();
            }
        });
        try {
            assertTrue("JavaFX task timed out", completed.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AssertionError(ex);
        }
        if (failure.get() != null) {
            throw new RuntimeException(failure.get());
        }
    }

    private static void waitForPulse() {
        runFxAndWait(() -> {
        });
        try {
            Thread.sleep(50);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AssertionError(ex);
        }
        runFxAndWait(() -> {
        });
    }

    private record TestContext(Stage stage, StackPane root, Editor editor) {
    }
}
