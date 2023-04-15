package org.my.model

import javafx.beans.property.ListProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import org.my.base.BaseModel
import java.io.File
import java.time.LocalDateTime

class CatalogModel() : BaseModel() {

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

class ArticleModel(id: Long) : BaseModel() {

    lateinit var title: StringProperty
    lateinit var fragments: ListProperty<FragmentModel>
    lateinit var createDate: ObjectProperty<LocalDateTime?>
    lateinit var updateDate: ObjectProperty<LocalDateTime?>
    lateinit var catalog: ObjectProperty<CatalogModel>

    init {
        initAllProperties()
    }

    constructor(
        id: Long,
        title: String,
        fragments: MutableList<FragmentModel> = mutableListOf(),
        createDate: LocalDateTime? = null,
        updateDate: LocalDateTime? = null,
        catalog: CatalogModel,
    ) : this(id) {
        this.title.set(title)
        this.fragments.set(FXCollections.observableList(fragments))
        this.createDate.set(createDate)
        this.updateDate.set(updateDate)
        this.catalog.set(catalog)
    }
}

class FragmentModel(id: Long) : BaseModel() {

    lateinit var text: StringProperty
    lateinit var article: ObjectProperty<ArticleModel>
    lateinit var pictures: ListProperty<File>
    lateinit var source: ObjectProperty<KnowledgeSource?>
    lateinit var locationInSource: StringProperty
    lateinit var linkedFragment: ObjectProperty<FragmentModel?>

    init {
        initAllProperties()
    }

    constructor(
        id: Long,
        text: String,
        article: ArticleModel,
        pictures: MutableList<File> = mutableListOf(),
        source: KnowledgeSource? = null,
        locationInSource: String = "",
        linkedFragment: FragmentModel? = null,
    ) : this(id) {
        this.text.set(text)
        this.article.set(article)
        this.pictures.set(FXCollections.observableList(pictures))
        this.source.set(source)
        this.locationInSource.set(locationInSource)
        this.linkedFragment.set(linkedFragment)
    }
}

sealed class KnowledgeSource(val sourceUrl: String, val name: String)

class Book(
    sourceUrl: String,
    name: String,
    val publishingYear: Int,
    val authors: Collection<String>,
) : KnowledgeSource(sourceUrl, name)

class SitePage(
    sourceUrl: String,
    name: String,
) : KnowledgeSource(sourceUrl, name)