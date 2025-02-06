//package cn.oyzh.fx.plus.controls.svg;
//
//import cn.oyzh.fx.plus.adapter.TipAdapter;
//import cn.oyzh.fx.plus.controls.box.FXHBox;
//import cn.oyzh.fx.plus.controls.box.FXVBox;
//import cn.oyzh.fx.plus.controls.text.FXText;
//import cn.oyzh.fx.plus.mouse.MouseAdapter;
//import javafx.geometry.NodeOrientation;
//import javafx.scene.Node;
//import lombok.Getter;
//
///**
// * svg面板
// *
// * @author oyzh
// * @since 2025/01/07
// */
//public class SVGBox extends FXVBox implements TipAdapter, MouseAdapter {
//
//    @Getter
//    protected String size;
//
//    public void setSize(String size) {
//        this.size = size;
//        Node node = this.getChild(0);
//        if (node instanceof SVGGlyph glyph) {
//            glyph.setSizeStr(size);
//        }
//    }
//
//    public void setText(String text) {
//        this.setText(new FXText(text));
//    }
//
//    public void setText(FXText text) {
//        if (this.isChildEmpty()) {
//            this.addChild(text);
//        } else {
//            Node node = this.getChild(1);
//            if (node == null) {
//                this.addChild(text);
//            } else {
//                this.setChild(1, text);
//            }
//        }
//    }
//
//    public FXText getText() {
//        return (FXText) this.getFirstChild();
//    }
//
//    public void setGlyph(String glyph) {
//        this.setGlyph(new SVGGlyph(glyph, this.size));
//    }
//
//    public void setGlyph(SVGGlyph glyph) {
//        if (this.isChildEmpty()) {
//            this.addChild(glyph);
//        } else {
//            Node node = this.getChild(0);
//            this.setChild(0, glyph);
//            if (node instanceof FXText) {
//                this.setChild(1, node);
//            }
//        }
//    }
//
//    public SVGGlyph getGlyph() {
//        return (SVGGlyph) this.getChild(1);
//    }
//}
