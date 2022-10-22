package org.conservationco.asana

import com.asana.Client
import com.asana.models.*
import com.asana.requests.CollectionRequest
import com.asana.requests.ItemRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.time.LocalDate

class RequestExecutor(
    private val client: Client,
) {

    fun taskDeleteRequest(task: Task): Task {
        return client.tasks.delete(task.gid).execute()
    }

    fun taskUpdateRequest(task: Task): Task {
        return executeDataRequestWith(
            client.tasks.update(task.gid),
            "custom_fields" to task.customFields.convertGidsToValues()
        )
    }

    fun taskCreateRequest(task: Task): Task {
        return executeDataRequestWith(
            client.tasks.createTask(),
            "customFields_fields" to task.customFields.convertGidsToValues(),
            "name" to task.name,
        )
    }

    fun attachmentGetRequest(task: Task): Collection<Attachment> {
        val request = client.attachments.getAttachmentsForObject(task.gid)
        return executeCollectionRequestWith(request, "opt_expand" to ".")
    }

    fun attachmentCreateRequest(task: Task, attachment: Attachment): Attachment {
        return client.attachments.createOnTask(
            task.gid,
            attachment.downloadUrl.openStream(),
            attachment.name,
            attachment.resourceType
        ).execute()
    }

    fun getTasksPaginated(project: Project, expanded: Boolean): List<Task> {
        val request = client.tasks.getTasksForProject(project.gid, LocalDate.EPOCH.toString())
        return if (expanded) executeCollectionRequestWith(request, "opt_expand" to ".")
        else executeCollectionRequestWith(request)
    }

    fun getCustomFieldSettings(project: Project): MutableList<CustomFieldSetting> {
        return client.customFieldSettings.findByProject(project.gid).execute()
    }

    fun searchWorkspace(
        workspace: Workspace,
        textQuery: String,
        vararg projectGids: String
    ): List<Task> {
        val request = client.tasks.searchInWorkspace(workspace.gid);
        request.query["projects="] = projectGids.joinToString(separator = ",")
        request.query["text="] = textQuery
        return request.execute()
    }

    fun getAllProjects(workspace: Workspace, includeArchived: Boolean): Collection<Project> {
        return client.projects
            .getProjectsForWorkspace(workspace.gid, includeArchived)
            .execute()
    }

    fun instantiateTemplate(projectGid: String, projectTitle: String, projectTeam: String): Job {
        val request = client.projectTemplates.instantiateProject(projectGid)
        return executeQueryRequestWith(
            request,
            "name" to projectTitle,
            "team" to projectTeam,
            "public" to false
        )
    }

// Request execution / pagination handling functions

    fun <T> executeDataRequestWith(request: ItemRequest<T>, vararg dataParameters: Pair<String, Any>): T {
        request.data.appendAll(dataParameters)
        return request.execute()
    }

    fun <T> executeQueryRequestWith(request: ItemRequest<T>, vararg queryParameters: Pair<String, Any>): T {
        request.query.appendAll(queryParameters)
        return request.execute()
    }

    fun <T> executeCollectionRequestWith(
        request: CollectionRequest<T>,
        vararg queryParameters: Pair<String, Any>,
    ): List<T> {
        request.query.appendAll(queryParameters)
        return collectPaginations(request)
    }

    fun <T> collectPaginations(request: CollectionRequest<T>): List<T> {
        val result = request.executeRaw()
        return if (result.nextPage != null) {
            request.query["offset"] = result.nextPage.offset
            result.data + collectPaginations(request)
        } else result.data
    }

    fun MutableMap<String, Any>.appendAll(parameters: Array<out Pair<String, Any>>) {
        parameters.forEach { property ->
            this[property.first] = property.second
        }
    }

}
