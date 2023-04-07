package org.conservationco.asana.serialization.stubs

import com.asana.models.CustomField
import org.conservationco.asana.serialization.customfield.context.CustomFieldContext
import org.conservationco.asana.serialization.stubs.OptionalCustomFields.absentOptionalField
import org.conservationco.asana.serialization.stubs.OptionalCustomFields.absentRequiredField
import org.conservationco.asana.serialization.stubs.OptionalCustomFields.presentOptionalField
import org.conservationco.asana.serialization.stubs.OptionalCustomFields.presentRequiredField
import org.conservationco.asana.util.customFieldOf

// Expect no errors when serializing with this
internal class `Context With All Fields` : CustomFieldContext() {
    val presentRequiredField = presentRequiredField()
    val presentOptionalField = presentOptionalField()
    val absentRequiredField = absentRequiredField()
    val absentOptionalField = absentOptionalField()
    override fun loadCustomFields() =
        mapOf(
            presentRequiredField.name to presentRequiredField,
            presentOptionalField.name to presentOptionalField,
            absentRequiredField.name to absentRequiredField,
            absentOptionalField.name to absentOptionalField,
        )
}

// Expect no errors when serializing with this
internal class `Context With Optional Fields Missing` : CustomFieldContext() {
    val presentRequiredField = presentRequiredField()
    val absentRequiredField = absentRequiredField()
    override fun loadCustomFields() =
        mapOf(
            presentRequiredField.name to presentRequiredField,
            absentRequiredField.name to absentRequiredField,
        )
}

// Expect errors on required fields when serializing with this
internal class `Context With Required Fields Missing` : CustomFieldContext() {
    override fun loadCustomFields() = emptyMap<String, CustomField>()
}

internal object OptionalCustomFields {
    internal fun presentRequiredField() = customFieldOf("Present required field", "text")
    internal fun presentOptionalField() = customFieldOf("Present optional field", "text")
    internal fun absentRequiredField() = customFieldOf("Absent required field", "text")
    internal fun absentOptionalField() = customFieldOf("Absent optional field", "text")
}
