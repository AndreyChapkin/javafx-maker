package org.my.util

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType

fun KProperty<*>.doesReturnSubtypeOf(clazz: KClass<*>) =
    this.returnType.isSubtypeOf(clazz.starProjectedType)

fun Class<*>.getFirstGenericSuperclassTypeArgument(): Type = (this.genericSuperclass as ParameterizedType)
    .actualTypeArguments[0]