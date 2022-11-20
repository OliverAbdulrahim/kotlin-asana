package org.conservationco.asana.extensions.events

/**
 * Encapsulates a single Asana event.
 *
 * For more details,
 * [see the corresponding section of the Asana documentation](https://developers.asana.com/docs/get-events-on-a-resource)
 *
 * @property resourceGid The [com.asana.models.Resource] that this event occurred on.
 * @property type The type of change that occurred.
 */
data class Event(
    val resourceGid: String,
    val type: Action,
)
