package org.my.util.html

import javafx.concurrent.Worker
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventTarget
import org.w3c.dom.html.HTMLDivElement
import org.w3c.dom.html.HTMLElement

fun WebView.modify(filler: WebEngine.() -> Unit) {
    this.engine.filler()
}

fun WebView.load(htmlUrl: String, documentLoadedCallback: Document.() -> Unit) {
    this.engine.load(htmlUrl)
    this.engine.onDocumentLoaded(documentLoadedCallback)
}

fun WebEngine.styleWith(cssLocation: String) {
    this.userStyleSheetLocation = cssLocation
}

inline fun WebEngine.onDocumentLoaded(crossinline filler: Document.() -> Unit) {
    this.loadWorker.stateProperty().addListener { _, _, newState ->
        if (newState === Worker.State.SUCCEEDED) {
            this.document.filler()
        }
    }
}

inline fun WebView.modifyDocument(crossinline filler: Document.() -> Unit) {
    this.engine.document.filler()
}

fun HTMLElement.addStyles(vararg classNames: String) {
    var resultClasses = this.className
    if (classNames.size > 0) {
        resultClasses += " ${classNames.joinToString(" ")}"
    }
    this.className = resultClasses
}

fun HTMLElement.removeStyles(vararg classNames: String) {
    var curClassNames = this.className
    classNames.forEach {
        curClassNames = curClassNames.replace(it, "")
    }
    this.className = curClassNames
}

inline fun <reified T> Document.findById(id: String): T? {
    return this.getElementById(id) as? T
}

inline fun <reified T> WebEngine.findBySelector(cssSelector: String): T? {
    return this.executeScript("document.querySelector(${cssSelector})") as T
}

fun Document.createDiv(id: String? = null, classes: Array<String>) : HTMLDivElement {
    val newDiv = this.createElement("div") as HTMLDivElement
    id?.let { newDiv.id = id }
    newDiv.addStyles(*classes)
    return newDiv
}

fun Element.removeLastChild() {
    this.removeChild(this.lastChild)
}

fun EventTarget.addClickFilter(listener: (Event) -> Unit) {
    this.addEventListener("click", listener, true)
}

fun EventTarget.addClickListener(listener: (Event) -> Unit) {
    this.addEventListener("click", listener, false)
}