package cn.oyzh.fx.plus.spring;

import cn.oyzh.fx.plus.ext.ApplicationExt;
import javafx.application.Application;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 支持Spring的fx程序入口
 *
 * @author oyzh
 * @since 2021/8/19
 */
@Slf4j
public abstract class SpringApplication extends ApplicationExt implements CommandLineRunner, DisposableBean {

    /**
     * 从spring启动
     *
     * @param appClass app类
     * @param args     参数
     */
    public static void launchSpring(@NonNull Class<? extends Application> appClass, String... args) {
        try {
            SpringApplicationBuilder builder = new SpringApplicationBuilder(appClass);
            builder.web(WebApplicationType.NONE).headless(false).run(args);
            launch(appClass, args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
