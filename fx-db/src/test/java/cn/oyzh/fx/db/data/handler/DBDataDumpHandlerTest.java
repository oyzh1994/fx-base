package cn.oyzh.fx.db.data.handler;

import cn.oyzh.fx.db.DBDialect;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * 数据库数据转储处理器测试
 *
 * @author oyzh
 * @since 2026-10-21
 */
public class DBDataDumpHandlerTest {

    @Test
    public void testContinueWithErrors() throws Exception {
        TestDBDataDumpHandler handler = new TestDBDataDumpHandler();

        Assert.assertTrue(handler.isContinueWithErrors());
        handler.setContinueWithErrors(false);
        Assert.assertFalse(handler.isContinueWithErrors());

        handler.setContinueWithErrors(true);
        handler.handle(new IOException("ignored"));

        handler.setContinueWithErrors(false);
        try {
            handler.handle(new IOException("reported"));
            Assert.fail("遇到错误不继续时应抛出异常");
        } catch (IOException ignored) {
        }
    }

    @Test(expected = InterruptedException.class)
    public void testInterruptIsNotIgnored() throws Exception {
        TestDBDataDumpHandler handler = new TestDBDataDumpHandler();
        handler.handle(new InterruptedException());
    }

    private static class TestDBDataDumpHandler extends DBDataDumpHandler {

        private TestDBDataDumpHandler() {
            super("test", DBDialect.MYSQL);
        }

        private void handle(Exception ex) throws Exception {
            this.handleDumpError(ex);
        }

        @Override
        protected void exception(Exception ex) throws Exception {
            if (ex instanceof InterruptedException) {
                throw ex;
            }
        }

        @Override
        public void doDump() {
        }

        @Override
        protected void writeHeader() {
        }

        @Override
        protected void writeTail() {
        }
    }
}
