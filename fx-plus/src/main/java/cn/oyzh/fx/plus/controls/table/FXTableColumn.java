package cn.oyzh.fx.plus.controls.table;

import cn.oyzh.common.object.Destroyable;
import cn.oyzh.fx.plus.adapter.DestroyAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.util.PropertiesUtil;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;


/**
 * @author oyzh
 * @since 2022/1/18
 */
public class FXTableColumn<S, T> extends TableColumn<S, T> implements FlexAdapter, ThemeAdapter, DestroyAdapter {

    {
        NodeManager.init(this);
    }

    /**
     * 设置列处理器
     *
     * @param cell 列处理器
     */
    public void setCell(TableCell<S, T> cell) {
        Callback<TableColumn<S,T>, TableCell<S,T>> cellFactory = param -> new FXTableCell<>();
        this.setCellFactory(cellFactory);
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

    public void text(String text) {
        FXUtil.runWait(() -> super.setText(text));
    }

    @Override
    public void setRealWidth(double width) {
        if (this.isResizable()) {
            if (!Double.isNaN(width) && width > 0) {
                this.setPrefWidth(width);
                this.setMinWidth(width);
            }
        } else {
            FlexAdapter.super.setRealWidth(width);
        }
    }

    // @Override
    // public void initNode() {
    //     // 设置默认cell
    //     this.setCell(new FXTableCell<>());
    //     FlexAdapter.super.initNode();
    // }
}
