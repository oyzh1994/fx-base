package cn.oyzh.fx.plus.util;

import cn.hutool.core.collection.CollUtil;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import lombok.experimental.UtilityClass;

/**
 * 表格工具类
 *
 * @author oyzh
 * @since 2023/8/11
 */
@UtilityClass
public class TableViewUtil {

    /**
     * 双击时，复制列数据到粘贴板
     *
     * @param tableView 表格组件
     */
    public static void copyCellOnDoubleClicked(TableView<?> tableView) {
        // 双击时，复制数据到粘贴板
        tableView.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if (cn.oyzh.fx.plus.util.MouseUtil.isDubboClick(event) && MouseUtil.isPrimaryButton(event)) {
                if (FXUtil.clipboardCopy((String) getSelectCellData(tableView))) {
                    MessageBox.okToast("已复制数据到粘贴板");
                }
            }
        });
    }

    /**
     * 获取当前选中列的数据
     *
     * @param tableView 表格组件
     * @return 列数据
     */
    public Object getSelectCellData(TableView<?> tableView) {
        ObservableList<TablePosition> positions = tableView.getSelectionModel().getSelectedCells();
        if (CollUtil.isNotEmpty(positions)) {
            TablePosition<?, ?> position = positions.get(0);
            TableColumn<?, ?> column = position.getTableColumn();
            return column.getCellData(position.getRow());
        }
        return null;
    }
}
