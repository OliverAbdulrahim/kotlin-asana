package org.conservationco.asana.extensions.workpsaces

import com.asana.models.*
import org.conservationco.asana.AsanaConfig
import org.conservationco.asana.extensions.collectPaginations
import org.conservationco.asana.extensions.executeCollectionRequestWith
import org.conservationco.asana.extensions.executeQueryRequestWith

/**
 * Container class for requests related to [Workspace] objects.
 */
class Workspaces(
    private val config: AsanaConfig,
) {

    private val client = config.client

    fun searchWorkspacePaginated(
        workspace: Workspace,
        filters: Array<out Pair<String, Any>>,
    ): List<Task> {
        val request = client.tasks.searchInWorkspace(workspace.gid);
        return executeCollectionRequestWith(request, filters)
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
