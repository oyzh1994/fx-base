// package cn.oyzh.fx.rich.incubator;
//
// import cn.oyzh.common.util.RegexHelper;
// import cn.oyzh.common.util.StringUtil;
// import cn.oyzh.fx.plus.util.FXUtil;
// import cn.oyzh.fx.rich.RichTextStyle;
//
// import java.util.ArrayList;
// import java.util.List;
// import java.util.regex.Matcher;
//
// /**
// * @author oyzh
// * @since 2025-02-20
// */
// public class RichJsonTextArea extends BaseRichTextArea {
//
//    @Override
//    public void initTextStyle() {
//        super.initTextStyle();
//        // 高亮模式则跳过
//        if (StringUtil.isNotBlank(this.getHighlightText())) {
//            return;
//        }
//        FXUtil.runWait(() -> {
//            String text = this.getText();
//            List<RichTextStyle> styles = new ArrayList<>();
//            Matcher matcher1 = RegexHelper.jsonSymbolPattern().matcher(text);
//            while (matcher1.find()) {
//                styles.add(new RichTextStyle(matcher1.start(), matcher1.end(), "-fx-fill: #4169E1;"));
//            }
//            Matcher matcher2 = RegexHelper.jsonKeyPattern().matcher(text);
//            while (matcher2.find()) {
//                styles.add(new RichTextStyle(matcher2.start(), matcher2.end(), "-fx-fill: #EE2C2C;"));
//            }
//            Matcher matcher3 = RegexHelper.jsonValuePattern().matcher(text);
//            while (matcher3.find()) {
//                styles.add(new RichTextStyle(matcher3.start(), matcher3.end(), "-fx-fill: green;"));
//            }
//            this.setStyles(styles);
//        });
//    }
// }
