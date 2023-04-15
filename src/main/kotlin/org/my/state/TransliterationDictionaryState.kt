package org.my.state

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.my.concurrent.getAsync
import org.my.dto.TransliterationDto
import org.my.util.JsonSerializer
import org.my.util.ListChange

object TransliterationDictionaryState {

    const val FILE_PATH = "D:\\Temp\\TransliterationDtos.json"

    val data: ObservableList<TransliterationDto> = FXCollections.observableArrayList()
    val isLoading: BooleanProperty = SimpleBooleanProperty(false)
    val isPersisted: BooleanProperty = SimpleBooleanProperty(true)
//    private var isInitialized: Boolean = false

    init {
        data.addListener { _: ListChange<TransliterationDto> ->
            if (!isLoading.value) {
                isPersisted.set(false)
            }
        }
    }

    // region async code

    private val asyncListProducer = {
        JsonSerializer
            .readFromFile<List<TransliterationDto>>(FILE_PATH)
            ?.toMutableList()
            ?: mutableListOf()
    }

    private val asyncListConsumer = { list: List<TransliterationDto> ->
        data.addAll(list)
        isPersisted.set(true)
        isLoading.set(false)
    }
    // endregion

    fun save() {
        JsonSerializer.writeToFile(data, FILE_PATH)
        isPersisted.set(true)
    }

    fun load() {
        isLoading.set(true)
        data.clear()
        getAsync(asyncListProducer, asyncListConsumer)
    }

    fun addDataChangeListener(listener: (List<TransliterationDto>) -> Unit) {
        data.addListener { event: ListChange<TransliterationDto> ->
            if (!this.isLoading.value) {
                listener(data)
            }
        }
        isLoading.addListener { prop, oldValue, newValue ->
            if (!newValue) {
                listener(data)
            }
        }
    }

    /*fun refresh() {
        isLoading.set(true)
        wordsAndTransliterations.clear()
        val newDtos = JsonSerializer.readFromFile<List<TransliterationDto>>(FILE_PATH)?.toMutableList()
            ?: mutableListOf()
        wordsAndTransliterations.addAll(newDtos)
        isLoading.set(false)
    }*/
}