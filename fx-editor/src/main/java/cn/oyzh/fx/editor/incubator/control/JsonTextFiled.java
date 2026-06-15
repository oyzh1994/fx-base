package cn.oyzh.fx.editor.incubator.control;


import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.fx.gui.text.field.LimitTextField;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import javafx.scene.control.Skin;

/**
 * @author oyzh
 * @since 2024/7/21
 */
public class JsonTextFiled extends LimitTextField {

    @Override
    public JsonTextFiledSkin skin() {
        return (JsonTextFiledSkin) super.skin();
    }

    @Override
    protected JsonTextFiledSkin createDefaultSkin() {
        return new JsonTextFiledSkin(this);
    }

    public void setEnlargeWidth(double width) {
        this.skin().setEnlargeWidth(width);
    }

    public double getEnlargeWidth() {
        return this.skin().getEnlargeWidth();
    }

    public void setEnlargeHeight(double height) {
        this.skin().setEnlargeHeight(height);
    }

    public double getEnlargeHeight() {
        return this.skin().getEnlargeHeight();
    }

    private boolean array;

    public boolean isArray() {
        return array;
    }

    public void setArray(boolean array) {
        this.array = array;
    }

    @Override
    public Object getValue() {
        String text = this.getText();
        return this.isArray() ? JSONArray.parseArray(text) : JSONObject.parseObject(text);
    }

    @Override
    public void setValue(Object value) {
        super.setValue(value);
        this.setText(format(value));
    }

    public static String format(Object val) {
        if (val instanceof CharSequence sequence) {
            return JSONUtil.toPretty(sequence.toString());
        }
        return JSONUtil.toPretty(val);
    }

}
