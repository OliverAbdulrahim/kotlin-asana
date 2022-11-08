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

/**
 * Class that (de)serializes Asana [Task] objects into data objects.
 *
 * @param T The type of the data class.
 * @property class The type class of the data class.
 * @property context The source of `CustomField` objects for this serializer.
 */
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

    /**
     * Serializes the given [source] ([T]) object, returning the [Task] result.
     */
    override fun serialize(source: T): Task {
        return Task().apply {
            customFields = source.convertPropertiesToCustomFields()
            name = source.name
            gid = source.id
        }
    }

    /**
     * Serializes the given [source] ([T]) object, then applies the given [runAfter] function, and finally returns the
     * [Task] result.
     */
    override fun serialize(source: T, runAfter: (source: T, destination: Task) -> Unit): Task {
        return serialize(source).apply { runAfter(source, this@apply) }
    }

    /**
     * Deserializes the given [source] ([Task]) object, returning the [T] result.
     */
    override fun deserialize(source: Task): T {
        return `class`.createInstance().apply {
            source.customFields?.forEach {
                val property = strategy[it.name]
                if (property != null) setProperty(this@apply, property, it.inferValue(context))
            }
            name = source.name.orEmpty()
            id = source.gid.orEmpty()
        }
    }

    /**
     * Deserializes the given [source] ([Task]) object, then applies the given [runAfter] function, and finally returns
     * the [T] result.
     */
    override fun deserialize(source: Task, runAfter: (source: Task, destination: T) -> Unit): T {
        return deserialize(source).apply { runAfter(source, this@apply) }
    }

    /**
     * Returns a [CustomField] list containing all properties of the [T] receiver object converted by this
     * [AsanaTaskSerializer]'s custom field [context].
     */
    private fun T.convertPropertiesToCustomFields(): List<CustomField> {
        return strategy.properties.entries.map { entry -> this.buildCustomFieldFrom(entry.key, entry.value) }
    }

    /**
     * Returns a single [CustomField] that represents the given [property] of the [T] receiver object, using that
     * property's [AsanaCustomField] annotation.
     *
     * @see AsanaCustomField
     * @throws CustomFieldException If no custom field in this object's [context] matches the name supplied in the
     *                              given property's [AsanaCustomField] annotation declaration.
     */
    private fun T.buildCustomFieldFrom(propertyName: String, property: KProperty1<out Any, *>): CustomField {
        val customField = context[propertyName] ?: throw CustomFieldException(
            "No custom field in $context\n\tmatches name=$property"
        )
        val value = property.getter.call(this)
        customField.asResourceSubtype(context).applyDataTo(customField, value)
        return customField
    }

}
