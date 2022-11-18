package org.conservationco.asana.requests

import com.google.gson.JsonElement

/**
 * Encapsulates Asana events
 */
data class Event(
    val resourceGid: String,
    val changeType: Action,
    val changeBody: JsonElement,
)
