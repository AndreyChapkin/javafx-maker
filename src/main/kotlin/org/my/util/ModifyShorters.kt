package org.my.util

fun <T> String.classpathToUrl(classForClassLoader: Class<T>): String? = classForClassLoader
    .classLoader
    .getResource(this)?.toExternalForm()