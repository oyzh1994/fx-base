package cn.oyzh.fx.terminal.histroy;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.FileUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.store.json.ArrayFileStore;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * shell命令历史存储
 *
 * @author oyzh
 * @since 2023/5/29
 */
@Deprecated
public abstract class TerminalHistoryStore extends ArrayFileStore<TerminalHistory> {

    /**
     * 数据列表
     */
    protected final List<TerminalHistory> histories = new ArrayList<>();

    @Override
    public List<TerminalHistory> load() {
        if (this.histories.isEmpty()) {
            String text = FileUtil.readString(this.storeFile(), this.charset());
            if (StringUtil.isBlank(text)) {
                return new ArrayList<>();
            }
            this.histories.addAll(JSONUtil.toBeanList(text, TerminalHistory.class));
        }
        return this.histories;
    }

    @Override
    public synchronized boolean add(@NonNull TerminalHistory history) {
        try {
            List<TerminalHistory> list = this.load();
            TerminalHistory last = CollectionUtil.getLast(list);
            if (last != null && last.getLine().equals(history.getLine())) {
                last.setSaveTime(System.currentTimeMillis());
            } else {
                history.setSaveTime(System.currentTimeMillis());
                this.histories.add(history);
            }
            // 更新数据
            return this.save(this.histories);
        } catch (Exception e) {
            JulLog.warn("add error,err:{}", e.getMessage());
        }
        return false;
    }

    @Override
    public synchronized boolean clear() {
        super.clear();
        this.histories.clear();
        return true;
    }
}
