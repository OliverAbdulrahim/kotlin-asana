package org.conservationco.asana.util

import com.asana.models.CustomField
import com.asana.models.Task

internal fun Task.selectMultiEnumOptions(customFieldName: String, vararg optionsToSelect: String) {
    val customField = this.findCustomField(customFieldName)
    customField!!.multiEnumValues = customField
        .enumOptions
        .filter { optionsToSelect.contains(it.name) }
}

internal fun Task.findCustomField(customFieldName: String): CustomField? = customFields.find { it.name.contains(customFieldName) }

internal fun Task.modTimeDiffers(other: Task): Boolean = modifiedAt != other.modifiedAt
