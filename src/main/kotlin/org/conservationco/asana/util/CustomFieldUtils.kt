package org.conservationco.asana.util

import com.asana.models.CustomField
import com.asana.models.Resource
import org.conservationco.asana.serialization.customfield.ResourceSubtype
import org.conservationco.asana.serialization.customfield.context.CustomFieldContext

fun CustomField.selectMultiEnumOptions(vararg optionsToSelect: String) {
    val options = enumOptions.filter { optionsToSelect.contains(it.name) }
    multiEnumValues = options
}

fun CustomField.multiEnumToGids(): Array<String> = if (multiEnumValues == null) arrayOf("") else multiEnumValues.toGidArray()

fun CustomField.isMultiEnum(): Boolean = this.resourceSubtype == "multi_enum"

fun CustomField.isEnum(): Boolean = this.resourceSubtype == "enum"

fun CustomField.isNumber(): Boolean = this.resourceSubtype == "number"

fun CustomField.isText(): Boolean = this.resourceSubtype == "text" || this.resourceSubtype == "people"

/**
 * Finds the data stored in a given [CustomField] based on its resource subtype. Resource subtypes that we care
 * about can be `"number"`, `"text"`, `"enum”`, or `“multi_enum”`. Those are each stored in different Java fields within
 * `CustomField`.
 *
 * @return Either a `String` (for `"number"`, `"text"`, `"enum”` subtypes) or an `Array<String>` (for `“multi_enum”`
 *         subtypes).
 */
fun CustomField.inferValue(context: CustomFieldContext): Any? = asResourceSubtype(context).convertToData(this)

internal fun CustomField.asResourceSubtype(context: CustomFieldContext): ResourceSubtype {
    val typeConstructor = ResourceSubtype.types[resourceSubtype]!!
    return if (typeConstructor.parameters.isEmpty()) typeConstructor.call()
    else typeConstructor.call(context)
}

// Collection functions

internal fun Collection<CustomField>.mapGidsToValues(context: CustomFieldContext): Map<String, Any?> =
    associateBy({ it.gid }, { it.inferValue(context) })

internal fun Collection<Resource>.toGidArray(): Array<String> = map { it.gid }.toTypedArray()