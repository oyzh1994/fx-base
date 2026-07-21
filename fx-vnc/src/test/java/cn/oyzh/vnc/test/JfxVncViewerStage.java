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
package cn.oyzh.vnc.test;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.vnc.VncClipboardHandler;
import cn.oyzh.fx.vnc.VncFramebufferView;
import com.glavsoft.rfb.ClipboardController;
import com.glavsoft.rfb.IRfbSessionListener;
import com.glavsoft.rfb.encoding.EncodingType;
import com.glavsoft.rfb.protocol.Protocol;
import com.glavsoft.rfb.protocol.ProtocolSettings;
import com.glavsoft.transport.BaudrateMeter;
import com.glavsoft.transport.Transport;
import com.glavsoft.viewer.settings.LocalMouseCursorShape;
import com.glavsoft.viewer.settings.UiSettings;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main VNC viewer window.
 * Replaces VNCViewer.kt from the Termora plugin.
 * Manages the connection lifecycle, framebuffer display, and toolbar.
 */
public class JfxVncViewerStage implements IRfbSessionListener {

    private final ConnectionConfig config;
    private final Stage stage;
    private final StackPane rootPane;
    private final ScrollPane scrollPane;
    private VncFramebufferView framebufferView;
    //private JfxToolbarOverlay toolbar;
    private Protocol protocol;
    private UiSettings uiSettings;
    private ClipboardController clipboardHandler;
    private Socket socket;
    private Transport transport;
    private ExecutorService executorService;

    public JfxVncViewerStage(Stage stage, ConnectionConfig config) {
        this.stage = stage;
        this.config = config;

        rootPane = new StackPane();
        rootPane.setStyle("-fx-background-color: black;");

        scrollPane = new ScrollPane();
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-background-color: black; -fx-border-color: transparent; -fx-padding: 0;");
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);
        rootPane.getChildren().add(scrollPane);

        Scene scene = new Scene(rootPane, 800, 600, Color.BLACK);
        stage.setScene(scene);
        stage.setTitle("VNC - " + config.getHost() + ":" + config.getPort());
        stage.setOnCloseRequest(e -> dispose());

        executorService = Executors.newVirtualThreadPerTaskExecutor();

        connectAsync();
    }

    private void connectAsync() {
        CompletableFuture.runAsync(this::connect, executorService)
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    MessageBox.exception(ex);
                    return null;
                });
    }

    private void connect() {
        try {
            // Create socket with optional proxy
            Proxy proxy = Proxy.NO_PROXY;
            if (config.getProxyType() != Proxy.Type.DIRECT
                    && !config.getProxyHost().isEmpty()) {
                proxy = new Proxy(config.getProxyType(),
                        new InetSocketAddress(config.getProxyHost(), config.getProxyPort()));
            }

            socket = new Socket(proxy);
            socket.setKeepAlive(true);
            socket.setTcpNoDelay(true);
            socket.connect(new InetSocketAddress(config.getHost(), config.getPort()), 5000);

            // Setup transport
            transport = new Transport(socket);
            transport.setBaudrateMeter(new BaudrateMeter());

            // Setup settings
            uiSettings = new UiSettings();
            ProtocolSettings protocolSettings = ProtocolSettings.getDefaultSettings();
            protocolSettings.setPreferredEncoding(EncodingType.ZRLE);

            // Create protocol
            protocol = new Protocol(transport,
                    () -> config.getPassword() != null ? config.getPassword() : "",
                    protocolSettings);

            // Create framebuffer view (must happen before startNormalHandling)
            Platform.runLater(() -> {
                framebufferView = new VncFramebufferView(protocol, uiSettings.getScaleFactor(),
                        LocalMouseCursorShape.NO_CURSOR);

                uiSettings.addListener(framebufferView);
                protocolSettings.addListener(framebufferView);

                // Replace scroll content
                scrollPane.setContent(framebufferView);

                stage.sizeToScene();
            });

            // Wait for UI setup
            Thread.sleep(100);

            // Setup clipboard
            String encoding = StringUtil.blankToDefault(config.getEncoding(), "ISO-8859-1");
            clipboardHandler = new VncClipboardHandler(protocol, encoding);
            protocolSettings.addListener(clipboardHandler);

            // Handshake and start
            protocol.handshake();
            protocol.startNormalHandling(this, framebufferView, clipboardHandler);

            // Start clipboard polling
            clipboardHandler.setEnabled(true);

            // Update stage title with remote desktop name
            String desktopName = protocol.getRemoteDesktopName();
            if (desktopName != null && !desktopName.isEmpty()) {
                Platform.runLater(() -> stage.setTitle(desktopName + " - VNC"));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void rfbSessionStopped(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Disconnected");
            alert.setHeaderText("VNC session ended");
            alert.setContentText(message != null ? message : "Connection closed");
            alert.showAndWait();
            stage.close();
        });
    }

    public void dispose() {
        if (clipboardHandler != null) {
            clipboardHandler.setEnabled(false);
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // ignore
        }

        if (protocol != null) {
            protocol.cleanUpSession();
        }

        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }

    public Stage getStage() {
        return stage;
    }
}
