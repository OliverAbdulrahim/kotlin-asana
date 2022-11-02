package org.conservationco.asana.serialization.customfield

import com.asana.models.CustomField
import org.conservationco.asana.serialization.customfield.context.CustomFieldContext
import org.conservationco.asana.serialization.customfield.context.NoOpCustomFieldContext
import org.conservationco.asana.util.camelToSnakeCase
import org.conservationco.asana.util.multiEnumToGids
import org.conservationco.asana.util.stringValue
import kotlin.reflect.full.primaryConstructor

sealed class ResourceSubtype (
    protected val context: CustomFieldContext = NoOpCustomFieldContext,
) {

    companion object {
        val types = ResourceSubtype::class.sealedSubclasses
            .associateBy(
                { it.simpleName!!.camelToSnakeCase() },
                { it.primaryConstructor!! }
            )
    }

    abstract fun convertToGids(customField: CustomField): Any?
    abstract fun convertToData(customField: CustomField): Any?
    abstract fun applyDataTo(customField: CustomField, fieldName: String, value: Any?)

    class Number : ResourceSubtype() {
        override fun convertToGids(customField: CustomField): String = customField.numberValue.orEmpty()
        override fun convertToData(customField: CustomField): String = customField.numberValue.orEmpty()
        override fun applyDataTo(customField: CustomField, fieldName: String, value: Any?) {
            customField.numberValue = value.stringValue().ifEmpty { "0" }
        }
    }

    class Text : ResourceSubtype() {
        override fun convertToGids(customField: CustomField): String = customField.textValue.orEmpty()
        override fun convertToData(customField: CustomField): String = customField.textValue.orEmpty()
        override fun applyDataTo(customField: CustomField, fieldName: String, value: Any?) {
            customField.textValue = value.stringValue()
        }
    }

    class Enum(context: CustomFieldContext) : ResourceSubtype(context) {
        override fun convertToGids(customField: CustomField): String = customField.enumValue.gid
        override fun convertToData(customField: CustomField): String = customField.enumValue.name
        override fun applyDataTo(customField: CustomField, fieldName: String, value: Any?) {
            customField.enumValue = context.optionForName(customField.name, value as String?)
        }
    }

    class MultiEnum(context: CustomFieldContext) : ResourceSubtype(context) {
        override fun convertToGids(customField: CustomField): Any = customField.multiEnumToGids()
        override fun convertToData(customField: CustomField): Array<String> {
            return customField.multiEnumValues.map { option -> option.name }.toTypedArray()
        }
        override fun applyDataTo(customField: CustomField, fieldName: String, value: Any?) {
            val selectedMultiEnumNames = value as Array<*>?
            customField.multiEnumValues = selectedMultiEnumNames?.map { context.optionForName(fieldName, it as String) }
        }
    }

    class People(context: CustomFieldContext) : ResourceSubtype(context) {
        override fun convertToGids(customField: CustomField): Any = throwFor(this)
        override fun convertToData(customField: CustomField): Any = throwFor(this)
        override fun applyDataTo(customField: CustomField, fieldName: String, value: Any?) = throwFor(this)
    }

    class Date : ResourceSubtype() {
        override fun convertToGids(customField: CustomField): Any = throwFor(this)
        override fun convertToData(customField: CustomField): Any = throwFor(this)
        override fun applyDataTo(customField: CustomField, fieldName: String, value: Any?) = throwFor(this)
    }

    protected fun throwFor(type: ResourceSubtype): Nothing = throw UnsupportedOperationException("""
        Serialization of ${type::class.simpleName} resources is not yet supported by the java-asana client library.
    """.trimIndent())

}
