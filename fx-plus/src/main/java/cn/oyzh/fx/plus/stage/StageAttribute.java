package cn.oyzh.fx.plus.stage;

import javafx.stage.Modality;
import javafx.stage.StageStyle;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 窗口注解
 *
 * @author oyzh
 * @since 2022/1/27
 */
@Lazy
@Component
@Scope("prototype")
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
    String title() default "";

    /**
     * css文件地址
     *
     * @return css文件地址
     */
    String[] cssUrls() default {};

    /**
     * 图标地址
     *
     * @return 图标地址
     */
    String[] iconUrls() default {};

    /**
     * 窗口模态
     *
     * @return 窗口模态
     */
    Modality modality() default Modality.NONE;

    /**
     * 是否可最大化
     *
     * @return 是否可最大化
     */
    boolean maximized() default false;

    /**
     * 是否可拉伸
     *
     * @return 是否可拉伸
     */
    boolean resizeable() default true;

    /**
     * 装饰样式
     *
     * @return 装饰样式
     */
    StageStyle stageStyle() default StageStyle.DECORATED;

    // /**
    //  * 是否使用主要的stage
    //  *
    //  * @return 结果
    //  */
    // boolean usePrimary() default false;

}
