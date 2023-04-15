package org.my.model

import javafx.beans.property.ListProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import org.my.base.BaseModel
import java.time.OffsetDateTime

class ToDoStateM() : BaseModel() {
    lateinit var todos: ListProperty<ToDoItemM>

    init {
        initAllProperties()
    }
}

class ToDoM() : BaseModel() {

    lateinit var name: StringProperty
    lateinit var parent: ObjectProperty<CatalogModel?>
    lateinit var articles: ListProperty<ArticleModel>
    lateinit var childs: ListProperty<CatalogModel>

    init {
        initAllProperties()
    }

    constructor(
        id: Long,
        name: String,
        parent: CatalogModel? = null,
        articles: MutableList<ArticleModel> = mutableListOf(),
        childs: MutableList<CatalogModel> = mutableListOf(),
    ) : this() {
        this.name.set(name)
        this.parent.set(parent)
        this.articles.set(FXCollections.observableList(articles))
        this.childs.set(FXCollections.observableList(childs))
    }
}

enum class ToDoItemStatus {
    ON_HOLD, NEED_ATTENTION, IN_PROGRESS,
}

class ToDoItemM() : BaseModel() {

    lateinit var short: StringProperty
    lateinit var moreInfo: StringProperty
    lateinit var status: ObjectProperty<ToDoItemStatus>
    lateinit var childItems: ListProperty<ToDoItemM>
    lateinit var createDate: ObjectProperty<OffsetDateTime?>
    lateinit var updateDate: ObjectProperty<OffsetDateTime?>

    init {
        initAllProperties()
    }

    constructor(
        id: Long,
        short: String = "What to do?",
        moreInfo: String = "",
        childItems: List<ToDoItemM> = listOf<ToDoItemM>(),
        createDate: OffsetDateTime? = OffsetDateTime.now(),
        updateDate: OffsetDateTime? = OffsetDateTime.now(),
    ) : this() {
        this.id = id
        this.short.set(short)
        this.moreInfo.set(moreInfo)
        this.childItems.set(FXCollections.observableList(childItems))
        this.createDate.set(createDate)
        this.updateDate.set(updateDate)
    }
}