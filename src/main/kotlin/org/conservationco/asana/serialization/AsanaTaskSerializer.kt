package org.conservationco.asana.serialization

import com.asana.models.CustomField
import com.asana.models.Task
import org.conservationco.asana.serialization.customfield.CustomFieldSerializable
import org.conservationco.asana.serialization.customfield.CustomFieldTransferStrategy
import org.conservationco.asana.serialization.customfield.context.CustomFieldContext
import org.conservationco.asana.serialization.customfield.context.CustomFieldContextException
import org.conservationco.asana.serialization.customfield.context.NoOpCustomFieldContext
import org.conservationco.asana.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation

class AsanaTaskSerializer<T : AsanaSerializable<T>>(
    private val context: CustomFieldContext,
    private val `class`: KClass<T>,
) : AsanaSerializer<Task, T> {

    private val strategy = CustomFieldTransferStrategy(`class`)

    init {
        if (context is NoOpCustomFieldContext) throw CustomFieldContextException(
            """
                The given context [$context] cannot be used to serialize tasks and data objects.
                 You must supply a valid CustomFieldContext to be able to serialize ${`class`.qualifiedName}."
            """.trimIndent()
        )
    }

    override fun serialize(source: T, alsoApply: Task.(source: T) -> Task): Task {
        val destination = Task().apply {
            customFields = source.convertPropertiesToCustomFields()
            name = source.name
            gid = source.id
        }
        return destination.alsoApply(source)
    }

    override fun deserialize(source: Task, alsoApply: T.(source: Task) -> T): T {
        val destination: T = `class`.createInstance()
        source.customFields.forEach {
            val property = strategy[it.name]
            if (property != null) setProperty(destination, property, it.inferValue(context))
        }
        return destination
            .apply { this.id = source.gid }
            .alsoApply(source)
    }

    private fun T.convertPropertiesToCustomFields(): List<CustomField> {
        return strategy.properties.values.map { property -> this.buildCustomFieldFrom(property) }
    }

    private fun T.buildCustomFieldFrom(property: KProperty1<out Any, *>): CustomField {
        val serializedName = property.findAnnotation<CustomFieldSerializable>()?.name!!
        val customField = context[serializedName]!!
        val value = property.getter.call(this)

        customField.asResourceSubtype(context).applyDataTo(customField, serializedName, value)
        return customField
    }

}
