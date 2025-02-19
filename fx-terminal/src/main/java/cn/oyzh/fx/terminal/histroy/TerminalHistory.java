package cn.oyzh.fx.terminal.histroy;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.store.jdbc.Column;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * zk命令行历史
 *
 * @author oyzh
 * @since 2023/5/29
 */
@Data
public class TerminalHistory implements ObjectComparator<TerminalHistory>, Serializable {

    /**
     * 命令行
     */
    @Column
    private String line;

    /**
     * 保存时间
     */
    @Column
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
