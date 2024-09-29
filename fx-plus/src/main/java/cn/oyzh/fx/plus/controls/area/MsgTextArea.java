package cn.oyzh.fx.plus.controls.area;

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
    private long lineLimit = 3000;

    @Override
    public void appendText(String s) {
        if (s != null) {
            try {
                // 检测最大行
                if (this.lineLimit != -1) {
                    // 追加文本计数
                    long count = StringUtil.count(s, LINE_SEPARATOR);
                    // 拼接文本已经大于最大限制，直接清除文本
                    if (count >= this.lineLimit) {
                        super.clear();
                    } else if (this.getText().contains(LINE_SEPARATOR)) {// 计算文本行数，将超过的内容删除
                        String[] texts = this.getText().split(LINE_SEPARATOR);
                        // 总文本行数
                        long lineCount = texts.length + count;
                        // 将超过的内容删除
                        if (lineCount > this.lineLimit) {
                            // 获取待删除内容
                            String str = Stream.of(texts).limit(lineCount - this.lineLimit).collect(Collectors.joining(LINE_SEPARATOR));
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

    // public void replaceLastLine(){
    //     this.replaceText();
    // }
}
