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
 * JavaFX replacement for ClipboardControllerImpl.java.
 * Uses javafx.scene.input.Clipboard instead of java.awt.datatransfer.Clipboard.
 */
public class VncClipboardHandler implements ClipboardController, Destroyable {

    private static final String STANDARD_CHARSET = "ISO-8859-1";
    private static final long CLIPBOARD_UPDATE_CHECK_INTERVAL_MILLIS = 1000L;

    private String clipboardText;
    private volatile boolean isRunning;
    private boolean isEnabled;
    private final Protocol protocol;
    private Charset charset;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> pollingTask;

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
