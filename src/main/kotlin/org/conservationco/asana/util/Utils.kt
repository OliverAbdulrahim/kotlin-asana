package org.conservationco.asana.util

fun Any?.stringValue(isNumber: Boolean): String {
    if (this is Array<*>) return this.contentToString()
    val parsed = this.toString()
    return if (parsed.isEmpty() || parsed == "null") {
        if (isNumber) "0" else ""
    } else parsed
}

fun MutableMap<String, Any>.appendAll(parameters: Array<out Pair<String, Any>>) {
    parameters.forEach { property ->
        this[property.first] = property.second
    }
}
