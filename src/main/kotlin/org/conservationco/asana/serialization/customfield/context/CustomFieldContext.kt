package org.conservationco.asana.serialization.customfield.context

import com.asana.models.CustomField
import org.conservationco.asana.util.isEnum
import org.conservationco.asana.util.isMultiEnum

sealed class CustomFieldContext {

    private val namesToCustomFields: Map<String, CustomField> by lazy { loadCustomFields() }
    private val enumOptions: Map<String, Collection<CustomField.EnumOption>> by lazy { namesToCustomFields.collectEnumOptions() }

    operator fun get(customFieldName: String): CustomField? = namesToCustomFields[customFieldName]

    internal fun optionForName(customFieldName: String, selectedEnumName: String?): CustomField.EnumOption? {
        return enumOptions[customFieldName]!!.find { it.name == selectedEnumName }
    }

    protected abstract fun loadCustomFields(): Map<String, CustomField>

    protected fun Collection<CustomField>.convertToMap(): Map<String, CustomField> {
        return this.associateBy { it.name }
    }

    private fun Map<String, CustomField>.collectEnumOptions(): Map<String, Collection<CustomField.EnumOption>> {
        return entries
            .filter { it.value.isEnum() || it.value.isMultiEnum() }
            .associateBy( { it.key }, { it.value.enumOptions } )
    }

}