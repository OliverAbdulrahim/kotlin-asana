package org.conservationco.asana.util

import com.asana.models.CustomField
import com.asana.models.Resource
import com.asana.models.Task

/**
 * @param customFieldName The field to select.
 * @param optionsToSelect The `String` argument(s) containing the names of the `EnumOption` values to assign to the
 *                        specified custom field.
 */
fun Task.selectMultiEnumOptions(customFieldName: String, vararg optionsToSelect: String) {
    val customField = this.findCustomField(customFieldName)
    customField!!.multiEnumValues = customField
        .enumOptions
        .filter { optionsToSelect.contains(it.name) }
}

fun CustomField.addMultiEnumOptions(vararg optionsToSelect: String) {
    val options = enumOptions.filter { optionsToSelect.contains(it.name) }
    multiEnumValues.addAll(options)
}

fun Task.findCustomField(customFieldName: String): CustomField? = customFields.find { it.name.contains(customFieldName) }

fun gidsForKnownMultiEnum(customField: CustomField): Array<String> {
    return if (customField.multiEnumValues == null)  arrayOf("")
    else customField.multiEnumValues.gidsForResourceCollection()
}

fun Collection<Resource>.gidsForResourceCollection(): Array<String> = map { it.gid }.toTypedArray()

fun modTimeDiffers(first: Task, second: Task): Boolean = first.modifiedAt != second.modifiedAt

fun CustomField.isMultiEnum(): Boolean = this.resourceSubtype == "multi_enum"

fun CustomField.isEnum(): Boolean = this.resourceSubtype == "enum"

fun CustomField.isNumber(): Boolean = this.resourceSubtype == "number"

fun CustomField.isText(): Boolean = this.resourceSubtype == "text" || this.resourceSubtype == "people"

fun Any?.stringValue(): String {
    if (this is Array<*>) return this.contentToString()
    val parsed = this.toString()
    return if (parsed.isEmpty() || parsed == "null") "" else parsed
}
