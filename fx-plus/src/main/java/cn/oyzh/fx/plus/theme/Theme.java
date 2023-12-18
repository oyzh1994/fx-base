package cn.oyzh.fx.plus.theme;

import lombok.Getter;

/**
 * 主题枚举
 *
 * @author oyzh
 * @since 2023/05/11
 */
public enum Theme {

    PRIMER_LIGHT("基本风格-明亮模式"),

    PRIMER_DARK("基本风格-暗黑模式"),

    NORD_LIGHT("北欧风格-明亮模式"),

    NORD_DARK("北欧风格-暗黑模式"),

    CUPERTINO_LIGHT("库比蒂诺-明亮模式"),

    CUPERTINO_DARK("库比蒂诺-暗黑模式"),

    DRACULA("暗黑模式");

    @Getter
    private final String desc;

    Theme(String desc) {
        this.desc = desc;
    }
}
