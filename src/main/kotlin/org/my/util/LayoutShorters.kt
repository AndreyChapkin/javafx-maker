package org.my.util

import javafx.scene.layout.Pane

fun Pane.sizeToScene() {
    this.sceneProperty().addListener { _, _, newScene ->
        if (newScene != null) {
            this.minWidthProperty().bind(newScene.widthProperty())
            this.minHeightProperty().bind(newScene.heightProperty())
        }
    }
}