package cn.oyzh.fx.plus.domain;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.font.FontConfig;
import cn.oyzh.fx.plus.opacity.OpacityConfig;
import cn.oyzh.fx.plus.theme.ThemeConfig;
import cn.oyzh.store.jdbc.Column;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.Serializable;


/**
 * app设置
 *
 * @author oyzh
 * @since 2022/8/26
 */
public class AppSetting implements Serializable, ObjectCopier<Object> {

    /**
     * 窗口透明度
     */
    @Column
    protected Float opacity;

    /**
     * 标题栏透明度
     */
    @Column
    @Deprecated
    protected Float titleBarOpacity;

    /**
     * 主题
     */
    @Column
    protected String theme;

    /**
     * 自定义前景色
     */
    @Column
    protected String fgColor;

    /**
     * 自定义背景色
     */
    @Column
    protected String bgColor;

    /**
     * 自定义强调色
     */
    @Column
    protected String accentColor;

    /**
     * 字体大小
     */
    @Column
    protected Byte fontSize;

    /**
     * 字体名称
     */
    @Column
    protected String fontFamily;

    /**
     * 字体粗细
     */
    @Column
    protected Short fontWeight;

    /**
     * 编辑器字体大小
     */
    @Column
    protected Byte editorFontSize;

    /**
     * 编辑器字体名称
     */
    @Column
    protected String editorFontFamily;

    /**
     * 编辑器字体粗细
     */
    @Column
    protected Short editorFontWeight;

    /**
     * 终端字体大小
     */
    @Column
    protected Byte terminalFontSize;

    /**
     * 终端字体名称
     */
    @Column
    protected String terminalFontFamily;

    /**
     * 终端字体粗细
     */
    @Column
    protected Short terminalFontWeight;

    //    /**
    //     * 查询字体大小
    //     */
    //    @Column
    //    protected Byte queryFontSize;
    //
    //    /**
    //     * 查询字体名称
    //     */
    //    @Column
    //    protected String queryFontFamily;
    //
    //    /**
    //     * 查询字体粗细
    //     */
    //    @Column
    //    protected Short queryFontWeight;

    /**
     * 区域
     */
    @Column
    protected String locale;

    /**
     * 应用退出
     * 0 到系统托盘
     * 1 每次询问
     * 2 直接关闭程序
     */
    @Column
    protected Byte exitMode;

    /**
     * 记住页面大小
     * 0|null 不记住
     * 1 记住
     */
    @Column
    protected Byte rememberPageSize;

    /**
     * 记住页面拉伸
     * 0 不记住
     * 1|null 记住
     */
    @Column
    @Deprecated
    protected Byte rememberPageResize;

    /**
     * 记住页面位置
     * 0|null 不记住
     * 1 记住
     */
    @Column
    protected Byte rememberPageLocation;

    /**
     * 页面宽
     */
    @Column
    protected Double pageWidth;

    /**
     * 页面高
     */
    @Column
    protected Double pageHeight;

    /**
     * 屏幕x
     */
    @Column
    protected Double pageScreenX;

    /**
     * 屏幕y
     */
    @Column
    protected Double pageScreenY;

    /**
     * 是否最大化
     */
    @Column
    protected Boolean pageMaximized;

    /**
     * 主页左侧宽
     */
    @Column
    @Deprecated
    protected Float pageLeftWidth;

    /**
     * 是否退出到系统托盘
     *
     * @return 结果
     */
    @Deprecated
    public boolean isExitTray() {
        return this.exitMode != null && this.exitMode == 0;
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
        return this.exitMode == null || this.exitMode == 2;
    }

    /**
     * 是否记住页面拉伸
     *
     * @return 结果
     */
    @Deprecated
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
    @Deprecated
    public Float getPageLeftWidth() {
        return this.pageLeftWidth == null ? null : this.pageLeftWidth < 100 ? null : this.pageLeftWidth;
    }

    /**
     * 获取透明度配置
     *
     * @return 透明度配置
     */
    public OpacityConfig opacityConfig() {
        if (this.opacity == null && this.titleBarOpacity == null) {
            return null;
        }
        OpacityConfig config = new OpacityConfig();
        config.setWindowOpacity(this.opacity);
        config.setTitleOpacity(this.titleBarOpacity);
        return config;
    }

    /**
     * 获取主题配置
     *
     * @return 主题配置
     */
    public ThemeConfig themeConfig() {
        if (this.theme == null && this.fgColor == null && this.bgColor == null && this.accentColor == null) {
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
        //        if (this.fontWeight == null && this.fontFamily == null && this.fontSize == null) {
        //            return null;
        //        }
        FontConfig config = new FontConfig();
        if (StringUtil.isNotBlank(this.fontFamily)) {
            config.setFamily(this.fontFamily);
        } else {
            config.setFamily(defaultFontFamily());
        }
        if (this.fontWeight != null) {
            config.setWeight(Integer.valueOf(this.fontWeight));
        } else {
            config.setWeight(defaultFontWeight());
        }
        if (this.fontSize != null) {
            config.setSize(Integer.valueOf(this.fontSize));
        } else {
            config.setSize((int) defaultFontSize());
        }
        return config;
    }

    /**
     * 获取编辑器字体配置
     *
     * @return 字体配置
     */
    public FontConfig editorFontConfig() {
        FontConfig config = new FontConfig();
        if (StringUtil.isNotBlank(this.editorFontFamily)) {
            config.setFamily(this.editorFontFamily);
        } else {
            config.setFamily(defaultEditorFontFamily());
        }
        if (this.editorFontWeight != null) {
            config.setWeight(Integer.valueOf(this.editorFontWeight));
        } else {
            config.setWeight(defaultEditorFontWeight());
        }
        if (this.editorFontSize != null) {
            config.setSize(Integer.valueOf(this.editorFontSize));
        } else {
            config.setSize((int) defaultEditorFontSize());
        }
        return config;
    }

    /**
     * 获取终端字体配置
     *
     * @return 字体配置
     */
    public FontConfig terminalFontConfig() {
        FontConfig config = new FontConfig();
        if (StringUtil.isNotBlank(this.terminalFontFamily)) {
            config.setFamily(this.terminalFontFamily);
        } else {
            config.setFamily(defaultTerminalFontFamily());
        }
        if (this.terminalFontWeight != null) {
            config.setWeight(Integer.valueOf(this.terminalFontWeight));
        } else {
            config.setWeight(defaultTerminalFontWeight());
        }
        if (this.terminalFontSize != null) {
            config.setSize(Integer.valueOf(this.terminalFontSize));
        } else {
            config.setSize((int) defaultTerminalFontSize());
        }
        return config;
    }

    /**
     * 获取透明度，下限为 50
     *
     * @return 透明度
     */
    public float getOpacity() {
        return opacity == null ? 100 : Math.max(50, opacity);
    }

    /**
     * 设置窗口透明度。
     *
     * @param opacity 窗口透明度
     */
    public void setOpacity(Float opacity) {
        this.opacity = opacity;
    }

    /**
     * 获取标题栏透明度。
     *
     * @return 标题栏透明度
     */
    @Deprecated
    public Float getTitleBarOpacity() {
        return titleBarOpacity;
    }

    /**
     * 设置标题栏透明度。
     *
     * @param titleBarOpacity 标题栏透明度
     */
    @Deprecated
    public void setTitleBarOpacity(Float titleBarOpacity) {
        this.titleBarOpacity = titleBarOpacity;
    }

    /**
     * 获取主题。
     *
     * @return 主题
     */
    public String getTheme() {
        return theme;
    }

    /**
     * 设置主题。
     *
     * @param theme 主题
     */
    public void setTheme(String theme) {
        this.theme = theme;
    }

    /**
     * 获取自定义前景色。
     *
     * @return 自定义前景色
     */
    public String getFgColor() {
        return fgColor;
    }

    /**
     * 设置自定义前景色。
     *
     * @param fgColor 自定义前景色
     */
    public void setFgColor(String fgColor) {
        this.fgColor = fgColor;
    }

    /**
     * 获取自定义背景色。
     *
     * @return 自定义背景色
     */
    public String getBgColor() {
        return bgColor;
    }

    /**
     * 设置自定义背景色。
     *
     * @param bgColor 自定义背景色
     */
    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }

    /**
     * 获取自定义强调色。
     *
     * @return 自定义强调色
     */
    public String getAccentColor() {
        return accentColor;
    }

    /**
     * 设置自定义强调色。
     *
     * @param accentColor 自定义强调色
     */
    public void setAccentColor(String accentColor) {
        this.accentColor = accentColor;
    }

    /**
     * 设置字体大小。
     *
     * @param fontSize 字体大小
     */
    public void setFontSize(Byte fontSize) {
        this.fontSize = fontSize;
    }

    /**
     * 设置字体名称。
     *
     * @param fontFamily 字体名称
     */
    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    /**
     * 设置字体粗细。
     *
     * @param fontWeight 字体粗细
     */
    public void setFontWeight(Short fontWeight) {
        this.fontWeight = fontWeight;
    }

    /**
     * 设置编辑器字体大小。
     *
     * @param editorFontSize 编辑器字体大小
     */
    public void setEditorFontSize(Byte editorFontSize) {
        this.editorFontSize = editorFontSize;
    }

    /**
     * 设置编辑器字体名称。
     *
     * @param editorFontFamily 编辑器字体名称
     */
    public void setEditorFontFamily(String editorFontFamily) {
        this.editorFontFamily = editorFontFamily;
    }

    /**
     * 设置编辑器字体粗细。
     *
     * @param editorFontWeight 编辑器字体粗细
     */
    public void setEditorFontWeight(Short editorFontWeight) {
        this.editorFontWeight = editorFontWeight;
    }

    /**
     * 设置终端字体大小。
     *
     * @param terminalFontSize 终端字体大小
     */
    public void setTerminalFontSize(Byte terminalFontSize) {
        this.terminalFontSize = terminalFontSize;
    }

    /**
     * 设置终端字体名称。
     *
     * @param terminalFontFamily 终端字体名称
     */
    public void setTerminalFontFamily(String terminalFontFamily) {
        this.terminalFontFamily = terminalFontFamily;
    }

    /**
     * 设置终端字体粗细。
     *
     * @param terminalFontWeight 终端字体粗细
     */
    public void setTerminalFontWeight(Short terminalFontWeight) {
        this.terminalFontWeight = terminalFontWeight;
    }

    //    public void setQueryFontSize(Byte queryFontSize) {
    //        this.queryFontSize = queryFontSize;
    //    }
    //
    //    public void setQueryFontFamily(String queryFontFamily) {
    //        this.queryFontFamily = queryFontFamily;
    //    }
    //
    //    public void setQueryFontWeight(Short queryFontWeight) {
    //        this.queryFontWeight = queryFontWeight;
    //    }
    //
    //    /**
    //     * 获取查询字体配置
    //     *
    //     * @return 字体配置
    //     */
    //    public FontConfig queryFontConfig() {
    //        if (this.queryFontWeight == null && this.queryFontFamily == null && this.queryFontSize == null) {
    //            return null;
    //        }
    //        FontConfig config = new FontConfig();
    //        if (StringUtil.isNotBlank(this.queryFontFamily)) {
    //            config.setFamily(this.queryFontFamily);
    //        } else {
    //            config.setFamily(defaultQueryFontFamily());
    //        }
    //        if (this.queryFontWeight != null) {
    //            config.setWeight(Integer.valueOf(this.queryFontWeight));
    //        } else {
    //            config.setWeight(defaultQueryFontWeight());
    //        }
    //        if (this.queryFontSize != null) {
    //            config.setSize(Integer.valueOf(this.queryFontSize));
    //        } else {
    //            config.setSize((int) defaultQueryFontSize());
    //        }
    //        return config;
    //    }

    /**
     * 获取区域。
     *
     * @return 区域
     */
    public String getLocale() {
        return locale;
    }

    /**
     * 设置区域。
     *
     * @param locale 区域
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * 获取应用退出模式。
     *
     * @return 应用退出模式
     */
    public Byte getExitMode() {
        return exitMode;
    }

    /**
     * 设置应用退出模式。
     *
     * @param exitMode 应用退出模式
     */
    public void setExitMode(Byte exitMode) {
        this.exitMode = exitMode;
    }

    /**
     * 是否记住页面大小
     *
     * @return 结果
     */
    public boolean isRememberPageSize() {
        return this.rememberPageSize != null && this.rememberPageSize == 1;
    }

    //    public Byte getRememberPageSize() {
    //        return rememberPageSize;
    //    }

    /**
     * 设置是否记住页面大小。
     *
     * @param rememberPageSize 是否记住页面大小
     */
    public void setRememberPageSize(Byte rememberPageSize) {
        this.rememberPageSize = rememberPageSize;
    }

    /**
     * 获取是否记住页面拉伸。
     *
     * @return 是否记住页面拉伸
     */
    @Deprecated
    public Byte getRememberPageResize() {
        return rememberPageResize;
    }

    /**
     * 设置是否记住页面拉伸。
     *
     * @param rememberPageResize 是否记住页面拉伸
     */
    @Deprecated
    public void setRememberPageResize(Byte rememberPageResize) {
        this.rememberPageResize = rememberPageResize;
    }

    /**
     * 获取是否记住页面位置。
     *
     * @return 是否记住页面位置
     */
    public Byte getRememberPageLocation() {
        return rememberPageLocation;
    }

    /**
     * 设置是否记住页面位置。
     *
     * @param rememberPageLocation 是否记住页面位置
     */
    public void setRememberPageLocation(Byte rememberPageLocation) {
        this.rememberPageLocation = rememberPageLocation;
    }

    /**
     * 设置页面宽。
     *
     * @param pageWidth 页面宽
     */
    public void setPageWidth(Double pageWidth) {
        this.pageWidth = pageWidth;
    }

    /**
     * 设置页面高。
     *
     * @param pageHeight 页面高
     */
    public void setPageHeight(Double pageHeight) {
        this.pageHeight = pageHeight;
    }

    /**
     * 获取屏幕 x 坐标。
     *
     * @return 屏幕 x 坐标
     */
    public Double getPageScreenX() {
        return pageScreenX;
    }

    /**
     * 设置屏幕 x 坐标。
     *
     * @param pageScreenX 屏幕 x 坐标
     */
    public void setPageScreenX(Double pageScreenX) {
        this.pageScreenX = pageScreenX;
    }

    /**
     * 获取屏幕 y 坐标。
     *
     * @return 屏幕 y 坐标
     */
    public Double getPageScreenY() {
        return pageScreenY;
    }

    /**
     * 设置屏幕 y 坐标。
     *
     * @param pageScreenY 屏幕 y 坐标
     */
    public void setPageScreenY(Double pageScreenY) {
        this.pageScreenY = pageScreenY;
    }

    //    public Boolean getPageMaximized() {
    //        return pageMaximized;
    //    }

    /**
     * 设置是否最大化。
     *
     * @param pageMaximized 是否最大化
     */
    public void setPageMaximized(Boolean pageMaximized) {
        this.pageMaximized = pageMaximized;
    }

    /**
     * 设置主页左侧宽。
     *
     * @param pageLeftWidth 主页左侧宽
     */
    @Deprecated
    public void setPageLeftWidth(Float pageLeftWidth) {
        this.pageLeftWidth = pageLeftWidth;
    }

    @Override
    public void copy(Object obj) {
        if (obj instanceof AppSetting t1) {
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
            //            this.queryFontSize = t1.queryFontSize;
            //            this.queryFontFamily = t1.queryFontFamily;
            //            this.queryFontWeight = t1.queryFontWeight;
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

    /**
     * 获取字体大小
     *
     * @return 字体大小
     */
    public byte getFontSize() {
        return this.fontSize == null ? defaultFontSize() : this.fontSize;
    }

    /**
     * 获取字体名称
     *
     * @return 字体名称
     */
    public String getFontFamily() {
        return this.fontFamily == null ? defaultFontFamily() : this.fontFamily;
    }

    /**
     * 获取字体粗细
     *
     * @return 字体粗细
     */
    public int getFontWeight() {
        return this.fontWeight == null ? defaultFontWeight() : this.fontWeight;
    }

    /**
     * 获取默认字体大小
     *
     * @return 默认字体大小
     */
    public static byte defaultFontSize() {
        return 12;
    }

    /**
     * 获取默认字体名称
     *
     * @return 默认字体名称
     */
    public static String defaultFontFamily() {
        return Font.getDefault().getFamily();
    }

    /**
     * 获取默认字体粗细
     *
     * @return 默认字体粗细
     */
    public static int defaultFontWeight() {
        return FontWeight.NORMAL.getWeight();
    }

    /**
     * 获取编辑器字体大小
     *
     * @return 编辑器字体大小
     */
    public Byte getEditorFontSize() {
        return this.editorFontSize == null ? defaultEditorFontSize() : this.editorFontSize;
    }

    /**
     * 获取编辑器字体名称
     *
     * @return 编辑器字体名称
     */
    public String getEditorFontFamily() {
        return StringUtil.isBlank(this.editorFontFamily) ? defaultEditorFontFamily() : this.editorFontFamily;
    }

    /**
     * 获取编辑器字体粗细
     *
     * @return 编辑器字体粗细
     */
    public int getEditorFontWeight() {
        return this.editorFontWeight == null ? defaultEditorFontWeight() : this.editorFontWeight;
    }

    /**
     * 获取默认编辑器字体大小
     *
     * @return 默认编辑器字体大小
     */
    public static byte defaultEditorFontSize() {
        return 14;
    }

    /**
     * 获取默认编辑器字体名称
     *
     * @return 默认编辑器字体名称
     */
    public static String defaultEditorFontFamily() {
        return Font.getDefault().getFamily();
    }

    /**
     * 获取默认编辑器字体粗细
     *
     * @return 默认编辑器字体粗细
     */
    public static int defaultEditorFontWeight() {
        return FontWeight.NORMAL.getWeight();
    }

    /**
     * 获取终端字体大小
     *
     * @return 终端字体大小
     */
    public Byte getTerminalFontSize() {
        return this.terminalFontSize == null ? defaultTerminalFontSize() : this.terminalFontSize;
    }

    /**
     * 获取终端字体粗细
     *
     * @return 终端字体粗细
     */
    public int getTerminalFontWeight() {
        return this.terminalFontWeight == null ? defaultTerminalFontWeight() : this.terminalFontWeight;
    }

    /**
     * 获取终端字体名称
     *
     * @return 终端字体名称
     */
    public String getTerminalFontFamily() {
        return StringUtil.isBlank(this.terminalFontFamily) ? defaultTerminalFontFamily() : this.terminalFontFamily;
    }

    /**
     * 获取默认终端字体大小
     *
     * @return 默认终端字体大小
     */
    public static byte defaultTerminalFontSize() {
        return 14;
    }

    /**
     * 获取默认终端字体名称
     *
     * @return 默认终端字体名称
     */
    public static String defaultTerminalFontFamily() {
        if (OSUtil.isWindows()) {
            return "Consolas";
        }
        if (OSUtil.isMacOS()) {
            return "Menlo";
        }
        return "Monospace";
    }

    /**
     * 获取默认终端字体粗细
     *
     * @return 默认终端字体粗细
     */
    public static int defaultTerminalFontWeight() {
        return FontWeight.NORMAL.getWeight();
    }

    //    public Byte getQueryFontSize() {
    //        return this.queryFontSize == null ? defaultQueryFontSize() : this.queryFontSize;
    //    }
    //
    //    public int getQueryFontWeight() {
    //        return this.queryFontWeight == null ? defaultQueryFontWeight() : this.queryFontWeight;
    //    }
    //
    //    public String getQueryFontFamily() {
    //        return StringUtil.isBlank(this.queryFontFamily) ? defaultQueryFontFamily() : this.queryFontFamily;
    //    }
    //
    //    public static byte defaultQueryFontSize() {
    //        return 15;
    //    }
    //
    //    public static String defaultQueryFontFamily() {
    //        return "System";
    //    }
    //
    //    public static int defaultQueryFontWeight() {
    //        return FontWeight.NORMAL.getWeight();
    //    }
}
