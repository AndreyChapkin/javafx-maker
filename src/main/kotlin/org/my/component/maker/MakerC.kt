package org.my.component.maker

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextFormatter
import javafx.scene.input.KeyCode
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import org.my.base.BaseSimpleComponent
import org.my.util.*
import java.lang.StringBuilder
import java.text.CharacterIterator
import java.text.StringCharacterIterator
import kotlin.math.abs

class MakerC : BaseSimpleComponent() {

    // views
    lateinit var canvas: Pane
    lateinit var code: TextArea
    lateinit var informator: Pane
    var selectedNode: SimpleObjectProperty<Node?> = SimpleObjectProperty()

    override fun layoutElements(): Pane = stackPaneAln {
        layer1().alsoAddTo(this)
    }

    fun layer1() = splitPaneAln {
        textArea {
            addStyles("code-editor")

            referredWith { code = it }
        }

        vbox {
            addStyles("canvas")

            referredWith { canvas = it }
        }
    }

    override fun configureElements() {
        // splitPane
        // code area
        val widthConfigurer = object : ChangeListener<Scene> {
            override fun changed(observable: ObservableValue<out Scene>?, oldValue: Scene?, newValue: Scene?) {
                if (newValue != null) {
                    code.prefWidthProperty().bind(newValue.widthProperty())
                    code.prefHeightProperty().bind(newValue.heightProperty())
                    root.sceneProperty().removeListener(this)
                }
            }
        }
        code.sceneProperty().addListener(widthConfigurer)
        code.isWrapText = true
        code.setOnKeyPressed {
            val key = it.code
            val isCtrl = it.isControlDown
            if (key == KeyCode.ENTER && isCtrl) {
                canvas.children.clear()
                val newRoot = parser(code.text)
                canvas.children.add(newRoot)
            }
        }
        code.textFormatter = TextFormatter<TextFormatter.Change> {
            when (it.text) {
                "{" -> {
                    val curLevelIndent = findCurNestingLevel(it, 2)
//                    val innerLevelIndent = stepIndent(curLevelIndent)
                    it.text = "{\n${curLevelIndent}\n}\n${stepIndent(curLevelIndent, -1)}"
                    val caretPosition = it.caretPosition
                    val newPosition = caretPosition + curLevelIndent.length + 1
                    it.selectRange(newPosition, newPosition)
                }

//                "}" -> {
//                    val codeLevel = findCurNestingLevel(it, -2)
//                    it.text = "}\n${codeLevel}"
//                    val prevTextLength = it.prevTextLength
//                    val lastTwoSymbolsOfPrevTextWihtoutNewLine =
//                        it.controlText.slice(prevTextLength - 2 until prevTextLength)
//                    if (lastTwoSymbolsOfPrevTextWihtoutNewLine == "  ") {
//                        it.setRange(prevTextLength - 2, prevTextLength)   // step one level up. eat spaces.
//                        it.caretPosition -= 2
//                    }
//                    val caret = it.caretPosition
//                    it.selectRange(caret + it.text.length - 1, caret + it.text.length - 1)
//                }

                "\n" -> {
                    val codeLevel = findCurNestingLevel(it)
                    it.text = "\n${codeLevel}"
                    // one-character increment is already done for the caret. consider rest characters
                    val correctPosition = it.caretPosition + codeLevel.length
                    it.selectRange(correctPosition, correctPosition)
                }
            }
            it
        }
        code.setOnKeyTyped {
            if (it.code == KeyCode.ENTER) {
                code.text = format(code.text)
            }
        }
        // canvas

    }

    // caret is already corrected after the type
    // so make one step back
    val TextFormatter.Change.prevTextLength: Int
        get() = this.caretPosition - 1

    fun stepIndent(indent: String, step: Int): String {
        if (step > 0) {
            return indent + " ".repeat(step)
        }
        return indent.take(indent.length - 2 * abs(step))
    }

    fun findCurNestingLevel(change: TextFormatter.Change, gain: Int = 0): String {
        val prevText = change.controlText
        val prevTextLength = change.prevTextLength
        var innerLevel = 0
        for (i in prevTextLength - 1 downTo 0) {
            when (prevText[i]) {
                ' ' -> {
                    // first space in the row or adjoining other space
                    if (i < 1 || prevText[i - 1] == '\n' || prevText[i - 1] == ' ') {
                        innerLevel += 1
                    }
                }

                '\n' -> break
            }
        }
        // if type \n after {
        if (prevText[prevTextLength - 1] == '{') {
            innerLevel += 2
        }
        return " ".repeat((innerLevel + gain).coerceAtLeast(0))
    }

    fun findCurLevelIndent(change: TextFormatter.Change): String {
        val prevText = change.controlText
        val prevTextLength = change.prevTextLength
        var curLevelIndent = 0
        for (i in prevTextLength - 1 downTo 0) {
            when (prevText[i]) {
                ' ' -> {
                    // first space in entire text or the row or adjoining other space/new line
                    if (i < 1 || prevText[i - 1] == '\n' || prevText[i - 1] == ' ') {
                        curLevelIndent += 1
                    }
                }

                '\n' -> break
            }
        }
        // if type \n after {
        if (prevText[prevTextLength - 1] == '{') {
            curLevelIndent += 2
        }
        return " ".repeat(curLevelIndent)
    }

    fun format(text: String): String {
        var innerLevel = ""
        val charIterator = StringCharacterIterator(text)
        val builder = StringBuilder()
        // Create root
        while (charIterator.current() != CharacterIterator.DONE) {
            if (charIterator.current() == '{') {
                innerLevel += "  "
            } else if (charIterator.current() == '}') {
                innerLevel = innerLevel.dropLast(2)
            }
            builder.append(charIterator.current())
            if (charIterator.current() == '\n') {
                builder.append(innerLevel)
            }
            charIterator.next()
        }

        return builder.toString()
    }

    fun parser(text: String): Parent {
        var root: Pane? = null
        var freshNode: Node? = null
        var curNode: Node? = null
        val charIterator = StringCharacterIterator(text)
        val builder = StringBuilder()
        // Create root
        val token = readToken(charIterator, builder)
        root = tokenMapper(token) as Pane
        curNode = root
        freshNode = root
        // Fill the tree
        while (charIterator.current() != CharacterIterator.DONE) {
            if (charIterator.current().isNotVoid()) {
                when (charIterator.current()) {
                    '{' -> curNode = freshNode
                    '}' -> curNode = curNode?.parent
                    else -> {
                        var isProperty = false
                        if (charIterator.current() == '_') {
                            isProperty = true
                            charIterator.next()
                        }
                        val token = readToken(charIterator, builder)
                        if (isProperty) {
                            while (charIterator.current() == ' ' || charIterator.current() == '=') {
                                charIterator.next()
                            }
                            val value = readValue(charIterator, builder)
                            curNode?.modifyOrValue(token, value)
                        } else {
                            freshNode = tokenMapper(token)
                            (curNode as? Pane)?.children?.add(freshNode)
                        }
                    }
                }
            }
            charIterator.next()
        }

        return root
    }

    fun readToken(characterIterator: CharacterIterator, builder: StringBuilder): String {
        while (characterIterator.current() in 'A'..'z') {
            builder.append(characterIterator.current())
            characterIterator.next()
        }
        val token = builder.toString()
        builder.setLength(0)
        return token
    }

    fun readValue(characterIterator: CharacterIterator, builder: StringBuilder): String {
        if (characterIterator.current() == '"') {
            characterIterator.next()
            while (characterIterator.current() != '"') {
                builder.append(characterIterator.current())
                characterIterator.next()
            }
        } else {
            while (characterIterator.current() == '.' || characterIterator.current() in '0'..'z') {
                builder.append(characterIterator.current())
                characterIterator.next()
            }
        }
        val value = builder.toString()
        builder.setLength(0)
        return value
    }

    fun Char.isNotVoid() = !(this == ' ' || this == '\n' || this == '\r')

    fun tokenMapper(token: String): Node = when (token) {
        "vbox" -> VBox()
        "hbox" -> HBox()
        "label" -> Label()
        "button" -> Button()
        else -> Pane()
    }

    fun Node.modifyOrValue(name: String, value: String) {
        when (name) {
            "width" -> this.prefWidth(value.toDouble())
            "height" -> this.prefHeight(value.toDouble())
            "text" -> {
                when (this) {
                    is Button -> this.text = value
                    is Label -> this.text = value
                }
            }

            "backgroundColor" -> this.style += "-fx-background-color: $value;"
        }
    }
}

fun String.showAllSymbols() = this.replace(" ", "_").replace("\n", "<N>")