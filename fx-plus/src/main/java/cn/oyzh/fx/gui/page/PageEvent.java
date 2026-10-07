package cn.oyzh.fx.gui.page;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * 分页事件
 *
 * @author oyzh
 * @since 2024-08-06
 */
public class PageEvent extends Event {

    /**
     * 跳页事件类型
     */
    public static final EventType<PageEvent> PAGE_JUMP_EVENT = new EventType<>("PAGE_JUMP_EVENT");

    /**
     * 构造分页事件
     *
     * @param eventType 事件类型
     */
    public PageEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }

    /**
     * 跳页事件
     */
    public static class PageJumpEvent extends PageEvent {

        /**
         * 目标页码
         */
        private int page;

        /**
         * 获取目标页码
         *
         * @return 目标页码
         */
        public int getPage() {
            return page;
        }

        /**
         * 构造跳页事件
         *
         * @param page 目标页码
         */
        public PageJumpEvent(int page) {
            super(PAGE_JUMP_EVENT);
            this.page = page;
        }
    }

    /**
     * 创建跳页事件
     *
     * @param page 目标页码
     * @return 跳页事件
     */
    public static PageJumpEvent jump(int page) {
        return new PageJumpEvent(page);
    }

}
