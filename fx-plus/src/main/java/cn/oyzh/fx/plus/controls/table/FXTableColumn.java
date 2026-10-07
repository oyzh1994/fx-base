package cn.oyzh.fx.plus.controls.table;

import cn.oyzh.common.object.Destroyable;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.node.NodeDestroyUtil;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.util.PropertiesUtil;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.NestedTableColumnHeader;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.util.Callback;

import java.util.function.Consumer;


/**
 * 表格列
 *
 * @author oyzh
 * @since 2022-01-18
 */
public class FXTableColumn<S, T> extends TableColumn<S, T> implements FlexAdapter, ThemeAdapter, Destroyable {

    {
        NodeManager.init(this);
    }

    /**
     * 构造表列对象。
     */
    public FXTableColumn() {
        super();
    }

    /**
     * 构造表列对象。
     *
     * @param text 文本
     */
    public FXTableColumn(String text) {
        super(text);
    }

    /**
     * 获取单元格工厂
     *
     * @return 单元格工厂
     */
    protected Callback<TableColumn<S, T>, TableCell<S, T>> cellFactory() {
        return param -> new FXTableCell<>();
    }

    /**
     * 设置列处理器
     *
     * @param cell 列处理器
     */
    public void setCell(TableCell<S, T> cell) {
        this.setCellFactory(this.cellFactory());
        PropertiesUtil.set(this.getTableView(), "cell", cell);
    }

    /**
     * 获取列处理器
     *
     * @return 列处理器
     */
    public TableCell<S, T> getCell() {
        return (TableCell<S, T>) PropertiesUtil.get(this.getTableView(), "cell");
    }

    /**
     * 设置行高
     *
     * @param lineHeight 行高
     */
    public void setLineHeight(double lineHeight) {
        FXTableCell<S, T> cell = (FXTableCell<S, T>) this.getCell();
        if (cell == null) {
            cell = new FXTableCell<>();
            this.setCell(cell);
        }
        cell.setLineHeight(lineHeight);
    }

    /**
     * 获取行高
     *
     * @return 行高
     */
    public double getLineHeight() {
        FXTableCell<S, T> cell = (FXTableCell<S, T>) this.getCell();
        if (cell != null) {
            return cell.getLineHeight();
        }
        return Double.NaN;
    }

    /**
     * 设置对齐方式
     *
     * @param pos 对齐方式
     */
    public void setAlignment(Pos pos) {
        FXTableCell<S, T> cell = (FXTableCell<S, T>) this.getCell();
        if (cell == null) {
            cell = new FXTableCell<>();
            this.setCell(cell);
        }
        cell.setAlignment(pos);
    }

    /**
     * 获取对齐方式
     *
     * @return 对齐方式
     */
    public Pos getAlignment() {
        FXTableCell<S, T> cell = (FXTableCell<S, T>) this.getCell();
        if (cell != null) {
            return cell.getAlignment();
        }
        return null;
    }

    /**
     * 值名称属性键
     */
    public static final String VALUE_NAME_PROP = "value_name";

    /**
     * 设置值名称
     *
     * @param valueName 值名称
     */
    public void setValueName(String valueName) {
        try {
            this.setCellValueFactory(new PropertyValueFactory<>(valueName));
            this.setProp(VALUE_NAME_PROP, valueName);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 获取值名称
     *
     * @return 值名称
     */
    public String getValueName() {
        return this.getProp(VALUE_NAME_PROP);
    }

    /**
     * 设置文本
     *
     * @param text 文本
     */
    public void text(String text) {
        FXUtil.runWait(() -> super.setText(text));
    }

    @Override
    public void setRealWidth(double width) {
        if (this.isResizable() && !Double.isNaN(width) && width >= 0) {
            this.setPrefWidth(width);
        }
    }

    /**
     * 仅显示图标
     */
    public void showGraphicOnly() {
        // 处理图标
        Consumer<TableColumnHeader> func1 = columnHeader -> {
            Label label = (Label) columnHeader.lookup(".label");
            if (label != null) {
                label.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }
        };
        // 处理头
        Consumer<TableView<?>> func2 = tableView -> {
            if (tableView == null) {
                return;
            }
            NestedTableColumnHeader header = TableViewUtil.getHeaderColumn(tableView);
            if (header == null) {
                return;
            }
            for (TableColumnHeader columnHeader : header.getColumnHeaders()) {
                if (columnHeader.getTableColumn() == this) {
                    func1.accept(columnHeader);
                    break;
                }
            }
            header.getColumnHeaders().addListener((ListChangeListener<TableColumnHeader>) c -> {
                if (c.next()) {
                    for (TableColumnHeader columnHeader : c.getAddedSubList()) {
                        if (columnHeader.getTableColumn() == this) {
                            func1.accept(columnHeader);
                            break;
                        }
                    }
                }
            });
        };
        if (this.getTableView() != null) {
            func2.accept(this.getTableView());
        } else {
            this.tableViewProperty().subscribe(tableView -> {
                if (tableView != null) {
                    func2.accept(tableView);
                }
            });
        }
    }

    /**
     * 仅显示图标，延迟处理
     */
    public void showGraphicOnlyLater() {
        this.showGraphicOnly();
        FXUtil.runPulse(this::showGraphicOnly);
        FXUtil.runLater(this::showGraphicOnly, 200);
    }

    @Override
    public void destroy() {
        NodeDestroyUtil.destroyObject(this);
    }
}
