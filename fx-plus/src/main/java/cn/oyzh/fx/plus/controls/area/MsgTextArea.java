package cn.oyzh.fx.plus.controls.area;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.common.thread.ExecutorUtil;
import cn.oyzh.fx.common.thread.ThreadUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 消息文本域
 *
 * @author oyzh
 * @since 2023/04/08
 */
@Slf4j
@ToString
public class MsgTextArea extends FlexTextArea {

    /**
     * 最大行数
     */
    @Getter
    @Setter
    private Long maxLine;

    /**
     * 缓冲区大小
     */
    @Getter
    @Setter
    private int buffSize = 20;

    /**
     * 文本任务
     */
    private Future<?> textTask;

    /**
     * 文本队列
     */
    private Queue<String> textQueue;

    /**
     * 行分隔符
     */
    public static final String LINE_SEPARATOR = "\n";

    @Override
    public void appendLines(Collection<String> list) {
        if (CollUtil.isNotEmpty(list)) {
            // 初始化队列
            if (this.textQueue == null) {
                this.textQueue = new ConcurrentLinkedQueue<>();
            }
            for (String s : list) {
                this.textQueue.add(s + "\n");
            }
            this.initTask();
        }
    }

    @Override
    public void appendLine(String s) {
        if (s != null) {
            // 初始化队列
            if (this.textQueue == null) {
                this.textQueue = new ConcurrentLinkedQueue<>();
            }
            this.textQueue.add(s + "\n");
            this.initTask();
        }
    }

    /**
     * 等待文本消耗完成
     */
    public void waitTextExpend() {
        while (CollUtil.isNotEmpty(this.textQueue)) {
            ThreadUtil.sleep(10);
        }
    }

    /**
     * 初始化任务
     */
    private void initTask() {
        // 初始化任务
        if (this.textTask == null) {
            // 创建任务
            this.textTask = ExecutorUtil.start(this::consumeText, 0, 5);
        }
    }

    /**
     * 消费文本
     */
    private void consumeText() {
        // 取出数据
        StringBuilder builder = new StringBuilder();
        while (!this.textQueue.isEmpty()) {
            builder.append(this.textQueue.poll());
        }
        // 追加内容
        if (builder.length() > 0) {
            this.appendText(builder.toString());
        }
    }

    @Override
    public void appendText(String s) {
        if (s == null) {
            return;
        }
        try {
            Long startTime = null;
            if (log.isDebugEnabled()) {
                startTime = System.currentTimeMillis();
            }
            // 检测最大行
            if (this.maxLine != null) {
                // 追加文本计数
                long count = StrUtil.count(s, LINE_SEPARATOR);
                // 拼接文本已经大于最大限制，直接清除文本
                if (count >= this.maxLine) {
                    super.clear();
                } else if (this.getText().contains(LINE_SEPARATOR)) {// 计算文本行数，将超过的内容删除
                    String[] texts = this.getText().split(LINE_SEPARATOR);
                    // 总文本行数
                    long lineCount = texts.length + count;
                    // 将超过的内容删除
                    if (lineCount > this.maxLine) {
                        // 获取待删除内容
                        String str = Stream.of(texts).limit(lineCount - this.maxLine).collect(Collectors.joining(LINE_SEPARATOR));
                        // 删除文本
                        this.deleteText(0, str.length());
                    }
                }
            }
            super.appendText(s);
            if (startTime != null) {
                log.debug("appendText cost:{}ms", (System.currentTimeMillis() - startTime));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void clear() {
        if (this.textTask != null) {
            ExecutorUtil.cancel(this.textTask);
            this.textTask = null;
        }
        CollUtil.clear(this.textQueue);
        super.clear();
    }

    @Override
    public void scrollToEnd() {
        this.waitTextExpend();
        super.scrollToEnd();
    }
}
