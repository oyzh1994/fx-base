package cn.oyzh.fx.rdp.test.clipboard;

import com.tangluobo.rdp4j.clipboard.FxSystemClipboardAdapter;
import com.tangluobo.rdp4j.clipboard.SystemClipboardAdapter;
import javafx.application.Platform;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 剪贴板所有权回归验证（macOS）。
 *
 * <p>修复点：本通道发布过文件列表后 publishedText 为 null，旧逻辑的
 * {@code owned && publishedText != null && !publishedText.equals(text)}
 * 判断恒为 false，ownsClipboard 永久卡在 true，导致 announceLocalFormats
 * 一直跳过通告，远程 Windows 右键菜单不会出现"粘贴"。</p>
 *
 * <p>本程序直接运行（无 JUnit 依赖）。用法：</p>
 * <pre>
 *   java --enable-native-access=ALL-UNNAMED -cp &lt;test classpath&gt; \
 *        cn.oyzh.fx.rdp.test.clipboard.ClipboardOwnershipRegression
 * </pre>
 */
public final class ClipboardOwnershipRegression {

    public static void main(String[] args) throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        new Thread(() -> Platform.startup(started::countDown), "clipboard-test-fx").start();
        if (!started.await(15, TimeUnit.SECONDS)) {
            fail("JavaFX 平台启动超时");
        }

        FxSystemClipboardAdapter adapter = new FxSystemClipboardAdapter();

        // 步骤1：本通道发布一个文件列表（publishedText=null, ownsClipboard=true）
        File tmp = Files.createTempFile("rdp-clipboard-regression", ".txt").toFile();
        Files.write(tmp.toPath(), "payload".getBytes("UTF-8"));
        CountDownLatch published = new CountDownLatch(1);
        Platform.runLater(() -> {
            adapter.writeFileList(List.of(tmp));
            published.countDown();
        });
        if (!published.await(5, TimeUnit.SECONDS)) {
            fail("发布文件列表超时");
        }

        // 步骤2：用户从外部拷贝一段文本（模拟 Command+C）
        String text = "USER-COPIED-" + System.currentTimeMillis();
        Process process = new ProcessBuilder("/bin/sh", "-c",
                "printf '%s' '" + text + "' | pbcopy").start();
        if (process.waitFor() != 0) {
            fail("设置系统剪贴板失败");
        }
        Thread.sleep(300);

        // 步骤3：读取，所有权应已被识别为"非本通道"
        SystemClipboardAdapter.LocalContents contents = adapter.read();
        System.out.println("剪贴板文本=" + contents.text());
        System.out.println("ownedByUs=" + contents.ownedByUs());
        if (!text.equals(contents.text())) {
            fail("未读到用户拷贝的文本");
        }
        if (contents.ownedByUs()) {
            fail("ownsClipboard 卡在 true：本地新拷贝的文本将永远不通告给远程");
        }

        System.out.println("RESULT=PASS");
        System.exit(0);
    }

    private static void fail(String message) {
        System.out.println("RESULT=FAIL: " + message);
        System.exit(1);
    }
}
