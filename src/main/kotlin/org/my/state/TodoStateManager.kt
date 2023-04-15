package org.my.state

import org.my.base.BaseStateManager
import org.my.model.ToDoItemM
import org.my.model.ToDoItemStatus
import org.my.model.ToDoStateM
import org.my.util.JsonSerializer
import org.my.util.ListChange

class TodoStateManager(
    filePath: String,
) : BaseStateManager<ToDoStateM>(
    filePath = filePath,
    state = ToDoStateM()
) {

//    const val FILE_PATH = "D:\\Temp\\ToDos.json"

    val emptyCatalog = ToDoItemM(-1)

    var uniqueId = 0L

    init {
        this.state.todos.addListener { _: ListChange<ToDoItemM> ->
            if (!isLoadingProp.value) {
                this.isPersistedProp.set(false)
            }
        }
    }

    fun reserveUniqueId() = uniqueId++

    // region listeners

    // TODO: generalize logic and move to BaseState
    fun addToDosChangeListener(listener: (List<ToDoItemM>) -> Unit) {
        this.state.todos.addListener { _: ListChange<ToDoItemM> ->
            if (!this.isLoadingProp.value) {
                listener(this.state.todos)
            }
        }
        this.isLoadingProp.addListener { _, _, newValue ->
            if (!newValue) {
                listener(this.state.todos)
            }
        }
    }

    // endregion listeners

    // region actions

    fun addToDo(toDoItemM: ToDoItemM) {
        this.addToList(this.state.todos, toDoItemM)
    }

    fun updateToDo(sourceToDoItemM: ToDoItemM) {
        this.merge(this.state.todos, sourceToDoItemM)
    }

    fun removeToDo(toDoItemM: ToDoItemM) {
        this.removeFromList(this.state.todos, toDoItemM)
    }

    fun insertToDo(toDoItemM: ToDoItemM, index: Long) {
        this.insertIntoList(this.state.todos, toDoItemM, index)
    }

    fun replaceToDo(oldToDo: ToDoItemM, newToDo: ToDoItemM) {
        this.replaceInsideList(this.state.todos, oldToDo, newToDo)
    }

    // region async code

    // TODO: write real implementation
    override val asyncStateProducer = {
//        Thread.sleep(500)
        ToDoStateM().apply {
            todos.addAll(
                ToDoItemM(1L).apply {
                    short.set("First")
                    moreInfo.set("First info\nA lot of nice text")
                    status.set(ToDoItemStatus.IN_PROGRESS)
                },
                ToDoItemM(2L).apply {
                    short.set("Second")
                    moreInfo.set("Second info\n A lot of nice text")
                    status.set(ToDoItemStatus.ON_HOLD)
                }
            )
        }
//        JsonSerializer
//            .readFromFile<ToDoStateM>(filePath)
//            ?: ToDoStateM(1L)
    }

    // TODO: bad types inference. Should connect with asyncListProducer
    override val asyncStateConsumer = { toDoStateM: ToDoStateM ->
        this.state = toDoStateM
        this.isPersistedProp.set(true)
        this.isLoadingProp.set(false)
    }
    // endregion
}