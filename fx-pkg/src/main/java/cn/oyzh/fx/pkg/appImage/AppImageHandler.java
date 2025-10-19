package cn.oyzh.fx.pkg.appImage;

import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.system.RuntimeUtil;
import cn.oyzh.common.thread.ProcessExecResult;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PostHandler;
import cn.oyzh.fx.pkg.config.PackConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * github处理器
 *
 * @author oyzh
 * @since 2025/09/22
 */
public class AppImageHandler implements PostHandler {

    private int order = PackOrder.ORDER_M7;

    @Override
    public int order() {
        return order;
    }

    @Override
    public void order(int order) {
        this.order = order;
    }

    @Override
    public String name() {
        return "AppImage处理";
    }

    @Override
    public void handle(PackConfig packConfig) throws Exception {
        this.copyAppIcon(packConfig);
        this.initDesktop(packConfig);
        this.initAppRun(packConfig);
        String pDir = new File(packConfig.getDest()).getParent();
        List<String> cmdList = new ArrayList<>();
        cmdList.add("appimagetool");
        cmdList.add(packConfig.getDest());
        cmdList.add(pDir + "/" + packConfig.getAppName() + ".AppImage");
        if (StringUtil.isNotBlank(packConfig.getAppImageRuntime())) {
            cmdList.add("--runtime-file");
            if (OSUtil.isAarch64()) {
                cmdList.add(FileNameUtil.concat(packConfig.getAppImageRuntime(), "runtime-aarch64"));
            } else if (OSUtil.isX64()) {
                cmdList.add(FileNameUtil.concat(packConfig.getAppImageRuntime(), "runtime-x86_64"));
            }
        }
        String[] cmdArr = ArrayUtil.toArray(cmdList, String.class);
        String cmdStr = StringUtil.join(" ", cmdArr);
        JulLog.info("AppImage cmd:{}", "\n" + cmdStr);
        ProcessExecResult result = RuntimeUtil.execForResult(cmdArr);
        JulLog.info("AppImage result:{}", result);
        if (!result.isSuccess()) {
            JulLog.error("AppImage error:{}", result.getError());
            throw new Exception("AppImage error:" + result.getError());
        }
    }

    /**
     * 复制app图标
     *
     * @param packConfig 打包配置
     * @throws Exception 异常
     */
    private void copyAppIcon(PackConfig packConfig) throws Exception {
        File icon = new File(packConfig.getAppIcon());
        String extName = FileNameUtil.extName(packConfig.getAppIcon());
        File iconNew = new File(packConfig.getDest(), packConfig.getAppName() + "." + extName);
        FileUtil.copy(icon, iconNew);
    }

    /**
     * 初始化Desktop文件
     *
     * @param packConfig 打包配置
     */
    private void initDesktop(PackConfig packConfig) {
        File desktop = new File(packConfig.getDest(), packConfig.getAppName() + ".desktop");
        List<String> lines = new ArrayList<>();
        lines.add("[Desktop Entry]");
        lines.add("Terminal=false");
        lines.add("Type=Application");
        lines.add("Categories=Development;");
        lines.add("Name=" + packConfig.getAppName());
        lines.add("Icon=" + packConfig.getAppName());
        lines.add("Comment=" + packConfig.getjPackageConfig().getDescription());
        FileUtil.writeUtf8Lines(lines, desktop);
    }

    /**
     * 初始化AppRun脚本
     *
     * @param packConfig 打包配置
     */
    private void initAppRun(PackConfig packConfig) {
        File dir = new File(packConfig.getDest());
        File file = new File(packConfig.getDest(), "AppRun");
        List<String> lines = new ArrayList<>();
        lines.add("#!/bin/bash");
        lines.add("APPDIR=$(dirname \"$0\")");
        lines.add("export LinuxAppImage=true");
        lines.add("exec \"$APPDIR/" + packConfig.getAppName() + "/bin/" + packConfig.getAppName() + "\" \"$@\"");
        FileUtil.writeUtf8Lines(lines, file);
        // 设置权限
        RuntimeUtil.exec("chmod +x AppRun", null, dir);
    }

}
