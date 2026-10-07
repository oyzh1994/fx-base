package cn.oyzh.fx.tty;

import cn.oyzh.ssh.util.SSHUtil;
import com.jediterm.terminal.DefaultTerminalCopyPasteHandler;
import org.jetbrains.annotations.Nullable;

/**
 * 终端复制粘贴处理器，在默认实现基础上移除文本中的 ANSI 转义序列。
 *
 * @author oyzh
 * @since 2025-09-15
 */
public class TtyTerminalCopyPasteHandler extends DefaultTerminalCopyPasteHandler {

    @Override
    public @Nullable String getContents(boolean useSystemSelectionClipboardIfAvailable) {
        String contents = super.getContents(useSystemSelectionClipboardIfAvailable);
        if (contents == null) {
            return null;
        }
        return SSHUtil.removeAnsi(contents);
    }
}
