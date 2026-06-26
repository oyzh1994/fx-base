package cn.oyzh.fx.plus.controller;

import cn.oyzh.common.object.Destroyable;
import cn.oyzh.fx.plus.i18n.I18nAdapter;
import cn.oyzh.fx.plus.node.NodeDestroyUtil;
import cn.oyzh.fx.plus.window.WindowListener;
import javafx.stage.WindowEvent;

import java.util.Locale;

/**
 * 组件控制器
 *
 * @author oyzh
 * @since 2024/07/12
 */
public abstract class Controller implements Destroyable, I18nAdapter, WindowListener {

    /**
     * 绑定监听器相关业务
     */
    protected void bindListeners() {

    }

    /**
     * 关闭舞台
     */
    protected void closeWindow() {
    }

//    /**
//     * 获取属性
//     *
//     * @param key key
//     * @param <T> 泛型
//     * @return 值
//     */
//    protected <T> T getWindowProp(String key) {
//        return null;
//    }

    @Override
    public void changeLocale(Locale locale) {

    }

    @Override
    public void onWindowShowing(WindowEvent event) {
        this.bindListeners();
    }

    @Override
    public void onWindowHiding(WindowEvent event) {
    }

    @Override
    public void onWindowHidden(WindowEvent event) {
    }

    @Override
    public void destroy() {
        NodeDestroyUtil.destroyObject(this);
    }
}
