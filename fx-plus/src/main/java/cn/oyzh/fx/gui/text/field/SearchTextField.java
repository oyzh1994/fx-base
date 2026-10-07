package cn.oyzh.fx.gui.text.field;

import cn.oyzh.fx.gui.skin.SearchTextFieldSkin;
import cn.oyzh.fx.plus.controls.popup.SearchHistoryPopup;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Skin;

/**
 * 搜索文本域
 *
 * @author oyzh
 * @since 2023/10/24
 */
public class SearchTextField extends LimitTextField {

    /**
     * 搜索触发事件
     */
    private EventHandler<SearchEvent> onSearch;

    /**
     * 搜索历史选中事件
     */
    private EventHandler<SearchEvent> onHistorySelected;

    /**
     * 设置搜索历史弹窗
     *
     * @param historyPopup 搜索历史弹窗
     */
    public void setHistoryPopup(SearchHistoryPopup historyPopup) {
        this.skin().setPopup(historyPopup);
    }

    /**
     * 获取搜索历史弹窗
     *
     * @return 搜索历史弹窗
     */
    public SearchHistoryPopup getHistoryPopup() {
        return this.skin().getPopup();
    }

    /**
     * 获取搜索触发事件处理器
     *
     * @return 搜索触发事件处理器
     */
    public EventHandler<SearchEvent> getOnSearch() {
        return onSearch;
    }

    /**
     * 设置搜索触发事件处理器
     *
     * @param onSearch 搜索触发事件处理器
     */
    public void setOnSearch(EventHandler<SearchEvent> onSearch) {
        this.onSearch = onSearch;
    }

    /**
     * 获取搜索历史选中事件处理器
     *
     * @return 搜索历史选中事件处理器
     */
    public EventHandler<SearchEvent> getOnHistorySelected() {
        return onHistorySelected;
    }

    /**
     * 设置搜索历史选中事件处理器
     *
     * @param onHistorySelected 搜索历史选中事件处理器
     */
    public void setOnHistorySelected(EventHandler<SearchEvent> onHistorySelected) {
        this.onHistorySelected = onHistorySelected;
    }

    @Override
    public SearchTextFieldSkin skin() {
        return (SearchTextFieldSkin) super.skin();
    }

    @Override
    protected SearchTextFieldSkin createDefaultSkin() {
        return new SearchTextFieldSkin(this) {
            @Override
            protected void onSearch(String text) {
                super.onSearch(text);
                if (onSearch != null) {
                    onSearch.handle(SearchEvent.searchTrigger(text));
                }
            }

            @Override
            protected void onHistorySelected(String text) {
                super.onHistorySelected(text);
                if (onHistorySelected != null) {
                    onHistorySelected.handle(SearchEvent.historySelected(text));
                }
            }
        };
    }

    /**
     * 搜索事件
     *
     * @author oyzh
     * @since 2023/10/23
     */
    public static class SearchEvent extends Event {

        /**
         * 搜索触发事件
         */
        public static final EventType<SearchEvent> SEARCH_TRIGGER_EVENT = new EventType<>("SEARCH_TRIGGER_EVENT");

        /**
         * 搜索历史选择事件
         */
        public static final EventType<SearchEvent> SEARCH_HISTORY_SELECTED_EVENT = new EventType<>("SEARCH_HISTORY_SELECTED_EVENT");

        /**
         * 搜索设置事件
         */
        public static final EventType<SearchEvent> SEARCH_SETTING_EVENT = new EventType<>("SEARCH_SETTING_EVENT");

        /**
         * 构造搜索事件
         *
         * @param text 事件源文本
         * @param type 事件类型
         */
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

        /**
         * 搜索设置事件
         *
         * @param type 设置类型
         * @return SearchEvent
         */
        public static SearchEvent searchSetting(String type) {
            return new SearchEvent(type, SEARCH_SETTING_EVENT);
        }
    }
}
