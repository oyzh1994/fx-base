package cn.oyzh.vnc.test; //// Copyright (C) 2010 - 2014 GlavSoft LLC.
//// All rights reserved.
////
//// -----------------------------------------------------------------------
//// This file is part of the TightVNC software.  Please visit our Web site:
////
////                       http://www.tightvnc.com/
////
//// This program is free software; you can redistribute it and/or modify
//// it under the terms of the GNU General Public License as published by
//// the Free Software Foundation; either version 2 of the License, or
//// (at your option) any later version.
////
//// This program is distributed in the hope that it will be useful,
//// but WITHOUT ANY WARRANTY; without even the implied warranty of
//// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//// GNU General Public License for more details.
////
//// You should have received a copy of the GNU General Public License along
//// with this program; if not, write to the Free Software Foundation, Inc.,
//// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//// -----------------------------------------------------------------------
////
//package com.jfxvnc;
//
//import com.glavsoft.rfb.client.KeyEventMessage;
//import com.glavsoft.rfb.protocol.Protocol;
//import com.glavsoft.utils.Keymap;
//import com.glavsoft.viewer.settings.UiSettings;
//
//import javafx.animation.FadeTransition;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.control.Button;
//import javafx.scene.control.Separator;
//import javafx.scene.control.Tooltip;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.input.MouseEvent;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.StackPane;
//import javafx.util.Duration;
//
//import java.io.InputStream;
//
///**
// * Auto-hiding floating toolbar overlay.
// * Replaces the MyToolbar inner class from VNCViewer.kt.
// * Collapses to a thin bar when the mouse is away, expands on hover.
// */
//public class JfxToolbarOverlay extends HBox {
//
//    private static final double COLLAPSED_HEIGHT = 4.0;
//    private static final double EXPANDED_HEIGHT = 36.0;
//
//    private final UiSettings uiSettings;
//    private final Protocol protocol;
//    private final StackPane parent;
//    private final Runnable onFitContent;
//    private boolean collapsed = true;
//
//    private Button zoomInBtn;
//    private Button zoomOutBtn;
//    private Button actualZoomBtn;
//    private Button fitContentBtn;
//    private Button ctrlAltDelBtn;
//
//    public JfxToolbarOverlay(StackPane parent, UiSettings uiSettings, Protocol protocol, Runnable onFitContent) {
//        this.parent = parent;
//        this.uiSettings = uiSettings;
//        this.protocol = protocol;
//        this.onFitContent = onFitContent;
//
//        setAlignment(Pos.CENTER);
//        setPadding(new Insets(3, 8, 3, 8));
//        setSpacing(4);
//        setStyle("-fx-background-color: rgba(60, 63, 65, 0.9); -fx-background-radius: 0 0 6 6;");
//        setOpacity(0.0);
//        setPickOnBounds(false);
//
//        initButtons();
//        initAutoHide();
//    }
//
//    private void initButtons() {
//        zoomInBtn = createButton("images/button-zoom-in.png", "Zoom In", () -> uiSettings.zoomIn());
//        zoomOutBtn = createButton("images/button-zoom-out.png", "Zoom Out", () -> uiSettings.zoomOut());
//        actualZoomBtn = createButton("images/button-zoom-100.png", "Actual Size (100%)", () -> uiSettings.zoomAsIs());
//        fitContentBtn = createButton("images/button-zoom-fit.png", "Fit to Window", () -> onFitContent.run());
//        ctrlAltDelBtn = createButton("images/button-ctrl-alt-del.png", "Send Ctrl+Alt+Del", this::sendCtrlAltDel);
//
//        getChildren().add(zoomInBtn);
//        getChildren().add(zoomOutBtn);
//        getChildren().add(actualZoomBtn);
//        getChildren().add(fitContentBtn);
//        getChildren().add(new Separator(javafx.geometry.Orientation.VERTICAL));
//        getChildren().add(ctrlAltDelBtn);
//    }
//
//    private Button createButton(String imagePath, String tooltip, Runnable action) {
//        Button button = new Button();
//        button.setTooltip(new Tooltip(tooltip));
//        button.setStyle("-fx-background-color: transparent; -fx-padding: 2;");
//        button.setPrefSize(28, 28);
//        button.setMinSize(28, 28);
//        button.setMaxSize(28, 28);
//
//        // Load icon
//        InputStream is = getClass().getClassLoader().getResourceAsStream(imagePath);
//        if (is != null) {
//            Image img = new Image(is, 20, 20, true, true);
//            ImageView iv = new ImageView(img);
//            iv.setFitWidth(20);
//            iv.setFitHeight(20);
//            button.setGraphic(iv);
//        } else {
//            // Fallback: use text on button
//            button.setText(tooltip.substring(0, 1));
//        }
//
//        button.setOnAction(e -> {
//            if (action != null) {
//                action.run();
//            }
//        });
//
//        // Hover effect
//        button.setOnMouseEntered(e ->
//                button.setStyle("-fx-background-color: rgba(255, 255, 255, 0.2); -fx-padding: 2; -fx-background-radius: 4;"));
//        button.setOnMouseExited(e ->
//                button.setStyle("-fx-background-color: transparent; -fx-padding: 2;"));
//
//        return button;
//    }
//
//    private void sendCtrlAltDel() {
//        protocol.sendMessage(new KeyEventMessage(Keymap.K_CTRL_LEFT, true));
//        protocol.sendMessage(new KeyEventMessage(Keymap.K_ALT_LEFT, true));
//        protocol.sendMessage(new KeyEventMessage(Keymap.K_DELETE, true));
//        protocol.sendMessage(new KeyEventMessage(Keymap.K_DELETE, false));
//        protocol.sendMessage(new KeyEventMessage(Keymap.K_ALT_LEFT, false));
//        protocol.sendMessage(new KeyEventMessage(Keymap.K_CTRL_LEFT, false));
//    }
//
//    private void initAutoHide() {
//        // Track mouse position on parent scene to determine collapse/expand
//        parent.setOnMouseMoved(this::handleMouseMove);
//        setOnMouseEntered(e -> expand());
//        setOnMouseExited(e -> collapse());
//    }
//
//    private void handleMouseMove(MouseEvent event) {
//        double mouseY = event.getSceneY();
//        double toolbarBottom = localToScene(getBoundsInLocal()).getMaxY();
//
//        if (mouseY < toolbarBottom + 20) {
//            expand();
//        } else {
//            collapse();
//        }
//    }
//
//    private void expand() {
//        if (!collapsed) return;
//        collapsed = false;
//        setVisible(true);
//        setOpacity(1.0);
//
//        // Animate expand
//        FadeTransition ft = new FadeTransition(Duration.millis(150), this);
//        ft.setFromValue(0.3);
//        ft.setToValue(1.0);
//        ft.play();
//    }
//
//    private void collapse() {
//        if (collapsed) return;
//        collapsed = true;
//
//        // Animate collapse
//        FadeTransition ft = new FadeTransition(Duration.millis(500), this);
//        ft.setFromValue(1.0);
//        ft.setToValue(0.3);
//        ft.setOnFinished(e -> {
//            if (collapsed) {
//                setOpacity(0.3);
//            }
//        });
//        ft.play();
//    }
//
//    public boolean isCollapsed() {
//        return collapsed;
//    }
//}
