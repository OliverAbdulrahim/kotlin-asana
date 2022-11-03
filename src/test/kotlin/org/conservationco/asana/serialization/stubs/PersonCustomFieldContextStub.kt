package org.conservationco.asana.serialization.stubs

import com.asana.models.CustomField
import org.conservationco.asana.serialization.customfield.context.CustomFieldContext
import org.conservationco.asana.serialization.stubs.PersonCustomFields.personEnumCustomField
import org.conservationco.asana.serialization.stubs.PersonCustomFields.personMultiEnumField
import org.conservationco.asana.serialization.stubs.PersonCustomFields.personTextCustomField
import org.conservationco.asana.util.customFieldOf
import org.conservationco.asana.util.enumOptionOf

internal class PersonCustomFieldContextStub : CustomFieldContext() {
    override fun loadCustomFields(): Map<String, CustomField> {
        val textField = personTextCustomField()
        val enumField = personEnumCustomField()
        val multiEnumField = personMultiEnumField()
        return mapOf(
            textField.name to textField,
            enumField.name to enumField,
            multiEnumField.name to multiEnumField
        )
    }
}

internal object PersonCustomFields {
    internal const val noOpGid = "1234567890"
    private val languages =
        arrayOf("Turkish", "French", "Yoruba", "English", "Vietnamese", "Korean", "Persian", "Inuktitut", "Spanish")
            .map { enumOptionOf(it, noOpGid) }
    private val seasons =
        arrayOf("Winter", "Spring", "Summer", "Fall")
            .map { enumOptionOf(it, noOpGid) }

    internal fun personTextCustomField() = customFieldOf("Favorite dessert", "text")
    internal fun personEnumCustomField() = customFieldOf("Favorite season", "enum", seasons)
    internal fun personMultiEnumField() = customFieldOf("Languages spoken", "multi_enum", languages)
}
