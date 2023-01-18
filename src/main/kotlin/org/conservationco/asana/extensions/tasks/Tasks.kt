package org.conservationco.asana.extensions.tasks

import com.asana.models.Attachment
import com.asana.models.Project
import com.asana.models.Task
import org.conservationco.asana.AsanaConfig
import org.conservationco.asana.extensions.executeDataRequestWith
import org.conservationco.asana.extensions.executeWithQueriesAndPaginate
import org.conservationco.asana.serialization.customfield.context.CustomFieldContext
import org.conservationco.asana.util.mapToGids
import org.conservationco.asana.util.toGidArray

/**
 * Container class for requests related to [Task] objects.
 */
class Tasks(
    private val config: AsanaConfig,
) {

    private val client = config.client

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
        return executeWithQueriesAndPaginate(request, true, config.fields)
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
