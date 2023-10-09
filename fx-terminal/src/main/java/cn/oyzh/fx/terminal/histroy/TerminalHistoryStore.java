package cn.oyzh.fx.terminal.histroy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.common.util.FileStore;
import com.alibaba.fastjson.JSON;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * shell命令历史存储
 *
 * @author oyzh
 * @since 2023/5/29
 */
@Slf4j
public abstract class TerminalHistoryStore extends FileStore<TerminalHistory> {

    /**
     * 数据列表
     */
    protected final List<TerminalHistory> histories = new ArrayList<>();

    @Override
    public List<TerminalHistory> load() {
        if (this.histories.isEmpty()) {
            String text = FileUtil.readString(this.storeFile(), this.charset());
            if (StrUtil.isBlank(text)) {
                return new ArrayList<>();
            }
            this.histories.addAll(JSON.parseArray(text, TerminalHistory.class));
        }
        return this.histories;
    }

    @Override
    public boolean add(@NonNull TerminalHistory history) {
        try {
            List<TerminalHistory> list = this.load();
            TerminalHistory last = CollUtil.getLast(list);
            if (last != null && last.getLine().equals(history.getLine())) {
                last.setSaveTime(System.currentTimeMillis());
            } else {
                history.setSaveTime(System.currentTimeMillis());
                this.histories.add(history);
            }
            // 更新数据
            return this.save(this.histories);
        } catch (Exception e) {
            log.warn("add error,err:{}", e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(@NonNull TerminalHistory cmdHistory) {
        return false;
    }

    @Override
    public boolean delete(@NonNull TerminalHistory cmdHistory) {
        return false;
    }

    /**
     * 清除数据
     */
    public void clear() {
        try {
            List<TerminalHistory> list = this.load();
            // 更新数据
            if (this.histories.removeAll(list)) {
                this.save(this.histories);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
