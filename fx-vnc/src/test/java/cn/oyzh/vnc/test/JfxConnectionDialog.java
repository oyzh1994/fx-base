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

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Window;

import java.net.Proxy;

/**
 * Connection dialog for entering VNC server connection parameters.
 * Replaces VNCHostOptionsPane.kt from the Termora plugin.
 */
public class JfxConnectionDialog {

    private TextField hostField;
    private Spinner<Integer> portSpinner;
    private PasswordField passwordField;
    private ComboBox<String> encodingCombo;
    private ComboBox<String> proxyTypeCombo;
    private TextField proxyHostField;
    private Spinner<Integer> proxyPortSpinner;
    private PasswordField proxyPasswordField;
    private Dialog<ConnectionConfig> dialog;

    public JfxConnectionDialog(Window owner, ConnectionConfig defaultConfig) {
        buildDialog(owner, defaultConfig);
    }

    private void buildDialog(Window owner, ConnectionConfig defaultConfig) {
        dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("VNC Connection");
        dialog.setHeaderText("Enter VNC server connection details");
        dialog.setResizable(true);

        // Build the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 20));
        grid.setAlignment(Pos.CENTER);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHalignment(HPos.RIGHT);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        col2.setPrefWidth(250);
        grid.getColumnConstraints().addAll(col1, col2);

        int row = 0;

        // Host
        Label hostLabel = new Label("Host:");
        hostField = new TextField(defaultConfig != null ? defaultConfig.getHost() : "");
        hostField.setPromptText("VNC server hostname or IP");
        grid.add(hostLabel, 0, row);
        grid.add(hostField, 1, row++);

        // Port
        Label portLabel = new Label("Port:");
        portSpinner = new Spinner<>();
        portSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 65535,
                defaultConfig != null ? defaultConfig.getPort() : 5900));
        portSpinner.setEditable(true);
        portSpinner.setMaxWidth(100);
        grid.add(portLabel, 0, row);
        grid.add(portSpinner, 1, row++);

        // Password
        Label passwordLabel = new Label("Password:");
        passwordField = new PasswordField();
        passwordField.setPromptText("VNC password (leave blank if none)");
        if (defaultConfig != null && defaultConfig.getPassword() != null) {
            passwordField.setText(defaultConfig.getPassword());
        }
        grid.add(passwordLabel, 0, row);
        grid.add(passwordField, 1, row++);

        // Encoding (charset for clipboard)
        Label encodingLabel = new Label("Encoding:");
        encodingCombo = new ComboBox<>();
        encodingCombo.getItems().addAll("ISO-8859-1", "UTF-8", "GBK", "windows-1252", "Shift_JIS");
        encodingCombo.setValue(defaultConfig != null ? defaultConfig.getEncoding() : "ISO-8859-1");
        encodingCombo.setEditable(true);
        encodingCombo.setMaxWidth(Double.MAX_VALUE);
        grid.add(encodingLabel, 0, row);
        grid.add(encodingCombo, 1, row++);

        // --- Proxy Section ---
        Label proxySectionLabel = new Label("Proxy (optional):");
        proxySectionLabel.setStyle("-fx-font-weight: bold;");
        grid.add(proxySectionLabel, 0, row++, 2, 1);

        // Proxy Type
        Label proxyTypeLabel = new Label("Type:");
        proxyTypeCombo = new ComboBox<>();
        proxyTypeCombo.getItems().addAll("None", "HTTP", "SOCKS5");
        Proxy.Type defType = defaultConfig != null ? defaultConfig.getProxyType() : Proxy.Type.DIRECT;
        proxyTypeCombo.setValue(proxyTypeToString(defType));
        proxyTypeCombo.setMaxWidth(Double.MAX_VALUE);
        grid.add(proxyTypeLabel, 0, row);
        grid.add(proxyTypeCombo, 1, row++);

        // Proxy Host
        Label proxyHostLabel = new Label("Proxy Host:");
        proxyHostField = new TextField(defaultConfig != null ? defaultConfig.getProxyHost() : "");
        proxyHostField.setPromptText("Proxy server hostname");
        grid.add(proxyHostLabel, 0, row);
        grid.add(proxyHostField, 1, row++);

        // Proxy Port
        Label proxyPortLabel = new Label("Proxy Port:");
        proxyPortSpinner = new Spinner<>();
        proxyPortSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 65535,
                defaultConfig != null ? defaultConfig.getProxyPort() : 1080));
        proxyPortSpinner.setEditable(true);
        proxyPortSpinner.setMaxWidth(100);
        grid.add(proxyPortLabel, 0, row);
        grid.add(proxyPortSpinner, 1, row++);

        // Proxy Password
        Label proxyPasswordLabel = new Label("Proxy Password:");
        proxyPasswordField = new PasswordField();
        proxyPasswordField.setPromptText("Proxy authentication password");
        if (defaultConfig != null && defaultConfig.getProxyPassword() != null) {
            proxyPasswordField.setText(defaultConfig.getProxyPassword());
        }
        grid.add(proxyPasswordLabel, 0, row);
        grid.add(proxyPasswordField, 1, row++);

        dialog.getDialogPane().setContent(grid);

        // Buttons
        ButtonType connectButtonType = new ButtonType("Connect", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(connectButtonType, ButtonType.CANCEL);

        Button connectButton = (Button) dialog.getDialogPane().lookupButton(connectButtonType);
        connectButton.setDefaultButton(true);

        // Set result converter
        dialog.setResultConverter(buttonType -> {
            if (buttonType == connectButtonType) {
                return getConnectionConfig();
            }
            return null;
        });
    }

    public ConnectionConfig showAndWait() {
        return dialog.showAndWait().orElse(null);
    }

    private ConnectionConfig getConnectionConfig() {
        ConnectionConfig config = new ConnectionConfig();
        config.setHost(hostField.getText().trim());
        config.setPort(portSpinner.getValue());
        config.setPassword(passwordField.getText());
        config.setEncoding(encodingCombo.getValue());
        config.setProxyType(stringToProxyType(proxyTypeCombo.getValue()));
        config.setProxyHost(proxyHostField.getText().trim());
        config.setProxyPort(proxyPortSpinner.getValue());
        config.setProxyPassword(proxyPasswordField.getText());
        return config;
    }

    private static String proxyTypeToString(Proxy.Type type) {
        switch (type) {
            case HTTP: return "HTTP";
            case SOCKS: return "SOCKS5";
            default: return "None";
        }
    }

    private static Proxy.Type stringToProxyType(String s) {
        if ("HTTP".equals(s)) return Proxy.Type.HTTP;
        if ("SOCKS5".equals(s)) return Proxy.Type.SOCKS;
        return Proxy.Type.DIRECT;
    }

}
