package org.conservationco.asana.extensions.events

import com.asana.errors.InvalidTokenError
import com.asana.models.Project
import com.asana.models.Resource
import com.google.gson.JsonElement
import org.conservationco.asana.AsanaConfig
import org.conservationco.asana.extensions.collectPaginationsRaw
import org.conservationco.asana.extensions.extractTaskEvents

/**
 * Container class for requests related to [Event] resources.
 */
class Events(
    private val config: AsanaConfig,
) {

    private val client = config.client

    /**
     * Stores [Resource.gid] `String`s to the last known sync token for that resource.
     */
    private val resourcesToSyncTokens = HashMap<String, String>()

    fun getEventStreamPaginated(resource: Resource): Collection<JsonElement> {
        fun eventRequest(token: String?) = client.events.getEvents(token, resource.gid)

        val potentialToken = resourcesToSyncTokens[resource.gid]
        val request = eventRequest(potentialToken)

        val response = try {
            collectPaginationsRaw(request)
        } catch (tokenError: InvalidTokenError) {
            val newToken = tokenError.sync
            val newRequest = eventRequest(newToken)
            collectPaginationsRaw(newRequest)
        }

        val nextToken = response.sync
        resourcesToSyncTokens[resource.gid] = nextToken

        return response.data
    }

    fun getTaskEvents(project: Project, vararg actions: Action): Set<Event> {
        return getEventStreamPaginated(project)
            .extractTaskEvents(*actions)
    }

}
