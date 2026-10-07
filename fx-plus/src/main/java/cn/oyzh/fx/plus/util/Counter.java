package cn.oyzh.fx.plus.util;

import cn.oyzh.i18n.I18nHelper;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 计数器，用于统计任务的失败、成功、忽略数量以及耗时等信息
 *
 * @author oyzh
 * @since 2023-10-09
 */
public class Counter {

    /**
     * 总数
     */
    private Integer sum;

    /**
     * 开始时间
     */
    private Long startTime;

    /**
     * 失败计数
     */
    private final AtomicInteger failCount = new AtomicInteger(0);

    /**
     * 忽略计数
     */
    private final AtomicInteger ignoreCount = new AtomicInteger(0);

    /**
     * 成功计数
     */
    private final AtomicInteger successCount = new AtomicInteger(0);

    /**
     * 额外信息
     */
    private String extraMsg;

    /**
     * 递增失败数量
     *
     * @param failCount 失败数量
     */
    public void incrFail(int failCount) {
        this.update(0, Math.abs(failCount));
    }

    /**
     * 递增数量，正数计为成功、负数计为失败、0 则忽略
     *
     * @param count 数量
     */
    public void incr(int count) {
        if (count == 0) {
        } else if (count > 0) {
            this.incrSuccess(count);
        } else {
            this.incrFail(count);
        }
    }

    /**
     * 递增成功数量
     *
     * @param successCount 成功数量
     */
    public void incrSuccess(int successCount) {
        this.update(1, Math.abs(successCount));
    }

    /**
     * 递增忽略数量
     *
     * @param ignoreCount 忽略
     */
    public void incrIgnore(int ignoreCount) {
        this.update(2, Math.abs(ignoreCount));
    }

    /**
     * 更新
     *
     * @param type 类型 0:失败 1:成功 2:忽略
     */
    public void update(int type) {
        this.update(type, 1);
    }

    /**
     * 更新
     *
     * @param type 类型 0:失败 1:成功 2:忽略
     * @param val  递增值
     */
    public void update(int type, int val) {
        if (this.startTime == null) {
            this.startTime = System.currentTimeMillis();
        }
        if (type == 0) {
            this.failCount.addAndGet(val);
        } else if (type == 1) {
            this.successCount.addAndGet(val);
        } else if (type == 2) {
            this.ignoreCount.addAndGet(val);
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
        this.startTime = null;
        this.failCount.set(0);
        this.ignoreCount.set(0);
        this.successCount.set(0);
        this.extraMsg = null;
    }

    /**
     * 获取耗时毫秒值
     *
     * @return 耗时毫秒值
     */
    public long getElapsed() {
        if (this.startTime == null) {
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
        return this.failCount.get() + this.successCount.get() + this.ignoreCount.get();
    }

    /**
     * 格式化
     *
     * @param tpl 模板
     * @return 结果
     */
    public String format(String tpl) {
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
        builder.append(I18nHelper.total()).append(": $sum, ");
        builder.append(I18nHelper.processed()).append(": $totalCount, ");
        builder.append(I18nHelper.success()).append(": $successCount, ");
        builder.append(I18nHelper.fail()).append(": $failCount, ");
        builder.append(I18nHelper.ignored()).append(": $ignoreCount, ");
        builder.append(I18nHelper.cost()).append(": $elapsed").append(I18nHelper.seconds());
        if (this.extraMsg != null) {
            builder.append(" ").append(I18nHelper.tips()).append(": $extraMsg ");
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
        builder.append(I18nHelper.processed()).append(": $totalCount, ");
        builder.append(I18nHelper.success()).append(": $successCount, ");
        builder.append(I18nHelper.fail()).append(": $failCount, ");
        builder.append(I18nHelper.ignored()).append(": $ignoreCount, ");
        builder.append(I18nHelper.cost()).append(": $elapsed ").append(I18nHelper.seconds());
        if (this.extraMsg != null) {
            builder.append(" ").append(I18nHelper.tips()).append(": $extraMsg ");
        }
        return this.format(builder.toString());
    }

    /**
     * 获取总和。
     *
     * @return 总和
     */
    public Integer getSum() {
        return sum;
    }

    /**
     * 设置总和。
     *
     * @param sum 总和
     */
    public void setSum(Integer sum) {
        this.sum = sum;
    }

    /**
     * 获取开始时间。
     *
     * @return 开始时间
     */
    public Long getStartTime() {
        return startTime;
    }

    /**
     * 设置开始时间。
     *
     * @param startTime 开始时间
     */
    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    /**
     * 获取额外消息。
     *
     * @return 额外消息
     */
    public String getExtraMsg() {
        return extraMsg;
    }

    /**
     * 设置额外消息。
     *
     * @param extraMsg 额外消息
     */
    public void setExtraMsg(String extraMsg) {
        this.extraMsg = extraMsg;
    }
}
