package org.my.base

import javafx.beans.property.Property
import javafx.scene.Node

/**
 * Registry of Property (of concrete model) to List of object-view pairs
 */
class DataToViewRegistry {

    private val registryMap = mutableMapOf<String, PropertyToViewsRecord>()
    private val DATA_STUB = ""

    private fun getRegistryKey(property: Property<*>): String {
        val modelPartKey = (property.bean as BaseModel).let {
            it::class.qualifiedName + it.id
        }
        return modelPartKey + property::class.simpleName
    }

    fun register(property: Property<*>, data: Any, view: Any) {
        val registryKey = getRegistryKey(property)
        val propertyRecord = registryMap.computeIfAbsent(registryKey) {
            PropertyToViewsRecord(property)
        }
        val dataToViewRecordIndex = propertyRecord.dataToViews.indexOfFirst { it.data == data }
        if (dataToViewRecordIndex == -1) {
            propertyRecord.dataToViews.add(DataToViewRecord(data, view))
        } else {
            propertyRecord.dataToViews[dataToViewRecordIndex] = DataToViewRecord(data, view)
        }
    }

    /**
     * Will have only singular stub data for this property
     */
    fun registerNoMatterData(property: Property<*>, view: Any) {
        val registryKey = getRegistryKey(property)
        val propertyRecord = registryMap.computeIfAbsent(registryKey) {
            PropertyToViewsRecord(property)
        }
        if (propertyRecord.dataToViews.size < 1) {
            propertyRecord.dataToViews.add(DataToViewRecord(DATA_STUB, view))
        } else {
            propertyRecord.dataToViews[0] = DataToViewRecord(DATA_STUB, view)
        }
    }

    fun getViewNoMatterData(property: Property<*>): Any? {
        return registryMap[getRegistryKey(property)]?.dataToViews?.get(0)?.view
    }

    fun unregister(property: Property<*>) {
        val registryKey = getRegistryKey(property)
        registryMap.remove(registryKey)
    }

    fun unregister(property: Property<*>, data: Any) {
        val registryKey = getRegistryKey(property)
        val propertyRecord = registryMap[registryKey]
        if (propertyRecord != null) {
            propertyRecord.dataToViews.removeIf { it.data == data }
            if (propertyRecord.dataToViews.size < 1) {
                registryMap.remove(registryKey)
            }
        }
    }

    fun hasRecord(property: Property<*>, data: Any): Boolean {
        return registryMap[getRegistryKey(property)]?.dataToViews?.find { it.data == data } != null
    }

    fun getRecord(property: Property<*>, data: Any): DataToViewRecord? {
        return registryMap[getRegistryKey(property)]?.dataToViews?.find { it.data == data }
    }

    fun clearAll() {
        this.registryMap.clear()
    }
}

class PropertyToViewsRecord(
    val property: Property<*>,
    val dataToViews: MutableList<DataToViewRecord> = mutableListOf()
)

class DataToViewRecord(
    val data: Any,
    /**
     * BaseComponent or Node
     */
    val view: Any
) {
    init {
        if (!(view is Node || view is BaseComponent<*>)) {
            throw RuntimeException("Wrong type of view: ${view::class.simpleName}")
        }
    }
}

class PropertiesAndListenersRegistry {

    // Property (for concrete model) key -> list of listeners
    private val registryMap = mutableMapOf<String, PropertyToListenersRecord>()

    private fun getRegistryKey(property: Property<*>): String {
        val modelPartKey = (property.bean as BaseModel).let {
            it::class.qualifiedName + it.id
        }
        return modelPartKey + property::class.simpleName
    }

    fun register(property: Property<*>, listener: Any) {
        val registryKey = getRegistryKey(property)
        val record = registryMap.computeIfAbsent(registryKey) {
            PropertyToListenersRecord(property)
        }
        record.listeners.add(listener)
    }

    fun getAllPropertiesWithListenersFor(model: BaseModel): List<PropertyToListenersRecord> {
        return model.propertiesAndNames.values.mapNotNull {
            registryMap[getRegistryKey(it)]
        }
    }

    fun getAllPropertiesWithListeners() = registryMap.values

    fun unregister(property: Property<*>) {
        registryMap.remove(getRegistryKey(property))
    }
}

class PropertyToListenersRecord(
    val property: Property<*>,
    val listeners: MutableList<Any> = mutableListOf()
)