package cn.oyzh.fx.gui.text.area;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.fx.plus.controls.text.area.FlexTextArea;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 消息文本域
 *
 * @author oyzh
 * @since 2023/04/08
 */
@ToString
public class MsgTextArea extends FlexTextArea {

    {
        this.setEditable(false);
    }

    /**
     * 最大行数
     */
    @Getter
    @Setter
    private int lineLimit = 3000;

    /**
     * 限制策略
     * 1 触发限制后保留限制行
     * 2 触发限制后清除全部行
     * 3 触发限制后保留90%行内容
     * 4 触发限制后保留70%行内容
     * 5 触发限制后保留50%行内容
     * 6 触发限制后保留30%行内容
     */
    @Getter
    @Setter
    private byte limitPolicy = 1;

    /**
     * 消息队列
     */
    private final Queue<String> queue = new LinkedBlockingQueue<>();

    /**
     * 拼接中标志位
     */
    private final AtomicBoolean appending = new AtomicBoolean(false);

    @Override
    public void appendText(String s) {
        if (s != null) {
            try {
                // 检测最大行
                if (this.lineLimit > 0) {
                    long lineCount = this.lineCount();
                    // 删除超出部分行
                    if (lineCount > this.lineLimit) {
                        this.deleteLimitLine(lineCount);
                    }
                }
//                super.appendText(s);
                this.doAppend(s);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 执行拼接
     */
    private void doAppend(String text) {
        this.queue.add(text);
        if (this.appending.get()) {
            return;
        }
        this.appending.compareAndSet(false, true);
        // 执行拼接
        ThreadUtil.startVirtual(() -> {
            try {
                StringBuilder builder = new StringBuilder();
                do {
                    String line = this.queue.poll();
                    if (line != null) {
                        builder.append(line);
                    }
                } while (!this.queue.isEmpty());
                this.appending.compareAndSet(true, false);
                super.appendText(builder.toString());
            } finally {
                this.appending.compareAndSet(true, false);
            }
        });
    }

    @Override
    public void setTextExt(String text) {
        this.queue.clear();
        super.setTextExt(text);

    }

    /**
     * 删除超出部分行
     *
     * @param lineCount 行计数
     */
    protected void deleteLimitLine(long lineCount) {
        AtomicInteger endPos = new AtomicInteger(0);
        String text = this.getText();
        // 保留未超出部分
        if (this.limitPolicy == 1) {
            text.lines().limit(lineCount - this.lineLimit).forEach(s -> endPos.addAndGet(s.length() + 1));
        } else if (this.limitPolicy == 2) {// 清空
            endPos.set(this.getLength());
        } else if (this.limitPolicy == 3) {// 保留90%
            text.lines().limit((long) (this.lineLimit * 0.1)).forEach(s -> endPos.addAndGet(s.length() + 1));
        } else if (this.limitPolicy == 4) {// 保留70%
            text.lines().limit((long) (this.lineLimit * 0.3)).forEach(s -> endPos.addAndGet(s.length() + 1));
        } else if (this.limitPolicy == 5) {// 保留50%
            text.lines().limit((long) (this.lineLimit * 0.5)).forEach(s -> endPos.addAndGet(s.length() + 1));
        } else if (this.limitPolicy == 6) {// 保留30%
            text.lines().limit((long) (this.lineLimit * 0.7)).forEach(s -> endPos.addAndGet(s.length() + 1));
        }
        // 删除文本
        if (endPos.get() > 0) {
            this.deleteText(0, endPos.get());
        }
    }
}
