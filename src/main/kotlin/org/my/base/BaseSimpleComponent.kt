package org.my.base

import javafx.animation.Animation
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.StringProperty
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.Pane
import javafx.scene.text.Text

abstract class BaseSimpleComponent {

    lateinit var root: Pane
    // TODO: switch to model properties names

    /**
     * List of pairs kind of component property - model property
     */
    private val bindings: MutableList<Pair<StringProperty, StringProperty>> = mutableListOf()
    val isShownProp = SimpleBooleanProperty(true)

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
        // make root visible or not with animations
        isShownProp.addListener { _, _, isShown ->
            val animation: Animation?
            if (isShown) {
                this.root.isVisible = true
                animation = showAnimation()
            } else {
                animation = hideAnimation()
                animation?.setOnFinished {
                    this.root.isVisible = false
                }
                if (animation == null) {
                    this.root.isVisible = false
                }
            }
            animation?.play()
        }
    }

    open fun initialize() {
        root = layoutElements()
        configureElements()
    }

    protected abstract fun layoutElements(): Pane
    protected open fun configureElements() {}

    fun Any.asNode(): Node = when (this) {
        is BaseSimpleComponent -> this@BaseSimpleComponent.root
        is Node -> this
        else -> throw RuntimeException("Can not convert object to Node")
    }

    protected inline fun <T : Node> T.referredWith(saver: (T) -> Unit): T {
        saver(this)
        return this
    }

    protected fun TextField.bindTo(property: StringProperty): TextField {
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
     * Remove component from render-tree manually.
     */
    fun removeFromParent() {
        when (val parent = this.root.parent) {
            is Pane -> parent.children.remove(this.root)
        }
    }
}
