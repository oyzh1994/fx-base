package cn.oyzh.fx.plus.controls.geometry;

import cn.oyzh.fx.plus.controls.textfield.ClearableTextField;

/**
 * @author oyzh
 * @since 2024/7/4
 */
public class GeometryTexFiled extends ClearableTextField {

    // public String getGeometryText() {
    //     return this.getText() ;
    // }
    // public String getGeometryText() {
    //     return "POINTFROMTEXT(" + this.getText() + ")";
    // }

    public String getGeometryText() {
        return "ST_GeomFromText('" + this.getText() + "')";
    }
}
