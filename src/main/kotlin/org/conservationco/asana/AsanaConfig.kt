package org.conservationco.asana

import com.asana.Client

class AsanaConfig(
    val client: Client = ClientDefaults.CLIENT,
    var verboseLogs: Boolean = false,
    var expandedResponses: Boolean = false,
    vararg val fields: String = arrayOf("name", "custom_fields"),
) {
    init {
        client.apply {
            logAsanaChangeWarnings = verboseLogs
            options["page_size"] = 100
        }
    }
}
