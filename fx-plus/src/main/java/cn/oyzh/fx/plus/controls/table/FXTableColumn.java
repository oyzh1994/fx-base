package cn.oyzh.fx.plus.controls.table;

import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.util.PropertiesUtil;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * @author oyzh
 * @since 2022/1/18
 */
public class FXTableColumn<S, T> extends TableColumn<S, T> implements NodeAdapter, FlexAdapter, ThemeAdapter {

    {
        NodeManager.init(this);
    }

    /**
     * 设置列处理器
     *
     * @param cell 列处理器
     */
    public void setCell(TableCell<S, T> cell) {
        this.setCellFactory((c) -> cell);
        PropertiesUtil.set(this.getTableView(), "cell", cell);
    }

    /**
     * 设置列处理器
     */
    public TableCell<S, T> getCell() {
        return (TableCell<S, T>) PropertiesUtil.get(this.getTableView(), "cell");
    }

    public void setLineHeight(double lineHeight) {
        FXTableCell<S, T> cell = (FXTableCell<S, T>) this.getCell();
        if (cell == null) {
            cell = new FXTableCell<>();
            this.setCell(cell);
        }
        cell.setLineHeight(lineHeight);
    }

    public double getLineHeight() {
        FXTableCell<S, T> cell = (FXTableCell<S, T>) this.getCell();
        if (cell != null) {
            return cell.getLineHeight();
        }
        return Double.NaN;
    }

    public void setAlignment(Pos pos) {
        FXTableCell<S, T> cell = (FXTableCell<S, T>) this.getCell();
        if (cell == null) {
            cell = new FXTableCell<>();
            this.setCell(cell);
        }
        cell.setAlignment(pos);
    }

    public Pos getAlignment() {
        FXTableCell<S, T> cell = (FXTableCell<S, T>) this.getCell();
        if (cell != null) {
            return cell.getAlignment();
        }
        return null;
    }

    public void setValueName(String valueName) {
        try {
            this.setCellValueFactory(new PropertyValueFactory<>(valueName));
            this.setProp("_valueName", valueName);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getValueName() {
        return this.getProp("_valueName");
    }

    public void setTextExt(String text) {
        FXUtil.runWait(() -> super.setText(text));
    }
}
