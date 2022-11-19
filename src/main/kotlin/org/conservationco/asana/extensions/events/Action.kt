package org.conservationco.asana.extensions.events

/**
 * Enumeration for all Asana actions that trigger webhooks and event streams. For more information,
 * [visit the corresponding section of the Asana documentation](https://developers.asana.com/docs/actions).
 */
enum class Action(val jsonName: String) {
    ADDED("added"),
    CHANGED("changed"),
    DELETED("deleted"),
    UNDELETED("undeleted"),
    REMOVED("removed");

    companion object {
        private val namesMap = Action.values().associateBy(Action::jsonName)
        /**
         * Returns the [Action] representation of the given [jsonName] `String`. This method fails if the given String
         * does not have a corresponding enumeration.
         */
        fun fromString(jsonName: String) = namesMap[jsonName]!!
    }
}
