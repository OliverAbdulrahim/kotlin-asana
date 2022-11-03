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

    override fun serialize(source: T, runAfter: (source: T, destination: Task) -> Unit): Task {
        return serialize(source).apply { runAfter(source, this@apply) }
    }

    override fun deserialize(source: Task): T {
        val destination: T = `class`.createInstance()
        source.customFields.forEach {
            val property = strategy[it.name]
            if (property != null) setProperty(destination, property, it.inferValue(context))
        }
        return destination.apply {
            name = source.name.orEmpty()
            id = source.gid.orEmpty()
        }
    }

    override fun deserialize(source: Task, runAfter: (source: Task, destination: T) -> Unit): T {
        return deserialize(source).apply { runAfter(source, this@apply) }
    }

    private fun T.convertPropertiesToCustomFields(): List<CustomField> {
        return strategy.properties.entries.map { entry -> this.buildCustomFieldFrom(entry.key, entry.value) }
    }

    private fun T.buildCustomFieldFrom(propertyName: String, property: KProperty1<out Any, *>): CustomField {
        val customField = context[propertyName] ?: throw CustomFieldException(
            "No custom field in $context\n\tmatches name=$property"
        )
        val value = property.getter.call(this)
        customField.asResourceSubtype(context).applyDataTo(customField, value)
        return customField
    }

}
