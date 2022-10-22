package org.conservationco.asana.customfield

import com.asana.models.CustomField
import org.conservationco.asana.gidsForResourceCollection

/**
 * Finds the data stored in a given [CustomField] based on its resource subtype. Resource subtypes that we care
 * about can be `"number"`, `"text"`, `"enum_value”`, or `“multi_enum_value”`. Those are each stored in different Java
 * fields within `CustomField`.
 *
 * @return Depending on the given [CustomField.resourceSubtype], either a `String` or an `Array<String>`.
 */
fun CustomField.getValue(): Any? = getResourceSubtype().convertCustomFieldToGids(this)

fun CustomField.addMultiEnumOptions(vararg optionsToSelect: String) {
    val options = enumOptions.filter { optionsToSelect.contains(it.name) }
    multiEnumValues.addAll(options)
}

fun CustomField.getResourceSubtype(): ResourceSubtype = ResourceSubtype.values().find { it.type == resourceSubtype }!!

fun CustomField.gidsForKnownMultiEnum(): Array<String> {
    return if (multiEnumValues == null)  arrayOf("")
    else multiEnumValues.gidsForResourceCollection()
}

fun CustomField.isMultiEnum(): Boolean = this.resourceSubtype == "multi_enum"

fun CustomField.isEnum(): Boolean = this.resourceSubtype == "enum"

fun CustomField.isNumber(): Boolean = this.resourceSubtype == "number"

fun CustomField.isText(): Boolean = this.resourceSubtype == "text" || this.resourceSubtype == "people"

fun optionForName(
    context: CustomFieldContext,
    customFieldName: String,
    selectedEnumName: String?
): CustomField.EnumOption? {
    return context.options[customFieldName]!!.find { it.name == selectedEnumName }
}
