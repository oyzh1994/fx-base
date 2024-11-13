package cn.oyzh.fx.terminal.command;

import cn.oyzh.common.util.ArrayUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;

/**
 * 终端命令
 *
 * @author oyzh
 * @since 2023/7/21
 */
@Data
@Accessors(chain = true, fluent = true)
public class TerminalCommand {

    /**
     * 命令
     */
    private String command;

    /**
     * 参数列表
     */
    private String[] args;

    public void parseArgs(String[] words) {
        if (ArrayUtil.isNotEmpty(words)) {
            this.command = words[0];
            this.args = ArrayUtil.sub(words, 1, words.length);
        }
    }

    public List<String> argsList() {
        if (this.args == null || this.args.length == 0) {
            return Collections.emptyList();
        }
        return List.of(this.args);
    }

}
