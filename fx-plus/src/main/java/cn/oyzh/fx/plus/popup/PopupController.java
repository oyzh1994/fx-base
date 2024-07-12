package cn.oyzh.fx.plus.popup;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.plus.event.EventListener;
import cn.oyzh.fx.plus.i18n.I18nAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.stage.StageListener;
import cn.oyzh.fx.plus.stage.StageWrapper;
import javafx.stage.PopupWindow;
import javafx.stage.WindowEvent;
import lombok.Getter;
import lombok.NonNull;

import java.util.Locale;

/**
 * 组件控制器
 *
 * @author oyzh
 * @since 2023/10/12
 */
public class PopupController implements PopupListener, I18nAdapter {

    /**
     * 舞台
     */
    @Getter
    protected PopupWrapper popup;

    /**
     * 设置舞台
     *
     * @param popup 舞台
     */
    protected void setPopup(@NonNull PopupWrapper popup) {
        this.popup = popup;
    }

    @Override
    public void onPopupInitialize(PopupWrapper popup) {
        // 设置页面
        this.setPopup(popup);
        NodeManager.init(this);
    }

    @Override
    public void onPopupShowing(WindowEvent event) {
        this.bindListeners();
    }

    @Override
    public void onPopupHiding(WindowEvent event) {
    }

    @Override
    public void onPopupHidden(WindowEvent event) {
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
        if (this.popup != null) {
            this.popup.disappear();
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
        return this.popup == null ? null : this.popup.getProp(key);
    }

    @Override
    public void changeLocale(Locale locale) {

    }

    public String getViewTitle() {
        return null;
    }
}
