package cn.oyzh.fx.plus.controls.table;

import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.fx.plus.util.IconUtil;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.function.BiFunction;

/**
 * 图标表单列
 *
 * @author oyzh
 * @since 2022/12/21
 */
public class IconTableCell<S, T> extends FXTableCell<S, T> {

    public IconTableCell() {

    }

    public IconTableCell(BiFunction<S, T, Object> iconFunc) {
        this.iconFunc = iconFunc;
    }

    /**
     * 图片视图
     */
    private ImageView imageView;

    /**
     * 图标函数
     */
    private BiFunction<S, T, Object> iconFunc;

    public BiFunction<S, T, Object> getIconFunc() {
        return iconFunc;
    }

    public void setIconFunc(BiFunction<S, T, Object> iconFunc) {
        this.iconFunc = iconFunc;
    }

    /**
     * 获取图标
     *
     * @param data 数据
     * @param item 内容
     * @return 结果
     */
    private Object getIcon(S data, T item) {
        if (this.iconFunc != null) {
            return this.iconFunc.apply(data, item);
        }
        String fName = item.toString();
        String extension = FileNameUtil.extName(fName);
        Image icon = OSUtil.isWindows() ? IconUtil.getSystemIcon(extension) : null;
        if (icon == null) {
            return IconUtil.getSVGIcon(extension);
        }
        return icon;
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            this.setText(null);
            this.setGraphic(null);
            if (this.imageView != null) {
                this.imageView.setImage(null);
            }
        } else {
            String fName = item.toString();
            this.setText(fName);
            // 获取图标
            Object icon = this.getIcon(this.getTableItem(), item);
            // 图片类型
            if (icon instanceof Image image) {
                if (this.imageView == null) {
                    this.imageView = new ImageView();
                }
                this.imageView.setImage(image);
                this.setGraphic(this.imageView);
            } else if (icon instanceof Node node) {// 节点类型
                this.setGraphic(node);
            }
        }
    }
}
