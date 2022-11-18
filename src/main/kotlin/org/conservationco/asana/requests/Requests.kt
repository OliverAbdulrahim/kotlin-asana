package org.conservationco.asana.requests

import com.asana.models.Attachment
import com.asana.models.ResultBodyCollection
import com.asana.requests.CollectionRequest
import com.asana.requests.ItemRequest
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.conservationco.asana.util.appendAll
import java.net.URL

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

internal fun Collection<JsonElement>.extractTaskEvents(vararg actions: Action): Set<Event> {
    if (this.isEmpty() || actions.isEmpty()) return emptySet()

    val jsonActions = actions.map(Action::jsonName)
    val taskEvents = HashSet<Event>()

    for (element in this) {
        if (element !is JsonObject) continue
        if (element["type"].asString != "task") continue

        val action = element["action"].asString
        if (action in jsonActions) {
            val changeTypeEnum = Action.fromString(action)
            val changeBody = element["change"] as JsonObject
            val taskBody = element["resource"] as JsonObject
            val taskGid = taskBody["gid"].asString
            val packaged = Event(taskGid, changeTypeEnum, changeBody)
            taskEvents.add(packaged)
        }
    }
    return taskEvents
}

internal fun transformToPermanentUrl(attachment: Attachment): Attachment = Attachment().apply {
    gid = attachment.gid
    name = attachment.name
    parent = attachment.parent
    downloadUrl = URL("https://app.asana.com/app/asana/-/get_asset?asset_id=${attachment.gid}")
}
