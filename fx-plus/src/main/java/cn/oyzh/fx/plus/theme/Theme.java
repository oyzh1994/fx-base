package cn.oyzh.fx.plus.theme;

import lombok.Getter;

/**
 * 主题枚举
 *
 * @author oyzh
 * @since 2023/05/11
 */
public enum Theme {

    LIGHT(0, "明亮模式"),

    DARK(1, "暗黑模式");

    @Getter
    private final Integer code;

    @Getter
    private final String desc;

    Theme(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
