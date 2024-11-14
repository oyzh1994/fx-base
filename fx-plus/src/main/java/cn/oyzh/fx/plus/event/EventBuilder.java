// package cn.oyzh.event;
//
// /**
//  * 事件构建器
//  *
//  * @author oyzh
//  * @since 2023/4/10
//  */
// public class EventBuilder<D> {
//
//     /**
//      * 数据
//      */
//     private D data;
//
//     /**
//      * 额外数据
//      */
//     private Object extra;
//
//     public EventBuilder<D> data(D data) {
//         this.data = data;
//         return this;
//     }
//
//     public EventBuilder<D> extra(Object extra) {
//         this.extra = extra;
//         return this;
//     }
//
//     public Event<D> build() {
//         Event<D> event = new Event<>();
//         event.data(data);
//         event.extra(extra);
//         return event;
//     }
//
//     public static <D> EventBuilder<D> newBuilder() {
//         return new EventBuilder<>();
//     }
//
// }
