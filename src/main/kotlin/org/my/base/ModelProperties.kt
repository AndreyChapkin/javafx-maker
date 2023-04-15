package org.my.base

import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import org.my.util.getFirstGenericSuperclassTypeArgument

class SimpleModelProperty<T : BaseModel> : SimpleObjectProperty<T>(),
    CheckableModelProperty<SimpleModelProperty<*>> {
    override fun equalsModelTypeWith(otherProperty: SimpleModelProperty<*>): Boolean {
        return equalArgumentTypes(otherProperty)
    }
}

class SimpleModelListProperty<T : BaseModel> : SimpleListProperty<T>(),
    CheckableModelProperty<SimpleModelListProperty<*>> {
    override fun equalsModelTypeWith(otherProperty: SimpleModelListProperty<*>): Boolean {
        return equalArgumentTypes(otherProperty)
    }
}

interface CheckableModelProperty<T> {

    fun equalArgumentTypes(other: CheckableModelProperty<*>): Boolean {
        val valueType = this::class.java.getFirstGenericSuperclassTypeArgument()
        val otherValueType = other::class.java.getFirstGenericSuperclassTypeArgument()
        return valueType == otherValueType
    }

    fun <R: BaseModel> createEmptyValueInstance(): R {
        return (this::class.java.getFirstGenericSuperclassTypeArgument() as Class<R>)
            .getDeclaredConstructor()
            .newInstance()
    }

    fun equalsModelTypeWith(otherProperty: T): Boolean
}