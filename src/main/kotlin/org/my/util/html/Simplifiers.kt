package org.my.util.html

import org.w3c.dom.NodeList

inline fun <reified T> NodeList.forEach(consumer: (T) -> Unit) {
    for (i in 0 until length) {
        consumer(item(i) as T)
    }
}