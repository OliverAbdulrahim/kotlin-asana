package org.conservationco.asana

import com.asana.models.*
import org.conservationco.asana.util.RequestExecutor

// asanaContext entrypoint
inline fun <R> asanaContext(
    client: AsanaClientExtension = AsanaClientExtension.Defaults.CLIENT.value,
    block: AsanaClientExtension.() -> R
): R = client.block()

class AsanaClientExtension(config: AsanaConfig) {

    object Defaults {
        val CONFIG = lazy { AsanaConfig() }
        val CLIENT = lazy { AsanaClientExtension(CONFIG.value) }
    }

    private val requestExecutor = RequestExecutor(config)

// Task extension functions

    fun Task.delete(): Task {
        return requestExecutor.deleteTask(this)
    }

    fun Task.update(): Task {
        return requestExecutor.updateTask(this)
    }

    fun Task.getAttachments(): Collection<Attachment> {
        return if (attachments == null) requestExecutor.getAttachment(this)
        else attachments
    }

    fun Task.createAttachment(attachment: Attachment): Attachment {
        return requestExecutor.createAttachment(this, attachment)
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
        val created = requestExecutor.createTask(task)
        if (task.attachments != null) task.attachments.forEach { created.createAttachment(it) }
        return created
    }

    fun Project.getCustomFields(): Collection<CustomField> {
        return requestExecutor
            .getCustomFieldSettingsPaginated(this)
            .map { it.customField }
    }

// Workspace extension functions

    fun Workspace.search(textQuery: String, vararg projectGids: String = arrayOf("")): List<Task> {
        return requestExecutor.searchWorkspacePaginated(this, textQuery, *projectGids)
    }

    fun Workspace.getAllProjects(includeArchived: Boolean = true): Collection<Project> {
        return requestExecutor.getProjectsPaginated(this, includeArchived)
    }

    fun Workspace.instantiateTemplate(projectGid: String, projectTitle: String, projectTeam: String): Job {
        return requestExecutor.instantiateTemplate(projectGid, projectTitle, projectTeam)
    }

    fun Workspace.getCustomFields(): Collection<CustomField> {
        return requestExecutor.getCustomFieldsPaginated(this)
    }

}
