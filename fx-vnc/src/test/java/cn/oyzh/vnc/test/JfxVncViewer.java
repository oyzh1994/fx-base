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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Main entry point for the JavaFX VNC Viewer application.
 * Shows the connection dialog and creates a viewer stage on successful connect.
 */
public class JfxVncViewer extends Application {

    private static final String APP_NAME = "JfxVNC Viewer";

    @Override
    public void start(Stage primaryStage) {
        // Parse command-line arguments for quick connect
        Parameters params = getParameters();
        ConnectionConfig defaultConfig = parseArgs(params);

        if (defaultConfig != null && !defaultConfig.getHost().isEmpty()) {
            // Quick connect from command line
            openViewer(primaryStage, defaultConfig);
        } else {
            // Show connection dialog
            showConnectionDialog(primaryStage, null);
        }
    }

    private void showConnectionDialog(Stage owner, ConnectionConfig defaultConfig) {
        JfxConnectionDialog dialog = new JfxConnectionDialog(owner, defaultConfig);
        ConnectionConfig config = dialog.showAndWait();
        if (config != null && !config.getHost().isEmpty()) {
            openViewer(new Stage(), config);
        }
    }

    private void openViewer(Stage viewerStage, ConnectionConfig config) {
        JfxVncViewerStage viewer = new JfxVncViewerStage(viewerStage, config);
        viewerStage.show();
    }

    private ConnectionConfig parseArgs(Parameters params) {
        ConnectionConfig config = new ConnectionConfig();

        for (String arg : params.getRaw()) {
            if (arg.startsWith("--host=")) {
                config.setHost(arg.substring(7));
            } else if (arg.startsWith("--port=")) {
                try {
                    config.setPort(Integer.parseInt(arg.substring(7)));
                } catch (NumberFormatException e) {
                    // use default
                }
            } else if (arg.startsWith("--password=")) {
                config.setPassword(arg.substring(11));
            } else if (arg.startsWith("--encoding=")) {
                config.setEncoding(arg.substring(11));
            } else if (!arg.startsWith("--")) {
                // Positional argument: treat as host:port or host::port
                String hostPort = arg;
                if (hostPort.contains(":")) {
                    String[] parts = hostPort.split(":");
                    config.setHost(parts[0]);
                    if (parts.length > 1 && !parts[1].isEmpty()) {
                        try {
                            config.setPort(Integer.parseInt(parts[1]));
                        } catch (NumberFormatException e) {
                            // use default
                        }
                    }
                } else {
                    config.setHost(hostPort);
                }
            }
        }

        return config;
    }

    @Override
    public void stop() {
        Platform.exit();
    }

    public static void main(String[] args) {
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.allowhidpi", "true");
        launch(args);
    }

    public static class JfxVncViewerStarter {
        public static void main(String[] args) {
            args = new String[]{
                    "--host=localhost",
                    "--port=5900",
                    "--password=123456",
            };
            JfxVncViewer.main(args);
        }
    }
}
