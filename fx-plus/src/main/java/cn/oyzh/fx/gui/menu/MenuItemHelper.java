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
import cn.oyzh.fx.gui.svg.glyph.EditDocumentSVGGlyph;
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

    /**
     * 打开视图。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem openView(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.openView(), new OpenSVGGlyph(), action);
    }

    /**
     * 打开事件。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem openEvent(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.openEvent(), new OpenSVGGlyph(), action);
    }

    /**
     * 打开查询。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem openQuery(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.openQuery(), new OpenSVGGlyph(), action);
    }

    /**
     * 查看信息。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem viewInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.viewInfo(), new InfoSVGGlyph(), action);
    }

    /**
     * 删除视图。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem deleteView(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteView(), new DeleteSVGGlyph(), action);
    }

    /**
     * 克隆视图。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem cloneView(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.cloneView(), new CopySVGGlyph(), action);
    }

    /**
     * 克隆函数。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem cloneFunction(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.cloneFunction(), new CopySVGGlyph(), action);
    }

    /**
     * 克隆过程。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem cloneProcedure(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.cloneProcedure(), new CopySVGGlyph(), action);
    }

    /**
     * 克隆事件。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem cloneEvent(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.cloneEvent(), new CopySVGGlyph(), action);
    }

    /**
     * 新增视图。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem addView(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addView(), new AddSVGGlyph(), action);
    }

    /**
     * 新增查询。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem addQuery(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addQuery(), new AddSVGGlyph(), action);
    }

    /**
     * 设计视图。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem designView(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.designView(), new DesignSVGGlyph(), action);
    }

    /**
     * 刷新数据。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem refreshData(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.refreshData(), new RefreshSVGGlyph(), action);
    }

    /**
     * 恢复历史。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem restoreHistory(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.restoreHistory(), new UndoSVGGlyph(), action);
    }

    /**
     * 查看历史。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem view1History(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.view1History(), new ViewSVGGlyph(), action);
    }

    /**
     * 刷新历史。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem refreshHistory(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.refreshHistory(), new RefreshSVGGlyph(), action);
    }

    /**
     * 刷新桶。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem refreshBucket(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.refreshBucket(), new RefreshSVGGlyph(), action);
    }

    /**
     * 刷新。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem refresh(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.refresh(), new RefreshSVGGlyph(), action);
    }

    /**
     * 刷新文件。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem refreshFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.refreshFile(), new RefreshSVGGlyph(), action);
    }

    /**
     * 重新加载数据。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem reloadData(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.reloadData(), new RefreshSVGGlyph(), action);
    }

    /**
     * 新增表。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem addTable(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addTable(), new AddSVGGlyph(), action);
    }

    /**
     * 新增集合。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem addCollection(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addCollection(), new AddSVGGlyph(), action);
    }

    /**
     * 创建用户。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem createUser(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.createUser(), new AddSVGGlyph(), action);
    }

    /**
     * 打开终端。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem openTerminal(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.openTerminal(), new TerminalSVGGlyph(), action);
    }

    /**
     * 打开过程。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem openProcedure(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.openProcedure(), new ProcedureSVGGlyph(), action);
    }

    /**
     * 设计过程。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem designProcedure(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.designProcedure(), new DesignSVGGlyph(), action);
    }

    /**
     * 设计事件。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem designEvent(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.designEvent(), new DesignSVGGlyph(), action);
    }

    /**
     * 新增过程。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem addProcedure(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addProcedure(), new AddSVGGlyph(), action);
    }

    /**
     * 新增事件。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem addEvent(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addEvent(), new AddSVGGlyph(), action);
    }

    /**
     * 过程信息。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem procedureInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.procedureInfo(), new InfoSVGGlyph(), action);
    }

    /**
     * 事件信息。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem eventInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.eventInfo(), new InfoSVGGlyph(), action);
    }

    /**
     * 新增函数。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem addFunction(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addFunction(), new AddSVGGlyph(), action);
    }

    /**
     * 打开函数。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem openFunction(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.openFunction(), new FunctionSVGGlyph(), action);
    }

    /**
     * 设计函数。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem designFunction(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.designFunction(), new DesignSVGGlyph(), action);
    }

    /**
     * 函数信息。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem functionInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.functionInfo(), new InfoSVGGlyph(), action);
    }

    /**
     * 移动键。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem moveKey(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.moveKey(), new MoveSVGGlyph(), action);
    }

    /**
     * 键过滤。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem keyFilter(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.keyFilter(), new FilterSVGGlyph(), action);
    }

    /**
     * 过滤键。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem filterKey(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.filterKey(), new FilterSVGGlyph(), action);
    }

    /**
     * 导出节点。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem exportNode(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.exportNode(), new ExportSVGGlyph(), action);
    }

    /**
     * 导出键。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem exportKey(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.exportKey(), new ExportSVGGlyph(), action);
    }

    /**
     * 导出键。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem exportKey1(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.exportKey1(), new ExportSVGGlyph(), action);
    }

    /**
     * 导出数据。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem exportData(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.exportData(), new ExportSVGGlyph(), action);
    }

    /**
     * 排序升序。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem sortAsc(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.sortAsc(), new SortAscSVGGlyph(), action);
    }

    /**
     * 排序降序。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem sortDesc(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.sortDesc(), new SortDescSVGGlyph(), action);
    }

    /**
     * 转储数据。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem dumpData(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.dumpData(), new DumpSVGGlyph(), action);
    }

    /**
     * 运行SQL文件。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem runSqlFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.runSqlFile(), new RunSqlFileSVGGlyph(), action);
    }

    /**
     * 运行脚本文件。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem runScriptFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.runScriptFile(), new RunFileSVGGlyph(), action);
    }

    /**
     * 运行。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem run(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.run(), new RunSVGGlyph(), action);
    }

    /**
     * 运行选中。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem runSelected(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.runSelected(), action);
    }

    /**
     * 运行镜像。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem runImage(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.runImage(), new RunSVGGlyph(), action);
    }

    /**
     * 保存镜像。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem saveImage(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.saveImage(), new ImportSVGGlyph(), action);
    }

    /**
     * 更新标签。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem updateTag(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.updateTag(), new EditSVGGlyph(), action);
    }

    /**
     * 保存容器。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem saveContainer(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.saveContainer(), new ExportSVGGlyph(), action);
    }

    /**
     * 导出连接。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem exportConnect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.exportConnect(), new ExportSVGGlyph(), action);
    }

    /**
     * 导入连接。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem importConnect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.importConnect(), new ImportSVGGlyph(), action);
    }

    /**
     * 导入数据。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem importData(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.importData(), new ImportSVGGlyph(), action);
    }

    /**
     * 重命名连接。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem renameConnect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameConnect(), new RenameSVGGlyph(), action);
    }

    /**
     * 重命名。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem rename(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.rename(), new RenameSVGGlyph(), action);
    }

    /**
     * 重命名片段。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem renameSnippet(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameSnippet(), new RenameSVGGlyph(), action);
    }

    /**
     * 重命名分组。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem renameGroup(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameGroup(), new RenameSVGGlyph(), action);
    }

    /**
     * 重命名文件夹。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem renameFolder(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameFolder(), new RenameSVGGlyph(), action);
    }

    /**
     * 重命名文件夹。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem renameFolder1(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameFolder1(), new RenameSVGGlyph(), action);
    }

    /**
     * 重命名节点。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem renameNode(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameNode(), new RenameSVGGlyph(), action);
    }

    /**
     * 重命名文件。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem renameFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameFile(), new RenameSVGGlyph(), action);
    }

    /**
     * 重命名目录。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem renameDir(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameDir(), new RenameSVGGlyph(), action);
    }

    /**
     * 文件权限。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem filePermission(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.filePermission(), new PermissionSVGGlyph(), action);
    }

    /**
     * 克隆节点。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem cloneNode(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.cloneNode(), new RepeatSVGGlyph(), action);
    }

    /**
     * 重命名键。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem renameKey(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameKey(), new RenameSVGGlyph(), action);
    }

    /**
     * 重命名键。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem renameKey1(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameKey1(), new RenameSVGGlyph(), action);
    }

    /**
     * 更新键。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem updateKey1(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.updateKey1(), new EditSVGGlyph(), action);
    }

    /**
     * 更新桶。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem updateBucket(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.updateBucket(), new EditSVGGlyph(), action);
    }

    /**
     * 编辑键。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem editKey1(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.updateKey1(), new EditSVGGlyph(), action);
    }

    /**
     * 编辑连接。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem editConnect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.editConnect(), new EditSVGGlyph(), action);
    }

    /**
     * 编辑。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem edit(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.edit(), new EditSVGGlyph(), action);
    }

    /**
     * 编辑。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem edit_no_graphic(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.edit(), action);
    }

    /**
     * 编辑片段。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem editSnippet(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.editSnippet(), new EditSVGGlyph(), action);
    }

    /**
     * 编辑查询。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem editQuery(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.editQuery(), new EditSVGGlyph(), action);
    }

    /**
     * 编辑文件。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem editFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.editFile(), new EditSVGGlyph(), action);
    }

    /**
     * 编辑文档。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem editDocument(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.editDocument(), new EditDocumentSVGGlyph(), action);
    }

    /**
     * 查看文件。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem view1File(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.view1File(), new ViewSVGGlyph(), action);
    }

    /**
     * 查看文档。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem view1Document(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.view1Document(), new ViewSVGGlyph(), action);
    }

    /**
     * 查看用户。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem view1User(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.view1User(), new ViewSVGGlyph(), action);
    }

    /**
     * 编辑集合。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem editCollections(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.editCollections(), new EditSVGGlyph(), action);
    }

    /**
     * 编辑分组。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem editGroup(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.editGroup(), new EditSVGGlyph(), action);
    }

    /**
     * 编辑行。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem editRow(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.editRow(), new EditSVGGlyph(), action);
    }

    /**
     * 删除键。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem deleteKey(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteKey(), new DeleteSVGGlyph(), action);
    }

    /**
     * 删除键。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem deleteKey1(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteKey1(), new DeleteSVGGlyph(), action);
    }

    /**
     * 复制键集合到主机。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem copyKeys1ToHost(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyKeys1ToHost(), new CopySVGGlyph(), action);
    }

    /**
     * 复制到主机。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem copyToHost(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyToHost(), new CopySVGGlyph(), action);
    }

    /**
     * 删除数据。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem deleteData(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteData(), new DeleteSVGGlyph(), action);
    }

    /**
     * 删除授权。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem deleteAuth(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteAuth(), new DeleteSVGGlyph(), action);
    }

    /**
     * 删除文件。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem deleteFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteFile(), new DeleteSVGGlyph(), action);
    }

    /**
     * 删除文档。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem deleteDocument(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteDocument(), new DeleteSVGGlyph(), action);
    }

    /**
     * 删除目录。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem deleteDir(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteDir(), new DeleteSVGGlyph(), action);
    }

    /**
     * 删除容器。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem deleteContainer(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteContainer(), new DeleteSVGGlyph(), action);
    }

    /**
     * 容器日志。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem containerLogs(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.containerLogs(), new LogSVGGlyph(), action);
    }

    /**
     * 重命名容器。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem renameContainer(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameContainer(), new EditSVGGlyph(), action);
    }

    /**
     * 删除镜像。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem deleteImage(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteImage(), new DeleteSVGGlyph(), action);
    }

    /**
     * 启动容器。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem start1Container(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.start1Container(), new RunSVGGlyph(), action);
    }

    /**
     * 停止容器。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem stop1Container(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.stop1Container(), new StopSVGGlyph(), action);
    }

    /**
     * 终止容器。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem killContainer(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.killContainer(), new KillSVGGlyph(), action);
    }

    /**
     * 终止进程。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem killProcess(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.killProcess(), new KillSVGGlyph(), action);
    }

    /**
     * 强制终止进程。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem forceKillProcess(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.forceKillProcess(), new ForceKillSVGGlyph(), action);
    }

    /**
     * 重启容器。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem restartContainer(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.restartContainer(), new RestartSVGGlyph(), action);
    }

    /**
     * 暂停容器。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem pauseContainer(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.pauseContainer(), new PauseSVGGlyph(), action);
    }

    /**
     * 恢复容器。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem unpauseContainer(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.unpauseContainer(), new UnPauseSVGGlyph(), action);
    }

    /**
     * 强制删除容器。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem forceDeleteContainer(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.forceDeleteContainer(), new DeleteForceSVGGlyph(), action);
    }

    /**
     * 强制删除镜像。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem forceDeleteImage(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.forceDeleteImage(), new DeleteForceSVGGlyph(), action);
    }

    /**
     * 删除分组。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem deleteGroup(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteGroup(), new DeleteSVGGlyph(), action);
    }

    /**
     * 删除文件夹。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem deleteFolder(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteFolder(), new DeleteSVGGlyph(), action);
    }

    /**
     * 删除文件夹。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem deleteFolder1(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteFolder1(), new DeleteSVGGlyph(), action);
    }

    /**
     * 删除节点。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem deleteNode(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteNode(), new DeleteSVGGlyph(), action);
    }

    /**
     * 删除连接。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem deleteConnect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteConnect(), new DeleteSVGGlyph(), action);
    }

    /**
     * 删除。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem delete(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.delete(), new DeleteSVGGlyph(), action);
    }

    /**
     * 删除历史。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem deleteHistory(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteHistory(), new DeleteSVGGlyph(), action);
    }

    /**
     * 删除片段。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem deleteSnippet(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteSnippet(), new DeleteSVGGlyph(), action);
    }

    /**
     * 删除模式。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem deleteSchema(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteSchema(), new DeleteSVGGlyph(), action);
    }

    /**
     * 删除桶。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem deleteBucket(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteBucket(), new DeleteSVGGlyph(), action);
    }

    /**
     * 强制删除桶。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem forceDeleteBucket(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.forceDeleteBucket(), new DeleteSVGGlyph(), action);
    }

    /**
     * 关闭连接。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem closeConnect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.closeConnect(), new CloseSVGGlyph(), action);
    }

    /**
     * 新增分组。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem addGroup(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addGroup(), new AddGroupSVGGlyph(), action);
    }

    /**
     * 新增文件夹。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem addFolder(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addFolder(), new AddGroupSVGGlyph(), action);
    }

    /**
     * 新增文件夹。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem addFolder1(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addFolder1(), new AddGroupSVGGlyph(), action);
    }

    /**
     * 新增连接。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem addConnect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addConnect(), new AddSVGGlyph(), action);
    }

    /**
     * 新增。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem add(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.add(), new AddSVGGlyph(), action);
    }

    /**
     * 新增片段。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem addSnippet(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addSnippet(), new AddSVGGlyph(), action);
    }

    /**
     * 新增桶。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem addBucket(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addBucket(), new AddSVGGlyph(), action);
    }

    /**
     * 新增集合。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem addCollections(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addCollections(), new AddSVGGlyph(), action);
    }

    /**
     * 收藏文件。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem collectFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.collectFile(), new CollectSVGGlyph(), action);
    }

    /**
     * 收藏键。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem collectKey(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.collectKey(), new CollectSVGGlyph(), action);
    }

    /**
     * 取消收藏。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem unCollect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.unCollect(), new UnCollectSVGGlyph(), action);
    }

    /**
     * 取消收藏键。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem unCollectKey(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.unCollectKey(), new UnCollectSVGGlyph(), action);
    }

    /**
     * 新增键。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem addKey(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addKey(), new AddSVGGlyph(), action);
    }

    /**
     * 新增节点。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem addNode(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addNode(), new AddSVGGlyph(), action);
    }

    /**
     * 传输数据。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem transportData(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.transportData(), new TransportSVGGlyph(), action);
    }

    /**
     * 传输文件。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem transportFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.transportFile(), new TransportSVGGlyph(), action);
    }

    /**
     * 传输键。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem transportKey(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.transportKey(), new TransportSVGGlyph(), action);
    }

    /**
     * 传输节点。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem transportNode(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.transportNode(), new TransportSVGGlyph(), action);
    }

    /**
     * 重新连接。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem repeatConnect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.repeatConnect(), new RepeatSVGGlyph(), action);
    }

    /**
     * 克隆连接。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem cloneConnect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.cloneConnect(), new RepeatSVGGlyph(), action);
    }

    /**
     * 复制连接。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem copyConnect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyConnect(), new CopySVGGlyph(), action);
    }

    /**
     * 复制信息。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem copyInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyInfo(), new CopySVGGlyph(), action);
    }

    /**
     * 克隆会话。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem cloneSession(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.cloneSession(), action);
    }

    /**
     * 复制当前会话。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem copyThisSession(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyThisSession(), action);
    }

    /**
     * 重新加载。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem reload(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.reload(), new RefreshSVGGlyph(), action);
    }

    /**
     * 重新加载数据库。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem reloadDatabase(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.reloadDatabase(), new RefreshSVGGlyph(), action);
    }

    /**
     * 重新加载模式。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem reloadSchema(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.reloadSchema(), new RefreshSVGGlyph(), action);
    }

    /**
     * 服务信息。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem serverInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.serverInfo(), new InfoSVGGlyph(), action);
    }

    /**
     * 启动连接。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem startConnect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.startConnect(), new PlaySVGGlyph(), action);
    }

    /**
     * 打开连接。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem openConnect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.openConnect(), new OpenSVGGlyph(), action);
    }

    /**
     * 打开SFTP。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem openSFTP(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.openSFTP(), new SFTPSVGGlyph(), action);
    }

    /**
     * 清空数据。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem clearData(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.clearData(), new ClearSVGGlyph(), action);
    }

    /**
     * 复制键。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem copyKey(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyKey(), new CopySVGGlyph(), action);
    }

    /**
     * 复制文件路径。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem copyFilePath(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyFilePath(), new CopySVGGlyph(), action);
    }

    /**
     * 复制节点路径。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem copyNodePath(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyNodePath(), new CopySVGGlyph(), action);
    }

    /**
     * 复制授权。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem copyAuth(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyAuth(), new CopySVGGlyph(), action);
    }

    /**
     * 授权节点。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem authNode(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.authNode(), new UnLockSVGGlyph(), action);
    }

    /**
     * 卸载。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem unload(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.unload(), new StopSVGGlyph(), action);
    }

    /**
     * 加载全部。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem loadAll(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.loadAll(), new LoadAllSVGGlyph(), action);
    }

    /**
     * 展开全部。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem expandAll(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.expandAll(), new ExpandAllSVGGlyph(), action);
    }

    /**
     * 折叠全部。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem collapseAll(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.collapseAll(), new CollapseAllSVGGlyph(), action);
    }

    /**
     * 删除函数。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem deleteFunction(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteFunction(), new DeleteSVGGlyph(), action);
    }

    /**
     * 删除过程。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem deleteProcedure(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteProcedure(), new DeleteSVGGlyph(), action);
    }

    /**
     * 删除事件。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem deleteEvent(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteEvent(), new DeleteSVGGlyph(), action);
    }

    /**
     * 打开表。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem openTable(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.openTable(), new OpenSVGGlyph(), action);
    }

    /**
     * 打开集合。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem openCollection(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.openCollection(), new OpenSVGGlyph(), action);
    }

    /**
     * 打开桶。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem openBucket(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.openBucket(), new OpenSVGGlyph(), action);
    }

    /**
     * 编辑表。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem editTable(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.editTable(), new EditSVGGlyph(), action);
    }

    /**
     * 设计表。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem designTable(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.designTable(), new DesignSVGGlyph(), action);
    }

    /**
     * 重命名表。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem renameTable(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameTable(), new RenameSVGGlyph(), action);
    }

    /**
     * 重命名集合。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem renameCollection(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameCollection(), new RenameSVGGlyph(), action);
    }

    /**
     * 重命名视图。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem renameView(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameView(), new RenameSVGGlyph(), action);
    }

    /**
     * 重命名事件。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem renameEvent(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameEvent(), new RenameSVGGlyph(), action);
    }

    /**
     * 重命名函数。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem renameFunction(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameFunction(), new RenameSVGGlyph(), action);
    }

    /**
     * 重命名过程。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem renameProcedure(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameProcedure(), new RenameSVGGlyph(), action);
    }

    /**
     * 重命名查询。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem renameQuery(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.renameQuery(), new RenameSVGGlyph(), action);
    }

    /**
     * 清空表。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem clearTable(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.clearTable(), new ClearSVGGlyph(), action);
    }

    /**
     * 清空表数据。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem clearTableData(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.clearTableData(), new ClearSVGGlyph(), action);
    }

    /**
     * 清空集合数据。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem clearCollectionData(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.clearCollectionData(), new ClearSVGGlyph(), action);
    }

    /**
     * 清空集合。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem clearCollection(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.clearCollection(), new ClearSVGGlyph(), action);
    }

    /**
     * 清空桶。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem clearBucket(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.clearBucket(), new ClearSVGGlyph(), action);
    }

    /**
     * 清空表。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem truncateTable(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.truncateTable(), new TruncateSVGGlyph(), action);
    }

    /**
     * 删除表。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem deleteTable(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteTable(), new DeleteSVGGlyph(), action);
    }

    /**
     * 删除集合。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem deleteCollection(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteCollection(), new DeleteSVGGlyph(), action);
    }

    /**
     * 删除用户。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem deleteUser(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteUser(), new DeleteSVGGlyph(), action);
    }

    /**
     * 删除查询。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem deleteQuery(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteQuery(), new DeleteSVGGlyph(), action);
    }

    /**
     * 表信息。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem tableInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.tableInfo(), new InfoSVGGlyph(), action);
    }

    /**
     * 复制。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem copy(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copy(), new CopySVGGlyph(), action);
    }

    /**
     * 复制。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem copy_no_graphic(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copy(), action);
    }

    /**
     * 复制为插入语句。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem copyAsInsertStatement_no_graphic(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyAsInsertStatement(), action);
    }

    /**
     * 复制为插入脚本。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem copyAsInsertScript_no_graphic(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyAsInsertScript(), action);
    }

    /**
     * 复制为更新语句。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem copyAsUpdateStatement_no_graphic(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyAsUpdateStatement(), action);
    }

    /**
     * 复制为更新脚本。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem copyAsUpdateScript_no_graphic(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyAsUpdateScript(), action);
    }

    /**
     * 复制文件。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem copyFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyFile(), new CopySVGGlyph(), action);
    }

    /**
     * 剪切文件。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem cutFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.cutFile(), new CutSVGGlyph(), action);
    }

    /**
     * 粘贴。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem paste_no_graphic(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.paste(), action);
    }

    /**
     * 粘贴文件。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem pasteFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.pasteFile(), new PasteSVGGlyph(), action);
    }

    /**
     * 取消压缩。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem unCompress(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.unCompress(), new UnCompressSVGGlyph(), action);
    }

    /**
     * 压缩。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem compress(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.unCompress(), new UnCompressSVGGlyph(), action);
    }

    /**
     * 删除记录。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem deleteRecord(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteRecord(), action);
    }

    /**
     * 设置到空。
     *
     * @param action 执行动作
     * @return 到空
     */
    public static FXMenuItem setToNull_no_graphic(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.setToNull(), action);
    }

    /**
     * 设置到空字符串。
     *
     * @param action 执行动作
     * @return 到空字符串
     */
    public static FXMenuItem setToEmptyString_no_graphic(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.setToEmptyString(), action);
    }

    /**
     * 文件信息。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem fileInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.fileInfo(), new InfoSVGGlyph(), action);
    }

    /**
     * 镜像信息。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem imageInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.imageInfo(), new InfoSVGGlyph(), action);
    }

    /**
     * 镜像检查。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem imageInspect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.imageInspect(), new InfoSVGGlyph(), action);
    }

    /**
     * 镜像历史。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem imageHistory(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.imageHistory(), new HistorySVGGlyph(), action);
    }

    /**
     * 数据历史。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem dataHistory(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.dataHistory(), new HistorySVGGlyph(), action);
    }

    /**
     * 容器信息。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem containerInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.containerInfo(), new InfoSVGGlyph(), action);
    }

    /**
     * 容器检查。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem containerInspect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.containerInspect(), new InfoSVGGlyph(), action);
    }

    /**
     * 容器资源。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem containerResource(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.containerResource(), new ResourceSVGGlyph(), action);
    }

    /**
     * 容器端口集合。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem containerPorts(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.containerPorts(), new PortSVGGlyph(), action);
    }

    /**
     * 字段信息。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem fieldInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.fieldInfo(), action);
    }

    /**
     * 列信息。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem columnInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.columnInfo(), action);
    }

    /**
     * 复制列名称。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem copyColumnName(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.copyColumnName(), action);
    }

    /**
     * 关闭数据库。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem closeDatabase(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.closeDatabase(), new CloseSVGGlyph(), action);
    }

    /**
     * 关闭模式。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem closeSchema(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.closeSchema(), new CloseSVGGlyph(), action);
    }

    /**
     * 编辑数据库。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem editDatabase(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.editDatabase(), new EditSVGGlyph(), action);
    }

    /**
     * 编辑模式。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem editSchema(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.editSchema(), new EditSVGGlyph(), action);
    }

    /**
     * 删除数据库。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem deleteDatabase(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.deleteDatabase(), new DeleteSVGGlyph(), action);
    }

    /**
     * 删除模式。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem dropSchema(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.dropSchema(), new DeleteSVGGlyph(), action);
    }

    /**
     * 数据库信息。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem databaseInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.databaseInfo(), new InfoSVGGlyph(), action);
    }

    /**
     * 新增数据库。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem addDatabase(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addDatabase(), new AddSVGGlyph(), action);
    }

    /**
     * 新增模式。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem addSchema(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.addSchema(), new AddSVGGlyph(), action);
    }

    /**
     * 关闭全部标签页。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem closeAllTab(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nResourceBundle.i18nString("base.closeAllTab"), action);
    }

    /**
     * 关闭当前标签页。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem closeCurrTab(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nResourceBundle.i18nString("base.closeCurrTab"), action);
    }

    /**
     * 关闭左标签页。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem closeLeftTab(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nResourceBundle.i18nString("base.closeLeftTab"), action);
    }

    /**
     * 关闭右标签页。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem closeRightTab(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nResourceBundle.i18nString("base.closeRightTab"), action);
    }

    /**
     * 关闭其它标签页。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem closeOtherTab(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nResourceBundle.i18nString("base.closeOtherTab"), action);
    }

    /**
     * 关闭其它连接标签页。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem closeOtherConnectTab(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nResourceBundle.i18nString("base.closeOtherConnectTab"), action);
    }

    /**
     * 取消。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem cancel(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.cancel(), new CancelSVGGlyph(), action);
    }

    /**
     * 取消连接。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem cancelConnect(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.cancelConnect(), new CancelSVGGlyph(), action);
    }

    /**
     * 取消下载。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem cancelDownload(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.cancelDownload(), new CancelSVGGlyph(), action);
    }

    /**
     * 取消上传。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem cancelUpload(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.cancelUpload(), new CancelSVGGlyph(), action);
    }

    /**
     * 取消传输。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem cancelTransport(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.cancelTransport(), new CancelSVGGlyph(), action);
    }

    /**
     * 重试。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem retry(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.retry(), new RestartSVGGlyph(), action);
    }

    /**
     * 移除传输。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem removeTransport(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.removeTransport(), new DeleteSVGGlyph(), action);
    }

    /**
     * 移除下载。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem removeDownload(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.removeDownload(), new DeleteSVGGlyph(), action);
    }

    /**
     * 移除上传。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem removeUpload(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.removeUpload(), new DeleteSVGGlyph(), action);
    }

    /**
     * 取消操作。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem cancelOperation(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.cancelOperation(), new CancelSVGGlyph(), action);
    }

    /**
     * 批量操作。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem batchOpt(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.batchOpt(), new BatchOptSVGGlyph(), action);
    }

    /**
     * 更新TTL。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem updateTtl(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.updateTtl(), new TimeSVGGlyph(), action);
    }

    /**
     * 打开数据。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem openData(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.openData(), new OpenSVGGlyph(), action);
    }

    /**
     * 打开服务。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem openServer(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.openServer(), new OpenSVGGlyph(), action);
    }

    /**
     * 打开信息。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem openInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.openInfo(), new OpenSVGGlyph(), action);
    }

    /**
     * 下载文件。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem downloadFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.downloadFile(), new DownloadSVGGlyph(), action);
    }

    /**
     * 共享文件。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem shareFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.shareFile(), new OpenSVGGlyph(), action);
    }

    /**
     * 上传文件。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem uploadFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.uploadFile(), new ExportSVGGlyph(), action);
    }

    /**
     * 上传文件夹。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem uploadFolder(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.uploadFolder(), new ExportSVGGlyph(), action);
    }

    /**
     * 新建文件。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem touchFile(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.touchFile(), new FileSVGGlyph(), action);
    }

    /**
     * 错误信息。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem errorInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.errorInfo(), new ErrorInfoSVGGlyph(), action);
    }

    /**
     * 更多信息。
     *
     * @param action 执行动作
     * @return 菜单项
     */
    public static FXMenuItem moreInfo(Runnable action) {
        return (FXMenuItem) MenuItemManager.getMenuItem(I18nHelper.moreInfo(), new MoreSVGGlyph(), action);
    }
}



