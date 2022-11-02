package org.conservationco.asana.serialization.stubs

import com.asana.models.CustomField
import com.asana.models.CustomField.EnumOption

internal val languages by lazy {
    arrayOf("Turkish", "French", "Yoruba", "English", "Vietnamese", "Korean", "Persian", "Inuktitut", "Spanish")
        .map { enumOptionWithName(it) }
}
internal val seasons by lazy {
    arrayOf("Winter", "Spring", "Summer", "Fall")
        .map { enumOptionWithName(it) }
}

internal fun enumOptionWithName(name: String): EnumOption = EnumOption().apply {
    this.name = name
    this.gid = "123456789"
}

internal fun getTextCustomField() = CustomField().apply {
    name = "Favorite dessert"
    resourceSubtype = "text"
}

internal fun getEnumCustomField() = CustomField().apply {
    name = "Favorite season"
    resourceSubtype = "enum"
    enumOptions = seasons
}

internal fun getMultiEnumField() = CustomField().apply {
    name = "Languages spoken"
    resourceSubtype = "multi_enum"
    enumOptions = languages
}

