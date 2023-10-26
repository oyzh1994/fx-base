package cn.oyzh.fx.terminal.histroy;

import cn.oyzh.fx.common.util.ObjectComparator;
import lombok.Data;

import java.util.Objects;

/**
 * zk命令行历史
 *
 * @author oyzh
 * @since 2023/5/29
 */
@Data
public class TerminalHistory implements ObjectComparator<TerminalHistory> {

    /**
     * 命令行
     */
    private String line;

    /**
     * 保存时间
     */
    private long saveTime;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof TerminalHistory history) {
            if (!Objects.equals(this.line, history.getLine())) {
                return false;
            }
            return Objects.equals(this.saveTime, history.getSaveTime());
        }
        return false;
    }

    @Override
    public boolean compare(TerminalHistory obj) {
        return this.equals(obj);
    }
}
