package cn.oyzh.fx.gui.textfield.search;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * 搜索事件
 *
 * @author oyzh
 * @since 2023/10/23
 */
@Deprecated
public class SearchEvent extends Event {

    /**
     * 搜索触发事件
     */
    public static final EventType<SearchEvent> SEARCH_TRIGGER_EVENT = new EventType<>("SEARCH_TRIGGER_EVENT");

    /**
     * 搜索历史选择事件
     */
    public static final EventType<SearchEvent> SEARCH_HISTORY_SELECTED_EVENT = new EventType<>("SEARCH_HISTORY_SELECTED_EVENT");

    public static final EventType<SearchEvent> SEARCH_SETTING_EVENT = new EventType<>("SEARCH_SETTING_EVENT");

    public SearchEvent(String text, EventType<SearchEvent> type) {
        super(text, null, type);
    }

    @Override
    public String getSource() {
        return (String) super.getSource();
    }

    /**
     * 搜索触发事件
     *
     * @param text 搜索内容
     * @return SearchEvent
     */
    public static SearchEvent searchTrigger(String text) {
        return new SearchEvent(text, SEARCH_TRIGGER_EVENT);
    }

    /**
     * 搜索历史选中事件
     *
     * @param history 搜索历史
     * @return SearchEvent
     */
    public static SearchEvent historySelected(String history) {
        return new SearchEvent(history, SEARCH_HISTORY_SELECTED_EVENT);
    }

    public static SearchEvent searchSetting(String type) {
        return new SearchEvent(type, SEARCH_SETTING_EVENT);
    }
}
