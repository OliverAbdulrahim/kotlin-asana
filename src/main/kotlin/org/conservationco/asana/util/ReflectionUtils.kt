package org.conservationco.asana.util

import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

internal fun setProperty(obj: Any, property: KProperty1<out Any, *>?, value: Any?) {
    if (property is KMutableProperty<*>) property.setter.call(obj, value)
}

internal fun getProperty(target: Any, propertyName: String): Any? {
    return findProperty(target, propertyName)!!.getter.call(target)
}

internal fun findProperty(target: Any, propertyName: String): KProperty1<out Any, *>? {
    return findProperty(target::class, propertyName)
}

internal fun findProperty(kClass: KClass<*>, propertyName: String): KProperty1<out Any, *>? {
    return findPropertyBy(kClass) { it.name == propertyName }
}

internal fun findPropertyBy(kClass: KClass<*>, predicate: (KProperty1<out Any, *>) -> Boolean): KProperty1<out Any, *>? {
    return kClass.memberProperties.find(predicate)
}
