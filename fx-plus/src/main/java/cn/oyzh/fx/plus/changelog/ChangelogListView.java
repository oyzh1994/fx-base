package cn.oyzh.fx.plus.changelog;

import cn.hutool.core.collection.CollUtil;
import cn.oyzh.fx.plus.controls.text.FlexLabel;
import cn.oyzh.fx.plus.controls.view.FlexListView;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;

import java.util.List;

/**
 * 更新日志组件
 *
 * @author oyzh
 * @since 2024/4/7
 */
public class ChangelogListView extends FlexListView<Node> {

    {
        this.setCursor(Cursor.DEFAULT);
    }

    /**
     * 初始化
     *
     * @param list 更新日志
     */
    public void init(List<Changelog> list) {
        if (CollectionUtil.isEmpty(list)) {
            this.clearItems();
        } else {
            for (Changelog changelog : list) {
                this.initChangelog(changelog);
            }
        }
    }

    /**
     * 初始化更新日志
     *
     * @param changelog 更新日志
     */
    private void initChangelog(Changelog changelog) {
        // 标题
        FlexLabel title = this.initTitle(changelog.getVersion() + "(" + changelog.getDate() + ")");
        title.addClass("default");
        this.addItem(title);

        // 特性
        if (CollectionUtil.isNotEmpty(changelog.getFeatures())) {
            FlexLabel label = this.initItem("全新功能");
            label.addClass("success");
            this.addItem(label);
            for (String feature : changelog.getFeatures()) {
                FlexLabel label1 = this.initItem(feature);
                label1.addClass("success");
                this.addItem(label1);
            }
        }

        // 优化
        if (CollectionUtil.isNotEmpty(changelog.getOptimize())) {
            FlexLabel label = this.initItem("优化内容");
            label.addClass("accent");
            this.addItem(label);
            for (String optimize : changelog.getOptimize()) {
                FlexLabel label1 = this.initItem(optimize);
                label1.addClass("accent");
                this.addItem(label1);
            }
        }

        // 问题处理
        if (CollectionUtil.isNotEmpty(changelog.getBugfix())) {
            FlexLabel label = this.initItem("问题修复");
            label.addClass("danger");
            this.addItem(label);
            for (String bugfix : changelog.getBugfix()) {
                FlexLabel label1 = this.initItem(bugfix);
                label1.addClass("danger");
                this.addItem(label1);
            }
        }
    }

    /**
     * 初始化标题
     *
     * @param text 文本内容
     * @return 组件
     */
    private FlexLabel initTitle(String text) {
        FlexLabel label = new FlexLabel(text);
        label.setFontSize(25);
        label.setEnableFont(false);
        label.setCursor(Cursor.DEFAULT);
        label.setPadding(new Insets(10, 5, 5, 5));
        return label;
    }

    /**
     * 初始化内容
     *
     * @param text 文本内容
     * @return 组件
     */
    private FlexLabel initItem(String text) {
        FlexLabel label = new FlexLabel(text);
        label.setFontSize(12);
        label.setEnableFont(false);
        label.setCursor(Cursor.DEFAULT);
        label.setPadding(new Insets(3, 5, 0, 10));
        return label;
    }
}
