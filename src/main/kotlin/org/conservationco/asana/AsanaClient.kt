package org.conservationco.asana

import com.asana.Client
import com.asana.models.*
import com.asana.models.Job
import com.asana.requests.CollectionRequest
import com.asana.requests.ItemRequest
import org.conservationco.asana.util.convertGidsToValues
import java.time.LocalDate

class AsanaClient(
    private val config: AsanaConfig = AsanaConfig()
) {

    private val client: Client = Client.accessToken(config.accessToken)

// Public task functions

    fun getTasks(projectGid: String, includeAttachments: Boolean = false, expanded: Boolean = false): List<Task> {
        val tasks: List<Task> = getTasksPaginated(projectGid, expanded)
        if (includeAttachments) {
            tasks
                .associateBy({ it }, { getAttachments(it.gid) })
                .forEach { it.key.attachments = it.value }
        }
        return tasks
    }

    fun createTask(projectGid: String, task: Task): Task {
        val created = executeDataRequestWith(
            taskCreateRequest(),
            "customFields_fields" to task.customFields.convertGidsToValues(),
            "name" to task.name,
        )
        if (task.attachments != null) {
            task.attachments.forEach { createAttachment(created.gid, it) }
        }
        return created
    }

    fun updateTask(task: Task): Task {
        return executeDataRequestWith(
            taskUpdateRequest(task),
            "custom_fields" to task.customFields.convertGidsToValues()
        )
    }

    fun deleteTask(taskGid: String): Task {
        return client.tasks.delete(taskGid).execute()
    }

// Public attachment functions

    fun getAttachments(taskGid: String): List<Attachment> {
        return attachmentGetRequest(taskGid)
    }

    fun createAttachment(taskGid: String, attachment: Attachment): Attachment {
        val request = client.attachments.createOnTask(
            taskGid,
            attachment.downloadUrl.openStream(),
            attachment.name,
            attachment.resourceType
        )
        return request.execute()
    }

    fun createAttachments(taskGid: String, attachments: Collection<Attachment>) {
        attachments.forEach { createAttachment(taskGid, it) }
    }

// Public search functions

    fun search(textQuery: String, vararg projectGids: String = arrayOf("")): List<Task> {
        val request = client.tasks.searchInWorkspace(config.workspaceId);
        request.query["projects="] = projectGids.joinToString(separator = ",")
        request.query["text="] = textQuery
        return request.execute()
    }

    fun customFieldsForProject(projectGid: String): Collection<CustomField> {
        val request = client.customFieldSettings.findByProject(projectGid)
        return request.execute().map { it.customField }
    }

// Workspace related functions

    fun getAllProjects(): MutableList<Project> {
        return client.projects
            .getProjectsForWorkspace(config.workspaceId, true)
            .execute()
    }

    fun createFromTemplate(projectGid: String, projectTitle: String, projectTeam: String): Job {
        val request = client.projectTemplates.instantiateProject(projectGid)
        return executeQueryRequestWith(
            request,
            "name" to projectTitle,
            "team" to projectTeam,
            "public" to false
        )
    }

// Request generating functions

    private fun taskUpdateRequest(task: Task): ItemRequest<Task> {
        return client.tasks.update(task.gid)
    }

    private fun taskCreateRequest(): ItemRequest<Task> {
        return client.tasks.createTask()
    }

    private fun getTasksPaginated(projectGid: String, expanded: Boolean): List<Task> {
        val request = client.tasks.getTasksForProject(projectGid, LocalDate.EPOCH.toString())
        return if (expanded) executeCollectionRequestWith(request, "opt_expand" to ".")
        else executeCollectionRequestWith(request)
    }

    private fun attachmentGetRequest(taskGid: String): List<Attachment> {
        val request = client.attachments.getAttachmentsForObject(taskGid)
        return executeCollectionRequestWith(request, "opt_expand" to ".")
    }

// Request execution / pagination handling functions

    private fun <T> executeDataRequestWith(request: ItemRequest<T>, vararg dataParameters: Pair<String, Any>): T {
        request.data.appendAll(dataParameters)
        return request.execute()
    }

    private fun <T> executeQueryRequestWith(request: ItemRequest<T>, vararg queryParameters: Pair<String, Any>): T {
        request.query.appendAll(queryParameters)
        return request.execute()
    }

    private fun <T> executeCollectionRequestWith(
        request: CollectionRequest<T>,
        vararg queryParameters: Pair<String, Any>,
    ): List<T> {
        request.query.appendAll(queryParameters)
        return collectPaginations(request)
    }

    private fun <T> collectPaginations(request: CollectionRequest<T>): List<T> {
        val result = request.executeRaw()
        return if (result.nextPage != null) {
            request.query["offset"] = result.nextPage.offset
            result.data + collectPaginations(request)
        } else result.data
    }

    private fun MutableMap<String, Any>.appendAll(parameters: Array<out Pair<String, Any>>) {
        parameters.forEach { property ->
            this[property.first] = property.second
        }
    }

}
