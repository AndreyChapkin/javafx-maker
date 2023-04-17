package org.my.model

import javafx.beans.property.ListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.Node
import org.my.base.BaseModel

class MakerM() : BaseModel() {

    lateinit var selectedNode: SimpleObjectProperty<Node?>

    init {
        initAllProperties()
    }
}

class TestM() : BaseModel() {

    lateinit var name: SimpleStringProperty

    init {
        initAllProperties()
    }
}