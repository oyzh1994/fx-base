package cn.oyzh.fx.plus.controls;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.plus.controls.text.FlexText;
import cn.oyzh.fx.plus.font.FontUtil;
import javafx.scene.CacheHint;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;

/**
 * 分割器
 *
 * @author oyzh
 * @since 2022/8/25
 */
public class Splitter extends FlexText {

    {
        this.setCache(false);
        this.setCacheHint(CacheHint.QUALITY);
        this.setDisable(true);
        this.setFill(Color.GREY);
        this.setFlexWidth("100%");
    }

    /**
     * 标题文字
     */
    @Getter
    @Setter
    private String titleText;

    /**
     * 切割文字
     */
    @Getter
    @Setter
    private String splitText = "-";

    ///**
    // * 字符度量
    // */
    //private FontMetrics fontMetrics;

    @Override
    public void resize(double width, double height) {
        super.resize(width, height);
        double computeWidth = this.computeWidth();
        if (!Double.isNaN(computeWidth)) {
            this.renderText(computeWidth);
        }
    }

    /**
     * 渲染文字
     *
     * @param availableWidth 可用宽度
     */
    private void renderText(double availableWidth) {
        //if (this.fontMetrics == null) {
        //    this.fontMetrics = FontUtil.fontMetrics(this.getFont());
        //}

        // 处理标题文字宽度
        if (StringUtil.isNotBlank(this.titleText)) {
            // 计算标题的宽度
            //int titleWidth = this.fontMetrics.stringWidth(this.titleText);
            double titleWidth = FontUtil.stringWidth(this.titleText);
            //titleWidth = titleWidth * Screen.getPrimary().getOutputScaleX();
            // 可用宽度减去标题宽度
            availableWidth = availableWidth - titleWidth;
        }

        // 处理切割文字
        if (StringUtil.isNotBlank(this.splitText)) {
            // 计算分割字符的宽度
            double splitWidth = FontUtil.stringWidth(this.splitText);
            //splitWidth = splitWidth * Screen.getPrimary().getOutputScaleX();
            //int splitWidth = this.fontMetrics.stringWidth(this.splitText);
            // 分割的字符数量=(可用宽度-分割字符的宽度) / 2
            double splitNum = availableWidth / splitWidth;
            if (splitNum > 0) {
                // 拼接文字
                String text = this.splitText.repeat((int) (splitNum * 0.3)) + this.titleText + this.splitText.repeat((int) (splitNum * 0.7));
                // 设置文字
                this.setText(text);
            }
        }
    }
}
