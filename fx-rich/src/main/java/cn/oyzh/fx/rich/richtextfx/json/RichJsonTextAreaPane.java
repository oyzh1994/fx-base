package cn.oyzh.fx.rich.richtextfx.json;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.RegexHelper;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.rich.RichTextStyle;
import cn.oyzh.fx.rich.richtextfx.control.FlexRichTextArea;
import cn.oyzh.fx.rich.richtextfx.control.RichTextAreaPane;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author oyzh
 * @since 2024/5/17
 */
public class RichJsonTextAreaPane extends RichTextAreaPane<FlexRichTextArea> {

    public RichJsonTextAreaPane() {
        super(new FlexRichTextArea());
    }

    @Override
    protected void initTextArea() {
        super.initTextArea();
        this.showLineNum();
        this.addTextChangeListener((observable, oldValue, newValue) -> this.initTextStyle());
    }

    @Override
    public void initTextStyle() {
        FXUtil.runWait(() -> {
            this.clearTextStyle();
            String text = this.getText();
            Matcher matcher1 = RegexHelper.jsonSymbolPattern().matcher(text);
            List<RichTextStyle> styles = new ArrayList<>();
            while (matcher1.find()) {
                styles.add(new RichTextStyle(matcher1.start(), matcher1.end(), "-fx-fill: #4169E1;"));
            }
            Matcher matcher2 = RegexHelper.jsonKeyPattern().matcher(text);
            while (matcher2.find()) {
                styles.add(new RichTextStyle(matcher2.start(), matcher2.end(), "-fx-fill: #EE2C2C;"));
            }
            Matcher matcher3 = RegexHelper.jsonValuePattern().matcher(text);
            while (matcher3.find()) {
                styles.add(new RichTextStyle(matcher3.start(), matcher3.end(), "-fx-fill: green;"));
            }
//            for (RichTextStyle style : styles) {
                this.setStyles(styles);
//            }
        });
    }

    public void setJsonStr(String jsonStr) {
        if (StringUtil.isEmpty(jsonStr)) {
            return;
        }
        if (!JSONUtil.isJson(jsonStr)) {
            this.requestFocus();
            throw new RuntimeException("invalid json text!");
        }
        this.setText(JSONUtil.toPretty(jsonStr));
    }

    public String getJsonStr() {
        String text = this.getText();
        if (StringUtil.isEmpty(text)) {
            return null;
        }
        if (!JSONUtil.isJson(text)) {
            this.requestFocus();
            throw new RuntimeException("invalid json text!");
        }
        return JSONUtil.toJson(text);
    }
}
