package org.conservationco.asana

import com.asana.Client
import org.conservationco.asana.exception.NoSuppliedAccessTokenException
import org.conservationco.asana.extensions.AsanaClientExtension

object ClientDefaults {
    private val CONFIG by lazy { AsanaConfig() }
    val CLIENT: Client by lazy { Client.accessToken(getAccessTokenFromEnv()) }
    val CLIENT_EXT by lazy { AsanaClientExtension(CONFIG) }

    private fun getAccessTokenFromEnv(): String {
        return System.getenv("ASANA_ACCESS_TOKEN")
            ?: (System.getenv("asana_access_token")
            ?: throw NoSuppliedAccessTokenException(
                "You must pass an environment variable with the key 'ASANA_ACCESS_TOKEN' or 'asana_access_token', " +
                "or you must provide your own com.asana.Client!"
        ))
    }

}
