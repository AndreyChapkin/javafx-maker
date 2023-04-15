package org.my.base

import javafx.beans.property.BooleanProperty
import javafx.beans.property.ListProperty
import javafx.beans.property.Property
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import org.my.concurrent.getAsync
import org.my.util.JsonSerializer
import org.my.util.SimpleCallback
import org.my.util.addSimpleChangeListener
import java.util.*

abstract class BaseStateManager<T : BaseModel>(
    val filePath: String,
    var state: T
) {

    val isLoadingProp: BooleanProperty = SimpleBooleanProperty(false)
    val isPersistedProp: BooleanProperty = SimpleBooleanProperty(true)
    val redoStack: LinkedList<BatchMutatingAction> = LinkedList()
    val undoStack: LinkedList<BatchMutatingAction> = LinkedList()

    fun processAndSaveUndoAction(action: BatchMutatingAction) {
        val historyReverseStateAction = doBatchAction(action)
        undoStack.add(historyReverseStateAction)
    }

    fun processAndSaveUndoAction(property: Property<*>, action: PropertyMutatingAction) {
        processAndSaveUndoAction(
            BatchMutatingAction(
                model = property.bean as BaseModel,
                actions = mutableListOf(action)
            )
        )
    }

    /**
     * Change values and remember reverse actions
     */
    fun change(property: SimpleObjectProperty<T>, newValue: T) {
        this.processAndSaveUndoAction(
            property,
            ChangeValueAction(
                propertyName = property.name,
                newValue = newValue
            )
        )
    }

    /**
     * Change values and remember reverse actions
     * @param targetProperty where to look for target model
     * @param sourceValue target value will be found with source id
     */
    fun merge(targetProperty: Property<*>, sourceValue: BaseModel) {
        val targetModel: BaseModel? = when (targetProperty) {
            is ListProperty<*> -> {
                val targetIndex = targetProperty.value.indexOf(sourceValue)
                if (targetIndex > -1) targetProperty.value[targetIndex] as BaseModel else null
            }

            else -> targetProperty.value as BaseModel
        }
        if (targetModel != null) {
            this.processAndSaveUndoAction(produceMergeBatchAction(targetModel, sourceValue))
        }
    }

    /**
     * Add to the list and remember reverse action
     */
    fun addToList(property: ListProperty<*>, item: Any) {
        this.processAndSaveUndoAction(
            property, AddToListAction(
                propertyName = property.name,
                newItem = item
            )
        )
    }

    /**
     * Insert into the list at specified position and remember reverse action
     */
    fun insertIntoList(property: ListProperty<*>, newValue: Any, index: Long) {
        this.processAndSaveUndoAction(
            property, InsertIntoListAction(
                propertyName = property.name,
                newItem = newValue,
                index = index
            )
        )
    }

    /**
     * Replace inside the list and remember reverse action
     */
    fun <U> replaceInsideList(property: ListProperty<*>, oldValue: U, newValue: U) {
        this.processAndSaveUndoAction(
            property, ReplaceInsideListAction(
                propertyName = property.name,
                oldValue = oldValue,
                newValue = newValue
            )
        )
    }

    /**
     * Remove from the list and remember reverse action
     */
    fun removeFromList(property: ListProperty<*>, item: Any) {
        this.processAndSaveUndoAction(
            property, RemoveFromListAction(
                propertyName = property.name,
                removingItem = item
            )
        )
    }

    fun undo() {
        val reverseAction: BatchMutatingAction? = undoStack.pollLast()
        if (reverseAction != null) {
            val batchReverseAction = doBatchAction(reverseAction)
            redoStack.add(batchReverseAction)
        }
    }

    fun redo() {
        val forwardAction: BatchMutatingAction? = redoStack.pollLast()
        if (forwardAction != null) {
            val batchReverseAction = doBatchAction(forwardAction)
            undoStack.add(batchReverseAction)
        }
    }

    fun clearHistory() {
        undoStack.clear()
        redoStack.clear()
    }

    // region async code
    protected abstract val asyncStateProducer: () -> T
    protected abstract val asyncStateConsumer: (T) -> Unit
    // endregion

//    fun <R> addPropertyChangeListener(propertyName: String, listener: ChangeListener)

    fun save() {
        JsonSerializer.writeToFile(state, filePath)
        this.isPersistedProp.set(true)
    }

    fun load(completeCallback: SimpleCallback? = null) {
        this.isLoadingProp.set(true)
//        getAsync(this.asyncStateProducer, this.asyncStateConsumer)
        getAsync(this.asyncStateProducer) { result ->
            this.asyncStateConsumer(result)
            completeCallback?.let { it() }
        }
    }

    override fun toString(): String {
        val buffer = StringBuffer()
        buffer.append(this.javaClass.simpleName)
            .append(" {").append(System.lineSeparator())
            .append(" state: ")
            .append(this.state)
            .append(",").append(System.lineSeparator())
            .append(" undo actions: ").append(this.undoStack)
            .append(",").append(System.lineSeparator())
            .append(" redo actions: ").append(this.redoStack)
            .append("}")
        return buffer.toString()
    }
}