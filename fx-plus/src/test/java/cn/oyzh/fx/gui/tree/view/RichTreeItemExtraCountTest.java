package cn.oyzh.fx.gui.tree.view;

import javafx.application.Platform;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RichTreeItemExtraCountTest {

    @BeforeClass
    public static void startJavaFx() throws Exception {
        CountDownLatch startup = new CountDownLatch(1);
        try {
            Platform.startup(startup::countDown);
        } catch (IllegalStateException ex) {
            startup.countDown();
        }
        assertTrue("JavaFX toolkit startup timed out", startup.await(20, TimeUnit.SECONDS));
    }

//    @Test
//    public void cachedAndLoadedChildCountsAreFormatted() throws Throwable {
//        AtomicReference<Throwable> error = new AtomicReference<>();
//        CountDownLatch done = new CountDownLatch(1);
//        Platform.runLater(() -> {
//            try {
//                TestItem item = new TestItem();
//                assertNull(item.extraCount(null));
//                assertEquals(" (3)", item.extraCount(3));
//
//                item.addChild(new TestItem());
//                assertEquals(" (1)", item.extraCount(null));
//
//                item.clearChild();
//                item.setLoaded(true);
//                assertEquals(" (0)", item.extraCount(null));
//            } catch (Throwable ex) {
//                error.set(ex);
//            } finally {
//                done.countDown();
//            }
//        });
//        assertTrue("Tree count test timed out", done.await(20, TimeUnit.SECONDS));
//        if (error.get() != null) {
//            throw new AssertionError("Tree count test failed", error.get());
//        }
//    }

    private static class TestItem extends RichTreeItem<RichTreeItemValue> {

        private TestItem() {
            super(null);
            this.setValue(new RichTreeItemValue(this));
        }
    }
}
