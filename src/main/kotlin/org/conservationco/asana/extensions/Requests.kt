package org.conservationco.asana.extensions

import com.asana.models.ResultBodyCollection
import com.asana.requests.CollectionRequest
import com.asana.requests.ItemRequest
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.conservationco.asana.extensions.events.Action
import org.conservationco.asana.extensions.events.Event
import org.conservationco.asana.util.appendAll

internal fun <T> executeDataRequestWith(
    request: ItemRequest<T>,
    vararg dataParameters: Pair<String, Any>
): T {
    request.data.appendAll(dataParameters)
    return request.execute()
}

internal fun <T> executeQueryRequestWith(
    request: ItemRequest<T>,
    vararg queryParameters: Pair<String, Any>
): T {
    request.query.appendAll(queryParameters)
    return request.execute()
}

internal fun <T> executeCollectionRequestWith(
    request: CollectionRequest<T>,
    filters: Array<out Pair<String, Any>>
): List<T> {
    request.query.appendAll(filters)
    return collectPaginations(request)
}

internal fun <T> executeWithQueriesAndPaginate(
    request: CollectionRequest<T>,
    expanded: Boolean,
    fields: Array<out String>
): List<T> {
    if (expanded) request.query["opt_expand"] = "."
    else request.query["opt_fields"] = fields.joinToString(separator = ",")
    return collectPaginations(request)
}

internal fun <T> collectPaginations(request: CollectionRequest<T>): List<T> {
    val result = request.executeRaw()
    return if (result.nextPage != null) {
        request.query["offset"] = result.nextPage.offset
        result.data + collectPaginations(request)
    } else result.data
}

internal fun <T> collectPaginationsRaw(request: CollectionRequest<T>): ResultBodyCollection<T> {
    val result = request.executeRaw()
    return if (result.nextPage != null) {
        request.query["offset"] = result.nextPage.offset
        ResultBodyCollection<T>().apply { result.data + collectPaginationsRaw(request).data }
    } else result
}

internal fun Collection<JsonElement>.extractTaskEvents(vararg actions: Action): Set<Event> =
    extractResourceEvents("task", *actions)

internal fun Collection<JsonElement>.extractResourceEvents(resourceType: String, vararg actions: Action): Set<Event> {
    if (this.isEmpty() || actions.isEmpty()) return emptySet()

    val actionsAsJsonNames = actions.map(Action::jsonName)
    val events = HashSet<Event>()

    for (element in this) {
        if (element !is JsonObject) continue
        if (element["type"].asString != resourceType) continue

        val action = element["action"].asString
        if (action !in actionsAsJsonNames) continue

        val changeTypeEnum = Action.fromString(action)
        val resourceBody = element["resource"] as JsonObject
        val gid = resourceBody["gid"].asString
        val packaged = Event(gid, changeTypeEnum)
        events.add(packaged)
    }
    return events
}
