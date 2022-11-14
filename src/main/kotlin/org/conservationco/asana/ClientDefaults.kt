package org.conservationco.asana

import com.asana.Client
import org.conservationco.asana.extensions.AsanaClientExtension

object ClientDefaults {
    val CONFIG by lazy { AsanaConfig() }
    val CLIENT by lazy { Client.accessToken(getAccessTokenFromEnv()) }
    val CLIENT_EXT by lazy { AsanaClientExtension(CONFIG) }

    private fun getAccessTokenFromEnv(): String {
        return System.getenv("asana_access_token") ?: throw NoSuchElementException("""
            You must pass the "asana_access_token" environment variable when using the default client.
        """.trimIndent())
    }
}
