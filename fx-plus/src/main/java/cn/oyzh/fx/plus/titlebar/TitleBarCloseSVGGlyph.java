//package cn.oyzh.fx.plus.titlebar;
//
//import cn.oyzh.fx.plus.theme.ThemeManager;
//import javafx.scene.paint.Color;
//
///**
// * @author oyzh
// * @since 2024/08/06
// */
//public class TitleBarCloseSVGGlyph extends ActionSVGGlyph {
//
//    public TitleBarCloseSVGGlyph() {
//        this.setUrl("/fx-svg/titlebar/close1.svg");
//    }
//
//    public TitleBarCloseSVGGlyph(String size) {
//        this();
//        this.setSizeStr(size);
//    }
//
//    @Override
//    protected void initEvents() {
//        this.setOnMouseEntered(event -> this.setColor(Color.RED));
//        this.setOnMouseExited(event -> this.setColor(ThemeManager.currentForegroundColor()));
//    }
//}
