package org.conservationco.asana.util

internal fun MutableMap<String, Any>.appendAll(parameters: Array<out Pair<String, Any>>) {
    parameters.forEach { property -> this[property.first] = property.second }
}

internal fun <T, K, V> Iterable<T>.associateByNotNull(
    keySelector: (T) -> K?,
    valueTransform: (T) -> V?,
): Map<K, V> = buildMap {
    for (item in this@associateByNotNull) {
        val key = keySelector(item) ?: continue
        val value = valueTransform(item) ?: continue
        this[key] = value
    }
}
