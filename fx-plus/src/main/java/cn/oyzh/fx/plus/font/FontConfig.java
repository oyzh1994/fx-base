package cn.oyzh.fx.plus.font;


/**
 * 字体配置
 *
 * @author oyzh
 * @since 2024/04/05
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

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }
}
