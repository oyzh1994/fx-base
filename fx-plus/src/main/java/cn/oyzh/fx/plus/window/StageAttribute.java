package cn.oyzh.fx.plus.window;

import javafx.stage.Modality;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 舞台属性
 *
 * @author oyzh
 * @since 2023/10/12
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface StageAttribute {

    /**
     * fxml地址
     *
     * @return fxml地址
     */
    String value();

    /**
     * 页面标题
     *
     * @return 页面标题
     */
    @Deprecated
    String title() default "";

    /**
     * css文件地址
     *
     * @return css文件地址
     */
    String[] cssUrls() default {};

    // /**
    //  * 图标地址
    //  *
    //  * @return 图标地址
    //  */
    // @Deprecated
    // String[] iconUrls() default {};

    /**
     * 图标地址
     *
     * @return 图标地址
     */
    String iconUrl() default "";

    /**
     * 窗口模态
     *
     * @return 窗口模态
     */
    Modality modality() default Modality.NONE;

    // /**
    //  * 是否可最大化
    //  *
    //  * @return 是否可最大化
    //  */
    // @Deprecated
    // boolean maximized() default false;

    // /**
    //  * 是否可拉伸
    //  *
    //  * @return 是否可拉伸
    //  */
    // @Deprecated
    // boolean resizeable() default true;

    /**
     * 是否可最大化
     *
     * @return 是否可最大化
     */
    boolean maximumAble() default true;

    /**
     * 是否可拉伸
     *
     * @return 是否可拉伸
     */
    boolean resizable() default true;

    /**
     * 是否可全屏
     *
     * @return 是否可全屏
     */
    boolean fullScreenAble() default false;

    /**
     * 是否可置顶
     *
     * @return 是否可置顶
     */
    boolean alwaysOnTopAble() default false;

    /**
     * 装饰样式
     *
     * @return 装饰样式
     */
    FXStageStyle stageStyle() default FXStageStyle.CUSTOM;

    /**
     * 是否使用主要的stage
     *
     * @return 结果
     */
    boolean usePrimary() default false;
}
