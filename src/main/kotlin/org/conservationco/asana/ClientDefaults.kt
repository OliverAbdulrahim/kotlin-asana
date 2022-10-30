package org.conservationco.asana

import com.asana.Client

object ClientDefaults {
    val CONFIG by lazy { AsanaConfig() }
    val CLIENT by lazy { Client.accessToken(System.getenv("asana_access_token")) }
    val CLIENT_EXT by lazy { AsanaClientExtension(CONFIG) }
}
