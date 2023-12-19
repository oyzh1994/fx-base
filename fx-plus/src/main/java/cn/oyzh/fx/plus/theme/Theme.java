package cn.oyzh.fx.plus.theme;

import lombok.Getter;

/**
 * 主题枚举
 *
 * @author oyzh
 * @since 2023/05/11
 */
public enum Theme {

    PRIMER_LIGHT("明亮基础") {
        @Override
        public boolean isDarkMode() {
            return false;
        }
    },

    PRIMER_DARK("暗黑基础") {
        @Override
        public boolean isDarkMode() {
            return true;
        }
    },

    NORD_LIGHT("明亮北欧") {
        @Override
        public boolean isDarkMode() {
            return false;
        }
    },

    NORD_DARK("暗黑北欧") {
        @Override
        public boolean isDarkMode() {
            return true;
        }
    },

    CUPERTINO_LIGHT("明亮库比蒂诺") {
        @Override
        public boolean isDarkMode() {
            return false;
        }
    },

    CUPERTINO_DARK("暗黑库比蒂诺") {
        @Override
        public boolean isDarkMode() {
            return true;
        }
    },

    DRACULA("暗黑德古拉") {
        @Override
        public boolean isDarkMode() {
            return true;
        }
    };

    /**
     * 描述内容
     */
    @Getter
    private final String desc;

    Theme(String desc) {
        this.desc = desc;
    }

    /**
     * 是否暗黑模式
     *
     * @return 结果
     */
    public abstract boolean isDarkMode();
}
