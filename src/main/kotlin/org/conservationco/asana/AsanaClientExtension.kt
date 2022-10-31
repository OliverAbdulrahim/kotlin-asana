package org.conservationco.asana

import com.asana.models.*
import org.conservationco.asana.serialization.AsanaSerializable
import org.conservationco.asana.serialization.AsanaTaskSerializer
import org.conservationco.asana.serialization.customfield.CustomFieldException
import org.conservationco.asana.serialization.customfield.context.CustomFieldContext
import org.conservationco.asana.serialization.customfield.context.ProjectCustomFieldContext
import org.conservationco.asana.serialization.customfield.context.TaskCustomFieldContext
import org.conservationco.asana.serialization.customfield.context.WorkspaceCustomFieldContext
import org.conservationco.asana.util.RequestExecutor
import kotlin.reflect.KClass

class AsanaClientExtension(private val config: AsanaConfig) {

    private val requestExecutor = RequestExecutor(config)
    private val contexts: MutableMap<String, CustomFieldContext> = HashMap()

// Task extension functions

    fun Task.delete(): Task {
        return requestExecutor.tasks.deleteTask(this)
    }

    fun Task.update(): Task {
        return requestExecutor.tasks.updateTask(this, getContextFor(this))
    }

    fun Task.getAttachments(): Collection<Attachment> {
        return if (attachments == null) requestExecutor.tasks.getAttachment(this)
        else attachments
    }

    fun Task.createAttachment(attachment: Attachment): Attachment {
        return requestExecutor.tasks.createAttachment(this, attachment)
    }

    fun Task.createAttachments(attachments: Collection<Attachment>) {
        attachments.forEach { createAttachment(it) }
    }

// Project extension functions

    fun Project.getTasks(includeAttachments: Boolean = false): List<Task> {
        val tasks = requestExecutor.projects.getTasksPaginated(this)
        if (includeAttachments) {
            tasks
                .associateBy({ it }, { it.getAttachments() })
                .forEach { it.key.attachments = it.value }
        }
        return tasks
    }

    fun Project.createTask(task: Task): Task {
        val created = requestExecutor.tasks.createTask(task, getContextFor(this))
        if (task.attachments != null) task.attachments.forEach { created.createAttachment(it) }
        return created
    }

    fun Project.getCustomFields(): Collection<CustomField> {
        return requestExecutor.projects.getCustomFieldSettingsPaginated(this).map { it.customField }
    }

// Workspace extension functions

    fun Workspace.search(textQuery: String, vararg projectGids: String = arrayOf("")): List<Task> {
        return requestExecutor.workspaces.searchWorkspacePaginated(this, textQuery, *projectGids)
    }

    fun Workspace.getAllProjects(includeArchived: Boolean = true): Collection<Project> {
        return requestExecutor.workspaces.getProjectsPaginated(this, includeArchived)
    }

    fun Workspace.instantiateTemplate(projectGid: String, projectTitle: String, projectTeam: String): Job {
        return requestExecutor.workspaces.instantiateTemplate(projectGid, projectTitle, projectTeam)
    }

    fun Workspace.getCustomFields(): Collection<CustomField> {
        return requestExecutor.workspaces.getCustomFieldsPaginated(this)
    }

// Type conversion functions

    fun <R : AsanaSerializable<R>> Task.convertTo(toClass: KClass<R>): R {
        checkConfigFieldsForSerializing()
        return AsanaTaskSerializer(toClass, getContextFor(this))
            .deserialize(this)
    }

    fun <R : AsanaSerializable<R>> R.convertToTask(context: Resource): Task {
        checkConfigFieldsForSerializing()
        return AsanaTaskSerializer(this::class, getContextFor(context))
            .serialize(this)
    }

    fun <R : AsanaSerializable<R>> Project.convertTasksToListOf(toClass: KClass<R>): List<R> {
        checkConfigFieldsForSerializing()
        return this
            .getTasks()
            .convertToListOfWithContext(toClass, getContextFor(this))
    }

    fun <R : AsanaSerializable<R>> List<Task>.convertToListOfWithContext(
        toClass: KClass<R>,
        context: CustomFieldContext
    ): List<R> {
        checkConfigFieldsForSerializing()
        if (isEmpty()) return emptyList()
        val converter = AsanaTaskSerializer(toClass, context)
        return map { converter.deserialize(it) }.toList()
    }

    fun <R : AsanaSerializable<R>> List<R>.convertToTaskList(context: Resource): List<Task> {
        checkConfigFieldsForSerializing()
        if (isEmpty()) return emptyList()
        val converter = AsanaTaskSerializer(this[0]::class, getContextFor(context))
        return this.map { converter.serialize(it) }.toList()
    }

// Internal helper functions

    private fun getContextFor(resource: Resource): CustomFieldContext {
        val existingContext = contextFor(resource)
        if (existingContext != null) return existingContext

        val newContext = when (resource) {
            is Task      -> TaskCustomFieldContext(resource)
            is Project   -> ProjectCustomFieldContext(resource, this)
            is Workspace -> WorkspaceCustomFieldContext(resource, this)
            else         -> throw CustomFieldException("""
                The given resource [$resource] cannot be used to serialize tasks and data objects.
                You may only supply Task, Project, or Workspace objects.
            """.trimIndent())
        }
        contexts[resource.gid] = newContext

        return newContext
    }

    private fun contextFor(resource: Resource): CustomFieldContext? = contexts[resource.gid]

    private fun checkConfigFieldsForSerializing() {
        if ("custom_fields" !in config.fields) throw CustomFieldException("""
            
            Cannot serialize with the specified: AsanaConfig.fields=${config.fields.contentToString()}
            
            Hint: if you're serializing a data object into tasks (or vice-versa), you must specify at least the 
            "custom_fields" option when constructing your AsanaConfig object.
            
        """.trimIndent())
    }

}
