package org.my.base

import javafx.beans.property.ListProperty
import javafx.beans.property.Property
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import org.my.util.doesReturnSubtypeOf
import java.lang.Exception
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.memberProperties


abstract class BaseModel {

    var id: Long = 0L
    val propertiesAndNames = mutableMapOf<String, Property<*>>()
//    var simpleNotifier: ChangeListener<in Any>? = null
//    var listNotifier: ListChangeListener<in Any>? = null
//    val observableAndNotifiables: MutableMap<Property<*>, MutableList<Property<*>>> = mutableMapOf()

    // TODO: this about constructor with initAllProperties call
    fun initAllProperties() {
        val memberProperties = this::class.memberProperties
        for (memberProperty in memberProperties) {
            // var property
            if (memberProperty is KMutableProperty1) {
                val mutableProperty = memberProperty as KMutableProperty1<Any, Any>
                if (!mutableProperty.doesReturnSubtypeOf(Property::class)) {
                    // Skip no javafx properties
                    continue
                }
                val initValue: Property<*> = when {
                    // Initialize StringProperty
                    mutableProperty.doesReturnSubtypeOf(StringProperty::class) -> SimpleStringProperty(
                        this,
                        memberProperty.name,
                        ""
                    )
                    // Initialize ListProperty
                    mutableProperty.doesReturnSubtypeOf(ListProperty::class) -> SimpleListProperty(
                        this,
                        memberProperty.name,
                        FXCollections.observableList(mutableListOf<Any>())
                    )
                    // Initialize ObjectProperty
                    else -> SimpleObjectProperty(
                        this,
                        memberProperty.name,
                        null,
                    )
                }
                propertiesAndNames[memberProperty.name] = initValue
                try {
                    mutableProperty.set(this, initValue)
                } catch (e: Exception) {
                    println("@@@ property = ${memberProperty.name}; initValue = ${initValue}")
                    throw e
                }

            }
        }
    }

    fun <T : BaseModel> copy(): T = newInstance<T>().apply {
        takeStateFrom(this)
    }

//    fun <T : BaseModel> copyNotifiable(): T = newInstance<T>().apply {
//        takeStateFrom(this)
//    }.also {
//        this.makeNotifiable(it)
//    }

    fun <T : BaseModel> newInstance(): T {
        return this::class.createInstance() as T
    }

    fun takeStateFrom(source: BaseModel) {
        val targetProperties = this.propertiesAndNames
        for ((name, targetProperty) in targetProperties) {
            val sourceProperty = source.propertiesAndNames[name]
                ?: throw RuntimeException("Can not get value. No property for name: $name")
            when (targetProperty) {
                is SimpleModelProperty<*> -> {
                    val sourceProperty = (sourceProperty as SimpleModelProperty<BaseModel>)
                    if (!sourceProperty.equalsModelTypeWith(targetProperty)) {
                        throw RuntimeException("Other argumentTypes for ModelProperty: ${sourceProperty.name}")
                    }
                    val sourceValue = sourceProperty.value
                    val targetValue = sourceValue.copy<BaseModel>()
                    targetProperty.value = targetValue
                }

                is SimpleModelListProperty<*> -> {
                    val sourceProperty = (sourceProperty as SimpleModelListProperty<BaseModel>)
                    val targetProperty = (targetProperty as SimpleModelListProperty<BaseModel>)
                    if (!sourceProperty.equalsModelTypeWith(targetProperty)) {
                        throw RuntimeException("Other argumentTypes for ModelProperty: ${sourceProperty.name}")
                    }
                    val targetList = sourceProperty.value.map { it.copy<BaseModel>() }
                    targetProperty.setAll(targetList)
                }
                // simple property type
                else -> {
                    val targetPropertiesAndNames = this.propertiesAndNames
                    for ((name, targetProperty) in targetPropertiesAndNames) {
                        val sourceProperty = source.propertiesAndNames[name]
                            ?: throw RuntimeException("No source property for '${targetProperty.name}'")
                        targetProperty.value = sourceProperty.value
                    }
                }
            }
        }
    }

//    fun makeNotifiable(notifiable: BaseModel) {
//        val notifiablePropertiesAndNames = notifiable.propertiesAndNames
//        for ((name, notifiableProperty) in notifiablePropertiesAndNames) {
//            val observableProperty = this.propertiesAndNames[name]
//                ?: throw RuntimeException("Can not subscribe. No property for name: $name")
//            when (notifiableProperty) {
//                is SimpleModelProperty<*> -> {  // recursively subscribe inner objects
//                    val innerNotifiable = notifiableProperty.value as BaseModel
//                    val innerObservable = (observableProperty as SimpleModelProperty<*>).value as BaseModel
//                    innerObservable.makeNotifiable(innerNotifiable)
//                }
//
//                is SimpleModelListProperty<*> -> {
//                    val observableListProperty = (observableProperty as SimpleModelListProperty<*>)
//                    // recursively subscribe inner objects
//                    notifiableProperty.forEachIndexed { index, innerNotifiable ->
//                        val innerObservable = observableListProperty[index] as BaseModel
//                        innerObservable.makeNotifiable(innerNotifiable as BaseModel)
//                    }
//                    // listen to new items. stop listen to removed items
//                    observableListProperty.addListener { observable, prevItems, actualItems ->
//                        val notifiableProperties = this.observableAndNotifiables[observable as Property<BaseModel>]!!
//                        for (notifiableProperty in notifiableProperties) {
//                            val notifiableListProperty = notifiableProperty as SimpleModelListProperty<BaseModel>
//                            for (oldNotifiableModel in notifiableListProperty) {
//
//                            }
//                        }
//                        val notifiableItems = mutableListOf<BaseModel>()
//                        val noMoreNotifiableItems = mutableListOf<BaseModel>()
//                        for (actualItem in actualItems) {
//
//                        }
//                    }
//                }
//
//                is ListProperty<*> -> {}
//
//                // simple property type. subscribe properties without further diving
//                else -> {
//                    val notifiableProperties = this.observableAndNotifiables.computeIfAbsent(observableProperty) {
//                        mutableListOf()
//                    }
//                    notifiableProperties.add(notifiableProperty)
//                    if (this.simpleNotifier == null) {
//                        this.simpleNotifier = ChangeListener<Any> { observable, _, newValue ->
//                            this.observableAndNotifiables[observable]!!.forEach {
//                                // notify every notifiable property
//                                it.value = newValue
//                            }
//                        }
//                    }
//                    observableProperty.addListener(this.simpleNotifier)
//                }
//            }
//        }
//    }

//    fun noLongerNotifiable(notifiable: BaseModel) {
//        val notifiablePropertiesAndNames = notifiable.propertiesAndNames
//        for ((name, notifiableProperty) in notifiablePropertiesAndNames) {
//            val observableProperty = this.propertiesAndNames[name]
//                ?: throw RuntimeException("Can not unsubscribe. No property for name: $name")
//            when (notifiableProperty) {
//                is SimpleModelProperty<*> -> {  // recursively unsubscribe inner objects
//                    val innerNotifiable = notifiableProperty.value
//                    val innerObservable = (observableProperty as SimpleModelProperty<*>).value
//                    innerObservable.noLongerNotifiable(innerNotifiable)
//                }
//                // recursively unsubscribe inner objects
//                is SimpleModelListProperty<*> -> notifiableProperty.forEachIndexed { index, innerNotifiable ->
//                    val innerObservable = (observableProperty as SimpleModelListProperty<*>)[index]
//                    innerObservable.noLongerNotifiable(innerNotifiable)
//                }
//                // simple property type. unsubscribe properties without further diving
//                else -> {
//                    val notifiableProperties = this.observableAndNotifiables[observableProperty]!!
//                    notifiableProperties.remove(notifiableProperty)
//                    if (notifiableProperties.size < 1) {
//                        this.observableAndNotifiables.remove(observableProperty)
//                        observableProperty.removeListener(this.simpleNotifier)
//                    }
//                    if (this.observableAndNotifiables.isEmpty()) {
//                        this.simpleNotifier = null
//                    }
//                }
//            }
//        }
//    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (other !is BaseModel) return false
        if (other::class.java != this::class.java) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        val buffer = StringBuffer()
        buffer.append(this.javaClass.simpleName)
            .append(" {").append(System.lineSeparator())
            .append(" id: ").append(this.id)
            .append(",").append(System.lineSeparator())
        this.propertiesAndNames.map { (name, property) ->
            buffer.append(" ").append(name)
                .append(": ")
                .append(property.value)
                .append(",").append(System.lineSeparator())
        }
        buffer.append("}")
        return buffer.toString()
    }
}
