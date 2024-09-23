package cn.oyzh.fx.plus.domain;

import cn.oyzh.fx.common.util.ObjectCopier;
import cn.oyzh.fx.plus.font.FontConfig;
import cn.oyzh.fx.plus.theme.ThemeConfig;
import lombok.Data;

import java.io.Serializable;


/**
 * zk设置
 *
 * @author oyzh
 * @since 2022/8/26
 */
@Data
public class Setting implements Serializable, ObjectCopier<Object> {

    /**
     * 透明度
     */
    private Double opacity;

    /**
     * 主题
     */
    private String theme;

    /**
     * 自定义前景色
     */
    private String fgColor;

    /**
     * 自定义背景色
     */
    private String bgColor;

    /**
     * 自定义强调色
     */
    private String accentColor;

    /**
     * 字体大小
     */
    private Integer fontSize;

    /**
     * 字体名称
     */
    private String fontFamily;

    /**
     * 字体粗细
     */
    private Integer fontWeight;

    /**
     * 区域
     */
    private String locale;

    /**
     * 应用退出
     * 0 到系统托盘
     * 1 每次询问
     * 2 直接关闭程序
     */
    private Integer exitMode;

    /**
     * 记住页面大小
     * 0|null 不记住
     * 1 记住
     */
    private Integer rememberPageSize;

    /**
     * 记住页面拉伸
     * 0 不记住
     * 1|null 记住
     */
    private Integer rememberPageResize;

    /**
     * 记住页面位置
     * 0|null 不记住
     * 1 记住
     */
    private Integer rememberPageLocation;

    /**
     * 标签策略
     * null或者ALL_CONNECT 代表所有连接
     * SINGLE_CONNECT 代表单个连接
     */
    private String tabStrategy;

    /**
     * 标签数量限制
     * 0代表无限，null或者1代表1个
     */
    private Integer tabLimit;

    /**
     * 标签策略是否限制全部连接
     *
     * @return 结果
     */
    public boolean isAllTabLimitStrategy() {
        return this.tabStrategy == null || "ALL_CONNECT".equals(this.tabStrategy);
    }

    /**
     * 标签策略是否限制单个连接
     *
     * @return 结果
     */
    public boolean isSingleTabLimitStrategy() {
        return "SINGLE_CONNECT".equals(this.tabStrategy);
    }

    /**
     * 获取标签数量限制
     *
     * @return 标签数量限制
     */
    public int getTabLimit() {
        return this.tabLimit == null ? 1 : this.tabLimit;
    }

    /**
     * 是否标签数量无限制
     *
     * @return 标签数量限制
     */
    public boolean isTabUnLimit() {
        return this.tabLimit != null && this.tabLimit == 0;
    }

    /**
     * 是否退出到系统托盘
     *
     * @return 结果
     */
    public boolean isExitTray() {
        return this.exitMode == null || this.exitMode == 0;
    }

    /**
     * 退出是否询问
     *
     * @return 结果
     */
    public boolean isExitAsk() {
        return this.exitMode != null && this.exitMode == 1;
    }

    /**
     * 是否直接退出
     *
     * @return 结果
     */
    public boolean isExitDirectly() {
        return this.exitMode != null && this.exitMode == 2;
    }

    /**
     * 是否记住页面大小
     *
     * @return 结果
     */
    public boolean isRememberPageSize() {
        return this.rememberPageSize != null && this.rememberPageSize == 1;
    }

    /**
     * 是否记住页面拉伸
     *
     * @return 结果
     */
    public boolean isRememberPageResize() {
        return this.rememberPageResize == null || this.rememberPageResize == 1;
    }

    /**
     * 是否记住页面位置
     *
     * @return 结果
     */
    public boolean isRememberPageLocation() {
        return this.rememberPageLocation != null && this.rememberPageLocation == 1;
    }

    /**
     * 获取主题配置
     *
     * @return 主题配置
     */
    public ThemeConfig themeConfig() {
        if (this.theme == null || this.fgColor == null || this.bgColor == null || this.accentColor == null) {
            return null;
        }
        ThemeConfig config = new ThemeConfig();
        config.setName(this.theme);
        config.setBgColor(this.bgColor);
        config.setFgColor(this.fgColor);
        config.setAccentColor(this.accentColor);
        return config;
    }

    /**
     * 获取字体配置
     *
     * @return 字体配置
     */
    public FontConfig fontConfig() {
        if (this.fontWeight == null || this.fontFamily == null || this.fontSize == null) {
            return null;
        }
        FontConfig config = new FontConfig();
        config.setSize(this.fontSize);
        config.setWeight(this.fontWeight);
        config.setFamily(this.fontFamily);
        return config;
    }

    @Override
    public void copy(Object obj) {
        if (obj instanceof Setting t1) {
            // 主题
            this.theme = t1.theme;
            this.fgColor = t1.fgColor;
            this.bgColor = t1.bgColor;
            this.accentColor = t1.accentColor;

            // 基本
            this.locale = t1.locale;
            this.opacity = t1.opacity;
            this.exitMode = t1.exitMode;

            // tab
            this.tabLimit = t1.tabLimit;
            this.tabStrategy = t1.tabStrategy;

            // 字体
            this.fontSize = t1.fontSize;
            this.fontWeight = t1.fontWeight;
            this.fontFamily = t1.fontFamily;

            // 页面
            this.rememberPageSize = t1.rememberPageSize;
            this.rememberPageResize = t1.rememberPageResize;
            this.rememberPageLocation = t1.rememberPageLocation;
        }
    }
}
