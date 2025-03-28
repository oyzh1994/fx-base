//package cn.oyzh.fx.plus.titlebar;
//
//import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
//import cn.oyzh.fx.plus.controls.svg.SVGPane;
//
///**
// * @author oyzh
// * @since 2024-12-09
// */
//public class TitleBarMaximumSVGPane extends SVGPane {
//
//    public TitleBarMaximumSVGPane(String size) {
//        this.setSize(size);
//        this.maximize();
//    }
//
//    public void unMaximize() {
//        this.setChild(new TitleBarUnMaximumSVGGlyph(this.size));
//    }
//
//    public void maximize() {
//        this.setChild(new TitleBarMaximumSVGGlyph(this.size));
//    }
//
//    public boolean isMaximize() {
//        SVGGlyph svgGlyph = (SVGGlyph) this.getChildren().getFirst();
//        return svgGlyph.getUrl().contains("un-maximum");
//    }
//
//    public void setMaximize(boolean maximize) {
//        if (maximize) {
//            this.maximize();
//        } else {
//            this.unMaximize();
//        }
//    }
//}
