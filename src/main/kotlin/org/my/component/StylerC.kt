package org.my.component

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.Pane
import org.my.base.BaseComponent
import org.my.model.StyleModel
import org.my.util.addStylesheets
import org.my.util.vboxAln
import java.io.File
import java.lang.Exception
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds
import java.util.concurrent.CompletableFuture

class StylerC(styleModel: StyleModel) : BaseComponent<StyleModel>(SimpleObjectProperty(styleModel)) {

    override fun layoutElements(): Pane = vboxAln {

    }

    init {
        watchCssFiles()
    }

    /**
     *  Watches for directory containing css file/-es and dynamically applies the css
     */
    private fun watchCssFiles() {
        val watchingRunnable = {
            try {
                val watchService = FileSystems.getDefault().newWatchService()
//                val cssFilePath = Paths.get(model.cssFileUrl.value)
//                val cssFileDirectoryPath = cssFilePath.parent
//                val cssFileName = cssFilePath.fileName
                val watchableCssDir = Paths.get(watchableCssDir())
                var key = watchableCssDir.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                )
                println("@@@ Start watching for css dir: $watchableCssDir")
                while (key != null && key.isValid) {
                    var modifiedCssStylesheet: String? = null
                    for (event in key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                            continue
                        }
                        val eventKind = event.kind().name()
                        val touchedFilename = event.context().toString()
                        modifiedCssStylesheet = this.root.stylesheets.find { it.endsWith(touchedFilename) }
                        if ("ENTRY_MODIFY" == eventKind && modifiedCssStylesheet != null) {
                            println("@@@ Detected css file modification: $modifiedCssStylesheet")
                            break
                        }
                    }
                    if (modifiedCssStylesheet != null) {
                        root.stylesheets.remove(modifiedCssStylesheet)
                        root.addStylesheets(modifiedCssStylesheet)
                    }
                    key.reset()
                    key = watchService.take()
                }
                println("@@@ Watch service is finished")
            } catch (e: Exception) {
                println("@@@ Watching thread was interrupted ${e.message}")
            }
        }
        CompletableFuture.runAsync(watchingRunnable)
    }

    fun watchableCssDir(): String {
        val currentWorkingDir = System.getProperty("user.dir")
        val classPackageDirs = StylerC::class.qualifiedName?.let {
            // get full package name and convert to directory path
            it.substringBeforeLast(".").replace(".", "\\")
        } ?: ""
        // fullCssFilePath
        return "$currentWorkingDir\\src\\main\\resources\\$classPackageDirs"
    }

    fun toFullCssFilePath(componentClassName: String): String {
        // fullCssFilePath
        return "${watchableCssDir()}\\$componentClassName.css"
    }

    fun toLocalFileUrl(localFilePath: String): String {
        val localFileUrl = "file:///$localFilePath"
        // replace backward slashes to forward ones
        return localFileUrl.replace("\\", "/")
    }

    /**
     * Recursively iterate through all level children,
     * detects all nodes containing className that ends with -c - writes down them.
     */
    fun loadAllComponentCssFiles(parent: Pane) {
        parent.children.forEach { node ->
            val componentClassName = node.styleClass.find { it.endsWith("-c") }
            if (componentClassName != null) {
                val componentCssFilePath = toFullCssFilePath(componentClassName)
                println("@@@ look for component css: $componentCssFilePath")
                if (Files.exists(Paths.get(componentCssFilePath))) {
                    val componentCssFileUrl = toLocalFileUrl(componentCssFilePath)
                    println("@@@ use component css: $componentCssFileUrl")
                    this.root.addStylesheets(componentCssFileUrl)
                }
            }
            if (node is Pane) {
                loadAllComponentCssFiles(node)
            }
        }
    }
}

fun Pane.styler(styleModel: StyleModel, filler: (Pane.() -> Unit)? = null): StylerC =
    StylerC(styleModel).also {
        it.initialize()
        it.appendTo(this)
        if (filler != null) {
            it.root.filler()
            it.loadAllComponentCssFiles(it.root)
        }
    }

fun stylerAln(styleModel: StyleModel, filler: (Pane.() -> Unit)? = null): StylerC =
    StylerC(styleModel).also {
        it.initialize()
        if (filler != null) {
            it.root.filler()
            it.loadAllComponentCssFiles(it.root)
        }
    }