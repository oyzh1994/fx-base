package cn.oyzh.fx.plus.controls.table;

import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * @author oyzh
 * @since 2022/1/18
 */
public class FlexTableColumn<S, T> extends TableColumn<S, T> implements NodeAdapter, FlexAdapter, ThemeAdapter {

    {
        NodeManager.init(this);
    }

    @Override
    public String getFlexWidth() {
        return FlexAdapter.super.flexWidth();
    }

    @Override
    public void setFlexWidth(String flexWidth) {
        FlexAdapter.super.flexWidth(flexWidth);
    }

    public String getFlexHeight() {
        return FlexAdapter.super.flexHeight();
    }

    @Override
    public void setFlexHeight(String flexHeight) {
        FlexAdapter.super.flexHeight(flexHeight);
    }

    @Override
    public String getFlexX() {
        return FlexAdapter.super.flexX();
    }

    @Override
    public void setFlexX(String flexX) {
        FlexAdapter.super.flexX(flexX);
    }

    @Override
    public String getFlexY() {
        return FlexAdapter.super.flexY();
    }

    @Override
    public void setFlexY(String flexY) {
        FlexAdapter.super.flexY(flexY);
    }

    @Override
    public double getRealWidth() {
        return FlexAdapter.super.realWidth();
    }

    @Override
    public void setRealWidth(double width) {
        FlexAdapter.super.realWidth(width);
    }

    @Override
    public double getRealHeight() {
        return FlexAdapter.super.realHeight();
    }

    @Override
    public void setRealHeight(double height) {
        FlexAdapter.super.realHeight(height);
    }

    @Override
    public void setStateManager(StateManager manager) {
        FlexAdapter.super.stateManager(manager);
    }

    @Override
    public StateManager getStateManager() {
        return FlexAdapter.super.stateManager();
    }

    /**
     * 设置列处理器
     *
     * @param cell 列处理器
     */
    public void setCell(TableCell<S, T> cell) {
        this.setCellFactory((c) -> cell);
        this.getTableView().getProperties().put("cell", cell);
    }

    /**
     * 设置列处理器
     */
    public TableCell<S, T> getCell() {
        return (TableCell<S, T>) this.getTableView().getProperties().get("cell");
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

    @Override
    public void initNode() {

    }

    public void setValueName(String valueName) {
        this.setCellValueFactory(new PropertyValueFactory<>(valueName));
        this.setProp("_valueName", valueName);
    }

    public String getValueName() {
        return this.getProp("_valueName");
    }
}
