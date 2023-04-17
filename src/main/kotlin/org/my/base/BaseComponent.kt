package org.my.base

import javafx.animation.Animation
import javafx.beans.InvalidationListener
import javafx.beans.property.ListProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.scene.Node
import javafx.scene.layout.Pane
import org.my.util.*

abstract class BaseComponent<T : BaseModel>(val modelProp: ObjectProperty<T>) {

    companion object {
        // TODO: think about more elegant approach to keep all components references to escape garbage collection
        val allComponents = mutableListOf<BaseComponent<out BaseModel>>()
    }

    init {
        allComponents.add(this)
    }

    lateinit var root: Pane
    // TODO: switch to model properties names

    val isShownProp = SimpleBooleanProperty(true)

    /**
     * List of pairs kind of component property - model property
     */
//    private val bindings: MutableList<Pair<StringProperty, StringProperty>> = mutableListOf()
    /**
     * List property name -> list of model-node pairs map
     */
//    val dynamicListBindings: MutableMap<String, DynamicListNodesRecord> = mutableMapOf()
    /**
     * List property name -> list of model-node pairs map
     */
//    val dynamicNodeBindings: MutableMap<String, DynamicNodeRecord> = mutableMapOf()
    val dataToListenersRegistry: PropertiesAndListenersRegistry = PropertiesAndListenersRegistry()
    val dataToViewRegistry: DataToViewRegistry = DataToViewRegistry()

    val model: T
        get() = modelProp.value

    init {
        // TODO: think about model changing approach
        // May be you should destroy this component together with the old model and corresponding view?
        // Create new one if you need to render new model
//        modelProp.addListener { _, oldModel, newModel ->
        // remove bindings to the properties of old model
        // and set bindings to the properties of new model
//            for (binding in bindings) {
//                val (componentProperty, modelProperty) = binding
//                val propertyName = modelProperty.name
//                componentProperty.unbindBidirectional(modelProperty)
//                componentProperty.bindBidirectional(newModel.returnPropertyWithName(propertyName))
//            }
        // remove dynamic nodes of old model
        // and create dynamic nodes of new model
//        }
        isShownProp.addListener { _, _, isShown ->
            val animation: Animation?
            if (isShown) {
                this.root.isVisible = true
                animation = showAnimation()
            } else {
                animation = hideAnimation()
                if (animation == null) {
                    this.root.isVisible = false
                } else {
                    animation.setOnFinished {
                        this.root.isVisible = false
                    }
                }
            }
            animation?.play()
        }
    }

    constructor(model: T) : this(SimpleObjectProperty(model))

    fun initialize() {
        root = layoutElements()
        configureElements()
        setReleaseResourcesHandler()
    }

    protected abstract fun layoutElements(): Pane
    protected open fun configureElements() {}

    // TODO: think about closures exclusion
    /**
     * @param viewProducer Should use 'alone' syntax and return Node or BaseComponent
     */
    protected inline fun <T : Any, R : Any> Pane.mapEachToNode(
        listProperty: ListProperty<R>,
        crossinline viewProducer: (R) -> T
    ) {
        // Initialize views
        listProperty.forEach {
            val initialView = viewProducer(it)
            initialView.asNode().alsoAddTo(this)
            // Register models and views
            this@BaseComponent.dataToViewRegistry.register(listProperty, it, initialView)
        }
        // Create and register change listener
        val inlinedListChangeListener: (List<R>) -> Unit = { curModels ->
            // Address only appearance/disappearance of models
            val allUnorderedStoredRecords = curModels.mapNotNull {
                this@BaseComponent.dataToViewRegistry.getRecord(listProperty, it)
            }
            var stillActualStoredModelsSize = 0
            // Remove stale model views
            allUnorderedStoredRecords.forEach {
                if (curModels.indexOf(it.data) == -1) {
                    this@BaseComponent.dataToViewRegistry.unregister(listProperty, it.data)
                    when (it.view) {
                        is BaseComponent<*> -> this.children.remove(it.view.root)
                        // Node
                        else -> this.children.remove(it.view)
                    }
                } else {
                    stillActualStoredModelsSize++
                }
            }
            // Add new model views
            curModels.forEachIndexed { i, curModel ->
                var shouldAdd = false
                var shouldAddToThePosition = false
                if (i >= stillActualStoredModelsSize) {
                    shouldAdd = true
                } else {
                    if (!this@BaseComponent.dataToViewRegistry.hasRecord(listProperty, curModel)) {
                        shouldAddToThePosition = true
                    }
                }
                if (shouldAdd || shouldAddToThePosition) {
                    val newNode = viewProducer(curModel).asNode()
                    // Add information to the registry and add node to render tree
                    this@BaseComponent.dataToViewRegistry.register(listProperty, curModel, newNode)
                    if (shouldAddToThePosition) {
                        this.children.add(i, newNode)
                    } else {
                        this.children.add(newNode)
                    }
                }
            }
        }
        val resultListChangeListener = listProperty.addSimpleListChangeListener(inlinedListChangeListener)
        // store listener for the listProperty
        this@BaseComponent.dataToListenersRegistry.register(listProperty, resultListChangeListener)
    }

    // TODO: think about closures exclusion
    /**
     * @param viewProducer Should use 'alone' syntax and return Node or BaseComponent
     */
    protected inline fun <T : Any, R> Pane.mapToNode(
        objectProperty: ObjectProperty<R>,
        crossinline viewProducer: (R) -> T
    ) {
        val objectValue = objectProperty.value
        // Initialize view
        val initialView: Any = if (objectValue == null) {
            vboxAln {
                isVisible = false
                isManaged = false
            }
        } else {
            viewProducer(objectValue)
        }
        initialView.asNode().alsoAddTo(this)
        // store view information
        this@BaseComponent.dataToViewRegistry.registerNoMatterData(objectProperty, initialView)
        // Create and register change listener
        val inlinedChangeListener: (R, R) -> Unit = { _, newObjectValue ->
            // Replace stale object view with the new one
            val storedView = this@BaseComponent.dataToViewRegistry.getViewNoMatterData(objectProperty)
            if (storedView != null) {
                val storedNodeTreeIndex = this.children.indexOf(storedView.asNode())
                // replace with the new value's view
                val newView: Any = if (newObjectValue == null) {
                    vboxAln {
                        isVisible = false
                        isManaged = false
                    }
                } else {
                    viewProducer(newObjectValue)
                }
                this.children[storedNodeTreeIndex] = newView.asNode()
                // replace record
                this@BaseComponent.dataToViewRegistry.registerNoMatterData(objectProperty, newView)
            }
        }
        val resultChangeListener = objectProperty.addSimpleChangeListener(inlinedChangeListener)
        // store listener information
        this@BaseComponent.dataToListenersRegistry.register(objectProperty, resultChangeListener)
    }

    protected open fun showAnimation(): Animation? = null

    protected open fun hideAnimation(): Animation? = null

    fun appendTo(parent: Pane) {
        parent.children.add(root)
    }

    /**
     * Handler will delete all component's handlers from external properties
     * when it is not in a render tree anymore.
     * Also, some other release-logic can be added here.
     */
    private fun setReleaseResourcesHandler() {
        this.root.sceneProperty().addListener { _, _, newScene ->
            if (newScene == null) {
                BaseComponent.allComponents.remove(this)
                clearAllExternalBindings()
            }
        }
    }

    /**
     * Remove component from render-tree manually.
     */
    fun removeFromParent() {
        when (val parent = this.root.parent) {
            is Pane -> parent.children.remove(this.root)
        }
    }

    fun clearAllBindingsFor(model: BaseModel) {
        // clear listeners
        this.dataToListenersRegistry.getAllPropertiesWithListenersFor(model).forEach(::removeListenerFromProperty)
        // clear references to models and properties used for dynamic views control
        this.dataToViewRegistry.clearAll()
    }

    /**
     * The method should be called when component is not part of render-tree anymore
     * and gets supposed to be garbage collected.
     * Should remove all unnecessary listeners from external properties.
     */
    private fun clearAllExternalBindings() {
        // clear listeners
        this.dataToListenersRegistry.getAllPropertiesWithListeners()
            .forEach(::removeListenerFromProperty)
        // clear references to models and properties used for dynamic views control
        this.dataToViewRegistry.clearAll()
    }

    private fun removeListenerFromProperty(record: PropertyToListenersRecord) {
        record.listeners.forEach {
            when (it) {
                is InvalidationListener -> record.property.removeListener(it)
                // ChangeListener
                else -> record.property.removeListener(it as ChangeListener<in Any>)
            }
        }
    }
}

fun Any.asNode(): Node = when (this) {
    is BaseComponent<*> -> this.root
    is Node -> this
    else -> throw RuntimeException("Can not convert object to Node")
}

fun <T : Node> T.referredWith(saver: (T) -> Unit): T {
    saver(this)
    return this
}
