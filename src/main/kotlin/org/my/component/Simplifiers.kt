package org.my.component

import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import org.my.util.hbox
import org.my.util.label
import org.my.util.textField
import org.my.util.vbox

fun Pane.vLabeledField(text: String, configurer: TextField.() -> Unit): VBox = this.vbox {
    label(text)
    textField().configurer()
}

fun Pane.hLabeledField(text: String, configurer: TextField.() -> Unit): HBox = this.hbox {
    label(text)
    textField().configurer()
}