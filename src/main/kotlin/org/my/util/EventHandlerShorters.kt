package org.my.util

import javafx.scene.Node
import javafx.scene.layout.Pane

/**
 * Use full-scene sized Pane as parent.
 */
fun Pane.makeDraggable() {
    this.parent.isPickOnBounds = false
    this.setOnMousePressed {
        this.parent.isPickOnBounds = true
        val mouseClickedSceneX = it.sceneX
        val mouseClickedSceneY = it.sceneY
        val target = it.target as Node
        val targetLocalBounds = target.boundsInLocal
        val targetSceneBounds = target.localToScene(targetLocalBounds)
        val handleX = mouseClickedSceneX - targetSceneBounds.minX
        val handleY = mouseClickedSceneY - targetSceneBounds.minY
        target.parent.setOnMouseDragged {
            val mouseSceneX = it.sceneX - handleX
            val mouseSceneY = it.sceneY - handleY
            this.translateX = mouseSceneX
            this.translateY = mouseSceneY
        }
    }
    this.setOnMouseReleased {
        this.parent.isPickOnBounds = false
        this.parent.onMouseDragged = null
    }
}