package org.conservationco.asana.serialization.customfield

import org.conservationco.asana.util.associateByNotNull
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

class CustomFieldTransferStrategy(
    kClass: KClass<*>
) {

    val properties: Map<String, KProperty1<out Any, *>> = serializableCustomFieldProperties(kClass)

    operator fun get(name: String): KProperty1<out Any, *>? {
        return properties[name]
    }

    private fun serializableCustomFieldProperties(kClass: KClass<*>): Map<String, KProperty1<out Any, *>> {
        return kClass.memberProperties.associateByNotNull( { it.getCustomFieldAnnotation()?.name }, { it } )
    }

    private fun KProperty1<out Any, *>.getCustomFieldAnnotation(): AsanaCustomField? = findAnnotation()

}
