package org.conservationco.asana.util

import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

fun Any.setProperty(property: KProperty1<out Any, *>?, value: Any?) {
    if (property is KMutableProperty<*>) property.setter.call(this, value)
}

fun getProperty(target: Any, propertyName: String): Any? {
    return findProperty(target, propertyName)!!.getter.call(target)
}

fun findProperty(target: Any, propertyName: String): KProperty1<out Any, *>? {
    return findProperty(target::class, propertyName)
}

fun findProperty(kClass: KClass<*>, propertyName: String): KProperty1<out Any, *>? {
    return findPropertyBy(kClass) { it.name == propertyName }
}

fun findPropertyBy(kClass: KClass<*>, predicate: (KProperty1<out Any, *>) -> Boolean): KProperty1<out Any, *>? {
    return kClass.memberProperties.find(predicate)
}
