package cn.oyzh.fx.gui.menu;

import cn.oyzh.fx.gui.svg.glyph.AddSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.BatchOptSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.CancelSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.ClearSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.CloseSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.CollapseAllSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.CollectSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.CopySVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.CutSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.DeleteForceSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.DeleteSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.DesignSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.EditSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.ExpandAllSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.ExportSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.FileSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.FilterSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.ForceKillSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.HistorySVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.ImportSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.InfoSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.KillSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.LoadAllSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.LogSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.MoveSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.OpenSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.PasteSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.PauseSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.PermissionSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.PlaySVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.PortSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.RefreshSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.RenameSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.RepeatSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.ResourceSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.RestartSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.RunSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.StopSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.TerminalSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.TimeSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.TransportSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.TruncateSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.UnCollectSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.UnCompressSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.UnLockSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.UnPauseSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.DumpSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.FunctionSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.ProcedureSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.RunSqlFileSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.SeparatorMenuItem;

/**
 * 菜单工具类
 *
 * @author oyzh
 * @since 2024/6/28
 */
public class MenuItemHelper {

    /**
     * 分割用菜单项
     *
     * @return 分割菜单项
     */
    public static SeparatorMenuItem separator() {
        return new SeparatorMenuItem();
    }

    public static FXMenuItem openView(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.openView(), new OpenSVGGlyph(iconSize), action);
    }

    public static FXMenuItem openQuery(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.openQuery(), new OpenSVGGlyph(iconSize), action);
    }

    public static FXMenuItem viewInfo(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.viewInfo(), new InfoSVGGlyph(iconSize), action);
    }

    public static FXMenuItem deleteView(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.deleteView(), new DeleteSVGGlyph(iconSize), action);
    }

    public static FXMenuItem addView(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.addView(), new AddSVGGlyph(iconSize), action);
    }

    public static FXMenuItem addQuery(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.addQuery(), new AddSVGGlyph(iconSize), action);
    }

    public static FXMenuItem designView(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.designView(), new DesignSVGGlyph(iconSize), action);
    }

    public static FXMenuItem refreshData(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.refreshData(), new RefreshSVGGlyph(iconSize), action);
    }

    public static FXMenuItem refreshBucket(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.refreshBucket(), new RefreshSVGGlyph(iconSize), action);
    }

    public static FXMenuItem refresh(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.refresh(), new RefreshSVGGlyph(iconSize), action);
    }

    public static FXMenuItem refreshFile(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.refreshFile(), new RefreshSVGGlyph(iconSize), action);
    }

    public static FXMenuItem reloadData(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.reloadData(), new RefreshSVGGlyph(iconSize), action);
    }

    public static FXMenuItem addTable(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.addTable(), new AddSVGGlyph(iconSize), action);
    }

    public static FXMenuItem openTerminal(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.openTerminal(), new TerminalSVGGlyph(iconSize), action);
    }

    public static FXMenuItem openProcedure(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.openProcedure(), new ProcedureSVGGlyph(iconSize), action);
    }

    public static FXMenuItem designProcedure(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.designProcedure(), new DesignSVGGlyph(iconSize), action);
    }

    public static FXMenuItem designEvent(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.designEvent(), new DesignSVGGlyph(iconSize), action);
    }

    public static FXMenuItem addProcedure(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.addProcedure(), new AddSVGGlyph(iconSize), action);
    }

    public static FXMenuItem addEvent(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.addEvent(), new AddSVGGlyph(iconSize), action);
    }

    public static FXMenuItem procedureInfo(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.procedureInfo(), new InfoSVGGlyph(iconSize), action);
    }

    public static FXMenuItem eventInfo(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.eventInfo(), new InfoSVGGlyph(iconSize), action);
    }

    public static FXMenuItem addFunction(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.addFunction(), new AddSVGGlyph(iconSize), action);
    }

    public static FXMenuItem openFunction(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.openFunction(), new FunctionSVGGlyph(iconSize), action);
    }

    public static FXMenuItem designFunction(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.designFunction(), new DesignSVGGlyph(iconSize), action);
    }

    public static FXMenuItem functionInfo(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.functionInfo(), new InfoSVGGlyph(iconSize), action);
    }

    public static FXMenuItem moveKey(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.moveKey(), new MoveSVGGlyph(iconSize), action);
    }

    public static FXMenuItem keyFilter(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.keyFilter(), new FilterSVGGlyph(iconSize), action);
    }

    public static FXMenuItem exportNode(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.exportNode(), new ExportSVGGlyph(iconSize), action);
    }

    public static FXMenuItem exportKey(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.exportKey(), new ExportSVGGlyph(iconSize), action);
    }

    public static FXMenuItem exportKey1(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.exportKey1(), new ExportSVGGlyph(iconSize), action);
    }

    public static FXMenuItem exportData(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.exportData(), new ExportSVGGlyph(iconSize), action);
    }

    public static FXMenuItem dumpData(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.dumpData(), new DumpSVGGlyph(iconSize), action);
    }

    public static FXMenuItem runSqlFile(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.runSqlFile(), new RunSqlFileSVGGlyph(iconSize), action);
    }

    public static FXMenuItem exportConnect(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.exportConnect(), new ExportSVGGlyph(iconSize), action);
    }

    public static FXMenuItem importConnect(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.importConnect(), new ImportSVGGlyph(iconSize), action);
    }

    public static FXMenuItem importData(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.importData(), new ImportSVGGlyph(iconSize), action);
    }

    public static FXMenuItem renameConnect(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.renameConnect(), new RenameSVGGlyph(iconSize), action);
    }

    public static FXMenuItem rename(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.rename(), new RenameSVGGlyph(iconSize), action);
    }

    public static FXMenuItem renameGroup(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.renameGroup(), new RenameSVGGlyph(iconSize), action);
    }

    public static FXMenuItem renameNode(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.renameNode(), new RenameSVGGlyph(iconSize), action);
    }

    public static FXMenuItem renameFile(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.renameFile(), new RenameSVGGlyph(iconSize), action);
    }

    public static FXMenuItem filePermission(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.filePermission(), new PermissionSVGGlyph(iconSize), action);
    }

    public static FXMenuItem cloneNode(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.cloneNode(), new RepeatSVGGlyph(iconSize), action);
    }

    public static FXMenuItem renameKey(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.renameKey(), new RenameSVGGlyph(iconSize), action);
    }

    public static FXMenuItem renameKey1(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.renameKey1(), new RenameSVGGlyph(iconSize), action);
    }

    public static FXMenuItem updateKey1(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.updateKey1(), new EditSVGGlyph(iconSize), action);
    }

    public static FXMenuItem editKey1(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.updateKey1(), new EditSVGGlyph(iconSize), action);
    }

    public static FXMenuItem editConnect(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.editConnect(), new EditSVGGlyph(iconSize), action);
    }

    public static FXMenuItem edit(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.edit(), new EditSVGGlyph(iconSize), action);
    }

    public static FXMenuItem editFile(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.editFile(), new EditSVGGlyph(iconSize), action);
    }

    public static FXMenuItem editCollections(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.editCollections(), new EditSVGGlyph(iconSize), action);
    }

    public static FXMenuItem editGroup(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.editGroup(), new EditSVGGlyph(iconSize), action);
    }

    public static FXMenuItem editRow(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.editRow(), new EditSVGGlyph(iconSize), action);
    }

    public static FXMenuItem deleteKey(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.deleteKey(), new DeleteSVGGlyph(iconSize), action);
    }

    public static FXMenuItem deleteKey1(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.deleteKey1(), new DeleteSVGGlyph(iconSize), action);
    }

    public static FXMenuItem copyKeys1ToHost(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.copyKeys1ToHost(), new CopySVGGlyph(iconSize), action);
    }

    public static FXMenuItem copyToHost(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.copyToHost(), new CopySVGGlyph(iconSize), action);
    }

    public static FXMenuItem deleteData(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.deleteData(), new DeleteSVGGlyph(iconSize), action);
    }

    public static FXMenuItem deleteFile(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.deleteFile(), new DeleteSVGGlyph(iconSize), action);
    }

    public static FXMenuItem deleteContainer(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.deleteContainer(), new DeleteSVGGlyph(iconSize), action);
    }

    public static FXMenuItem containerLogs(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.containerLogs(), new LogSVGGlyph(iconSize), action);
    }

    public static FXMenuItem renameContainer(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.renameContainer(), new EditSVGGlyph(iconSize), action);
    }

    public static FXMenuItem deleteImage(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.deleteImage(), new DeleteSVGGlyph(iconSize), action);
    }

    public static FXMenuItem start1Container(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.start1Container(), new RunSVGGlyph(iconSize), action);
    }

    public static FXMenuItem stopContainer(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.stopContainer(), new StopSVGGlyph(iconSize), action);
    }

    public static FXMenuItem killContainer(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.killContainer(), new KillSVGGlyph(iconSize), action);
    }

    public static FXMenuItem killProcess(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.killProcess(), new KillSVGGlyph(iconSize), action);
    }

    public static FXMenuItem forceKillProcess(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.forceKillProcess(), new ForceKillSVGGlyph(iconSize), action);
    }

    public static FXMenuItem restartContainer(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.restartContainer(), new RestartSVGGlyph(iconSize), action);
    }

    public static FXMenuItem pauseContainer(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.pauseContainer(), new PauseSVGGlyph(iconSize), action);
    }

    public static FXMenuItem unpauseContainer(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.unpauseContainer(), new UnPauseSVGGlyph(iconSize), action);
    }

    public static FXMenuItem forceDeleteContainer(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.forceDeleteContainer(), new DeleteForceSVGGlyph(iconSize), action);
    }

    public static FXMenuItem forceDeleteImage(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.forceDeleteImage(), new DeleteForceSVGGlyph(iconSize), action);
    }

    public static FXMenuItem deleteGroup(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.deleteGroup(), new DeleteSVGGlyph(iconSize), action);
    }

    public static FXMenuItem deleteNode(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.deleteNode(), new DeleteSVGGlyph(iconSize), action);
    }

    public static FXMenuItem deleteConnect(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.deleteConnect(), new DeleteSVGGlyph(iconSize), action);
    }

    public static FXMenuItem delete(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.delete(), new DeleteSVGGlyph(iconSize), action);
    }

    public static FXMenuItem deleteBucket(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.deleteBucket(), new DeleteSVGGlyph(iconSize), action);
    }

    public static FXMenuItem forceDeleteBucket(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.forceDeleteBucket(), new DeleteSVGGlyph(iconSize), action);
    }

    public static FXMenuItem closeConnect(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.closeConnect(), new CloseSVGGlyph(iconSize), action);
    }

    public static FXMenuItem addGroup(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.addGroup(), new AddSVGGlyph(iconSize), action);
    }

    public static FXMenuItem addConnect(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.addConnect(), new AddSVGGlyph(iconSize), action);
    }

    public static FXMenuItem add(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.add(), new AddSVGGlyph(iconSize), action);
    }

    public static FXMenuItem addBucket(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.addBucket(), new AddSVGGlyph(iconSize), action);
    }

    public static FXMenuItem addCollections(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.addCollections(), new AddSVGGlyph(iconSize), action);
    }

    public static FXMenuItem collectFile(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.collectFile(), new CollectSVGGlyph(iconSize), action);
    }

    public static FXMenuItem unCollect(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.unCollect(), new UnCollectSVGGlyph(iconSize), action);
    }

    public static FXMenuItem addKey(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.addKey(), new AddSVGGlyph(iconSize), action);
    }

    public static FXMenuItem addNode(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.addNode(), new AddSVGGlyph(iconSize), action);
    }

    public static FXMenuItem transportData(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.transportData(), new TransportSVGGlyph(iconSize), action);
    }

    public static FXMenuItem transportFile(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.transportFile(), new TransportSVGGlyph(iconSize), action);
    }

    public static FXMenuItem transportKey(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.transportKey(), new TransportSVGGlyph(iconSize), action);
    }

    public static FXMenuItem transportNode(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.transportNode(), new TransportSVGGlyph(iconSize), action);
    }

    public static FXMenuItem repeatConnect(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.repeatConnect(), new RepeatSVGGlyph(iconSize), action);
    }

    public static FXMenuItem cloneConnect(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.cloneConnect(), new RepeatSVGGlyph(iconSize), action);
    }

    public static FXMenuItem reload(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.reload(), new RefreshSVGGlyph(iconSize), action);
    }

    public static FXMenuItem serverInfo(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.serverInfo(), new InfoSVGGlyph(iconSize), action);
    }

    public static FXMenuItem startConnect(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.startConnect(), new PlaySVGGlyph(iconSize), action);
    }

    public static FXMenuItem clearData(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.clearData(), new ClearSVGGlyph(iconSize), action);
    }

    public static FXMenuItem copyKey(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.copyKey(), new CopySVGGlyph(iconSize), action);
    }

    public static FXMenuItem copyFilePath(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.copyFilePath(), new CopySVGGlyph(iconSize), action);
    }

    public static FXMenuItem copyNodePath(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.copyNodePath(), new CopySVGGlyph(iconSize), action);
    }

    public static FXMenuItem authNode(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.authNode(), new UnLockSVGGlyph(iconSize), action);
    }

    public static FXMenuItem unload(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.unload(), new StopSVGGlyph(iconSize), action);
    }

    public static FXMenuItem loadAll(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.loadAll(), new LoadAllSVGGlyph(iconSize), action);
    }

    public static FXMenuItem expandAll(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.expandAll(), new ExpandAllSVGGlyph(iconSize), action);
    }

    public static FXMenuItem collapseAll(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.collapseAll(), new CollapseAllSVGGlyph(iconSize), action);
    }

    public static FXMenuItem deleteFunction(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.deleteFunction(), new DeleteSVGGlyph(iconSize), action);
    }

    public static FXMenuItem deleteProcedure(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.deleteProcedure(), new DeleteSVGGlyph(iconSize), action);
    }

    public static FXMenuItem deleteEvent(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.deleteEvent(), new DeleteSVGGlyph(iconSize), action);
    }

    public static FXMenuItem openTable(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.openTable(), new OpenSVGGlyph(iconSize), action);
    }

    public static FXMenuItem editTable(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.editTable(), new EditSVGGlyph(iconSize), action);
    }

    public static FXMenuItem designTable(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.designTable(), new DesignSVGGlyph(iconSize), action);
    }

    public static FXMenuItem renameTable(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.renameTable(), new RenameSVGGlyph(iconSize), action);
    }

    public static FXMenuItem renameQuery(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.renameQuery(), new RenameSVGGlyph(iconSize), action);
    }

    public static FXMenuItem clearTable(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.clearTable(), new ClearSVGGlyph(iconSize), action);
    }

    public static FXMenuItem truncateTable(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.truncateTable(), new TruncateSVGGlyph(iconSize), action);
    }

    public static FXMenuItem deleteTable(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.deleteTable(), new DeleteSVGGlyph(iconSize), action);
    }

    public static FXMenuItem deleteQuery(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.deleteQuery(), new DeleteSVGGlyph(iconSize), action);
    }

    public static FXMenuItem tableInfo(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.tableInfo(), new InfoSVGGlyph(iconSize), action);
    }

    public static FXMenuItem copy(Runnable action) {
        return FXMenuItem.newItem(I18nHelper.copy(), action);
    }

    public static FXMenuItem copy(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.copy(), new CopySVGGlyph(iconSize), action);
    }

    public static FXMenuItem copyFile(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.copyFile(), new CopySVGGlyph(iconSize), action);
    }

    public static FXMenuItem cutFile(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.cutFile(), new CutSVGGlyph(iconSize), action);
    }

    public static FXMenuItem paste(Runnable action) {
        return FXMenuItem.newItem(I18nHelper.paste(), action);
    }

    public static FXMenuItem pasteFile(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.pasteFile(), new PasteSVGGlyph(iconSize), action);
    }

    public static FXMenuItem unCompress(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.unCompress(), new UnCompressSVGGlyph(iconSize), action);
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

    public static FXMenuItem fileInfo(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.fileInfo(), new InfoSVGGlyph(iconSize), action);
    }

    public static FXMenuItem imageInfo(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.imageInfo(), new InfoSVGGlyph(iconSize), action);
    }

    public static FXMenuItem imageInspect(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.imageInspect(), new InfoSVGGlyph(iconSize), action);
    }

    public static FXMenuItem imageHistory(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.imageHistory(), new HistorySVGGlyph(iconSize), action);
    }

    public static FXMenuItem containerInfo(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.containerInfo(), new InfoSVGGlyph(iconSize), action);
    }

    public static FXMenuItem containerInspect(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.containerInspect(), new InfoSVGGlyph(iconSize), action);
    }

    public static FXMenuItem containerResource(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.containerResource(), new ResourceSVGGlyph(iconSize), action);
    }

    public static FXMenuItem containerPorts(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.containerPorts(), new PortSVGGlyph(iconSize), action);
    }

    public static FXMenuItem fieldInfo(Runnable action) {
        return FXMenuItem.newItem(I18nHelper.fieldInfo(), action);
    }

    public static FXMenuItem closeDatabase(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.closeDatabase(), new CloseSVGGlyph(iconSize), action);
    }

    public static FXMenuItem closeSchema(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.closeSchema(), new CloseSVGGlyph(iconSize), action);
    }

    public static FXMenuItem editDatabase(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.editDatabase(), new EditSVGGlyph(iconSize), action);
    }

    public static FXMenuItem editSchema(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.editSchema(), new EditSVGGlyph(iconSize), action);
    }

    public static FXMenuItem deleteDatabase(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.deleteDatabase(), new DeleteSVGGlyph(iconSize), action);
    }

    public static FXMenuItem dropSchema(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.dropSchema(), new DeleteSVGGlyph(iconSize), action);
    }

    public static FXMenuItem databaseInfo(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.databaseInfo(), new InfoSVGGlyph(iconSize), action);
    }

    public static FXMenuItem addDatabase(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.addDatabase(), new AddSVGGlyph(iconSize), action);
    }

    public static FXMenuItem closeAllTab(Runnable action) {
        return FXMenuItem.newItem(I18nResourceBundle.i18nString("base.closeAllTab"), action);
    }

    public static FXMenuItem closeCurrTab(Runnable action) {
        return FXMenuItem.newItem(I18nResourceBundle.i18nString("base.closeCurrTab"), action);
    }

    public static FXMenuItem closeLeftTab(Runnable action) {
        return FXMenuItem.newItem(I18nResourceBundle.i18nString("base.closeLeftTab"), action);
    }

    public static FXMenuItem closeRightTab(Runnable action) {
        return FXMenuItem.newItem(I18nResourceBundle.i18nString("base.closeRightTab"), action);
    }

    public static FXMenuItem closeOtherTab(Runnable action) {
        return FXMenuItem.newItem(I18nResourceBundle.i18nString("base.closeOtherTab"), action);
    }

    public static FXMenuItem closeOtherConnectTab(Runnable action) {
        return FXMenuItem.newItem(I18nResourceBundle.i18nString("base.closeOtherConnectTab"), action);
    }

    public static FXMenuItem cancelConnect(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.cancelConnect(), new CancelSVGGlyph(iconSize), action);
    }

    public static FXMenuItem cancelDownload(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.cancelDownload(), new CancelSVGGlyph(iconSize), action);
    }

    public static FXMenuItem cancelUpload(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.cancelUpload(), new CancelSVGGlyph(iconSize), action);
    }

    public static FXMenuItem cancelTransport(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.cancelTransport(), new CancelSVGGlyph(iconSize), action);
    }

    public static FXMenuItem retry(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.retry(), new RestartSVGGlyph(iconSize), action);
    }

    public static FXMenuItem removeTransport(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.removeTransport(), new DeleteSVGGlyph(iconSize), action);
    }

    public static FXMenuItem removeDownload(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.removeDownload(), new DeleteSVGGlyph(iconSize), action);
    }

    public static FXMenuItem removeUpload(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.removeUpload(), new DeleteSVGGlyph(iconSize), action);
    }

    public static FXMenuItem cancelOperation(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.cancelOperation(), new CancelSVGGlyph(iconSize), action);
    }

    public static FXMenuItem batchOpt(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.batchOpt(), new BatchOptSVGGlyph(iconSize), action);
    }

    public static FXMenuItem updateTtl(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.updateTtl(), new TimeSVGGlyph(iconSize), action);
    }

    public static FXMenuItem openData(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.openData(), new OpenSVGGlyph(iconSize), action);
    }

    public static FXMenuItem openServer(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.openServer(), new OpenSVGGlyph(iconSize), action);
    }

    public static FXMenuItem openInfo(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.openInfo(), new OpenSVGGlyph(iconSize), action);
    }

    public static FXMenuItem downloadFile(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.downloadFile(), new ImportSVGGlyph(iconSize), action);
    }

    public static FXMenuItem uploadFile(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.uploadFile(), new ExportSVGGlyph(iconSize), action);
    }

    public static FXMenuItem uploadFolder(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.uploadFolder(), new ExportSVGGlyph(iconSize), action);
    }

    public static FXMenuItem touchFile(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.touchFile(), new FileSVGGlyph(iconSize), action);
    }
}



