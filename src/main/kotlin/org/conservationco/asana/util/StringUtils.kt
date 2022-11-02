package org.conservationco.asana.util

import java.util.*

private val camelCaseRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()

internal fun String.camelToSnakeCase(): String {
    return camelCaseRegex
        .replace(this) { "_${it.value}" }
        .lowercase(Locale.getDefault())
}

internal fun Any?.stringValue(): String {
    if (this is Array<*>) return this.contentToString()
    val parsed = this.toString()
    return if (parsed.isEmpty() || parsed == "null") "" else parsed
}
