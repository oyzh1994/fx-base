package cn.oyzh.fx.plus.font;


/**
 * 字体配置
 *
 * @author oyzh
 * @since 2024-04-06
 */
public class FontConfig {

    /**
     * 字体大小
     */
    private Integer size;

    /**
     * 字体名称
     */
    private String family;

    /**
     * 字体粗细
     */
    private Integer weight;

    /**
     * 获取大小。
     *
     * @return 大小
     */
    public Integer getSize() {
        return size;
    }

    /**
     * 设置大小。
     *
     * @param size 大小
     */
    public void setSize(Integer size) {
        this.size = size;
    }

    /**
     * 获取族。
     *
     * @return 族
     */
    public String getFamily() {
        return family;
    }

    /**
     * 设置族。
     *
     * @param family 族
     */
    public void setFamily(String family) {
        this.family = family;
    }

    /**
     * 获取粗细。
     *
     * @return 粗细
     */
    public Integer getWeight() {
        return weight;
    }

    /**
     * 设置粗细。
     *
     * @param weight 粗细
     */
    public void setWeight(Integer weight) {
        this.weight = weight;
    }
}
