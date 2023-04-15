package org.my.base

import javafx.animation.Animation
import javafx.beans.property.ListProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.Property
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ChangeListener
import javafx.collections.ListChangeListener
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.Pane
import javafx.scene.text.Text
import org.my.util.addSimpleChangeListener
import org.my.util.addSimpleListChangeListener
import org.my.util.alsoAddTo

abstract class BaseComponent<T : BaseModel>(val modelProp: ObjectProperty<T>) {

    lateinit var root: Pane
    // TODO: switch to model properties names

    /**
     * List of pairs kind of component property - model property
     */
    private val bindings: MutableList<Pair<StringProperty, StringProperty>> = mutableListOf()
    val isShownProp = SimpleBooleanProperty(true)

    /**
     * List property name -> list of model-node pairs map
     */
    val dynamicListBindings: MutableMap<String, DynamicListNodesRecord> = mutableMapOf()

    /**
     * List property name -> list of model-node pairs map
     */
    val dynamicNodeBindings: MutableMap<String, DynamicNodeRecord> = mutableMapOf()

    val model: T
        get() = modelProp.value

    init {
        // TODO: think about model changing approach
        // May be you should destroy this component together with the old model and corresponding view?
        // Create new one if you need to render new model
//        modelProp.addListener { _, _, newModel ->
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
    protected inline fun <T : Any, R : BaseModel> Pane.forEachToNode(
        modelsListProperty: ListProperty<R>,
        crossinline viewProducer: (R) -> T
    ) {
        // validate property
        if (modelsListProperty.bean == null || modelsListProperty.bean != this@BaseComponent.model) {
            throw RuntimeException("Use listProperty of external model or undefined bean: ${modelsListProperty.bean}")
        }
        val storedModelsAndNodes: MutableList<Pair<BaseModel, Node>> = mutableListOf()
        val listPropertyName = modelsListProperty.name
        // Initialize views
        for (model in modelsListProperty) {
            val node = viewProducer(model).asNode().alsoAddTo(this)
            storedModelsAndNodes.add(model to node)
        }
        // Create and register change listener
        val embeddedListChangeListener: (List<R>) -> Unit = { curModels ->
            // Remove stale model views
            val dynamicListNodesRecord = this@BaseComponent.dynamicListBindings[listPropertyName]
            if (dynamicListNodesRecord != null) {
                val storedModelsAndNodesIterator = dynamicListNodesRecord.modelsAndNodes.iterator()
                for (pair in storedModelsAndNodesIterator) {
                    val (storedModel, storedNode) = pair
                    if (curModels.indexOf(storedModel) == -1) {
                        storedModelsAndNodesIterator.remove()
                        this.children.remove(storedNode)
                    }
                }
                val storedModelsAndNodes = dynamicListNodesRecord.modelsAndNodes
                // Add new model views
                for ((modelIndex, curModel) in curModels.withIndex()) {
                    var shouldAdd = false
                    var shouldAddToThePosition = false
                    if (modelIndex >= storedModelsAndNodes.size) {
                        shouldAdd = true
                    } else {
                        val storedModel = storedModelsAndNodes[modelIndex].first
                        if (curModel != storedModel) {
                            shouldAddToThePosition = true
                        }
                    }
                    if (shouldAdd || shouldAddToThePosition) {
                        val newNode = viewProducer(curModel).asNode()
                        // Add information to the store and add node to render tree
                        if (shouldAddToThePosition) {
                            storedModelsAndNodes.add(modelIndex, curModel to newNode)
                            this.children.add(modelIndex, newNode)
                        } else {
                            storedModelsAndNodes.add(curModel to newNode)
                            this.children.add(newNode)
                        }
                    }
                }
            }
        }
        val resultListChangeListener = modelsListProperty.addSimpleListChangeListener(embeddedListChangeListener)
        // store dynamic list information
        val dynamicListNodesRecord = DynamicListNodesRecord(
            storedModelsAndNodes,
            resultListChangeListener as ListChangeListener<BaseModel>
        )
        this@BaseComponent.dynamicListBindings[listPropertyName] = dynamicListNodesRecord
    }

    // TODO: think about closures exclusion
    /**
     * @param viewProducer Should use 'alone' syntax and return Node or BaseComponent
     */
    protected inline fun <T : Any, R> Pane.mapToNode(
        objectProperty: ObjectProperty<R>,
        crossinline viewProducer: (R) -> T
    ) {
        // validate property
        if (objectProperty.bean == null || objectProperty.bean != this@BaseComponent.model) {
            throw RuntimeException("Use objectProperty of external model or undefined bean: ${objectProperty.bean}")
        }
        val propertyName = objectProperty.name
        val objectValue = objectProperty.value
        // Initialize view
        val initialNode = viewProducer(objectValue).asNode().alsoAddTo(this)
        // Create and register change listener
        val embeddedChangeListener: (R) -> Unit = { newObjectValue ->
            // Replace stale object view with the new one
            val dynamicNodeRecord = dynamicNodeBindings[propertyName]
            if (dynamicNodeRecord != null) {
                val storedNode = dynamicNodeRecord.node
                val storedNodeTreeIndex = this.children.indexOf(storedNode)
                if (newObjectValue == null) {
                    // remove previous value's view
                    this.children.removeAt(storedNodeTreeIndex)
                } else {
                    // replace with the new value's view
                    val newNode = viewProducer(newObjectValue).asNode()
                    this.children[storedNodeTreeIndex] = newNode
                    dynamicNodeRecord.node = newNode
                }
            }
        }
        val resultListChangeListener = objectProperty.addSimpleChangeListener(embeddedChangeListener)
        // store dynamic list information
        val dynamicNodeRecord = DynamicNodeRecord(
            initialNode,
            resultListChangeListener as ChangeListener<Any>
        )
        this@BaseComponent.dynamicNodeBindings[propertyName] = dynamicNodeRecord
    }

    fun Any.asNode(): Node = when (this) {
        is BaseComponent<*> -> this.root
        is Node -> this
        else -> throw RuntimeException("Can not convert object to Node")
    }

    protected fun <T : Node> T.referredWith(saver: (T) -> Unit): T {
        saver(this)
        return this
    }

    protected fun TextField.bindTo(property: StringProperty): TextField {
        this.textProperty().bindBidirectional(property)
        bindings.add(this.textProperty() to property)
        return this
    }

    protected fun TextArea.bindTo(property: StringProperty): TextArea {
        this.textProperty().bindBidirectional(property)
        bindings.add(this.textProperty() to property)
        return this
    }

    protected fun Text.bindTo(property: StringProperty): Text {
        this.textProperty().bindBidirectional(property)
        bindings.add(this.textProperty() to property)
        return this
    }

    protected fun Label.bindTo(property: StringProperty): Label {
        this.textProperty().bindBidirectional(property)
        bindings.add(this.textProperty() to property)
        return this
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

    /**
     * The method should be called when component is not part of render-tree anymore
     * and gets supposed to be garbage collected.
     * Should remove all unnecessary listeners from external properties.
     */
    private fun clearAllExternalBindings() {
        dynamicListBindings.entries.forEach { (listPropertyName, dynamicNodesRecord) ->
            this.model.propertiesAndNames[listPropertyName]?.let {
                (it as ListProperty<BaseModel>).removeListener(dynamicNodesRecord.changeListener)
            }
        }
        dynamicNodeBindings.entries.forEach { (propertyName, node) ->
            this.model.propertiesAndNames[propertyName]?.let {
                (it as Property<Any>).removeListener(node.changeListener)
            }
        }
    }
}
