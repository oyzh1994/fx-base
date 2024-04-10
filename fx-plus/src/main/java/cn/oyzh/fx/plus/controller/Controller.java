package cn.oyzh.fx.plus.controller;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.plus.event.EventListener;
import cn.oyzh.fx.plus.i18n.BaseResourceBundle;
import cn.oyzh.fx.plus.i18n.I18nAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.stage.StageListener;
import cn.oyzh.fx.plus.stage.StageWrapper;
import javafx.fxml.Initializable;
import javafx.stage.WindowEvent;
import lombok.Getter;
import lombok.NonNull;

import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * 组件控制器
 *
 * @author oyzh
 * @since 2023/10/12
 */
public class Controller implements StageListener, EventListener, I18nAdapter, Initializable {

    /**
     * 舞台
     */
    @Getter
    protected StageWrapper stage;

    /**
     * 资源
     */
    @Getter
    protected ResourceBundle resources;

    /**
     * 设置舞台
     *
     * @param stage 舞台
     */
    protected void setStage(@NonNull StageWrapper stage) {
        this.stage = stage;
    }

    @Override
    public void onStageInitialize(StageWrapper stage) {
        // 设置页面
        this.setStage(stage);
        // 处理标题
        if (StrUtil.isEmpty(this.stage.getTitleExt())) {
            String title = this.i18nString("title");
            if (title != null) {
                this.stage.setTitleExt(title);
            }
        }
        NodeManager.init(this);
    }

    @Override
    public void onStageShowing(WindowEvent event) {
        EventListener.super.register();
    }

    @Override
    public void onStageShown(WindowEvent event) {
        this.bindListeners();
    }

    @Override
    public void onStageCloseRequest(WindowEvent event) {
    }

    @Override
    public void onStageHiding(WindowEvent event) {
    }

    @Override
    public void onStageHidden(WindowEvent event) {
        EventListener.super.unregister();
    }

    @Override
    public void onSystemExit() {
    }

    /**
     * 绑定监听器相关业务
     */
    protected void bindListeners() {

    }

    /**
     * 关闭舞台
     */
    protected void closeStage() {
        if (this.stage != null) {
            this.stage.disappear();
        }
    }

    /**
     * 获取属性
     *
     * @param key key
     * @param <T> 泛型
     * @return 值
     */
    protected <T> T getStageProp(String key) {
        return this.stage == null ? null : this.stage.getProp(key);
    }

    @Override
    public String i18nString(String key) {
        try {
            if (this.i18nId() == null) {
                return this.resources.getString(key);
            }
            return this.resources.getString(this.i18nId() + "." + key);
        } catch (MissingResourceException ex) {
            ex.printStackTrace();
        }
        return BaseResourceBundle.getBaseString(key);
    }

    @Override
    public void changeLocale(Locale locale) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
    }
}
