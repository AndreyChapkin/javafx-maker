package org.my.state

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.my.concurrent.getAsync
import org.my.dto.ArticleDto
import org.my.dto.FragmentDto
import org.my.dto.TransliterationDto
import org.my.model.ArticleModel
import org.my.model.CatalogModel
import org.my.util.JsonSerializer
import org.my.util.ListChange

object KnowledgeLibraryState {

    const val FILE_PATH = "D:\\Temp\\KnowledgeDtos.json"

    val emptyCatalog = CatalogModel(-1, "")

    var uniqId = 0L

    val catalogs: ObservableList<CatalogModel> = FXCollections.observableList(mutableListOf())
    /*FXCollections.observableList(
        JsonSerializer.readFromFile<List<CatalogDto>>(FILE_PATH)?.toMutableList()
            ?: mutableListOf()
    )*/
    val isLoadingProp: BooleanProperty = SimpleBooleanProperty(false)
    val isPersisted: BooleanProperty = SimpleBooleanProperty(true)

    init {
        catalogs.addListener { _: ListChange<CatalogModel> ->
            if (!isLoadingProp.value) {
                isPersisted.set(false)
            }
        }
    }

    fun addArticlesChangeListener(catalog: CatalogModel, listener: (List<ArticleModel>) -> Unit) {
        catalogs.find { it == catalog }?.let {
            it.articles.addListener { _: ListChange<ArticleModel> ->
                if (!isLoadingProp.value) {
                    listener(it.articles)
                }
            }
            isLoadingProp.addListener { _, _, newValue ->
                if (!newValue) {
                    listener(it.articles)
                }
            }
        }
    }

    fun reserveUniqId() = uniqId++

    fun addCatalogsChangeListener(listener: (List<CatalogModel>) -> Unit) {
        catalogs.addListener { _: ListChange<CatalogModel> ->
            if (!isLoadingProp.value) {
                listener(catalogs)
            }
        }
        isLoadingProp.addListener { _, _, newValue ->
            if (!newValue) {
                listener(catalogs)
            }
        }
    }

    fun addCatalog(catalog: CatalogModel) {
        catalogs.add(catalog)
    }

    fun updateCatalog(catalog: CatalogModel) {
        val indexOfUpdatedCatalog = catalogs.indexOf(catalog)
        catalogs[indexOfUpdatedCatalog] = emptyCatalog
        catalogs[indexOfUpdatedCatalog] = catalog
    }

    fun removeCatalog(catalog: CatalogModel) {
        catalogs.remove(catalog)
    }

    fun addArticle(catalog: CatalogModel, article: ArticleModel) {
        catalog.articles.add(article)
    }

    fun removeArticle(article: ArticleModel) {
        article.catalog.value.articles.remove(article)
    }

    fun addFragment(articleDto: ArticleDto, fragmentDto: FragmentDto) {
        fragmentDto.article.set(articleDto)
        articleDto.fragments.add(fragmentDto)
    }

    fun removeFragment(articleDto: ArticleDto, fragmentDto: FragmentDto) {
        articleDto.fragments.remove(fragmentDto)
    }

    fun addDataChangeListener(listener: (List<CatalogModel>) -> Unit) {
        catalogs.addListener { _: ListChange<CatalogModel> ->
            if (!isLoadingProp.value) {
                listener(catalogs)
            }
        }
        isLoadingProp.addListener { _, _, newValue ->
            if (!newValue) {
                listener(catalogs)
            }
        }
    }

    // region async code

    private val asyncListProducer = {
        JsonSerializer
            .readFromFile<List<CatalogModel>>(FILE_PATH)
            ?.toMutableList()
            ?: mutableListOf()
    }

    private val asyncListConsumer = { list: List<CatalogModel> ->
        catalogs.addAll(list)
        isPersisted.set(true)
        isLoadingProp.set(false)
    }
    // endregion

    fun save() {
        JsonSerializer.writeToFile(catalogs, FILE_PATH)
        isPersisted.set(true)
    }

    fun load() {
        isLoadingProp.set(true)
        catalogs.clear()
        getAsync(asyncListProducer, asyncListConsumer)
    }
}