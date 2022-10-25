package org.conservationco.asana

import com.asana.Client
import org.conservationco.asana.serialization.customfield.context.CustomFieldContext
import org.conservationco.asana.serialization.customfield.context.NoOpCustomFieldContext

class AsanaConfig(
    val client: Client = Client.accessToken(System.getenv("asana_access_token")),
    val context: CustomFieldContext = NoOpCustomFieldContext,
    var verboseLogs: Boolean = false,
) {
    init { client.apply { logAsanaChangeWarnings = verboseLogs } }
}
