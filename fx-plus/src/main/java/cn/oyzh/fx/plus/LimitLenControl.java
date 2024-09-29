package cn.oyzh.fx.plus;

import cn.oyzh.fx.common.util.NumberUtil;
import javafx.scene.control.TextFormatter;

/**
 * @author oyzh
 * @since 2024/1/31
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

    Long getMaxLen();

    void setMaxLen(Long maxLen);
}
