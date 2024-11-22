// package cn.oyzh.fx.gui.textfield.search;
//
// import cn.oyzh.fx.gui.skin.SearchTextFieldSkin;
// import cn.oyzh.fx.plus.controls.popup.SearchHistoryPopup;
// import cn.oyzh.fx.plus.controls.textfield.LimitTextField;
// import javafx.event.EventHandler;
// import javafx.scene.control.Skin;
// import lombok.Getter;
// import lombok.Setter;
//
// /**
//  * 搜索文本域
//  *
//  * @author oyzh
//  * @since 2023/10/24
//  */
// @Deprecated
// public class SearchTextField extends LimitTextField {
//
//     /**
//      * 搜索触发事件
//      */
//     @Getter
//     @Setter
//     private EventHandler<SearchEvent> onSearch;
//
//     /**
//      * 搜索历史选中事件
//      */
//     @Getter
//     @Setter
//     private EventHandler<SearchEvent> onHistorySelected;
//
//     /**
//      * 当前皮肤
//      *
//      * @return 皮肤
//      */
//     public SearchTextFieldSkin skin() {
//         return (SearchTextFieldSkin) this.getSkin();
//     }
//
//     /**
//      * 设置搜索历史弹窗
//      *
//      * @param historyPopup 搜索历史弹窗
//      */
//     public void setHistoryPopup(SearchHistoryPopup historyPopup) {
//         this.skin().setHistoryPopup(historyPopup);
//     }
//
//     /**
//      * 获取搜索历史弹窗
//      *
//      * @return 搜索历史弹窗
//      */
//     public SearchHistoryPopup getHistoryPopup() {
//         return this.skin().getHistoryPopup();
//     }
//
//     @Override
//     protected Skin<?> createDefaultSkin() {
//         return new SearchTextFieldSkin(this) {
//             @Override
//             protected void onSearch(String text) {
//                 super.onSearch(text);
//                 if (onSearch != null) {
//                     onSearch.handle(SearchEvent.searchTrigger(text));
//                 }
//             }
//
//             @Override
//             protected void onHistorySelected(String text) {
//                 super.onHistorySelected(text);
//                 if (onHistorySelected != null) {
//                     onHistorySelected.handle(SearchEvent.historySelected(text));
//                 }
//             }
//         };
//     }
// }
