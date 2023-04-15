package org.my.base

import javafx.beans.value.ChangeListener
import javafx.collections.ListChangeListener
import javafx.scene.Node

class DynamicListNodesRecord(
    val modelsAndNodes: MutableList<Pair<BaseModel, Node>> = mutableListOf(),
    val changeListener: ListChangeListener<BaseModel>
)

class DynamicNodeRecord(
    var node: Node,
    var changeListener: ChangeListener<Any>
)