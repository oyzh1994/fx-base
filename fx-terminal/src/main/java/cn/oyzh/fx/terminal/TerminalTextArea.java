package cn.oyzh.fx.terminal;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.rich.RichTextStyle;
import cn.oyzh.fx.rich.richtextfx.control.FlexRichTextArea;
import cn.oyzh.fx.rich.richtextfx.control.RichTextAreaPane;
import cn.oyzh.fx.rich.richtextfx.terminal.RichTerminalTextArea;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.command.TerminalCommandHandler;
import cn.oyzh.fx.terminal.complete.TerminalCompleteHandler;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import cn.oyzh.fx.terminal.help.TerminalHelpHandler;
import cn.oyzh.fx.terminal.histroy.TerminalHistoryHandler;
import cn.oyzh.fx.terminal.key.TerminalKeyHandler;
import cn.oyzh.fx.terminal.mouse.TerminalMouseHandler;
import cn.oyzh.fx.terminal.util.TerminalManager;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 命令行文本域
 *
 * @author oyzh
 * @since 2023/05/28
 */
public class TerminalTextArea extends RichTextAreaPane<FlexRichTextArea> implements Terminal {

    /**
     * 不可操作边界
     */
    private final AtomicInteger NOP = new AtomicInteger(0);

    /**
     * 提示符号
     */
    private String prompt;

    /**
     * 键盘按键实现
     */
    @Getter
    @Accessors(fluent = true, chain = false)
    private TerminalKeyHandler<?> keyHandler;

    /**
     * 命令帮助实现
     */
    @Getter
    @Setter
    @Accessors(fluent = true, chain = false)
    private TerminalHelpHandler helpHandler;

    /**
     * 鼠标按键实现
     */
    @Getter
    @Accessors(fluent = true, chain = false)
    private TerminalMouseHandler mouseHandler;

    /**
     * 命令历史实现
     */
    @Getter
    @Setter
    @Accessors(fluent = true, chain = false)
    private TerminalHistoryHandler historyHandler;

    /**
     * 命令补全实现
     */
    @Getter
    @Setter
    @Accessors(fluent = true, chain = false)
    private TerminalCompleteHandler completeHandler;

    {
        // 保证字符等宽
        this.caretPositionProperty().addListener((observableValue, number, t1) -> {
            // 对边界做检查
            if (this.getNOP() > this.contentLength()) {
                this.flushNOP();
            }
            JulLog.debug("nop:{}, length:{}", this.getNOP(), this.contentLength());
            if (t1.longValue() < this.getNOP()) {
                this.disableInput();
            } else {
                this.enableInput();
            }
        });
        this.addTextChangeListener((observable, oldValue, newValue) -> this.initTextStyle());
        this.init();
    }

    public TerminalTextArea() {
        super(new RichTerminalTextArea());
    }

    @Override
    public void keyHandler(TerminalKeyHandler keyHandler) {
        this.keyHandler = keyHandler;
        if (keyHandler != null) {
            this.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                try {
                    if (event.getCode() == KeyCode.ENTER) {
                        if (!keyHandler.onEnterKeyPressed(this)) {
                            event.consume();
                        }
                    } else if (event.getCode() == KeyCode.TAB) {
                        if (!keyHandler.onTabKeyPressed(this)) {
                            event.consume();
                        }
                    } else if (event.getCode() == KeyCode.BACK_SPACE) {
                        if (!keyHandler.onBackspaceKeyPressed(this)) {
                            event.consume();
                        }
                    } else if (event.getCode() == KeyCode.UP) {
                        if (!keyHandler.onUpKeyPressed(this)) {
                            event.consume();
                        }
                    } else if (event.getCode() == KeyCode.DOWN) {
                        if (!keyHandler.onDownKeyPressed(this)) {
                            event.consume();
                        }
                    } else if (event.getCode() == KeyCode.LEFT) {
                        if (!keyHandler.onLeftKeyPressed(this)) {
                            event.consume();
                        }
                    } else if (event.getCode() == KeyCode.PAGE_UP) {
                        if (!keyHandler.onPageUpKeyPressed(this)) {
                            event.consume();
                        }
                    } else if (event.getCode() == KeyCode.PAGE_DOWN) {
                        if (!keyHandler.onPageDownKeyPressed(this)) {
                            event.consume();
                        }
                    } else if (event.getCode() == KeyCode.A && event.isControlDown()) {
                        if (!keyHandler.onCtrlAKeyPressed(this)) {
                            event.consume();
                        }
                    } else if (event.getCode() == KeyCode.E && event.isControlDown()) {
                        if (!keyHandler.onCtrlEKeyPressed(this)) {
                            event.consume();
                        }
                    } else if (event.getCode() == KeyCode.Z && event.isControlDown()) {
                        if (!keyHandler.onCtrlZKeyPressed(this)) {
                            event.consume();
                        }
                    } else if (event.getCode() == KeyCode.Y && event.isControlDown()) {
                        if (!keyHandler.onCtrlYKeyPressed(this)) {
                            event.consume();
                        }
                    } else if (event.getCode() == KeyCode.X && event.isControlDown()) {
                        if (!keyHandler.onCtrlXKeyPressed(this)) {
                            event.consume();
                        }
                    } else if (event.getCode() == KeyCode.V && event.isControlDown()) {
                        if (!keyHandler.onCtrlVKeyPressed(this)) {
                            event.consume();
                        }
                    } else if (event.getCode() == KeyCode.C && event.isControlDown()) {
                        if (!keyHandler.onCtrlCKeyPressed(this)) {
                            event.consume();
                        }
                    } else if (event.getCode() == KeyCode.HOME) {
                        if (!keyHandler.onHomeKeyPressed(this)) {
                            event.consume();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    this.onError(ex);
                }
            });
        }
    }

    @Override
    public void mouseHandler(TerminalMouseHandler mouseHandler) {
        this.mouseHandler = mouseHandler;
        if (mouseHandler != null) {
            this.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                try {
                    if (event.getButton() == MouseButton.PRIMARY) {
                        if (!mouseHandler.onPrimaryMousePressed(this)) {
                            event.consume();
                        }
                    } else if (event.getButton() == MouseButton.SECONDARY) {
                        if (!mouseHandler.onSecondMousePressed(this)) {
                            event.consume();
                        }
                    }
                } catch (Exception ex) {
                    this.onError(ex);
                }
            });
        }
    }

    @Override
    public boolean checkNop() {
        int pos = this.getCaretPosition();
        return pos <= this.getNOP();
    }

    @Override
    public void flushNOP() {
        this.NOP.set(this.contentLength());
        this.requestFocus();
    }

    @Override
    public String getInput() {
        String text = CollectionUtil.getLast(this.getText().lines().toList());
        if (text == null || text.isEmpty() || text.equals(this.prompt())) {
            return "";
        }
        if (!text.startsWith(this.prompt()) || text.length() <= this.promptLength()) {
            return text;
        }
        return text.substring(this.promptLength());
    }

    @Override
    public void clearInput() {
        this.deleteText(this.getNOP(), this.getLength());
    }

    @Override
    public void coverInput(@NonNull String input) {
        this.replaceText(this.getNOP(), this.getLength(), input);
    }

    @Override
    public void output(String output) {
        if (output != null) {
            this.replaceText(this.getNOP(), this.getLength(), output);
            this.scrollToEnd();
        }
    }

    @Override
    public void outputLine(String output) {
        if (output != null) {
            this.appendLine(output);
        }
    }

    public void outputLine(String output, boolean endLine) {
        if (output != null) {
            this.appendLine(output, endLine);
        }
    }

    @Override
    public void outputByPrompt(String output) {
        if (output != null) {
            this.outputLine(output);
            this.outputPrompt();
        }
    }

    @Override
    public void outputByAppend(String output) {
        if (output != null) {
            this.appendText(output);
        }
    }

    @Override
    public void appendByPrompt(String output) {
        this.outputPrompt();
        this.flushNOP();
        if (output != null) {
            this.appendText(output);
        }
    }

    @Override
    public void enableInput() {
        if (!this.isEditable()) {
            this.setEditable(true);
        }
    }

    @Override
    public void disableInput() {
        if (this.isEditable()) {
            this.setEditable(false);
        }
    }

    @Override
    public String prompt() {
        return StringUtil.nullToDefault(this.prompt, "");
    }

    @Override
    public void prompt(String prompt) {
        if (!StringUtil.equals(prompt, this.prompt)) {
            if (prompt != null) {
                prompt = prompt.replaceAll("\r", "").replaceAll("\n", "");
            }
            this.prompt = prompt;
        }
    }

    @Override
    public void outputPrompt() {
        String text = this.getText();
        String prompt = this.prompt();
        if (StringUtil.equals(text, "\n")) {
            this.setText(prompt);
        } else if (!StringUtil.endWith(text, prompt)) {
            this.appendText(prompt);
        }
        this.flushNOP();
        super.scrollToEnd();
    }

    @Override
    public void flushPrompt() {

    }

    @Override
    public int caretPosition() {
        return this.getCaretPosition();
    }

    @Override
    public void caretPosition(int caretPosition) {
        FXUtil.runWait(() -> this.positionCaret(caretPosition));
    }

    @Override
    public String content() {
        return this.getText();
    }

    @Override
    public int contentLength() {
        return this.getLength();
    }

    @Override
    public void cutContent() {
        if (!this.checkNop()) {
            this.cut();
        }
    }

    @Override
    public void pasteContent() {
        if (this.getCaretPosition() >= this.getNOP()) {
            this.paste();
        }
    }

    @Override
    public void selectContent(int start, int end) {
        this.selectRange(start, end);
    }

    @Override
    public void onCommand(String input) throws Exception {
        TerminalCommandHandler handler = this.findHandler(input);
        if (handler != null) {
            TerminalCommand command = handler.parseCommand(input);
            TerminalExecuteResult result = handler.execute(command, this);
            if (result == null) {
                this.outputByPrompt("");
            } else if (!result.isSuccess()) {
                this.outputByPrompt(result.getErrMsg());
            } else if (!result.isIgnoreOutput()) {
                this.outputByPrompt(result.result());
            }
        }
    }

    protected TerminalCommandHandler findHandler(String input) {
        return TerminalManager.findHandler(input);
    }

    @Override
    public void flushCaret() {
        this.caretPosition(this.getNOP());
        this.requestFocus();
    }

    @Override
    public void fontSizeIncr() {
        super.fontSizeIncr();
    }

    @Override
    public void fontSizeDecr() {
        super.fontSizeDecr();
    }

    @Override
    public void moveCaretEnd() {
        this.caretPosition(this.getLength());
        this.requestFocus();
    }

    /**
     * 基础内容正则模式
     */
    private Pattern contentPrompts;

    /**
     * 设置内容提示词
     *
     * @param collection 内容提示词列表
     */
    public void setContentPrompts(Collection<String> collection) {
        if (collection == null || collection.isEmpty()) {
            this.contentPrompts = null;
        } else {
            StringBuilder regex = new StringBuilder("\\b(");
            for (String s : collection) {
                regex.append(s).append("|");
            }
            regex.append(")\\b");
            this.contentPrompts = Pattern.compile(regex.toString().replaceFirst("\\|\\)", ")"), Pattern.CASE_INSENSITIVE);
        }
        this.initTextStyle();
    }

    /**
     * 初始化组件
     */
    protected void init() {
        // 初始化字体
        this.initFont();
        // 显示行号
        this.showLineNum();
        // 初始化内容提示符
        this.initContentPrompts();
        // 覆盖默认的菜单
        this.setContextMenu(new ContextMenu());
//        // 添加类
//        this.addClass("terminal-text-area");
    }

    /**
     * 初始化字体
     */
    protected void initFont() {
        // 禁用字体管理
        this.disableFont();
        this.setFontSize(11);
        this.setFontFamily("Monospaced");
        this.setFontWeight(FontWeight.NORMAL);
    }

    /**
     * 初始化内容提示词
     */
    protected void initContentPrompts() {
        // 设置内容提示符
        Collection<TerminalCommandHandler<?, ?>> handlers = TerminalManager.listHandler();
        Set<String> set = new HashSet<>();
        for (TerminalCommandHandler<?, ?> handler : handlers) {
            if (StringUtil.isNotBlank(handler.commandName())) {
                set.add(handler.commandName());
            }
            if (StringUtil.isNotBlank(handler.commandSubName())) {
                set.add(handler.commandSubName());
            }
        }
        this.setContentPrompts(set);
//        super.forgetHistory();
    }

    @Override
    public void initTextStyle() {
//        this.clearTextStyle();
//        this.setProp("init:text:style", false);
//        this.changeTheme(ThemeManager.currentTheme());
//        this.removeProp("init:text:style");
        super.initTextStyle();
        if (this.contentPrompts != null) {
            String text = this.getText();
            Matcher matcher1 = this.contentPrompts.matcher(text);
            List<RichTextStyle> styles = new ArrayList<>();
            while (matcher1.find()) {
                styles.add(new RichTextStyle(matcher1.start(), matcher1.end(), "-fx-fill: #008B45;"));
            }
            this.setStyles(styles);
            this.forgetHistory();
        }
    }

    @Override
    public int getNOP() {
        return this.NOP.get();
    }

    @Override
    public void changeFont(Font font) {
        super.changeFont(font);
        this.initFont();
    }
}
