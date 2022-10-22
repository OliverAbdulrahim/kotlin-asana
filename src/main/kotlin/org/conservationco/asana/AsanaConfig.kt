package org.conservationco.asana

import com.asana.Client

class AsanaConfig(
    accessToken: String = System.getenv("asana_access_token"),
    var verboseLogs: Boolean = false,
) {
    val client: Client = Client.accessToken(accessToken).apply { logAsanaChangeWarnings = verboseLogs }
}
