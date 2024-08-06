package cn.oyzh.fx.plus.controls.page;

import javafx.event.Event;
import javafx.event.EventType;
import lombok.Getter;

/**
 * @author oyzh
 * @since 2024/8/6
 */
public class PageEvent extends Event {

    /**
     * 搜索触发事件
     */
    public static final EventType<PageEvent> PAGE_JUMP_EVENT = new EventType<>("PAGE_JUMP_EVENT");

    public PageEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }

    public static class PageJumpEvent extends PageEvent {

        @Getter
        private int page;

        public PageJumpEvent(int page) {
            super(PAGE_JUMP_EVENT);
            this.page = page;
        }
    }

    public static PageJumpEvent jump(int page) {
        return new PageJumpEvent(page);
    }

}
