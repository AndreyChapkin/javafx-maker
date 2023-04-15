package org.my.concurrent

import javafx.concurrent.Task
import java.util.concurrent.CompletableFuture

inline fun <T> getAsync(crossinline asyncProducer: () -> T, crossinline consumer: (T) -> Unit) {
    val task = object : Task<T>() {
        init {
            setOnSucceeded { consumer(value) }
        }
        override fun call(): T = asyncProducer()
    }
    CompletableFuture.runAsync(task)
}

// TODO: not work
inline fun runAsync(crossinline asyncRunner: () -> Unit, crossinline modifyRunner: () -> Unit) {
    val task = object : Task<Unit>() {
        init {
            setOnSucceeded { modifyRunner() }
        }
        override fun call(): Unit = asyncRunner()
    }
    CompletableFuture.runAsync(task)
}