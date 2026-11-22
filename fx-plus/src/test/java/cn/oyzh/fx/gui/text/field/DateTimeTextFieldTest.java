package cn.oyzh.fx.gui.text.field;

import javafx.application.Platform;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DateTimeTextFieldTest {

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

    @Test
    public void parsesFlexibleDateTimeFormats() {
        LocalDateTime expected = LocalDateTime.of(2026, 3, 10, 0, 0);
        assertEquals(expected, DateTimeTextField.parseDateTime("2026-03-10T00:00"));
        assertEquals(expected, DateTimeTextField.parseDateTime("2026-03-10 00:00"));
        assertEquals(expected, DateTimeTextField.parseDateTime("2026-3-10 0:00"));
        assertEquals(expected, DateTimeTextField.parseDateTime("2026-03-10"));
        assertEquals(expected, DateTimeTextField.parseDateTime("2026-03-10T00:00:00.000"));
        assertEquals(expected, DateTimeTextField.parseDateTime("2026-03-10T00:00:00+08:00"));
        assertEquals(expected, DateTimeTextField.parseDateTime("2026-03-10T00:00:00 +08:00"));
        assertEquals(expected, DateTimeTextField.parseDateTime("20260310000000"));
        assertEquals(LocalDateTime.of(2026, 3, 10, 12, 34, 56),
                DateTimeTextField.parseDateTime("2026/03/10 12:34:56"));
        assertNull(DateTimeTextField.parseDateTime("not-a-date"));
    }

    @Test
    public void formatsValidTextIntoDatabaseFriendlyDateTime() {
        assertEquals("2026-03-10 00:00:00",
                DateTimeTextField.format("2026-03-10T00:00"));
        assertEquals("2026-03-10 12:34:56.123456",
                DateTimeTextField.format(LocalDateTime.of(2026, 3, 10, 12, 34, 56, 123_456_000)));
        assertEquals("CURRENT_TIMESTAMP", DateTimeTextField.format("CURRENT_TIMESTAMP"));
        assertEquals("not-a-date", DateTimeTextField.format("not-a-date"));
    }

    @Test
    public void fieldReadsMinuteOnlyIsoDateTime() throws Throwable {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                DateTimeTextField field = new DateTimeTextField();
                field.setText("2026-03-10T00:00");
                assertEquals(Timestamp.valueOf("2026-03-10 00:00:00"), field.getTimestamp());
                assertNotNull(field.getValue());

                field.setValue(LocalDateTime.of(2026, 3, 10, 0, 0));
                assertEquals("2026-03-10 00:00:00", field.getText());
            } catch (Throwable ex) {
                error.set(ex);
            } finally {
                done.countDown();
            }
        });
        assertTrue("DateTimeTextField test timed out", done.await(20, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError("DateTimeTextField test failed", error.get());
        }
    }
}
