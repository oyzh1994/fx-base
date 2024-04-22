package cn.oyzh.fx.plus.util;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * 计数器
 *
 * @author oyzh
 * @since 2023/2/22
 */
public class Counter {

    /**
     * 总数
     */
    @Getter
    @Setter
    private Integer sum;

    /**
     * 失败计数
     */
    @Getter
    private int failCount;

    /**
     * 开始时间
     */
    @Getter
    private long startTime;

    /**
     * 忽略计数
     */
    @Getter
    private int ignoreCount;

    /**
     * 成功计数
     */
    @Getter
    private int successCount;

    /**
     * 额外信息
     */
    @Setter
    private String extraMsg;

    /**
     * 更新
     *
     * @param type 类型 0:失败 1:成功 2:忽略
     */
    public void update(int type) {
        if (this.startTime == 0) {
            this.startTime = System.currentTimeMillis();
        }
        if (type == 0) {
            this.failCount++;
        } else if (type == 1) {
            this.successCount++;
        } else if (type == 2) {
            this.ignoreCount++;
        }
    }

    /**
     * 更新失败
     */
    public void updateFail() {
        this.update(0);
    }

    /**
     * 更新成功
     */
    public void updateSuccess() {
        this.update(1);
    }

    /**
     * 更新忽略
     */
    public void updateIgnore() {
        this.update(2);
    }

    /**
     * 重置数据
     */
    public void reset() {
        this.sum = null;
        this.failCount = 0;
        this.startTime = 0;
        this.ignoreCount = 0;
        this.successCount = 0;
        this.extraMsg = null;
    }

    /**
     * 获取耗时毫秒值
     *
     * @return 耗时毫秒值
     */
    public long getElapsed() {
        if (this.startTime == 0) {
            return 0;
        }
        return System.currentTimeMillis() - this.startTime;
    }

    /**
     * 获取处理总数
     *
     * @return 处理总数
     */
    public int getTotalCount() {
        return this.failCount + this.successCount + this.ignoreCount;
    }

    /**
     * 格式化
     *
     * @param tpl 模板
     * @return 结果
     */
    public String format(@NonNull String tpl) {
        if (this.sum != null) {
            tpl = tpl.replace("$sum", String.valueOf(this.sum));
        }
        if (this.extraMsg != null) {
            tpl = tpl.replace("$extraMsg", this.extraMsg);
        }
        tpl = tpl.replace("$failCount", String.valueOf(this.failCount));
        tpl = tpl.replace("$ignoreCount", String.valueOf(this.ignoreCount));
        tpl = tpl.replace("$successCount", String.valueOf(this.successCount));
        tpl = tpl.replace("$totalCount", String.valueOf(this.getTotalCount()));
        tpl = tpl.replace("$elapsed", String.valueOf(this.getElapsed() / 1000));
        return tpl;
    }

    /**
     * 已知总数的格式化
     *
     * @return 格式化内容
     */
    public String knownFormat() {
        StringBuilder builder = new StringBuilder();
        builder.append(I18nResourceBundle.i18nString("base.total")).append(": $sum, ");
        builder.append(I18nResourceBundle.i18nString("base.processed")).append(": $totalCount, ");
        builder.append(I18nResourceBundle.i18nString("base.success")).append(": $successCount, ");
        builder.append(I18nResourceBundle.i18nString("base.fail")).append(": $failCount, ");
        builder.append(I18nResourceBundle.i18nString("base.Ignored")).append(": $ignoreCount, ");
        builder.append(I18nResourceBundle.i18nString("base.cost")).append(": $elapsed").append(I18nResourceBundle.i18nString("base.second"));
        if (this.extraMsg != null) {
            builder.append(" ").append(I18nResourceBundle.i18nString("base.tip")).append(": $extraMsg ");
        }
        return this.format(builder.toString());
    }

    /**
     * 未知总数的格式化
     *
     * @return 格式化内容
     */
    public String unknownFormat() {
        StringBuilder builder = new StringBuilder();
        builder.append(I18nResourceBundle.i18nString("base.processed")).append(": $totalCount, ");
        builder.append(I18nResourceBundle.i18nString("base.success")).append(": $successCount, ");
        builder.append(I18nResourceBundle.i18nString("base.fail")).append(": $failCount, ");
        builder.append(I18nResourceBundle.i18nString("base.ignored")).append(": $ignoreCount, ");
        builder.append(I18nResourceBundle.i18nString("base.cost")).append(": $elapsed ").append(I18nResourceBundle.i18nString("base.second"));
        if (this.extraMsg != null) {
            builder.append(" ").append(I18nResourceBundle.i18nString("base.tip")).append(": $extraMsg ");
        }
        return this.format(builder.toString());
    }
}
