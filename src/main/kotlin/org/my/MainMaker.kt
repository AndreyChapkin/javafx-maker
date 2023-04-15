package org.my

import javafx.application.Application
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import org.my.component.maker.MakerC

class MainMaker : Application() {

    override fun start(primaryStage: Stage) {
        val scene = Scene(root(), 400.0, 400.0)
        applyStyles(scene)
        primaryStage.title = "Hello JavaFX Application"
        primaryStage.scene = scene
        primaryStage.show()
    }

    private fun applyStyles(scene: Scene) {
        scene.stylesheets.addAll("org/my/component/field/labeled-field.css")
    }

    private fun root(): Parent {
        val makerC = MakerC()
        makerC.initialize()
        return makerC.root
    }
}