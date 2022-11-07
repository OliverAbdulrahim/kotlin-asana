package org.conservationco.asana.serialization.customfield

import com.asana.models.CustomField
import org.conservationco.asana.serialization.customfield.context.CustomFieldContext
import org.conservationco.asana.serialization.customfield.context.NoOpCustomFieldContext
import org.conservationco.asana.util.camelToSnakeCase
import org.conservationco.asana.util.multiEnumToGids
import org.conservationco.asana.util.stringValue
import kotlin.reflect.full.primaryConstructor

/**
 * Sealed hierarchy encapsulating operations on [CustomField] objects based on their [CustomField.resourceSubtype].
 *
 * @property context The custom field supplier for this object. This is needed to find possible values for "`enum`" and
 *                   "`multi_enum`" type fields, which are Project- or Workspace-dependent. Other types, such as
 *                   "`number`" and "`text`" are context-independent, and thus default to a non-operative context.
 */
sealed class ResourceSubtype (
    protected val context: CustomFieldContext = NoOpCustomFieldContext,
) {

    companion object {
        /** Stores type names (in `snake_case`) to their representative constructor. */
        val types = ResourceSubtype::class.sealedSubclasses
            .associateBy(
                { it.simpleName!!.camelToSnakeCase() },
                { it.primaryConstructor!! }
            )
    }

    /**
     * Converts the given [customField] into its representative globally unique identifier(s), returning either a
     * `String?` or `Array<String>?`.
     */
    abstract fun convertToGids(customField: CustomField): Any?

    /**
     * Converts the given [customField] into its representative data.
     *
     * [CustomField] objects store their true value in any number of internal fields, such as (non-exhaustive list):
     *   - [CustomField.textValue] for "`text`" resources
     *   - [CustomField.enumValue] for "`enum`" resources
     *   - [CustomField.multiEnumValues] for "`multi_enum`" resources
     */
    abstract fun convertToData(customField: CustomField): Any?

    /**
     * Applies the given [value] to the specified [customField].
     */
    abstract fun applyDataTo(customField: CustomField, value: Any?)

    class Number : ResourceSubtype() {
        override fun convertToGids(customField: CustomField): String = customField.numberValue.orEmpty()
        override fun convertToData(customField: CustomField): String = customField.numberValue.orEmpty()
        override fun applyDataTo(customField: CustomField, value: Any?) {
            customField.numberValue = value.stringValue().ifEmpty { "0" }
        }
    }

    class Text : ResourceSubtype() {
        override fun convertToGids(customField: CustomField): String = customField.textValue.orEmpty()
        override fun convertToData(customField: CustomField): String = customField.textValue.orEmpty()
        override fun applyDataTo(customField: CustomField, value: Any?) {
            customField.textValue = value.stringValue()
        }
    }

    class Enum(context: CustomFieldContext) : ResourceSubtype(context) {
        override fun convertToGids(customField: CustomField): String = customField.enumValue?.gid.orEmpty()
        override fun convertToData(customField: CustomField): String = customField.enumValue?.name.orEmpty()
        override fun applyDataTo(customField: CustomField, value: Any?) {
            customField.enumValue = context.optionForName(customField.name, value as String?)
        }
    }

    class MultiEnum(context: CustomFieldContext) : ResourceSubtype(context) {
        override fun convertToGids(customField: CustomField): Any = customField.multiEnumToGids()
        override fun convertToData(customField: CustomField): Array<String> {
            return customField.multiEnumValues.map { option -> option.name }.toTypedArray()
        }
        override fun applyDataTo(customField: CustomField, value: Any?) {
            val selectedMultiEnumNames = value as Array<*>?
            customField.multiEnumValues = selectedMultiEnumNames?.map { context.optionForName(customField.name, it as String) }
        }
    }

    class People(context: CustomFieldContext) : ResourceSubtype(context) {
        override fun convertToGids(customField: CustomField): Any = throwFor(this)
        override fun convertToData(customField: CustomField): Any = throwFor(this)
        override fun applyDataTo(customField: CustomField, value: Any?) = throwFor(this)
    }

    class Date : ResourceSubtype() {
        override fun convertToGids(customField: CustomField): Any = throwFor(this)
        override fun convertToData(customField: CustomField): Any = throwFor(this)
        override fun applyDataTo(customField: CustomField, value: Any?) = throwFor(this)
    }

    protected fun throwFor(type: ResourceSubtype): Nothing = throw UnsupportedOperationException("""
        Serialization of ${type::class.simpleName} resources is not yet supported by the java-asana client library.
    """.trimIndent())

}
