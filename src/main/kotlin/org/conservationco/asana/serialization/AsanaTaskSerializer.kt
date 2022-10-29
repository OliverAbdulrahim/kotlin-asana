package org.conservationco.asana.serialization

import com.asana.models.CustomField
import com.asana.models.Task
import org.conservationco.asana.serialization.customfield.CustomFieldException
import org.conservationco.asana.serialization.customfield.AsanaCustomField
import org.conservationco.asana.serialization.customfield.CustomFieldTransferStrategy
import org.conservationco.asana.serialization.customfield.context.CustomFieldContext
import org.conservationco.asana.serialization.customfield.context.NoOpCustomFieldContext
import org.conservationco.asana.util.asResourceSubtype
import org.conservationco.asana.util.inferValue
import org.conservationco.asana.util.setProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation

class AsanaTaskSerializer<T : AsanaSerializable<T>>(
    private val `class`: KClass<out T>,
    private val context: CustomFieldContext,
) : AsanaSerializer<Task, T> {

    private val strategy = CustomFieldTransferStrategy(`class`)

    init {
        if (context is NoOpCustomFieldContext) throw CustomFieldException(
            """
                The given context [$context] cannot be used to serialize custom fields <-> data objects.
                 You must supply a Resource-based CustomFieldContext to be able to serialize ${`class`.qualifiedName}."
            """.trimIndent()
        )
    }

    override fun serialize(source: T): Task {
        val destination = Task().apply {
            customFields = source.convertPropertiesToCustomFields()
            name = source.name
            gid = source.id
        }
        return destination
    }

    override fun deserialize(source: Task): T {
        val destination: T = `class`.createInstance()
        source.customFields.forEach {
            val property = strategy[it.name]
            if (property != null) setProperty(destination, property, it.inferValue(context))
        }
        return destination.apply {
            name = source.name
            id = source.gid
        }
    }

    private fun T.convertPropertiesToCustomFields(): List<CustomField> {
        return strategy.properties.values.mapNotNull { property -> this.buildCustomFieldFrom(property) }
    }

    private fun T.buildCustomFieldFrom(property: KProperty1<out Any, *>): CustomField? {
        val serializedName = property.findAnnotation<AsanaCustomField>()?.name!!
        val customField = context[serializedName]
        if (customField != null) {
            val value = property.getter.call(this)
            customField.asResourceSubtype(context).applyDataTo(customField, serializedName, value)
        }
        return customField
    }

}
