package cn.oyzh.fx.editor.incubator;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.fx.gui.text.field.HighlightTextField;

/**
 *
 * @author oyzh
 * @since 2025-09-24
 */
public class EditorUtil {

    //    /**
    //     * 安装输入法支持
    //     * TODO: 由于官方已经支持了输入法，此方法废弃
    //     *
    //     * @param editor 编辑器
    //     */
    //    @Deprecated
    //    public static void setupIMESupport(Editor editor) {
    //        // 上一次未提交位置记录
    //        AtomicReference<Integer> lastComposedEnd = new AtomicReference<>();
    //        AtomicReference<Integer> lastComposedStart = new AtomicReference<>();
    //        // 输入法文本变更事件
    //        editor.addEventFilter(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED, event -> {
    //            // 删除选中内容
    //            if (editor.isSelectedText()) {
    //                SelectionSegment segment = editor.getSelection();
    //                editor.deleteText(segment.getMin(), segment.getMax());
    //            }
    //            // 删除未提交内容
    //            Integer end = lastComposedEnd.get();
    //            Integer start = lastComposedStart.get();
    //            if (start != null && end != null) {
    //                editor.deleteText(start, end);
    //            }
    //            TextPos textPos = editor.getCaretPosition();
    //            int caretPosition = editor.caretPosition();
    //            // 如果已经提交的文本不为空，则插入到当前位置
    //            String committed = event.getCommitted();
    //            ObservableList<InputMethodTextRun> composed = event.getComposed();
    //            if (StringUtil.isNotEmpty(committed)) {
    //                StyledInput input = StyledInput.of(committed);
    //                // 插入内容
    //                editor.insertText(textPos, input);
    //                int caretEnd = caretPosition + committed.length();
    //                // 移动光标到插入位置后
    //                editor.positionCaret(caretEnd);
    //                // 清除未提交内容位置
    //                lastComposedEnd.set(null);
    //                lastComposedStart.set(null);
    //            } else if (CollectionUtil.isNotEmpty(event.getComposed())) {
    //                StringBuilder builder = new StringBuilder();
    //                for (InputMethodTextRun run : composed) {
    //                    builder.append(run.getText());
    //                }
    //                StyledInput input = StyledInput.of(builder.toString());
    //                // 插入内容
    //                editor.insertText(textPos, input);
    //                int caretEnd = caretPosition + builder.length();
    //                // 移动光标到插入位置后
    //                editor.positionCaret(caretEnd);
    //                // 记录未提交内容位置
    //                lastComposedEnd.set(caretEnd);
    //                lastComposedStart.set(caretPosition);
    //            }
    //            event.consume();
    //        });
    //        // 输入法请求
    //        editor.setInputMethodRequests(new InputMethodRequests() {
    //            @Override
    //            public Point2D getTextLocation(int offset) {
    //                Optional<Bounds> bounds = editor.getCaretBounds();
    //                // 返回光标位置，以便输入法窗口可以正确显示
    //                return bounds.map(value -> new Point2D(value.getMinX(), value.getMinY())).orElseGet(() -> new Point2D(0, 0));
    //            }
    //
    //            @Override
    //            public int getLocationOffset(int x, int y) {
    //                return 0;
    //            }
    //
    //            @Override
    //            public void cancelLatestCommittedText() {
    //            }
    //
    //            @Override
    //            public String getSelectedText() {
    //                return editor.getSelectedText();
    //            }
    //        });
    //    }

    /**
     * 绑定高亮组件
     *
     * @param editor 编辑器
     * @param field  高亮文本组件
     */
    public static void bindHighlight(Editor editor, HighlightTextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            editor.setHighlight(newValue);
        });
        field.regexPropery().addListener((observable, oldValue, newValue) -> {
            editor.setHighlightRegex(newValue);
        });
        field.wholeWordPropery().addListener((observable, oldValue, newValue) -> {
            editor.setHighlightWholeWord(newValue);
        });
        field.matchCasePropery().addListener((observable, oldValue, newValue) -> {
            editor.setHighlightMacthCase(newValue);
        });
    }

    //    /**
    //     * 高亮搜索索引名称
    //     */
    //    private final static String HIGHLIGHT_SEARCH_INDEX = "_highlight_search_index";

    //    /**
    //     * 清除高亮搜索索引
    //     *
    //     * @param editor 编辑器
    //     */
    //    public static void clearHighlightSearchIndex(Editor editor) {
    //        editor.setProp(HIGHLIGHT_SEARCH_INDEX, 0);
    //    }

    /**
     * 搜索下一个高亮
     *
     * @param editor 编辑器
     * @param field  高亮文本组件
     */
    public static void searchNextHighlight(Editor editor, HighlightTextField field) {
        try {
            //            searchIndex = editor.hasProp(HIGHLIGHT_SEARCH_INDEX) ? editor.getProp(HIGHLIGHT_SEARCH_INDEX) : 0;
            String filterText = field.getText();
            if (StringUtil.isBlank(filterText)) {
                return;
            }
            int searchIndex = editor.caretPosition();
            String text = editor.getText();
            if (searchIndex >= text.length()) {
                searchIndex = 0;
            }
            // 搜索高亮
            TextUtil.MatchText matchText = searchHighlight(text, field, searchIndex);
            // 无效则返回
            if (matchText == TextUtil.MatchText.INVALID) {
                //                searchIndex = 0;
                return;
            }
            // 如果未找到，可能是到尾了，再从头执行一次搜索
            if (matchText == TextUtil.MatchText.NOT_FOUND) {
                searchIndex = 0;
                matchText = searchHighlight(text, field, searchIndex);
                if (matchText == TextUtil.MatchText.NOT_FOUND) {
                    return;
                }
            }
            //            searchIndex = matchText.index() + matchText.text().length();
            editor.selectRange(matchText.index(), matchText.index() + matchText.text().length());
        } catch (Exception ex) {
            ex.printStackTrace();
            //        } finally {
            //            editor.setProp(HIGHLIGHT_SEARCH_INDEX, searchIndex);
        }
    }

    /**
     * 搜索高亮
     *
     * @param text  文本
     * @param field 高亮文本组件
     * @return 匹配文本
     */
    private static TextUtil.MatchText searchHighlight(String text, HighlightTextField field, int searchIndex) {
        String filterText = field.getText();
        if (StringUtil.isBlank(filterText)) {
            return TextUtil.MatchText.INVALID;
        }
        return TextUtil.findText(text,
                filterText,
                searchIndex,
                field.isMatchCase(),
                field.isWholeWord(),
                field.isRegex());
    }
}
