package org.conservationco.asana.extensions.projects

import com.asana.models.CustomFieldSetting
import com.asana.models.Project
import com.asana.models.Task
import org.conservationco.asana.AsanaConfig
import org.conservationco.asana.extensions.collectPaginations
import org.conservationco.asana.extensions.executeQueryRequestWith
import org.conservationco.asana.extensions.executeWithQueriesAndPaginate
import java.time.LocalDate

/**
 * Container class for requests related to [Project] objects.
 */
class Projects(
    private val config: AsanaConfig,
) {

    private val client = config.client

    fun getTasksPaginated(project: Project, createdSince: LocalDate = LocalDate.EPOCH): List<Task> {
        val request = client.tasks.getTasksForProject(project.gid, createdSince.toString())
        return executeWithQueriesAndPaginate(request, config.expandedResponses, config.fields)
    }

    fun getCustomFieldSettingsPaginated(project: Project): List<CustomFieldSetting> {
        val request = client.customFieldSettings.findByProject(project.gid)
        return collectPaginations(request)
    }

    fun getTaskCount(project: Project): Int {
        val request = client.projects.getTaskCountsForProject(project.gid)
        val taskCountField = "num_tasks"
        return executeQueryRequestWith(request, "opt_fields" to taskCountField)
            .asJsonObject[taskCountField]
            .asInt
    }
}
