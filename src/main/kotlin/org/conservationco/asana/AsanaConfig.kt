package org.conservationco.asana

import com.asana.Client

/**
 * Configuration class for [AsanaClientExtension].
 *
 * @property client The `java-asana` client to use within the context of this object.
 * @property verboseLogs Flag for verbose client warning logs.
 * @property expandedResponses Flag for including all optional fields when making `GET` requests (not recommended – only
 *                             for debugging use)
 * @property fields The optional fields to include when making `GET` requests.
 */
class AsanaConfig(
    val client: Client = ClientDefaults.CLIENT,
    var verboseLogs: Boolean = false,
    var expandedResponses: Boolean = false,
    vararg val fields: String = arrayOf("name", "custom_fields", "resource_type"),
) {
    init {
        client.apply {
            logAsanaChangeWarnings = verboseLogs
            options["page_size"] = 100
        }
    }
}
