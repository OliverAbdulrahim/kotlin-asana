package org.conservationco.asana.serialization.stubs

import com.asana.models.CustomField
import com.asana.models.CustomField.EnumOption
import org.conservationco.asana.serialization.customfield.context.CustomFieldContext

internal class PersonCustomFieldContextStub : CustomFieldContext() {
    override fun loadCustomFields(): Map<String, CustomField> {
        val textField = getTextCustomField()
        val enumField = getEnumCustomField()
        val multiEnumField = getMultiEnumField()
        return mapOf(
            textField.name to textField,
            enumField.name to enumField,
            multiEnumField.name to multiEnumField
        )
    }
}
