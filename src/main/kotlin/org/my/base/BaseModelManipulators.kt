package org.my.base

import javafx.beans.property.ListProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import java.lang.RuntimeException

/**
 * Should automatically iterate through target and source properties,
 * find differences, update target to source state.
 * @return corresponding merging action. Show names of modified properties,
 * how or from/to which values they were modified.
 */
fun produceMergeBatchAction(targetModel: BaseModel, sourceModel: BaseModel): BatchMutatingAction {
    val targetNamesAndProperties = targetModel.propertiesAndNames
    val sourceNamesAndProperties = sourceModel.propertiesAndNames
    val batchMutatingAction = BatchMutatingAction(targetModel)
    for (targetNameAndProp in targetNamesAndProperties) {
        val (targetName, targetProp) = targetNameAndProp
        val correspondingSourceProp = sourceNamesAndProperties[targetName]
        if (correspondingSourceProp != null) {
            comparePropertiesAndFillBatch(batchMutatingAction, targetProp, correspondingSourceProp)
        }
    }
    return batchMutatingAction
}

/**
 * Should be called for properties with the same names
 * @param batchMutatingAction - can be modified!
 * @param targetProperty - value can be modified!
 */
fun comparePropertiesAndFillBatch(batchMutatingAction: BatchMutatingAction, targetProperty: Any, sourceProperty: Any) {
    if (tryToMergeAsStringProperties(batchMutatingAction, targetProperty, sourceProperty)) {
        return
    }
    if (tryToMergeAsLongProperties(batchMutatingAction, targetProperty, sourceProperty)) {
        return
    }
    if (tryToMergeAsBooleanProperties(batchMutatingAction, targetProperty, sourceProperty)) {
        return
    }
    tryToMergeAsObjectProperties(batchMutatingAction, targetProperty, sourceProperty)
}

/**
 * Will be called for properties with the same names.
 * @return true - if made merging, false - if didn't.
 */
fun tryToMergeAsObjectProperties(
    batchMutatingAction: BatchMutatingAction,
    targetProperty: Any,
    sourceProperty: Any
): Boolean {
    var wasProcessed = false
    val targetObjectProperty = targetProperty as? SimpleObjectProperty<*>
    val sourceObjectProperty = sourceProperty as? SimpleObjectProperty<*>
    if (targetObjectProperty != null && sourceObjectProperty != null) {
        val prevValue = targetObjectProperty.value
        val newValue = sourceObjectProperty.value
        // Simple objects
        if (prevValue != newValue) {
            wasProcessed = true
            // Add reverse action
            batchMutatingAction.actions.add(
                ChangeValueAction(
                    propertyName = targetObjectProperty.name,
                    newValue = prevValue
                )
            )
        }
    }
    return wasProcessed
}

/**
 * Will be called for properties with the same names.
 * @return true - if made merging, false - if didn't.
 */
fun tryToMergeAsStringProperties(
    batchModifyAction: BatchMutatingAction,
    targetProperty: Any,
    sourceProperty: Any
): Boolean {
    var wasProcessed = false
    val targetStringProperty = targetProperty as? SimpleStringProperty
    val sourceStringProperty = sourceProperty as? SimpleStringProperty
    if (targetStringProperty != null && sourceStringProperty != null) {
        val prevValue = targetStringProperty.value
        val newValue = sourceStringProperty.value
        if (prevValue != newValue) {
            wasProcessed = true
            // Add reverse action
            batchModifyAction.actions.add(
                ChangeValueAction(
                    propertyName = targetStringProperty.name,
                    newValue = prevValue
                )
            )
        }
    }
    return wasProcessed
}

/**
 * Will be called for properties with the same names.
 * @return true - if made merging, false - if didn't.
 */
fun tryToMergeAsLongProperties(
    batchModifyAction: BatchMutatingAction,
    targetProperty: Any,
    sourceProperty: Any
): Boolean {
    var wasProcessed = false
    val targetLongProperty = targetProperty as? SimpleLongProperty
    val sourceLongProperty = sourceProperty as? SimpleLongProperty
    if (targetLongProperty != null && sourceLongProperty != null) {
        val prevValue = targetLongProperty.value
        val newValue = sourceLongProperty.value
        if (prevValue != newValue) {
            wasProcessed = true
            // Add reverse action
            batchModifyAction.actions.add(
                ChangeValueAction(
                    propertyName = targetLongProperty.name,
                    newValue = prevValue
                )
            )
        }
    }
    return wasProcessed
}

/**
 * Will be called for properties with the same names.
 * @return true - if made merging, false - if didn't.
 */
fun tryToMergeAsBooleanProperties(
    batchModifyAction: BatchMutatingAction,
    targetProperty: Any,
    sourceProperty: Any
): Boolean {
    var wasProcessed = false
    val targetBooleanProperty = targetProperty as? SimpleBooleanProperty
    val sourceBooleanProperty = sourceProperty as? SimpleBooleanProperty
    if (targetBooleanProperty != null && sourceBooleanProperty != null) {
        val prevValue = targetBooleanProperty.value
        val newValue = sourceBooleanProperty.value
        if (prevValue != newValue) {
            wasProcessed = true
            // Add reverse action
            batchModifyAction.actions.add(
                ChangeValueAction(
                    propertyName = targetBooleanProperty.name,
                    newValue = prevValue
                )
            )
        }
    }
    return wasProcessed
}

sealed interface MutatingAction

class BatchMutatingAction(
    val model: BaseModel,
    val actions: MutableList<PropertyMutatingAction> = mutableListOf()
) : MutatingAction {
    override fun toString(): String {
        val buffer = StringBuffer()
        buffer.append(this.javaClass.simpleName)
            .append(" ").append(actions)
        return buffer.toString()
    }
}

sealed class PropertyMutatingAction(
    val propertyName: String
) : MutatingAction {

    override fun toString(): String {
        val buffer = StringBuffer()
        buffer.append(this.javaClass.simpleName)
            .append(" {").append(System.lineSeparator())
        toStringInfo(buffer)
        buffer.append("}")
        return buffer.toString()
    }

    abstract fun toStringInfo(buffer: StringBuffer)
}

class ChangeValueAction<T>(
    propertyName: String,
    val newValue: T,
) : PropertyMutatingAction(propertyName) {

    override fun toStringInfo(buffer: StringBuffer) {
        buffer
            .append(" propertyName: ").append(this.propertyName)
            .append(",").append(System.lineSeparator())
            .append(" newValue: ").append(this.newValue)
            .append(",").append(System.lineSeparator())
    }
}

class AddToListAction<T>(
    propertyName: String,
    val newItem: T,
) : PropertyMutatingAction(propertyName) {

    override fun toStringInfo(buffer: StringBuffer) {
        buffer
            .append(" propertyName: ").append(this.propertyName)
            .append(",").append(System.lineSeparator())
            .append(" newItem: ").append(this.newItem)
            .append(",").append(System.lineSeparator())
    }
}

class InsertIntoListAction<T>(
    propertyName: String,
    val newItem: T,
    val index: Long,
) : PropertyMutatingAction(propertyName) {

    override fun toStringInfo(buffer: StringBuffer) {
        buffer
            .append(" propertyName: ").append(this.propertyName)
            .append(",").append(System.lineSeparator())
            .append(" newItem: ").append(this.newItem)
            .append(",").append(System.lineSeparator())
            .append(" index: ").append(this.index)
            .append(",").append(System.lineSeparator())
    }
}

class RemoveFromListAction<T>(
    propertyName: String,
    val removingItem: T,
) : PropertyMutatingAction(propertyName) {

    override fun toStringInfo(buffer: StringBuffer) {
        buffer
            .append(" propertyName: ").append(this.propertyName)
            .append(",").append(System.lineSeparator())
            .append(" removedItem: ").append(this.removingItem)
            .append(",").append(System.lineSeparator())
    }
}

class ReplaceInsideListAction<T>(
    propertyName: String,
    val oldValue: T,
    val newValue: T,
) : PropertyMutatingAction(propertyName) {

    override fun toStringInfo(buffer: StringBuffer) {
        buffer
            .append(" propertyName: ").append(this.propertyName)
            .append(",").append(System.lineSeparator())
            .append(" oldValue: ").append(this.oldValue)
            .append(",").append(System.lineSeparator())
            .append(" newValue: ").append(this.newValue)
            .append(",").append(System.lineSeparator())
    }
}

/**
 * @return batch of reverse actions for undo/redo functionality
 */
fun doBatchAction(action: BatchMutatingAction): BatchMutatingAction {
    val reverseAction = BatchMutatingAction(
        model = action.model,
        actions = action.actions.map {
            mutateProperty(action.model, it)
        }.toMutableList()
    )
    return reverseAction
}

/**
 * @return reverse PropertyMutatingAction
 */
fun mutateProperty(model: BaseModel, action: PropertyMutatingAction): PropertyMutatingAction {
    val mutatingProperty = model.propertiesAndNames[action.propertyName]
    if (mutatingProperty != null) {
        val undoAction: PropertyMutatingAction = when (action) {
            is ChangeValueAction<*> -> {
                val undoAction = ChangeValueAction<Any?>(
                    propertyName = action.propertyName,
                    newValue = mutatingProperty.value
                )
                mutatingProperty.value = action.newValue
                undoAction
            }

            is AddToListAction<*> -> {
                val undoAction = RemoveFromListAction(
                    propertyName = action.propertyName,
                    removingItem = action.newItem
                )
                (mutatingProperty as ListProperty<Any?>).add(action.newItem)
                return undoAction
            }

            is InsertIntoListAction<*> -> {
                val undoAction = RemoveFromListAction(
                    propertyName = action.propertyName,
                    removingItem = action.newItem
                )
                (mutatingProperty as ListProperty<Any>).add(action.index.toInt(), action.newItem)
                return undoAction
            }

            is RemoveFromListAction<*> -> {
                val listProperty = (mutatingProperty as ListProperty<Any>)
                val removedIndex = listProperty.indexOf(action.removingItem)
                if (removedIndex < 0) {
                    throw RuntimeException("There is no such element for deletion: ${action.removingItem}")
                }
                val removedItem = listProperty[removedIndex]
                val undoAction = InsertIntoListAction(
                    propertyName = action.propertyName,
                    newItem = removedItem,
                    index = removedIndex.toLong(),
                )
                listProperty.removeAt(removedIndex)
                return undoAction
            }

            is ReplaceInsideListAction<*> -> {
                val listProperty = (mutatingProperty as ListProperty<Any>)
                val replaceIndex = listProperty.indexOf(action.oldValue)
                if (replaceIndex < 0) {
                    throw RuntimeException("There is no such element for replacement: ${action.oldValue}")
                }
                val oldValue = listProperty[replaceIndex]
                val undoAction = ReplaceInsideListAction(
                    propertyName = action.propertyName,
                    oldValue = action.newValue,
                    newValue = oldValue,
                )
                listProperty[replaceIndex] = action.newValue
                return undoAction
            }
        }
        return undoAction
    }
    throw RuntimeException("No such property: ${action.propertyName}")
}