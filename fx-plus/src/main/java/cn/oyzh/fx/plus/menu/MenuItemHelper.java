package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.AddSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.ClearSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.CloseSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.CollapseAllSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.CopySVGGlyph;
import cn.oyzh.fx.plus.controls.svg.DeleteSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.DesignSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.EditSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.ExpandAllSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.ExportSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.FileSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.FunctionSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.ImportSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.InfoSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.LoadAllSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.MoveSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.OpenSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.PlaySVGGlyph;
import cn.oyzh.fx.plus.controls.svg.ProcedureSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.RefreshSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.RenameSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.RepeatSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.StopSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.TerminalSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.TransportSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.TruncateSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.UnLockSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import lombok.experimental.UtilityClass;

/**
 * 菜单工具类
 *
 * @author oyzh
 * @since 2024/6/28
 */
@UtilityClass
public class MenuItemHelper {

    public static FXMenuItem openView(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.openView(), new OpenSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem viewInfo(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.viewInfo(), new InfoSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem deleteView(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.deleteView(), new DeleteSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem addView(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.addView(), new AddSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem designView(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.designView(), new DesignSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem refreshData(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.refreshData(), new RefreshSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem reloadData(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.reloadData(), new RefreshSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem addTable(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.addTable(), new AddSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem openTerminal(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.openTerminal(), new TerminalSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem openProcedure(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.openProcedure(), new ProcedureSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem addProcedure(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.addProcedure(), new AddSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem procedureInfo(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.procedureInfo(), new InfoSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem addFunction(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.addFunction(), new AddSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem openFunction(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.openFunction(), new FunctionSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem moveKey(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.moveKey(), new MoveSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem keyFilter(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.keyFilter(), new FileSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem exportNode(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.exportNode(), new ExportSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem exportKey(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.exportKey(), new ExportSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem exportData(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.exportData(), new ExportSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem exportConnect(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.exportConnect(), new ExportSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem importConnect(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.importConnect(), new ImportSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem importData(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.importData(), new ImportSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem renameConnect(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.renameConnect(), new RenameSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem renameGroup(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.renameGroup(), new RenameSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem renameNode(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.renameNode(), new RenameSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem renameKey(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.renameKey(), new RenameSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem editConnect(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.editConnect(), new RenameSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem editGroup(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.editGroup(), new EditSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem editRow(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.editRow(), new EditSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem deleteKey(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.deleteKey(), new DeleteSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem deleteData(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.deleteData(), new DeleteSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem deleteGroup(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.deleteGroup(), new DeleteSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem deleteNode(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.deleteNode(), new DeleteSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem deleteConnect(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.deleteConnect(), new DeleteSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem closeConnect(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.closeConnect(), new CloseSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem addGroup(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.addGroup(), new AddSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem addConnect(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.addConnect(), new AddSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem addKey(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.addKey(), new AddSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem addNode(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.addNode(), new AddSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem transportData(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.transportData(), new TransportSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem transportKey(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.transportKey(), new TransportSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem transportNode(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.transportNode(), new TransportSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem repeatConnect(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.repeatConnect(), new RepeatSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem reload(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.reload(), new RefreshSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem serverInfo(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.serverInfo(), new InfoSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem startConnect(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.startConnect(), new PlaySVGGlyph(iconSize), I18nResourceBundle.i18nString("base.startTip1"), action);
    }

    public static FXMenuItem clearData(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.clearData(), new ClearSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem copyKey(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.copyKey(), new CopySVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem authNode(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.authNode(), new UnLockSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem unload(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.unload(), new StopSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem loadAll(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.loadAll(), new LoadAllSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem expandAll(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.expandAll(), new ExpandAllSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem collapseAll(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.collapseAll(), new CollapseAllSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem deleteFunction(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.deleteFunction(), new DeleteSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem deleteProcedure(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.deleteProcedure(), new DeleteSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem openTable(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.openTable(), new OpenSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem editTable(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.editTable(), new EditSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem renameTable(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.renameTable(), new RenameSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem clearTable(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.clearTable(), new ClearSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem truncateTable(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.truncateTable(), new TruncateSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem deleteTable(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.deleteTable(), new DeleteSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem tableInfo(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.tableInfo(), new InfoSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem copy(Runnable action) {
        return FXMenuItem.newItem(I18nHelper.copy(), action);
    }

    public static FXMenuItem paste(Runnable action) {
        return FXMenuItem.newItem(I18nHelper.paste(), action);
    }

    public static FXMenuItem deleteRecord(Runnable action) {
        return FXMenuItem.newItem(I18nHelper.deleteRecord(), action);
    }

    public static FXMenuItem setToNull(Runnable action) {
        return FXMenuItem.newItem(I18nHelper.setToNull(), action);
    }

    public static FXMenuItem setToEmptyString(Runnable action) {
        return FXMenuItem.newItem(I18nHelper.setToEmptyString(), action);
    }

    public static FXMenuItem fieldInfo(Runnable action) {
        return FXMenuItem.newItem(I18nHelper.fieldInfo(), action);
    }

    public static FXMenuItem closeDatabase(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.closeDatabase(), new CloseSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem editDatabase(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.editDatabase(), new EditSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem deleteDatabase(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.deleteDatabase(), new DeleteSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem databaseInfo(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.databaseInfo(), new InfoSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem addDatabase(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.addDatabase(), new AddSVGGlyph(iconSize), null, action);
    }
}



