// Copyright (C) 2010 - 2014 GlavSoft LLC.
// All rights reserved.
//
// -----------------------------------------------------------------------
// This file is part of the TightVNC software.  Please visit our Web site:
//
//                       http://www.tightvnc.com/
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
// -----------------------------------------------------------------------
//
package cn.oyzh.fx.vnc;

import cn.oyzh.common.object.Destroyable;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import com.glavsoft.core.SettingsChangedEvent;
import com.glavsoft.rfb.ClipboardController;
import com.glavsoft.rfb.client.ClientCutTextMessage;
import com.glavsoft.rfb.protocol.Protocol;
import com.glavsoft.rfb.protocol.ProtocolSettings;
import com.glavsoft.utils.Strings;

import java.nio.charset.Charset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * VNC 剪贴板控制器，负责本地系统剪贴板与远端剪贴板之间的文本同步。
 * 使用 JavaFX 剪贴板实现，替代原 TightVNC 中基于 AWT 的实现。
 *
 * @author oyzh
 * @since 2026-07-18
 */
public class VncClipboardHandler implements ClipboardController, Destroyable {

    /** 标准字符集，多字节字符集不支持时回退使用 */
    private static final String STANDARD_CHARSET = "ISO-8859-1";

    /** 剪贴板内容变化轮询检查间隔（毫秒） */
    private static final long CLIPBOARD_UPDATE_CHECK_INTERVAL_MILLIS = 1000L;

    /** 上一次读取到的剪贴板文本 */
    private String clipboardText;

    /** 轮询任务是否处于运行状态 */
    private volatile boolean isRunning;

    /** 是否启用剪贴板同步 */
    private boolean isEnabled;

    /** VNC 协议对象，用于向远端发送剪贴板消息 */
    private final Protocol protocol;

    /** 文本编解码字符集 */
    private Charset charset;

    /** 剪贴板轮询调度线程池 */
    private ScheduledExecutorService scheduler;

    /** 剪贴板轮询任务句柄 */
    private ScheduledFuture<?> pollingTask;

    /**
     * 构造剪贴板控制器。
     *
     * @param protocol    VNC 协议对象
     * @param charsetName 字符集名称，为空时使用系统默认字符集
     */
    public VncClipboardHandler(Protocol protocol, String charsetName) {
        this.protocol = protocol;
        this.clipboardText = null;
        this.isEnabled = false;

        if (Strings.isTrimmedEmpty(charsetName)) {
            charset = Charset.defaultCharset();
        } else if ("standard".equalsIgnoreCase(charsetName)) {
            charset = Charset.forName(STANDARD_CHARSET);
        } else {
            charset = Charset.isSupported(charsetName) ? Charset.forName(charsetName) : Charset.defaultCharset();
        }
        // Not supported UTF-charsets as they are multibytes
        if (charset.name().startsWith("UTF")) {
            charset = Charset.forName(STANDARD_CHARSET);
        }
    }

    @Override
    public void updateSystemClipboard(byte[] bytes) {
        if (isEnabled) {
            ClipboardUtil.setString(new String(bytes, charset));
            //FXUtil.runLater(() -> StringSelectionHelper.setClipboardText(new String(bytes, charset)));
        }
    }

    @Override
    public String getClipboardText() {
        return clipboardText;
    }

    @Override
    public String getRenewedClipboardText() {
        String old = clipboardText;
        updateSavedClipboardContent();
        if (clipboardText != null && !clipboardText.equals(old)) {
            return clipboardText;
        }
        return null;
    }

    /**
     * 更新本地缓存的剪贴板内容，读取动作实际由轮询任务在 JavaFX 线程中完成。
     */
    private void updateSavedClipboardContent() {
        try {
            // Must run on JavaFX thread for clipboard access
            // Use a synchronous approach via Platform.runLater with a latch would be complex
            // Instead, we read clipboard in the polling task which uses Platform.runLater
        } catch (Exception e) {
            clipboardText = null;
        }
    }

    @Override
    public void setEnabled(boolean enable) {
        if (!enable) {
            isRunning = false;
            stopPolling();
        }
        if (enable && !isEnabled) {
            startPolling();
        }
        isEnabled = enable;
    }

    /**
     * 启动剪贴板变化轮询任务，检测到变化时向远端发送剪贴板消息。
     */
    private void startPolling() {
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "VncClipboardHandler");
                t.setDaemon(true);
                return t;
            });
        }

        isRunning = true;
        pollingTask = scheduler.scheduleWithFixedDelay(() -> {
            if (!isRunning) {
                return;
            }
            FXUtil.runLater(() -> {
                try {
                    javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                    if (clipboard.hasString()) {
                        String text = clipboard.getString();
                        if (text != null && !text.equals(clipboardText)) {
                            clipboardText = text;
                            protocol.sendMessage(new ClientCutTextMessage(clipboardText, charset));
                        }
                    }
                } catch (Exception e) {
                    // Clipboard access may throw if another app holds the clipboard
                }
            });
        }, 0, CLIPBOARD_UPDATE_CHECK_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
    }

    /**
     * 取消轮询任务并关闭调度线程池。
     */
    private void stopPolling() {
        if (pollingTask != null) {
            pollingTask.cancel(false);
            pollingTask = null;
        }
        if (scheduler != null) {
            scheduler.shutdown();
            scheduler = null;
        }
    }

    @Override
    public void settingsChanged(SettingsChangedEvent e) {
        ProtocolSettings settings = (ProtocolSettings) e.getSource();
        setEnabled(settings.isAllowClipboardTransfer());
    }

    @Override
    public void destroy() {
        this.setEnabled(false);
    }

    ///**
    // * Helper to set clipboard text. Uses Platform.runLater since clipboard access requires FX thread.
    // */
    //private static class StringSelectionHelper {
    //    static void setClipboardText(String text) {
    //        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
    //        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
    //        content.putString(text);
    //        clipboard.setContent(content);
    //    }
    //}
}
