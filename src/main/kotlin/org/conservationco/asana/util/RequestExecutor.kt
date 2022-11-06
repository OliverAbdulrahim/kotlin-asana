package org.conservationco.asana.util

import com.asana.models.*
import com.asana.requests.CollectionRequest
import com.asana.requests.ItemRequest
import org.conservationco.asana.AsanaConfig
import org.conservationco.asana.serialization.customfield.context.CustomFieldContext
import java.time.LocalDate

/**
 * Utility class for executing Asana requests.
 *
 * @property config Configuration for this object, including what client to select, verbosity of logs, and optional
 *                  fields to return.
 */
class RequestExecutor(
    private val config: AsanaConfig,
) {

    private val client = config.client
    val tasks = Tasks()
    val projects = Projects()
    val workspaces = Workspaces()

    /**
     * Container class for requests related to [Task] objects.
     */
    inner class Tasks {
        fun deleteTask(task: Task): Task {
            val request = client.tasks.delete(task.gid)
            return request.execute()
        }

        fun updateTask(task: Task, context: CustomFieldContext): Task {
            val request = client.tasks.update(task.gid)
            return executeDataRequestWith(
                request,
                "custom_fields" to task.customFields.mapGidsToValues(context)
            )
        }

        fun createTask(task: Task, context: CustomFieldContext): Task {
            val request = client.tasks.createTask()
            return executeDataRequestWith(
                request,
                "custom_fields" to task.customFields.mapGidsToValues(context),
                "name" to task.name,
            )
        }

        fun getAttachment(task: Task): Collection<Attachment> {
            val request = client.attachments.getAttachmentsForObject(task.gid)
            return executeWithQueriesAndPaginate(request, expanded = true)
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

        fun getProjects(taskGid: String): List<Project> {
            val request = client.projects.getProjectsForTask(taskGid)
            return executeWithQueriesAndPaginate(request)
        }
    }

    /**
     * Container class for requests related to [Project] objects.
     */
    inner class Projects {
        fun getTasksPaginated(project: Project, createdSince: LocalDate = LocalDate.EPOCH): List<Task> {
            val request = client.tasks.getTasksForProject(project.gid, createdSince.toString())
            return executeWithQueriesAndPaginate(request)
        }

        fun getCustomFieldSettingsPaginated(project: Project): List<CustomFieldSetting> {
            val request = client.customFieldSettings.findByProject(project.gid)
            return collectPaginations(request)
        }
    }

    /**
     * Container class for requests related to [Workspace] objects.
     */
    inner class Workspaces {
        fun searchWorkspacePaginated(
            workspace: Workspace,
            textQuery: String,
            vararg projectGids: String
        ): List<Task> {
            val request = client.tasks.searchInWorkspace(workspace.gid);
            request.query["projects="] = projectGids.joinToString(separator = ",")
            request.query["text="] = textQuery
            return collectPaginations(request)
        }

        fun getProjectsPaginated(workspace: Workspace, includeArchived: Boolean): Collection<Project> {
            val request = client.projects.getProjectsForWorkspace(workspace.gid, includeArchived)
            return collectPaginations(request)
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

        fun getCustomFieldsPaginated(workspace: Workspace): List<CustomField> {
            val request = client.customFields.getCustomFieldsForWorkspace(workspace.gid)
            return collectPaginations(request)
        }
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

    private fun <T> executeWithQueriesAndPaginate(
        request: CollectionRequest<T>,
        expanded: Boolean = config.expandedResponses,
        vararg fields: String = config.fields
    ): List<T> {
        if (expanded) request.query["opt_expand"] = "."
        else request.query["opt_fields"] = fields.joinToString(separator = ",")
        return collectPaginations(request)
    }

    private fun <T> collectPaginations(request: CollectionRequest<T>): List<T> {
        val result = request.executeRaw()
        return if (result.nextPage != null) {
            request.query["offset"] = result.nextPage.offset
            result.data + collectPaginations(request)
        } else result.data
    }

}
