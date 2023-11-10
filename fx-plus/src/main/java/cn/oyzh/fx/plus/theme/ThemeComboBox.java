package cn.oyzh.fx.plus.theme;//package cn.oyzh.fx.plus.theme;
//
//import cn.oyzh.fx.plus.SimpleStringConverter;
//import cn.oyzh.fx.plus.controls.combo.FlexComboBox;
//
//public class ThemeComboBox extends FlexComboBox<Theme> {
//
//    {
//        this.addItems(Theme.values());
//        this.setConverter(new SimpleStringConverter<>() {
//            @Override
//            public String toString(Theme o) {
//                return o.getDesc();
//            }
//        });
//    }
//
//    public void select(String themeName) {
//        if (themeName == null) {
//            this.select(0);
//            return;
//        }
//        try {
//            Theme theme = Theme.valueOf(themeName);
//            super.select(theme);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
//}
