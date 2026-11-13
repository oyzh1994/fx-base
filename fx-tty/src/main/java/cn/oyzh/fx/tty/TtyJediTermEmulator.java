package cn.oyzh.fx.tty;

import com.jediterm.terminal.Terminal;
import com.jediterm.terminal.TerminalDataStream;
import com.jediterm.terminal.emulator.JediEmulator;

import java.io.IOException;

/**
 * JediTerm emulator that preserves all parameters in ANSI mode sequences.
 */
public class TtyJediTermEmulator extends JediEmulator {

    private static final int MAX_MODE_SEQUENCE_LENGTH = 256;

    private final StringBuilder modeSequence = new StringBuilder();

    private boolean parsingModeSequence;

    private boolean privateModeSequence;

    public TtyJediTermEmulator(TerminalDataStream dataStream, Terminal terminal) {
        super(dataStream, terminal);
    }

    @Override
    public void processChar(char ch, Terminal terminal) throws IOException {
        if (!this.parsingModeSequence) {
            if (ch == TtyAscii.ASCII_ESC) {
                this.parsingModeSequence = true;
                this.privateModeSequence = false;
                this.modeSequence.setLength(0);
                this.modeSequence.append(ch);
                return;
            }
            super.processChar(ch, terminal);
            return;
        }

        this.modeSequence.append(ch);
        int length = this.modeSequence.length();
        if (length > MAX_MODE_SEQUENCE_LENGTH) {
            this.replayModeSequence(terminal);
            return;
        }
        if (length == 2) {
            if (ch == '[') {
                return;
            }
            this.replayModeSequence(terminal);
            return;
        }
        if (length == 3) {
            if (ch == '?') {
                this.privateModeSequence = true;
                return;
            }
            this.privateModeSequence = false;
        }
        if (isModeParameter(ch)) {
            return;
        }
        if ((ch == 'h' || ch == 'l') && this.hasModeParameter()) {
            this.completeModeSequence(terminal, ch);
            return;
        }
        this.replayModeSequence(terminal);
    }

    private void completeModeSequence(Terminal terminal, char finalChar) throws IOException {
        int parameterStart = this.privateModeSequence ? 3 : 2;
        String parameters = this.modeSequence.substring(parameterStart, this.modeSequence.length() - 1);
        String[] values = parameters.split(";", -1);
        if (values.length <= 1) {
            this.replayModeSequence(terminal);
            return;
        }

        StringBuilder sequences = new StringBuilder();
        String prefix = this.privateModeSequence ? "\u001B[?" : "\u001B[";
        for (String value : values) {
            sequences.append(prefix).append(value.isBlank() ? "0" : value).append(finalChar);
        }
        this.pushBack(sequences.toString());
        this.resetModeSequence();
    }

    private void replayModeSequence(Terminal terminal) throws IOException {
        String sequence = this.modeSequence.toString();
        this.resetModeSequence();
        this.pushBack(sequence.substring(1));
        super.processChar((char) TtyAscii.ASCII_ESC, terminal);
    }

    private void pushBack(String value) throws IOException {
        char[] chars = value.toCharArray();
        this.myDataStream.pushBackBuffer(chars, chars.length);
    }

    private boolean hasModeParameter() {
        int parameterStart = this.privateModeSequence ? 3 : 2;
        return isModeParameter(this.modeSequence.charAt(parameterStart));
    }

    private void resetModeSequence() {
        this.modeSequence.setLength(0);
        this.parsingModeSequence = false;
        this.privateModeSequence = false;
    }

    private static boolean isModeParameter(char ch) {
        return Character.isDigit(ch) || ch == ';';
    }
}
