package org.my.dto

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.io.File
import java.time.LocalDateTime

class CatalogDto(
    val id: Long,
    var name: String,
    var parent: ObjectProperty<CatalogDto?> = SimpleObjectProperty(),
    var articles: ObservableList<ArticleDto> = FXCollections.observableList(listOf()),
    var childs: ObservableList<CatalogDto> = FXCollections.observableList(listOf()),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CatalogDto

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

class ArticleDto(
    val id: Long,
    var title: String,
    var fragments: ObservableList<FragmentDto> = FXCollections.observableList(listOf()),
    var createDate: LocalDateTime? = null,
    var updateDate: LocalDateTime? = null,
    var catalog: ObjectProperty<CatalogDto> = SimpleObjectProperty(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArticleDto

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

class FragmentDto(
    val id: Long,
    var text: String,
    var article: ObjectProperty<ArticleDto> = SimpleObjectProperty(),
    var pictures: List<File>,
    var source: KnowledgeSource?,
    var locationInSource: String?,
    var linkedFragment: ObjectProperty<FragmentDto?> = SimpleObjectProperty(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FragmentDto

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

sealed class KnowledgeSource(val sourceUrl: String)

class Book(
    sourceUrl: String,
    val name: String,
    val publishingYear: Int,
    val authors: Collection<String>,
): KnowledgeSource(sourceUrl)

class SitePage(
    sourceUrl: String,
    val name: String,
): KnowledgeSource(sourceUrl)