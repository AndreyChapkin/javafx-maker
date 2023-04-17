package org.my.util

import javafx.beans.property.Property
import javafx.collections.ObservableList
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.text.Text
import javafx.scene.web.WebView
import javafx.util.Callback
import javafx.util.StringConverter
import org.my.dto.TransliterationDto

fun <T : Node> T.alsoAddTo(parent: Pane): T {
    parent.children.add(this)
    return this
}

// region Styles

fun <T : Parent> T.addStylesheets(vararg stylesheetsPaths: String): T = this.apply {
    stylesheets.addAll(stylesheetsPaths)
}

fun <T : Node> T.addStyles(vararg styleClasses: String): T = this.apply { this.styleClass.addAll(styleClasses) }

fun <T : Node> T.removeStyles(vararg styleClasses: String): T =
    this.apply { styleClass.removeAll(styleClasses.toSet()) }

// endregion

// region Pane

// endregion Pane

inline fun paneAln(filler: Pane.() -> Unit) = Pane()
    .apply(filler)
    .apply {
        addStyles("pane")
    }

inline fun Pane.pane(filler: Pane.() -> Unit) = paneAln(filler)
    .alsoAddTo(this)

// region VBox

inline fun vboxAln(filler: VBox.() -> Unit) = VBox()
    .apply(filler)
    .apply {
        addStyles("vbox")
    }

inline fun Pane.vbox(filler: VBox.() -> Unit) = VBox()
    .apply(filler)
    .apply {
        addStyles("vbox")
    }
    .alsoAddTo(this)

// endregion VBox

// region HBox

inline fun hboxAln(filler: HBox.() -> Unit) = HBox()
    .apply(filler)
    .apply {
        addStyles("hbox")
    }

inline fun Pane.hbox(filler: HBox.() -> Unit) = HBox()
    .apply(filler)
    .apply {
        addStyles("hbox")
    }
    .alsoAddTo(this)

// endregion HBox

// region GridPane

inline fun gridPaneAln(filler: GridPane.() -> Unit) = GridPane().apply(filler)

inline fun Pane.gridPane(filler: GridPane.() -> Unit) = GridPane().apply(filler).alsoAddTo(this)

class RowInfo(val gridPane: GridPane) {
    val rowIndex = gridPane.rowCount
    var columnIndex = 0

    fun emptyCell() {
        columnIndex++
    }

    inline fun newCell(nodeProducer: () -> Node) {
        gridPane.add(nodeProducer(), rowIndex, columnIndex++)
    }
}

inline fun GridPane.newRow(filler: RowInfo.() -> Unit) {
    val rowInfo = RowInfo(this)
    rowInfo.filler()
}

inline fun GridPane.allToColumn(filler: Pane.() -> Unit) {
    val tempParent = Pane()
    tempParent.filler()
    val curColumnCount = this.columnCount
    val nodes = tempParent.children.toTypedArray()
    tempParent.children.clear()
    addColumn(curColumnCount, *nodes)
}

inline fun GridPane.allToRow(filler: Pane.() -> Unit) {
    val tempParent = Pane()
    tempParent.filler()
    val curRowCount = this.rowCount
    val nodes = tempParent.children.toTypedArray()
    tempParent.children.clear()
    addRow(curRowCount, *nodes)
}

// endregion

// region StackPane

inline fun stackPaneAln(filler: StackPane.() -> Unit) = StackPane().apply(filler)

inline fun Pane.stackPane(filler: StackPane.() -> Unit) = StackPane().apply(filler).alsoAddTo(this)

// endregion StackPane

// region BorderPane

inline fun Pane.borderPane(filler: BorderPane.() -> Unit) = BorderPane().apply(filler).alsoAddTo(this)

inline fun borderPaneAln(filler: BorderPane.() -> Unit) = BorderPane().apply(filler)

// endregion BorderPane

// region FlowPane

inline fun flowPaneAln(filler: FlowPane.() -> Unit) = FlowPane().apply(filler)

inline fun Pane.flowPane(filler: FlowPane.() -> Unit) = FlowPane().apply(filler).alsoAddTo(this)

// endregion

// region SplitPane

inline fun Pane.splitPane(filler: Pane.() -> Unit): SplitPane {
    val splitPane = SplitPane().alsoAddTo(this)
    val tempParent = Pane().apply(filler)
    val childs = tempParent.childrenUnmodifiable
    splitPane.items.addAll(childs)
    tempParent.children.clear()
    return splitPane
}

inline fun splitPaneAln(filler: Pane.() -> Unit): SplitPane {
    val splitPane = SplitPane()
    val tempParent = Pane().apply(filler)
    val childs = tempParent.childrenUnmodifiable
    splitPane.items.addAll(childs)
    tempParent.children.clear()
    return splitPane
}

// endregion

// region WebView

fun webViewAln() = WebView()

fun Pane.webView() = WebView().alsoAddTo(this)

// endregion

// region Texts

fun Pane.label(text: String? = null, filler: (Label.() -> Unit)? = null): Label = Label(text)
    .alsoAddTo(this)
    .also {
        if (filler != null) {
            it.filler()
        }
    }

fun Pane.labelAln(text: String? = null, filler: (Label.() -> Unit)? = null): Label = Label(text)
    .also {
        if (filler != null) {
            it.filler()
        }
    }

// TODO: choose right and necessary styles
fun Pane.selectableText(text: String? = null, filler: (TextField.() -> Unit)? = null): TextField =
    TextField(text).apply {
        style = "-fx-faint-focus-color: transparent;" +
            "-fx-focus-color: transparent;" +
            "-fx-display-caret: false;" +
            "-fx-background-color: transparent;"
    }.alsoAddTo(this)
        .also {
            if (filler != null) {
                it.filler()
            }
        }

fun Pane.selectableTextArea(text: String? = null, filler: (TextArea.() -> Unit)? = null): TextArea =
    TextArea(text).apply {
        style = "-fx-faint-focus-color: transparent;" +
            "-fx-focus-color: transparent;" +
            "-fx-display-caret: false;" +
            "-fx-background-color: transparent;"
    }.alsoAddTo(this)
        .also {
            if (filler != null) {
                it.filler()
            }
        }

fun Pane.text(value: Any? = null, filler: (Text.() -> Unit)? = null): Text {
    val textValue = when (value) {
        is Property<*> -> value.value.toString()
        is Any -> value.toString()
        else -> ""
    }
    return Text(textValue)
        .alsoAddTo(this)
        .also {
            if (filler != null) {
                it.filler()
            }
        }
}

fun textAln(text: String? = null, filler: (Text.() -> Unit)? = null): Text =
    Text(text)
        .also {
            if (filler != null) {
                it.filler()
            }
        }

fun Pane.textField(): TextField = TextField().alsoAddTo(this)

fun Pane.textField(filler: (TextField.() -> Unit)? = null): TextField = TextField()
    .alsoAddTo(this)
    .also {
        if (filler != null) {
            it.filler()
        }
    }

fun Pane.textArea(): TextArea = TextArea().alsoAddTo(this)

fun Pane.textArea(filler: (TextArea.() -> Unit)? = null): TextArea = TextArea()
    .alsoAddTo(this)
    .also {
        if (filler != null) {
            it.filler()
        }
    }

// endregion

// region Image

fun Pane.imageView(image: Image, filler: (ImageView.() -> Unit)? = null): ImageView = ImageView(image)
    .alsoAddTo(this)
    .also {
        if (filler != null) {
            it.filler()
        }
    }

fun imageViewAln(image: Image, filler: (ImageView.() -> Unit)? = null): ImageView = ImageView(image)
    .also {
        if (filler != null) {
            it.filler()
        }
    }


// endregion Image

// region Buttons

fun Pane.button(text: String? = null, filler: (Button.() -> Unit)? = null): Button = Button(text)
    .alsoAddTo(this)
    .also {
        if (filler != null) {
            it.filler()
        }
    }

// endregion

// region Group

fun Pane.group(filler: (Pane.() -> Unit)? = null): Group = Group()
    .alsoAddTo(this)
    .also {
        val tempParent = Pane()
        if (filler != null) {
            tempParent.filler()
        }
        it.children.addAll(tempParent.children)
        tempParent.children.clear()
    }

// endregion

// region EditableListView

fun Pane.editableListView(
    converter: StringConverter<TransliterationDto>,
    items: ObservableList<TransliterationDto>
): ListView<TransliterationDto> =
    ListView<TransliterationDto>().also {
        it.isEditable = true
        it.cellFactory = CustomCellFactory()//TextFieldListCell.forListView(converter)
        it.items = items
    }.alsoAddTo(this)


class CustomCellFactory() : Callback<ListView<TransliterationDto>, ListCell<TransliterationDto>> {
    override fun call(param: ListView<TransliterationDto>?): ListCell<TransliterationDto> {
        return CustomListCell()
    }
}

class CustomListCell() : ListCell<TransliterationDto>() {

    override fun updateItem(item: TransliterationDto?, empty: Boolean) {
        super.updateItem(item, empty)
        item?.let {
            children.addAll(
                TextField().apply { text = it.source },
                TextField().apply { text = it.transliteration },
            )
        }
    }

}

// endregion