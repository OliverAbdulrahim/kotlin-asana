package org.conservationco.asana.util

import com.asana.models.*
import com.asana.requests.CollectionRequest
import com.asana.requests.ItemRequest
import org.conservationco.asana.AsanaConfig
import org.conservationco.asana.serialization.customfield.context.CustomFieldContext
import org.conservationco.asana.serialization.customfield.context.NoOpCustomFieldContext
import org.conservationco.asana.serialization.customfield.context.TaskCustomFieldContext
import java.time.LocalDate

class RequestExecutor(
    private val config: AsanaConfig,
) {

    private val client = config.client

    fun deleteTask(task: Task): Task {
        val request = client.tasks.delete(task.gid)
        return request.execute()
    }

    fun updateTask(task: Task): Task {
        val request = client.tasks.update(task.gid)
        return executeDataRequestWith(
            request,
            "custom_fields" to task.customFields.mapGidsToValues(decideContextFor(task))
        )
    }

    fun createTask(task: Task): Task {
        val request = client.tasks.createTask()
        return executeDataRequestWith(
            request,
            "customFields_fields" to task.customFields.mapGidsToValues(decideContextFor(task)),
            "name" to task.name,
        )
    }

    fun getAttachment(task: Task): Collection<Attachment> {
        val request = client.attachments.getAttachmentsForObject(task.gid)
        return executeCollectionRequestWith(request, "opt_expand" to ".")
    }

    fun createAttachment(task: Task, attachment: Attachment): Attachment {
        val request = client.attachments.createOnTask(
            task.gid,
            attachment.downloadUrl.openStream(),
            attachment.name,
            attachment.resourceType
        )
        return request.execute()
    }

    fun getTasksPaginated(project: Project, expanded: Boolean): List<Task> {
        val request = client.tasks.getTasksForProject(project.gid, LocalDate.EPOCH.toString())
        return if (expanded) executeCollectionRequestWith(request, "opt_expand" to ".")
        else executeCollectionRequestWith(request)
    }

    fun getCustomFieldSettings(project: Project): MutableList<CustomFieldSetting> {
        val request = client.customFieldSettings.findByProject(project.gid)
        return request.execute()
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
        val request = client.projects.getProjectsForWorkspace(workspace.gid, includeArchived)
        return request.execute()
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

    fun getCustomFields(workspace: Workspace): List<CustomField> {
        val request = client.customFields.getCustomFieldsForWorkspace(workspace.gid)
        return collectPaginations(request)
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

    private fun decideContextFor(task: Task): CustomFieldContext {
        return if (config.context is NoOpCustomFieldContext) TaskCustomFieldContext(task) else config.context
    }

}
