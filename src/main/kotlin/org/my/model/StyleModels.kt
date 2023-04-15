package org.my.model

import javafx.beans.property.SimpleStringProperty
import org.my.base.BaseModel

class StyleModel(id: Long) : BaseModel() {

    lateinit var cssFileUrl: SimpleStringProperty
}