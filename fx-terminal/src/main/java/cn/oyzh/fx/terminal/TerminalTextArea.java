package cn.oyzh.fx.terminal;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.common.thread.ExecutorUtil;
import cn.oyzh.fx.plus.controls.area.FlexTextArea;
import cn.oyzh.fx.plus.util.FXUtil;
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
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * 命令行文本域
 *
 * @author oyzh
 * @since 2023/05/28
 */
@Slf4j
public class TerminalTextArea extends FlexTextArea implements Terminal {

    /**
     * 不可操作边界
     */
    @Getter
    private int NOP;

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
        this.setFontFamily("Monospaced");
        this.setContextMenu(new ContextMenu());
        this.caretPositionProperty().addListener((observableValue, number, t1) -> {
            if (t1.longValue() < this.NOP) {
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
        return pos <= this.NOP;
    }

    @Override
    public void flushNOP() {
        this.NOP = this.contentLength();
    }

    @Override
    public String getInput() {
        String text = CollUtil.getLast(this.getText().lines().toList());
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
        this.deleteText(this.NOP, this.getLength());
    }

    @Override
    public void coverInput(@NonNull String input) {
        this.replaceText(this.NOP, this.getLength(), input);
    }

    @Override
    public void output(String output) {
        if (output != null) {
            this.replaceText(this.NOP, this.getLength(), output);
            this.scrollToEnd();
        }
    }

    @Override
    public void outputLine(String output) {
        if (output != null) {
            this.appendLine(output);
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
        return StrUtil.nullToDefault(this.prompt, "");
    }

    @Override
    public void prompt(String prompt) {
        if (!StrUtil.equals(prompt, this.prompt)) {
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
        if (StrUtil.equals(text, "\n")) {
            this.setText(prompt);
        } else if (!StrUtil.endWith(text, prompt)) {
            this.appendText(prompt);
        }
        this.flushNOP();
        ExecutorUtil.start(() -> FXUtil.runLater(() -> {
            super._scrollToEnd();
            this._flushCaret();
        }), 20);
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
        this.positionCaret(caretPosition);
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
        if (this.getCaretPosition() >= this.NOP) {
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
            } else if (result.isSuccess()) {
                if (result.getResult() == null) {
                    this.outputByPrompt("");
                } else {
                    this.outputByPrompt(String.valueOf(result.getResult()));
                }
            } else {
                this.outputByPrompt(result.getErrMsg());
            }
        }
    }

    protected TerminalCommandHandler findHandler(String input) {
        return TerminalManager.findHandler(input);
    }

    @Override
    public void flushCaret() {
        FXUtil.runWait(this::_flushCaret);
    }

    @Override
    public void fontSizeIncr() {
        super.fontSizeIncr();
    }

    @Override
    public void fontSizeDecr() {
        super.fontSizeDecr();
    }

    protected void _flushCaret() {
        this.caretPosition(this.getNOP());
        this.requestFocus();
    }

    @Override
    public void moveCaretEnd() {
        FXUtil.runWait(() -> {
            this.caretPosition(this.getLength());
            // this.positionCaret(this.getLength());
            this.requestFocus();
        });
    }
}
