package org.conservationco.asana.requests

import com.asana.errors.InvalidTokenError
import com.asana.models.*
import com.google.gson.JsonElement
import org.conservationco.asana.AsanaConfig
import org.conservationco.asana.serialization.customfield.context.CustomFieldContext
import org.conservationco.asana.util.*
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
    val events = Events()

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
                "custom_fields" to task.customFields.mapToGids(context)
            )
        }

        fun createTask(task: Task, context: CustomFieldContext): Task {
            val request = client.tasks.createTask()
            return executeDataRequestWith(
                request,
                "custom_fields" to task.customFields.mapToGids(context),
                "projects" to task.projects.toGidArray(),
                "name" to task.name,
            )
        }

        fun getAttachment(task: Task): Collection<Attachment> {
            val request = client.attachments.getAttachmentsForObject(task.gid)
            return executeWithQueriesAndPaginate(request, config.expandedResponses, config.fields)
                .map(::transformToPermanentUrl)
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

        fun getProjects(task: Task): List<Project> {
            val request = client.projects.getProjectsForTask(task.gid)
            return executeWithQueriesAndPaginate(request, config.expandedResponses, config.fields)
        }

        fun getTask(task: Task): Task {
            val request = client.tasks.getTask(task.gid)
            return request.execute()
        }
    }

    /**
     * Container class for requests related to [Project] objects.
     */
    inner class Projects {
        fun getTasksPaginated(project: Project, createdSince: LocalDate = LocalDate.EPOCH): List<Task> {
            val request = client.tasks.getTasksForProject(project.gid, createdSince.toString())
            return executeWithQueriesAndPaginate(request, config.expandedResponses, config.fields)
        }

        fun getCustomFieldSettingsPaginated(project: Project): List<CustomFieldSetting> {
            val request = client.customFieldSettings.findByProject(project.gid)
            return collectPaginations(request)
        }

        fun getTaskEvents(project: Project, vararg actions: Action): Set<Event> {
            return events
                .getEventStreamPaginated(project)
                .extractTaskEvents(*actions)
        }

        fun getTaskCount(project: Project): Int {
            val request = client.projects.getTaskCountsForProject(project.gid)
            val taskCountField = "num_tasks"
            return executeQueryRequestWith(request, "opt_fields" to taskCountField)
                .asJsonObject[taskCountField]
                .asInt
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

    /**
     * Container class for requests related to [Event] resources.
     */
    inner class Events {
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
    }

}
