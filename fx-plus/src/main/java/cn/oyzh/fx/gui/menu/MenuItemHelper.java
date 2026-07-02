package cn.oyzh.fx.gui.menu;

import cn.oyzh.fx.gui.svg.glyph.AddGroupSVGGlyph;
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
import cn.oyzh.fx.gui.svg.glyph.DownloadSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.EditSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.ErrorInfoSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.ExpandAllSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.ExportSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.FilterSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.ForceKillSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.HistorySVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.ImportSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.InfoSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.KillSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.LoadAllSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.LogSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.MoreSVGGlyph;
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
import cn.oyzh.fx.gui.svg.glyph.SFTPSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.SortAscSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.SortDescSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.StopSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.TerminalSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.TimeSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.TransportSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.TruncateSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.UnCollectSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.UnCompressSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.UnLockSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.UnPauseSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.UndoSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.DumpSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.FunctionSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.ProcedureSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.RunFileSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.RunSqlFileSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.file.FileSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.menu.MenuItemManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

/**
 * 菜单工具类
 *
 * @author oyzh
 * @since 2024/6/28
 */
public class MenuItemHelper {

    /**
     * 菜单
     *
     * @param text 文本
     * @return 菜单
     */
    public static Menu menu(String text) {
        return MenuItemManager.getMenu(text, null, null);
    }

    /**
     * 菜单
     *
     * @param text    文本
     * @param graphic 图标
     * @return 菜单
     */
    public static Menu menu(String text, Node graphic) {
        return MenuItemManager.getMenu(text, graphic, null);
    }

    /**
     * 菜单
     *
     * @param text    文本
     * @param graphic 图标
     * @param action  操作
     * @return 菜单
     */
    public static Menu menu(String text, Node graphic, Runnable action) {
        return MenuItemManager.getMenu(text, graphic, action);
    }

    /**
     * 菜单项
     *
     * @param text   文本
     * @param action 操作
     * @return 菜单项
     */
    public static MenuItem menuItem(String text, Runnable action) {
        return MenuItemManager.getMenuItem(text, action);
    }

    /**
     * 菜单项
     *
     * @param text    文本
     * @param graphic 图标
     * @param action  操作
     * @return 菜单项
     */
    public static MenuItem menuItem(String text, Node graphic, Runnable action) {
        return MenuItemManager.getMenuItem(text, graphic, action);
    }

    // /**
    //  * 新菜单项
    //  *
    //  * @param text   文本
    //  * @param action 操作
    //  * @return 菜单项
    //  */
    // public static MenuItem newMenuItem(String text, Runnable action) {
    //     return new FXMenuItem(null, text, action);
    // }
    //
    // /**
    //  * 新菜单项
    //  *
    //  * @param text    文本
    //  * @param graphic 图标
    //  * @param action  操作
    //  * @return 菜单项
    //  */
    // public static MenuItem newMenuItem(String text, Node graphic, Runnable action) {
    //     return new FXMenuItem(graphic, text, action);
    // }

    /**
     * 分割菜单项
     *
     * @return 分割菜单项
     */
    public static SeparatorMenuItem separator() {
        return MenuItemManager.getSeparatorMenuItem();
    }

    public static FXMenuItem openView(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.openView(), new OpenSVGGlyph(), action);
    }

    public static FXMenuItem openEvent(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.openEvent(), new OpenSVGGlyph(), action);
    }

    public static FXMenuItem openQuery(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.openQuery(), new OpenSVGGlyph(), action);
    }

    public static FXMenuItem viewInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.viewInfo(), new InfoSVGGlyph(), action);
    }

    public static FXMenuItem deleteView(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteView(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem cloneView(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.cloneView(), new CopySVGGlyph(), action);
    }

    public static FXMenuItem cloneFunction(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.cloneFunction(), new CopySVGGlyph(), action);
    }

    public static FXMenuItem cloneProcedure(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.cloneProcedure(), new CopySVGGlyph(), action);
    }

    public static FXMenuItem cloneEvent(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.cloneEvent(), new CopySVGGlyph(), action);
    }

    public static FXMenuItem addView(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addView(), new AddSVGGlyph(), action);
    }

    public static FXMenuItem addQuery(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addQuery(), new AddSVGGlyph(), action);
    }

    public static FXMenuItem designView(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.designView(), new DesignSVGGlyph(), action);
    }

    public static FXMenuItem refreshData(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.refreshData(), new RefreshSVGGlyph(), action);
    }

    public static FXMenuItem restoreHistory(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.restoreHistory(), new UndoSVGGlyph(), action);
    }

    public static FXMenuItem view1History(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.view1History(), new ViewSVGGlyph(), action);
    }

    public static FXMenuItem refreshHistory(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.refreshHistory(), new RefreshSVGGlyph(), action);
    }

    public static FXMenuItem refreshBucket(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.refreshBucket(), new RefreshSVGGlyph(), action);
    }

    public static FXMenuItem refresh(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.refresh(), new RefreshSVGGlyph(), action);
    }

    public static FXMenuItem refreshFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.refreshFile(), new RefreshSVGGlyph(), action);
    }

    public static FXMenuItem reloadData(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.reloadData(), new RefreshSVGGlyph(), action);
    }

    public static FXMenuItem addTable(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addTable(), new AddSVGGlyph(), action);
    }

    public static FXMenuItem addCollection(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addCollection(), new AddSVGGlyph(), action);
    }

    public static FXMenuItem createUser(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.createUser(), new AddSVGGlyph(), action);
    }

    public static FXMenuItem openTerminal(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.openTerminal(), new TerminalSVGGlyph(), action);
    }

    public static FXMenuItem openProcedure(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.openProcedure(), new ProcedureSVGGlyph(), action);
    }

    public static FXMenuItem designProcedure(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.designProcedure(), new DesignSVGGlyph(), action);
    }

    public static FXMenuItem designEvent(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.designEvent(), new DesignSVGGlyph(), action);
    }

    public static FXMenuItem addProcedure(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addProcedure(), new AddSVGGlyph(), action);
    }

    public static FXMenuItem addEvent(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addEvent(), new AddSVGGlyph(), action);
    }

    public static FXMenuItem procedureInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.procedureInfo(), new InfoSVGGlyph(), action);
    }

    public static FXMenuItem eventInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.eventInfo(), new InfoSVGGlyph(), action);
    }

    public static FXMenuItem addFunction(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addFunction(), new AddSVGGlyph(), action);
    }

    public static FXMenuItem openFunction(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.openFunction(), new FunctionSVGGlyph(), action);
    }

    public static FXMenuItem designFunction(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.designFunction(), new DesignSVGGlyph(), action);
    }

    public static FXMenuItem functionInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.functionInfo(), new InfoSVGGlyph(), action);
    }

    public static FXMenuItem moveKey(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.moveKey(), new MoveSVGGlyph(), action);
    }

    public static FXMenuItem keyFilter(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.keyFilter(), new FilterSVGGlyph(), action);
    }

    public static FXMenuItem filterKey(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.filterKey(), new FilterSVGGlyph(), action);
    }

    public static FXMenuItem exportNode(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.exportNode(), new ExportSVGGlyph(), action);
    }

    public static FXMenuItem exportKey(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.exportKey(), new ExportSVGGlyph(), action);
    }

    public static FXMenuItem exportKey1(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.exportKey1(), new ExportSVGGlyph(), action);
    }

    public static FXMenuItem exportData(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.exportData(), new ExportSVGGlyph(), action);
    }

    public static FXMenuItem sortAsc(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.sortAsc(), new SortAscSVGGlyph(), action);
    }

    public static FXMenuItem sortDesc(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.sortDesc(), new SortDescSVGGlyph(), action);
    }

    public static FXMenuItem dumpData(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.dumpData(), new DumpSVGGlyph(), action);
    }

    public static FXMenuItem runSqlFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.runSqlFile(), new RunSqlFileSVGGlyph(), action);
    }

    public static FXMenuItem runScriptFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.runScriptFile(), new RunFileSVGGlyph(), action);
    }

    public static FXMenuItem run(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.run(), new RunSVGGlyph(), action);
    }

    public static FXMenuItem runSelected(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.runSelected(), action);
    }

    public static FXMenuItem runImage(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.runImage(), new RunSVGGlyph(), action);
    }

    public static FXMenuItem saveImage(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.saveImage(), new ImportSVGGlyph(), action);
    }

    public static FXMenuItem updateTag(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.updateTag(), new EditSVGGlyph(), action);
    }

    public static FXMenuItem saveContainer(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.saveContainer(), new ExportSVGGlyph(), action);
    }

    public static FXMenuItem exportConnect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.exportConnect(), new ExportSVGGlyph(), action);
    }

    public static FXMenuItem importConnect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.importConnect(), new ImportSVGGlyph(), action);
    }

    public static FXMenuItem importData(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.importData(), new ImportSVGGlyph(), action);
    }

    public static FXMenuItem renameConnect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameConnect(), new RenameSVGGlyph(), action);
    }

    public static FXMenuItem rename(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.rename(), new RenameSVGGlyph(), action);
    }

    public static FXMenuItem renameSnippet(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameSnippet(), new RenameSVGGlyph(), action);
    }

    public static FXMenuItem renameGroup(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameGroup(), new RenameSVGGlyph(), action);
    }

    public static FXMenuItem renameFolder(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameFolder(), new RenameSVGGlyph(), action);
    }

    public static FXMenuItem renameFolder1(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameFolder1(), new RenameSVGGlyph(), action);
    }

    public static FXMenuItem renameNode(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameNode(), new RenameSVGGlyph(), action);
    }

    public static FXMenuItem renameFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameFile(), new RenameSVGGlyph(), action);
    }

    public static FXMenuItem renameDir(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameDir(), new RenameSVGGlyph(), action);
    }

    public static FXMenuItem filePermission(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.filePermission(), new PermissionSVGGlyph(), action);
    }

    public static FXMenuItem cloneNode(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.cloneNode(), new RepeatSVGGlyph(), action);
    }

    public static FXMenuItem renameKey(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameKey(), new RenameSVGGlyph(), action);
    }

    public static FXMenuItem renameKey1(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameKey1(), new RenameSVGGlyph(), action);
    }

    public static FXMenuItem updateKey1(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.updateKey1(), new EditSVGGlyph(), action);
    }

    public static FXMenuItem updateBucket(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.updateBucket(), new EditSVGGlyph(), action);
    }

    public static FXMenuItem editKey1(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.updateKey1(), new EditSVGGlyph(), action);
    }

    public static FXMenuItem editConnect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.editConnect(), new EditSVGGlyph(), action);
    }

    public static FXMenuItem edit(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.edit(), new EditSVGGlyph(), action);
    }

    public static FXMenuItem edit_no_graphic(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.edit(), action);
    }

    public static FXMenuItem editSnippet(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.editSnippet(), new EditSVGGlyph(), action);
    }

    public static FXMenuItem editQuery(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.editQuery(), new EditSVGGlyph(), action);
    }

    public static FXMenuItem editFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.editFile(), new EditSVGGlyph(), action);
    }

    public static FXMenuItem view1File(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.view1File(), new ViewSVGGlyph(), action);
    }

    public static FXMenuItem view1User(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.view1User(), new ViewSVGGlyph(), action);
    }

    public static FXMenuItem editCollections(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.editCollections(), new EditSVGGlyph(), action);
    }

    public static FXMenuItem editGroup(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.editGroup(), new EditSVGGlyph(), action);
    }

    public static FXMenuItem editRow(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.editRow(), new EditSVGGlyph(), action);
    }

    public static FXMenuItem deleteKey(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteKey(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem deleteKey1(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteKey1(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem copyKeys1ToHost(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyKeys1ToHost(), new CopySVGGlyph(), action);
    }

    public static FXMenuItem copyToHost(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyToHost(), new CopySVGGlyph(), action);
    }

    public static FXMenuItem deleteData(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteData(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem deleteAuth(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteAuth(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem deleteFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteFile(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem deleteDir(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteDir(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem deleteContainer(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteContainer(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem containerLogs(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.containerLogs(), new LogSVGGlyph(), action);
    }

    public static FXMenuItem renameContainer(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameContainer(), new EditSVGGlyph(), action);
    }

    public static FXMenuItem deleteImage(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteImage(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem start1Container(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.start1Container(), new RunSVGGlyph(), action);
    }

    public static FXMenuItem stop1Container(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.stop1Container(), new StopSVGGlyph(), action);
    }

    public static FXMenuItem killContainer(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.killContainer(), new KillSVGGlyph(), action);
    }

    public static FXMenuItem killProcess(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.killProcess(), new KillSVGGlyph(), action);
    }

    public static FXMenuItem forceKillProcess(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.forceKillProcess(), new ForceKillSVGGlyph(), action);
    }

    public static FXMenuItem restartContainer(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.restartContainer(), new RestartSVGGlyph(), action);
    }

    public static FXMenuItem pauseContainer(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.pauseContainer(), new PauseSVGGlyph(), action);
    }

    public static FXMenuItem unpauseContainer(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.unpauseContainer(), new UnPauseSVGGlyph(), action);
    }

    public static FXMenuItem forceDeleteContainer(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.forceDeleteContainer(), new DeleteForceSVGGlyph(), action);
    }

    public static FXMenuItem forceDeleteImage(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.forceDeleteImage(), new DeleteForceSVGGlyph(), action);
    }

    public static FXMenuItem deleteGroup(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteGroup(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem deleteFolder(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteFolder(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem deleteFolder1(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteFolder1(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem deleteNode(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteNode(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem deleteConnect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteConnect(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem delete(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.delete(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem deleteHistory(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteHistory(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem deleteSnippet(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteSnippet(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem deleteBucket(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteBucket(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem forceDeleteBucket(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.forceDeleteBucket(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem closeConnect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.closeConnect(), new CloseSVGGlyph(), action);
    }

    public static FXMenuItem addGroup(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addGroup(), new AddGroupSVGGlyph(), action);
    }

    public static FXMenuItem addFolder(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addFolder(), new AddGroupSVGGlyph(), action);
    }

    public static FXMenuItem addFolder1(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addFolder1(), new AddGroupSVGGlyph(), action);
    }

    public static FXMenuItem addConnect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addConnect(), new AddSVGGlyph(), action);
    }

    public static FXMenuItem add(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.add(), new AddSVGGlyph(), action);
    }

    public static FXMenuItem addSnippet(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addSnippet(), new AddSVGGlyph(), action);
    }

    public static FXMenuItem addBucket(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addBucket(), new AddSVGGlyph(), action);
    }

    public static FXMenuItem addCollections(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addCollections(), new AddSVGGlyph(), action);
    }

    public static FXMenuItem collectFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.collectFile(), new CollectSVGGlyph(), action);
    }

    public static FXMenuItem collectKey(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.collectKey(), new CollectSVGGlyph(), action);
    }

    public static FXMenuItem unCollect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.unCollect(), new UnCollectSVGGlyph(), action);
    }

    public static FXMenuItem unCollectKey(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.unCollectKey(), new UnCollectSVGGlyph(), action);
    }

    public static FXMenuItem addKey(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addKey(), new AddSVGGlyph(), action);
    }

    public static FXMenuItem addNode(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addNode(), new AddSVGGlyph(), action);
    }

    public static FXMenuItem transportData(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.transportData(), new TransportSVGGlyph(), action);
    }

    public static FXMenuItem transportFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.transportFile(), new TransportSVGGlyph(), action);
    }

    public static FXMenuItem transportKey(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.transportKey(), new TransportSVGGlyph(), action);
    }

    public static FXMenuItem transportNode(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.transportNode(), new TransportSVGGlyph(), action);
    }

    public static FXMenuItem repeatConnect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.repeatConnect(), new RepeatSVGGlyph(), action);
    }

    public static FXMenuItem cloneConnect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.cloneConnect(), new RepeatSVGGlyph(), action);
    }

    public static FXMenuItem copyConnect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyConnect(), new CopySVGGlyph(), action);
    }

    public static FXMenuItem copyInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyInfo(), new CopySVGGlyph(), action);
    }

    public static FXMenuItem cloneSession(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.cloneSession(), action);
    }

    public static FXMenuItem copyThisSession(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyThisSession(), action);
    }

    public static FXMenuItem reload(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.reload(), new RefreshSVGGlyph(), action);
    }

    public static FXMenuItem reloadDatabase(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.reloadDatabase(), new RefreshSVGGlyph(), action);
    }

    public static FXMenuItem serverInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.serverInfo(), new InfoSVGGlyph(), action);
    }

    public static FXMenuItem startConnect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.startConnect(), new PlaySVGGlyph(), action);
    }

    public static FXMenuItem openConnect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.openConnect(), new OpenSVGGlyph(), action);
    }

    public static FXMenuItem openSFTP(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.openSFTP(), new SFTPSVGGlyph(), action);
    }

    public static FXMenuItem clearData(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.clearData(), new ClearSVGGlyph(), action);
    }

    public static FXMenuItem copyKey(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyKey(), new CopySVGGlyph(), action);
    }

    public static FXMenuItem copyFilePath(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyFilePath(), new CopySVGGlyph(), action);
    }

    public static FXMenuItem copyNodePath(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyNodePath(), new CopySVGGlyph(), action);
    }

    public static FXMenuItem copyAuth(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyAuth(), new CopySVGGlyph(), action);
    }

    public static FXMenuItem authNode(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.authNode(), new UnLockSVGGlyph(), action);
    }

    public static FXMenuItem unload(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.unload(), new StopSVGGlyph(), action);
    }

    public static FXMenuItem loadAll(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.loadAll(), new LoadAllSVGGlyph(), action);
    }

    public static FXMenuItem expandAll(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.expandAll(), new ExpandAllSVGGlyph(), action);
    }

    public static FXMenuItem collapseAll(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.collapseAll(), new CollapseAllSVGGlyph(), action);
    }

    public static FXMenuItem deleteFunction(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteFunction(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem deleteProcedure(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteProcedure(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem deleteEvent(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteEvent(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem openTable(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.openTable(), new OpenSVGGlyph(), action);
    }

    public static FXMenuItem openCollection(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.openCollection(), new OpenSVGGlyph(), action);
    }

    public static FXMenuItem openBucket(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.openBucket(), new OpenSVGGlyph(), action);
    }

    public static FXMenuItem editTable(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.editTable(), new EditSVGGlyph(), action);
    }

    public static FXMenuItem designTable(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.designTable(), new DesignSVGGlyph(), action);
    }

    public static FXMenuItem renameTable(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameTable(), new RenameSVGGlyph(), action);
    }

    public static FXMenuItem renameCollection(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameCollection(), new RenameSVGGlyph(), action);
    }

    public static FXMenuItem renameView(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameView(), new RenameSVGGlyph(), action);
    }

    public static FXMenuItem renameEvent(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameEvent(), new RenameSVGGlyph(), action);
    }

    public static FXMenuItem renameFunction(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameFunction(), new RenameSVGGlyph(), action);
    }

    public static FXMenuItem renameProcedure(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameProcedure(), new RenameSVGGlyph(), action);
    }

    public static FXMenuItem renameQuery(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameQuery(), new RenameSVGGlyph(), action);
    }

    public static FXMenuItem clearTable(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.clearTable(), new ClearSVGGlyph(), action);
    }

    public static FXMenuItem clearTableData(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.clearTableData(), new ClearSVGGlyph(), action);
    }

    public static FXMenuItem clearCollectionData(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.clearCollectionData(), new ClearSVGGlyph(), action);
    }

    public static FXMenuItem clearCollection(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.clearCollection(), new ClearSVGGlyph(), action);
    }

    public static FXMenuItem clearBucket(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.clearBucket(), new ClearSVGGlyph(), action);
    }

    public static FXMenuItem truncateTable(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.truncateTable(), new TruncateSVGGlyph(), action);
    }

    public static FXMenuItem deleteTable(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteTable(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem deleteCollection(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteCollection(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem deleteUser(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteUser(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem deleteQuery(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteQuery(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem tableInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.tableInfo(), new InfoSVGGlyph(), action);
    }

    public static FXMenuItem copy(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copy(), new CopySVGGlyph(), action);
    }

    public static FXMenuItem copy_no_graphic(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copy(), action);
    }

    public static FXMenuItem copyAsInsertStatement_no_graphic(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyAsInsertStatement(), action);
    }

    public static FXMenuItem copyAsInsertScript_no_graphic(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyAsInsertScript(), action);
    }

    public static FXMenuItem copyAsUpdateStatement_no_graphic(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyAsUpdateStatement(), action);
    }

    public static FXMenuItem copyAsUpdateScript_no_graphic(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyAsUpdateScript(), action);
    }

    public static FXMenuItem copyFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyFile(), new CopySVGGlyph(), action);
    }

    public static FXMenuItem cutFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.cutFile(), new CutSVGGlyph(), action);
    }

    public static FXMenuItem paste_no_graphic(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.paste(), action);
    }

    public static FXMenuItem pasteFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.pasteFile(), new PasteSVGGlyph(), action);
    }

    public static FXMenuItem unCompress(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.unCompress(), new UnCompressSVGGlyph(), action);
    }

    public static FXMenuItem compress(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.unCompress(), new UnCompressSVGGlyph(), action);
    }

    public static FXMenuItem deleteRecord(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteRecord(), action);
    }

    public static FXMenuItem setToNull_no_graphic(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.setToNull(), action);
    }

    public static FXMenuItem setToEmptyString_no_graphic(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.setToEmptyString(), action);
    }

    public static FXMenuItem fileInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.fileInfo(), new InfoSVGGlyph(), action);
    }

    public static FXMenuItem imageInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.imageInfo(), new InfoSVGGlyph(), action);
    }

    public static FXMenuItem imageInspect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.imageInspect(), new InfoSVGGlyph(), action);
    }

    public static FXMenuItem imageHistory(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.imageHistory(), new HistorySVGGlyph(), action);
    }

    public static FXMenuItem dataHistory(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.dataHistory(), new HistorySVGGlyph(), action);
    }

    public static FXMenuItem containerInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.containerInfo(), new InfoSVGGlyph(), action);
    }

    public static FXMenuItem containerInspect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.containerInspect(), new InfoSVGGlyph(), action);
    }

    public static FXMenuItem containerResource(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.containerResource(), new ResourceSVGGlyph(), action);
    }

    public static FXMenuItem containerPorts(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.containerPorts(), new PortSVGGlyph(), action);
    }

    public static FXMenuItem fieldInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.fieldInfo(), action);
    }

    public static FXMenuItem columnInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.columnInfo(), action);
    }

    public static FXMenuItem copyColumnName(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyColumnName(), action);
    }

    public static FXMenuItem closeDatabase(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.closeDatabase(), new CloseSVGGlyph(), action);
    }

    public static FXMenuItem closeSchema(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.closeSchema(), new CloseSVGGlyph(), action);
    }

    public static FXMenuItem editDatabase(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.editDatabase(), new EditSVGGlyph(), action);
    }

    public static FXMenuItem editSchema(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.editSchema(), new EditSVGGlyph(), action);
    }

    public static FXMenuItem deleteDatabase(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteDatabase(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem dropSchema(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.dropSchema(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem databaseInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.databaseInfo(), new InfoSVGGlyph(), action);
    }

    public static FXMenuItem addDatabase(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addDatabase(), new AddSVGGlyph(), action);
    }

    public static FXMenuItem closeAllTab(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nResourceBundle.i18nString("base.closeAllTab"), action);
    }

    public static FXMenuItem closeCurrTab(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nResourceBundle.i18nString("base.closeCurrTab"), action);
    }

    public static FXMenuItem closeLeftTab(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nResourceBundle.i18nString("base.closeLeftTab"), action);
    }

    public static FXMenuItem closeRightTab(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nResourceBundle.i18nString("base.closeRightTab"), action);
    }

    public static FXMenuItem closeOtherTab(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nResourceBundle.i18nString("base.closeOtherTab"), action);
    }

    public static FXMenuItem closeOtherConnectTab(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nResourceBundle.i18nString("base.closeOtherConnectTab"), action);
    }

    public static FXMenuItem cancelConnect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.cancelConnect(), new CancelSVGGlyph(), action);
    }

    public static FXMenuItem cancelDownload(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.cancelDownload(), new CancelSVGGlyph(), action);
    }

    public static FXMenuItem cancelUpload(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.cancelUpload(), new CancelSVGGlyph(), action);
    }

    public static FXMenuItem cancelTransport(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.cancelTransport(), new CancelSVGGlyph(), action);
    }

    public static FXMenuItem retry(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.retry(), new RestartSVGGlyph(), action);
    }

    public static FXMenuItem removeTransport(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.removeTransport(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem removeDownload(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.removeDownload(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem removeUpload(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.removeUpload(), new DeleteSVGGlyph(), action);
    }

    public static FXMenuItem cancelOperation(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.cancelOperation(), new CancelSVGGlyph(), action);
    }

    public static FXMenuItem batchOpt(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.batchOpt(), new BatchOptSVGGlyph(), action);
    }

    public static FXMenuItem updateTtl(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.updateTtl(), new TimeSVGGlyph(), action);
    }

    public static FXMenuItem openData(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.openData(), new OpenSVGGlyph(), action);
    }

    public static FXMenuItem openServer(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.openServer(), new OpenSVGGlyph(), action);
    }

    public static FXMenuItem openInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.openInfo(), new OpenSVGGlyph(), action);
    }

    public static FXMenuItem downloadFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.downloadFile(), new DownloadSVGGlyph(), action);
    }

    public static FXMenuItem shareFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.shareFile(), new OpenSVGGlyph(), action);
    }

    public static FXMenuItem uploadFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.uploadFile(), new ExportSVGGlyph(), action);
    }

    public static FXMenuItem uploadFolder(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.uploadFolder(), new ExportSVGGlyph(), action);
    }

    public static FXMenuItem touchFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.touchFile(), new FileSVGGlyph(), action);
    }

    public static FXMenuItem errorInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.errorInfo(), new ErrorInfoSVGGlyph(), action);
    }

    public static FXMenuItem moreInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.moreInfo(), new MoreSVGGlyph(), action);
    }
}



