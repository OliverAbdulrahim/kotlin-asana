package org.conservationco.asana.util

import com.asana.models.Attachment
import com.asana.models.ResultBodyCollection
import com.asana.models.Task
import com.asana.requests.CollectionRequest
import com.asana.requests.ItemRequest
import com.google.gson.JsonElement
import com.google.gson.JsonObject
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

internal fun Collection<JsonElement>.extractTasks(): Collection<Task> {
    val gidsToTasks = HashMap<String, Task>()
    for (element in this) {
        if (element !is JsonObject) continue
        if (element["type"].asString != "task") continue
        if (element["action"].asString == "deleted") continue

        val taskBody = element["resource"] as JsonObject
        val gid = taskBody["gid"].asString
        if (gidsToTasks.containsKey(gid)) continue

        gidsToTasks[gid] = Task().apply { this.gid = gid }
    }
    return gidsToTasks.values
}
