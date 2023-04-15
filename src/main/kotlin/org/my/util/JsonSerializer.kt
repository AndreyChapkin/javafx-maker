package org.my.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File
import java.io.FileOutputStream

object JsonSerializer {

    val mapper = jacksonObjectMapper()
    val prettier = DefaultPrettyPrinter().apply {
        indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE)
        indentObjectsWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE)
    }

    fun toJsonString(obj: Any) = mapper.writeValueAsString(obj)

    inline fun <reified T> readFromFile(path: String): T? = File(path).let {
        if (it.exists()) mapper.readValue(it, object : TypeReference<T>() {}) else null
    }

    fun <T> writeToFile(obj: T, path: String) {
        val file = File(path).apply { createNewFile() }
        FileOutputStream(file).use {
            mapper.writer(prettier).writeValue(it, obj)
        }
    }
}