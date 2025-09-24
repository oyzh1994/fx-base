package cn.oyzh.fx.editor.tm4javafx;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.InputMethodRequests;
import javafx.scene.input.InputMethodTextRun;
import jfx.incubator.scene.control.richtext.TextPos;
import jfx.incubator.scene.control.richtext.model.StyledInput;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author oyzh
 * @since 2025-09-24
 */
public class EditorUtil {

    /**
     * 安装输入法支持
     *
     * @param editor 编辑器
     */
    public static void setupIMESupport(Editor editor) {
        // 上一次未提交位置记录
        AtomicReference<Integer> lastComposedEnd = new AtomicReference<>();
        AtomicReference<Integer> lastComposedStart = new AtomicReference<>();
        // 输入法文本变更事件
        editor.addEventFilter(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED, event -> {
            // 删除未提交内容
            Integer end = lastComposedEnd.get();
            Integer start = lastComposedStart.get();
            if (start != null && end != null) {
                editor.deleteText(start, end);
            }
            TextPos textPos = editor.getCaretPosition();
            int caretPosition = editor.caretPosition();
            // 如果已经提交的文本不为空，则插入到当前位置
            String committed = event.getCommitted();
            ObservableList<InputMethodTextRun> composed = event.getComposed();
            if (StringUtil.isNotEmpty(committed)) {
                StyledInput input = StyledInput.of(committed);
                // 插入内容
                editor.insertText(textPos, input);
                int caretEnd = caretPosition + committed.length();
                // 移动光标到插入位置后
                editor.positionCaret(caretEnd);
                // 清除未提交内容位置
                lastComposedEnd.set(null);
                lastComposedStart.set(null);
            } else if (CollectionUtil.isNotEmpty(event.getComposed())) {
                StringBuilder builder = new StringBuilder();
                for (InputMethodTextRun run : composed) {
                    builder.append(run.getText());
                }
                StyledInput input = StyledInput.of(builder.toString());
                // 插入内容
                editor.insertText(textPos, input);
                int caretEnd = caretPosition + builder.length();
                // 移动光标到插入位置后
                editor.positionCaret(caretEnd);
                // 记录未提交内容位置
                lastComposedEnd.set(caretEnd);
                lastComposedStart.set(caretPosition);
            }
            event.consume();
        });
        // 输入法请求
        editor.setInputMethodRequests(new InputMethodRequests() {
            @Override
            public Point2D getTextLocation(int offset) {
                Optional<Bounds> bounds = editor.getCaretBounds();
                // 返回光标位置，以便输入法窗口可以正确显示
                return bounds.map(value -> new Point2D(value.getMinX(), value.getMinY())).orElseGet(() -> new Point2D(0, 0));
            }

            @Override
            public int getLocationOffset(int x, int y) {
                return 0;
            }

            @Override
            public void cancelLatestCommittedText() {
            }

            @Override
            public String getSelectedText() {
                return editor.getSelectedText();
            }
        });
    }
}
