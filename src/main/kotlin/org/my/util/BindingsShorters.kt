package org.my.util

import javafx.beans.property.StringProperty
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.text.Text

fun TextField.bindBidirectionalTo(property: StringProperty): TextField {
    this.textProperty().bindBidirectional(property)
    return this
}

fun TextArea.bindBidirectionalTo(property: StringProperty): TextArea {
    this.textProperty().bindBidirectional(property)
    return this
}

fun Text.bindBidirectionalTo(property: StringProperty): Text {
    this.textProperty().bindBidirectional(property)
    return this
}

fun Label.bindBidirectionalTo(property: StringProperty): Label {
    this.textProperty().bindBidirectional(property)
    return this
}