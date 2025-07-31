// package cn.oyzh.fx.editor;
//
// import cn.oyzh.fx.plus.util.FXUtil;
// import org.fxmisc.richtext.GenericStyledArea;
// import org.fxmisc.richtext.model.TwoDimensional;
//
// import java.util.concurrent.atomic.AtomicBoolean;
//
// /**
//  * @author oyzh
//  * @since 2025-07-30
//  */
// public class EditorUtil {
//
//     public static boolean isRangeInVisiblePar(Editor editor, int start, int end, int pStart, int pEnd) {
//         try {
//             TwoDimensional.Position index1 = editor.getContent().offsetToPosition(start, TwoDimensional.Bias.Backward);
//             if (index1.getMajor() < pStart) {
//                 return false;
//             }
//             TwoDimensional.Position index2 = editor.getContent().offsetToPosition(end, TwoDimensional.Bias.Backward);
//             if (index2.getMajor() > pEnd) {
//                 return false;
//             }
//             return true;
//         } catch (Exception ex) {
//             ex.printStackTrace();
//         }
//         return false;
//     }
// }
