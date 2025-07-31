// package cn.oyzh.fx.editor;
//
// import cn.oyzh.fx.plus.util.FXUtil;
//
// /**
//  * @author oyzh
//  * @since 2025-07-30
//  */
// public class EditorStyleMatcher {
//
//     private int visibleParEnd;
//
//     private int visibleParStart;
//
//     private int findStart;
//     private int findEnd;
//
//     private boolean matchStart;
//
//     private boolean matchFinish;
//
//     private final Editor editor;
//
//     public EditorStyleMatcher(Editor editor) {
//         this.editor = editor;
//         FXUtil.runWait(() -> {
//             try {
//                 this.findStart = 0;
//                 this.findStart = editor.getFirstVisibleTextIndex();
//                 this.findEnd = editor.getLastVisibleTextIndex();
//                 System.out.println("findStart=" + findStart);
//                 this.visibleParStart = editor.firstVisibleParToAllParIndex();
//                 this.visibleParEnd = visibleParStart + editor.lastVisibleParToAllParIndex();
//             } catch (Exception ex) {
//                 ex.printStackTrace();
//             }
//         });
//     }
//
//     public boolean isFinish() {
//         return this.matchFinish;
//     }
//
//     public boolean isMatch(int start, int end) {
//         try {
//             this.findStart = end + 1;
//             System.out.println("findStart=" + findStart);
//             if (start < this.findStart) {
//                 return false;
//             }
//             this.matchStart = true;
//             return true;
//         } finally {
//             if (this.findStart >= this.findEnd) {
//                 this.matchFinish = true;
//             }
//         }
//     }
//
//     public int findStart() {
//         return findStart;
//     }
// }
