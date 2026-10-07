package cn.oyzh.fx.db.query;


/**
 * 查询提示项，封装提示类型、内容、相关度及扩展内容
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class DBQueryPromptItem {

    /**
     * 类型
     */
    private byte type;

    /**
     * 内容
     */
    private String content;

    /**
     * 相关度
     */
    private double correlation;

    /**
     * 额外内容
     */
    private String extContent;

    /**
     * 获取类型。
     *
     * @return 类型
     */
    public byte getType() {
        return type;
    }

    /**
     * 设置类型。
     *
     * @param type 类型
     */
    public void setType(byte type) {
        this.type = type;
    }

    /**
     * 获取内容。
     *
     * @return 内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置内容。
     *
     * @param content 内容
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 获取关联。
     *
     * @return 关联
     */
    public double getCorrelation() {
        return correlation;
    }

    /**
     * 设置关联。
     *
     * @param correlation 关联
     */
    public void setCorrelation(double correlation) {
        this.correlation = correlation;
    }

    /**
     * 获取扩展内容。
     *
     * @return 扩展内容
     */
    public String getExtContent() {
        return extContent;
    }

    /**
     * 设置扩展内容。
     *
     * @param extContent 扩展内容
     */
    public void setExtContent(String extContent) {
        this.extContent = extContent;
    }
}
