package cn.oyzh.fx.plus;

import cn.oyzh.common.util.NumberUtil;
import javafx.scene.control.TextFormatter;

/**
 * 文本长度限制控件接口，为控件提供统一的文本长度校验能力
 *
 * @author oyzh
 * @since 2024-01-31
 */
public interface LimitLenControl {

    /**
     * 检查边界
     *
     * @param change 更新内容
     * @return 结果
     */
    default boolean checkLenLimit(TextFormatter.Change change) {
        String text = change.getControlNewText();
        if (text.isEmpty()) {
            return true;
        }
        if (this.getMaxLen() != null) {
            try {
                // 新增
                if (change.isAdded() && NumberUtil.isGTEq(text.length(), this.getMaxLen())) {
                    return false;
                }
                // 替换
                if (change.isReplaced() && NumberUtil.isGTEq(text.length(), this.getMaxLen())) {
                    return false;
                }
            } catch (Exception ignore) {
            }
        }
        return true;
    }

    /**
     * 获取最大允许长度
     *
     * @return 最大允许长度
     */
    Long getMaxLen();

    /**
     * 设置最大允许长度
     *
     * @param maxLen 最大允许长度
     */
    void setMaxLen(Long maxLen);
}
