package cn.oyzh.fx.tty.ghostty;

import java.util.regex.Pattern;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.util.Duration;

import cn.oyzh.fx.gui.svg.glyph.CloseSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.Down1SVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.Up1SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Ghostty 搜索栏。
 *
 * <p>UI 结构与 JediTerm 的 {@code FXJediTermDefaultSearchComponent} 对齐：
 * 输入框 + 匹配计数 + 上一个/下一个 + 关闭按钮，使用应用主题的控件样式。</p>
 */
final class SearchUi {
    private static final double WIDTH = 360;
    private static final double HEIGHT = 30;
    private static final Pattern SHORT_QUERY = Pattern.compile("\\X{1,2}");
    private static final long SEARCH_BUDGET_NS = 1_000_000L;
    private static final long REFRESH_INTERVAL_NS = 24_000_000L;

    private final TerminalSession terminalSession;
    private final Runnable matchSelected;
    private final Runnable render;
    private final Runnable closeAction;
    private final HBox view;
    private final TextField field;
    private final Label count;
    private final SVGGlyph next;
    private final SVGGlyph previous;
    private final SVGGlyph close;
    private final PauseTransition debounce = new PauseTransition(Duration.millis(300));
    private final AnimationTimer searchTimer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            if (now - lastProgress < REFRESH_INTERVAL_NS) {
                return;
            }
            lastProgress = now;
            terminalSession.advanceSearch(SEARCH_BUDGET_NS);
            if (refreshResults()) {
                render.run();
            }
        }
    };
    private TerminalSession.SearchSnapshot snapshot = TerminalSession.SearchSnapshot.empty();
    private long lastProgress;

    SearchUi(
            TerminalSession terminalSession,
            ObjectProperty<Font> font,
            StringProperty searchPromptText,
            ReadOnlyDoubleProperty terminalWidth,
            Runnable matchSelected,
            Runnable render,
            Runnable closeAction) {
        this.terminalSession = terminalSession;
        this.matchSelected = matchSelected;
        this.render = render;
        this.closeAction = closeAction;
        field = new TextField();
        field.setId("ghosttyfx-search-field");
        field.setPrefHeight(22);
        field.setMaxHeight(22);
        field.setMinWidth(0);
        field.setMaxWidth(220);
        field.setFocusTraversable(true);
        // 显式安装默认皮肤，确保 TextField 的编辑/全选/快捷键行为立即可用。
        field.setSkin(new TextFieldSkin(field));
        field.promptTextProperty().bind(searchPromptText);
        field.textProperty().addListener((_, _, value) -> {
            debounce.stop();
            if (visible()) {
                if (SHORT_QUERY.matcher(value).matches()) {
                    debounce.playFromStart();
                } else {
                    startSearch();
                }
            }
        });
        field.sceneProperty().addListener((_, _, _) -> updateSearchTimer());
        debounce.setOnFinished(_ -> startSearch());

        count = new Label();
        count.setId("ghosttyfx-search-count");
        count.setPrefHeight(24);
        count.setMinWidth(Label.USE_PREF_SIZE);

        next = createNextButton();
        next.setId("ghosttyfx-search-next");
        previous = createPreviousButton();
        previous.setId("ghosttyfx-search-previous");
        close = createCloseButton();
        close.setId("ghosttyfx-search-close");

        view = new HBox(field, count, next, previous, close);
        view.setId("ghosttyfx-search");
        view.setAlignment(Pos.CENTER_LEFT);
        view.setPadding(new Insets(3));
        view.setMinWidth(0);
        view.setPrefHeight(HEIGHT);
        view.setMinHeight(HEIGHT);
        view.setMaxHeight(HEIGHT);
        view.setMaxWidth(WIDTH);
        view.prefWidthProperty().bind(Bindings.createDoubleBinding(
                () -> Math.max(0, Math.min(WIDTH, terminalWidth.get() - 16)), terminalWidth));
        view.setStyle("-fx-background-color: -fx-background;");
        view.setVisible(false);

        HBox.setHgrow(field, Priority.ALWAYS);
        HBox.setMargin(count, new Insets(3, 0, 0, 3));
        HBox.setMargin(next, new Insets(3, 0, 0, 3));
        HBox.setMargin(previous, new Insets(3, 0, 0, 3));
        HBox.setMargin(close, new Insets(3, 0, 0, 3));
        next.setOnMousePrimaryClicked(event -> this.selectNext());
        previous.setOnMousePrimaryClicked(event -> this.selectPrevious());
        close.setOnMousePrimaryClicked(event -> this.closeAction.run());
    }

    private SVGGlyph createNextButton() {
        return new Down1SVGGlyph();
    }

    private SVGGlyph createPreviousButton() {
        return new Up1SVGGlyph();
    }

    private SVGGlyph createCloseButton() {
        return new CloseSVGGlyph();
    }

    HBox view() {
        return view;
    }

    boolean visible() {
        return view.isVisible();
    }

    boolean fieldFocused() {
        return visible() && field.getScene() != null && field.getScene().getFocusOwner() == field;
    }

    TerminalSession.SearchResult result() {
        return snapshot.result();
    }

    int selectedMatch() {
        return snapshot.highlightedMatch();
    }

    int selectedIndex() {
        return snapshot.selectedIndex();
    }

    int matchCount() {
        return snapshot.totalMatches();
    }

    String text() {
        return field.getText();
    }

    void open(String query) {
        var wasVisible = visible();
        view.setVisible(true);
        if (query != null || !wasVisible) {
            field.setText(query == null ? "" : query);
        }
        field.requestFocus();
        field.selectAll();
        if (!wasVisible && debounce.getStatus() != javafx.animation.Animation.Status.RUNNING) {
            startSearch();
        }
    }

    void close() {
        view.setVisible(false);
        cancel();
    }

    void cancel() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::cancel);
            return;
        }
        debounce.stop();
        searchTimer.stop();
        terminalSession.setSearchNeedle("");
        snapshot = TerminalSession.SearchSnapshot.empty();
        count.setText("");
    }

    private void startSearch() {
        if (!visible()) {
            return;
        }
        terminalSession.setSearchNeedle(field.getText());
        refresh();
    }

    void refresh() {
        if (visible()) {
            refreshResults();
            render.run();
        }
    }

    boolean refreshResults() {
        if (!visible()) {
            return false;
        }
        var nextSnapshot = terminalSession.searchSnapshot();
        var changed = !nextSnapshot.equals(snapshot);
        snapshot = nextSnapshot;
        updateCount();
        updateSearchTimer();
        return changed;
    }

    private void updateCount() {
        if (field.getText().isEmpty()) {
            count.setText("");
            return;
        }
        if (snapshot.totalMatches() == 0) {
            count.setText(snapshot.complete() ? "0 of 0" : "...");
            return;
        }
        int selected = snapshot.selectedIndex();
        count.setText((selected < 0 ? 1 : selected + 1) + " of " + snapshot.totalMatches());
    }

    boolean selectNext() {
        return selectMatch(true);
    }

    boolean selectPrevious() {
        return selectMatch(false);
    }

    private boolean selectMatch(boolean next) {
        if (!visible()) {
            return false;
        }
        if (debounce.getStatus() == javafx.animation.Animation.Status.RUNNING) {
            debounce.stop();
            startSearch();
        }
        terminalSession.advanceSearch(SEARCH_BUDGET_NS);
        refreshResults();
        if (snapshot.selectedIndex() >= 0
                && snapshot.selectedIndex() == (next ? snapshot.totalMatches() - 1 : 0)) {
            render.run();
            return true;
        }
        var selected = terminalSession.selectSearchMatch(next);
        if (selected) {
            matchSelected.run();
        }
        refresh();
        return true;
    }

    private void updateSearchTimer() {
        if (field.getScene() != null && visible() && !snapshot.complete()) {
            searchTimer.start();
        } else {
            searchTimer.stop();
        }
    }

    void applyTheme(TerminalTheme theme) {
        // 搜索栏使用应用主题的控件样式，与 JediTerm 搜索组件保持一致。
    }
}
