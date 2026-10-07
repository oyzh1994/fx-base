package cn.oyzh.fx.tty;

/**
 * 终端滚动条工具类。
 *
 * @author oyzh
 * @since 2026-07-06
 */
public class TtyScrollBarUtils {

    /**
     * 根据行索引计算滚动条对应的值。
     *
     * @param lineIndex    行索引
     * @param totalLines   总行数
     * @param scrollBarMin 滚动条最小值
     * @param scrollBarMax 滚动条最大值
     * @return 滚动条值
     */
    public static double getValueFor(int lineIndex, int totalLines, double scrollBarMin, double scrollBarMax) {
        double result;
        if ((int) scrollBarMin == 0 && totalLines < scrollBarMax) {
            result = lineIndex;
        } else {
            double normalizedValue = (double) lineIndex / (totalLines - 1);
            result = scrollBarMin + normalizedValue * (scrollBarMax - scrollBarMin);
        }
        return result;
    }
}
