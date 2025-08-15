package cn.oyzh.fx.terminal;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.editor.rsyntaxtextarea.EditorPane;
import cn.oyzh.fx.editor.tem4javafx.Editor;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.command.TerminalCommandHandler;
import cn.oyzh.fx.terminal.complete.TerminalCompleteHandler;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import cn.oyzh.fx.terminal.help.TerminalHelpHandler;
import cn.oyzh.fx.terminal.histroy.TerminalHistoryHandler;
import cn.oyzh.fx.terminal.key.TerminalKeyHandler;
import cn.oyzh.fx.terminal.mouse.TerminalMouseHandler;
import cn.oyzh.fx.terminal.util.TerminalManager;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 命令行组件
 *
 * @author oyzh
 * @since 2025/08/08
 */
public class TerminalPane extends Editor implements Terminal {

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
    private TerminalKeyHandler<?> keyHandler;

    /**
     * 命令帮助实现
     */
    private TerminalHelpHandler helpHandler;

    /**
     * 鼠标按键实现
     */
    private TerminalMouseHandler mouseHandler;

    /**
     * 命令历史实现
     */
    private TerminalHistoryHandler historyHandler;

    /**
     * 命令补全实现
     */
    private TerminalCompleteHandler completeHandler;

    {
        this.caretPositionProperty().addListener((observableValue, number, t1) -> {
            int nop = this.getNOP();
            int len = this.contentLength();
            int caretPosition = this.caretPosition();
            // 对边界做检查
            if (nop > len) {
                this.flushNOP();
                nop = this.getNOP();
            }
            if (JulLog.isDebugEnabled()) {
                JulLog.debug("nop:{}, length:{}", nop, len);
            }
            if (caretPosition < nop) {
                this.disableInput();
            } else {
                this.enableInput();
            }
        });
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
                    } else if (KeyboardUtil.selectAll_keyCombination.match(event)) {
                        if (!keyHandler.onCtrlAKeyPressed(this)) {
                            event.consume();
                        }
                    } else if (OSUtil.isMacOS() && event.getCode() == KeyCode.A && event.isControlDown()) {
                        if (!keyHandler.onHomeKeyPressed(this)) {
                            event.consume();
                        }
                    } else if (event.getCode() == KeyCode.E && event.isControlDown()) {
                        if (!keyHandler.onCtrlEKeyPressed(this)) {
                            event.consume();
                        }
                    } else if (KeyboardUtil.undo_keyCombination.match(event)) {
                        if (!keyHandler.onCtrlZKeyPressed(this)) {
                            event.consume();
                        }
                    } else if (KeyboardUtil.redo_keyCombination.match(event)) {
                        if (!keyHandler.onCtrlYKeyPressed(this)) {
                            event.consume();
                        }
                    } else if (KeyboardUtil.cut_keyCombination.match(event)) {
                        if (!keyHandler.onCtrlXKeyPressed(this)) {
                            event.consume();
                        }
                    } else if (KeyboardUtil.paste_keyCombination.match(event)) {
                        if (!keyHandler.onCtrlVKeyPressed(this)) {
                            event.consume();
                        }
                    } else if (KeyboardUtil.copy_keyCombination.match(event)) {
                        if (!keyHandler.onCtrlCKeyPressed(this)) {
                            event.consume();
                        }
                    } else if (event.getCode() == KeyCode.HOME) {
                        if (!keyHandler.onHomeKeyPressed(this)) {
                            event.consume();
                        }
                    } else if (event.getCode() == KeyCode.END) {
                        if (!keyHandler.onEndKeyPressed(this)) {
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
    public TerminalMouseHandler mouseHandler() {
        return this.mouseHandler;
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
    public TerminalHistoryHandler historyHandler() {
        return this.historyHandler;
    }

    @Override
    public void historyHandler(TerminalHistoryHandler handler) {
        this.historyHandler = handler;
    }

    @Override
    public boolean checkNop() {
        int pos = this.caretPosition();
        return pos <= this.getNOP();
    }

    @Override
    public void flushNOP() {
        this.NOP.set(this.contentLength());
        super.moveCaretEnd();
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
    public void coverInput(String input) {
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
    }

    @Override
    public void flushPrompt() {

    }

    @Override
    public int caretPosition() {
        return super.caretPosition();
    }

    @Override
    public void caretPosition(int caretPosition) {
        this.positionCaret(caretPosition);
    }

    @Override
    public String fontFamily() {
        return super.getFontFamily();
    }

    @Override
    public void fontFamily(String fontFamily) {
        super.setFontFamily(fontFamily);
    }

    @Override
    public double fontSize() {
        return super.getFontSize();
    }

    @Override
    public void fontSize(double fontSize) {
        super.setFontSize(fontSize);
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
        if (this.caretPosition() >= this.getNOP()) {
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

    @Override
    public TerminalHelpHandler helpHandler() {
        return this.helpHandler;
    }

    @Override
    public void helpHandler(TerminalHelpHandler handler) {
        this.helpHandler = handler;
    }

    @Override
    public TerminalCompleteHandler completeHandler() {
        return this.completeHandler;
    }

    @Override
    public void completeHandler(TerminalCompleteHandler handler) {
        this.completeHandler = handler;
    }

    @Override
    public TerminalKeyHandler keyHandler() {
        return this.keyHandler;
    }

    protected TerminalCommandHandler findHandler(String input) {
        return TerminalManager.findHandler(input);
    }

    @Override
    public void flushCaret() {
        this.caretPosition(this.getNOP());
    }

    @Override
    public void fontSizeIncr() {
        super.fontSizeIncr();
        this.flushCaret();
    }

    @Override
    public void fontSizeDecr() {
        super.fontSizeDecr();
        this.flushCaret();
    }

    @Override
    public void changeFont(Font font) {
        Font font1 = Font.font("Monospaced", FontWeight.NORMAL, 11);
        super.changeFont(font1);
    }

    @Override
    public Set<String> getPrompts() {
        if (super.getPrompts() == null) {
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
                if (StringUtil.isNotBlank(handler.commandFullName())) {
                    set.add(handler.commandFullName());
                }
            }
            this.setPrompts(set);
        }
        return super.getPrompts();
    }

    @Override
    public int getNOP() {
        return this.NOP.get();
    }
}
