package cn.oyzh.fx.plus.domain;

import cn.oyzh.common.util.ObjectCopier;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.font.FontConfig;
import cn.oyzh.fx.plus.theme.ThemeConfig;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


/**
 * zk设置
 *
 * @author oyzh
 * @since 2022/8/26
 */
@Setter
public class Setting implements Serializable, ObjectCopier<Object> {

    /**
     * 透明度
     */
    @Column
    @Getter
    private Float opacity;

    /**
     * 主题
     */
    @Column
    @Getter
    private String theme;

    /**
     * 自定义前景色
     */
    @Column
    @Getter
    private String fgColor;

    /**
     * 自定义背景色
     */
    @Column
    @Getter
    private String bgColor;

    /**
     * 自定义强调色
     */
    @Column
    @Getter
    private String accentColor;

    /**
     * 字体大小
     */
    @Column
    private Byte fontSize;

    /**
     * 字体名称
     */
    @Column
    @Getter
    private String fontFamily;

    /**
     * 字体粗细
     */
    @Column
    private Short fontWeight;

    /**
     * 编辑器字体大小
     */
    @Column
    private Byte editorFontSize;

    /**
     * 编辑器字体名称
     */
    @Column
    private String editorFontFamily;

    /**
     * 编辑器字体粗细
     */
    @Column
    private Short editorFontWeight;

    /**
     * 终端字体大小
     */
    @Column
    private Byte terminalFontSize;

    /**
     * 终端字体名称
     */
    @Column
    private String terminalFontFamily;

    /**
     * 终端字体粗细
     */
    @Column
    private Short terminalFontWeight;

    /**
     * 区域
     */
    @Column
    @Getter
    private String locale;

    /**
     * 应用退出
     * 0 到系统托盘
     * 1 每次询问
     * 2 直接关闭程序
     */
    @Column
    @Getter
    private Byte exitMode;

    /**
     * 记住页面大小
     * 0|null 不记住
     * 1 记住
     */
    @Column
    @Getter
    private Byte rememberPageSize;

    /**
     * 记住页面拉伸
     * 0 不记住
     * 1|null 记住
     */
    @Column
    @Getter
    private Byte rememberPageResize;

    /**
     * 记住页面位置
     * 0|null 不记住
     * 1 记住
     */
    @Column
    @Getter
    private Byte rememberPageLocation;

    /**
     * 页面宽
     */
    @Column
    @Getter
    private Double pageWidth;

    /**
     * 页面高
     */
    @Column
    @Getter
    private Double pageHeight;

    /**
     * 屏幕x
     */
    @Column
    @Getter
    private Double pageScreenX;

    /**
     * 屏幕y
     */
    @Column
    @Getter
    private Double pageScreenY;

    /**
     * 是否最大化
     */
    @Column
    @Getter
    private Boolean pageMaximized;

    /**
     * 主页左侧宽
     */
    @Column
    @Getter
    private Float pageLeftWidth;

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
     * 获取页面宽
     *
     * @return 页面宽
     */
    public Double getPageWidth() {
        return this.pageWidth == null ? null : this.pageWidth < 100 ? null : this.pageWidth;
    }

    /**
     * 获取页面高
     *
     * @return 页面高
     */
    public Double getPageHeight() {
        return this.pageHeight == null ? null : this.pageHeight < 100 ? null : this.pageHeight;
    }

    /**
     * 是否最大化
     *
     * @return 结果
     */
    public Boolean isPageMaximized() {
        return this.pageMaximized != null && this.pageMaximized;
    }

    /**
     * 获取主页左侧宽
     *
     * @return 主页左侧宽
     */
    public Float getPageLeftWidth() {
        return this.pageLeftWidth == null ? null : this.pageLeftWidth < 100 ? null : this.pageLeftWidth;
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
        if (this.fontWeight == null && this.fontFamily == null && this.fontSize == null) {
            return null;
        }
        FontConfig config = new FontConfig();
        if (StringUtil.isNotBlank(this.fontFamily)) {
            config.setFamily(this.fontFamily);
        } else {
            config.setFamily(Font.getDefault().getFamily());
        }
        if (this.fontWeight != null) {
            config.setWeight(Integer.valueOf(this.fontWeight));
        } else {
            config.setWeight(FontWeight.NORMAL.getWeight());
        }
        if (this.fontSize != null) {
            config.setSize(Integer.valueOf(this.fontSize));
        } else {
            config.setSize(11);
        }
        return config;
    }

    /**
     * 获取编辑器字体配置
     *
     * @return 字体配置
     */
    public FontConfig editorFontConfig() {
        if (this.editorFontWeight == null && this.editorFontFamily == null && this.editorFontSize == null) {
            return null;
        }
        FontConfig config = new FontConfig();
        if (StringUtil.isNotBlank(this.editorFontFamily)) {
            config.setFamily(this.editorFontFamily);
        } else {
            config.setFamily(Font.getDefault().getFamily());
        }
        if (this.editorFontWeight != null) {
            config.setWeight(Integer.valueOf(this.editorFontWeight));
        } else {
            config.setWeight(FontWeight.NORMAL.getWeight());
        }
        if (this.editorFontSize != null) {
            config.setSize(Integer.valueOf(this.editorFontSize));
        } else {
            config.setSize(10);
        }
        return config;
    }

    /**
     * 获取终端字体配置
     *
     * @return 字体配置
     */
    public FontConfig terminalFontConfig() {
        if (this.terminalFontWeight == null && this.terminalFontFamily == null && this.terminalFontSize == null) {
            return null;
        }
        FontConfig config = new FontConfig();
        if (StringUtil.isNotBlank(this.terminalFontFamily)) {
            config.setFamily(this.terminalFontFamily);
        } else {
            config.setFamily("Monospaced");
        }
        if (this.terminalFontWeight != null) {
            config.setWeight(Integer.valueOf(this.terminalFontWeight));
        } else {
            config.setWeight(FontWeight.NORMAL.getWeight());
        }
        if (this.terminalFontSize != null) {
            config.setSize(Integer.valueOf(this.terminalFontSize));
        } else {
            config.setSize(10);
        }
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

            // 字体
            this.fontSize = t1.fontSize;
            this.fontWeight = t1.fontWeight;
            this.fontFamily = t1.fontFamily;
            this.editorFontSize = t1.editorFontSize;
            this.editorFontFamily = t1.editorFontFamily;
            this.editorFontWeight = t1.editorFontWeight;
            this.terminalFontSize = t1.terminalFontSize;
            this.terminalFontFamily = t1.terminalFontFamily;
            this.terminalFontWeight = t1.terminalFontWeight;

            // 页面
            this.pageWidth = t1.pageWidth;
            this.pageHeight = t1.pageHeight;
            this.pageScreenX = t1.pageScreenX;
            this.pageScreenY = t1.pageScreenY;
            this.pageMaximized = t1.pageMaximized;
            this.pageLeftWidth = t1.pageLeftWidth;
            this.rememberPageSize = t1.rememberPageSize;
            this.rememberPageResize = t1.rememberPageResize;
            this.rememberPageLocation = t1.rememberPageLocation;
        }
    }

    public byte getFontSize() {
        return this.fontSize == null ? defaultFontSize() : this.fontSize;
    }

    public String getFontFamily() {
        return this.fontFamily == null ? defaultFontFamily() : this.fontFamily;
    }

    public int getFontWeight() {
        return this.fontWeight == null ? defaultFontWeight() : this.fontWeight;
    }

    public static byte defaultFontSize() {
        return 11;
    }

    public static String defaultFontFamily() {
        return Font.getDefault().getFamily();
    }

    public static int defaultFontWeight() {
        return FontWeight.NORMAL.getWeight();
    }

    public Byte getEditorFontSize() {
        return this.editorFontSize == null ? defaultEditorFontSize() : this.editorFontSize;
    }

    public String getEditorFontFamily() {
        return this.editorFontFamily == null ? defaultEditorFontFamily() : this.editorFontFamily;
    }

    public int getEditorFontWeight() {
        return this.editorFontWeight == null ? defaultEditorFontWeight() : this.editorFontWeight;
    }

    public static byte defaultEditorFontSize() {
        return 10;
    }

    public static String defaultEditorFontFamily() {
        return Font.getDefault().getFamily();
    }

    public static int defaultEditorFontWeight() {
        return FontWeight.NORMAL.getWeight();
    }

    public Byte getTerminalFontSize() {
        return this.terminalFontSize == null ? defaultTerminalFontSize() : this.terminalFontSize;
    }

    public int getTerminalFontWeight() {
        return this.terminalFontWeight == null ? defaultTerminalFontWeight() : this.terminalFontWeight;
    }

    public String getTerminalFontFamily() {
        return this.terminalFontFamily == null ? defaultTerminalFontFamily() : this.terminalFontFamily;
    }

    public static byte defaultTerminalFontSize() {
        return 10;
    }

    public static String defaultTerminalFontFamily() {
        return "Monospace";
    }

    public static int defaultTerminalFontWeight() {
        return FontWeight.NORMAL.getWeight();
    }
}
