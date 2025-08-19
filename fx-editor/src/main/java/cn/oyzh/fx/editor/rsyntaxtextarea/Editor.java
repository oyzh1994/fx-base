// package cn.oyzh.fx.editor.rsyntaxtextarea;
//
// import cn.oyzh.common.thread.ThreadUtil;
// import cn.oyzh.common.util.CollectionUtil;
// import cn.oyzh.common.util.StringUtil;
// import cn.oyzh.common.util.TextUtil;
// import cn.oyzh.fx.plus.swing.SwingUtil;
// import javafx.beans.property.BooleanProperty;
// import javafx.beans.property.LongProperty;
// import javafx.beans.property.ObjectProperty;
// import javafx.beans.property.SimpleBooleanProperty;
// import javafx.beans.property.SimpleLongProperty;
// import javafx.beans.property.SimpleObjectProperty;
// import javafx.beans.property.SimpleStringProperty;
// import javafx.beans.property.StringProperty;
// import javafx.beans.value.ChangeListener;
// import javafx.geometry.BoundingBox;
// import javafx.geometry.Bounds;
// import org.fife.ui.rsyntaxtextarea.TextEditorPane;
//
// import javax.swing.event.DocumentEvent;
// import javax.swing.event.DocumentListener;
// import javax.swing.event.UndoableEditListener;
// import javax.swing.text.DefaultHighlighter;
// import javax.swing.text.Highlighter;
// import java.awt.Color;
// import java.awt.Font;
// import java.awt.Point;
// import java.awt.geom.Rectangle2D;
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.List;
// import java.util.Optional;
// import java.util.Set;
// import java.util.concurrent.CopyOnWriteArrayList;
// import java.util.regex.Matcher;
// import java.util.regex.Pattern;
//
// /**
//  * 编辑器
//  *
//  * @author oyzh
//  * @since 2025/07/30
//  */
// public class Editor extends TextEditorPane {
//
//     {
//         // 格式变化事件
//         this.formatTypeProperty().addListener((observableValue, formatType, t1) -> {
//             this.initTextStyle();
//         });
//         // 提示词变化事件
//         this.promptsProperty().addListener((observableValue, formatType, t1) -> {
//             this.initPromptsStyle();
//         });
//         // 高亮变化事件
//         this.highlightTextProperty().addListener((observableValue, formatType, t1) -> {
//             this.initHighlightStyle();
//         });
//         // 内容变更事件
//         this.addDocumentListener(new EditorDocumentListener() {
//             @Override
//             public void changedUpdate(DocumentEvent e) {
//                 textProperty().setValue(getText());
//             }
//         });
//         // 文本变更事件
//         this.addTextChangeListener((observableValue, s, t1) -> {
//             this.initTextStyle();
//         });
//     }
//
//     /**
//      * 文本属性
//      */
//     private StringProperty textProperty;
//
//     public StringProperty textProperty() {
//         if (this.textProperty == null) {
//             this.textProperty = new SimpleStringProperty(this.getText());
//         }
//         return this.textProperty;
//     }
//
//     /**
//      * 添加文本监听器
//      *
//      * @param listener 监听器
//      */
//     public void addTextChangeListener(ChangeListener<String> listener) {
//         synchronized (this) {
//             this.textProperty().addListener((observable, oldValue, newValue) -> {
//                 if (!this.ignoreChange) {
//                     listener.changed(observable, oldValue, newValue);
//                 }
//             });
//         }
//     }
//
//     /**
//      * 光标位置属性
//      */
//     private LongProperty caretPositionProperty;
//
//     public LongProperty caretPositionProperty() {
//         if (this.caretPositionProperty == null) {
//             this.caretPositionProperty = new SimpleLongProperty();
//             // 光标位置变更事件
//             this.addCaretListener(e -> {
//                 this.caretPositionProperty.set(this.getCaretPosition());
//             });
//         }
//         return this.caretPositionProperty;
//     }
//
//     /**
//      * 可撤销属性
//      */
//     private BooleanProperty undoableProperty;
//
//     public BooleanProperty undoableProperty() {
//         if (this.undoableProperty == null) {
//             this.undoableProperty = new SimpleBooleanProperty();
//             // 撤销监听器
//             this.addUndoableEditListener(e -> {
//                 this.undoableProperty.set(e.getEdit().canUndo());
//             });
//         }
//         return this.undoableProperty;
//     }
//
//     /**
//      * 可重做属性
//      */
//     private BooleanProperty redoableProperty;
//
//     public BooleanProperty redoableProperty() {
//         if (this.redoableProperty == null) {
//             this.redoableProperty = new SimpleBooleanProperty();
//             // 重做监听器
//             this.addUndoableEditListener(e -> {
//                 this.redoableProperty.set(e.getEdit().canRedo());
//             });
//         }
//         return this.redoableProperty;
//     }
//
//     /**
//      * 可编辑属性
//      */
//     private BooleanProperty editableProperty;
//
//     public BooleanProperty editableProperty() {
//         if (this.editableProperty == null) {
//             this.editableProperty = new SimpleBooleanProperty();
//             // 可编辑变更事件
//             this.addPropertyChangeListener("editable", e -> {
//                 this.editableProperty.setValue((Boolean) e.getNewValue());
//             });
//         }
//         return this.editableProperty;
//     }
//
//     /**
//      * 是否忽略变化
//      */
//     private boolean ignoreChange;
//
//     /**
//      * 格式类型
//      */
//     private ObjectProperty<EditorFormatType> formatTypeProperty;
//
//     public EditorFormatType getFormatType() {
//         return this.formatTypeProperty == null ? EditorFormatType.RAW : this.formatTypeProperty.get();
//     }
//
//     public void setFormatType(EditorFormatType formatTypeProperty) {
//         this.formatTypeProperty().set(formatTypeProperty);
//     }
//
//     public ObjectProperty<EditorFormatType> formatTypeProperty() {
//         if (this.formatTypeProperty == null) {
//             this.formatTypeProperty = new SimpleObjectProperty<>(EditorFormatType.RAW);
//         }
//         return formatTypeProperty;
//     }
//
//     /**
//      * 显示数据
//      *
//      * @param rawData 显示数据
//      */
//     public void showData(Object rawData) {
//         this.showData(rawData, this.getFormatType());
//     }
//
//     /**
//      * 显示数据
//      *
//      * @param rawData    显示数据
//      * @param formatType 格式类型
//      */
//     public void showData(Object rawData, EditorFormatType formatType) {
//         try {
//             this.ignoreChange = true;
//             this.setEnabled(false);
//             String data = null;
//             if (rawData instanceof CharSequence sequence) {
//                 data = sequence.toString();
//             } else if (rawData instanceof byte[] array) {
//                 data = Arrays.toString(array);
//             } else if (rawData != null) {
//                 data = rawData.toString();
//             }
//             if (data != null) {
//                 this.setText(data);
//             } else {
//                 this.clear();
//             }
//             this.setFormatType(formatType);
//         } finally {
//             this.setEnabled(true);
//             this.ignoreChange = false;
//         }
//     }
//
//     /**
//      * 显示检测后的数据
//      *
//      * @param rawData 显示数据
//      * @return EditorFormatType
//      */
//     public EditorFormatType showDetectData(Object rawData) {
//         // 检测类型
//         byte detectType = TextUtil.detectType(rawData);
//         EditorFormatType formatType;
//         if (detectType == 1) {
//             formatType = EditorFormatType.JSON;
//         } else if (detectType == 3) {
//             formatType = EditorFormatType.XML;
//         } else if (detectType == 4) {
//             formatType = EditorFormatType.HTML;
//         } else if (detectType == 7) {
//             formatType = EditorFormatType.CSS;
//         } else if (detectType == 8) {
//             formatType = EditorFormatType.PROPERTIES;
//         } else if (detectType == 9) {
//             formatType = EditorFormatType.YAML;
//         } else {
//             formatType = EditorFormatType.RAW;
//         }
//         this.showData(rawData, formatType);
//         return formatType;
//     }
//
//     @Override
//     public void setText(String text) {
//         try {
//             if (StringUtil.isEmpty(text)) {
//                 this.clear();
//             } else {
//                 super.setText(text);
//             }
//         } catch (Throwable ignore) {
//
//         }
//     }
//
//     /**
//      * 初始化样式
//      */
//     public void initTextStyle() {
//         String editingStyle = this.getSyntaxEditingStyle();
//         String syntax = EditorUtil.toSyntax(this.getFormatType());
//         if (!StringUtil.equalsAnyIgnoreCase(editingStyle, syntax)) {
//             this.setSyntaxEditingStyle(syntax);
//         }
//         this.initPromptsStyle();
//         this.initHighlightStyle();
//     }
//
//     @Override
//     public void setSyntaxEditingStyle(String styleKey) {
//         super.setSyntaxEditingStyle(styleKey);
//         // SwingUtil.runLater(() -> super.setSyntaxEditingStyle(styleKey));
//     }
//
//     /**
//      * 添加高亮
//      *
//      * @param start   开始位置
//      * @param end     结束位置
//      * @param painter 样式
//      * @return id
//      */
//     public Object addHighlight(int start, int end, Highlighter.HighlightPainter painter) {
//         try {
//             return this.getHighlighter().addHighlight(start, end, painter);
//         } catch (Exception ex) {
//             ex.printStackTrace();
//         }
//         return null;
//     }
//
//     /**
//      * 添加高亮
//      *
//      * @param highlights 高亮列表
//      * @param painter    样式
//      * @return id列表
//      */
//     public List<Object> addHighlights(List<EditorHighlight> highlights, Highlighter.HighlightPainter painter) {
//         List<Object> ids = new ArrayList<>();
//         for (EditorHighlight highlight : highlights) {
//             Object id = this.addHighlight(highlight.getStart(), highlight.getEnd(), painter);
//             ids.add(id);
//         }
//         return ids;
//     }
//
//     /**
//      * 移除高亮
//      *
//      * @param id id
//      */
//     public void removeHighlight(Object id) {
//         if (id != null) {
//             try {
//                 this.getHighlighter().removeHighlight(id);
//             } catch (Exception ex) {
//                 ex.printStackTrace();
//             }
//         }
//     }
//
//     /**
//      * 移除高亮
//      *
//      * @param ids id列表
//      */
//     public void removeHighlights(List<Object> ids) {
//         for (Object id : ids) {
//             this.removeHighlight(id);
//         }
//     }
//
//     /**
//      * 高亮样式
//      */
//     protected static final Highlighter.HighlightPainter HIGHLIGHT_PAINTER = new DefaultHighlighter.DefaultHighlightPainter(
//             new Color(255, 102, 0)
//             // new Color(185, 214, 251)
//             // new Color(248, 201, 171)
//     );
//
//     /**
//      * 提示词样式线程
//      */
//     private Thread highlightStyleThread;
//
//     /**
//      * 高亮id列表
//      */
//     private final List<Object> highlightIds = new CopyOnWriteArrayList<>();
//
//     /**
//      * 初始化高亮样式
//      */
//     protected void initHighlightStyle() {
//         // 停止旧线程
//         ThreadUtil.interrupt(this.highlightStyleThread);
//         // 移除高亮
//         if (!this.highlightIds.isEmpty()) {
//             List<Object> ids;
//             synchronized (this.highlightIds) {
//                 ids = new ArrayList<>(this.highlightIds);
//                 this.highlightIds.clear();
//             }
//             ThreadUtil.start(() -> this.removeHighlights(ids));
//         }
//         // 无高亮内容，返回
//         String highlightText = this.getHighlightText();
//         if (StringUtil.isEmpty(highlightText)) {
//             return;
//         }
//         // 生成高亮
//         Pattern highlightPattern = Pattern.compile(highlightText, Pattern.CASE_INSENSITIVE);
//         this.highlightStyleThread = ThreadUtil.start(() -> {
//             String text = this.getText();
//             // 高亮正则模式
//             Matcher matcher = highlightPattern.matcher(text);
//             while (matcher.find() && !Thread.currentThread().isInterrupted()) {
//                 Object id = this.addHighlight(matcher.start(), matcher.end(), HIGHLIGHT_PAINTER);
//                 this.highlightIds.add(id);
//             }
//         });
//     }
//
//     /**
//      * 提示词样式
//      */
//     protected static final Highlighter.HighlightPainter PROMPTS_PAINTER = new DefaultHighlighter.DefaultHighlightPainter(
//             new Color(125, 190, 93)
//             // new Color(166, 38, 164)
//     );
//
//     /**
//      * 提示词属性
//      */
//     private ObjectProperty<Set<String>> promptsProperty;
//
//     public ObjectProperty<Set<String>> promptsProperty() {
//         if (this.promptsProperty == null) {
//             this.promptsProperty = new SimpleObjectProperty<>();
//         }
//         return this.promptsProperty;
//     }
//
//     /**
//      * 设置提示词
//      *
//      * @param prompts 提示词列表
//      */
//     public void setPrompts(Set<String> prompts) {
//         this.promptsProperty().set(prompts);
//     }
//
//     /**
//      * 获取提示词
//      *
//      * @return 提示词列表
//      */
//     public Set<String> getPrompts() {
//         return this.promptsProperty == null ? null : this.promptsProperty.get();
//     }
//
//     /**
//      * 提示词样式线程
//      */
//     private Thread promptsStyleThread;
//
//     /**
//      * 提示词高亮id列表
//      */
//     private final List<Object> promptIds = new CopyOnWriteArrayList<>();
//
//     /**
//      * 初始化提示词样式
//      */
//     protected void initPromptsStyle() {
//         // 停止旧线程
//         ThreadUtil.interrupt(this.promptsStyleThread);
//         // 移除提示词
//         if (!this.promptIds.isEmpty()) {
//             List<Object> ids;
//             synchronized (this.promptIds) {
//                 ids = new ArrayList<>(this.promptIds);
//                 this.promptIds.clear();
//             }
//             ThreadUtil.start(() -> this.removeHighlights(ids));
//         }
//         // 无高亮内容，返回
//         Set<String> prompts = this.getPrompts();
//         if (CollectionUtil.isEmpty(prompts)) {
//             return;
//         }
//         // 生成高亮
//         Pattern promptsPattern = Pattern.compile("\\b(" + String.join("|", prompts) + ")\\b", Pattern.CASE_INSENSITIVE);
//         this.promptsStyleThread = ThreadUtil.start(() -> {
//             String text = this.getText();
//             // 提示词正则模式
//             Matcher matcher = promptsPattern.matcher(text);
//             while (matcher.find() && !Thread.currentThread().isInterrupted()) {
//                 Object id = this.addHighlight(matcher.start(), matcher.end(), PROMPTS_PAINTER);
//                 this.promptIds.add(id);
//             }
//         });
//     }
//
//     /**
//      * 是否为空
//      *
//      * @return 结果
//      */
//     public boolean isEmpty() {
//         try {
//             String str = this.getDocument().getText(0, 1);
//             if (str == null || StringUtil.equalsAny(str, "\n", "\r", "\r\n")) {
//                 return true;
//             }
//         } catch (Exception ex) {
//             ex.printStackTrace();
//         }
//         return false;
//     }
//
//     /**
//      * 高亮文本
//      */
//     private StringProperty highlightTextProperty;
//
//     public String getHighlightText() {
//         return this.highlightTextProperty == null ? null : this.highlightTextProperty.get();
//     }
//
//     /**
//      * 设置高亮文本
//      *
//      * @param highlightText 高亮文本
//      */
//     public void setHighlightText(String highlightText) {
//         this.highlightTextProperty().set(highlightText);
//     }
//
//     public StringProperty highlightTextProperty() {
//         if (this.highlightTextProperty == null) {
//             this.highlightTextProperty = new SimpleStringProperty();
//         }
//         return this.highlightTextProperty;
//     }
//
//     /**
//      * 获取文本长度
//      *
//      * @return 文本长度
//      */
//     public int getLength() {
//         return this.getDocument().getLength();
//     }
//
//     /**
//      * 清除文本
//      */
//     public void clear() {
//         try {
//             int len = this.getDocument().getLength();
//             this.getDocument().remove(0, len);
//         } catch (Exception ex) {
//             ex.printStackTrace();
//         }
//     }
//
//     public void addDocumentListener(DocumentListener documentListener) {
//         this.getDocument().addDocumentListener(documentListener);
//     }
//
//     public void addUndoableEditListener(UndoableEditListener undoableEditListener) {
//         this.getDocument().addUndoableEditListener(undoableEditListener);
//     }
//
//     /**
//      * 获取内容，去除前后空格
//      *
//      * @return 内容
//      */
//     public String getTextTrim() {
//         String text = this.getText();
//         if (text == null) {
//             return null;
//         }
//         return text.trim();
//     }
//
//     /**
//      * 撤销
//      */
//     public void undo() {
//         super.undoLastAction();
//     }
//
//     /**
//      * 重做
//      */
//     public void redo() {
//         super.redoLastAction();
//     }
//
//     /**
//      * 遗忘历史
//      */
//     public void forgetHistory() {
//         super.discardAllEdits();
//     }
//
//     /**
//      * 替换内容
//      *
//      * @param start   开始位置
//      * @param end     结束位置
//      * @param content 内容
//      */
//     public void replaceText(int start, int end, String content) {
//         if (content != null) {
//             try {
//                 super.replaceRange(content, start, end);
//             } catch (Exception ignored) {
//
//             }
//         }
//     }
//
//     /**
//      * 追加内容
//      *
//      * @param content 内容
//      */
//     public void appendText(String content) {
//         if (content != null) {
//             SwingUtil.runWait(() -> {
//                 super.append(content);
//             });
//         }
//     }
//
//     /**
//      * 追加行
//      *
//      * @param content 内容
//      */
//     public void appendLine(String content) {
//         this.appendLine(content, true);
//     }
//
//     /**
//      * 追加行
//      *
//      * @param content 内容
//      * @param endLine 尾部是否追加换行符
//      */
//     public void appendLine(String content, boolean endLine) {
//         if (content != null) {
//             int len = this.getLength();
//             String text = null;
//             try {
//                 this.getText(len - 1, len);
//             } catch (Exception ex) {
//                 text = this.getText();
//             }
//             if (text != null && !text.isEmpty() && !text.endsWith("\n") && !content.startsWith("\n")) {
//                 content = System.lineSeparator() + content;
//             }
//             if (endLine && !content.endsWith(System.lineSeparator())) {
//                 content = content + "\n";
//             }
//             this.append(content);
//         }
//     }
//
//     @Override
//     public void setFont(Font font) {
//         try {
//             super.setFont(font);
//         } catch (Exception ignored) {
//
//         }
//     }
//
//     /**
//      * 获取光标边界
//      *
//      * @return 光标边界
//      */
//     public Optional<Bounds> getCaretBounds() {
//         try {
//             // 1. 获取光标在文档中的偏移量
//             int caretOffset = this.getCaretPosition();
//             // 2. 将文档偏移量转换为文本区域内的坐标（相对于文本区域的左上角）
//             // 注意：viewToModel 和 modelToView 是 Swing 文本组件的核心坐标转换方法
//             Rectangle2D caretRect = this.modelToView2D(caretOffset);
//             // 3. 计算光标在屏幕上的绝对坐标
//             //  - caretRect.x/y 是光标在文本区域内的相对坐标
//             //  - 加上文本区域在屏幕上的绝对坐标，得到最终位置
//             if (caretRect != null) {
//                 Point point = this.getLocationOnScreen();
//                 double screenX1 = point.x + caretRect.getCenterX();
//                 double screenY1 = point.y + caretRect.getCenterY();
//                 BoundingBox bounds = new BoundingBox(screenX1, screenY1, caretRect.getWidth(), caretRect.getHeight());
//                 return Optional.of(bounds);
//             }
//         } catch (Exception ex) {
//             ex.printStackTrace();
//         }
//         return Optional.empty();
//     }
//
//     /**
//      * 选中选区
//      *
//      * @param start 开始位置
//      * @param end   结束位置
//      */
//     public void selectRange(int start, int end) {
//         SwingUtil.runLater(() -> this.select(start, end));
//     }
//
//     @Override
//     public Rectangle2D modelToView2D(int pos) {
//         try {
//             return super.modelToView2D(pos);
//         } catch (Exception ex) {
//             ex.printStackTrace();
//         }
//         return null;
//     }
//
//     /**
//      * 设置光标位置
//      *
//      * @param caretPosition 光标位置
//      */
//     public void positionCaret(int caretPosition) {
//         if (caretPosition >= this.getLength()) {
//             this.moveCaretEnd();
//         } else {
//             SwingUtil.runLater(() -> this.setCaretPosition(caretPosition));
//         }
//     }
//
//     /**
//      * 移动光标到末尾
//      */
//     public void moveCaretEnd() {
//         int len = this.getLength();
//         SwingUtil.runLater(() -> {
//             this.setCaretPosition(len);
//         });
//     }
//
//     /**
//      * 删除内容
//      *
//      * @param start 开始位置
//      * @param end   结束位置
//      */
//     public void deleteText(int start, int end) {
//         try {
//             this.getDocument().remove(start, end);
//         } catch (Exception ignored) {
//
//         }
//     }
// }
