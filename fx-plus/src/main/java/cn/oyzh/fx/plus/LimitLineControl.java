package cn.oyzh.fx.plus;

import cn.oyzh.common.util.NumberUtil;
import javafx.scene.control.TextFormatter;

/**
 * 文本行数限制控件接口，为控件提供统一的文本行数校验能力
 *
 * @author oyzh
 * @since 2024-06-21
 */
public interface LimitLineControl {

    /**
     * 检查边界
     *
     * @param change 更新内容
     * @return 结果
     */
    default boolean checkLineLimit(TextFormatter.Change change) {
        String text = change.getControlNewText();
        if (text.isEmpty()) {
            return true;
        }
        if (this.getMaxLine() != null) {
            try {
                var count = text.lines().count();
                // 新增
                if (change.isAdded() && NumberUtil.isGTEq(count, this.getMaxLine())) {
                    return false;
                }
                // 替换
                if (change.isReplaced() && NumberUtil.isGTEq(text.length(), this.getMaxLine())) {
                    return false;
                }
            } catch (Exception ignore) {
            }
        }
        return true;
    }

    /**
     * 获取最大允许行数
     *
     * @return 最大允许行数
     */
    Long getMaxLine();

    /**
     * 设置最大允许行数
     *
     * @param maxLen 最大允许行数
     */
    void setMaxLine(Long maxLen);
}
