package cn.oyzh.fx.plus.controls.area;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private long maxLine = -1;

    // @Getter
    // private boolean autoScroll;

    // /**
    //  * 缓冲区大小
    //  */
    // @Getter
    // @Setter
    // private int buffSize = 20;

    // /**
    //  * 文本任务
    //  */
    // private Future<?> textTask;
    //
    // /**
    //  * 文本队列
    //  */
    // private Queue<String> textQueue;

    // private ChangeListener<String> autoScrollListener;
    //
    // public void setAutoScroll(boolean autoScroll) {
    //     this.autoScroll = autoScroll;
    //     if (autoScroll) {
    //         if (this.autoScrollListener == null) {
    //             this.autoScrollListener = (observable, oldValue, newValue) -> this.setScrollTop(Double.MAX_VALUE);
    //         }
    //         this.textProperty().addListener(this.autoScrollListener);
    //     } else {
    //         if (this.autoScrollListener != null) {
    //             this.textProperty().removeListener(this.autoScrollListener);
    //             this.autoScrollListener = null;
    //         }
    //     }
    // }

    // @Override
    // public void appendLines(Collection<String> list) {
    //     if (CollUtil.isNotEmpty(list)) {
    //         // // 初始化队列
    //         // if (this.textQueue == null) {
    //         //     this.textQueue = new ConcurrentLinkedQueue<>();
    //         // }
    //         StringBuilder builder = new StringBuilder();
    //         for (String s : list) {
    //             builder.append(s).append(FXTextArea.LINE_SEPARATOR);
    //         }
    //         this.appendText(builder.toString());
    //     }
    // }

    // @Override
    // public void appendLine(String s) {
    //     if (s != null) {
    //         // // 初始化队列
    //         // if (this.textQueue == null) {
    //         //     this.textQueue = new ConcurrentLinkedQueue<>();
    //         // }
    //         // this.textQueue.add(s + FXTextArea.LINE_SEPARATOR);
    //         // this.initTask();
    //         this.appendText(s + FXTextArea.LINE_SEPARATOR);
    //     }
    // }

    // /**
    //  * 等待文本消耗完成
    //  */
    // public void waitTextExpend() {
    //     while (CollUtil.isNotEmpty(this.textQueue)) {
    //         ThreadUtil.sleep(10);
    //     }
    // }

    // /**
    //  * 初始化任务
    //  */
    // private void initTask() {
    //     // 初始化任务
    //     if (this.textTask == null) {
    //         // 创建任务
    //         this.textTask = ExecutorUtil.start(this::consumeText, 0, 5);
    //     }
    // }

    // /**
    //  * 消费文本
    //  */
    // private void consumeText() {
    //     // 取出数据
    //     if (!this.textQueue.isEmpty()) {
    //         StringBuilder builder = new StringBuilder();
    //         do {
    //             builder.append(this.textQueue.poll());
    //         } while (!this.textQueue.isEmpty());
    //         // 追加内容
    //         if (!builder.isEmpty()) {
    //             this.appendText(builder.toString());
    //         }
    //     }
    // }

    @Override
    public void appendText(String s) {
        if (s != null) {
            try {
                // 检测最大行
                if (this.maxLine != -1) {
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
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // @Override
    // public void clear() {
    //     if (this.textTask != null) {
    //         ExecutorUtil.cancel(this.textTask);
    //         this.textTask = null;
    //     }
    //     CollUtil.clear(this.textQueue);
    //     super.clear();
    // }

    // @Override
    // public void scrollToEnd() {
    //     this.waitTextExpend();
    //     super.scrollToEnd();
    // }
}
