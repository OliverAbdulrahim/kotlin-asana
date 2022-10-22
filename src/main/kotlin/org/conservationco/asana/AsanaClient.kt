package org.conservationco.asana

import com.asana.models.*
import com.asana.models.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AsanaClient(
    config: AsanaConfig = AsanaConfig(),
) {

    private val requestExecutor = RequestExecutor(config.client)
    private val requestScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

// Task extension functions

    fun Task.delete(): Task {
        return requestExecutor.taskDeleteRequest(this)
    }

    fun Task.update(): Task {
        return requestExecutor.taskUpdateRequest(this)
    }

    fun Task.getAttachments(): Collection<Attachment> {
        return if (attachments == null) requestExecutor.attachmentGetRequest(this)
        else attachments
    }

    fun Task.createAttachment(attachment: Attachment): Attachment {
        return requestExecutor.attachmentCreateRequest(this, attachment)
    }

    fun Task.createAttachments(attachments: Collection<Attachment>) {
        attachments.forEach { createAttachment(it) }
    }

// Project extension functions

    fun Project.getTasks(includeAttachments: Boolean = false, expanded: Boolean = false): List<Task> {
        val tasks = requestExecutor.getTasksPaginated(this, expanded)
        if (includeAttachments) {
            tasks
                .associateBy({ it }, { it.getAttachments() })
                .forEach { it.key.attachments = it.value }
        }
        return tasks
    }

    fun Project.createTask(task: Task): Task {
        val created = requestExecutor.taskCreateRequest(task)
        if (task.attachments != null) task.attachments.forEach { created.createAttachment(it) }
        return created
    }

    fun Project.getCustomFields(): Collection<CustomField> {
        return requestExecutor
            .getCustomFieldSettings(this)
            .map { it.customField }
    }

// Workspace extension functions

    fun Workspace.search(textQuery: String, vararg projectGids: String = arrayOf("")): List<Task> {
        return requestExecutor.searchWorkspace(this, textQuery, *projectGids)
    }

    fun Workspace.getAllProjects(includeArchived: Boolean = true): Collection<Project> {
        return requestExecutor.getAllProjects(this, includeArchived)
    }

    fun Workspace.instantiateTempalte(projectGid: String, projectTitle: String, projectTeam: String): Job {
        return requestExecutor.instantiateTemplate(projectGid, projectTitle, projectTeam)
    }

}
