package cn.oyzh.fx.plus.controller;

import cn.oyzh.fx.plus.i18n.I18nAdapter;

public abstract class Controller1 implements I18nAdapter {

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

    /**
     * 获取属性
     *
     * @param key key
     * @param <T> 泛型
     * @return 值
     */
    protected <T> T getWindowProp(String key) {
        return null;
    }

}
