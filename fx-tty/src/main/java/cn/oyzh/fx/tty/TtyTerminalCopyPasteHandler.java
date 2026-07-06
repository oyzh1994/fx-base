package cn.oyzh.fx.tty;

import cn.oyzh.ssh.util.SSHUtil;
import com.jediterm.terminal.DefaultTerminalCopyPasteHandler;
import org.jetbrains.annotations.Nullable;

/**
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
