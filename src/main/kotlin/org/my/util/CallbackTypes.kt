package org.my.util

import javafx.collections.ListChangeListener
import javafx.event.ActionEvent
import javafx.scene.input.MouseEvent
import org.my.dto.CatalogDto
import org.my.dto.TransliterationDto
import org.my.model.CatalogModel

typealias SimpleCallback = () -> Unit
//typealias ListChangeListener<T> = (ListChangeListener.Change<out T>) -> Unit
typealias ListChange<T> = ListChangeListener.Change<out T>
typealias ActionCallback = (ActionEvent) -> Unit
typealias MouseClickedCallback = (MouseEvent) -> Unit
typealias TransliterationDtoCallback = (TransliterationDto) -> Unit
typealias CatalogCallback = (CatalogModel) -> Unit
typealias ChangeTransliterationDtoCallback = (TransliterationDto, TransliterationDto) -> Unit
typealias ChangeCatalogCallback = (CatalogModel, CatalogModel) -> Unit