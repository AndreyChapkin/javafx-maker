package org.my.state

object IdSequence {
    private var id = 0L
    val uniqId: Long
        get() = id++
}