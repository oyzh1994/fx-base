package cn.oyzh.fx.plus.window;

import atlantafx.base.controls.Popover;
import javafx.stage.PopupWindow;
// import org.springframework.context.annotation.Lazy;
// import org.springframework.context.annotation.Scope;
// import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 弹窗属性
 *
 * @author oyzh
 * @since 2024/07/12
 */
// @Lazy
// @Component
// @Scope("prototype")
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PopupAttribute {

    /**
     * fxml地址
     *
     * @return fxml地址
     */
    String value();

    /**
     * css文件地址
     *
     * @return css文件地址
     */
    String[] cssUrls() default {};

    /**
     * 箭头位置
     * @return 箭头位置
     */
    Popover.ArrowLocation arrowLocation() default Popover.ArrowLocation.LEFT_TOP;

    /**
     * 弹窗位置
     * @return 弹窗位置
     */
    PopupWindow.AnchorLocation anchorLocation() default PopupWindow.AnchorLocation.WINDOW_TOP_LEFT;
}
