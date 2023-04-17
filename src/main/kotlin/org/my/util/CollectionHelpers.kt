package org.my.util

import javafx.beans.property.ObjectProperty
import javafx.beans.property.Property
import javafx.beans.value.ChangeListener
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.collections.WeakListChangeListener
import org.my.model.CatalogModel
import org.my.state.KnowledgeLibraryState

fun <T> List<T>.isValidIndex(i: Int) = -1 < i && i < this.size

fun <T : Comparable<T>> MutableList<T>.insertAsSorted(obj: T) {
    val lastIndex = this.size - 1
    val insertIndex = findIndexForInsertion(this, obj, 0, lastIndex)
    when {
        insertIndex == lastIndex + 1 -> this.add(obj)
        insertIndex < lastIndex + 1 -> this.add(insertIndex, obj)
    }
}

fun <T : Comparable<T>> findIndexForInsertion(list: List<T>, obj: T, begin: Int, end: Int): Int {
    if (begin > end) {
        return begin
    }
    val middle = (begin + end) / 2
    val middleObj = list[middle]
    val compareResult = obj.compareTo(middleObj)
    return when {
        compareResult == 0 -> middle
        compareResult < 0 -> findIndexForInsertion(list, obj, begin, middle - 1)
        else -> findIndexForInsertion(list, obj, middle + 1, end)
    }
}

inline fun <T> ObservableList<T>.addSimpleListChangeListener(crossinline listener: (List<T>) -> Unit): ListChangeListener<T> {
    val resultListener = ListChangeListener { event: ListChange<T> ->
        listener(event.list)
    }
    this.addListener(resultListener)
    return resultListener
}

inline fun <T> Property<T>.addSimpleChangeListener(crossinline listener: (T, T) -> Unit): ChangeListener<T> {
    val resultListener = ChangeListener { _, prevValue, newValue -> listener(prevValue, newValue) }
    this.addListener(resultListener)
    return resultListener
}

inline fun <T> ObservableList<T>.addWeakSimpleListChangeListener(crossinline listener: (List<T>) -> Unit): ListChangeListener<T> {
    val weakChangeListener = WeakListChangeListener<T> { event: ListChange<T> ->
        listener(event.list)
    }
    this.addListener(weakChangeListener)
    return weakChangeListener
}